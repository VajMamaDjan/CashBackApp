package com.example.cashbackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Текущая вкладка – "Банки"
        bottomNavigationView.setSelectedItemId(R.id.nav_banks);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_banks) {
                // уже на этом экране, ничего не делаем
                return true;
            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                overridePendingTransition(0, 0); // без анимации
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        // каждый раз при возврате на экран перечитываем банки из prefs
        reloadBanks();
    }

    private void reloadBanks() {
        banksContainer.removeAllViews(); // очищаем старые карточки
        loadSavedBanks();                // заново добавляем из SharedPreferences
    }

    private void loadSavedBanks() {
        Set<String> saved = prefs.getStringSet("selected_banks", new HashSet<>());
        for (String bank : saved) {
            addBankCard(bank);
        }
    }

    // ---------- ДИАЛОГ ВЫБОРА БАНКА ----------

    private void showBankPicker() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_banks, null);
        bottomSheetDialog.setContentView(sheetView);

        ListView lvBanks = sheetView.findViewById(R.id.listBanks);
        TextView tvTitle = sheetView.findViewById(R.id.tvBottomSheetTitle);
        EditText etSearch = sheetView.findViewById(R.id.etSearchBank);

        // Адаптер на основе списка банков
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(Arrays.asList(allBanks))
        );
        lvBanks.setAdapter(adapter);

        // --- Иконки для поля поиска ---
        Drawable searchDrawable = ContextCompat.getDrawable(this, R.drawable.ic_search);
        Drawable clearDrawable = ContextCompat.getDrawable(this, R.drawable.ic_clear);

        if (searchDrawable != null) {
            searchDrawable.setBounds(0, 0,
                    searchDrawable.getIntrinsicWidth(),
                    searchDrawable.getIntrinsicHeight());
        }
        if (clearDrawable != null) {
            clearDrawable.setBounds(0, 0,
                    clearDrawable.getIntrinsicWidth(),
                    clearDrawable.getIntrinsicHeight());
        }

        // Изначально показываем только лупу слева, без крестика
        etSearch.setCompoundDrawables(
                searchDrawable,   // left
                null,
                null,             // right = null
                null
        );

        // ---- Фильтрация + показ/скрытие крестика ----
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);

                if (s.length() > 0) {
                    // есть текст → показываем крестик справа
                    etSearch.setCompoundDrawables(
                            searchDrawable,
                            null,
                            clearDrawable,
                            null
                    );
                } else {
                    // нет текста → убираем крестик
                    etSearch.setCompoundDrawables(
                            searchDrawable,
                            null,
                            null,
                            null
                    );
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ---- Обработка нажатия на крестик справа ----
        etSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && clearDrawable != null) {
                // координата X нажатия
                int touchX = (int) event.getX();
                int width = etSearch.getWidth();
                int paddingRight = etSearch.getPaddingRight();
                int drawableWidth = clearDrawable.getBounds().width();

                // зона нажатия: справа, где нарисован крестик
                int clearStart = width - paddingRight - drawableWidth;
                if (touchX >= clearStart) {
                    etSearch.setText(""); // очистить текст
                    adapter.getFilter().filter(null); // вернуть полный список
                    return true; // событие обработано
                }
            }
            return false; // остальные события не трогаем
        });

        // ---- Клик по банку ----
        lvBanks.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapter.getItem(position);
            if (selected == null) return;

            if (isBankAlreadyAdded(selected)) {
                Toast.makeText(this, "Банк уже добавлен", Toast.LENGTH_SHORT).show();
            } else {
                addBankCard(selected);
                saveBank(selected);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();

        // ---- Ограничение высоты и свайп по заголовку ----
        View bottomSheet = bottomSheetDialog.getDelegate()
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false); // нельзя тянуть за любую область

            int targetHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
            bottomSheet.getLayoutParams().height = targetHeight;
            bottomSheet.requestLayout();

            final float[] startY = new float[1];
            final int threshold = (int) (getResources().getDisplayMetrics().density * 40); // ~40dp

            tvTitle.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY[0] = event.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float diffY = event.getY() - startY[0];
                        if (diffY > threshold) {
                            bottomSheetDialog.dismiss();
                            return true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                }
                return false;
            });
        }
    }

    private boolean isBankAlreadyAdded(String bankName) {
        Set<String> saved = prefs.getStringSet("selected_banks", new HashSet<>());
        return saved.contains(bankName);
    }

    // ---------- ДОБАВЛЕНИЕ КАРТОЧКИ БАНКА ----------

    private void addBankCard(String bankName) {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_bank_swipe, banksContainer, false);

        TextView tvName = itemView.findViewById(R.id.tvBankName);
        ImageView ivLogo = itemView.findViewById(R.id.ivBankLogo);
        View cardContent = itemView.findViewById(R.id.cardContent);
        View deleteBackground = itemView.findViewById(R.id.deleteBackground);

        tvName.setText(bankName);

        // Логотипы
        if (bankName.contains("Сбер")) {
            ivLogo.setImageResource(R.drawable.ic_sber);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivLogo.setImageResource(R.drawable.ic_tbank);
        } else {
            ivLogo.setImageResource(R.drawable.ic_bank_placeholder);
        }

        // ---- Логика свайпа ----
        final float[] downX = new float[1];
        final float[] startTranslationX = new float[1];
        final float swipeThreshold = getResources().getDisplayMetrics().density * 40; // порог ~40dp
        final float maxSwipe = getResources().getDisplayMetrics().density * 160;

        cardContent.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    startTranslationX[0] = cardContent.getTranslationX();
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float moveX = event.getX();
                    float diff = moveX - downX[0];

                    // новое положение = то, что было, плюс сдвиг пальца
                    float newTranslation = startTranslationX[0] + diff;

                    // не даём уезжать вправо дальше исходной позиции
                    if (newTranslation > 0) {
                        newTranslation = 0;
                    }

                    // не даём уезжать влево больше, чем -maxSwipe
                    float max = maxSwipe > 0 ? maxSwipe : getResources().getDisplayMetrics().density * 120;
                    if (newTranslation < -max) {
                        newTranslation = -max;
                    }

                    cardContent.setTranslationX(newTranslation);

                    // если есть сдвиг влево — показываем красный фон
                    if (newTranslation < 0) {
                        deleteBackground.setVisibility(View.VISIBLE);
                    } else if (newTranslation == 0) {
                        deleteBackground.setVisibility(View.GONE);
                    }

                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    float currentTranslation = cardContent.getTranslationX();
                    float max = maxSwipe > 0 ? maxSwipe : getResources().getDisplayMetrics().density * 120;

                    if (Math.abs(currentTranslation) < swipeThreshold) {
                        // мало потянули → закрываем обратно
                        cardContent.animate()
                                .translationX(0)
                                .setDuration(150)
                                .withEndAction(() ->
                                        deleteBackground.setVisibility(View.GONE))
                                .start();
                    } else {
                        // достаточно потянули → раскрываем до конца (надпись полностью видна)
                        deleteBackground.setVisibility(View.VISIBLE);
                        cardContent.animate()
                                .translationX(-max)
                                .setDuration(150)
                                .start();
                    }
                    return true;
                }
            }
            return false;
        });

        // Нажатие на красную область "Удалить банк"
        deleteBackground.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить банк?")
                    .setMessage("Удалить «" + bankName + "» из списка?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        removeBank(bankName);
                        banksContainer.removeView(itemView);
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        // вернуть карточку назад и спрятать фон
                        cardContent.animate()
                                .translationX(0)
                                .setDuration(150)
                                .withEndAction(() ->
                                        deleteBackground.setVisibility(View.GONE))
                                .start();
                    })
                    .show();
        });

        banksContainer.addView(itemView);
    }
}