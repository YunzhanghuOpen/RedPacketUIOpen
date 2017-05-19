package com.yunzhanghu.redpacketui.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {

    private static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    private static final String FORMAT_MM_DD = "MM-dd";
    private static final String FORMAT_HH_MM = "HH:mm";
    private static final String FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

    private static int getNowYear() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.YEAR);
    }

    private static int getNowMonth() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.MONTH);
    }

    private static int getNowDay() {
        Calendar mCalendar = Calendar.getInstance();
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private static int getOldYear(Calendar mCalendar) {
        return mCalendar.get(Calendar.YEAR);
    }

    private static int getOldMonth(Calendar mCalendar) {
        return mCalendar.get(Calendar.MONTH);
    }

    private static int getOldDay(Calendar mCalendar) {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isToday(Calendar mCalendar) {
        return getNowYear() == getOldYear(mCalendar) && getNowMonth() == getOldMonth(mCalendar) && getNowDay() == getOldDay(mCalendar);
    }

    private static boolean isYesterday(Calendar mCalendar) {
        return getNowYear() == getOldYear(mCalendar);
    }

    /**
     * 字符串日期转成Date
     *
     * @param dateStr 日期字符串
     * @return Date
     */
    private static Date stringToDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_TIMESTAMP);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Date转成格式化日期字符串
     *
     * @param date       Date
     * @param dateFormat 日期格式
     * @return 对应格式的时间
     */
    private static String dateToString(Date date, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);
    }

    /**
     * 获取格式化日期
     *
     * @param time 字符串日期
     * @return 字符串格式的时间
     */
    public static String getDateFormat(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        Date date = stringToDate(time);
        if (date == null) {
            return time;
        }
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTime(date);
        if (isToday(mCalendar)) {
            return dateToString(date, FORMAT_HH_MM);

        } else if (isYesterday(mCalendar)) {
            return dateToString(date, FORMAT_MM_DD);
        } else {
            return dateToString(date, FORMAT_YYYY_MM_DD);
        }

    }


}
