package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends BaseActivity  {

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
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        boolean onboardingCompleted = prefs.getBoolean("onboarding_completed", false);

        Intent intent;

        // 1️⃣ Уже зарегистрирован и залогинен
        if (isRegistered && isLoggedIn) {
            intent = new Intent(this, MainMenuActivity.class);
        }
        // 2️⃣ Зарегистрирован, но НЕ залогинен → показать логин
        else if (isRegistered) {
            intent = new Intent(this, LoginActivity.class);
        }
        // 3️⃣ Онбординг пройден, но НЕ зарегистрирован
        else if (onboardingCompleted) {
            intent = new Intent(this, SimpleRegistrationActivity.class);
        }
        // 4️⃣ Новый пользователь → Онбординг
        else {
            intent = new Intent(this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}