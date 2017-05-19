package com.yunzhanghu.redpacketui.callback;

import android.text.Editable;
import android.text.TextWatcher;

import java.io.UnsupportedEncodingException;

/**
 * Created by hhx on 16/12/28
 */

public abstract class GreetingTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            byte[] srtByte = s.toString().getBytes("UTF-8");
            if (srtByte.length > 64) {
                s.delete(s.length() - 1, s.length());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
