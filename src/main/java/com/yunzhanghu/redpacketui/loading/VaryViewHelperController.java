package com.yunzhanghu.redpacketui.loading;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yunzhanghu.redpacketui.R;


public class VaryViewHelperController {

    private IVaryViewHelper helper;

    private View mLoadView;

    private View mErrorView;

    public VaryViewHelperController(View view) {
        this(new VaryViewHelper(view));
    }

    private VaryViewHelperController(IVaryViewHelper helper) {
        super();
        this.helper = helper;
    }

    public void showError(String errorMsg, View.OnClickListener onClickListener) {
        if (mErrorView == null) {
            mErrorView = helper.inflate(R.layout.rp_error_page);
            TextView textView = (TextView) mErrorView.findViewById(R.id.tv_error_hint);
            if (!TextUtils.isEmpty(errorMsg)) {
                textView.setText(errorMsg);
            } else {
                textView.setText(helper.getContext().getResources().getString(R.string.msg_error_page_hint));
            }

            if (null != onClickListener) {
                mErrorView.setOnClickListener(onClickListener);
            }
        }
        helper.showLayout(mErrorView);
    }

    public void showLoading() {
        if (mLoadView == null) {
            mLoadView = helper.inflate(R.layout.rp_loading);
        }
        helper.showLayout(mLoadView);
    }

    public void restore() {
        helper.restoreView();
    }
}
