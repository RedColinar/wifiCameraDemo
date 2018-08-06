package com.example.pq.wificamerademo.camera;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 12:37
 * @description
 */
public interface ISdkSession {

    boolean prepareSession();
    boolean prepareSession(String ip);
    boolean checkWifiConnection();
    boolean destroySession();
}
