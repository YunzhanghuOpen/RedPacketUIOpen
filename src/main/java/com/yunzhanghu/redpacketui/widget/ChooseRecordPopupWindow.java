package com.yunzhanghu.redpacketui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yunzhanghu.redpacketui.R;

/**
 * Created by max on 16/5/7
 */
public class ChooseRecordPopupWindow extends PopupWindow {


    public ChooseRecordPopupWindow(Context context, View.OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.rp_choose_record_popup_layout, null);
        TextView tvSend = (TextView) popupView.findViewById(R.id.tv_choose_send);
        TextView tvReceived = (TextView) popupView.findViewById(R.id.tv_choose_received);
        //设置按钮监听
        tvSend.setOnClickListener(itemsOnClick);
        tvReceived.setOnClickListener(itemsOnClick);
        //设置ChooseRecordPopupWindow的View
        this.setContentView(popupView);
        //设置ChooseRecordPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置ChooseRecordPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置ChooseRecordPopupWindow弹出窗体可点击
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xbffffff);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

    }
}
