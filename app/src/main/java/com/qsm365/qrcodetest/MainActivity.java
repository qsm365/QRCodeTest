package com.qsm365.qrcodetest;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    private WebView wv;
    //初始化加载的页面，这个页面需要判断是否已登录，如果已登录则跳转
    private String INITPAGE = "file:///android_asset/index.html";
    //出错页面，如网络不通，因此需要使用本地文件
    private String ERROR_PAGE = "file:///android_asset/404.html";
    //用于存放下载文件的前置路径
    private String DOWNLOAD_PATH = "http://someweburl/download-path/";
    //Home页面，一般是登录完的页面，用户在这些页面时，再按返回键，就会提示退出app，而不是webview的返回上一页
    private List<String> HOMEPAGE = new ArrayList<>();


    private void init(){
        HOMEPAGE.add(INITPAGE);
        HOMEPAGE.add("file:///android_asset/index.html#");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wv = (WebView)findViewById(R.id.webview);
        init();

        CookieManager cm = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        cm.setAcceptCookie(true);

        wv.setWebViewClient(new webViewClient());
        wv.setWebChromeClient(new webViewChromeClient());

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);

        class JsObject {
            @JavascriptInterface
            public void scan() {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent,0);
            }
        }
        wv.addJavascriptInterface(new JsObject(), "android");

        Log.e(TAG,INITPAGE);
        wv.loadUrl(INITPAGE);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                String result = data.getStringExtra("result");
                Log.e(TAG,result);
                wv.loadUrl("javascript:getScanResult('"+result+"')");
                break;
            default:
                Log.e(TAG,"nothing get");
                break;
        }
    }

    class webViewClient extends WebViewClient {
        //重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("url:",""+url);
            if(url.startsWith("tel:")){
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                view.reload();
                return true;
            }else if(url.startsWith(DOWNLOAD_PATH)){
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                view.reload();
                return true;
            }else{
                view.loadUrl(url);
                //如果不需要其他对点击链接事件的处理返回true，否则返回false
                return true;
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            view.loadUrl(ERROR_PAGE);
        }

    }

    class webViewChromeClient extends WebChromeClient{
        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            Log.e("console", message + "(" +sourceID  + ":" + lineNumber+")");
            super.onConsoleMessage(message, lineNumber, sourceID);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("console", "["+consoleMessage.messageLevel()+"] "+ consoleMessage.message() + "(" +consoleMessage.sourceId()  + ":" + consoleMessage.lineNumber()+")");
            return super.onConsoleMessage(consoleMessage);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()) {
            Log.e(TAG,wv.getUrl());
            if(HOMEPAGE.contains(wv.getUrl())){
                // 退出应用
                return super.onKeyDown(keyCode, event);
            }
            // 返回前一个页面
            wv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
