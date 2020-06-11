package com.isgm.camreport.activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import com.isgm.camreport.R;
import com.isgm.camreport.utility.Utilities;
import butterknife.ButterKnife;
import butterknife.OnClick;
public class ChooseSurveyPacActivity extends BaseActivity {
    String fiberOperationType;
    @OnClick(R.id.surveyButton) void Survey() {
        Intent intent = new Intent(ChooseSurveyPacActivity.this, CircuitAreaAndRouteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("surveyOrPAC","Survey");
        intent.putExtra("fiberOperationType", fiberOperationType);
        startActivity(intent);
    }
    @OnClick(R.id.pacButton) void PAC() {
        Intent intent = new Intent(ChooseSurveyPacActivity.this, CircuitAreaAndRouteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("surveyOrPAC","PAC");
        intent.putExtra("fiberOperationType", fiberOperationType);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        fiberOperationType = intent.getStringExtra("fiberOperationType");
        setContentView(R.layout.activity_choose_survey_pac);
        if(Utilities.getAllPermissions(this)) {
            initialize();
        }
    }
    private void initialize() {
        ButterKnife.bind(this);
        Utilities.saveToPreference(this, "server", "http://167.172.70.248:8000");
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0) {
            boolean isgGranted =true;
            for(int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isgGranted = false;
                    break;
                }
            }
            if (!isgGranted) { //Ask for permission
                Utilities.getAllPermissions(this);
            }
            else {
                initialize();
            }
        }
        else {
            Utilities.getAllPermissions(this); // Request For Permission Again
        }
        if (grantResults.length > 0) {
            if(grantResults[0]==PackageManager.PERMISSION_DENIED) {
                Utilities.getAllPermissions(this);
            }
        }
    }
}