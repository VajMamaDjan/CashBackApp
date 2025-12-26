package com.example.cashbackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CashbackCategoriesActivity extends BaseActivity {

    // extras
    private String bankName;
    private String cardName;
    private String last4;
    private String ps;
    private int cardColor;
    private String cardId;

    // Month navigation
    private java.util.Calendar currentMonthCal;
    private TextView tvMonthName;
    private TextView tvYear;
    private View btnPrevMonth;
    private View btnNextMonth;

    // prefs
    private SharedPreferences prefs;        // app_prefs: лимит + выбранные категории
    private SharedPreferences customPrefs;  // отдельный prefs: кастомные категории

    // UI
    private LinearLayout selectedChipsContainer;
    private TextView tvHeaderSelected;
    private TextView tvPlacesLeft;
    private ImageView btnCategorySettings; // ✅ поле, не локальная переменная

    // selected categories: category -> percent (без знака %)
    private final java.util.LinkedHashMap<String, String> selectedCategories = new java.util.LinkedHashMap<>();

    // bottom sheet filtering
    private CashbackCategoryAdapter categoryAdapter;
    private final java.util.ArrayList<CashbackCategory> allCategories = new java.util.ArrayList<>();
    private final java.util.ArrayList<CashbackCategory> filteredCategories = new java.util.ArrayList<>();

    // card limit (1..20)
    private Integer categoryLimit;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashback_categories);

        // ✅ разделяем prefs
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        customPrefs = getSharedPreferences("custom_categories_prefs", MODE_PRIVATE);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        tvMonthName = findViewById(R.id.tvMonthName);
        tvYear = findViewById(R.id.tvYear);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        // текущий месяц = сегодня (с обнулением дня)
        currentMonthCal = java.util.Calendar.getInstance();
        currentMonthCal.set(java.util.Calendar.DAY_OF_MONTH, 1);

        renderMonthHeader();

        btnPrevMonth.setOnClickListener(v -> {
            shiftMonth(-1);
        });

        btnNextMonth.setOnClickListener(v -> {
            shiftMonth(+1);
        });


        // ---------- extras ----------
        Intent i = getIntent();
        bankName = i.getStringExtra("bank_name");
        cardName = i.getStringExtra("card_name");
        last4 = i.getStringExtra("card_last4");
        ps = i.getStringExtra("card_ps");
        cardColor = i.getIntExtra("card_color", Color.parseColor("#8A3CFF"));
        cardId = i.getStringExtra("card_id");

        if (bankName == null) bankName = "Банк";
        if (cardName == null) cardName = "Моя карта";
        if (last4 == null) last4 = "0000";

        // ---------- header gradient ----------
        View topContainer = findViewById(R.id.topContainer);
        applyHeaderGradient(topContainer, cardColor);

        View headerCardRoot = findViewById(R.id.headerCardRoot);
        applyCardGradient(headerCardRoot, cardColor);

        TextView tvHeaderCardName = findViewById(R.id.tvHeaderCardName);
        TextView tvHeaderCardLast4 = findViewById(R.id.tvHeaderCardLast4);
        tvHeaderCardName.setText(cardName);
        tvHeaderCardLast4.setText("•••• " + last4);

        // ---------- chips container ----------
        selectedChipsContainer = findViewById(R.id.selectedChipsContainer);

        // ---------- texts ----------
        tvHeaderSelected = findViewById(R.id.tvHeaderSelected);
        tvPlacesLeft = findViewById(R.id.tvPlacesLeft);
        tvPlacesLeft.setText("Выберите категории кэшбэка");

        // settings button (⚙️)
        try {
            btnCategorySettings = findViewById(R.id.btnCategorySettings);
        } catch (Exception ignored) {
            btnCategorySettings = null;
        }

        // load limit
        categoryLimit = loadCategoryLimitForCard();
        restoreSelectedCategoriesFromPrefs(); // уже будет грузить по текущему месяцу
        updateSelectedCount();

        if (btnCategorySettings != null) {
            btnCategorySettings.setOnClickListener(v -> showCategoryLimitBottomSheet());
        }

        // add category button
        findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            if (isLimitReached()) {
                Toast.makeText(
                        this,
                        "Достигнут лимит категорий для этой карты",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            showChooseCategorySheet();
        });

    }

    private void shiftMonth(int delta) {
        currentMonthCal.add(java.util.Calendar.MONTH, delta);
        currentMonthCal.set(java.util.Calendar.DAY_OF_MONTH, 1);

        renderMonthHeader();

        // Загружаем категории для нового месяца
        restoreSelectedCategoriesFromPrefs();
        updateSelectedCount();
    }

    private void renderMonthHeader() {
        java.util.Locale ru = new java.util.Locale("ru");
        String month = currentMonthCal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, ru);
        if (month != null && month.length() > 0) {
            month = month.substring(0,1).toUpperCase(ru) + month.substring(1);
        }
        int year = currentMonthCal.get(java.util.Calendar.YEAR);

        if (tvMonthName != null) tvMonthName.setText(month);
        if (tvYear != null) tvYear.setText(String.valueOf(year));
    }

    private String getMonthKey() {
        int y = currentMonthCal.get(java.util.Calendar.YEAR);
        int m = currentMonthCal.get(java.util.Calendar.MONTH) + 1; // 1..12
        return String.format(java.util.Locale.US, "%04d-%02d", y, m); // например 2025-12
    }

    // =========================================================
    // BottomSheet choose category (4 columns + search)
    // =========================================================

    private void showChooseCategorySheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_cashback_categories, null);
        dialog.setContentView(sheet);

        sheet.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        RecyclerView rv = sheet.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new GridLayoutManager(this, 4));

        android.widget.EditText etSearch = sheet.findViewById(R.id.etSearch);
        View btnClear = sheet.findViewById(R.id.btnClearSearch);
        View btnAddCustom = sheet.findViewById(R.id.btnAddCustomCategory);

        TextView tvAddCustom = sheet.findViewById(R.id.tvAddCustom);
        ImageView ivAddCustom = sheet.findViewById(R.id.ivAddCustom);

        // обновляем состояние сразу при открытии
        updateAddCustomCategoryState(btnAddCustom, tvAddCustom, ivAddCustom);

        // load categories (built-in + custom)
        rebuildAllCategories();
        filteredCategories.clear();
        filteredCategories.addAll(allCategories);

        categoryAdapter = new CashbackCategoryAdapter(filteredCategories, category -> {
            dialog.dismiss();
            String existing = selectedCategories.get(category.name);
            askCashbackPercent(category.name, existing);
        });
        rv.setAdapter(categoryAdapter);

        // search
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s.toString().trim().toLowerCase();
                btnClear.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);

                filteredCategories.clear();
                if (q.isEmpty()) {
                    filteredCategories.addAll(allCategories);
                } else {
                    for (CashbackCategory cc : allCategories) {
                        if (cc.name.toLowerCase().contains(q)) filteredCategories.add(cc);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });

        btnClear.setOnClickListener(v -> etSearch.setText(""));

        if (btnAddCustom != null) {
            btnAddCustom.setOnClickListener(v -> {
                if (isLimitReached()) {
                    Toast.makeText(
                            this,
                            "Достигнут лимит категорий для этой карты",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                showAddCustomCategoryDialog(() -> {
                    updateSelectedCount();

                    rebuildAllCategories();
                    String q = etSearch.getText().toString().trim().toLowerCase();

                    filteredCategories.clear();
                    if (q.isEmpty()) {
                        filteredCategories.addAll(allCategories);
                    } else {
                        for (CashbackCategory cc : allCategories) {
                            if (cc.name.toLowerCase().contains(q)) {
                                filteredCategories.add(cc);
                            }
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                });
            });
        }

        dialog.show();
    }

    // =========================================================
    // Custom categories  (✅ отдельный prefs)
    // =========================================================

    private static final String PREF_CUSTOM_CATEGORIES = "custom_cashback_categories";

    private void rebuildAllCategories() {
        allCategories.clear();

        // built-in
        String[] names = getResources().getStringArray(R.array.cashback_categories);
        for (String n : names) allCategories.add(new CashbackCategory(applyNiceWrap(n)));

        // custom (global for user)
        for (String n : loadCustomCategories()) allCategories.add(new CashbackCategory(applyNiceWrap(n)));
    }

    private String applyNiceWrap(String name) {
        if (name == null) return "";
        switch (name) {
            case "Автозапчасти":
                return "Автозап\u00ADчасти";
            default:
                return name;
        }
    }

    private java.util.ArrayList<String> loadCustomCategories() {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        String json = customPrefs.getString(PREF_CUSTOM_CATEGORIES, "[]");
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.optString(i, "").trim();
                if (!s.isEmpty()) result.add(s);
            }
        } catch (Exception ignored) {}
        return result;
    }

    private void addCustomCategoryToPrefs(String name) {
        java.util.ArrayList<String> list = loadCustomCategories();
        list.add(name);

        org.json.JSONArray arr = new org.json.JSONArray();
        for (String s : list) arr.put(s);

        customPrefs.edit().putString(PREF_CUSTOM_CATEGORIES, arr.toString()).apply();
    }

    private boolean isCategoryExists(String name) {
        String n = name.trim().toLowerCase();

        // built-in
        String[] builtIn = getResources().getStringArray(R.array.cashback_categories);
        for (String s : builtIn) {
            if (s != null && s.trim().toLowerCase().equals(n)) return true;
        }

        // custom
        for (String s : loadCustomCategories()) {
            if (s.trim().toLowerCase().equals(n)) return true;
        }

        return false;
    }

    private int getEffectiveLimit() {
        return (categoryLimit == null) ? 5 : categoryLimit; // дефолт 5
    }

    private boolean isLimitReached() {
        return selectedCategories.size() >= getEffectiveLimit();
    }

    private void updateAddCustomCategoryState(View btnAddCustom, TextView tvAddCustom, ImageView ivAddCustom) {
        if (btnAddCustom == null || tvAddCustom == null || ivAddCustom == null) return;

        int limit = getEffectiveLimit();
        boolean reached = selectedCategories.size() >= limit;

        btnAddCustom.setEnabled(!reached);
        btnAddCustom.setAlpha(reached ? 0.45f : 1f);

        int activeColor = Color.parseColor("#6A35FF");
        int disabledColor = Color.parseColor("#9A9A9A");

        tvAddCustom.setTextColor(reached ? disabledColor : activeColor);
        ivAddCustom.setColorFilter(reached ? disabledColor : activeColor);
    }


    private void showAddCustomCategoryDialog(Runnable onAdded) {
        final android.widget.EditText et = new android.widget.EditText(this);
        et.setHint("Например: Аптеки у дома");
        et.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        new AlertDialog.Builder(this)
                .setTitle("Новая категория")
                .setMessage("Введите название категории")
                .setView(et)
                .setPositiveButton("Добавить", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (name.length() > 30) {
                        Toast.makeText(this, "Слишком длинное название (до 30 символов)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isCategoryExists(name)) {
                        Toast.makeText(this, "Такая категория уже существует", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addCustomCategoryToPrefs(name);
                    Toast.makeText(this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                    if (onAdded != null) onAdded.run();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // =========================================================
    // Add/Edit percent dialog
    // =========================================================

    private void askCashbackPercent(String categoryName, @Nullable String prefillPercent) {
        final android.widget.EditText et = new android.widget.EditText(this);
        et.setHint("Например: 5");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (prefillPercent != null && !prefillPercent.isEmpty()) {
            et.setText(prefillPercent);
            et.setSelection(prefillPercent.length());
        }

        boolean isEdit = selectedCategories.containsKey(categoryName);

        int effectiveLimit = (categoryLimit == null) ? 5 : categoryLimit;

        if (!isEdit && selectedCategories.size() >= getEffectiveLimit()) {
            Toast.makeText(this, "Достигнут лимит категорий для этой карты", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(categoryName)
                .setMessage(isEdit ? "Измените % кэшбэка" : "Укажите % кэшбэка для этой категории")
                .setView(et)
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (d, w) -> {
                    String raw = et.getText().toString().trim().replace(",", ".");
                    if (raw.isEmpty()) {
                        Toast.makeText(this, "Введите процент", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double val = Double.parseDouble(raw);
                        if (val <= 0 || val > 100) {
                            Toast.makeText(this, "Процент должен быть от 0 до 100", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Некорректное значение", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedCategories.put(categoryName, raw);
                    refreshChipsUI();
                    updateSelectedCount();
                    saveSelectedCategoriesToPrefs();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // =========================================================
    // Chips UI
    // =========================================================

    private void refreshChipsUI() {
        selectedChipsContainer.removeAllViews();
        for (java.util.Map.Entry<String, String> e : selectedCategories.entrySet()) {
            addChip(e.getKey(), e.getValue());
        }
    }

    private void addChip(String name, String percent) {
        View chip = getLayoutInflater().inflate(R.layout.item_selected_category_chip, selectedChipsContainer, false);

        TextView tvName = chip.findViewById(R.id.tvChipName);
        TextView tvPercent = chip.findViewById(R.id.tvChipPercent);
        ImageButton btnMenu = chip.findViewById(R.id.btnChipMenu);

        tvName.setText(name);
        tvPercent.setText(percent + "%");

        // ✅ редактирование ТОЛЬКО через меню
        btnMenu.setOnClickListener(v -> showChipContextMenu(v, name, chip));

        selectedChipsContainer.addView(chip);
    }

    // =========================================================
    // Chip context menu (⋮)
    // =========================================================

    private void showChipContextMenu(View anchor, String categoryName, View chipView) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END, 0, R.style.WhitePopupMenu);
        popup.getMenuInflater().inflate(R.menu.menu_selected_category_chip, popup.getMenu());
        forceShowPopupMenuIcons(popup);
        popup.setOnMenuItemClickListener(item -> handleChipMenuItem(item, categoryName, chipView));
        popup.show();
    }

    private boolean handleChipMenuItem(MenuItem item, String categoryName, View chipView) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            String current = selectedCategories.get(categoryName);
            askCashbackPercent(categoryName, current);
            return true;
        }

        if (id == R.id.action_delete) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Удалить категорию?")
                    .setMessage("Вы точно хотите удалить \"" + categoryName + "\" из выбранных категорий?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        selectedCategories.remove(categoryName);
                        selectedChipsContainer.removeView(chipView);
                        updateSelectedCount();
                        saveSelectedCategoriesToPrefs();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        }

        if (id == R.id.action_set_period) {
            Toast.makeText(this, "Указать период — в разработке", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_copy) {
            Toast.makeText(this, "Копировать — в разработке", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_limit) {
            Toast.makeText(this, "Лимит — в разработке", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void forceShowPopupMenuIcons(PopupMenu popup) {
        try {
            java.lang.reflect.Field f = PopupMenu.class.getDeclaredField("mPopup");
            f.setAccessible(true);
            Object helper = f.get(popup);
            Class<?> cls = Class.forName(helper.getClass().getName());
            java.lang.reflect.Method m = cls.getDeclaredMethod("setForceShowIcon", boolean.class);
            m.setAccessible(true);
            m.invoke(helper, true);
        } catch (Exception ignored) {}
    }

    private void updateSelectedCount() {
        if (tvHeaderSelected == null) return;

        int selected = selectedCategories.size();
        int limitToShow = (categoryLimit == null) ? 5 : categoryLimit;

        tvHeaderSelected.setText("Выбрано " + selected + " из " + limitToShow + " категорий");
    }

    // =========================================================
    // Preferences (save/restore by card)
    // =========================================================

    private String getPrefsKeyForThisCard() {
        String monthKey = getMonthKey();

        if (cardId != null && !cardId.trim().isEmpty()) {
            return "cashback_categories_card_" + cardId.trim() + "_" + monthKey;
        }
        return "cashback_categories_" + bankName + "_" + last4 + "_" + monthKey;
    }

    private void saveSelectedCategoriesToPrefs() {
        org.json.JSONObject obj = new org.json.JSONObject();
        try {
            for (java.util.Map.Entry<String, String> e : selectedCategories.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }

        prefs.edit()
                .putString(getPrefsKeyForThisCard(), obj.toString())
                .apply();
    }

    private void restoreSelectedCategoriesFromPrefs() {
        selectedCategories.clear();
        selectedChipsContainer.removeAllViews();

        String json = prefs.getString(getPrefsKeyForThisCard(), null);
        if (json == null || json.isEmpty()) return;

        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            java.util.Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String category = keys.next();
                String percent = obj.optString(category, "");
                if (!percent.isEmpty()) {
                    selectedCategories.put(category, percent);
                    addChip(category, percent);
                }
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // Category limit (per card)
    // =========================================================

    private String getLimitPrefsKeyForThisCard() {
        if (cardId != null && !cardId.trim().isEmpty()) {
            return "category_limit_card_" + cardId.trim();
        }

        String safePs = (ps == null) ? "" : ps.trim();
        String safeLast4 = (last4 == null) ? "" : last4.trim();
        String safeName = (cardName == null) ? "" : cardName.trim();
        return "category_limit_" + safePs + "_" + safeLast4 + "_" + safeName;
    }

    private void saveCategoryLimitForCard() {
        SharedPreferences.Editor ed = prefs.edit();
        if (categoryLimit == null) {
            ed.remove(getLimitPrefsKeyForThisCard());
        } else {
            ed.putInt(getLimitPrefsKeyForThisCard(), categoryLimit);
        }
        ed.apply();
    }

    private Integer loadCategoryLimitForCard() {
        String key = getLimitPrefsKeyForThisCard();
        if (!prefs.contains(key)) return null;

        int v = prefs.getInt(key, 0);
        return (v <= 0) ? null : v;
    }

    // ✅ ВОЗВРАЩАЕМ РАБОЧЕЕ ОКНО ЛИМИТА (как у тебя было)
    private void showCategoryLimitBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_category_limit, null);
        dialog.setContentView(sheet);

        View btnClose = sheet.findViewById(R.id.btnClose);
        View btnCancel = sheet.findViewById(R.id.btnCancel);
        View btnSave = sheet.findViewById(R.id.btnSave);

        android.widget.EditText etLimit = sheet.findViewById(R.id.etLimit);
        View btnUp = sheet.findViewById(R.id.btnUp);
        View btnDown = sheet.findViewById(R.id.btnDown);

        int start = (categoryLimit == null) ? 5 : categoryLimit;
        start = clamp(start, 1, 20);
        etLimit.setText(String.valueOf(start));
        etLimit.setSelection(etLimit.getText().length());

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUp.setOnClickListener(v -> {
            int cur = parseIntSafe(etLimit.getText().toString(), 1);
            cur = clamp(cur + 1, 1, 20);
            etLimit.setText(String.valueOf(cur));
            etLimit.setSelection(etLimit.getText().length());
        });

        btnDown.setOnClickListener(v -> {
            int cur = parseIntSafe(etLimit.getText().toString(), 1);
            cur = clamp(cur - 1, 1, 20);
            etLimit.setText(String.valueOf(cur));
            etLimit.setSelection(etLimit.getText().length());
        });

        btnSave.setOnClickListener(v -> {
            int val = parseIntSafe(etLimit.getText().toString(), 1);
            val = clamp(val, 1, 20);

            categoryLimit = val;
            saveCategoryLimitForCard();
            updateSelectedCount();
            dialog.dismiss();
        });

        dialog.show();
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            s = s.trim();
            if (s.isEmpty()) return fallback;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    // =========================================================
    // Gradient helpers
    // =========================================================

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
        return Color.rgb(
                (int) (Color.red(color) * factor),
                (int) (Color.green(color) * factor),
                (int) (Color.blue(color) * factor)
        );
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}