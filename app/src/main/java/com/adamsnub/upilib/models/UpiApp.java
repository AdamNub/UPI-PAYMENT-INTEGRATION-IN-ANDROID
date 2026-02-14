package com.adamsnub.upilib.models;

import android.graphics.drawable.Drawable;

public class UpiApp {
    private String name;
    private String packageName;
    private Drawable icon;
    private boolean isPreferred;

    public UpiApp(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isPreferred = false;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isPreferred() {
        return isPreferred;
    }

    public void setPreferred(boolean preferred) {
        isPreferred = preferred;
    }
}