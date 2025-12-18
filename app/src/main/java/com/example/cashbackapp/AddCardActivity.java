package com.example.cashbackapp;

import android.app.AlertDialog;
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
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

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

    private android.widget.RadioGroup rgCashbackUnit;
    private android.widget.RadioButton rbUnitRub, rbUnitMiles;
    private String selectedCashbackUnit = "RUB"; // "RUB" или "MILES"

    // Состояние
    private String selectedPaymentSystem = "MIR";
    private int selectedBaseColor = Color.parseColor("#8A3CFF");   // текущий базовый цвет карты

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

        rgCashbackUnit = findViewById(R.id.rgCashbackUnit);
        rbUnitRub = findViewById(R.id.rbUnitRub);
        rbUnitMiles = findViewById(R.id.rbUnitMiles);

        // выбор рубли/мили
        rgCashbackUnit.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbUnitMiles) {
                selectedCashbackUnit = "MILES";
            } else {
                selectedCashbackUnit = "RUB";
            }
        });

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

        // ---------- цвета (градиентные чипы) ----------
        initColorChips();

        // стартовый цвет карты: фиолетовый
        setCardColorFromBase(selectedBaseColor);

        // палитра с R/G/B/Hex
        btnPalette.setOnClickListener(v -> openColorPickerDialog());

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

    /** Рисуем мини-градиент внутри цветного квадратика */
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

    // ---------- Палитра формата R/G/B + Hex ----------

    private void openColorPickerDialog() {
        new ColorPickerDialog.Builder(this)
                .setTitle("Выбор цвета карты")
                .setPreferenceName("CardColorPicker") // опционально, для запоминания
                .attachBrightnessSlideBar(true)       // ползунок яркости
                .attachAlphaSlideBar(false)           // прозрачность нам не нужна
                .setPositiveButton("Готово", new ColorEnvelopeListener() {
                    @Override
                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                        int color = envelope.getColor();

                        // ставим цвет карты (градиент как раньше)
                        setCardColorFromBase(color);

                        // подсветить кнопку "Палитра" этим же градиентом
                        int darker = darken(color, 0.75f);
                        GradientDrawable bg = new GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                new int[]{darker, color});
                        bg.setCornerRadius(dp(12));
                        btnPalette.setBackground(bg);
                        btnPalette.setTextColor(Color.WHITE);
                    }
                })
                .setNegativeButton("Отмена", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
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
        data.putExtra("card_cashback_unit", selectedCashbackUnit);
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
}
