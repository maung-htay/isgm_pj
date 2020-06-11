package com.isgm.camreport.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.isgm.camreport.R;
import com.isgm.camreport.roomdb.History;

import butterknife.BindView;

public class HistoryViewHolder extends BaseViewHolder<History> {

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.route_name)
    TextView route;

    @BindView(R.id.photo_file)
    TextView photo;

    @BindView(R.id.location)
    TextView location;

    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(History data) {
        route.setText(data.getRoute());
        photo.setText(data.getImage());
        date.setText(data.getDate() +" "+ data.getTime());
        location.setText(data.getLatitude() +", " +data.getLongitude());
    }

}
