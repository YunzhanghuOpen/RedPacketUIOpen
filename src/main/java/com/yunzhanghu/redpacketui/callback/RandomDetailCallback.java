package com.yunzhanghu.redpacketui.callback;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.ReceivePacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.ReceivePacketPresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.ui.fragment.PayTipsDialogFragment;
import com.yunzhanghu.redpacketui.ui.fragment.RandomDetailDialogFragment;
import com.yunzhanghu.redpacketui.utils.RPRedPacketUtil;

/**
 * Created by hhx on 16/10/22
 */
public class RandomDetailCallback implements ReceivePacketContract.View, AliPay.AliAuthCallBack {

    private RPRedPacketUtil.RPOpenPacketCallback mRPOpenPacketCallback;

    private FragmentActivity mFragmentActivity;

    private ReceivePacketPresenter mPresenter;

    private RedPacketInfo mRedPacketInfo;

    public RandomDetailCallback(RedPacketInfo redPacketInfo, RPRedPacketUtil.RPOpenPacketCallback RPOpenPacketCallback, FragmentActivity fragmentActivity) {
        mRedPacketInfo = redPacketInfo;
        mRPOpenPacketCallback = RPOpenPacketCallback;
        mFragmentActivity = fragmentActivity;
    }

    public void receiveRedPacket() {
        mPresenter = new ReceivePacketPresenter();
        mPresenter.attach(this);
        mPresenter.receiveRedPacket(mRedPacketInfo.redPacketId, mRedPacketInfo.redPacketType);
    }

    @Override
    public void onReceivePacketSuccess(String redPacketId, String myAmount, String landingPage) {
        mRPOpenPacketCallback.hideLoading();
        mRedPacketInfo.status = RPConstant.RED_PACKET_STATUS_RECEIVED;
        RandomDetailDialogFragment randomDetailDialogFragment = RandomDetailDialogFragment.newInstance(mRedPacketInfo);
        if (mFragmentActivity != null && !randomDetailDialogFragment.isAdded()) {
            randomDetailDialogFragment.showAllowingStateLost(randomDetailDialogFragment, mFragmentActivity);
            RPRedPacketUtil.getInstance().playSound(mFragmentActivity);
            final RedPacketInfo currentUserInfo = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
            RedPacketInfo redPacketInfo = new RedPacketInfo();
            redPacketInfo.senderId = mRedPacketInfo.senderId;
            redPacketInfo.senderNickname = mRedPacketInfo.senderNickname;
            redPacketInfo.receiverId = currentUserInfo.currentUserId;
            redPacketInfo.receiverNickname = currentUserInfo.currentNickname;
            redPacketInfo.myAmount = myAmount;
            mRPOpenPacketCallback.onSuccess(redPacketInfo);
        }
    }

    @Override
    public void onRedPacketSnappedUp(String redPacketType) {

    }

    @Override
    public void onRedPacketAlreadyReceived() {
        Toast.makeText(mFragmentActivity, mFragmentActivity.getString(R.string.red_packet_already_received), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserUnauthorized(final String authInfo) {
        mRPOpenPacketCallback.hideLoading();
        showTipDialog(RPConstant.CLIENT_CODE_ALI_NO_AUTHORIZED, mFragmentActivity.getString(R.string.str_authorized_receive_rp), new PayTipsDialogFragment.OnDialogConfirmClickCallback() {
            @Override
            public void onConfirmClick() {
                AliPay aliPay = new AliPay(mFragmentActivity);
                aliPay.setAuthCallBack(RandomDetailCallback.this);
                aliPay.auth(authInfo);
            }
        });
    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        mRPOpenPacketCallback.showLoading();
    }

    @Override
    public void onUploadAuthInfoSuccess() {
        mRPOpenPacketCallback.hideLoading();
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, mFragmentActivity.getString(R.string.str_ali_auth_success), null);
    }

    @Override
    public void onError(String code, String message) {
        mRPOpenPacketCallback.hideLoading();
        if (code.equals(RPConstant.CLIENT_CODE_RECEIVE_PACKET_ERROR)) {
            mRPOpenPacketCallback.onError(code, message);
        }
        showTipDialog(RPConstant.CLIENT_CODE_OTHER_ERROR, message, null);
    }


    private void showTipDialog(String code, String msg, PayTipsDialogFragment.OnDialogConfirmClickCallback callback) {
        PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(code, msg);
        dialog.setCallback(callback);
        if (mFragmentActivity != null) {
            FragmentTransaction ft = mFragmentActivity.getSupportFragmentManager().beginTransaction();
            ft.add(dialog, RPConstant.RP_TIP_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }

    public void detach() {
        if (mPresenter != null) {
            mPresenter.detach(true);
            mPresenter = null;
        }
    }
}
