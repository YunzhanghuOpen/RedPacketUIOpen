package com.yunzhanghu.redpacketui.ui.base;

/**
 * Author:  Max
 * Date:    2015/17/9.
 * Description: the base view
 */
interface RPBaseView {

    /**
     * show loading message
     */
    void showLoading();

    /**
     * hide loading
     */
    void hideLoading();

    /**
     * show toast message
     */
    void showToastMsg(String msg);

}
