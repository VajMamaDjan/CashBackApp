package com.example.cashbackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.util.HashSet;
import java.util.Set;

public class MainMenuActivity extends BaseActivity {

    private CardView cardAddBank;
    private FrameLayout profileButton;
    private LinearLayout banksContainer;
    private SharedPreferences prefs;
    private String[] allBanks;

    @Override
    protected boolean useFullscreenStatusBar() {
        // Для главного меню можно оставить false, чтобы статус-бар был обычным
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        allBanks = getResources().getStringArray(R.array.bank_names);

        initViews();
        setupClicks();

        // при старте загрузим уже выбранные банки
        loadSavedBanks();
    }

    // ---------- ИНИЦИАЛИЗАЦИЯ ВЬЮ ----------

    private void initViews() {
        cardAddBank = findViewById(R.id.cardAddBank);
        profileButton = findViewById(R.id.profileButton);
        banksContainer = findViewById(R.id.banksContainer);
    }

    private void setupClicks() {
        // Добавить банк
        cardAddBank.setOnClickListener(v -> showBankPicker());

        // Кнопка профиля
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    // ---------- РАБОТА С SharedPreferences ----------

    private void saveBank(String bankName) {
        Set<String> saved = new HashSet<>(
                prefs.getStringSet("selected_banks", new HashSet<>())
        );
        saved.add(bankName);
        prefs.edit().putStringSet("selected_banks", saved).apply();
    }

    private void removeBank(String bankName) {
        Set<String> saved = new HashSet<>(
                prefs.getStringSet("selected_banks", new HashSet<>())
        );
        if (saved.remove(bankName)) {
            prefs.edit().putStringSet("selected_banks", saved).apply();
        }
    }

    private void loadSavedBanks() {
        Set<String> saved = prefs.getStringSet("selected_banks", new HashSet<>());
        for (String bank : saved) {
            addBankCard(bank);
        }
    }

    // ---------- ДИАЛОГ ВЫБОРА БАНКА ----------

    private void showBankPicker() {
        new AlertDialog.Builder(this)
                .setTitle("Выберите банк")
                .setItems(allBanks, (dialog, which) -> {
                    String selected = allBanks[which];

                    // Проверка на дубликат
                    if (isBankAlreadyAdded(selected)) {
                        Toast.makeText(this, "Банк уже добавлен", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addBankCard(selected);
                    saveBank(selected);
                })
                .show();
    }

    private boolean isBankAlreadyAdded(String bankName) {
        Set<String> saved = prefs.getStringSet("selected_banks", new HashSet<>());
        return saved.contains(bankName);
    }

    // ---------- ДОБАВЛЕНИЕ КАРТОЧКИ БАНКА ----------

    private void addBankCard(String bankName) {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_bank, banksContainer, false);

        TextView tvName = itemView.findViewById(R.id.tvBankName);
        ImageView ivLogo = itemView.findViewById(R.id.ivBankLogo);

        tvName.setText(bankName);

        // логотипы по условию
        if (bankName.contains("Сбер")) {
            ivLogo.setImageResource(R.drawable.ic_sber);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivLogo.setImageResource(R.drawable.ic_tbank);
        } else {
            ivLogo.setImageResource(R.drawable.ic_bank_placeholder);
        }

        // Удаление по долгому нажатию
        itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить банк?")
                    .setMessage("Удалить «" + bankName + "» из списка?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        // убираем из SharedPreferences
                        removeBank(bankName);
                        // убираем карточку с экрана
                        banksContainer.removeView(itemView);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true; // событие обработано
        });

        banksContainer.addView(itemView);
    }
}