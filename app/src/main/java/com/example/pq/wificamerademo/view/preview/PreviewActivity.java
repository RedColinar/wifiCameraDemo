package com.example.pq.wificamerademo.view.preview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pq.wificamerademo.R;
import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.bean.StreamInfo;
import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.camera.SdkEvent;
import com.example.pq.wificamerademo.camera.sdkApi.CameraAction;
import com.example.pq.wificamerademo.camera.sdkApi.CameraProperties;
import com.example.pq.wificamerademo.camera.sdkApi.PreviewStream;
import com.example.pq.wificamerademo.constants.AppInfo;
import com.example.pq.wificamerademo.constants.GlobalInfo;
import com.example.pq.wificamerademo.constants.PreviewMode;
import com.example.pq.wificamerademo.constants.PropertyId;
import com.example.pq.wificamerademo.function.PhotoCapture;
import com.example.pq.wificamerademo.rx.BaseObserver;
import com.example.pq.wificamerademo.util.FastClickUtils;
import com.example.pq.wificamerademo.view.lookback.LookBackActivity;
import com.example.pq.wificamerademo.widget.MPreview;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.type.ICatchEventID;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;
import com.icatch.wificam.customer.type.ICatchH264StreamParam;
import com.icatch.wificam.customer.type.ICatchMJPGStreamParam;
import com.icatch.wificam.customer.type.ICatchPreviewMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.pq.wificamerademo.constants.PropertyId.CAPTURE_DELAY_MODE;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";
    MPreview mPreview;
    Button capture;
    Button continuePhoto;
    Button albums;

    MyCamera myCamera;
    PreviewStream previewStream;
    VrPanoramaView vrView;
    ImageView ivPreview;

    SdkEvent sdkEvent;

    List<Integer> eventId = new ArrayList<>();

    private int curCacheTime;
    private int curMode = PreviewMode.NONE_MODE;

    private boolean supportStreaming = true;
    private PreviewHandler previewHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mPreview = findViewById(R.id.m_preview);
        vrView = findViewById(R.id.pano_view);
        ivPreview = findViewById(R.id.iv_preview);
        capture = findViewById(R.id.do_capture);
        continuePhoto = findViewById(R.id.continue_photo);
        albums = findViewById(R.id.albums);

        capture.setOnClickListener(v -> startCapture());
        continuePhoto.setOnClickListener(v -> initPreview());
        albums.setOnClickListener(v -> {
            stopMPreview();
            stopMediaStream();
            deleteEvent();

            ICatchFile iCatchFile = new ICatchFile(10);
            LookBackActivity.startActivity(PreviewActivity.this, iCatchFile);
        });

        myCamera = MyApplication.getMyCamera();
        previewStream = PreviewStream.getInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initEvent();
        initPreview();
    }

    private void initData() {
        previewHandler = new PreviewHandler();

        if (CameraProperties.getInstance().hasProperty(0xD7F0)) {
            CameraProperties.getInstance().setCaptureDelayMode(1);
        }
    }

    private void initEvent() {
        sdkEvent = new SdkEvent(previewHandler);
        eventId.add(ICatchEventID.ICH_EVENT_SDCARD_FULL);
        eventId.add(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED);
        eventId.add(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE);
        eventId.add(ICatchEventID.ICH_EVENT_FILE_ADDED);
        eventId.add(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD);
        for (int id : eventId) {
            sdkEvent.addEventListener(id);
        }
    }

    private void initPreview() {
        // 改变 sdk 控制的相机模式
        if (curMode == PreviewMode.NONE_MODE) {
            previewStream.changePreviewMode(myCamera.getPreviewStream(), ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
        }
        // 改变成员变量标志，并调用 sdk 的 startMediaStream
        changeCameraMode(PreviewMode.STILL_PREVIEW, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
    }

    public void changeCameraMode(final int cameraMode, final ICatchPreviewMode ichVideoPreviewMode) {
        if (myCamera.isStreaming) return;
        Observable.fromCallable(() -> {
            if (cameraMode == PreviewMode.STILL_PREVIEW || cameraMode == PreviewMode.STILL_CAPTURE) {
                CameraProperties.getInstance().getRemainImageNum();
            }
            // startMediaStream 可能不成功，重复3次
            int i = 0;
            boolean isSuccess = false;
            while (i < 2 && !isSuccess) {
                isSuccess = startMediaStream(ichVideoPreviewMode);
                i++;
            }
            if (isSuccess) {
                myCamera.isStreaming = true;
            }
            return isSuccess;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(isNormal -> {
                    if (isNormal) {
                        curMode = cameraMode;
                        supportStreaming = true;
                        startMPreview(myCamera);
                    }
                })
                .subscribe(new BaseObserver<>());
    }

    private void deleteEvent() {
        for (int id : eventId) {
            sdkEvent.delEventListener(id);
        }
        eventId.clear();
    }

    private boolean startMediaStream(ICatchPreviewMode ichVideoPreviewMode) {
        String streamUrl = CameraProperties.getInstance().getCurrentStreamInfo();

        curCacheTime = CameraProperties.getInstance().getPreviewCacheTime();

        if (streamUrl == null) {
            if (curCacheTime > 0 && curCacheTime < 200) {
                curCacheTime = 200;
            }
            ICatchWificamConfig.getInstance().setPreviewCacheParam(curCacheTime, 200);
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam();
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        }

        StreamInfo streamInfo = StreamInfo.convertToStreamInfo(streamUrl);
        GlobalInfo.curFps = streamInfo.fps;

        if (streamInfo.mediaCodecType.equals("MJPG")) {
            if (curCacheTime > 0 && curCacheTime < 200) {
                curCacheTime = 200;
            }
            ICatchWificamConfig.getInstance().setPreviewCacheParam(curCacheTime, 200);
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, 50);
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        } else if (streamInfo.mediaCodecType.equals("H264")) {
            if (curCacheTime > 0 && curCacheTime < 200) {
                curCacheTime = 500;
            }
            ICatchWificamConfig.getInstance().setPreviewCacheParam(curCacheTime, 200);
            ICatchH264StreamParam param = new ICatchH264StreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, streamInfo.fps);
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        }
        return false;
    }

    /** 拍照時不需要停止 Streaming, 這點 APP 可以透過 Property 0xD704 判斷，
     *  有則是support 快速拍照，沒有則必須要停止stream 後才能拍照，
     *  拍完後再 start streaming.拍完收到FW event 告訴APP 檔案path,
     *  APP 就可以透過 download  API 去下載檔案
     */
    private void startCapture() {
        if (FastClickUtils.isFastClick()) return;

        if (curMode == PreviewMode.STILL_PREVIEW) {
            int remainImageNum = CameraProperties.getInstance().getRemainImageNum();
            if (!CameraProperties.getInstance().isSDCardExist()) {
                Toast.makeText(this, "SD 卡未插入", Toast.LENGTH_SHORT).show();
                return;
            } else if (remainImageNum == 0) {
                Toast.makeText(this, "SD 卡已满", Toast.LENGTH_SHORT).show();
                return;
            } else if (remainImageNum < 0) {
                Toast.makeText(this, "SD 卡故障", Toast.LENGTH_SHORT).show();
                return;
            }
            curMode = PreviewMode.STILL_CAPTURE;
            this.setCaptureBtnEnable(false);
            if (CameraProperties.getInstance().hasProperty(CAPTURE_DELAY_MODE)) {
                PhotoCapture.getInstance().addOnStopPreviewListener(() -> {
                    if (!CameraProperties.getInstance().hasProperty(0xd704)) {
                        stopMPreview();
                        stopMediaStream();
                    }
                });
                PhotoCapture.getInstance().startCapture();
            } else {
                // 相机不支持 快速拍照
//            if (!CameraProperties.getInstance().hasProperty(0xd704)) {
//            }
                stopMPreview();
                stopMediaStream();
                // CameraAction.getInstance().capturePhoto();
                Observable.fromCallable(() -> {
                    CameraAction.getInstance().capturePhoto();
                    // changeCameraMode(PreviewMode.STILL_PREVIEW, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                    return true;
                })
                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnNext(bool -> {
//                            changeCameraMode(PreviewMode.STILL_PREVIEW, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
//                        })
                        .subscribe(new BaseObserver<>());
            }
        }
    }

    public boolean stopMediaStream() {
        if (!myCamera.isStreaming) {
            Log.d("", "stopMediaStream current Streaming has stopped,do not need to stop again!");
            return true;
        }
        myCamera.isStreaming = false;
        return previewStream.stopMediaStream(myCamera.getPreviewStream());
    }

    public void startMPreview(MyCamera myCamera) {
        mPreview.setVisibility(View.VISIBLE);
        mPreview.start(myCamera, MPreview.RT_PREVIEW_MODE);
    }

    public void stopMPreview() {
        mPreview.stop();
    }

    public void setOnDecodeTimeListener(MPreview.OnDecodeTimeListener onDecodeTimeListener) {
        mPreview.setOnDecodeTimeListener(onDecodeTimeListener);
    }

    public void setCaptureBtnEnable(boolean enable) {
        capture.setEnabled(enable);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PreviewActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        stopMPreview();
        stopMediaStream();
        deleteEvent();
        myCamera.destroyCamera();
        super.onDestroy();
    }

    private class PreviewHandler extends Handler {
        String filePath;
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case SdkEvent.EVENT_BATTERY_ELETRIC_CHANGED:
                    Log.i(TAG, "receive EVENT_BATTERY_ELETRIC_CHANGED power =" + msg.arg1);
                    //need to update battery eletric
                    break;
                case SdkEvent.EVENT_SD_CARD_FULL:
                    Log.i(TAG, "receive EVENT_SD_CARD_FULL");
                    sdkEvent.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);
                    break;
                case SdkEvent.EVENT_CAPTURE_COMPLETED:
                    Log.i(TAG, "receive EVENT_CAPTURE_COMPLETED:" + msg.arg1 + msg.arg2);
                    if (curMode == PreviewMode.STILL_CAPTURE) {
                        //ret = changeCameraMode(PreviewMode.APP_STATE_STILL_MODE, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                        if (!CameraProperties.getInstance().hasProperty(0xd704)) {
                            boolean ret = startMediaStream(ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                            if (!ret) {
                                return;
                            }
                            myCamera.isStreaming = true;
                            startMPreview(myCamera);
                        }
                        curMode = PreviewMode.STILL_PREVIEW;
                    }
                    filePath = CameraProperties.getInstance().getCurrentPropertyStringValue(PropertyId.GET_FILENAME_PATH);
                    filePath = filePath.replace("D:", "").replace("\\", "/");
                    //LookBackActivity.startActivity(PreviewActivity.this, (ICatchFile) msg.obj);
                    setCaptureBtnEnable(true);
                    Toast.makeText(PreviewActivity.this, "拍照成功", Toast.LENGTH_SHORT).show();
                    break;
                case SdkEvent.EVENT_FILE_ADDED:
//                    stopMPreview();
//                    stopMediaStream();
                    // deleteEvent();
                    ICatchFile old = (ICatchFile) msg.obj;
                    ICatchFile iCatchFile = new ICatchFile(old.getFileHandle(), ICatchFileType.ICH_TYPE_IMAGE, filePath, old.getFileName(), old.getFileSize());
                    LookBackActivity.startActivity(PreviewActivity.this, iCatchFile);

                    // postDelayed(() -> LookBackActivity.startActivity(PreviewActivity.this, (ICatchFile) msg.obj), 1000);
                    break;
                case SdkEvent.EVENT_FILE_DOWNLOAD:
                    Log.d(TAG, "receive EVENT_FILE_DOWNLOAD  msg.arg1 =" + msg.arg1);
                    if (!AppInfo.autoDownloadAllow) {
                        Log.d(TAG, "GlobalInfo.autoDownload == false");
                        return;
                    }
                    break;
                case SdkEvent.EVENT_SDCARD_INSERT:
                    Log.i(TAG, "receive EVENT_SDCARD_INSERT");
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
