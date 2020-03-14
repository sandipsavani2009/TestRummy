package in.glg.rummy;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.koushikdutta.async.AsyncSocket;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.WebEngageConfig;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import in.glg.rummy.api.response.LobbyTablesResponse;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.models.AuthReq;
import in.glg.rummy.models.Event;
import in.glg.rummy.utils.ISoundPoolLoaded;
import in.glg.rummy.utils.SoundPoolManager;
import in.glg.rummy.utils.Utils;
import in.glg.rummy.utils.VibrationManager;
import io.fabric.sdk.android.Fabric;

public class RummyApplication extends Application {
    String TAG = getClass().getSimpleName()+"";
    private static final String TRIPOD_KEY = "97e7286fba6390f";
    private static boolean activityVisible;
    private AuthReq authReq;
    private int balance;
    private List<Event> eventList = new ArrayList();
    private List<String> joinedTableIds;
    private AsyncSocket socket;
    private LoginResponse userData;
    private LobbyTablesResponse lobbyTablesData;

    public static boolean userNeedsAuthentication = true;

    public LobbyTablesResponse getLobbyTablesData() {
        return lobbyTablesData;
    }

    public void setLobbyTablesData(LobbyTablesResponse lobbyTablesData) {
        this.lobbyTablesData = lobbyTablesData;
    }

    class C16231 implements ISoundPoolLoaded {
        C16231() {
        }

        public void onSuccess() {
            SoundPoolManager.getInstance().setPlaySound(true);
        }
    }

    public List<Event> getEventList() {
        return this.eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void onCreate() {
        super.onCreate();

        initfb();

        try {
            Fabric.with(this, new Crashlytics());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



//        Logger.setLoggable(true);   //setting logger for zendesk


        init();
        initSoundPoolManager();
        initVibrations();
    }

    private void initfb()
    {
        Log.e(TAG,"initfb");
        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                WebEngage.get().setRegistrationID(token);
                Log.e(TAG,token+"");
            }
        });
        initWebEngage();
    }

    private void initWebEngage()
    {
        Log.e(TAG,"initWebEngage");
//        FirebaseApp.initializeApp(this);
        WebEngageConfig webEngageConfig = new WebEngageConfig.Builder()
                .setWebEngageKey(Utils.WEBENGAGE_LICENSE_CODE)
//                .setDebugMode(true)
                // only in development mode
                .build();
//        registerActivityLifecycleCallbacks(new WebEngageActivityLifeCycleCallbacks(this, webEngageConfig));
        WebEngage.engage(this.getApplicationContext(), webEngageConfig);


        /*User weUser = WebEngage.get().user();
        weUser.login("59820");

        weUser.setUserProfile(new UserProfile.Builder()
                .build());

        Map<String, Object> customAttributes = new HashMap<>();
        weUser.setAttributes(customAttributes);*/
    }
    private void initVibrations() {
        VibrationManager.CreateInstance();
        try {
            VibrationManager.getInstance().InitializeVibrator(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        VibrationManager.getInstance().setVibration(true);
    }

    private void initSoundPoolManager() {
        SoundPoolManager.CreateInstance();
        List<Integer> sounds = new ArrayList();
        sounds.add(Integer.valueOf(R.raw.bell));
        sounds.add(Integer.valueOf(R.raw.card_distribute));
        sounds.add(Integer.valueOf(R.raw.clock));
        sounds.add(Integer.valueOf(R.raw.pick_discard));
        sounds.add(Integer.valueOf(R.raw.sit));
        sounds.add(Integer.valueOf(R.raw.toss));
        sounds.add(Integer.valueOf(R.raw.drop));
        sounds.add(Integer.valueOf(R.raw.meld));
        sounds.add(Integer.valueOf(R.raw.winners));
        SoundPoolManager.getInstance().setSounds(sounds);
        try {
            SoundPoolManager.getInstance().InitializeSoundPool(this, new C16231());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void init() {
        this.joinedTableIds = new ArrayList();
        this.eventList = new ArrayList();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        if (event.getTableId() != null) {
            this.eventList.add(event);
        }
    }

    public AuthReq getAuthReq() {
        return this.authReq;
    }

    public void setAuthReq(AuthReq authReq) {
        this.authReq = authReq;
    }

    public void setUserData(LoginResponse userData) {
        this.userData = userData;
    }

    public LoginResponse getUserData() {
        return this.userData;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setSocket(AsyncSocket socket) {
        this.socket = socket;
    }

    public AsyncSocket getSocket() {
        return this.socket;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public List<String> getJoinedTableIds() {
        return this.joinedTableIds;
    }

    public void setJoinedTableIds(String joinedTableId) {
        this.joinedTableIds.add(joinedTableId);
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public void refreshTableIds(String tableId) {
        for (String id : this.joinedTableIds) {
            if (tableId.equalsIgnoreCase(id)) {
                this.joinedTableIds.remove(tableId);
                return;
            }
        }
    }

    public void onTerminate() {
        super.onTerminate();
        clearJoinedTablesIds();
        EventBus.getDefault().unregister(this);
    }

    public void clearJoinedTablesIds() {
        if (this.joinedTableIds != null) {
            this.joinedTableIds.clear();
        }
        if (this.eventList != null) {
            this.eventList.clear();
        }
        if (Utils.tableDetailsList != null) {
            Utils.tableDetailsList.clear();
        }
    }

    public void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void clearEventList() {
        if (this.eventList != null) {
            this.eventList.clear();
        }
    }
}
