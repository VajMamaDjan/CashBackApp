package com.example.cashbackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CashbackCategoriesActivity extends BaseActivity {

    // extras
    private String cardId;
    private String bankName;
    private String cardName;
    private String last4;
    private String ps;
    private int cardColor;

    // RUB / MILES
    private String cashbackUnit;

    // prefs
    private SharedPreferences prefs;

    // UI
    private LinearLayout selectedChipsContainer;
    private TextView tvHeaderSelected;
    private TextView tvPlacesLeft;

    // selected categories: category -> percent (без знака %)
    private final java.util.LinkedHashMap<String, String> selectedCategories = new java.util.LinkedHashMap<>();

    // bottom sheet filtering
    private CashbackCategoryAdapter categoryAdapter;
    private final java.util.ArrayList<CashbackCategory> allCategories = new java.util.ArrayList<>();
    private final java.util.ArrayList<CashbackCategory> filteredCategories = new java.util.ArrayList<>();

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashback_categories);

        // ✅ обязательно первым, чтобы не было NPE
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // ---------- extras ----------
        Intent i = getIntent();
        bankName = i.getStringExtra("bank_name");
        cardName = i.getStringExtra("card_name");
        last4 = i.getStringExtra("card_last4");
        ps = i.getStringExtra("card_ps");
        cardColor = i.getIntExtra("card_color", Color.parseColor("#8A3CFF"));
        cardId = getIntent().getStringExtra("card_id");

        cashbackUnit = i.getStringExtra("card_cashback_unit");
        if (cashbackUnit == null) cashbackUnit = "RUB";

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

        // RUB / ✈️ + "МИЛИ" в хедере (если у тебя добавлен tvHeaderCashbackUnit)
        TextView tvHeaderCashbackUnit = null;
        try {
            tvHeaderCashbackUnit = findViewById(R.id.tvHeaderCashbackUnit);
        } catch (Exception ignored) {}

        if (tvHeaderCashbackUnit != null) {
            if ("MILES".equalsIgnoreCase(cashbackUnit)) {
                tvHeaderCashbackUnit.setText("МИЛИ");
                tvHeaderCashbackUnit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plane, 0, 0, 0);
                tvHeaderCashbackUnit.setCompoundDrawablePadding((int) dp(4));
            } else {
                tvHeaderCashbackUnit.setCompoundDrawables(null, null, null, null);
                tvHeaderCashbackUnit.setText("РУБ");
            }
        }

        // ---------- chips container ----------
        selectedChipsContainer = findViewById(R.id.selectedChipsContainer);

        // ---------- texts ----------
        tvHeaderSelected = findViewById(R.id.tvHeaderSelected);
        tvPlacesLeft = findViewById(R.id.tvPlacesLeft);

        tvPlacesLeft.setText("Выберите категории кэшбэка");

        // restore saved chips
        restoreSelectedCategoriesFromPrefs();
        updateSelectedCount();

        // add category button
        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showChooseCategorySheet());
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

        // load categories from resources
        allCategories.clear();
        filteredCategories.clear();

        String[] names = getResources().getStringArray(R.array.cashback_categories);
        for (String n : names) allCategories.add(new CashbackCategory(n));

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

        dialog.show();
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

        // ❌ УДАЛЕНО: ограничение максимум 5 категорий
        // if (!isEdit && selectedCategories.size() >= 5) { ... return; }

        new android.app.AlertDialog.Builder(this)
                .setTitle(categoryName)
                .setMessage(isEdit ? "Измените % кэшбэка" : "Укажите % кэшбэка для этой категории")
                .setView(et)
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (d, w) -> {
                    String raw = et.getText().toString().trim().replace(",", ".");
                    if (raw.isEmpty()) {
                        android.widget.Toast.makeText(this, "Введите процент", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double val = Double.parseDouble(raw);
                        if (val <= 0 || val > 100) {
                            android.widget.Toast.makeText(this, "Процент должен быть от 0 до 100", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        android.widget.Toast.makeText(this, "Некорректное значение", android.widget.Toast.LENGTH_SHORT).show();
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

        // edit on tap (оставил как было — удобно)
        chip.setOnClickListener(v -> {
            String current = selectedCategories.get(name);
            askCashbackPercent(name, current);
        });

        // menu (⋮)
        btnMenu.setOnClickListener(v -> showChipContextMenu(v, name, chip));

        selectedChipsContainer.addView(chip);
    }

    // =========================================================
    // Chip context menu (⋮)
    // =========================================================

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
            selectedCategories.remove(categoryName);
            selectedChipsContainer.removeView(chipView);
            updateSelectedCount();
            saveSelectedCategoriesToPrefs();
            return true;
        }

        // пока заглушки — чтобы не падало
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

    private void updateSelectedCount() {
        if (tvHeaderSelected == null) return;
        tvHeaderSelected.setText("Выбрано " + selectedCategories.size() + " категорий");
    }

    // =========================================================
    // Preferences (save/restore by card)
    // =========================================================

    private String getPrefsKeyForThisCard() {
        // ✅ основной стабильный ключ
        if (cardId != null && !cardId.trim().isEmpty()) {
            return "cashback_categories_card_" + cardId.trim();
        }

        // fallback (если вдруг card_id не передали)
        String safePs = (ps == null) ? "" : ps.trim();
        String safeLast4 = (last4 == null) ? "" : last4.trim();
        String safeName = (cardName == null) ? "" : cardName.trim();
        return "cashback_categories_" + safePs + "_" + safeLast4 + "_" + safeName;
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
        if (prefs == null) prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (selectedChipsContainer == null) return;

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
