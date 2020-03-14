package in.glg.rummy.packagedev.android.api.base.requests;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

public abstract class AbstractDataRequest {
    public transient String URL;
    public transient Context context;
    public transient HttpEntity entity;
    public transient Header[] headers;
    public transient DataRequestDelegate requestDelegate;
    public transient DataRequestType requestType;
    public transient AsyncHttpResponseHandler responseHandler;
}
