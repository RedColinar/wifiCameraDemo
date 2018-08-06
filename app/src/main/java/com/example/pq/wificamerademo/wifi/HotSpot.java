package com.example.pq.wificamerademo.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

    public static String getSsid(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getSSID().replaceAll("\"", "");
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
