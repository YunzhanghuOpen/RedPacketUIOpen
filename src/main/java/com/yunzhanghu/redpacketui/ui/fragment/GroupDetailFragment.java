package com.yunzhanghu.redpacketui.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.yunzhanghu.redpacketsdk.bean.PageInfo;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.RedPacketDetailContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.RedPacketDetailPresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.adapter.GroupDetailAdapter;
import com.yunzhanghu.redpacketui.callback.RecyclerItemClickListener;
import com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;
import com.yunzhanghu.redpacketui.utils.NetUtils;
import com.yunzhanghu.redpacketui.utils.PageUtil;

import java.util.ArrayList;

/**
 * Created by max on 16/3/27
 */
public class GroupDetailFragment extends RPBaseFragment<RedPacketDetailContract.View, RedPacketDetailContract.Presenter<RedPacketDetailContract.View>> implements RedPacketDetailContract.View {

    private static final String ARGS_HEADER_INFO = "header_info";

    private static final String ARGS_LIST_INFO = "list_info";

    private static final String ARGS_PAGE_INFO = "page_info";

    private RedPacketInfo mHeaderInfo;

    private ArrayList<RedPacketInfo> mListInfo;

    private LinearLayoutManager mLayoutManager;

    private GroupDetailAdapter mAdapter;

    private int mCurrentPage = 1;

    private int mOffset = 0;

    private int mLength = PageUtil.PAGE_LIMIT;

    private int mTakenPageNo = 0;

    private boolean mIsLoading;

    private String mLatestReceiverId = "";


    public static GroupDetailFragment newInstance(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_HEADER_INFO, headerInfo);
        args.putParcelableArrayList(ARGS_LIST_INFO, listInfo);
        args.putParcelable(ARGS_PAGE_INFO, pageInfo);
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
            mHeaderInfo = getArguments().getParcelable(ARGS_HEADER_INFO);
            mListInfo = getArguments().getParcelableArrayList(ARGS_LIST_INFO);
            PageInfo pageInfo = getArguments().getParcelable(ARGS_PAGE_INFO);
            mOffset = pageInfo != null ? pageInfo.offset : 0;
            mLength = pageInfo != null ? pageInfo.length : PageUtil.PAGE_LIMIT;
            if (mListInfo.size() != 0) {
                mLatestReceiverId = mListInfo.get(0).receiverId;
            }
        }
        mTakenPageNo = PageUtil.getInstance().calculateTotalPages(mHeaderInfo.takenCount);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.money_detail_list);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GroupDetailAdapter(mContext);
        mAdapter.addHeader(mHeaderInfo);
        mAdapter.addAll(mListInfo);
        if (!TextUtils.isEmpty(mHeaderInfo.myAmount)) {
            if (mCurrentPage == mTakenPageNo) {
                //随机红包才有查看记录的按钮
                if (mHeaderInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
                    RedPacketInfo footerInfo = new RedPacketInfo();
                    footerInfo.itemType = 3;
                    mAdapter.addFooter(footerInfo);
                }
            }
        }
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!mIsLoading && mLayoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1 && dy * dy > dx * dx) {
                    if (mCurrentPage < mTakenPageNo) {
                        mCurrentPage++;
                        mOffset = mOffset + PageUtil.PAGE_LIMIT;
                        RedPacketInfo redPacketInfo = new RedPacketInfo();
                        redPacketInfo.itemType = 2;
                        mAdapter.addFooter(redPacketInfo);
                        mIsLoading = true;
                        if (NetUtils.isNetworkConnected(mContext)) {
                            mPresenter.getPacketDetail(mHeaderInfo.redPacketId, mLatestReceiverId, mOffset, mLength);
                        } else {
                            showToastMsg(getString(R.string.no_network_connected));
                            mAdapter.removeFooter(mAdapter.getItemCount() - 1);
                            mIsLoading = false;
                        }
                    }
                }
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int type = mAdapter.getItemViewType(position);
                if (type == 3) {
                    Intent intent = new Intent(getActivity(), RPRecordActivity.class);
                    startActivity(intent);
                }
            }
        }));
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_fragment_group_packet_detail;
    }


    @Override
    public void onSinglePacketDetailSuccess(RedPacketInfo redPacketInfo) {

    }

    @Override
    public void onGroupPacketDetailSuccess(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo) {
        mOffset = pageInfo.offset;
        mLength = pageInfo.length;
        if (!mIsLoading) {
            mAdapter.addAll(listInfo);
        } else {
            mAdapter.removeFooter(mAdapter.getItemCount() - 1);
            mAdapter.addAll(listInfo);
        }
        mIsLoading = false;
        if (!TextUtils.isEmpty(headerInfo.myAmount)) {
            if (mCurrentPage == mTakenPageNo) {
                if (headerInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_GROUP_RANDOM)) {
                    RedPacketInfo footerInfo = new RedPacketInfo();
                    footerInfo.itemType = 3;
                    mAdapter.addFooter(footerInfo);
                }
            }
        }
    }

    @Override
    public void onError(String code, String message) {
        showToastMsg(message);
    }

    @Override
    public RedPacketDetailContract.Presenter<RedPacketDetailContract.View> initPresenter() {
        return new RedPacketDetailPresenter();
    }
}
