package in.glg.rummy.api;

import android.os.Handler;
import android.os.Message;

import in.glg.rummy.utils.Utils;

public abstract class OnResponseListener<T> extends Handler {
    public static final int SERVER_RESONSE = 1000;
    private final Class<? extends T> entity;

    public abstract void onResponse(T t);

    public OnResponseListener(Class<? extends T> entity) {
        this.entity = entity;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what)
        {
            case 1000:
                onResponse(Utils.getObject((String) msg.obj, this.entity));
                return;
            default:
                return;
        }
    }

    public Message getResponseMessage(String response) {
        return obtainMessage(1000, response);
    }
}
