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

    // NEW
    private int maxCategories;          // лимит категорий для этой карты
    private String cashbackUnit;        // "RUB" или "MILES"

    // Заглушка: пока выбранных категорий нет
    private int selectedCount = 0;

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

        cashbackUnit = i.getStringExtra("card_cashback_unit");
        if (cashbackUnit == null) cashbackUnit = "RUB";
        if (maxCategories <= 0) maxCategories = 5;

        // ---------- шапка ----------
        View topContainer = findViewById(R.id.topContainer);
        applyHeaderGradient(topContainer, cardColor);

        // Если захочешь доп. строку, можно будет вывести bankName/ps/unit, но XML ты фиксировал как эталонный.
        // Поэтому здесь ничего лишнего не трогаем.

        // ---------- карточка в хедере (твоя структура headerCardRoot) ----------
        View headerCardRoot = findViewById(R.id.headerCardRoot);
        TextView tvHeaderCardName = findViewById(R.id.tvHeaderCardName);
        TextView tvHeaderCardLast4 = findViewById(R.id.tvHeaderCardLast4);
        TextView tvHeaderCashbackUnit = findViewById(R.id.tvHeaderCashbackUnit);
        TextView tvHeaderSelected = findViewById(R.id.tvHeaderSelected);

        tvHeaderCardName.setText(cardName != null ? cardName : "Моя карта");
        tvHeaderCardLast4.setText("•••• " + (last4 != null ? last4 : "0000"));

        if ("MILES".equalsIgnoreCase(cashbackUnit)) {
            tvHeaderCashbackUnit.setText("МИЛИ");
            tvHeaderCashbackUnit.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_plane, 0, 0, 0
            );
            tvHeaderCashbackUnit.setCompoundDrawablePadding((int) dp(4));
        } else {
            tvHeaderCashbackUnit.setCompoundDrawables(null, null, null, null);
            tvHeaderCashbackUnit.setText("РУБ");
        }

        applyCardGradient(headerCardRoot, cardColor);

        // ---------- блок "Добавить категорию" ----------
        TextView tvPlacesLeft = findViewById(R.id.tvPlacesLeft);

        // Можно красиво подсказать единицы возврата (рубли/мили) — не меняя разметку:
        // добавим к подписи снизу (если хочешь — уберу)
        // tvPlacesLeft.setText("Осталось выбрать " + (maxCategories - selectedCount) + " категорий • " + unitLabel());

        updateCounters(tvHeaderSelected, tvPlacesLeft);

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            // Заглушка: имитируем добавление категории, чтобы проверить лимит/тексты
            if (selectedCount >= maxCategories) return;
            selectedCount++;
            updateCounters(tvHeaderSelected, tvPlacesLeft);
        });
    }

    private void updateCounters(TextView tvHeaderSelected, TextView tvPlacesLeft) {
        tvHeaderSelected.setText("Выбрано " + selectedCount + " из " + maxCategories + " категорий");
        tvPlacesLeft.setText("Осталось выбрать " + (maxCategories - selectedCount) + " категорий");
        // Если хочешь всегда показывать единицу возврата:
        // tvPlacesLeft.setText("Осталось выбрать " + (maxCategories - selectedCount) + " категорий • " + unitLabel());
    }

    private String unitLabel() {
        return "MILES".equalsIgnoreCase(cashbackUnit) ? "милями" : "рублями";
    }

    // ======================================================
    // Gradient helpers (тот же стиль, что и в твоих экранах)
    // ======================================================

    /** Фон шапки = тот же стиль, что у карты, но темнее */
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

    /** Градиент карты как в BankCardsActivity */
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
