package in.glg.rummy.engine;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.Util;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.fragments.TablesFragment;
import in.glg.rummy.models.AuthReq;
import in.glg.rummy.models.EngineRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.utils.PrefManagerTracker;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class GameEngine
{
    Context mContext;
    private static final String TAG = GameEngine.class.getName();
    private static OnResponseListener eventListener = new OnResponseListener(Event.class) {
        public void onResponse(Object response) {
            if(response != null) {
                Utils.sendEvent((Event)response);
            }
        }
    };
    private static GameEngine gameEngine;
    private static OnResponseListener requestListner = new OnResponseListener(EngineRequest.class) {
        public void onResponse(Object var1) {
            if(var1 != null) {
                Utils.sendRequest((EngineRequest)var1);
            }
        }
    };
    private static OnResponseListener responseListener;
    private AuthReq authReq;
    private String msgUUID;
    private InputStream inputStream;
    private boolean isConnected = false;
    private boolean isDataDeliverReady = false;
    private boolean isHavingAuthRequest = false;
    private boolean isOtherLogin = false;
    private OutputStream outputStream;
    private String response = "";
    private AsyncSocket socket;
    private Socket tcpSocket = null;

    // $FF: synthetic method
    static InputStream access$200(GameEngine var0) {
        return var0.inputStream;
    }

    public static GameEngine getInstance() {
        if(gameEngine == null) {
            gameEngine = new GameEngine();
        }
        return gameEngine;
    }

    private void handleConnectionResponses(Exception exp)
    {
        if(exp != null) {
            throw new RuntimeException(exp);
        } else {
            this.socket.setDataCallback(new DataCallback() {
                public void onDataAvailable(DataEmitter var1, ByteBufferList var2) {
                    int var3 = 0;
                    byte[] var7 = var2.getAllByteArray();
                    String var8 = new String(var7, 0, var7.length);

                    if(var8.endsWith("\u0000"))
                    {
                        GameEngine.this.isDataDeliverReady = true;
                    }

                    GameEngine.this.response = GameEngine.this.response + var8;
                    if(!response.contains("HEART_BEAT") && !response.contains("gamesetting_update")){
//                        Log.e("HEART_BEAT",response);
                    }

                    if(GameEngine.this.isDataDeliverReady)
                    {
                        GameEngine.this.isDataDeliverReady = false;
                        if(GameEngine.this.authReq == null && GameEngine.this.response.startsWith("<authreq"))
                        {
                            GameEngine.this.authReq = (AuthReq)Utils.getObject(GameEngine.this.response, AuthReq.class);
                            GameEngine.this.isHavingAuthRequest = true;
                            GameEngine.this.isConnected = true;
                            if(GameEngine.this.authReq != null)
                            {
                                Utils.sendEvent(GameEvent.SERVER_CONNECTED);

                                Context tableFragmentContext = TablesFragment.getTableFragment();
                                if(null == tableFragmentContext)
                                    return;

                                Date currentTime = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                String dateFormat = sdf.format(currentTime);
                                PrefManagerTracker.saveString(tableFragmentContext, "engineconnect", dateFormat + "");
                                TablesFragment.alTrackList.add("engineconnect");
                                Log.e("engineconnect", dateFormat+"");

                                PrefManagerTracker.saveString(tableFragmentContext, "internetconnect", dateFormat + "");
                                TablesFragment.alTrackList.add("internetconnect");
                                Log.e("internetconnect", dateFormat+"");
                            }
                        }
                        else
                        {
                            String[] response = GameEngine.this.response.split("\u0000");

                            for(int i = response.length; var3 < i; ++var3)
                            {
                                String var6 = response[var3];
                                if(var6.startsWith("<reply"))
                                {
                                    TLog.d(GameEngine.TAG, "Reply  : " + var6);
                                    GameEngine.responseListener.sendMessage(GameEngine.responseListener.getResponseMessage(var6));
                                }
                                else if(var6.startsWith("<event"))
                                {
                                    Event var5 = (Event)Utils.getObject(var6, Event.class);

                                    // to log the event
                                    if(!var5.getEventName().equalsIgnoreCase("gamesetting_update") && !var5.getEventName().equalsIgnoreCase("HEART_BEAT"))
                                        TLog.d(GameEngine.TAG, "Event  : " + var6);

                                    if(var5.getEventName().equalsIgnoreCase("get_table_details")) {
                                        Utils.tableDetailsList.add(var5);
                                    }

                                    if(var5.getEventName().equalsIgnoreCase("players_rank")) {
                                        Utils.tableDetailsList.add(var5);
                                    }

                                    if(var5.getEventName().equalsIgnoreCase("TOURNEY_BALANCE")) {
                                        Utils.tableDetailsList.add(var5);
                                    }

                                    if(var5.getEventName().equalsIgnoreCase("OTHER_LOGIN")) {
                                        GameEngine.this.isOtherLogin = true;
                                    }

                                    if(var5.getEventName().equalsIgnoreCase("BALANCE_UPDATE")) {
                                        Utils.tableDetailsList.add(var5);
                                        Log.e(TAG+"","BALANCE_UPDATE");
                                    }
                                    // catching SHOW request
                                    try {
                                        if (var5.getEventName().equalsIgnoreCase("show")) {
                                            Utils.SHOW_EVENT = var5;
                                            Log.w(TAG, "SHOW EVENT CAUGHT");
                                        }
                                    } catch (Exception e){
                                        Log.e(TAG, "EXP: catching show request-->> "+e.toString());
                                    }

                                    /*// catching SEND_DEAL request
                                    try {
                                        if (var5.getEventName().equalsIgnoreCase("SEND_DEAL")) {
                                            Log.w(TAG, "SEND_DEAL EVENT CAUGHT------");
                                            Utils.DEAL_SENT = true;
                                        }
                                    } catch (Exception e){
                                        Log.e(TAG, "EXP: catching SEND_DEAL request-->> "+e.toString());
                                    }*/

                                    GameEngine.eventListener.sendMessage(GameEngine.eventListener.getResponseMessage(var6));
                                }
                                else if(var6.startsWith("<request"))
                                {
                                    TLog.d(GameEngine.TAG, "Request from engine : " + var6);
                                    Utils.getObject(GameEngine.this.response, EngineRequest.class);
                                    GameEngine.requestListner.sendMessage(GameEngine.requestListner.getResponseMessage(var6));

                                    try {
                                        EngineRequest var5 = (EngineRequest) Utils.getObject(var6, EngineRequest.class);
                                        if (var5.getCommand().equalsIgnoreCase("meld")) {
                                            Utils.MELD_REQUEST = var5;
                                        }
                                    } catch (Exception e){
                                        Log.e(TAG, "EXP: handling meld request-->> "+e.toString());
                                    }
                                }
                            }
                        }

                        GameEngine.this.response = "";
                    }

                    var2.recycle();
                }
            });
            this.socket.setClosedCallback(new CompletedCallback() {
                public void onCompleted(Exception var1) {
                    TLog.d(GameEngine.TAG, "Socket closed");

                    RummyApplication.userNeedsAuthentication = true;

                    GameEngine.this.isHavingAuthRequest = false;
                    GameEngine.this.stop();
                    GameEngine.this.stopEngine();
                    if(!GameEngine.this.isOtherLogin) {
                        Utils.sendEvent(GameEvent.SERVER_DISCONNECTED);
                    } else {
                        GameEngine.this.isOtherLogin = false;
                        Utils.sendEvent(GameEvent.OTHER_LOGIN);
                    }

                }
            });
            this.socket.setEndCallback(new CompletedCallback() {
                public void onCompleted(Exception var1) {
                    TLog.d(GameEngine.TAG, "Socket Ended");
                }
            });
        }
    }

    private void readResponse(OnResponseListener var1) {
        (new Thread(new ReaderThread(var1))).start();
    }

    public static void sendRequestToEngine(Context ctx, String eventData, OnResponseListener listener) throws GameEngineNotRunning {
        GameEngine ge = getInstance();
        if (ge == null)
        {
            throw new GameEngineNotRunning("Game Engine not running");
        }
        else if (Utils.isNetworkAvailable(ctx))
        {
            ge.sendDataToEngine(ctx, eventData, listener);
        }
        else {
            Log.e("vikas","calling from game engine");
            Toast.makeText(ctx, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    private void tcpAsyncConn()
    {
            InetSocketAddress address = new InetSocketAddress(Utils.ENGINE_IP, 4271);
        AsyncServer.getDefault().connectSocket(address, new ConnectCallback() {
            public void onConnectCompleted(Exception var1, AsyncSocket var2) {
                GameEngine.this.socket = var2;
                if(var1 != null)
                {
                    GameEngine.this.isHavingAuthRequest = false;
                    GameEngine.this.stop();
                    GameEngine.this.stopEngine();
                    Utils.sendEvent(GameEvent.SERVER_DISCONNECTED);
                } else {
                    GameEngine.this.handleConnectionResponses(var1);
                }

            }
        });
    }

    public void stopEngine() {
        this.isConnected = false;
        gameEngine = null;
        this.authReq = null;
    }

    public boolean haveAuthRequest() {
        return this.isHavingAuthRequest;
    }

    public boolean isSocketConnected() {
        return this.isConnected;
    }

    public void sendDataToEngine(Context context, String req, OnResponseListener listener) {
        final String request = req+ "\u0000";
        if(request.startsWith("<request") || request.startsWith("<authrep")) {
            responseListener = listener;
        }

        if(this.socket != null)
        {
            if(!request.contains("HEART_BEAT"))
                TLog.w(TAG, "Req: " + request);

            if(request.contains("authrep")) {
                Log.e(TAG, "User Needs Authentication: " + RummyApplication.userNeedsAuthentication);

                if(RummyApplication.userNeedsAuthentication) {
                    Util.writeAll(this.socket, (byte[]) request.getBytes(), new CompletedCallback() {
                        public void onCompleted(Exception var1) {
                            if (request.contains("authrep")) {
                                Log.e(TAG, "AUTHREP onCompleted");
                                RummyApplication.userNeedsAuthentication = false;
                            }
                            if (var1 != null) {
                                Log.d(GameEngine.TAG, "sendDataToEngine(): + " + var1.getLocalizedMessage());
                                throw new RuntimeException(var1);
                            }
                        }
                    });
                }
            }
            else {
                Util.writeAll(this.socket, (byte[]) request.getBytes(), new CompletedCallback() {
                    public void onCompleted(Exception var1) {
                        if (var1 != null) {
                            Log.d(GameEngine.TAG, "sendDataToEngine(): + " + var1.getLocalizedMessage());
                            throw new RuntimeException(var1);
                        }
                    }
                });
            }
        } else {
            TLog.e(TAG, "Socket is NULL: ");
            Utils.sendEvent(GameEvent.SERVER_DISCONNECTED);
        }

    }

    public void start() {
        (new Thread(new GameThread(null))).start();
    }

    public void stop() {
        this.isConnected = false;
        if(this.socket != null) {
            this.socket.close();
        }

    }

    private class GameThread implements Runnable {
        private GameThread() {
        }
        // $FF: synthetic method
        GameThread(Object var2) {
            this();
        }

        public void run() {
            GameEngine.this.tcpAsyncConn();
        }
    }

    private class ReaderThread implements Runnable {
        private OnResponseListener listener;

        public ReaderThread(OnResponseListener var2) {
            this.listener = var2;
        }

        public void run() {
            // $FF: Couldn't be decompiled
        }
    }
}
