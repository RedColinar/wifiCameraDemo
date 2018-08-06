package com.example.pq.wificamerademo.util;

import android.util.SparseIntArray;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/4 14:56
 * @description
 */
public class DataConvertUtils {

    // region bustMap
    private static SparseIntArray burstMap = new SparseIntArray();
    public static int getBurstConverFromFw(int fwValue) {
        if (burstMap.size() == 0) {
            burstMap.put(0, 0);
            burstMap.put(1, 1);
            burstMap.put(2, 3);
            burstMap.put(3, 5);
            burstMap.put(4, 10);
        }
        if (fwValue >= 0 && fwValue <= 4) {
            return burstMap.get(fwValue);
        }
        return 0;
    }
    // endregion

}
