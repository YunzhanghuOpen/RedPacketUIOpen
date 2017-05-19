package com.yunzhanghu.redpacketui.indexrecyclerview;


import android.support.v7.widget.RecyclerView;

/**
 * Interface for getting the orientation of a RecyclerView from its LayoutManager
 */
interface OrientationProvider {

    int getOrientation(RecyclerView recyclerView);

    boolean isReverseLayout(RecyclerView recyclerView);
}
