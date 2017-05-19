package com.yunzhanghu.redpacketui.ui.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.ReceivePacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.ReceivePacketPresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.ClickUtil;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;
import com.yunzhanghu.redpacketui.widget.RPTextView;

import java.lang.reflect.Method;

/**
 * Created by desert on 16/6/4
 */
public class ADPacketFragment extends RPBaseFragment<ReceivePacketContract.View, ReceivePacketContract.Presenter<ReceivePacketContract.View>> implements View.OnClickListener, ReceivePacketContract.View, AliPay.AliAuthCallBack, PayTipsDialogFragment.OnDialogConfirmClickCallback {

    private ImageView mAdBg;

    private ImageView mAdIcon;

    private Button mBtnReceive;

    private TextView mTVSponsor;

    private TextView mTVGreeting;//领取红包之后的祝福语

    private TextView mTVMoney;//领取红包之前是祝福语,领取之后是金额

    private TextView mTVHint;

    private RPTextView mTVLook;

    private RPTextView mTVShare;

    private RelativeLayout mLayoutBom;

    private RedPacketInfo mRedPacketInfo;

    private String mBannerUrl = "none";

    private View mButtonLayout;

    private boolean mIsShare;

    public static ADPacketFragment newInstance(RedPacketInfo redPacketInfo) {
        ADPacketFragment fragment = new ADPacketFragment();
        Bundle args = new Bundle();
        args.putParcelable(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(RPConstant.EXTRA_RED_PACKET_INFO);
            assert mRedPacketInfo != null;
            mPresenter.sendADStatistics(RPConstant.STATISTICS_TYPE_OPEN_AD, mRedPacketInfo.redPacketId);
        }
        mAdBg = (ImageView) view.findViewById(R.id.iv_advert_bg);
        mAdIcon = (ImageView) view.findViewById(R.id.iv_advert_icon);
        mTVSponsor = (TextView) view.findViewById(R.id.tv_ad_sponsor_name);
        mTVGreeting = (TextView) view.findViewById(R.id.tv_ad_receive_greeting);
        mTVMoney = (TextView) view.findViewById(R.id.tv_ad_money);
        mTVHint = (TextView) view.findViewById(R.id.tv_ad_hint);
        mLayoutBom = (RelativeLayout) view.findViewById(R.id.layout_ad_bottom);
        mBtnReceive = (Button) view.findViewById(R.id.btn_click_received);
        mTVLook = (RPTextView) view.findViewById(R.id.tv_check_land);
        mTVShare = (RPTextView) view.findViewById(R.id.tv_check_share);
        mButtonLayout = view.findViewById(R.id.ad_ll_extra);
        mBtnReceive.setOnClickListener(this);
        mTVLook.setOnClickListener(this);
        mTVShare.setOnClickListener(this);
        mIsShare = !(TextUtils.isEmpty(mRedPacketInfo.shareMsg) || TextUtils.isEmpty(mRedPacketInfo.shareUrl));
        showAdPacket();
    }

    private void showAdPacket() {
        adaptationADBanner();
        Glide.with(mContext).load(mBannerUrl).into(new GlideDrawableImageViewTarget(mAdBg) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                super.onResourceReady(resource, animation);
                mPresenter.sendADStatistics(RPConstant.STATISTICS_TYPE_VIEW_AD, mRedPacketInfo.redPacketId);
            }
        });
        Glide.with(mContext).load(mRedPacketInfo.logoURL).transform(new CircleTransform(mContext)).into(mAdIcon);
        mLayoutBom.setBackgroundColor(Color.parseColor(mRedPacketInfo.adBgColor));
        mTVSponsor.setText(String.format(getString(R.string.money_sponsor_username_format), mRedPacketInfo.ownerName));
        if (mRedPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {//未领取
            mTVMoney.setText(mRedPacketInfo.redPacketGreeting);
        } else if (mRedPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVED) {//已被领取
            //myAmount大于0，说明当前用户领取到了红包
            if (Double.parseDouble(mRedPacketInfo.myAmount) > 0) {
                receiveSuccess();
            } else {//红包抢完了
                redPacketOut(getString(R.string.money_is_out));
            }
        } else if (mRedPacketInfo.status == RPConstant.RED_PACKET_STATUS_EXPIRED) {//红包过期
            redPacketOut(getString(R.string.ad_packet_out));
        }
    }

    private void adaptationADBanner() {
        //默认banner选择3x
        if (!TextUtils.isEmpty(mRedPacketInfo.bannerURL3rd)) {
            mBannerUrl = mRedPacketInfo.bannerURL3rd;
        } else if (!TextUtils.isEmpty(mRedPacketInfo.bannerURL2nd)) {
            mBannerUrl = mRedPacketInfo.bannerURL2nd;
        } else if (!TextUtils.isEmpty(mRedPacketInfo.bannerURL1st)) {
            mBannerUrl = mRedPacketInfo.bannerURL1st;
        }

        if (1 < mScreenDensity && mScreenDensity < 3) {//2X
            mBannerUrl = TextUtils.isEmpty(mRedPacketInfo.bannerURL2nd) ? mBannerUrl : mRedPacketInfo.bannerURL2nd;
        } else if (mScreenDensity == 1) {//1X
            mBannerUrl = TextUtils.isEmpty(mRedPacketInfo.bannerURL1st) ? mBannerUrl : mRedPacketInfo.bannerURL1st;
        }
    }

    private void redPacketOut(String msg) {
        mTVMoney.setText(mRedPacketInfo.redPacketGreeting);
        mBtnReceive.setEnabled(false);
        mBtnReceive.setText(msg);
    }

    private void receiveSuccess() {
        mTVGreeting.setVisibility(View.VISIBLE);
        mTVGreeting.setText(mRedPacketInfo.redPacketGreeting);
        mTVMoney.setSingleLine(true);
        mTVMoney.setText(String.format(getString(R.string.detail_money_sign), mRedPacketInfo.myAmount));
        mTVMoney.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 38);
        mBtnReceive.setVisibility(View.GONE);
        mButtonLayout.setVisibility(View.VISIBLE);
        if (!mIsShare) {
            mTVShare.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(mRedPacketInfo.landingPage)) {
            mTVLook.setGravity(Gravity.CENTER);
            mTVLook.setCompoundDrawables(null, null, null, null);
            mTVLook.setText(getString(R.string.ad_check_land_page));
        } else {
            mTVLook.setEnabled(false);
            mTVLook.setText(getString(R.string.ad_receive));
        }
        mTVHint.setText(getString(R.string.money_detail_use));
    }

    @Override
    public void onResume() {
        super.onResume();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, getAdBgHeight());
        mAdBg.setLayoutParams(layoutParams);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_fragment_advert;
    }

    @Override
    public ReceivePacketContract.Presenter<ReceivePacketContract.View> initPresenter() {
        return new ReceivePacketPresenter();
    }

    @Override
    public void onClick(View view) {
        if (ClickUtil.isFastClick()) return;
        if (view.getId() == R.id.btn_click_received) {//领取
            mPresenter.receiveRedPacket(mRedPacketInfo.redPacketId, mRedPacketInfo.redPacketType);
            showLoading();
            mBtnReceive.setEnabled(false);
        }
        if (view.getId() == R.id.tv_check_land) {//着陆页
            mPresenter.sendADStatistics(RPConstant.STATISTICS_TYPE_CLICK_AD, mRedPacketInfo.redPacketId);
            // TODO: handler landingPage
            String msg = "由开发者自行处理着陆页的加载";
            showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, msg, null);
        }
        if (view.getId() == R.id.tv_check_share) {
            showTipDialog(RPConstant.CLIENT_CODE_AD_SHARE_SUCCESS, mRedPacketInfo.shareMsg, this);
        }
    }


    @Override
    public void onReceivePacketSuccess(String redPacketId, String myAmount, String landingPage) {
        hideLoading();
        mBtnReceive.setEnabled(true);
        mRedPacketInfo.myAmount = myAmount;
        mRedPacketInfo.landingPage = landingPage;
        mRedPacketInfo.redPacketId = redPacketId;
        receiveSuccess();
        if (mRedPacketInfo.isPlaySound) {
            RPRedPacketUtil.getInstance().playSound(getActivity());
        }
    }

    @Override
    public void onRedPacketSnappedUp(String redPacketType) {
        hideLoading();
        mBtnReceive.setEnabled(true);
        redPacketOut(getString(R.string.money_is_out));
    }

    @Override
    public void onRedPacketAlreadyReceived() {
        showToastMsg(getString(R.string.red_packet_already_received));
    }

    @Override
    public void onUserUnauthorized(final String authInfo) {
        hideLoading();
        mBtnReceive.setEnabled(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED, getString(R.string.str_authorized_receive_rp), new PayTipsDialogFragment.OnDialogConfirmClickCallback() {
            @Override
            public void onConfirmClick() {
                mBtnReceive.setEnabled(true);
                AliPay aliPay = new AliPay(mContext);
                aliPay.setAuthCallBack(ADPacketFragment.this);
                aliPay.auth(authInfo);
            }
        });
    }

    @Override
    public void onUploadAuthInfoSuccess() {
        hideLoading();
        mBtnReceive.setEnabled(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, getString(R.string.str_ali_auth_success), null);
    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        mBtnReceive.setEnabled(true);
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, message, null);
    }


    @Override
    public void onConfirmClick() {
        // TODO: handler share
        String msg = "点击「分享」按钮，红包SDK将该红包素材内配置的分享链接传递给商户APP，由商户APP自行定义分享渠道完成分享动作。";
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, msg, null);
    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        showLoading();
        mBtnReceive.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.clear(mAdBg);//取消下载
    }

    private int getStatusBarHeight() {
        int statusBarHeight;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = 45;
        }
        return statusBarHeight;
    }

    private int getRealScreenHeight() {
        int height = 0;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            height = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
    }

    public boolean hasNavigationBar() {
        return getRealScreenHeight() != mScreenHeight;
    }

    public int getNavigationBarHeight(Context context) {
        int mNavHeight = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mNavHeight = resources.getDimensionPixelSize(resourceId);
        }
        return mNavHeight;
    }

    private int getAdBgHeight() {
        int height;
        int mTitleHeight = (int) (mScreenDensity * 48 + 0.5f);
        if (hasNavigationBar()) {
            height = mScreenHeight - getStatusBarHeight() - mTitleHeight - getNavigationBarHeight(mContext);
        } else {
            height = mScreenHeight - getStatusBarHeight() - mTitleHeight;
        }
        height = (int) (height * 0.48);
        return height;
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
}
