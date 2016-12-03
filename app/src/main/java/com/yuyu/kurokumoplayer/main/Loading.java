package com.yuyu.kurokumoplayer.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.yuyu.kurokumoplayer.R;

public class Loading extends Activity {

    public static int last;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        // last 라는 값을 불러옴
        // 첫 시작 시 last가 0이므로 밑의 else if 문이 실행되고,
        // 그 후로는 last가 1의 값을 받아오므로 밑의 if문이 실행됨
        // 즉, else if문(last 0)은 처음 단 한 번만 실행되는 구문

        // last를 2로 대입한 이유는 시작 시 MainActivity에서 호출할 때 처음 한 번만 실행되고,
        // 그 후로 MainActivity를 재시작해도 더이상 호출되지 않게 만들었기 때문
        SharedPreferences las = getSharedPreferences("last", MODE_PRIVATE);
        last = las.getInt("last", 0);
        if (last == 1) {
            last = 2;
            loading();
        } else if (last == 0) {
            ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isConnected() || mobile.isConnected()) {
                last = 2;
                SharedPreferences.Editor editRep = las.edit();
                editRep.putInt("last", 1);
                editRep.commit();
                new Thread() {
                    // DBHelper Class의 finish가 4가 될 때 까지 계속 대기 후 MainActivity로 넘어감
                    // -> finish는 DB의 view 값을 모두 Update 하게되면 4까지 증가함
                    @Override
                    public void run() {
                        while (MusicDB.finish <= 4) {
                            if (MusicDB.finish >= 4) {
                                MusicDB.finish = 5;
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }.start();
            } else {
                // 처음 단 한 번 실행될 때, HTML의 값을 Parsing해야 되는데
                // Network 연결이 처음부터 안되는 경우 팅기게 구현
                Toast.makeText(getApplicationContext(), "Please restart an app,\nafter connecting with network.", Toast.LENGTH_LONG).show();
                loadingException();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Back Key를 눌러 Activity를 종료할 수 없게 super Method를 호출하지 않음
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void loading() {
        // 1초의 대기 후 MainActivity로 넘어감
        // - 평범한 Loading 화면
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    public void loadingException() {
        // Network 연결이 처음부터 안되는 경우, 5초의 대기 후 모든 Activity를 닫고 App을 종료
        Handler handler = new Handler() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void handleMessage(Message msg) {
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        };
        handler.sendEmptyMessageDelayed(0, 5000);
    }
}