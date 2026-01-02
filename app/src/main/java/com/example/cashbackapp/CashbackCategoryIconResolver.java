package com.example.cashbackapp;

import android.content.Context;

import androidx.annotation.DrawableRes;

import java.util.HashMap;
import java.util.Map;

public class CashbackCategoryIconResolver {

    private static final Map<String, Integer> ICON_MAP = new HashMap<>();

    static {
        // ===== АВТО =====
        ICON_MAP.put("Аптеки", R.drawable.apteka);
    }

    /**
     * Получить иконку категории по названию
     */
    @DrawableRes
    public static int getIcon(Context context, String categoryName) {
        if (categoryName == null) {
            return R.drawable.ic_cat_placeholder;
        }

        Integer icon = ICON_MAP.get(categoryName);
        if (icon != null) {
            return icon;
        }

        return R.drawable.ic_cat_placeholder;
    }
}
