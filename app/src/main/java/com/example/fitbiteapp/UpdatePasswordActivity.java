package com.example.fitbiteapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitbiteapp.api.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdatePasswordActivity extends AppCompatActivity {

    TextInputLayout newPasswordLayout;
    MaterialButton btnUpdatePassword;
    CircularProgressIndicator loader;
    String recoveryAccessToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        newPasswordLayout = findViewById(R.id.input_new_password);
        btnUpdatePassword = findViewById(R.id.btn_update_password);
        loader = findViewById(R.id.loading_indicator);

        extractTokenFromIntent();

        btnUpdatePassword.setOnClickListener(v -> performPasswordUpdate());
    }

    private void extractTokenFromIntent() {
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null) {
            // Supabase puts tokens in the fragment (after the #), not query params
            String fragment = uri.getEncodedFragment();
            if (fragment != null) {
                String[] params = fragment.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("access_token")) {
                        recoveryAccessToken = keyValue[1];
                        break;
                    }
                }
            }
        }

        if (recoveryAccessToken == null) {
            Toast.makeText(this, "Invalid or expired reset link.", Toast.LENGTH_LONG).show();
            btnUpdatePassword.setEnabled(false);
        }
    }

    private void performPasswordUpdate() {
        String newPassword = newPasswordLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        SupabaseClient.getInstance().updatePassword(newPassword, recoveryAccessToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(UpdatePasswordActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(UpdatePasswordActivity.this, "Password Updated Successfully!", Toast.LENGTH_LONG).show();

                        // Send them back to Login Activity
                        Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(UpdatePasswordActivity.this, "Failed to update password. Link may have expired.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            loader.setVisibility(View.VISIBLE);
            btnUpdatePassword.setEnabled(false);
        } else {
            loader.setVisibility(View.GONE);
            btnUpdatePassword.setEnabled(true);
        }
    }
}