package com.isgm.camreport.views.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SmartRecyclerView extends RecyclerView {

    private SmoothScrollGridLayoutManager mLayoutManager;
    private View mEmptyView;
    private int mSpanCount = 1;
    private DividerItemDecoration mDividerItemDecoration;
    private HorizGridItemDecoration mHorizGridItemDecoration;
    private RecyclerViewContextMenuInfo mContextMenuInfo;

    private AdapterDataObserver dataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            checkIfEmpty();
        }
    };

    public SmartRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public SmartRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SmartRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(dataObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(dataObserver);
        }
        checkIfEmpty();
    }

    public void setEmptyView(View view) {
        mEmptyView = view;
        checkIfEmpty();
    }

    public void setupGridLayoutManager(int spanCount, boolean isHorizontal) {
        mSpanCount = spanCount;
        if (!isHorizontal) {
            mLayoutManager = new SmoothScrollGridLayoutManager(getContext(), spanCount); //
            removeAllItemDecorations();
            addItemDecoration(mDividerItemDecoration);
        } else {
            mLayoutManager = new SmoothScrollGridLayoutManager(getContext(), spanCount, GridLayoutManager.HORIZONTAL, false);
            removeAllItemDecorations();
            addItemDecoration(mHorizGridItemDecoration);
        }
        setLayoutManager(mLayoutManager);
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    private void init(Context context, AttributeSet attrs) {
        setHasFixedSize(true);
        mDividerItemDecoration = new DividerItemDecoration(getContext());
        mHorizGridItemDecoration = new HorizGridItemDecoration(getContext());
        mLayoutManager = new SmoothScrollGridLayoutManager(getContext(), mSpanCount); //
        setLayoutManager(mLayoutManager);

        setItemAnimator(new DefaultItemAnimator());
    }

    private void checkIfEmpty() {
        if (mEmptyView != null && getAdapter() != null) {
            final boolean isEmpty = getAdapter().getItemCount() == 0;
            mEmptyView.setVisibility(isEmpty ? VISIBLE : GONE);
            setVisibility(isEmpty ? GONE : VISIBLE);
        }
    }

    private void removeAllItemDecorations() {
        removeItemDecoration(mHorizGridItemDecoration);
        removeItemDecoration(mDividerItemDecoration);
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildPosition(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
            //This check doesn't require API-19.
            if (isAttachedToWindow()) {
                return super.showContextMenuForChild(originalView);
            }
        }
        return false;
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {
        final public int position;
        final public long id;

        public RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }
    }
}
