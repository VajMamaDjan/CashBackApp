package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private View cardEmptyState;          // CardView "Нет карт"
    private LinearLayout cardsContainer;  // контейнер для добавленных карт
    private TextView tvAddCardTop;        // "+ Добавить" рядом с заголовком
    private TextView btnAddCardEmpty;     // кнопка внутри "Нет карт"
    private TextView btnAddCardBottom;    // нижняя кнопка "Добавить карту"

    private String bankName;
    private static final int REQ_ADD_CARD = 1001;

    // Хранилище
    private SharedPreferences prefs;

    // Модель карты
    private static class CardData {
        String name;
        String last4;
        String ps;

        CardData(String name, String last4, String ps) {
            this.name = name;
            this.last4 = last4;
            this.ps = ps;
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
        tvTotalCards     = findViewById(R.id.tvTotalCards);
        tvActiveCards    = findViewById(R.id.tvActiveCards);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        bankName = intent != null ? intent.getStringExtra("bank_name") : null;
        if (bankName == null) bankName = "Банк";
        tvBankNameHeader.setText(bankName);

        if (bankName.contains("Сбер")) {
            ivBankLogoHeader.setImageResource(R.drawable.ic_sber);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivBankLogoHeader.setImageResource(R.drawable.ic_tbank);
        } else {
            ivBankLogoHeader.setImageResource(R.drawable.ic_bank_placeholder);
        }

        // ---------- "МОИ КАРТЫ" ----------
        cardEmptyState   = findViewById(R.id.cardEmptyState);
        cardsContainer   = findViewById(R.id.cardsContainer);
        tvAddCardTop     = findViewById(R.id.tvAddCardTop);
        btnAddCardEmpty  = findViewById(R.id.btnAddCardEmpty);
        btnAddCardBottom = findViewById(R.id.btnAddCardBottom);

        View.OnClickListener addCardClickListener = v -> {
            Intent i = new Intent(BankCardsActivity.this, AddCardActivity.class);
            i.putExtra("bank_name", bankName);
            startActivityForResult(i, REQ_ADD_CARD);
        };

        tvAddCardTop.setOnClickListener(addCardClickListener);
        btnAddCardEmpty.setOnClickListener(addCardClickListener);
        btnAddCardBottom.setOnClickListener(addCardClickListener);

        // Загружаем сохранённые карты
        loadCardsFromPrefs();

        // Обновляем статистику (на случай, если карт нет)
        updateStats();
    }

    // ---------- ПОЛУЧЕНИЕ КАРТЫ ИЗ ФОРМЫ ----------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ADD_CARD && resultCode == RESULT_OK && data != null) {
            String cardName = data.getStringExtra("card_name");
            String last4    = data.getStringExtra("card_last4");
            String ps       = data.getStringExtra("card_ps");
            // int color       = data.getIntExtra("card_color", 0xFF8A3CFF); // пока не используем в UI

            addCardFromForm(cardName, last4, ps);
        }
    }

    private void addCardFromForm(String cardName, String last4, String psCode) {
        if (cardName == null || cardName.trim().isEmpty()) {
            cardName = "Моя карта";
        }

        CardData card = new CardData(cardName, last4, psCode);
        cards.add(card);

        ensureListVisible();
        addCardView(card);

        updateStats();
        saveCardsToPrefs();
    }

    // ---------- ОТОБРАЖЕНИЕ КАРТОЧКИ ----------

    private void ensureListVisible() {
        if (cardsContainer.getVisibility() != View.VISIBLE) {
            cardEmptyState.setVisibility(View.GONE);
            cardsContainer.setVisibility(View.VISIBLE);
            btnAddCardBottom.setVisibility(View.VISIBLE);
        }
    }

    private void addCardView(CardData card) {
        View cardView = getLayoutInflater().inflate(R.layout.item_bank_card, cardsContainer, false);

        TextView tvCardTitle    = cardView.findViewById(R.id.tvCardTitle);
        TextView tvCardSubtitle = cardView.findViewById(R.id.tvCardSubtitle);

        String psLabel;
        if ("MIR".equalsIgnoreCase(card.ps)) {
            psLabel = "МИР";
        } else if ("VISA".equalsIgnoreCase(card.ps)) {
            psLabel = "VISA";
        } else if ("MC".equalsIgnoreCase(card.ps)) {
            psLabel = "Mastercard";
        } else {
            psLabel = "";
        }

        tvCardTitle.setText(card.name);

        if (!TextUtils.isEmpty(psLabel)) {
            tvCardSubtitle.setText(psLabel + " •••• " + card.last4);
        } else {
            tvCardSubtitle.setText("•••• " + card.last4);
        }

        cardsContainer.addView(cardView);
    }

    // ---------- СТАТИСТИКА ----------

    private void updateStats() {
        int count = cards.size();
        tvTotalCards.setText(String.valueOf(count));
        tvActiveCards.setText(String.valueOf(count)); // пока считаем все активными
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
            // карт нет → показываем empty-state
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

                CardData card = new CardData(name, last4, ps);
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
