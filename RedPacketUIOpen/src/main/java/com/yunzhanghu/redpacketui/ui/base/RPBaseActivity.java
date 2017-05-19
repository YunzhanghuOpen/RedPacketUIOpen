package com.yunzhanghu.redpacketui.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yunzhanghu.redpacketui.AppManager;
import com.yunzhanghu.redpacketui.loading.VaryViewHelperController;

/**
 * Created by max on 16/3/2
 */
public abstract class RPBaseActivity extends FragmentActivity implements RPBaseView {
    /**
     * Log tag
     */
    protected static String LOG_TAG = null;
    /**
     * Context
     */
    protected Context mContext;
    /**
     * Screen information
     */
    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;
    protected float mScreenDensity = 0.5f;
    /**
     * Controller show loading/error view
     */
    private VaryViewHelperController mVaryViewHelperController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        //bundle extras
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            getBundleExtras(extras);
        }

        //设置状态栏透明
        setTranslucentStatus(isApplyStatusBarTranslucency());

        LOG_TAG = this.getClass().getSimpleName();
        AppManager.getInstance().addActivity(this);
        //获取屏幕密度及宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.density;
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        if (getContentViewLayoutID() != 0) {
            setContentView(getContentViewLayoutID());
        } else {
            throw new IllegalArgumentException("You must return a right contentView layout resource Id");
        }
        initViewsAndEvents(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (getLoadingTargetView() != null) {
            mVaryViewHelperController = new VaryViewHelperController(getLoadingTargetView());
        }
    }

    @Override
    public void finish() {
        super.finish();
        AppManager.getInstance().removeActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Resources getResources() {
        //保持字体不变
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * show toast
     *
     * @param msg 吐司显示的内容
     */
    protected void showToast(String msg) {
        //防止遮盖虚拟按键
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

    /**
     * toggle show error
     *
     * @param toggle 是否开启showError
     */
    protected void toggleShowError(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mVaryViewHelperController) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }

        if (toggle) {
            mVaryViewHelperController.showError(msg, onClickListener);
        } else {
            mVaryViewHelperController.restore();
        }
    }

    /**
     * set status bar translucency
     *
     * @param on 是否开启沉浸式状态栏
     */
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * close soft keyboard
     */
    protected void closeSoftKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            closeSoftKeyboard();
        }
        return super.onTouchEvent(event);
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

    /**
     * get bundle data
     *
     * @param extras Bundle
     */
    protected abstract void getBundleExtras(Bundle extras);

    /**
     * bind layout resource file
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();

    /**
     * get loading target view
     */
    protected abstract View getLoadingTargetView();

    /**
     * init all views and add events
     */
    protected abstract void initViewsAndEvents(Bundle savedInstanceState);

    /**
     * is applyStatusBarTranslucency
     *
     * @return boolean
     */
    protected abstract boolean isApplyStatusBarTranslucency();
}
