package com.example.pq.wificamerademo.view.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.pq.wificamerademo.R;
import com.example.pq.wificamerademo.application.MyApplication;
import com.example.pq.wificamerademo.camera.MyCamera;
import com.example.pq.wificamerademo.rx.BaseObserver;
import com.example.pq.wificamerademo.view.preview.PreviewActivity;
import com.example.pq.wificamerademo.wifi.HotSpot;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button btConnect;
    MyCamera currentCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btConnect = findViewById(R.id.bt_connect);
        btConnect.setOnClickListener(v -> launchCamera());
    }

    private void launchCamera() {
        final String ip = HotSpot.getIp(this);

        Observable.fromCallable(() -> connectCamera(ip))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(isSuccess -> {
                    String notify;
                    notify = "wifi 相机: "+ HotSpot.getSsid(this) + (isSuccess ? " 连接成功" : " 连接失败");
                    Toast.makeText(getApplicationContext(), notify, Toast.LENGTH_SHORT).show();

                    if (isSuccess)
                        PreviewActivity.startActivity(MainActivity.this);
                })
                .subscribe(new BaseObserver<>());
    }

    private boolean connectCamera(final String ip) {
        currentCamera = new MyCamera();
        if (!currentCamera.getCameraSession().prepareSession(ip)) {
            return false;
        }
        if (currentCamera.getCameraSession().checkWifiConnection()) {
            MyApplication.setMyCamera(currentCamera);
            currentCamera.initCamera();
            return true;
        }
        return false;
    }
}
