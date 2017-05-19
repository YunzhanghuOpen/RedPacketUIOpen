package com.yunzhanghu.redpacketui.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.RPTokenCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.SendPacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.SendPacketPresenter;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.ui.activity.RPRedPacketActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseDialogFragment;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.ClickUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hhx on 16/10/22
 */
public class RandomPacketDialogFragment extends RPBaseDialogFragment<SendPacketContract.View, SendPacketContract.Presenter<SendPacketContract.View>> implements View.OnClickListener, SendPacketContract.View, AliPay.AliPayCallBack, AliPay.AliAuthCallBack, PayTipsDialogFragment.OnDialogConfirmClickCallback {

    private RedPacketInfo mRedPacketInfo;

    private View mAvatarView;

    private View mSwitchAmount;

    private TextView mTvGreeting;

    private TextView mTvAmount;

    private Button mBtnSend;

    private ArrayList<RedPacketInfo> mRandomGreetings;

    private String[] mGreetingArray;

    private String[] mAmountArray;

    private String mAuthInfo;

    public static RandomPacketDialogFragment newInstance(RedPacketInfo redPacketInfo) {
        RandomPacketDialogFragment frag = new RandomPacketDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(RPConstant.EXTRA_RED_PACKET_INFO);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        params.setMargins(params.leftMargin, marginTop, params.rightMargin, params.bottomMargin);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_random_dialog;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.ll_random_loading);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        initView(view);
        initToken();
    }

    @Override
    public SendPacketContract.Presenter<SendPacketContract.View> initPresenter() {
        return new SendPacketPresenter();
    }

    private void initToken() {
        showLoading();
        RedPacket.getInstance().initRPToken(mRedPacketInfo.senderId, new RPTokenCallback() {

            @Override
            public void onTokenSuccess() {
                hideLoading();
                setGreetingView();
            }

            @Override
            public void onSettingSuccess() {
                hideLoading();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                hideLoading();
                showToastMsg(errorMsg);
            }
        });
    }

    private void initView(View view) {
        mAvatarView = view.findViewById(R.id.layout_random_avatar);
        View closeLayout = view.findViewById(R.id.rl_random_closed);
        View switchPacket = view.findViewById(R.id.tv_random_packet);
        mSwitchAmount = view.findViewById(R.id.tv_random_switch);
        TextView tvUserName = (TextView) view.findViewById(R.id.tv_random_username);
        mTvGreeting = (TextView) view.findViewById(R.id.tv_random_greeting);
        mTvAmount = (TextView) view.findViewById(R.id.tv_random_amount);
        mBtnSend = (Button) view.findViewById(R.id.btn_random);
        ImageView ivAvatar = (ImageView) view.findViewById(R.id.iv_random_avatar);

        closeLayout.setOnClickListener(this);
        switchPacket.setOnClickListener(this);
        mSwitchAmount.setOnClickListener(this);
        mBtnSend.setOnClickListener(this);
        mBtnSend.setVisibility(View.GONE);
        mSwitchAmount.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(mRedPacketInfo.receiverAvatarUrl)) {
            Glide.with(mContext).load(mRedPacketInfo.receiverAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(ivAvatar);
        }
        if (!TextUtils.isEmpty(mRedPacketInfo.receiverNickname)) {
            String receiverNickname = calculateNameByte(mRedPacketInfo.receiverNickname);
            if (receiverNickname.length() < mRedPacketInfo.receiverNickname.length()) {
                receiverNickname = receiverNickname + "...";
            }

            tvUserName.setText(String.format(mContext.getString(R.string.random_to_username), receiverNickname));
        }
    }

    private String calculateNameByte(String str) {
        try {
            byte[] strByte = str.getBytes("UTF-8");
            if (strByte.length > 30) {
                str = str.substring(0, str.length() - 1);
                return calculateNameByte(str);
            } else {
                return str;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    private void setGreetingView() {
        mBtnSend.setVisibility(View.VISIBLE);
        mSwitchAmount.setVisibility(View.VISIBLE);
        mRandomGreetings = RPPreferenceManager.getInstance().getRandomGreeting();
        if (mRandomGreetings != null && mRandomGreetings.size() > 0) {
            mTvAmount.setText(String.format(getString(R.string.detail_money_sign), doubleNumberFormat(Double.valueOf(mRandomGreetings.get(0).redPacketAmount))));
            mTvGreeting.setText(mRandomGreetings.get(0).redPacketGreeting);
        } else {
            setLocalGreeting();
        }
    }

    /**
     * 从setting接口获取greeting失败或为空时，本地设置
     */
    private void setLocalGreeting() {
        if (getHost() == null) return;
        mGreetingArray = getResources().getStringArray(R.array.ConstGreetings);
        mAmountArray = getResources().getStringArray(R.array.ConstAmount);
        mTvAmount.setText(String.format(getString(R.string.detail_money_sign), mAmountArray[0]));
        mTvGreeting.setText(mGreetingArray[0]);
    }


    @Override
    public void onClick(View v) {
        if (ClickUtil.isFastClick()) return;
        int i = v.getId();
        if (i == R.id.rl_random_closed) {//关闭对话框
            dismiss();
        } else if (i == R.id.tv_random_packet) {//切换到普通红包
            dismiss();
            Intent intent = new Intent(getActivity(), RPRedPacketActivity.class);
            intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, mRedPacketInfo);
            startActivity(intent);
        } else if (i == R.id.tv_random_switch) {//切换金额
            Random random = new Random();
            if (mRandomGreetings != null && mRandomGreetings.size() > 0) {
                RedPacketInfo randomGreeting = mRandomGreetings.get(random.nextInt(mRandomGreetings.size()));
                mTvAmount.setText(String.format(getString(R.string.detail_money_sign), doubleNumberFormat(Double.valueOf(randomGreeting.redPacketAmount))));
                mTvGreeting.setText(randomGreeting.redPacketGreeting);
            } else {
                int randomCount = random.nextInt(mAmountArray.length);
                mTvAmount.setText(String.format(getString(R.string.detail_money_sign), doubleNumberFormat(Double.valueOf(mAmountArray[randomCount]))));
                mTvGreeting.setText(mGreetingArray[randomCount]);
            }

        } else if (i == R.id.btn_random) {//发送红包
            if (!TextUtils.isEmpty(mTvAmount.getText().toString())) {
                String amount = mTvAmount.getText().toString();
                String[] amounts = amount.split("￥");
                mRedPacketInfo.redPacketAmount = amounts[1];
                mRedPacketInfo.redPacketGreeting = mTvGreeting.getText().toString();
                mRedPacketInfo.redPacketType = RPConstant.RED_PACKET_TYPE_SINGLE_RANDOM;
                mPresenter.preparePayment(mRedPacketInfo.redPacketAmount);
                showLoading();
                mBtnSend.setClickable(false);

            }
        }
    }


    @Override
    public void onPreparePaymentSuccess(String tradeNo, String orderInfo) {
        hideLoading();
        mBtnSend.setClickable(true);
        mRedPacketInfo.tradeNo = tradeNo;
        AliPay aliPay = new AliPay(mContext);
        aliPay.setPayCallBack(this);
        aliPay.pay(orderInfo);
    }

    @Override
    public void onUserUnauthorized(String authInfo) {
        mAuthInfo = authInfo;
        hideLoading();
        mBtnSend.setClickable(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED, getString(R.string.str_authorized_content), this);
    }

    @Override
    public void onUploadAuthInfoSuccess() {
        hideLoading();
        mBtnSend.setClickable(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, getString(R.string.str_ali_auth_success), this);
    }

    @Override
    public void onSendPacketSuccess(String redPacketId) {
        hideLoading();
        mBtnSend.setClickable(true);
        dismiss();
        mRedPacketInfo.redPacketId = redPacketId;
        if (RedPacket.getInstance().getRPSendPacketCallback() != null) {
            RedPacket.getInstance().getRPSendPacketCallback().onSendPacketSuccess(mRedPacketInfo);
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        mBtnSend.setClickable(true);
        if (code.equals(RPConstant.CLIENT_CODE_CHECK_ALI_ORDER_ERROR)) {
            message = getString(R.string.str_check_ali_order_error_content);
        }
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, message, this);
    }

    @Override
    public void onConfirmClick() {
        hideLoading();
        AliPay aliPay = new AliPay(mContext);
        aliPay.setAuthCallBack(this);
        aliPay.auth(mAuthInfo);
        mBtnSend.setClickable(true);
    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        showLoading();
        mBtnSend.setClickable(false);
    }

    @Override
    public void AliPaySuccess() {
        mPresenter.sendRedPacket(mRedPacketInfo);
        showLoading();
        mBtnSend.setClickable(false);
    }

    @Override
    public void AliPayError(String code, String msg) {
        showTipDialog(code, msg, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RedPacket.getInstance().detachTokenPresenter();
    }
}
