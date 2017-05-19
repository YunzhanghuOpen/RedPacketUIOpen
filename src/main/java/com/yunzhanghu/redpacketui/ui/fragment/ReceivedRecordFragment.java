package com.yunzhanghu.redpacketui.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.yunzhanghu.redpacketsdk.bean.PageInfo;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.RPRecordContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.RPRecordPresenter;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.adapter.ReceivedRecordAdapter;
import com.yunzhanghu.redpacketui.alipay.AliPay;
import com.yunzhanghu.redpacketui.callback.OnAliUserClickListener;
import com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity;
import com.yunzhanghu.redpacketui.ui.base.RPBaseFragment;
import com.yunzhanghu.redpacketui.utils.NetUtils;
import com.yunzhanghu.redpacketui.utils.PageUtil;

import java.util.ArrayList;

/**
 * Created by max on 16/3/27
 */
public class ReceivedRecordFragment extends RPBaseFragment<RPRecordContract.View, RPRecordContract.Presenter<RPRecordContract.View>> implements RPRecordContract.View, AliPay.AliAuthCallBack, OnAliUserClickListener {


    private final static String ARGS_USER_NAME = "user_name";

    private final static String ARGS_USER_AVATAR = "user_avatar";

    private int mCurrentPage = 1;

    private int mOffset = 0;

    private int mLength = PageUtil.PAGE_LIMIT;

    private int mTotalPageNo = 0;

    private LinearLayoutManager mLayoutManager;

    private ReceivedRecordAdapter mAdapter;

    private boolean mIsLoading;

    private boolean mIsAccountFail;

    private String mCurrentUserName;

    private String mCurrentAvatarUrl;

    private View mView;

    public static ReceivedRecordFragment newInstance(String currentUserName, String currentAvatarUrl) {
        ReceivedRecordFragment fragment = new ReceivedRecordFragment();
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
        return new RPRecordPresenter(RPConstant.RECORD_TAG_RECEIVED);
    }

    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        initRecyclerView(view);
        showLoading();
        if (TextUtils.isEmpty(RPPreferenceManager.getInstance().getAliAccount())) {
            mPresenter.getAliUserInfo();
        } else {//有账户信息
            mPresenter.getRecordList(RPConstant.EVENT_REFRESH_DATA, mOffset, mLength);
        }
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.record_list);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mAdapter = new ReceivedRecordAdapter(mContext, mCurrentUserName, mCurrentAvatarUrl, this);
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
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_record_fragment;
    }

    @Override
    public void onAliUserInfoSuccess(String userInfo, boolean isRefreshed) {
        RPPreferenceManager.getInstance().setAliAccount(userInfo);
        if (isRefreshed) {
            //绑定支付宝账户成功后，刷新支付宝账户信息
            mAdapter.notifyAliUserStatusChanged(ReceivedRecordAdapter.ALI_USER_STATUS_BOUND);
        } else {
            mPresenter.getRecordList(RPConstant.EVENT_REFRESH_DATA, mOffset, mLength);
        }
    }

    @Override
    public void onUserUnauthorized(String authInfo) {
        hideLoading();
        mView.setEnabled(true);
        AliPay aliPay = new AliPay(mContext);
        aliPay.setAuthCallBack(this);
        aliPay.auth(authInfo);
    }


    @Override
    public void onUploadAuthInfoSuccess() {
        hideLoading();
        mView.setEnabled(true);
        showTipDialog(RPConstant.CLIENT_CODE_ALI_AUTH_SUCCESS, getString(R.string.str_ali_auth_success), null);
    }

    @Override
    public void onRecordListSuccess(RedPacketInfo headerInfo, ArrayList<RedPacketInfo> listInfo, PageInfo pageInfo) {
        hideLoading();
        if (getActivity() != null) {
            ((RPRecordActivity) getActivity()).hideProgressBar();
        }
        if (TextUtils.isEmpty(RPPreferenceManager.getInstance().getAliAccount())) {
            if (mIsAccountFail) {
                headerInfo.aliUserStatus = ReceivedRecordAdapter.ALI_USER_STATUS_ERROR;
            } else {
                headerInfo.aliUserStatus = ReceivedRecordAdapter.ALI_USER_STATUS_UNBOUND;
            }
        } else {
            headerInfo.aliUserStatus = ReceivedRecordAdapter.ALI_USER_STATUS_BOUND;
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
        hideLoading();
        mView.setEnabled(true);
        RPPreferenceManager.getInstance().setAliAccount("");
        //解绑支付宝账户成功后，刷新UI
        mAdapter.notifyAliUserStatusChanged(ReceivedRecordAdapter.ALI_USER_STATUS_UNBOUND);
    }

    @Override
    public void onError(String code, String message) {
        hideLoading();
        switch (code) {
            case RPConstant.CLIENT_CODE_RECORD_LOAD_MORE_ERROR:
                showToastMsg(message);
                break;
            case RPConstant.CLIENT_CODE_RECORD_REFRESH_ERROR:
                RedPacketInfo headInfo = new RedPacketInfo();
                headInfo.totalMoney = "-.--";
                headInfo.totalCount = 0;
                headInfo.bestCount = 0;
                headInfo.itemType = 0;
                mAdapter.addHeader(headInfo);
                mLength = 0;
                mOffset = 0;
                mTotalPageNo = 0;
                showToastMsg(message);
                break;
            case RPConstant.CLIENT_CODE_RECORD_ALI_USER_ERROR:
                mPresenter.getRecordList(RPConstant.EVENT_REFRESH_DATA, mOffset, mLength);
                mIsAccountFail = true;
                break;
            default:
                if (mView != null) {
                    mView.setEnabled(true);
                }
                showTipDialog(code, message, null);
                break;
        }

    }

    @Override
    public void AliAuthSuccess(String authCode, String userID) {
        mPresenter.uploadAuthInfo(authCode, userID);
        showLoading();
        mView.setEnabled(false);
    }

    @Override
    public void onAliUserClick(View view, boolean isBound) {
        mView = view;
        if (isBound) {
            mPresenter.getAuthInfo();
            showLoading();
            mView.setEnabled(false);
        } else {
            showTipDialog(RPConstant.CLIENT_CODE_UNBIND_ALI_ACCOUNT, getString(R.string.tip_msg_unbind_ali), new PayTipsDialogFragment.OnDialogConfirmClickCallback() {
                @Override
                public void onConfirmClick() {
                    mPresenter.unBindAliUser();
                    showLoading();
                    mView.setEnabled(false);
                }
            });
        }
    }

    private void showTipDialog(String code, String msg, PayTipsDialogFragment.OnDialogConfirmClickCallback callback) {
        PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(code, msg);
        dialog.setCallback(callback);
        if (getActivity() != null) {
            FragmentTransaction ft = getMyFragmentManager(getActivity()).beginTransaction();
            ft.add(dialog, RPConstant.RP_TIP_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }
}
