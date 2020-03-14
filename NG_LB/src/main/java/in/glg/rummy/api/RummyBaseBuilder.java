package in.glg.rummy.api;

/**
 * Created by GridLogic on 31/8/17.
 */

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;

import java.lang.reflect.Type;
import java.security.KeyStore;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import in.glg.rummy.packagedev.android.api.base.builders.BaseBuilder;
import in.glg.rummy.packagedev.android.api.base.builders.OnRequestListener;
import in.glg.rummy.packagedev.android.api.base.builders.json.CommonJsonBuilder;
import in.glg.rummy.packagedev.android.api.base.result.DataResult;
import in.glg.rummy.utils.TLog;

public abstract class RummyBaseBuilder<T> extends BaseBuilder<T> {
    public RummyBaseBuilder(OnRequestListener listener, Class<T> entity) {
        super(listener, (Class) entity);
        try {
            AsyncHttpClient asyncHttpClient = getAsyncHttpClient();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            asyncHttpClient.setSSLSocketFactory(socketFactory);
        } catch (Exception e) {
            TLog.e("RummyBaseBuilder ", e.getMessage());
        }
    }

    public RummyBaseBuilder(OnRequestListener listener, Type type) {
        super(listener, type);
    }

    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
        if (isResultOk(statusCode)) {
            DataResult<T> dataresult = new DataResult();
            dataresult.statusCode = statusCode;
            dataresult.successful = isResultOk(statusCode);
            sendOnResultMessage(dataresult);
            return;
        }
        sendOnFailMessage("Server Error");
    }

    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable t) {
        super.onFailure(statusCode, headers, errorResponse, t);
        if (errorResponse != null) {
            DataResult<Error> dataresult = new DataResult();
            dataresult.statusCode = statusCode;
            dataresult.successful = isResultOk(statusCode);
            try {
                dataresult.entity = new CommonJsonBuilder().getEntityForJson(new String(errorResponse), Error.class);
                sendOnFailMessage((DataResult) dataresult);
            } catch (Exception e) {
                sendOnFailMessage(e.getLocalizedMessage());
            }
        }
    }

    protected Header[] getDefautHeaders() {
        return new Header[]{new BasicHeader("Content-Type", "application/x-www-form-urlencoded;"), new BasicHeader("charset", "utf-8")};
    }
}