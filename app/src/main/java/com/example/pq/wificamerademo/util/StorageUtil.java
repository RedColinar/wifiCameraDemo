package com.example.pq.wificamerademo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.example.pq.wificamerademo.constants.AppInfo;
import java.io.File;

import static android.content.Context.MODE_PRIVATE;

public class StorageUtil {
    private static String TAG = "StorageUtil";

    public static String getDownloadPath(Context context) {
        String path = null;
        SharedPreferences preferences = context.getSharedPreferences("appData", MODE_PRIVATE);
        String storageLocation = preferences.getString("storageLocation", "InternalStorage");
        if (storageLocation.equals("InternalStorage")) {
            return Environment.getExternalStorageDirectory().toString() + AppInfo.DOWNLOAD_PATH;
        }
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            File[] fs = context.getExternalFilesDirs(null);
            // at index 0 you have the internal storage and at index 1 the real external...
            if (fs != null && fs.length >= 2) {
                if (fs[1] == null || fs[1].getAbsolutePath().isEmpty()) {
                    path = Environment.getExternalStorageDirectory().toString() + AppInfo.DOWNLOAD_PATH;
                } else {
                    path = fs[1].getAbsolutePath() + AppInfo.DOWNLOAD_PATH;
                }
//                path = fs[1].getAbsolutePath() + AppInfo.SDK_LOG_DIRECTORY_PATH;
            } else {
                path = Environment.getExternalStorageDirectory().toString() + AppInfo.DOWNLOAD_PATH;
            }
        } else {
            path = Environment.getExternalStorageDirectory().toString() + AppInfo.DOWNLOAD_PATH;
        }
        Log.i(TAG, "End getDownloadVideoPath path=" + path);
        return path;
    }

    public static File getStorageDirectory(Context context) {
        Log.i(TAG, "start getDownloadVideoPath");
        SharedPreferences preferences = context.getSharedPreferences("appData", MODE_PRIVATE);
        String storageLocation = preferences.getString("storageLocation", "InternalStorage");
        if (storageLocation.equals("InternalStorage")) {
            return Environment.getExternalStorageDirectory();
        }
        File path = null;
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            File[] fs = context.getExternalFilesDirs(null);
            // at index 0 you have the internal storage and at index 1 the real external...
            if (fs != null && fs.length >= 2) {
                if (fs[1] == null || fs[1].getAbsolutePath() == null || fs[1].getAbsolutePath().isEmpty()) {
                    path = Environment.getExternalStorageDirectory();
                } else {
                    path = fs[1];
                }
            } else {
                path = Environment.getExternalStorageDirectory();
            }
        } else {
            path = Environment.getExternalStorageDirectory();
        }
        Log.i(TAG, "End getDownloadVideoPath path=" + path);
        return path;
    }

    public static boolean sdCardExist(Context context) {
        Log.i(TAG, "start getDownloadVideoPath");
        boolean sdCardExist = false;
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            File[] fs = context.getExternalFilesDirs(null);
            // at index 0 you have the internal storage and at index 1 the real external...
            if (fs != null && fs.length >= 2) {
                if (fs[1] == null || fs[1].getAbsolutePath() == null || fs[1].getAbsolutePath().isEmpty()) {
                    sdCardExist = false;
                } else {
                    sdCardExist = true;
                }
            } else {
                sdCardExist = false;
            }
        } else {
            sdCardExist = false;
        }
//         = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Log.i(TAG, "sdCardExist=" + sdCardExist);
        return sdCardExist;
    }

    public static String getCurStorageLocation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("appData", MODE_PRIVATE);
        String storageLocation = preferences.getString("storageLocation", "InternalStorage");
        String storageLocationUIString = "";
        if (storageLocation.equals("InternalStorage")) {
            // storageLocationUIString = context.getResources().getString(R.string.setting_internal_storage);
        } else {
            // storageLocationUIString = context.getResources().getString(R.string.setting_sd_card_storage);
        }
        return storageLocationUIString;
    }
}
