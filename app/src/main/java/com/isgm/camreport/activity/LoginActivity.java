package com.isgm.camreport.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ib.custom.toast.CustomToastView;
import com.isgm.camreport.R;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth firebaseAuth;

    @BindView(R.id.fieldEmail)
    EditText edtEmail;

    @BindView(R.id.fieldPassword)
    EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this, this);
        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.resetPasswordButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.verifyEmailButton).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        updateUI(firebaseUser);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.resetPasswordButton) resetPassword(edtEmail.getText().toString().trim());
        else if (i == R.id.emailSignInButton)
            signIn(edtEmail.getText().toString(), edtPassword.getText().toString());
        else if (i == R.id.signOutButton) signOut();
        else if (i == R.id.verifyEmailButton) sendEmailVerification();
    }

    private void updateUI(FirebaseUser firebaseuser) {
        if (firebaseuser != null) {
            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.GONE);
            Intent intent = new Intent(this, FiberOperationTypeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.emailPasswordFields).setVisibility(View.VISIBLE);
            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
        }
    }

    private void signIn(String email, String password) {
        if (validateForm()) return;
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, (task) -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                updateUI(firebaseUser);
            } else {
                CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.ERROR, "Authentication Failed!",
                        false).show();
                updateUI(null);
            }
        });
    }

    private void signOut() {
        firebaseAuth.signOut();
        updateUI(null);
    }

    private void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, (task) ->
        {
            if (task.isSuccessful())
                CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_LONG, CustomToastView.SUCCESS,
                        "Reset Password link is sent to email successfully",
                        false).show();
            else
                CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_LONG, CustomToastView.ERROR, "Error when sending reset password email",
                        false).show();
        });
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = this.edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            this.edtEmail.setError("Required!");
            valid = false;
        } else this.edtEmail.setError(null);
        String password = this.edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            this.edtPassword.setError("Required!");
            valid = false;
        } else this.edtPassword.setError(null);
        return !valid;
    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verifyEmailButton).setEnabled(false);
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Objects.requireNonNull(firebaseUser).sendEmailVerification().
                addOnCompleteListener(this, (task) -> {
                    findViewById(R.id.verifyEmailButton).setEnabled(true);
                    if (task.isSuccessful())
                        CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.SUCCESS,
                                "Verification email sent to " + firebaseUser.getEmail(), false).show();
                    else CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.ERROR,
                            "Failed to send verification email.", false).show();
                });
    }
}