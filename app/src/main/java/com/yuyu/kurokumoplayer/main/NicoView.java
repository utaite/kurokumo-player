package com.yuyu.kurokumoplayer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.yuyu.kurokumoplayer.R;

import java.lang.reflect.InvocationTargetException;

public class NicoView extends Activity {

    // Main Activity에서 Expandablelistview 첫 번째 Child를 클릭했을 경우 Intent 되는 Activity

    private WebView webView;
    private long backpress;
    private Toast mToast;
    private CheckTypesTask task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        webView = (WebView) findViewById(R.id.webview);
        task = new CheckTypesTask();
        if(mToast == null) {
            mToast = Toast.makeText(this, "null", Toast.LENGTH_SHORT);
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (task != null) {
                    task.onPreExecute();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (task != null) {
                    task.onPostExecute(null);
                    task = null;
                }
            }
        });
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected() || mobile.isConnected()) {
            webView.loadUrl(getIntent().getStringExtra("nico"));
        } else {
            if (MainActivity.lang == MainActivity.LANG_KO) {
                mToast.setText("인터넷 접속에 실패하였습니다.");
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                mToast.setText("ネットの繋がりが失敗しました。");
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                mToast.setText("Internet Connection Fail.");
            }
            mToast.show();
        }
    }

    // Webview에서 뒷 페이지가 있을 경우 종료하지 않고 뒤로 가기 기능
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_BACK) && System.currentTimeMillis() > backpress + 2000) {
            backpress = System.currentTimeMillis();
            if (MainActivity.lang == MainActivity.LANG_KO) {
                mToast.setText("\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.");
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                mToast.setText("\'Back\' ボタンをもう一度押しと終了になります。");
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                mToast.setText("Press \'Back\' again to exit.");
            }
            mToast.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Activity가 멈출 경우 현재 Webview에서 재생되는 노래도 같이 정지시킴
    @Override
    public void onStop() {
        super.onPause();
        if (webView != null) {
            try {
                Class.forName("android.webkit.WebView")
                        .getMethod("onPause", (Class[]) null)
                        .invoke(webView, (Object[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(NicoView.this, AlertDialog.THEME_HOLO_LIGHT);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (MainActivity.lang == MainActivity.LANG_KO) {
                asyncDialog.setMessage("로딩중입니다 ...");
            } else if (MainActivity.lang == MainActivity.LANG_JA) {
                asyncDialog.setMessage("ロード中です 。。。");
            } else if (MainActivity.lang == MainActivity.LANG_EN) {
                asyncDialog.setMessage("Loading ...");
            }
            asyncDialog.show();
            asyncDialog.setCancelable(false);
            asyncDialog.setCanceledOnTouchOutside(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }
}