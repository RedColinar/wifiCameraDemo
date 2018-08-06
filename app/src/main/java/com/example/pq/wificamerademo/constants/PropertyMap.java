package com.example.pq.wificamerademo.constants;

import android.annotation.SuppressLint;

import com.example.pq.wificamerademo.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 19:54
 * @description
 */
@SuppressLint("UseSparseArrays")
public class PropertyMap {

    private static class WhiteBalanceMapHolder {
        private static final Map<Integer, ItemInfo> whiteBalanceMap = new HashMap<>();

        static {
            whiteBalanceMap.put(WhiteBalance.WB_AUTO, new ItemInfo("自动", R.drawable.awb_auto));
            whiteBalanceMap.put(WhiteBalance.WB_DAYLIGHT, new ItemInfo("日光", R.drawable.awb_daylight));
            whiteBalanceMap.put(WhiteBalance.WB_CLOUDY, new ItemInfo("多云", R.drawable.awb_cloudy));
            whiteBalanceMap.put(WhiteBalance.WB_FLUORESCENT, new ItemInfo("荧光", R.drawable.awb_fluoresecent));
            whiteBalanceMap.put(WhiteBalance.WB_TUNGSTEN, new ItemInfo("白炽", R.drawable.awb_incandescent));
        }
    }

    public static Map<Integer, ItemInfo> getWhiteBalanceMap() {
        return WhiteBalanceMapHolder.whiteBalanceMap;
    }
}
