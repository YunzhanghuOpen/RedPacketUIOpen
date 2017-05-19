package com.yunzhanghu.redpacketui.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.ReceivePacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.ReceivePacketPresenter;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.ui.activity.RPDetailActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseDialogFragment;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.ClickUtil;

/**
 * Created by desert on 16/6/13
 */
public class SRedPacketDialogFragment extends RPBaseDialogFragment<ReceivePacketContract.View, ReceivePacketContract.Presenter<ReceivePacketContract.View>> implements View.OnClickListener, ReceivePacketContract.View, AliPay.AliAuthCallBack {

    private RedPacketInfo mRedPacketInfo;

    private final static String ARGS_RED_PACKET_INFO = "red_packet_info";

    private FrameLayout mLayoutAvatar;

    private TextView mTvGreeting;

    private TextView mTvAmount;

    private TextView tvUserName;

    private TextView tvTitle;

    private Button mBtnOpen;

    private String mCurrentUserId = "";

    private int mBtnState = 0;//0拆红包1默默关掉2偷看一下

    private ImageView mAvatarView;

    private RPValueCallback<String> mCallback;

    public static SRedPacketDialogFragment newInstance(RedPacketInfo redPacketInfo) {
        SRedPacketDialogFragment frag = new SRedPacketDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_INFO, redPacketInfo);
        frag.setArguments(args);
        return frag;
    }

    public void setArguments(RedPacketInfo redPacketInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_INFO, redPacketInfo);
        this.setArguments(args);
    }

    public void setCallback(RPValueCallback<String> callback) {
        mCallback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(ARGS_RED_PACKET_INFO);
            mCurrentUserId = mRedPacketInfo != null ? mRedPacketInfo.currentUserId : "";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        params.setMargins(params.leftMargin, marginTop, params.rightMargin, params.bottomMargin);
    }

    @Override
    public ReceivePacketContract.Presenter<ReceivePacketContract.View> initPresenter() {
        return new ReceivePacketPresenter();
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_open_exclusive_packet_dialog;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        mAvatarView = (ImageView) view.findViewById(R.id.iv_avatar);
        mLayoutAvatar = (FrameLayout) view.findViewById(R.id.layout_exclusive_avatar);
        mBtnOpen = (Button) view.findViewById(R.id.btn_exclusive_open_money);
        mTvGreeting = (TextView) view.findViewById(R.id.tv_exclusive_greeting);
        mTvAmount = (TextView) view.findViewById(R.id.tv_exclusive_amount);
        tvUserName = (TextView) view.findViewById(R.id.tv_exclusive_username);
        tvTitle = (TextView) view.findViewById(R.id.tv_exclusive_title);
        View closeLayout = view.findViewById(R.id.layout_exclusive_closed);
        ImageView ivOpenBg = (ImageView) view.findViewById(R.id.iv_open_bg);
        ImageView mIvSendAvatar = (ImageView) view.findViewById(R.id.iv_send_avatar);
        ImageView mIvReceiveAvatar = (ImageView) view.findViewById(R.id.iv_receive_avatar);
        closeLayout.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        if (!TextUtils.isEmpty(mRedPacketInfo.senderAvatarUrl)) {
            Glide.with(mContext).load(mRedPacketInfo.senderAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(mIvSendAvatar);
        }
        if (!TextUtils.isEmpty(mRedPacketInfo.receiverAvatarUrl)) {
            Glide.with(mContext).load(mRedPacketInfo.receiverAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(mIvReceiveAvatar);
        }
        if (!TextUtils.isEmpty(RPPreferenceManager.getInstance().getOpenUrl())) {
            Glide.with(mContext).load(RPPreferenceManager.getInstance().getOpenUrl())
                    .error(R.drawable.rp_open_packet_bg)
                    .into(ivOpenBg);
        }
        int status = mRedPacketInfo.status;
        tvUserName.setText(mRedPacketInfo.senderNickname);
        if (status == RPConstant.RED_PACKET_STATUS_EXPIRED) {//红包过期
            mBtnOpen.setVisibility(View.GONE);
            tvTitle.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.equals(mCurrentUserId, mRedPacketInfo.receiverId)) {//不是专属红包接受者
            if (status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {//可领取
                SpannableString sp = new SpannableString(String.format(mContext.getString(R.string.send_who_money), mRedPacketInfo.receiverNickname));
                sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.rp_title_color)), 1, mRedPacketInfo.receiverNickname.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTitle.setText(sp);
                //默默关掉
                mBtnOpen.setText(R.string.btn_silence_turn_off);
                mBtnState = 1;

            } else if (status == RPConstant.RED_PACKET_STATUS_RECEIVED) {//被领取了
                SpannableString sp = new SpannableString(String.format(mContext.getString(R.string.send_who_money), mRedPacketInfo.receiverNickname));
                sp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.rp_title_color)), 1, mRedPacketInfo.receiverNickname.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTitle.setText(sp);
                //偷看一下
                mBtnOpen.setText(R.string.btn_secretly_look);
                mBtnState = 2;
            }
        } else {
            tvTitle.setText(R.string.send_you_money);
            mBtnState = 0;
        }
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.isFastClick()) return;
        if (v.getId() == R.id.btn_exclusive_open_money) {//拆红包
            if (mBtnState == 0) {
                //请求收红包接口
                mPresenter.receiveRedPacket(mRedPacketInfo.redPacketId, mRedPacketInfo.redPacketType);
                showLoading();
                mBtnOpen.setClickable(false);
            } else if (mBtnState == 1) {//默默关掉
                dismiss();
            } else if (mBtnState == 2) {//偷看一下
                mBtnOpen.setClickable(false);
                mLayoutAvatar.findViewById(R.id.layout_exclusive_avatar).setVisibility(View.GONE);
                tvUserName.setVisibility(View.GONE);
                tvTitle.setVisibility(View.GONE);
                mTvGreeting.setVisibility(View.VISIBLE);
                mTvAmount.setVisibility(View.VISIBLE);
                mTvGreeting.setText(mRedPacketInfo.redPacketGreeting);
                mTvAmount.setText(String.format(mContext.getString(R.string.detail_money_sign), mRedPacketInfo.redPacketAmount));
                mBtnOpen.setText(R.string.btn_silence_turn_off);
                mBtnState = 1;
                mBtnOpen.setClickable(true);
            }

        } else if (v.getId() == R.id.layout_exclusive_closed) {
            dismiss();
        }
    }

    @Override
    public void onReceivePacketSuccess(String redPacketId, String myAmount, String landingPage) {
        hideLoading();
        mBtnOpen.setClickable(true);
        if (mCallback != null) {
            mCallback.onSuccess(myAmount);
            Intent intent = new Intent(getActivity(), RPDetailActivity.class);
            intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, mRedPacketInfo);
            startActivity(intent);
            this.dismiss();
        }
    }

    @Override
    public void onRedPacketSnappedUp(String redPacketType) {

    }

    @Override
    public void onRedPacketAlreadyReceived() {
        hideLoading();
        mBtnOpen.setClickable(true);
        Intent intent = new Intent(getActivity(), RPDetailActivity.class);
        intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, mRedPacketInfo);
        startActivity(intent);
        this.dismissAllowingStateLoss();
    }

    @Override
    public void onUserUnauthorized(final String authInfo) {
        hideLoading();
        mBtnOpen.setClickable(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED, getString(R.string.str_authorized_receive_rp), new PayTipsDialogFragment.OnDialogConfirmClickCallback() {
            @Override
            public void onConfirmClick() {
                AliPay aliPay = new AliPay(mContext);
                aliPay.setAuthCallBack(SRedPacketDialogFragment.this);
                aliPay.auth(authInfo);
            }
        });
    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        showLoading();
        mBtnOpen.setClickable(false);
    }

    @Override
    public void onUploadAuthInfoSuccess() {
        hideLoading();
        mBtnOpen.setClickable(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, getString(R.string.str_ali_auth_success), null);
    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        mBtnOpen.setClickable(true);
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, message, null);
    }


}
