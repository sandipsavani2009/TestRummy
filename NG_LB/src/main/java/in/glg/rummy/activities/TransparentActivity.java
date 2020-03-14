package in.glg.rummy.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import androidx.appcompat.app.ActionBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import in.glg.rummy.NetworkProvider.VolleySingleton;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.LoginRequest;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;
import static in.glg.rummy.utils.RummyConstants.UNIQUE_ID_REST;

public class TransparentActivity extends BaseActivity
{
   private static final String TAG = GameEngine.class.getSimpleName();

   private static final boolean AUTO_HIDE = true;
   private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
   private static final int UI_ANIMATION_DELAY = 300;
   private OnResponseListener listener = new OnResponseListener(LoginResponse.class) {
      public void onResponse(Object var1) {
         LoginResponse var2 = (LoginResponse)var1;
         TransparentActivity.this.onLoginResult(var2);
      }
   };
   private final OnTouchListener mDelayHideTouchListener = new OnTouchListener() {
      public boolean onTouch(View var1, MotionEvent var2) {
         TransparentActivity.this.delayedHide(3000);
         return false;
      }
   };
   private final Handler mHideHandler = new Handler();
   private final Runnable mHidePart2Runnable = new Runnable() {
      @SuppressLint({"InlinedApi"})
      public void run() {
      }
   };
   private final Runnable mHideRunnable = new Runnable() {
      public void run() {
         TransparentActivity.this.hide();
      }
   };
   private Handler mNetworkHandler;
   private final Runnable mShowPart2Runnable = new Runnable() {
      public void run() {
         ActionBar var1 = TransparentActivity.this.getSupportActionBar();
         if(var1 != null) {
            var1.show();
         }

      }
   };
   private RummyApplication mRummyApp;
   private boolean mVisible;

   private void delayedHide(int delayMillis) {
      this.mHideHandler.removeCallbacks(this.mHideRunnable);
      this.mHideHandler.postDelayed(this.mHideRunnable, (long)delayMillis);
   }

   private void doLogin(String email, String password) {
      Log.e(TAG, "Doing Login via credentials");
      LoginRequest r = new LoginRequest();
      r.setEmail(email);
      r.setPassword(password);
      r.setSessionId("None");
      r.setDeviceType(getDeviceType());
      r.setUuid(Utils.generateUuid());
      r.setDeviceId(getDeviceType());
      r.setBuildVersion(Utils.getVersionNumber(this));
      String loginReust = Utils.getObjXMl(r);
      try {
         GameEngine.getInstance();
         GameEngine.sendRequestToEngine(getApplicationContext(), loginReust, this.listener);
      } catch (GameEngineNotRunning e) {
         TLog.e(TAG, "Error in Splash Screen : doLogin");
      }
   }

   private void gotoLogin() {
      if(LoginRequest.flash.equalsIgnoreCase("1")) {

         this.launchNewActivity(new Intent(this, NostraJsonActivity.class), true);
      }
      else{
         this.launchNewActivity(new Intent(this, NostraJsonActivity.class), true);
      }
      this.finish();
   }

   private void gotoMain(boolean isFromIamBack) {
      Utils.isFromSocketDisconnection = true;
      Intent intent = new Intent(this, HomeActivity.class);
      intent.putExtra("isIamBack", isFromIamBack);
      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      launchNewActivity(intent, false);
      LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("CLOSE_ACTIVITIES"));
   }

   private void hide() {
      this.mVisible = false;
      this.mHideHandler.removeCallbacks(this.mShowPart2Runnable);
      this.mHideHandler.postDelayed(this.mHidePart2Runnable, 300L);
   }

   private void init() {
      GameEngine.getInstance().stop();
      this.mRummyApp = (RummyApplication)this.getApplication();
      this.mRummyApp.clearJoinedTablesIds();
      this.mRummyApp.getEventList().clear();
      this.registerEventBus();
   }

   private void launchHomeActivity() {
      UnsupportedEncodingException e;
      if (PrefManager.getBool(getApplicationContext(), "isLoggedIn", false))
      {
            String uniqueId = PrefManager.getString(getBaseContext(), RummyConstants.UNIQUE_ID_REST, "");
            doLogin(uniqueId);
            return;
      }
      gotoLogin();
   }
   private RequestQueue queue;

   private void updateSessionId()
   {
      try
      {
         final String TOKEN = PrefManager.getString(getApplicationContext(), ACCESS_TOKEN_REST, "");
         queue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

         String apiURL = Utils.SERVER_ADDRESS+"api/v1/get-user-session-id";
         Log.w(TAG, TOKEN);
         Log.w(TAG, apiURL);

         final StringRequest stringRequest = new StringRequest(Request.Method.GET, apiURL,
                 new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                       try {
                          JSONObject jsonObject = new JSONObject(response.toString());
                          Log.w(TAG, "UNIQUE ID: "+jsonObject.getString("unique_id"));
                          PrefManager.saveString(getBaseContext(), UNIQUE_ID_REST, jsonObject.getString("unique_id"));

                       }
                       catch (Exception e){

                       }
                    }
                 },
                 new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       Log.d(TAG, "Error Resp: " + error.toString());


                    }
                 })
         {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
               HashMap<String, String> headers = new HashMap<String, String>();
               headers.put("Authorization", "Token "+TOKEN);
               return headers;
            }

            @Override
            public String getBodyContentType() {
               return "application/x-www-form-urlencoded; charset=UTF-8";
            }
         };

         stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                 DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //TIMEOUT INTERVAL (Default: 2500ms)
                 2,    //No.Of Retries (Default: 1)
                 2));  //BackOff Multiplier (Default: 1.0)

         queue.add(stringRequest);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }


   private void doLogin(String uniqueId)
   {
      Log.e(TAG, "Doing Login via unique ID");
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
         TLog.e(TAG, "Error in Splash Screen : doLogin");
      }
   }

   private void onLoginResult(LoginResponse response) {
      if (response == null) {
         return;
      }
      if (response.isSuccessful()) {
         PrefManager.saveBool(getApplicationContext(), "isLoggedIn", true);
         this.mRummyApp.setUserData(response);
         String tableId = this.mRummyApp.getUserData().getTableId();
         boolean isIamBack = false;
         if (tableId != null && tableId.length() > 0) {
            isIamBack = true;
         }
         gotoMain(isIamBack);
         runTimer();
         checkIamBack(this.mRummyApp);
         if(LoadingActivity.getInstance() != null)
         {
            LoadingActivity.getInstance().finish();
         }
         finish();
         return;
      }
      gotoLogin();
   }

   private void registerEventBus() {
      if(!EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().register(this);
      }
   }

   private void resetNetworkHandler() {
      if(this.mNetworkHandler != null) {
         this.mNetworkHandler.removeCallbacks((Runnable)null);
         this.mNetworkHandler.removeCallbacksAndMessages((Object)null);
         this.mNetworkHandler = null;
      }
   }

   private void setNetworkConnectionTimer() {
      this.resetNetworkHandler();
      this.mNetworkHandler = new Handler();
      this.mNetworkHandler.postDelayed(new Runnable() {
         public void run() {
            TransparentActivity.this.startGameEngine();
         }
      }, 3000L);
   }

   @SuppressLint({"InlinedApi"})
   private void show() {
      this.mVisible = true;
      this.mHideHandler.removeCallbacks(this.mHidePart2Runnable);
      this.mHideHandler.postDelayed(this.mShowPart2Runnable, 300L);
   }

   private void startGameEngine() {
      if(Utils.isNetworkAvailable(this)) {
         this.resetNetworkHandler();
         GameEngine.getInstance().start();
      }

   }

   private void toggle() {
      if(this.mVisible) {
         this.hide();
      } else {
         this.show();
      }
   }

   private void unregisterEventBus() {
      if(EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().unregister(this);
      }
   }

   protected int getLayoutResource() {
      return R.layout.activity_transparent;
   }

   protected int getToolbarResource() {
      return 0;
   }

   public void goToNextScreen() {
      this.launchHomeActivity();
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setUpFullScreen();
      init();
   }

   protected void onDestroy() {
      super.onDestroy();
      this.unregisterEventBus();
   }

   @Subscribe
   public void onMessageEvent(GameEvent var1) {
      if(var1.name().equalsIgnoreCase("SERVER_CONNECTED")) {
         (new SetupAsyncTask()).execute(new Void[0]);
      }

   }

   protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      delayedHide(100);
   }

   protected void onStart() {
      super.onStart();
      this.startGameEngine();
   }

   protected void onStop() {
      super.onStop();
      this.resetNetworkHandler();
   }

   public class SetupAsyncTask extends AsyncTask<Void, Void, Void> {
      protected void onPreExecute() {
         super.onPreExecute();
         TransparentActivity.this.resetNetworkHandler();
      }

      protected Void doInBackground(Void... params) {
         SystemClock.sleep(3000);
         return null;
      }

      protected void onPostExecute(Void result) {
         super.onPostExecute(result);
         if (GameEngine.getInstance().isSocketConnected()) {
            TransparentActivity.this.goToNextScreen();
         } else {
            TransparentActivity.this.startGameEngine();
         }
      }
   }
}
