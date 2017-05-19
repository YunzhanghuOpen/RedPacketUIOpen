package com.yunzhanghu.redpacketui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.yunzhanghu.redpacketui.R;

/**
 * 圆角卡片
 *
 * @author color desert
 */
public class RPRoundCard extends RelativeLayout {
    /**
     * 默认圆角半径
     */
    private static final int DEFAULT_CORNER_RADIUS = 6;
    /**
     * 默认背景颜色
     */
    private static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#35B7F3");
    /**
     * 背景图片
     */
    private Bitmap mBg;
    /**
     * 圆角半径
     */
    private int mCornerRadius = DEFAULT_CORNER_RADIUS;
    /**
     * 背景颜色
     */
    private int mBgColor;
    /**
     * 背景类型0颜色值1图片
     */
    private int mBgType;

    public RPRoundCard(Context context) {
        this(context, null);
    }

    public RPRoundCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RPRoundCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);//设置调用onDraw方法  
        init(context, attrs, defStyle);
    }

    /**
     * 得到自定义属性
     *
     * @param context 环境变量
     * @param attrs   属性集合
     * @param defStyle  样式
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RPRoundCard, defStyle, 0);
        mCornerRadius = (int) ta.getDimension(R.styleable.RPRoundCard_RPCardRadius, DEFAULT_CORNER_RADIUS);
        mBgType = ta.getInt(R.styleable.RPRoundCard_RPCardBgType, 0);
        if (mBgType == 0) {
            mBgColor = ta.getColor(R.styleable.RPRoundCard_RPCardBgColor, DEFAULT_BACKGROUND_COLOR);
        } else {
            int resourceId = ta.getResourceId(R.styleable.RPRoundCard_RPCardBgImg, 0);
            mBg = BitmapFactory.decodeResource(getResources(), resourceId);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();//得到测量的高度
        int height = getMeasuredHeight();//得到测量的宽度
        if (mBgType == 0) {//使用颜色值设置背景
            canvas.drawBitmap(createRoundImage(width, height), 0, 0, null);//绘制圆角背景
        } else if (mBgType == 1 && mBg != null) {//使用图片设置背景
            mBg = Bitmap.createScaledBitmap(mBg, width, height, false);//创建一个缩放到指定大小的bitmap
            canvas.drawBitmap(createRoundImage(mBg, width, height), 0, 0, null);//绘制圆角背景
        }
        super.onDraw(canvas);//让RelativeLayout绘制自己  
    }

    /**
     * 创建圆角图片
     *
     * @param bitmap 源图片
     * @param width  宽度
     * @param height 高度
     * @return 圆角图片
     */
    private Bitmap createRoundImage(Bitmap bitmap, int width, int height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿画笔
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//创建一个bitmap
        Canvas canvas = new Canvas(target);//创建一个画布
        RectF rectF = new RectF(0, 0, width, height);//矩形
        //绘制圆角矩形  
        canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//画笔模式
        canvas.drawBitmap(bitmap, 0, 0, paint);//将画笔
        return target;
    }

    /**
     * 使用颜色值创建圆角图片
     *
     * @param width  宽度
     * @param height 高度
     * @return Bitmap
     */
    private Bitmap createRoundImage(int width, int height) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿画笔
        paint.setColor(mBgColor);
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//创建一个bitmap
        Canvas canvas = new Canvas(target);//创建一个画布
        RectF rectF = new RectF(0, 0, width, height);//矩形
        //绘制圆角矩形
        canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, paint);
        return target;
    }

    /**
     * 设置背景图片
     *
     * @param r 资源ID
     */
    public void setBGResource(int r) {
        this.mBg = BitmapFactory.decodeResource(getResources(), r);
        invalidate();
    }

    /**
     * 设置背景颜色
     *
     * @param color
     */
    public void setBGColor(int color) {
        this.mBgColor = color;
        invalidate();
    }

    /**
     * 设置背景颜色
     *
     * @param color
     */
    public void setBGColor(String color) {
        if (!TextUtils.isEmpty(color)) {
            this.mBgColor = Color.parseColor(color);
            invalidate();
        }
    }

    /**
     * 设置背景图片
     *
     * @param b bitmap
     */
    public void setBGBitmap(Bitmap b) {
        this.mBg = b;
        invalidate();
    }

}  