package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.ICatchWificamSession;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 14:51
 * @description
 */
@SuppressWarnings("all")
public class CameraAction {

    private ICatchWificamControl cameraControl;
    private ICatchWificamAssist cameraAssist;

    private CameraAction() {
        init();
    }

    public static CameraAction getInstance() {
        return CameraActionHolder.sInstance;
    }

    private static class CameraActionHolder {
        private static final CameraAction sInstance = new CameraAction();
    }

    public void init() {
        cameraControl = MyApplication.getMyCamera().getCameraControl();
        cameraAssist = MyApplication.getMyCamera().getCameraAssist();
    }

    public boolean capturePhoto() {
        return ExceptionHelper.invokeBool(cameraControl::capturePhoto);
    }

    public boolean triggerCapturePhoto() {
        return ExceptionHelper.invokeBool(cameraControl::triggerCapturePhoto);
    }

    public static boolean addGlobalEventListener(int iCatchEventId, ICatchWificamListener listener, Boolean forAllSession) {
        return ExceptionHelper.invokeBool(() -> ICatchWificamSession.addEventListener(iCatchEventId, listener, forAllSession));
    }

    public static boolean delGlobalEventListener(int iCatchEventId, ICatchWificamListener listener, Boolean forAllSession) {
        return ExceptionHelper.invokeBool(() -> ICatchWificamSession.delEventListener(iCatchEventId, listener, forAllSession));
    }

    public boolean addEventListener(int eventId, ICatchWificamListener listener) {
        return ExceptionHelper.invokeBool(() -> cameraControl.addEventListener(eventId, listener));
    }

    public boolean delEventListener(int eventId, ICatchWificamListener listener) {
        return ExceptionHelper.invokeBool(() -> cameraControl.delEventListener(eventId, listener));
    }

    public boolean addCustomEventListener(int eventId, ICatchWificamListener listener) {
        return ExceptionHelper.invokeBool(() -> cameraControl.addCustomEventListener(eventId, listener));
    }

    public boolean delCustomEventListener(int eventId, ICatchWificamListener listener) {
        return ExceptionHelper.invokeBool(() -> cameraControl.delCustomEventListener(eventId, listener));
    }
}
