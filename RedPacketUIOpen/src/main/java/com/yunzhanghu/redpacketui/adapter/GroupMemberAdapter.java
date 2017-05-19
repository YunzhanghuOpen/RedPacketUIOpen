
package com.yunzhanghu.redpacketui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.indexrecyclerview.StickyRecyclerHeadersAdapter;
import com.yunzhanghu.redpacketui.utils.CircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by desert on 16/6/10
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ContactViewHolder>
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private List<RPUserBean> mList;

    private OnItemClickListener mOnItemClickListener;

    private Context mContext;

    public GroupMemberAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    @Override
    public GroupMemberAdapter.ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rp_group_member_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupMemberAdapter.ContactViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.mTvName.setText(mList.get(position).userNickname);
        int drawableId = R.drawable.rp_avatar;
        if (position == 0) {
            drawableId = R.drawable.rp_group_everyone;
        }
        Glide.with(mContext).load(mList.get(position).userAvatar)
                .error(drawableId)
                .placeholder(drawableId)
                .transform(new CircleTransform(mContext))
                .into(holder.mIvAvatar);
        if (mList.size() > (position + 1) && mList.get(position).sortLetters.equals(mList.get(position + 1).sortLetters)) {
            holder.mLine.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mList.get(position), position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getHeaderId(int position) {

        return mList.get(position).sortLetters.charAt(0);

    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rp_group_member_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue = String.valueOf(mList.get(position).sortLetters.charAt(0));
        if ("$".equals(showValue)) {
            textView.setText("任何人");
        } else {
            textView.setText(showValue);
        }

    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mList.get(i).sortLetters;
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }

    public void addAll(List<RPUserBean> data) {
        if (mList.size() > 0) {
            mList.clear();
        }
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public ArrayList<RPUserBean> getAll() {
        return (ArrayList<RPUserBean>) mList;
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView mTvName;
        ImageView mIvAvatar;
        View mLine;

        ContactViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.item_name);
            mIvAvatar = (ImageView) itemView.findViewById(R.id.item_image);
            mLine = itemView.findViewById(R.id.item_line);

        }


    }

    public interface OnItemClickListener {
        void onItemClick(RPUserBean mBean, int position);
    }
}
