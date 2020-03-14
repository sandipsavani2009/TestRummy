package in.glg.rummy.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import in.glg.rummy.GameRoom.TableActivity;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.LoginRequest;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.HeartBeatEvent;
import in.glg.rummy.models.LoginResponseRest;
import in.glg.rummy.models.LogoutRequest;
import in.glg.rummy.models.TableCards;
import in.glg.rummy.service.HeartBeatService;
import in.glg.rummy.utils.ErrorCodes;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static OnResponseListener heartBeatListener = new OnResponseListener(Event.class) {
        public void onResponse(Object response) {
        }
    };
    protected Context context;
    private OnResponseListener listener = new OnResponseListener(LoginResponse.class) {
        public void onResponse(Object response) {
            BaseActivity.this.onLoginResult((LoginResponse) response);
        }
    };
    private OnResponseListener facebookLoginListener = new OnResponseListener(LoginResponse.class) {
        public void onResponse(Object response) {
            BaseActivity.this.onFacebookLoginResult((LoginResponse) response);
        }
    };
    private EditText mEmailView;
    private View mForm;
    private Handler mHeartBeatHandler;
    private boolean mIsFromRgistration = false;
    private EditText mPasswordView;
    private View mProgressView;
    private TableCards mTableCards;
    protected Toolbar toolbar;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    public static String fb_email = "";
    private String login_method = "FORM";

    class C16327 implements Runnable {
        C16327() {
        }

        public void run() {
            if (GameEngine.getInstance().haveAuthRequest()) {
                BaseActivity.this.sendHeartBeat();
                BaseActivity.this.mHeartBeatHandler.postDelayed(this, HeartBeatService.NOTIFY_INTERVAL);
                return;
            }
            BaseActivity.this.mHeartBeatHandler.removeCallbacks(null);
            BaseActivity.this.mHeartBeatHandler.removeCallbacksAndMessages(null);
        }
    }

    protected abstract int getLayoutResource();

    protected abstract int getToolbarResource();

    protected void setupActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setUpFullScreen() {

    }

    public void navigateToGameRoom() {
        Intent playIntent = new Intent(this, TableActivity.class);
        playIntent.putExtra("iamBack", true);
        startActivity(playIntent);
        finish();
    }

    protected void setTitles(String title, String subTitle) {
        if (this.toolbar != null) {
            this.toolbar.setTitle(title);
            this.toolbar.setSubtitle(subTitle);
        }
    }

    protected void setTitles(int title, int subTitle) {
        if (this.toolbar != null) {
            this.toolbar.setTitle(title);
            this.toolbar.setSubtitle(subTitle);
        }
    }

    protected void setActionBarIcon(int iconRes) {
        this.toolbar.setNavigationIcon(iconRes);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        this.context = this;
        setToolbar(getToolbarResource());
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void setToolbar(int id) {
        if (id > 0) {
            this.toolbar = (Toolbar) findViewById(id);
            if (this.toolbar != null) {
                setSupportActionBar(this.toolbar);
            }
        }
    }

    public Typeface getTypeFace(String fontName) {
        try {
            return Typeface.createFromAsset(getResources().getAssets(), "fonts/" + fontName);
        } catch (Exception e) {
            TLog.e(TAG, "Exception in getOpenSansLightTypeFace" + e.getMessage());
            return null;
        }
    }

    @SuppressLint("WrongConstant")
    protected void showShortSb(int messageId, View v) {
        Snackbar.make(v, messageId, -1).show();
    }

    @SuppressLint("WrongConstant")
    public void showLongSb(int messageId, View v) {
        try {
            Snackbar snackbar =
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            messageId,
                            Snackbar.LENGTH_SHORT);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }
    }

    @SuppressLint("WrongConstant")
    protected void showIndefiniteSb(int messageId, View v) {
        try {
            Snackbar snackbar =
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            messageId,
                            Snackbar.LENGTH_SHORT);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }
    }

    public void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void launchNewActivity(Intent intent, boolean removeStack) {
        if (removeStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        startActivity(intent);
    }

    public void launchNewActivityFinishAll(Intent intent, boolean removeStack) {
        if (removeStack) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
    }

    public void showLobbyScreen() {
        Log.e("flagNativeHybird3",NostraGamusActivity.flagNativeHybird+"");
        Intent lobbyIntent = new Intent(this, HomeActivity.class);
        lobbyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(lobbyIntent);
    }

    public void showFragment(Fragment fragment) {
//        Log.e(TAG, "showFragment");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(fragment);
        ft.commit();
//        ft.commitAllowingStateLoss();

    }

    public void hideFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(fragment);
        ft.commit();
    }

    public void removeFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    public void removeIamBackFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
        getSupportFragmentManager().popBackStack();
    }

    public void checkIamBack(RummyApplication app) {
        app = (RummyApplication) getApplication();
        String tableId = app.getUserData().getTableId();
        if (tableId != null) {
            String[] tableIds = tableId.split(",");
            for (String id : tableIds) {
                app.setJoinedTableIds(id);
            }
            if (tableIds.length > 0) {
                navigateToGameRoom();
            }
        }
    }

    public void launchFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(new Bundle());
        fragmentTransaction.add(R.id.content_frame, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    public void showGenericDialog(Context context, String message) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_generic);
        ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
        ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void doLogin(View form, EditText emailView, EditText passwordView, View progressView) {
        Log.e(TAG, "doLogin");
        this.mProgressView = progressView;
        this.mEmailView = emailView;
        this.mPasswordView = passwordView;
        this.mForm = form;
        showProgress(true);
        String email = this.mEmailView.getText().toString();
        String password = this.mPasswordView.getText().toString();
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        r.setSessionId("None");
        r.setDeviceType(getDeviceType());
        r.setUuid(Utils.generateUuid());
        r.setDeviceId(getDeviceType());
        r.setBuildVersion(Utils.getVersionCode(this));
        String loginReust = Utils.getObjXMl(r);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), loginReust, this.listener);
        } catch (GameEngineNotRunning e) {
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void doLoginWithEngine(View form, EditText emailView, EditText passwordView, View progressView, String uniqueId) {
        Log.e(TAG, "doLoginWithEngine1");
        this.mProgressView = progressView;
        this.mEmailView = emailView;
        this.mPasswordView = passwordView;
        this.mForm = form;
        showProgress(true);
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
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void doLoginWithEngineNastro( String uniqueId) {
        Log.e(TAG, "doLoginWithEngineNastro");
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
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void doLoginWithEngine(View form, EditText emailView, EditText passwordView, View progressView, String uniqueId, String login_method) {
        Log.e(TAG, "doLoginWithEngine2");
        PrefManager.saveString(getBaseContext(), RummyConstants.SOCIAL_LOGIN, login_method);
        RummyConstants.doLogin = true;
        this.login_method = login_method;
        this.mProgressView = progressView;
        this.mEmailView = emailView;
        this.mPasswordView = passwordView;
        this.mForm = form;
        //showProgress(true);
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
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void doLoginWithEngine(String uniqueId, String login_method) {
        Log.e(TAG, "doLoginWithEngine3");
        PrefManager.saveString(getBaseContext(), RummyConstants.SOCIAL_LOGIN, login_method);
        RummyConstants.doLogin = true;
        this.login_method = login_method;
        this.mProgressView = null;
        this.mEmailView = null;
        this.mPasswordView = null;
        this.mForm = null;
        //showProgress(true);
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
            GameEngine.sendRequestToEngine(getApplicationContext(), loginReust, this.facebookLoginListener);
        } catch (GameEngineNotRunning e) {
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void doLoginWithEngine(String uniqueId, String login_method, EditText emailView, EditText passwordView) {
        Log.e(TAG, "doLoginWithEngine4");
        this.mEmailView = emailView;
        this.mPasswordView = passwordView;
        this.login_method = login_method;
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
            //showIndefiniteSb(com.vc.rummyvilla.R.string.error_restart, this.mForm);
        }
    }

    private void onLoginResult(LoginResponse response) {
        Log.e("onLoginResult",response.toString()+"");
        if (response != null) {
            int code = Integer.parseInt(response.getCode());
            if (code == ErrorCodes.SUCCESS) {
               // MyWebEngage.trackLoginRegisterEventWE(MyWebEngage.USER_LOGGED_IN, response.getUserId());

                saveCredentials();
                ((RummyApplication) getApplication()).setUserData(response);
                Log.e("flagNativeHybird2",NostraGamusActivity.flagNativeHybird+"");
                Intent lobbyIntent = new Intent(this, HomeActivity.class);
                lobbyIntent.putExtra("isFromReg", isFromRegistration());
                lobbyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                launchNewActivity(lobbyIntent, true);
                runTimer();
                if (!isFromRegistration()) {
                    checkIamBack((RummyApplication) getApplication());
                }
            } else if (code == ErrorCodes.INVALID_PASSWORD) {
                this.mPasswordView.setError(getString(R.string.error_invalid_password));
                this.mPasswordView.requestFocus();
            } else if (code == ErrorCodes.INVALID_USER_NAME) {
                this.mEmailView.setError(getString(R.string.error_invalid_email));
                this.mEmailView.requestFocus();
            } else if (code == ErrorCodes.WRONG_SESSION_ID) {
                showAskPasswordDialog();
            } else {
                showLongSb(R.string.unknown_server_error, this.mEmailView);
            }
//            showProgress(false);
        }
    }

    private void onFacebookLoginResult(LoginResponse response) {
        Log.e("onFacebookLoginResult","onFacebookLoginResult");
        if (response != null) {
            int code = Integer.parseInt(response.getCode());
            if (code == ErrorCodes.SUCCESS) {
                MyWebEngage.trackLoginRegisterEventWE(MyWebEngage.USER_LOGGED_IN, response.getUserId());

                saveCredentials();
                ((RummyApplication) getApplication()).setUserData(response);
                runTimer();
                Log.e("flagNativeHybird1",NostraGamusActivity.flagNativeHybird+"");
                Intent lobbyIntent = new Intent(this, HomeActivity.class);
                lobbyIntent.putExtra("isFromReg", isFromRegistration());
                lobbyIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                launchNewActivity(lobbyIntent, true);
                if (!isFromRegistration()) {
                    checkIamBack((RummyApplication) getApplication());
                }
            } else if (code == ErrorCodes.WRONG_SESSION_ID) {
                showAskPasswordDialog();
            } else {
                showLongSb(R.string.unknown_server_error, this.mEmailView);
            }
            showProgress(false);
        }
    }

    private void showAskPasswordDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ask_password);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        final EditText password = (EditText) dialog.findViewById(R.id.password);
        Button submit = (Button) dialog.findViewById(R.id.submit);

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password.getText().toString().isEmpty()) {
                    password.setError("* Required");
                } else {
                    doLoginWithUsernamePassword(PrefManager.getString(getApplicationContext(), "username", ""), password.getText().toString());
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public void doLoginWithUsernamePassword(String username, String password) {
        Log.e(TAG+"@542","doLoginWithUsernamePassword");
        showProgress(true);
        LoginRequest r = new LoginRequest();
        r.setEmail(username);
        r.setPassword(password);
        r.setSessionId("None");
        r.setDeviceType(getDeviceType());
        r.setUuid(Utils.generateUuid());
        r.setDeviceId(getDeviceType());
        r.setBuildVersion(Utils.getVersionCode(this));
        String loginReust = Utils.getObjXMl(r);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), loginReust, this.listener);
        } catch (GameEngineNotRunning e) {
            showIndefiniteSb(R.string.error_restart, this.mForm);
        }
    }

    public void hideKeyboard(View view) {
        try {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getApplicationWindowToken(), 2);
        } catch (Exception e) {

        }
    }

    @TargetApi(13)
    private void showProgress(final boolean var1) {
        try {
            Log.e(TAG,"showProgress1");
            float var3 = 1.0F;
            byte var6 = 4;
            byte var5 = 0;
            byte var4;
            View var7;
            if (VERSION.SDK_INT >= 13) {
                int var8 = 100;
                var7 = this.mForm;
                if (var1) {
                    var4 = 4;
                } else {
                    var4 = 0;
                }
                Log.e(TAG,"showProgress2");
                var7.setVisibility(var4);
                ViewPropertyAnimator var9 = this.mForm.animate().setDuration((long) var8);
                float var2;
                if (var1) {
                    var2 = 0.0F;
                } else {
                    var2 = 1.0F;
                }

                var9.alpha(var2).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator var1x) {
                        Log.e(TAG,"showProgress3");
                        View var3 = BaseActivity.this.mForm;
                        byte var2;
                        if (var1) {
                            var2 = 4;
                        } else {
                            var2 = 0;
                        }

                        var3.setVisibility(var2);
                    }
                });
                var7 = this.mProgressView;
                if (var1) {
                    var4 = var5;
                } else {
                    var4 = 4;
                }

                Log.e(TAG,"showProgress4");
                var7.setVisibility(var4);
                var9 = this.mProgressView.animate().setDuration((long) var8);
                if (var1) {
                    var2 = var3;
                } else {
                    var2 = 0.0F;
                }

                var9.alpha(var2).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator var1x) {
                        Log.e(TAG,"showProgress5");
                        View var3 = BaseActivity.this.mProgressView;
                        byte var2;
                        if (var1) {
                            var2 = 0;
                        } else {
                            var2 = 4;
                        }

                        var3.setVisibility(var2);
                    }
                });
            } else {
                Log.e(TAG,"showProgress6");
                var7 = this.mProgressView;
                if (var1) {
                    var4 = 0;
                } else {
                    var4 = 4;
                }

                var7.setVisibility(var4);
                var7 = this.mForm;
                if (var1) {
                    var4 = var6;
                } else {
                    var4 = 0;
                }

                Log.e(TAG,"showProgress7");
                var7.setVisibility(var4);
            }
        } catch (Exception e) {
            Log.e(TAG,e+"");
        }

    }

    private void saveCredentials() {
        String userName = "";
        String password = "";
        if (mEmailView != null && mPasswordView != null) {
            userName = this.mEmailView.getText().toString();
            password = this.mPasswordView.getText().toString();
        }

        byte[] userData = new byte[0];
        byte[] passwordData = new byte[0];
        try {
            userData = userName.getBytes("UTF-8");
            passwordData = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encryptedUserName = Base64.encodeToString(userData, 0);
        String encryptedPassword = Base64.encodeToString(passwordData, 0);
        PrefManager.saveString(getApplicationContext(), "userName", encryptedUserName);
        PrefManager.saveString(getApplicationContext(), "password", encryptedPassword);
        PrefManager.saveBool(getApplicationContext(), "isLoggedIn", true);
    }

    public void setIsFromRegistration(boolean isFromRegistration) {
        this.mIsFromRgistration = isFromRegistration;
    }

    private boolean isFromRegistration() {
        return this.mIsFromRgistration;
    }

    public void showSuccessPopUp() {
        /*final Dialog dialog = new Dialog(this.context);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.dialog_registarion);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();
        ((Button) dialog.findViewById(R.id.play_now_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });*/
    }

    public boolean isInputValid(String input) {
        return Pattern.compile("^[a-zA-Z ]+$").matcher(input.toString()).matches();
    }

    public void runTimer() {
        disableHearBeat();
        this.mHeartBeatHandler = new Handler();
        this.mHeartBeatHandler.postDelayed(new C16327(), HeartBeatService.NOTIFY_INTERVAL);
    }

    public void sendCardSlots(TableCards tableCards) {
        this.mTableCards = tableCards;
        LoginResponse userData = ((RummyApplication) getApplication()).getUserData();
        HeartBeatEvent request = new HeartBeatEvent();
        request.setEventName("HEART_BEAT");
        request.setPlayerIn("new_lobby");
        request.setMsg_uuid(Utils.generateUuid());
        request.setNickName(userData.getNickName());
        request.setTable(tableCards);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(request), heartBeatListener);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "EXTRA_TIME" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    public TableCards getTableCards() {
        return this.mTableCards;
    }

    private void sendHeartBeat() {
        if (GameEngine.getInstance().isSocketConnected()) {
            LoginResponse userData = ((RummyApplication) getApplication()).getUserData();
            HeartBeatEvent request = new HeartBeatEvent();
            request.setEventName("HEART_BEAT");
            request.setPlayerIn("new_lobby");
            request.setMsg_uuid(Utils.generateUuid());
            request.setNickName(userData.getNickName());
            if (this.mTableCards != null) {
                request.setTable(this.mTableCards);
            }
            request.setTimestamp(String.valueOf(System.currentTimeMillis()));
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(request), heartBeatListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                TLog.d(TAG, "EXTRA_TIME" + gameEngineNotRunning.getLocalizedMessage());
            }
            this.mTableCards = null;
        }
    }

    public void disableHearBeat() {
        if (this.mHeartBeatHandler != null) {
            this.mHeartBeatHandler.removeCallbacks(null);
            this.mHeartBeatHandler.removeCallbacksAndMessages(null);
        }
    }

    public String getDeviceType() {
        if (getResources().getBoolean(R.bool.isTablet)) {
            return "Tablet";
        }
        return "Mobile";
    }



    public void handleOtherLogin() {
        disableHearBeat();
        PrefManager.saveBool(getApplicationContext(), "isLoggedIn", false);
        LogoutRequest request = new LogoutRequest();
        request.setCommand("logout");
        request.setMsg_uuid(Utils.generateUuid());
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(request), null);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.e("Error", "logout" + gameEngineNotRunning.getLocalizedMessage());
        }
        /*Intent i = new Intent(this, NostraJsonActivity.class);
        i.putExtra("OTHER_LOGIN", true);
        launchNewActivityFinishAll(i, true);*/
        if(LoadingActivity.getInstance() != null)
        {
            LoadingActivity.getInstance().finish();
        }

        this.finish();
    }

    public String getIMEI() {
        Log.d(TAG, "Getting IMEI");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            return "";
        }
        Log.e(TAG, telephonyManager.getDeviceId()+"");
        if(null == telephonyManager.getDeviceId()){
            return "";
        }
        return telephonyManager.getDeviceId();
    }

    public void createLoginResponseObject(String response) {
        try {
            PrefManager.saveString(getBaseContext(), RummyConstants.LOGIN_RESPONSE_REST, response);
            Gson gson;
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("MM/dd/yyyy hh:mm aa");
            gson = gsonBuilder.create();
            LoginResponseRest loginResponseRest = gson.fromJson(response, LoginResponseRest.class);

            Log.w(MyWebEngage.WE_LOG, "Saving User ID: " + loginResponseRest.getPlayerid());
            PrefManager.saveString(getBaseContext(), RummyConstants.PLAYER_USER_ID, String.valueOf(loginResponseRest.getPlayerid()));

            MyWebEngage.doWebEngageLogin(loginResponseRest);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    public void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    public void invisibleView(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    public void emptyDynamicAffiliateStrings() {
        PrefManager.saveString(getApplicationContext(), RummyConstants.DYNAMIC_AFFILIATE_ID, "");
        PrefManager.saveString(getApplicationContext(), RummyConstants.DYNAMIC_CAMPAIGN_ID, "");
        PrefManager.saveString(getApplicationContext(), RummyConstants.DYNAMIC_CLICK_ID, "");
        PrefManager.saveString(getApplicationContext(), RummyConstants.UTM_STRING, "");

        //Log.w(LoginActivity.class.getName(), "Clearing affiliate data from "+source);
    }
}
