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
 * Created by max on 16/1/22
 */
public class RedPacketDialogFragment extends RPBaseDialogFragment<ReceivePacketContract.View, ReceivePacketContract.Presenter<ReceivePacketContract.View>> implements View.OnClickListener, ReceivePacketContract.View, AliPay.AliAuthCallBack {

    private RedPacketInfo mRedPacketInfo;

    private final static String ARGS_RED_PACKET_INFO = "red_packet_info";

    private TextView mTvGreeting;

    private Button mBtnOpen;

    private TextView mTvCheckLucky;

    private TextView mTvTitle;

    private View mAvatarView;

    private RPValueCallback<String> mCallback;


    public static RedPacketDialogFragment newInstance(RedPacketInfo redPacketInfo) {
        RedPacketDialogFragment frag = new RedPacketDialogFragment();
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
        return R.layout.rp_open_packet_dialog;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        ImageView ivOpenBg = (ImageView) view.findViewById(R.id.iv_open_bg);
        mBtnOpen = (Button) view.findViewById(R.id.btn_open_money);
        View closeLayout = view.findViewById(R.id.layout_closed);
        TextView tvUserName = (TextView) view.findViewById(R.id.tv_username);
        mTvGreeting = (TextView) view.findViewById(R.id.tv_greeting);
        mAvatarView = view.findViewById(R.id.layout_avatar);
        ImageView ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        mTvTitle = (TextView) view.findViewById(R.id.tv_open_title);
        mTvCheckLucky = (TextView) view.findViewById(R.id.tv_check_lucky);
        mTvCheckLucky.setOnClickListener(this);
        if (!TextUtils.isEmpty(mRedPacketInfo.senderAvatarUrl)) {
            Glide.with(mContext).load(mRedPacketInfo.senderAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(ivAvatar);
        }
        if (!TextUtils.isEmpty(RPPreferenceManager.getInstance().getOpenUrl())) {
            Glide.with(mContext).load(RPPreferenceManager.getInstance().getOpenUrl())
                    .error(R.drawable.rp_open_packet_bg)
                    .into(ivOpenBg);
        }
        closeLayout.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        int status = mRedPacketInfo.status;
        tvUserName.setText(mRedPacketInfo.senderNickname);
        if (mRedPacketInfo.chatType == RPConstant.CHAT_TYPE_GROUP) {
            mTvCheckLucky.setVisibility(View.VISIBLE);
            if (mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
                mTvTitle.setText(R.string.title_random_money);
            } else if (mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE)
                    || mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_PRI)) {
                mTvTitle.setText(R.string.send_you_money);
            }
            if (status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {//可以领取
                if (mRedPacketInfo.messageDirect.equals(RPConstant.MESSAGE_DIRECT_RECEIVE)) {
                    mTvCheckLucky.setVisibility(View.INVISIBLE);
                }
                mBtnOpen.setVisibility(View.VISIBLE);
                mTvGreeting.setText(mRedPacketInfo.redPacketGreeting);
            } else if (status == RPConstant.RED_PACKET_STATUS_RECEIVED) {//领完了
                mTvTitle.setVisibility(View.INVISIBLE);
                if (mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE)
                        || mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_PRI)) {
                    mTvCheckLucky.setVisibility(View.INVISIBLE);
                    mTvGreeting.setText(R.string.money_is_out_avg);
                } else {
                    mTvGreeting.setText(R.string.money_is_out);
                }
                mBtnOpen.setVisibility(View.GONE);
            } else if (status == RPConstant.RED_PACKET_STATUS_EXPIRED) {
                mTvTitle.setVisibility(View.INVISIBLE);
                mTvGreeting.setText(R.string.money_expired_str);
                mTvCheckLucky.setVisibility(View.INVISIBLE);
                mBtnOpen.setVisibility(View.GONE);
            }
        } else if (mRedPacketInfo.chatType == RPConstant.CHAT_TYPE_SINGLE) {
            mTvCheckLucky.setVisibility(View.INVISIBLE);
            if (status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {
                mBtnOpen.setVisibility(View.VISIBLE);
                mTvGreeting.setText(mRedPacketInfo.redPacketGreeting);
            } else if (status == RPConstant.RED_PACKET_STATUS_EXPIRED) {
                mBtnOpen.setVisibility(View.GONE);
                tvUserName.setText(mRedPacketInfo.senderNickname);
                mTvTitle.setVisibility(View.INVISIBLE);
                mTvGreeting.setText(R.string.money_expired_str);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.isFastClick()) return;
        if (v.getId() == R.id.btn_open_money) {
            //请求收红包接口
            mPresenter.receiveRedPacket(mRedPacketInfo.redPacketId, mRedPacketInfo.redPacketType);
            showLoading();
            mBtnOpen.setClickable(false);
        } else if (v.getId() == R.id.layout_closed) {
            dismiss();
        } else if (v.getId() == R.id.tv_check_lucky) {
            //查看详情
            Intent intent = new Intent(getActivity(), RPDetailActivity.class);
            intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, mRedPacketInfo);
            startActivity(intent);
            this.dismissAllowingStateLoss();
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
        hideLoading();
        mBtnOpen.setClickable(true);
        mBtnOpen.setVisibility(View.GONE);
        mTvTitle.setVisibility(View.INVISIBLE);
        mTvGreeting.setText(R.string.money_is_out);
        if (redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE)) {
            mTvCheckLucky.setVisibility(View.GONE);
        } else if (redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
            mTvCheckLucky.setVisibility(View.VISIBLE);
        }
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
                aliPay.setAuthCallBack(RedPacketDialogFragment.this);
                aliPay.auth(authInfo);
                mBtnOpen.setClickable(false);
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
