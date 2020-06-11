package com.isgm.camreport.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isgm.camreport.R;
import com.isgm.camreport.activity.MultiPhotoActivity;
import com.isgm.camreport.model.MultiPhoto;
import com.isgm.camreport.roomdb.History;

import java.io.File;
import java.util.List;



public class MultiPhotoAdapter extends RecyclerView.Adapter<MultiPhotoAdapter.MultiPhotoAdapterViewHolder> {

    private List<History> multiPhotoUtilsList;
    private Context mContext;
    private OnPhotoListener mOnPhotoLister;
    int selected_position = 0;

    public MultiPhotoAdapter(Context baseContext, List<History> multiPhotoUtilList, OnPhotoListener onPhotoListener) {
        if (baseContext == null) {
            throw new NullPointerException("context can not be NULL");
        }
        if (multiPhotoUtilList == null) {
            throw new NullPointerException("MultiPhotos list can not be NULL");
        }
        this.multiPhotoUtilsList = multiPhotoUtilList;
        this.mContext = baseContext;
        this.mOnPhotoLister = onPhotoListener;

    }

    @NonNull
    @Override
    public MultiPhotoAdapter.MultiPhotoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_multiphoto_card_view, parent, false);

        return new MultiPhotoAdapterViewHolder(view, mOnPhotoLister);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiPhotoAdapter.MultiPhotoAdapterViewHolder holder, int position) {

        if(multiPhotoUtilsList != null){
            History multiPhoto = multiPhotoUtilsList.get(position);
            final int THUMBSIZE = 150;

            File imgFile = new  File(multiPhotoUtilsList.get(position).getImagePath());
            holder.itemView.setBackgroundColor(multiPhoto.isSelected() ? Color.CYAN : Color.WHITE);

            if(imgFile.exists()){

                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(imgFile.getAbsolutePath()),
                        THUMBSIZE,
                        THUMBSIZE);
                holder.photoImageView.setImageBitmap(thumbImage);

            }

            holder.photoTitleText.setText(multiPhotoUtilsList.get(position).getFiberOperationType());
            //  holder.photoImageView.setImageResource(multiPhotoUtilsList.get(position).get);
            holder.photoType.setText(multiPhotoUtilsList.get(position).getActivityType());
            holder.photoRoute.setText(multiPhotoUtilsList.get(position).getRoute());

            holder.photoDate.setText(multiPhotoUtilsList.get(position).getDate() + "   " +  multiPhotoUtilsList.get(position).getTime());


        }

    }

    @Override
    public int getItemCount() {
        return multiPhotoUtilsList.size();
    }

    public class MultiPhotoAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView photoTitleText,photoDate , photoType, photoRoute ;
        ImageView photoImageView ;
        CheckBox checkBox ;
        OnPhotoListener onPhotoListener;
        public MultiPhotoAdapterViewHolder(@NonNull View itemView, OnPhotoListener onPhotoListener) {
            super(itemView);
            Log.i("TAG", "Item View: ");
            photoTitleText = (TextView)itemView.findViewById(R.id.card_view_image_title);
            photoDate = (TextView) itemView.findViewById(R.id.card_view_image_date);
            photoType = (TextView)itemView.findViewById(R.id.card_view_image_type);
            photoRoute = (TextView) itemView.findViewById(R.id.card_view_image_route);
            photoImageView = (ImageView)itemView.findViewById(R.id.card_view_image);


            this.onPhotoListener = onPhotoListener;


            itemView.setOnClickListener(v -> {
                final History history = multiPhotoUtilsList.get(getAdapterPosition());
                history.setSelected(!history.isSelected());

                itemView.setBackgroundColor(history.isSelected() ? Color.CYAN : Color.WHITE);
                onPhotoListener.onPhotoClick(getAdapterPosition(), multiPhotoUtilsList);
            });
        }

    }
    public interface OnPhotoListener{
        void onPhotoClick(int position,List<History> multiPhotoUtilsList );
    }

}