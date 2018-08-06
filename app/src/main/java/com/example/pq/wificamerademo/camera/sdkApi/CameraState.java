package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.icatch.wificam.customer.ICatchWificamState;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 17:59
 * @description
 */
public class CameraState {
    private ICatchWificamState cameraState;

    private CameraState() {
        cameraState = MyApplication.getMyCamera().getCameraState();
    }

    private static class CameraStateHolder {
        private static final CameraState sInstance = new CameraState();
    }

    public static CameraState getInstance() {
        return CameraStateHolder.sInstance;
    }

    public boolean isTimeLapseStillOn() {
        return ExceptionHelper.invokeBool(cameraState::isTimeLapseStillOn);
    }

    public boolean isSupportImageAutoDownload() {
        return ExceptionHelper.invokeBool(cameraState::supportImageAutoDownload);
    }
}
