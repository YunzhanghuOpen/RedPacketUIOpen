package com.yunzhanghu.redpacketui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunzhanghu.redpacketui.R;

/**
 * 标题栏
 */
public class RPTitleBar extends RelativeLayout {

    protected RelativeLayout leftLayout;
    protected ImageView leftImage;
    protected RelativeLayout rightImageLayout;
    protected RelativeLayout rightTextLayout;
    protected ImageView rightImage;
    protected TextView rightText;
    protected TextView titleView;
    protected TextView subTitleText;
    protected RelativeLayout titleLayout;


    public RPTitleBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public RPTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RPTitleBar(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.rp_widget_title_bar, this);
        leftLayout = (RelativeLayout) findViewById(R.id.left_layout);
        leftImage = (ImageView) findViewById(R.id.left_image);
        rightImageLayout = (RelativeLayout) findViewById(R.id.right_layout);
        rightTextLayout = (RelativeLayout) findViewById(R.id.right_text_layout);
        rightImage = (ImageView) findViewById(R.id.right_image);
        rightText = (TextView) findViewById(R.id.right_text);
        titleView = (TextView) findViewById(R.id.title);
        subTitleText = (TextView) findViewById(R.id.subtitle);
        titleLayout = (RelativeLayout) findViewById(R.id.root);

        parseStyle(context, attrs);
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.app);
            String title = ta.getString(R.styleable.app_RPmytitle);
            int color = ta.getColor(R.styleable.app_RPtitleTextColor, ContextCompat.getColor(context, R.color.rp_title_color));
            float size = ta.getDimension(R.styleable.app_RPtitleTextSize, 17);
            titleView.setText(title);
            titleView.setTextColor(color);
            titleView.setTextSize(size);
            String rightMsg = ta.getString(R.styleable.app_RPrightText);
            int rightColor = ta.getColor(R.styleable.app_RPrightTextColor, ContextCompat.getColor(context, R.color.rp_title_color));
            float rightSize = ta.getDimension(R.styleable.app_RPrightTextSize, 15);
            rightText.setText(rightMsg);
            rightText.setTextColor(rightColor);
            rightText.setTextSize(rightSize);
            String subText = ta.getString(R.styleable.app_RPsubTitleText);
            int subColor = ta.getColor(R.styleable.app_RPsubTitleTextColor, ContextCompat.getColor(context, R.color.rp_title_transparent_color));
            float subSize = ta.getDimension(R.styleable.app_RPsubTitleTextSize, 10);
            subTitleText.setText(subText);
            subTitleText.setTextColor(subColor);
            subTitleText.setTextSize(subSize);
            Drawable leftDrawable = ta.getDrawable(R.styleable.app_RPleftImage);
            if (null != leftDrawable) {
                leftImage.setImageDrawable(leftDrawable);
            }
            Drawable rightDrawable = ta.getDrawable(R.styleable.app_RPrightImage);
            if (null != rightDrawable) {
                rightImage.setImageDrawable(rightDrawable);
            }

            Drawable background = ta.getDrawable(R.styleable.app_RPtitleBackground);
            if (null != background) {
                titleLayout.setBackgroundDrawable(background);
            }

            ta.recycle();
        }
    }

    public void setLeftImageResource(int resId) {
        leftImage.setImageResource(resId);
    }

    public void setRightImageResource(int resId) {
        rightImage.setImageResource(resId);
    }

    public void setLeftLayoutClickListener(OnClickListener listener) {
        leftLayout.setOnClickListener(listener);
    }

    public void setRightImageLayoutClickListener(OnClickListener listener) {
        rightImageLayout.setOnClickListener(listener);
    }

    public void setRightTextLayoutClickListener(OnClickListener listener) {
        rightTextLayout.setOnClickListener(listener);
    }

    public void setLeftLayoutVisibility(int visibility) {
        leftLayout.setVisibility(visibility);
    }

    public void setRightImageLayoutVisibility(int visibility) {
        rightImageLayout.setVisibility(visibility);
    }

    public void setRightTextLayoutVisibility(int visibility) {
        rightTextLayout.setVisibility(visibility);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setSubTitle(String subTitle) {
        subTitleText.setText(subTitle);
    }

    public void setTitleColor(int titleColor) {
        titleView.setTextColor(titleColor);
    }

    public void setSubTitleColor(int subTitleColor) {
        subTitleText.setTextColor(subTitleColor);
    }

    public void setSubTitleVisibility(int visibility) {
        subTitleText.setVisibility(visibility);
    }

    public void setRightText(String text) {
        rightText.setText(text);
    }

    public void setBackgroundColor(int color) {
        titleLayout.setBackgroundColor(color);
    }

    public RelativeLayout getLeftLayout() {
        return leftLayout;
    }

    public RelativeLayout getRightImageLayout() {
        return rightImageLayout;
    }

    public RelativeLayout getRightTextLayout() {
        return rightTextLayout;
    }
}
