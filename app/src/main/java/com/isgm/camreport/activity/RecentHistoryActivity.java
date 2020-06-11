package com.isgm.camreport.activity;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.isgm.camreport.R;
import com.isgm.camreport.recyclerview.RecyclerAdapter;
import com.isgm.camreport.roomdb.DatabaseClient;
import com.isgm.camreport.roomdb.History;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecentHistoryActivity extends BaseActivity {
    private String date;
    private String dayString;
    private String monthString;
    private int year;
    private int month;
    private int day;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.data)
    TextView data;
    @BindView(R.id.chooseDate)
    EditText chooseDate;
    @BindView(R.id.total)
    TextView total;
    @BindView(R.id.count)
    TextView count;

    @OnClick(R.id.chooseDate)
    void onClick() {
        total.setVisibility(View.GONE);
        count.setVisibility(View.GONE);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day)
                -> {
            monthString = month < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
            dayString = day < 10 ? "0" + day : String.valueOf(day);
            date = dayString + "-" + monthString + "-" + year;
            chooseDate.setText(date);
            GetHistory getHistory = new GetHistory(RecentHistoryActivity.this);
            getHistory.execute();
        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_layout);
        ButterKnife.bind(this);
        // Get Current Date
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    static class GetHistory extends AsyncTask<Void, Void, List<History>> {
        final WeakReference<RecentHistoryActivity> context;

        GetHistory(RecentHistoryActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected List<History> doInBackground(Void... voids) {
            return DatabaseClient.getInstance(this.context.get().getApplicationContext())
                    .getAppDatabase().historyDao().getHistory(this.context.get().date,true);
        }

        @Override
        protected void onPostExecute(List<History> histories) {
            super.onPostExecute(histories);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.context.get().getApplicationContext());
            if (histories.isEmpty()) {
                this.context.get().data.setVisibility(View.VISIBLE);
                this.context.get().recyclerView.setVisibility(View.GONE);
            } else {
                this.context.get().total.setVisibility(View.VISIBLE);
                this.context.get().count.setVisibility(View.VISIBLE);
                this.context.get().count.setText(String.valueOf(histories.size()));
                this.context.get().data.setVisibility(View.GONE);
                this.context.get().recyclerView.setVisibility(View.VISIBLE);
                RecyclerAdapter recyclerAdapter = new RecyclerAdapter(this.context.get().getApplicationContext());
                recyclerAdapter.setNewData(histories);
                this.context.get().recyclerView.setLayoutManager(linearLayoutManager);
                this.context.get().recyclerView.setAdapter(recyclerAdapter);
            }
        }
    }
}