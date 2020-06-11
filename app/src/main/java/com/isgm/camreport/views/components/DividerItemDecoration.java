package com.isgm.camreport.views.components;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.isgm.camreport.R;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    private Drawable mDivider;
    private Context mContext;

    public DividerItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mContext = context;
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        Resources resources = mContext.getResources();
        int bottomSpacing = (int) resources.getDimension(R.dimen.list_item_bottom_spacing);
        int rightSpacing = (int) resources.getDimension(R.dimen.list_item_right_spacing);
        outRect.set(0, bottomSpacing, 0, bottomSpacing);
    }
}
