package com.yunzhanghu.redpacketui.indexrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;



public interface StickyRecyclerHeadersAdapter<VH extends RecyclerView.ViewHolder> {
  /**
   * Get the ID of the header associated with this item.  For example, if your headers group
   * items by their first letter, you could return the character representation of the first letter.
   * Return a value < 0 if the view should not have a header (like, a header view or footer view)
   *
   * @param position 位置
   * @return long
   */
  long getHeaderId(int position);

  /**
   * Creates a new ViewHolder for a header.  This works the same way onCreateViewHolder in
   * Recycler.Adapter, ViewHolders can be reused for different views.  This is usually a good place
   * to inflate the layout for the header.
   *
   * @param parent ViewGroup
   * @return VH
   */
  VH onCreateHeaderViewHolder(ViewGroup parent);

  /**
   * Binds an existing ViewHolder to the specified adapter position.
   *
   * @param holder VH
   * @param position 位置
   */
  void onBindHeaderViewHolder(VH holder, int position);

  int getItemCount();
}
