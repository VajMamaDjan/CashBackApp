package com.example.cashbackapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private LinearLayout dotsContainer;

    private float startX = 0;
    private boolean isProcessingTouch = false;

    private final int DOT_ACTIVE_COLOR = 0xFF2196F3;
    private final int DOT_INACTIVE_COLOR = 0xFFE0E0E0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Инициализация всех View
        viewPager = findViewById(R.id.viewPager);
        buttonStart = findViewById(R.id.buttonStart);
        dotsContainer = findViewById(R.id.dotsContainer);

        // Настройка адаптера
        adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        // 1. Создаем индикаторы точек ПЕРВЫМ ДЕЛОМ
        createDotsIndicator();

        // 2. Проверяем, нужно ли открыть конкретный экран
        int screenPosition = getIntent().getIntExtra("screen_position", -1);
        if (screenPosition != -1 && screenPosition < adapter.getItemCount()) {
            // Переходим на указанный экран (2 = третий экран)
            viewPager.setCurrentItem(screenPosition, false);
            // Обновляем состояние кнопки и точек
            updateButtonVisibility(screenPosition);
            updateDotsIndicator(screenPosition);
        }

        // 3. Настраиваем обработчики
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDotsIndicator(position);
                updateButtonVisibility(position);
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                boolean isRegistered = prefs.getBoolean("user_registered", false);

                if (isRegistered) {
                    startActivity(new Intent(OnboardingActivity.this, MainMenuActivity.class));
                } else {
                    startActivity(new Intent(OnboardingActivity.this, SimpleRegistrationActivity.class));
                }
                finish();
            }
        });

        setupImprovedTouchListener();
    }

    private void updateButtonVisibility(int position) {
        if (position == 2) {
            buttonStart.setVisibility(View.VISIBLE);
        } else {
            buttonStart.setVisibility(View.GONE);
        }
    }

    private void createDotsIndicator() {
        dotsContainer.removeAllViews();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            View dot = new View(this);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(dpToPx(8), dpToPx(8));

            // Устанавливаем начальный цвет
            drawable.setColor(DOT_INACTIVE_COLOR);

            dot.setBackground(drawable);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(8),
                    dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);

            dot.setLayoutParams(params);
            dotsContainer.addView(dot);
        }
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) dot.getBackground();
            drawable.setColor(i == position ? DOT_ACTIVE_COLOR : DOT_INACTIVE_COLOR);
        }
    }

    // ... остальные методы без изменений
    private void setupImprovedTouchListener() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (buttonStart.getVisibility() == View.VISIBLE &&
                        isPointInsideView(event.getRawX(), event.getRawY(), buttonStart)) {
                    return false;
                }

                if (isPointInsideView(event.getRawX(), event.getRawY(), dotsContainer)) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        isProcessingTouch = false;
                        return true;

                    case MotionEvent.ACTION_UP:
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

    private void handleSwipe(float startX, float endX) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int currentItem = viewPager.getCurrentItem();

        float leftZone = screenWidth * 0.3f;
        float rightZone = screenWidth * 0.7f;
        float minSwipeDistance = screenWidth * 0.1f;
        float swipeDistance = Math.abs(endX - startX);

        if (swipeDistance < minSwipeDistance) {
            return;
        }

        if (startX < leftZone && endX < startX) {
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true);
            }
        } else if (startX > rightZone && endX > startX) {
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1, true);
            }
        } else {
            float tapX = endX;
            if (tapX < screenWidth / 2) {
                if (currentItem > 0) {
                    viewPager.setCurrentItem(currentItem - 1, true);
                }
            } else {
                if (currentItem < adapter.getItemCount() - 1) {
                    viewPager.setCurrentItem(currentItem + 1, true);
                }
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private boolean isPointInsideView(float rawX, float rawY, View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int viewX = location[0];
        int viewY = location[1];
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        return (rawX >= viewX && rawX <= (viewX + viewWidth) &&
                rawY >= viewY && rawY <= (viewY + viewHeight));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (buttonStart.getVisibility() == View.VISIBLE &&
                isPointInsideView(ev.getRawX(), ev.getRawY(), buttonStart)) {
            return super.dispatchTouchEvent(ev);
        }

        if (isPointInsideView(ev.getRawX(), ev.getRawY(), dotsContainer)) {
            return super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }
}