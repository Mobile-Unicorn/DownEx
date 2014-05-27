/**
 * 
 */
package com.unicorn.downex.core;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.unicorn.downex.utils.FileUtil;
import com.unicorn.downex.utils.IoUtil;

/**
 * 下载线程
 * @author xu
 *
 */
public final class DownloadThread implements Runnable {

	private String TAG = "DownloadThread";
	
	private DownloadInfo mInfo;
	
	public DownloadThread(DownloadInfo info) {
		mInfo = info;
	}
	
	//http响应状态码-临时重定向需要再次向服务端请求相同的地址
	private static final int HTTP_TEMP_REDIRECT = 307;
	//http响应状态码-断点续传的Range参数没有覆盖当前请求的资源的长度
	private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	// 默认超时时间
	private static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);
	    
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	        checkInfo(mInfo);
	        URL url = null;
	        int finalStatus = Constants.Status.ERROR_UNKNOWN;
	        String errorMsg = null;
	        try {
	            try {
	                url = new URL(mInfo.mUrl);
	            } catch (MalformedURLException e) {
	                throw new StopException(Constants.Status.ERROR_BAD_REQUEST, e);
	            }
	            doDownload(url);
	            finalStatus = Constants.Status.COMPLETED;
	            finalizeFile(mInfo);
            } catch (StopException e) {
                errorMsg = e.getMessage();
                Log.w(TAG, "Aborting request for download " + mInfo.mKey + ": " + errorMsg);
                finalStatus = e.getFinalStatus();
            } finally {
                if(finalStatus == Constants.Status.ERROR_HTTP_DATA) {
                    mInfo.mRetryAfter = IoUtil.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1) * 1000;
                }
                mInfo.mStatus = finalStatus;
            }
	        
	}
	//执行下载操作
	private void doDownload(URL url) throws StopException {
        int redirectionCount = 0;
        while(redirectionCount++ < Constants.MAX_REDIRECTS) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);
                
                addRequestHeaders(conn, mInfo);
                
                final int responseCode = conn.getResponseCode();
                switch (responseCode) {
                    case HTTP_OK:       //首次下载
                    case HTTP_PARTIAL:  //断点续传
                        readResponseHeaders(conn, mInfo);
                        SpaceManager spaceManager = new SpaceManager();
                        spaceManager.verifySpace(mInfo.mDir, mInfo.mTotalBytes);
                        transferData(conn, mInfo);
                        return;
                    case HTTP_MOVED_PERM:   //处理重定向
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("Location");
                        url = new URL(location);
                        continue;
                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                        throw new StopException(
                                Constants.Status.ERROR_CANNOT_RESUME, "Requested range not satisfiable");
                    case HTTP_UNAVAILABLE:                      //该情况可以进行重试，服务器会反馈下次重试时间
                    case HTTP_INTERNAL_ERROR:
                        parseRetryAfterHeaders(conn, mInfo);
                        throw new StopException(Constants.Status.ERROR_HTTP_UNAVAILIABLE, conn.getResponseMessage());
                    default:
                        throw new StopException(Constants.Status.ERROR_UNKNOWN, conn.getResponseMessage());
                }
                
            } catch (IOException e) {
                throw new StopException(Constants.Status.ERROR_HTTP_DATA, e);
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
        }
	}
	
	//处理下载完成的文件
	private void finalizeFile(DownloadInfo info) {
	    File file = new File(getTempFileName(info));
	    if(file.exists()) {
	        File destFile = new File(FileUtil.combine(info.mDir, info.mKey, FileUtil.getSuffix(info.mMime)));
	        file.renameTo(destFile);
	    }
	}
	
	//检查下载项信息
	private void checkInfo(final DownloadInfo info) {
	    //在临时文件夹中查找文件
	    String dir = FileUtil.combine(info.mDir, Constants.DOWNLOAD_TEMP_DIRECTORY, null);
	    File[] files = FileUtil.filter(dir, info.mKey);
	    if(files != null) {
	        if(files.length == 1) {    //找到文件
	            if(files[0].exists()) {
	                String name = files[0].getName();
	                info.mCurrentBytes = files[0].length();
	                int start = name.indexOf("(");
	                int end = name.indexOf(")");
	                info.mEtag = name.substring(start + 1, end);
	                Log.e(TAG, "mEtag:" + info.mEtag);
	            }else{
	                try {
	                    files[0].createNewFile();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }else {    //没有找到或匹配的文件有多个
	            if(files.length != 0) {
	                for(File file : files) {
	                    file.delete();
	                }
	            }
	        }
	    }
	}
	
	//添加Http协议头
    private void addRequestHeaders(HttpURLConnection conn, DownloadInfo info) {
        //避免在传输压缩（gzip）文件时，获取的文件大小不正确的问题
        conn.setRequestProperty("Accept-Encoding", "identity");
        if(info.mCurrentBytes != 0) { //断点续传
            if(info.mEtag != null) {
                conn.addRequestProperty("If-Match", info.mEtag);
                conn.addRequestProperty("Range", "bytes=" + info.mCurrentBytes + "-");
            }
        }
    }
    
    //读取响应头参数
    private void readResponseHeaders(HttpURLConnection conn, DownloadInfo info){
    	if(info.mCurrentBytes != 0) {  //断点续传
    	    mInfo.mTotalBytes = mInfo.mCurrentBytes + conn.getContentLength();
    	}else {                        //初始下载
    	    info.mTotalBytes = conn.getContentLength();
    	    String etag = conn.getHeaderField("ETag");
            info.mEtag = etag.substring(1, etag.length() - 1);
        }
    	info.mMime = conn.getHeaderField("Content-Type");
    }
    
    //传输数据
    private void transferData(HttpURLConnection conn, DownloadInfo info) throws StopException {
        
    	InputStream in = null;
        OutputStream out = null;
        FileDescriptor outFd = null;
        
        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new StopException(Constants.Status.ERROR_HTTP_DATA, e);
        }
        
        try {
            //没有下载完成的文件放置在临时目录中
        	String fileName = getTempFileName(info);
			out = new FileOutputStream(fileName, true);
			outFd = ((FileOutputStream) out).getFD();
			doTransfer(info, in, out);
		} catch(IOException e) {
		    e.printStackTrace();
		    throw new StopException(Constants.Status.ERROR_FILE, e);
		} finally {
			IoUtil.closeQuietly(in);
            try {
                if (out != null) out.flush();
                if (outFd != null) outFd.sync();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IoUtil.closeQuietly(out);
            }
		}
    }

    private void doTransfer(DownloadInfo info, InputStream in, OutputStream out) throws StopException {
		final byte data[] = new byte[Constants.BUFFER_SIZE];
		try {
			int bytesread = 0;
			info.mStatus = Constants.Status.RUNNING;			
			while((info.mStatus == Constants.Status.RUNNING) && (bytesread = in.read(data)) != -1) {
				writeDataToFile(data, bytesread, out);
				info.mCurrentBytes += bytesread;
			}
			if(info.mStatus == Constants.Status.INTERRUPTED) { //下载被中断
			    throw new StopException(Constants.Status.INTERRUPTED, "User stop downloading " + info.mKey + " file");
			}
		} catch (IOException e) {
			throw new StopException(Constants.Status.ERROR_HTTP_DATA, e);
		}
    }
    
    private void writeDataToFile(byte[] data, int bytesRead, OutputStream out) throws StopException{
    	while(true) {
    		try {
				out.write(data, 0, bytesRead);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				throw new StopException(Constants.Status.ERROR_FILE, "Failed to write data: " + e);
			}
    	}
    }     
    
    //获得下载项的临时文件名
    private String getTempFileName(DownloadInfo info) {
        return FileUtil.combine(info.mDir.concat(Constants.DOWNLOAD_TEMP_DIRECTORY), info.mKey.concat("(").concat(info.mEtag).concat(")") , Constants.DOWNLOAD_TEMP_FILE_SUFFIX);
    }
    
    //解析http协议头获取重试间隔时间
    private void parseRetryAfterHeaders(HttpURLConnection conn, DownloadInfo info) {
        info.mRetryAfter = conn.getHeaderFieldInt("Retry-After", -1);
        if (info.mRetryAfter < 0) {
            info.mRetryAfter = 0;
        } else {
            if (info.mRetryAfter < Constants.MIN_RETRY_AFTER) {
                info.mRetryAfter = Constants.MIN_RETRY_AFTER;
            } else if (info.mRetryAfter > Constants.MAX_RETRY_AFTER) {
                info.mRetryAfter = Constants.MAX_RETRY_AFTER;
            }
            info.mRetryAfter += IoUtil.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1);
            info.mRetryAfter *= 1000;
        }
    }
}
