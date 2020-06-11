package com.isgm.camreport.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.ib.custom.toast.CustomToastView;
import com.isgm.camreport.BuildConfig;
import com.isgm.camreport.R;
import com.isgm.camreport.utility.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_base);
        assert getSupportActionBar()!=null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null)
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null)
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        switch (menuItem.getItemId()) {
            case R.id.setting:
                Intent settingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                settingsActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(settingsActivityIntent);
                break;
            case R.id.recent:
                Intent recentHistoryActivityIntent = new Intent(getApplicationContext(), RecentHistoryActivity.class);
                recentHistoryActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(recentHistoryActivityIntent);
                break;
            case R.id.check_server:
                new BaseActivity.CheckServerAccess(this).execute(Utilities.getFromPreference(this, "server"));
                break;
            case R.id.logout:
                signOut();
                break;
            case R.id.about:
                try {
                    AlertDialog aboutAlertDialog =
                            new AlertDialog.Builder(BaseActivity.this).create();

                    if (aboutAlertDialog.getWindow() != null) {
                        Window window = aboutAlertDialog.getWindow();
                        window.setLayout(WRAP_CONTENT, WRAP_CONTENT);
                        window.setGravity(Gravity.CENTER);
                    }
                    aboutAlertDialog.setInverseBackgroundForced(true);
                    // Deprecated in API Level 23 (Android 6.0 Marshmallow
                    // This flag is only used for pre-Material Themes.
                    // Instead, specify the window background.
                    aboutAlertDialog.setTitle("About CamReport App");
                    aboutAlertDialog.setIcon(R.drawable.ic_info_blue_24dp);
                    String serial = "Permission Required";
                    if (Build.VERSION.SDK_INT > 26) {
                        serial = Build.getSerial();
                    }
                    aboutAlertDialog.setMessage(
                            "Application ID: " + BuildConfig.APPLICATION_ID +
                                    "\nBuild Type: " + BuildConfig.BUILD_TYPE +
                                    "\nVersion Name: " + BuildConfig.VERSION_NAME +
                                    "\nVersion Code: " + BuildConfig.VERSION_CODE +
                                    "\nDeviceManufacturer: " + Build.MANUFACTURER +
                                    "\nRadio Version: " + Build.getRadioVersion() +
                                    "\nSerialNumber: " + serial +
                                    "\nBootLoader: " + Build.BOOTLOADER +
                                    "\nBrand: " + Build.BRAND +
                                    "\nBoard: " + Build.BOARD +
                                    "\nDevice: " + Build.DEVICE +
                                    "\nDisplay: " + Build.DISPLAY +
                                    "\nFingerPrint: " + Build.FINGERPRINT +
                                    "\nHardware: " + Build.HARDWARE +
                                    "\nHost: " + Build.HOST +
                                    "\nID: " + Build.ID +
                                    "\nType: " + Build.TYPE +
                                    "\nModel: " + Build.MODEL);
                    aboutAlertDialog.setButton(Dialog.BUTTON_POSITIVE, "Report to Developers",
                            (dialogInterface, index) -> {
                                reportToDevelopers(aboutAlertDialog);
                                aboutAlertDialog.dismiss();
                            });
                    aboutAlertDialog.show();
                } catch (SecurityException securityexception) {
                    securityexception.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this).addOnCompleteListener((task) -> {
            finishAffinity(); // To delete Activity's History Data
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    // Check server access
    static class CheckServerAccess extends AsyncTask<String, String, Boolean> {
        final WeakReference<BaseActivity> context;

        CheckServerAccess(BaseActivity baseActivity) {
            this.context = new WeakReference<>(baseActivity);
        }

        protected Boolean doInBackground(String... urls) {
            try {
                if (urls.length != 1)
                    throw new IllegalArgumentException("Not Enough Arguments are given. Required one");
                publishProgress("Starting");
                URL url = new URL(urls[0]);
                publishProgress("Url Created");
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                publishProgress("Url Connected");
                return true;
            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (this.context.get() != null)
                CustomToastView.makeText(this.context.get(), Toast.LENGTH_SHORT, CustomToastView.INFO, values[0], false).show();
        }

        protected void onPostExecute(Boolean flag) {
            if (this.context.get() != null) {
                if (flag)
                    CustomToastView.makeText(this.context.get(), Toast.LENGTH_SHORT, CustomToastView.SUCCESS, "Server can be accessed!",
                            false).show();
                else
                    CustomToastView.makeText(this.context.get(), Toast.LENGTH_SHORT, CustomToastView.ERROR, "Server is not accessible!",
                            false).show();
            }
        }
    }

    private void reportToDevelopers(AlertDialog dialog) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", now);
        //Create Bitmap Screen Capture
        if (dialog.getWindow() != null) {
            View view = dialog.getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            File file = new File(Environment.getExternalStorageDirectory().toString() + "/CamreportReports" + now + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}