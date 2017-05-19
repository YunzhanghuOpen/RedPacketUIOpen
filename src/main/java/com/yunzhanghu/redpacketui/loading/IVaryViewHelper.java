package com.yunzhanghu.redpacketui.loading;

import android.content.Context;
import android.view.View;

/**
 * Created by max on 15/9/17
 * 加载相关接口
 */
public interface IVaryViewHelper {

    View getCurrentLayout();

    void restoreView();

    void showLayout(View view);

    View inflate(int layoutId);

    Context getContext();

    View getView();
}
