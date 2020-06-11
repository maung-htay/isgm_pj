package com.isgm.camreport.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.ButterKnife;


/**
 * Base View Holder Class
 *
 * @param <T> ValueObject
 *
 * @author ZiSarKNar on 6/6/2020
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected boolean mDetachedFromWindow;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);

        itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mDetachedFromWindow = false;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mDetachedFromWindow = true;
            }
        });
    }

    public abstract void bind(T data);

    @Override
    public void onClick(View v) {

    }
}
