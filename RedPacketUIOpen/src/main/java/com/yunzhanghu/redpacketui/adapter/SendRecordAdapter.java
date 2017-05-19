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
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.utils.CircleTransform;
import com.yunzhanghu.redpacketui.utils.DateUtils;

import java.util.ArrayList;

/**
 * Created by max on 14/12/11
 */
public class SendRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<RedPacketInfo> mList = new ArrayList<>();

    private static final int TYPE_HEADER = 0;

    private static final int TYPE_ITEM = 1;

    private static final int TYPE_FOOTER = 2;

    private Context mContext;

    private String mCurrentAvatarUrl;

    private String mCurrentUserName;


    public SendRecordAdapter(Context context, String currentUserName, String currentAvatarUrl) {
        mContext = context;
        mCurrentAvatarUrl = currentAvatarUrl;
        if (TextUtils.isEmpty(currentUserName)) {
            mCurrentUserName = "[unknown]";
        } else {
            mCurrentUserName = currentUserName;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_send_record_list_header, parent, false);
            holder = new HeaderViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rp_send_record_list_item, parent, false);
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
        RedPacketInfo redPacketInfo = mList.get(position);
        headerViewHolder.tvUserName.setText(mCurrentUserName);
        headerViewHolder.tvTotalMoney.setText(String.format("￥%s", redPacketInfo.totalMoney));
        headerViewHolder.tvTotalCount.setText(String.valueOf(redPacketInfo.totalCount));
        if (!TextUtils.isEmpty(mCurrentAvatarUrl)) {
            Glide.with(mContext).load(mCurrentAvatarUrl)
                    .error(R.drawable.rp_avatar)
                    .placeholder(R.drawable.rp_avatar)
                    .transform(new CircleTransform(mContext))
                    .into(headerViewHolder.ivAvatar);
        }
    }

    private void setItemViews(ItemViewHolder itemViewHolder, int position) {
        RedPacketInfo redPacketInfo = mList.get(position);
        itemViewHolder.tvDate.setText(DateUtils.getDateFormat(redPacketInfo.date));
        itemViewHolder.tvMoney.setText(String.format("%s元", redPacketInfo.redPacketAmount));
        itemViewHolder.tvMoneyType.setText(redPacketInfo.redPacketType);
        itemViewHolder.tvMerchantName.setText(redPacketInfo.merchantName);
        String status;
        if (redPacketInfo.takenCount == redPacketInfo.totalCount) {
            status = mContext.getString(R.string.money_record_status_all);
        } else {
            status = mContext.getString(R.string.money_record_status);
        }
        if (position == mList.size() - 1) {
            itemViewHolder.vLine.setVisibility(View.VISIBLE);
            itemViewHolder.vLineNoHead.setVisibility(View.GONE);
        } else {
            itemViewHolder.vLine.setVisibility(View.GONE);
            itemViewHolder.vLineNoHead.setVisibility(View.VISIBLE);
        }
        itemViewHolder.tvMoneyStatus.setText(String.format(status, redPacketInfo.takenCount, redPacketInfo.totalCount));
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
        TextView tvTotalCount;
        TextView tvTotalMoney;
        ImageView ivAvatar;

        HeaderViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvUserName = (TextView) v.findViewById(R.id.tv_username);
            tvTotalCount = (TextView) v.findViewById(R.id.tv_send_money_count);
            tvTotalMoney = (TextView) v.findViewById(R.id.tv_send_money_amount);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoneyType;
        TextView tvMoney;
        TextView tvDate;
        TextView tvMoneyStatus;
        TextView tvMerchantName;
        View vLine;
        View vLineNoHead;

        ItemViewHolder(View v) {
            super(v);
            initView(v);
        }

        private void initView(View v) {
            tvMoneyType = (TextView) v.findViewById(R.id.tv_money_type);
            tvMoney = (TextView) v.findViewById(R.id.tv_item_money_amount);
            tvDate = (TextView) v.findViewById(R.id.tv_time);
            tvMoneyStatus = (TextView) v.findViewById(R.id.tv_item_status);
            tvMerchantName = (TextView) v.findViewById(R.id.tv_merchant_name);
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
