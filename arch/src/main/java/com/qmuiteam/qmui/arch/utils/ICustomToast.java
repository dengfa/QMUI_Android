package com.qmuiteam.qmui.arch.utils;

public interface ICustomToast {

    int LENGTH_SHORT = 2000;
    int LENGTH_LONG = 3500;
    int LENGTH_WITH_ICON = 1500;

    void showCustomToast(String text);

    void showCustomLongToast(int iconId, String text);

    void showCustomToast(String text, int duration, int gravity);

    void showCustomToast(int iconId, String text);

    void showCustomToast(int iconId, String text, int duration, int gravity);

    void dismissCustomToast();
}
