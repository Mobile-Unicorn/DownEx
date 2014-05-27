/*
 * Copyright (C) 2014 The DownEx Project of Unicorn
 *
 * muzzyhorse
 */
package com.unicorn.downex.core;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.unicorn.downex.core.DownloadMessage.IMessage;
import com.unicorn.downex.utils.IoUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * 消息流
 * <p>
 * 以长连接的方式提供发送和接受消息的功能
 * </p>
 * @author xuchunlei
 *
 */
final class MessageStreamer {

    private String TAG = "MessageStreamer";
    
    private LocalSocket mClient;
    private DataInputStream mIn;
    private DataOutputStream mOut;
    
    public MessageStreamer() {
        
    }
    /**
     * 
     */
    public MessageStreamer(LocalSocket client) {        
        try {
            mClient = client;
            mIn = new DataInputStream(client.getInputStream());
            mOut = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {            
            e.printStackTrace();
            close();
            Log.e(TAG, "Failed to init MessageStream");
        }
        
    }
    
    public void sendCommand(int cmd) {
        try {
            mOut.writeInt(cmd);
            mOut.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void sendForResult(IMessage reqMsg, IMessage resMsg) throws IOException {
//        synchronized (this) {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(mOut));
            JsonReader reader = new JsonReader(new InputStreamReader(mIn));
            reqMsg.send(writer);
            resMsg.receive(reader);
            
//        }
    }
    
    /**
     * 发送消息
     * @param reqMsg
     * @throws IOException 
     */
    public void send(IMessage reqMsg) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(mOut));
        reqMsg.send(writer);
    }
    /**
     * 接收消息
     * @param resMsg
     * @throws IOException 
     */
    public void receive(IMessage resMsg) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(mIn));
        resMsg.receive(reader);
    }
    
    /**
     * 打开消息流
     * @param name
     */
    public void open(String name) {
        if(mClient == null) {
            mClient = new LocalSocket();
            try {
                mClient.connect(new LocalSocketAddress(name));
                mIn = new DataInputStream(mClient.getInputStream());
                mOut = new DataOutputStream(mClient.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to open MessageStreamer for " + name);
                close();
            }
        }        
    }
    /**
     * 关闭消息流
     */
    public void close() {
        IoUtil.closeQuietly(mIn);
        IoUtil.closeQuietly(mOut);
        if(mClient != null) {
            try {
                mClient.close();
                mClient = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 消息流是否打开
     * @return
     */
    public boolean isOpened() {
        return mClient != null;
    }
}
