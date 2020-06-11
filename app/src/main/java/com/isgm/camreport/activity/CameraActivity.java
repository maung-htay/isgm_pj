package com.isgm.camreport.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ib.custom.toast.CustomToastView;
import com.isgm.camreport.BuildConfig;
import com.isgm.camreport.R;
import com.isgm.camreport.network.APIService;
import com.isgm.camreport.network.RetrofitAgent;
import com.isgm.camreport.roomdb.DatabaseClient;
import com.isgm.camreport.roomdb.History;
import com.isgm.camreport.utility.GetCurrentLocation;
import com.isgm.camreport.utility.ShowAlerts;
import com.isgm.camreport.utility.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends BaseActivity {
    private byte[] imageBytes;
    private double latitude;
    private double longitude;
    private double altitude;
    private double useLatitude;
    private double useLongitude;
    private File imageFile;
    private int routeId;
    private PhotoView photoView, photoViewUnused;
    private String fiberOperationType;
    private String route; // Real Route Code [not include Township Name]
    private String routeName; // Route Code got from Previous Activity [include Township Name]
    private String type;
    private String subType1;
    private String subType2;
    private String code;
    private String imageFileName;
    private String surveyOrPAC;
    private String remark;
    private Uri photoUri = null;
    private final Gson gson = new Gson();
    static String userEmail = "";
    GetCurrentLocation getCurrentLocation;

    @BindView(R.id.lLayoutSend)
    LinearLayout lLayoutSend;

    @BindView(R.id.lLayoutPhoto)
    LinearLayout lLayoutCapture;

    @BindView(R.id.lLayoutSave)
    LinearLayout lLayoutSave;

    @BindView(R.id.lLayoutGallery)
    LinearLayout lLayoutGallery;

    @BindView(R.id.sendPhotoButton)
    FloatingActionButton sendPhotoButton;

    @BindView(R.id.takePhotoButton)
    FloatingActionButton takePhotoButton;

    @BindView(R.id.previewText)
    TextView previewPhotoText;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.fabSave)
    FloatingActionButton savePhotoButton;

    @BindView(R.id.fabGallery)
    FloatingActionButton galleryButton;

    @OnClick(R.id.takePhotoButton)
    void openCamera() {
        openCameraIntent();
    }

    @OnClick(R.id.sendPhotoButton)
    void sendData() {
        sendDataToServer();
    }

    @OnClick(R.id.fabSave)
    void savePhotoInfo() {
        savePhotoButton.setEnabled(false);
        savePhoto();
    }

    @OnClick(R.id.fabGallery)
    void showGallery() {
        Intent intent = new Intent(this, MultiPhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initialize();

        if (isMyServiceRunning(GetCurrentLocation.class)) {
            Log.i("GPS", "GetLocationService started");
        }
    }

    // Stop the gps service
    @Override
    protected void onStop() {
        super.onStop();
        if (isMyServiceRunning(GetCurrentLocation.class)) {
            stopService(new Intent(this, GetCurrentLocation.class));
            Log.i("GPS", "GetLocationService stopped");
        }
    }

    private void initialize() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        userEmail = firebaseUser.getEmail();
        ButterKnife.bind(this);
        photoView = findViewById(R.id.imageView);
        photoViewUnused = findViewById(R.id.imageViewUnused);
        getDataFromPreviousActivity();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { // After Taking Photo
        super.onActivityResult(requestCode, resultCode, intent);
        takePhotoButton.setEnabled(true);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            ButterKnife.bind(this);
            previewPhotoText.setVisibility(View.GONE);
            photoViewUnused.setImageURI(photoUri);

            getLocationData();

            if (useLatitude != 0.0 || useLongitude != 0.0) {
                // water mark
                Bitmap bitmap = fiberPhotoProcessing();
                photoView.setImageBitmap(bitmap);
                photoView.setVisibility(View.VISIBLE);
                previewPhotoText.setVisibility(View.GONE);
                lLayoutSend.setVisibility(View.VISIBLE);
                lLayoutSave.setVisibility(View.VISIBLE);
                savePhotoButton.setEnabled(true);
                getContentResolver().delete(photoUri, null, null); // Delete raw photo without texts on photo
            } else {
                previewPhotoText.setVisibility(View.VISIBLE);
                photoViewUnused.setVisibility(View.GONE);
                photoView.setVisibility(View.GONE);
                lLayoutSend.setVisibility(View.GONE);
                lLayoutSave.setVisibility(View.GONE);
                ShowAlerts.showAlert(
                        CameraActivity.this, "Error", "Please set your location service to High Accuracy.",
                        R.drawable.ic_error_black_24dp);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoUri != null) {
            outState.putString("cameraImageUri", photoUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            photoUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    // Get intent data
    private void getDataFromPreviousActivity() {
        Intent intent = getIntent();
        surveyOrPAC = intent.getStringExtra("surveyOrPAC");
        routeId = intent.getIntExtra("routeId", 0);
        routeName = intent.getStringExtra("routeName");
        type = intent.getStringExtra("type");
        subType1 = intent.getStringExtra("subType1");
        subType2 = intent.getStringExtra("subType2");
        code = intent.getStringExtra("code");
        remark = intent.getStringExtra("remark");
        fiberOperationType = intent.getStringExtra("fiberOperationType");
        Utilities.saveIntToPreference(this, "routeId", routeId);
        Utilities.saveToPreference(this, "route", routeName);
        if(!fiberOperationType.equals("FTTX")){
            Utilities.saveToPreference(this, "shouldSearchFromRoute", "false");
        }
        if (code.length() == 1) {
            code = "000" + code;
        } else if (code.length() == 2) {
            code = "00" + code;
        } else if (code.length() == 3) {
            code = "0" + code;
        }
        if (routeName.contains("[")) {
            int index = routeName.indexOf("["); // To remove Township Name from Route Name
            route = routeName.substring(0, index);
        } else {
            route = routeName;
        }
    }

    private void openCameraIntent() {
        takePhotoButton.setEnabled(false); // To prevent Monkey Click Error in takePhotoButton
        useLatitude = 0.0;
        useLongitude = 0.0;
        latitude = 0.0;
        longitude = 0.0;
        if (getLocation()) {
            takePhotoButton.setEnabled(true);
            Intent actionImageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, "MyPicture");
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            actionImageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(actionImageCaptureIntent, 1);
        } else {
            AlertDialog dialog = new AlertDialog.Builder(CameraActivity.this).create();
            dialog.setTitle("Location");
            dialog.setIcon(R.drawable.ic_error_black_24dp);
            dialog.setMessage("Location data is needed to take a photo.");
            dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                takePhotoButton.setEnabled(true);

            });
            dialog.show();
        }
    }

    /**
     * Get actual lat, lng data
     */
    private void getLocationData() {
        if (getCurrentLocation != null) {
            getCurrentLocation = new GetCurrentLocation(this);
            Log.i("GPS", "GetCurrentLocation Object created.");
        }
        getCurrentLocation.getLocation();
        latitude = getCurrentLocation.getLatitude();
        longitude = getCurrentLocation.getLongitude();
        altitude = getCurrentLocation.getAltitude();
        // To set 6 Points to Latitude and Longitude
        int temporaryLatitude = (int) (latitude * 1000000.0);
        useLatitude = ((double) temporaryLatitude) / 1000000.0;
        int temporaryLongitude = (int) (longitude * 1000000.0);
        useLongitude = ((double) temporaryLongitude) / 1000000.0;

    }

    /**
     * Check location is able to get
     *
     * @return [true] enable [false] disable
     */
    private boolean getLocation() {
        getCurrentLocation = new GetCurrentLocation(this);
        return getCurrentLocation.getLocation();
    }

    /**
     * Check if the background service is running
     *
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        Log.i("GPS", "Service Checking......");
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private Bitmap fiberPhotoProcessing() {
        Bitmap bitmap;
        Bitmap newBitmap = null;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) photoViewUnused.getDrawable();
        try {
            //
            bitmap = bitmapDrawable.getBitmap();

            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }
            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTextSize(90);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setShadowLayer(10f, 10f, 10f, Color.BLACK);

            // write text
            String textsToWriteOnPhoto = getTextsToWriteOnPhoto();
            String[] textToWriteOnPhotos = textsToWriteOnPhoto.split("\n");
            int noOfLines = textToWriteOnPhotos.length;
            Rect rect = new Rect();
            paint.getTextBounds(textsToWriteOnPhoto, 0, textsToWriteOnPhoto.length(), rect);
            int x = 20;
            int y = (newBitmap.getHeight() - rect.height() * noOfLines);
            for (String textToWriteOnPhoto : textToWriteOnPhotos) {
                canvas.drawText(textToWriteOnPhoto, x, y, paint);
                y += paint.descent() - paint.ascent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newBitmap;
    }

    private String getTextsToWriteOnPhoto() {
        String subCategory1 = subType1;
        String subCategory2 = subType2;
        String newCode = code;
        if (!subCategory1.equals("")) {
            if (subCategory2.equals("")) {
                if (newCode.equals("")) {
                    subCategory1 = "_" + subCategory1;
                    newCode = "\n";
                } else {
                    subCategory1 = "_" + subCategory1;
                    newCode = "_" + newCode + "\n";
                }
            } else {
                if (newCode.equals("")) {
                    subCategory1 = "_" + subCategory1;
                    subCategory2 = "_" + subCategory2;
                    newCode = "\n";
                } else {
                    subCategory1 = "_" + subCategory1;
                    subCategory2 = "_" + subCategory2;
                    newCode = "_" + newCode + "\n";
                }
            }
        } else {
            if (newCode.equals("")) newCode = "\n";
            else newCode = "_" + newCode + "\n";
        }
        return "Latitude : " + useLatitude + "\nLongitude : " + useLongitude + "\n" + route + "\n"
                + type + subCategory1 + subCategory2 + newCode + Utilities.nowTimeFormat2() + "\n" + "";
    }

    private void sendDataToServer() {
        sendPhotoButton.setEnabled(false);
        photoView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // save to Gallery
        savePhotoToShowInGallery(route);
        // save to database
        SaveHistory saveHistory = new SaveHistory(CameraActivity.this);
        saveHistory.execute();

        // image to ByteArray
        try {
            InputStream inputStream = new FileInputStream(imageFile);
            imageBytes = toByteArray(inputStream);
            inputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Covert Image to Base 64 String
        if (imageBytes != null) {
            String imageString = Base64.encodeToString(this.imageBytes, Base64.DEFAULT);
            imageString = "data:image/jpg;base64," + imageString;
            // Upload Data to Server
            try {
                RetrofitAgent retrofit = RetrofitAgent.getInstance();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("altitude", altitude);
                jsonObject.addProperty("name", imageFileName);
                jsonObject.addProperty("image_name", imageFileName);
                jsonObject.addProperty("latitude", useLatitude);
                jsonObject.addProperty("longitude", useLongitude);
                jsonObject.addProperty("route_id", routeId);
                jsonObject.addProperty("category", calculateCategory());
                jsonObject.addProperty("activity_type", surveyOrPAC);
                jsonObject.addProperty("basic", fiberOperationType);
                jsonObject.addProperty("image_data", imageString);
                jsonObject.addProperty("remark", remark);
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
                                        photoView.setVisibility(View.VISIBLE);
                                        sendPhotoButton.setEnabled(true);
                                        progressBar.setVisibility(View.GONE);
                                        UpdateHistory updateHistory = new UpdateHistory(CameraActivity.this);
                                        updateHistory.execute();
                                        ShowAlerts.showAlert(
                                                CameraActivity.this, "Success",
                                                "Photo Uploaded Successfully",
                                                R.drawable.ic_sentiment_very_satisfied_black_24dp);
                                    } else {
                                        photoView.setVisibility(View.VISIBLE);
                                        sendPhotoButton.setEnabled(true);
                                        progressBar.setVisibility(View.GONE);
                                        ShowAlerts.showAlert(
                                                CameraActivity.this, "Error", error,
                                                R.drawable.ic_error_black_24dp);
                                    }
                                }
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        photoView.setVisibility(View.VISIBLE);
                        sendPhotoButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        ShowAlerts.showAlert(
                                CameraActivity.this, "Error", "Connection Error",
                                R.drawable.ic_error_black_24dp);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CameraActivity.this,
                    "Something went wrong! Please take the photo again!", Toast.LENGTH_SHORT).show();
        }
    }


    private void savePhotoToShowInGallery(String route) {
        String subCategory1 = subType1;
        String subCategory2 = subType2;
        String newCode = code;
        String newRemark = remark;

        // Save Photo in Gallery
        BitmapDrawable bitmapDrawable = (BitmapDrawable) photoView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        // Get max size
        String maxImgSize = Utilities.getFromPreference(this, "image_size");

        Double number;

        // if number string
        try {
           number = Double.parseDouble(maxImgSize);
        } catch (Exception e) {
            number = 1920.0;
        }

        // if not positive number string
        if(number < 0) {
              number =  1920.0;
        }

        Bitmap scaledBitmap = getResizedBitmap(bitmap, number);

        File camreportImagesDirectory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // For Android 10
            camreportImagesDirectory = getExternalFilesDir(Environment.DIRECTORY_DCIM + "/CamReport/" + route);
        } else {
            camreportImagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/CamReport/" + route);
        }
        if (camreportImagesDirectory == null) {
            throw new NullPointerException("No External files directory found.");
        }
        boolean dir = camreportImagesDirectory.mkdirs();

        if (!subCategory1.equals("")) {
            subCategory1 = "_" + subCategory1;
        }
        if (!subCategory2.equals("")) {
            subCategory2 = "_" + subCategory2;
        }
        if (!newCode.equals("")) {
            newCode = "_" + newCode;
        }
        if (!newRemark.equals("")) {
            newRemark = "_" + newRemark;
            if (newRemark.contains("/")) {
                newRemark = newRemark.replace("/", ",");
            }
        }
        imageFileName = fiberOperationType + "_" + type + subCategory1 + subCategory2 + newCode + newRemark +
                "_" + Utilities.nowTimeFormat1() + ".jpg";
        File file = new File(camreportImagesDirectory, imageFileName);
        imageFile = file;

        if (file.exists()) {
            CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.INFO,
                    "Image is already saved!", false).show();
        } else {

            // Write bitmap to file
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // For Android 10
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM +
                        "/CamReport/" + route);
                Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    assert uri != null;
                    OutputStream outputStream = contentResolver.openOutputStream(uri);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                // For Non-Android 10
            }
        }
        addMetaDataToPhoto(file);
    }

    private void addMetaDataToPhoto(File file) {
        try {
            imageFile = file;
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, dec2DMS(useLatitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, dec2DMS(useLongitude));
            exifInterface.setAttribute(ExifInterface.TAG_ARTIST, userEmail);
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, Utilities.nowTimeFormat1());
            exifInterface.saveAttributes();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String dec2DMS(double coordinate) {
        coordinate = coordinate > 0 ? coordinate : -coordinate;
        String sOut = (int) coordinate + "/1,";
        coordinate = (coordinate % 1) * 60;
        sOut = sOut + (int) coordinate + "/1,";
        coordinate = (coordinate % 1) * 60000;
        sOut = sOut + (int) coordinate + "/1000";
        return sOut;
    }

    //  Save and archived to send later
    private void savePhoto() {
        savePhotoToShowInGallery(route);
        SaveHistory history = new SaveHistory(this);
        history.execute();
    }

    private String calculateCategory() {
        if (subType1.equals("")) {
            return type;
        }
        return subType2.equals("") ? subType1 : subType2;
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

    // Save history
    static class SaveHistory extends AsyncTask<Void, Void, Void> {
        final WeakReference<CameraActivity> context;

        SaveHistory(CameraActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            History history = new History();
            history.setFiberOperationType(this.context.get().fiberOperationType);
            history.setActivityType(this.context.get().surveyOrPAC);
            history.setRemark(this.context.get().remark);
            history.setCategory(this.context.get().calculateCategory());
            history.setRouteId(this.context.get().routeId);
            history.setRoute(this.context.get().routeName);
            history.setImage(this.context.get().imageFileName);
            history.setImagePath(this.context.get().imageFile.getAbsolutePath());
            history.setDate(Utilities.nowTimeFormat3());
            history.setTime(Utilities.nowTimeFormat4());
            history.setLatitude(this.context.get().useLatitude);
            history.setLongitude(this.context.get().useLongitude);
            history.setAltitude(this.context.get().altitude);
            history.setSent(false);

            DatabaseClient.getInstance(this.context.get().getApplicationContext()).getAppDatabase()
                    .historyDao().insert(history);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(this.context.get(), "Photo is saved", Toast.LENGTH_LONG).show();
        }
    }

    // Update history
    static class UpdateHistory extends AsyncTask<Void, Void, Void> {
        final WeakReference<CameraActivity> context;

        UpdateHistory(CameraActivity context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseClient.getInstance(this.context.get().getApplicationContext()).getAppDatabase()
                    .historyDao().update(true, this.context.get().imageFileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(this.context.get(), "Photo is sent", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * reduces the size of the image
     *
     * @param image
     * @param maxSize
     * @return scaledBitmap
     */
    public Bitmap getResizedBitmap(Bitmap image, double maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        // check if need to compress
        if (width < maxSize) {
            return image;
        }

        int newHeight = (int) (height * (maxSize / width));
        return Bitmap.createScaledBitmap(image, (int) maxSize, newHeight, true);
    }

}