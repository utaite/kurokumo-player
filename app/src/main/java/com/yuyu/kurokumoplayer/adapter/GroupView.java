package com.yuyu.kurokumoplayer.adapter;

import java.util.ArrayList;

public class GroupView {
    private int id;
    private int image;
    private String groupName1;
    private String groupName2;
    private ArrayList<ChildView> childList = new ArrayList<>();

    public GroupView(int id, int image, String groupName1, String groupName2, ArrayList<ChildView> childList) {
        this.id = id;
        this.image = image;
        this.groupName1 = groupName1;
        this.groupName2 = groupName2;
        this.childList = childList;
    }

    public int getImage() {
        return image;
    }

    public int getId() {
        return id;
    }

    public String getGroupName1() {
        return groupName1;
    }

    public String getGroupName2() {
        return groupName2;
    }

    public ArrayList<ChildView> getChildList() {
        return childList;
    }

}