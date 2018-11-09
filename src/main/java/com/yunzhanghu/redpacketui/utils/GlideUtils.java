package com.yunzhanghu.redpacketui.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by Max on 2018/11/8
 */
public class GlideUtils {
    public static void loadRoundAvatar(Context context, int placeholderResId, String url, ImageView target) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholderResId)
                .error(placeholderResId)
                .circleCrop();
        Glide.with(context).load(url)
                .apply(options)
                .into(target);

    }
}
