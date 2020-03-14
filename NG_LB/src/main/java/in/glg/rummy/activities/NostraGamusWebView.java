package in.glg.rummy.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import in.glg.rummy.R;

public class NostraGamusWebView  extends Activity {

    //    String url="https://www.tajrummy.com/promotions/x/";
    String url="http://rh.glserv.info/static_files/html5/gamerh.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nostra_webview);

        webViewSettings();
    }
    WebView webView;
    public static String WebSessionId = "";
    String webViewFunction = "androidFunction";
    private void webViewSettings()
    {
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        JavaScriptInterfaceDemo javaScriptInterfaceDemo
                = new JavaScriptInterfaceDemo(this,webView);
        webView.addJavascriptInterface(javaScriptInterfaceDemo, webViewFunction);

        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                //"javascript:<your javaScript function"
                webView.loadUrl("javascript:init('" + WebSessionId + "')");
            }
        });
    }
}
