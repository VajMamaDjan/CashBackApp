package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ProfileActivity extends BaseActivity {

    private static final int REQUEST_IMPORT_BACKUP = 1001;

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
        Button btnExport = findViewById(R.id.btnExport);
        Button btnImport = findViewById(R.id.btnImport);

        // Отображаем имя пользователя
        String name = prefs.getString("user_name", "Пользователь");
        tvUserName.setText(name);

        // Logout
        btnLogout.setOnClickListener(v -> {
            prefs.edit().putBoolean("is_logged_in", false).apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Экспорт данных
        btnExport.setOnClickListener(v -> exportData());

        // Импорт данных
        btnImport.setOnClickListener(v -> importData());
    }

    // ---------------- ЭКСПОРТ ДАННЫХ ----------------

    private void exportData() {
        try {
            // 1. Собираем JSON из SharedPreferences
            Set<String> selected = prefs.getStringSet("selected_banks", new HashSet<>());
            JSONObject root = new JSONObject();
            JSONArray banks = new JSONArray();
            for (String b : selected) {
                banks.put(b);
            }
            root.put("selected_banks", banks);
            root.put("version", 1); // запас на будущее

            // 2. Пишем во временный файл в cacheDir
            File backupFile = new File(getCacheDir(), "cashback_backup.json");
            try (FileOutputStream fos = new FileOutputStream(backupFile)) {
                fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            }

            // 3. Получаем Uri через FileProvider
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.example.cashbackapp.fileprovider",
                    backupFile
            );

            // 4. Открываем системное "Поделиться"
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/json");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(sendIntent, "Отправить резервную копию"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------- ИМПОРТ ДАННЫХ ----------------

    private void importData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMPORT_BACKUP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                restoreFromBackup(uri);
            }
        }
    }

    private void restoreFromBackup(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) {
                Toast.makeText(this, "Не удалось открыть файл", Toast.LENGTH_SHORT).show();
                return;
            }

            // читаем текст файла
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject root = new JSONObject(sb.toString());

            // читаем список банков
            JSONArray banks = root.optJSONArray("selected_banks");
            Set<String> restored = new HashSet<>();
            if (banks != null) {
                for (int i = 0; i < banks.length(); i++) {
                    restored.add(banks.getString(i));
                }
            }

            // сохраняем в SharedPreferences
            prefs.edit().putStringSet("selected_banks", restored).apply();

            Toast.makeText(this, "Данные успешно импортированы", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка импорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
