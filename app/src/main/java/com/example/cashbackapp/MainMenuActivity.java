package com.example.cashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.cardview.widget.CardView;

public class MainMenuActivity extends BaseActivity {

    private CardView cardAddBank;
    private CardView cardBankSber;
    private CardView cardBankTBank;
    private FrameLayout profileButton;

    @Override
    protected boolean useFullscreenStatusBar() {
        // Для главного меню можно оставить false, чтобы статус-бар был обычным
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initViews();
        setupClicks();
    }

    private void initViews() {
        cardAddBank = findViewById(R.id.cardAddBank);
        cardBankSber = findViewById(R.id.cardBankSber);
        cardBankTBank = findViewById(R.id.cardBankTBank);
        profileButton = findViewById(R.id.profileButton);
    }

    private void setupClicks() {
        // Добавить банк
        cardAddBank.setOnClickListener(v -> {
            // TODO: открыть экран добавления банка
            // Например: startActivity(new Intent(this, AddBankActivity.class));
        });

        // Карточка банка "Сбер"
        cardBankSber.setOnClickListener(v -> {
            // TODO: открыть экран настроек кешбэка для Сбера
            // startActivity(new Intent(this, BankDetailsActivity.class).putExtra("bank", "sber"));
        });

        // Карточка банка "ТБанк"
        cardBankTBank.setOnClickListener(v -> {
            // TODO: открыть экран настроек кешбэка для ТБанка
            // startActivity(new Intent(this, BankDetailsActivity.class).putExtra("bank", "tbank"));
        });

        // Кнопка профиля
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
