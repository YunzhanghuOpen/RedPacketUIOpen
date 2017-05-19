package com.yunzhanghu.redpacketui.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;

import java.lang.reflect.Field;


/**
 * Created by max on 16/1/22
 */
public class PayTipsDialogFragment extends DialogFragment implements View.OnClickListener {

    private final static String ARGS_PAY_STATUS = "pay_status";

    private final static String ARGS_MESSAGE = "message";

    private Context mContext;

    private String mMessage;

    private String mPayStatus = "-1";

    private OnDialogConfirmClickCallback mCallback;

    public static PayTipsDialogFragment newInstance(String status, String message) {
        PayTipsDialogFragment payTipsDialogFragment = new PayTipsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_MESSAGE, message);
        bundle.putString(ARGS_PAY_STATUS, status);
        payTipsDialogFragment.setArguments(bundle);
        return payTipsDialogFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //去掉Dialog的Title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPayStatus = getArguments().getString(ARGS_PAY_STATUS);
            mMessage = getArguments().getString(ARGS_MESSAGE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        keepFontSize();//保持字体不变
        return inflater.inflate(R.layout.rp_pay_tips_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        View dividerView = view.findViewById(R.id.dialog_hint_divider);
        TextView buttonOk = (TextView) view.findViewById(R.id.btn_ok);
        buttonOk.setOnClickListener(this);
        TextView buttonCancel = (TextView) view.findViewById(R.id.btn_cancel);
        buttonCancel.setOnClickListener(this);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        TextView tvMessage = (TextView) view.findViewById(R.id.tv_msg);
        String titleText = mContext.getString(R.string.hint_title);
        switch (mPayStatus) {
            case RPConstant.CLIENT_CODE_OTHER_ERROR:
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
            case RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED:
                titleText = mContext.getString(R.string.str_authorized_bind_ali_title);
                buttonCancel.setText(R.string.btn_cancel);
                buttonOk.setText(R.string.str_authorized);
                break;
            case RPConstant.CLIENT_CODE_CHECK_ALI_ORDER_ERROR:
                titleText = mContext.getString(R.string.str_heck_ali_order_error_title);
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
            case RPConstant.CLIENT_CODE_ALI_PAY_CANCEL:
                titleText = mContext.getString(R.string.str_ali_cancel_pay_title);
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
            case RPConstant.CLIENT_CODE_ALI_PAY_FAIL:
                titleText = mContext.getString(R.string.str_ali_pay_fail_title);
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
            case RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS:
                tvTitle.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, getResources().getDisplayMetrics());
                tvMessage.setLayoutParams(params);
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
            case RPConstant.CLIENT_CODE_AD_SHARE_SUCCESS:
                tvTitle.setVisibility(View.GONE);
                LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) tvMessage.getLayoutParams();
                params1.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 38, getResources().getDisplayMetrics());
                tvMessage.setLayoutParams(params1);
                buttonOk.setText(R.string.ad_share);
                break;
            case RPConstant.CLIENT_CODE_UNBIND_ALI_ACCOUNT:
                titleText = getString(R.string.tip_title_unbind_ali);
                buttonOk.setText(getString(R.string.btn_unbind));
                buttonOk.setTextColor(ContextCompat.getColor(mContext, R.color.rp_top_red_color));
                buttonCancel.setText(R.string.btn_cancel);
                break;
            default:
                buttonCancel.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
                buttonOk.setText(R.string.btn_know);
                break;
        }
        tvTitle.setText(titleText);
        tvMessage.setText(mMessage);
    }

    @Override
    public void onClick(View v) {
        this.dismissAllowingStateLoss();
        if (v.getId() == R.id.btn_ok && mCallback != null) {
            switch (mPayStatus) {
                case RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED:
                    mCallback.onConfirmClick();
                    break;
                case RPConstant.CLIENT_CODE_AD_SHARE_SUCCESS:
                    mCallback.onConfirmClick();
                    break;
                case RPConstant.CLIENT_CODE_UNBIND_ALI_ACCOUNT:
                    mCallback.onConfirmClick();
                    break;
            }
        }
    }

    private void keepFontSize() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // for bug ---> java.lang.IllegalStateException: Activity has been destroyed
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCallback(OnDialogConfirmClickCallback callback) {
        mCallback = callback;
    }

    public interface OnDialogConfirmClickCallback {
        void onConfirmClick();
    }


}
