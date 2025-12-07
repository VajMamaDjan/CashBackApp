package com.example.cashbackapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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

    @SuppressLint("ClickableViewAccessibility")
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
        View bottomSheet = (View) sheetView.getParent();

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

        final float density = getResources().getDisplayMetrics().density;

        final float maxSwipe      = density * 145f;   // Максимальный вылет
        final long  animDuration  = 160L;             // Мягкая анимация

        // Порог для открытия — почти нулевой (мгновенное открытие)
        final float openThresholdPart  = 0.05f;       // 5% пути

        // Порог для закрытия — очень лёгкий
        final float closeThresholdPart = 0.85f;       // если осталось < 85% → закрыть

        // -------------------------------------------------------------------

        final float[] downX = new float[1];
        final float[] startTranslationX = new float[1];

        cardContent.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    startTranslationX[0] = cardContent.getTranslationX();
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float moveX = event.getX();
                    float diffX = moveX - downX[0];

                    float newTranslation = startTranslationX[0] + diffX;

                    // Не вправо
                    if (newTranslation > 0) newTranslation = 0;

                    // Не слишком влево
                    if (newTranslation < -maxSwipe) newTranslation = -maxSwipe;

                    cardContent.setTranslationX(newTranslation);

                    // Фон появляется сразу при малейшем движении влево
                    if (newTranslation < 0) {
                        deleteBackground.setVisibility(View.VISIBLE);
                    } else {
                        deleteBackground.setVisibility(View.GONE);
                    }

                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {

                    float current = cardContent.getTranslationX();
                    float openedPart = Math.abs(current) / maxSwipe;

                    boolean startedClosed = Math.abs(startTranslationX[0]) < maxSwipe * 0.5f;

                    if (startedClosed) {
                        // Жест "Открыть"
                        if (openedPart < openThresholdPart) {
                            // Слишком маленький — закрыть назад
                            cardContent.animate()
                                    .translationX(0)
                                    .setDuration(animDuration)
                                    .withEndAction(() -> deleteBackground.setVisibility(View.GONE))
                                    .start();
                        } else {
                            // Открыть полностью
                            deleteBackground.setVisibility(View.VISIBLE);
                            cardContent.animate()
                                    .translationX(-maxSwipe)
                                    .setDuration(animDuration)
                                    .start();
                        }
                    } else {
                        // Жест "Закрыть"
                        if (openedPart < closeThresholdPart) {
                            // Достаточно чуть вернуть — закрываем
                            cardContent.animate()
                                    .translationX(0)
                                    .setDuration(animDuration)
                                    .withEndAction(() -> deleteBackground.setVisibility(View.GONE))
                                    .start();
                        } else {
                            // Осталось много открытого — оставляем открытой
                            deleteBackground.setVisibility(View.VISIBLE);
                            cardContent.animate()
                                    .translationX(-maxSwipe)
                                    .setDuration(animDuration)
                                    .start();
                        }
                    }

                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }

            return false;
        });

        // свайп и тап по красной зоне "Удалить банк"
        final float touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        final float[] delDownX = new float[1];
        final float[] delDownY = new float[1];
        final float[] delStartTranslationX = new float[1];
        final boolean[] delIsDragging = new boolean[1];

        deleteBackground.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    delDownX[0] = event.getX();
                    delDownY[0] = event.getY();
                    delStartTranslationX[0] = cardContent.getTranslationX();
                    delIsDragging[0] = false;
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getX() - delDownX[0];
                    float dy = event.getY() - delDownY[0];

                    // решаем, это свайп или ещё потенциальный тап
                    if (!delIsDragging[0]) {
                        if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                            delIsDragging[0] = true; // начинаем тащить
                        } else {
                            return true; // ждём UP для тапа
                        }
                    }

                    float newTranslation = delStartTranslationX[0] + dx;

                    if (newTranslation > 0) newTranslation = 0;
                    if (newTranslation < -maxSwipe) newTranslation = -maxSwipe;

                    cardContent.setTranslationX(newTranslation);

                    if (newTranslation < 0) {
                        deleteBackground.setVisibility(View.VISIBLE);
                    } else {
                        deleteBackground.setVisibility(View.GONE);
                    }

                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    v.getParent().requestDisallowInterceptTouchEvent(false);

                    float dx = event.getX() - delDownX[0];
                    float dy = event.getY() - delDownY[0];

                    // если не тянули (нет драг-жеста) → считаем это тапом → диалог
                    if (!delIsDragging[0] &&
                            Math.abs(dx) < touchSlop &&
                            Math.abs(dy) < touchSlop) {

                        new AlertDialog.Builder(this)
                                .setTitle("Удалить банк?")
                                .setMessage("Удалить «" + bankName + "» из списка?")
                                .setPositiveButton("Удалить", (dialog, which) -> {
                                    removeBank(bankName);
                                    banksContainer.removeView(itemView);
                                })
                                .setNegativeButton("Отмена", (dialog, which) -> {
                                    cardContent.animate()
                                            .translationX(0)
                                            .setDuration(animDuration)
                                            .withEndAction(() -> deleteBackground.setVisibility(View.GONE))
                                            .start();
                                })
                                .show();

                        return true;
                    }

                    // если тянули → завершаем свайп (логика закрытия/оставить открытой)
                    float current = cardContent.getTranslationX();
                    float openedPart = Math.abs(current) / maxSwipe;

                    if (openedPart < closeThresholdPart) {
                        // закрыть
                        cardContent.animate()
                                .translationX(0)
                                .setDuration(animDuration)
                                .withEndAction(() -> deleteBackground.setVisibility(View.GONE))
                                .start();
                    } else {
                        // оставить полностью открытой
                        deleteBackground.setVisibility(View.VISIBLE);
                        cardContent.animate()
                                .translationX(-maxSwipe)
                                .setDuration(animDuration)
                                .start();
                    }

                    return true;
                }
            }

            return false;
        });

        banksContainer.addView(itemView);
    }


}