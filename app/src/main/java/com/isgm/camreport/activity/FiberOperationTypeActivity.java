package com.isgm.camreport.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.isgm.camreport.R;
import com.isgm.camreport.utility.Utilities;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FiberOperationTypeActivity extends BaseActivity {
    @OnClick(R.id.fttxButton)
    void fttxButtonClick() {
        Intent intent = new Intent(FiberOperationTypeActivity.this, ChooseSurveyPacActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fiberOperationType", "FTTX");
        startActivity(intent);
    }


    @OnClick(R.id.b2bButton)
    void b2bButtonClick() {
        Intent intent = new Intent(FiberOperationTypeActivity.this, ChooseSurveyPacActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fiberOperationType", "B2B");
        startActivity(intent);
    }

    @OnClick(R.id.mobiButton)
    void mobiButtonClick() {
        Intent intent = new Intent(FiberOperationTypeActivity.this, ChooseSurveyPacActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fiberOperationType", "MOBI");
        startActivity(intent);
    }

    @OnClick(R.id.b2bPremiumButton)
    void b2bPremiumButtonClick() {
        Intent intent = new Intent(FiberOperationTypeActivity.this, ChooseSurveyPacActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("fiberOperationType", "B2B_PREMIUM");
        startActivity(intent);
    }

    @OnClick(R.id.btnGallery)
    void galleryBtnClick() {
        Intent intent = new Intent(this, MultiPhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiber_operation_type);
        assert getSupportActionBar()!=null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if (Utilities.getAllPermissions(this)) {
            initialize();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FiberOperationTypeActivity.this);
        builder.setTitle("Exit from App?");
        builder.setIcon(R.drawable.ic_exit_to_app_black_24dp);
        builder.setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initialize() {
        ButterKnife.bind(this, this);
        Utilities.saveToPreference(this, "server", "http://167.172.70.248:8000");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean isGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (!isGranted) {
                Utilities.getAllPermissions(this); //Ask for permission
            } else {
                initialize();
            }
        } else Utilities.getAllPermissions(this); //Request For Permission Again
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Utilities.getAllPermissions(this);
            }
        }
    }
}