/*
 * Copyright (C) 2014 The CyouDown Project of Unicorn
 *
 * muzzyhorse
 */
package com.unicorn.downex.core;

import java.io.File;

/**
 * @author xuchunlei
 *
 */
public final class Constants {
    
    /**
     * 
     */
    private Constants() {
        
    }

    public static final String INVALID_STRING = "n/a";
    
    /**
     * 最大重定向次数
     */
    public static final int MAX_REDIRECTS = 5; //小于7
    /**
     * 缓冲区大小
     */
    public static final int BUFFER_SIZE = 4096;
    /**
     * 未下载完成的文件扩展名
     */
    public static final String DOWNLOAD_TEMP_FILE_SUFFIX = ".tmp";
    /**
     * 未知类型的文件扩展名
     */
    public static final String DOWNLOAD_UNKNOWN_FILE_SUFFIX = ".unknown";
    /**
     * 临时文件夹相对路径
     */
    public static final String DOWNLOAD_TEMP_DIRECTORY = File.separator.concat("temp");
    /** 到下次重试的最小时间间隔  */
    public static final int MIN_RETRY_AFTER = 10;           //10秒
    /** 到下次重试的最大时间间隔 */
    public static final int MAX_RETRY_AFTER = 1 * 60 * 60;  //1小时
    /**
     * 文件的MimeType
     * @author xu
     *
     */
    public static class MimeType {
    	
    	private MimeType() {
    		
    	}
    	/** Mime类型-apk */
    	public static final String APK = "application/vnd.android.package-archive";
    	
    	/** Mime类型-jpg */
    	public static final String JPG = "image/jpeg";
    }
    
    /**
     * 消息类型常量
     * @author xu
     *
     */
    public class Command {
    	private Command() {
    		
    	}
    	
    	/** 下载请求消息-开始 */
    	public static final int REQUEST_START = 1;
    	/** 下载请求消息-暂停 */
    	public static final int REQUEST_PAUSE = REQUEST_START + 1;
    	/** 下载请求消息-继续 */
    	public static final int REQUEST_RESUME = REQUEST_PAUSE + 1;
    	/** 下载请求消息-终止 */
    	public static final int REQUEST_STOP = REQUEST_RESUME + 1;
    	/** 下载请求消息-进度 */
    	public static final int REQUEST_PROGRESS = REQUEST_STOP + 1;
    	/** 下载请求消息-退出 */
    	public static final int REQUEST_EXIT = REQUEST_PROGRESS + 1;
    	/** 下载请求消息-查询 */
//    	public static final int REQUEST_QUERY = REQUEST_EXIT + 1;
    	
    	/** 下载响应消息-开始  *//*
    	public static final int RESPONSE_START = 100;
    	*//** 下载响应消息-暂停 *//*
    	public static final int RESPONSE_PAUSE = RESPONSE_START + 1;
    	*//** 下载响应消息-继续 *//*
    	public static final int RESPONSE_RESUME = RESPONSE_PAUSE + 1;
    	*//** 下载响应消息-终止 *//*
    	public static final int RESPONSE_STOP = RESPONSE_RESUME + 1;*/
    }
    
    /**
     * 下载状态常量
     * @author xuchunlei
     *
     */
    public static class Status {
        
        /** 挂起状态 */
        public static final int PENDING = 1;
        /** 运行状态 */
        public static final int RUNNING = PENDING + 1;
        /** 中断状态(用户操作) */
        public static final int INTERRUPTED = RUNNING + 1;
        /** 完成状态 */
        public static final int COMPLETED = INTERRUPTED + 1;
        /** 未知错误 */
        public static final int ERROR_UNKNOWN = 400;
        /** HTTP数据错误 */
        public static final int ERROR_HTTP_DATA = 401;
        /** 文件错误 */
        public static final int ERROR_FILE = 402;
        /** 请求失败错误 */
        public static final int ERROR_BAD_REQUEST = 403;
        /** 存储空间不足错误 */
        public static final int ERROR_STORAGE = 404;
        /** 请求资源不可达错误 */
        public static final int ERROR_HTTP_UNAVAILIABLE = 405;
        /** 无法继续下载错误（断点续传时，断点位置超过文件总大小） */
        public static final int ERROR_CANNOT_RESUME = 407;
        /** 重定向次数过多错误 */
        public static final int ERROR_TOO_MANY_REDIRECTS = 408;
        
        private Status() {
            
        }
        
        /**
         * 判断状态是否属于错误状态
         * @param status
         * @return
         */
        public static boolean isStatusError(int status) {
            return status >= 400 && status < 500;
        }
    }
}
