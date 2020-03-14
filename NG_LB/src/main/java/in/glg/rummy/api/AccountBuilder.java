package in.glg.rummy.api;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import java.lang.reflect.Type;

import in.glg.rummy.api.requests.SignUpRequest;
import in.glg.rummy.models.EmialUsRequest;
import in.glg.rummy.packagedev.android.api.base.builders.OnRequestListener;
import in.glg.rummy.packagedev.android.api.base.requests.AbstractDataRequest;
import in.glg.rummy.packagedev.android.api.base.requests.DataRequestType;

public class AccountBuilder<T> extends RummyBaseBuilder {

    public enum LoginRequestType implements DataRequestType {
        FORGOT_PASSWORD,
        REGISTER,
        CONTACT_US
    }

    public AccountBuilder(OnRequestListener listener, Class<T> entity) {
        super(listener, (Class) entity);
    }

    public AccountBuilder(OnRequestListener listener, Type type) {
        super(listener, type);
    }

    public void execute(AbstractDataRequest dataRequest) {
        switch ((LoginRequestType) dataRequest.requestType) {
            case FORGOT_PASSWORD:
                forgotPassword((SignUpRequest) dataRequest);
                return;
            case REGISTER:
                register((SignUpRequest) dataRequest);
                return;
            case CONTACT_US:
                contactUs((EmialUsRequest) dataRequest);
                return;
            default:
                return;
        }
    }

    private void register(SignUpRequest dataRequest) {
        dataRequest.responseHandler = this;
        dataRequest.URL = UrlBuilder.getSignUpURL(dataRequest);
        dataRequest.entity = getRequestEntity(new Gson().toJson(dataRequest));
        get(dataRequest);
    }

    private void forgotPassword(SignUpRequest dataRequest) {
        dataRequest.responseHandler = this;
        dataRequest.URL = UrlBuilder.getForgotPasswordUrl(dataRequest);
        dataRequest.entity = getRequestEntity(new Gson().toJson(dataRequest));
        get(dataRequest);
    }

    private void contactUs(EmialUsRequest dataRequest) {
        dataRequest.responseHandler = this;
        RequestParams params = new RequestParams();
        params.add("name", dataRequest.getName());
        params.add("message", dataRequest.getMessage());
        params.add("email", dataRequest.getEmail());
        params.add("subject", dataRequest.getSubject());
        params.add("playerid", dataRequest.getPlayerid());
        dataRequest.URL = UrlBuilder.getContactUsUrl();
        postFormData(dataRequest, params);
    }
}
