package com.example.lockprakashji;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String name;
    private String packageName;
    private Drawable icon;
    private boolean isSelected;

    public AppInfo(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isSelected = false; // Default to not selected
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
} 