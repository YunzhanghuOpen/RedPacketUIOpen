package com.yunzhanghu.redpacketui.ui.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.callback.GreetingTextWatcher;
import com.yunzhanghu.redpacketui.utils.ClickUtil;

public class SendSinglePacketFragment extends SendPacketBaseFragment {

    private EditText mEtMoneyAmount;

    private EditText mEtGreeting;

    private TextView mTvMoneyAmount;

    private Button mBtnPutMoney;

    private String mMoneyAmount;

    private TextView mTvAmount;

    private TextView mTvMoneyUnit;

    public static SendSinglePacketFragment newInstance(RedPacketInfo redPacketInfo) {
        SendSinglePacketFragment fragment = new SendSinglePacketFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_RED_PACKET_INFO, redPacketInfo);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected View getLoadingTargetView(View view) {
        return view.findViewById(R.id.target_layout);
    }


    @Override
    protected void initViewsAndEvents(View view, Bundle savedInstanceState) {
        super.initViewsAndEvents(view, savedInstanceState);
        mEtGreeting = (EditText) view.findViewById(R.id.et_greetings);
        mEtGreeting.setHint(mGreetingArray[0]);
        mBtnPutMoney = (Button) view.findViewById(R.id.btn_single_put_money);
        mBtnPutMoney.setOnClickListener(this);
        final View changeGreeting = view.findViewById(R.id.btn_layout);
        changeGreeting.setOnClickListener(this);
        enableButton(false);
        mTvAmount = (TextView) view.findViewById(R.id.tv_money_amount);
        mTvMoneyUnit = (TextView) view.findViewById(R.id.tv_money_unit);
        mEtMoneyAmount = (EditText) view.findViewById(R.id.et_money_amount);
        mTvMoneyAmount = (TextView) view.findViewById(R.id.tv_money);
        mPopupParent = getActivity().findViewById(R.id.title_bar);
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
                            enableButton(false);
                            setAmountRedColor();
                            showPopupWindow(mPopupParent, mTvPopupMsg, mContext.getString(R.string.input_money_error));
                        } else {
                            try {
                                double money = Double.valueOf(moneyAmount);
                                if (money == 0) {
                                    enableButton(false);
                                    setAmountRedColor();
                                    showPopupWindow(mPopupParent, mTvPopupMsg, mContext.getString(R.string.input_money_error));
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mEtMoneyAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                updateLimit();
                mEtMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_black));
                mTvAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_black));
                mTvMoneyUnit.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_black));
                mTvMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_black));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                    enableButton(false);
                    hidePopupWindow();
                } else if (s.length() == 1 && s.toString().indexOf(".") == 0) {
                    enableButton(false);
                    hidePopupWindow();
                } else {
                    double money;
                    try {
                        money = Double.valueOf(s.toString());
                        int posDot = s.toString().indexOf(".");
                        if (money == 0) {
                            //当用户输入.00时,提示输入金额错误
                            if (posDot >= 0 && s.length() > 2) {
                                String[] st = s.toString().split("\\.");
                                if (st.length == 2 && st[1].equals("00")) {
                                    mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                                    mTvMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_text_black));
                                    enableButton(false);
                                    setAmountRedColor();
                                    showPopupWindow(mPopupParent, mTvPopupMsg, mContext.getString(R.string.input_money_error));
                                } else {
                                    hidePopupWindow();
                                    mTvMoneyAmount.setText(getString(R.string.rp_str_amount_zero));
                                    enableButton(false);
                                }
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
                        if (money < mMinLimit) {
                            enableButton(false);
                            setAmountRedColor();
                            mTvMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
                            String minLimit = String.format(getString(R.string.input_money_limited_minimum), getNumberLimit(mMinLimit));
                            showPopupWindow(mPopupParent, mTvPopupMsg, minLimit);
                            return;
                        }
                        if (money > mSingleLimit) {
                            enableButton(false);
                            setAmountRedColor();
                            mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                            String limit = String.format(mContext.getString(R.string.input_money_limited), getNumberLimit(mSingleLimit));
                            showPopupWindow(mPopupParent, mTvPopupMsg, limit);
                            return;
                        }
                        enableButton(true);
                        mTvMoneyAmount.setText(String.format("￥%s", doubleNumberFormat(money)));
                        hidePopupWindow();
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
        });
    }

    private void setAmountRedColor() {
        enableButton(false);
        mEtMoneyAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvAmount.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
        mTvMoneyUnit.setTextColor(ContextCompat.getColor(mContext, R.color.rp_money_red_light));
    }

    private void enableButton(boolean enable) {
        mBtnPutMoney.setEnabled(enable);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.rp_fragment_single_chat_packet;
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);//必须调用父类方法
        if (v.getId() == R.id.btn_single_put_money) {
            if (ClickUtil.isFastClick()) return;
            closeSoftKeyboard();
            mMoneyAmount = mEtMoneyAmount.getText().toString().trim();
            String greeting = mEtGreeting.getText().toString().trim();
            if (verifyParams()) return;
            if (TextUtils.isEmpty(greeting)) {
                greeting = mEtGreeting.getHint().toString();
            }
            mRedPacketInfo.redPacketAmount = mMoneyAmount;
            mRedPacketInfo.redPacketGreeting = greeting.replaceAll("\n|\r", "");
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
    }

    private boolean verifyParams() {
        if (TextUtils.isEmpty(mMoneyAmount)) {
            showPopupWindow(mPopupParent, mTvPopupMsg, mContext.getString(R.string.input_money_amount));
            return true;
        }
        if (Double.valueOf(mMoneyAmount) <= 0) {
            showPopupWindow(mPopupParent, mTvPopupMsg, mContext.getString(R.string.input_money_error));
            return true;
        }
        if (Double.valueOf(mMoneyAmount) > mSingleLimit) {
            String limit = String.format(mContext.getResources().getString(R.string.input_money_limited), getNumberLimit(mSingleLimit));
            showPopupWindow(mPopupParent, mTvPopupMsg, limit);
            return true;
        }
        return false;
    }

}
