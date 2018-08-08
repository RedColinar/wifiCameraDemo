package com.example.pq.wificamerademo.camera;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.pq.wificamerademo.bean.SearchedCameraInfo;
import com.example.pq.wificamerademo.camera.sdkApi.CameraAction;
import com.example.pq.wificamerademo.constants.AppInfo;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.type.ICatchEvent;
import com.icatch.wificam.customer.type.ICatchEventID;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 14:47
 * @description
 */
@SuppressWarnings("all")
public class SdkEvent {
    private static final String TAG = "SDKEvent";
    public static final int EVENT_BATTERY_ELETRIC_CHANGED = 0;
    public static final int EVENT_CAPTURE_COMPLETED = 1;
    public static final int EVENT_CAPTURE_START = 3;
    public static final int EVENT_SD_CARD_FULL = 4;
    public static final int EVENT_VIDEO_OFF = 5;
    public static final int EVENT_VIDEO_ON = 6;
    public static final int EVENT_FILE_ADDED = 7;
    public static final int EVENT_CONNECTION_FAILURE = 8;
    public static final int EVENT_TIME_LAPSE_STOP = 9;
    public static final int EVENT_SERVER_STREAM_ERROR = 10;
    public static final int EVENT_FILE_DOWNLOAD = 11;
    public static final int EVENT_VIDEO_RECORDING_TIME = 12;
    public static final int EVENT_FW_UPDATE_COMPLETED = 13;
    public static final int EVENT_FW_UPDATE_POWEROFF = 14;
    public static final int EVENT_SEARCHED_NEW_CAMERA = 15;
    public static final int EVENT_SDCARD_REMOVED = 16;
    public static final int EVENT_SDCARD_INSERT = 17;
    public static final int EVENT_FW_UPDATE_CHECK = 18;
    public static final int EVENT_FW_UPDATE_CHKSUMERR = 19;
    public static final int EVENT_FW_UPDATE_NG = 20;

    private CameraAction cameraAction = CameraAction.getInstance();
    private Handler handler;
    private SdcardStateListener sdcardStateListener;
    private BatteryStateListener batteryStateListener;
    private CaptureDoneListener captureDoneListener;
    private FileAddedListener fileAddedListener;

    private ConnectionFailureListener connectionFailureListener;
    private ServerStreamErrorListener serverStreamErrorListener;
    private FileDownloadListener fileDownloadListener;
    private UpdateFWCompletedListener updateFWCompletedListener;
    private UpdateFWPoweroffListener updateFWPoweroffListener;
    private NoSdcardListener noSdcardListener;
    private ScanCameraListener scanCameraListener;

    public SdkEvent(Handler handler) {
        this.handler = handler;
    }

    public void addEventListener(int iCatchEventID) {
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_FULL) {
            sdcardStateListener = new SdcardStateListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL, sdcardStateListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED) {
            batteryStateListener = new BatteryStateListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED, batteryStateListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE) {
            captureDoneListener = new CaptureDoneListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE, captureDoneListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_ADDED) {
            fileAddedListener = new FileAddedListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED, fileAddedListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR) {
            serverStreamErrorListener = new ServerStreamErrorListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR, serverStreamErrorListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_DOWNLOAD) {
            fileDownloadListener = new FileDownloadListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD, fileDownloadListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED) {
            updateFWCompletedListener = new UpdateFWCompletedListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED, updateFWCompletedListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF) {
            updateFWPoweroffListener = new UpdateFWPoweroffListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF, updateFWPoweroffListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_REMOVED) {
            noSdcardListener = new NoSdcardListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener);
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED) {
            connectionFailureListener = new ConnectionFailureListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener);
        }
    }

    public void addGlobalEventListener(int iCatchEventID, Boolean forAllSession) {
        switch (iCatchEventID) {
            case ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD:
                scanCameraListener = new ScanCameraListener();
                CameraAction.addGlobalEventListener(ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD, scanCameraListener, forAllSession);
                break;
            case ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED:
                connectionFailureListener = new ConnectionFailureListener();
                CameraAction.addGlobalEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener, forAllSession);
                break;
            case ICatchEventID.ICH_EVENT_SDCARD_REMOVED:
                noSdcardListener = new NoSdcardListener();
                CameraAction.addGlobalEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener, forAllSession);
                break;
        }
    }

    public void delGlobalEventListener(int iCatchEventID, Boolean forAllSession) {
        switch (iCatchEventID) {
            case ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD:
                if (scanCameraListener != null) {
                    CameraAction.delGlobalEventListener(ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD, scanCameraListener, forAllSession);
                }
                break;
            case ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED:
                if (connectionFailureListener != null) {
                    CameraAction.delGlobalEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener, forAllSession);
                }
                break;
            case ICatchEventID.ICH_EVENT_SDCARD_REMOVED:
                if (noSdcardListener != null) {
                    CameraAction.delGlobalEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener, forAllSession);
                }
                break;
        }
    }

    public void delEventListener(int iCatchEventID) {
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_FULL && sdcardStateListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL, sdcardStateListener);
            sdcardStateListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED && batteryStateListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED, batteryStateListener);
            batteryStateListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE && captureDoneListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE, captureDoneListener);
            captureDoneListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_ADDED && fileAddedListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED, fileAddedListener);
            fileAddedListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR && serverStreamErrorListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR, serverStreamErrorListener);
            serverStreamErrorListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_DOWNLOAD && fileDownloadListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD, fileDownloadListener);
            fileDownloadListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED && updateFWCompletedListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED, updateFWCompletedListener);
            updateFWCompletedListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF && updateFWPoweroffListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF, updateFWPoweroffListener);
            updateFWPoweroffListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_REMOVED && noSdcardListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener);
            noSdcardListener = null;
        } else if (iCatchEventID == ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED && connectionFailureListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener);
            connectionFailureListener = null;
        }
    }

    public class SdcardStateListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            handler.obtainMessage(EVENT_SD_CARD_FULL).sendToTarget();
        }
    }

    public class BatteryStateListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            handler.obtainMessage(EVENT_BATTERY_ELETRIC_CHANGED).sendToTarget();
        }
    }

    public class CaptureDoneListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive event:capture done");
            handler.obtainMessage(EVENT_CAPTURE_COMPLETED).sendToTarget();
        }
    }

    public class FileAddedListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            if (arg0 != null && arg0.getFileValue1() != null) {
                Log.i(TAG, "--------------receive fileValue " + arg0.getFileValue1().toString());
                Message message = handler.obtainMessage(EVENT_FILE_ADDED, arg0.getFileValue1());
                handler.handleMessage(message);
            }
        }
    }

    public class ConnectionFailureListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive event:ConnectionFailureListener");
            handler.obtainMessage(EVENT_CONNECTION_FAILURE).sendToTarget();
        }
    }

    public class ServerStreamErrorListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive event:ServerStreamErrorListener");
            handler.obtainMessage(EVENT_SERVER_STREAM_ERROR).sendToTarget();
        }
    }

    public class FileDownloadListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive event:FileDownloadListener");
            Log.d("1111", "receive event:FileDownloadListener");
            handler.obtainMessage(EVENT_FILE_DOWNLOAD, arg0.getFileValue1()).sendToTarget();
        }
    }

    public class UpdateFWCompletedListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive UpdateFWCompletedListener");
            handler.obtainMessage(EVENT_FW_UPDATE_COMPLETED).sendToTarget();
        }
    }

    public class UpdateFWPoweroffListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive UpdateFWPoweroffListener");
            handler.obtainMessage(EVENT_FW_UPDATE_POWEROFF).sendToTarget();
        }
    }

    public class NoSdcardListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive NoSdcardListener");
            AppInfo.isSdCardExist = false;
            handler.obtainMessage(EVENT_SDCARD_REMOVED).sendToTarget();
            Log.i(TAG, "receive NoSdcardListener GlobalInfo.isSdCard = " + AppInfo.isSdCardExist);
        }
    }

    public class ScanCameraListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "Send..........EVENT_SEARCHED_NEW_CAMERA");
            Log.d("1111", "get a uid arg0.getgetStringValue3() ==" + arg0.getStringValue3());
            handler.obtainMessage(EVENT_SEARCHED_NEW_CAMERA,
                    new SearchedCameraInfo(arg0.getStringValue2(), arg0.getStringValue1(), arg0.getIntValue1(), arg0.getStringValue3()))
                    .sendToTarget();
        }
    }

    public class InsertSdcardListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {
            Log.i(TAG, "--------------receive InsertSdcardListener");
            AppInfo.isSdCardExist = true;
            handler.obtainMessage(EVENT_SDCARD_INSERT).sendToTarget();
            Log.i(TAG, "receive InsertSdcardListener GlobalInfo.isSdCard = " + AppInfo.isSdCardExist);
        }
    }
}
