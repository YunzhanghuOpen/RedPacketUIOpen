package com.yunzhanghu.redpacketui.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketui.R;

import java.util.Map;

/**
 * Created by desert on 16/6/1
 */
public class AliPay {

    private Context mContext;

    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_AUTH_FLAG = 2;

    private AliPayCallBack mPayCallBack;

    private AliAuthCallBack mAuthCallBack;

    public AliPay(Context context) {
        mContext = context;
    }

    public void setPayCallBack(AliPayCallBack payCallBack) {
        mPayCallBack = payCallBack;
    }

    public void setAuthCallBack(AliAuthCallBack authCallBack) {
        mAuthCallBack = authCallBack;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /*
                     *同步返回的结果须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     *detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     *docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        mPayCallBack.AliPaySuccess();
                    } else {
                        if (mPayCallBack == null) {
                            Toast.makeText(mContext, payResult.getMemo(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            mPayCallBack.AliPaySuccess();
                        } else if (TextUtils.equals(resultStatus, "6001")) {
                            ///用户中途取消
                            mPayCallBack.AliPayError(RPConstant.CLIENT_CODE_ALI_PAY_CANCEL, mContext.getString(R.string.str_ali_cancel_pay_content));
                        } else {
                            // 其他值就可以判断为支付失败
                            mPayCallBack.AliPayError(RPConstant.CLIENT_CODE_ALI_PAY_FAIL, mContext.getResources().getString(R.string.str_ali_pay_fail_content));
                        }
                    }
                    break;
                }
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();
                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        if (mAuthCallBack != null) {
                            mAuthCallBack.AliAuthSuccess(authResult.getAuthCode(), authResult.getUserId());
                        }

                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        // 其他状态值则为授权失败
                        Toast.makeText(mContext, "取消授权", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "授权失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };


    /**
     * call aliPay sdk pay. 调用SDK支付
     */
    public void pay(final String orderInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask aliPay = new PayTask((Activity) mContext);
                // 调用支付接口，获取支付结果
                Map<String, String> result = aliPay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);

            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 支付宝账户授权业务
     */
    public void auth(final String authInfo) {
        Runnable authRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造AuthTask 对象
                AuthTask authTask = new AuthTask((Activity) mContext);
                // 调用授权接口，获取授权结果
                Map<String, String> result = authTask.authV2(authInfo, true);
                Message msg = new Message();
                msg.what = SDK_AUTH_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }


    public interface AliPayCallBack {

        void AliPaySuccess();

        void AliPayError(String code, String msg);

    }

    public interface AliAuthCallBack {

        void AliAuthSuccess(String authCode, String userID);

    }

}
