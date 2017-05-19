package com.yunzhanghu.redpacketui.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.ReceivePacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.ReceivePacketPresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.ui.base.RPBaseDialogFragment;
import com.yunzhanghu.redpacketui.utils.CircleTransform;

import java.io.UnsupportedEncodingException;

/**
 * Created by hhx on 16/10/22
 */
public class RandomDetailDialogFragment extends RPBaseDialogFragment implements View.OnClickListener {

    private RedPacketInfo mRandomDetail;

    private View mAvatarView;

    public static RandomDetailDialogFragment newInstance(RedPacketInfo redPacketInfo) {
        RandomDetailDialogFragment frag = new RandomDetailDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        frag.setArguments(args);
        return frag;
    }

    public void setArguments(RedPacketInfo redPacketInfo) {
        Bundle args = new Bundle();
        args.putParcelable(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRandomDetail = getArguments().getParcelable(RPConstant.EXTRA_RED_PACKET_INFO);
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
        return R.layout.rp_random_detail_dialog;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.ll_random_detail);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        View closeLayout = view.findViewById(R.id.rl_random_detail_closed);
        TextView toUserName = (TextView) view.findViewById(R.id.tv_random_detail_username);
        TextView randomGreeting = (TextView) view.findViewById(R.id.tv_random_detail_greeting);
        mAvatarView = view.findViewById(R.id.layout_random_detail_avatar);
        ImageView toAvatar = (ImageView) view.findViewById(R.id.iv_random_detail_avatar);
        TextView randomAmount = (TextView) view.findViewById(R.id.tv_random_detail_amount);
        TextView randomState = (TextView) view.findViewById(R.id.tv_random_detail_state);
        LinearLayout randomFromLayout = (LinearLayout) view.findViewById(R.id.ll_random_detail_switch);
        ImageView fromAvatar = (ImageView) view.findViewById(R.id.iv_random_detail_from_icon);
        TextView fromUserName = (TextView) view.findViewById(R.id.tv_random_detail_from_name);
        int status = mRandomDetail.status;

        closeLayout.setOnClickListener(this);
        if (mRandomDetail.messageDirect.equals(RPConstant.MESSAGE_DIRECT_RECEIVE)) {
            randomFromLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mRandomDetail.senderAvatarUrl)) {
                Glide.with(mContext).load(mRandomDetail.senderAvatarUrl)
                        .error(R.drawable.rp_avatar)
                        .placeholder(R.drawable.rp_avatar)
                        .transform(new CircleTransform(mContext))
                        .into(fromAvatar);
            }
            if (!TextUtils.isEmpty(mRandomDetail.senderNickname)) {
                String senderNickname = calculateNameByte(mRandomDetail.senderNickname);
                if (senderNickname.length() < mRandomDetail.senderNickname.length()) {
                    senderNickname = senderNickname + "...";
                }
                fromUserName.setText(String.format(mContext.getString(R.string.random_from_username), senderNickname));
            }
            if (status == RPConstant.RED_PACKET_STATUS_RECEIVED) {//已经领过
                randomState.setText(R.string.random_status_taken_receive);
            } else if (status == RPConstant.RED_PACKET_STATUS_EXPIRED) {
                randomState.setText(R.string.random_status_out);
            }
        } else {
            randomFromLayout.setVisibility(View.GONE);
            if (status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {//可领取
                randomState.setText(R.string.random_status_no_taken);
            } else if (status == RPConstant.RED_PACKET_STATUS_RECEIVED) {//已经领过
                randomState.setText(R.string.random_status_taken);
            } else if (status == RPConstant.RED_PACKET_STATUS_EXPIRED) {
                randomState.setText(R.string.random_status_out);
            }
        }

        if (!TextUtils.isEmpty(mRandomDetail.receiverNickname)) {
            String receiverNickname = calculateNameByte(mRandomDetail.receiverNickname);
            if (receiverNickname.length() < mRandomDetail.receiverNickname.length()) {
                receiverNickname = receiverNickname + "...";
            }
            toUserName.setText(String.format(mContext.getString(R.string.random_to_username), receiverNickname));
        }
        if (!TextUtils.isEmpty(mRandomDetail.receiverAvatarUrl)) {
            Glide.with(mContext).load(mRandomDetail.receiverAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(toAvatar);
        }
        if (!TextUtils.isEmpty(mRandomDetail.redPacketAmount)) {
            randomAmount.setText(String.format(getString(R.string.detail_money_sign), mRandomDetail.redPacketAmount));
        }
        if (!TextUtils.isEmpty(mRandomDetail.redPacketGreeting)) {
            randomGreeting.setText(mRandomDetail.redPacketGreeting);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_random_detail_closed) {
            dismiss();
        }
    }
}
