package com.yunzhanghu.redpacketui.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.presenter.IBasePresenter;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.DateUtils;

/**
 * Created by max on 16/3/27
 */
public class SingleDetailFragment extends RPBaseFragment implements View.OnClickListener {

    private static final String ARGS_RED_PACKET_DETAIL = "red_packet_detail";

    private RedPacketInfo mRedPacketInfo = new RedPacketInfo();

    public static SingleDetailFragment newInstance(RedPacketInfo redPacketInfo) {
        SingleDetailFragment fragment = new SingleDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_DETAIL, redPacketInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return null;
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(ARGS_RED_PACKET_DETAIL);
        }
        ImageView ivBg = (ImageView) view.findViewById(R.id.iv_detail_bg);
        ImageView mIvSenderAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        ImageView mIvIcon = (ImageView) view.findViewById(R.id.iv_group_random);
        TextView mTvSender = (TextView) view.findViewById(R.id.tv_money_sender);
        TextView mTvGreeting = (TextView) view.findViewById(R.id.tv_greeting);
        TextView mTvMoneyAmount = (TextView) view.findViewById(R.id.tv_money_amount);
        TextView mTvMoneyUse = (TextView) view.findViewById(R.id.tv_money_use);
        TextView tvCheckRecords = (TextView) view.findViewById(R.id.tv_check_records);
        TextView mTvMoneyStatus = (TextView) view.findViewById(R.id.tv_money_status);
        View statusLayout = view.findViewById(R.id.status_layout);
        View mLayoutItem = view.findViewById(R.id.layout_item);
        ImageView mIvReceiverAvatar = (ImageView) view.findViewById(R.id.iv_item_avatar_icon);
        TextView mTvReceiver = (TextView) view.findViewById(R.id.tv_money_to_user);
        TextView mTvTime = (TextView) view.findViewById(R.id.tv_time);
        TextView mTvItemAmount = (TextView) view.findViewById(R.id.tv_item_money_amount);
        tvCheckRecords.setOnClickListener(this);
        if (!TextUtils.isEmpty(RPPreferenceManager.getInstance().getBgUrl())) {
            Glide.with(mContext).load(RPPreferenceManager.getInstance().getBgUrl())
                    .error(R.drawable.rp_open_packet_bg)
                    .into(ivBg);
        }
        if (mRedPacketInfo.messageDirect.equals(RPConstant.MESSAGE_DIRECT_SEND)) {
            //红包发送者看到的红包详情内容
            mTvSender.setText(mRedPacketInfo.senderNickname);
            mTvGreeting.setText(mRedPacketInfo.redPacketGreeting);
            if (mRedPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {
                mTvMoneyStatus.setText(String.format(getResources().getString(R.string.money_status_no_taken), mRedPacketInfo.redPacketAmount));
                mLayoutItem.setVisibility(View.GONE);
            } else if (mRedPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVED) {
                mTvMoneyStatus.setText(String.format(getResources().getString(R.string.money_status_taken), mRedPacketInfo.redPacketAmount));
                mLayoutItem.setVisibility(View.VISIBLE);
            } else {
                mTvMoneyStatus.setText(String.format(getResources().getString(R.string.money_status_expired), mRedPacketInfo.redPacketAmount));
                mLayoutItem.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(mRedPacketInfo.senderAvatarUrl)) {
                Glide.with(mContext).load(mRedPacketInfo.senderAvatarUrl)
                        .error(R.drawable.rp_avatar)
                        .placeholder(R.drawable.rp_avatar)
                        .transform(new CircleTransform(mContext))
                        .into(mIvSenderAvatar);
            }
            if (!TextUtils.isEmpty(mRedPacketInfo.receiverAvatarUrl)) {
                Glide.with(mContext).load(mRedPacketInfo.receiverAvatarUrl)
                        .error(R.drawable.rp_avatar)
                        .placeholder(R.drawable.rp_avatar)
                        .transform(new CircleTransform(mContext))
                        .into(mIvReceiverAvatar);
            }
            mTvReceiver.setText(mRedPacketInfo.receiverNickname);
            mTvTime.setText(DateUtils.getDateFormat(mRedPacketInfo.date));
            mTvItemAmount.setText(String.format(getString(R.string.detail_money_sign), mRedPacketInfo.redPacketAmount));
            mTvMoneyAmount.setVisibility(View.GONE);
            statusLayout.setVisibility(View.VISIBLE);
            mTvMoneyUse.setVisibility(View.GONE);
            tvCheckRecords.setVisibility(View.GONE);
        } else if (mRedPacketInfo.messageDirect.equals(RPConstant.MESSAGE_DIRECT_RECEIVE)) {
            //红包接收者看到的红包详情内容
            statusLayout.setVisibility(View.GONE);
            mLayoutItem.setVisibility(View.GONE);
            mTvMoneyUse.setVisibility(View.VISIBLE);
            mTvSender.setText(mRedPacketInfo.senderNickname);
            mTvGreeting.setText(mRedPacketInfo.redPacketGreeting);
            if (!TextUtils.isEmpty(mRedPacketInfo.redPacketType) && mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE)) {
                mTvMoneyAmount.setText(String.format(getString(R.string.detail_money_sign), mRedPacketInfo.myAmount));
            } else {
                mTvMoneyAmount.setText(String.format(getString(R.string.detail_money_sign), mRedPacketInfo.redPacketAmount));
            }
            //专属红包接受者显示
            if (!TextUtils.isEmpty(mRedPacketInfo.redPacketType) && mRedPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE)) {
                mIvIcon.setVisibility(View.VISIBLE);
                mIvIcon.setBackgroundResource(R.drawable.rp_exclusive_icon);
            }
            if (!TextUtils.isEmpty(mRedPacketInfo.senderAvatarUrl)) {
                Glide.with(mContext).load(mRedPacketInfo.senderAvatarUrl)
                        .error(R.drawable.rp_avatar)
                        .placeholder(R.drawable.rp_avatar)
                        .transform(new CircleTransform(mContext))
                        .into(mIvSenderAvatar);
            }
        }
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_fragment_single_packet_detail;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_check_records) {
            //跳转到红包记录页面
            Intent intent = new Intent(getActivity(), RPRecordActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public IBasePresenter initPresenter() {
        return null;
    }
}
