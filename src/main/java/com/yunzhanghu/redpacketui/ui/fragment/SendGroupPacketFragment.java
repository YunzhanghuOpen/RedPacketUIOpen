package com.yunzhanghu.redpacketui.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.utils.RPPreferenceManager;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.callback.GreetingTextWatcher;
import com.yunzhanghu.redpacketui.ui.activity.RPGroupMemberActivity;
import com.yunzhanghu.redpacketui.utils.ClickUtil;

import java.util.ArrayList;

public class SendGroupPacketFragment extends SendPacketBaseFragment {

    private static final int FLAG_OPEN_GROUP_MEMBER = 1;

    private EditText mEtMoneyAmount;

    private EditText mEtMoneyCount;

    private EditText mEtGreeting;

    private TextView mTvMoneyAmount;

    private TextView mTvReceiveName;

    private TextView mTvGroupCount;

    private Button mBtnPutMoney;

    private String mMoneyAmount;

    private int mMoneyCount = 1;

    private TextView mTvMoneyType;

    private TextView mTvMoneyTypeInfo;

    private TextView mTvAmount;

    private TextView mTvCount;

    private TextView mTvCountUnit;

    private TextView mTvAmountUnit;

    private View mMoneyLayout;

    private boolean mIsRandom = true;

    private boolean mIsExclusive = false;

    private ImageView mIvRandomIcon;

    private String mDefaultMoneyAmount = "";

    private String mDefaultMoneyCount = "";

    private ArrayList<RPUserBean> mList;

    private RPUserBean mRPUserBean;

    private View mPopViewLayout;

    private TextView mTvPopMsg;


    public static SendGroupPacketFragment newInstance(RedPacketInfo redPacketInfo) {
        SendGroupPacketFragment fragment = new SendGroupPacketFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_INFO, redPacketInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRedPacketInfo = getArguments().getParcelable(ARGS_RED_PACKET_INFO);
        }
    }

    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }

    @Override
    protected void initViewsAndEvents(final View view, Bundle savedInstanceState) {
        super.initViewsAndEvents(view, savedInstanceState);
        mPopViewLayout = view.findViewById(R.id.pop_layout);
        mTvPopMsg = (TextView) view.findViewById(R.id.tv_popup_msg);
        mIvRandomIcon = (ImageView) view.findViewById(R.id.iv_random_icon);
        mTvGroupCount = (TextView) view.findViewById(R.id.tv_group_count);
        if (mRedPacketInfo.groupMemberCount <= 0) {
            mTvGroupCount.setVisibility(View.GONE);
        } else {
            String count = String.format(getResources().getString(R.string.group_member_count), mRedPacketInfo.groupMemberCount + "");
            mTvGroupCount.setText(count);
        }
        if (RedPacket.getInstance().getRPGroupMemberListener() != null && mRedPacketInfo.groupMemberCount > 1) {
            view.findViewById(R.id.layout_members).setVisibility(View.VISIBLE);
        }
        mMoneyLayout = view.findViewById(R.id.money_amount_layout);
        mBtnPutMoney = (Button) view.findViewById(R.id.btn_group_put_money);
        mBtnPutMoney.setOnClickListener(this);
        mTvMoneyType = (TextView) view.findViewById(R.id.tv_change_type);
        mTvMoneyType.setOnClickListener(this);
        mTvMoneyTypeInfo = (TextView) view.findViewById(R.id.tv_type_info);
        final View changeGreeting = view.findViewById(R.id.btn_layout);
        changeGreeting.setOnClickListener(this);
        enableButton(false);
        mTvCount = (TextView) view.findViewById(R.id.tv_money_count);
        mTvCountUnit = (TextView) view.findViewById(R.id.tv_count_unit);
        mTvAmount = (TextView) view.findViewById(R.id.tv_total_money);
        mTvAmountUnit = (TextView) view.findViewById(R.id.tv_money_unit);
        mEtMoneyAmount = (EditText) view.findViewById(R.id.et_money_amount);
        mEtMoneyCount = (EditText) view.findViewById(R.id.et_money_count);
        mTvMoneyAmount = (TextView) view.findViewById(R.id.tv_money);
        mEtGreeting = (EditText) view.findViewById(R.id.et_greetings);
        mEtGreeting.setHint(mGreetingArray[0]);
        mTvReceiveName = (TextView) view.findViewById(R.id.tv_receive_name);
        view.findViewById(R.id.layout_members).setOnClickListener(this);
        initPopupWindow();
        mEtGreeting.addTextChangedListener(new GreetingTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
            }
        });
        mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
        mEtMoneyAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String moneyAmount = mEtMoneyAmount.getText().toString();
                    if (!TextUtils.isEmpty(moneyAmount)) {
                        if (moneyAmount.length() == 1 && moneyAmount.indexOf(".") == 0) {
                            setAmountRedColor();
                            showErrorMsg(mContext.getString(R.string.input_money_error));
                        } else {
                            try {
                                double money = Double.valueOf(moneyAmount);
                                if (money == 0) {
                                    setAmountRedColor();
                                    showErrorMsg(mContext.getString(R.string.input_money_error));
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mEtMoneyAmount.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        updateLimit();
                        resetColorStatus(mContext);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0) {
                            mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                            enableButton(false);
                            hideErrorMsg();
                        } else if (s.length() == 1 && s.toString().indexOf(".") == 0) {
                            enableButton(false);
                            hideErrorMsg();
                        } else {
                            double money;
                            int moneyCount;
                            try {
                                money = Double.valueOf(s.toString());
                                String countStr = TextUtils.isEmpty(mEtMoneyCount.getText().toString().trim()) ? "0" : mEtMoneyCount.getText().toString().trim();
                                moneyCount = Integer.valueOf(countStr);
                                if (moneyCount == 0) {
                                    setCountRedColor();
                                    mTvMoneyAmount.setText(String.format(getString(R.string.detail_money_sign), doubleNumberFormat(money)));
                                    showErrorMsg(mContext.getString(R.string.tip_money_count_zero));
                                    return;
                                } else {
                                    hideErrorMsg();
                                }
                                if (moneyCount > RPPreferenceManager.getInstance().getMaxPacketCount()) {
                                    if (mIsRandom) {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                                    } else {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money * moneyCount)));
                                    }
                                    setCountRedColor();
                                    showErrorMsg(String.format(mContext.getString(R.string.tip_money_count_limit), RPPreferenceManager.getInstance().getMaxPacketCount() + ""));
                                    return;
                                }
                                int posDot = s.toString().indexOf(".");
                                if (money == 0) {
                                    //当用户输入.00时,提示输入金额错误
                                    if (posDot >= 0 && s.length() > 2) {
                                        String[] st = s.toString().split("\\.");
                                        if (st.length == 2 && st[1].equals("00")) {
                                            setAmountRedColor();
                                            showErrorMsg(mContext.getString(R.string.input_money_error));
                                        } else {
                                            hidePopupWindow();
                                            enableButton(false);
                                        }
                                        mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                                    } else {
                                        hidePopupWindow();
                                        if (getStartZeroNumber(s.toString()) < 9) {
                                            mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                                            enableButton(false);
                                        }
                                    }
                                    return;
                                }
                                if (posDot < 0) {//只有在没有小数点的情况下走下面方法
                                    if (s.toString().startsWith("0") && money >= 1) {
                                        if (getStartZeroNumber(s.toString()) == 9) {
                                            return;
                                        }
                                    }
                                }
                                double perMoney;
                                if (mIsRandom) {
                                    perMoney = money / moneyCount;
                                } else {
                                    perMoney = money;
                                }
                                if (perMoney < mMinLimit) {
                                    setAmountRedColor();
                                    String minLimit = String.format(getString(R.string.input_money_limited_minimum), getNumberLimit(mMinLimit));
                                    showErrorMsg(minLimit);
                                    return;
                                }
                                if (perMoney > mSingleLimit) {
                                    setAmountRedColor();
                                    if (mIsRandom) {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                                    } else {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money * moneyCount)));
                                    }
                                    String limit = String.format(mContext.getString(R.string.input_money_limited), getNumberLimit(mSingleLimit));
                                    showErrorMsg(limit);
                                    return;
                                }
                                if (!TextUtils.isEmpty(mEtMoneyCount.getText().toString())) {
                                    enableButton(true);
                                }
                                if (mIsRandom) {
                                    mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                                } else {
                                    mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money * moneyCount)));
                                }
                                hideErrorMsg();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String temp = s.toString();
                        int posDot = temp.indexOf(".");
                        if (posDot < 0) {
                            if (temp.length() - 1 > 8) {
                                s.delete(9, 10);
                            }
                            return;
                        }
                        if (temp.length() - posDot - 1 > 2) {
                            s.delete(posDot + 3, posDot + 4);
                        }
                    }
                }

        );

        mEtMoneyCount.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        resetColorStatus(mContext);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0) {
                            setCountRedColor();
                            showErrorMsg(mContext.getString(R.string.tip_money_count_zero));
                        } else {
                            try {
                                String moneyStr = TextUtils.isEmpty(mEtMoneyAmount.getText().toString().trim()) ? "0" : mEtMoneyAmount.getText().toString().trim();
                                double money = Double.valueOf(moneyStr);
                                mMoneyCount = Integer.valueOf(TextUtils.isEmpty(s.toString().trim()) ? "0" : s.toString().trim());
                                if (mMoneyCount == 0) {
                                    setCountRedColor();
                                    showErrorMsg(mContext.getString(R.string.tip_money_count_zero));
                                    return;
                                } else {
                                    hideErrorMsg();
                                }
                                if (mMoneyCount > RPPreferenceManager.getInstance().getMaxPacketCount()) {
                                    setCountRedColor();
                                    showErrorMsg(String.format(mContext.getString(R.string.tip_money_count_limit), RPPreferenceManager.getInstance().getMaxPacketCount() + ""));
                                    return;
                                }
                                if (money == 0) {
                                    enableButton(false);
                                    return;
                                }
                                double perMoney;
                                if (mIsRandom) {
                                    perMoney = money / mMoneyCount;
                                } else {
                                    perMoney = money;
                                }
                                if (perMoney < mMinLimit) {
                                    setAmountRedColor();
                                    String minLimit = String.format(getString(R.string.input_money_limited_minimum), getNumberLimit(mMinLimit));
                                    showErrorMsg(minLimit);
                                    return;
                                }
                                if (perMoney > mSingleLimit) {
                                    setAmountRedColor();
                                    if (mIsRandom) {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                                    } else {
                                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money * mMoneyCount)));
                                    }
                                    String limit = String.format(mContext.getString(R.string.input_money_limited), getNumberLimit(mSingleLimit));
                                    showErrorMsg(limit);
                                    return;
                                }
                                if (!TextUtils.isEmpty(mEtMoneyAmount.getText().toString())) {
                                    enableButton(true);
                                }
                                if (mIsRandom) {
                                    mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                                } else {
                                    mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money * mMoneyCount)));
                                }
                                hideErrorMsg();
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }

        );
    }

    private void setAmountRedColor() {
        enableButton(false);
        mEtMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvAmountUnit.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
    }

    private void setCountRedColor() {
        enableButton(false);
        mEtMoneyCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvCount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvCountUnit.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
    }

    private void enableButton(boolean enable) {
        mBtnPutMoney.setEnabled(enable);
    }

    private void resetColorStatus(Context context) {
        mEtMoneyAmount.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
        mEtMoneyCount.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
        mTvAmount.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
        mTvAmountUnit.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
        mTvCount.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
        mTvCountUnit.setTextColor(ContextCompat.getColor(context, R.color.rp_text_black));
    }


    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_fragment_group_chat_packet;
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);//必须调用父类方法
        if (v.getId() == R.id.btn_group_put_money) {
            if (ClickUtil.isFastClick()) return;
            closeSoftKeyboard();
            mMoneyAmount = mEtMoneyAmount.getText().toString().trim();
            mMoneyCount = Integer.valueOf(mEtMoneyCount.getText().toString());
            String greeting = mEtGreeting.getText().toString().trim();
            if (verifyParams()) return;
            if (TextUtils.isEmpty(greeting)) {
                greeting = mEtGreeting.getHint().toString();
            }
            double money;
            String type;
            mRedPacketInfo.redPacketGreeting = greeting.replaceAll("\n|\r", "");
            if (mIsExclusive) {
                type = RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE;
                money = Double.valueOf(mMoneyAmount);
                mRedPacketInfo.receiverId = mRPUserBean.userId;
                mRedPacketInfo.receiverNickname = mRPUserBean.userNickname;
                mRedPacketInfo.receiverAvatarUrl = mRPUserBean.userAvatar;
            } else {
                if (mIsRandom) {
                    type = RPConstant.RED_PACKET_TYPE_GROUP_RANDOM;
                    money = Double.valueOf(mMoneyAmount);
                } else {
                    type = RPConstant.RED_PACKET_TYPE_GROUP_AVERAGE;
                    money = Double.valueOf(mMoneyAmount) * mMoneyCount;
                }
            }
            mRedPacketInfo.redPacketAmount = doubleNumberFormat(money);
            mRedPacketInfo.redPacketType = type;
            mRedPacketInfo.totalCount = mMoneyCount;
            retryRPToken(mBtnPutMoney);
        }
        if (v.getId() == R.id.btn_layout) {
            String greeting;
            if (mArrayIndex < mGreetingArray.length) {
                greeting = mGreetingArray[mArrayIndex];
                mArrayIndex++;
            } else {
                mArrayIndex = 0;
                greeting = mGreetingArray[mArrayIndex];
                mArrayIndex++;
            }
            mEtGreeting.setText(greeting);
        }
        if (v.getId() == R.id.tv_change_type) {
            mTvMoneyType.setEnabled(false);
            mIsRandom = !mIsRandom;
            ObjectAnimator animator = ObjectAnimator.ofFloat(mMoneyLayout, "translationX", mMoneyLayout.getWidth(), 0.0F);
            animator.setDuration(300);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    String moneyAmount = mEtMoneyAmount.getText().toString();
                    String moneyCount = mEtMoneyCount.getText().toString();
                    double money;
                    int count;
                    String amount;
                    if (!TextUtils.isEmpty(moneyAmount) && !TextUtils.isEmpty(moneyCount)) {
                        money = Double.valueOf(moneyAmount);
                        count = Integer.valueOf(moneyCount);
                        if (count == 0) {
                            count = 1;
                        }
                        if (mIsRandom) {//普通红包变随机红包
                            amount = doubleNumberFormat(money * count);
                        } else {
                            amount = doubleNumberFormat(money / count);
                        }
                        mEtMoneyAmount.setText(amount);
                    }
                    if (mIsRandom) {//普通红包变随机红包
                        mTvAmount.setText(mContext.getString(R.string.group_money_total));
                        mIvRandomIcon.setVisibility(View.VISIBLE);
                        mTvMoneyTypeInfo.setText(mContext.getString(R.string.group_rule_tips_random));
                        mTvMoneyType.setText(mContext.getString(R.string.group_change_normal));
                    } else {//随机红包变普通红包
                        mTvAmount.setText(mContext.getString(R.string.group_money_every));
                        mIvRandomIcon.setVisibility(View.GONE);
                        mTvMoneyTypeInfo.setText(mContext.getString(R.string.group_rule_tips_normal));
                        mTvMoneyType.setText(mContext.getString(R.string.group_change_random));
                    }
                    mTvMoneyType.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        if (v.getId() == R.id.layout_members) {
            if (!mIsExclusive) {
                mDefaultMoneyAmount = mEtMoneyAmount.getText().toString();
                mDefaultMoneyCount = mEtMoneyCount.getText().toString();
            }
            Intent intent = new Intent(mContext, RPGroupMemberActivity.class);
            intent.putExtra(RPConstant.EXTRA_GROUP_ID, mRedPacketInfo.groupId);
            intent.putParcelableArrayListExtra(RPConstant.EXTRA_GROUP_MEMBERS, mList);
            startActivityForResult(intent, FLAG_OPEN_GROUP_MEMBER);
        }
    }

    public void switchDefaultRedPacketType() {
        mEtMoneyCount.setText(mDefaultMoneyCount);
        mEtMoneyAmount.setText(mDefaultMoneyAmount);
        mIvRandomIcon.setBackgroundResource(R.drawable.rp_random_icon);
        if (mIsRandom) {//普通红包变随机红包
            mTvAmount.setText(mContext.getString(R.string.group_money_total));
            mIvRandomIcon.setVisibility(View.VISIBLE);
            mTvMoneyTypeInfo.setText(mContext.getString(R.string.group_rule_tips_random));
            mTvMoneyType.setText(mContext.getString(R.string.group_change_normal));
        } else {//随机红包变普通红包
            mTvAmount.setText(mContext.getString(R.string.group_money_every));
            mIvRandomIcon.setVisibility(View.GONE);
            mTvMoneyTypeInfo.setText(mContext.getString(R.string.group_rule_tips_normal));
            mTvMoneyType.setText(mContext.getString(R.string.group_change_random));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (requestCode == FLAG_OPEN_GROUP_MEMBER && resultCode == Activity.RESULT_OK) {
            mRPUserBean = data.getParcelableExtra(RPConstant.EXTRA_GROUP_USER);
            mList = data.getParcelableArrayListExtra(RPConstant.EXTRA_GROUP_MEMBERS);
            if (!TextUtils.equals(mRPUserBean.userNickname, mContext.getString(R.string.tv_all_person))) {
                mIsExclusive = true;
                mEtMoneyCount.setEnabled(false);
                mEtMoneyCount.setText("1");
                mEtMoneyAmount.setText("");
                mTvAmount.setText("");
                enableButton(false);
                mIvRandomIcon.setVisibility(View.VISIBLE);
                mIvRandomIcon.setBackgroundResource(R.drawable.rp_exclusive_icon);
                mTvMoneyType.setVisibility(View.GONE);
                mTvAmount.setText(mContext.getString(R.string.group_money_total));
                if (mRedPacketInfo.groupMemberCount <= 0) {
                    mTvGroupCount.setVisibility(View.VISIBLE);
                    mTvGroupCount.setText(R.string.group_choose_few_person2);
                } else {
                    mTvGroupCount.setText(String.format(getResources().getString(R.string.group_choose_few_person), mRedPacketInfo.groupMemberCount + ""));
                }
                mTvMoneyTypeInfo.setText(mContext.getString(R.string.msg_choose_few_person_red_packet));
            } else {//恢复之前的红包类型
                mIsExclusive = false;
                mEtMoneyCount.setEnabled(true);
                mTvMoneyType.setVisibility(View.VISIBLE);
                if (mRedPacketInfo.groupMemberCount <= 0) {
                    mTvGroupCount.setVisibility(View.GONE);
                } else {
                    mTvGroupCount.setText(String.format(getResources().getString(R.string.group_member_count), mRedPacketInfo.groupMemberCount + ""));

                }
                switchDefaultRedPacketType();
            }
            mTvReceiveName.setText(mRPUserBean.userNickname);

        }
    }

    private boolean verifyParams() {
        if (TextUtils.isEmpty(mMoneyAmount)) {
            showErrorMsg(mContext.getString(R.string.input_money_amount));
            return true;
        }
        if (Double.valueOf(mMoneyAmount) <= 0) {
            showErrorMsg(mContext.getString(R.string.input_money_zero));
            return true;
        }
        double perMoney = Double.valueOf(mMoneyAmount) / mMoneyCount;
        if (perMoney > mSingleLimit) {
            String limit = String.format(mContext.getResources().getString(R.string.input_money_limited), getNumberLimit(mSingleLimit));
            showErrorMsg(limit);
            return true;
        }
        return false;
    }

    private void showErrorMsg(String message) {
        mPopViewLayout.setVisibility(View.VISIBLE);
        mTvPopMsg.setText(message);
    }

    private void hideErrorMsg() {
        mPopViewLayout.setVisibility(View.GONE);
    }

}
