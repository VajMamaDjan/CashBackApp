package com.example.cashbackapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AnalyticsActivity extends BaseActivity {

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_analytics);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_banks) {
                startActivity(new Intent(this, MainMenuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_analytics) {
                return true; // уже здесь
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}