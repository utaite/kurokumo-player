package com.yuyu.kurokumoplayer.adapter;

public class DrawerView {
    private int icon;
    private String drawerName;

    public DrawerView(int icon, String drawerName) {
        this.icon = icon;
        this.drawerName = drawerName;
    }

    public int getIcon() {
        return icon;
    }

    public String getDrawerName() {
        return drawerName;
    }

}