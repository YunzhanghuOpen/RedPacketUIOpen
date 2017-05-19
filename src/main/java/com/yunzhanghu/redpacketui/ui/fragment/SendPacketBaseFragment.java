package com.yunzhanghu.redpacketui.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yunzhanghu.redpacketsdk.RPCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.SendPacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.SendPacketPresenter;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.callback.RetryTokenListener;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;


public class SendPacketBaseFragment extends RPBaseFragment<SendPacketContract.View, SendPacketContract.Presenter<SendPacketContract.View>> implements View.OnClickListener, SendPacketContract.View, AliPay.AliPayCallBack, AliPay.AliAuthCallBack {


    public final static String ARGS_RED_PACKET_INFO = "red_packet_info";

    public RedPacketInfo mRedPacketInfo = new RedPacketInfo();

    public String[] mGreetingArray;

    public int mArrayIndex;

    private PopupWindow mPopupWindow;

    public View mPopupParent;

    public TextView mTvPopupMsg;

    public double mSingleLimit = 100;

    public double mMinLimit = 0.01f;

    private View mPopupView;

    private Button mButton;

    private RetryTokenListener mRetryTokenListener;

    public static SendPacketBaseFragment newInstance(RedPacketInfo redPacketInfo) {
        SendPacketBaseFragment fragment = new SendPacketBaseFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_INFO, redPacketInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mRetryTokenListener = (RetryTokenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RetryTokenListener");
        }
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(ARGS_RED_PACKET_INFO);
        }
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        //初始化设置项
        if (RPPreferenceManager.getInstance().getGreetings().length == 0) {
            mGreetingArray = getResources().getStringArray(R.array.greetings);
        } else {
            mGreetingArray = RPPreferenceManager.getInstance().getGreetings();
        }
        mArrayIndex++;
        updateLimit();
    }

    @Override
    protected int getContentViewLayoutID() {
        return 0;
    }

    @Override
    public SendPacketContract.Presenter<SendPacketContract.View> initPresenter() {
        return new SendPacketPresenter();
    }

    @Override
    public void onPreparePaymentSuccess(String tradeNo, String orderInfo) {
        hideLoading();
        enableButton(mButton);
        mRedPacketInfo.tradeNo = tradeNo;
        AliPay aliPay = new AliPay(mContext);
        aliPay.setPayCallBack(this);
        aliPay.pay(orderInfo);
    }

    @Override
    public void onUserUnauthorized(final String authInfo) {
        hideLoading();
        enableButton(mButton);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED, getString(R.string.str_authorized_content), new PayTipsDialogFragment.OnDialogConfirmClickCallback() {
            @Override
            public void onConfirmClick() {
                enableButton(mButton);
                AliPay aliPay = new AliPay(mContext);
                aliPay.setAuthCallBack(SendPacketBaseFragment.this);
                aliPay.auth(authInfo);
            }
        });
    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        showLoading();
        disableButton(mButton);
    }

    @Override
    public void onUploadAuthInfoSuccess() {
        hideLoading();
        enableButton(mButton);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, getString(R.string.str_ali_auth_success), null);
    }

    @Override
    public void AliPaySuccess() {
        mPresenter.sendRedPacket(mRedPacketInfo);
        showLoading();
        disableButton(mButton);
    }

    @Override
    public void AliPayError(String code, String msg) {
        showTipDialog(code, msg, null);
    }

    @Override
    public void onSendPacketSuccess(String redPacketId) {
        //发红包请求成功后 会触发该回调方法
        hideLoading();
        enableButton(mButton);
        if (RedPacket.getInstance().getRPSendPacketCallback() != null) {
            mRedPacketInfo.redPacketId = redPacketId;
            RedPacket.getInstance().getRPSendPacketCallback().onSendPacketSuccess(mRedPacketInfo);
            getActivity().finish();
        }
    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        enableButton(mButton);
        if (code.equals(RPConstant.CLIENT_CODE_CHECK_ALI_ORDER_ERROR)) {
            message = getString(R.string.str_check_ali_order_error_content);
        }
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, message, null);
    }


    public void initPopupWindow() {
        if (mPopupView == null) {
            mPopupView = getActivity().getLayoutInflater().inflate(R.layout.rp_popup_layout, null,false);
        }
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(mPopupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mTvPopupMsg = (TextView) mPopupView.findViewById(R.id.tv_popup_msg);
            mPopupWindow.setOutsideTouchable(false);
        }
    }


    public void showPopupWindow(View parentView, TextView tvPopupMsg, String message) {
        tvPopupMsg.setText(message);
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        int offsetY = location[1] + parentView.getHeight();
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(parentView, Gravity.START | Gravity.TOP, 0, offsetY);
        }
    }

    public void hidePopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void updateLimit() {
        mSingleLimit = Double.valueOf(RPPreferenceManager.getInstance().getLimit());
        mMinLimit = Double.valueOf(RPPreferenceManager.getInstance().getMinLimit());
    }

    /**
     * 把200.00变成200
     */
    public String getNumberLimit(double number) {
        String numberLimit = String.valueOf(number);
        if (number >= 1) {
            int singleLimitPosDot = numberLimit.indexOf(".");
            if (singleLimitPosDot > 0) {
                String[] st = numberLimit.split("\\.");
                numberLimit = st[0];
            }
        }
        return numberLimit;
    }

    /**
     * 判断字符串前面有几个0开头
     */
    public int getStartZeroNumber(String num) {
        double money = Double.valueOf(num);
        if (money == 0) {
            return num.length();
        } else {
            return num.length() - String.valueOf(Integer.parseInt(num)).length();
        }
    }


    public void enableButton(Button button) {
        button.setEnabled(true);
    }

    public void disableButton(Button button) {
        mButton = button;
        button.setEnabled(false);
    }

    public void retryRPToken(final Button button) {
        if (TextUtils.isEmpty(RPPreferenceManager.getInstance().getRPToken())) {
            showLoading();
            mRetryTokenListener.onRetryToken(new RPCallback() {
                @Override
                public void onSuccess() {
                    preparePayment(button);
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    hideLoading();
                    showToastMsg(errorMsg);
                    enableButton(button);
                }
            });
        } else {
            preparePayment(button);
        }
    }

    private void preparePayment(Button button) {
        mPresenter.preparePayment(mRedPacketInfo.redPacketAmount);
        disableButton(button);
        showLoading();
    }

    @Override
    public void onClick(View v) {

    }

    private void showTipDialog(String code, String msg, PayTipsDialogFragment.OnDialogConfirmClickCallback callback) {
        PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(code, msg);
        dialog.setCallback(callback);
        if (getActivity() != null) {
            FragmentTransaction ft = getMyFragmentManager(getActivity()).beginTransaction();
            ft.add(dialog, RPConstant.RP_TIP_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        closeSoftKeyboard();
    }
}
