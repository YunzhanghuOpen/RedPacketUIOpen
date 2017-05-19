package com.yunzhanghu.redpacketui.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.presenter.IBasePresenter;
import com.yunzhanghu.redpacketui.R;
import com.yunzhanghu.redpacketui.loading.VaryViewHelperController;
import com.yunzhanghu.redpacketui.ui.fragment.PayTipsDialogFragment;
import com.yunzhanghu.redpacketui.utils.ClickUtil;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by max on 16/2/28
 */
public abstract class RPBaseDialogFragment<V, P extends IBasePresenter<V>> extends DialogFragment implements RPBaseView {

    public P mPresenter;
    /**
     * Log tag
     */
    protected static String TAG_LOG = null;
    /**
     * AvatarView marginTop
     */
    protected int marginTop;
    /**
     * Screen information
     */
    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;
    protected float mScreenDensity = 0.0f;

    /**
     * Context
     */
    protected Context mContext = null;
    /**
     * Controller show loading/error view
     */
    private VaryViewHelperController mVaryViewHelperController = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG_LOG = this.getClass().getSimpleName();
        mPresenter = initPresenter();
        if (mPresenter != null) {
            mPresenter.attach((V) this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //去掉Dialog的Title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        keepFontSize();
        if (getContentViewLayoutID() != 0) {
            return inflater.inflate(getContentViewLayoutID(), container, false);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (null != getLoadingTargetView(view)) {
            mVaryViewHelperController = new VaryViewHelperController(getLoadingTargetView(view));
        }
        //获取屏幕密度及宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mScreenDensity = displayMetrics.density;
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        initViewsAndEvents(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        float layoutWidth = getResources().getDimension(R.dimen.dialogWidth);
        float layoutHeight = getResources().getDimension(R.dimen.dialogHeight);
        float rate = layoutHeight / layoutWidth;
        int width = (int) (mScreenWidth * 0.80f);
        int height = (int) (width * rate);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setLayout(width, height);
        }
        float marginRate = 0.05f;
        if (mScreenDensity <= 1.5f) {
            marginRate = 0.02f;
        } else if (mScreenDensity <= 2.0f) {
            marginRate = 0.072f;
        } else if (mScreenDensity <= 2.625f) {
            marginRate = 0.09f;
        } else if (mScreenDensity <= 3.0f) {
            marginRate = 0.05f;
        } else if (mScreenDensity == 3.5f) {
            marginRate = 0.087f;
        }
        marginTop = (int) (height * marginRate);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        // for bug ---> java.lang.IllegalStateException: Activity has been destroyed
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ClickUtil.sLastClickTime = 0;
        if (mPresenter != null) {
            mPresenter.detach(true);
            mPresenter = null;
        }
    }

    /**
     * show toast
     *
     * @param msg 吐司显示的内容
     */
    protected void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * toggle show loading
     *
     * @param toggle 是否开启showLoading
     */
    protected void toggleShowLoading(boolean toggle) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }
        if (toggle) {
            mVaryViewHelperController.showLoading();
        } else {
            mVaryViewHelperController.restore();
        }
    }

    public String doubleNumberFormat(double number) {
        DecimalFormat format = new DecimalFormat("###########0.00");
        format.setRoundingMode(RoundingMode.FLOOR);
        return format.format(number);
    }

    private void keepFontSize() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    public void showLoading() {
        toggleShowLoading(true);
    }

    @Override
    public void hideLoading() {
        toggleShowLoading(false);
    }

    @Override
    public void showToastMsg(String msg) {
        showToast(msg);
    }

    public void showTipDialog(String code, String msg, PayTipsDialogFragment.OnDialogConfirmClickCallback callback) {
        PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(code, msg);
        dialog.setCallback(callback);
        if (getActivity() != null) {
            FragmentTransaction ft = getMyFragmentManager(getActivity()).beginTransaction();
            ft.add(dialog, RPConstant.RP_TIP_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }

    public void showAllowingStateLost(DialogFragment fragment, FragmentActivity activity) {
        if (activity != null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.add(fragment, RPConstant.RP_PACKET_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }

    /**
     * get the support fragment manager
     *
     * @return FragmentManager
     */
    protected FragmentManager getMyFragmentManager(FragmentActivity activity) {
        return activity.getSupportFragmentManager();
    }

    /**
     * bind layout resource file
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();

    /**
     * get loading target view
     */
    protected abstract View getLoadingTargetView(View view);

    /**
     * init all views and add events
     */
    protected abstract void initViewsAndEvents(View view, Bundle savedInstanceState);

    public abstract P initPresenter();

}
