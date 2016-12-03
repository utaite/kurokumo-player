package com.yuyu.kurokumoplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuyu.kurokumoplayer.R;

public class DrawerAdapter extends ArrayAdapter<DrawerView> {
    private Context context;
    private int layoutId;
    private DrawerView data[];

    public DrawerAdapter(Context context, int layoutId, DrawerView[] data) {
        super(context, layoutId, data);
        this.layoutId = layoutId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutId, parent, false);
        ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);
        TextView textViewName = (TextView) convertView.findViewById(R.id.textViewName);
        DrawerView folder = data[position];
        imageViewIcon.setImageResource(folder.getIcon());
        textViewName.setText(folder.getDrawerName());
        return convertView;
    }
}