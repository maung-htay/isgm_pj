package com.isgm.camreport.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isgm.camreport.R;
import com.isgm.camreport.roomdb.History;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyRecyclerViewHolder> implements ItemTouchHelperAdapter {
    Context context;
    List<History> data;

    public MyRecyclerViewAdapter(Context context, List<History> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyRecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_multiphoto_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyRecyclerViewHolder holder, int position) {
        holder.name.setText(data.get(position).getFiberOperationType());
        final int THUMBSIZE = 228;
        File imgFile = new  File(data.get(position).getImagePath());

        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(imgFile.getAbsolutePath()),
                    THUMBSIZE,
                    THUMBSIZE);
            holder.imageView.setImageBitmap(myBitmap);
        }
        holder.imageDate.setText(data.get(position).getDate() + data.get(position).getTime());
        holder.imageType.setText(data.get(position).getActivityType());
        holder.imageRoute.setText(data.get(position).getRoute());

       /* holder.dragdrop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    MultiPhotoActivity.itemTouchHelper.startDrag(holder);
                }
                return false;
            }
        });*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You click position:" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        History moveItem = data.get(fromPosition);
        data.remove(fromPosition);
        data.add(toPosition, moveItem);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        Log.i("TAG," ,"onItemDismiss: ");
    }

    public class MyRecyclerViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.card_view_image_title)
        TextView name;

        @BindView(R.id.card_view_image)
        ImageView imageView;

        @BindView(R.id.card_view_image_date)
        TextView imageDate;

        @BindView(R.id.card_view_image_type)
        TextView imageType;

        @BindView(R.id.card_view_image_route)
        TextView imageRoute;

        public MyRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnPhotoListener{
        void onPhotoClick(List<History> multiPhotoUtilsList );
    }
}