/*
 * Copyright (C) 2014 The CyouDown Project of Unicorn
 *
 * muzzyhorse
 */
package com.unicorn.downex.utils;

import android.text.TextUtils;
import android.util.Log;

import com.unicorn.downex.core.Constants;

import java.io.File;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件工具类
 * 提供操作文件的方法
 * @author xuchunlei
 *
 */
public final class FileUtil {
    
    private FileUtil(){
        
    }
    
    /**
     * 获取文件的键值
     * <p>
     * 可用于唯一标识文件
     * </p>
     * @param value
     * @return
     */
    public static String getFileKey(String value) {
        String key = null;
        if(value != null) {
        	byte[] buf = value.getBytes();
        	try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(buf);
		        byte [] tmp = md5.digest();
		        StringBuilder sb = new StringBuilder();
		        for (byte b:tmp) {
		            sb.append(Integer.toHexString(b&0xff));
		        }
		        key = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
        }
        return key;
    }
    
    /**
     * 合并目录和文件名，形成完整文件名
     * @param path
     * @param name
     * @return
     */
    public static String combine(String path, String name, String suffix) {
        if(path == null || name == null) {
            return null;
        }
        //验证路径合法性
        if(path.lastIndexOf(File.separator) != (path.length() -1)) { //最后一位不是"\"
            path = path.concat(File.separator);
        }
        
        //验证文件名合法性
        if(name.indexOf(File.separator) == 0) { //第一位是"\"
            name = name.substring(1);
        }
        if(suffix != null) {
            return path.concat(name).concat(suffix);
        }
        return path.concat(name);
    }
    
    /**
     * 查找目录下的文件
     * @param dir 目录
     * @param keyword 关键字
     * @return 文件集合
     */
    public static File[] filter(String dirName, final String keyword) {
        File dir = new File(dirName);
        File[] files = null;
        if(dir.exists()) {
            files = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    if(TextUtils.isEmpty(keyword)) {    //为空时，返回目录下所有文件
                        return true;
                    } else {
                        if (filename.contains(keyword)){//返回匹配文件名的文件
                            return true;
                        }else {
                            return false;
                        }
                    }
                }
            });
        }
        
        return files;
    }
    
    /**
     * 根据Mime类型获取文件扩展名
     * @param mime
     * @return 文件扩展名
     */
    public static String getSuffix(String mime) {
        String suffix = null;
        if(mime.equals(Constants.MimeType.APK)) {
            suffix = ".apk";
        }else if(mime.equals(Constants.MimeType.JPG)) {
            suffix = ".jpg";
        }else {
            suffix = Constants.DOWNLOAD_UNKNOWN_FILE_SUFFIX;
        }
        return suffix;
    }
}
