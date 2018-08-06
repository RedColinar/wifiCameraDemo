package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.example.pq.wificamerademo.camera.ISdkSession;
import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 11:29
 * @description
 */
public class CameraSession implements ISdkSession {

    private ICatchWificamSession cameraSession;
    private boolean sessionPrepared;

    private final String defaultIp = "192.168.1.1";
    private final String username = "anonymous";
    private final String password = "anonymous@icatchtek.com";

    @Override
    public boolean prepareSession() {
        return prepareSession(defaultIp);
    }

    @Override
    public boolean prepareSession(String ip) {
        ICatchWificamConfig.getInstance().enablePTPIP();
        cameraSession = new ICatchWificamSession();
        sessionPrepared = ExceptionHelper.invokeBool(() -> cameraSession.prepareSession(ip, username, password));
        return sessionPrepared;
    }

    @Override
    public boolean checkWifiConnection() {
        boolean retValue = false;
        try {
            retValue = cameraSession.checkConnection();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    @Override
    public boolean destroySession() {
        return ExceptionHelper.invokeBool(cameraSession::destroySession);
    }

    public ICatchWificamSession getCameraSession() {
        return cameraSession;
    }

    public boolean isSessionPrepared() {
        return sessionPrepared;
    }
}
