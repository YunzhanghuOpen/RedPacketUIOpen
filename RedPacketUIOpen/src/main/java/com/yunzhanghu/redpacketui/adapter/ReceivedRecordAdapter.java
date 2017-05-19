package com.yunzhanghu.redpacketui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.callback.OnAliUserClickListener;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.DateUtils;

import java.util.ArrayList;

/**
 * Created by max on 14/12/11
 */
public class ReceivedRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 获取支付宝用户信息失败
     */
    public final static int ALI_USER_STATUS_ERROR = -1;
    /**
     * 未绑定支付宝账户
     */
    public final static int ALI_USER_STATUS_UNBOUND = 0;
    /**
     * 已绑定支付宝账户
     */
    public final static int ALI_USER_STATUS_BOUND = 1;

    private ArrayList<RedPacketInfo> mList = new ArrayList<>();

    private static final int TYPE_HEADER = 0;

    private static final int TYPE_ITEM = 1;

    private static final int TYPE_FOOTER = 2;

    private Context mContext;

    private String mCurrentAvatarUrl;

    private String mCurrentUserName;

    private OnAliUserClickListener mOnAliUserClickListener;


    public ReceivedRecordAdapter(Context context, String currentUserName, String currentAvatarUrl, OnAliUserClickListener onAliUserClickListener) {
        mContext = context;
        mCurrentAvatarUrl = currentAvatarUrl;
        if (TextUtils.isEmpty(currentUserName)) {
            mCurrentUserName = "[unknown]";
        } else {
            mCurrentUserName = currentUserName;
        }
        mOnAliUserClickListener = onAliUserClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_received_record_list_header, parent, false);
            holder = new HeaderViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_received_record_list_item, parent, false);
            holder = new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_record_list_footer, parent, false);
            holder = new FooterViewHolder(view);
        }
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            setHeaderViews(headerViewHolder, position);
        } else if (viewType == TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            setItemViews(itemViewHolder, position);
        } else if (viewType == TYPE_FOOTER) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
        }
    }

    private void setHeaderViews(HeaderViewHolder headerViewHolder, int position) {
        final RedPacketInfo redPacketInfo = mList.get(position);
        headerViewHolder.tvUserName.setText(mCurrentUserName);
        if (redPacketInfo.totalCount > 0) {
            headerViewHolder.tvTotalCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_grey));
        } else {
            headerViewHolder.tvTotalCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_light_grey));
        }
        if (redPacketInfo.bestCount > 0) {
            headerViewHolder.tvBestCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_grey));
        } else {
            headerViewHolder.tvBestCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_light_grey));
        }
        headerViewHolder.tvTotalMoney.setText(String.format("￥%s", redPacketInfo.totalMoney));
        headerViewHolder.tvTotalCount.setText(String.valueOf(redPacketInfo.totalCount));
        headerViewHolder.tvBestCount.setText(String.valueOf(redPacketInfo.bestCount));
        //默认状态
        headerViewHolder.tvAliAccount.setText("");
        headerViewHolder.tvAliAccountHint.setVisibility(View.GONE);
        headerViewHolder.llShowAli.setVisibility(View.GONE);
        headerViewHolder.llBindAli.setVisibility(View.GONE);
        switch (redPacketInfo.aliUserStatus) {
            case ALI_USER_STATUS_BOUND:
                headerViewHolder.tvAliAccountHint.setVisibility(View.VISIBLE);
                headerViewHolder.llShowAli.setVisibility(View.VISIBLE);
                headerViewHolder.tvAliAccount.setText(RPPreferenceManager.getInstance().getAliAccount());
                headerViewHolder.llShowAli.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnAliUserClickListener.onAliUserClick(v, false);
                    }
                });
                break;
            case ALI_USER_STATUS_UNBOUND:
                headerViewHolder.llBindAli.setVisibility(View.VISIBLE);
                headerViewHolder.btnBindAli.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnAliUserClickListener.onAliUserClick(v, true);
                    }
                });
                break;
        }
        if (!TextUtils.isEmpty(mCurrentAvatarUrl)) {
            Glide.with(mContext).load(mCurrentAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(headerViewHolder.ivAvatar);
        }
        if (mList.size() == 1) {
            headerViewHolder.tvNoRedPacket.setVisibility(View.VISIBLE);
        }
    }

    private void setItemViews(ItemViewHolder itemViewHolder, int position) {
        RedPacketInfo redPacketInfo = mList.get(position);
        itemViewHolder.tvDate.setText(DateUtils.getDateFormat(redPacketInfo.date));
        itemViewHolder.tvMoney.setText(String.format("%s元", redPacketInfo.redPacketAmount));
        itemViewHolder.tvSenderNickname.setText(redPacketInfo.senderNickname);
        switch (redPacketInfo.redPacketType) {
            case RPConstant.RED_PACKET_TYPE_GROUP_RANDOM:
                itemViewHolder.ivRandomIcon.setVisibility(View.VISIBLE);
                itemViewHolder.ivRandomIcon.setBackgroundResource(R.drawable.rp_random_icon);
                break;
            case RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE:
                itemViewHolder.ivRandomIcon.setVisibility(View.VISIBLE);
                itemViewHolder.ivRandomIcon.setBackgroundResource(R.drawable.rp_exclusive_icon);
                break;
            default:
                itemViewHolder.ivRandomIcon.setVisibility(View.GONE);
                break;
        }
        itemViewHolder.tvMerchantName.setText(redPacketInfo.merchantName);
        if (position == mList.size() - 1) {
            itemViewHolder.vLine.setVisibility(View.VISIBLE);
            itemViewHolder.vLineNoHead.setVisibility(View.GONE);
        } else {
            itemViewHolder.vLine.setVisibility(View.GONE);
            itemViewHolder.vLineNoHead.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(redPacketInfo.senderAvatarUrl)) {
            redPacketInfo.senderAvatarUrl = "none";
        }
        Glide.with(mContext).load(redPacketInfo.senderAvatarUrl)
                .error(R.drawable.rp_avatar)
                .placeholder(R.drawable.rp_avatar)
                .transform(new CircleTransform(mContext))
                .into(itemViewHolder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mList.get(position).itemType;
    }

    public void addFooter(RedPacketInfo redPacketInfo) {
        mList.add(redPacketInfo);
        notifyItemInserted(mList.size() - 1);
    }

    public void addHeader(RedPacketInfo redPacketInfo) {
        mList.add(redPacketInfo);
        notifyItemInserted(0);
    }

    public void notifyAliUserStatusChanged(int userStatus) {
        RedPacketInfo redPacketInfo = mList.get(0);
        redPacketInfo.aliUserStatus = userStatus;
        notifyItemChanged(0);
    }


    public void addAll(ArrayList<RedPacketInfo> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void removeFooter(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvTotalCount;
        TextView tvTotalMoney;
        TextView tvAliAccountHint;
        TextView tvAliAccount;
        ImageView ivAvatar;
        ImageView ivUnbindAli;
        TextView tvBestCount;
        TextView tvNoRedPacket;
        Button btnBindAli;
        LinearLayout llBindAli;
        LinearLayout llShowAli;


        HeaderViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvUserName = (TextView) v.findViewById(R.id.tv_username);
            tvTotalCount = (TextView) v.findViewById(R.id.tv_received_count);
            tvTotalMoney = (TextView) v.findViewById(R.id.tv_received_money_amount);
            tvAliAccountHint = (TextView) v.findViewById(R.id.tv_ali_account_hint);
            tvAliAccount = (TextView) v.findViewById(R.id.tv_ali_account_name);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
            ivUnbindAli = (ImageView) v.findViewById(R.id.iv_unbind_ali_account);
            tvBestCount = (TextView) v.findViewById(R.id.tv_best_count);
            tvNoRedPacket = (TextView) v.findViewById(R.id.tv_record_no_rp);
            llBindAli = (LinearLayout) v.findViewById(R.id.ll_bind_ali);
            btnBindAli = (Button) v.findViewById(R.id.btn_bind_ali_account);
            llShowAli = (LinearLayout) v.findViewById(R.id.ll_show_ali);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderNickname;
        TextView tvMoney;
        TextView tvDate;
        TextView tvMerchantName;
        ImageView ivAvatar;
        ImageView ivRandomIcon;
        View vLine;
        View vLineNoHead;

        ItemViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvSenderNickname = (TextView) v.findViewById(R.id.tv_sender_nickname);
            tvMoney = (TextView) v.findViewById(R.id.tv_item_money_amount);
            tvDate = (TextView) v.findViewById(R.id.tv_time);
            tvMerchantName = (TextView) v.findViewById(R.id.tv_item_merchant_name);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_record_sender_avatar);
            ivRandomIcon = (ImageView) v.findViewById(R.id.iv_random_icon);
            vLine = v.findViewById(R.id.view_line);
            vLineNoHead = v.findViewById(R.id.view_line_no_head);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        TextView tvLoadingMsg;

        FooterViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvLoadingMsg = (TextView) v.findViewById(R.id.tv_loading_msg);
        }
    }
}
