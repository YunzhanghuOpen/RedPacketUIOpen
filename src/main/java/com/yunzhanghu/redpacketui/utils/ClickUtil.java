package com.yunzhanghu.redpacketui.utils;

/**
 * Created by max on 16/4/23
 */
public class ClickUtil {

    private static final long INTERVAL = 500L;

    public static long sLastClickTime = 0L;

    public static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - sLastClickTime > 0 && time - sLastClickTime < INTERVAL) {
            return true;
        } else {
            sLastClickTime = time;
            return false;
        }
    }
}
