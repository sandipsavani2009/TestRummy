package in.glg.rummy.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.adapter.JoinedPlayersAdapter;
import in.glg.rummy.adapter.LevelsAdapter;
import in.glg.rummy.adapter.PrizeListAdapter;
import in.glg.rummy.adapter.TourneyTablesAdapter;
import in.glg.rummy.adapter.WaitingPlayersAdapter;
import in.glg.rummy.adapter.WinnersAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.GetTableDetailsRequest;
import in.glg.rummy.api.requests.TournamentsDetailsRequest;
import in.glg.rummy.api.response.BaseReply;
import in.glg.rummy.api.response.JoinedPlayersResponse;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.api.response.PrizeListResponse;
import in.glg.rummy.api.response.TournamentDetailsResponse;
import in.glg.rummy.api.response.TournamentsListener;
import in.glg.rummy.api.response.TournamentsTablesResponse;
import in.glg.rummy.api.response.TourneyRegistrationResponse;
import in.glg.rummy.api.response.WaitListResponse;
import in.glg.rummy.api.response.WinnerResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.JoinedPlayers;
import in.glg.rummy.models.Levels;
import in.glg.rummy.models.PrizeList;
import in.glg.rummy.models.WaitingPlayers;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class TournamentDetailsFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = TournamentDetailsFragment.class.getName();

    private String mTourneyId;
    private FragmentActivity mContext;
    private RummyApplication mRummyApplication;
    private LoginResponse userData;
    private List<Levels> mLevels;
    private List<PrizeList> mPrizeList;
    private List<JoinedPlayers> mJoinedPlayers;
    private List<WaitingPlayers> mWaitingPlayers;

    private TournamentDetailsResponse mTourneyDetailsResponse;
    private TourneyRegistrationResponse mTourneyRegistrationResponse;
    private TournamentsListener mTournamentsListener;
    private TournamentDetailsResponse mLevelsResponse;

    private Button register_btn_players;
    private Button register_btn_schedules;
    private Button deregister_btn_players;
    private Button deregister_btn_schedules;
    private Button deregister_btn_info;
    private Button register_btn_info;

    private ListView levelsList;
    private ListView prizeList;
    private ListView joinedPlayersList;
    private ListView waitingPlayersList;
    private ListView tourneyTablesList;

    private LinearLayout mPrizeInfo_label;
    private LinearLayout mSchedulesTables_label;
    private LinearLayout mPlayers_label;
    private ImageView mPrizeInfo_iv;
    private ImageView mSchedules_iv;
    private ImageView mPlayers_iv;
    private TextView mPrizeInfo_tv;
    private TextView mSchedules_tv;
    private TextView mPlayers_tv;
    private RelativeLayout prizeInfo_content;
    private RelativeLayout schedules_content;
    private RelativeLayout players_content;

    private TextView tableLabel;
    private TextView schedulesLabel_tv;
    private TextView schedulesValue_tv;
    private TextView joinedPlayers_tv;
    private TextView waitingPlayers_tv;
    private TextView playerInfo_tv;
    private TextView tourneyInfo_tv;
    private TextView prizeAmountLabel_tv;
    private TextView positionLabel_tv;
    private TextView playerLabel_tv;
    private TextView levelStatus_tv;
    private TextView levelCountdown_tv;
    private TextView tourneyStart_tv;

    private TextView tid_tv;
    private TextView tourneyType_tv;
    private TextView entryFee_tv;
    private TextView joined_tv;
    private TextView regCloses_tv;
    private TextView balance_tv;
    private TextView yourRank_tv;
    private TextView level_tv;
    private TextView timeBetweenLevels_tv;
    private TextView status_tv;
    private TextView tourneyCompleteTime_tv;

    private LinearLayout runningStatus;
    private LinearLayout completedStatus;
    private LinearLayout othersStatus;

    private Dialog mLoadingDialog;

    private static boolean prizeInfoSelected = true;
    private static boolean schedulesSelected = false;
    private static boolean playersSelected = false;

    private LevelsAdapter levelsAdapter;
    private PrizeListAdapter prizeListAdapter;
    private WinnersAdapter winnersAdapter;
    private JoinedPlayersAdapter joinedPlayersAdapter;
    private WaitingPlayersAdapter waitingPlayersAdapter;
    private TourneyTablesAdapter tourneyTablesAdapter;

    CountDownTimer levelTimer;

    RefreshTournamentsList mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (RefreshTournamentsList) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RefreshTournamentsList");
        }
    }

    private OnResponseListener tournamentsResponseListener = new OnResponseListener(TournamentsListener.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TournamentDetailsFragment.this.mTournamentsListener = (TournamentsListener) response;

                if (TournamentDetailsFragment.this.mTourneyDetailsResponse.getCode().equalsIgnoreCase("200")) {
                    Log.d(TAG, "Received at: " + tournamentsResponseListener);
                } else {
                    Log.d(TAG, TournamentDetailsFragment.this.mTourneyDetailsResponse.getCode() +
                            " : tournamentsResponseListener");
                }
            }
        }
    };

    private OnResponseListener tournamentsDetailsListener = new OnResponseListener(TournamentDetailsResponse.class) {
        @Override
        public void onResponse(Object response) {
            try {
                if (response != null) {
                    TournamentDetailsFragment.this.mTourneyDetailsResponse = (TournamentDetailsResponse) response;

                    if (TournamentDetailsFragment.this.mTourneyDetailsResponse.getCode().equalsIgnoreCase("200")) {
                        TournamentDetailsFragment.this.mLevels = ((TournamentDetailsResponse) response).getLevels();
                        TournamentDetailsFragment.this.displayTourneyPrizeInfoData();
                        TournamentDetailsFragment.this.populateLevelsData();
                        TournamentDetailsFragment.this.setTimeBetweenNextLevel();
                        TournamentDetailsFragment.this.getLeaderBoard();
                    } else {
                        Log.d(TAG, TournamentDetailsFragment.this.mTourneyDetailsResponse.getCode() +
                                " : tournamentsDetailsListener");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e + "");
            }
        }
    };

    private OnResponseListener winnersResponse = new OnResponseListener(WinnerResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                if (((WinnerResponse) response).getCode().equalsIgnoreCase("200")) {
                    TournamentDetailsFragment.this.showWinnersList(((WinnerResponse) response).getPlayers());
                } else {
                    Log.d(TAG, TournamentDetailsFragment.this.mTourneyDetailsResponse.getCode() +
                            " : winnersResponse");
                }
            }
        }
    };

    private OnResponseListener tournamentTablesResponse = new OnResponseListener(TournamentsTablesResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                if (((TournamentsTablesResponse) response).getCode().equalsIgnoreCase("200")) {
                    TournamentDetailsFragment.this.showRunningTables((TournamentsTablesResponse) response);
                    //TournamentDetailsFragment.this.getLeaderBoard();
                } else {
                    Log.d(TAG, ((TournamentsTablesResponse) response).getCode() +
                            " : tournamentTablesResponse");
                }
            }
        }
    };

    private OnResponseListener leaderBoardResponse = new OnResponseListener(JoinedPlayersResponse.class) {
        @Override
        public void onResponse(Object response) {
            try {
                if (response != null) {
                    if (((JoinedPlayersResponse) response).getCode().equalsIgnoreCase("200")) {
                        TournamentDetailsFragment.this.mJoinedPlayers = ((JoinedPlayersResponse) response).getJoinedPlayers();
                        TournamentDetailsFragment.this.populateJoinedPlayers();
                        TournamentDetailsFragment.this.getPlayerWaitList();
                    } else {
                        Log.d(TAG, ((JoinedPlayersResponse) response).getCode() +
                                " : leaderBoardResponse");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e + "");
            }
        }
    };

    private OnResponseListener prizeListResponse = new OnResponseListener(PrizeListResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                if (((PrizeListResponse) response).getCode().equalsIgnoreCase("200")) {
                    TournamentDetailsFragment.this.mPrizeList = ((PrizeListResponse) response).getPrize_list();
                    TournamentDetailsFragment.this.populatePrizeList();

                    if (TournamentDetailsFragment.this.mTourneyDetailsResponse.getStatus().equalsIgnoreCase("running"))
                        TournamentDetailsFragment.this.getLevelTimer();
                } else {
                    Log.d(TAG, ((PrizeListResponse) response).getCode() +
                            "prizeListResponse");
                }
            }
        }
    };

    private OnResponseListener waitListResponse = new OnResponseListener(WaitListResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                if (((WaitListResponse) response).getCode().equalsIgnoreCase("200")) {
                    TournamentDetailsFragment.this.mWaitingPlayers = ((WaitListResponse) response).getWaitingPlayers();
                    TournamentDetailsFragment.this.populateWaitingPlayers();
                    if (TournamentDetailsFragment.this.mTourneyDetailsResponse.getStatus().equalsIgnoreCase("completed"))
                        TournamentDetailsFragment.this.getWinnerList();
                    else
                        TournamentDetailsFragment.this.getPrizeList();
                } else {
                    Log.d(TAG, ((WaitListResponse) response).getCode() + "waitListResponse");
                }
            }
        }
    };

    private OnResponseListener levelsTimerListener = new OnResponseListener(TournamentDetailsResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TournamentDetailsFragment.this.mLevelsResponse = (TournamentDetailsResponse) response;

                if (TournamentDetailsFragment.this.mLevelsResponse.getCode().equalsIgnoreCase("200")) {
                    if (TournamentDetailsFragment.this.mLevelsResponse.getData().equalsIgnoreCase("get_level_timer")) {
                        Log.d(TAG, "SETTING LEVEL TIMER");
                        TournamentDetailsFragment.this.setLevelTimer();
                        TournamentDetailsFragment.this.getTournamentTables();
                    }
                } else {
                    Log.d(TAG, TournamentDetailsFragment.this.mLevelsResponse.getCode() + " : tournamentsDetailsListener");
                }
            }
        }
    };

    private OnResponseListener tournamentRegistrationResponse = new OnResponseListener(TourneyRegistrationResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TournamentDetailsFragment.this.hideProgress();
                TournamentDetailsFragment.this.mTourneyRegistrationResponse = (TourneyRegistrationResponse) response;

                if (TournamentDetailsFragment.this.mTourneyRegistrationResponse != null
                        && TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("200")) {
                    if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getData().equalsIgnoreCase("register_tournament")) {
                        TournamentDetailsFragment.this.mTourneyDetailsResponse.setTourney_chips(TournamentDetailsFragment.this.mTourneyRegistrationResponse.getTournament_chips());
                        TournamentDetailsFragment.this.getTournamentDetails();
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Congratulations! You have been registered for the tourney and you have " + ((TourneyRegistrationResponse) response).getTournament_chips() + " T. chips for the tournament.");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getData().equalsIgnoreCase("deregister_tournament")) {

                        TournamentDetailsFragment.this.getTournamentDetails();
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Deregistered from tournament !");
                    }

                } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse != null) {
                    if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7013")) {
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("You cannot cancel your registration request since the tourney is going to start");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("507")) {
                        if (mTourneyDetailsResponse.getTourney_cost().equalsIgnoreCase("LOYALTYPOINTS_CASH"))
                            TournamentDetailsFragment.this.showErrorLoyaltyChipsDialog(TournamentDetailsFragment.this.mContext, "You don't have sufficient loyalty chips to register for this tourney.");
                        else
                            TournamentDetailsFragment.this.showErrorBuyChipsDialog(TournamentDetailsFragment.this.mContext, "You don't have sufficient balance to register for this tourney.");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7046")) {
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Please verify your Email/phone in My Account");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7042")) {
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Please verify your Email in My Account");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7041")) {
                        TournamentDetailsFragment.this.showErrorBuyChipsDialog(TournamentDetailsFragment.this.mContext, "To register for this tourney, you need to have a minimum deposit within last 7 days.");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7028")) {
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Registrations for this tournament have been blocked in your state");
                    } else if (TournamentDetailsFragment.this.mTourneyRegistrationResponse.getCode().equalsIgnoreCase("7043")) {
                        TournamentDetailsFragment.this.showGenericDialogWithMessage("Please verify your Mobile Number to play tournaments.");
                    }
                }
            }
        }
    };

    private void setLevelTimer() {
        try {
            this.othersStatus.setVisibility(View.GONE);
            this.completedStatus.setVisibility(View.GONE);
            this.runningStatus.setVisibility(View.VISIBLE);

            if (this.levelTimer != null)
                this.levelTimer.cancel();

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
            this.levelStatus_tv.setText(this.mLevelsResponse.getLeveldetails());

            Date start = new Date();
            start = sdf.parse(sdf.format(start));
            Date end = sdf.parse(Utils.convertTimeStampToAnyDateFormat(this.mLevelsResponse.getLeveltimer(), "hh:mm:ss aa"));

            long millis = end.getTime() - start.getTime();

            this.levelTimer = new CountDownTimer(millis, 1000) {

                public void onTick(long millisUntilFinished) {
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);

                    if (minutes >= 1)
                        millisUntilFinished = millisUntilFinished - (minutes * (1000 * 60));
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                    String strMins = "00", strSeconds = "00";
                    if (String.valueOf(minutes).length() == 1)
                        strMins = "0" + String.valueOf(minutes);
                    else
                        strMins = String.valueOf(minutes);

                    if (String.valueOf(seconds).length() == 1)
                        strSeconds = "0" + String.valueOf(seconds);
                    else
                        strSeconds = String.valueOf(seconds);

                    levelCountdown_tv.setText(strMins + ":" + strSeconds);
                }

                public void onFinish() {
                    //Log.d("local","done!");
                }

            }.start();
        } catch (Exception e) {
            Log.e(TAG, "EXP: setLevelTimer-->> " + e.toString());
        }
    }

    private void getWinnerList() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("prize_distribution");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.winnersResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getWinnerList" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getWinnerList-->> " + e.toString());
        }
    }

    private void showWinnersList(List<GamePlayer> players) {
        hideView(this.othersStatus);
        hideView(this.runningStatus);
        showView(this.completedStatus);

        showView(this.playerLabel_tv);
        this.winnersAdapter = new WinnersAdapter(this.mContext, players, this);
        this.prizeList.setAdapter(winnersAdapter);
        if (this.winnersAdapter != null) {
            this.winnersAdapter.notifyDataSetChanged();
        }
    }

    private void toggleDeregisterButtonVisibility(int visibility) {
        this.deregister_btn_players.setVisibility(visibility);
        this.deregister_btn_schedules.setVisibility(visibility);
        this.deregister_btn_info.setVisibility(visibility);
    }

    private void toggleRegisterButtonVisibility(int visibility) {
        this.register_btn_players.setVisibility(visibility);
        this.register_btn_schedules.setVisibility(visibility);
        this.register_btn_info.setVisibility(visibility);
    }

    private void showGenericDialogWithMessage(final String message) {
        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_generic);
        dialog.setCanceledOnTouchOutside(false);

        TextView dialog_msg_tv = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);

        dialog_msg_tv.setText(message);

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                dialog.dismiss();
            }

        }.start();

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (message.equalsIgnoreCase("This tournament has been cancelled")) {
                    dialog.dismiss();
                    launchTLFragment(new TournamentsFragment(), TournamentsFragment.class.getName());
                } else
                    dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void populateJoinedPlayers() {
        this.joinedPlayersAdapter = new JoinedPlayersAdapter(this.mContext, this.mJoinedPlayers, this);
        this.joinedPlayersList.setAdapter(joinedPlayersAdapter);
        if (this.joinedPlayersAdapter != null) {
            this.joinedPlayersAdapter.notifyDataSetChanged();
        }

        if (mJoinedPlayers.size() > 0)
            joined_tv.setText(this.mJoinedPlayers.size() + "/" + this.mTourneyDetailsResponse.getMax_registration());
        else
            joined_tv.setText("0/" + this.mTourneyDetailsResponse.getMax_registration());

        if (mTourneyDetailsResponse.getStatus().equalsIgnoreCase("running") || mTourneyDetailsResponse.getStatus().equalsIgnoreCase("completed")) {
            for (JoinedPlayers player : this.mJoinedPlayers) {
                if (player.getNick_name().equalsIgnoreCase(userData.getNickName())) {
                    Log.d("flow", "INSIDE THERE");
                    this.yourRank_tv.setText(player.getRank());
                    break;
                }
            }
        }
    }

    private void populateWaitingPlayers() {
        this.waitingPlayersAdapter = new WaitingPlayersAdapter(this.mContext, this.mWaitingPlayers, this);
        this.waitingPlayersList.setAdapter(waitingPlayersAdapter);
        if (this.waitingPlayersAdapter != null) {
            this.waitingPlayersAdapter.notifyDataSetChanged();
        }
    }

    private void populateLevelsData() {

        this.levelsAdapter = new LevelsAdapter(this.mContext, this.mLevels, this);
        this.levelsList.setAdapter(levelsAdapter);
        if (this.levelsAdapter != null) {
            this.levelsAdapter.notifyDataSetChanged();
        }
    }

    private void populatePrizeList() {
        try {
            this.prizeListAdapter = new PrizeListAdapter(this.mContext, this.mPrizeList, this, this.mTourneyDetailsResponse.getStatus());
            this.prizeList.setAdapter(prizeListAdapter);
            if (this.prizeListAdapter != null) {
                this.prizeListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e+"");
        }
    }

    private void displayTourneyPrizeInfoData() {
        try {
            String strStatus = this.mTourneyDetailsResponse.getStatus();

            this.tid_tv.setText(mTourneyDetailsResponse.getTournament_id());
            this.entryFee_tv.setText(mTourneyDetailsResponse.getEntry());
            this.regCloses_tv.setText(Utils.convertTimeStampToAnyDateFormat(mTourneyDetailsResponse.getTime_to_close_registrations(), "dd-MMM hh:mm aa"));
            this.tourneyStart_tv.setText(Utils.convertTimeStampToAnyDateFormat(mTourneyDetailsResponse.getTournament_start_date(), "dd-MMM hh:mm aa"));
            this.level_tv.setText(mTourneyDetailsResponse.getCurrent_level());

            if (mTourneyDetailsResponse.getTournament_type().equalsIgnoreCase("REGURLAR"))
                this.tourneyType_tv.setText("Regular");
            else
                this.tourneyType_tv.setText(Utils.toTitleCase(mTourneyDetailsResponse.getTournament_type()));

            if (strStatus.equalsIgnoreCase("canceled")) {
                this.yourRank_tv.setText("0");
                this.balance_tv.setText("NA");
                this.status_tv.setText("Cancelled");
            } else if (strStatus.equalsIgnoreCase("registration open") || strStatus.equalsIgnoreCase("announced")
                    || strStatus.equalsIgnoreCase("registrations closed")) {
                this.yourRank_tv.setText("0");
                this.balance_tv.setText(mTourneyDetailsResponse.getTourney_chips());
                this.status_tv.setText(Utils.toTitleCase(mTourneyDetailsResponse.getStatus()));
            } else {
                Log.d("flow", "INSIDE HERE");
                this.yourRank_tv.setText("0");
                this.balance_tv.setText("NA");
                this.status_tv.setText(Utils.toTitleCase(mTourneyDetailsResponse.getStatus()));
            }

            if (strStatus.equalsIgnoreCase("canceled") || strStatus.equalsIgnoreCase("announced") || strStatus.equalsIgnoreCase("running")
                    || strStatus.equalsIgnoreCase("completed") || strStatus.equalsIgnoreCase("registrations closed")) {
                this.toggleRegisterButtonVisibility(View.INVISIBLE);
                this.toggleDeregisterButtonVisibility(View.INVISIBLE);
            } else {
                if (this.mTourneyDetailsResponse.getRegistered().equalsIgnoreCase("YES")) {
                    if (Utils.compareDateWithCurrentDate(this.mTourneyDetailsResponse.getCancel_or_withdraw_registration_time()).equalsIgnoreCase(Utils.DATE_IS_BEFORE)) {
                        this.toggleRegisterButtonVisibility(View.INVISIBLE);
                        this.toggleDeregisterButtonVisibility(View.INVISIBLE);
                    } else {
                        this.toggleRegisterButtonVisibility(View.GONE);
                        this.toggleDeregisterButtonVisibility(View.VISIBLE);
                    }
                } else if (this.mTourneyDetailsResponse.getStatus_in_tourney().equalsIgnoreCase("NotRegistered")) {
                    this.toggleRegisterButtonVisibility(View.VISIBLE);
                    this.toggleDeregisterButtonVisibility(View.GONE);
                }
            }

            if (mLevels.size() > 0 && this.mTourneyDetailsResponse.getMy_current_level() != null) {
                //this.levelStatus_tv.setText(this.mTourneyDetailsResponse.getMy_current_level()+"/"+mLevels.size());
            }

            this.schedulesValue_tv.setText("(" + Utils.convertTimeStampToAnyDateFormat(mTourneyDetailsResponse.getTournament_start_date(), "dd-MMM hh:mm aa") + ")");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }

    }

    private void setTimeBetweenNextLevel() {
        int currentLevel = Integer.parseInt(this.mTourneyDetailsResponse.getCurrent_level());
        for (int i = 0; i < this.mLevels.size(); i++) {
            /*if(currentLevel == (i-1))
            {
                int he = (int) Float.parseFloat(this.mLevels.get(i - 1).getDelay_between_level());
                this.timeBetweenLevels_tv.setText(String.valueOf(he) + " sec");
            }*/
            if (i == 0) {
                int he = (int) Float.parseFloat(this.mLevels.get(i).getDelay_between_level());
                this.timeBetweenLevels_tv.setText(String.valueOf(he) + " sec");
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tourney_details, container, false);

        this.mTourneyId = getArguments().getString("tourneyID");

        setIdsToViews(v);
        init();
        setListeners();
        handleBackButton(v);

        if (GameEngine.getInstance().isSocketConnected()) {
            getTournamentDetails();
        }

        ImageView back_button = (ImageView) v.findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TournamentDetailsFragment.this.popFragment(TournamentDetailsFragment.class.getName());
                launchTLFragment(new TournamentsFragment(), TournamentsFragment.class.getName());
                mCallback.refreshTourneyList();
            }
        });

        return v;
    }

    private void getTournamentTables() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("get_tournament_tables");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentTablesResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getTournamentTables" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getTournamentTables-->> " + e.toString());
        }
    }

    private boolean checkFragmentVisible() {
        return this.getFragmentManager().findFragmentByTag(TournamentDetailsFragment.class.getName()).isVisible();
    }

    private void setListeners() {
        this.mPrizeInfo_label.setOnClickListener(this);
        this.mSchedulesTables_label.setOnClickListener(this);
        this.mPlayers_label.setOnClickListener(this);
        this.register_btn_players.setOnClickListener(this);
        this.register_btn_schedules.setOnClickListener(this);
        this.deregister_btn_players.setOnClickListener(this);
        this.deregister_btn_schedules.setOnClickListener(this);
        this.register_btn_info.setOnClickListener(this);
        this.deregister_btn_info.setOnClickListener(this);
    }

    private void init() {
        this.mContext = getActivity();
        this.mRummyApplication = (RummyApplication) this.mContext.getApplication();
        RummyApplication app = (RummyApplication) this.mContext.getApplication();
        if (app != null) {
            this.userData = app.getUserData();
        }

        this.tableLabel.setPaintFlags(this.tableLabel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.schedulesLabel_tv.setPaintFlags(this.schedulesLabel_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.schedulesValue_tv.setPaintFlags(this.schedulesValue_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.joinedPlayers_tv.setPaintFlags(this.joinedPlayers_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.waitingPlayers_tv.setPaintFlags(this.waitingPlayers_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.playerInfo_tv.setPaintFlags(this.playerInfo_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.prizeAmountLabel_tv.setPaintFlags(this.prizeAmountLabel_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.playerLabel_tv.setPaintFlags(this.playerLabel_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.positionLabel_tv.setPaintFlags(this.positionLabel_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        this.tourneyInfo_tv.setPaintFlags(this.tourneyInfo_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        prizeInfoSelected = true;
        schedulesSelected = false;
        playersSelected = false;
    }

    private void hideProgress() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismiss();
    }

    private void showProgress() {
        mLoadingDialog = new Dialog(mContext, R.style.DialogTheme);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setContentView(R.layout.dialog_loading);
        mLoadingDialog.setCanceledOnTouchOutside(false);

        mLoadingDialog.show();
    }

    private void handleBackButton(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode != 4) {
                    return false;
                }
                //TournamentDetailsFragment.this.popFragment(TournamentDetailsFragment.class.getName());
                launchTLFragment(new TournamentsFragment(), TournamentsFragment.class.getName());
                mCallback.refreshTourneyList();
                return true;
            }
        });
    }

    public void launchTLFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.home_content_frame, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void setIdsToViews(View v) {
        this.mPrizeInfo_label = (LinearLayout) v.findViewById(R.id.prizeInfo_label);
        this.mSchedulesTables_label = (LinearLayout) v.findViewById(R.id.schedulesTables_label);
        this.mPlayers_label = (LinearLayout) v.findViewById(R.id.players_label);
        this.mPrizeInfo_iv = (ImageView) v.findViewById(R.id.prizeInfo_iv);
        this.mSchedules_iv = (ImageView) v.findViewById(R.id.schedules_iv);
        this.mPlayers_iv = (ImageView) v.findViewById(R.id.players_iv);
        this.mPrizeInfo_tv = (TextView) v.findViewById(R.id.prizeInfo_tv);
        this.mSchedules_tv = (TextView) v.findViewById(R.id.schedules_tv);
        this.mPlayers_tv = (TextView) v.findViewById(R.id.players_tv);
        this.prizeInfo_content = (RelativeLayout) v.findViewById(R.id.prizeInfo_content);
        this.schedules_content = (RelativeLayout) v.findViewById(R.id.schedules_content);
        this.players_content = (RelativeLayout) v.findViewById(R.id.players_content);

        this.tableLabel = (TextView) v.findViewById(R.id.tableLabel);
        this.schedulesLabel_tv = (TextView) v.findViewById(R.id.schedulesLabel_tv);
        this.schedulesValue_tv = (TextView) v.findViewById(R.id.schedulesValue_tv);
        this.joinedPlayers_tv = (TextView) v.findViewById(R.id.joinedPlayers_tv);
        this.waitingPlayers_tv = (TextView) v.findViewById(R.id.waitingPlayers_tv);
        this.tourneyInfo_tv = (TextView) v.findViewById(R.id.tourneyInfo_tv);
        this.playerInfo_tv = (TextView) v.findViewById(R.id.playerInfo_tv);
        this.prizeAmountLabel_tv = (TextView) v.findViewById(R.id.prizeAmountLabel_tv);
        this.positionLabel_tv = (TextView) v.findViewById(R.id.positionLabel_tv);
        this.playerLabel_tv = (TextView) v.findViewById(R.id.playerLabel_tv);
        this.levelStatus_tv = (TextView) v.findViewById(R.id.levelStatus_tv);
        this.levelCountdown_tv = (TextView) v.findViewById(R.id.levelCountdown_tv);
        this.tourneyStart_tv = (TextView) v.findViewById(R.id.tourneyStart_tv);

        this.tid_tv = (TextView) v.findViewById(R.id.tid_tv);
        this.tourneyType_tv = (TextView) v.findViewById(R.id.tourneyType_tv);
        this.entryFee_tv = (TextView) v.findViewById(R.id.entryFee_tv);
        this.joined_tv = (TextView) v.findViewById(R.id.joined_tv);
        this.regCloses_tv = (TextView) v.findViewById(R.id.regCloses_tv);
        this.balance_tv = (TextView) v.findViewById(R.id.balance_tv);
        this.yourRank_tv = (TextView) v.findViewById(R.id.yourRank_tv);
        this.level_tv = (TextView) v.findViewById(R.id.level_tv);
        this.timeBetweenLevels_tv = (TextView) v.findViewById(R.id.timeBetweenLevels_tv);
        this.status_tv = (TextView) v.findViewById(R.id.status_tv);
        this.tourneyCompleteTime_tv = (TextView) v.findViewById(R.id.tourneyCompleteTime_tv);

        this.levelsList = (ListView) v.findViewById(R.id.levelsList);
        this.prizeList = (ListView) v.findViewById(R.id.prizeList);
        this.joinedPlayersList = (ListView) v.findViewById(R.id.joinedPlayersList);
        this.waitingPlayersList = (ListView) v.findViewById(R.id.waitingPlayersList);
        this.tourneyTablesList = (ListView) v.findViewById(R.id.tourneyTablesList);

        this.register_btn_players = (Button) v.findViewById(R.id.register_btn_players);
        this.register_btn_schedules = (Button) v.findViewById(R.id.register_btn_schedules);
        this.deregister_btn_players = (Button) v.findViewById(R.id.deregister_btn_players);
        this.deregister_btn_schedules = (Button) v.findViewById(R.id.deregister_btn_schedules);
        this.register_btn_info = (Button) v.findViewById(R.id.register_btn_info);
        this.deregister_btn_info = (Button) v.findViewById(R.id.deregister_btn_info);

        this.runningStatus = (LinearLayout) v.findViewById(R.id.runningStatus);
        this.completedStatus = (LinearLayout) v.findViewById(R.id.completedStatus);
        this.othersStatus = (LinearLayout) v.findViewById(R.id.othersStatus);
    }

    private void getTournamentDetails() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("get_tournament_details");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentsDetailsListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getTourneyDetails" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getTourneyDetails-->> " + e.toString());
        }
    }

    // joined players
    private void getLeaderBoard() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("leader_board");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.leaderBoardResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getLeaderBoard" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getLeaderBoard-->> " + e.toString());
        }
    }

    // prize List
    private void getPrizeList() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("get_prize_list");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.prizeListResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getPrizeList" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getPrizeList-->> " + e.toString());
        }
    }

    // players wait list
    private void getPlayerWaitList() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("tournament_wait_list");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.waitListResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getPlayerWaitList" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getPlayerWaitList-->> " + e.toString());
        }
    }

    private void getTableDetails(String tableId) {
        try {
            GetTableDetailsRequest request = new GetTableDetailsRequest();
            request.setCommand("get_table_details");
            request.setUuid(Utils.generateUuid());
            request.setTable_id(tableId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentsResponseListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getTableDetails" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getTableDetails-->> " + e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (view == mPrizeInfo_label) {
                if (!prizeInfoSelected) {
                    schedulesSelected = true;
                    playersSelected = true;
                    this.togglePrizeInfo();
                    this.toggleSchedules();
                    this.togglePlayers();
                    this.showView(this.prizeInfo_content);
                    this.hideView(schedules_content);
                    this.hideView(players_content);
                }
            } else if (view == mSchedulesTables_label) {
                if (!schedulesSelected) {
                    prizeInfoSelected = true;
                    playersSelected = true;
                    this.toggleSchedules();
                    this.togglePrizeInfo();
                    this.togglePlayers();
                    this.hideView(this.prizeInfo_content);
                    this.showView(schedules_content);
                    this.hideView(players_content);
                }
            } else if (view == mPlayers_label) {
                if (!playersSelected) {
                    prizeInfoSelected = true;
                    schedulesSelected = true;
                    this.togglePlayers();
                    this.togglePrizeInfo();
                    this.toggleSchedules();
                    this.hideView(this.prizeInfo_content);
                    this.hideView(schedules_content);
                    this.showView(players_content);
                }
            } else if (view == register_btn_players || view == register_btn_schedules || view == register_btn_info) {
                Log.e("register_btn_info","register_btn_info");
                if (this.mTourneyDetailsResponse != null) {
                    disableView(register_btn_info);
                    disableClick(register_btn_info);
                    enableView(deregister_btn_info);
                    Log.e("mTourneyDetailsResponse",mTourneyDetailsResponse.getEntry()+"");
                    if (this.mTourneyDetailsResponse. getEntry().equalsIgnoreCase("0") || this.mTourneyDetailsResponse.getEntry().equalsIgnoreCase("0.0"))
                    {
                        Log.e("getEntry","getEntry");
                        this.registerTournament();
                    }
                    else {
                        Log.e("else","openConfirmDialog");
                        this.openConfirmDialog("register");
                    }
                }
            } else if (view == deregister_btn_players || view == deregister_btn_schedules || view == deregister_btn_info) {
                if (this.mTourneyDetailsResponse != null) {
                    enableView(register_btn_info);
                    disableView(deregister_btn_info);
                    disableClick(deregister_btn_info);
                    if (this.mTourneyDetailsResponse.getEntry().equalsIgnoreCase("0") || this.mTourneyDetailsResponse.getEntry().equalsIgnoreCase("0.0"))
                        this.deregisterTournament();
                    else {
                        this.openConfirmDialog("deregister");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "@onclick " + e.toString());
        }
    }

    private void openConfirmDialog(String action) {
        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_leave_table);
        dialog.setCanceledOnTouchOutside(false);

        TextView dialog_msg_tv = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        Button yes_btn = (Button) dialog.findViewById(R.id.yes_btn);
        Button no_btn = (Button) dialog.findViewById(R.id.no_btn);

        //Do you want to register for the tourney? 500 Chips will be deducted from your account.
        // Are you sure, you want to unregister from TID 15129?

        if (action.equalsIgnoreCase("register")) {
            if (this.mTourneyDetailsResponse.getTourney_cost().equalsIgnoreCase("LOYALTYPOINTS_CASH"))
                dialog_msg_tv.setText("Do you want to register for the tourney? " + this.mTourneyDetailsResponse.getEntry() + " Loyalty Chips will be deducted from your account.");
            else
                dialog_msg_tv.setText("Do you want to register for the tourney? " + this.mTourneyDetailsResponse.getEntry() + " Chips will be deducted from your account.");

            yes_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    TournamentDetailsFragment.this.registerTournament();
                }
            });

            no_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    enableView(register_btn_info);
                }
            });
        } else if (action.equalsIgnoreCase("deregister")) {
            dialog_msg_tv.setText("Are you sure, you want to unregister from TID " + this.mTourneyId + "?");

            yes_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    TournamentDetailsFragment.this.deregisterTournament();
                }
            });

            no_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    enableView(deregister_btn_info);
                }
            });
        }

        dialog.show();

    }

    private void registerTournament() {
        try {
            this.showProgress();
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("register_tournament");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);
            request.setLevel("1");
            request.setPlayer_amount("0");
            //request.setPlayer_amount(this.mTourneyDetailsResponse.getEntry());
            request.setVipcode("None");

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentRegistrationResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "registerTournament" + gameEngineNotRunning.getLocalizedMessage());
                this.hideProgress();
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: registerTournament-->> " + e.toString());
            this.hideProgress();
        }
    }

    private void deregisterTournament() {
        try {
            this.showProgress();
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("deregister_tournament");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);
            request.setLevel("1");

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentRegistrationResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "deregisterTournament" + gameEngineNotRunning.getLocalizedMessage());
                this.hideProgress();
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: deregisterTournament-->> " + e.toString());
            this.hideProgress();
        }
    }

    private void togglePrizeInfo() {
        if (prizeInfoSelected) {
            this.mPrizeInfo_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.white));
            this.mPrizeInfo_iv.setImageResource(R.drawable.prizeinfo);
            prizeInfoSelected = false;
        } else {
            this.mPrizeInfo_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorAccent));
            this.mPrizeInfo_iv.setImageResource(R.drawable.prizeinfo_on);
            prizeInfoSelected = true;
        }
    }

    private void toggleSchedules() {
        if (schedulesSelected) {
            this.mSchedules_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.white));
            this.mSchedules_iv.setImageResource(R.drawable.tables_off);
            schedulesSelected = false;
        } else {
            this.mSchedules_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorAccent));
            this.mSchedules_iv.setImageResource(R.drawable.tables_on);
            schedulesSelected = true;
        }
    }

    private void togglePlayers() {
        if (playersSelected) {
            this.mPlayers_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.white));
            this.mPlayers_iv.setImageResource(R.drawable.player);
            playersSelected = false;
        } else {
            this.mPlayers_tv.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorAccent));
            this.mPlayers_iv.setImageResource(R.drawable.player_on);
            playersSelected = true;
        }
    }

    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        MyWebEngage.trackScreenNameWE(MyWebEngage.TOURNEY_DETAILS_SCREEN, getContext());
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        String eventName = event.getEventName();

        if (!eventName.equalsIgnoreCase("gamesetting_update") && !eventName.equalsIgnoreCase("HEART_BEAT") &&
                event.getTournamentId().equalsIgnoreCase(this.mTourneyId)) {
            if (eventName.equalsIgnoreCase("stop_registration")) {
                this.toggleDeregisterButtonVisibility(View.INVISIBLE);
                this.toggleRegisterButtonVisibility(View.INVISIBLE);
                this.getTournamentDetails();

                if (event.getTournamentId().equalsIgnoreCase(this.mTourneyId))
                    this.showGenericDialogWithMessage("Registrations for this tournament have been closed.");
            } else if (eventName.equalsIgnoreCase("start_registration")) {
                this.toggleDeregisterButtonVisibility(View.INVISIBLE);
                this.toggleRegisterButtonVisibility(View.VISIBLE);

                this.getTournamentDetails();
                this.showGenericDialogWithMessage("Registrations for this tournament are now open.");
            } else if (eventName.equalsIgnoreCase("tournament_cancel")) {
                this.status_tv.setText("Cancelled");
                this.toggleDeregisterButtonVisibility(View.INVISIBLE);
                this.toggleRegisterButtonVisibility(View.INVISIBLE);

                this.getTournamentDetails();
                this.showGenericDialogWithMessage("This tournament has been cancelled");
            } else if (eventName.equalsIgnoreCase("BALANCE_UPDATE")) {
                LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();
                userData.setFunChips(event.getFunChips());
                userData.setFunInPlay(event.getFunInPlay());
                userData.setRealChips(event.getReaChips());
                userData.setRealInPlay(event.getRealInPlay());
                userData.setLoyalityChips(event.getLoyaltyChips());
            } else if (eventName.equalsIgnoreCase("player_registered") || eventName.equalsIgnoreCase("player_deregistered")) {
                if (event.getTournamentId().equalsIgnoreCase(this.mTourneyId))
                    this.getLeaderBoard();
            } else if (eventName.equalsIgnoreCase("stop_cancel_registration")) {
                this.toggleDeregisterButtonVisibility(View.GONE);

                if (this.mTourneyDetailsResponse.getRegistered().equalsIgnoreCase("YES"))
                    this.toggleRegisterButtonVisibility(View.INVISIBLE);
                else
                    this.toggleRegisterButtonVisibility(View.VISIBLE);

                this.showGenericDialogWithMessage("Time to cancel registrations has ended.");
            } else if (eventName.equalsIgnoreCase("start_tournament") && !this.mTourneyDetailsResponse.getRegistered().equalsIgnoreCase("yes")) {
                this.getLevelTimer();
                this.status_tv.setText("Running");
                this.getTournamentDetails();
            }
        } else if (eventName.equalsIgnoreCase("BALANCE_UPDATE")) {
            LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();
            userData.setFunChips(event.getFunChips());
            userData.setFunInPlay(event.getFunInPlay());
            userData.setRealChips(event.getReaChips());
            userData.setRealInPlay(event.getRealInPlay());
        }

    }

    @Subscribe
    public void onMessageEvent(GameEvent event) {
        if (event.name().equalsIgnoreCase("SERVER_CONNECTED") || !event.name().equalsIgnoreCase("SERVER_DISCONNECTED")) {
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void sendReply(String msg_uuid) {
        try {
            BaseReply reply = new BaseReply();
            reply.setCode("200");
            reply.setUuid(msg_uuid);
            reply.setType("+OK");

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(reply), this.tournamentsResponseListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getTableDetails-->> " + e.toString());
        }
    }

    private void updateUserData(Event event) {
        if (event != null && this.userData != null) {
            this.userData.setFunChips(event.getFunChips());
            this.userData.setFunInPlay(event.getFunInPlay());
            this.userData.setRealChips(event.getReaChips());
            this.userData.setRealInPlay(event.getRealInPlay());
        }
    }

    public interface RefreshTournamentsList {
        public void refreshTourneyList();
    }

    private void getLevelTimer() {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("get_level_timer");
            request.setUuid(Utils.generateUuid());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.levelsTimerListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "get_level_timer" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: get_level_timer-->> " + e.toString());
        }
    }

    private void showRunningTables(TournamentsTablesResponse response) {
        try {
            Log.d("local", "TOTAL TABLES: " + response.getTables().size());

            this.tourneyTablesAdapter = new TourneyTablesAdapter(this.mContext, response.getTables());
            this.tourneyTablesList.setAdapter(tourneyTablesAdapter);
            if (this.tourneyTablesAdapter != null) {
                this.tourneyTablesAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: showRunningTables-->> " + e.toString());
        }
    }
}
