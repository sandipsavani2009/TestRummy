package in.glg.rummy.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.glg.rummy.GameRoom.TableActivity;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.activities.HomeActivity;
import in.glg.rummy.adapter.LobbyAdapter;
import in.glg.rummy.adapter.VariantsAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.LobbyDataRequest;
import in.glg.rummy.api.response.JoinTableResponse;
import in.glg.rummy.api.response.LobbyTablesResponse;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.BaseTrRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.JoinRequest;
import in.glg.rummy.models.Table;
import in.glg.rummy.utils.ErrorCodes;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class LobbyFragment extends BaseFragment implements OnItemClickListener, OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String LOBBY_POSITION = "LOBBY_POSITION";
    private static final String TAG = LobbyFragment.class.getSimpleName();
    private VariantsAdapter betAdapter;
    private OnResponseListener chipLoadListner = new OnResponseListener(LoginResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                LoginResponse loginResponse = (LoginResponse) response;
                String message = "";
                int code = Integer.parseInt(loginResponse.getCode());
                if (code == ErrorCodes.SUCCESS) {
                    ((RummyApplication) LobbyFragment.this.getActivity().getApplication()).getUserData().setFunChips(loginResponse.getFunChips());
                    message = String.format("%s %s %s", new Object[]{LobbyFragment.this.mContext.getString(R.string.chips_reload_success_msg), loginResponse.getFunChips(), LobbyFragment.this.mContext.getString(R.string.lobby_chips_title).toLowerCase()});
                } else if (code == ErrorCodes.PLAYER_HAS_CHIPS_MORE_THAN_MINIMUN) {
                    message = String.format("%s %s %s", new Object[]{LobbyFragment.this.mContext.getString(R.string.balance_reload_err_msg), loginResponse.getMinToReload(), LobbyFragment.this.mContext.getString(R.string.lobby_chips_title).toLowerCase()});
                }
                LobbyFragment.this.showGenericDialog(LobbyFragment.this.mContext, message);
            }
        }
    };
    private VariantsAdapter chipsAdapter;
    private VariantsAdapter gameTypeAdapter;
    private String gameVariant;
    private OnResponseListener joinTableListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                JoinTableResponse joinTableResponse = (JoinTableResponse) response;
                int code = Integer.parseInt(joinTableResponse.getCode());

                if (code == ErrorCodes.PLAYER_ALREADY_INPLAY || code == ErrorCodes.SUCCESS) {
                    LobbyFragment.this.mRummyApplication.clearEventList();
                    if (!LobbyFragment.this.isFoundTable(LobbyFragment.this.mSelectedTable)) {
                        LobbyFragment.this.mRummyApplication.setJoinedTableIds(joinTableResponse.getTableId());
                    }
                    LobbyFragment.this.launchTableActivity();
                } else if (code == ErrorCodes.LOW_BALANCE) {
                    if (LobbyFragment.this.mSelectedTable.getTable_cost().contains("CASH_CASH")) {
                        showErrorBalanceBuyChips(getContext(), "You don't have enough balance to join this table, please deposit now");
                        //LobbyFragment.this.showErrorChipsDialog(LobbyFragment.this.getContext(), String.format("%s - %s", new Object[]{"You don't have enough balance to join this table , please deposit from website", Utils.getWebSite()}));
                    } else {
                        LobbyFragment.this.showLowBalanceDialog(LobbyFragment.this.mContext, "You don't have enough balance to join this table. Please click OK to reload your chips");
                    }
                } else if (code == ErrorCodes.TABLE_FULL) {
                    LobbyFragment.this.showGenericDialog(LobbyFragment.this.mContext, "This table is full");
                } else if (code == 483) {
                    showGenericDialog(getContext().getResources().getString(R.string.state_block_message));
                }
            }
            LobbyFragment.this.dismissLoadingDialog();
        }
    };
    private LobbyAdapter lobbyAdapter;
    private OnResponseListener lobbyDataListener = new OnResponseListener(LobbyTablesResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                LobbyFragment.this.tables = ((LobbyTablesResponse) response).getTables();
                LobbyFragment.this.sortTablesList();
                LobbyFragment.this.progressBar.setVisibility(View.GONE);
                LobbyFragment.this.tablesList.setVisibility(View.VISIBLE);
                LobbyFragment.this.setListnersToViews();
            }
        }
    };

    private TextView mBet;
    private RelativeLayout mBetLayout;
    private ImageView mBetTypeIv;
    private TextView mChips;
    private RelativeLayout mChipsLayout;
    private ImageView mChipsTypeDropDownIv;
    private ImageView lobby_back_button;
    private FragmentActivity mContext;
    private TextView mGameType;
    private ImageView mGameTypeDropDownIv;
    private RelativeLayout mGameTypeLayout;
    private TextView mNoOfPlayers;
    private TextView mNoOfTables;
    private TextView mPlayerType;
    private ImageView mPlayerTypeIv;
    private RelativeLayout mPlayerTypeLayout;
    private Table mSelectedTable;
    private List<Table> mSortedList;
    private RummyApplication mRummyApplication;
    private TextView mVariant;
    private RelativeLayout mVariantsLayout;
    private ImageView mVarieantsDropDown;
    private VariantsAdapter playersAdapter;
    private float poolLow;
    private float poolMedium;
    private float prLow;
    private float prMedium;
    private ProgressBar progressBar;
    private List<Table> tables;
    private ListView tablesList;
    private LoginResponse userData;
    private boolean variantSelected = false;
    private VariantsAdapter variantsAdapter = null;


    //filter items
    private ImageView filter;
    private LinearLayout filter_layout, empty_layout;
    private LinearLayout game_type_pools, game_type_deals, game_type_points;
    private ImageView closeFilter;
    private CheckBox free, cash;
    private CheckBox select_variant;
    private CheckBox jokerType, noJokerType;
    private CheckBox pools, deals, points;
    private CheckBox pool101, pool201, bestOf3;
    private CheckBox bestOf2, bestOf6;
    private CheckBox lowBet, mediumBet, highBet;
    private CheckBox players2, players6;
    private Drawable unchecked_dr;
    private Drawable checked_dr;
    private static int paddingPixel;
    private static int CHIPS_FILTER = 0;
    private static int VARIANTS_FILTER = 1;
    private static int GAME_TYPE_FILTER = 2;
    private static int BET_FILTER = 3;
    private static int PLAYERS_FILTER = 4;
    private static boolean isAutoToggle = false;

    private void filterTableList(int position, PopupWindow popupWindow, int id) {
        popupWindow.dismiss();
        handleMultiSelection(position, id);
        sortTablesList();
    }

    private void showGenericDialog(String message) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_generic);
        dialog.setCanceledOnTouchOutside(false);

        Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);
        TextView message_tv = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        message_tv.setText(message);

        ok_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private List<Table> getBetTableList(List<Table> tableList, String compareStr, List<Table> betTypeList) {
        for (int i = tableList.size() - 1; i >= 0; i--) {
            Table table = (Table) tableList.get(i);
            float bet = Float.parseFloat(table.getBet());
            if (table.getTable_type().contains("_POOL") || table.getTable_type().contains("BEST")) {
                if (compareStr.equalsIgnoreCase("Low") && Float.compare(bet, this.poolLow) <= 0) {
                    betTypeList.add(table);
                }
                if (compareStr.contains("Medium") && Float.compare(bet, this.poolMedium) <= 0) {
                    betTypeList.add(table);
                }
                if (compareStr.contains("High") && Float.compare(bet, this.poolMedium) >= 0) {
                    betTypeList.add(table);
                }
            } else if (table.getTable_type().contains(Utils.PR)) {
                if (compareStr.contains("Low") && Float.compare(bet, this.prLow) <= 0) {
                    betTypeList.add(table);
                }
                if (compareStr.contains("Medium") && Float.compare(bet, this.prMedium) > 0) {
                    betTypeList.add(table);
                }
                if (compareStr.contains("High") && Float.compare(bet, this.prMedium) >= 0) {
                    betTypeList.add(table);
                }
            }
        }
        return betTypeList;
    }

    private List<Table> getChipsTypeList(List<Table> tableList, String compareStr, List<Table> chipTypeList) {
        for (int i = tableList.size() - 1; i >= 0; i--) {
            Table table = (Table) tableList.get(i);
            if (table.getTable_cost().contains(compareStr)) {
                chipTypeList.add(table);
            }
        }
        return chipTypeList;
    }

    private List<Table> getGameTypeList(List<Table> tableList, String compareStr, List<Table> gameTypeFilerList) {
        if (tableList != null) {
            for (int i = tableList.size() - 1; i >= 0; i--) {
                Table table = (Table) tableList.get(i);
                if (compareStr.equalsIgnoreCase("deals")) {
                    if (table.getTable_type().equalsIgnoreCase("BEST_OF_2") || table.getTable_type().equalsIgnoreCase("BEST_OF_6")
                            || table.getTable_type().equalsIgnoreCase("BEST_OF_3")) {
                        gameTypeFilerList.add(table);
                    }
                } else if (compareStr.equalsIgnoreCase("POOLS")) {
                    if (table.getTable_type().contains("POOL")) {
                        gameTypeFilerList.add(table);
                    }
                } else if (table.getTable_type().contains(compareStr)) {
                    gameTypeFilerList.add(table);
                }
            }
        }
        return gameTypeFilerList;
    }

    private void getLobbyData() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.tablesList.setVisibility(View.INVISIBLE);
        LobbyDataRequest request = new LobbyDataRequest();
        request.setCommand("list_gamesettings");
        request.setUuid(Utils.generateUuid());

        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.lobbyDataListener);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
            TLog.e(TAG, "getLobbyData" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private List<Table> getPlayerTableList(List<Table> tableList, String compareStr, List<Table> playerTypeList) {

        for (int i = tableList.size() - 1; i >= 0; i--) {
            Table table = (Table) tableList.get(i);
            if (table.getMaxplayer().contains(compareStr)) {
                playerTypeList.add(table);
            }
        }
        return playerTypeList;
    }

    private List<Table> getSortedList() {
        return (this.mSortedList == null || this.mSortedList.size() <= 0) ? this.tables : this.mSortedList;
    }

    private void handleMultiSelection(int position, int id) {
        Log.w(TAG, "Position: " + position);
        if (id == R.id.variants_layout || id == R.id.variants_drop_down_iv || id == VARIANTS_FILTER) {
            this.variantsAdapter.toggleChecked(position);
            this.variantSelected = true;
        } else if (id == R.id.game_type_layout || id == R.id.game_type_drop_down_iv || id == GAME_TYPE_FILTER) {
            this.variantSelected = false;
            this.gameTypeAdapter.toggleChecked(position);
        } else if (id == R.id.chips_layout || id == R.id.chips_type_drop_down_iv || id == CHIPS_FILTER) {
            this.variantSelected = false;
            this.chipsAdapter.toggleChipsItem(position);
            if (position == 0)
                PrefManager.saveInt(getContext(), "tableCost", 0);
            else if (position == 1)
                PrefManager.saveInt(getContext(), "tableCost", 1);
        } else if (id == R.id.bet_layout || id == R.id.bet_type_drop_down_iv || id == BET_FILTER) {
            this.variantSelected = false;
            this.betAdapter.toggleChecked(position);
        } else if (id == R.id.player_type_layout || id == R.id.player_type_drop_down_iv || id == PLAYERS_FILTER) {
            this.variantSelected = false;
            this.playersAdapter.toggleChecked(position);
        }
    }

    private void init() {
        // filter inits
        unchecked_dr = getContext().getResources().getDrawable(R.drawable.curve_edges_black);
        checked_dr = getContext().getResources().getDrawable(R.drawable.curve_edges_filled_green);
        int paddingDp = 8;
        paddingPixel = Utils.convertDpToPixel(paddingDp);
        // ENDS HERE


        Bundle bundle = getArguments();
        if (bundle != null) {
            this.gameVariant = bundle.getString("game_variant");
        }
        this.mContext = getActivity();
        this.mRummyApplication = (RummyApplication) this.mContext.getApplication();
        RummyApplication app = (RummyApplication) this.mContext.getApplication();
        if (app != null) {
            this.userData = app.getUserData();
        }
        if (this.userData != null) {
            this.poolLow = Float.valueOf(this.userData.getPoolLowBet()).floatValue();
            this.poolMedium = Float.valueOf(this.userData.getPoolMediumBet()).floatValue();
            this.prLow = Float.valueOf(this.userData.getPrLowBet()).floatValue();
            this.prMedium = Float.valueOf(this.userData.getPrMediumBet()).floatValue();
        }

        int[] players = getResources().getIntArray(R.array.players_items);
        String[] playersArray = new String[players.length];
        for (int i = 0; i < players.length; i++) {
            playersArray[i] = String.valueOf(players[i]);
        }

        String[] gameTypeArray = new String[]{"101", "201"};

        this.gameTypeAdapter = new VariantsAdapter(this.mContext, gameTypeArray);
        this.variantsAdapter = new VariantsAdapter(this.mContext, getResources().getStringArray(R.array.variant_items));
        this.chipsAdapter = new VariantsAdapter(this.mContext, getResources().getStringArray(R.array.chips_items));
        this.betAdapter = new VariantsAdapter(this.mContext, getResources().getStringArray(R.array.bet_items));
        this.playersAdapter = new VariantsAdapter(this.mContext, playersArray);

        if (this.gameVariant != null && this.gameVariant.length() > 0) {
            Log.e("gameVariant", gameVariant + "");
            if (this.gameVariant.equalsIgnoreCase("strikes")) {
                this.variantsAdapter.toggleChecked(2);
                this.gameTypeAdapter = new VariantsAdapter(this.mContext, getResources().getStringArray(R.array.pr_game_types));
            } else if (this.gameVariant.equalsIgnoreCase("pools")) {
                this.variantsAdapter.toggleChecked(0);
                this.gameTypeAdapter = new VariantsAdapter(this.mContext, gameTypeArray);
            } else if (this.gameVariant.equalsIgnoreCase("deals")) {
                this.variantsAdapter.toggleChecked(1);
                this.gameTypeAdapter = new VariantsAdapter(this.mContext, getResources().getStringArray(R.array.deals_game_types));
            }
        }
        if (this.chipsAdapter != null) {
            this.chipsAdapter.toggleChipsItem(PrefManager.getInt(getContext(), "tableCost", 0));
        }
    }

    private void loadChips() {
        BaseTrRequest request = new BaseTrRequest();
        request.setCommand("chipreload");
        request.setUuid(Utils.generateUuid());
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.chipLoadListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
            TLog.d(TAG, "getTableData" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private int measureContentWidth(VariantsAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        VariantsAdapter adapter = listAdapter;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(this.mContext);
            }
            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            int itemWidth = itemView.getMeasuredWidth();
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
        return maxWidth;
    }

    public static LobbyFragment newInstance(int position) {
        LobbyFragment fr = new LobbyFragment();
        Bundle b = new Bundle();
        b.putInt(LOBBY_POSITION, position);
        fr.setArguments(b);
        return fr;
    }

    private void setFilterAdapters(int id, PopupWindow popupWindow, ListView listViewVariants) {
        if (id == R.id.variants_layout || id == R.id.variants_drop_down_iv) {
            listViewVariants.setAdapter(this.variantsAdapter);
        } else if (id == R.id.game_type_layout || id == R.id.game_type_drop_down_iv) {
            listViewVariants.setAdapter(this.gameTypeAdapter);
        } else if (id == R.id.chips_layout || id == R.id.chips_type_drop_down_iv) {
            listViewVariants.setAdapter(this.chipsAdapter);
        } else if (id == R.id.bet_layout || id == R.id.bet_type_drop_down_iv) {
            listViewVariants.setAdapter(this.betAdapter);
        } else if (id == R.id.player_type_layout || id == R.id.player_type_drop_down_iv) {
            listViewVariants.setAdapter(this.playersAdapter);
        }
    }

    private void setFilterBatData(List<String> chekedItemsList, TextView textView, int type) {
        try {
            if (chekedItemsList.size() == 0) {
                textView.setText("Select");
            } else if (chekedItemsList.size() == 1) {
                String message = "";
                if (type == Utils.VARIANT) {
//                    message = String.format("%s%s%s", new Object[]{chekedItemsList.get(0), " ", this.mContext.getString(R.string.lobby_rummy_title_txt)});
                    message = String.format("%s%s%s", new Object[]{chekedItemsList.get(0), " ", ""});
                } else if (type == Utils.GAME_TYPE) {
                    String str = "%s%s%s";
                    Object[] objArr = new Object[3];
                    objArr[0] = chekedItemsList.get(0);
                    objArr[1] = " ";
                    objArr[2] = !((String) chekedItemsList.get(0)).contains("0") ? "" : this.mContext.getString(R.string.lobby_pool_title_txt);
                    message = String.format(str, objArr);
                } else if (type == Utils.CHIPS) {
                    message = (String) chekedItemsList.get(0);
                } else if (type == Utils.BET) {
                    message = (String) chekedItemsList.get(0);
                } else if (type == Utils.PLAYERS) {
                    message = (String) chekedItemsList.get(0);
                }
                textView.setText(message);
            } else if (chekedItemsList.size() > 1) {
                textView.setText("Multiple");
            }
            if (type == Utils.VARIANT) {
                if (chekedItemsList.size() == this.variantsAdapter.getCount()) {
                    textView.setText("Any");
                }
            } else if (type == Utils.GAME_TYPE) {
                if (chekedItemsList.size() == this.gameTypeAdapter.getCount()) {
                    textView.setText("Any");
                }
            } else if (type == Utils.CHIPS) {
                if (chekedItemsList.size() == this.chipsAdapter.getCount()) {
                    //bonus_code_tv.setText("Any");
                    textView.setText("Free");
                }
            } else if (type == Utils.BET) {
                if (chekedItemsList.size() == this.betAdapter.getCount()) {
                    textView.setText("Any");
                }
            } else if (type == Utils.PLAYERS && chekedItemsList.size() == this.playersAdapter.getCount()) {
                textView.setText("Any");
            }
        } catch (Exception e) {
            TLog.e(TAG, "Exception :: " + e.getMessage());
        }
    }

    private void setFilterSelectionUI(List<String> variantsList, List<String> gameTypeList, List<String> chipsList, List<String> betList, List<String> playerList) {
        setFilterBatData(variantsList, this.mVariant, Utils.VARIANT);
        setFilterBatData(gameTypeList, this.mGameType, Utils.GAME_TYPE);
        setFilterBatData(chipsList, this.mChips, Utils.CHIPS);
        setFilterBatData(betList, this.mBet, Utils.BET);
        setFilterBatData(playerList, this.mPlayerType, Utils.PLAYERS);
    }

    private void setIdsToViews(View v) {
        this.mVarieantsDropDown = (ImageView) v.findViewById(R.id.variants_drop_down_iv);
        this.mGameTypeDropDownIv = (ImageView) v.findViewById(R.id.game_type_drop_down_iv);
        this.mChipsTypeDropDownIv = (ImageView) v.findViewById(R.id.chips_type_drop_down_iv);
        this.lobby_back_button = (ImageView) v.findViewById(R.id.lobby_back_button);
        this.mBetTypeIv = (ImageView) v.findViewById(R.id.bet_type_drop_down_iv);
        this.mPlayerTypeIv = (ImageView) v.findViewById(R.id.player_type_drop_down_iv);
        this.progressBar = (ProgressBar) v.findViewById(R.id.lobby_progress);
        this.tablesList = (ListView) v.findViewById(R.id.lobbylist);
        this.mVariantsLayout = (RelativeLayout) v.findViewById(R.id.variants_layout);
        this.mGameTypeLayout = (RelativeLayout) v.findViewById(R.id.game_type_layout);
        this.mChipsLayout = (RelativeLayout) v.findViewById(R.id.chips_layout);
        this.mBetLayout = (RelativeLayout) v.findViewById(R.id.bet_layout);
        this.mPlayerTypeLayout = (RelativeLayout) v.findViewById(R.id.player_type_layout);
        this.mVariant = (TextView) v.findViewById(R.id.variant_value_tv);
        this.mGameType = (TextView) v.findViewById(R.id.game_type_value_tv);
        this.mChips = (TextView) v.findViewById(R.id.chip_type_value_tv);
        this.mBet = (TextView) v.findViewById(R.id.bet_type_value_tv);
        this.mPlayerType = (TextView) v.findViewById(R.id.player_type_value_tv);
        this.mNoOfPlayers = (TextView) v.findViewById(R.id.no_of_players_tv);
        this.mNoOfTables = (TextView) v.findViewById(R.id.no_of_tables_tv);
    }

    private void setIDsToFilterViews(View v) {
        // filter items
        this.filter = (ImageView) v.findViewById(R.id.filter);
        this.closeFilter = (ImageView) v.findViewById(R.id.closeFilter);
        this.filter_layout = (LinearLayout) v.findViewById(R.id.filter_layout);
        this.game_type_deals = (LinearLayout) v.findViewById(R.id.game_type_deals);
        this.game_type_pools = (LinearLayout) v.findViewById(R.id.game_type_pools);
        this.game_type_points = (LinearLayout) v.findViewById(R.id.game_type_points);
        this.empty_layout = (LinearLayout) v.findViewById(R.id.empty_layout);
        this.free = (CheckBox) v.findViewById(R.id.free);
        this.cash = (CheckBox) v.findViewById(R.id.cash);

        this.pools = (CheckBox) v.findViewById(R.id.pools);
        this.deals = (CheckBox) v.findViewById(R.id.deals);
        this.points = (CheckBox) v.findViewById(R.id.points);
        this.pool101 = (CheckBox) v.findViewById(R.id.pool101);
        this.pool201 = (CheckBox) v.findViewById(R.id.pool201);
        this.bestOf3 = (CheckBox) v.findViewById(R.id.bestOf3);
        this.bestOf2 = (CheckBox) v.findViewById(R.id.bestOf2);
        this.bestOf6 = (CheckBox) v.findViewById(R.id.bestOf6);
        this.lowBet = (CheckBox) v.findViewById(R.id.lowBet);
        this.mediumBet = (CheckBox) v.findViewById(R.id.mediumBet);
        this.highBet = (CheckBox) v.findViewById(R.id.highBet);
        this.players2 = (CheckBox) v.findViewById(R.id.players2);
        this.players6 = (CheckBox) v.findViewById(R.id.players6);
        this.jokerType = (CheckBox) v.findViewById(R.id.jokerType);
        this.noJokerType = (CheckBox) v.findViewById(R.id.noJokerType);
        this.select_variant = (CheckBox) v.findViewById(R.id.select_variant);
    }

    private void setListnersToViews() {
        this.tablesList.setOnItemClickListener(this);
        this.mVarieantsDropDown.setOnClickListener(this);
        this.mGameTypeDropDownIv.setOnClickListener(this);
        this.mChipsTypeDropDownIv.setOnClickListener(this);
        this.mBetTypeIv.setOnClickListener(this);
        this.mPlayerTypeIv.setOnClickListener(this);
        this.mVariantsLayout.setOnClickListener(this);
        this.mGameTypeLayout.setOnClickListener(this);
        this.mChipsLayout.setOnClickListener(this);
        this.mBetLayout.setOnClickListener(this);
        this.mPlayerTypeLayout.setOnClickListener(this);
        this.filter.setOnClickListener(this);
        this.lobby_back_button.setOnClickListener(this);
    }

    private void setListenersForFilters() {
        free.setOnCheckedChangeListener(this);
        cash.setOnCheckedChangeListener(this);
        pools.setOnCheckedChangeListener(this);
        deals.setOnCheckedChangeListener(this);
        points.setOnCheckedChangeListener(this);
        pool101.setOnCheckedChangeListener(this);
        pool201.setOnCheckedChangeListener(this);
        bestOf3.setOnCheckedChangeListener(this);
        bestOf6.setOnCheckedChangeListener(this);
        bestOf2.setOnCheckedChangeListener(this);
        lowBet.setOnCheckedChangeListener(this);
        mediumBet.setOnCheckedChangeListener(this);
        highBet.setOnCheckedChangeListener(this);
        players2.setOnCheckedChangeListener(this);
        players6.setOnCheckedChangeListener(this);
        jokerType.setOnCheckedChangeListener(this);
        noJokerType.setOnCheckedChangeListener(this);

        closeFilter.setOnClickListener(this);
        empty_layout.setOnClickListener(this);
    }

    private void setSortedList(List<Table> list) {
        this.mSortedList = list;
    }

    private void showGameTypeDropDown(View v) {
        if (this.variantsAdapter != null && this.variantsAdapter.getCheckedItems().size() > 0) {
            popUpWindowVariants(v.getId()).showAsDropDown(this.mGameTypeLayout);
        }
    }

    private void sortTablesList() {
        List<String> variantsList;
        List<String> gameTypeList;
        List<String> chipsList;
        List<String> betList;
        List<String> playerList;
        String gameType;
        List<String> temp = new ArrayList();
        if (this.variantsAdapter != null) {
            variantsList = this.variantsAdapter.getCheckedItems();
        } else {
            variantsList = temp;
        }
        if (this.gameTypeAdapter != null) {
            gameTypeList = this.gameTypeAdapter.getCheckedItems();
        } else {
            gameTypeList = temp;
        }
        if (this.chipsAdapter != null) {
            chipsList = this.chipsAdapter.getCheckedItems();
        } else {
            chipsList = temp;
        }
        if (this.betAdapter != null) {
            betList = this.betAdapter.getCheckedItems();
        } else {
            betList = temp;
        }
        if (this.playersAdapter != null) {
            playerList = this.playersAdapter.getCheckedItems();
        } else {
            playerList = temp;
        }

        setFilterSelectionUI(variantsList, gameTypeList, chipsList, betList, playerList);
        List<Table> sortedList = new ArrayList();
        List<Table> variantTypeFilterList = new ArrayList();
        List<String> gameTyeList = new ArrayList();
        if (variantsList.size() > 0) {
            String variant;

            for (String variant2 : variantsList) {
                if (variant2.equalsIgnoreCase(this.mContext.getString(R.string.points_txt))) {
                    variant2 = Utils.PR_JOKER;
                    gameTyeList.add(Utils.GAME_TYPE_PR_JOKER);
                    gameTyeList.add(Utils.GAME_TYPE_PR_NO_JOKER);
                }
                if (variant2.equalsIgnoreCase(this.mContext.getString(R.string.pools_txt))) {
                    variant2 = "POOLS";
                    gameTyeList.add(Utils.GAME_TYPE_101);
                    gameTyeList.add(Utils.GAME_TYPE_201);
                }
                if (variant2.equalsIgnoreCase(this.mContext.getString(R.string.deals_txt))) {
                    variant2 = "deals";
                    gameTyeList.add(Utils.GAME_TYPE_BEST_OF_2);
                    gameTyeList.add(Utils.GAME_TYPE_BEST_OF_6);
                    gameTyeList.add(Utils.GAME_TYPE_BEST_OF_3);
                }
                sortedList = getGameTypeList(this.tables, variant2, variantTypeFilterList);
            }

            if (this.variantSelected) {
                int i;
                String[] gameTypeArray = (String[]) gameTyeList.toArray(new String[gameTyeList.size()]);
                List<String> unCheckedItems = new ArrayList();
                if (this.gameTypeAdapter != null) {
                    unCheckedItems = this.gameTypeAdapter.getUnCheckedItems();
                }
                this.gameTypeAdapter = new VariantsAdapter(this.mContext, gameTypeArray);
                for (String variant22 : variantsList) {
                    if (variant22.equalsIgnoreCase("deals")) {
                        for (i = 0; i < gameTypeArray.length; i++) {
                            gameType = gameTypeArray[i];
                            if (gameType.contains(Utils.GAME_TYPE_BEST_OF_2) || gameType.contains("6") || gameType.contains(Utils.GAME_TYPE_BEST_OF_3)) {
                                this.gameTypeAdapter.toggleChecked(i);
                            }
                        }
                    }
                    if (variant22.equalsIgnoreCase("points")) {
                        for (i = 0; i < gameTypeArray.length; i++) {
                            if (gameTypeArray[i].contains(Utils.GAME_TYPE_PR_JOKER)) {
                                this.gameTypeAdapter.toggleChecked(i);
                            }
                        }
                    }
                    if (variant22.equalsIgnoreCase("pools")) {
                        for (i = 0; i < gameTypeArray.length; i++) {
                            gameType = gameTypeArray[i];
                            if (gameType.contains(Utils.GAME_TYPE_101) || gameType.contains(Utils.GAME_TYPE_201)) {
                                this.gameTypeAdapter.toggleChecked(i);
                            }
                        }
                    }
                }
                if (unCheckedItems.size() > 0) {
                    for (i = 0; i < gameTypeArray.length; i++) {
                        for (String chekedItem : unCheckedItems) {
                            if (gameTypeArray[i].equalsIgnoreCase(chekedItem)) {
                                this.gameTypeAdapter.setUnChecked(i);
                            }
                        }
                    }
                }
                if (this.gameTypeAdapter.getCheckedItems().size() == gameTypeArray.length) {
                    this.mGameType.setText("Any");
                } else if (this.gameTypeAdapter.getCheckedItems().size() == 1) {
                    this.mGameType.setText((CharSequence) this.gameTypeAdapter.getCheckedItems().get(0));
                } else {
                    this.mGameType.setText("Multiple");
                }
                if (this.gameTypeAdapter != null) {
                    gameTypeList = this.gameTypeAdapter.getCheckedItems();
                } else {
                    gameTypeList = temp;
                }
                this.variantSelected = false;
            }
        } else {
            gameTypeList.clear();
            this.mGameType.setText("Select");
            this.gameTypeAdapter = null;
            sortedList = this.tables;
        }

        List<Table> gameTypeFilterList = new ArrayList();
        if (gameTypeList.size() > 0 && sortedList.size() > 0) {
            for (String gameType2 : gameTypeList) {
                if (gameType2.equalsIgnoreCase(Utils.GAME_TYPE_BEST_OF_3)) {
                    gameType2 = "BEST_OF_3";
                }
                if (gameType2.equalsIgnoreCase(Utils.GAME_TYPE_BEST_OF_2)) {
                    gameType2 = "BEST_OF_2";
                }
                if (gameType2.equalsIgnoreCase(Utils.GAME_TYPE_BEST_OF_6)) {
                    gameType2 = "BEST_OF_6";
                }
                if (gameType2.equalsIgnoreCase(Utils.GAME_TYPE_PR_JOKER)) {
                    gameType2 = "JOKER";
                }
                if (gameType2.equalsIgnoreCase(Utils.GAME_TYPE_PR_NO_JOKER)) {
                    gameType2 = "NO_JOKER";
                }
                if (variantTypeFilterList.size() > 0) {
                    sortedList = variantTypeFilterList;
                }
                sortedList = getGameTypeList(sortedList, gameType2, gameTypeFilterList);
            }
        }

        List<Table> chipsTypeFilterList = new ArrayList();
        if (chipsList.size() > 0 && sortedList.size() > 0) {
            for (String chipType : chipsList) {
                String chipType2 = chipType;
                if (chipType2.equalsIgnoreCase("Free")) {
                    chipType2 = "FUNCHIPS_FUNCHIPS";
                } else {
                    chipType2 = "CASH_CASH";
                }
                if (gameTypeFilterList.size() > 0) {
                    sortedList = gameTypeFilterList;
                }
                sortedList = getChipsTypeList(sortedList, chipType2, chipsTypeFilterList);
            }
        }

        List<Table> betFilterList = new ArrayList();
        if (betList.size() > 0 && sortedList.size() > 0) {
            for (String betType : betList) {
                if (chipsTypeFilterList.size() > 0) {
                    sortedList = chipsTypeFilterList;
                }
                sortedList = getBetTableList(sortedList, betType, betFilterList);
            }
        }


        List<Table> playerFilterList = new ArrayList();
        if (playerList.size() == 1 && sortedList.size() > 0) {
            for (String playerType : playerList) {
                if (betFilterList.size() > 0) {
                    sortedList = betFilterList;
                }
                sortedList = getPlayerTableList(sortedList, playerType, playerFilterList);
            }
        }
        if (variantsList.size() == 0 && chipsList.size() == 0 && betFilterList.size() == 0 && playerFilterList.size() == 0) {
            sortedList = this.tables;
        }
        setSortedList(sortedList);
        Collections.sort(sortedList, new PlayerComparator());
        this.lobbyAdapter = new LobbyAdapter(this.mContext, sortedList, this);
        this.tablesList.setAdapter(this.lobbyAdapter);
        if (this.lobbyAdapter != null) {
            this.lobbyAdapter.notifyDataSetChanged();
        }

    }

    public boolean isFoundTable(Table table) {
        for (String id : this.mRummyApplication.getJoinedTableIds()) {
            if (id.equalsIgnoreCase(table.getTable_id())) {
                return true;
            }
        }
        return false;
    }

    public void joinTable(Table table, String buyInAmt) {
        showLoadingDialog(this.mContext);
        this.mRummyApplication = (RummyApplication) this.mContext.getApplication();
        List<String> joinedTableIds = this.mRummyApplication.getJoinedTableIds();
        boolean foundTable = isFoundTable(table);
        if (joinedTableIds != null && joinedTableIds.size() == 1 && !foundTable) {
            dismissLoadingDialog();
            showGenericDialog(this.mContext, this.mContext.getString(R.string.max_table_reached_msg));
        } else if (table != null) {
            setSelectedTable(table);
            JoinRequest request = new JoinRequest();
            request.setCommand("join_table");
            request.setTableId(table.getTable_id());
            request.setUuid(Utils.generateUuid());
            request.setTableJoinAs("play");
            request.setTableType(table.getTable_type());
            request.setTableCost(table.getTable_cost());
            request.setBuyinamount(buyInAmt);
            request.setSeat(0);
            request.setCharNo("0");
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.joinTableListner);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.d(TAG, "getTableData" + gameEngineNotRunning.getLocalizedMessage());
            }
        }
    }

    public void launchTableActivity() {
        int joinedTableIds = this.mRummyApplication.getJoinedTableIds().size();
        Intent playIntent = new Intent(this.mContext, TableActivity.class);
        playIntent.putExtra("iamBack", false);
        playIntent.putExtra("table", this.mSelectedTable);
        playIntent.putExtra("table", this.mSelectedTable);
        playIntent.putExtra("tableId", this.mSelectedTable.getTable_id());
        playIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(playIntent);
    }

    public void onClick(View v) {
        if(v.getId() == R.id.lobby_back_button)
        {

            ((HomeActivity) this.mContext).exitDialog();
        }
        else if(v.getId() == R.id.chips_layout)
        {

        }
        else if(v.getId() == R.id.chips_type_drop_down_iv)
        {
            popUpWindowVariants(v.getId()).showAsDropDown(this.mChipsLayout);
        }
        else if(v.getId() == R.id.variants_layout)
        {

        }
        else if(v.getId() == R.id.variants_drop_down_iv)
        {
            popUpWindowVariants(v.getId()).showAsDropDown(this.mVariantsLayout);
        }
        else if(v.getId() == R.id.game_type_layout)
        {

        }
        else if(v.getId() == R.id.game_type_drop_down_iv)
        {
            showGameTypeDropDown(v);
        }
        else if(v.getId() == R.id.bet_layout)
        {

        }
        else if(v.getId() == R.id.bet_type_drop_down_iv)
        {
            popUpWindowVariants(v.getId()).showAsDropDown(this.mBetLayout);
        }
        else if(v.getId() == R.id.player_type_layout)
        {

        }
        else if(v.getId() == R.id.player_type_drop_down_iv)
        {
            popUpWindowVariants(v.getId()).showAsDropDown(this.mPlayerTypeLayout);
        }
        else if(v.getId() == R.id.filter)
        {
            if (PrefManager.getInt(getContext(), "tableCost", 0) == 1)
                cash.setChecked(true);
            else
                free.setChecked(true);
            filter_layout.setVisibility(View.VISIBLE);
            toggleFilterLayoutView(true);
        }
        else if(v.getId() == R.id.closeFilter)
        {
            toggleFilterLayoutView(false);
        }
        else if(v.getId() == R.id.empty_layout)
        {
            toggleFilterLayoutView(false);
        }

    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_lobby, container, false);
        try {
            init();
            setIdsToViews(v);
            setIDsToFilterViews(v);
            setListenersForFilters();
            filter_layout.setVisibility(View.INVISIBLE);
            if (GameEngine.getInstance().isSocketConnected()) {
                getLobbyData();
            }
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return v;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("flow", "onDestroy: LF");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Table table = (Table) this.tables.get(position);
        Intent playIntent = new Intent(this.mContext, TableActivity.class);
        playIntent.putExtra("table", table);
        startActivity(playIntent);
    }

    @Subscribe
    public void onMessageEvent(GameEvent event) {
        if (event.name().equalsIgnoreCase("SERVER_CONNECTED") || !event.name().equalsIgnoreCase("SERVER_DISCONNECTED")) {
        }
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        if (event.getEventName().equalsIgnoreCase("BALANCE_UPDATE")) {
            LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();
            userData.setFunChips(event.getFunChips());
            userData.setFunInPlay(event.getFunInPlay());
            userData.setRealChips(event.getReaChips());
            userData.setRealInPlay(event.getRealInPlay());
            userData.setLoyalityChips(event.getLoyaltyChips());
        } else if (event.getEventName().equalsIgnoreCase("gamesetting_update")) {
            new RefreshAdapter().execute(new Event[]{event});
        } else if (event.getEventName().equalsIgnoreCase("HEART_BEAT")) {
            this.mNoOfPlayers.setText(String.format("%s %s", new Object[]{event.getTotalNoOfPlayers(), "Players"}));
            this.mNoOfTables.setText(String.format("%s %s", new Object[]{event.getNoOfTables(), "Tables"}));
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        MyWebEngage.trackScreenNameWE(MyWebEngage.LOBBY_SCREEN, getContext());
    }

    public PopupWindow popUpWindowVariants(final int id) {
        final PopupWindow popupWindow = new PopupWindow(this.mContext);
        ListView listViewVariants = new ListView(this.mContext);
        listViewVariants.setChoiceMode(3);
        setFilterAdapters(id, popupWindow, listViewVariants);
        listViewVariants.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long longId) {
                LobbyFragment.this.filterTableList(position, popupWindow, id);
            }
        });
        popupWindow.setFocusable(true);
        int width = (int) (this.mContext.getResources().getDimension(R.dimen.dropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        if (id == R.id.variants_layout || id == R.id.variants_drop_down_iv) {
            width = (int) (this.mContext.getResources().getDimension(R.dimen.variantDropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        } else if (id == R.id.game_type_layout || id == R.id.game_type_drop_down_iv) {
            width = (int) (this.mContext.getResources().getDimension(R.dimen.gameTypeDropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        } else if (id == R.id.chips_layout || id == R.id.chips_type_drop_down_iv) {
            width = (int) (this.mContext.getResources().getDimension(R.dimen.chipsDropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        } else if (id == R.id.bet_layout || id == R.id.bet_type_drop_down_iv) {
            width = (int) (this.mContext.getResources().getDimension(R.dimen.betDropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        } else if (id == R.id.player_type_layout || id == R.id.player_type_drop_down_iv) {
            width = (int) (this.mContext.getResources().getDimension(R.dimen.playerDropDownWidth) / this.mContext.getResources().getDisplayMetrics().density);
        }
        popupWindow.setHeight(-2);
        popupWindow.setWidth(width);
        popupWindow.setContentView(listViewVariants);
        return popupWindow;
    }

    public void setSelectedTable(Table table) {
        this.mSelectedTable = table;
    }

    public void showErrorPopUp(String message) {
        showGenericDialog(this.mContext, message);
    }

    public void showLowBalanceDialog(Context context, String message) {
        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.requestWindowFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_generic);
        ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
        ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                if (!LobbyFragment.this.mSelectedTable.getTable_cost().contains("CASH_CASH")) {
                    LobbyFragment.this.loadChips();
                }
            }
        });
        ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class PlayerComparator implements Comparator<Table> {
        public int compare(Table table1, Table table2) {
            int currentPlayers1 = Integer.parseInt(table1.getCurrent_players());
            int currentPlayers2 = Integer.parseInt(table2.getCurrent_players());
            if (currentPlayers2 < currentPlayers1) {
                return -1;
            }
            return currentPlayers2 > currentPlayers1 ? 1 : 0;
        }
    }

    private class RefreshAdapter extends AsyncTask<Event, Integer, List<Table>> {
        private RefreshAdapter() {
        }

        protected List<Table> doInBackground(Event... event) {
            String id = event[0].getId();
            if (LobbyFragment.this.tables != null && LobbyFragment.this.tables.size() > 0) {
                for (int i = LobbyFragment.this.tables.size() - 1; i >= 0; i--) {
                    Table table = (Table) LobbyFragment.this.tables.get(i);
                    if (id.equalsIgnoreCase(table.getId())) {
                        table.setCurrent_players(event[0].getCurrentPlayers());
                        table.setTotal_player(event[0].getTotalPlayers());
                        table.setTable_id(event[0].getTableId());
                        table.setJoined_players(event[0].getJoinedPlayers());
                        table.setStatus("Open");
                        break;
                    }
                }
            }
            return LobbyFragment.this.tables;
        }

        protected void onPostExecute(List<Table> list) {
            if (LobbyFragment.this.lobbyAdapter != null) {
                Collections.sort(LobbyFragment.this.getSortedList(), new PlayerComparator());
                LobbyFragment.this.lobbyAdapter.notifyDataSetChanged();
            }
        }
    }

    private void toggleFilterLayoutView(boolean show) {
        int width;

        if (show)
            width = 0;
        else
            width = filter_layout.getWidth();

        filter_layout.animate()
                .translationX(width)
                .alpha(1f)
                .setDuration(1000)
                .setListener(null);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getBackground().getConstantState() == unchecked_dr.getConstantState())
            toggleBackground(true, (CheckBox) compoundButton);
        else
            toggleBackground(false, (CheckBox) compoundButton);


        if (pools.isChecked()) {
            game_type_pools.setVisibility(View.VISIBLE);
        } else
            game_type_pools.setVisibility(View.GONE);
        if (deals.isChecked())
            game_type_deals.setVisibility(View.VISIBLE);
        else
            game_type_deals.setVisibility(View.GONE);
        if (points.isChecked())
            game_type_points.setVisibility(View.VISIBLE);
        else
            game_type_points.setVisibility(View.GONE);

        if (pools.isChecked() || deals.isChecked() || points.isChecked())
            select_variant.setVisibility(View.GONE);
        else if (!pools.isChecked() && !deals.isChecked() && !points.isChecked()) {
            select_variant.setVisibility(View.VISIBLE);
            game_type_pools.setVisibility(View.GONE);
            game_type_points.setVisibility(View.GONE);
            game_type_deals.setVisibility(View.GONE);
        }


        if (compoundButton == free) {
            if (free.isChecked()) {
                handleMultiSelection(0, CHIPS_FILTER);
                cash.setChecked(false);
            } else {
                handleMultiSelection(1, CHIPS_FILTER);
                cash.setChecked(true);
            }
        } else if (compoundButton == cash) {
            if (cash.isChecked()) {
                handleMultiSelection(1, CHIPS_FILTER);
                free.setChecked(false);
            } else {
                handleMultiSelection(0, CHIPS_FILTER);
                free.setChecked(true);
            }
        }

        if (compoundButton == pools) {
            handleMultiSelection(0, VARIANTS_FILTER);
            toggleBackground(true, pool101);
            toggleBackground(true, pool201);
            toggleBackground(true, bestOf3);
        } else if (compoundButton == deals) {
            handleMultiSelection(1, VARIANTS_FILTER);
            toggleBackground(true, bestOf2);
            toggleBackground(true, bestOf6);
        } else if (compoundButton == points) {
            handleMultiSelection(2, VARIANTS_FILTER);
            toggleBackground(true, jokerType);
            toggleBackground(true, noJokerType);
        } else if (compoundButton == pool101)
            handleMultiSelection(0, GAME_TYPE_FILTER);
        else if (compoundButton == pool201)
            handleMultiSelection(1, GAME_TYPE_FILTER);
        else if (compoundButton == bestOf3)
            handleMultiSelection(2, GAME_TYPE_FILTER);
        else if (compoundButton == lowBet)
            handleMultiSelection(0, BET_FILTER);
        else if (compoundButton == mediumBet)
            handleMultiSelection(1, BET_FILTER);
        else if (compoundButton == highBet)
            handleMultiSelection(2, BET_FILTER);
        else if (compoundButton == players2)
            handleMultiSelection(0, PLAYERS_FILTER);
        else if (compoundButton == players6)
            handleMultiSelection(1, PLAYERS_FILTER);
        else if (compoundButton == bestOf2) {
            if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(0, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(3, GAME_TYPE_FILTER);
        } else if (compoundButton == bestOf6) {
            if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(1, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(4, GAME_TYPE_FILTER);
        } else if (compoundButton == jokerType) {
            if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState() && deals.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(0, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState() && deals.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(5, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState() && deals.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(3, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState() && deals.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(2, GAME_TYPE_FILTER);
        } else if (compoundButton == noJokerType) {
            if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState() && deals.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(1, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState() && deals.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(6, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == checked_dr.getConstantState() && deals.getBackground().getConstantState() == unchecked_dr.getConstantState())
                handleMultiSelection(4, GAME_TYPE_FILTER);
            else if (pools.getBackground().getConstantState() == unchecked_dr.getConstantState() && deals.getBackground().getConstantState() == checked_dr.getConstantState())
                handleMultiSelection(3, GAME_TYPE_FILTER);
        }

        sortTablesList();
    }

    private void toggleBackground(boolean checked, CheckBox compoundButton) {
        if (checked)
            compoundButton.setBackground(checked_dr);
        else
            compoundButton.setBackground(unchecked_dr);
        compoundButton.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
    }


}
