package com.example.pq.wificamerademo.view.lookback;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.pq.wificamerademo.R;
import com.example.pq.wificamerademo.camera.sdkApi.CameraFile;
import com.example.pq.wificamerademo.rx.BaseObserver;
import com.example.pq.wificamerademo.util.StorageUtil;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/7 17:22
 * @description
 */
public class LookBackActivity extends AppCompatActivity {

    private static ICatchFile file;

    private static String filePath;

    private VrPanoramaView panoView;

    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookback);
        panoView = findViewById(R.id.pano_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Observable.fromCallable(() -> {
            ICatchFile fileAdded = file;
            if (fileAdded != null) {
                final String path = StorageUtil.getDownloadPath(this);
                File directory = new File(path);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                final String filePath = path + fileAdded.getFileHandle() + ".jpg";

                // 方式一
                // CameraFile.getInstance().downloadFile(fileAdded, filePath);
                CameraFile.getInstance().downloadFile(fileAdded.getFilePath(), filePath);
                panoView.loadImageFromBitmap(BitmapFactory.decodeFile(filePath), panoOptions);

                // 方式二
                // 如果用下面的 ICatchFrameBuffer 可以成功看照片的回放，但是退出当前页面，回到 PreviewActivity 后无法再次进入预览
                List<ICatchFile> list = CameraFile.getInstance().getFileList(ICatchFileType.ICH_TYPE_IMAGE);
//
//                for (int i = 0; i < list.size(); i++) {
//                    if (list.get(i).getFileHandle() == fileAdded.getFileHandle()) {
//                        fileAdded = list.get(i);
//                        break;
//                    }
//                }
//
//                ICatchFrameBuffer buffer = CameraFile.getInstance().downloadFile(fileAdded);
//                if (buffer != null) {
//                    panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
//                    try {
//                        // panoView.loadImageFromByteArray(buffer.getBuffer(), panoOptions);
//                        panoView.loadImageFromBitmap(BitmapFactory.decodeByteArray(buffer.getBuffer(), 0, buffer.getBuffer().length), panoOptions);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                return buffer;
            }
            return null;
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(buffer -> {
                // Toast.makeText(this, "图片加载"+ (buffer == null ? "失败" : "成功"), Toast.LENGTH_SHORT).show();
            })
            .subscribe(new BaseObserver<>());
    }

    @Override
    protected void onPause() {
        super.onPause();
        panoView.pauseRendering();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        panoView.shutdown();
    }

    public static void startActivity(Context context, ICatchFile file) {
        Intent intent = new Intent(context, LookBackActivity.class);
        context.startActivity(intent);
        LookBackActivity.file = file;
    }

    public static void startActivity(Context context, String filePath) {
        Intent intent = new Intent(context, LookBackActivity.class);
        context.startActivity(intent);
        LookBackActivity.filePath = filePath;
    }
}
