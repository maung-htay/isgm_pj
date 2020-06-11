package com.isgm.camreport.views.components;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SmoothScrollGridLayoutManager extends GridLayoutManager {
    private LinearSmoothScroller mLinearSmoothScroller;

    public SmoothScrollGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLinearSmoothScroller(context);
    }

    public SmoothScrollGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        initLinearSmoothScroller(context);
    }

    public SmoothScrollGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
        initLinearSmoothScroller(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        //super.smoothScrollToPosition(recyclerView, state, position);

        mLinearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(mLinearSmoothScroller);
    }

    private void initLinearSmoothScroller(Context context) {
        mLinearSmoothScroller = new LinearSmoothScroller(context) {
            private static final float MILLISECONDS_PER_INCH = 100f;

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                //return super.computeScrollVectorForPosition(targetPosition);
                return SmoothScrollGridLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                //return super.calculateSpeedPerPixel(displayMetrics);
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
    }
}
