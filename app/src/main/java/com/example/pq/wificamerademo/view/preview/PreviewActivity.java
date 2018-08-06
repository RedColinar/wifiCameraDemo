package com.example.pq.wificamerademo.view.preview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pq.wificamerademo.R;
import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.bean.StreamInfo;
import com.example.pq.wificamerademo.camera.MyCamera;
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
import com.example.pq.wificamerademo.widget.MPreview;
import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.type.ICatchH264StreamParam;
import com.icatch.wificam.customer.type.ICatchMJPGStreamParam;
import com.icatch.wificam.customer.type.ICatchPreviewMode;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.pq.wificamerademo.constants.PropertyId.CAPTURE_DELAY_MODE;

public class PreviewActivity extends AppCompatActivity {
    MPreview mPreview;
    Button capture;

    MyCamera myCamera;
    PreviewStream previewStream;

    private int curCacheTime;
    private int curMode = PreviewMode.NONE_MODE;

    private boolean supportStreaming = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mPreview = findViewById(R.id.m_preview);
        capture = findViewById(R.id.do_capture);
        capture.setOnClickListener(v -> startCapture());

        myCamera = MyApplication.getMyCamera();
        previewStream = PreviewStream.getInstance();

        initData();
        initPreview();
    }

    private void initData() {
        if (CameraProperties.getInstance().hasProperty(CAPTURE_DELAY_MODE)) {
            CameraProperties.getInstance().setCaptureDelayMode(1);
        }
    }

    private void initPreview() {
        // 改变 sdk 控制的相机模式
        previewStream.changePreviewMode(myCamera.getPreviewStream(), ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
        // 改变成员变量标志，并调用 sdk 的 startMediaStream
        changeCameraMode(PreviewMode.STILL_PREVIEW, ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
    }

    public void changeCameraMode(final int previewMode, final ICatchPreviewMode ichVideoPreviewMode) {

        Observable.fromCallable(() -> startMediaStream(ichVideoPreviewMode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(isNormal -> {
                    curMode = previewMode;
                    supportStreaming = isNormal;
                    if (isNormal) {
                        myCamera.isStreaming = true;
                        startMPreview(myCamera);
                    }
                })
                .subscribe(new BaseObserver<>());
    }

    private boolean startMediaStream(ICatchPreviewMode ichVideoPreviewMode) {
        String streamUrl = CameraProperties.getInstance().getCurrentStreamInfo();
        int cacheTime = CameraProperties.getInstance().getPreviewCacheTime();

        curCacheTime = cacheTime;

        if (streamUrl == null) {
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam();
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        }

        StreamInfo streamInfo = StreamInfo.convertToStreamInfo(streamUrl);
        GlobalInfo.curFps = streamInfo.fps;

        if (streamInfo.mediaCodecType.equals("MJPG")) {
            if (cacheTime > 0 && cacheTime < 200) {
                cacheTime = 200;
            }
            ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, 50);
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        } else if (streamInfo.mediaCodecType.equals("H264")) {
            if (cacheTime > 0 && cacheTime < 200) {
                cacheTime = 500;
            }

            ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
            curCacheTime = cacheTime;

            ICatchH264StreamParam param = new ICatchH264StreamParam(streamInfo.width, streamInfo.height, streamInfo.bitrate, streamInfo.fps);
            return previewStream.startMediaStream(myCamera.getPreviewStream(), param, ichVideoPreviewMode, AppInfo.disableAudio);
        }

        return false;
    }

    private void startCapture() {
        if (FastClickUtils.isFastClick()) return;

        if (curMode == PreviewMode.STILL_PREVIEW) {
            if (!CameraProperties.getInstance().isSDCardExist()) {
                Toast.makeText(this, "SD 卡未插入", Toast.LENGTH_SHORT).show();
                return;
            }
            int remainImageNum = CameraProperties.getInstance().getRemainImageNum();
            if (remainImageNum == 0) {
                Toast.makeText(this, "SD 卡已满", Toast.LENGTH_SHORT).show();
                return;
            } else if (remainImageNum < 0) {
                Toast.makeText(this, "SD 卡故障", Toast.LENGTH_SHORT).show();
                return;
            }
            curMode = PreviewMode.STILL_CAPTURE;
            startPhotoCapture();
        }
    }

    private void startPhotoCapture() {
        this.setCaptureBtnEnable(false);
        if (CameraProperties.getInstance().hasProperty(CAPTURE_DELAY_MODE)) {
            PhotoCapture.getInstance().addOnStopPreviewListener(new PhotoCapture.OnStopPreviewListener() {
                @Override
                public void onStop() {
                    if (!CameraProperties.getInstance().hasProperty(0xd704)) {
                        stopMPreview();
                        stopMediaStream();
                    }
                }
            });
            PhotoCapture.getInstance().startCapture();
        } else {
            if (!CameraProperties.getInstance().hasProperty(0xd704)) {
                stopMPreview();
                if (!stopMediaStream()) {
                    return;
                }
            }
            CameraAction.getInstance().capturePhoto();
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
}
