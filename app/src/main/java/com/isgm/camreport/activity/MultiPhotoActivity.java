package com.isgm.camreport.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.isgm.camreport.BuildConfig;
import com.isgm.camreport.R;
import com.isgm.camreport.network.APIService;
import com.isgm.camreport.network.ResponseSuccessEvent;
import com.isgm.camreport.network.RetrofitAgent;
import com.isgm.camreport.recyclerview.MultiPhotoAdapter;
import com.isgm.camreport.roomdb.DatabaseClient;
import com.isgm.camreport.roomdb.History;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MultiPhotoActivity extends BaseActivity implements MultiPhotoAdapter.OnPhotoListener {

    private static final String TAG = MultiPhotoActivity.class.getName();
    RecyclerView recyclerView;
    Button button, button_delete;
    ProgressDialog dialog;
    MultiPhotoAdapter multiPhotoAdapter;
    List<History> multiPhotoUtilList = new ArrayList<>();
    public static ItemTouchHelper itemTouchHelper;
    int upload_sum_count;
    int uploaded_sum_count;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_multi_photo);
        init();
        recyclerView = findViewById(R.id.card_view_recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        GetHistory getHistory = new GetHistory(this);
        getHistory.execute();

    }

    public class GetHistory extends AsyncTask<Void, Void, List<History>> {
        final WeakReference<MultiPhotoActivity> context;


        GetHistory(MultiPhotoActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected List<History> doInBackground(Void... voids) {
            return DatabaseClient.getInstance(this.context.get().getApplicationContext())
                    .getAppDatabase().historyDao().getNotSendData(false);
        }

        @Override
        protected void onPostExecute(List<History> histories) {
            super.onPostExecute(histories);

            if (histories.isEmpty()) {
                button.setEnabled(false);
                button_delete.setEnabled(false);
            } else {
                Log.i(TAG, "onPostExecute: Reach time");
                multiPhotoUtilList.clear();
                multiPhotoUtilList = histories;
                recyclerView.setLayoutManager(new LinearLayoutManager(this.context.get().getApplicationContext()));
                multiPhotoAdapter = new MultiPhotoAdapter(getBaseContext(), multiPhotoUtilList,MultiPhotoActivity.this::onPhotoClick);
                recyclerView.setAdapter(multiPhotoAdapter);
                // button.setEnabled(true);
                multiPhotoAdapter.notifyDataSetChanged();
            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        button = findViewById(R.id.button);
        button_delete = findViewById(R.id.button_delete);
        button_delete.setBackgroundColor(Color.RED);
        button.setEnabled(false);
        button_delete.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Do file uploading to server
                photoUploadingToServer();

                GetHistory getHistory = new GetHistory(MultiPhotoActivity.this);
                getHistory.execute();
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog alertDialog = new AlertDialog.Builder(MultiPhotoActivity.this).create();
                alertDialog.setTitle("Photo Deleting!");
                alertDialog.setMessage("Do you really want to delete? \n\nThis is permanently deleting.");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DeletePhotoFromDB();

                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                layoutParams.leftMargin = 2;
                layoutParams.rightMargin = 2;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void DeletePhotoFromDB() {
        List<History> selectedHistory = multiPhotoUtilList.stream().filter(newList -> newList.isSelected()).collect(Collectors.toList());
        for(History history: selectedHistory){
            if(history.isSelected()){
                deletePhotoById(history);
            }

        }

        finish();
        startActivity(getIntent());

    }

    private void deletePhotoById(History history) {
         class deletPhoto extends AsyncTask<Void, Void, Void> {

             @Override
             protected Void doInBackground(Void... voids) {
                 DatabaseClient.getInstance(MultiPhotoActivity.this.getApplicationContext())
                         .getAppDatabase().historyDao().deleteById(history.getId());
                 return null;
             }
         }
         new deletPhoto().execute();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void photoUploadingToServer() {

        List<History> selectedHistory = multiPhotoUtilList.stream().filter(newList -> newList.isSelected()).collect(Collectors.toList());
        upload_sum_count = selectedHistory.size();

        dialog = new ProgressDialog(MultiPhotoActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading...., Please Wait");
        dialog.show();

        Log.i(TAG, "photoUploadingToServer: => " + selectedHistory.size());
        for(History history: selectedHistory){
            // Log.i(TAG, "doInBackground: => " + history.isSelected());
            // no need but to be sure
            if(history.isSelected() == true) {
                sendDataToServer(history);
            }
        }
    }

    private void sendDataToServer(History history) {
        //Intialize variable
        byte[] imageBytes = new byte[0];
        final Gson gson = new Gson();
        // image to ByteArray
        try {
            InputStream inputStream = new FileInputStream(history.getImagePath());
            imageBytes = toByteArray(inputStream);
            inputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Covert Image to Base 64 String
        if (imageBytes != null) {
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            imageString = "data:image/jpg;base64," + imageString;
            // Upload Data to Server
            try {
                RetrofitAgent retrofit = RetrofitAgent.getInstance();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("altitude", history.getAltitude());
                jsonObject.addProperty("name", history.getImage());
                jsonObject.addProperty("image_name", history.getImage());
                jsonObject.addProperty("latitude", history.getLatitude());
                jsonObject.addProperty("longitude", history.getLongitude());
                jsonObject.addProperty("route_id", history.getRouteId());
                jsonObject.addProperty("category", history.getCategory());
                jsonObject.addProperty("activity_type", history.getActivityType());
                jsonObject.addProperty("basic", history.getFiberOperationType());
                jsonObject.addProperty("image_data", imageString);
                jsonObject.addProperty("remark", history.getRemark());
                jsonObject.addProperty("ver_code", BuildConfig.VERSION_NAME);
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(jsonObject);
                APIService apiService = retrofit.getApiService();
                Call<ResponseBody> call = apiService.uploadImage(jsonArray);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            JsonObject responseJsonObject;
                            try {
                                if (response.body() != null) {
                                    responseJsonObject = gson.fromJson(response.body().string(),
                                            JsonElement.class).getAsJsonObject();
                                    String status = responseJsonObject.get("status").getAsString();
                                    String error = responseJsonObject.get("error").getAsString();

                                    if (status.equals("success")) {
                                        // update progress bar and db
                                        Log.i(TAG, "onResponse: => Success" + uploaded_sum_count);
                                        ResponseSuccessEvent.ImageUploadedSuccessEvent event = new ResponseSuccessEvent.ImageUploadedSuccessEvent(status, error, true, history.getId());
                                        EventBus.getDefault().post(event);
                                    } else {
                                        Log.i(TAG, "onResponse: => Failure " );
                                        // show toast for failure and update db
                                        ResponseSuccessEvent.ImageUploadedSuccessEvent event = new ResponseSuccessEvent.ImageUploadedSuccessEvent(status, error, false, history.getId());
                                        EventBus.getDefault().post(event);
                                    }
                                }
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        // show toast for failure and update db
                        Log.i(TAG, "onFailure: Failure");
                        ResponseSuccessEvent.ImageUploadedSuccessEvent event = new ResponseSuccessEvent.ImageUploadedSuccessEvent("fail", "error", false, history.getId());
                        EventBus.getDefault().post(event);
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MultiPhotoActivity.this,
                    "Something went wrong! Please take the photo again!", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateDbIsSentStatus(boolean b, int id) {

        class updateIsSentStatus extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                Log.i(TAG, "doInBackground: => " + b);
                DatabaseClient.getInstance(MultiPhotoActivity.this.getApplicationContext())
                        .getAppDatabase().historyDao().updateById(b,id);
                return null;
            }
        }
        new updateIsSentStatus().execute();

    }

    // convert image input to byte[]
    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteArray = new byte[1024];
        int length;
        // Read Bytes from the InputStream and Store them in byteArray
        while ((length = inputStream.read(byteArray)) != -1) {
            // Write Bytes from the byteArray into OutputStream
            byteArrayOutputStream.write(byteArray, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }



    @Override
    public void onPhotoClick(int position, List<History> multiPhotoUtilsList) {
        boolean check = false;

        for(History multiPhoto: multiPhotoUtilsList){
            if(multiPhoto.isSelected() == true){
                check = true;
            }
        }

        if (check) {
            button.setEnabled(true);
            button_delete.setEnabled(true);
        }
        else {
            button.setEnabled(false);
            button_delete.setEnabled(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImageUploadSuccess(ResponseSuccessEvent.ImageUploadedSuccessEvent event) {

        Log.i("EventBus", event.getStatus() + ":-> result");
        updateDbIsSentStatus(event.isB(), event.getId());
        uploaded_sum_count += 1;

        if(dialog.isShowing() && upload_sum_count == uploaded_sum_count) {
            // fetch again
            dialog.dismiss();

            finish();
            startActivity(getIntent());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}