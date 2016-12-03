package com.yuyu.kurokumoplayer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuyu.kurokumoplayer.R;

import java.util.ArrayList;

public class ExpandableAdapter extends BaseExpandableListAdapter {
    public static int close;
    private Context context;
    private ArrayList<GroupView> groupList;
    private ArrayList<GroupView> searchList;
    private static final char HANGUL_BEGIN_UNICODE = 44032;
    private static final char HANGUL_LAST_UNICODE = 55203;
    private static final char HANGUL_BASE_UNIT = 588;
    private static final char[] INITIAL_SOUND = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};

    public ExpandableAdapter(Context context, ArrayList<GroupView> groupList) {
        this.context = context;
        this.groupList = new ArrayList<>();
        this.groupList.addAll(groupList);
        this.searchList = new ArrayList<>();
        this.searchList.addAll(groupList);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildView> childList = groupList.get(groupPosition).getChildList();
        return childList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildView child = (ChildView) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_row, null);
        }
        TextView childName1 = (TextView) view.findViewById(R.id.childName1);
        TextView childName2 = (TextView) view.findViewById(R.id.childName2);
        childName1.setText(child.getChildName1().trim());
        childName2.setText(child.getChildName2().trim());
        view.setTag(groupList.get(groupPosition));
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ChildView> childList = groupList.get(groupPosition).getChildList();
        return childList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {
        GroupView group = (GroupView) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_row, null);
        }
        ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView groupName1 = (TextView) view.findViewById(R.id.groupName1);
        TextView groupName2 = (TextView) view.findViewById(R.id.groupName2);
        image.setImageResource(group.getImage());
        groupName1.setText(group.getGroupName1().trim());
        groupName2.setText(group.getGroupName2().trim());
        groupName1.setHorizontallyScrolling(true);
        groupName1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        groupName1.setSelected(true);
        groupName1.setSingleLine(true);
        groupName2.setHorizontallyScrolling(true);
        groupName2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        groupName2.setSelected(true);
        groupName2.setSingleLine(true);
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void filterData(String query) {
        query = query.toLowerCase();
        groupList.clear();
        if (query.isEmpty()) {
            groupList.clear();
            groupList.addAll(searchList);
        } else {
            close = 0;
            for (GroupView group : searchList) {
                if (matchString(group.getGroupName1().toLowerCase(), query) ||
                        matchString(group.getGroupName2().toLowerCase(), query)) {
                    close += 1;
                    groupList.add(group);
                }
            }
        }
        notifyDataSetChanged();
    }

    private static boolean isInitialSound(char searchar) {
        for (char c : INITIAL_SOUND) {
            if (c == searchar) {
                return true;
            }
        }
        return false;
    }

    private static char getInitialSound(char c) {
        int hanBegin = (c - HANGUL_BEGIN_UNICODE);
        int index = hanBegin / HANGUL_BASE_UNIT;
        return INITIAL_SOUND[index];
    }

    private static boolean isHangul(char c) {
        return HANGUL_BEGIN_UNICODE <= c && c <= HANGUL_LAST_UNICODE;
    }

    public static boolean matchString(String value, String search) {
        int t;
        int seof = value.length() - search.length();
        int slen = search.length();
        if (seof < 0)
            return false;
        for (int i = 0; i <= seof; i++) {
            t = 0;
            while (t < slen) {
                if (isInitialSound(search.charAt(t)) == true && isHangul(value.charAt(i + t))) {
                    if (getInitialSound(value.charAt(i + t)) == search.charAt(t))
                        t++;
                    else
                        break;
                } else {
                    if (value.charAt(i + t) == search.charAt(t))
                        t++;
                    else
                        break;
                }
            }
            if (t == slen)
                return true;
        }
        return false;
    }
}