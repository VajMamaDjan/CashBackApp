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
import androidx.appcompat.app.AppCompatActivity;

public class SimpleRegistrationActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText editTextName, editTextPassword, editTextConfirmPassword, editTextSecretAnswer;
    private Spinner spinnerSecretQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_registration);

        // Кнопка "Назад" в ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Регистрация");
        }

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        initializeViews();
        setupSecretQuestions();

        // 👉 Читаем флаг: зашли ли сюда из LoginActivity для "повторной регистрации"
        boolean allowReregistration = getIntent().getBooleanExtra("allow_reregistration", false);

        // Если уже зарегистрирован И НЕ в режиме повторной регистрации → сразу в главное меню
        if (isUserRegistered() && !allowReregistration) {
            startMainApp();
            return;
        }

        // Кнопка "Зарегистрироваться"
        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
                startMainApp();
            }
        });

        // 👉 Кнопка-ссылка "У меня уже есть аккаунт — Войти"
        TextView goToLogin = findViewById(R.id.textGoToLogin);
        if (goToLogin != null) {
            goToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    // Возврат на Онбординг по стрелке "Назад" в ActionBar
    private void returnToLastOnboardingScreen() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.putExtra("screen_position", 2);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        returnToLastOnboardingScreen();
        return true;
    }

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
                "Имя первого питомца?",
                "Любимый фильм?",
                "Город рождения?",
                "Кличка первого учителя?"
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

        if (password.isEmpty() || password.length() < 4) {
            editTextPassword.setError("Пароль должен быть не менее 4 символов");
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

        String normalizedSecretAnswer = secretAnswer.trim().toLowerCase();
        String passwordHash = PasswordUtils.hashString(password);
        String secretAnswerHash = PasswordUtils.hashString(normalizedSecretAnswer);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("user_registered", true);
        editor.putString("user_name", userName);
        editor.putString("user_password_hash", passwordHash);
        editor.putString("secret_question", secretQuestion);
        editor.putString("secret_answer_hash", secretAnswerHash);
        editor.putString("user_id", "user_" + System.currentTimeMillis());
        editor.putBoolean("onboarding_completed", true);
        editor.apply();

        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
    }

    private boolean isUserRegistered() {
        return prefs.getBoolean("user_registered", false);
    }

    private void startMainApp() {
        startActivity(new Intent(this, MainMenuActivity.class));
        finish();
    }
}
