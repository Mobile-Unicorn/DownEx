/**
 * 
 */
package com.unicorn.downex.widget;

import com.unicorn.downex.core.Downloader;
import com.unicorn.downex.core.DownloadMessage.BooleanMessage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * @author xu
 *
 */
public abstract class DownloadActivity extends Activity {
	
	protected String TAG;
	
	private Downloader mDownloader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = getClass().getSimpleName();
		
		mDownloader = Downloader.getInstance();
	}
	
	/**
	 * 开始下载指定url的资源
	 * @param url
	 */
	protected BooleanMessage startDownload(String url, String dir) {
		Log.i(TAG, "Start download " + url);
		return mDownloader.start(url, dir);
	}
	
	protected void stopDownload(String url) {
		Log.i(TAG, "Stop download " + url);
		mDownloader.stop(url);
	}
	/**
	 * 暂停下载任务
	 * @param key
	 */
	protected void pauseDownload(String key) {
		Log.i(TAG, "Pause download " + key);
		mDownloader.pause(key);
	}
	/**
	 * 继续下载任务
	 * @param key
	 */
	protected void resumeDownload(String url, String dir) {
		Log.i(TAG, "Resume download " + url);
		mDownloader.resume(url, dir);
	}
	
	/**
	 * 查询下载项信息
	 * @param dir
	 */
	/*protected void queryDownload(String dir, String...keys) {
	    mDownloader.query(dir, keys);
	}*/
	
	/**
	 * 开启下载过程监控
	 * @param timeSpan 间隔的秒数
	 */
	protected void startMonitorDownload(int timeSpan) {
	    mDownloader.startMonitor(timeSpan);
	}
	/**
	 * 关闭下载过程监控
	 */
	protected void stopMonitorDownload() {
		mDownloader.stopMonitor();
	}
	
	/**
	 * 获取处理下载进度的Handler
	 * @return
	 */
	protected abstract Handler getHandler();
}
