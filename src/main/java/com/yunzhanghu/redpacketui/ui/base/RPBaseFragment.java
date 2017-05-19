package com.yunzhanghu.redpacketui.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yunzhanghu.redpacketsdk.presenter.IBasePresenter;
import com.yunzhanghu.redpacketui.loading.VaryViewHelperController;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by max on 16/3/1
 */
public abstract class RPBaseFragment<V, P extends IBasePresenter<V>> extends Fragment implements RPBaseView {

    public P mPresenter;
    /**
     * Log tag
     */
    protected static String TAG_LOG = null;

    /**
     * Screen information
     */
    protected int mScreenWidth = 0;
    protected int mScreenHeight = 0;
    protected float mScreenDensity = 0.0f;

    /**
     * context
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContentViewLayoutID() != 0) {
            return inflater.inflate(getContentViewLayoutID(), null);
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
        if (mPresenter != null) {
            mPresenter.detach(true);
            mPresenter = null;
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
     * show toast
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

    public String doubleNumberFormat(double number) {
        DecimalFormat format = new DecimalFormat("###########0.00");
        format.setRoundingMode(RoundingMode.FLOOR);
        return format.format(number);
    }

    /**
     * close soft keyboard
     */
    protected void closeSoftKeyboard() {
        View view = getActivity().getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
     * get loading target view
     */
    protected abstract View getLoadingTargetView(View view);

    /**
     * init all views and add events
     */
    protected abstract void initViewsAndEvents(View view, Bundle savedInstanceState);

    /**
     * bind layout resource file
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();

    public abstract P initPresenter();

}
