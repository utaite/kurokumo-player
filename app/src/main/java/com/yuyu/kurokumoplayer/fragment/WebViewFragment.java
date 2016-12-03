package com.yuyu.kurokumoplayer.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.yuyu.kurokumoplayer.R;
import com.yuyu.kurokumoplayer.main.MainActivity;

public class WebViewFragment extends Fragment {
    public static WebView webView;
    private String url;
    private Toast mToast;
    private CheckTypesTask task;
    private boolean check;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.webview, container, false);
        url = getArguments().getString("url");
        check = getArguments().getBoolean("check");
        if (check) {
            task = new CheckTypesTask();
        }
        webView = (WebView) rootView.findViewById(R.id.webview);
        mToast = Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (task != null && check) {
                    task.onPreExecute();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (task != null && check) {
                    task.onPostExecute(null);
                    task = null;
                }
            }
        });
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected() || mobile.isConnected()) {
            webView.loadUrl(url);
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
        return rootView;
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);

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