package com.isgm.camreport.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.isgm.camreport.R;
import com.isgm.camreport.roomdb.History;
import com.isgm.camreport.viewholder.HistoryViewHolder;

public class RecyclerAdapter extends BaseRecyclerAdapter<HistoryViewHolder, History> {

    public RecyclerAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_view_item, viewGroup, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

}