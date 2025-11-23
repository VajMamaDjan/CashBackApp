package com.example.cashbackapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLightStatusBar();
    }

    // по умолчанию все экраны — fullscreen
    protected boolean useFullscreenStatusBar() {
        return true;
    }

    protected void setupLightStatusBar() {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = window.getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            if (useFullscreenStatusBar()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }

            decor.setSystemUiVisibility(flags);
        }
    }
}

