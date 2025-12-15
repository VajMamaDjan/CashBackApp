package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BankCardsActivity extends BaseActivity {

    // Шапка
    private ImageView ivBankLogoHeader;
    private TextView tvBankNameHeader;
    private TextView tvTotalCards;
    private TextView tvActiveCards;

    // "Мои карты"
    private View cardEmptyState;
    private LinearLayout cardsContainer;
    private TextView tvDeleteCardTop;    // "Очистить все карты"
    private TextView btnAddCardEmpty;    // кнопка внутри empty-state
    private TextView btnAddCardBottom;   // нижняя кнопка "Добавить карту"

    private String bankName;
    private static final int REQ_ADD_CARD = 1001;

    private SharedPreferences prefs;

    // Модель карты
    private static class CardData {
        String name;
        String last4;
        String ps;     // MIR / VISA / MC
        int color;     // базовый цвет (без затемнения)

        CardData(String name, String last4, String ps, int color) {
            this.name = name;
            this.last4 = last4;
            this.ps = ps;
            this.color = color;
        }
    }

    private final List<CardData> cards = new ArrayList<>();

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_cards);

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // ---------- ШАПКА ----------
        ivBankLogoHeader = findViewById(R.id.ivBankLogoHeader);
        tvBankNameHeader = findViewById(R.id.tvBankNameHeader);
        tvTotalCards = findViewById(R.id.tvTotalCards);
        tvActiveCards = findViewById(R.id.tvActiveCards);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        bankName = intent != null ? intent.getStringExtra("bank_name") : null;
        if (bankName == null) bankName = "Банк";
        tvBankNameHeader.setText(bankName);

        if (bankName.contains("Сбер")) {
            ivBankLogoHeader.setImageResource(R.drawable.sber_logo);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivBankLogoHeader.setImageResource(R.drawable.ic_tbank);
        } else {
            ivBankLogoHeader.setImageResource(R.drawable.ic_bank_placeholder);
        }

        // ---------- "МОИ КАРТЫ" ----------
        cardEmptyState = findViewById(R.id.cardEmptyState);
        cardsContainer = findViewById(R.id.cardsContainer);
        tvDeleteCardTop = findViewById(R.id.tvDeleteCardTop);
        btnAddCardEmpty = findViewById(R.id.btnAddCardEmpty);
        btnAddCardBottom = findViewById(R.id.btnAddCardBottom);

        // обработчик "Добавить карту"
        View.OnClickListener addCardClickListener = v -> {
            Intent i = new Intent(BankCardsActivity.this, AddCardActivity.class);
            i.putExtra("bank_name", bankName);
            startActivityForResult(i, REQ_ADD_CARD);
        };

        // кнопки "Добавить карту"
        btnAddCardEmpty.setOnClickListener(addCardClickListener);
        btnAddCardBottom.setOnClickListener(addCardClickListener);

        // "Очистить все карты"
        tvDeleteCardTop.setOnClickListener(v -> {
            if (cards.isEmpty()) return;

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Удалить все карты?")
                    .setMessage("Вы действительно хотите удалить все карты этого банка? После удаления невозможно будет восстановить.")
                    .setPositiveButton("Удалить", (dialog, which) -> {

                        cards.clear();
                        saveCardsToPrefs();
                        cardsContainer.removeAllViews();

                        cardEmptyState.setVisibility(View.VISIBLE);
                        cardsContainer.setVisibility(View.GONE);
                        btnAddCardBottom.setVisibility(View.GONE);

                        updateStats();
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Загружаем сохранённые карты
        loadCardsFromPrefs();
        updateStats();
    }

    // ---------- РЕЗУЛЬТАТ С AddCardActivity ----------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_CARD && resultCode == RESULT_OK && data != null) {
            String cardName = data.getStringExtra("card_name");
            String last4 = data.getStringExtra("card_last4");
            String ps = data.getStringExtra("card_ps");
            int color = data.getIntExtra("card_color", Color.parseColor("#8A3CFF"));

            addCardFromForm(cardName, last4, ps, color);
        }
    }

    private void addCardFromForm(String cardName, String last4, String psCode, int color) {
        if (cardName == null || cardName.trim().isEmpty()) {
            cardName = "Моя карта";
        }
        if (last4 == null) last4 = "0000";

        CardData card = new CardData(cardName, last4, psCode, color);
        cards.add(card);

        ensureListVisible();
        addCardView(card);

        updateStats();
        saveCardsToPrefs();
    }

    // ---------- ОТОБРАЖЕНИЕ / СОСТОЯНИЯ ----------

    private void ensureListVisible() {
        if (cardsContainer.getVisibility() != View.VISIBLE) {
            cardEmptyState.setVisibility(View.GONE);
            cardsContainer.setVisibility(View.VISIBLE);
            btnAddCardBottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Создаём view для одной карты и добавляем в контейнер
     */
    private void addCardView(CardData card) {
        View itemView = getLayoutInflater().inflate(R.layout.item_bank_card, cardsContainer, false);

        View cardRoot = itemView.findViewById(R.id.cardRoot);
        TextView tvCardName = itemView.findViewById(R.id.tvCardName);
        TextView tvCardLast4 = itemView.findViewById(R.id.tvCardLast4);
        TextView tvCardSystem = itemView.findViewById(R.id.tvCardSystem);

        // Название
        tvCardName.setText(card.name);

        // Последние цифры
        tvCardLast4.setText(card.last4);

        // Платёжная система
        tvCardSystem.setText(mapPsLabel(card.ps));

        // Градиентный фон как в AddCardActivity
        applyCardGradient(cardRoot, card.color);

        cardRoot.setOnClickListener(v -> {
            Intent i = new Intent(BankCardsActivity.this, CashbackCategoriesActivity.class);
            i.putExtra("bank_name", bankName);
            i.putExtra("card_name", card.name);
            i.putExtra("card_last4", card.last4);
            i.putExtra("card_ps", card.ps);
            i.putExtra("card_color", card.color); // важно для такого же градиента
            startActivity(i);
        });

        cardsContainer.addView(itemView);
    }

    /**
     * Преобразуем код платёжной системы в надпись на карте
     */
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

    /**
     * Устанавливаем градиентный фон на карту
     */
    private void applyCardGradient(View view, int baseColor) {
        int darker = darken(baseColor, 0.75f);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{darker, baseColor});
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

    private void updateStats() {
        int count = cards.size();
        tvTotalCards.setText(String.valueOf(count));
        tvActiveCards.setText(String.valueOf(count)); // пока все считаем активными
    }

    // ---------- PREFERENCES: СОХРАНЕНИЕ / ЗАГРУЗКА ----------

    private String getPrefsKeyForBank() {
        // отдельный ключ для каждого банка
        return "cards_for_" + bankName;
    }

    private void saveCardsToPrefs() {
        JSONArray arr = new JSONArray();
        try {
            for (CardData c : cards) {
                JSONObject obj = new JSONObject();
                obj.put("name", c.name);
                obj.put("last4", c.last4);
                obj.put("ps", c.ps);
                obj.put("color", c.color);
                arr.put(obj);
            }
            prefs.edit()
                    .putString(getPrefsKeyForBank(), arr.toString())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadCardsFromPrefs() {
        String json = prefs.getString(getPrefsKeyForBank(), null);
        if (json == null || json.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            cardsContainer.setVisibility(View.GONE);
            btnAddCardBottom.setVisibility(View.GONE);
            return;
        }

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.optString("name", "Моя карта");
                String last4 = obj.optString("last4", "0000");
                String ps = obj.optString("ps", "");
                int color = obj.optInt("color", Color.parseColor("#8A3CFF"));

                CardData card = new CardData(name, last4, ps, color);
                cards.add(card);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!cards.isEmpty()) {
            ensureListVisible();
            for (CardData c : cards) {
                addCardView(c);
            }
        } else {
            cardEmptyState.setVisibility(View.VISIBLE);
            cardsContainer.setVisibility(View.GONE);
            btnAddCardBottom.setVisibility(View.GONE);
        }
    }
}