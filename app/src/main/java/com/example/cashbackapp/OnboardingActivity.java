package com.example.cashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private Button buttonStart;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Инициализация всех View
        viewPager = findViewById(R.id.viewPager);
        buttonStart = findViewById(R.id.buttonStart);
        rootLayout = findViewById(R.id.rootLayout);

        // Настройка адаптера
        adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        // Скрыть кнопку на первых двух экранах
        buttonStart.setVisibility(View.GONE);

        // Обработчик перелистывания страниц
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Показываем кнопку только на последнем экране
                if (position == 2) {
                    buttonStart.setVisibility(View.VISIBLE);
                } else {
                    buttonStart.setVisibility(View.GONE);
                }
            }
        });

        // Обработчик нажатия на кнопку "Начать"
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем интент для перехода к CategorySelectionActivity
                Intent intent = new Intent(OnboardingActivity.this, CategorySelectionActivity.class);
                startActivity(intent);
                finish(); // Закрываем onboarding после перехода
            }
        });

        // Обработчик касаний ТОЛЬКО для rootLayout (не для кнопки)
        setupTouchListener();
    }

    private void setupTouchListener() {
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Проверяем, что касание НЕ на кнопке "Начать"
                if (buttonStart.getVisibility() == View.VISIBLE &&
                        isPointInsideView(event.getRawX(), event.getRawY(), buttonStart)) {
                    return false; // Позволяем кнопке обработать нажатие
                }

                // Обрабатываем только событие поднятия пальца (тап)
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handleTap(event.getX());
                    return true;
                }
                return false;
            }
        });
    }

    // Проверяем, находится ли точка внутри View
    private boolean isPointInsideView(float rawX, float rawY, View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }

        // Получаем координаты view на экране
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int viewX = location[0];
        int viewY = location[1];
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        // Проверяем, попадает ли точка в границы view
        return (rawX >= viewX && rawX <= (viewX + viewWidth) &&
                rawY >= viewY && rawY <= (viewY + viewHeight));
    }

    private void handleTap(float x) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int currentItem = viewPager.getCurrentItem();

        if (x < screenWidth / 2) {
            // Левая часть экрана - предыдущая страница
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true);
            }
        } else {
            // Правая часть экрана - следующая страница
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1, true);
            }
        }
    }

    // Обрабатываем касания на всем активити
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Если кнопка видна и касание в области кнопки - пропускаем событие
        if (buttonStart.getVisibility() == View.VISIBLE &&
                isPointInsideView(ev.getRawX(), ev.getRawY(), buttonStart)) {
            return super.dispatchTouchEvent(ev);
        }

        // Для остальных случаев обрабатываем жесты
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            handleTap(ev.getX());
        }

        return super.dispatchTouchEvent(ev);
    }
}