package in.glg.rummy.activities;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class JavaScriptInterfaceDemo {
    protected Activity parentActivity;
    protected WebView mWebView;

    public JavaScriptInterfaceDemo(Activity _activity, WebView _webView)  {
        parentActivity = _activity;
        mWebView = _webView;

    }

    @JavascriptInterface
    public void setResult(String val){
        Log.e("JavascriptInterface","JavaScriptHandler : " + val);
//        this.parentActivity.javascriptCallFinished(val);
        Toast.makeText(parentActivity,val+"", Toast.LENGTH_LONG).show();
    }

}

