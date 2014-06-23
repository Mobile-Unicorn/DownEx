/**
 * 
 */
package com.unicorn.downex.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.unicorn.downex.core.DownloadMessage.BooleanMessage;
import com.unicorn.downex.core.DownloadMessage.ProgressMessageList;
import com.unicorn.downex.core.DownloadMessage.StartReqMessage;
import com.unicorn.downex.core.DownloadMessage.StartResMessage;
import com.unicorn.downex.core.DownloadMessage.StringMessage;
import com.unicorn.downex.utils.FileUtil;

import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author xu
 *
 */
public final class Downloader {
	
	private static final String TAG = "Downloader";
	
	private static Downloader mInstance;
	
	private static String mName;
	
	private List<Handler> mHanlders;
	
	//消息流实例
	private MessageStreamer mStreamer;
	//定期查询下载进度进程
	private ScheduledExecutorService mMonitor;
	
	static {
	    mName = String.valueOf(Binder.getCallingUid());
	    //为下载器开启一个本地服务
        Executors.newSingleThreadExecutor().execute(new DownloadServerThread(mName));
	}
	
	private Downloader() {
		mStreamer = new MessageStreamer();
		mStreamer.open(mName);
		mHanlders = Collections.synchronizedList(new ArrayList<Handler>());
	}
	
	public static Downloader getInstance() {
		if(mInstance == null) {
			mInstance = new Downloader();
		}
		return mInstance;
	}
	
	/**
	 * 启动新的下载任务
	 * @param url 下载的url
	 * @param dir 保存下载文件的目录
	 * @return {@link StartResMessage}下载响应消息
	 */
	public BooleanMessage start(String url, String dir) {
		StartReqMessage reqMsg = new StartReqMessage();
		reqMsg.url = url;
		reqMsg.dir = dir;
		BooleanMessage resMsg = new BooleanMessage();
		try {
		    synchronized (mInstance) {
		        mStreamer.sendCommand(Constants.Command.REQUEST_START);
	            mStreamer.sendForResult(reqMsg, resMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to start download " + url);
        }
		return resMsg;		
	}
	
	/**
	 * 取消下载任务，将删除下载的文件
	 * @param key
	 * @return
	 */
	public StringMessage stop(String key) {
	    StringMessage resMsg = pause(key);
	    //删除文件
	    File[] files = FileUtil.filter(resMsg.value, key);
	    if(files.length == 1) {
	        files[0].delete();
	    }
        return resMsg;
	}
	/**
	 * 暂停正在下载的任务
	 * @param key
	 * @return
	 */
	public StringMessage pause(String key) {
	    StringMessage reqMsg = new StringMessage();
        reqMsg.value = key;
        StringMessage resMsg = new StringMessage();
        try {
            synchronized (mInstance) {
                mStreamer.sendCommand(Constants.Command.REQUEST_PAUSE);
                mStreamer.sendForResult(reqMsg, resMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resMsg;
	}
	/**
	 * 继续暂停的下载任务
	 * @param key
	 * @return
	 */
	public BooleanMessage resume(String url, String dir) {
	    return start(url, dir);
	}

	/**
	 * 开启定期轮询
	 * <p>
	 * 默认每隔一秒反馈当前系统中正在下载的任务进度信息
	 * </p>
	 * @param timespan 轮询时间间隔
	 */
	public void startMonitor(int timespan) {
	    if(mMonitor == null || mMonitor.isShutdown()) {
	    	mMonitor = Executors.newScheduledThreadPool(5);
	        //接收进度消息
	        final ProgressMessageList list = new ProgressMessageList();
	        final Runnable queryThread = new Runnable() {
				public void run() {
                    //同步发送接收消息
                    synchronized (mInstance) {
                        mStreamer.sendCommand(Constants.Command.REQUEST_PROGRESS);
                        try {
                            list.clear();
                            mStreamer.receive(list);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Failed to receive Download progress messages");
                        }
                    }
                    if(list.size() != 0) {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = list;
                        for(Handler handler : mHanlders) {
                            handler.sendMessage(msg);
                        }
                    }
				}
			};
	        
	        mMonitor.scheduleAtFixedRate(queryThread, timespan, timespan, TimeUnit.SECONDS);
	    }

	}

	/**
	 * 关闭监控
	 */
	public void stopMonitor() {
	    if(mMonitor != null) {
	        mMonitor.shutdownNow();
	        mMonitor = null;
	    }
	}
	
	/**
	 * 注册观察者
	 * @param watcher
	 */
	public void registerWatcher(Handler watcher) {
	    
	    if(!mHanlders.contains(watcher)) {
	        mHanlders.add(watcher);
	    }
	}
	
	/**
	 * 反注册观察者
	 * @param watcher
	 */
	public void unRegisterWatcher(Handler watcher) {
	    if(mHanlders.contains(watcher)) {
	        mHanlders.remove(watcher);
	    }
	}
	
	/**
	 * 释放连接
	 */
	public void realease() {
	    stopMonitor();
	    mStreamer.sendCommand(Constants.Command.REQUEST_EXIT);
        mStreamer.close();
        mInstance = null;
	}
}
