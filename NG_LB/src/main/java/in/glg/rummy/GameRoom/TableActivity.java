package in.glg.rummy.GameRoom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.glg.rummy.NetworkProvider.VolleySingleton;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.activities.BaseActivity;
import in.glg.rummy.activities.LoadingActivity;
import in.glg.rummy.activities.NostraJsonActivity;
import in.glg.rummy.adapter.PlayerDiscardCardsAdapter;
import in.glg.rummy.adapter.PointsScoreBoardAdapter;
import in.glg.rummy.adapter.ScoreBoardAdapter;
import in.glg.rummy.adapter.SettingsAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.response.JoinTableResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.fragments.IamBackFragment;
import in.glg.rummy.fragments.TablesFragment;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GameInfo;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.PlayingCard;
import in.glg.rummy.models.ReportBugRequest;
import in.glg.rummy.models.TableDetails;
import in.glg.rummy.utils.EventComparator;
import in.glg.rummy.utils.GamePlayerComparator;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.PrefManagerTracker;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.SoundPoolManager;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;
import in.glg.rummy.utils.VibrationManager;


public class TableActivity extends BaseActivity implements OnClickListener, OnItemSelectedListener {
   private static final String TAG = GameEngine.class.getSimpleName();
   public static int[] settingsImages = new int[]{R.drawable.gamesettings,
           R.drawable.playerdiscard,
           R.drawable.lasthand,
           R.drawable.scoreboard,
           R.drawable.info,
           R.drawable.report};
   public static String[] settingsItems;
   private String CALL_ACTION = "android.intent.action.PHONE_STATE";
   private ArrayList<PlayingCard> discardList;
   private boolean isInOnPause = false;
   private boolean isTableAnimting = false;
   private String mActiveTableId;
   private String mBugType;
   private Spinner mBugTypeSpinner;
   private BroadcastReceiver mCloseActivityReceiver = new C16601();
   private DrawerLayout mDrawerLayout;
   private Event mEvent;
   private View mFirstTableBtn;
   private View mGameInfoView;
   private List<Event> mGameResultsList = new ArrayList();
   private View mGameSettingsView;
   private LinearLayout mGameTablesLayout;
   private Boolean mIamBack = Boolean.valueOf(false);
   private boolean mIsActivityVisble = false;
   private boolean mIsBackPressed = false;
   private List<String> mJoinedTablesIds;
   private HashMap<String, Event> mLastHandMap = new HashMap();
   private View mPlayerCardsView;
   private Spinner mPointsRummySpinner;
   private Spinner mPoolsRummySpinner;
   private View mReportView;
   private FrameLayout mRootLayout;
   private Dialog mScoreBoardDialog;
   private View mSecondTableBtn;
   private ListView mSettingsListView;
   private Button mTable1Btn;
   private Button mTable2Btn;
   private HashMap<String, TableDetails> mTableDetailsList = new HashMap();
   private RummyApplication mRummyApplication;
   private View mVisibleView;
   private OnResponseListener reportListner = new OnResponseListener(JoinTableResponse.class) {
      public void onResponse(Object response) {
      }
   };
   private boolean showCardDistributeAnimation = true;
   private Button soundsOffBtn;
   private boolean soundsOn = false;
   private Button soundsOnBtn;
   private Button vibrationOffBtn;
   private boolean vibrationOn = false;
   private Button vibrationOnBtn;
   private String gameType = "", mTourneyId="";

   private TextView tableIdButton1;
   private TextView tableIdButton2;

   static TableActivity mTableActivity;

   class C16601 extends BroadcastReceiver {
      C16601() {
      }

      public void onReceive(Context context, Intent intent) {
         TableActivity.this.finish();
      }
   }

   class C16612 implements OnClickListener {
      C16612() {
      }

      public void onClick(View v) {
         TableActivity.this.hideNavigationMenu();
      }
   }

   class C16623 implements OnClickListener {
      C16623() {
      }

      public void onClick(View v) {
         TableActivity.this.mScoreBoardDialog.dismiss();
      }
   }

   class C16634 implements OnClickListener {
      C16634() {
      }

      public void onClick(View view) {
         TableActivity.this.dismissScoreBoard();
      }
   }

   class C16645 implements OnClickListener {
      C16645() {
      }

      public void onClick(View v) {
         TableActivity.this.hideVisibleView();
      }
   }

   protected int getLayoutResource() {
      return R.layout.activity_table;
   }

   protected int getToolbarResource() {
      return 0;
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().addFlags(128);
//      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      try {
         init();
         setUpFullScreen();
         setIdsToViews();
         addSettingsHeader();
         setUpSettingsView();
         setUpDropDownUI();

         mTableActivity = this;
      } catch (Exception e) {
         Log.e(TAG, e + "");
         e.printStackTrace();
      }
   }

   public void clearScoreBoard() {
      this.mGameResultsList.clear();
   }

   public void setGameResultEvents(Event event) {
      this.mGameResultsList.add(event);
      if (((TableDetails) this.mTableDetailsList.get(this.mActiveTableId)).getTableType().contains(Utils.PR)) {
         updatePointsScoreBoard();
      } else {
         updateScoreBoard();
      }
   }

   private void setUpGameRoom()
   {
      Log.e(TAG, "Setting game room........");
      this.mRummyApplication = (RummyApplication) getApplication();
      List<String> joinedTables = this.mRummyApplication.getJoinedTableIds();
      updateTableButtons();
      if (!this.mIamBack.booleanValue() || joinedTables.size() <= 1) {
         for (String tableId : joinedTables) {
            if (getFragmentByTag(tableId) == null) {
               launchTableFragment(tableId);
            }
         }
      } else {
         this.showCardDistributeAnimation = false;
         hideGameTablesLayoutOnImaBack();
         launchTableFragment((String) joinedTables.get(0));
         this.showCardDistributeAnimation = true;
      }
      if (this.mIamBack.booleanValue() && isIamBackVisible()==false) {
         hideGameTablesLayoutOnImaBack();
         launchFragment(new IamBackFragment(), IamBackFragment.class.getName());
      }
   }

   private boolean isIamBackVisible()
   {
      IamBackFragment test = (IamBackFragment) getSupportFragmentManager().findFragmentByTag(IamBackFragment.class.getName());
      if (test != null && test.isVisible())
         return true;
      else
         return false;
   }

   private void init() {
      this.mIsActivityVisble = true;
      this.mIamBack = Boolean.valueOf(getIntent().getBooleanExtra("iamBack", false));
      this.gameType = getIntent().getStringExtra("gameType");
      this.mTourneyId = getIntent().getStringExtra("tourneyId");
      this.discardList = new ArrayList();
      this.mJoinedTablesIds = new ArrayList();
      this.mGameResultsList.clear();
      settingsItems = getResources().getStringArray(R.array.settings_items);
      this.mRummyApplication = (RummyApplication) getApplication();
      this.mJoinedTablesIds = this.mRummyApplication.getJoinedTableIds();
      this.mEvent = (Event) getIntent().getSerializableExtra("event");
      Utils.HOME_BACK_PRESSED = false;
      LocalBroadcastManager.getInstance(this).registerReceiver(this.mCloseActivityReceiver, new IntentFilter("CLOSE_ACTIVITIES"));
   }

   public void launchTableFragment(String tableId) {
      Log.e("launchTableF",tableId+"");
      this.mActiveTableId = tableId;
      handleTableButtonClickEvents(tableId);
      TablesFragment tablesFragment = new TablesFragment();
      Bundle args = new Bundle();
      args.putString("tableId", tableId);
      args.putString("gameType", this.gameType);
      args.putString("tourneyId", this.mTourneyId);
      args.putSerializable("event", this.mEvent);
      args.putBoolean("iamBack", this.mIamBack.booleanValue());

      args.putBoolean("canShowAnimation", this.showCardDistributeAnimation);

      tablesFragment.setArguments(args);
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.add(R.id.content_frame, tablesFragment, tableId);
      fragmentTransaction.commit();
   }

   public void updateFragment(String oldTableId, String newtableId, String gameType)
   {
      String firstTableBtnStr = this.mTable1Btn.getText().toString().replaceAll("\\D+", "");
      String secondTableBtnStr = this.mTable2Btn.getText().toString().replaceAll("\\D+", "");
      if (oldTableId.equalsIgnoreCase(firstTableBtnStr)) {
         this.mTable1Btn.setText(String.format("%s%s", new Object[]{"TABLE\n", newtableId}));
         this.tableIdButton1.setText(String.format("%s%s", new Object[]{"TABLE\n", newtableId.substring(newtableId.length() - 4)}));
      } else if (oldTableId.equalsIgnoreCase(secondTableBtnStr)) {
         this.mTable2Btn.setText(String.format("%s%s", new Object[]{"TABLE\n", newtableId}));
         this.tableIdButton2.setText(String.format("%s%s", new Object[]{"TABLE\n", newtableId.substring(newtableId.length() - 4)}));
      }
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      Fragment oldFragment = getFragmentByTag(oldTableId);
      if (oldFragment != null) {
         fragmentTransaction.remove(oldFragment);
         fragmentTransaction.commit();
      }
      if(gameType!=null && gameType.equalsIgnoreCase("tournament"))
         this.gameType = gameType;

      launchTableFragment(newtableId);
      if (getActiveTableId().equalsIgnoreCase(firstTableBtnStr)) {
         handleTableButtonClickEvents(firstTableBtnStr);
      } else if (getActiveTableId().equalsIgnoreCase(secondTableBtnStr)) {
         handleTableButtonClickEvents(secondTableBtnStr);
      }
   }

   private void setUpSettingsView() {
      this.mSettingsListView.setAdapter(new SettingsAdapter(this, settingsImages, settingsItems));
   }

   private void setUpDropDownUI() {
      this.mPoolsRummySpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.pool_rummy_items, R.layout.spinner_item));
      this.mPointsRummySpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.points_rummy_items, R.layout.spinner_item));
      this.mBugTypeSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.report_problem_items, R.layout.spinner_item));
      this.mBugType = getResources().getStringArray(R.array.report_problem_items)[0];
      this.mPoolsRummySpinner.setOnItemSelectedListener(this);
      this.mPointsRummySpinner.setOnItemSelectedListener(this);
      this.mBugTypeSpinner.setOnItemSelectedListener(this);
   }

   private void setIdsToViews() {
      this.mGameTablesLayout = (LinearLayout) findViewById(R.id.game_tables_layout);
      this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      this.mSettingsListView = (ListView) findViewById(R.id.settingsListView);
      this.mGameInfoView = findViewById(R.id.game_info_layout);
      this.mGameSettingsView = findViewById(R.id.game_settings_layout);
      setUpGameSettingsView(this.mGameSettingsView);
      this.mReportView = findViewById(R.id.report_problem_layout);
      this.mPointsRummySpinner = (Spinner) this.mGameSettingsView.findViewById(R.id.points_rummy_spinner);
      this.mPoolsRummySpinner = (Spinner) this.mGameSettingsView.findViewById(R.id.pools_rummy_spinner);
      this.mBugTypeSpinner = (Spinner) this.mReportView.findViewById(R.id.bug_type_spinner);
      ((Button) this.mReportView.findViewById(R.id.submit_report_button)).setOnClickListener(this);
      this.mRootLayout = (FrameLayout) findViewById(R.id.content_frame);
      this.mPlayerCardsView = findViewById(R.id.player_discard_cards_layout);
      this.mRootLayout.setOnClickListener(this);
      this.mPointsRummySpinner.getBackground().setColorFilter(-1, Mode.SRC_ATOP);
      this.mPoolsRummySpinner.getBackground().setColorFilter(-1, Mode.SRC_ATOP);
      this.mBugTypeSpinner.getBackground().setColorFilter(-1, Mode.SRC_ATOP);
      this.mFirstTableBtn = findViewById(R.id.table1Btn);
      this.mSecondTableBtn = findViewById(R.id.table2Btn);
      this.mTable1Btn = (Button) this.mFirstTableBtn.findViewById(R.id.tableButton);
      this.mTable2Btn = (Button) this.mSecondTableBtn.findViewById(R.id.tableButton);
      this.tableIdButton1 = (TextView) this.mFirstTableBtn.findViewById(R.id.tableIdButton);
      this.tableIdButton2 = (TextView) this.mSecondTableBtn.findViewById(R.id.tableIdButton);
      this.mFirstTableBtn.setOnClickListener(this);
      this.mSecondTableBtn.setOnClickListener(this);
   }

   public void handleTableButtonClickEvents(String tableId)
   {
      TablesFragment.myTableId = tableId;
      changeButtonState(tableId, this.mTable1Btn.getText().toString().replaceAll("\\D+", ""), this.mTable2Btn.getText().toString().replaceAll("\\D+", ""));
      for (String id : this.mRummyApplication.getJoinedTableIds()) {
         TablesFragment fragment = (TablesFragment) getFragmentByTag(id);
         if (!(fragment == null || fragment.getTag() == null)) {
            if (tableId.equalsIgnoreCase(id)) {
               this.mActiveTableId = tableId;
               fragment.showQuickAction(id);
               showFragment(fragment);
            } else {
               fragment.hideQuickAction();
               hideFragment(fragment);
            }
         }
      }
   }

   private void changeButtonState(String tableId, String firstTableBtnStr, String secondTableBtnStr) {
      if (firstTableBtnStr.equalsIgnoreCase(tableId)) {
         this.mFirstTableBtn.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.game_table_border));
         this.mSecondTableBtn.setBackgroundDrawable(null);
      } else if (secondTableBtnStr.equalsIgnoreCase(tableId)) {
         this.mSecondTableBtn.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.game_table_border));
         this.mFirstTableBtn.setBackgroundDrawable(null);
      }
   }

   private void setUpGameSettingsView(View gameSettingsView) {
      this.soundsOnBtn = (Button) gameSettingsView.findViewById(R.id.sounds_on_btn);
      this.soundsOffBtn = (Button) gameSettingsView.findViewById(R.id.sounds_off_btn);
      this.vibrationOnBtn = (Button) gameSettingsView.findViewById(R.id.vibration_on_btn);
      this.vibrationOffBtn = (Button) gameSettingsView.findViewById(R.id.vibration_off_btn);
      this.soundsOnBtn.setOnClickListener(this);
      this.soundsOffBtn.setOnClickListener(this);
      this.vibrationOnBtn.setOnClickListener(this);
      this.vibrationOffBtn.setOnClickListener(this);
      boolean soundsOn = PrefManager.getBool(this, "sounds", true);
      setSoundSettings(soundsOn);
      if (soundsOn) {
         setSoundsOnButton();
      } else {
         setSoundsOffButton();
      }
      boolean vibrationOn = PrefManager.getBool(this, "vibration", true);
      setVibrationSettings(vibrationOn);
      if (vibrationOn) {
         setVibrationOnButton();
      } else {
         setVibrationOffButton();
      }
   }

   private void addSettingsHeader() {
      ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.settings_header, this.mSettingsListView, false);
      ((ImageView) header.findViewById(R.id.header_settings_iv)).setOnClickListener(new C16612());
      this.mSettingsListView.addHeaderView(header, null, false);
   }

   @SuppressLint("WrongConstant")
   public void hideNavigationMenu() {
      this.mDrawerLayout.closeDrawer(5);
   }

   public void showGameInfo() {
      this.mVisibleView = this.mGameInfoView;
      this.mGameInfoView.setVisibility(View.VISIBLE);
      slideInView(this.mGameInfoView);
   }

   public void setReportProblem() {
      this.mVisibleView = this.mReportView;
      this.mReportView.setVisibility(View.VISIBLE);
      slideInView(this.mReportView);
   }

   public void setUpPlayerDiscards() {
      updatePlayerDiscards();
      this.mVisibleView = this.mPlayerCardsView;
      this.mPlayerCardsView.setVisibility(View.VISIBLE);
      slideInView(this.mPlayerCardsView);
   }

   public void setUpGameSettings() {
      this.mVisibleView = this.mGameSettingsView;
      slideInView(this.mGameSettingsView);
      this.mGameSettingsView.setVisibility(View.VISIBLE);
      setUpGameSettingsView(this.mGameSettingsView);
   }

   public boolean isSlideMenuVisible() {
      if (this.mVisibleView == null || this.mVisibleView.getVisibility() != View.VISIBLE) {
         return false;
      }
      return true;
   }

   private boolean updateScoreBoard() {
      dismissScoreBoard();
      initScoreBoardDialog();
      ListView scoreBoardList = (ListView) this.mScoreBoardDialog.findViewById(R.id.score_board_lv);
      TextView noEntriesTv = (TextView) this.mScoreBoardDialog.findViewById(R.id.no_entry_found_tv);
      View footerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.score_board_list_item, null, false);
      int footerCount = scoreBoardList.getFooterViewsCount();
      if (scoreBoardList.getFooterViewsCount() > 0) {
         for (int i = 0; i < footerCount; i++) {
            scoreBoardList.removeFooterView(footerView);
         }
      }
      scoreBoardList.addFooterView(footerView);
      View v = this.mScoreBoardDialog.findViewById(R.id.score_board_header_view);
      ((ImageView) this.mScoreBoardDialog.findViewById(R.id.pop_up_close_btn)).setOnClickListener(new C16623());
      TextView player1 = (TextView) v.findViewById(R.id.sb_player_1_tv);
      TextView player2 = (TextView) v.findViewById(R.id.sb_player_2_tv);
      TextView player3 = (TextView) v.findViewById(R.id.sb_player_3_tv);
      TextView player4 = (TextView) v.findViewById(R.id.sb_player_4_tv);
      TextView player5 = (TextView) v.findViewById(R.id.sb_player_5_tv);
      TextView player6 = (TextView) v.findViewById(R.id.sb_player_6_tv);
      ((TextView) footerView.findViewById(R.id.sb_game_no_tv)).setText("Total");
      TextView player1Score = (TextView) footerView.findViewById(R.id.sb_player_1_tv);
      TextView player2Score = (TextView) footerView.findViewById(R.id.sb_player_2_tv);
      TextView player3Score = (TextView) footerView.findViewById(R.id.sb_player_3_tv);
      TextView player4Score = (TextView) footerView.findViewById(R.id.sb_player_4_tv);
      TextView player5Score = (TextView) footerView.findViewById(R.id.sb_player_5_tv);
      TextView player6Score = (TextView) footerView.findViewById(R.id.sb_player_6_tv);
      List<Event> scoreEventList = new ArrayList();
      if (this.mGameResultsList != null && this.mGameResultsList.size() > 0) {
         for (Event event : this.mGameResultsList) {
            if (event.getTableId().equalsIgnoreCase(this.mActiveTableId)) {
               scoreEventList.add(event);
            }
         }
      }
      if (scoreEventList.size() > 0) {
         scoreEventList = Utils.removeDuplicateEvents(scoreEventList);
      }
      if (scoreEventList.size() > 0) {
         Collections.sort(scoreEventList, Collections.reverseOrder(new EventComparator()));
         scoreBoardList.setVisibility(View.VISIBLE);
         noEntriesTv.setVisibility(View.GONE);
         scoreBoardList.setAdapter(new ScoreBoardAdapter(this, scoreEventList));
         for (Event event2 : scoreEventList) {
            int playerCount = event2.getPlayer().size();
            List<GamePlayer> gamePlayerList = event2.getPlayer();
            Collections.sort(gamePlayerList, new GamePlayerComparator());
            switch (playerCount) {
               case 1:
                  GamePlayer gamePlayer = (GamePlayer) gamePlayerList.get(0);
                  player1.setText(gamePlayer.getNick_name());
                  player1Score.setText(WordUtils.capitalize(gamePlayer.getTotalScore()));
                  break;
               case 2:
                  GamePlayer gamePlayer1 = (GamePlayer) gamePlayerList.get(0);
                  GamePlayer gamePlayer2 = (GamePlayer) gamePlayerList.get(1);
                  player1.setText(gamePlayer1.getNick_name());
                  player2.setText(gamePlayer2.getNick_name());
                  player1Score.setText(WordUtils.capitalize(gamePlayer1.getTotalScore()));
                  player2Score.setText(WordUtils.capitalize(gamePlayer2.getTotalScore()));
                  break;
               case 3:
                  GamePlayer p1 = (GamePlayer) gamePlayerList.get(0);
                  GamePlayer p2 = (GamePlayer) gamePlayerList.get(1);
                  GamePlayer p3 = (GamePlayer) gamePlayerList.get(2);
                  player1.setText(p1.getNick_name());
                  player2.setText(p2.getNick_name());
                  player3.setText(p3.getNick_name());
                  player1Score.setText(WordUtils.capitalize(p1.getTotalScore()));
                  player2Score.setText(WordUtils.capitalize(p2.getTotalScore()));
                  player3Score.setText(WordUtils.capitalize(p3.getTotalScore()));
                  break;
               case 4:
                  GamePlayer p14 = (GamePlayer) gamePlayerList.get(0);
                  GamePlayer p24 = (GamePlayer) gamePlayerList.get(1);
                  GamePlayer p34 = (GamePlayer) gamePlayerList.get(2);
                  GamePlayer p44 = (GamePlayer) gamePlayerList.get(3);
                  player1.setText(p14.getNick_name());
                  player2.setText(p24.getNick_name());
                  player3.setText(p34.getNick_name());
                  player4.setText(p44.getNick_name());
                  player1Score.setText(WordUtils.capitalize(p14.getTotalScore()));
                  player2Score.setText(WordUtils.capitalize(p24.getTotalScore()));
                  player3Score.setText(WordUtils.capitalize(p34.getTotalScore()));
                  player4Score.setText(WordUtils.capitalize(p44.getTotalScore()));
                  break;
               case 5:
                  GamePlayer p15 = (GamePlayer) gamePlayerList.get(0);
                  GamePlayer p25 = (GamePlayer) gamePlayerList.get(1);
                  GamePlayer p35 = (GamePlayer) gamePlayerList.get(2);
                  GamePlayer p45 = (GamePlayer) gamePlayerList.get(3);
                  GamePlayer p55 = (GamePlayer) gamePlayerList.get(4);
                  player1.setText(p15.getNick_name());
                  player2.setText(p25.getNick_name());
                  player3.setText(p35.getNick_name());
                  player4.setText(p45.getNick_name());
                  player5.setText(p55.getNick_name());
                  player1Score.setText(p15.getTotalScore());
                  player2Score.setText(p25.getTotalScore());
                  player3Score.setText(p35.getTotalScore());
                  player4Score.setText(p45.getTotalScore());
                  player5Score.setText(p55.getTotalScore());
                  break;
               case 6:
                  GamePlayer p16 = (GamePlayer) gamePlayerList.get(0);
                  GamePlayer p26 = (GamePlayer) gamePlayerList.get(1);
                  GamePlayer p36 = (GamePlayer) gamePlayerList.get(2);
                  GamePlayer p46 = (GamePlayer) gamePlayerList.get(3);
                  GamePlayer p56 = (GamePlayer) gamePlayerList.get(4);
                  GamePlayer p66 = (GamePlayer) gamePlayerList.get(5);
                  player1.setText(p16.getNick_name());
                  player2.setText(p26.getNick_name());
                  player3.setText(p36.getNick_name());
                  player4.setText(p46.getNick_name());
                  player5.setText(p56.getNick_name());
                  player6.setText(p66.getNick_name());
                  player1Score.setText(WordUtils.capitalize(p16.getTotalScore()));
                  player2Score.setText(WordUtils.capitalize(p26.getTotalScore()));
                  player3Score.setText(WordUtils.capitalize(p36.getTotalScore()));
                  player4Score.setText(WordUtils.capitalize(p46.getTotalScore()));
                  player5Score.setText(WordUtils.capitalize(p56.getTotalScore()));
                  player6Score.setText(WordUtils.capitalize(p66.getTotalScore()));
                  break;
               default:
                  break;
            }
         }
         return true;
      }
      scoreBoardList.setVisibility(View.GONE);
      noEntriesTv.setVisibility(View.VISIBLE);
      return false;
   }

   private void initScoreBoardDialog() {
      this.mScoreBoardDialog = new Dialog(this);
      this.mScoreBoardDialog.requestWindowFeature(1);
      this.mScoreBoardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.black_trans85)));
      this.mScoreBoardDialog.setContentView(R.layout.dialog_score_board);
      ((ImageView) this.mScoreBoardDialog.findViewById(R.id.pop_up_close_btn)).setOnClickListener(new C16634());
   }

   public void dismissScoreBoard() {
      if (this.mScoreBoardDialog != null && this.mScoreBoardDialog.isShowing()) {
         this.mScoreBoardDialog.dismiss();
      }
   }

   public void showScoreBoardView() {
      if (((TableDetails) this.mTableDetailsList.get(this.mActiveTableId)).getTableType().contains(Utils.PR)) {
         if (!updatePointsScoreBoard()) {
            showGenericDialog(this, getString(R.string.no_entries_found_msg));
         } else if (this.mScoreBoardDialog != null) {
            this.mScoreBoardDialog.show();
         }
      } else if (!updateScoreBoard()) {
         showGenericDialog(this, getString(R.string.no_entries_found_msg));
      } else if (this.mScoreBoardDialog != null) {
         this.mScoreBoardDialog.show();
      }
   }

   private boolean updatePointsScoreBoard() {
      initScoreBoardDialog();
      ListView scoreBoardList = (ListView) this.mScoreBoardDialog.findViewById(R.id.score_board_lv);
      TextView noEntriesTv = (TextView) this.mScoreBoardDialog.findViewById(R.id.no_entry_found_tv);
      this.mScoreBoardDialog.findViewById(R.id.score_board_header_view).setVisibility(View.GONE);
      View headerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.points_sb_list_header, null, false);
      int headerViewsCount = scoreBoardList.getHeaderViewsCount();
      if (headerViewsCount > 0) {
         for (int i = 0; i < headerViewsCount; i++) {
            scoreBoardList.removeFooterView(headerView);
         }
      }
      scoreBoardList.addHeaderView(headerView);
      List<Event> scoreEventList = new ArrayList();
      if (this.mGameResultsList != null && this.mGameResultsList.size() > 0) {
         for (Event event : this.mGameResultsList) {
            if (event.getTableId().equalsIgnoreCase(this.mActiveTableId)) {
               scoreEventList.add(event);
            }
         }
      }
      if (scoreEventList.size() > 0) {
         scoreEventList = Utils.removeDuplicateEvents(scoreEventList);
      }
      if (scoreEventList.size() > 0) {
         String userId = this.mRummyApplication.getUserData().getUserId();
         List<Event> updatedScoreEventList = new ArrayList();
         for (Event scoreEvent : scoreEventList) {
            for (GamePlayer player : scoreEvent.getPlayer()) {
               if (userId.equalsIgnoreCase(player.getUser_id())) {
                  updatedScoreEventList.add(scoreEvent);
               }
            }
         }
         if (updatedScoreEventList == null || updatedScoreEventList.size() <= 0) {
            return false;
         }
         Collections.sort(updatedScoreEventList, Collections.reverseOrder(new EventComparator()));
         scoreBoardList.setVisibility(View.VISIBLE);
         noEntriesTv.setVisibility(View.GONE);

         scoreBoardList.setAdapter(new PointsScoreBoardAdapter(this, updatedScoreEventList, ((RummyApplication) getApplication()).getUserData().getUserId()));
         return true;
      }
      scoreBoardList.setVisibility(View.GONE);
      noEntriesTv.setVisibility(View.VISIBLE);
      return false;
   }

   public void updateGameId(String tableId, String gameId) {
      TableDetails tableDetails = (TableDetails) this.mTableDetailsList.get(tableId);
      if (tableDetails != null) {
         tableDetails.setGameId(gameId);
      }
   }

   private void handleCloseBtn(View view) {
      ((ImageView) view.findViewById(R.id.pop_up_close_btn)).setOnClickListener(new C16645());
   }

   private void hideVisibleView() {
      if (this.mVisibleView != null) {
         slideOutView(this.mVisibleView);
         this.mVisibleView.setVisibility(View.GONE);
         this.mVisibleView = null;
         TablesFragment fragment = (TablesFragment) getFragmentByTag(this.mActiveTableId);
         if (fragment != null) {
            fragment.showQuickAction(fragment.getTag());
         }
      }
   }

   public void updateTableButtons() {
      List<String> joinedTables = this.mRummyApplication.getJoinedTableIds();
      if (joinedTables.size() == 2) {
         showTableButtons();
         this.mTable1Btn.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(0)}));
         this.mTable2Btn.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(1)}));
         this.tableIdButton1.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(0).substring(joinedTables.get(0).length() - 4)}));
         this.tableIdButton2.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(1).substring(joinedTables.get(1).length() - 4)}));
         this.mSecondTableBtn.performClick();
      } else if (joinedTables.size() == 1) {
         this.mTable1Btn.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(0)}));
         this.tableIdButton1.setText(String.format("%s%s", new Object[]{"TABLE\n", joinedTables.get(0).substring(joinedTables.get(0).length() - 4)}));
         this.mFirstTableBtn.setVisibility(View.VISIBLE);
         this.mSecondTableBtn.setVisibility(View.INVISIBLE);
         this.mFirstTableBtn.performClick();
      }
   }

   public void showTableButtons() {
      this.mFirstTableBtn.setVisibility(View.VISIBLE);
      this.mSecondTableBtn.setVisibility(View.VISIBLE);
   }

   public void onClick(View v) {

      if(v.getId() == R.id.content_frame)
      {
         if (this.mVisibleView != null) {
            hideVisibleView();
            return;
         }
      }
      else if(v.getId() == R.id.table1Btn)
      {
         hideVisibleView();
         setIsAnimating(false);
         ((ImageView) this.mFirstTableBtn.findViewById(R.id.table_flash_iv)).clearAnimation();
         handleTableButtonClickEvents(this.mTable1Btn.getText().toString().replaceAll("\\D+", ""));
      }
      else if(v.getId() == R.id.table2Btn)
      {
         hideVisibleView();
         setIsAnimating(false);
         ((ImageView) this.mSecondTableBtn.findViewById(R.id.table_flash_iv)).clearAnimation();
         handleTableButtonClickEvents(this.mTable2Btn.getText().toString().replaceAll("\\D+", ""));
      }
      else if(v.getId() == R.id.sounds_on_btn)
      {
         setSoundSettings(true);
         setSoundsOnButton();
      }
      else if(v.getId() == R.id.sounds_off_btn)
      {
         setSoundSettings(false);
         setSoundsOffButton();
      }
      else if(v.getId() == R.id.vibration_on_btn)
      {

         setVibrationSettings(true);
         setVibrationOnButton();
      }
      else if(v.getId() == R.id.vibration_off_btn)
      {
         setVibrationSettings(false);
         setVibrationOffButton();
      }
      else if(v.getId() == R.id.submit_report_button)
      {
         reportBugViaRest();
      }

   }

   public void setIsBackPressed(boolean isBackPressed) {
      this.mIsBackPressed = isBackPressed;
   }

   public boolean isBackPressed() {
      return this.mIsBackPressed;
   }

   private void setSoundsOffButton() {
      this.soundsOnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trans50));
      this.soundsOffBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
   }

   private void setSoundsOnButton() {
      this.soundsOffBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trans50));
      this.soundsOnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
   }

   private void setVibrationOnButton() {
      this.vibrationOffBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trans50));
      this.vibrationOnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
   }

   private void setVibrationOffButton() {
      this.vibrationOnBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trans50));
      this.vibrationOffBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
   }

   private void setSoundSettings(boolean soundsOn) {
      PrefManager.saveBool(this, "sounds", soundsOn);
      SoundPoolManager.getInstance().setPlaySound(soundsOn);
   }

   private void setVibrationSettings(boolean vibrationOn) {
      PrefManager.saveBool(this, "vibration", vibrationOn);
      VibrationManager.getInstance().setVibration(vibrationOn);
   }

   private void slideOutView(View view) {
      if (view != null) {
         view.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_right));
      }
   }

   private void slideInView(View view) {
      if (view != null) {
         view.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_left));
         handleCloseBtn(view);
      }
   }

   protected void onStart() {
      super.onStart();
      if (!EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().register(this);
      }
   }

   protected void onStop() {
      super.onStop();
   }

   protected void onPause() {
      super.onPause();
      this.mIsActivityVisble = false;
      if (!isBackPressed()) {
         this.isInOnPause = true;
         GameEngine.getInstance().stop();
         IamBackFragment iamBackFragment = (IamBackFragment) getFragmentByTag(IamBackFragment.class.getName());
         if (iamBackFragment != null) {
            iamBackFragment.clearDiscardedCards();
            removeIamBackFragment(iamBackFragment);
         }
      }
      setIsBackPressed(false);
   }

   protected void onDestroy() {
      super.onDestroy();
      unregisterEventBus();
   }

   @Subscribe
   public void onMessageEvent(Event event) {
      if (event.getEventName().equalsIgnoreCase("OTHER_LOGIN")) {
         TLog.e(TAG, "GAME ROOM : OTHER_LOGIN");
         PrefManager.clearPreferences(getApplicationContext());
         showLongToast(this, getString(R.string.other_login_err_msg));
         launchNewActivityFinishAll(new Intent(this, NostraJsonActivity.class), true);
      }
      String firstTableId = "";
      String secondTableId = "";
      if (this.mJoinedTablesIds.size() == 1) {
         firstTableId = (String) this.mJoinedTablesIds.get(0);
      } else if (this.mJoinedTablesIds.size() == 2) {
         firstTableId = (String) this.mJoinedTablesIds.get(0);
         secondTableId = (String) this.mJoinedTablesIds.get(1);
      }
      if (event.getTableId() == null) {
         return;
      }
      if (!event.getTableId().equalsIgnoreCase(firstTableId) && !event.getTableId().equalsIgnoreCase(secondTableId)) {
         return;
      }
      if (event.getEventName().equalsIgnoreCase("GAME_RESULT") || event.getEventName().equalsIgnoreCase("PRE_GAME_RESULT")) {
         updateLastHandEvent(event.getTableId(), event);
         if(mGameResultsList.size()>0)
         {
            for(int i=0; i<this.mGameResultsList.size(); i++)
            {
               if(this.mGameResultsList.get(i).getGameId().equalsIgnoreCase(event.getGameId())) {
                  this.mGameResultsList.remove(i);
                  this.mGameResultsList.add(event);
               }
               else
                  this.mGameResultsList.add(event);
            }
         }
         else
            this.mGameResultsList.add(event);
         updateScoreBoard();
      } else if (event.getEventName().equalsIgnoreCase("START_GAME")) {
         dismissScoreBoard();
         TableDetails tableDetails = (TableDetails) this.mTableDetailsList.get(event.getTableId());
         if (tableDetails != null) {
            tableDetails.setGameId(event.getGameId());
         }
         clearDiscards(event.getTableId());
      } else if (event.getEventName().equalsIgnoreCase("TABLE_CLOSED")) {
         clearDiscards(event.getTableId());
         updateScoreBoard();
      } else if (event.getEventName().equalsIgnoreCase("GAME_END")) {
         clearDiscards(event.getTableId());
      }
   }

   public Event updateLastHandEvent(String tableId, Event event) {
      return (Event) this.mLastHandMap.put(tableId, event);
   }

   public void setLastHandEvent() {
      Event lastHandEvent = (Event) this.mLastHandMap.get(this.mActiveTableId);
      TablesFragment fragment = (TablesFragment) getFragmentByTag(this.mActiveTableId);
      if (fragment != null) {
         fragment.showLastGameResult(lastHandEvent);
      }
   }

   public String getActiveTableId() {
      return this.mActiveTableId;
   }

   private void updatePlayerDiscards()
   {
      Set<String> playerUserIdList = new HashSet();
      HashMap<String, List<PlayingCard>> playerHashMap = new HashMap();
      if (this.discardList != null && this.discardList.size() > 0) {
         Iterator it = this.discardList.iterator();
         while (it.hasNext()) {
            playerUserIdList.add(((PlayingCard) it.next()).getUserName());
         }
         for (String userId : playerUserIdList) {
            List<PlayingCard> playerDiscards = new ArrayList();
            for (int i = this.discardList.size() - 1; i >= 0; i--) {
               PlayingCard player = (PlayingCard) this.discardList.get(i);
               if (this.mActiveTableId.equalsIgnoreCase(player.getTableId())) {
                  if (userId.equalsIgnoreCase(player.getUserName())) {
                     playerDiscards.add(player);
                  }
                  if (playerDiscards.size() == 5) {
                     break;
                  }
               }
            }
            if (playerDiscards.size() > 0) {
               playerHashMap.put(userId, playerDiscards);
            }
         }
      }
      GridView gridView = (GridView) this.mPlayerCardsView.findViewById(R.id.player_discard_gv);
      if (playerHashMap.size() > 0) {
         gridView = (GridView) this.mPlayerCardsView.findViewById(R.id.player_discard_gv);
         gridView.setVisibility(View.VISIBLE);
         gridView.setAdapter(new PlayerDiscardCardsAdapter(this, playerHashMap));
         return;
      }
      gridView.setVisibility(View.GONE);
   }

   public void addDiscardToPlayer(Event event) {
      String firstTableId = "";
      String secondTableId = "";
      if (this.mJoinedTablesIds.size() == 1) {
         firstTableId = (String) this.mJoinedTablesIds.get(0);
      } else if (this.mJoinedTablesIds.size() == 2) {
         firstTableId = (String) this.mJoinedTablesIds.get(0);
         secondTableId = (String) this.mJoinedTablesIds.get(1);
      }
      if (event.getTableId() == null) {
         return;
      }
      if (event.getTableId().equalsIgnoreCase(firstTableId) || event.getTableId().equalsIgnoreCase(secondTableId)) {
         PlayingCard discardCard = new PlayingCard();
         discardCard.setFace(event.getFace());
         discardCard.setSuit(event.getSuit());
         discardCard.setUserName(event.getNickName());
         discardCard.setTableId(event.getTableId());

         String username = PrefManager.getString(getBaseContext(), "username", "");

         if (event.getAutoPlay() != null && event.getAutoPlay().equalsIgnoreCase("true") && event.getNickName().equalsIgnoreCase(username)) {
            Log.e(TAG, "Not Adding Card");
         } else {
            this.discardList.add(discardCard);
         }
      }
   }

   public void clearDiscards(String tableId) {
      if (this.discardList != null && this.discardList.size() > 0) {
         Iterator<PlayingCard> cardIterator = this.discardList.iterator();
         while (cardIterator.hasNext()) {
            if (((PlayingCard) cardIterator.next()).getTableId().equalsIgnoreCase(tableId)) {
               cardIterator.remove();
            }
         }
      }
   }

   @SuppressLint("WrongConstant")
   public void onBackPressed() {
      if (this.mVisibleView != null) {
         hideVisibleView();
      } else if (this.mDrawerLayout.isDrawerOpen(5)) {
         hideNavigationMenu();
      } else {
         setIsBackPressed(true);
         showLobbyScreen();
      }
   }

   @Subscribe
   public void onMessageEvent(GameEvent event) {
      Log.e("vikas","Table activity event name= "+event);
      if (event.name().equalsIgnoreCase("SERVER_DISCONNECTED")) {

         Log.e("vikas","Table activity server disconnected");

          Date currentTime = Calendar.getInstance().getTime();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
          String dateFormat = sdf.format(currentTime);
          PrefManagerTracker.saveString(this, "enginedisconnect", dateFormat + "");
          TablesFragment.alTrackList.add("enginedisconnect");
          Log.e("enginedisconnect", dateFormat+"");

          PrefManagerTracker.saveString(this, "internetdisconnect", dateFormat + "");
          TablesFragment.alTrackList.add("internetdisconnect");
          Log.e("internetdisconnect", dateFormat+"");

         if (this.isInOnPause) {
            Log.e("vikas","Table activity server disconnected in onpause");
            this.isInOnPause = false;
         } else if (!Utils.HOME_BACK_PRESSED && this.mIsActivityVisble) {
            Log.e("vikas","Table activity server disconnected loading screen");
            disableHearBeat();
            unregisterEventBus();
            navigateToLoadingScreen(true);
         }
      } else if (event.name().equalsIgnoreCase("OTHER_LOGIN")) {
         unregisterEventBus();
         handleOtherLogin();
      }
   }

   private void unregisterEventBus() {
      if (EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().unregister(this);
      }
   }

   private void navigateToLoadingScreen(boolean isSocketDisconnected) {
      this.mRummyApplication.getEventList().clear();
      Intent intent = new Intent(this, LoadingActivity.class);
      intent.putExtra("isSocketDisconnected", isSocketDisconnected);
      startActivity(intent);
   }

   public void setGameInfo() {
      TableDetails tableDetails = (TableDetails) this.mTableDetailsList.get(this.mActiveTableId);
      TLog.e(TAG, "TABLE ID : " + tableDetails.getTableId());
      GameInfo gameInfo = Utils.getGameInfo(tableDetails.getTableType());
      TextView gameType = (TextView) this.mGameInfoView.findViewById(R.id.game_info_game_type_tv);
      TextView variantType = (TextView) this.mGameInfoView.findViewById(R.id.game_info_game_variant_tv);
      TextView tableId = (TextView) this.mGameInfoView.findViewById(R.id.game_info_table_id_tv);
      TextView entryFee = (TextView) this.mGameInfoView.findViewById(R.id.entry_fee_tv);
      TextView middleDrop = (TextView) this.mGameInfoView.findViewById(R.id.middle_drop_tv);
      TextView fullCount = (TextView) this.mGameInfoView.findViewById(R.id.full_count_tv);
      TextView moveTime = (TextView) this.mGameInfoView.findViewById(R.id.move_time_tv);
      TextView extraTime = (TextView) this.mGameInfoView.findViewById(R.id.extra_time_tv);
      TextView maxExtraTime = (TextView) this.mGameInfoView.findViewById(R.id.max_time_tv);
      ((TextView) this.mGameInfoView.findViewById(R.id.first_drop_tv)).setText(gameInfo.getFirstDrop());
      middleDrop.setText(gameInfo.getMiddleDrop());
      fullCount.setText(gameInfo.getFullCount());
      moveTime.setText(gameInfo.getMoveTime());
      extraTime.setText(gameInfo.getExtraTime());
      maxExtraTime.setText(gameInfo.getMaxExtraTime());
      if (tableDetails != null) {
         entryFee.setText(tableDetails.getBet());
         String tableType = tableDetails.getTableType();
         variantType.setText(Utils.getVariantType(tableType));
         gameType.setText(Utils.formatTableName(tableType));
         tableId.setText(tableDetails.getTableId());
         setReportInfo(tableDetails);
         if (tableDetails.getGameId() != null) {
            setGameId(tableDetails.getGameId());
         } else {
            setGameId("");
         }
      }
   }

   private void setGameId(String gameId) {
      if (this.mGameInfoView != null) {
         ((TextView) this.mGameInfoView.findViewById(R.id.game_info_game_id_tv)).setText(gameId);
      }
      if (this.mReportView != null) {
         ((TextView) this.mReportView.findViewById(R.id.report_view_game_id_tv)).setText(gameId);
      }
   }

   private void setReportInfo(TableDetails tableDetails) {
      TextView gameType = (TextView) this.mReportView.findViewById(R.id.report_view_game_type_tv);
      TextView tableId = (TextView) this.mReportView.findViewById(R.id.report_view_table_id_tv);
      if (tableDetails != null) {
         gameType.setText(Utils.formatTableName(tableDetails.getTableType()));
         tableId.setText(tableDetails.getTableId());
      }
   }

   protected void onResume() {
      super.onResume();
      try {
         this.mIsActivityVisble = true;
         setUpGameRoom();
         if (!GameEngine.getInstance().isSocketConnected() && !Utils.HOME_BACK_PRESSED) {
            navigateToLoadingScreen(false);
         }
      } catch (Exception e) {
         e.printStackTrace();
         Log.e(TAG, e + "");
      }
   }

   public void setIamBackFlag() {
      Fragment fragment = getFragmentByTag(IamBackFragment.class.getName());
      if (fragment != null) {
         removeIamBackFragment(fragment);
      }
      this.mIamBack = Boolean.valueOf(false);
      this.showCardDistributeAnimation = false;
      this.mGameTablesLayout.setVisibility(View.VISIBLE);
      for (String id : this.mRummyApplication.getJoinedTableIds()) {
         if (getFragmentByTag(id) == null) {
            launchTableFragment(id);
         }
      }
      this.showCardDistributeAnimation = true;
   }

   public boolean isFromIamBack() {
      return this.mIamBack.booleanValue();
   }

   public void hideGameTablesLayout(String tableId) {
      if (this.mActiveTableId.equalsIgnoreCase(tableId)) {
         this.mGameTablesLayout.setVisibility(View.INVISIBLE);
      }
   }

   public void hideGameTablesLayoutOnImaBack() {
      this.mGameTablesLayout.setVisibility(View.INVISIBLE);
   }

   public void showGameTablesLayoutOnImaBack() {
       Log.e("OnImaBack","showGameTablesLayoutOnImaBack");
      this.mGameTablesLayout.setVisibility(View.INVISIBLE);
      TablesFragment tablesFragment = (TablesFragment) getFragmentByTag(this.mActiveTableId);
      boolean isGameResultsShowing = false;
      if (tablesFragment != null) {
         isGameResultsShowing = tablesFragment.isGameResultsShowing() || tablesFragment.isMeldScreenIsShowing();
      }
      if (!isGameResultsShowing) {
         this.mGameTablesLayout.setVisibility(View.VISIBLE);
         if (tablesFragment != null && tablesFragment.isOpponentValidShow()) {
            tablesFragment.showDeclareHelpView();
         }
      }
   }

   public void showGameTablesLayout(String tableId) {
      if (this.mActiveTableId.equalsIgnoreCase(tableId)) {
         this.mGameTablesLayout.setVisibility(View.VISIBLE);
      }
   }

   public boolean isIamBackShowing()
   {
      return this.mIamBack.booleanValue();
   }

   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
   }

   public Fragment getFragmentByTag(String tag) {
      return getSupportFragmentManager().findFragmentByTag(tag);
   }

   public void updateTableFragment(String tableId) {
      List<String> joinedTables = this.mRummyApplication.getJoinedTableIds();
      for (String id : joinedTables) {
         if (tableId.equalsIgnoreCase(id)) {
            joinedTables.remove(id);
            break;
         }
      }
      if (joinedTables.size() == 0) {
         finish();
      } else if (joinedTables.size() == 1) {
         TablesFragment fragment = (TablesFragment) getFragmentByTag(tableId);
         if (!(fragment == null || fragment.getTag() == null)) {
            resetPlayerIconsOnTableBtn(tableId);
            hideFragment(fragment);
            removeFragment(fragment);
         }
         TablesFragment fragment1 = (TablesFragment) getFragmentByTag((String) joinedTables.get(0));
         showFragment(fragment1);
         updateTableButtons();
         TableDetails tableDetails = fragment1.getTableInfo();
         List<GamePlayer> joinedPlayerList = fragment1.getJoinedPlayerList();
         if (joinedPlayerList != null) {
            for (GamePlayer player : joinedPlayerList) {
               if (tableDetails != null) {
                  setGameTableBtnUI(tableDetails.getTableId(), player, Integer.parseInt(tableDetails.getMaxPlayer()), false);
               }
            }
         }
      }
   }

   public void updateScoreBoard(String tableId, Event event) {
   }

   public void resetPlayerIconsOnTableBtn(String tableId) {
      View view = getActiveTableView(tableId);
      ImageView user2 = (ImageView) view.findViewById(R.id.table_user_2);
      ImageView user3 = (ImageView) view.findViewById(R.id.table_user_3);
      ImageView user4 = (ImageView) view.findViewById(R.id.table_user_4);
      ImageView user5 = (ImageView) view.findViewById(R.id.table_user_5);
      ImageView user6 = (ImageView) view.findViewById(R.id.table_user_6);
      ((ImageView) view.findViewById(R.id.table_user_1)).setImageResource(R.drawable.table_grey_circle);
      user2.setImageResource(R.drawable.table_grey_circle);
      user3.setImageResource(R.drawable.table_grey_circle);
      user4.setImageResource(R.drawable.table_grey_circle);
      user5.setImageResource(R.drawable.table_grey_circle);
      user6.setImageResource(R.drawable.table_grey_circle);
   }

   public void setGameTableBtnUI(String tableId, GamePlayer player, int maxPlayerCount, boolean isLeft) {
      View view = getActiveTableView(tableId);
      ImageView user1 = (ImageView) view.findViewById(R.id.table_user_1);
      ImageView user2 = (ImageView) view.findViewById(R.id.table_user_2);
      ImageView user3 = (ImageView) view.findViewById(R.id.table_user_3);
      ImageView user4 = (ImageView) view.findViewById(R.id.table_user_4);
      ImageView user5 = (ImageView) view.findViewById(R.id.table_user_5);
      ImageView user6 = (ImageView) view.findViewById(R.id.table_user_6);
      if (maxPlayerCount == 2) {
         user2.setVisibility(View.INVISIBLE);
         user3.setVisibility(View.INVISIBLE);
         user5.setVisibility(View.INVISIBLE);
         user6.setVisibility(View.INVISIBLE);
      } else if (maxPlayerCount == 6) {
         user2.setVisibility(View.VISIBLE);
         user3.setVisibility(View.VISIBLE);
         user5.setVisibility(View.VISIBLE);
         user6.setVisibility(View.VISIBLE);
      }
      switch (Integer.parseInt(player.getSeat())) {
         case 1:
            if (isLeft) {
               user1.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user1.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         case 2:
            if (isLeft) {
               user2.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user2.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         case 3:
            if (isLeft) {
               user3.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user3.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         case 4:
            if (isLeft) {
               user4.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user4.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         case 5:
            if (isLeft) {
               user5.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user5.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         case 6:
            if (isLeft) {
               user6.setImageResource(R.drawable.table_grey_circle);
               return;
            } else {
               user6.setImageResource(R.drawable.table_orange_circle);
               return;
            }
         default:
            return;
      }
   }

   private View getActiveTableView(String tableId) {
      String tableId1 = this.mTable1Btn.getText().toString().replaceAll("\\D+", "");
      View view = this.mFirstTableBtn;
      if (tableId.equalsIgnoreCase(tableId1)) {
         return this.mFirstTableBtn;
      }
      return this.mSecondTableBtn;
   }

   public void setTableDetailsList(TableDetails tableDetails) {
      this.mTableDetailsList.put(tableDetails.getTableId(), tableDetails);
   }

   public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      if (adapterView == this.mBugTypeSpinner) {
         this.mBugType = (String) adapterView.getItemAtPosition(i);
      }
      ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.white));
   }

   public void onNothingSelected(AdapterView<?> adapterView) {
   }

   private void reportProblem() {
      EditText bugExplanation = (EditText) this.mReportView.findViewById(R.id.bug_explanation_et);
      TextView tableId = (TextView) this.mReportView.findViewById(R.id.report_view_table_id_tv);
      TextView gameId = (TextView) this.mReportView.findViewById(R.id.report_view_game_id_tv);
      TextView gameType = (TextView) this.mReportView.findViewById(R.id.report_view_game_type_tv);
      String bugExplanationStr = bugExplanation.getText().toString();
      if (bugExplanationStr == null || bugExplanationStr.length() <= 0) {
         bugExplanation.setError("Please enter your report");
         return;
      }
      ReportBugRequest reportBugRequest = new ReportBugRequest();
      reportBugRequest.setEventName("REPORT_BUG");
      reportBugRequest.setTableId(tableId.getText().toString());
      reportBugRequest.setGameId(gameId.getText().toString());
      reportBugRequest.setGameType(gameType.getText().toString());
      reportBugRequest.setUuid(Utils.generateUuid());
      reportBugRequest.setBugExplanation(bugExplanation.getText().toString());
      reportBugRequest.setBugType(this.mBugType);
      try {
         GameEngine.getInstance();
         GameEngine.sendRequestToEngine(getApplicationContext(), Utils.getObjXMl(reportBugRequest), this.reportListner);
      } catch (GameEngineNotRunning gameEngineNotRunning) {
         TLog.d(TAG, "discardCard" + gameEngineNotRunning.getLocalizedMessage());
      }
      showGenericDialog(this, "Thanks for reporting problem.");
      bugExplanation.setText("");
   }

   private void reportBugViaRest()
   {
      final String TOKEN = PrefManager.getString(getApplicationContext(), RummyConstants.ACCESS_TOKEN_REST, "");
      EditText bugExplanation = (EditText) this.mReportView.findViewById(R.id.bug_explanation_et);
      final TextView tableId = (TextView) this.mReportView.findViewById(R.id.report_view_table_id_tv);
      final TextView gameId = (TextView) this.mReportView.findViewById(R.id.report_view_game_id_tv);
      final TextView gameType = (TextView) this.mReportView.findViewById(R.id.report_view_game_type_tv);
      final Spinner bugType = (Spinner) this.mReportView.findViewById(R.id.bug_type_spinner);
      final String bugExplanationStr = bugExplanation.getText().toString();
      if (bugExplanationStr == null || bugExplanationStr.length() <= 0) {
         bugExplanation.setError("Please enter your report");
         return;
      }

      try
      {
         RequestQueue queue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

         String apiURL = Utils.SERVER_ADDRESS+"api/v1/bug-report/";

         final StringRequest stringRequest = new StringRequest(Request.Method.POST, apiURL,
                 new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                       Log.d(TAG, "Response: "+response.toString());

                       try{
                          JSONObject jsonObject = new JSONObject(response);
                          trackRABEventWE();
                          Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                          hideVisibleView();
                       }
                       catch (Exception e){
                       }

                    }
                 },
                 new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       Log.d(TAG, "Error Resp: " + error.toString());

                       NetworkResponse response = error.networkResponse;
                       if (error instanceof ServerError && response != null) {
                          try {
                             String res = new String(response.data,
                                     HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                             Log.d(TAG, "Error: "+res);
                             if(res != null) {
                                try{
                                   JSONObject json = new JSONObject(res.toString());
                                   if(json.getString("status").equalsIgnoreCase("Error"))
                                      Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();

                                   hideVisibleView();
                                }
                                catch (Exception e){
                                   Log.e(TAG, "EXP: parsing error for login -->> "+e.toString());
                                }
                             }
                          } catch (UnsupportedEncodingException e1) {
                             // Couldn't properly decode data to string
                             e1.printStackTrace();
                          }
                       }
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

            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
               Map<String, String> params = new HashMap<String, String>();
               params.put("tableid", tableId.getText().toString());
               params.put("gametype", gameType.getText().toString());
               params.put("bugtype", bugType.getSelectedItem().toString());
               params.put("message", bugExplanationStr);
               params.put("gameid", gameId.getText().toString());
               params.put("device_type", getDeviceType());
               params.put("client_type", Utils.CLIENT_TYPE);
               params.put("version", Utils.getVersionCode(getApplicationContext()));

               return params;
            }
         };

         stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                 DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //TIMEOUT INTERVAL (Default: 2500ms)
                 2,    //No.Of Retries (Default: 1)
                 2));  //BackOff Multiplier (Default: 1.0)

         //add request to queue
         queue.add(stringRequest);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void flashButton(String tableId) {
      String firstTableBtnStr = this.mTable1Btn.getText().toString().replaceAll("\\D+", "");
      String secondTableBtnStr = this.mTable2Btn.getText().toString().replaceAll("\\D+", "");
      if (firstTableBtnStr.equalsIgnoreCase(tableId) && !tableId.equalsIgnoreCase(this.mActiveTableId)) {
         animateTable(this.mFirstTableBtn);
      } else if (secondTableBtnStr.equalsIgnoreCase(tableId) && !tableId.equalsIgnoreCase(this.mActiveTableId)) {
         animateTable(this.mSecondTableBtn);
      }
   }

   private void animateTable(View view) {
      if (view != null) {
         ImageView flashOImage = (ImageView) view.findViewById(R.id.table_flash_iv);
         Animation animation = new AlphaAnimation(1.0f, 0.0f);
         animation.setDuration(300);
         animation.setInterpolator(new LinearInterpolator());
         animation.setRepeatCount(-1);
         animation.setRepeatMode(2);
         flashOImage.startAnimation(animation);
      }
   }

   public boolean canStopAnimateTable() {
      return this.isTableAnimting;
   }

   private void setIsAnimating(boolean isAnimating) {
      this.isTableAnimting = isAnimating;
   }

   public void closeSettingsMenu() {
      hideNavigationMenu();
      dismissScoreBoard();
      hideVisibleView();
   }

   private void trackRABEventWE()
   {
      Map<String, Object> map = new HashMap<>();
      map.put(MyWebEngage.USER_ID, PrefManager.getString(getBaseContext(), RummyConstants.PLAYER_USER_ID, ""));
      map.put(MyWebEngage.DEVICE_TYPE, getDeviceType());
      map.put(MyWebEngage.CLIENT_TYPE, Utils.CLIENT_TYPE);

      MyWebEngage.trackWEEvent(MyWebEngage.REPORT_BUG, map);
   }

   public static TableActivity getInstance(){
      return mTableActivity;
   }
}
