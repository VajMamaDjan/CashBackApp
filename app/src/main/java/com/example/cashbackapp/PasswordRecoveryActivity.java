package com.example.cashbackapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordRecoveryActivity extends BaseActivity {

    private SharedPreferences prefs;
    private TextView textSecretQuestion;
    private EditText editTextSecretAnswer;
    private TextView textRecoveryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        textSecretQuestion = findViewById(R.id.textSecretQuestion);
        editTextSecretAnswer = findViewById(R.id.editTextSecretAnswer);
        textRecoveryResult = findViewById(R.id.textRecoveryResult);
        Button btnRecover = findViewById(R.id.btnRecover);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        String secretQuestion = prefs.getString("secret_question", "");
        if (secretQuestion.isEmpty()) {
            textSecretQuestion.setText("Секретный вопрос не найден");
            btnRecover.setEnabled(false);
        } else {
            textSecretQuestion.setText(secretQuestion);
        }

        btnRecover.setOnClickListener(v -> recoverPassword());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void recoverPassword() {
        String userAnswer = editTextSecretAnswer.getText().toString().trim().toLowerCase();
        String savedAnswer = prefs.getString("secret_answer", "");
        String userName = prefs.getString("user_name", "");
        String userPassword = prefs.getString("user_password", "");

        if (userAnswer.isEmpty()) {
            editTextSecretAnswer.setError("Введите ответ на вопрос");
            return;
        }

        if (userAnswer.equals(savedAnswer)) {
            String recoveryInfo = "Данные для входа:\n" +
                    "Имя: " + userName + "\n" +
                    "Пароль: " + userPassword;
            textRecoveryResult.setText(recoveryInfo);
            textRecoveryResult.setVisibility(TextView.VISIBLE);
            Toast.makeText(this, "Данные восстановлены!", Toast.LENGTH_LONG).show();
        } else {
            textRecoveryResult.setText("Неверный ответ на секретный вопрос");
            textRecoveryResult.setVisibility(TextView.VISIBLE);
            Toast.makeText(this, "Ответ неверный", Toast.LENGTH_SHORT).show();
        }
    }
}