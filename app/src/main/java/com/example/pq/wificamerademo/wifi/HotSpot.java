package com.example.pq.wificamerademo.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 11:12
 * @description
 */
public class HotSpot {

    private static final String TAG = HotSpot.class.getSimpleName();

    private static int getWifiApState(Context mContext) {
        int state = WifiManager.WIFI_STATE_DISABLED;
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            state = wifiManager.getWifiState();
        }

        Log.i(TAG,"wifi state:  " + state);
        return state;
    }

    private static boolean isApEnabled(Context mContext) {
        int state = getWifiApState(mContext);
        return WifiManager.WIFI_STATE_ENABLED == state;
    }

    /** 获取 wifi 名字 */
    public static String getSsid(Context context){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    String extra = info.getExtraInfo();
                    // 返回的字符串可能带有额外的引号 “”
                    if (extra.startsWith("\"")) {
                        return extra.substring(1, extra.length() - 1);
                    } else {
                        return extra;
                    }
                }
            }
        } else {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getSSID().replaceAll("\"", "");
            }
        }
        return "";
    }

    public static String getIp(Context context){
        String ip = "192.168.1.1";
        if (HotSpot.isApEnabled(context)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                int strIp = dhcpInfo.serverAddress;
                //此处获取ip为整数类型，需要进行转换
                return intToIp(strIp);
            }
        }
        Log.d(TAG,"getIp ip=" + ip);
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }
}
