package in.glg.rummy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.HeartBeatEvent;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class HeartBeatService extends Service {
    public static final long NOTIFY_INTERVAL = 1000;
    private OnResponseListener heartBeatListener = new OnResponseListener(Event.class) {
        public void onResponse(Object response)
        {
        }
    };
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    class TimeDisplayTimerTask extends TimerTask {

        class C17361 implements Runnable {
            C17361() {
            }

            public void run() {
                HeartBeatService.this.sendHeartBeat();
            }
        }

        TimeDisplayTimerTask() {
        }

        public void run() {
            HeartBeatService.this.mHandler.post(new C17361());
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
        } else {
            this.mTimer = new Timer();
        }
        this.mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    private void sendHeartBeat() {
        LoginResponse userData = ((RummyApplication) getApplication()).getUserData();
        HeartBeatEvent request = new HeartBeatEvent();
        request.setEventName("HEART_BEAT");
        request.setPlayerIn("new_lobby");
        request.setMsg_uuid(Utils.generateUuid());
        request.setNickName(userData.getNickName());
        try {
            Log.d("flow", "Sending HeartBeat");
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(request), this.heartBeatListener);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d("HeartBEat", "EXTRA_TIME" + gameEngineNotRunning.getLocalizedMessage());
        }
    }
}
