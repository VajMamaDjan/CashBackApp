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

    // ---------- ШАПКА ----------
    private ImageView ivBankLogoHeader;
    private TextView tvBankNameHeader;
    private TextView tvTotalCards;
    private TextView tvActiveCards;

    // ---------- СПИСОК КАРТ ----------
    private View cardEmptyState;
    private LinearLayout cardsContainer;
    private TextView tvDeleteCardTop;
    private TextView btnAddCardEmpty;
    private TextView btnAddCardBottom;

    private String bankName;
    private static final int REQ_ADD_CARD = 1001;

    private SharedPreferences prefs;

    // ---------- МОДЕЛЬ КАРТЫ ----------
    private static class CardData {
        String name;
        String last4;
        String ps;
        int color;
        String cashbackUnit; // "RUB" | "MILES"

        CardData(String name, String last4, String ps, int color, String cashbackUnit) {
            this.name = name;
            this.last4 = last4;
            this.ps = ps;
            this.color = color;
            this.cashbackUnit = cashbackUnit;
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

        // ---------- СПИСОК ----------
        cardEmptyState = findViewById(R.id.cardEmptyState);
        cardsContainer = findViewById(R.id.cardsContainer);
        tvDeleteCardTop = findViewById(R.id.tvDeleteCardTop);
        btnAddCardEmpty = findViewById(R.id.btnAddCardEmpty);
        btnAddCardBottom = findViewById(R.id.btnAddCardBottom);

        View.OnClickListener addCardClickListener = v -> {
            Intent i = new Intent(this, AddCardActivity.class);
            i.putExtra("bank_name", bankName);
            startActivityForResult(i, REQ_ADD_CARD);
        };

        btnAddCardEmpty.setOnClickListener(addCardClickListener);
        btnAddCardBottom.setOnClickListener(addCardClickListener);

        tvDeleteCardTop.setOnClickListener(v -> {
            if (cards.isEmpty()) return;

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Удалить все карты?")
                    .setMessage("Вы действительно хотите удалить все карты этого банка?")
                    .setPositiveButton("Удалить", (d, w) -> {
                        cards.clear();
                        saveCardsToPrefs();
                        cardsContainer.removeAllViews();
                        cardEmptyState.setVisibility(View.VISIBLE);
                        cardsContainer.setVisibility(View.GONE);
                        btnAddCardBottom.setVisibility(View.GONE);
                        updateStats();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        loadCardsFromPrefs();
        updateStats();
    }

    // ---------- RESULT ----------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_CARD && resultCode == RESULT_OK && data != null) {
            String cardName = data.getStringExtra("card_name");
            String last4 = data.getStringExtra("card_last4");
            String ps = data.getStringExtra("card_ps");
            int color = data.getIntExtra("card_color", Color.parseColor("#8A3CFF"));

            String cashbackUnit = data.getStringExtra("card_cashback_unit");
            if (cashbackUnit == null) cashbackUnit = "RUB";

            addCardFromForm(cardName, last4, ps, color, cashbackUnit);
        }
    }

    private void addCardFromForm(String cardName, String last4, String psCode, int color, String cashbackUnit)
    {

        if (TextUtils.isEmpty(cardName)) cardName = "Моя карта";
        if (last4 == null) last4 = "0000";
        if (cashbackUnit == null) cashbackUnit = "RUB";

        CardData card = new CardData(cardName, last4, psCode, color, cashbackUnit);
        cards.add(card);

        ensureListVisible();
        addCardView(card);
        updateStats();
        saveCardsToPrefs();
    }

    // ---------- UI ----------
    private void ensureListVisible() {
        cardEmptyState.setVisibility(View.GONE);
        cardsContainer.setVisibility(View.VISIBLE);
        btnAddCardBottom.setVisibility(View.VISIBLE);
    }

    private void addCardView(CardData card) {
        View itemView = getLayoutInflater().inflate(R.layout.item_bank_card, cardsContainer, false);

        View cardRoot = itemView.findViewById(R.id.cardRoot);
        TextView tvCashbackUnit = itemView.findViewById(R.id.tvCashbackUnit);
        TextView tvCardName = itemView.findViewById(R.id.tvCardName);
        TextView tvCardLast4 = itemView.findViewById(R.id.tvCardLast4);
        TextView tvCardSystem = itemView.findViewById(R.id.tvCardSystem);

        tvCardName.setText(card.name);
        tvCardLast4.setText(card.last4);
        tvCardSystem.setText(mapPsLabel(card.ps));

        if ("MILES".equalsIgnoreCase(card.cashbackUnit)) {
            // самолёт
            tvCashbackUnit.setText("МИЛИ");
            tvCashbackUnit.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_plane, 0, 0, 0
            );
        } else {
            // рубли
            tvCashbackUnit.setCompoundDrawables(null, null, null, null);
            tvCashbackUnit.setText("РУБ");
        }

        applyCardGradient(cardRoot, card.color);

        cardRoot.setOnClickListener(v -> {
            Intent i = new Intent(this, CashbackCategoriesActivity.class);
            i.putExtra("bank_name", bankName);
            i.putExtra("card_name", card.name);
            i.putExtra("card_last4", card.last4);
            i.putExtra("card_ps", card.ps);
            i.putExtra("card_color", card.color);
            i.putExtra("card_cashback_unit", card.cashbackUnit);
            startActivity(i);
        });

        cardsContainer.addView(itemView);
    }

    private String mapCashbackUnit(String unit) {
        if (unit == null) return "RUB";
        if ("MILES".equalsIgnoreCase(unit)) return "R.drawable.ic_tbank";
        return "рублями";
    }

    private String mapPsLabel(String psCode) {
        if (psCode == null) return "";
        psCode = psCode.toUpperCase();
        switch (psCode) {
            case "MIR": return "МИР";
            case "VISA": return "VISA";
            case "MC":
            case "MASTERCARD": return "Mastercard";
            default: return "";
        }
    }

    private void applyCardGradient(View view, int baseColor) {
        int darker = darken(baseColor, 0.75f);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{darker, baseColor});
        gd.setCornerRadius(dp(18));
        view.setBackground(gd);
    }

    private int darken(int color, float factor) {
        return Color.rgb(
                (int) (Color.red(color) * factor),
                (int) (Color.green(color) * factor),
                (int) (Color.blue(color) * factor));
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private void updateStats() {
        int count = cards.size();
        tvTotalCards.setText(String.valueOf(count));
        tvActiveCards.setText(String.valueOf(count));
    }

    // ---------- PREFS ----------
    private String getPrefsKeyForBank() {
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
                obj.put("cashbackUnit", c.cashbackUnit);
                arr.put(obj);
            }
            prefs.edit().putString(getPrefsKeyForBank(), arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadCardsFromPrefs() {
        // 1) сброс UI и списка
        cards.clear();
        cardsContainer.removeAllViews();

        String json = prefs.getString(getPrefsKeyForBank(), null);

        // 2) если нет данных — показываем empty-state
        if (json == null || json.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            cardsContainer.setVisibility(View.GONE);
            btnAddCardBottom.setVisibility(View.GONE);
            return;
        }

        // 3) парсим
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String name = obj.optString("name", "Моя карта");
                String last4 = obj.optString("last4", "0000");
                String ps = obj.optString("ps", "");
                int color = obj.optInt("color", Color.parseColor("#8A3CFF"));
                String cashbackUnit = obj.optString("cashbackUnit", "RUB");

                cards.add(new CardData(name, last4, ps, color, cashbackUnit));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 4) ВАЖНО: показываем правильное состояние
        if (cards.isEmpty()) {
            cardEmptyState.setVisibility(View.VISIBLE);
            cardsContainer.setVisibility(View.GONE);
            btnAddCardBottom.setVisibility(View.GONE);
        } else {
            ensureListVisible();
            for (CardData c : cards) addCardView(c);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadCardsFromPrefs();
        updateStats();
    }
}