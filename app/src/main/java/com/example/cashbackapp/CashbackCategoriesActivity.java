package com.example.cashbackapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class CashbackCategoriesActivity extends BaseActivity {

    private String bankName;
    private String cardName;
    private String last4;
    private String ps;
    private int cardColor;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashback_categories);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // ---------- данные из предыдущего экрана ----------
        Intent i = getIntent();
        bankName = i.getStringExtra("bank_name");
        cardName = i.getStringExtra("card_name");
        last4 = i.getStringExtra("card_last4");
        ps = i.getStringExtra("card_ps");
        cardColor = i.getIntExtra("card_color", Color.parseColor("#8A3CFF"));

        // ---------- шапка ----------
        View topContainer = findViewById(R.id.topContainer);
        applyHeaderGradient(topContainer, cardColor);

        View headerCardRoot = findViewById(R.id.headerCardRoot);
        TextView tvHeaderCardName = findViewById(R.id.tvHeaderCardName);
        TextView tvHeaderCardLast4 = findViewById(R.id.tvHeaderCardLast4);
        TextView tvHeaderSelected = findViewById(R.id.tvHeaderSelected);

        tvHeaderCardName.setText(cardName);
        tvHeaderCardLast4.setText("•••• " + last4);
        tvHeaderSelected.setText("Выбрано 0 из 5 категорий");

// тот же градиент, что у карты (стиль сохраняем)
        applyCardGradient(headerCardRoot, cardColor);


        // ---------- счётчик категорий (пока заглушка) ----------
        tvHeaderSelected.setText("Выбрано 0 из 5 категорий");
    }

    // ======================================================
    // helpers
    // ======================================================

    private String mapPsLabel(String psCode) {
        if (psCode == null) return "";
        psCode = psCode.toUpperCase();

        switch (psCode) {
            case "MIR":
                return "МИР";
            case "VISA":
                return "VISA";
            case "MC":
            case "MASTERCARD":
                return "Mastercard";
            default:
                return "";
        }
    }

    /** Тот же стиль, что у карты, но темнее — для шапки */
    private void applyHeaderGradient(View view, int baseColor) {
        int c1 = darken(baseColor, 0.35f);
        int c2 = darken(baseColor, 0.55f);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{c1, c2}
        );

        float r = dp(22);
        gd.setCornerRadii(new float[]{
                0, 0,
                0, 0,
                r, r,
                r, r
        });

        view.setBackground(gd);
    }

    /** Абсолютно такой же градиент, как у карточки */
    private void applyCardGradient(View view, int baseColor) {
        int darker = darken(baseColor, 0.75f);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{darker, baseColor}
        );
        gd.setCornerRadius(dp(18));
        view.setBackground(gd);
    }

    private int darken(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
