package com.tenegi.busstops;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by lyndon on 07/02/2017.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration{

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private static final String TAG = "Item Decorator";
    private Drawable divider;

    public DividerItemDecoration(Context context){
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }

    public DividerItemDecoration(Context context, int resId){
        divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - ((parent.getPaddingRight() + left) + 20);

        int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin + 5;
            int bottom = top + divider.getIntrinsicHeight();
            Log.d(TAG, "left= "+ left + " top=" + top + " right=" + right + " bottom=" + bottom);
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
    /*
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
    */
}
