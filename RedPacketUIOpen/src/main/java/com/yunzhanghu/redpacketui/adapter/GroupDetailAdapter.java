package com.yunzhanghu.redpacketui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.DateUtils;

import java.util.ArrayList;

/**
 * Created by max on 14/12/11
 */
public class GroupDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RedPacketInfo> mList = new ArrayList<>();

    private static final int TYPE_HEADER = 0;

    private static final int TYPE_ITEM = 1;

    private static final int TYPE_FOOTER = 2;

    private static final int TYPE_CHECK = 3;

    private Context mContext;


    public GroupDetailAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_details_list_header, parent, false);
            holder = new HeaderViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_details_list_item, parent, false);
            holder = new ItemViewHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_record_list_footer, parent, false);
            holder = new FooterViewHolder(view);
        } else if (viewType == TYPE_CHECK) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_details_list_footer, parent, false);
            holder = new CheckViewHolder(view);
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
        } else if (viewType == TYPE_CHECK) {
            CheckViewHolder checkViewHolder = (CheckViewHolder) holder;
        }
    }

    private void setHeaderViews(HeaderViewHolder headerViewHolder, int position) {
        RedPacketInfo redPacketInfo = mList.get(position);
        String messageDirect = redPacketInfo.messageDirect;
        headerViewHolder.tvUserName.setText(redPacketInfo.senderNickname);
        if (!TextUtils.isEmpty(redPacketInfo.senderAvatarUrl)) {
            Glide.with(mContext).load(redPacketInfo.senderAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(headerViewHolder.ivAvatar);
        }
        headerViewHolder.tvGreeting.setText(redPacketInfo.redPacketGreeting);
        if (TextUtils.isEmpty(redPacketInfo.myAmount)) {//没抢到
            headerViewHolder.tvMoneyAmount.setVisibility(View.GONE);
            headerViewHolder.tvMoneyUse.setVisibility(View.GONE);
        } else {//抢到了
            headerViewHolder.tvMoneyAmount.setVisibility(View.VISIBLE);
            headerViewHolder.tvMoneyAmount.setText(String.format("￥%s", redPacketInfo.myAmount));
            headerViewHolder.tvMoneyUse.setVisibility(View.VISIBLE);
        }
        switch (redPacketInfo.redPacketType) {
            case RPConstant.RED_PACKET_TYPE_GROUP_RANDOM:
                headerViewHolder.ivGroupIcon.setVisibility(View.VISIBLE);
                headerViewHolder.ivGroupIcon.setBackgroundResource(R.drawable.rp_random_icon);
                break;
            case RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE:
                headerViewHolder.ivGroupIcon.setVisibility(View.VISIBLE);
                headerViewHolder.ivGroupIcon.setBackgroundResource(R.drawable.rp_exclusive_icon);
                break;
            default:
                headerViewHolder.ivGroupIcon.setVisibility(View.GONE);
                break;
        }
        String redPacketStatus = "";
        if (redPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {
            if (messageDirect.equals(RPConstant.MESSAGE_DIRECT_SEND)) {
                redPacketStatus = String.format(mContext.getString(R.string.group_money_available_sender), redPacketInfo.takenCount + "", redPacketInfo.totalCount + "", redPacketInfo.takenMoney, redPacketInfo.redPacketAmount);
            } else {
                redPacketStatus = String.format(mContext.getString(R.string.group_money_available_receiver), redPacketInfo.takenCount + "", redPacketInfo.totalCount + "");
            }
        } else if (redPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVED) {
            if (redPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
                if (messageDirect.equals(RPConstant.MESSAGE_DIRECT_SEND)) {
                    redPacketStatus = String.format(mContext.getString(R.string.group_money_unavailable_rand_sender), redPacketInfo.totalCount + "", redPacketInfo.redPacketAmount, redPacketInfo.timeLength);
                } else {
                    redPacketStatus = String.format(mContext.getString(R.string.group_money_unavailable_rand_receiver), redPacketInfo.totalCount + "", redPacketInfo.timeLength);
                }
            } else {
                if (messageDirect.equals(RPConstant.MESSAGE_DIRECT_SEND)) {
                    redPacketStatus = String.format(mContext.getString(R.string.group_money_unavailable_avg_sender), redPacketInfo.totalCount + "", redPacketInfo.redPacketAmount);
                } else {
                    redPacketStatus = String.format(mContext.getString(R.string.group_money_unavailable_avg_receiver), redPacketInfo.totalCount + "");
                }
            }
        } else if (redPacketInfo.status == RPConstant.RED_PACKET_STATUS_EXPIRED) {
            redPacketStatus = String.format(mContext.getString(R.string.group_money_expired), redPacketInfo.takenCount + "", redPacketInfo.totalCount + "", redPacketInfo.takenMoney, redPacketInfo.redPacketAmount);
        }
        headerViewHolder.tvMoneyStatus.setText(redPacketStatus);
    }

    private void setItemViews(ItemViewHolder itemViewHolder, int position) {
        RedPacketInfo redPacketInfo = mList.get(position);
        itemViewHolder.tvUserName.setText(redPacketInfo.receiverNickname);
        itemViewHolder.tvMoneyAmount.setText(String.format(mContext.getString(R.string.money_detail_money_unit), redPacketInfo.redPacketAmount));
        itemViewHolder.tvDate.setText(DateUtils.getDateFormat(redPacketInfo.date));
        if (TextUtils.isEmpty(redPacketInfo.receiverAvatarUrl)) {
            redPacketInfo.receiverAvatarUrl = "none";
        }
        Glide.with(mContext).load(redPacketInfo.receiverAvatarUrl)
                .error(R.drawable.rp_avatar)
                .placeholder(R.drawable.rp_avatar)
                .transform(new CircleTransform(mContext))
                .into(itemViewHolder.ivAvatar);
        if (redPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
            if (redPacketInfo.isBest) {
                itemViewHolder.tvBestLuck.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.tvBestLuck.setVisibility(View.GONE);
            }
        }
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


    public void addAll(ArrayList<RedPacketInfo> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void removeFooter(int i) {
        mList.remove(i);
        notifyItemRemoved(i);
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvGreeting;
        TextView tvMoneyAmount;
        TextView tvMoneyStatus;
        ImageView ivAvatar;
        TextView tvMoneyUse;
        ImageView ivGroupIcon;

        HeaderViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvUserName = (TextView) v.findViewById(R.id.tv_money_sender);
            tvGreeting = (TextView) v.findViewById(R.id.tv_greeting);
            tvMoneyStatus = (TextView) v.findViewById(R.id.tv_money_status);
            tvMoneyAmount = (TextView) v.findViewById(R.id.tv_money_amount);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
            tvMoneyUse = (TextView) v.findViewById(R.id.tv_money_use);
            ivGroupIcon = (ImageView) v.findViewById(R.id.iv_group_random);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUserName;
        TextView tvDate;
        TextView tvMoneyAmount;
        TextView tvBestLuck;

        ItemViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            ivAvatar = (ImageView) v.findViewById(R.id.iv_item_avatar_icon);
            tvUserName = (TextView) v.findViewById(R.id.tv_money_to_user);
            tvDate = (TextView) v.findViewById(R.id.tv_time);
            tvMoneyAmount = (TextView) v.findViewById(R.id.tv_item_money_amount);
            tvBestLuck = (TextView) v.findViewById(R.id.tv_best_icon);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
        }
    }

    private static class CheckViewHolder extends RecyclerView.ViewHolder {

        CheckViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
        }
    }
}
