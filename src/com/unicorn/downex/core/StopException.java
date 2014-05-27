/*
 * Copyright (C) 2014 The DownEx Project of ChangYou
 *
 * 本文件涉及代码允许在畅游公司的所属项目中使用
 */
package com.unicorn.downex.core;

/**
 * 停止异常
 * <p>
 * 用于描述下载过程停止的异常情况
 * </p>
 * @author xuchunlei
 *
 */
@SuppressWarnings("serial")
public final class StopException extends Exception {

    //最终状态
    private final int mFinalStatus;
    
    /**
     * 
     */
    public StopException(int finalStatus, String message) {
        super(message);
        mFinalStatus = finalStatus;
    }

    /**
     * @param throwable
     */
    public StopException(int finalStatus, Throwable throwable) {
        super(throwable);
        mFinalStatus = finalStatus;
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public StopException(int finalStatus, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        mFinalStatus = finalStatus;
    }
    
    /**
     * 获得导致停止的最终状态
     * @return
     */
    public int getFinalStatus() {
        return mFinalStatus;
    }

}
