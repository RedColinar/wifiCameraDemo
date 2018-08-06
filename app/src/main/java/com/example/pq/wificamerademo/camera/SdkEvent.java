package com.example.pq.wificamerademo.camera;

import com.example.pq.wificamerademo.camera.sdkApi.CameraAction;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.type.ICatchEvent;
import com.icatch.wificam.customer.type.ICatchEventID;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 14:47
 * @description
 */
public class SdkEvent {

    public void addGlobalEventListener(int iCatchEventID, Boolean forAllSession) {
        switch (iCatchEventID) {

            case ICatchEventID.ICH_EVENT_SDCARD_REMOVED:
                NoSdcardListener noSdcardListener = new NoSdcardListener();
                CameraAction.addGlobalEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener, forAllSession);
                break;
        }
    }

    public class NoSdcardListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {}
    }
}
