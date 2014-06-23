/*
 * Copyright (C) 2014 The DownEx Project of Unicorn
 *
 */
package com.unicorn.downex.utils;

import com.unicorn.downex.core.StopException;

/**
 * 预处理工具类
 * @author xuchunlei
 *
 */
public class Precondition {

    
    private Precondition() {
        
    }
    
    /**
     * 检查参数是否合法
     * @param condition
     * @throws StopException 
     */
    public static void checkArgument(boolean condition) throws Exception {
        if(!condition) {
            throw new Exception();
        }
    }

}
