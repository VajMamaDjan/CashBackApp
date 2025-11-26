package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

    private SharedPreferences prefs;
    private EditText editTextName, editTextPassword;
    private Button btnLogin;
    private TextView btnToRecovery;
    private TextView textGoToRegistration;

    @Override
    protected boolean useFullscreenStatusBar() {
        // Для логина удобнее без fullscreen, чтобы клавиатура вела себя нормально
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRecovery = findViewById(R.id.btnToRecovery);
        textGoToRegistration = findViewById(R.id.textGoToRegistration);

        btnLogin.setOnClickListener(v -> handleLogin());

        btnToRecovery.setOnClickListener(v ->
                startActivity(new Intent(this, PasswordRecoveryActivity.class)));

        textGoToRegistration.setOnClickListener(v -> {

            boolean isRegistered = prefs.getBoolean("user_registered", false);

            // Если уже есть регистрация → показываем предупреждение
            if (isRegistered) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Перезаписать аккаунт?")
                        .setMessage("Уже существует зарегистрированная учётная запись. " +
                                "Новая регистрация полностью перезапишет старые данные. " +
                                "Вы уверены, что хотите продолжить?")
                        .setPositiveButton("Продолжить", (dialog, which) -> {
                            Intent intent = new Intent(this, SimpleRegistrationActivity.class);
                            intent.putExtra("allow_reregistration", true);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            else {
                // Если регистрации нет — сразу открываем
                Intent intent = new Intent(this, SimpleRegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(v -> handleLogin());

        btnToRecovery.setOnClickListener(v ->
                startActivity(new Intent(this, PasswordRecoveryActivity.class)));
    }


    private void handleLogin() {
        String enteredName = editTextName.getText().toString().trim();
        String enteredPassword = editTextPassword.getText().toString().trim();

        if (enteredName.isEmpty()) {
            editTextName.setError("Введите имя");
            return;
        }

        if (enteredPassword.isEmpty()) {
            editTextPassword.setError("Введите пароль");
            return;
        }

        String savedName = prefs.getString("user_name", null);
        String savedPasswordHash = prefs.getString("user_password_hash", null);

        if (savedName == null || savedPasswordHash == null) {
            Toast.makeText(this, "Пользователь не зарегистрирован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Хешируем введённый пароль
        String enteredPasswordHash = PasswordUtils.hashString(enteredPassword);

        if (enteredName.equals(savedName) && savedPasswordHash.equals(enteredPasswordHash)) {
            // успех — логин прошёл
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Неверное имя или пароль", Toast.LENGTH_SHORT).show();
        }

        if (enteredName.equals(savedName) && savedPasswordHash.equals(enteredPasswordHash)) {

            // Успешный вход → ставим флаг
            prefs.edit().putBoolean("is_logged_in", true).apply();

            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        }

    }
}