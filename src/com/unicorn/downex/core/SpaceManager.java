/*
 * Copyright (C) 2014 The DownEx Project of ChangYou
 *
 * 本文件涉及代码允许在畅游公司的所属项目中使用
 */
package com.unicorn.downex.core;

import java.io.File;

/**
 * 存储工具类
 * @author xuchunlei
 *
 */
public final class SpaceManager {

//    private String TAG = "SpaceManager";
    
    /**
     * 
     */
    public SpaceManager() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 验证存储空间
     * @param dir
     * @param requiredBytes
     * @throws StopException
     */
    public void verifySpace(String dir, long requiredBytes) throws  StopException {
        long remainSpace = getAvailiableSpace(dir) - requiredBytes;
        if(remainSpace <= 0) {
            throw new StopException(Constants.Status.ERROR_STORAGE, "Storage space is insufficient");
        }
    }
    
    //获取可用空间大小
    private long getAvailiableSpace(String rootName) {
        long space = 0;
        File root = new File(rootName);
        //保证可用空间大于总空间的10%
        space = root.getUsableSpace() - (root.getTotalSpace()/10);
        return space < 0 ? -1 : space;
    }
}
