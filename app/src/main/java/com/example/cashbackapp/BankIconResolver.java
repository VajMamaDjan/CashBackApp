package com.example.cashbackapp;

import java.util.Locale;

/**
 * Единственный источник правды по логотипам банков.
 *
 * Как добавлять новый банк:
 * 1) Добавь иконку в res/drawable (png или vector xml)
 * 2) Добавь правило в normalize(...) и/или case в resolveLogoRes(...)
 */
public final class BankIconResolver {

    private BankIconResolver() {}

    /**
     * Нормализует любое название/вариант написания в стабильный алиас.
     * Это удобно, если позже захочешь хранить алиасы в prefs.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);

        // Сбер
        if (s.contains("сбер")) return "sber";

        // Т-Банк / Тинькофф
        if (s.contains("т-банк") || s.contains("тбанк") || s.contains("тиньк") || s.contains("tinkoff")) {
            return "tbank";
        }

        // ВТБ
        if (s.equals("втб") || s.contains(" втб") || s.contains("vtb")) return "vtb";

        // Альфа
        if (s.contains("альфа") || s.contains("alf")) return "alfa";

        // Райффайзен
        if (s.contains("райфф") || s.contains("raiff")) return "raiffeisen";

        // Оставляем как есть (если это уже алиас)
        return s;
    }

    /**
     * Возвращает drawable-ресурс логотипа банка.
     * По умолчанию возвращает плейсхолдер.
     */
    public static int resolveLogoRes(String bankNameOrAlias) {
        String key = normalize(bankNameOrAlias);

        switch (key) {
            case "sber":
                return R.drawable.sber_logo;
            case "tbank":
                return R.drawable.ic_tbank;
            case "vtb":
                return R.drawable.ic_vtb;
            case "alfa":
                return R.drawable.ic_alfabank;
            // case "raiffeisen": return R.drawable.ic_raiffeisen;

            default:
                return R.drawable.ic_bank_placeholder;
        }
    }
}
