package com.example.pq.wificamerademo.constants;

import android.support.annotation.DrawableRes;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/3 19:56
 * @description
 */
public class ItemInfo {
    public String text;
    public int resource;
    public @DrawableRes int icon;

    public ItemInfo(int resource, int icon) {
        this.resource = resource;
        this.icon = icon;
    }

    public ItemInfo(String text, int icon) {
        this.text = text;
        this.icon = icon;
    }
}
