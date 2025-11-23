package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SimpleRegistrationActivity extends BaseActivity {

    private SharedPreferences prefs;
    private EditText editTextName, editTextPassword, editTextConfirmPassword, editTextSecretAnswer;
    private Spinner spinnerSecretQuestion;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;   // здесь fullscreen не нужен
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_registration);

        // Включение кнопки "Назад" в ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Регистрация");
        }

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        initializeViews();
        setupSecretQuestions();

        // Проверяем, не зарегистрирован ли уже пользователь
        if (isUserRegistered()) {
            startMainApp();
            return;
        }

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
                startMainApp();
            }
        });

        // Обработчик для кнопки "Забыли данные для входа?"
        TextView forgotPassword = findViewById(R.id.textForgotPassword);
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, PasswordRecoveryActivity.class));
        });
    }

    // Обработка нажатия кнопки "Назад" в ActionBar
    private void returnToLastOnboardingScreen() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        // Указываем конкретную позицию экрана (2 = третий экран)
        intent.putExtra("screen_position", 2);
        startActivity(intent);
        finish();
    }

    // Обработка нажатия кнопки "Назад" в ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        returnToLastOnboardingScreen();
        return true;
    }

    // Обработка системной кнопки "Назад"
    @Override
    public void onBackPressed() {
        returnToLastOnboardingScreen();
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextSecretAnswer = findViewById(R.id.editTextSecretAnswer);
        spinnerSecretQuestion = findViewById(R.id.spinnerSecretQuestion);
    }

    private void setupSecretQuestions() {
        String[] questions = {
                "Девичья фамилия матери?",
                "Имя первого учителя?",
                "Любимый фильм?",
                "Город рождения?",
                "Кличка первого питомца?"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, questions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecretQuestion.setAdapter(adapter);
    }

    private boolean validateInput() {
        String userName = editTextName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String secretAnswer = editTextSecretAnswer.getText().toString().trim();

        if (userName.isEmpty()) {
            editTextName.setError("Введите ваше имя");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            editTextPassword.setError("Пароль должен быть не менее 6 символов");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Пароли не совпадают");
            return false;
        }

        if (secretAnswer.isEmpty()) {
            editTextSecretAnswer.setError("Ответьте на секретный вопрос");
            return false;
        }

        return true;
    }

    private void registerUser() {
        String userName = editTextName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String secretQuestion = spinnerSecretQuestion.getSelectedItem().toString();
        String secretAnswer = editTextSecretAnswer.getText().toString().trim();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("user_registered", true);
        editor.putString("user_name", userName);
        editor.putString("user_password", password);
        editor.putString("secret_question", secretQuestion);
        editor.putString("secret_answer", secretAnswer.toLowerCase());
        editor.putString("user_id", "user_" + System.currentTimeMillis());
        editor.putBoolean("onboarding_completed", true);
        editor.apply();

        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
    }

    private boolean isUserRegistered() {
        return prefs.getBoolean("user_registered", false);
    }

    private void startMainApp() {
        // ⬇️ Единственное важное изменение: теперь идём на главное меню
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
