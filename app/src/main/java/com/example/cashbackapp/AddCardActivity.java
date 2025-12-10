package com.example.cashbackapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.annotation.Nullable;

public class AddCardActivity extends BaseActivity {

    // Предпросмотр карты
    private LinearLayout previewCard;
    private TextView tvBankNameSmall;
    private TextView tvPreviewName;
    private TextView tvPreviewLast4;

    // Поля ввода
    private EditText etCardName;
    private EditText etCardLast4;

    // Платёжные системы
    private TextView btnPsMir;
    private TextView btnPsVisa;
    private TextView btnPsMc;

    // Цвета
    private View colorPurple, colorBlue, colorGreen, colorOrange, colorBlack;
    private TextView btnPalette;

    // Кнопка "Добавить карту"
    private TextView btnAddCard;

    // Состояние
    private String selectedPaymentSystem = "MIR";
    private int selectedBaseColor = 0xFF8A3CFF;   // текущий базовый цвет карты

    private AlertDialog paletteDialog;

    @Override
    protected boolean useFullscreenStatusBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        // ---------- биндим вью ----------
        previewCard      = findViewById(R.id.previewCard);
        tvBankNameSmall  = findViewById(R.id.tvBankNameSmall);
        tvPreviewName    = findViewById(R.id.tvPreviewName);
        tvPreviewLast4   = findViewById(R.id.tvPreviewLast4);

        etCardName       = findViewById(R.id.etCardName);
        etCardLast4      = findViewById(R.id.etCardLast4);

        btnPsMir         = findViewById(R.id.btnPsMir);
        btnPsVisa        = findViewById(R.id.btnPsVisa);
        btnPsMc          = findViewById(R.id.btnPsMc);

        colorPurple      = findViewById(R.id.colorPurple);
        colorBlue        = findViewById(R.id.colorBlue);
        colorGreen       = findViewById(R.id.colorGreen);
        colorOrange      = findViewById(R.id.colorOrange);
        colorBlack       = findViewById(R.id.colorBlack);
        btnPalette       = findViewById(R.id.btnPalette);

        btnAddCard       = findViewById(R.id.btnAddCardConfirm);

        // Название банка под заголовком
        String bankName = getIntent().getStringExtra("bank_name");
        if (bankName == null) bankName = "Банк";
        tvBankNameSmall.setText(bankName);

        // Кнопка назад
        findViewById(R.id.btnBackAddCard).setOnClickListener(v -> onBackPressed());

        // ---------- платёжные системы ----------
        btnPsMir.setOnClickListener(v -> selectPaymentSystem(btnPsMir, "MIR"));
        btnPsVisa.setOnClickListener(v -> selectPaymentSystem(btnPsVisa, "VISA"));
        btnPsMc.setOnClickListener(v -> selectPaymentSystem(btnPsMc, "MC"));

        // дефолт — МИР
        selectPaymentSystem(btnPsMir, "MIR");

        // ---------- цвета ----------
        initColorChips();

        // начальный цвет карты: фиолетовый
        setCardColorFromBase(Color.parseColor("#8A3CFF"));

        // палитра
        btnPalette.setOnClickListener(v -> openPaletteDialog());

        // ---------- обновление предпросмотра при вводе ----------
        TextWatcher previewWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updatePreviewText();
            }
        };

        etCardName.addTextChangedListener(previewWatcher);
        etCardLast4.addTextChangedListener(previewWatcher);

        updatePreviewText();

        // ---------- кнопка "Добавить карту" ----------
        btnAddCard.setOnClickListener(v -> onAddCardClicked());
    }

    // ==================== ПЛАТЁЖНЫЕ СИСТЕМЫ ====================

    private void selectPaymentSystem(TextView selectedView, String code) {
        resetPaymentSystemStyle(btnPsMir);
        resetPaymentSystemStyle(btnPsVisa);
        resetPaymentSystemStyle(btnPsMc);

        selectedView.setBackgroundResource(R.drawable.bg_ps_selected);
        // фон выбранной системы светлый → текст делаем тёмным
        selectedView.setTextColor(Color.BLACK);

        selectedPaymentSystem = code;
        updatePreviewText();
    }

    private void resetPaymentSystemStyle(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_ps_unselected);
        tv.setTextColor(Color.BLACK);
    }

    // ==================== ЦВЕТА / ГРАДИЕНТЫ ====================

    private void initColorChips() {
        // каждому чипу в XML прописан android:tag="#HEX"
        setupChipGradient(colorPurple);
        setupChipGradient(colorBlue);
        setupChipGradient(colorGreen);
        setupChipGradient(colorOrange);
        setupChipGradient(colorBlack);

        View.OnClickListener chipClickListener = v -> {
            String hex = (String) v.getTag();
            if (hex != null) {
                int base = Color.parseColor(hex);
                setCardColorFromBase(base);
                // сброс оформления кнопки "Палитра"
                btnPalette.setBackgroundResource(R.drawable.bg_color_chip);
                btnPalette.setTextColor(Color.BLACK);
            }
        };

        colorPurple.setOnClickListener(chipClickListener);
        colorBlue.setOnClickListener(chipClickListener);
        colorGreen.setOnClickListener(chipClickListener);
        colorOrange.setOnClickListener(chipClickListener);
        colorBlack.setOnClickListener(chipClickListener);
    }

    /** Рисуем мини-градиент внутри квадратика */
    private void setupChipGradient(View chip) {
        String hex = (String) chip.getTag();
        if (hex == null) return;

        int base = Color.parseColor(hex);
        int darker = darken(base, 0.75f);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{darker, base});
        gd.setCornerRadius(dp(12));
        chip.setBackground(gd);
    }

    /** Устанавливаем цвет карты по базовому цвету (делаем из него градиент) */
    private void setCardColorFromBase(int base) {
        selectedBaseColor = base;

        int darker = darken(base, 0.75f);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{darker, base});
        gd.setCornerRadius(dp(16));
        previewCard.setBackground(gd);
    }

    /** Затемнить цвет на factor */
    private int darken(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }

    // ---------- Палитра с готовыми цветами ----------

    private void openPaletteDialog() {
        // Небольшой набор пресетов
        final int[] palette = new int[]{
                Color.parseColor("#8A3CFF"),
                Color.parseColor("#2563EB"),
                Color.parseColor("#16A34A"),
                Color.parseColor("#F97316"),
                Color.parseColor("#111827"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#EC4899"),
                Color.parseColor("#3B82F6"),
                Color.parseColor("#22C55E"),
                Color.parseColor("#F43F5E"),
                Color.parseColor("#0EA5E9"),
                Color.parseColor("#A855F7")
        };

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dpPx(12);
        root.setPadding(pad, pad, pad, pad);

        int perRow = 4;
        LinearLayout row = null;

        for (int i = 0; i < palette.length; i++) {
            if (i % perRow == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dpPx(4), 0, dpPx(4));
                root.addView(row);
            }

            final int color = palette[i];

            View swatch = new View(this);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, dpPx(40), 1f);
            lp.setMarginStart(dpPx(4));
            lp.setMarginEnd(dpPx(4));
            swatch.setLayoutParams(lp);

            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{darken(color, 0.75f), color});
            gd.setCornerRadius(dp(12));
            swatch.setBackground(gd);

            swatch.setOnClickListener(v -> {
                setCardColorFromBase(color);

                // подсветим саму кнопку "Палитра" этим цветом
                GradientDrawable bg = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{darken(color, 0.75f), color});
                bg.setCornerRadius(dp(12));
                btnPalette.setBackground(bg);
                btnPalette.setTextColor(Color.WHITE);

                if (paletteDialog != null) {
                    paletteDialog.dismiss();
                }
            });

            row.addView(swatch);
        }

        paletteDialog = new AlertDialog.Builder(this)
                .setTitle("Выберите цвет карты")
                .setView(root)
                .setNegativeButton("Отмена", null)
                .create();

        paletteDialog.show();
    }

    // ==================== ПРЕДПРОСМОТР ТЕКСТА ====================

    private void updatePreviewText() {
        String name = etCardName.getText().toString().trim();
        String last4 = etCardLast4.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tvPreviewName.setText("НАЗВАНИЕ КАРТЫ");
        } else {
            tvPreviewName.setText(name);
        }

        if (last4.length() == 4 && TextUtils.isDigitsOnly(last4)) {
            tvPreviewLast4.setText("•••• •••• •••• " + last4);
        } else {
            tvPreviewLast4.setText("•••• •••• •••• 0000");
        }
    }

    // ==================== СОХРАНЕНИЕ РЕЗУЛЬТАТА ====================

    private void onAddCardClicked() {
        String name = etCardName.getText().toString().trim();
        String last4 = etCardLast4.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            name = "Моя карта";
        }

        if (last4.length() != 4 || !TextUtils.isDigitsOnly(last4)) {
            android.widget.Toast.makeText(this, "Введите последние 4 цифры карты", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra("card_name", name);
        data.putExtra("card_last4", last4);
        data.putExtra("card_ps", selectedPaymentSystem);
        data.putExtra("card_color", selectedBaseColor);

        setResult(RESULT_OK, data);
        finish();
    }

    // ==================== УТИЛИТЫ ====================

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private int dpPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
