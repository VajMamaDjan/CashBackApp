package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        TextView tvUserName = findViewById(R.id.tvUserName);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Отображаем имя пользователя
        String name = prefs.getString("user_name", "Пользователь");
        tvUserName.setText(name);

        // Logout
        btnLogout.setOnClickListener(v -> {

            // Пользователь больше не залогинен
            prefs.edit().putBoolean("is_logged_in", false).apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }
}
