
package com.yunzhanghu.redpacketui.utils;


/**
 * Author:  Max
 * Date:    2015/9/17
 */
public class PageUtil {

    private static volatile PageUtil instance = null;

    /**
     * 20 data per page
     */
    public static final int PAGE_LIMIT = 12;

    private PageUtil() {
    }

    public static PageUtil getInstance() {
        if (null == instance) {
            synchronized (PageUtil.class) {
                if (null == instance) {
                    instance = new PageUtil();
                }
            }
        }
        return instance;
    }

    public int calculateTotalPages(int totalNumber) {
        if (totalNumber > 0) {
            return totalNumber % PAGE_LIMIT != 0 ? (totalNumber / PAGE_LIMIT + 1) : totalNumber / PAGE_LIMIT;
        } else {
            return 0;
        }
    }


}
