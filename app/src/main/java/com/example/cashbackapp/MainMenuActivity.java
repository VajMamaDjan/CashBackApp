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
import android.view.ViewGroup;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.widget.NestedScrollView;

public class MainMenuActivity extends BaseActivity {

    private CardView cardAddBank;
    private FrameLayout profileButton;
    private LinearLayout banksContainer;
    private SharedPreferences prefs;
    private String[] allBanks;
    private LinearLayout layoutMyBanksHeader;
    private ImageView ivMyBanksArrow;
    private boolean isBanksExpanded = true;
    private SwipeRefreshLayout swipeRefresh;
    private NestedScrollView scrollContent;

    @Override
    protected boolean useFullscreenStatusBar() {
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
        setupSwipeToRefresh();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_banks);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_banks) {
                return true;
            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        loadSavedBanks();
    }

    private void toggleBanksList() {
        isBanksExpanded = !isBanksExpanded;

        if (isBanksExpanded) {
            banksContainer.setVisibility(View.VISIBLE);
            banksContainer.setAlpha(0f);
            banksContainer.animate().alpha(1f).setDuration(150).start();
            ivMyBanksArrow.animate().rotation(0f).setDuration(150).start();
        } else {
            banksContainer.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> banksContainer.setVisibility(View.GONE))
                    .start();
            ivMyBanksArrow.animate().rotation(180f).setDuration(150).start();
        }
    }

    private void syncDeleteBgHeight(View deleteBackground, View cardContent) {
        int h = cardContent.getHeight();
        if (h <= 0) return;
        ViewGroup.LayoutParams lp = deleteBackground.getLayoutParams();
        if (lp.height != h) {
            lp.height = h;
            deleteBackground.setLayoutParams(lp);
        }
    }


    private void initViews() {
        cardAddBank = findViewById(R.id.cardAddBank);
        profileButton = findViewById(R.id.profileButton);
        banksContainer = findViewById(R.id.banksContainer);
        layoutMyBanksHeader = findViewById(R.id.layoutMyBanksHeader);
        ivMyBanksArrow = findViewById(R.id.ivMyBanksArrow);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        scrollContent = findViewById(R.id.scrollContent);
    }

    private void setupClicks() {
        cardAddBank.setOnClickListener(v -> showBankPicker());

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        layoutMyBanksHeader.setOnClickListener(v -> toggleBanksList());
    }

    // ---------- SharedPreferences ----------
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
        reloadBanks();
    }

    private void reloadBanks() {
        banksContainer.removeAllViews();
        loadSavedBanks();
    }

    private void loadSavedBanks() {
        Set<String> saved = prefs.getStringSet("selected_banks", new HashSet<>());
        for (String bank : saved) {
            addBankCard(bank);
        }
    }

    // ---------- Bottom sheet ----------
    @SuppressLint("ClickableViewAccessibility")
    private void showBankPicker() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_banks, null);
        bottomSheetDialog.setContentView(sheetView);

        ListView lvBanks = sheetView.findViewById(R.id.listBanks);
        TextView tvTitle = sheetView.findViewById(R.id.tvBottomSheetTitle);
        EditText etSearch = sheetView.findViewById(R.id.etSearchBank);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(Arrays.asList(allBanks))
        );
        lvBanks.setAdapter(adapter);

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

        etSearch.setCompoundDrawables(searchDrawable, null, null, null);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                if (s.length() > 0) {
                    etSearch.setCompoundDrawables(searchDrawable, null, clearDrawable, null);
                } else {
                    etSearch.setCompoundDrawables(searchDrawable, null, null, null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && clearDrawable != null) {
                int touchX = (int) event.getX();
                int width = etSearch.getWidth();
                int paddingRight = etSearch.getPaddingRight();
                int drawableWidth = clearDrawable.getBounds().width();

                int clearStart = width - paddingRight - drawableWidth;
                if (touchX >= clearStart) {
                    etSearch.setText("");
                    adapter.getFilter().filter(null);
                    return true;
                }
            }
            return false;
        });

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

        View bottomSheet = (View) sheetView.getParent();

        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);

            int targetHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
            bottomSheet.getLayoutParams().height = targetHeight;
            bottomSheet.requestLayout();

            final float[] startY = new float[1];
            final int threshold = (int) (getResources().getDisplayMetrics().density * 40);

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

    // ---------- Bank card item ----------
    // ---------- Bank card item ----------
    // ---------- Bank card item ----------
    private void addBankCard(String bankName) {

        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_bank_swipe, banksContainer, false);

        TextView tvName = itemView.findViewById(R.id.tvBankName);
        ImageView ivLogo = itemView.findViewById(R.id.ivBankLogo);
        View cardContent = itemView.findViewById(R.id.cardContent);
        View deleteBackground = itemView.findViewById(R.id.deleteBackground);

        tvName.setText(bankName);

        // Логотипы (как у тебя сейчас)
        if (bankName.contains("Сбер")) {
            ivLogo.setImageResource(R.drawable.sber_logo);
        } else if (bankName.contains("Т-Банк") || bankName.contains("ТБанк")) {
            ivLogo.setImageResource(R.drawable.ic_tbank);
        } else {
            ivLogo.setImageResource(R.drawable.ic_bank_placeholder);
        }

        final float density = getResources().getDisplayMetrics().density;
        final float maxSwipe = density * 145f;
        final long animDuration = 160L;

        final float openThresholdPart = 0.05f;
        final float closeThresholdPart = 0.85f;

        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final float[] startTranslationX = new float[1];
        final boolean[] isSwipingHorizontally = new boolean[1];
        final boolean[] isDecided = new boolean[1];

        final float touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        // ======= Фикс высоты deleteBackground =======
        // deleteBackground в XML имеет height=0dp -> задаём высоту равную cardContent
        final Runnable syncDeleteHeight = () -> {
            int h = cardContent.getHeight();
            if (h <= 0) return;
            ViewGroup.LayoutParams lp = deleteBackground.getLayoutParams();
            if (lp.height != h) {
                lp.height = h;
                deleteBackground.setLayoutParams(lp);
            }
        };

        // на старте и при любых изменениях layout
        itemView.post(syncDeleteHeight);
        cardContent.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, orr, ob) -> syncDeleteHeight.run());

        // фон по умолчанию скрыт через alpha (без GONE/INVISIBLE)
        deleteBackground.setAlpha(0f);

        cardContent.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN: {
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    startTranslationX[0] = cardContent.getTranslationX();

                    isSwipingHorizontally[0] = false;
                    isDecided[0] = false;

                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    float dx = event.getX() - downX[0];
                    float dy = event.getY() - downY[0];

                    if (!isDecided[0]) {
                        if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                            isDecided[0] = true;

                            if (Math.abs(dx) > Math.abs(dy)) {
                                isSwipingHorizontally[0] = true;
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            } else {
                                isSwipingHorizontally[0] = false;
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                return false;
                            }
                        } else {
                            return true;
                        }
                    }

                    if (!isSwipingHorizontally[0]) {
                        return false;
                    }

                    float newTranslation = startTranslationX[0] + dx;

                    if (newTranslation > 0) newTranslation = 0;
                    if (newTranslation < -maxSwipe) newTranslation = -maxSwipe;

                    cardContent.setTranslationX(newTranslation);

                    // показываем/прячем фон ТОЛЬКО через alpha (не трогаем visibility)
                    if (newTranslation < 0) {
                        syncDeleteHeight.run();
                        if (deleteBackground.getAlpha() < 1f) deleteBackground.setAlpha(1f);
                    } else {
                        if (deleteBackground.getAlpha() > 0f) deleteBackground.setAlpha(0f);
                    }

                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {

                    float dxTap = event.getX() - downX[0];
                    float dyTap = event.getY() - downY[0];

                    // ТАП по карточке
                    if (!isDecided[0] && Math.abs(dxTap) < touchSlop && Math.abs(dyTap) < touchSlop) {

                        // если открыто — закрываем
                        if (Math.abs(cardContent.getTranslationX()) > 1f) {
                            cardContent.animate()
                                    .translationX(0f)
                                    .setDuration(animDuration)
                                    .withEndAction(() -> deleteBackground.setAlpha(0f))
                                    .start();
                        }

                        v.performClick();

                        Intent intent = new Intent(MainMenuActivity.this, BankCardsActivity.class);
                        intent.putExtra("bank_name", bankName);
                        startActivity(intent);

                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    }

                    // вертикальный жест — отдаём скроллу
                    if (!isSwipingHorizontally[0]) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }

                    // открыть/закрыть
                    float current = cardContent.getTranslationX();
                    float openedPart = Math.abs(current) / maxSwipe;

                    boolean startedClosed = Math.abs(startTranslationX[0]) < maxSwipe * 0.5f;

                    if (startedClosed) {
                        if (openedPart < openThresholdPart) {
                            cardContent.animate()
                                    .translationX(0f)
                                    .setDuration(animDuration)
                                    .withEndAction(() -> deleteBackground.setAlpha(0f))
                                    .start();
                        } else {
                            syncDeleteHeight.run();
                            deleteBackground.setAlpha(1f);
                            cardContent.animate()
                                    .translationX(-maxSwipe)
                                    .setDuration(animDuration)
                                    .start();
                        }
                    } else {
                        if (openedPart < closeThresholdPart) {
                            cardContent.animate()
                                    .translationX(0f)
                                    .setDuration(animDuration)
                                    .withEndAction(() -> deleteBackground.setAlpha(0f))
                                    .start();
                        } else {
                            syncDeleteHeight.run();
                            deleteBackground.setAlpha(1f);
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

        // Клик по красной зоне -> удалить
        deleteBackground.setOnClickListener(v ->
                confirmDeleteBank(bankName, itemView, cardContent, deleteBackground)
        );

        // Если хочешь — можно оставить жесты на deleteBackground, но безопаснее просто клик.
        banksContainer.addView(itemView);
    }

    // ===================== ГЛУБОКОЕ УДАЛЕНИЕ БАНКА =====================

    private void confirmDeleteBank(
            String bankName,
            View itemView,
            View cardContent,
            View deleteBackground
    ) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить банк?")
                .setMessage("Будут удалены все карты и категории кэшбэка, связанные с «" + bankName + "».")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deepDeleteBankData(bankName);
                    banksContainer.removeView(itemView);
                    Toast.makeText(this, "Банк удалён", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    if (cardContent != null && deleteBackground != null) {
                        cardContent.animate()
                                .translationX(0)
                                .setDuration(160)
                                .withEndAction(() -> deleteBackground.setVisibility(View.INVISIBLE))
                                .start();
                    }
                })
                .show();
    }

    /**
     * Полное удаление банка:
     * - банк из selected_banks
     * - cards_for_<bank>
     * - cashback_categories_card_<cardId>
     * - category_limit_card_<cardId>
     * - и любых ключей, начинающихся с этих префиксов (на будущее: помесячные ключи и т.п.)
     */
    private void deepDeleteBankData(String bankName) {
        if (prefs == null) {
            prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = prefs.edit();

        // 1) убрать банк из выбранных
        java.util.Set<String> selectedBanks =
                new java.util.HashSet<>(prefs.getStringSet("selected_banks", new java.util.HashSet<>()));
        selectedBanks.remove(bankName);
        editor.putStringSet("selected_banks", selectedBanks);

        // 2) достать карты банка
        String cardsKey = "cards_for_" + bankName;
        String cardsJson = prefs.getString(cardsKey, null);

        if (cardsJson != null && !cardsJson.isEmpty()) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(cardsJson);

                // снимок ключей, чтобы удалять по префиксам
                java.util.Set<String> allKeys = new java.util.HashSet<>(prefs.getAll().keySet());

                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;

                    String cardId = obj.optString("id", "").trim();
                    if (cardId.isEmpty()) continue;

                    String categoriesKey = "cashback_categories_card_" + cardId;
                    String limitKey = "category_limit_card_" + cardId;

                    // точные ключи
                    editor.remove(categoriesKey);
                    editor.remove(limitKey);

                    // любые ключи с префиксами
                    for (String k : allKeys) {
                        if (k.startsWith(categoriesKey) || k.startsWith(limitKey)) {
                            editor.remove(k);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3) удалить список карт банка
        editor.remove(cardsKey);

        editor.apply();
    }
    // ---------- Swipe-to-refresh ----------
    private void setupSwipeToRefresh() {
        if (swipeRefresh == null) return;

        // Pull-to-refresh должен срабатывать только когда ScrollView в самом верху.
        swipeRefresh.setOnChildScrollUpCallback((parent, child) -> {
            if (scrollContent == null) return false;
            return scrollContent.canScrollVertically(-1);
        });

        swipeRefresh.setOnRefreshListener(this::refreshMainMenuData);
    }

    private void refreshMainMenuData() {
        // Основное обновление данных на экране
        reloadBanks();

        // Сохраняем состояние свернуто/развернуто
        if (!isBanksExpanded) {
            banksContainer.setVisibility(View.GONE);
            banksContainer.setAlpha(1f);
            if (ivMyBanksArrow != null) ivMyBanksArrow.setRotation(180f);
        } else {
            banksContainer.setVisibility(View.VISIBLE);
            banksContainer.setAlpha(1f);
            if (ivMyBanksArrow != null) ivMyBanksArrow.setRotation(0f);
        }

        // Остановить индикатор
        if (swipeRefresh != null) {
            swipeRefresh.post(() -> swipeRefresh.setRefreshing(false));
        }
    }
}
