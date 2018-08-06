package com.example.pq.wificamerademo.util;

import android.graphics.Rect;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/4 9:43
 * @description
 */
public class ScaleUtils {

    public static Rect getScaledPosition(int frmW, int frmH, int wndW, int wndH) {
        int rectLeft;
        int rectRight;
        int rectTop;
        int rectBottom;
        Rect rect;

        if (wndW * frmH < wndH * frmW) {
            // full filled with width
            rectLeft = 0;
            rectRight = wndW;
            rectTop = (wndH - wndW * frmH / frmW) / 2;
            rectBottom = wndH - rectTop;

        } else if (wndW * frmH > wndH * frmW) {
            // full filled with height
            rectLeft = (wndW - wndH * frmW / frmH) / 2;
            rectRight = wndW - rectLeft;
            rectTop = 0;
            rectBottom = wndH;
        } else {
            // full filled with width and height
            rectLeft = 0;
            rectRight = wndW;
            rectTop = 0;
            rectBottom = wndH;
        }

        rect = new Rect(rectLeft, rectTop, rectRight, rectBottom);
        return rect;
    }
}
