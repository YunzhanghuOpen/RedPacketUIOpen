package com.yunzhanghu.redpacketui.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.adapter.GroupMemberAdapter;
import com.yunzhanghu.redpacketui.indexrecyclerview.CharacterParser;
import com.yunzhanghu.redpacketui.indexrecyclerview.StickyRecyclerHeadersDecoration;
import com.yunzhanghu.redpacketui.ui.base.RPBaseActivity;
import com.yunzhanghu.redpacketui.utils.NetUtils;
import com.yunzhanghu.redpacketui.widget.RPSideBar;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by desert on 16/6/10
 */
public class RPGroupMemberActivity extends RPBaseActivity implements View.OnClickListener, GroupMemberAdapter.OnItemClickListener, RPValueCallback<List<RPUserBean>> {

    private RecyclerView mRecyclerView;

    private ProgressBar mProgressBar;

    private RelativeLayout mLayoutHead;

    private FrameLayout mLayoutContent;

    private CharacterParser characterParser;

    private GroupMemberAdapter mAdapter;

    private ArrayList<RPUserBean> mList;

    private String mGroupId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getBundleExtras(Bundle extras) {
        mList = getIntent().getParcelableArrayListExtra(RPConstant.EXTRA_GROUP_MEMBERS);
        mGroupId = getIntent().getStringExtra(RPConstant.EXTRA_GROUP_ID);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_activity_group_member;
    }

    @Override
    protected View getLoadingTargetView() {
        return findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(Bundle savedInstanceState) {
        mLayoutHead = (RelativeLayout) findViewById(R.id.layout_group_member_head);
        mLayoutContent = (FrameLayout) findViewById(R.id.layout_group_member);
        mProgressBar = (ProgressBar) findViewById(R.id.group_progressBar);
        characterParser = CharacterParser.getInstance();
        RPSideBar sideBar = (RPSideBar) findViewById(R.id.contact_sidebar);
        TextView userDialog = (TextView) findViewById(R.id.contact_dialog);
        mRecyclerView = (RecyclerView) findViewById(R.id.contact_member);
        sideBar.setTextView(userDialog);
        mLayoutHead.setOnClickListener(this);
        RPTitleBar titleBar = (RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (TextUtils.isEmpty(RPPreferenceManager.getInstance().getOwnerName())) {
            titleBar.setSubTitleVisibility(View.GONE);
        } else {
            String subTitle = String.format(getString(R.string.subtitle_content), RPPreferenceManager.getInstance().getOwnerName());
            titleBar.setSubTitle(subTitle);
        }
        sideBar.setOnTouchingLetterChangedListener(new RPSideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {

                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mRecyclerView.scrollToPosition(position);
                }

            }
        });

        mAdapter = new GroupMemberAdapter(mContext);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
        mRecyclerView.addItemDecoration(headersDecor);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
        initData();
    }


    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return false;
    }

    private void initData() {
        if (mList == null) {
            if (NetUtils.isNetworkConnected(this)) {
                showLoading();
                if (RedPacket.getInstance().getRPGroupMemberListener() != null) {
                    RedPacket.getInstance().getRPGroupMemberListener().getGroupMember(mGroupId, this);
                }
            } else {
                showToastMsg(getString(R.string.error_not_net_connect));
                onShowError();
            }
        } else {
            mAdapter.addAll(mList);
        }

    }

    private void sortList(List<RPUserBean> list) {
        RPUserBean rpUserBean = new RPUserBean();
        rpUserBean.userNickname = "任何人";
        rpUserBean.sortLetters = "$";
        rpUserBean.userId = "-1";
        rpUserBean.userAvatar = "none";
        rpUserBean.sortLetters = "$";
        list.add(0, rpUserBean);
        for (int i = 1; i < list.size(); i++) {
            if (TextUtils.isEmpty(list.get(i).userAvatar)) {
                list.get(i).userAvatar = "none";
            }
            if (TextUtils.isEmpty(list.get(i).userNickname)) {
                list.get(i).userNickname = "unknown";
            }
            String pinyin = characterParser.getSelling(list.get(i).userNickname);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                list.get(i).sortLetters = sortString.toUpperCase();
            } else {
                list.get(i).sortLetters = "#";
            }
        }
        Collections.sort(list, new RPUserBean());
        mAdapter.addAll(list);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.layout_group_member_head) {
            RPUserBean rpUserBean = new RPUserBean();
            rpUserBean.userNickname = "任何人";
            rpUserBean.sortLetters = "$";
            rpUserBean.userId = "-1";
            rpUserBean.userAvatar = "none";
            Intent data = getIntent();
            data.putExtra(RPConstant.EXTRA_GROUP_USER, rpUserBean);
            data.putParcelableArrayListExtra(RPConstant.EXTRA_GROUP_MEMBERS, null);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onItemClick(RPUserBean mBean, int position) {
        Intent data = getIntent();
        data.putExtra(RPConstant.EXTRA_GROUP_USER, mBean);
        data.putParcelableArrayListExtra(RPConstant.EXTRA_GROUP_MEMBERS, mAdapter.getAll());
        setResult(RESULT_OK, data);
        finish();
    }

    private void onShowError() {
        hideLoading();
        hideProgressBar();
        mLayoutContent.setVisibility(View.GONE);
        mLayoutHead.setVisibility(View.VISIBLE);
        toggleShowError(true, "", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetUtils.isNetworkConnected(RPGroupMemberActivity.this)) {
                    showProgressBar();
                    if (RedPacket.getInstance().getRPGroupMemberListener() != null) {
                        RedPacket.getInstance().getRPGroupMemberListener().getGroupMember(mGroupId, RPGroupMemberActivity.this);
                    }
                } else {
                    showToastMsg(getString(R.string.error_not_net_connect));
                }
            }
        });
    }

    private void hideProgressBar() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void showProgressBar() {
        if (mProgressBar.getVisibility() == View.GONE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSuccess(List<RPUserBean> data) {
        mLayoutContent.setVisibility(View.VISIBLE);
        mLayoutHead.setVisibility(View.GONE);
        hideLoading();
        hideProgressBar();
        if (data != null && data.size() != 0) {
            sortList(data);
        } else {
            onShowError();
        }
    }

    @Override
    public void onError(String errorCode, String errorMsg) {
        onShowError();
    }
}
