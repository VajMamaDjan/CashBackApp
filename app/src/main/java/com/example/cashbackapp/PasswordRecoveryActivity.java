package com.example.cashbackapp;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordRecoveryActivity extends BaseActivity {

    private SharedPreferences prefs;
    private TextView textSecretQuestion;
    private EditText editTextSecretAnswer;
    private TextView textRecoveryResult;

    // Блок для нового пароля
    private View newPasswordContainer;
    private EditText editTextNewPassword;
    private EditText editTextConfirmNewPassword;
    private Button btnSaveNewPassword;

    @Override
    protected boolean useFullscreenStatusBar() {
        // Для экрана восстановления можно не делать fullscreen, чтобы с клавиатурой было комфортнее
        return false;
    }

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

        // Новый блок
        newPasswordContainer = findViewById(R.id.newPasswordContainer);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmNewPassword = findViewById(R.id.editTextConfirmNewPassword);
        btnSaveNewPassword = findViewById(R.id.btnSaveNewPassword);

        String secretQuestion = prefs.getString("secret_question", "");
        if (secretQuestion.isEmpty()) {
            textSecretQuestion.setText("Секретный вопрос не найден");
            btnRecover.setEnabled(false);
        } else {
            textSecretQuestion.setText(secretQuestion);
        }

        btnRecover.setOnClickListener(v -> recoverPassword());
        btnBackToLogin.setOnClickListener(v -> finish());

        btnSaveNewPassword.setOnClickListener(v -> saveNewPassword());
    }

    private void recoverPassword() {
        String userAnswerRaw = editTextSecretAnswer.getText().toString().trim();

        if (userAnswerRaw.isEmpty()) {
            editTextSecretAnswer.setError("Введите ответ на вопрос");
            return;
        }

        // нормализуем и хешируем ответ
        String normalizedAnswer = userAnswerRaw.toLowerCase();
        String userAnswerHash = PasswordUtils.hashString(normalizedAnswer);

        // читаем сохранённый хеш
        String savedAnswerHash = prefs.getString("secret_answer_hash", null);

        if (savedAnswerHash == null) {
            textRecoveryResult.setText("Секретный ответ не задан. Повторите регистрацию.");
            textRecoveryResult.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Ошибка восстановления", Toast.LENGTH_SHORT).show();
            return;
        }

        if (savedAnswerHash.equals(userAnswerHash)) {
            // Ответ верный — даём возможность задать новый пароль
            textRecoveryResult.setText("Ответ верный. Задайте новый пароль ниже.");
            textRecoveryResult.setVisibility(View.VISIBLE);
            newPasswordContainer.setVisibility(View.VISIBLE);
        } else {
            textRecoveryResult.setText("Неверный ответ на секретный вопрос");
            textRecoveryResult.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Ответ неверный", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNewPassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmNewPassword.getText().toString().trim();

        if (newPassword.isEmpty() || newPassword.length() < 4) {
            editTextNewPassword.setError("Пароль должен быть не менее 4 символов");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmNewPassword.setError("Пароли не совпадают");
            return;
        }

        String newPasswordHash = PasswordUtils.hashString(newPassword);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_password_hash", newPasswordHash);
        editor.apply();

        Toast.makeText(this, "Пароль успешно обновлён", Toast.LENGTH_SHORT).show();
        finish(); // возвращаемся назад (например, на экран логина)
    }
}
