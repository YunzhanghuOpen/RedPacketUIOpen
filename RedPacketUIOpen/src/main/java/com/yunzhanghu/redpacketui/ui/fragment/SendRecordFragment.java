package com.yunzhanghu.redpacketui.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yunzhanghu.redpacketsdk.bean.PageInfo;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.RPRecordContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.RPRecordPresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.adapter.SendRecordAdapter;
import com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;
import com.yunzhanghu.redpacketui.utils.NetUtils;
import com.yunzhanghu.redpacketui.utils.PageUtil;

import java.util.ArrayList;

/**
 * Created by max on 16/3/27
 */
public class SendRecordFragment extends RPBaseFragment<RPRecordContract.View, RPRecordContract.Presenter<RPRecordContract.View>> implements RPRecordContract.View {

    private final static String ARGS_USER_NAME = "user_name";

    private final static String ARGS_USER_AVATAR = "user_avatar";

    private int mCurrentPage = 1;

    private int mOffset = 0;

    private int mLength = PageUtil.PAGE_LIMIT;

    private int mTotalPageNo = 0;

    private LinearLayoutManager mLayoutManager;

    private SendRecordAdapter mAdapter;

    private boolean mIsLoading;

    private String mCurrentUserName;

    private String mCurrentAvatarUrl;

    public static SendRecordFragment newInstance(String currentUserName, String currentAvatarUrl) {
        SendRecordFragment fragment = new SendRecordFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_USER_NAME, currentUserName);
        args.putString(ARGS_USER_AVATAR, currentAvatarUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentUserName = getArguments().getString(ARGS_USER_NAME);
            mCurrentAvatarUrl = getArguments().getString(ARGS_USER_AVATAR);
        }
    }

    @Override
    public RPRecordContract.Presenter<RPRecordContract.View> initPresenter() {
        return new RPRecordPresenter(RPConstant.RECORD_TAG_SEND);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.record_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mAdapter = new SendRecordAdapter(mContext, mCurrentUserName, mCurrentAvatarUrl);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!mIsLoading && mLayoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1 && dy * dy > dx * dx) {
                    if (mCurrentPage < mTotalPageNo) {
                        mCurrentPage++;
                        mOffset = mOffset + PageUtil.PAGE_LIMIT;
                        RedPacketInfo redPacketInfo = new RedPacketInfo();
                        redPacketInfo.itemType = 2;
                        mAdapter.addFooter(redPacketInfo);
                        mIsLoading = true;
                        if (NetUtils.isNetworkConnected(mContext)) {
                            mPresenter.getRecordList(RPConstant.EVENT_LOAD_MORE_DATA, mOffset, mLength);
                        } else {
                            showToastMsg(getString(R.string.error_not_net_connect));
                            mAdapter.removeFooter(mAdapter.getItemCount() - 1);
                            mIsLoading = false;
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        mPresenter.getRecordList(RPConstant.EVENT_REFRESH_DATA, mOffset, mLength);
        showLoading();
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_record_fragment;
    }

    @Override
    public void onAliUserInfoSuccess(String userInfo, boolean isRefreshed) {

    }

    @Override
    public void onUserUnauthorized(String authInfo) {

    }

    @Override
    public void onUploadAuthInfoSuccess() {

    }

    @Override
    public void onRecordListSuccess(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo) {
        hideLoading();
        if (getActivity() != null) {
            ((RPRecordActivity) getActivity()).hideProgressBar();
        }
        mAdapter.addHeader(headerInfo);
        mAdapter.addAll(listInfo);
        mLength = pageInfo.length;
        mOffset = pageInfo.offset;
        mTotalPageNo = PageUtil.getInstance().calculateTotalPages(headerInfo.totalCount);
    }

    @Override
    public void onMoreRecordListSuccess(ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo) {
        mLength = pageInfo.length;
        mOffset = pageInfo.offset;
        if (!mIsLoading) {
            mAdapter.addAll(listInfo);
        } else {
            mAdapter.removeFooter(mAdapter.getItemCount() - 1);
            mAdapter.addAll(listInfo);
        }
        mIsLoading = false;
    }

    @Override
    public void onUnboundAliSuccess() {

    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        if (code.equals(RPConstant.CLIENT_CODE_RECORD_REFRESH_ERROR)) {
            if (getActivity() != null) {
                ((RPRecordActivity) getActivity()).hideProgressBar();
            }
            toggleShowError(true, "", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((RPRecordActivity) getActivity()).showProgressBar();
                    mPresenter.getRecordList(RPConstant.EVENT_REFRESH_DATA, mOffset, mLength);
                }
            });
        }
        showToastMsg(message);
    }

}
