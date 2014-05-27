/**
 * 
 */
package com.unicorn.downex.core;

/**
 * @author xu
 *
 */
public class DownloadInfo {
	public String mUrl;
	public volatile int mStatus = Constants.Status.PENDING;
	public long mCurrentBytes;
	public long mTotalBytes;
	public String mEtag;
	public String mDir;
	public String mKey;
	public String mMime;
	public long mRetryAfter =  -1l;
}
