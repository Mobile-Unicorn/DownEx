/**
 * 
 */
package com.unicorn.downex.utils;

import android.os.SystemClock;

import java.io.Closeable;
import java.util.Random;

/**
 * 输入输出工具类
 * @author xu
 *
 */
public final class IoUtil {
    
	/** 全局的随机数生成器 */
    public static Random sRandom = new Random(SystemClock.uptimeMillis());
    
	private IoUtil() {
		
	}
	
	/**
     * 关闭实现{@link Closeable}接口的对象
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            	
            }
        }
    }
}
