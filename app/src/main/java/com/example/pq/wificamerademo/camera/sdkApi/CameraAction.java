package com.example.pq.wificamerademo.camera.sdkApi;

import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.ExceptionHelper;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchCaptureImageException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchListenerExistsException;
import com.icatch.wificam.customer.exception.IchSocketException;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 14:51
 * @description
 */
public class CameraAction {

    private ICatchWificamControl cameraControl;
    private ICatchWificamAssist cameraAssist;

    private CameraAction() {
        cameraControl = MyApplication.getMyCamera().getCameraControl();
        cameraAssist = MyApplication.getMyCamera().getCameraAssist();
    }

    public static CameraAction getInstance() {
        return CameraActionHolder.sInstance;
    }

    private static class CameraActionHolder {
        private static final CameraAction sInstance = new CameraAction();
    }
    public boolean capturePhoto() {
        return ExceptionHelper.invokeBool(cameraControl::capturePhoto);
    }

    public boolean triggerCapturePhoto() {
        return ExceptionHelper.invokeBool(cameraControl::triggerCapturePhoto);
    }

    public static boolean addGlobalEventListener(int iCatchEventID, ICatchWificamListener listener, Boolean forAllSession) {
        boolean retValue = false;
        try {
            retValue = ICatchWificamSession.addEventListener(iCatchEventID, listener,forAllSession);
        } catch (IchListenerExistsException e) {
            e.printStackTrace();
        }
        return retValue;
    }



}
