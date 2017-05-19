package com.yunzhanghu.redpacketui.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;

import com.yunzhanghu.redpacketsdk.RPTokenCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.ui.base.RPBaseActivity;
import com.yunzhanghu.redpacketui.ui.fragment.ReceivedRecordFragment;
import com.yunzhanghu.redpacketui.ui.fragment.SendRecordFragment;
import com.yunzhanghu.redpacketui.widget.ChooseRecordPopupWindow;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;


/**
 * Created by max on 16/2/26
 */
public class RPRecordActivity extends RPBaseActivity implements RPTokenCallback {

    private ChooseRecordPopupWindow mPopupWindow;

    private ProgressBar mProgressBar;

    private RPTitleBar mTitleBar;

    private int mTokenType = 0;//0第一次初始化Token,1重试操作初始化Token

    private int offsetY;

    private String mCurrentUserId;

    private String mCurrentUserName;

    private String mCurrentAvatarUrl;

    @Override
    protected void getBundleExtras(Bundle extras) {
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_activity_record;
    }

    @Override
    protected View getLoadingTargetView() {
        return findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(Bundle savedInstanceState) {
        RedPacketInfo currentUserInfo = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
        mCurrentUserId = currentUserInfo.currentUserId;
        mCurrentUserName = currentUserInfo.currentNickname;
        mCurrentAvatarUrl = currentUserInfo.currentAvatarUrl;
        mTitleBar = (RPTitleBar) findViewById(R.id.title_bar);
        final int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, mContext.getResources().getDisplayMetrics());
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        setSubTitle();
        mTitleBar.setRightImageLayoutVisibility(View.GONE);
        mTitleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setRightTextLayoutVisibility(View.VISIBLE);
        mTitleBar.setRightTextLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow == null) {
                    mPopupWindow = new ChooseRecordPopupWindow(mContext, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.tv_choose_send) {
                                mTitleBar.setTitle(mContext.getString(R.string.money_send_text));
                                getSupportFragmentManager().beginTransaction().replace(R.id.record_fragment_container, SendRecordFragment.newInstance(mCurrentUserName, mCurrentAvatarUrl)).commit();
                            }
                            if (v.getId() == R.id.tv_choose_received) {
                                mTitleBar.setTitle(mContext.getString(R.string.money_received_text));
                                getSupportFragmentManager().beginTransaction().replace(R.id.record_fragment_container, ReceivedRecordFragment.newInstance(mCurrentUserName, mCurrentAvatarUrl)).commit();
                            }
                            mPopupWindow.dismiss();
                        }
                    });
                    int[] location = new int[2];
                    mTitleBar.getLocationOnScreen(location);
                    offsetY = location[1] + mTitleBar.getHeight();
                }
                mPopupWindow.showAtLocation(mTitleBar, Gravity.END | Gravity.TOP, dp, offsetY + dp);
            }
        });
        showLoading();
        RedPacket.getInstance().initRPToken(mCurrentUserId, this);
    }

    private void setSubTitle() {
        if (TextUtils.isEmpty(RPPreferenceManager.getInstance().getOwnerName())) {
            mTitleBar.setSubTitleVisibility(View.GONE);
        } else {
            mTitleBar.setSubTitleVisibility(View.VISIBLE);
            String subTitle = String.format(getString(R.string.subtitle_content), RPPreferenceManager.getInstance().getOwnerName());
            mTitleBar.setSubTitle(subTitle);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void showProgressBar() {
        if (mProgressBar.getVisibility() == View.GONE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return false;
    }

    @Override
    public void onTokenSuccess() {
        hideLoading();
        if (!isFinishing()) {
            getSupportFragmentManager().beginTransaction().add(R.id.record_fragment_container, ReceivedRecordFragment.newInstance(mCurrentUserName, mCurrentAvatarUrl)).commitAllowingStateLoss();
        }

    }

    @Override
    public void onSettingSuccess() {
        setSubTitle();
    }

    @Override
    public void onError(String errorCode, String errorMsg) {
        if (mTokenType == 0) {
            hideLoading();
            mTokenType = 1;
            toggleShowError(true, "", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showProgressBar();
                    RedPacket.getInstance().initRPToken(mCurrentUserId, RPRecordActivity.this);
                }
            });
        } else {//重试操作
            hideProgressBar();
            showToastMsg(errorMsg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RedPacket.getInstance().detachTokenPresenter();
        RPPreferenceManager.getInstance().setAliAccount("");
    }
}
