package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserAndRedirect();
        }, 1000);
    }

    private void checkUserAndRedirect() {
        boolean isRegistered = prefs.getBoolean("user_registered", false);
        boolean onboardingCompleted = prefs.getBoolean("onboarding_completed", false);

        Intent intent;

        if (isRegistered) {
            intent = new Intent(this, CategorySelectionActivity.class);
        } else if (onboardingCompleted) {
            intent = new Intent(this, SimpleRegistrationActivity.class);
        } else {
            intent = new Intent(this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}