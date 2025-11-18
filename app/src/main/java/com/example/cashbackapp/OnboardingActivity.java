package com.example.cashbackapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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
    private LinearLayout dotsContainer;

    // Переменные для обработки касаний
    private float startX = 0;
    private boolean isProcessingTouch = false;

    // Цвета для точек
    private final int DOT_ACTIVE_COLOR = 0xFF2196F3;   // Синий
    private final int DOT_INACTIVE_COLOR = 0xFFE0E0E0; // Светло-серый

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Инициализация всех View
        viewPager = findViewById(R.id.viewPager);
        buttonStart = findViewById(R.id.buttonStart);
        rootLayout = findViewById(R.id.rootLayout);
        dotsContainer = findViewById(R.id.dotsContainer);

        // Настройка адаптера
        adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        // Создаем индикаторы точек
        createDotsIndicator();

        // Скрыть кнопку на первых двух экранах
        buttonStart.setVisibility(View.GONE);

        // Обработчик перелистывания страниц
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Обновляем индикаторы точек
                updateDotsIndicator(position);

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

        // Обработчик касаний с улучшенной логикой
        setupImprovedTouchListener();
    }

    // Создаем индикаторы точек
    private void createDotsIndicator() {
        dotsContainer.removeAllViews(); // Очищаем контейнер

        for (int i = 0; i < adapter.getItemCount(); i++) {
            // Создаем View для точки
            View dot = new View(this);

            // Создаем круглую форму
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(dpToPx(8), dpToPx(8)); // Размер 8dp
            drawable.setColor(DOT_INACTIVE_COLOR); // Начальный цвет - неактивный

            // Устанавливаем фон
            dot.setBackground(drawable);

            // Параметры layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8),
                    dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0); // Отступы 4dp слева и справа

            dot.setLayoutParams(params);

            // Добавляем точку в контейнер
            dotsContainer.addView(dot);
        }

        // Устанавливаем первую точку как активную
        updateDotsIndicator(0);
    }

    // Обновляем индикаторы точек
    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) dot.getBackground();

            if (i == position) {
                // Активная точка
                drawable.setColor(DOT_ACTIVE_COLOR);
            } else {
                // Неактивная точка
                drawable.setColor(DOT_INACTIVE_COLOR);
            }
        }
    }

    // Улучшенный обработчик касаний
    private void setupImprovedTouchListener() {
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Проверяем, что касание НЕ на кнопке "Начать"
                if (buttonStart.getVisibility() == View.VISIBLE &&
                        isPointInsideView(event.getRawX(), event.getRawY(), buttonStart)) {
                    return false; // Позволяем кнопке обработать нажатие
                }

                // Проверяем, что касание НЕ на индикаторах точек
                if (isPointInsideView(event.getRawX(), event.getRawY(), dotsContainer)) {
                    return false; // Позволяем индикаторам оставаться не кликабельными
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Запоминаем начальную позицию касания
                        startX = event.getX();
                        isProcessingTouch = false;
                        return true;

                    case MotionEvent.ACTION_UP:
                        // Обрабатываем только если еще не обработали это касание
                        if (!isProcessingTouch) {
                            float endX = event.getX();
                            handleSwipe(startX, endX);
                            isProcessingTouch = true;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    // Обработчик свайпа с улучшенной логикой
    private void handleSwipe(float startX, float endX) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int currentItem = viewPager.getCurrentItem();

        // Определяем зоны экрана более точно
        float leftZone = screenWidth * 0.3f;    // Левая 30% экрана
        float rightZone = screenWidth * 0.7f;   // Правая 30% экрана

        // Минимальная дистанция для срабатывания (чтобы избежать случайных касаний)
        float minSwipeDistance = screenWidth * 0.1f; // 10% ширины экрана
        float swipeDistance = Math.abs(endX - startX);

        // Если дистанция слишком маленькая - игнорируем
        if (swipeDistance < minSwipeDistance) {
            return;
        }

        if (startX < leftZone && endX < startX) {
            // Свайп из левой части влево - предыдущая страница
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true);
            }
        } else if (startX > rightZone && endX > startX) {
            // Свайп из правой части вправо - следующая страница
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1, true);
            }
        } else {
            // Простое определение по конечной позиции
            float tapX = endX;
            if (tapX < screenWidth / 2) {
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
    }

    // Конвертируем dp в пиксели
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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

    // Обрабатываем касания на всем активити
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Если кнопка видна и касание в области кнопки - пропускаем событие
        if (buttonStart.getVisibility() == View.VISIBLE &&
                isPointInsideView(ev.getRawX(), ev.getRawY(), buttonStart)) {
            return super.dispatchTouchEvent(ev);
        }

        // Если касание в области индикаторов - пропускаем событие
        if (isPointInsideView(ev.getRawX(), ev.getRawY(), dotsContainer)) {
            return super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }
}