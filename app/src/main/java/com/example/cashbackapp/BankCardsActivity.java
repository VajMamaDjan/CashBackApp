package com.example.cashbackapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class BankCardsActivity extends BaseActivity {

    private ImageView ivBankLogoHeader;
    private TextView tvBankNameHeader;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_cards);

        ivBankLogoHeader = findViewById(R.id.ivBankLogoHeader);
        tvBankNameHeader = findViewById(R.id.tvBankNameHeader);

        String bankName = getIntent().getStringExtra("bank_name");
        if (bankName == null) bankName = "Банк";

        tvBankNameHeader.setText(bankName);

        // подбираем логотип по имени (как ты делаешь в MainMenuActivity)
        if (bankName.contains("Сбер")) {
            ivBankLogoHeader.setImageResource(R.drawable.sber_logo);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivBankLogoHeader.setImageResource(R.drawable.ic_tbank);
        } else {
            ivBankLogoHeader.setImageResource(R.drawable.ic_bank_placeholder);
        }

        // кнопка "назад"
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }
}
