package com.example.cashbackapp;

import android.content.SharedPreferences;

public class UserManager {

    public static boolean isUserRegistered(SharedPreferences prefs) {
        return prefs.getBoolean("user_registered", false);
    }

    public static String getUserName(SharedPreferences prefs) {
        return prefs.getString("user_name", "Пользователь");
    }

    public static String getUserPassword(SharedPreferences prefs) {
        return prefs.getString("user_password", "");
    }

    public static String getUserId(SharedPreferences prefs) {
        return prefs.getString("user_id", "");
    }

    public static boolean validatePassword(SharedPreferences prefs, String inputPassword) {
        String savedPassword = prefs.getString("user_password", "");
        return savedPassword.equals(inputPassword);
    }
}