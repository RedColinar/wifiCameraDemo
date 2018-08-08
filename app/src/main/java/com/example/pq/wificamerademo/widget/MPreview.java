package com.example.pq.wificamerademo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.camera.sdkApi.PreviewStream;
import com.example.pq.wificamerademo.decodeThead.H264DecoderThread;
import com.example.pq.wificamerademo.decodeThead.MjpgDecoderThread;
import com.icatch.wificam.customer.type.ICatchCodec;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 15:47
 * @description ICH_CODEC_H264 视频编码格式，ICH_CODEC_RGBA_8888 图片编码格式
 */
public class MPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static final int VIDEO_PB_MODE = 1;
    public static final int RT_PREVIEW_MODE = 2;
    private int previewLaunchMode;

    private SurfaceHolder mSurfaceHolder;
    private boolean mIsDrawing;
    private boolean needStart;

    private int frmW = 0;
    private int frmH = 0;

    private MyCamera myCamera;
    private MjpgDecoderThread mjpgDecoderThread;
    private H264DecoderThread h264DecoderThread;
    private PreviewStream previewStream = PreviewStream.getInstance();
    private ICatchVideoFormat videoFormat;
    private int previewCodec;

    private VideoFramePtsChangedListener videoPbUpdateBarListener;
    private OnDecodeTimeListener onDecodeTimeListener;

    public interface VideoFramePtsChangedListener {
        void onFramePtsChanged(double pts);
    }

    public interface OnDecodeTimeListener {
        void decodeTime(long time);
    }

    public MPreview(Context context) {
        this(context, null);
    }

    public MPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        setKeepScreenOn(true);
        // setFocusable(true);
        // setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        startDecoderThread(RT_PREVIEW_MODE, videoFormat);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
        stop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getHandler().post(this::setSurfaceViewArea);
    }

    public boolean start(MyCamera myCamera, int previewLaunchMode) {
        this.previewLaunchMode = previewLaunchMode;
        if (previewLaunchMode == RT_PREVIEW_MODE) {
            // 抛出异常的地方
            videoFormat = previewStream.getVideoFormat(myCamera.getPreviewStream());
            if (videoFormat != null) {
                frmW = videoFormat.getVideoW();
                frmH = videoFormat.getVideoH();
            }
        }

        if (frmH * frmW == 0) {
            return false;
        }

        this.myCamera = myCamera;
        if (!mIsDrawing) {
            needStart = true;
            return false;
        }

        startDecoderThread(previewLaunchMode, videoFormat);
        return true;
    }

    public void stop() {
        if (mjpgDecoderThread != null) {
            mjpgDecoderThread.stop();
            postInvalidate();
        }
        if (h264DecoderThread != null) {
            h264DecoderThread.stop();
        }
        needStart = false;
    }

    public void startDecoderThread(int previewLaunchMode, ICatchVideoFormat videoFormat) {
        boolean enableAudio = false;
        if (videoFormat == null) {
            return;
        }
        previewCodec = videoFormat.getCodec();
        if (previewLaunchMode == VIDEO_PB_MODE) {
            enableAudio = true;
        } else if (previewLaunchMode == RT_PREVIEW_MODE) {
            enableAudio = previewStream.supportAudio(myCamera.getPreviewStream());
        }
        switch (previewCodec) {
            case ICatchCodec.ICH_CODEC_RGBA_8888:
                mjpgDecoderThread = new MjpgDecoderThread(myCamera, mSurfaceHolder, this, previewLaunchMode, videoFormat, videoPbUpdateBarListener);
                mjpgDecoderThread.setOnDecodeTimeListener(onDecodeTimeListener);
                mjpgDecoderThread.start(enableAudio, true);
                setSurfaceViewArea();
                break;
            case ICatchCodec.ICH_CODEC_H264:
                h264DecoderThread = new H264DecoderThread(myCamera, mSurfaceHolder, this, previewLaunchMode, videoFormat, videoPbUpdateBarListener);
                h264DecoderThread.setOnDecodeTimeListener(onDecodeTimeListener);
                h264DecoderThread.start(enableAudio, true);
                setSurfaceViewArea();
                break;
        }
    }

    public void setSurfaceViewArea() {
        if (frmH == 0 || frmW == 0) {
            return;
        }
        View parentView = (View) this.getParent();
        int mWidth = parentView.getWidth();
        int mHeight = parentView.getHeight();

        if (frmH <= 0 || frmW <= 0) {
            mSurfaceHolder.setFixedSize(mWidth, mWidth * 9 / 16);
            return;
        }
        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        if (previewCodec == ICatchCodec.ICH_CODEC_RGBA_8888) {
            if (mjpgDecoderThread != null && previewLaunchMode == VIDEO_PB_MODE) {
                mjpgDecoderThread.redrawBitmap(mSurfaceHolder, mWidth, mHeight);
            }
        } else if (previewCodec == ICatchCodec.ICH_CODEC_H264) {
            if (mWidth * frmH / frmW <= mHeight) {
                mSurfaceHolder.setFixedSize(mWidth, mWidth * frmH / frmW);
            } else {
                mSurfaceHolder.setFixedSize(mHeight * frmW / frmH, mHeight);
            }
        }
    }

    public void setVideoFramePtsChangedListener(VideoFramePtsChangedListener videoPbUpdateBarListener) {
        this.videoPbUpdateBarListener = videoPbUpdateBarListener;
    }

    public void setOnDecodeTimeListener(OnDecodeTimeListener onDecodeTimeListener) {
        this.onDecodeTimeListener = onDecodeTimeListener;
    }
}
