package in.glg.rummy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.LoginRequest;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.LogoutRequest;
import in.glg.rummy.service.HeartBeatService;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;

public class NostraGamusActivity extends BaseActivity {
    private boolean isBackpressed = false;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_nostra_gamus;
    }

    @Override
    protected int getToolbarResource() {
        return 0;
    }

    String strToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_nostra_gamus);
        this.mRummyApp = (RummyApplication) getApplication();
        strToken = PrefManager.getString(this, RummyConstants.ACCESS_TOKEN_REST, "");
        Log.e("strToken", strToken + " *");
//        GameEngine.getInstance().stop();
        displaySingleChoice();
    }

    public static String flagNativeHybird = "";
    String userId = "";

    public void onNativeClick(View view) {
        flagNativeHybird = "native";
        String merchant_id = "1111";
//        String Name = "suresh.motoe2@gridlogic.in";
        String userIdTemp = userId;
        Intent in = new Intent(getApplicationContext(), NostraJsonActivity.class);
        in.putExtra("merchant_id", merchant_id);
        in.putExtra("userid", userIdTemp);
        startActivity(in);

    }

    public void onHybridClick(View view) {
        flagNativeHybird = "hybrid";
        String merchant_id = "1111";
//        String Name = "suresh.motoe2@gridlogic.in";
        String userIdTemp = userId;
//        Intent in = new Intent(getApplicationContext(), NostraGamusWebView.class);
        Intent in = new Intent(getApplicationContext(), NostraJsonActivity.class);
        in.putExtra("merchant_id", merchant_id);
        in.putExtra("userid", userIdTemp);
        startActivity(in);

    }


//    temp code

    public void onLoginClick(View view) {
        /*engineInit();
        startGameEngine();
        strToken = PrefManager.getString(this, RummyConstants.ACCESS_TOKEN_REST, "");
        Log.e("strToken", strToken + " #");
        if (strToken.length() > 0) {
            String uniqueId = PrefManager.getString(getBaseContext(), RummyConstants.UNIQUE_ID_REST, "");
            doLogin(uniqueId);
//            Intent in = new Intent(getApplicationContext(), HomeActivity.class);
//            startActivity(in);
        }*/
    }


    private void engineInit() {

        //updateFirstInstall();
        if (GameEngine.getInstance().isSocketConnected() == false) {
            setNetworkConnectionTimer();
        }


        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("isLoggedOut", false)) {
            disableHearBeat();
            LogoutRequest request = new LogoutRequest();
            request.setCommand("logout");
            request.setMsg_uuid(Utils.generateUuid());
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(request), null);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                TLog.d("Error", "logout" + gameEngineNotRunning.getLocalizedMessage());
            }
        }
    }

    private void setNetworkConnectionTimer() {
        this.resetNetworkHandler();
        this.mNetworkHandler = new Handler();
        this.mNetworkHandler.postDelayed(new Runnable() {
            public void run() {
                startGameEngine();
            }
        }, 1000L);
    }

    private void doLogin(String uniqueId) {
        Log.e("doLogin", "Session ID: " + uniqueId);

        LoginRequest r = new LoginRequest();
        r.setEmail("None");
        r.setPassword("None");
        r.setSessionId(uniqueId);
        r.setDeviceType(getDeviceType());
        r.setUuid(Utils.generateUuid());
        r.setDeviceId(getDeviceType());
        r.setBuildVersion(Utils.getVersionNumber(this));
        String loginReust = Utils.getObjXMl(r);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), loginReust, this.listener);
        } catch (GameEngineNotRunning e) {
            TLog.e("GameEngineNotRunning", "Error in Splash Screen : doLogin");
        }
    }

    private OnResponseListener listener = new OnResponseListener(LoginResponse.class) {
        public void onResponse(Object var1) {
            LoginResponse var2 = (LoginResponse) var1;
            onLoginResult(var2);
        }
    };


    private RummyApplication mRummyApp;

    private void onLoginResult(LoginResponse response) {
        if (response == null) {
            return;
        }
        if (response.isSuccessful()) {
            PrefManager.saveBool(getApplicationContext(), "isLoggedIn", true);
            this.mRummyApp.setUserData(response);
            runTimer();
            String tableId = this.mRummyApp.getUserData().getTableId();
            boolean isIamBack = false;
            if (tableId != null && tableId.length() > 0) {
                isIamBack = true;
            }
            gotoMain(isIamBack);
            checkIamBack(this.mRummyApp);
            return;
        }
        gotoLogin();

    }

    private void gotoMain(boolean isFromIamBack) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("isIamBack", isFromIamBack);
        launchNewActivity(intent, false);
        finish();
    }

    private void gotoLogin() {
        Log.e("gotoLogin", "gotoLogin");
        if (LoginRequest.flash.equalsIgnoreCase("1")) {
//            this.launchNewActivity(new Intent(this, InitialActionActivity.class), true);
            this.launchNewActivity(new Intent(this, NostraJsonActivity.class), true);
        } else {
            this.launchNewActivity(new Intent(this, NostraJsonActivity.class), true);
        }
        finish();
    }


    private void startGameEngine() {
        if (Utils.isNetworkAvailable(this)) {

            resetNetworkHandler();
            GameEngine.getInstance().start();
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
                return;
            }
            return;
        }
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

    }

    @Subscribe
    public void onMessageEvent(GameEvent event) {
        Log.e("onMessageEvent", "onMessageEvent");
        if (event.name().equalsIgnoreCase("SERVER_CONNECTED")) {
            new SetupAsyncTask().execute(new Void[0]);
        }
    }

    public class SetupAsyncTask extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            resetNetworkHandler();
        }

        protected Void doInBackground(Void... params) {
            SystemClock.sleep(HeartBeatService.NOTIFY_INTERVAL);
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (GameEngine.getInstance().isSocketConnected()) {
                goToNextScreen();
            } else {
                startGameEngine();
            }
        }
    }

    public void goToNextScreen() {
        unregisterEventBus();
        if (this.isBackpressed) {
            GameEngine.getInstance().stop();
        } else {
            Log.e("flow", "TOKEN: " + PrefManager.getString(getApplicationContext(), ACCESS_TOKEN_REST, ""));
            if (PrefManager.getString(getApplicationContext(), ACCESS_TOKEN_REST, "").equalsIgnoreCase(""))
            {

            }
            else
                launchHomeActivity();
        }
    }

    private void launchHomeActivity() {
        UnsupportedEncodingException e;
        if (PrefManager.getBool(getApplicationContext(), "isLoggedIn", false)) {
            String encryptedUserName = PrefManager.getString(getApplicationContext(), "userName", "");
            String encryptedPassword = PrefManager.getString(getApplicationContext(), "password", "");
            String socialLogin = PrefManager.getString(getApplicationContext(), RummyConstants.SOCIAL_LOGIN, "");

            Log.e("Username", "Username: " + encryptedUserName);
            Log.e("Password", "Password: " + encryptedPassword);

            Log.e("Social", "Social login: " + socialLogin);

            if (!socialLogin.equalsIgnoreCase("GOOGLE") && !socialLogin.equalsIgnoreCase("FACEBOOK")) {
                if (encryptedUserName.length() <= 0 || encryptedPassword.length() <= 0) {
                    gotoLogin();
                    return;
                }
            } else {
                Log.e("else", "Do Else");
                String uniqueId = PrefManager.getString(getBaseContext(), RummyConstants.UNIQUE_ID_REST, "");
                doLogin(uniqueId);
                return;
            }
            byte[] userData = Base64.decode(encryptedUserName, 0);
            byte[] passwordData = Base64.decode(encryptedPassword, 0);
            String userName = null;
            String password = null;
            try {
                String userName2 = new String(userData, "UTF-8");
                try {
                    password = new String(passwordData, "UTF-8");
                    userName = userName2;
                } catch (UnsupportedEncodingException e2) {
                    e = e2;
                    userName = userName2;
                    e.printStackTrace();
//                    doLogin(userName, password);
                    return;
                }
            } catch (UnsupportedEncodingException e3) {
                e = e3;
                e.printStackTrace();
//                doLogin(userName, password);
                return;
            }
//            doLogin(userName, password);
            return;
        }
        gotoLogin();

    }

    private Handler mNetworkHandler;

    private void resetNetworkHandler() {
        if (this.mNetworkHandler != null) {
            this.mNetworkHandler.removeCallbacks((Runnable) null);
            this.mNetworkHandler.removeCallbacksAndMessages((Object) null);
            this.mNetworkHandler = null;
        }
    }


    //    single choice dialog
    int from = 0; //This must be declared as global !
    final CharSequence[] choice = {
            "5014710",
            "5014711",
            "5014712",
            "5014713",
            "5014714"

//            "nostrarummy9@nosatragamus.com", "nostrarummy10@nosatragamus.com"
    };

    public void displaySingleChoice() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select One User");
        alert.setCancelable(false);
        alert.setSingleChoiceItems(choice, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                from = which;
                Log.e("which", which + "");
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), choice[from],
                        Toast.LENGTH_SHORT).show();
                Log.e("from", from + "");
                userId = choice[from] + "";
            }
        });
        alert.show();
    }

    public void onUserName(View view) {
        displaySingleChoice();
    }
}
