package in.glg.rummy.activities;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import in.glg.rummy.GameRoom.TableActivity;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.api.requests.LoginRequest;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.fragments.HomeFragment;
import in.glg.rummy.fragments.LobbyFragment;
import in.glg.rummy.fragments.TournamentDetailsFragment;
import in.glg.rummy.fragments.TournamentsFragment;
import in.glg.rummy.interfaces.RefreshBankAccountList;
import in.glg.rummy.models.EngineRequest;
import in.glg.rummy.service.NetworkChangeReceiver;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class HomeActivity extends BaseActivity implements OnCheckedChangeListener, NetworkChangeReceiver.OnConnectionChangeListener, RefreshBankAccountList,
        TournamentDetailsFragment.RefreshTournamentsList {
    public static int flagBackKey = 0;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private boolean isBackPressed = false;
    private RummyApplication mApplication;
    private boolean mIsActivityVisble = false;
    private boolean mIsYourTurn = false;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private RadioGroup mRadioGroup;
    private String mSelectedVariant = RummyConstants.POOLS_RUMMY;
    private RelativeLayout mainLayout;

    private RummyApplication mRummyApplication;

    private RadioButton rb_home, rb_home_image;
    private RadioButton rb_lobby, rb_lobby_image;
    private RadioButton rb_tables, rb_tables_image;
    private RadioButton rb_support, rb_support_image;
    private RadioButton rb_more, rb_more_image;
    private ArrayList<RadioButton> rb_list;
    private ArrayList<RadioButton> rb_list_images;

    private BroadcastReceiver mTurnUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HomeActivity.this.mIsYourTurn = intent.getBooleanExtra("turn_update", false);
            HomeActivity.this.setTablesTab();
        }
    };

    static HomeActivity mHomeActivity;

    public void launchFragment(Fragment fragment, String tag) {
        this.mIsActivityVisble = true;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.home_content_frame, fragment, tag);
        //fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void navigateToLoadingScreen(boolean isSocketDisconnected) {
        disableHearBeat();
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra("isSocketDisconnected", isSocketDisconnected);
        startActivity(intent);
    }


    private void refreshLobby() {
        if (Utils.isFromSocketDisconnection && getLastCheckedRadioButton() == R.id.lobby) {
            this.mRadioGroup.check(R.id.home);
            this.mRadioGroup.check(R.id.lobby);
        }
        Utils.isFromSocketDisconnection = false;
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public int getLastCheckedRadioButton() {
        return PrefManager.getInt(this, "lastCheckedItem", R.id.home);
    }

    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    protected int getToolbarResource() {
        return R.id.toolbar;
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {

        if(checkedId == R.id.home)
        {
            saveLastCheckedRadioButton(checkedId);
            if (LoginRequest.flash.equalsIgnoreCase("1")) {
                launchFragment(new HomeFragment(), HomeFragment.class.getName());
            } else {
                launchFragment(new HomeFragment(), HomeFragment.class.getName());
//                    launchFragment(new HomeFragmentScroller(), HomeFragmentScroller.class.getName());
            }
            toggleSelectorLabels(rb_home);
            mainLayout.setBackground(getResources().getDrawable(R.drawable.ic_bg_all));
        }
        else if(checkedId == R.id.lobby)
        {
            saveLastCheckedRadioButton(checkedId);
            Fragment fragment = new LobbyFragment();
            Bundle bundle = new Bundle();
            bundle.putString("game_variant", this.mSelectedVariant);
            fragment.setArguments(bundle);
            launchFragment(fragment, LobbyFragment.class.getName());
            toggleSelectorLabels(rb_lobby);
            mainLayout.setBackground(getResources().getDrawable(R.drawable.ic_bg_all));
        }
        else if(checkedId == R.id.tables)
        {
            List<String> joinedTableList = ((RummyApplication) getApplication()).getJoinedTableIds();
            if (joinedTableList != null && joinedTableList.size() > 0) {
                Intent playIntent = new Intent(this, TableActivity.class);
                playIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(playIntent);
                this.mRadioGroup.check(getLastCheckedRadioButton());
                return;
            }
        }
        else if(checkedId == R.id.more)
        {
            saveLastCheckedRadioButton(checkedId);
          //  launchFragment(new MoreFragmentNew(), MoreFragmentNew.class.getName());
            toggleSelectorLabels(rb_more);
            mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
        }
        else if(checkedId == R.id.support)
        {
            saveLastCheckedRadioButton(checkedId);
          //  launchFragment(new SupportFragment(), SupportFragment.class.getName());
            toggleSelectorLabels(rb_support);
            mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
        }
        else if(checkedId == R.id.tournaments)
        {
            Log.e("checkedId", checkedId + "");
            saveLastCheckedRadioButton(checkedId);
            launchFragment(new TournamentsFragment(), TournamentsFragment.class.getName());
        }


    }

    public void TestLog() {
        Log.e("TestLog", "TestLog");
//        saveLastCheckedRadioButton(2131297534);
        launchFragment(new TournamentsFragment(), TournamentsFragment.class.getName());
    }

    public void toggleSelectorLabels(RadioButton btn) {
        for (int i = 0; i < rb_list.size(); i++) {
            if (rb_list.get(i) == btn) {
                rb_list.get(i).setTextColor(getResources().getColor(R.color.home_bottom_font));
                //rb_list.get(i).setBackgroundColor(getResources().getColor(R.color.black));
                rb_list_images.get(i).setChecked(true);
            } else {
                rb_list.get(i).setTextColor(getResources().getColor(R.color.white));
                //rb_list.get(i).setBackgroundColor(getResources().getColor(R.color.transparent));
                rb_list_images.get(i).setChecked(false);
            }
        }
    }

    public void onConnectionChange(boolean isConnected) {
        TLog.e(TAG, "isConnected :: " + isConnected);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PrefManager.saveInt(this, "tableCost", 1);
        mHomeActivity = this;

        setUpFullScreen();

        setIdsToViews();

        this.mRummyApplication = (RummyApplication) getApplication();

        this.mIsActivityVisble = true;
        this.mApplication = (RummyApplication) getApplication();
        if (getIntent() != null && getIntent().getBooleanExtra("isFromReg", false)) {
            showSuccessPopUp();
        }
        this.mRadioGroup = (RadioGroup) findViewById(R.id.tab_group);
        this.mRadioGroup.setOnCheckedChangeListener(this);
        this.mRadioGroup.check(R.id.lobby);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mTurnUpdateReceiver, new IntentFilter("TURN_UPDATE_EVENT"));

        RadioButton moreBtn = (RadioButton) findViewById(R.id.more);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLastCheckedRadioButton(view.getId());
               // launchFragment(new MoreFragmentNew(), MoreFragmentNew.class.getName());
            }
        });

    }

    private void setIdsToViews() {
        rb_home = findViewById(R.id.home);
        rb_home_image = findViewById(R.id.rb_home_image);
        rb_lobby = findViewById(R.id.lobby);
        rb_lobby_image = findViewById(R.id.rb_lobby_image);
        rb_tables = findViewById(R.id.tables);
        rb_tables_image = findViewById(R.id.rb_table_image);
        rb_support = findViewById(R.id.support);
        rb_support_image = findViewById(R.id.rb_support_image);
        rb_more = findViewById(R.id.more);
        rb_more_image = findViewById(R.id.rb_more_image);
        mainLayout = findViewById(R.id.main_layout);

        rb_list = new ArrayList<>();
        rb_list.add(rb_home);
        rb_list.add(rb_lobby);
        rb_list.add(rb_tables);
        rb_list.add(rb_support);
        rb_list.add(rb_more);

        rb_list_images = new ArrayList<>();
        rb_list_images.add(rb_home_image);
        rb_list_images.add(rb_lobby_image);
        rb_list_images.add(rb_tables_image);
        rb_list_images.add(rb_support_image);
        rb_list_images.add(rb_more_image);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.disableHearBeat();
        this.unregisterEventBus();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mTurnUpdateReceiver);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("back_button", "CODE: " + keyCode);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Log.e("back_button", "Count: " + count);

        if (keyCode == 4) {
            exitDialog();
        }
        return false;
    }

    public void exitLogic() {
        if(LoadingActivity.getInstance() != null)
        {
            LoadingActivity.getInstance().finish();
        }
        finish();

        Utils.HOME_BACK_PRESSED = true;
        this.isBackPressed = true;
        unregisterEventBus();
        GameEngine.getInstance().stop();
    }

    public void exitDialog() {
        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        Button yes_btn = (Button) dialog.findViewById(R.id.yes_btn);
        Button no_btn = (Button) dialog.findViewById(R.id.no_btn);

        title.setText("Do You Want To Exit?");

        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                exitLogic();
            }
        });

        dialog.show();
    }


    @Subscribe
    public void onMessageEvent(GameEvent event) {
        Log.e("vikas","Home activity event name= "+event);
        if (!this.isBackPressed && this.mIsActivityVisble) {
            if (event.name().equalsIgnoreCase("SERVER_DISCONNECTED") && !this.isBackPressed) {
                Log.e("vikas","Home activity server disconnected");
                disableHearBeat();
                unregisterEventBus();
                navigateToLoadingScreen(true);
            } else if (event.name().equalsIgnoreCase("OTHER_LOGIN")) {
                Log.e("vikas","Home activity Other login");
                unregisterEventBus();
                handleOtherLogin();
            }
        }
    }

    protected void onPause() {
        super.onPause();
        Log.e("vikas","Home activity on puase calling");
        this.mIsActivityVisble = false;
    }

    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Utils.SHOW_LOBBY) {
            this.mRadioGroup.check(R.id.lobby);
            Utils.SHOW_LOBBY = false;
        }
        this.mIsActivityVisble = true;
        registerEventBus();
        if (!GameEngine.getInstance().isSocketConnected()) {
            navigateToLoadingScreen(false);
        }
        setTablesTab();
        refreshLobby();

    }

    protected void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    protected void onStop() {
        super.onStop();
        this.unregisterEventBus();
    }

    public void saveLastCheckedRadioButton(int id) {
        PrefManager.saveInt(this, "lastCheckedItem", id);
    }

    public void setGameVariant(String var1) {
        this.mSelectedVariant = var1;
    }

    public void setTablesTab() {
        this.mApplication = (RummyApplication) getApplication();
        List<String> joinedTableIds = this.mApplication.getJoinedTableIds();
        RadioButton tablesButton = (RadioButton) findViewById(R.id.tables);
        if (joinedTableIds == null && joinedTableIds.size() == 0) {
            this.mIsYourTurn = false;
        }
        if (this.mIsYourTurn) {
            //tablesButton.setBackgroundResource(R.drawable.tables_on_alert);
            rb_tables_image.setBackgroundResource(R.drawable.tables_on_alert);
            tablesButton.setEnabled(true);
        } else if (joinedTableIds == null || joinedTableIds.size() <= 0) {
            //tablesButton.setBackgroundResource(R.drawable.table_off);
            rb_tables_image.setBackgroundResource(R.drawable.table_off);
            tablesButton.setEnabled(false);
        } else {
            //tablesButton.setBackgroundResource(R.drawable.table_on);
            rb_tables_image.setBackgroundResource(R.drawable.table_on);
            tablesButton.setEnabled(true);
        }
    }

    public void showFragment(int id) {
        this.mRadioGroup.check(id);
    }

    @Override
    public void refreshTourneyList() {
        try {
            TournamentsFragment frag = (TournamentsFragment)
                    getSupportFragmentManager().findFragmentByTag(TournamentsFragment.class.getName());
            frag.getTournamentsData();
        } catch (Exception e) {
            Log.e(TAG, "EXP: refreshTourneyList -->> " + e.toString());
        }
    }

    @Subscribe
    public void onMessageEvent(EngineRequest engineRequest) {
        String command = engineRequest.getCommand();

        if (command.equalsIgnoreCase("request_join_table")) {
            HomeActivity.this.mRummyApplication.setJoinedTableIds(engineRequest.getTableId());
            Intent playIntent = new Intent(HomeActivity.this, TableActivity.class);
            playIntent.putExtra("iamBack", false);
            playIntent.putExtra("tableId", engineRequest.getTableId());
            playIntent.putExtra("gameType", "tournament");
            playIntent.putExtra("tourneyId", engineRequest.getTournament_id());
            playIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(playIntent);
        }
    }

    @Override
    public void sendData() {
        FragmentManager fm = getSupportFragmentManager();
       // WithdrawFragment frag = (WithdrawFragment) fm.findFragmentByTag(WithdrawFragment.class.getName());
    }

    public static HomeActivity getInstance() {
        return mHomeActivity;
    }
}
