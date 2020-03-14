package in.glg.rummy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import in.glg.rummy.R;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.fragments.LobbyFragment;
import in.glg.rummy.models.LogoutRequest;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;
import static in.glg.rummy.utils.RummyConstants.SERVER_IP_REST;
import static in.glg.rummy.utils.RummyConstants.UNIQUE_ID_REST;

public class NostraJsonActivity extends BaseActivity {
    Context mContext;
    private static final String TAG = "NostraJsonActivity";
    TextView tv_json;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_notra_json;
    }

    @Override
    protected int getToolbarResource() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        EventBus.getDefault().register(this);
        tv_json = findViewById(R.id.tv_json);
        Intent in = getIntent();
        if(in.hasExtra("merchant_id") && in.hasExtra("userid"))
        {
            merchant_id = in.getStringExtra("merchant_id");
            userId = in.getStringExtra("userid");

        }
        else
        {
            merchant_id = PrefManager.getString(mContext,"NOSTRO_MERCHANT_ID","");
            userId = PrefManager.getString(mContext,"NOSTRO_USER_ID","");
        }
        getCheckSumFromServer();

        //   threadRequest();
    }



    private void getCheckSumFromServer()
    {
        String url = Utils.API_SERVER_ADDRESS+Utils.checkSumUrl;
        Map<String, String> params = new HashMap<String, String>();
        params.clear();
        params.put("merchant_id", merchant_id + "");
        params.put("userid", userId + "");

        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            if (status.equalsIgnoreCase("Success")) {
                                String checksum_str = response.getString("checksum_str");
                                 getUserInfoFromServer(checksum_str);
                            }
                            else
                            {
                                showGenericDialog(mContext,""+message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JsonException" + e.toString());
                            showGenericDialog(mContext,"Something went wrong with response formate");
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vikas","calling error response");
                VolleyLog.e(TAG, "Error: " + error.getMessage());


            }
            }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000, //TIMEOUT INTERVAL (Default: 2500ms)
                1,    //No.Of Retries (Default: 1)
                1));  //BackOff Multiplier (Default: 1.0)

        queue.add(jsonObjReq);
    }


    private void getUserInfoFromServer(String checksum_str)
    {
        String url = Utils.API_SERVER_ADDRESS+Utils.getUserInfoUrl;
        Map<String, String> params = new HashMap<String, String>();
        params.clear();
        params.put("merchant_id", merchant_id + "");
        params.put("userid", userId + "");
        params.put("checksum",checksum_str);

        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            String status = response.getString("status");
                          //  String message = response.getString("message");
                            if (status.equalsIgnoreCase("Success")) {

                                loginAttempt(response);
                            }
                            else
                            {
                                showGenericDialog(mContext,"Something went wrong, Please try again");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JsonException" + e.toString());
                            showGenericDialog(mContext,"Something went wrong with response formate");
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vikas","calling error response");
                VolleyLog.e(TAG, "Error: " + error.getMessage());


            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000, //TIMEOUT INTERVAL (Default: 2500ms)
                1,    //No.Of Retries (Default: 1)
                1));  //BackOff Multiplier (Default: 1.0)

        queue.add(jsonObjReq);
    }



    @Subscribe
    public void onMessageEvent(GameEvent event) {
        if (event.name().equalsIgnoreCase("SERVER_CONNECTED")) {
            Log.e("vikas","engine connected");
          //  postEngine();
                try {
                if (null == jsonUserInfo) {
                    showGenericDialog(mContext,"Something went wrong, Please try again");
                }
                else
                {
                    //createLoginResponseObject(jsonUserInfo.toString());
                    emptyDynamicAffiliateStrings();

                    RummyConstants.doLogin = true;
                    doLoginWithEngineNastro(jsonUserInfo.getString("unique_id"));
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "postEngine of Login -->> " + e.toString());
            }

        }
    }

    private void engineInit() {
        Log.e("engineInit", "engineInit");

        if (!GameEngine.getInstance().isSocketConnected()) {
            setNetworkConnectionTimer();
        }
        else
        {
            try {
                if (null == jsonUserInfo) {
                    showGenericDialog(mContext,"Something went wrong, Please try again");
                }
                else
                {
                   // createLoginResponseObject(jsonUserInfo.toString());
                    emptyDynamicAffiliateStrings();

                    RummyConstants.doLogin = true;
                    doLoginWithEngineNastro(jsonUserInfo.getString("unique_id"));
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "postEngine of Login -->> " + e.toString());
            }
        }





    }

    // Resetting network connection with engine
    private void startGameEngine() {
        resetNetworkHandler();
        GameEngine.getInstance().start();
    }

    private Handler mNetworkHandler;

    private void resetNetworkHandler() {
        if (this.mNetworkHandler != null) {
            this.mNetworkHandler.removeCallbacks((Runnable) null);
            this.mNetworkHandler.removeCallbacksAndMessages((Object) null);
            this.mNetworkHandler = null;
        }
    }

    private void setNetworkConnectionTimer() {
        this.resetNetworkHandler();
        this.mNetworkHandler = new Handler();
        this.mNetworkHandler.postDelayed(new Runnable() {
            public void run() {
                startGameEngine();
            }
        }, 10);
    }

    //

    String merchant_id = "";
    String userId = "";
    String urlNastro;


    /*private void jsonObjectRequestOne() {
        Log.e("jsonObjectRequestOne", "jsonObjectRequestOne");
        Log.e("checksum_str", checksum_str + " #");


        Map<String, String> params = new HashMap<String, String>();
        params.clear();
        params.put("merchant_id", merchant_id + "");
        params.put("userid", userId + "");
        if (flagNastro.equalsIgnoreCase("checksum")) {
        } else {
            params.put("checksum", checksum_str + "");
        }
        RequestQueue queue = Volley.newRequestQueue(mContext);
        // url
        String url = urlNastro;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        tv_json.setText(response.toString());
                        parseResponse(response);
//                        parseJson(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("vikas","calling error response");
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                tv_json.setText(error.getMessage());
            }
        }) {
            *//**
             * Passing some request headers
             *//*
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000, //TIMEOUT INTERVAL (Default: 2500ms)
                1,    //No.Of Retries (Default: 1)
                1));  //BackOff Multiplier (Default: 1.0)

        queue.add(jsonObjReq);
    }*/


    String status, message, checksum_str = "";

   /* private void parseResponse(JSONObject jsonObjResp) {
        try {
            Log.e("response","vikas "+jsonObjResp.toString());
            if (flagNastro.equalsIgnoreCase("checksum")) {
                status = jsonObjResp.getString("status");
                message = jsonObjResp.getString("message");
                checksum_str = jsonObjResp.getString("checksum_str");
                if (status.equalsIgnoreCase("Success")) {
                    urlNastro = urlUserInfo;
                    flagNastro = "userinfo";
                    jsonObjectRequestOne();
                } else {

                }
            } else {
                Log.e(TAG, "-----else------");
                loginAttempt(jsonObjResp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONException: " + e.getMessage());
        }
    }*/

    JSONObject jsonUserInfo;

    private void loginAttempt(JSONObject response) {
        try {
            jsonUserInfo = new JSONObject(response.toString());
            PrefManager.saveString(context, ACCESS_TOKEN_REST, jsonUserInfo.getString("token"));
            PrefManager.saveString(context, SERVER_IP_REST, jsonUserInfo.getString("ip"));
            PrefManager.saveString(context, UNIQUE_ID_REST, jsonUserInfo.getString("unique_id"));
            PrefManager.saveString(context, "username", jsonUserInfo.getString("username"));
            PrefManager.saveString(context, RummyConstants.CHANGE_USERNAME, jsonUserInfo.getString("change_username"));

            Utils.ENGINE_IP = PrefManager.getString(this, SERVER_IP_REST, "");
            Log.e("ENGINE_IP", Utils.ENGINE_IP + " $");

            PrefManager.saveString(context, "NOSTRO_MERCHANT_ID", merchant_id);
            PrefManager.saveString(context, "NOSTRO_USER_ID", userId);

            Log.e("unique_id", jsonUserInfo.getString("unique_id") + "");

            engineInit();

            /*if (NostraGamusActivity.flagNativeHybird.equalsIgnoreCase("native")) {
                engineInit();
            } else {
                sessionIdWeb = jsonUserInfo.getString("unique_id")+"";
                NostraGamusWebView.WebSessionId = jsonUserInfo.getString("unique_id")+"";
                webPageData();
            }*/

        } catch (Exception e) {
            Log.e(TAG, "EXP: Parsing success response of Login -->> " + e.toString());
        }
    }

    public static String sessionIdWeb = "";
    public void webPageData() {
        String merchant_id = "1111";
        String Name = "suresh.gandham@gridlogic.in";
//        Intent in = new Intent(getApplicationContext(), WebViewJavaScriptActivity.class);
        Intent in = new Intent(getApplicationContext(), NostraGamusWebView.class);
        in.putExtra("merchant_id", merchant_id);
        in.putExtra("Name", Name);
        startActivity(in);
    }

    private void postEngine() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null == jsonUserInfo) {
                        return;
                    }
                    Thread.sleep(1000);
                    createLoginResponseObject(jsonUserInfo.toString());
                    emptyDynamicAffiliateStrings();

                    RummyConstants.doLogin = true;
                    doLoginWithEngineNastro(jsonUserInfo.getString("unique_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "postEngine of Login -->> " + e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    //

    public void OnNastroLogin(View view) {

    }

    public void OnNastroLogout(View view) {
        doLogout();
    }

    private void doLogout() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    //    dynamic json parsing
    /*private void parseJson(JSONObject data) {

        if (data != null) {
            Iterator<String> it = data.keys();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    if (data.get(key) instanceof JSONArray) {
                        JSONArray arry = data.getJSONArray(key);
                        int size = arry.length();
                        for (int i = 0; i < size; i++) {
                            parseJson(arry.getJSONObject(i));
                        }
                    } else if (data.get(key) instanceof JSONObject) {
                        parseJson(data.getJSONObject(key));
                    } else {
                        Log.e(TAG, key + ":" + data.getString(key));
                    }
                } catch (Throwable e) {
                    try {
                        Log.e(TAG, key + ":" + data.getString(key));
                    } catch (Exception ee) {
                    }
                    e.printStackTrace();

                }
            }
        }
    }*/

    private void unregisterEventBus() {
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        this.unregisterEventBus();
    }

}
