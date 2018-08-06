package com.example.pq.wificamerademo.util;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/4 14:14
 * @description
 */
public class FastClickUtils {
    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
