/**
 * 
 */

package com.unicorn.downex;

import com.unicorn.downex.utils.FileUtil;

/**
 * @author xuchunlei
 */
public final class DownloadItem {

    public static final int MODEL_STATUS_UNDOWNLOAD = 1;
    public static final int MODEL_STATUS_DOWNLOADING = MODEL_STATUS_UNDOWNLOAD + 1;
    public static final int MODEL_STATUS_DOWNLOADED = MODEL_STATUS_DOWNLOADING + 1;
    public static final int MODEL_STATUS_PAUSED = MODEL_STATUS_DOWNLOADED + 1;
    /*public static final int MODEL_STATUS_DOWNERROR = MODEL_STATUS_DOWNLOADED + 1;
     */

    public String title;
    public String url;
    public int status;
    public String key;
//    public String file;
//    public long currentSize;
//    public long totalSize;

    public DownloadItem(String title, int status, String url) {
        this.title = title;
        this.status = status;
        this.url = url;
        this.key = FileUtil.getFileKey(url);
    }
}
