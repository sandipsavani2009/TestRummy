package in.glg.rummy.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.activities.HomeActivity;
import in.glg.rummy.adapter.TournamentsAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.TournamentsDataRequest;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.api.response.TournamentsDataResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.Tournament;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

/**
 * Created by GridLogic on 1/12/17.
 */

public class TournamentsFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = TournamentsFragment.class.getName();
    private RummyApplication mRummyApplication;
    private FragmentActivity mContext;
    private LoginResponse userData;

    private TournamentsAdapter tournamentsAdapter;
    private List<Tournament> tournaments;
    private ListView tourneyList;
    private TextView noTournaments_tv;
    private ProgressBar tourney_progress;
    private CheckBox free_cb, cash_cb;
    private ImageView tourney_back_button;

    private static String tourneyFilter = "";

    private OnResponseListener tournamentsDataListener = new OnResponseListener(TournamentsDataResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                hideProgress();
                TournamentsFragment.this.tournaments = ((TournamentsDataResponse) response).getTournaments();
                if (!((TournamentsDataResponse) response).getCode().equalsIgnoreCase("200")) {
                    TournamentsFragment.this.noTournaments_tv.setVisibility(View.VISIBLE);
                    TournamentsFragment.this.tourneyList.setVisibility(View.GONE);
                } else {
                    if (TournamentsFragment.this.tournaments != null && TournamentsFragment.this.tournaments.size() > 0) {
                        TournamentsFragment.this.noTournaments_tv.setVisibility(View.GONE);
                        TournamentsFragment.this.tourneyList.setVisibility(View.VISIBLE);
                        TournamentsFragment.this.populateTourneyData();
                    } else {
                        TournamentsFragment.this.noTournaments_tv.setVisibility(View.VISIBLE);
                        TournamentsFragment.this.tourneyList.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    private void populateTourneyData() {
        sortTournaments(tournaments);
        this.tournamentsAdapter = new TournamentsAdapter(this.mContext, tournaments, this);
        this.tourneyList.setAdapter(tournamentsAdapter);
        if (this.tournamentsAdapter != null) {
            this.tournamentsAdapter.notifyDataSetChanged();
        }

        checkFilter();
    }

    // Applying bubble sort
    private void sortTournaments(List<Tournament> tournaments) {
        try {
            String formatDate = "dd MMM yyyy hh:mm aa";
            SimpleDateFormat sdf = new SimpleDateFormat(formatDate);
            Date fromDate, toDate;
            Tournament tourney;
            for (int i = 0; i < tournaments.size(); i++) {
                for (int j = 1; j < (tournaments.size() - i); j++) {
                    fromDate = sdf.parse(Utils.convertTimeStampToAnyDateFormat(tournaments.get(j - 1).getStartDate(), formatDate));
                    toDate = sdf.parse(Utils.convertTimeStampToAnyDateFormat(tournaments.get(j).getStartDate(), formatDate));

                    if (fromDate.compareTo(toDate) > 0) {
                        tourney = tournaments.get(j - 1);
                        tournaments.set((j - 1), tournaments.get(j));
                        tournaments.set((j), tourney);
                    }
                }
            }

            this.tournaments = tournaments;
        } catch (Exception e) {
            Log.e(TAG, "EXP: Sorting tournaments data -->> " + e.toString());
        }
    }

    private void checkFilter() {
        Log.d(TAG, "checking filter");
        if (RummyConstants.T_FILTER.equalsIgnoreCase("cash"))
            cash_cb.setChecked(true);
        else if (RummyConstants.T_FILTER.equalsIgnoreCase("free"))
            free_cb.setChecked(true);
        else {
            free_cb.setChecked(false);
            cash_cb.setChecked(false);
        }
    }

    public void launchTDFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.home_content_frame, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        String eventName = event.getEventName();
        if (eventName.equalsIgnoreCase("show_tournament")) {
            Tournament tournament = new Tournament();
            tournament.setTourneyId(event.getTournament().getTourneyId());
            tournament.setEntry(event.getTournament().getEntry());
            tournament.setStartDate(event.getTournament().getStartDate());
            tournament.setStatus(event.getTournament().getStatus());
            tournament.setPlayers(event.getTournament().getPlayers());
            tournament.setCashPrize(event.getTournament().getCashPrize());

            if (this.tournaments.size() == 0) {
                getTournamentsData();
            } else {
                this.tournaments.add(tournament);
                this.tournamentsAdapter.addNewItem(tournament);
                updateFilter();
            }
        } else if (eventName.equalsIgnoreCase("end_tournament")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setStatus("completed");
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("start_tournament")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setStatus("running");
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("stop_registration")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setStatus("registrations closed");
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("tournament_cancel")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setStatus("canceled");
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("start_registration")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setStatus("registration open");
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("BALANCE_UPDATE")) {
            LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();
            userData.setFunChips(event.getFunChips());
            userData.setFunInPlay(event.getFunInPlay());
            userData.setRealChips(event.getReaChips());
            userData.setRealInPlay(event.getRealInPlay());
            userData.setLoyalityChips(event.getLoyaltyChips());
        } else if (eventName.equalsIgnoreCase("player_registered") || eventName.equalsIgnoreCase("player_deregistered")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.get(i).setPlayers(event.getTotalgamePlayers());
                    break;
                }
            }
            updateList();
            updateFilter();
        } else if (eventName.equalsIgnoreCase("tournament_closed")) {
            for (int i = 0; i < this.tournaments.size(); i++) {
                if (this.tournaments.get(i).getTourneyId().equalsIgnoreCase(event.getTournamentId())) {
                    this.tournaments.remove(i);
                    break;
                }
            }
            updateList();
            updateFilter();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_tournaments, container, false);
        Log.e("tournaments", "tournaments");
        setIdsToViews(v);
        setListenersToViews();
        init();

        if (GameEngine.getInstance().isSocketConnected()) {
            getTournamentsData();
        }

        return v;
    }

    private void setListenersToViews() {
        free_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cash_cb.setChecked(false);
                    if (tournaments != null && tournaments.size() > 0)
                        tournamentsAdapter.filter("FREE", TournamentsFragment.this.tournaments);
                } else {
                    if (tournaments != null && tournaments.size() > 0)
                        tournamentsAdapter.filter("ALL", TournamentsFragment.this.tournaments);
                }

                if (b)
                    RummyConstants.T_FILTER = "free";
                else if (!cash_cb.isChecked())
                    RummyConstants.T_FILTER = "all";
            }
        });

        cash_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    free_cb.setChecked(false);
                    if (tournaments != null && tournaments.size() > 0)
                        tournamentsAdapter.filter("CASH", TournamentsFragment.this.tournaments);
                } else {
                    if (tournaments != null && tournaments.size() > 0)
                        tournamentsAdapter.filter("ALL", TournamentsFragment.this.tournaments);
                }

                if (b)
                    RummyConstants.T_FILTER = "cash";
                else if (!free_cb.isChecked())
                    RummyConstants.T_FILTER = "all";
            }
        });

        this.tourney_back_button.setOnClickListener(this);
    }

    private void updateFilter() {
        if (free_cb.isChecked())
            tournamentsAdapter.filter("FREE", TournamentsFragment.this.tournaments);
        else if (cash_cb.isChecked())
            tournamentsAdapter.filter("CASH", TournamentsFragment.this.tournaments);
        else
            tournamentsAdapter.filter("ALL", TournamentsFragment.this.tournaments);
    }

    private void init() {
        this.mContext = getActivity();
        this.mRummyApplication = (RummyApplication) this.mContext.getApplication();
        RummyApplication app = (RummyApplication) this.mContext.getApplication();
        if (app != null) {
            this.userData = app.getUserData();
        }
    }

    private void setIdsToViews(View v) {
        this.tourneyList = (ListView) v.findViewById(R.id.tourneyList);
        this.noTournaments_tv = (TextView) v.findViewById(R.id.noTournaments_tv);
        this.tourney_progress = (ProgressBar) v.findViewById(R.id.tourney_progress);
        this.free_cb = (CheckBox) v.findViewById(R.id.free_cb);
        this.cash_cb = (CheckBox) v.findViewById(R.id.cash_cb);
        tourney_back_button = (ImageView) v.findViewById(R.id.tourney_back_button);
    }

    public void getTournamentsData() {
        try {
            showProgress();
            this.free_cb.setChecked(false);
            this.cash_cb.setChecked(false);
            TournamentsDataRequest request = new TournamentsDataRequest();
            request.setCommand("list_tournaments");
            request.setUuid(Utils.generateUuid());

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tournamentsDataListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "getTourneyData" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: getTournamentsData-->> " + e.toString());
            hideProgress();
        }
    }

    private void showProgress() {
        this.tourney_progress.setVisibility(View.VISIBLE);
        this.tourneyList.setVisibility(View.GONE);
    }

    private void hideProgress() {
        this.tourney_progress.setVisibility(View.GONE);
        this.tourneyList.setVisibility(View.VISIBLE);
    }

    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        MyWebEngage.trackScreenNameWE(MyWebEngage.TOURNEY_LOBBY_SCREEN, getContext());
    }

    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void updateList() {
        if (this.tournamentsAdapter != null) {
            this.tournamentsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.tourney_back_button)
        {
            ((HomeActivity) this.mContext).showFragment(R.id.home);
        }


    }
}
