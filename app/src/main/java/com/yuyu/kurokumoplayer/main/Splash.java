package com.yuyu.kurokumoplayer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.yuyu.kurokumoplayer.R;

public class Splash extends Activity {

    // App 실행 시 가장 처음 실행되는 Activity
    // No ActionBar로 Setting하여 검정 배경을 띄운 후,
    // Main Activity로 이동했다가 바로 Loading Activity로 이동
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("intent", getIntent().getIntExtra("intent", -1)));
        finish();
    }
}