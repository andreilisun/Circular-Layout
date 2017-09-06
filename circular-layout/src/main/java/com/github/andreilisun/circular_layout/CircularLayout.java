package com.github.andreilisun.circular_layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static java.lang.Math.max;

public class CircularLayout extends ViewGroup {

    private final double BAD_ANGLE = -1.0;

    private int maxWidth;
    private int maxHeight;

    private final Rect childRect = new Rect();
    private final Point childCenter = new Point();
    private final Point center = new Point();
    private double angleInRadians = BAD_ANGLE;
    private int inflatedChildCount;

    public CircularLayout(Context context) {
        super(context);
    }

    public CircularLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircularLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context
                .obtainStyledAttributes(attrs, R.styleable.CircularLayoutAttrs);

        int capacity = typedArray.getInteger(R.styleable.CircularLayoutAttrs_capacity, 0);
        if (capacity != 0) {
            setCapacity(capacity);
        }
        /*angle attr always wins*/
        float angle = typedArray.getFloat(R.styleable.CircularLayoutAttrs_angle, (float) BAD_ANGLE);
        if (angle != BAD_ANGLE) {
            setAngle(angle);
        }
        typedArray.recycle();
    }

    public void setAngle(double degrees) {
        this.angleInRadians = degreesToRadians(degrees);
        requestLayout();
    }

    public void setCapacity(int expectedViewsQuantity) {
        this.angleInRadians = degreesToRadians(360.0 / expectedViewsQuantity);
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        inflatedChildCount = getChildCount();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = max(maxWidth, child.getMeasuredWidth());
                maxHeight = max(maxHeight, child.getMeasuredHeight());
            }
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {

        final int childCount = getChildCount();

        /*if angle hasn't been set, try to calculate it
        taking into account how many items declared
        in xml inside CircularLayout*/
        if (angleInRadians == BAD_ANGLE && inflatedChildCount > 0) {
            setCapacity(childCount);
        }

        if (angleInRadians == BAD_ANGLE) {
            throw new IllegalStateException("set angle or capacity first with " +
                    "setAngle(double degrees)/setCapacity(int expectedViewsQuantity) or " +
                    "with xml angle/capacity attrs.");
        }

        int width = right - left;
        int height = bottom - top;

        if (width != height) {
            throw new IllegalStateException("width should be the same as height");
        }

        int radius = width / 2 - max(maxWidth / 2, maxHeight / 2) - maxPadding();
        center.set(width / 2, width / 2);

        boolean firstIsLaidOut = false;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                if (!firstIsLaidOut) {
                    childCenter.x = center.x;
                    childCenter.y = center.y - radius;
                    firstIsLaidOut = true;
                } else {
                    int deltaX = childCenter.x - center.x;
                    int deltaY = childCenter.y - center.y;
                    double cos = Math.cos(angleInRadians);
                    double sin = Math.sin(angleInRadians);
                    childCenter.x = (int) (center.x + deltaX * cos - deltaY * sin);
                    childCenter.y = (int) (center.y + deltaX * sin + deltaY * cos);
                }
                layoutChild(child);
            }
        }
    }

    private int maxPadding() {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        return max(max(max(paddingTop, paddingBottom), paddingLeft), paddingRight);
    }

    private void layoutChild(View child) {
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        childRect.top = childCenter.y - childHeight / 2;
        childRect.left = childCenter.x - childWidth / 2;
        childRect.right = childRect.left + childWidth;
        childRect.bottom = childRect.top + childHeight;
        child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
    }

    private double degreesToRadians(double degrees) {
        return (degrees * Math.PI / 180);
    }
}