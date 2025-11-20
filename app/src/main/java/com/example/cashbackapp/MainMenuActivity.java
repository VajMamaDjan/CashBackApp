package com.example.cashbackapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity {

    private CardView cardAddCategory;
    private CardView cardCurrentMonth;
    private CardView cardPreviousMonth;
    private CardView cardTip;

    private TextView tvCurrentMonthSubtitle;
    private TextView tvPreviousMonthSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initViews();
        setupMonthTexts();
        setupClicks();
    }

    private void initViews() {
        cardAddCategory = findViewById(R.id.cardAddCategory);
        cardCurrentMonth = findViewById(R.id.cardCurrentMonth);
        cardPreviousMonth = findViewById(R.id.cardPreviousMonth);
        cardTip = findViewById(R.id.cardTip);

        tvCurrentMonthSubtitle = findViewById(R.id.tvCurrentMonthSubtitle);
        tvPreviousMonthSubtitle = findViewById(R.id.tvPreviousMonthSubtitle);
    }

    private void setupMonthTexts() {
        Locale ru = new Locale("ru");
        SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy 'г.'", ru);

        Calendar now = Calendar.getInstance();
        String currentMonth = sdf.format(now.getTime());

        Calendar prev = (Calendar) now.clone();
        prev.add(Calendar.MONTH, -1);
        String previousMonth = sdf.format(prev.getTime());

        tvCurrentMonthSubtitle.setText(currentMonth);
        tvPreviousMonthSubtitle.setText(previousMonth);
    }

    private void setupClicks() {
        // Добавить категорию
        cardAddCategory.setOnClickListener(v -> {
            // TODO: открыть экран добавления категории
        });

        // Текущий месяц
        cardCurrentMonth.setOnClickListener(v -> {
            // TODO: открыть экран категорий текущего месяца
        });

        // Предыдущий месяц
        cardPreviousMonth.setOnClickListener(v -> {
            // TODO: открыть экран категорий предыдущего месяца
        });

        // Совет
        cardTip.setOnClickListener(v -> {
            // Можно показать тост/диалог с подсказкой
            // Toast.makeText(this, "Проверяйте категории раз в месяц :)", Toast.LENGTH_SHORT).show();
        });
    }
}
