package com.example.cashbackapp;

public class Category {
    private String name;
    private int iconResId;
    private int cashback;

    public Category(String name, int iconResId, int cashback) {
        this.name = name;
        this.iconResId = iconResId;
        this.cashback = cashback;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getCashback() {
        return cashback;
    }
}
