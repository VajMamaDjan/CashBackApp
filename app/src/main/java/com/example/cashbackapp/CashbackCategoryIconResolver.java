package com.example.cashbackapp;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CashbackCategoryIconResolver {

    private CashbackCategoryIconResolver() {}

    private static Map<String, Integer> cache;

    private static void ensureLoaded(Context ctx) {
        if (cache != null) return;

        Map<String, Integer> map = new HashMap<>();
        Resources r = ctx.getResources();

        String[] names = r.getStringArray(R.array.cashback_categories);
        TypedArray icons = r.obtainTypedArray(R.array.cashback_category_icons);

        int n = names.length;
        int m = icons.length();

        // если вдруг массивы разной длины — берём минимальную, чтобы не падало
        int len = Math.min(n, m);

        for (int i = 0; i < len; i++) {
            String key = normalize(names[i]);
            int resId = icons.getResourceId(i, 0); // 0 = нет иконки
            if (!key.isEmpty() && resId != 0) {
                map.put(key, resId);
            }
        }

        icons.recycle();
        cache = map;
    }

    /** Нормализация: "Электроника", "электроника " -> "электроника" */
    private static String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /** Получить иконку по названию категории */
    public static int resolve(Context ctx, String categoryName) {
        ensureLoaded(ctx);
        Integer res = cache.get(normalize(categoryName));
        return (res != null) ? res : R.drawable.ic_cat_placeholder;
    }

    /** Если ты меняешь массивы на лету/в будущем — можно сбросить кэш */
    public static void invalidate() {
        cache = null;
    }
}
