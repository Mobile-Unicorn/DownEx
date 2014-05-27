/*
 * Copyright (C) 2014 The DownEx Project of Unicorn
 *
 * muzzyhorse
 */
package com.unicorn.downex.core;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

import com.unicorn.downex.core.DownloadMessage.BooleanMessage;
import com.unicorn.downex.core.DownloadMessage.ProgressMessage;
import com.unicorn.downex.core.DownloadMessage.ProgressMessageList;
import com.unicorn.downex.core.DownloadMessage.StartReqMessage;
import com.unicorn.downex.core.DownloadMessage.StringMessage;
import com.unicorn.downex.utils.FileUtil;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端线程
 * 用于处理各类下载请求
 * @author xuchunlei
 *
 */
class DownloadServerThread implements Runnable {

    private final String TAG = "DownloadServerThread";
    
    private static final int DEFAULT_THREAD_SIZE = 5;
    
    private String mName;
    
    private ConcurrentHashMap<String, DownloadInfo> mInfos;
    
    private ExecutorService mThreadTool = Executors.newFixedThreadPool(DEFAULT_THREAD_SIZE);
    
    private LocalServerSocket mServer;
    
    public DownloadServerThread(String name) {
        mName = name;
        mInfos = new ConcurrentHashMap<String, DownloadInfo>();
        try {
            mServer = new LocalServerSocket(mName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mRunning = false;
        }
    }
    
    private boolean mRunning = true;
    
    @Override
    public void run() {
        try {
            while(mRunning) {
                LocalSocket client = mServer.accept();
                //开启连接线程处理客户端各类请求
                
                ConnectThread t = new ConnectThread(client);
                Executors.newSingleThreadExecutor().execute(t);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 连接线程
     * @author xuchunlei
     *
     */
    class ConnectThread implements Runnable {

        private LocalSocket mClient;
        private DataInputStream mIn;
        private MessageStreamer mStreamer;
        private boolean mRunFlag;
        
        public ConnectThread(LocalSocket client) {
            mClient = client;
            mStreamer = new MessageStreamer(client);
            mRunFlag = true;
            
            try {
                mIn = new DataInputStream(mClient.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to create ConnectThread!");
                mRunFlag = false;
            }
        }
        
        @Override
        public void run() {
            //创建消息实例           
            ProgressMessageList plMsg = new ProgressMessageList();
            BooleanMessage boolMsg = new BooleanMessage();
            StartReqMessage startReq = new StartReqMessage();
            StringMessage strMsg = new StringMessage();
            
            DownloadInfo info = null;
            DownloadThread thread = null;
            
            while(mRunFlag) {
                try {
                    //接收指令消息
                    int cmd = mIn.readInt();
                    switch (cmd) {
                        case Constants.Command.REQUEST_START:
                            //接收请求消息
                            mStreamer.receive(startReq);
                            //处理消息
                            String key = FileUtil.getFileKey(startReq.url);
                            if(!mInfos.containsKey(key)) {  //未下载，开启下载
                                info = new DownloadInfo();
                                info.mUrl = startReq.url;
                                info.mDir = startReq.dir;
                                info.mKey = key;
                                initDir(info.mDir.concat(Constants.DOWNLOAD_TEMP_DIRECTORY));
                                thread = new DownloadThread(info);
                                mThreadTool.execute(thread);
                                boolMsg.value = true;
                                mInfos.put(info.mKey, info);
                            }else {                         //下载中, 不需要开启
                                boolMsg.value = false;
                            }
                            //发送响应消息
                            mStreamer.send(boolMsg);
                            break;
                        case Constants.Command.REQUEST_PROGRESS:
                            plMsg.clear();
                            for(Entry<String, DownloadInfo> entry : mInfos.entrySet()) {
                                info = entry.getValue();
                                if(info.mStatus == Constants.Status.RUNNING
                                   || Constants.Status.isStatusError(info.mStatus)
                                   || info.mStatus == Constants.Status.COMPLETED) {
                                    ProgressMessage msg = new ProgressMessage();
                                    msg.key = entry.getKey();
                                    msg.currentBytes = entry.getValue().mCurrentBytes;
                                    msg.totalBytes = entry.getValue().mTotalBytes;
                                    msg.status = info.mStatus;
                                    plMsg.add(msg);
                                } 
                                
                                if(info.mStatus != Constants.Status.PENDING
                                   && info.mStatus != Constants.Status.RUNNING) {
                                    mInfos.remove(entry.getKey());
                                }
                            }
                            Log.i(TAG, "ConnectThread is RUNNING! Download itmes'count is " + plMsg.size());
                            mStreamer.send(plMsg);
                            break;
                        case Constants.Command.REQUEST_PAUSE:
                            //接收请求消息
                            mStreamer.receive(strMsg);
                            //处理消息
                            info = mInfos.get(strMsg.value);
                            if(info != null) {
                                info.mStatus = Constants.Status.INTERRUPTED;
                                strMsg.value = info.mDir.concat(Constants.DOWNLOAD_TEMP_DIRECTORY); //临时下载文件夹路径
                            }else {
                                strMsg.value = Constants.INVALID_STRING;
                            }
                            //发送响应消息
                            mStreamer.send(strMsg);
                            break;
                        case Constants.Command.REQUEST_EXIT:
                            mRunFlag = false;
                            Log.i(TAG, "Terminate download thread");
                            continue;
                        default:
                            break;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            if(mStreamer.isOpened()) {
                mStreamer.close();
            }
        }
        
    }
    
    private void initDir(String dirName) {
        File dir = new File(dirName);
        if(!dir.exists()) {
            dir.mkdirs();
        }
    }
    
}
