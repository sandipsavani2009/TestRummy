package in.glg.rummy.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.cookie.ClientCookie;
import in.glg.rummy.CommonMethods.CommonMethods;
import in.glg.rummy.GameRoom.TableActivity;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.actionmenu.ActionItem;
import in.glg.rummy.actionmenu.QuickAction;
import in.glg.rummy.activities.BaseActivity;
import in.glg.rummy.adapter.TourneyResultsAdapter;
import in.glg.rummy.anim.Animation;
import in.glg.rummy.anim.AnimationListener;
import in.glg.rummy.anim.TransferAnimation;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.CardDiscard;
import in.glg.rummy.api.requests.LeaveTableRequest;
import in.glg.rummy.api.requests.RebuyRequest;
import in.glg.rummy.api.requests.TableDetailsRequest;
import in.glg.rummy.api.requests.TournamentsDetailsRequest;
import in.glg.rummy.api.response.CheckMeldResponse;
import in.glg.rummy.api.response.JoinTableResponse;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.api.response.RebuyResponse;
import in.glg.rummy.api.response.ShowEventResponse;
import in.glg.rummy.api.response.TableDeatailResponse;
import in.glg.rummy.api.response.TableExtraResponce;
import in.glg.rummy.api.response.TournamentDetailsResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.enums.GameEvent;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.fancycoverflow.FancyCoverFlow;
import in.glg.rummy.models.EngineRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.Game;
import in.glg.rummy.models.GameDetails;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.Levels;
import in.glg.rummy.models.MeldBox;
import in.glg.rummy.models.MeldCard;
import in.glg.rummy.models.MeldReply;
import in.glg.rummy.models.MeldRequest;
import in.glg.rummy.models.Middle;
import in.glg.rummy.models.PickDiscard;
import in.glg.rummy.models.PlayingCard;
import in.glg.rummy.models.RejoinRequest;
import in.glg.rummy.models.ScoreBoard;
import in.glg.rummy.models.SearchTableRequest;
import in.glg.rummy.models.SmartCorrectionRequest;
import in.glg.rummy.models.SplitRequest;
import in.glg.rummy.models.Stack;
import in.glg.rummy.models.TableCards;
import in.glg.rummy.models.TableDetails;
import in.glg.rummy.models.Tourney;
import in.glg.rummy.models.User;
import in.glg.rummy.service.HeartBeatService;
import in.glg.rummy.timer.RummyCountDownTimer;
import in.glg.rummy.utils.ErrorCodes;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PlayerComparator;
import in.glg.rummy.utils.PrefManagerTracker;
import in.glg.rummy.utils.SlideAnimation;
import in.glg.rummy.utils.SoundPoolManager;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;
import in.glg.rummy.utils.VibrationManager;
import in.glg.rummy.view.CorouselView;
import in.glg.rummy.view.RummyView;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip.Builder;


public class TablesFragment extends BaseFragment implements OnClickListener, OnItemClickListener {
    private static final int ID_DISCARD = 1;
    private static final int ID_GROUP = 2;
    private static final String TAG = GameEngine.class.getName();
    private boolean actionPerformed = false;
    private boolean autoExtraTime = false;
    private String autoPlayCount = "0";
    private boolean canLeaveTable = true;
    private boolean canShowCardDistributeAnimation = true;
    private ArrayList<PlayingCard> cardStack;
    int cardsSize = 12;
    private Dialog dropDialog;
    int count = 0;
    private OnResponseListener checkMeldListner = new OnResponseListener(CheckMeldResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.updateScoreOnPreMeld((CheckMeldResponse) response, mMeldCardsView);
            }
        }
    };
    private OnResponseListener autoPLayListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            TablesFragment.this.getTableDetailsFromAutoPlayResult();
        }
    };
    private OnResponseListener cardPickListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            if (response == null) {
            }
        }
    };
    private OnResponseListener declareListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.removeMeldCardsFragment();
            }
        }
    };
    private OnResponseListener disCardListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
        }
    };
    private OnResponseListener dropPlayerListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            if (response == null) {
                TablesFragment.this.mRummyView.removeAllViews();
            }
        }
    };
    private OnResponseListener extraTimeListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            JoinTableResponse extraTimeResponce = (JoinTableResponse) response;
            int code = Integer.parseInt(extraTimeResponce.getCode());
            if (extraTimeResponce != null && code == ErrorCodes.SUCCESS) {
                TablesFragment.this.mAutoExtraTimeEvent = null;
                TablesFragment.this.mUserAutoTimerTv.setText("0");
                TablesFragment.this.mGameShecduleTv.setText(TablesFragment.this.mContext.getString(R.string.user_chosen_extra_time_msg));
                TablesFragment.this.showView(TablesFragment.this.mGameShecduleTv);
                TablesFragment.this.handleTurnUpdateEvent(Integer.parseInt(TablesFragment.this.userData.getUserId()), extraTimeResponce.getTimeOut());
                TablesFragment.this.showView(TablesFragment.this.mUserAutoChunksLayout);
            } else if (code == ErrorCodes.ALREADY_REQUESTED_EXTRATIME) {
                TablesFragment.this.showGenericDialog(TablesFragment.this.mContext, TablesFragment.this.mContext.getString(R.string.extra_time_err_msg));
            }
        }
    };
    private OnResponseListener rebuyResponseListener = new OnResponseListener(RebuyResponse.class) {
        public void onResponse(Object response) {
            try {
                RebuyResponse rebuyResponse = (RebuyResponse) response;
                if (rebuyResponse != null) {
                    String name;
                    for (int i = 0; i < TablesFragment.this.mPlayerBoxesAll.size(); i++) {
                        name = ((TextView) (TablesFragment.this.mPlayerBoxesAll.get(i).findViewById(R.id.player_name_tv))).getText().toString();
                        if (name.equalsIgnoreCase(TablesFragment.this.userData.getNickName())) {
                            Log.e("TwoTables", String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(rebuyResponse.getTable_ammount())) + "@211"));
//                            Utils.PR_JOKER_POINTS = String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(rebuyResponse.getTable_ammount())));
                            ((TextView) (TablesFragment.this.mPlayerBoxesAll.get(i).findViewById(R.id.player_points_tv))).setText(String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(rebuyResponse.getTable_ammount()))));
                            ((TextView) (TablesFragment.this.mPlayerBoxesAll.get(i).findViewById(R.id.player_points_tv))).setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    };

    private ArrayList<PlayingCard> faceDownCardList;
    private ArrayList<PlayingCard> faceUpCardList;
    private boolean isCardsDistributing = false;
    private boolean isGameDescheduled = false;
    private boolean isGameResultsShowing = false;
    private boolean isGameStarted = false;
    private boolean isMeldFragmentShowing = false;
    private boolean isPlacedShow = false;
    private boolean isSmartCorrectionShowing = false;
    private boolean isSplitRequested = false;
    private boolean isTossEventRunning = false;
    private boolean isUserDropped = false;
    private boolean isUserPlacedValidShow = false;
    private boolean isWinnerEventExecuted = false;
    private boolean isYourTurn = false;
    private OnResponseListener leaveTableListner = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            JoinTableResponse joinTableResponse = (JoinTableResponse) response;
            if (joinTableResponse != null) {
                int code = Integer.parseInt(joinTableResponse.getCode());
                if (code == ErrorCodes.SUCCESS || code == ErrorCodes.NOT_ACCEPTABLE) {
                    ((TableActivity) TablesFragment.this.mContext).resetPlayerIconsOnTableBtn(TablesFragment.this.tableId);
                    if (((RummyApplication) TablesFragment.this.mContext.getApplication()).getJoinedTableIds().size() == 0) {
                        TablesFragment.this.mContext.finish();
                    } else {
                        ((TableActivity) TablesFragment.this.mContext).updateTableFragment(TablesFragment.this.tableId);
                    }
                }
            }
            TablesFragment.this.dismissLoadingDialog();
        }
    };
    private RummyApplication mApplication;
    private Event mAutoExtraTimeEvent = null;
    private TextView mBet;
    private ArrayList<PlayingCard> mCards;
    private ImageView mClosedCard;
    public static FragmentActivity mContext;
    private String mDealerId = "";
    private Button mDeclareBtn;
    private RelativeLayout mDialogLayout;
    private Drawable mDiscardImage;
    private ActionItem mDiscardView;
    private PlayingCard mDiscardedCard;
    private DrawerLayout mDrawerLayout;
    private Button mDropPlayer;
    private CheckBox mAutoDropPlayer;
    public static String myTableId = "";
    public static ArrayList<String> alAutoDrop = new ArrayList<>();
    public static ArrayList<Boolean> alAutoDropBoolean = new ArrayList<>();

    private String strGameId;
    private RelativeLayout mDummyLayout;
    private RelativeLayout mDummyOpenDeckLayout;
    private ArrayList<ImageView> mDummyVies = new ArrayList();
    private ImageView mExtraTimeBtn;
    private ImageView mFaceDownCards;
    private View mFifthPlayerAutoChunksLayout;
    private View mFifthPlayerAutoTimerLayout;
    private TextView mFifthPlayerAutoTimerTv;
    private View mFifthPlayerLayout;
    private View mFifthPlayerTimerLayout;
    private TextView mFifthPlayerTimerTv;
    private View mFourthPlayerAutoChunksLayout;
    private View mFourthPlayerAutoTimerLayout;
    private TextView mFourthPlayerAutoTimerTv;
    private View mFourthPlayerLayout;
    private View mFourthPlayerTimerLayout;
    private TextView mFourthPlayerTimerTv;
    private LinearLayout mGameDeckLayout;
    private ImageView mGameLogoIv;
    private HashMap<String, GamePlayer> mGamePlayerMap;
    private View mGameResultsView;
    private CountDownTimer mGameScheduleTimer;
    private TextView mGameShecduleTv;
    private TextView mGameType;
    private ArrayList<ArrayList<PlayingCard>> mGroupList = new ArrayList();
    private ActionItem mGroupView;
    private boolean mIamBack = false;
    private boolean mIsMelding = false;
    private PlayingCard mJockerCard;
    private ArrayList<GamePlayer> mJoinedPlayersList;
    private ImageView mJokerCard;
    private Dialog mLeaveDialog;
    private ImageView mLeaveTableBtn;
    private ImageView mLobbyBtn;
    private View mMeldCardsView;
    private ArrayList<ArrayList<PlayingCard>> mMeldGroupList;
    private int mNoOfGamesPlayed = 0;
    private ImageView mOpenCard;
    private ImageView mOpenJokerCard;
    private ImageView mPlayer2Cards;
    private ImageView mPlayer2TossCard;
    private ImageView mPlayer3Cards;
    private ImageView mPlayer3TossCard;
    private ImageView mPlayer4Cards;
    private ImageView mPlayer4TossCard;
    private ImageView mPlayer5Cards;
    private ImageView mPlayer5TossCard;
    private ImageView mPlayer6Cards;
    private ImageView mPlayer6TossCard;
    private TextView mPrizeMoney;
    private QuickAction mQuickAction;
    private CountDownTimer mRejoinTimer;
    private View mReshuffleView;
    private RummyView mRummyView;
    private View mSecondPlayerAutoChunksLayout;
    private View mSecondPlayerAutoTimerLayout;
    private TextView mSecondPlayerAutoTimerTv;
    private View mSecondPlayerLayout;
    private View mSecondPlayerTimerLayout;
    private TextView mSecondPlayerTimerTv;
    private ArrayList<PlayingCard> mSelectedCards;
    private ArrayList<ImageView> mSelectedImgList;
    private ArrayList<LinearLayout> mSelectedLayoutList;
    private ImageView mSettingsBtn;
    private Button mShowBtn;
    private View mSixthPlayerAutoChunksLayout;
    private View mSixthPlayerAutoTimerLayout;
    private TextView mSixthPlayerAutoTimerTv;
    private View mSixthPlayerLayout;
    private View mSixthPlayerTimerLayout;
    private TextView mSixthPlayerTimerTv;
    private View mSmartCorrectionView;
    private FrameLayout mSubFragment;
    private TableDetails mTableDetails;
    private TextView mTableId;
    private View mThirdPlayerAutoChunksLayout;
    private View mThirdPlayerAutoTimerLayout;
    private TextView mThirdPlayerAutoTimerTv;
    private View mThirdPlayerLayout;
    private View mThirdPlayerTimerLayout;
    private TextView mThirdPlayerTimerTv;
    private View mUserAutoChunksLayout;
    private int mUserAutoPlayCount = -1;
    private View mUserAutoTimerRootLayout;
    private TextView mUserAutoTimerTv;
    private RelativeLayout mUserDiscardLaout;
    private View mUserPlayerLayout;
    private View mUserTimerRootLayout;
    private TextView mUserTimerTv;
    private ImageView mUserTossCard;
    private CountDownTimer mWrongMeldTimer;
    private TextView mWrongMeldTv;
    private OnResponseListener meldListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.removeMeldCardsFragment();
                ShowEventResponse eventResponse = (ShowEventResponse) response;
                if (Integer.parseInt(eventResponse.getCode()) == ErrorCodes.SUCCESS) {
                    TablesFragment.this.clearAnimationData();
                    TablesFragment.this.clearSelectedCards();
                    TablesFragment.this.mRummyView.removeViews();
                    TablesFragment.this.mRummyView.invalidate();
                    TablesFragment.this.showView(TablesFragment.this.mGameShecduleTv);
                    TablesFragment.this.startGameScheduleTimer(Integer.parseInt(eventResponse.getMeldtimer()), true);
                    TablesFragment.this.hideView(TablesFragment.this.mDeclareBtn);
                    TablesFragment.this.disableView(TablesFragment.this.sortCards);
                    TablesFragment.this.disableUserOptions();
                }
            }
        }
    };
    private String meldMsgUdid;
    private String meldTimeOut = null;
    private RummyCountDownTimer meldTimer;
    private int messageVisibleCount = 1;
    private boolean opponentValidShow = false;
    private List<LinearLayout> playerCards;
    private CountDownTimer playerTurnOutTimer;
    private int playerUserId = -1;
    private OnResponseListener rejoinListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                TLog.e(TablesFragment.TAG, "rejoinListner :: " + response);
            }
        }
    };
    private View searchGameView;
    private OnResponseListener searchTableResponse = new OnResponseListener(JoinTableResponse.class) {
        public void onResponse(Object response) {
            TablesFragment.this.dismissDialog();
            if (response != null) {
                JoinTableResponse joinTableResponse = (JoinTableResponse) response;
                int code = Integer.parseInt(joinTableResponse.getCode());
                if (code == ErrorCodes.PLAYER_ALREADY_INPLAY || code == ErrorCodes.SUCCESS) {
                    TablesFragment.this.resetAllPlayers();
                    TablesFragment.this.resetDealer();
                    ((RummyApplication) TablesFragment.this.mContext.getApplication()).setJoinedTableIds(joinTableResponse.getTableId());
                    TablesFragment.this.removeWinnerDialog();
                    TablesFragment.this.removeGameResultFragment();
                    ((TableActivity) TablesFragment.this.mContext).updateFragment(TablesFragment.this.tableId, joinTableResponse.getTableId(), null);
                } else if (code == ErrorCodes.LOW_BALANCE) {
                    TablesFragment.this.showGenericDialog(TablesFragment.this.mContext, "You don't have enough balance to join this table");
                } else if (code == ErrorCodes.TABLE_FULL) {
                    TablesFragment.this.showGenericDialog(TablesFragment.this.mContext, "This table is full");
                }
            }
        }
    };
    private Dialog showDialog;
    private OnResponseListener showEventListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response == null) {
            }
        }
    };
    private SimpleTooltip simpleTooltip;
    private ArrayList<PlayingCard> slotCards;
    private OnResponseListener slotEventListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.removeMeldCardsFragment();
            }
        }
    };
    private Button sortCards;
    private OnResponseListener splitListner = new OnResponseListener(ShowEventResponse.class) {
        public void onResponse(Object response) {
            if (response == null) {
            }
        }
    };
    private View splitRejectedView;
    private OnResponseListener tableDetailsListner = new OnResponseListener(TableDeatailResponse.class) {
        public void onResponse(Object response) {
            TableDeatailResponse tableDeatailResponse = (TableDeatailResponse) response;
            if (tableDeatailResponse != null && tableDeatailResponse.getTableId() != null && tableDeatailResponse.getTableId().equalsIgnoreCase(TablesFragment.this.tableId)) {
                TablesFragment.this.strIsTourneyTable = tableDeatailResponse.getTournamentTable();
                TablesFragment.this.handleGetTableDetailsEvent(tableDeatailResponse.getTableDeatils(), tableDeatailResponse.getTimestamp());
            }
        }
    };
    private OnResponseListener tableExtraLisner = new OnResponseListener(TableExtraResponce.class) {
        public void onResponse(Object response) {
            TableExtraResponce tableExtraResponce = (TableExtraResponce) response;
            if (tableExtraResponce != null) {
                if (tableExtraResponce.getPickDiscardsList().size() > 0) {
                    isCardPicked = true;
                    Log.e(TAG, "CARD PICKED");
                } else
                    Log.e(TAG, "CARD NOT PICKED");

                if (Utils.SHOW_EVENT != null) {
                    Log.w(TAG, "SHOW REQUEST: " + Utils.SHOW_EVENT.getEventName());
                    doShowTemp(Utils.SHOW_EVENT);
                } else
                    Log.w(TAG, "SHOW REQUEST IS NULL");

                if (Utils.MELD_REQUEST != null) {
                    Log.w(TAG, "MELD REQUEST: " + Utils.MELD_REQUEST.getEventName());
                    doMeldTemp(Utils.MELD_REQUEST);
                } else
                    Log.w(TAG, "MELD REQUEST IS NULL");

                getTournamentDetails();
                Event event;
                Iterator it;
                PickDiscard pickDiscard;
                ScoreBoard scoreBoard = tableExtraResponce.getScoreBoard();
                Event gameResultEvent = tableExtraResponce.getEvent();
                if (scoreBoard != null) {
                    List<Game> gameList = scoreBoard.getGame();
                    if (gameList != null && gameList.size() > 0) {
                        ((TableActivity) TablesFragment.this.mContext).clearScoreBoard();
                        for (Game game : gameList) {
                            List<User> users = game.getGamePlayer();
                            event = new Event();
                            event.setGameId(game.getGameId());
                            event.setTableId(tableExtraResponce.getTableId());
                            List<GamePlayer> gamePlayerList = new ArrayList();
                            if (users == null || users.size() <= 0) {
                                GamePlayer player = new GamePlayer();
                                player.setUser_id(TablesFragment.this.userData.getUserId());
                                player.setNick_name(game.getNickName());
                                player.setPoints(game.getPoints());
                                player.setScore(game.getScore());
                                player.setTotalScore(game.getScore());
                                player.setResult(game.getResult());
                                gamePlayerList.add(player);
                            } else {
                                for (User user : users) {
                                    GamePlayer gamePlayer = new GamePlayer();
                                    gamePlayer.setUser_id(user.getUser_id());
                                    gamePlayer.setNick_name(user.getNick_name());
                                    gamePlayer.setScore(user.getScore());
                                    gamePlayer.setTotalScore(user.getTableScore());
                                    gamePlayerList.add(gamePlayer);
                                }
                            }
                            if (gamePlayerList.size() > 0) {
                                event.setPlayer(gamePlayerList);
                                ((TableActivity) TablesFragment.this.mContext).setGameResultEvents(event);
                            }
                        }
                    }
                }
                List<PickDiscard> pickDiscardList = tableExtraResponce.getPickDiscardsList();
                if (pickDiscardList != null && pickDiscardList.size() > 0) {
                    for (PickDiscard pickDiscard2 : pickDiscardList) {
                        if (pickDiscard2.getType().equalsIgnoreCase(ClientCookie.DISCARD_ATTR)) {
                            TablesFragment.this.turnCount = TablesFragment.this.turnCount + 1;
                            event = new Event();
                            event.setTableId(tableExtraResponce.getTableId());
                            event.setFace(pickDiscard2.getFace());
                            event.setSuit(pickDiscard2.getSuit());
                            event.setNickName(pickDiscard2.getNickName());
                            ((TableActivity) TablesFragment.this.mContext).addDiscardToPlayer(event);
                        }
                    }
                }
                ArrayList<PickDiscard> autoDiscardedList = new ArrayList();
                if (pickDiscardList != null && pickDiscardList.size() > 0) {
                    for (PickDiscard pickDiscard22 : pickDiscardList) {
                        if (pickDiscard22.getType().equalsIgnoreCase(ClientCookie.DISCARD_ATTR) && pickDiscard22.getAutoPlay().equalsIgnoreCase("true") && pickDiscard22.getUserId().equalsIgnoreCase(TablesFragment.this.userData.getUserId())) {
                            autoDiscardedList.add(pickDiscard22);
                        }
                    }
                    PickDiscard pickDiscard22;
                    if (autoDiscardedList.size() > 0) {
                        pickDiscard22 = (PickDiscard) autoDiscardedList.get(autoDiscardedList.size() - 1);
                        PlayingCard autoDiscardedCard = new PlayingCard();
                        autoDiscardedCard.setFace(pickDiscard22.getFace());
                        autoDiscardedCard.setSuit(pickDiscard22.getSuit());
                        PlayingCard lastAutoPlayDiscardedCard = autoDiscardedCard;
                        if (!(TablesFragment.this.slotCards == null || TablesFragment.this.cardStack == null || TablesFragment.this.slotCards.size() <= TablesFragment.this.cardStack.size())) {
                            String lastCardFace = lastAutoPlayDiscardedCard.getFace();
                            String lastCardSuit = lastAutoPlayDiscardedCard.getSuit();
                            it = TablesFragment.this.slotCards.iterator();
                            while (it.hasNext()) {
                                PlayingCard card = (PlayingCard) it.next();
                                String cardFace = card.getFace();
                                String cardSuit = card.getSuit();
                                if (lastCardFace.equalsIgnoreCase(cardFace) && lastCardSuit.equalsIgnoreCase(cardSuit)) {
                                    TablesFragment.this.slotCards.remove(card);
                                    break;
                                }
                            }
                            TablesFragment.this.setCardsOnIamBack(TablesFragment.this.slotCards);
                        }
                    }
                    if (TablesFragment.this.mUserAutoPlayCount != 0) {
                        TablesFragment.this.mUserAutoPlayCount = TablesFragment.this.mUserAutoPlayCount - 1;
                        if (autoDiscardedList.size() > 0 && TablesFragment.this.mUserAutoPlayCount > -1) {
                            int startIndex;
                            if (TablesFragment.this.mUserAutoPlayCount >= autoDiscardedList.size()) {
                                startIndex = 0;
                            } else {
                                startIndex = autoDiscardedList.size() - TablesFragment.this.mUserAutoPlayCount;
                                if (startIndex == autoDiscardedList.size()) {
                                    startIndex = autoDiscardedList.size() - 1;
                                }
                            }


                            for (int i = startIndex; i < autoDiscardedList.size() - 1; i++) {
                                pickDiscard22 = (PickDiscard) autoDiscardedList.get(i);

                                event = new Event();
                                event.setFace(pickDiscard22.getFace());
                                event.setSuit(pickDiscard22.getSuit());
                                event.setAutoPlay(pickDiscard22.getAutoPlay());
                                event.setUserId(Integer.parseInt(pickDiscard22.getUserId()));
                                event.setNickName(pickDiscard22.getNickName());
                                event.setTableId(tableExtraResponce.getTableId());
                                PlayingCard discardCard = new PlayingCard();
                                discardCard.setFace(event.getFace());
                                discardCard.setSuit(event.getSuit());
                                TablesFragment.this.showAutoDiscardedCards(event, discardCard);
                            }
                        }
                    } else {
                        return;
                    }
                }
                if (gameResultEvent != null && gameResultEvent.getEventName().equalsIgnoreCase("GAME_RESULT")) {
                    ((TableActivity) TablesFragment.this.mContext).updateLastHandEvent(gameResultEvent.getTableId(), gameResultEvent);
                }


            }
        }
    };

    private String tableId;
    private int turnCount = 0;
    private LoginResponse userData;
    private boolean userNotDeclaredCards;
    private String winnerId = "";
    private View winnerView;
    private PlayingCard tempDiscardedCard;
    private String discardedCardChildTag = "";
    private String mGameId = "";
    private LinearLayout levelTimerLayout;
    private RelativeLayout normal_game_bar;
    private TextView levelTimerValue, level_number_tv;
    private ImageView expandTourneyInfo, collapseTourneyInfo;
    private LinearLayout tourney_expanded_layout;
    private RelativeLayout tourneyBar;
    private int tourneyInfoMaxHeight;
    private String mTourneyId;
    private TournamentDetailsResponse mTourneyDetailsResponse;
    private List<Levels> mLevels;
    private CountDownTimer levelTimer;
    private String strIsTourneyTable = "";
    private Event mPlayersRank;
    private List<GamePlayer> mPlayersList;
    private HashMap<String, View> mPlayerBoxesForRanks = new HashMap<>();
    private ArrayList<View> mPlayerBoxesAll = new ArrayList<>();
    private TextView tourney_type_tv, entry_tourney_tv, bet_tourney_tv, rebuy_tourney_tv, rank_tourney_tv, balance_tourney_tv, tourney_prize_tv, game_level_tv;
    private TextView tid_tourney_tv, gameid_tourney_tv;
    private boolean isTourneyEnd;
    private Dialog disqualifyDialog;
    private boolean isTourneyBarVisible = false;
    private TextView game_id_tb;
    private static boolean isCardPicked = false;

    private LinearLayout player_2_autoplay_box;
    private LinearLayout player_3_autoplay_box;
    private LinearLayout player_4_autoplay_box;
    private LinearLayout player_5_autoplay_box;
    private LinearLayout player_6_autoplay_box;
    private TextView autoplay_count_player_2;
    private TextView autoplay_count_player_3;
    private TextView autoplay_count_player_4;
    private TextView autoplay_count_player_5;
    private TextView autoplay_count_player_6;

    private TournamentDetailsResponse mLevelsResponse;
    private OnResponseListener tournamentsDetailsListener = new OnResponseListener(TournamentDetailsResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.mTourneyDetailsResponse = (TournamentDetailsResponse) response;

                if (TablesFragment.this.mTourneyDetailsResponse.getCode().equalsIgnoreCase("200")) {
                    if (TablesFragment.this.mTourneyDetailsResponse.getData().equalsIgnoreCase("get_tournament_details")) {
                        TablesFragment.this.mLevels = ((TournamentDetailsResponse) response).getLevels();
                        getLevelTimer();
                    }
                } else {
                    Log.d(TAG, TablesFragment.this.mTourneyDetailsResponse.getCode() + " : tournamentsDetailsListener");
                }
            }
        }
    };

    private OnResponseListener levelsTimerListener = new OnResponseListener(TournamentDetailsResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TablesFragment.this.mLevelsResponse = (TournamentDetailsResponse) response;
                if (TablesFragment.this.mLevelsResponse.getCode().equalsIgnoreCase("200")) {
                    if (TablesFragment.this.mLevelsResponse.getData().equalsIgnoreCase("get_level_timer")) {
                        setLevelTimer();
                    }
                } else {
                    Log.d(TAG, TablesFragment.this.mLevelsResponse.getCode() + " : tournamentsDetailsListener");
                }
            }
        }
    };

    private OnResponseListener rebuyinListener = new OnResponseListener(TournamentDetailsResponse.class) {
        @Override
        public void onResponse(Object response) {
            if (response != null) {
                TournamentDetailsResponse rebuyinResponse = (TournamentDetailsResponse) response;
                if (rebuyinResponse.getCode().equalsIgnoreCase("200")) {
                    if (rebuyinResponse.getData().equalsIgnoreCase("tournament_rebuyin")) {

                    }
                } else {
                    Log.d(TAG, rebuyinResponse.getCode() + " : tournamentsDetailsListener");
                }
            }
        }
    };

    class C17265 extends AsyncTask<Void, Void, Void> {
        C17265() {
        }

        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(HeartBeatService.NOTIFY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TablesFragment.this.animateDispatchCards();
        }
    }

    class C17276 implements DrawerLayout.DrawerListener {
        C17276() {
        }

        public void onDrawerSlide(View drawerView, float slideOffset) {
        }

        public void onDrawerOpened(View drawerView) {
            TablesFragment.this.dismissQuickMenu();
        }

        public void onDrawerClosed(View drawerView) {
            try {
                if (!((TableActivity) TablesFragment.this.mContext).isSlideMenuVisible() && TablesFragment.this.mGameResultsView.getVisibility() != View.VISIBLE) {
                    TablesFragment.this.showQuickAction(TablesFragment.this.getTag());
                }
            } catch (Exception e) {
                Log.e("onDrawerClosed", e + "");
            }
        }

        public void onDrawerStateChanged(int newState) {
        }
    }

    public class FaceComparator implements Comparator<PlayingCard> {
        public int compare(PlayingCard p1, PlayingCard p2) {
            int face1 = Integer.valueOf(p1.getFace()).intValue();
            int face2 = Integer.valueOf(p2.getFace()).intValue();
            if (face1 == face2) {
                return 0;
            }
            if (face1 > face2) {
                return 1;
            }
            return -1;
        }
    }

    public boolean isUserNotDeclaredCards() {
        return this.userNotDeclaredCards;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game, container, false);
        try {
            init();
            getUserData();
            setIdsToViews(v);
            setBuildVersion();
            initializePlayerBoxesInList();
            handleBackButton(v);
            setListnersToViews();
            initGameRoom();
            checkGameType();
            if (((TableActivity) this.mContext).isIamBackShowing()) {
                boolean tableDetailsEventFound = false;
                Event tableDetailsEvent = null;

                Iterator it = Utils.tableDetailsList.iterator();
                while (it.hasNext()) {
                    Event event = (Event) it.next();
                    if (event.getEventName().equalsIgnoreCase("get_table_details")) {
                        TableDetails details = event.getTableDetails();
                        if (details.getTournament_table() != null && details.getTournament_table().equalsIgnoreCase("yes"))
                            this.strIsTourneyTable = "yes";
                        if (!(details == null || details.getTableId() == null || !details.getTableId().equalsIgnoreCase(this.tableId))) {
                            tableDetailsEventFound = true;
                            tableDetailsEvent = event;
                        }

                        if (details.getGameDetails() != null) {
                            this.mGameId = details.getGameDetails().getGameId();
                            this.gameid_tourney_tv.setText(this.mGameId);
                        }
                    } else if (event.getEventName().equalsIgnoreCase("players_rank")) {
                        this.mTourneyId = event.getTournamentId();
                        this.mPlayersRank = event;
                        this.mPlayersList = event.getPlayers();

                        this.updatePlayersRank();
                    }
                }
                if (!tableDetailsEventFound) {
                    getTableDetails();
                } else if (!(tableDetailsEvent == null || tableDetailsEvent.getTableDetails() == null)) {
                    handleGetTableDetailsEvent(tableDetailsEvent.getTableDetails(), tableDetailsEvent.getTimestamp());
                }

                this.checkTournament();
            } else {
                sendAutoPlayStatus();
            }
            setUserOptions(false);
            loadDummyCardsView();
            setTableButtonsUI();
            checkTourneyBalance();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }
        return v;
    }

    private void setBuildVersion() {
        TextView versionNumber_ms = (TextView) this.mMeldCardsView.findViewById(R.id.versionNumber_ms);
        TextView versionNumber_gs = (TextView) this.mGameResultsView.findViewById(R.id.versionNumber_gs);
        versionNumber_ms.setText("" + Utils.getVersionNumber(this.getContext()));
        versionNumber_gs.setText("" + Utils.getVersionNumber(this.getContext()));
    }

    private void initializePlayerBoxesInList() {
        this.mPlayerBoxesAll.clear();

        this.mPlayerBoxesAll.add(mUserPlayerLayout);
        this.mPlayerBoxesAll.add(mSecondPlayerLayout);
        this.mPlayerBoxesAll.add(mThirdPlayerLayout);
        this.mPlayerBoxesAll.add(mFourthPlayerLayout);
        this.mPlayerBoxesAll.add(mFifthPlayerLayout);
        this.mPlayerBoxesAll.add(mSixthPlayerLayout);
    }

    private void setTableButtonsUI() {
        TableActivity tableActivity = (TableActivity) this.mContext;
        boolean isIamBackShowing = tableActivity.isIamBackShowing();
        if (this.isGameResultsShowing || isIamBackShowing || this.isMeldFragmentShowing || this.isSmartCorrectionShowing) {
            tableActivity.hideGameTablesLayout(this.tableId);
        } else {
            tableActivity.showGameTablesLayout(this.tableId);
        }
    }

    public void hideQuickAction() {
        dismissQuickMenu();
        dismissToolTipView();
    }

    public void arrangeSelectedCards(String id) {
        if (id.equalsIgnoreCase(this.tableId)) {
            ArrayList<PlayingCard> selectedCards = new ArrayList();
            selectedCards.addAll(this.mSelectedCards);
            this.mSelectedCards.clear();
            Iterator it = selectedCards.iterator();
            while (it.hasNext()) {
                PlayingCard card = (PlayingCard) it.next();
                getLastSelectedCardView(card.getSuit() + card.getFace() + "-" + card.getIndex()).performClick();
            }
        }
    }

    public void showQuickAction(String id) {
        try {
            dismissQuickMenu();
            if (this.mGameResultsView.getVisibility() == View.VISIBLE || !id.equalsIgnoreCase(this.tableId)) {
                return;
            }
            if (this.mSelectedCards.size() == 0) {
                dismissQuickMenu();
                return;
            }
            int numberOfCards = getTotalCards();
            if (this.mSelectedCards.size() > 0) {
                PlayingCard lastCard = (PlayingCard) this.mSelectedCards.get(this.mSelectedCards.size() - 1);
                Iterator it = this.mSelectedImgList.iterator();
                while (it.hasNext()) {
                    ImageView img = (ImageView) it.next();
                    Object[] objArr = new Object[ID_DISCARD];
                    objArr[0] = img.getTag().toString();
                    String imgTagValue = String.format("%s", objArr);
                    String lastCardValue = String.format("%s%s-%s", new Object[]{lastCard.getSuit(), lastCard.getFace(), Integer.valueOf(lastCard.getIndex())});
                    if (imgTagValue.equalsIgnoreCase(lastCardValue)) {
                        this.mQuickAction = new QuickAction(getContext(), ID_DISCARD);
                        if (this.mSelectedCards.size() > ID_DISCARD) {
                            this.mGroupView = new ActionItem((int) ID_GROUP, "GROUP");
                            this.mQuickAction.addActionItem(this.mGroupView);
                            this.mQuickAction.show(getLastSelectedView(img));
                            if (this.mIsMelding) {
                                enableView(this.mShowBtn);
                            } else {
                                disableView(this.mShowBtn);
                            }
                        } else if (numberOfCards > 13) {
                            this.mDiscardView = new ActionItem((int) ID_DISCARD, "DISCARD");
                            this.mDiscardView.setTag(lastCardValue);
                            this.mQuickAction.addActionItem(this.mDiscardView);
                            LinearLayout lastSelectedLayout = getLastSelectedView(img);
                            animateCard(lastSelectedLayout);
                            this.mQuickAction.show(lastSelectedLayout);
                            enableView(this.mShowBtn);
                        }
                        this.mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                            public void onItemClick(QuickAction source, int pos, int actionId) {
                                if (actionId == TablesFragment.ID_DISCARD) {
                                    TablesFragment.this.mSelectedCards.remove(TablesFragment.this.mSelectedCards.get(0));
                                    TablesFragment.this.removeCardAndArrangeCards(TablesFragment.this.mDiscardView);
                                    TablesFragment.this.mQuickAction.dismiss();
                                } else if (actionId == TablesFragment.ID_GROUP) {
                                    TablesFragment.this.groupCards();
                                } else {
                                    TablesFragment.this.meldCards();
                                }
                            }
                        });
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("TablesFragment@926", e + "");
        }
    }

    private LinearLayout getLastSelectedCardView(String img) {
        RelativeLayout mainHolder = this.mRummyView.getRootView();
        if (mainHolder == null) {
            return null;
        }
        for (int i = 0; i < mainHolder.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) mainHolder.getChildAt(i);
            if (linearLayout != null && linearLayout.getTag() != null && img.equalsIgnoreCase(linearLayout.getTag().toString())) {
                return linearLayout;
            }
        }
        return null;
    }

    private LinearLayout getLastSelectedView(ImageView img) {
        Iterator it = this.mSelectedLayoutList.iterator();
        while (it.hasNext()) {
            LinearLayout linearLayout = (LinearLayout) it.next();
            if (img.getTag().toString().equalsIgnoreCase(linearLayout.getTag().toString())) {
                return linearLayout;
            }
        }
        return null;
    }

    private void loadDummyCardsView() {
        try {
            Log.e(TAG, "loadDummyCardsView@954");
            clearAnimationData();
            LayoutParams lprams = new LayoutParams(-1, -1);
            for (int i = 12; i >= 0; i--) {
                ImageView image = new ImageView(getActivity());
                image.setLayoutParams(lprams);
                image.setImageResource(R.drawable.closedcard);
                image.setVisibility(View.INVISIBLE);
                this.mDummyLayout.addView(image);
                this.mDummyVies.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }
    }

    private void animatePickCaCard(int position, View destinationView, boolean isFaceDown, boolean isUser) {
        if (!((TableActivity) this.mContext).isFromIamBack()) {
            if (!(this.faceUpCardList == null || this.faceUpCardList.size() != 1 || isFaceDown)) {
                hideView(this.mOpenJokerCard);
            }
            clearAnimationData();
            LayoutParams lprams = new LayoutParams(-1, -1);
            ImageView image;
            ImageView iv;
            TransferAnimation an;
            if (isFaceDown) {
                image = new ImageView(getContext());
                image.setLayoutParams(lprams);
                image.setImageResource(R.drawable.closedcard);
                image.setVisibility(View.INVISIBLE);
                this.mDummyLayout.addView(image);
                this.mDummyVies.add(image);
                iv = (ImageView) this.mDummyVies.get(position);
                iv.setVisibility(View.VISIBLE);
                an = new TransferAnimation(iv);
                an.setDuration(80);
                an.setListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        TablesFragment.this.clearAnimationData();
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                });
                if (!isUser) {
                    an.setDestinationView(destinationView).animate();
                    return;
                } else if (this.mRummyView.getChildAt(this.mRummyView.getChildCount() - 1) != null) {
                    an.setDestinationView(destinationView).animate();
                    return;
                } else {
                    return;
                }
            }
            image = new ImageView(getContext());
            image.setLayoutParams(lprams);
            image.setVisibility(View.INVISIBLE);
            image.setImageDrawable(this.mOpenCard.getDrawable());
            this.mDummyOpenDeckLayout.removeAllViews();
            this.mDummyOpenDeckLayout.addView(image);
            this.mDummyVies.add(image);
            iv = (ImageView) this.mDummyVies.get(position);
            iv.setVisibility(View.VISIBLE);
            an = new TransferAnimation(iv);
            an.setDuration(80);
            an.setListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    TablesFragment.this.clearAnimationData();
                }

                @Override
                public void onAnimationStart(Animation animation) {

                }
            });
            an.setDestinationView(destinationView).animate();
        }
    }

    private void animateDiscardCard(int position, View destinationView) {
        if (!((TableActivity) this.mContext).isFromIamBack()) {
            final RelativeLayout playerDiscardLayout = (RelativeLayout) destinationView.findViewById(R.id.player_discard_dummy_layout);
            playerDiscardLayout.removeAllViews();
            playerDiscardLayout.invalidate();
            clearAnimationData();
            LayoutParams lprams = new LayoutParams(-1, -1);
            ImageView image = new ImageView(getContext());
            image.setLayoutParams(lprams);
            image.setImageResource(R.drawable.closedcard);
            image.setVisibility(View.INVISIBLE);
            playerDiscardLayout.addView(image);
            this.mDummyVies.add(image);
            ImageView iv = (ImageView) this.mDummyVies.get(position);
            iv.setVisibility(View.VISIBLE);
            TransferAnimation an = new TransferAnimation(iv);
            an.setDuration(80);
            an.setListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    playerDiscardLayout.removeAllViews();
                    playerDiscardLayout.invalidate();
                    TablesFragment.this.clearAnimationData();
                }
            });
            an.setDestinationView(this.mOpenCard).animate();
        }
    }

    private void clearAnimationData() {
        this.mDummyOpenDeckLayout.removeAllViews();
        this.mDummyOpenDeckLayout.invalidate();
        this.mDummyLayout.removeAllViews();
        this.mDummyLayout.invalidate();
        this.mDummyVies.clear();
    }

    public void showDeclareHelpView() {
        if (((TableActivity) this.mContext).getActiveTableId().equalsIgnoreCase(this.tableId)) {
            showView(this.mDeclareBtn);
            enableView(this.mDeclareBtn);
            showView(this.sortCards);
            enableView(this.sortCards);
            showView(this.mShowBtn);
            dismissToolTipView();
            this.simpleTooltip = new Builder(this.mDeclareBtn.getContext()).anchorView(this.mDeclareBtn).text("Please group your cards and click on 'Declare' button.").gravity(48).animated(true).arrowColor(-1).backgroundColor(-1).transparentOverlay(false).build();
            this.simpleTooltip.show();
            return;
        }
        dismissToolTipView();
    }

    private void dismissToolTipView() {
        try {
            if (this.simpleTooltip != null) {
                this.simpleTooltip.dismiss();
            }
        } catch (Exception e) {
            TLog.e(TAG, "Exception in dismissToolTipView");
        }
    }

    private void setUserOptions(boolean showOptions) {
        if (showOptions) {
            showView(this.mExtraTimeBtn);
            showView(this.sortCards);
            showView(this.mDropPlayer);
            showView(this.mAutoDropPlayer);
            showView(this.mShowBtn);
            return;
        }
        if (((TableActivity) this.mContext).isFromIamBack()) {
            return;
        }
        invisibleView(this.mExtraTimeBtn);
        invisibleView(this.sortCards);
        invisibleView(this.mDropPlayer);
        invisibleView(this.mAutoDropPlayer);
        invisibleView(this.mShowBtn);
    }

    private void initGameRoom() {
        hidePlayerView();
        hideView(this.mDeclareBtn);
        disableView(this.mShowBtn);
        disableView(this.mDropPlayer);
        disableView(this.sortCards);
        disableView(this.mExtraTimeBtn);
        invisibleView(this.mUserTimerRootLayout);
    }

    private void hidePlayerView() {
        invisibleView(this.mSecondPlayerLayout);
        invisibleView(this.mThirdPlayerLayout);
        invisibleView(this.mFourthPlayerLayout);
        invisibleView(this.mFifthPlayerLayout);
        invisibleView(this.mSixthPlayerLayout);
    }

    private void showPlayerView() {
        showView(this.mSecondPlayerLayout);
        showView(this.mThirdPlayerLayout);
        showView(this.mFourthPlayerLayout);
        showView(this.mFifthPlayerLayout);
        showView(this.mSixthPlayerLayout);
    }

    private void hideTossCardsView() {
        invisibleView(this.mUserTossCard);
        invisibleView(this.mPlayer2TossCard);
        invisibleView(this.mPlayer3TossCard);
        invisibleView(this.mPlayer4TossCard);
        invisibleView(this.mPlayer5TossCard);
        invisibleView(this.mPlayer6TossCard);
    }

    private void init() {
        this.tableId = getTag();
        this.mIamBack = getArguments().getBoolean("iamBack");
        this.canShowCardDistributeAnimation = getArguments().getBoolean("canShowAnimation");
        this.mGamePlayerMap = new HashMap();
        this.mContext = getActivity();
        this.mJoinedPlayersList = new ArrayList();
        this.faceDownCardList = new ArrayList();
        this.faceUpCardList = new ArrayList();
        setUpFullScreen();
        this.mSelectedCards = new ArrayList();
        this.playerCards = new ArrayList();
        this.mCards = new ArrayList();
        this.mSelectedImgList = new ArrayList();
        this.mSelectedLayoutList = new ArrayList();
        this.mGroupList = new ArrayList();
        this.mMeldGroupList = new ArrayList();
        this.mApplication = (RummyApplication) this.mContext.getApplication();
        if (this.mApplication != null) {
            this.userData = this.mApplication.getUserData();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        cancelTimer(this.meldTimer);
        cancelTimer(this.playerTurnOutTimer);
        cancelTimer(this.mGameScheduleTimer);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        clearData();
    }

    String strJson = "";
    String comma = ",";
    public static ArrayList<String> alTrackList = new ArrayList<String>();

    private void getTrackSharedPrefs() {
        /*
        String[] trackKeys = {"userid", "tableid", "gameid", "subgameid", "startgame",
                "show", "drop", "eliminate", "gameend"};*/

        String[] trackKeys = new String[alTrackList.size()];
        trackKeys = alTrackList.toArray(trackKeys);
        comma = ",";
        strJson = "{\"events_list\"" + ": [";
        for (int i = 0; i < trackKeys.length; i++) {
//            Log.e(trackKeys[i]+"",PrefManagerTracker.getString(mContext, trackKeys[i], "NA")+"");
            if (i == trackKeys.length - 1) {
                comma = "";
            }
//            strJson += "\"" + trackKeys[i] + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, trackKeys[i], "NA") + "\"" + comma;
            strJson += "{";
            strJson += "\"" + "playerid" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "userid", "NA") + "\"" + ",";
            strJson += "\"" + "tableid" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "tableid", "NA") + "\"" + ",";
            strJson += "\"" + "gameid" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "gameid", "NA") + "\"" + ",";
            strJson += "\"" + "subgameid" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "subgameid", "NA") + "\"" + ",";
            strJson += "\"" + "event" + "\"" + ":" + "\"" + trackKeys[i] + "\"" + ",";
            strJson += "\"" + "timestamp" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, trackKeys[i], "NA") + "\"" + ",";
            strJson += "\"" + "log_level" + "\"" + ":" + "\"" + "inf" + "\"" + ",";
            strJson += "\"" + "chipstype" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "chipstype", "NA") + "\"" + ",";
            strJson += "\"" + "bet" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "bet", "NA") + "\"" + ",";
            strJson += "\"" + "gametype" + "\"" + ":" + "\"" + PrefManagerTracker.getString(mContext, "gametype", "NA") + "\"" + "";
            strJson += "}" + comma;
        }
        strJson += "]}";
//        Log.e("strJson", strJson + "");
        try {
            jsonObject = new JSONObject(strJson);
            Log.e("Json", jsonObject.toString());
            threadRequest();
//            Log.e("Json", jsonObject.getString("userid"));
        } catch (Throwable t) {
            Log.e("Throwable", "Could not parse malformed JSON: \"" + strJson + "\"");
        }
        PrefManagerTracker.clearPreferences(mContext);
    }

    private void threadRequest() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jsonObjectRequest();
                    // The code written in doInBackground()
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    JSONObject jsonObject;

    private void jsonObjectRequest() {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        // url
        String url = Utils.events_url;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        alTrackList.clear();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(jsonObjReq);
    }

    public static Context getTableFragment() {

        return mContext;
    }

    private void clearData() {
        loadDummyCardsView();
        Utils.MELD_REQUEST = null;
        Utils.SHOW_EVENT = null;
        this.game_id_tb.setText("");
        this.isCardPicked = false;
        this.turnCount = 0;
        this.isYourTurn = false;
        this.userNotDeclaredCards = false;
        this.isUserDropped = false;
        this.isGameStarted = false;
        this.isTossEventRunning = false;
        this.isCardsDistributing = false;
        this.isMeldFragmentShowing = false;
        this.canLeaveTable = true;
        this.isPlacedShow = false;
        this.opponentValidShow = false;
        dismissQuickMenu();
        this.mSelectedCards.clear();
        this.mIsMelding = false;
        this.isUserPlacedValidShow = false;
        this.faceUpCardList.clear();
        this.faceDownCardList.clear();
        cancelTimer(this.meldTimer);
        cancelTimer(this.mGameScheduleTimer);
        cancelTimer(this.playerTurnOutTimer);
        this.mRummyView.removeViews();
        this.mOpenCard.setVisibility(View.INVISIBLE);
        this.mUserTossCard.setVisibility(View.GONE);
        this.mJokerCard.setVisibility(View.GONE);
        this.mFaceDownCards.setVisibility(View.INVISIBLE);
        this.mGameShecduleTv.setVisibility(View.INVISIBLE);
        this.mOpenCard.setVisibility(View.INVISIBLE);
        this.mOpenJokerCard.setVisibility(View.GONE);
        this.mClosedCard.setVisibility(View.INVISIBLE);
        cancelTimer(this.meldTimer);
        cancelTimer(this.playerTurnOutTimer);
        this.mSecondPlayerLayout.setAlpha(1.0f);
        this.mThirdPlayerLayout.setAlpha(1.0f);
        this.mFourthPlayerLayout.setAlpha(1.0f);
        this.mFifthPlayerLayout.setAlpha(1.0f);
        this.mSixthPlayerLayout.setAlpha(1.0f);
        resetPlayerUi(this.mSecondPlayerLayout);
        resetPlayerUi(this.mThirdPlayerLayout);
        resetPlayerUi(this.mFourthPlayerLayout);
        resetPlayerUi(this.mFifthPlayerLayout);
        resetPlayerUi(this.mSixthPlayerLayout);
        resetPlayerUi(this.mUserPlayerLayout);
        disableView(this.mExtraTimeBtn);
        hideView(this.mDeclareBtn);
        hideView(this.mWrongMeldTv);
        setUserOptions(false);
        hideTimerUI();
        this.mGroupList.clear();
        invisibleView(this.mPlayer2Cards);
        invisibleView(this.mPlayer3Cards);
        invisibleView(this.mPlayer4Cards);
        invisibleView(this.mPlayer5Cards);
        invisibleView(this.mPlayer6Cards);
        hideView(this.mReshuffleView);
        sendTurnUpdateMessage(false);
        this.userData.setMiddleJoin(false);
    }

    private void cancelTimer(CountDownTimer timer) {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        MyWebEngage.trackScreenNameWE(MyWebEngage.GAME_TABLE_SCREEN, getContext());
    }

    private void setTableData(TableDetails tableDetails) {
        Log.e("setTableData", "setTableData");
        myTableId = tableDetails.getTableId() + "";
        alAutoDrop.add(myTableId + "");
        alAutoDropBoolean.add(false);
        this.mTableId.setText(String.format("%s%s", new Object[]{this.mContext.getString(R.string.hash), tableDetails.getTableId()}));
        this.mGameType.setText(Utils.formatTableName(tableDetails.getTableType()));
        this.mBet.setText(String.format("%s %s", new Object[]{tableDetails.getBet(), Utils.getTableType(tableDetails.getTableCost())}));

        Crashlytics.setString("table_id", String.format("%s%s", new Object[]{this.mContext.getString(R.string.hash), tableDetails.getTableId()}));

        PrefManagerTracker.saveString(getContext(), "gametype", String.format("%s %s", new Object[]{tableDetails.getBet(), Utils.getTableType(tableDetails.getTableCost())}) + "");
        Log.e("gametype", String.format("%s %s", new Object[]{tableDetails.getBet(), Utils.getTableType(tableDetails.getTableCost())}) + "");
        String chipsTypeBet = String.format("%s %s", new Object[]{tableDetails.getBet(), Utils.getTableType(tableDetails.getTableCost())});
        String[] chipsTypeBetAry = chipsTypeBet.split(" ");
        PrefManagerTracker.saveString(getContext(), "bet", chipsTypeBetAry[0] + "");
        Log.e("bet", chipsTypeBetAry[0] + "");
        PrefManagerTracker.saveString(getContext(), "chipstype", chipsTypeBetAry[1] + "");
        Log.e("chipstype", chipsTypeBetAry[1] + "");

//        Crashlytics.setString("game_type", String.format("%s %s", new Object[]{tableDetails.getBet(), Utils.getTableType(tableDetails.getTableCost())}));
//        Crashlytics.setString("game_bet", chipsTypeBetAry[0]);
//        Crashlytics.setString("chips_type", chipsTypeBetAry[1]);
    }

    private void dispatchCards() {
        new C17265().execute(new Void[0]);
    }

    private void animateDispatchCards() {
        if (this.count >= this.playerCards.size() - 1) {
            this.count = 0;
        }
    }

    private void setUpFullScreen() {
        this.mContext.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setIdsToViews(View v) {
        this.mPlayer2Cards = (ImageView) v.findViewById(R.id.player_2_cards);
        this.mPlayer3Cards = (ImageView) v.findViewById(R.id.player_3_cards);
        this.mPlayer4Cards = (ImageView) v.findViewById(R.id.player_4_cards);
        this.mPlayer5Cards = (ImageView) v.findViewById(R.id.player_5_cards);
        this.mPlayer6Cards = (ImageView) v.findViewById(R.id.player_6_cards);
        this.mDummyLayout = (RelativeLayout) v.findViewById(R.id.dummy_layout);
        this.mDummyOpenDeckLayout = (RelativeLayout) v.findViewById(R.id.dummy_open_deck_layout);
        this.mUserDiscardLaout = (RelativeLayout) v.findViewById(R.id.user_discard_dummy_layout);
        ((ListView) this.mContext.findViewById(R.id.settingsListView)).setOnItemClickListener(this);
        this.mSubFragment = (FrameLayout) v.findViewById(R.id.inner_content_frame);
        this.mDialogLayout = (RelativeLayout) v.findViewById(R.id.dialog_layout);
        this.mReshuffleView = v.findViewById(R.id.reshuffle_view);
        hideView(this.mReshuffleView);
        this.winnerView = v.findViewById(R.id.winner_view);
        this.searchGameView = v.findViewById(R.id.search_table_view);
        this.splitRejectedView = v.findViewById(R.id.split_rjected_view);
        this.mDummyLayout = (RelativeLayout) v.findViewById(R.id.dummy_layout);
        this.mRummyView = (RummyView) v.findViewById(R.id.rummy_view);
        this.mGameShecduleTv = (TextView) v.findViewById(R.id.game_start_time_tv);
        this.mWrongMeldTv = (TextView) v.findViewById(R.id.wrong_meld_tv);
        this.mExtraTimeBtn = (ImageView) v.findViewById(R.id.extra_time_btn);
        this.sortCards = (Button) v.findViewById(R.id.sort_cards);
        this.mDropPlayer = (Button) v.findViewById(R.id.drop_player_iv);
        this.mAutoDropPlayer = v.findViewById(R.id.auto_drop_player_iv);
        this.mShowBtn = (Button) v.findViewById(R.id.show_iv);
        this.mDeclareBtn = (Button) v.findViewById(R.id.declare_iv);
        this.mSettingsBtn = (ImageView) v.findViewById(R.id.settings_btn);
        this.mLeaveTableBtn = (ImageView) v.findViewById(R.id.exit_btn);
        this.mLobbyBtn = (ImageView) v.findViewById(R.id.lobby_back_btn);
        this.mDrawerLayout = (DrawerLayout) this.mContext.findViewById(R.id.drawer_layout);
        this.mDrawerLayout.addDrawerListener(new C17276());
        this.mClosedCard = (ImageView) v.findViewById(R.id.closed_card_iv);
        this.mUserTimerRootLayout = v.findViewById(R.id.timer_root_ayout);
        this.mUserTimerTv = (TextView) this.mUserTimerRootLayout.findViewById(R.id.player_timer_tv);
        this.mUserAutoTimerRootLayout = v.findViewById(R.id.auto_timer_root_ayout);
        this.mUserAutoTimerTv = (TextView) this.mUserAutoTimerRootLayout.findViewById(R.id.player_auto_timer_tv);
        this.mSecondPlayerAutoTimerLayout = v.findViewById(R.id.player_2_auto_timer_root_ayout);
        this.mSecondPlayerAutoTimerTv = (TextView) this.mSecondPlayerAutoTimerLayout.findViewById(R.id.player_auto_timer_tv);
        this.mThirdPlayerAutoTimerLayout = v.findViewById(R.id.player_3_auto_timer_root_ayout);
        this.mThirdPlayerAutoTimerTv = (TextView) this.mThirdPlayerAutoTimerLayout.findViewById(R.id.player_auto_timer_tv);
        this.mFourthPlayerAutoTimerLayout = v.findViewById(R.id.player_4_auto_timer_root_ayout);
        this.mFourthPlayerAutoTimerTv = (TextView) this.mFourthPlayerAutoTimerLayout.findViewById(R.id.player_auto_timer_tv);
        this.mFifthPlayerAutoTimerLayout = v.findViewById(R.id.player_5_auto_timer_root_ayout);
        this.mFifthPlayerAutoTimerTv = (TextView) this.mFifthPlayerAutoTimerLayout.findViewById(R.id.player_auto_timer_tv);
        this.mSixthPlayerAutoTimerLayout = v.findViewById(R.id.player_6_auto_timer_root_ayout);
        this.mSixthPlayerAutoTimerTv = (TextView) this.mSixthPlayerAutoTimerLayout.findViewById(R.id.player_auto_timer_tv);
        this.mUserAutoChunksLayout = v.findViewById(R.id.player_1_auto_chunks_root_ayout);
        this.mSecondPlayerAutoChunksLayout = v.findViewById(R.id.player_2_auto_chunks_root_ayout);
        this.mThirdPlayerAutoChunksLayout = v.findViewById(R.id.player_3_auto_chunks_root_ayout);
        this.mFourthPlayerAutoChunksLayout = v.findViewById(R.id.player_4_auto_chunks_root_ayout);
        this.mFifthPlayerAutoChunksLayout = v.findViewById(R.id.player_5_auto_chunks_root_ayout);
        this.mSixthPlayerAutoChunksLayout = v.findViewById(R.id.player_6_auto_chunks_root_ayout);
        this.mGameType = (TextView) v.findViewById(R.id.table_type_tv);
        this.mTableId = (TextView) v.findViewById(R.id.table_id_tv);
        this.mGameType = (TextView) v.findViewById(R.id.table_type_tv);
        this.mPrizeMoney = (TextView) v.findViewById(R.id.prize_tv);
        this.mBet = (TextView) v.findViewById(R.id.bet_tv);
        this.mSecondPlayerLayout = v.findViewById(R.id.player_2_box);
        this.mThirdPlayerLayout = v.findViewById(R.id.player_3_box);
        this.mFourthPlayerLayout = v.findViewById(R.id.player_4_box);
        this.mFifthPlayerLayout = v.findViewById(R.id.player_5_box);
        this.mSixthPlayerLayout = v.findViewById(R.id.player_6_box);
        this.mUserPlayerLayout = v.findViewById(R.id.user_details_layout);
        this.mUserTossCard = (ImageView) v.findViewById(R.id.user_toss_card);
        this.mPlayer2TossCard = (ImageView) v.findViewById(R.id.player_2_toss_card);
        this.mPlayer3TossCard = (ImageView) v.findViewById(R.id.player_3_toss_card);
        this.mPlayer4TossCard = (ImageView) v.findViewById(R.id.player_4_toss_card);
        this.mPlayer5TossCard = (ImageView) v.findViewById(R.id.player_5_toss_card);
        this.mPlayer6TossCard = (ImageView) v.findViewById(R.id.player_6_toss_card);
        this.mOpenCard = (ImageView) v.findViewById(R.id.open_deck_card_iv);
        this.mOpenJokerCard = (ImageView) v.findViewById(R.id.open_jocker_iv);
        this.mJokerCard = (ImageView) v.findViewById(R.id.jokerImageView);
        this.mFaceDownCards = (ImageView) v.findViewById(R.id.faceDownCards);
        this.mSecondPlayerTimerLayout = v.findViewById(R.id.player_2_timer_layout);
        this.mSecondPlayerTimerTv = (TextView) this.mSecondPlayerTimerLayout.findViewById(R.id.player_timer_tv);
        this.mThirdPlayerTimerLayout = v.findViewById(R.id.player_3_timer_layout);
        this.mThirdPlayerTimerTv = (TextView) this.mThirdPlayerTimerLayout.findViewById(R.id.player_timer_tv);
        this.mFourthPlayerTimerLayout = v.findViewById(R.id.player_4_timer_layout);
        this.mFourthPlayerTimerTv = (TextView) this.mFourthPlayerTimerLayout.findViewById(R.id.player_timer_tv);
        this.mFifthPlayerTimerLayout = v.findViewById(R.id.player_5_timer_layout);
        this.mFifthPlayerTimerTv = (TextView) this.mFifthPlayerTimerLayout.findViewById(R.id.player_timer_tv);
        this.mSixthPlayerTimerLayout = v.findViewById(R.id.player_6_timer_layout);
        this.mSixthPlayerTimerTv = (TextView) this.mSixthPlayerTimerLayout.findViewById(R.id.player_timer_tv);
        this.mGameResultsView = v.findViewById(R.id.game_results_view);
        this.mMeldCardsView = v.findViewById(R.id.meld_cards_view);
        this.mSmartCorrectionView = v.findViewById(R.id.sc_view);
        this.mRummyView.setTableFragment(this);
        this.mGameDeckLayout = (LinearLayout) v.findViewById(R.id.game_deck_layout);
        this.mGameLogoIv = (ImageView) v.findViewById(R.id.game_taj_logo_iv);
        this.levelTimerLayout = (LinearLayout) v.findViewById(R.id.levelTimerLayout);
        this.tourneyBar = (RelativeLayout) v.findViewById(R.id.tourneyBar);
        this.tourney_expanded_layout = (LinearLayout) v.findViewById(R.id.tourney_expanded_layout);
        this.normal_game_bar = (RelativeLayout) v.findViewById(R.id.normal_game_bar);
        this.levelTimerValue = (TextView) v.findViewById(R.id.levelTimerValue);
        this.level_number_tv = (TextView) v.findViewById(R.id.level_number_tv);
        this.expandTourneyInfo = (ImageView) v.findViewById(R.id.expandTourneyInfo);
        this.collapseTourneyInfo = (ImageView) v.findViewById(R.id.collapseTourneyInfo);

        this.tourney_type_tv = (TextView) v.findViewById(R.id.tourney_type_tv);
        this.entry_tourney_tv = (TextView) v.findViewById(R.id.entry_tourney_tv);
        this.bet_tourney_tv = (TextView) v.findViewById(R.id.bet_tourney_tv);
        this.rebuy_tourney_tv = (TextView) v.findViewById(R.id.rebuy_tourney_tv);
        this.rank_tourney_tv = (TextView) v.findViewById(R.id.rank_tourney_tv);
        this.balance_tourney_tv = (TextView) v.findViewById(R.id.balance_tourney_tv);
        this.tourney_prize_tv = (TextView) v.findViewById(R.id.tourney_prize_tv);
        this.game_level_tv = (TextView) v.findViewById(R.id.game_level_tv);

        this.tid_tourney_tv = (TextView) v.findViewById(R.id.tid_tourney_tv);
        this.gameid_tourney_tv = (TextView) v.findViewById(R.id.gameid_tourney_tv);
        this.game_id_tb = (TextView) v.findViewById(R.id.game_id_tb);

        this.player_2_autoplay_box = (LinearLayout) v.findViewById(R.id.player_2_autoplay_box);
        this.player_3_autoplay_box = (LinearLayout) v.findViewById(R.id.player_3_autoplay_box);
        this.player_4_autoplay_box = (LinearLayout) v.findViewById(R.id.player_4_autoplay_box);
        this.player_5_autoplay_box = (LinearLayout) v.findViewById(R.id.player_5_autoplay_box);
        this.player_6_autoplay_box = (LinearLayout) v.findViewById(R.id.player_6_autoplay_box);

        this.autoplay_count_player_2 = (TextView) v.findViewById(R.id.autoplay_count_player_2);
        this.autoplay_count_player_3 = (TextView) v.findViewById(R.id.autoplay_count_player_3);
        this.autoplay_count_player_4 = (TextView) v.findViewById(R.id.autoplay_count_player_4);
        this.autoplay_count_player_5 = (TextView) v.findViewById(R.id.autoplay_count_player_5);
        this.autoplay_count_player_6 = (TextView) v.findViewById(R.id.autoplay_count_player_6);
    }

    private void setListnersToViews() {
        this.sortCards.setOnClickListener(this);
        this.mSettingsBtn.setOnClickListener(this);
        this.mFaceDownCards.setOnClickListener(this);
        this.mOpenCard.setOnClickListener(this);
        this.mDropPlayer.setOnClickListener(this);
        this.mLeaveTableBtn.setOnClickListener(this);
        this.mShowBtn.setOnClickListener(this);
        this.mDeclareBtn.setOnClickListener(this);
        this.mLobbyBtn.setOnClickListener(this);
        this.mExtraTimeBtn.setOnClickListener(this);
        this.expandTourneyInfo.setOnClickListener(this);
        this.collapseTourneyInfo.setOnClickListener(this);
        this.tourney_expanded_layout.setOnClickListener(this);

        mAutoDropPlayer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                invisibleView(mDropPlayer);
                setAutoDropSetting(isChecked);
            }
        });
    }

    private void setAutoDropCheck(boolean isChecked) {
        Log.e("myTableId", myTableId + "");
        mAutoDropPlayer.setChecked(isChecked);
    }

    private void setAutoDropSetting(boolean isChecked) {
        Log.e("myTableId", myTableId + "");
        Log.e("alAutoDrop", alAutoDrop.size() + "");
        Log.e("alAutoDropBoolean", alAutoDropBoolean.size() + "");

        int intIndex = alAutoDrop.indexOf(myTableId);
        Log.e("intIndex", intIndex + "");
        alAutoDropBoolean.set(intIndex, isChecked);
        mAutoDropPlayer.setChecked(isChecked);

    }

    private String getGameId() {
        Log.e("game_id_tb", game_id_tb.getText() + "");
        String gameId = game_id_tb.getText() + "";
        String[] gameIdArray = gameId.split("-");
        gameId = gameIdArray[0];
        return gameId;
    }

    public boolean isActionPerformed() {
        return this.actionPerformed;
    }

    public void onClick(View v) {
        this.actionPerformed = true;

        if(v == mLobbyBtn)
        {
            dismissQuickMenu();
            ((TableActivity) this.mContext).setIsBackPressed(true);
            Utils.SHOW_LOBBY = true;
            ((BaseActivity) this.mContext).showLobbyScreen();
        }
        else if(v == mFaceDownCards)
        {
            dismissQuickMenu();
            this.mGroupList = this.mRummyView.getUpdatedCardsGroup();
            if (this.mGroupList != null && this.mGroupList.size() > 0) {
                pickCardFromClosedDeck();
                return;
            }
        }
        else if(v == mOpenCard)
        {
            dismissQuickMenu();
            this.mGroupList = this.mRummyView.getUpdatedCardsGroup();
            if (this.mGroupList != null && this.mGroupList.size() > 0) {
                pickCardFromOpenDeck();
                return;
            }
        }
        else if(v == mLeaveTableBtn)
        {
            dismissQuickMenu();
            int totalNoOfCard = getTotalCards();
            if (this.strIsTourneyTable.equalsIgnoreCase("yes")) {
                if (TablesFragment.this.tableId != null) {
                    if (this.isTourneyEnd)
                        TablesFragment.this.mContext.finish();
                    else if (this.userData.isMiddleJoin() || (this.canLeaveTable && totalNoOfCard <= 13))
                        TablesFragment.this.showLeaveTourneyDialog();
                    else {
                        String message;
                        if (totalNoOfCard == 14) {
                            message = getString(R.string.leave_table_pick_card);
                        } else if (this.isTossEventRunning) {
                            message = getString(R.string.leave_table_toss);
                        } else if (this.isCardsDistributing) {
                            message = getString(R.string.leave_table_card_distribution);
                        } else if (this.isUserPlacedValidShow) {
                            message = getString(R.string.leave_table_show);
                        } else {
                            message = getString(R.string.leave_table_info);
                        }
                        showGenericDialog(this.mContext, message);
                    }
                } else {
                    TablesFragment.this.mContext.finish();
                }
            } else {

                if (this.isUserDropped) {
                    showLeaveTableDialog();
                    return;
                } else if (this.userData.isMiddleJoin() || (this.canLeaveTable && totalNoOfCard <= 13)) {
                    showLeaveTableDialog();
                    return;
                } else {
                    String message;
                    if (totalNoOfCard == 14) {
                        message = getString(R.string.leave_table_pick_card);
                    } else if (this.isTossEventRunning) {
                        message = getString(R.string.leave_table_toss);
                    } else if (this.isCardsDistributing) {
                        message = getString(R.string.leave_table_card_distribution);
                    } else if (this.isUserPlacedValidShow) {
                        message = getString(R.string.leave_table_show);
                    } else {
                        message = getString(R.string.leave_table_info);
                    }
                    showGenericDialog(this.mContext, message);
                }
            }
        }
        else if(v == mSettingsBtn)
        {
            toggleNavigationMenu();
        }
        else if(v == mExtraTimeBtn)
        {
            turnExtraTime();
        }
        else if(v == sortCards)
        {
            dismissQuickMenu();
            sortPlayerCards();
        }
        else if(v == mDropPlayer)
        {
            disableView(mDropPlayer);
            dismissQuickMenu();
            showDropDialog();
        }
        else if(v == mShowBtn)
        {
            //                mBet.setText(cardsSize);
            disableView(mShowBtn);
            this.userPlacedShow = true;
            //this.groupCards();
            SoundPoolManager.getInstance().playSound(R.raw.toss);
            VibrationManager.getInstance().vibrate(1);
            this.mRummyView.getPlayerDiscardCard().setVisibility(View.INVISIBLE);
            showPlaceShowDialog(v);

        }
        else if(v == mDeclareBtn)
        {
            SoundPoolManager.getInstance().playSound(R.raw.toss);
            VibrationManager.getInstance().vibrate(1);
            this.mRummyView.getPlayerDiscardCard().setVisibility(View.INVISIBLE);
            launchMeldFragment();
        }
        else if(v == expandTourneyInfo)
        {
            animateTourneyInfo(true);
        }
        else if(v == collapseTourneyInfo)
        {
            animateTourneyInfo(false);
        }
        else if(v == tourney_expanded_layout)
        {
            if (this.isTourneyBarVisible)
                animateTourneyInfo(false);
            else
                animateTourneyInfo(true);
        }

    }

    private void animateTourneyInfo(boolean showView) {
        SlideAnimation animation;
        if (showView) {
            animation = new SlideAnimation(tourney_expanded_layout, 0, tourneyInfoMaxHeight);
            showView(collapseTourneyInfo);
            hideView(expandTourneyInfo);
            isTourneyBarVisible = true;
        } else {
            animation = new SlideAnimation(tourney_expanded_layout, tourneyInfoMaxHeight, 0);
            hideView(collapseTourneyInfo);
            showView(expandTourneyInfo);
            isTourneyBarVisible = false;
        }

        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(400);
        tourney_expanded_layout.setAnimation(animation);
        tourney_expanded_layout.startAnimation(animation);
    }

    private void requestSplit() {
        cancelTimer(this.mGameScheduleTimer);
        removeGameResultFragment();
        this.isSplitRequested = true;
        SplitRequest request = new SplitRequest();
        request.setUuid(Utils.generateUuid());
        request.setTableId(this.tableId);
        request.setCommand("split");
        request.setUserId(this.userData.getUserId());
        request.setNick_name(this.userData.getNickName());
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.splitListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    public void showLeaveTableDialog() {
        this.mLeaveDialog = getLeaveTableDialog(this.mContext, this.mContext.getString(R.string.leave_table_msg));
        ((Button) this.mLeaveDialog.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TablesFragment.this.mLeaveDialog.dismiss();
                if (TablesFragment.this.tableId != null) {
                    if (TablesFragment.this.strIsTourneyTable.equalsIgnoreCase("yes"))
                        TablesFragment.this.leaveTournament();
                    else
                        TablesFragment.this.leaveTable();
                } else {
                    TablesFragment.this.mContext.finish();
                }

            }
        });
        ((Button) this.mLeaveDialog.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TablesFragment.this.mLeaveDialog.dismiss();
            }
        });
        this.mLeaveDialog.show();
    }

    public void showLeaveTourneyDialog() {
        this.mLeaveDialog = getLeaveTableDialog(this.mContext, this.mContext.getString(R.string.leave_table_msg));
        ((Button) this.mLeaveDialog.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TablesFragment.this.mLeaveDialog.dismiss();
                if (TablesFragment.this.tableId != null) {
                    TablesFragment.this.leaveTournament();
                } else {
                    TablesFragment.this.mContext.finish();
                }
            }
        });
        ((Button) this.mLeaveDialog.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TablesFragment.this.mLeaveDialog.dismiss();
            }
        });
        this.mLeaveDialog.show();
    }

    @SuppressLint("WrongConstant")
    private void toggleNavigationMenu() {
        if (this.mDrawerLayout.isDrawerOpen(5)) {
            this.mDrawerLayout.closeDrawer(5);
            return;
        }
        this.mDrawerLayout.openDrawer(5);
        dismissQuickMenu();
    }

    private void pickCardFromClosedDeck() {
        int numberOfCards = getTotalCards();
        if (canPickCard(numberOfCards) && numberOfCards < 14) {
            SoundPoolManager.getInstance().playSound(R.raw.pick_discard);
            VibrationManager.getInstance().vibrate(1);
            clearSelectedCards();
            if (this.faceDownCardList != null && this.faceDownCardList.size() > 0) {
                PlayingCard playerCard = (PlayingCard) this.faceDownCardList.get(this.faceDownCardList.size() - 1);
                cardPick(playerCard.getSuit(), playerCard.getFace(), "face_down");
                addCardToStack(playerCard);
                this.faceDownCardList.remove(this.faceDownCardList.size() - 1);
            }
        }
    }

    private void pickCardFromOpenDeck() {
        int totalCards = getTotalCards();
        if (canPickCard(totalCards) && totalCards < 14) {
            PlayingCard playerCard = (PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1);
            if (this.isCardPicked == true) {
                Log.w(TAG, "Not the first card");
                if (this.mJockerCard != null && ((this.mJockerCard.getFace().equalsIgnoreCase(playerCard.getFace()) && this.turnCount > 1) || (this.mJockerCard.getFace().equalsIgnoreCase(playerCard.getFace()) && this.turnCount <= 1 && this.faceUpCardList.size() > 1))) {
                    showJokerInfo();
                } else if (this.turnCount > 1 && playerCard.getFace().equalsIgnoreCase("0")) {
                    showJokerInfo();
                } else if (this.turnCount <= 1 && playerCard.getFace().equalsIgnoreCase("0") && this.faceUpCardList.size() > 1) {
                    showJokerInfo();
                } else if (this.turnCount > 1 && this.mJockerCard.getFace().equalsIgnoreCase("0") && playerCard.getFace().equalsIgnoreCase("1")) {
                    showJokerInfo();
                } else if (this.turnCount > 1 || !this.mJockerCard.getFace().equalsIgnoreCase("0") || !playerCard.getFace().equalsIgnoreCase("1") || this.faceUpCardList.size() <= 1) {
                    clearSelectedCards();
                    SoundPoolManager.getInstance().playSound(R.raw.pick_discard);
                    VibrationManager.getInstance().vibrate(1);
                    this.mGameShecduleTv.setVisibility(View.INVISIBLE);
                    cardPick(playerCard.getSuit(), playerCard.getFace(), "face_up");
                    addCardToStack(playerCard);
                    this.faceUpCardList.remove(this.faceUpCardList.size() - 1);
                    if (this.faceUpCardList.size() > 0) {
                        setOpenCard((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
                        return;
                    }
                    this.mOpenJokerCard.setVisibility(View.INVISIBLE);
                    this.mOpenCard.setVisibility(View.INVISIBLE);
                } else {
                    showJokerInfo();
                }
            } else {
                Log.w(TAG, "Picking first card");
                clearSelectedCards();
                SoundPoolManager.getInstance().playSound(R.raw.pick_discard);
                VibrationManager.getInstance().vibrate(1);
                this.mGameShecduleTv.setVisibility(View.INVISIBLE);
                cardPick(playerCard.getSuit(), playerCard.getFace(), "face_up");
                addCardToStack(playerCard);
                this.faceUpCardList.remove(this.faceUpCardList.size() - 1);
                if (this.faceUpCardList.size() > 0) {
                    setOpenCard((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
                    return;
                }
                this.mOpenJokerCard.setVisibility(View.INVISIBLE);
                this.mOpenCard.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showJokerInfo() {
        this.mGameShecduleTv.setVisibility(View.VISIBLE);
        this.mGameShecduleTv.setText(this.mContext.getString(R.string.joker_card_pick_info_msg));
    }

    public void addCardToStack(PlayingCard playerCard) {
        ArrayList<PlayingCard> cardsList = new ArrayList();
        ((ArrayList) this.mGroupList.get(this.mGroupList.size() - 1)).add(playerCard);
        setGroupView(false);
    }

    private boolean canPickCard(int numberOfCards) {
        if (numberOfCards > 13 && this.isYourTurn) {
            this.mGameShecduleTv.setText(R.string.card_pick_msg);
            this.mGameShecduleTv.setVisibility(View.VISIBLE);
            return false;
        } else if (numberOfCards != 13 || this.isYourTurn) {
            return true;
        } else {
            this.mGameShecduleTv.setText(R.string.info_wait_for_turn);
            this.mGameShecduleTv.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private void addCardToRummyView(final PlayingCard playerCard, int index) {
        try {
            final LinearLayout cardLayout = this.mRummyView.getCard();
            final ImageView cardImg = (ImageView) cardLayout.findViewById(R.id.cardImageView);
            ImageView jockerImg = (ImageView) cardLayout.findViewById(R.id.jokerCardImg);
            String cardValue = String.format("%s%s", new Object[]{playerCard.getSuit(), playerCard.getFace()});

            if (this.mJockerCard != null) {
                if (playerCard != null && playerCard.getFace().equalsIgnoreCase(this.mJockerCard.getFace())) {
                    jockerImg.setVisibility(View.VISIBLE);
                } else if (playerCard == null || !playerCard.getFace().equalsIgnoreCase("1")) {
                    jockerImg.setVisibility(View.GONE);
                } else if (this.mJockerCard.getFace().equalsIgnoreCase("0")) {
                    jockerImg.setVisibility(View.VISIBLE);
                } else {
                    jockerImg.setVisibility(View.GONE);
                }
            }
            int imgId = this.mContext.getResources().getIdentifier(cardValue, "drawable", this.mContext.getPackageName());
            cardImg.setVisibility(View.VISIBLE);
            cardImg.setImageResource(imgId);
            String tag = String.format("%s-%s", new Object[]{cardValue, String.valueOf(index)});
            cardImg.setTag(tag);
            cardLayout.setTag(tag);
            playerCard.setIndex(String.valueOf(index));
            cardImg.setId(index);
            cardLayout.setId(index);
            cardLayout.setOnClickListener(new OnClickListener() {
                class C17201 implements QuickAction.OnActionItemClickListener {
                    C17201() {
                    }

                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        if (actionId == 1) {
                            TablesFragment.this.mSelectedCards.remove(playerCard);
                            TablesFragment.this.removeCardAndArrangeCards(TablesFragment.this.mDiscardView);
                            TablesFragment.this.mQuickAction.dismiss();
                        } else if (actionId == 2) {
                            TablesFragment.this.groupCards();
                        } else {
                            TablesFragment.this.meldCards();
                        }
                    }
                }

                class C17212 implements QuickAction.OnActionItemClickListener {
                    C17212() {
                    }

                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        if (actionId == 1) {
                            TablesFragment.this.mSelectedCards.remove(playerCard);
                            TablesFragment.this.removeCardAndArrangeCards(TablesFragment.this.mDiscardView);
                            TablesFragment.this.mQuickAction.dismiss();
                        } else if (actionId == 2) {
                            TablesFragment.this.groupCards();
                        } else {
                            TablesFragment.this.meldCards();
                        }
                    }
                }

                public void onClick(View v) {
                    ImageView selectedCardIv = (ImageView) v.findViewById(R.id.cardImageViewSelected);
                    TablesFragment.this.showView(selectedCardIv);
                    int numberOfCards = TablesFragment.this.getTotalCards();
                    Iterator it = TablesFragment.this.mSelectedCards.iterator();
                    while (it.hasNext()) {
                        String imgTagValue;
                        PlayingCard card = (PlayingCard) it.next();
                        if (String.format("%s", new Object[]{v.getTag().toString()}).equalsIgnoreCase(String.format("%s%s-%s", new Object[]{card.getSuit(), card.getFace(), card.getIndex()}))) {
                            TablesFragment.this.hideView(selectedCardIv);
                            v.clearAnimation();
                            TablesFragment.this.dismissQuickMenu();
                            TablesFragment.this.mSelectedCards.remove(card);
                            TablesFragment.this.disableView(TablesFragment.this.mShowBtn);
                            if (TablesFragment.this.mSelectedCards.size() > 0) {
                                PlayingCard lastCard = (PlayingCard) TablesFragment.this.mSelectedCards.get(TablesFragment.this.mSelectedCards.size() - 1);
                                it = TablesFragment.this.mSelectedImgList.iterator();
                                while (it.hasNext()) {
                                    ImageView img = (ImageView) it.next();
                                    imgTagValue = String.format("%s", new Object[]{img.getTag().toString()});
                                    String lastCardValue = String.format("%s%s-%s", new Object[]{lastCard.getSuit(), lastCard.getFace(), lastCard.getIndex()});
                                    if (imgTagValue.equalsIgnoreCase(lastCardValue)) {
                                        TablesFragment.this.mQuickAction = new QuickAction(v.getContext(), 1);
                                        if (TablesFragment.this.mSelectedCards.size() > 1) {
                                            TablesFragment.this.mGroupView = new ActionItem(2, "GROUP");
                                            TablesFragment.this.mQuickAction.addActionItem(TablesFragment.this.mGroupView);
                                            TablesFragment.this.mQuickAction.show(TablesFragment.this.getLastSelectedView(img));
                                            if (TablesFragment.this.mIsMelding) {
                                                TablesFragment.this.enableView(TablesFragment.this.mShowBtn);
                                            } else {
                                                TablesFragment.this.disableView(TablesFragment.this.mShowBtn);
                                            }
                                        } else if (numberOfCards > 13) {
                                            TablesFragment.this.mDiscardView = new ActionItem(1, "DISCARD");
                                            TablesFragment.this.mDiscardView.setTag(lastCardValue);
                                            TablesFragment.this.mQuickAction.addActionItem(TablesFragment.this.mDiscardView);
                                            LinearLayout linearLayout = TablesFragment.this.getLastSelectedView(img);
                                            TablesFragment.this.animateCard(linearLayout);
                                            TablesFragment.this.mQuickAction.show(linearLayout);
                                            TablesFragment.this.enableView(TablesFragment.this.mShowBtn);
                                        }
                                        TablesFragment.this.mQuickAction.setOnActionItemClickListener(new C17201());
                                        return;
                                    }
                                }
                                return;
                            }
                            return;
                        }
                    }
                    TablesFragment.this.mSelectedCards.add(playerCard);
                    TablesFragment.this.mSelectedImgList.add(cardImg);
                    TablesFragment.this.mSelectedLayoutList.add(cardLayout);
                    TablesFragment.this.animateCard(v);
                    TablesFragment.this.dismissQuickMenu();
                    TablesFragment.this.mQuickAction = new QuickAction(v.getContext(), 1);
                    if (TablesFragment.this.mSelectedCards.size() > 1) {
                        TablesFragment.this.mGroupView = new ActionItem(2, "GROUP");
                        TablesFragment.this.mQuickAction.addActionItem(TablesFragment.this.mGroupView);
                        TablesFragment.this.mQuickAction.show(v);
                        if (TablesFragment.this.mIsMelding) {
                            TablesFragment.this.enableView(TablesFragment.this.mShowBtn);
                        } else {
                            TablesFragment.this.disableView(TablesFragment.this.mShowBtn);
                        }
                    } else if (numberOfCards > 13) {
                        String imgTagValue = String.format("%s", new Object[]{v.getTag().toString()});
                        TablesFragment.this.mDiscardView = new ActionItem(1, "DISCARD");
                        TablesFragment.this.mDiscardView.setTag(imgTagValue);
                        TablesFragment.this.mQuickAction.addActionItem(TablesFragment.this.mDiscardView);
                        TablesFragment.this.mQuickAction.show(v);
                        TablesFragment.this.setDiscardCard(playerCard);
                        TablesFragment.this.enableView(TablesFragment.this.mShowBtn);
                    }
                    TablesFragment.this.mQuickAction.setOnActionItemClickListener(new C17212());
                }
            });
            this.playerCards.add(cardLayout);
            this.mRummyView.addCard(cardLayout);
        } catch (Exception e) {
            TLog.e(TAG, "Exception :: addCardToRummyView " + e.getMessage());
        }
    }

    private void meldCards() {
        ArrayList<PlayingCard> meldCards = new ArrayList();
        PlayingCard card = getDiscardedCard();
        String mCardValue = String.format("%s%s%s", new Object[]{card.getSuit(), card.getFace(), card.getIndex()});
        Iterator it = this.mSelectedCards.iterator();
        CardDiscard playingCard = new CardDiscard();
        while (it.hasNext()) {
            if (!String.format("%s%s%s", new Object[]{playingCard.getSuit(), playingCard.getFace(), ((PlayingCard) it.next()).getIndex()}).equalsIgnoreCase(mCardValue)) {
                meldCards.add((PlayingCard) it.next());
            }
        }
        this.mMeldGroupList.add(meldCards);
        this.mSelectedCards.clear();
        removeCardsOnMeld(meldCards);
    }

    private void setDiscardCard(PlayingCard card) {
        this.mDiscardedCard = card;
    }

    private int getTotalCards() {
        int totalCards = 0;
        Iterator it = this.mGroupList.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((ArrayList) it.next()).iterator();
            while (it2.hasNext()) {
                PlayingCard card = (PlayingCard) it2.next();
                totalCards++;
            }
        }
        return totalCards;
    }

    private void groupCards() {
        for (int i = this.mGroupList.size() - 1; i >= 0; i--) {
            ArrayList<PlayingCard> cardGroup = (ArrayList) this.mGroupList.get(i);
            for (int j = cardGroup.size() - 1; j >= 0; j--) {
                PlayingCard mcard = (PlayingCard) cardGroup.get(j);
                String mCardValue = String.format("%s%s-%s", new Object[]{mcard.getSuit(), mcard.getFace(), mcard.getIndex()});
                for (int x = this.mSelectedCards.size() - 1; x >= 0; x--) {
                    PlayingCard selectedCard = (PlayingCard) this.mSelectedCards.get(x);
                    if (String.format("%s%s-%s", new Object[]{selectedCard.getSuit(), selectedCard.getFace(), selectedCard.getIndex()}).equalsIgnoreCase(mCardValue)) {
                        cardGroup.remove(j);
                    }
                }
            }
        }
        ArrayList<PlayingCard> cardsList = new ArrayList();
        cardsList.addAll(this.mSelectedCards);
        this.mGroupList.add(cardsList);
        setGroupView(true);
        clearSelectedCards();
    }

    private void clearSelectedCards() {
        dismissQuickMenu();
        this.mSelectedCards.clear();
        this.mSelectedImgList.clear();
        this.mSelectedLayoutList.clear();
    }

    private void removeCardAndArrangeCards(ActionItem actionItem) {
        this.isYourTurn = false;
        this.mGameShecduleTv.setVisibility(View.INVISIBLE);
        String discardTag = actionItem.getTag();
        String discardSuit = null;
        String disCardFace = null;
        Iterator it = this.mGroupList.iterator();
        while (it.hasNext()) {
            ArrayList<PlayingCard> groupList = (ArrayList) it.next();
            Iterator it2 = groupList.iterator();
            while (it2.hasNext()) {
                PlayingCard card = (PlayingCard) it2.next();
                if (discardTag.equalsIgnoreCase(String.format("%s%s-%s", new Object[]{card.getSuit(), card.getFace(), card.getIndex()}))) {
                    discardSuit = card.getSuit();
                    disCardFace = card.getFace();
                    groupList.remove(card);
                    break;
                }
            }
        }
        if (!(discardSuit == null || disCardFace == null)) {
            discardCard(discardSuit, disCardFace);
        }
        clearSelectedCards();
        setGroupView(false);
    }

    private void getTableDetails() {
        if (this.tableId != null) {
            TableDetailsRequest request = new TableDetailsRequest();
            request.setCommand("get_table_details");
            request.setTableId(this.tableId);
            request.setUuid(Utils.generateUuid());
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tableDetailsListner);
            } catch (GameEngineNotRunning e) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getTableDetailsFromAutoPlayResult() {
        if (this.tableId != null) {
            TableDetailsRequest request = new TableDetailsRequest();
            request.setCommand("get_table_details");
            request.setTableId(this.tableId);
            request.setUuid(Utils.generateUuid());
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tableDetailsListner);
            } catch (GameEngineNotRunning e) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGetTableDetailsEvent(TableDetails tableDetails, String timeStampStr) {
        if (tableDetails != null) {
            GamePlayer player;
            int i;
            this.mTableDetails = tableDetails;
            ((TableActivity) this.mContext).clearDiscards(this.tableId);
            int maxNoOfPlayers = Integer.parseInt(this.mTableDetails.getMaxPlayer());
            ((TableActivity) this.mContext).setTableDetailsList(tableDetails);
            setTableData(tableDetails);
            setDealerId(tableDetails.getDealer_id());
            String gameStartTime = tableDetails.getStarttime();
            if (gameStartTime != null && gameStartTime.matches(".*\\d.*")) {
                int timerValue = (int) (Double.valueOf(gameStartTime).doubleValue() - Double.valueOf(timeStampStr).doubleValue());
                if (!this.opponentValidShow) {
                    startGameScheduleTimer(timerValue, false);
                }
            }
            List<GamePlayer> playerList = tableDetails.getPlayers();
            int playerCount = playerList.size();
            if (playerCount == 1) {
                showView(this.mGameShecduleTv);
                this.mGameShecduleTv.setText(this.mContext.getString(R.string.wait_for_next_player_msg));
                ((TableActivity) this.mContext).resetPlayerIconsOnTableBtn(this.tableId);
            }
            if (Integer.parseInt(tableDetails.getMaxPlayer()) == 2) {
                hidePlayerView();
                showView(this.mUserPlayerLayout);
                showView(this.mFourthPlayerLayout);
            } else {
                showPlayerView();
            }
            String middleJoin = tableDetails.getMiddlejoin();
            if (middleJoin != null && middleJoin.equalsIgnoreCase("True")) {
                setMiddleJoinPlayers(tableDetails, playerList);
            }
            for (GamePlayer player2 : playerList) {
                if (player2.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                    String autoPlayCountStr = player2.getAutoplay_count();
                    if (autoPlayCountStr != null) {
                        this.mUserAutoPlayCount = Integer.parseInt(autoPlayCountStr);
                        break;
                    }
                }
            }
            Collections.sort(playerList, new PlayerComparator());
            GamePlayer player2;
            if (Boolean.valueOf(tableDetails.getGameStart()).booleanValue()) {
                GameDetails gameDetails = tableDetails.getGameDetails();
                if (gameDetails != null) {
                    ((TableActivity) this.mContext).updateGameId(tableDetails.getTableId(), gameDetails.getGameId());
                    this.game_id_tb.setText("#" + gameDetails.getGameId());
                    this.mPrizeMoney.setText(gameDetails.getPrizeMoney());
                }
                ArrayList<GamePlayer> sortedPlayerList = new ArrayList();
                if (maxNoOfPlayers == 2 || playerList.size() == 2) {
                    for (i = 0; i < playerList.size(); i++) {
                        player2 = (GamePlayer) playerList.get(i);
                        if (player2.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                            player2.setSeat("1");
                        } else {
                            player2.setSeat("4");
                        }
                        this.mJoinedPlayersList.add(player2);
                        this.mGamePlayerMap.put(player2.getUser_id(), player2);
                        Log.d(TAG, "SEATING VIA: player Size 2");
                        setUpPlayerUI(player2, false);
                        if (this.mTableDetails != null) {
                            setPlayerPositionsOnTableBtn(this.mTableDetails, player2, false);
                        }
                    }
                } else {
                    int userPlace = 0;
                    for (i = 0; i < playerList.size(); i++) {
                        if (((GamePlayer) playerList.get(i)).getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                            userPlace = i;
                            break;
                        }
                    }
                    for (i = 0; i < playerList.size(); i++) {
                        sortedPlayerList.add(i, playerList.get(userPlace));
                        userPlace = (userPlace + 1) % playerList.size();
                    }
                    playerList.clear();
                    playerList.addAll(sortedPlayerList);
                    for (i = 0; i < playerList.size(); i++) {
                        player2 = (GamePlayer) playerList.get(i);
                        player2.setSeat(String.valueOf(i + 1));
                        this.mJoinedPlayersList.add(player2);
                        this.mGamePlayerMap.put(player2.getUser_id(), player2);
                        Log.d(TAG, "SEATING VIA: player size not 2");
                        setUpPlayerUIForRunningGame(player2, false);
                        setPlayerPositionsOnTableBtn(tableDetails, player2, false);
                    }
                }
            } else {
                for (i = 0; i < playerCount; i++) {
                    player2 = (GamePlayer) playerList.get(i);
                    player2.setSeat(String.valueOf(player2.getPlace()));
                    this.mJoinedPlayersList.add(player2);
                    this.mGamePlayerMap.put(player2.getUser_id(), player2);
                    Log.d(TAG, "SEATING VIA: getGameStart is false");
                    setUpPlayerUI(player2, false);
                    setPlayerPositionsOnTableBtn(tableDetails, player2, false);
                }
            }
            List<GamePlayer> dropPayerList = tableDetails.getDropList();
            if (dropPayerList != null && dropPayerList.size() > 0) {
                setDroppedPlayers(playerList, dropPayerList);
                for (GamePlayer player22 : dropPayerList) {
                    if (player22.isDropped()) {
                        handlePlayerDrop(Integer.parseInt(player22.getUser_id()));
                    }
                }
            }
            for (i = 0; i < this.mJoinedPlayersList.size(); i++) {
                setAutoPlayUIonReconnect((GamePlayer) this.mJoinedPlayersList.get(i));
            }

            if (this.strIsTourneyTable.equalsIgnoreCase("yes")) {
                this.bet_tourney_tv.setText(tableDetails.getBet());
            }
        }
        broadCastEvents();
        getTableExtra();
        this.updatePlayersRank();
    }

    private void setDroppedPlayers(List<GamePlayer> playerList, List<GamePlayer> dropPayerList) {
        if (dropPayerList != null && dropPayerList.size() > 0) {
            for (GamePlayer player : playerList) {
                for (GamePlayer dropPlayer : dropPayerList) {
                    if (player.getUser_id().equalsIgnoreCase(dropPlayer.getUser_id()) && !player.isMiddleJoin()) {
                        dropPlayer.setDropped(true);
                    }
                }
            }
        }
    }

    private void setAutoPlayUIonReconnect(GamePlayer player) {
        String autoPlay = player.getAutoplay();
        if (autoPlay != null && autoPlay.equalsIgnoreCase("true")) {
            player.setAutoplay(player.getAutoplay());
            player.setTotalCount("5");
            String autoPlayCount = player.getAutoplay_count();
            if (autoPlayCount != null) {
                player.setAutoplay_count(autoPlayCount);
            }
            setAutoPlayUI(player);
        }
    }

    private void sendAutoPlayStatus() {
        try {
            RummyApplication app = (RummyApplication) getActivity().getApplication();
            TableDetailsRequest request = new TableDetailsRequest();
            request.setCommand("autoplaystatus");
            request.setUuid(Utils.generateUuid());
            request.setUserId(String.valueOf(app.getUserData().getUserId()));
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.autoPLayListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "sendIamBack" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void setPlayerPositionsOnTableBtn(TableDetails tableDetails, GamePlayer player, boolean isLeft) {
        if (tableDetails != null) {
            ((TableActivity) this.mContext).setGameTableBtnUI(tableDetails.getTableId(), player, Integer.parseInt(tableDetails.getMaxPlayer()), isLeft);
        }
    }

    private void setMiddleJoinPlayers(TableDetails tableDetails, List<GamePlayer> playerList) {
        Middle middle = tableDetails.getMiddle();
        if (middle != null) {
            List<GamePlayer> gamePlayerList = middle.getPlayer();
            if (gamePlayerList != null) {
                for (int i = 0; i < gamePlayerList.size(); i++) {
                    GamePlayer gamePlayer = (GamePlayer) gamePlayerList.get(i);
                    gamePlayer.setMiddleJoin(true);
                    playerList.add(gamePlayer);
                    if (gamePlayer.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                        this.userData.setMiddleJoin(true);
                    }
                }
            }
        }
    }

    private void setDealerId(String dealerId) {
        if (dealerId != null && dealerId.length() > 0) {
            this.mDealerId = dealerId;
        }
    }

    private void broadCastEvents() {
        this.mApplication = (RummyApplication) this.mContext.getApplication();
        List<Event> tempList = this.mApplication.getEventList();
        List<Event> eventList = new ArrayList();
        eventList.addAll(tempList);
        for (Event event : eventList) {
            onMessageEvent(event);
        }
    }

    private void setSeating(List<GamePlayer> playerList) {
        int i = 0;
        while (i < playerList.size()) {
            GamePlayer player = (GamePlayer) playerList.get(i);
            int seat = i + 1;
            if (playerList.size() == 2 && i == 1) {
                seat += 2;
            }
            player.setBuyinammount(player.getMinimumBuyin());
            player.setSeat(String.valueOf(seat));
            this.mJoinedPlayersList.add(player);
            Log.d(TAG, "SEATING VIA: setSeating");
            setUpPlayerUI(player, false);
            if (this.mTableDetails != null) {
                setPlayerPositionsOnTableBtn(this.mTableDetails, player, false);
            }
            i++;
        }
    }

    public List<GamePlayer> getJoinedPlayerList() {
        return this.mJoinedPlayersList;
    }

    private void resetDealer() {
        hideView(this.mUserPlayerLayout.findViewById(R.id.player_dealer_iv));
        hideView(this.mSecondPlayerLayout.findViewById(R.id.player_dealer_iv));
        hideView(this.mThirdPlayerLayout.findViewById(R.id.player_dealer_iv));
        hideView(this.mFourthPlayerLayout.findViewById(R.id.player_dealer_iv));
        hideView(this.mFifthPlayerLayout.findViewById(R.id.player_dealer_iv));
        hideView(this.mSixthPlayerLayout.findViewById(R.id.player_dealer_iv));
    }

    private void setUpPlayerUI(GamePlayer player, boolean isPlayerJoined) {
        Log.d(TAG, "Setting player position-->> " + player.getNick_name() + " (" + player.getPlace() + ")");

        String seat;
        if (this.mGamePlayerMap.get(player.getUser_id()) != null) {
            player.setPlayerlevel(((GamePlayer) this.mGamePlayerMap.get(player.getUser_id())).getPlayerlevel());
        }
        if (player.getSeat() != null) {
            seat = player.getSeat();
        } else {
            seat = "1";
        }
        Crashlytics.setString("player_" + seat, player.getUser_id() + " - " + player.getNick_name());
        switch (Integer.parseInt(seat)) {
            case 1:
                setUserDetails(this.mUserPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mUserPlayerLayout);
                return;
            case 2:
                showView(this.mSecondPlayerLayout);
                setUpPlayerDetails(this.mSecondPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mSecondPlayerLayout);
                return;
            case 3:
                showView(this.mThirdPlayerLayout);
                setUpPlayerDetails(this.mThirdPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mThirdPlayerLayout);
                return;
            case 4:
                showView(this.mFourthPlayerLayout);
                setUpPlayerDetails(this.mFourthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mFourthPlayerLayout);
                return;
            case 5:
                showView(this.mFifthPlayerLayout);
                setUpPlayerDetails(this.mFifthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mFifthPlayerLayout);
                return;
            case 6:
                showView(this.mSixthPlayerLayout);
                setUpPlayerDetails(this.mSixthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mSixthPlayerLayout);
                return;
            default:
                return;
        }
    }

    private void setUpPlayerUIForRunningGame(GamePlayer player, boolean isPlayerJoined) {
        Log.d(TAG, "Setting player position-->> " + player.getNick_name() + " (" + player.getPlace() + ")");

        String seat;
        if (this.mGamePlayerMap.get(player.getUser_id()) != null) {
            player.setPlayerlevel(((GamePlayer) this.mGamePlayerMap.get(player.getUser_id())).getPlayerlevel());
        }
        if (player.getSeat() != null) {
            seat = player.getSeat();
        } else {
            seat = "1";
        }
        switch (Integer.parseInt(seat)) {
            case 1:
                setUserDetails(this.mUserPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mUserPlayerLayout);
                return;
            case 2:
                showView(this.mSecondPlayerLayout);
                showView(this.mPlayer2Cards);
                setUpPlayerDetails(this.mSecondPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mSecondPlayerLayout);
                return;
            case 3:
                showView(this.mThirdPlayerLayout);
                showView(this.mPlayer3Cards);
                setUpPlayerDetails(this.mThirdPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mThirdPlayerLayout);
                return;
            case 4:
                showView(this.mFourthPlayerLayout);
                showView(this.mPlayer4Cards);
                setUpPlayerDetails(this.mFourthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mFourthPlayerLayout);
                return;
            case 5:
                showView(this.mFifthPlayerLayout);
                showView(this.mPlayer5Cards);
                setUpPlayerDetails(this.mFifthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mFifthPlayerLayout);
                return;
            case 6:
                showView(this.mSixthPlayerLayout);
                showView(this.mPlayer6Cards);
                setUpPlayerDetails(this.mSixthPlayerLayout, player, this.mDealerId, isPlayerJoined);
                mPlayerBoxesForRanks.put(player.getNick_name(), this.mSixthPlayerLayout);
                return;
            default:
                return;
        }
    }

    public boolean isOpponentValidShow() {
        return this.opponentValidShow;
    }

    @Subscribe
    public void onMessageEvent(EngineRequest engineRequest) {
        try {
            if (!engineRequest.getTableId().equalsIgnoreCase(this.tableId)) {
                if (engineRequest.getCommand().equalsIgnoreCase("request_join_table")) {
                    TablesFragment.this.resetAllPlayers();
                    TablesFragment.this.resetDealer();
                    this.mRummyView.removeViews();
                    this.mRummyView.invalidate();
                    TablesFragment.this.removeGameResultFragment();
                    TablesFragment.this.removeMeldCardsFragment();
                    this.mSelectedCards.clear();
                    this.playerCards.clear();
                    this.mMeldGroupList.clear();

                    clearData();
                    ((RummyApplication) TablesFragment.this.mContext.getApplication()).setJoinedTableIds(engineRequest.getTableId());
                    ((TableActivity) TablesFragment.this.mContext).updateFragment(TablesFragment.this.tableId, engineRequest.getTableId(), "tournament");

                }
            } else {
                if (engineRequest.command.equalsIgnoreCase("meld")) {
                    ((TableActivity) this.mContext).closeSettingsMenu();
                    String successUserId = engineRequest.getSucessUserId();
                    String successUserName = engineRequest.getSucessUserName();
                    if (successUserId != null && successUserName != null) {
                        if (successUserId.equalsIgnoreCase(this.userData.getUserId())) {
                            showView(this.mClosedCard);
                            setUserOptionsOnValidShow();
                            return;
                        }
                        showView(this.mClosedCard);
                        this.meldTimeOut = engineRequest.getTimeout();
                        this.opponentValidShow = true;
                        SoundPoolManager.getInstance().playSound(R.raw.meld);
                        ((TableActivity) this.mContext).dismissScoreBoard();
                        removeGameResultFragment();
                        this.canLeaveTable = false;
                        this.isPlacedShow = true;
                        this.isTossEventRunning = false;
                        this.isCardsDistributing = false;
                        this.meldMsgUdid = engineRequest.getMsg_uuid();
                        startMeldTimer(Integer.parseInt(Utils.formatString(engineRequest.getTimeout())), String.format("%s placed valid show , %s", new Object[]{successUserName, this.mContext.getString(R.string.meld_success_msg)}), this.mGameShecduleTv);
                        if (getTotalCards() > 0) {
                            invisibleView(this.mShowBtn);
                            disableView(this.mShowBtn);
                            showView(this.mDeclareBtn);
                            enableView(this.mDeclareBtn);
                            if (!((TableActivity) this.mContext).isIamBackShowing()) {
                                showDeclareHelpView();
                                return;
                            }
                            return;
                        }
                        //invisibleView(this.mDeclareBtn);      // Before
                        showView(this.mDeclareBtn);             // After
                        showView(this.mShowBtn);
                        disableView(this.mShowBtn);
                        disableView(this.mDropPlayer);
                        disableView(this.sortCards);
                    }
                } else if (engineRequest.command.equalsIgnoreCase("wrong_meld_correction")) {
                    this.isCardsDistributing = false;
                    this.isTossEventRunning = false;
                    this.canLeaveTable = false;
                    this.isUserPlacedValidShow = true;
                    showView(this.mSubFragment);
                    hideView(this.mGameResultsView);
                    hideView(this.mMeldCardsView);
                    showView(this.mSmartCorrectionView);
                    this.isSmartCorrectionShowing = true;
                    setSmartCorrectionView(engineRequest);
                    startWrongMeldTimer(Integer.parseInt(Utils.formatString(engineRequest.getTimeout())), "Please send your cards: ");
                } else if (engineRequest.command.equalsIgnoreCase("rejoin")) {
                    showView(getGameResultsMessageView());
                    hideWinnerView(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView);
                    showRejoinTablePopUp("Sorry, you are eliminated from this table. Do you want to rejoin?", engineRequest.getMsg_uuid());
                    startRejoinTimer(Integer.parseInt(Utils.formatString(engineRequest.getTimeout())));
                } else if (engineRequest.command.equalsIgnoreCase("split")) {
                    cancelTimer(this.mGameScheduleTimer);
                    hideView(getGameResultsMessageView());
                    hideWinnerView(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView);
                    String message = "Player " + engineRequest.getRequester() + " placed a request to Split the prize money. Do you agree to split?";
                    showSplitPopUp(message, engineRequest.getMsg_uuid());
                    startSplitTimer(Integer.parseInt(Utils.formatString(engineRequest.getTimeout())), message, engineRequest.getMsg_uuid());
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        } finally {
        }
    }

    private void setSmartCorrectionView(final EngineRequest request) {
        setSmartCorrectionMeldView(this.mSmartCorrectionView, this.mJockerCard, request);
        ((ImageView) this.mSmartCorrectionView.findViewById(R.id.pop_up_close_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TablesFragment.this.hideView(TablesFragment.this.mWrongMeldTv);
                TablesFragment.this.cancelTimer(TablesFragment.this.mWrongMeldTimer);
                TablesFragment.this.sendSmartCorrectionAcceptRequest(request.getMsg_uuid(), false, request.getGameId());
                TablesFragment.this.isSmartCorrectionShowing = false;
                TablesFragment.this.hideView(TablesFragment.this.mSmartCorrectionView);
            }
        });
        ((Button) this.mSmartCorrectionView.findViewById(R.id.no_thanks_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TablesFragment.this.hideView(TablesFragment.this.mWrongMeldTv);
                TablesFragment.this.cancelTimer(TablesFragment.this.mWrongMeldTimer);
                TablesFragment.this.isSmartCorrectionShowing = false;
                TablesFragment.this.hideView(TablesFragment.this.mSmartCorrectionView);
                TablesFragment.this.sendSmartCorrectionAcceptRequest(request.getMsg_uuid(), false, request.getGameId());
            }
        });
        ((Button) this.mSmartCorrectionView.findViewById(R.id.sc_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TablesFragment.this.hideView(TablesFragment.this.mWrongMeldTv);
                TablesFragment.this.cancelTimer(TablesFragment.this.mWrongMeldTimer);
                TablesFragment.this.isSmartCorrectionShowing = false;
                TablesFragment.this.hideView(TablesFragment.this.mSmartCorrectionView);
                TablesFragment.this.sendSmartCorrectionAcceptRequest(request.getMsg_uuid(), true, request.getGameId());
            }
        });
    }

    private void startRejoinTimer(int scheduleTime) {
        try {
            cancelTimer(this.mRejoinTimer);
            this.mRejoinTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
                public void onTick(long millisUntilFinished) {
                    TextView textView = (TextView) TablesFragment.this.searchGameView.findViewById(R.id.dialog_msg_tv);
                    textView.setText("Sorry, you are eliminated from this table.Do you want to rejoin ? time left [ " + (millisUntilFinished / 1000) + " ] seconds");
                    textView.setTextSize(14.0f);
                }

                public void onFinish() {
                    TablesFragment.this.leaveTable();
                }
            }.start();
        } catch (Exception e) {
            TLog.e(TAG, "Exception in startRejoinTimer :: " + e.getMessage());
        }
    }

    private void startSplitTimer(int scheduleTime, String messageStr, String megUuid) {
        try {
            cancelTimer(this.mRejoinTimer);
            final String str = messageStr;
            final String str2 = megUuid;
            this.mRejoinTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
                public void onTick(long millisUntilFinished) {
                    TextView textView = (TextView) TablesFragment.this.searchGameView.findViewById(R.id.dialog_msg_tv);
                    textView.setText(str + " Please respond in " + (millisUntilFinished / 1000) + " seconds");
                    textView.setTextSize(14.0f);
                }

                public void onFinish() {
                    TablesFragment.this.sendSplitAcceptRequest(str2, false);
                    TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                    TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                }
            }.start();
        } catch (Exception e) {
            TLog.e(TAG, "Exception in startRejoinTimer :: " + e.getMessage());
        }
    }

    private void sendSmartCorrectionAcceptRequest(String msg_uuid, boolean isAccepted, String gameId) {
        SmartCorrectionRequest request = new SmartCorrectionRequest();
        request.setType("+OK");
        request.setText("200");
        request.setGameId(gameId);
        request.setAgree(isAccepted ? "1" : "0");
        request.setUuid(msg_uuid);
        request.setTableId(this.tableId);
        request.setUserId(this.userData.getUserId());
        request.setNickName(this.userData.getNickName());
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), null);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void sendSplitAcceptRequest(String msg_uuid, boolean isAccepted) {
        RejoinRequest request = new RejoinRequest();
        request.setType(isAccepted ? "+OK" : "-ERR");
        request.setUuid(msg_uuid);
        request.setTableId(this.tableId);
        request.setUserId(this.userData.getUserId());
        request.setNickName(this.userData.getNickName());
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), null);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void sendRejoinRequest(String msg_uuid) {
        RejoinRequest request = new RejoinRequest();
        request.setType("+OK");
        request.setUuid(msg_uuid);
        request.setTableId(this.tableId);
        request.setUserId(this.userData.getUserId());
        request.setNickName(this.userData.getNickName());
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.rejoinListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void setUserOptionsOnValidShow() {
        showView(this.mShowBtn);
        invisibleView(this.mShowBtn);
        invisibleView(this.mDeclareBtn);
        disableView(this.mShowBtn);
        dismissToolTipView();
        disableView(this.mDropPlayer);
        disableView(this.sortCards);
        this.canLeaveTable = false;
        this.isCardsDistributing = false;
        this.isTossEventRunning = false;
        this.isUserPlacedValidShow = true;
    }

    private void startMelding() {
        Log.d(TAG, "Inside startMelding************************************");
        this.mIsMelding = true;
        launchMeldFragment();
    }

    @Subscribe
    public void onMessageEvent(Event event) {

        try {
            if (event.getTableId() != null && event.getTableId().equalsIgnoreCase(this.tableId)) {
                dismissDialog();
                String eventName = event.getEventName();
                //            Log.e("eventName",eventName);
                if (eventName.equalsIgnoreCase("PLAYER_JOIN")) {
                    handlePlayerJoinEvent(event);
                } else if (!eventName.equalsIgnoreCase("get_table_details")) {
                    Iterator<GamePlayer> r3;
                    if (eventName.equalsIgnoreCase("PLAYER_QUIT")) {
                        if (event.getUserId() != Integer.parseInt(this.userData.getUserId())) {
                            showView(this.mGameShecduleTv);
                            this.mGameShecduleTv.setText(String.format("%s %s", new Object[]{event.getNickName(), "left the table"}));
                        }
                        handlePlayerQuitEvent(event);
                        if (this.isGameStarted) {
                            String joinedAs = event.getTableJoinAs();
                            if (joinedAs != null) {
                                if (joinedAs.equalsIgnoreCase("play")) {
                                    handlePlayerDrop(event.getUserId());
                                }
                            }
                        }
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        String dateFormat = sdf.format(currentTime);
                        PrefManagerTracker.saveString(getContext(), "playerquit", dateFormat + "");
                        alTrackList.add("playerquit");
                        Log.e("gameend", dateFormat + "");
                        getTrackSharedPrefs();
                    } else if (eventName.equalsIgnoreCase("GAME_SCHEDULE")) {
                        //                    Log.e("TwoTables","GAME_SCHEDULE");
                        //                    Log.e("TwoTables",mTableDetails.getTableType()+"");
                        if (this.mTableDetails.getTableType().equalsIgnoreCase(Utils.PR_JOKER))
                            checkRebuyIn();

                        hideView(this.mReshuffleView);
                        sendTurnUpdateMessage(false);
                        resetIamBackScreen();
                        this.canLeaveTable = true;
                        this.isGameDescheduled = false;
                        dismissQuickMenu();
                        resetDealer();
                        handleGameScheduleEvent(event);
                    } else if (eventName.equalsIgnoreCase("GAME_DESCHEDULE")) {
                        String reason = event.getReason();
                        if (reason != null) {
                            if (!reason.contains("Split")) {
                                sendTurnUpdateMessage(false);
                                this.isGameDescheduled = true;
                                clearOtherPlayersData();
                                resetDealer();
                                clearData();
                                showHideView(true, this.mGameShecduleTv, false);
                                this.mGameShecduleTv.setText(reason);
                                ((TableActivity) this.mContext).closeSettingsMenu();
                                removeGameResultFragment();
                                return;
                            }
                        }
                        this.mGameShecduleTv.setText(reason);
                        if (this.isSplitRequested) {
                            this.mGameShecduleTv.setText(getString(R.string.split_request_messsage));
                            this.isSplitRequested = false;
                        }
                        showHideView(true, this.mGameShecduleTv, false);
                        cancelTimer(this.meldTimer);
                        cancelTimer(this.playerTurnOutTimer);
                    } else if (eventName.equalsIgnoreCase("TABLE_TOSS")) {
                        this.isGameDescheduled = false;
                        this.isTossEventRunning = true;
                        this.canLeaveTable = false;
                        handleTossEvent(event);
                    } else if (eventName.equalsIgnoreCase("SITTING_SEQ")) {
                        hideView(this.mDialogLayout);
                        hideView(this.splitRejectedView);
                        this.isGameDescheduled = false;
                        resetAllPlayers();
                        showView(this.mGameDeckLayout);
                        hideView(this.mGameLogoIv);
                        hideView(this.mReshuffleView);
                        resetDealer();
                        setDealerId(event.getDealerId());
                        this.canLeaveTable = false;
                        handleSittingSeqEvent(event);
                        SoundPoolManager.getInstance().playSound(R.raw.card_distribute);
                    } else if (eventName.equalsIgnoreCase("START_GAME")) {

                        this.isGameStarted = true;
                        this.isGameDescheduled = false;
                        ((TableActivity) this.mContext).updateGameId(event.getTableId(), event.getGameId());
                        this.mGameId = event.getGameId();
                        this.gameid_tourney_tv.setText(this.mGameId);
                        Crashlytics.setString("game_id", mGameId);

                        showView(this.mGameDeckLayout);
                        hideView(this.mGameLogoIv);
                        showHideView(false, this.mGameShecduleTv, false);
                        this.mPrizeMoney.setText(event.getPrizeMoney());
                        dismissDialog(this.mLeaveDialog);
                        this.game_id_tb.setText("#" + event.getGameId());
                        this.isCardPicked = false;

                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        String dateFormat = sdf.format(currentTime);
                        PrefManagerTracker.saveString(getContext(), "startgame", dateFormat + "");
                        alTrackList.add("startgame");
                        Log.e("startgame", dateFormat + "");
                        //         PrefManagerTracker.createSharedPreferencesTracker(getContext());
                        PrefManagerTracker.saveString(getContext(), "userid", userData.getUserId() + "");
                        Log.e("userid", userData.getUserId() + "");
                        String tableIdTrack = mTableId.getText() + "";
                        tableIdTrack = tableIdTrack.replace("#", "");
                        PrefManagerTracker.saveString(getContext(), "tableid", tableIdTrack + "");
                        Log.e("tableId", tableIdTrack + "");
                        Crashlytics.setString("chips_type", tableIdTrack);
                        String[] gameIdTrack = mGameId.split("-");
                        PrefManagerTracker.saveString(getContext(), "gameid", gameIdTrack[0] + "");
                        Log.e("gameid", this.mGameId + "");
                        Crashlytics.setString("chips_type", gameIdTrack[0]);
                        String[] subGameId = mGameId.split("-");
                        if (subGameId.length > 1) {
                            PrefManagerTracker.saveString(getContext(), "subgameid", subGameId[1] + "");
                            Log.e("subGameId", subGameId[1] + "");
                            Crashlytics.setString("chips_type", subGameId[1]);
                        } else {
                            PrefManagerTracker.saveString(getContext(), "subgameid", 0 + "");
                            Log.e("subGameId", 0 + "");
                            Crashlytics.setString("chips_type", 0 + "");
                        }


                    } else if (eventName.equalsIgnoreCase("autoplaystatus")) {
                        if (!this.isGameDescheduled) {
                            updateUserOnAutoPlay(event);
                        }
                        this.isGameDescheduled = false;
                    } else if (eventName.equalsIgnoreCase("CARD_DISCARD")) {
                        handleCardDisCardEvent(event);
                        //                    Crashlytics.getInstance().crash();
                    } else if (eventName.equalsIgnoreCase("TURN_UPDATE") || eventName.equalsIgnoreCase("TURN_EXTRATIME_RECONNECT")) {
                        Stack stack = event.getTurnUpdateStack();
                        if (stack != null) {
                            this.faceDownCardList.clear();
                            this.faceDownCardList.addAll(stack.getFaceDownStack());
                        }
                        if (this.playerUserId == event.getUserId()) {
                            String autoPlayCountStr = event.getAutoPlayCount();
                            if (autoPlayCountStr != null && Integer.parseInt(autoPlayCountStr) < Integer.parseInt(this.autoPlayCount)) {
                                return;
                            }
                        }
                        if (event.getAutoPlayCount() != null) {
                            this.autoPlayCount = event.getAutoPlayCount();
                            this.playerUserId = event.getUserId();
                        } else {
                            this.autoPlayCount = "-1";
                            this.playerUserId = -1;
                        }
                        this.turnCount++;
                        this.canLeaveTable = true;
                        this.isTossEventRunning = false;
                        this.isCardsDistributing = false;
                        this.userData = getUserData();
                        if (event.getUserId() == Integer.parseInt(this.userData.getUserId())) {
                            Log.e("TURN_UPDATE", "My TURN_UPDATE");
                            dismissDialog(this.mLeaveDialog);
                            sendTurnUpdateMessage(true);
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            removeGameResultFragment();
                            SoundPoolManager.getInstance().playSound(R.raw.bell);
                            VibrationManager.getInstance().vibrate(1);

                            for (int i=0; i<alAutoDrop.size();i++){
                                String strTableId = alAutoDrop.get(i);
                                int intIndex = alAutoDrop.indexOf(strTableId);
                                if(alAutoDropBoolean.get(intIndex)){
                                    autoDropPlayer(alAutoDrop.get(i)+"");
                                }else{
                                    goneView(mAutoDropPlayer);
                                }
                            }

                        } else {
                            sendTurnUpdateMessage(false);
                        }
                        showView(this.mGameDeckLayout);
                        hideView(this.mGameLogoIv);
                        this.autoExtraTime = false;
                        handleTurnUpdateEvent(event);
                    } else if (eventName.equalsIgnoreCase("TURN_EXTRATIME")) {
                        handleTurnExtraTimeEvent(event);
                    } else if (eventName.equalsIgnoreCase("SEND_DEAL")) {
                        Log.d(TAG, "TABLE ID        : " + this.tableId);
                        Log.d(TAG, "TABLE ID IN DEAL: " + event.getTableId());
                        Log.d(TAG, "CARDS SIZE      : " + event.getPlayingCards().size());

                        Log.d(TAG, "Inside SEND_DEAL *********************************************************************");
                        this.isYourTurn = false;
                        hideView(this.mReshuffleView);
                        ((TableActivity) this.mContext).hideNavigationMenu();
                        this.isTossEventRunning = false;
                        this.isCardsDistributing = true;
                        enableView(this.sortCards);
                        showView(this.mGameDeckLayout);

                        if (this.canShowCardDistributeAnimation) {
                            animateCards(0, event);
                        } else {
                            handleSendDealEvent(event);
                            this.canShowCardDistributeAnimation = true;
                        }

                        hideView(this.mGameLogoIv);
                        r3 = this.mJoinedPlayersList.iterator();
                        while (r3.hasNext()) {
                            setUpPlayerCardsUI((GamePlayer) r3.next());
                        }
                    } else if (eventName.equalsIgnoreCase("SEND_STACK")) {
                        ((TableActivity) this.mContext).hideNavigationMenu();
                        this.isTossEventRunning = false;
                        this.isCardsDistributing = true;
                        disableView(this.sortCards);
                        showView(this.mGameDeckLayout);
                        hideView(this.mGameLogoIv);
                        handleStackEvent(event);
                    } else if (eventName.equalsIgnoreCase("CARD_PICK")) {
                        this.isCardPicked = true;
                        handleCardPickEvent(event);
                        //                    Crashlytics.getInstance().crash();
                    } else if (eventName.equalsIgnoreCase("GAME_END")) {
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        String dateFormat = sdf.format(currentTime);
                        PrefManagerTracker.saveString(getContext(), "gameend", dateFormat + "");
                        alTrackList.add("gameend");
                        Log.e("gameend", dateFormat + "");
                        updateAutoPlayOnGameEnd();
                        resetIamBackScreen();
                        dismissToolTipView();
                        clearData();
                        getTrackSharedPrefs();
                    } else if (eventName.equalsIgnoreCase("STACK_RESUFFLE")) {
                        ((TableActivity) this.mContext).closeSettingsMenu();
                        clearStacks();
                        refreshStacks(event);
                    } else if (eventName.equalsIgnoreCase("meld_fail")) {
                        updateOpenDeckOnMeldFail(event);
                        cancelTimer(this.mGameScheduleTimer);
                        if (this.meldTimer != null) {
                            this.meldTimer.cancel();
                            this.meldTimer = null;
                            showHideView(false, this.mGameShecduleTv, false);
                        }
                        showView(this.mGameShecduleTv);
                        String playerName = event.getNickName();
                        if (Integer.parseInt(this.userData.getUserId()) == event.getUserId()) {
                            playerName = "You";
                            disbaleDeckCards();
                        } else {
                            enableDeckCards();
                        }
                        this.mGameShecduleTv.setText(String.format("%s %s", new Object[]{playerName, "placed the invalid show"}));
                        handlePlayerDrop(event.getUserId());
                        hideView(this.mClosedCard);
                    } else if (eventName.equalsIgnoreCase("meld_sucess")) {
                        hideView(this.mReshuffleView);
                        cancelTimer(this.meldTimer);
                        invisibleView(this.mGameShecduleTv);
                    } else if (eventName.equalsIgnoreCase("PRE_GAME_RESULT")) {
                        this.isSmartCorrectionShowing = false;
                        hideView(this.mReshuffleView);
                        clearAnimationData();
                        clearSelectedCards();
                        this.mRummyView.removeViews();
                        this.mRummyView.invalidate();
                        showView(this.mGameShecduleTv);
                        hideView(this.mDeclareBtn);
                        disableView(this.sortCards);
                        disableUserOptions();
                        ArrayList<GamePlayer> mPlayersList = (ArrayList) event.getPlayer();
                        if (mPlayersList != null && mPlayersList.size() > 0) {
                            r3 = mPlayersList.iterator();
                            GamePlayer player;
                            while (r3.hasNext()) {
                                player = (GamePlayer) r3.next();
                                if (player != null && player.getMeldList() == null) {
                                    this.userNotDeclaredCards = true;
                                    break;
                                }
                            }
                        }
                        sendTurnUpdateMessage(false);
                        ((TableActivity) this.mContext).closeSettingsMenu();
                        launchGameResultsFragment(event);
                    } else if (eventName.equalsIgnoreCase("SHOW")) {
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        String dateFormat = sdf.format(currentTime);
                        PrefManagerTracker.saveString(getContext(), "show", dateFormat + "");
                        alTrackList.add("show");
                        Log.e("show", dateFormat + "");
                        Log.d(TAG, "Inside SHOW************************************");
                        hideView(this.mReshuffleView);
                        sendTurnUpdateMessage(false);
                        ((TableActivity) this.mContext).closeSettingsMenu();
                        if (this.strIsTourneyTable.equalsIgnoreCase("yes") && this.playerCards.size() == 0)
                            clearData();
                        else
                            handleShowEvent(event);
                    } else if (eventName.equalsIgnoreCase("BALANCE_UPDATE")) {
                        Log.e("BALANCE_UPDATE", "BALANCE_UPDATE");
                        this.mTableDetails.getFun_chips();
                        Log.e("getFun_chips", this.mTableDetails.getFun_chips() + "@3010");

                    } else if (eventName.equalsIgnoreCase("GAME_RESULT")) {

                        /*if (this.mTableDetails.getTableType().equalsIgnoreCase(Utils.PR_JOKER))
                            checkRebuyIn();*/

                        this.isSmartCorrectionShowing = false;
                        hideView(this.mReshuffleView);
                        this.userNotDeclaredCards = false;
                        sendTurnUpdateMessage(false);
                        ((TableActivity) this.mContext).closeSettingsMenu();
                        handleGameResultsEvent(event);
                        ((TableActivity) this.mContext).updateScoreBoard(this.tableId, event);
                    } else if (eventName.equalsIgnoreCase("TABLE_CLOSED")) {
                        hideView(this.mReshuffleView);
                        ((TableActivity) this.mContext).closeSettingsMenu();
                        resetIamBackScreen();
                        this.isGameResultsShowing = true;
                        invisibleView(this.mUserPlayerLayout);
                        if (!this.isWinnerEventExecuted) {
                            showMaxPointsPopUp(event);
                            this.isWinnerEventExecuted = false;
                        }
                        handleTableCloseEvent();
                        setTableButtonsUI();
                    } else if (!eventName.equalsIgnoreCase("PLAYER_ELIMINATE")) {
                        if (eventName.equalsIgnoreCase("rejoin")) {
                            String score = event.getRejoinScore();
                            int userId = event.getUserId();
                            GamePlayer rejoinedPlayer = null;
                            r3 = this.mJoinedPlayersList.iterator();
                            GamePlayer player;
                            while (r3.hasNext()) {
                                player = (GamePlayer) r3.next();
                                if (player.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                                    player.setTotalScore(score);
                                    rejoinedPlayer = player;
                                    break;
                                }
                            }
                            if (rejoinedPlayer != null) {
                                setPointsUI(userId, rejoinedPlayer);
                            }
                        } else if (eventName.equalsIgnoreCase("PLAYER_DROP")) {
                            Date currentTime = Calendar.getInstance().getTime();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                            String dateFormat = sdf.format(currentTime);
                            PrefManagerTracker.saveString(getContext(), "drop", dateFormat + "");
                            alTrackList.add("drop");
                            Log.e("drop", dateFormat + "");
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            removeGameResultFragment();
                            if (event.getUserId() != Integer.parseInt(this.userData.getUserId())) {
                                this.mGameShecduleTv.setText(String.format("%s %s", new Object[]{event.getNickName(), "dropped"}));
                                showView(this.mGameShecduleTv);
                            } else {
                                dismissQuickMenu();
                                SoundPoolManager.getInstance().playSound(R.raw.drop);
                            }
                            handlePlayerDrop(event.getUserId());
                            if (this.strIsTourneyTable.equalsIgnoreCase("yes"))
                                this.removePlayerLevelFromBox(event.getNickName());
                        } else if (eventName.equalsIgnoreCase("TURN_TIMEOUT")) {
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            removeGameResultFragment();
                            handlePlayerDrop(event.getUserId());
                        } else if (eventName.contains("_WINNER")) {
                            hideView(this.mReshuffleView);
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            resetTimerInfo(this.mGameResultsView);
                            this.isGameResultsShowing = true;
                            this.isWinnerEventExecuted = true;
                            setTableButtonsUI();
                            SoundPoolManager.getInstance().playSound(R.raw.winners);
                            VibrationManager.getInstance().vibrate(1);
                            List<GamePlayer> gamePlayers = new ArrayList();
                            GamePlayer player = new GamePlayer();
                            player.setAmount(event.getPrizeMoney());
                            player.setNick_name(event.getWinnerNickName());
                            gamePlayers.add(player);
                            event.setPlayer(gamePlayers);
                            showWinnerFragment(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView, event, this.mTableDetails);
                            String joinAnotherGameMsg = "Congratulations , you won the game\nDo you want to join 1 more game?";
                            this.winnerId = event.getWinnerId();
                            if (this.winnerId == null || !this.winnerId.equalsIgnoreCase(this.userData.getUserId())) {
                                joinAnotherGame(event, "You have reached maximum number of points, would you like to play another game?");
                            } else {
                                joinAnotherGame(event, joinAnotherGameMsg);
                            }
                        } else if (eventName.equalsIgnoreCase("VALID_SHOW")) {
                            hideView(this.mReshuffleView);
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            removeGameResultFragment();
                            this.canLeaveTable = false;
                            this.isTossEventRunning = false;
                            this.isCardsDistributing = false;
                            String successUserName = event.getNickName();
                            if (event.getUserId() == Integer.parseInt(this.userData.getUserId())) {
                                showView(this.mClosedCard);
                                setUserOptionsOnValidShow();
                                showView(this.mGameShecduleTv);
                                startGameScheduleTimer(Integer.parseInt(Utils.formatString(this.mTableDetails.getMeldTimeout())), true);
                            } else {
                                showView(this.mClosedCard);
                                this.isPlacedShow = true;
                                String message = String.format("%s placed valid show , %s", new Object[]{successUserName, this.mContext.getString(R.string.meld_success_msg)});
                                String timeOut = this.mTableDetails.getMeldTimeout();
                                if (this.meldTimeOut != null && this.meldTimeOut.length() > 0) {
                                    timeOut = this.meldTimeOut;
                                }
                                startMeldTimer(Integer.parseInt(Utils.formatString(timeOut)), message, this.mGameShecduleTv);
                            }
                            if (getTotalCards() > 0) {
                                invisibleView(this.mShowBtn);
                                disableView(this.mShowBtn);
                                showView(this.mDeclareBtn);
                                enableView(this.mDeclareBtn);
                                if (!((TableActivity) this.mContext).isIamBackShowing()) {
                                    showDeclareHelpView();
                                    return;
                                }
                                return;
                            }
                            invisibleView(this.mDeclareBtn);
                            invisibleView(this.mShowBtn);
                            invisibleView(this.mShowBtn);
                            invisibleView(this.mDropPlayer);
                            invisibleView(this.sortCards);
                        } else if (eventName.equalsIgnoreCase("SEND_SLOTS")) {
                            this.slotCards = new ArrayList();
                            this.slotCards.addAll(event.getTableCards().getCards());
                            setCardsOnIamBack(this.slotCards);
                        } else if (eventName.equalsIgnoreCase("SPLIT_STATUS")) {
                            if (event.getSplit().equalsIgnoreCase("True")) {
                                hideWinnerView(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView);
                                showSplitRequestPopUp(getString(R.string.split_request_pop_up_msg));
                            }
                        } else if (eventName.equalsIgnoreCase("SPLIT_RESULT")) {
                            hideView(this.mReshuffleView);
                            ((TableActivity) this.mContext).closeSettingsMenu();
                            resetTimerInfo(this.mGameResultsView);
                            this.isGameResultsShowing = true;
                            this.isWinnerEventExecuted = true;
                            setTableButtonsUI();
                            SoundPoolManager.getInstance().playSound(R.raw.winners);
                            VibrationManager.getInstance().vibrate(1);
                            ArrayList arrayList = new ArrayList();
                            event.setPlayer(event.getSplitter().getPlayer());
                            showWinnerFragment(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView, event, this.mTableDetails);
                            joinAnotherGame(event, getString(R.string.winner_congrats_pop_up_msg));
                        } else if (eventName.equalsIgnoreCase("SPLIT_FALSE")) {
                            String split = event.getSplit();
                            boolean isNameFound = false;
                            for (String equalsIgnoreCase : split.split(",")) {
                                if (equalsIgnoreCase.equalsIgnoreCase(this.userData.getNickName())) {
                                    isNameFound = true;
                                    break;
                                }
                            }
                            if (event.getSplit() != null && split.length() > 0 && !isNameFound) {
                                removeGameResultFragment();
                                showView(this.mDialogLayout);
                                invisibleView(this.winnerView);
                                invisibleView(this.searchGameView);
                                View splitRejectedView = this.mDialogLayout.findViewById(R.id.split_rjected_view);
                                showView(splitRejectedView);
                                ((TextView) splitRejectedView.findViewById(R.id.dialog_msg_tv)).setText("Player " + event.getSplit() + " has rejected your split request . You cannot split the prize money in this game.");
                                View view = splitRejectedView;
                                final View finalView = view;
                                ((Button) splitRejectedView.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        TablesFragment.this.hideView(finalView);
                                        TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                                    }
                                });
                                view = splitRejectedView;
                                final View finalView1 = view;
                                ((ImageView) splitRejectedView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        TablesFragment.this.hideView(finalView1);
                                        TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                                    }
                                });
                            }
                        }
                    } else if (eventName.equalsIgnoreCase("PLAYER_ELIMINATE") && this.strIsTourneyTable.equalsIgnoreCase("yes")
                            && event.getNickName().equalsIgnoreCase(this.userData.getNickName())) {
                        Date currentTime = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        String dateFormat = sdf.format(currentTime);
                        PrefManagerTracker.saveString(getContext(), "eliminated", dateFormat + "");
                        alTrackList.add("eliminated");
                        Log.e("eliminated", dateFormat);
                        Log.d(TAG, "Eliminating player ---------------------------------------------------------");
                        clearSelectedCards();
                        clearStacks();
                        clearAnimationData();
                        removeMeldCardsFragment();
                        this.playerCards.clear();
                        this.mRummyView.removeViews();
                        this.mRummyView.invalidate();
                        this.mRummyView.setVisibility(View.INVISIBLE);
                        isCardsDistributing = false;
                        clearData();
                    }
                }
            } else if (event.getEventName().equalsIgnoreCase("players_rank")) {
                this.mTourneyId = event.getTournamentId();
                this.mPlayersRank = event;
                this.mPlayersList = event.getPlayers();
                this.updatePlayersRank();
            } else if (event.getEventName().equalsIgnoreCase("end_tournament")) {
                if (event.getTournamentId().equalsIgnoreCase(this.mTourneyId)) {
                    clearStacks();
                    clearData();
                    clearSelectedCards();
                    this.levelTimerLayout.setVisibility(View.GONE);

                    if (this.levelTimer != null)
                        this.levelTimer.cancel();

                    this.canLeaveTable = true;
                    this.isTourneyEnd = true;
                    this.showGenericDialogWithMessage("This tournament has ended !", "end_tournament");
                }
            } else if (event.getEventName().equalsIgnoreCase("level_start")) {
                getLevelTimer();
            } else if (event.getEventName().equalsIgnoreCase("level_end")) {
                getLevelTimer();
            } else if (event.getEventName().equalsIgnoreCase("tournament_result")) {
                List<GamePlayer> tourneyResults = event.getPlayers();
                displayTourneyResults(tourneyResults);
            } else if (event.getEventName().equalsIgnoreCase("disqualified")) {
                if (event.getTournamentId().equalsIgnoreCase(this.mTourneyId) && event.getNickName().equalsIgnoreCase(this.userData.getNickName())) {
                    this.showGenericDialogWithMessage("Sorry! You have been disqualified from this tournament.", "disqualified");
                    clearSelectedCards();
                    clearData();
                    clearStacks();
                    clearAnimationData();
                    this.playerCards.clear();
                    removeMeldCardsFragment();
                }
            } else if (event.getEventName().equalsIgnoreCase("tournament_rebuyin")) {
                if (event.getTournamentId().equalsIgnoreCase(this.mTourneyId) && this.strIsTourneyTable.equalsIgnoreCase("yes"))
                    this.showRebuyinDialog(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OME", e + "");
        }

    }

    private void checkRebuyIn() {
//        Log.e("TwoTables","checkRebuyIn");
//        Log.e("TwoTables",mTableDetails.getTableCost()+"");
        if (this.mTableDetails.getTableCost().equalsIgnoreCase("FUNCHIPS_FUNCHIPS")) {
            Log.e("TwoTables", mTableDetails.getMinimumbuyin() + "@1");
            Log.e("TwoTables", userData.getFunInPlay() + "@2");
            Log.e("TwoTables", Utils.PR_JOKER_POINTS + "@3");
//            Float funChipsCompare = Float.parseFloat(this.userData.getFunChips()) - Float.parseFloat(this.userData.getFunInPlay());
//            if (Float.parseFloat(this.mTableDetails.getMinimumbuyin()) > Float.parseFloat(this.userData.getFunInPlay()))
//            Log.e("TwoTables", userData.getFunChips() + " - " + mTableDetails.getMinimumbuyin());
//            if (Float.parseFloat(this.userData.getFunChips()) >= Float.parseFloat(this.mTableDetails.getMinimumbuyin()))
//            if (Float.parseFloat(this.mTableDetails.getMinimumbuyin()) > Float.parseFloat(this.userData.getFunInPlay()))
            //            if (Float.parseFloat(this.mTableDetails.getMinimumbuyin()) > Float.parseFloat(Utils.PR_JOKER_POINTS))
            if (Float.parseFloat(this.mTableDetails.getMinimumbuyin()) > Float.parseFloat(this.userData.getFunInPlay()))
                showRebuyInPopTimer();
        } else if (this.mTableDetails.getTableCost().equalsIgnoreCase("CASH_CASH")) {
            if (Float.parseFloat(this.mTableDetails.getMinimumbuyin()) > Float.parseFloat(this.userData.getRealInPlay()))
                showRebuyInPopTimer();
        }
    }

    private void showRebuyInPopTimer() {
        Log.e("TwoTables", "showRebuyInPopTimer");
        final Dialog dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rebuyin_timer);
        dialog.setCanceledOnTouchOutside(false);

        final TextView timer_tv = (TextView) dialog.findViewById(R.id.timer_tv);
        final Button yes_btn = (Button) dialog.findViewById(R.id.yes_btn);
        final Button no_btn = (Button) dialog.findViewById(R.id.no_btn);

        final CountDownTimer timer = new CountDownTimer(10 * 1000, 1000) { // timer for 10 seconds
            public void onTick(long millisUntilFinished) {
                timer_tv.setText("Time remaining: " + (millisUntilFinished / 1000) + " sec");
                timeLeft = Math.round(millisUntilFinished / 1000);
            }

            public void onFinish() {
                try {
                    dialog.dismiss();
                } catch (Exception e) {

                }
                TablesFragment.this.leaveTable();
            }
        }.start();

        no_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                TablesFragment.this.leaveTable();
            }
        });

        yes_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TablesFragment.this.rebuyChips();
                TablesFragment.this.showBuyInPopUpSlider(TablesFragment.this.mTableDetails);
                timer.cancel();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void hideTable() {
        if (TablesFragment.this.mDialogLayout.getVisibility() == View.VISIBLE) {
            TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
        } else if (TablesFragment.this.mSubFragment.getVisibility() != View.VISIBLE) {
        } else {
            if (!((TableActivity) TablesFragment.this.mContext).isIamBackShowing()) {
                ((TableActivity) TablesFragment.this.mContext).showGameTablesLayout(TablesFragment.this.tableId);
            }
            TablesFragment.this.hideView(TablesFragment.this.mSubFragment);
        }
    }

    int timeLeft;
    int flagRejoin = 0;

    private void showBuyInPopUpSlider(TableDetails tableDetails) {
        String balance;

        final DecimalFormat format = new DecimalFormat("0.#");
        if (tableDetails.getTableCost().equalsIgnoreCase("CASH_CASH")) {
            balance = this.userData.getRealChips();
        } else {
            balance = this.userData.getFunChips();
        }
        final Dialog dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.table_details_pop_up);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();

        final CountDownTimer timer = new CountDownTimer(timeLeft * 1000, 1000) { // timer for 10 seconds
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (flagRejoin == 0) {
                    try {
                        dialog.dismiss();
                        TablesFragment.this.leaveTable();
                    } catch (Exception e) {
                    }
                } else {
                    hideTable();
                }
//                Toast.makeText(getContext(), "Eliminated", Toast.LENGTH_SHORT).show();
            }
        }.start();

        TextView minBuyTv = (TextView) dialog.findViewById(R.id.min_buy_value_tv);
        TextView maxBuyTv = (TextView) dialog.findViewById(R.id.max_buy_value_tv);
        final TextView balanceTv = (TextView) dialog.findViewById(R.id.balance_value_tv);
        final EditText buyInTv = (EditText) dialog.findViewById(R.id.buy_in_value_tv);
        ((TextView) dialog.findViewById(R.id.bet_value_tv)).setText(tableDetails.getBet());
        minBuyTv.setText(tableDetails.getMinimumbuyin());
        maxBuyTv.setText(tableDetails.getMaximumbuyin());
        final String maximumBuyIn = tableDetails.getMaximumbuyin();
        final int max = Integer.parseInt(maximumBuyIn);
        final int min = Integer.parseInt(tableDetails.getMinimumbuyin());
        boolean decreaseBalance = true;
        if (balance.contains(".")) {
            String subBalance = balance.substring(balance.lastIndexOf(".") + 1);
            if (subBalance != null && subBalance.length() > 0) {
                decreaseBalance = Integer.parseInt(subBalance) > 50;
            }
        }
        final float balanceInt = new Float((float) Math.round(Float.parseFloat(balance))).floatValue();
        balanceTv.setText(String.valueOf(format.format((double) balanceInt)));
        final TableDetails table2 = tableDetails;
        ((Button) dialog.findViewById(R.id.join_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                flagRejoin = 1;
                if (buyInTv.getText() == null || buyInTv.getText().length() <= 0) {
                    CommonMethods.showSnackbar(balanceTv, "Please enter minimum amount");
                    return;
                }
                float selectedBuyInAmt = Float.valueOf(buyInTv.getText().toString()).floatValue();
                if (selectedBuyInAmt <= balanceInt || selectedBuyInAmt >= Float.valueOf((float) max).floatValue()) {
                    if (selectedBuyInAmt > Float.valueOf((float) max).floatValue()) {
                        CommonMethods.showSnackbar(balanceTv, "You can take only ( " + maximumBuyIn + " ) " + "in to the table");
                    } else if (selectedBuyInAmt < Float.valueOf((float) min).floatValue()) {
                        CommonMethods.showSnackbar(balanceTv, "Please enter minimum amount");
                    } else {
                        TablesFragment.this.rebuyChips(buyInTv.getText().toString());
                        dialog.dismiss();
                    }
                } else if (table2.getTableCost().contains("CASH_CASH")) {
                    TablesFragment.this.showErrorBuyChipsDialog(getContext(), "You don't have enough balance to join this table");
                } else {
                    TablesFragment.this.showErrorBuyChipsDialog(getContext(), "You don't have enough balance to join this table");
                }


            }
        });
        ((Button) dialog.findViewById(R.id.cancel_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seek_bar);
        seekBar.setMax((max - min) / 1);
        seekBar.setProgress(seekBar.getMax());
        if (Float.valueOf((float) max).floatValue() <= balanceInt) {
            buyInTv.setText(tableDetails.getMaximumbuyin());
        } else {
            float newBalance = balanceInt;
            if (decreaseBalance) {
                newBalance = balanceInt - 1.0f;
            }
            buyInTv.setText(String.valueOf(format.format((double) newBalance)));
        }
        final int i = min;
        final float f = balanceInt;
        final EditText editText = buyInTv;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String sliderValue = String.valueOf(format.format((double) (i + (progress * 1))));
                if (Float.valueOf(sliderValue).floatValue() >= f) {
                    sliderValue = String.valueOf(format.format((double) f));
                }
                editText.setText(sliderValue);
            }
        });
    }

    private void rebuyChips(String chips) {
        RebuyRequest request = new RebuyRequest();
        request.setCommand("rebuyin");
        request.setUuid(Utils.generateUuid());
        request.setTable_id(this.tableId);
        request.setUser_id(this.userData.getUserId());
        request.setRebuyinamt(chips);

        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.rebuyResponseListener);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "rebuyChips" + gameEngineNotRunning.getLocalizedMessage());
        }
    }


    private void updateOpenDeckOnMeldFail(Event event) {
        String suit = event.getSuit();
        String face = event.getFace();
        PlayingCard meldFailCard = new PlayingCard();
        meldFailCard.setFace(face);
        meldFailCard.setSuit(suit);
        setOpenCard(meldFailCard);
        this.faceUpCardList.add(meldFailCard);
    }

    private LoginResponse getUserData() {
        if (this.userData == null) {
            RummyApplication app = (RummyApplication) this.mContext.getApplication();
            if (app != null) {
                this.userData = app.getUserData();
            }
        }
        return this.userData;
    }

    private void resetIamBackScreen() {
        IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment != null) {
            iamBackFragment.clearDiscardedCards();
        }
    }

    public void clearOtherPlayersData() {
        resetAllPlayers();
        ((TableActivity) this.mContext).resetPlayerIconsOnTableBtn(this.tableId);
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                GamePlayer player = gamePlayer;
                Log.d(TAG, "SEATING VIA: clearOtherPlayersData");
                setUpPlayerUI(player, false);
                setPlayerPositionsOnTableBtn(this.mTableDetails, player, false);
                return;
            }
        }
    }

    private void handleCardDisCardEvent(Event event) {
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(event.getUserId()))) {
                player = gamePlayer;
                break;
            }
        }
        if (!(player == null || player.getSeat() == null)) {
            switch (Integer.parseInt(player.getSeat())) {
                case 2:
                    animateDiscardCard(0, this.mSecondPlayerLayout);
                    break;
                case 3:
                    animateDiscardCard(0, this.mThirdPlayerLayout);
                    break;
                case 4:
                    animateDiscardCard(0, this.mFourthPlayerLayout);
                    break;
                case 5:
                    animateDiscardCard(0, this.mFifthPlayerLayout);
                    break;
                case 6:
                    animateDiscardCard(0, this.mSixthPlayerLayout);
                    break;
            }
        }
        PlayingCard discardCard = new PlayingCard();
        discardCard.setFace(event.getFace());
        discardCard.setSuit(event.getSuit());
        setOpenCard(discardCard);
        this.faceUpCardList.add(discardCard);
        ((TableActivity) this.mContext).addDiscardToPlayer(event);
        if (event.getAutoPlay().equalsIgnoreCase("true") && event.getUserId() == Integer.parseInt(this.userData.getUserId())) {
            discardCardOnAutoPlay(discardCard);
            showAutoDiscardedCards(event, discardCard);
        }
    }

    private void showAutoDiscardedCards(Event event, PlayingCard discardCard) {
        IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment != null) {
            iamBackFragment.showAutoPlayCards(discardCard, event);
        }
    }

    private void discardCardOnAutoPlay(PlayingCard discardCard) {
        String discardCardStr = String.format("%s%s", new Object[]{discardCard.getSuit(), discardCard.getFace()});
        String discardSuit = null;
        String disCardFace = null;
        boolean cardRemoved = false;
        for (int i = this.mGroupList.size() - 1; i >= 0; i--) {
            ArrayList<PlayingCard> groupList = (ArrayList) this.mGroupList.get(i);
            for (int j = groupList.size() - 1; j >= 0; j--) {
                PlayingCard card = (PlayingCard) groupList.get(j);
                if (discardCardStr.equalsIgnoreCase(String.format("%s%s", new Object[]{card.getSuit(), card.getFace()}))) {
                    discardSuit = card.getSuit();
                    disCardFace = card.getFace();
                    groupList.remove(card);
                    cardRemoved = true;
                    break;
                }
            }
            if (cardRemoved) {
                break;
            }
        }
        if (!(discardSuit == null || disCardFace == null)) {
            updateDeckCardsOnDiscard(discardSuit, disCardFace);
        }
        setGroupView(false);
    }

    private void updateUserOnTurn(Event event) {
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(event.getUserId()))) {
                String autoPlayCount = event.getAutoPlayCount();
                String autoPlay = event.getAutoPlay();
                String totalCount = event.getTotalCounr();
                if (autoPlayCount != null) {
                    gamePlayer.setAutoplay_count(String.valueOf(Integer.parseInt(autoPlayCount) - 1));
                }
                if (autoPlay == null) {
                    autoPlay = "";
                }
                gamePlayer.setAutoplay(autoPlay);
                if (totalCount == null) {
                    totalCount = "";
                }
                gamePlayer.setTotalCount(totalCount);
                player = gamePlayer;
                if (player != null) {
                    setAutoPlayUI(player);
                }
            }
        }
        if (player != null) {
            setAutoPlayUI(player);
        }
    }

    private void updateAutoPlayOnGameEnd() {
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            String seat = ((GamePlayer) it.next()).getSeat();
            if (seat != null) {
                hideAutoPlayUI(Integer.parseInt(seat));
            }
        }
    }

    private void updateUserOnAutoPlay(Event event) {
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(event.getUserId()))) {
                gamePlayer.setAutoplay(event.getStatus());
                gamePlayer.setAutoplay_count("0");
                gamePlayer.setTotalCount("5");
                player = gamePlayer;
                break;
            }
        }
        if (player != null) {
            setAutoPlayUI(player);
        }
    }

    private void showSplitRequestPopUp(String message) {
        showView(this.searchGameView);
        TextView textView = (TextView) this.searchGameView.findViewById(R.id.dialog_msg_tv);
        textView.setText(message);
        textView.setTextSize(14.0f);
        ((ImageView) this.searchGameView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                TablesFragment.this.requestSplit();
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
            }
        });
    }

    private void showRejoinTablePopUp(String message, final String megUuid) {
        showView(this.searchGameView);
        TextView textView = (TextView) this.searchGameView.findViewById(R.id.dialog_msg_tv);
        textView.setText(message);
        textView.setTextSize(14.0f);
        ((ImageView) this.searchGameView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.leaveTable();
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                TablesFragment.this.sendRejoinRequest(megUuid);
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.leaveTable();
            }
        });
    }

    private void showSplitPopUp(String message, final String megUuid) {
        showView(this.searchGameView);
        TextView textView = (TextView) this.searchGameView.findViewById(R.id.dialog_msg_tv);
        textView.setText(message);
        textView.setTextSize(14.0f);
        ((ImageView) this.searchGameView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.sendSplitAcceptRequest(megUuid, false);
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.removeGameResultFragment();
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                TablesFragment.this.removeGameResultFragment();
                TablesFragment.this.sendSplitAcceptRequest(megUuid, true);
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.sendSplitAcceptRequest(megUuid, false);
                TablesFragment.this.removeGameResultFragment();
                TablesFragment.this.cancelTimer(TablesFragment.this.mRejoinTimer);
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
            }
        });
    }

    private void joinAnotherGame(final Event event, String message) {
        String winnerName = "";
        String joinAnotherGameMsg = "Do you want to join 1 more game?";
        showView(this.searchGameView);
        TextView textView = (TextView) this.searchGameView.findViewById(R.id.dialog_msg_tv);
        textView.setText(message);
        textView.setTextSize(15.0f);
        ((ImageView) this.searchGameView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (TablesFragment.this.winnerView.getVisibility() == View.VISIBLE) {
                    TablesFragment.this.invisibleView(TablesFragment.this.searchGameView);
                    return;
                }
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.removeGameResultFragment();
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                TablesFragment.this.invisibleView(TablesFragment.this.mGameShecduleTv);
                TablesFragment.this.mApplication.refreshTableIds(TablesFragment.this.tableId);
                TablesFragment.this.showView(TablesFragment.this.mUserPlayerLayout);
                TablesFragment.this.searchTable(event);
            }
        });
        ((Button) this.searchGameView.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (TablesFragment.this.winnerView.getVisibility() == View.VISIBLE) {
                    TablesFragment.this.invisibleView(TablesFragment.this.searchGameView);
                    return;
                }
                TablesFragment.this.hideView(TablesFragment.this.searchGameView);
                TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
            }
        });
    }

    private void handleTableCloseEvent() {
        resetTimerInfo(this.mGameResultsView);
        hidePlayerView();
        disableView(this.mDropPlayer);
        disableView(this.mShowBtn);
        disableView(this.mDeclareBtn);
        disableView(this.sortCards);
        disableView(this.mExtraTimeBtn);
        clearData();
        this.mNoOfGamesPlayed = 0;
        showView(this.mGameShecduleTv);
        this.mGameShecduleTv.setText(this.mContext.getString(R.string.table_close_msg));
    }

    private void dropUser() {
        this.canLeaveTable = true;
        dismissQuickMenu();
        invisibleView(this.mUserAutoTimerRootLayout);
        invisibleView(this.mUserAutoChunksLayout);
        hideView(this.mDeclareBtn);
        showView(this.sortCards);
        disableView(this.sortCards);
        disableUserOptions();
        disbaleDeckCards();
        showHideView(false, this.mUserTimerRootLayout, false);
        disbaleDeckCards();
        showDropViewForUser();
        setPlayerUiOnDrop(this.mUserPlayerLayout);
    }

    private void disbaleDeckCards() {
        disableClick(this.mOpenCard);
        disableClick(this.mFaceDownCards);
    }

    private void showDropViewForUser() {
        this.mRummyView.removeViews();
    }

    private void enableDeckCards() {
        enableView(this.mOpenCard);
        enableView(this.mFaceDownCards);
    }

    private void setAutoPlayCountOnTurn(Event event) {
        int userId = event.getUserId();
        String autoPlay = event.getAutoPlay();
        Iterator it;
        GamePlayer player;
        if (autoPlay != null) {
            String autoPlayCount = event.getAutoPlayCount();
            it = this.mJoinedPlayersList.iterator();
            while (it.hasNext()) {
                player = (GamePlayer) it.next();
                if (Integer.parseInt(player.getUser_id()) == userId) {
                    player.setAutoplay(autoPlay);
                    if (autoPlayCount != null) {
                        player.setAutoplay_count(String.valueOf(Integer.parseInt(autoPlayCount) - 1));
                    }
                    GamePlayer gamePlayer = player;
                    return;
                }
            }
            return;
        }
        it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            player = (GamePlayer) it.next();
            if (Integer.parseInt(player.getUser_id()) == userId) {
                player.setAutoplay("false");
                player.setAutoplay_count(null);
                return;
            }
        }
    }

    private void handleTurnUpdateEvent(Event event) {
        int userId = event.getUserId();
        String timeOutString = event.getTimeOut();
        updateUserOnTurn(event);
        if (event.getAutoExtraTime() != null) {
            this.autoExtraTime = true;
            this.mAutoExtraTimeEvent = event;
            disableView(this.mExtraTimeBtn);
        }
        if (!timeOutString.equalsIgnoreCase("0")) {
            handleTurnUpdateEvent(userId, timeOutString);
        }
    }

    private void removeWinnerDialog() {
        List<Fragment> fragments = this.mContext.getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof DialogFragment) {
                    WinnerDialogFragment winnerDialogFragment = (WinnerDialogFragment) fragment;
                    winnerDialogFragment.getDialog().dismiss();
                    getActivity().getSupportFragmentManager().beginTransaction().remove(winnerDialogFragment);
                    return;
                }
            }
        }
    }

    private void handlePlayerJoinEvent(Event event) {

        try {
            if (event.getUserId() != Integer.parseInt(this.userData.getUserId())) {
                GamePlayer joinedPlayer;
                this.mGameShecduleTv.setText(R.string.game_start_msg);
                SoundPoolManager.getInstance().playSound(R.raw.sit);
                VibrationManager.getInstance().vibrate(1);
                GamePlayer player = null;
                boolean isPlayerFound = false;
                Iterator it = this.mJoinedPlayersList.iterator();
                while (it.hasNext()) {
                    joinedPlayer = (GamePlayer) it.next();
                    if (joinedPlayer.getUser_id().equalsIgnoreCase(String.valueOf(event.getUserId()))) {
                        player = joinedPlayer;
                        isPlayerFound = true;
                        break;
                    }
                }
                if (player == null) {
                    player = new GamePlayer();
                }
                player.setNick_name(event.getNickName());
                String availableSeat = event.getSeat();
                if (Integer.parseInt(this.mTableDetails.getMaxPlayer()) == 6 && this.mTableDetails.getTableType().contains(Utils.PR)) {
                    int index = 1;
                    boolean seatAssigned = false;
                    List<Integer> seatNumbers = getJoinedSeats();
                    for (int j = 0; j < seatNumbers.size(); j++) {
                        if (index != ((Integer) seatNumbers.get(j)).intValue()) {
                            availableSeat = String.valueOf(index);
                            seatAssigned = true;
                            break;
                        }
                        index++;
                    }
                    if (!seatAssigned) {
                        availableSeat = String.valueOf(seatNumbers.size() + 1);
                    }
                } else if (Integer.parseInt(this.mTableDetails.getMaxPlayer()) == 2 && this.mTableDetails.getTableType().contains(Utils.PR)) {
                    int userSeat = 0;
                    it = this.mJoinedPlayersList.iterator();
                    while (it.hasNext()) {
                        joinedPlayer = (GamePlayer) it.next();
                        if (joinedPlayer.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                            userSeat = Integer.parseInt(joinedPlayer.getSeat());
                            break;
                        }
                    }
                    if (userSeat == 1) {
                        availableSeat = "4";
                    } else if (userSeat == 4) {
                        availableSeat = "1";
                    }
                }
                player.setSeat(availableSeat);
                String joinedAs = event.getTableJoinAs();
                if (joinedAs != null && joinedAs.equalsIgnoreCase("middle")) {
                    player.setMiddleJoin(true);
                }
                player.setPoints(event.getBuyinamount());
                player.setDEVICE_ID(event.getDeviceId());
                player.setUser_id(String.valueOf(event.getUserId()));
                player.setPlayerlevel(event.getPlayerLevel());
                if (!isPlayerFound) {
                    this.mJoinedPlayersList.add(player);
                    this.mGamePlayerMap.put(player.getUser_id(), player);
                }
                Log.d(TAG, "SEATING VIA: handlePlayerJoinEvent");
                setUpPlayerUI(player, true);
                if (this.mTableDetails != null) {
                    setPlayerPositionsOnTableBtn(this.mTableDetails, player, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e + "");
        }

    }

    public TableDetails getTableInfo() {
        return this.mTableDetails;
    }

    @NonNull
    private List<Integer> getJoinedSeats() {
        List<Integer> seatNumbers = new ArrayList();
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            seatNumbers.add(Integer.valueOf(Integer.parseInt(((GamePlayer) it.next()).getSeat())));
        }
        Collections.sort(seatNumbers);
        return seatNumbers;
    }

    private void handlePlayerQuitEvent(Event event) {
        if (event.getUserId() != Integer.parseInt(this.userData.getUserId())) {
            handlePlayerQuit(event.getUserId());
        } else {
            this.canLeaveTable = true;
            this.mGroupList.clear();
            clearOtherPlayersData();
            clearData();
            this.mGameShecduleTv.setText("You have been eliminated from the game");
            this.mGameShecduleTv.setVisibility(View.VISIBLE);
        }
    }

    private void handleGameScheduleEvent(Event event) {
        disableView(this.mShowBtn);
        disableView(this.mDropPlayer);
        disableView(this.sortCards);
        clearData();
        this.userPlacedShow = false;
        this.mClosedCard.setVisibility(View.INVISIBLE);
        enableDeckCards();
        int gameStartTime = (int) (Double.valueOf(event.getStartTime()).doubleValue() - Double.valueOf(event.getTimestamp()).doubleValue());
        this.mGameShecduleTv.setVisibility(View.VISIBLE);
        this.mGameShecduleTv.setText("Your game starts in " + gameStartTime + " seconds.");
        startGameScheduleTimer(gameStartTime, false);

    }

    private void handleTurnExtraTimeEvent(Event event) {
        this.mGameShecduleTv.setVisibility(View.INVISIBLE);
        String userName = event.getNickName();
        this.mGameShecduleTv.setText(String.format("%s%s%s", new Object[]{"Player ", userName, " chosen extra time"}));
        this.mGameShecduleTv.setVisibility(View.VISIBLE);
        int timerValue = Integer.parseInt(Utils.formatString(event.getTimeOut()));
        hideTimerUI();
        handleTurnUpdateTimer(event.getUserId(), timerValue);
    }

    private void handleSendDealEvent(Event event) {
        setUserOptions(true);
        enableView(this.sortCards);
        this.mGroupList.clear();
        this.cardStack = new ArrayList();
        this.cardStack.addAll(event.getPlayingCards());
        this.mGroupList.add(this.cardStack);
        setGroupView(false);
    }

    private void handleStackEvent(Event event) {
        setJokerCard(event);
        this.faceDownCardList.clear();
        this.faceUpCardList.clear();
        this.faceDownCardList.addAll(event.getFaceDownStack());
        this.mFaceDownCards.setVisibility(View.VISIBLE);
        this.faceUpCardList.addAll(event.getFaceUpStack());
        if (this.faceUpCardList.size() > 0) {
            setOpenCard((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
        }
    }

    private void handleCardPickEvent(Event event) {
        boolean isUser = false;
        try {
            boolean faceDown;
            if (event.getGameStack().equalsIgnoreCase("face_down")) {
                faceDown = true;
            } else {
                faceDown = false;
            }
            GamePlayer player = null;
            Iterator it = this.mJoinedPlayersList.iterator();
            while (it.hasNext()) {
                GamePlayer gamePlayer = (GamePlayer) it.next();
                if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(event.getUserId()))) {
                    player = gamePlayer;
                    break;
                }
            }
            if (!(player == null || player.getSeat() == null)) {
                switch (Integer.parseInt(player.getSeat())) {
                    case 1:
                        isUser = true;
                        animatePickCaCard(0, this.mUserPlayerLayout, faceDown, true);
                        if (event.getAutoPlay().equalsIgnoreCase("true")) {
                            if (!event.getGameStack().equalsIgnoreCase("face_down")) {
                                if (event.getGameStack().equalsIgnoreCase("face_up") && this.faceUpCardList != null && this.faceUpCardList.size() > 0) {
                                    addCardToStack((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
                                    this.faceUpCardList.remove(this.faceUpCardList.size() - 1);
                                    if (this.faceUpCardList.size() <= 0) {
                                        this.mOpenCard.setVisibility(View.INVISIBLE);
                                        break;
                                    } else {
                                        setOpenCard((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
                                        break;
                                    }
                                }
                            }
                            clearSelectedCards();
                            if (this.faceDownCardList != null && this.faceDownCardList.size() > 0) {
                                addCardToStack((PlayingCard) this.faceDownCardList.get(this.faceDownCardList.size() - 1));
                                this.faceDownCardList.remove(this.faceDownCardList.size() - 1);
                                break;
                            }
                        }
                        break;
                    case 2:
                        isUser = false;
                        animatePickCaCard(0, this.mSecondPlayerLayout, faceDown, false);
                        break;
                    case 3:
                        isUser = false;
                        animatePickCaCard(0, this.mThirdPlayerLayout, faceDown, false);
                        break;
                    case 4:
                        isUser = false;
                        animatePickCaCard(0, this.mFourthPlayerLayout, faceDown, false);
                        break;
                    case 5:
                        isUser = false;
                        animatePickCaCard(0, this.mFifthPlayerLayout, faceDown, false);
                        break;
                    case 6:
                        isUser = false;
                        animatePickCaCard(0, this.mSixthPlayerLayout, faceDown, false);
                        break;
                }
            }
            if (!isUser) {
                updatedDeckCards(faceDown);
            }
        } catch (Exception e) {
            TLog.e(TAG, "Exception in handleCardPickEvent() : " + e.getMessage());
        }
    }

    private void updatedDeckCards(boolean faceDown) {
        if (faceDown) {
            if (this.faceDownCardList != null && this.faceDownCardList.size() > 0) {
                this.faceDownCardList.remove(this.faceDownCardList.size() - 1);
            }
        } else if (this.faceUpCardList != null && this.faceUpCardList.size() > 0) {
            this.faceUpCardList.remove(this.faceUpCardList.size() - 1);
            if (this.faceUpCardList.size() > 0) {
                setOpenCard((PlayingCard) this.faceUpCardList.get(this.faceUpCardList.size() - 1));
            } else {
                this.mOpenCard.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void hideAutoPlayUI(int seat) {
        switch (seat) {
            case 2:
                //hideAutoPlayView(this.mSecondPlayerLayout);
                hideView(this.player_2_autoplay_box);
                return;
            case 3:
                //hideAutoPlayView(this.mThirdPlayerLayout);
                hideView(this.player_3_autoplay_box);
                return;
            case 4:
                //hideAutoPlayView(this.mFourthPlayerLayout);
                hideView(this.player_4_autoplay_box);
                return;
            case 5:
                //hideAutoPlayView(this.mFifthPlayerLayout);
                hideView(this.player_5_autoplay_box);
                return;
            case 6:
                //hideAutoPlayView(this.mSixthPlayerLayout);
                hideView(this.player_6_autoplay_box);
                return;
            default:
                return;
        }
    }

    private void setAutoPlayUI(GamePlayer player) {
        switch (Integer.parseInt(player.getSeat())) {
            case 2:
                //setAutoPlayView(this.mSecondPlayerLayout, player);
                setAutoPlayUser(this.player_2_autoplay_box, autoplay_count_player_2, player);
                return;
            case 3:
                //setAutoPlayView(this.mThirdPlayerLayout, player);
                setAutoPlayUser(this.player_3_autoplay_box, autoplay_count_player_3, player);
                return;
            case 4:
                //setAutoPlayView(this.mFourthPlayerLayout, player);
                setAutoPlayUser(this.player_4_autoplay_box, autoplay_count_player_4, player);
                return;
            case 5:
                //setAutoPlayView(this.mFifthPlayerLayout, player);
                setAutoPlayUser(this.player_5_autoplay_box, autoplay_count_player_5, player);
                return;
            case 6:
                //setAutoPlayView(this.mSixthPlayerLayout, player);
                setAutoPlayUser(this.player_6_autoplay_box, autoplay_count_player_6, player);
                return;
            default:
                return;
        }
    }

    private void setAutoPlayUser(LinearLayout playerBox, TextView count_tv, GamePlayer gamePlayer) {
        String autoPlay = gamePlayer.getAutoplay();
        String autoPlayCount = gamePlayer.getAutoplay_count();
        String totalCount = gamePlayer.getTotalCount();
        if (autoPlay == null) {
            hideView(playerBox);
        } else if (autoPlay.equalsIgnoreCase("True")) {
            showView(playerBox);
            if (autoPlayCount != null) {
                count_tv.setText(autoPlayCount + "/" + totalCount);
            }
        } else {
            hideView(playerBox);
        }
    }

    private void handleGameResultsEvent(Event event) {
        hideView(this.mSubFragment);
        dismissDialog(this.showDialog);
        dismissDialog(this.dropDialog);
        dismissDialog(this.mLeaveDialog);
        this.mNoOfGamesPlayed++;
        String userScore = "0";
        Iterator it = ((ArrayList) event.getPlayer()).iterator();
        while (it.hasNext()) {
            GamePlayer player = (GamePlayer) it.next();
            setPointsUI(Integer.parseInt(player.getUser_id()), player);
            if (this.userData.getUserId().equalsIgnoreCase(player.getUser_id())) {
                userScore = player.getTotalScore();
            }
        }
        launchGameResultsFragment(event);
        String tableType = event.getTableType();
        if (tableType.contains("101_POOL")) {
            if (Integer.parseInt(Utils.formatString(userScore)) >= 101) {
                hideGameResultsTimer();
                showMaxPointsPopUp(event);
            }
        } else if (tableType.contains("201_POOL") && Integer.parseInt(Utils.formatString(userScore)) >= 201) {
            hideGameResultsTimer();
            showMaxPointsPopUp(event);
        }
        clearData();
    }

    private void hideGameResultsTimer() {
        hideView(getGameResultsMessageView());
    }

    private void showMaxPointsPopUp(Event event) {
        hideWinnerView(this.mDialogLayout, this.winnerView, this.searchGameView, this.splitRejectedView);
        joinAnotherGame(event, "You have reached maximum number of points,Would you like to play another game?");
    }

    private void handleShowEvent(Event event) {
        Log.d(TAG, "Inside handleShowEvent************************************");
        this.isYourTurn = false;
        this.isCardsDistributing = false;
        this.canLeaveTable = false;
        disbaleDeckCards();
        if (!(event.getSuit() == null || event.getFace() == null)) {
            PlayingCard card = new PlayingCard();
            card.setFace(event.getFace());
            card.setSuit(event.getSuit());
            setDiscardCard(card);
        }
        cancelTimer(this.playerTurnOutTimer);
        hideTimerUI();
        if (event.getUserId() != Integer.parseInt(this.userData.getUserId())) {
            Utils.FLAG_OPPOSITE_USER = false;
            SoundPoolManager.getInstance().playSound(R.raw.meld);
            this.mClosedCard.setVisibility(View.VISIBLE);
            this.mGameShecduleTv.setVisibility(View.VISIBLE);
            String userName = event.getNickName();
            Log.e("show-user", userName + "");
            disableView(this.mShowBtn);
            disableView(this.mDropPlayer);
            hideView(this.mDeclareBtn);
            String showMessage = this.mContext.getString(R.string.show_message);
            String message = String.format("%s%s", new Object[]{userName, showMessage + " "});
            this.mGameShecduleTv.setText(message);
            startMeldTimer(Integer.parseInt(Utils.formatString(event.getMeldTimeOut())), message, this.mGameShecduleTv);
            return;
        } else {
            String userName = event.getNickName();
            Log.e("show-user", userName + "");
        }
        this.isPlacedShow = true;
        showView(this.mDeclareBtn);
        enableView(this.mDeclareBtn);
        disableView(this.mDropPlayer);
        removeGameResultFragment();
        startMelding();
        startMeldTimer(Integer.parseInt(Utils.formatString(event.getMeldTimeOut())), this.mContext.getString(R.string.send_your_cards_msg), this.mGameShecduleTv);
    }

    private void handleSittingSeqEvent(Event event) {
        showHideView(true, this.mGameShecduleTv, false);
        this.mGameShecduleTv.setText("Re-arranging seats");
        this.mJoinedPlayersList.clear();
        hideTossCardsView();
        List<GamePlayer> gamePlayersList = event.getPlayer();
        ((TableActivity) this.mContext).resetPlayerIconsOnTableBtn(this.tableId);
        setSeating(gamePlayersList);
    }

    private void handleTossEvent(Event event) {
        SoundPoolManager.getInstance().playSound(R.raw.toss);
        VibrationManager.getInstance().vibrate(1);
        hideTossCardsView();
        showHideView(true, this.mGameShecduleTv, false);
        String tossWinner = event.getTossWinner();
        if (tossWinner.equalsIgnoreCase(this.userData.getNickName())) {
            tossWinner = "You";
        }
        this.mGameShecduleTv.setText(String.format("%s %s", new Object[]{tossWinner, this.mContext.getString(R.string.won_toss_msg)}));
        List<GamePlayer> playerList = event.getPlayers();
        HashMap<String, Integer> cardMap = new HashMap();
        CardDiscard player = new CardDiscard();
        for (int i = 0; i < playerList.size(); i++) {
            String name = playerList.get(i).getSuit() + playerList.get(i).getFace();
            int resourceId = this.getResources().getIdentifier(name, "drawable", this.mContext.getPackageName());
            cardMap.put(((GamePlayer) playerList.get(i)).getUser_id(), resourceId);
        }
        for (Entry pair : cardMap.entrySet()) {
            setTossCards(pair);
        }
    }

    private void handleTurnUpdateEvent(int userId, String timerValueStr) {
        int timerValue = Integer.parseInt(Utils.formatString(timerValueStr));
        disableUserOptions();
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer player = (GamePlayer) it.next();
            if (player.getUser_id().equalsIgnoreCase(this.userData.getUserId())) {
                if (player.isDropped() || getTotalCards() <= 0) {
                    showView(this.sortCards);
                    disableView(this.sortCards);
                } else {
                    showView(this.sortCards);
                    enableView(this.sortCards);
                }
                hideTimerUI();
                handleTurnUpdateTimer(userId, timerValue);
            }
        }
        hideTimerUI();
        handleTurnUpdateTimer(userId, timerValue);
    }

    private void disableUserOptions() {
        showHideView(true, this.mDropPlayer, false);
        disableView(this.mDropPlayer);
        showHideView(true, this.mExtraTimeBtn, false);
        disableView(this.mExtraTimeBtn);
        showHideView(true, this.mShowBtn, false);
        disableView(this.mShowBtn);
    }

    private void setPointsUI(int userId, GamePlayer player) {
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                player.setSeat(gamePlayer.getSeat());
                break;
            }
        }
        if (player != null && player.getSeat() != null) {
            switch (Integer.parseInt(player.getSeat())) {
                case 1:
                    setPlayerPoints(this.mUserPlayerLayout, player);
                    return;
                case 2:
                    setPlayerPoints(this.mSecondPlayerLayout, player);
                    return;
                case 3:
                    setPlayerPoints(this.mThirdPlayerLayout, player);
                    return;
                case 4:
                    setPlayerPoints(this.mFourthPlayerLayout, player);
                    return;
                case 5:
                    setPlayerPoints(this.mFifthPlayerLayout, player);
                    return;
                case 6:
                    setPlayerPoints(this.mSixthPlayerLayout, player);
                    return;
                default:
                    return;
            }
        }
    }

    private void setTossCards(Entry pair) {
        String userId = pair.getKey().toString();
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                player = gamePlayer;
                break;
            }
        }
        int imageResource = Integer.parseInt(pair.getValue().toString());
        if (player != null && player.getSeat() != null) {
            Log.d("toss", player.getNick_name() + " : " + player.getSeat());
            switch (Integer.parseInt(player.getSeat())) {
                case 1:
                    this.mUserTossCard.setImageResource(imageResource);
                    this.mUserTossCard.setVisibility(View.VISIBLE);
                    return;
                case 2:
                    this.mPlayer2TossCard.setImageResource(imageResource);
                    this.mPlayer2TossCard.setVisibility(View.VISIBLE);
                    return;
                case 3:
                    this.mPlayer3TossCard.setImageResource(imageResource);
                    this.mPlayer3TossCard.setVisibility(View.VISIBLE);
                    return;
                case 4:
                    this.mPlayer4TossCard.setImageResource(imageResource);
                    this.mPlayer4TossCard.setVisibility(View.VISIBLE);
                    return;
                case 5:
                    this.mPlayer5TossCard.setImageResource(imageResource);
                    this.mPlayer5TossCard.setVisibility(View.VISIBLE);
                    return;
                case 6:
                    this.mPlayer6TossCard.setImageResource(imageResource);
                    this.mPlayer6TossCard.setVisibility(View.VISIBLE);
                    return;
                default:
                    return;
            }
        }
    }

    private void handlePlayerQuit(int userId) {
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                player = gamePlayer;
                player.setLeft(true);
                break;
            }
        }
        if (player != null && player.getSeat() != null) {
            if (this.mTableDetails != null) {
                setPlayerPositionsOnTableBtn(this.mTableDetails, player, true);
            }
            switch (Integer.parseInt(player.getSeat())) {
                case 1:
                    resetPlayerData(this.mUserPlayerLayout);
                    this.mJoinedPlayersList.remove(player);
                    return;
                case 2:
                    resetPlayerData(this.mSecondPlayerLayout);
                    invisibleView(this.player_2_autoplay_box);
                    invisibleView(this.mPlayer2Cards);
                    this.mJoinedPlayersList.remove(player);
                    return;
                case 3:
                    resetPlayerData(this.mThirdPlayerLayout);
                    invisibleView(this.player_3_autoplay_box);
                    invisibleView(this.mPlayer3Cards);
                    this.mJoinedPlayersList.remove(player);
                    return;
                case 4:
                    resetPlayerData(this.mFourthPlayerLayout);
                    invisibleView(this.player_4_autoplay_box);
                    invisibleView(this.mPlayer4Cards);
                    this.mJoinedPlayersList.remove(player);
                    return;
                case 5:
                    resetPlayerData(this.mFifthPlayerLayout);
                    invisibleView(this.player_5_autoplay_box);
                    invisibleView(this.mPlayer5Cards);
                    this.mJoinedPlayersList.remove(player);
                    return;
                case 6:
                    resetPlayerData(this.mSixthPlayerLayout);
                    invisibleView(this.player_6_autoplay_box);
                    invisibleView(this.mPlayer6Cards);
                    this.mJoinedPlayersList.remove(player);
                    return;
                default:
                    return;
            }
        }
    }

    private void setPlayerUiOnDrop(View view) {
        ((RelativeLayout) view.findViewById(R.id.player_details_root_layout)).setAlpha(CorouselView.SMALL_SCALE);
        showDropImage(view);
        RelativeLayout autoPlayLayout = (RelativeLayout) view.findViewById(R.id.auto_play_layout);
        if (autoPlayLayout != null) {
            invisibleView(autoPlayLayout);
        }
    }

    private void resetPlayerUi(View view) {
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.player_details_root_layout);
        float playerAlpha = relativeLayout.getAlpha();
        if (playerAlpha == 1.0f || playerAlpha == CorouselView.SMALL_SCALE) {
            relativeLayout.setAlpha(1.0f);
        } else {
            relativeLayout.setAlpha(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);
        }
        hideDropPayerImage(view);
    }

    private void handlePlayerDrop(int userId) {
        if (userId == Integer.parseInt(this.userData.getUserId())) {
            this.isUserDropped = true;
        }
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                player = gamePlayer;
                player.setDropped(true);
                break;
            }
        }
        if (player != null && player.getSeat() != null) {
            switch (Integer.parseInt(player.getSeat())) {
                case 1:
                    dropUser();
                    break;
                case 2:
                    setPlayerUiOnDrop(this.mSecondPlayerLayout);
                    invisibleView(this.player_2_autoplay_box);
                    invisibleView(this.mSecondPlayerTimerLayout);
                    invisibleView(this.mSecondPlayerAutoTimerLayout);
                    invisibleView(this.mSecondPlayerAutoChunksLayout);
                    break;
                case 3:
                    setPlayerUiOnDrop(this.mThirdPlayerLayout);
                    invisibleView(this.player_3_autoplay_box);
                    invisibleView(this.mThirdPlayerTimerLayout);
                    invisibleView(this.mThirdPlayerAutoTimerLayout);
                    invisibleView(this.mThirdPlayerAutoChunksLayout);
                    break;
                case 4:
                    setPlayerUiOnDrop(this.mFourthPlayerLayout);
                    invisibleView(this.player_4_autoplay_box);
                    invisibleView(this.mFourthPlayerTimerLayout);
                    invisibleView(this.mFourthPlayerAutoTimerLayout);
                    invisibleView(this.mFourthPlayerAutoChunksLayout);
                    break;
                case 5:
                    setPlayerUiOnDrop(this.mFifthPlayerLayout);
                    invisibleView(this.player_5_autoplay_box);
                    invisibleView(this.mFifthPlayerTimerLayout);
                    invisibleView(this.mFifthPlayerAutoTimerLayout);
                    invisibleView(this.mFifthPlayerAutoChunksLayout);
                    break;
                case 6:
                    setPlayerUiOnDrop(this.mSixthPlayerLayout);
                    invisibleView(this.player_6_autoplay_box);
                    invisibleView(this.mSixthPlayerTimerLayout);
                    invisibleView(this.mSixthPlayerAutoTimerLayout);
                    invisibleView(this.mSixthPlayerAutoChunksLayout);
                    break;
            }
            if (player.isLeft()) {
                this.mJoinedPlayersList.remove(player);
            }
        }
    }

    private void handleTurnUpdateTimer(int userId, int timerValue) {
        GamePlayer player = null;
        Iterator it = this.mJoinedPlayersList.iterator();
        while (it.hasNext()) {
            GamePlayer gamePlayer = (GamePlayer) it.next();
            if (gamePlayer.getUser_id().equalsIgnoreCase(String.valueOf(userId))) {
                player = gamePlayer;
                break;
            }
        }
        if (player != null) {
            cancelTimer(this.playerTurnOutTimer);
            startPlayerTimer(timerValue, Integer.parseInt(player.getSeat()), player);
        }
    }

    private void startMeldTimer(int timeOut, String message, TextView timerTv) {
        if (this.meldTimer != null) {
            this.meldTimer.cancel();
            this.meldTimer = null;
        }
        TextView textView = timerTv;
        this.meldTimer = new RummyCountDownTimer(this, (long) (Utils.TIMER_INTERVAL * timeOut), (long) Utils.TIMER_INTERVAL, textView, (TextView) this.mMeldCardsView.findViewById(R.id.game_timer), message, this.isPlacedShow);
        this.meldTimer.start();
    }

    public void animateTableButtons() {
        if (this.mApplication.getJoinedTableIds().size() == 2) {
            ((TableActivity) this.mContext).flashButton(getTag());
        }
    }

    private void hideTimerUI() {
        if (this.playerTurnOutTimer != null) {
            this.playerTurnOutTimer.cancel();
        }
        invisibleView(this.mUserTimerRootLayout);
        invisibleView(this.mSecondPlayerTimerLayout);
        invisibleView(this.mThirdPlayerTimerLayout);
        invisibleView(this.mFourthPlayerTimerLayout);
        invisibleView(this.mFifthPlayerTimerLayout);
        invisibleView(this.mSixthPlayerTimerLayout);
        invisibleView(this.mUserAutoTimerRootLayout);
        invisibleView(this.mSecondPlayerAutoTimerLayout);
        invisibleView(this.mThirdPlayerAutoTimerLayout);
        invisibleView(this.mFourthPlayerAutoTimerLayout);
        invisibleView(this.mFifthPlayerAutoTimerLayout);
        invisibleView(this.mSixthPlayerAutoTimerLayout);
        invisibleView(this.mUserAutoChunksLayout);
        invisibleView(this.mSecondPlayerAutoChunksLayout);
        invisibleView(this.mThirdPlayerAutoChunksLayout);
        invisibleView(this.mFourthPlayerAutoChunksLayout);
        invisibleView(this.mFifthPlayerAutoChunksLayout);
        invisibleView(this.mSixthPlayerAutoChunksLayout);
    }

    private void clearStacks() {
        this.faceDownCardList.clear();
        this.faceUpCardList.clear();
    }

    private void refreshStacks(Event event) {
        this.faceDownCardList.addAll(event.getFaceDownStack());
        this.faceUpCardList.addAll(event.getFaceUpStack());
        ((TextView) this.mReshuffleView.findViewById(R.id.dialog_msg_tv)).setText("Cards reshuffled.");
        showView(this.mReshuffleView);
        ((Button) this.mReshuffleView.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.hideView(TablesFragment.this.mReshuffleView);
            }
        });
        ((ImageView) this.mReshuffleView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.hideView(TablesFragment.this.mReshuffleView);
            }
        });
    }

    public void dismissQuickMenu() {
        if (this.mQuickAction != null) {
            this.mQuickAction.dismiss();
        }
    }

    private void animateCard(View view) {
        android.view.animation.Animation animation = AnimationUtils.loadAnimation(this.mContext.getApplicationContext(), R.anim.move_up);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    private void setOpenCard(PlayingCard playingCard) {
        this.mOpenJokerCard.setVisibility(View.GONE);
        int imgId = getResources().getIdentifier(String.format("%s%s", new Object[]{playingCard.getSuit(), playingCard.getFace()}), "drawable", this.mContext.getPackageName());
        this.mOpenCard.setVisibility(View.VISIBLE);
        this.mOpenCard.setImageResource(imgId);
        if (this.mJockerCard == null) {
            return;
        }
        if (this.mJockerCard.getFace().equalsIgnoreCase(playingCard.getFace())) {
            this.mOpenJokerCard.setVisibility(View.VISIBLE);
        } else if (playingCard == null || !playingCard.getFace().equalsIgnoreCase("1")) {
            this.mOpenJokerCard.setVisibility(View.GONE);
        } else if (this.mJockerCard.getFace().equalsIgnoreCase("0")) {
            this.mOpenJokerCard.setVisibility(View.VISIBLE);
        } else {
            this.mOpenJokerCard.setVisibility(View.GONE);
        }
    }

    private void setJokerCard(Event event) {
        if (Boolean.valueOf(event.getJocker()).booleanValue()) {
            PlayingCard jokerCard = new PlayingCard();
            jokerCard.setFace(event.getFace());
            jokerCard.setSuit(event.getSuit());
            IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
            if (iamBackFragment != null) {
                iamBackFragment.setJokerCard(jokerCard);
            }
            this.mJockerCard = jokerCard;
            int imgId = getResources().getIdentifier(String.format("%s%s", new Object[]{event.getSuit(), event.getFace()}), "drawable", this.mContext.getPackageName());
            this.mJokerCard.setVisibility(View.VISIBLE);
            this.mJokerCard.setImageResource(imgId);
        }
    }

    public TextView getGameResultsMessageView() {
        return (TextView) this.mGameResultsView.findViewById(R.id.game_timer);
    }

    private void startGameScheduleTimer(int scheduleTime, boolean isMelding) {
        try {
            cancelTimer(this.mGameScheduleTimer);
            final boolean z = isMelding;
            final int i = scheduleTime;
            this.mGameScheduleTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
                public void onTick(long millisUntilFinished) {
                    long timeFinished = millisUntilFinished / 1000;
                    if (!z) {
                        if (timeFinished <= 10) {
                            TablesFragment.this.canLeaveTable = false;
                            TablesFragment.this.dismissDialog(TablesFragment.this.mLeaveDialog);
                        }
                        TablesFragment.this.mGameShecduleTv.setText(TablesFragment.this.mContext != null ? TablesFragment.this.mContext.getString(R.string.game_starts_msg) + " " + timeFinished + " " + TablesFragment.this.mContext.getString(R.string.seconds_txt) : "");
                    }
                    if (z) {
                        TablesFragment.this.showView(TablesFragment.this.mGameShecduleTv);
                        TablesFragment.this.canLeaveTable = false;
                        TablesFragment.this.isUserPlacedValidShow = true;
                        TablesFragment.this.mGameShecduleTv.setText("Please wait while we check your opponent cards in " + timeFinished + " " + TablesFragment.this.mContext.getString(R.string.seconds_txt));
                        TablesFragment.this.setGameResultsTimer(TablesFragment.this.mGameResultsView, TablesFragment.this.mContext != null ? "Please wait while we check your opponent cards in " + timeFinished + TablesFragment.this.mContext.getString(R.string.seconds_txt) : "");
                    }
                    if (TablesFragment.this.isGameResultsShowing && !z) {
                        if (timeFinished == ((long) (i / 2))) {
                            TablesFragment.this.removeGameResultFragment();
                        } else {
                            TablesFragment.this.setGameResultsTimer(TablesFragment.this.mGameResultsView, TablesFragment.this.mContext != null ? TablesFragment.this.mContext.getString(R.string.game_result_game_starts_msg) + " " + timeFinished + " " + TablesFragment.this.mContext.getString(R.string.seconds_txt) : "");
                        }
                    }
                    if (TablesFragment.this.mMeldCardsView.getVisibility() == View.VISIBLE) {
                        ((TextView) TablesFragment.this.mMeldCardsView.findViewById(R.id.game_timer)).setText(String.format("%s%s%s", new Object[]{TablesFragment.this.mContext.getString(R.string.game_result_game_starts_msg) + " ", String.valueOf(timeFinished), " " + TablesFragment.this.mContext.getString(R.string.seconds_txt)}));
                    }
                    TablesFragment.this.setTableButtonsUI();
                }

                public void onFinish() {
                    if (z || TablesFragment.this.mNoOfGamesPlayed > 0) {
                        TablesFragment.this.mGameShecduleTv.setVisibility(View.INVISIBLE);
                    } else {
                        TablesFragment.this.isTossEventRunning = true;
                        TablesFragment.this.mGameShecduleTv.setText(R.string.toss_info_msg);
                    }
                    if (TablesFragment.this.isGameResultsShowing && !z) {
                        TablesFragment.this.removeGameResultFragment();
                    }
                    TablesFragment.this.setGameResultsTimer(TablesFragment.this.mGameResultsView, "");
                }
            }.start();
        } catch (Exception e) {
            TLog.e(TAG, "Exception in startGameScheduleTimer :: " + e.getMessage());
        }
    }

    private void startWrongMeldTimer(int scheduleTime, String message) {
        try {
            hideView(this.mGameShecduleTv);
            final TextView timerTv = (TextView) this.mSmartCorrectionView.findViewById(R.id.sc_game_timer);
            cancelTimer(this.mWrongMeldTimer);
            final String str = message;
            this.mWrongMeldTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
                public void onTick(long millisUntilFinished) {
                    TablesFragment.this.hideView(TablesFragment.this.mGameShecduleTv);
                    TablesFragment.this.showView(TablesFragment.this.mWrongMeldTv);
                    long timeFinished = millisUntilFinished / 1000;
                    timerTv.setText(String.format("%s%s%s", new Object[]{str, Long.valueOf(millisUntilFinished / 1000), " seconds."}));
                }

                public void onFinish() {
                    TablesFragment.this.isSmartCorrectionShowing = false;
                    TablesFragment.this.hideView(TablesFragment.this.mSmartCorrectionView);
                    TablesFragment.this.hideView(TablesFragment.this.mWrongMeldTv);
                }
            }.start();
        } catch (Exception e) {
            TLog.e(TAG, "Exception in startWrongMeldTimer :: " + e.getMessage());
        }
    }

    public boolean isGameResultsShowing() {
        return this.isGameResultsShowing;
    }

    public boolean isMeldScreenIsShowing() {
        return this.isMeldFragmentShowing;
    }

    private void removeGameResultFragment() {
        hideView(this.searchGameView);
        hideView(this.mSubFragment);
        this.isGameResultsShowing = false;
        this.mMeldGroupList.clear();
        setTableButtonsUI();
    }

    private void removeMeldCardsFragment() {
        hideView(this.mSubFragment);
        this.isMeldFragmentShowing = false;
        this.mMeldGroupList.clear();
        setTableButtonsUI();
    }

    private void startPlayerTimer(int scheduleTime, int playerPosition, GamePlayer player) {
        IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment != null) {
            iamBackFragment.enableIamBackButton();
        }
        final int i = playerPosition;
        final GamePlayer gamePlayer = player;
        this.playerTurnOutTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                int timeRemaining = (int) (millisUntilFinished / 1000);
                switch (i) {
                    case 1:
                        if (TablesFragment.this.userData.getUserId().equalsIgnoreCase(gamePlayer.getUser_id())) {
                            TablesFragment.this.isYourTurn = true;
                            TablesFragment.this.showHideView(true, TablesFragment.this.mDropPlayer, false);
                            int totalCards = TablesFragment.this.getTotalCards();
                            if (totalCards < 14 && !TablesFragment.this.isPlacedShow) {
                                TablesFragment.this.setDropButton();
                            } else if (totalCards == 14) {
                                showView(mAutoDropPlayer);
                                TablesFragment.this.disableView(TablesFragment.this.mDropPlayer);
                            }
                            TablesFragment.this.showHideView(true, TablesFragment.this.mUserTimerRootLayout, false);
                            if (TablesFragment.this.mApplication.getJoinedTableIds().size() == 2) {
                                ((TableActivity) TablesFragment.this.mContext).flashButton(TablesFragment.this.getTag());
                            }
                            if (TablesFragment.this.mAutoExtraTimeEvent != null && Integer.parseInt(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime()) == 0) {
                                TablesFragment.this.showView(TablesFragment.this.mExtraTimeBtn);
                                TablesFragment.this.enableView(TablesFragment.this.mExtraTimeBtn);
                            }
                        } else {
                            TablesFragment.this.setUserOptions(false);
                            TablesFragment.this.showHideView(true, TablesFragment.this.mUserTimerRootLayout, false);
                        }
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mUserAutoTimerRootLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mUserAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mUserAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mUserTimerRootLayout, TablesFragment.this.mUserTimerTv, TablesFragment.this.isYourTurn);
                        break;
                    case 2:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mSecondPlayerTimerLayout);
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mSecondPlayerAutoTimerLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mSecondPlayerAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mSecondPlayerAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mSecondPlayerTimerLayout, TablesFragment.this.mSecondPlayerTimerTv, TablesFragment.this.isYourTurn);
                        break;
                    case 3:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mThirdPlayerTimerLayout);
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mThirdPlayerAutoTimerLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mThirdPlayerAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mThirdPlayerAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mThirdPlayerTimerLayout, TablesFragment.this.mThirdPlayerTimerTv, TablesFragment.this.isYourTurn);
                        break;
                    case 4:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mFourthPlayerTimerLayout);
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mFourthPlayerAutoTimerLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mFourthPlayerAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mFourthPlayerAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mFourthPlayerTimerLayout, TablesFragment.this.mFourthPlayerTimerTv, TablesFragment.this.isYourTurn);
                        break;
                    case 5:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mFifthPlayerTimerLayout);
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mFifthPlayerAutoTimerLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mFifthPlayerAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mFifthPlayerAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mFifthPlayerTimerLayout, TablesFragment.this.mFifthPlayerTimerTv, TablesFragment.this.isYourTurn);
                        break;
                    case 6:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mSixthPlayerTimerLayout);
                        if (TablesFragment.this.autoExtraTime) {
                            TablesFragment.this.showView(TablesFragment.this.mSixthPlayerAutoTimerLayout);
                            if (!(TablesFragment.this.mAutoExtraTimeEvent == null || TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime() == null)) {
                                TablesFragment.this.mSixthPlayerAutoTimerTv.setText(TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime());
                                TablesFragment.this.setAutoExtraTimeChunks(TablesFragment.this.mSixthPlayerAutoChunksLayout, TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraChunks());
                            }
                        }
                        TablesFragment.this.setUserTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mSixthPlayerTimerLayout, TablesFragment.this.mSixthPlayerTimerTv, TablesFragment.this.isYourTurn);
                        break;
                }
                if (TablesFragment.this.mGameShecduleTv.getVisibility() == View.VISIBLE) {
                    TablesFragment.this.messageVisibleCount = TablesFragment.this.messageVisibleCount + 1;
                    if (TablesFragment.this.messageVisibleCount == 5) {
                        TablesFragment.this.invisibleView(TablesFragment.this.mGameShecduleTv);
                        TablesFragment.this.messageVisibleCount = 0;
                    }
                }
            }

            public void onFinish() {
                if (TablesFragment.this.isYourTurn) {
                    IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) TablesFragment.this.mContext).getFragmentByTag(IamBackFragment.class.getName());
                    if (iamBackFragment != null) {
                        iamBackFragment.disableIamBackButton();
                    }
                }
                TablesFragment.this.isYourTurn = false;
                TablesFragment.this.cancelTimer(TablesFragment.this.playerTurnOutTimer);
                if (TablesFragment.this.autoExtraTime && TablesFragment.this.mAutoExtraTimeEvent != null) {
                    String autoExtraTimeStr = TablesFragment.this.mAutoExtraTimeEvent.getAutoExtraTime();
                    if (!(autoExtraTimeStr == null || autoExtraTimeStr.equalsIgnoreCase("0"))) {
                        TablesFragment.this.startPlayerAutoTimer(Integer.parseInt(autoExtraTimeStr), i, gamePlayer);
                    }
                }
                switch (i) {
                    case 1:
                        TablesFragment.this.mUserTimerTv.setText("0");
                        return;
                    case 2:
                        TablesFragment.this.mSecondPlayerTimerTv.setText("0");
                        return;
                    case 3:
                        TablesFragment.this.mThirdPlayerTimerTv.setText("0");
                        return;
                    case 4:
                        TablesFragment.this.mFourthPlayerTimerTv.setText("0");
                        return;
                    case 5:
                        TablesFragment.this.mFifthPlayerTimerTv.setText("0");
                        return;
                    case 6:
                        TablesFragment.this.mSixthPlayerTimerTv.setText("0");
                        return;
                    default:
                        return;
                }
            }
        };
        this.playerTurnOutTimer.start();
    }

    private void setAutoExtraTimeChunks(View autoChunkLayout, String chunks) {
        showView(autoChunkLayout);
        ImageView chunk1Iv = (ImageView) autoChunkLayout.findViewById(R.id.auto_chunk_1);
        ImageView chunk2Iv = (ImageView) autoChunkLayout.findViewById(R.id.auto_chunk_2);
        ImageView chunk3Iv = (ImageView) autoChunkLayout.findViewById(R.id.auto_chunk_3);
        chunk1Iv.setImageResource(0);
        chunk2Iv.setImageResource(0);
        chunk3Iv.setImageResource(0);
        if (chunks != null && !chunks.isEmpty()) {
            switch (Integer.parseInt(chunks.split("/")[0])) {
                case 1:
                    chunk1Iv.setImageResource(R.drawable.chunk1);
                    return;
                case 2:
                    chunk1Iv.setImageResource(R.drawable.chunk1);
                    chunk2Iv.setImageResource(R.drawable.chunk2);
                    return;
                case 3:
                    chunk1Iv.setImageResource(R.drawable.chunk1);
                    chunk2Iv.setImageResource(R.drawable.chunk2);
                    chunk3Iv.setImageResource(R.drawable.chunk3);
                    return;
                default:
                    return;
            }
        }
    }

    private void startPlayerAutoTimer(int scheduleTime, int playerPosition, GamePlayer player) {
        IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment != null) {
            iamBackFragment.enableIamBackButton();
        }
        final int i = playerPosition;
        final GamePlayer gamePlayer = player;
        this.playerTurnOutTimer = new CountDownTimer((long) (scheduleTime * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                int timeRemaining = (int) (millisUntilFinished / 1000);
                switch (i) {
                    case 1:
                        if (TablesFragment.this.userData.getUserId().equalsIgnoreCase(gamePlayer.getUser_id())) {
                            TablesFragment.this.isYourTurn = true;
                            TablesFragment.this.showHideView(true, TablesFragment.this.mDropPlayer, false);
                            int totalCards = TablesFragment.this.getTotalCards();
                            if (totalCards < 14 && !TablesFragment.this.isPlacedShow) {
                                TablesFragment.this.setDropButton();
                            } else if (totalCards == 14) {
                                showView(mAutoDropPlayer);
                                TablesFragment.this.disableView(TablesFragment.this.mDropPlayer);
                            }
                            TablesFragment.this.showHideView(true, TablesFragment.this.mUserTimerRootLayout, false);
                            if (TablesFragment.this.mApplication.getJoinedTableIds().size() == 2) {
                                ((TableActivity) TablesFragment.this.mContext).flashButton(TablesFragment.this.getTag());
                            }
                        } else {
                            TablesFragment.this.setUserOptions(false);
                            TablesFragment.this.showHideView(true, TablesFragment.this.mUserTimerRootLayout, false);
                        }
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mUserAutoTimerRootLayout, TablesFragment.this.isYourTurn);
                        break;
                    case 2:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mSecondPlayerTimerLayout);
                        TablesFragment.this.showView(TablesFragment.this.mSecondPlayerAutoTimerLayout);
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mSecondPlayerAutoTimerLayout, TablesFragment.this.isYourTurn);
                        break;
                    case 3:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mThirdPlayerTimerLayout);
                        TablesFragment.this.showView(TablesFragment.this.mThirdPlayerAutoTimerLayout);
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mThirdPlayerAutoTimerLayout, TablesFragment.this.isYourTurn);
                        break;
                    case 4:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mFourthPlayerTimerLayout);
                        TablesFragment.this.showView(TablesFragment.this.mFourthPlayerAutoTimerLayout);
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mFourthPlayerAutoTimerLayout, TablesFragment.this.isYourTurn);
                        break;
                    case 5:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mFifthPlayerTimerLayout);
                        TablesFragment.this.showView(TablesFragment.this.mFifthPlayerAutoTimerLayout);
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mFifthPlayerAutoTimerLayout, TablesFragment.this.isYourTurn);
                        break;
                    case 6:
                        TablesFragment.this.isYourTurn = false;
                        TablesFragment.this.showView(TablesFragment.this.mSixthPlayerTimerLayout);
                        TablesFragment.this.showView(TablesFragment.this.mSixthPlayerAutoTimerLayout);
                        TablesFragment.this.setUserAutoTimer(millisUntilFinished, timeRemaining, TablesFragment.this.mSixthPlayerAutoTimerLayout, TablesFragment.this.isYourTurn);
                        break;
                }
                if (TablesFragment.this.mGameShecduleTv.getVisibility() == View.VISIBLE) {
                    TablesFragment.this.messageVisibleCount = TablesFragment.this.messageVisibleCount + 1;
                    if (TablesFragment.this.messageVisibleCount == 5) {
                        TablesFragment.this.invisibleView(TablesFragment.this.mGameShecduleTv);
                        TablesFragment.this.messageVisibleCount = 0;
                    }
                }
            }

            public void onFinish() {
                if (TablesFragment.this.isYourTurn) {
                    IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) TablesFragment.this.mContext).getFragmentByTag(IamBackFragment.class.getName());
                    if (iamBackFragment != null) {
                        iamBackFragment.disableIamBackButton();
                    }
                }
                TablesFragment.this.isYourTurn = false;
                TablesFragment.this.cancelTimer(TablesFragment.this.playerTurnOutTimer);
                switch (i) {
                    case 1:
                        TablesFragment.this.mUserAutoTimerTv.setText("0");
                        return;
                    case 2:
                        TablesFragment.this.mSecondPlayerAutoTimerTv.setText("0");
                        return;
                    case 3:
                        TablesFragment.this.mThirdPlayerAutoTimerTv.setText("0");
                        return;
                    case 4:
                        TablesFragment.this.mFourthPlayerAutoTimerTv.setText("0");
                        return;
                    case 5:
                        TablesFragment.this.mFifthPlayerAutoTimerTv.setText("0");
                        return;
                    case 6:
                        TablesFragment.this.mSixthPlayerAutoTimerTv.setText("0");
                        return;
                    default:
                        return;
                }
            }
        };
        this.playerTurnOutTimer.start();
    }

    private void setUpExtraTimeUI() {
        if (this.mAutoExtraTimeEvent == null) {
            showHideView(true, this.mExtraTimeBtn, false);
            enableView(this.mExtraTimeBtn);
        } else if (Integer.parseInt(this.mAutoExtraTimeEvent.getAutoExtraChunks().split("/")[0]) == 1) {
            enableView(this.mExtraTimeBtn);
        } else {
            disableView(this.mExtraTimeBtn);
        }
    }

    private void setDropButton() {

        if (this.mTableDetails == null || !(this.mTableDetails.getTableType().equalsIgnoreCase("BEST_OF_2") || this.mTableDetails.getTableType().equalsIgnoreCase("BEST_OF_3"))) {
            enableView(this.mDropPlayer);
        } else {
            disableView(this.mDropPlayer);
        }
    }

    private void setUserTimer(long millisUntilFinished, int timeRemaining, View layout, TextView timerIv, boolean isYourTurn) {
        layout.setVisibility(View.VISIBLE);
        if (!isYourTurn || timeRemaining > 10) {
            this.canLeaveTable = true;
        } else {
            dismissDialog(this.mLeaveDialog);
            this.canLeaveTable = false;
            this.isTossEventRunning = false;
            this.isCardsDistributing = false;
            SoundPoolManager.getInstance().playSound(R.raw.clock);
            VibrationManager.getInstance().vibrate(1);
        }
        if (timeRemaining < 10) {
            timerIv.setTextColor(ContextCompat.getColor(this.mContext, R.color.Red));
            layout.setBackgroundDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.timer_border_red));
        } else if (timeRemaining < 20) {
            timerIv.setTextColor(ContextCompat.getColor(this.mContext, R.color.yellow));
            layout.setBackgroundDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.timer_border_yellow));
        } else {
            timerIv.setTextColor(ContextCompat.getColor(this.mContext, R.color.timerTextColor));
            layout.setBackgroundDrawable(ContextCompat.getDrawable(this.mContext, R.drawable.timer_border_green));
        }
        timerIv.setText("" + (millisUntilFinished / 1000) + "");
        IamBackFragment iamBackFragment;
        if (timeRemaining < 5) {
            iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
            if (iamBackFragment == null) {
                return;
            }
            if (isYourTurn) {
                iamBackFragment.disableIamBackButton();
                return;
            } else {
                iamBackFragment.enableIamBackButton();
                return;
            }
        }
        iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment != null) {
            iamBackFragment.enableIamBackButton();
        }
    }

    private void setUserAutoTimer(long millisUntilFinished, int timeRemaining, View layout, boolean isYourTurn) {
        layout.setVisibility(View.VISIBLE);
        TextView timerIv = (TextView) layout.findViewById(R.id.player_auto_timer_tv);
        if (!isYourTurn || timeRemaining > 10) {
            this.canLeaveTable = true;
        } else {
            dismissDialog(this.mLeaveDialog);
            this.canLeaveTable = false;
            this.isTossEventRunning = false;
            this.isCardsDistributing = false;
            SoundPoolManager.getInstance().playSound(R.raw.clock);
            VibrationManager.getInstance().vibrate(1);
        }
        if (timeRemaining < 5) {
            if (isYourTurn) {
                setUpExtraTimeUI();
            }
        } else if (timeRemaining < 10) {
        }
        timerIv.setText("" + (millisUntilFinished / 1000) + "");
        IamBackFragment iamBackFragment = (IamBackFragment) ((TableActivity) this.mContext).getFragmentByTag(IamBackFragment.class.getName());
        if (iamBackFragment == null) {
            return;
        }
        if (isYourTurn) {
            iamBackFragment.disableIamBackButton();
        } else {
            iamBackFragment.enableIamBackButton();
        }
    }

    private void sortPlayerCards() {
        dismissQuickMenu();
        sortPlayerStack();
    }

    public PlayingCard getDiscardedCard() {
        return this.mDiscardedCard;
    }

    public void updateCardsGroup(ArrayList<ArrayList<PlayingCard>> updatedCardsList) {
        this.mGroupList = updatedCardsList;
    }

    public void updateCardsView() {
        setGroupView(false);
    }

    public void setSelectedCards() {
        TLog.e(TAG, "Selected cards ::  " + this.mSelectedCards.size());
    }

    private void animateUserDiscardCard() {
        if (!((TableActivity) this.mContext).isFromIamBack()) {
            this.mUserDiscardLaout.removeAllViews();
            this.mUserDiscardLaout.invalidate();
            clearAnimationData();
            LayoutParams lprams = new LayoutParams(-1, -1);
            ImageView image = new ImageView(getContext());
            image.setLayoutParams(lprams);
            if (this.mDiscardImage != null) {
                image.setImageDrawable(this.mDiscardImage);
            } else {
                image.setImageResource(R.drawable.closedcard);
            }
            image.setVisibility(View.INVISIBLE);
            this.mUserDiscardLaout.addView(image);
            this.mDummyVies.add(image);
            ImageView iv = (ImageView) this.mDummyVies.get(0);
            iv.setVisibility(View.VISIBLE);
            TransferAnimation an = new TransferAnimation(iv);
            an.setDuration(80);
            an.setListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    TablesFragment.this.mUserDiscardLaout.removeAllViews();
                    TablesFragment.this.mUserDiscardLaout.invalidate();
                    TablesFragment.this.clearAnimationData();
                }
            });
            an.setDestinationView(this.mOpenCard).animate();
        }
    }

    private void discardCard(String suit, String face) {
        CardDiscard request = new CardDiscard();
        request.setEventName("CARD_DISCARD");
        request.setTableId(this.tableId);
        request.setUuid(Utils.generateUuid());
        request.setNickName(this.userData.getNickName());
        request.setAutoPlay("False");
        request.setFace(face);
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setSuit(suit);
        request.setCardSending("player");
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        updateDeckCardsOnDiscard(suit, face);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.disCardListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "discardCard" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void updateDeckCardsOnDiscard(String suit, String face) {
        if (this.mSelectedImgList.size() > 0) {
            this.mDiscardImage = ((ImageView) this.mSelectedImgList.get(0)).getDrawable();
        }
        animateUserDiscardCard();
        String cardValue = String.format("%s%s", new Object[]{suit, face});
        PlayingCard discardCard = new PlayingCard();
        discardCard.setFace(face);
        discardCard.setSuit(suit);
        setOpenCard(discardCard);
        this.faceUpCardList.add(discardCard);
        updateDiscardsList(discardCard);
        int imgId = getResources().getIdentifier(cardValue, "drawable", this.mContext.getPackageName());
        this.mOpenCard.setVisibility(View.VISIBLE);
        this.mOpenCard.setImageResource(imgId);
        Event event = new Event();
        event.setFace(face);
        event.setSuit(suit);
        event.setNickName(this.userData.getNickName());
        ((TableActivity) this.mContext).addDiscardToPlayer(event);
        SoundPoolManager.getInstance().playSound(R.raw.pick_discard);
        VibrationManager.getInstance().vibrate(1);
    }

    private void updateDiscardsList(PlayingCard discardCard) {
        Event discardEvent = new Event();
        discardEvent.setFace(discardCard.getFace());
        discardEvent.setSuit(discardCard.getSuit());
        discardEvent.setUserId(Integer.parseInt(this.userData.getUserId()));
        discardEvent.setNickName(this.userData.getNickName());
        discardEvent.setTableId(this.tableId);
        discardEvent.setEventName("CARD_DISCARD");
        ((TableActivity) this.mContext).addDiscardToPlayer(discardEvent);
    }

    private void cardPick(String suit, String face, String stack) {
        if (stack.contains("down")) {
            animatePickCaCard(0, this.mUserTossCard, true, true);
        } else {
            animatePickCaCard(0, this.mUserTossCard, false, true);
        }
        CardDiscard request = new CardDiscard();
        request.setEventName("CARD_PICK");
        request.setStack(stack);
        request.setTableId(this.tableId);
        request.setUuid(Utils.generateUuid());
        request.setNickName(this.userData.getNickName());
        request.setFace(face);
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setSuit(suit);
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.cardPickListner);
            isCardPicked = true;
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "cardPickListner" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void dropPlayer() {
        setUserOptions(false);
        CardDiscard request = new CardDiscard();
        request.setEventName("PLAYER_DROP");
        request.setUuid(Utils.generateUuid());
        request.setTableId(this.tableId);
        request.setNickName(this.userData.getNickName());
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.dropPlayerListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "dropPlayer" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void autoDropPlayer(String pTableId) {
        Log.e("autoDropPlayer", pTableId+"");
        setUserOptions(false);
        CardDiscard request = new CardDiscard();
        request.setEventName("PLAYER_DROP");
        request.setUuid(Utils.generateUuid());
        request.setTableId(pTableId);
        request.setNickName(this.userData.getNickName());
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.dropPlayerListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "dropPlayer" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void turnExtraTime() {
        LeaveTableRequest request = new LeaveTableRequest();
        request.setEventName("extratime");
        request.setUuid(Utils.generateUuid());
        request.setTableId(this.tableId);
        request.setNickName(this.userData.getNickName());
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.extraTimeListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "EXTRA_TIME" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void leaveTable() {
//
        int intIndex = alAutoDrop.indexOf(myTableId);
        alAutoDropBoolean.remove(intIndex);
        alAutoDrop.remove(tableId);
//
        if (((RummyApplication) this.mContext.getApplication()).getJoinedTableIds().size() == 1) {
            ((TableActivity) this.mContext).setIsBackPressed(true);
        }
        showLoadingDialog(this.mContext);
        LeaveTableRequest request = new LeaveTableRequest();
        request.setEventName("quit_table");
        request.setUuid(Utils.generateUuid());
        request.setTableId(this.tableId);
        request.setNickName(this.userData.getNickName());
        request.setUserId(String.valueOf(this.userData.getUserId()));
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.leaveTableListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "LEAVE_TABLE" + gameEngineNotRunning.getLocalizedMessage());
        }

    }

    public void sortPlayerStack() {
        this.mRummyView.removeViews();
        this.playerCards.clear();
        clearSelectedCards();
        ArrayList<PlayingCard> mHeartCards = new ArrayList();
        ArrayList<PlayingCard> mClubsCards = new ArrayList();
        ArrayList<PlayingCard> mDiamondCards = new ArrayList();
        ArrayList<PlayingCard> mSpadeCards = new ArrayList();
        ArrayList<PlayingCard> jokerCardList = new ArrayList();
        Iterator it = this.mGroupList.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((ArrayList) it.next()).iterator();
            while (it2.hasNext()) {
                PlayingCard card = (PlayingCard) it2.next();
                if (card.getSuit().equalsIgnoreCase(Utils.DIAMOND)) {
                    mDiamondCards.add(card);
                } else if (card.getSuit().equalsIgnoreCase(Utils.CLUB)) {
                    mClubsCards.add(card);
                } else if (card.getSuit().equalsIgnoreCase(Utils.SPADE)) {
                    mSpadeCards.add(card);
                } else if (card.getSuit().equalsIgnoreCase(Utils.HEART)) {
                    mHeartCards.add(card);
                } else {
                    jokerCardList.add(card);
                }
            }
        }
        this.mGroupList.clear();
        if (mDiamondCards.size() > 0) {
            Collections.sort(mDiamondCards, new FaceComparator());
            this.mGroupList.add(mDiamondCards);
        }
        if (mClubsCards.size() > 0) {
            Collections.sort(mClubsCards, new FaceComparator());
            this.mGroupList.add(mClubsCards);
        }
        if (mSpadeCards.size() > 0) {
            Collections.sort(mSpadeCards, new FaceComparator());
            this.mGroupList.add(mSpadeCards);
        }
        if (mHeartCards.size() > 0) {
            Collections.sort(mHeartCards, new FaceComparator());
            this.mGroupList.add(mHeartCards);
        }
        if (jokerCardList.size() > 0) {
            Collections.sort(jokerCardList, new FaceComparator());
            this.mGroupList.add(jokerCardList);
        }
        setGroupView(false);
    }

    private void addSampleCards() {
        this.isYourTurn = true;
        ArrayList<PlayingCard> group1 = new ArrayList();
        ArrayList<PlayingCard> group2 = new ArrayList();
        ArrayList<PlayingCard> group3 = new ArrayList();
        ArrayList<PlayingCard> group4 = new ArrayList();
        ArrayList<PlayingCard> group5 = new ArrayList();
        this.mCards.clear();
        setUserOptions(true);
        showView(this.mUserPlayerLayout);
        showView(this.sortCards);
        enableView(this.sortCards);
        PlayingCard jokerCard = new PlayingCard();
        jokerCard.setFace("10");
        jokerCard.setSuit("s");
        this.mJockerCard = jokerCard;
        PlayingCard card = new PlayingCard();
        card.setFace("10");
        card.setSuit("c");
        PlayingCard card1 = new PlayingCard();
        card1.setFace("11");
        card1.setSuit("c");
        PlayingCard card2 = new PlayingCard();
        card2.setFace("12");
        card2.setSuit("c");
        PlayingCard card01 = new PlayingCard();
        card01.setFace("13");
        card01.setSuit("c");
        group1.add(card);
        group1.add(card1);
        group1.add(card2);
        group1.add(card01);
        PlayingCard card3 = new PlayingCard();
        card3.setFace("10");
        card3.setSuit("s");
        PlayingCard card4 = new PlayingCard();
        card4.setFace("12");
        card4.setSuit("s");
        PlayingCard card5 = new PlayingCard();
        card5.setFace("13");
        card5.setSuit("s");
        PlayingCard card6 = new PlayingCard();
        card6.setFace("11");
        card6.setSuit("s");
        group2.add(card3);
        group2.add(card4);
        group2.add(card5);
        group2.add(card6);
        PlayingCard card7 = new PlayingCard();
        card7.setFace("6");
        card7.setSuit("d");
        PlayingCard card8 = new PlayingCard();
        card8.setFace("9");
        card8.setSuit("d");
        group3.add(card7);
        group3.add(card8);
        PlayingCard card10 = new PlayingCard();
        card10.setFace("8");
        card10.setSuit("c");
        PlayingCard card11 = new PlayingCard();
        card11.setFace("1");
        card11.setSuit("c");
        group4.add(card10);
        group4.add(card11);
        PlayingCard card12 = new PlayingCard();
        card12.setFace("3");
        card12.setSuit("s");
        PlayingCard card13 = new PlayingCard();
        card13.setFace("4");
        card13.setSuit("s");
        showView(this.mOpenCard);
        enableDeckCards();
        this.faceUpCardList = new ArrayList();
        this.faceUpCardList.add(card13);
        setOpenCard(card13);
        group5.add(card12);
        group5.add(card13);
        this.mGroupList = new ArrayList();
        this.mGroupList.add(group1);
        this.mGroupList.add(group2);
        this.mGroupList.add(group3);
        this.mGroupList.add(group4);
        this.mGroupList.add(group5);
        setGroupView(false);
    }

    private void setCardsOnIamBack(List<PlayingCard> cards) {
        if (cards != null && this.cardStack != null) {
            int i;
            List<PlayingCard> sortedCards = checkCardsWithSendDealStack(cards);
            if (sortedCards != null && sortedCards.size() > 0) {
                cards = sortedCards;
            }
            this.mGroupList = new ArrayList();
            ArrayList<Integer> slotsList = new ArrayList();
            for (i = 0; i < cards.size(); i++) {
                slotsList.add(Integer.valueOf(Integer.parseInt(((PlayingCard) cards.get(i)).getSlot())));
            }
            int maxSlot = ((Integer) Collections.max(slotsList)).intValue();
            ArrayList<PlayingCard> tempList = new ArrayList();
            for (i = 0; i <= maxSlot; i++) {
                tempList.add(i, null);
            }
            for (i = 0; i < cards.size(); i++) {
                PlayingCard card = (PlayingCard) cards.get(i);
                tempList.set(Integer.parseInt(card.getSlot()), card);
            }
            int groupCount = 1;
            for (i = 0; i < tempList.size(); i++) {
                if (tempList.get(i) == null) {
                    groupCount++;
                }
            }
            int index = 0;
            for (i = 0; i < groupCount; i++) {
                ArrayList<PlayingCard> cardList = new ArrayList();
                for (int j = index; j < tempList.size(); j++) {
                    if (tempList.get(j) == null) {
                        index++;
                        break;
                    }
                    cardList.add(tempList.get(j));
                    index++;
                }
                if (cardList.size() > 0) {
                    this.mGroupList.add(cardList);
                }
            }
            setGroupView(false);
        }
    }

    private List<PlayingCard> checkCardsWithSendDealStack(List<PlayingCard> slotCards) {
        if (slotCards.size() != this.cardStack.size()) {
            Iterator<PlayingCard> slotIter = slotCards.iterator();
            while (slotIter.hasNext()) {
                PlayingCard slotCard = (PlayingCard) slotIter.next();
                String slotFace = slotCard.getFace();
                String slotSuit = slotCard.getSuit();
                Iterator<PlayingCard> stackIter = this.cardStack.iterator();
                boolean isCardFound = false;
                while (stackIter.hasNext()) {
                    PlayingCard stackCard = (PlayingCard) stackIter.next();
                    String stackFace = stackCard.getFace();
                    String stackSuit = stackCard.getSuit();
                    if (slotFace.equalsIgnoreCase(stackFace) && slotSuit.equalsIgnoreCase(stackSuit)) {
                        isCardFound = true;
                        break;
                    }
                }
                if (!isCardFound) {
                    slotIter.remove();
                }
            }
        }
        return slotCards;
    }

    private void setGroupView(boolean isGrouping) {
        Log.d(TAG, "Inside setGroupView *********************************************************************");
        Log.d(TAG, "Inside setGroupView: CARDS SIZE: " + this.playerCards.size());

        this.mRummyView.removeViews();
        this.mRummyView.invalidate();
        this.mRummyView.setVisibility(View.VISIBLE);
        if (isGrouping) {
            setUpCardsOnGroup();
            return;
        }

        int index = 0;
        int groupCount = 0;
        Iterator it = this.mGroupList.iterator();
        while (it.hasNext()) {
            ArrayList<PlayingCard> groupList = (ArrayList) it.next();
            if (groupList.size() > 0) {
                Iterator it2 = groupList.iterator();
                while (it2.hasNext()) {
                    addCardToRummyView((PlayingCard) it2.next(), index);
                    index++;
                }
                addEmptyView();
                groupCount++;
            }
        }
        if (groupCount < 6) {
            addEmptyView();
            addEmptyView();
        }
        if (!this.isCardsDistributing) {
            sendCardsSlots();
        }
    }

    private void setUpCardsOnGroup() {
        ArrayList<ArrayList<PlayingCard>> updatedList = new ArrayList();
        ArrayList<PlayingCard> groupedList = (ArrayList) this.mGroupList.get(this.mGroupList.size() - 1);
        updatedList.add(0, groupedList);
        this.mGroupList.remove(groupedList);
        Iterator it = this.mGroupList.iterator();
        while (it.hasNext()) {
            updatedList.add((ArrayList) it.next());
        }
        this.mGroupList = updatedList;
        int index = 0;
        int groupCount = 0;
        int noOfSlots = 0;
        it = this.mGroupList.iterator();
        while (it.hasNext()) {
            ArrayList<PlayingCard> groupList = (ArrayList) it.next();
            if (groupList.size() > 0) {
                Iterator it2 = groupList.iterator();
                while (it2.hasNext()) {
                    addCardToRummyView((PlayingCard) it2.next(), index);
                    index++;
                    noOfSlots++;
                }
                noOfSlots++;
                if (noOfSlots < 23 && groupList.size() > 1) {
                    addEmptyView();
                }
                groupCount++;
            }
        }
        if (groupCount < 6) {
            addEmptyView();
            addEmptyView();
        }
        sendCardsSlots();
    }

    public void sendCardsSlots() {
        ArrayList<PlayingCard> cardsList;
        cardsList = this.mRummyView.getUpdateCardsSlots();
        TableCards table = new TableCards();
        table.setTableId(this.tableId);
        table.setCards(cardsList);
        ((BaseActivity) this.mContext).sendCardSlots(table);
    }

    private void addEmptyView() {
        this.mRummyView.addCard(this.mRummyView.getCard());
    }

    private void doShow(PlayingCard card) {
        if (this.tableId != null && card != null) {
            CardDiscard request = new CardDiscard();
            request.setEventName("SHOW");
            request.setFace(card.getFace());
            request.setSuit(card.getSuit());
            request.setUuid(Utils.generateUuid());
            request.setTableId(this.tableId);
            request.setNickName(this.userData.getNickName());
            request.setUserId(String.valueOf(this.userData.getUserId()));
            request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.showEventListner);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                TLog.d(TAG, "doShow()" + gameEngineNotRunning.getLocalizedMessage());
            }
        }
    }

    private void launchMeldFragment() {
        hideView(this.mSmartCorrectionView);
        final ArrayList<ArrayList<PlayingCard>> updatedCardGroups = getUpdatedCardGroups();
        checkMeld(updatedCardGroups);
        this.isGameResultsShowing = false;
        clearSelectedCards();
        dismissQuickMenu();
        dismissToolTipView();
        setGroupView(false);
        hideView(this.mGameResultsView);
        this.isMeldFragmentShowing = true;
        setTableButtonsUI();
        MeldCard meldCard = new MeldCard();
        meldCard.msgUuid = this.meldMsgUdid;
        meldCard.tableId = this.tableId;
        meldCard.meldGroup = updatedCardGroups;
        meldCard.dicardCard = getDiscardedCard();
        meldCard.isValidShow = this.opponentValidShow;
        meldCard.jokerCard = this.mJockerCard;
        Button mCancelBtn = (Button) this.mMeldCardsView.findViewById(R.id.meld_cancel_btn);
        ((Button) this.mMeldCardsView.findViewById(R.id.meld_yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TablesFragment.this.dismissToolTipView();
                TablesFragment.this.sendCardsToEngine(updatedCardGroups);
            }
        });
        mCancelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TablesFragment.this.removeMeldCardsFragment();
                TablesFragment.this.showDeclareHelpView();
            }
        });

        ((TextView) (this.mMeldCardsView.findViewById(R.id.table_id_tv))).setText(this.mTableId.getText().toString());
        ((TextView) (this.mMeldCardsView.findViewById(R.id.game_id_tv))).setText(this.mGameId);

        handlePopUpCloseBtn(this.mMeldCardsView);
        setMeldCardsView(this.mMeldCardsView, meldCard);
        showView(this.mSubFragment);
    }

    public ArrayList<ArrayList<PlayingCard>> getUpdatedCardGroups() {
        return this.mRummyView.getUpdatedCardsGroup();
    }

    public void sendCardsToEngine(ArrayList<ArrayList<PlayingCard>> updatedCardGroups) {
        if (this.opponentValidShow) {
            sendCardsOnDeclare(updatedCardGroups);
        } else {
            sendMeldCards(updatedCardGroups);
        }
    }

    private void handlePopUpCloseBtn(View view) {
        ((ImageView) view.findViewById(R.id.close_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                try {
                    TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                    TablesFragment.this.removeGameResultFragment();
                    TablesFragment.this.removeMeldCardsFragment();
                    TablesFragment.this.showQuickAction(TablesFragment.this.getTag());
                } catch (Exception e) {
                    Log.e("handlePopUpCloseBtn", e + "");
                }
            }
        });
    }

    private void launchGameResultsFragment(Event event) {
        hideView(this.mSmartCorrectionView);
        this.isGameResultsShowing = true;
        dismissToolTipView();
        setTableButtonsUI();
        hideView(this.mMeldCardsView);
        setGameResultsView(this.mGameResultsView, event);
        handlePopUpCloseBtn(this.mGameResultsView);
        showView(this.mSubFragment);
    }

    private void removeCardsOnMeld(ArrayList<PlayingCard> meldList) {
        for (int i = this.mGroupList.size() - 1; i >= 0; i--) {
            ArrayList<PlayingCard> cardGroup = (ArrayList) this.mGroupList.get(i);
            for (int j = cardGroup.size() - 1; j >= 0; j--) {
                PlayingCard mcard = (PlayingCard) cardGroup.get(j);
                String mCardValue = String.format("%s%s%s", new Object[]{mcard.getSuit(), mcard.getFace(), mcard.getIndex()});
                for (int x = meldList.size() - 1; x >= 0; x--) {
                    PlayingCard selectedCard = (PlayingCard) meldList.get(x);
                    if (String.format("%s%s%s", new Object[]{selectedCard.getSuit(), selectedCard.getFace(), selectedCard.getIndex()}).equalsIgnoreCase(mCardValue)) {
                        cardGroup.remove(j);
                    }
                }
            }
        }
        setGroupView(false);
    }

    public void updateGroupView(ArrayList<ArrayList<PlayingCard>> updatedGroiupList) {
        this.mGroupList.clear();
        this.mGroupList.addAll(updatedGroiupList);
        setGroupView(false);
    }

    private void removeDiscardedCardOnShow(PlayingCard discardedCard) {
        this.mSelectedCards.clear();
        String mCardValue = String.format("%s%s-%s", new Object[]{discardedCard.getSuit(), discardedCard.getFace(), discardedCard.getIndex()});

        PlayingCard card;
        for (int i = 0; i < this.mGroupList.size(); i++) {
            for (int k = 0; k < this.mGroupList.get(i).size(); k++) {
                card = this.mGroupList.get(i).get(k);

                if (String.format("%s%s-%s", new Object[]{card.getSuit(), card.getFace(), card.getIndex()}).equalsIgnoreCase(mCardValue)) {
                    try {
                        this.mGroupList.get(i).remove(discardedCard);
                    } catch (Exception e) {
                        Log.e("flow", "EXP: TablesFrag : removeDiscardedCardOnShow -->> " + e.toString());
                    }
                    break;
                }
            }
        }

        this.tempDiscardedCard = discardedCard;
        setGroupView(true);
    }

    private void showPlaceShowDialog(View v) {
        disableView(this.mDropPlayer);
        this.showDialog = getLeaveTableDialog(v.getContext(), this.mContext.getString(R.string.place_show_msg));
        ((Button) this.showDialog.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.showDialog.dismiss();
                TablesFragment.this.groupCards();
                TablesFragment.this.clearSelectedCards();
                TablesFragment.this.dismissQuickMenu();
                TablesFragment.this.showHideView(false, TablesFragment.this.mShowBtn, true);
                TablesFragment.this.showHideView(true, TablesFragment.this.mDeclareBtn, false);
                TablesFragment.this.disableView(TablesFragment.this.mDropPlayer);
                PlayingCard discardedCard = TablesFragment.this.getDiscardedCard();
                TablesFragment.this.mClosedCard.setVisibility(View.VISIBLE);
                TablesFragment.this.doShow(discardedCard);
                TablesFragment.this.removeDiscardedCardOnShow(discardedCard);
            }
        });
        ((Button) this.showDialog.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.showHideView(true, TablesFragment.this.mShowBtn, false);
                TablesFragment.this.enableView(TablesFragment.this.mShowBtn);
                TablesFragment.this.showDialog.dismiss();
            }
        });
        this.showDialog.show();
    }

    private void showDropDialog() {
        this.dropDialog = getLeaveTableDialog(this.mContext, this.mContext.getString(R.string.drop_game_msg));
        ((Button) this.dropDialog.findViewById(R.id.yes_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.dropDialog.dismiss();
                TablesFragment.this.dropPlayer();
            }
        });
        ((Button) this.dropDialog.findViewById(R.id.no_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TablesFragment.this.dropDialog.dismiss();
                TablesFragment.this.enableView(TablesFragment.this.mDropPlayer);
            }
        });
        this.dropDialog.show();
    }

    public void searchTable(Event event) {
        if (this.mTableDetails != null) {
            SearchTableRequest request = new SearchTableRequest();
            request.setCommand("search_join_table");
            request.setTableId(event.getTableId());
            request.setUuid(event.getMsg_uuid());
            request.setBet(this.mTableDetails.getBet());
            request.setUserId(this.userData.getUserId());
            request.setTableType(this.mTableDetails.getTableType());
            request.setTableCost(this.mTableDetails.getTableCost());
            request.setMaxPlayers(this.mTableDetails.getMaxPlayer());
            request.setConversion(this.mTableDetails.getConversion());
            request.setStreamId(this.mTableDetails.getStreamid());
            request.setStreamName(this.mTableDetails.getStreamname());
            request.setGamesettingsId(this.mTableDetails.getGamesettingid());
            request.setNickName(this.userData.getNickName());
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.searchTableResponse);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.d(TAG, "getTableData" + gameEngineNotRunning.getLocalizedMessage());
            }
        }
    }

    private void resetAllPlayers() {
        resetPlayerData(this.mUserPlayerLayout);
        resetPlayerData(this.mSecondPlayerLayout);
        resetPlayerData(this.mThirdPlayerLayout);
        resetPlayerData(this.mFourthPlayerLayout);
        resetPlayerData(this.mFifthPlayerLayout);
        resetPlayerData(this.mSixthPlayerLayout);
        invisibleView(this.mPlayer2Cards);
        invisibleView(this.mPlayer3Cards);
        invisibleView(this.mPlayer4Cards);
        invisibleView(this.mPlayer5Cards);
        invisibleView(this.mPlayer6Cards);

        invisibleView(this.player_2_autoplay_box);
        invisibleView(this.player_3_autoplay_box);
        invisibleView(this.player_4_autoplay_box);
        invisibleView(this.player_5_autoplay_box);
        invisibleView(this.player_6_autoplay_box);
    }

    private void resetOtherPlayers() {
        resetPlayerData(this.mSecondPlayerLayout);
        resetPlayerData(this.mThirdPlayerLayout);
        resetPlayerData(this.mFourthPlayerLayout);
        resetPlayerData(this.mFifthPlayerLayout);
        resetPlayerData(this.mSixthPlayerLayout);
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    private void sendCardsOnDeclare(ArrayList<ArrayList<PlayingCard>> meldGroup) {
        List<MeldBox> mMeldBoxes = new ArrayList();
        Iterator it = meldGroup.iterator();
        while (it.hasNext()) {
            ArrayList<PlayingCard> meldCards = (ArrayList) it.next();
            MeldBox box = new MeldBox();
            box.setMeldBoxes(meldCards);
            mMeldBoxes.add(box);
        }
        MeldReply request = new MeldReply();
        request.setText("200");
        request.setType("+OK");
        request.setUuid(this.meldMsgUdid);
        request.setTableId(this.tableId);
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        request.setMeldBoxes(mMeldBoxes);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.declareListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void sendMeldCards(ArrayList<ArrayList<PlayingCard>> meldGroup) {
        if (this.mDiscardedCard != null) {
            List<MeldBox> mMeldBoxes = new ArrayList();
            Iterator it = meldGroup.iterator();
            while (it.hasNext()) {
                ArrayList<PlayingCard> meldCards = (ArrayList) it.next();
                MeldBox box = new MeldBox();
                box.setMeldBoxes(meldCards);
                mMeldBoxes.add(box);
            }
            MeldRequest request = new MeldRequest();
            request.setCommand("meld");
            request.setMeldBoxes(mMeldBoxes);
            request.setFace(this.mDiscardedCard.getFace());
            request.setSuit(this.mDiscardedCard.getSuit());
            request.setUuid(Utils.generateUuid());
            request.setTableId(this.tableId);
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.meldListner);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
            }
        }
    }

    private void handleBackButton(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != 4) {
                    return false;
                }
                if (TablesFragment.this.mDialogLayout.getVisibility() == View.VISIBLE) {
                    TablesFragment.this.hideView(TablesFragment.this.mDialogLayout);
                    return true;
                } else if (TablesFragment.this.mSubFragment.getVisibility() != View.VISIBLE) {
                    return false;
                } else {
                    if (!((TableActivity) TablesFragment.this.mContext).isIamBackShowing()) {
                        ((TableActivity) TablesFragment.this.mContext).showGameTablesLayout(TablesFragment.this.tableId);
                    }
                    TablesFragment.this.hideView(TablesFragment.this.mSubFragment);
                    return true;
                }
            }
        });
    }

    private void getTableExtra() {
        if (this.tableId != null) {
            TableDetailsRequest request = new TableDetailsRequest();
            request.setCommand("get_table_extra");
            request.setTableId(this.tableId);
            request.setUuid(Utils.generateUuid());
            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.tableExtraLisner);
            } catch (GameEngineNotRunning e) {
                Toast.makeText(this.mContext, R.string.error_restart, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatedStacks(PickDiscard pickDiscard) {
        if (pickDiscard.getDeck().equalsIgnoreCase("face_down")) {
            Iterator it = this.faceDownCardList.iterator();
            while (it.hasNext()) {
                PlayingCard card = (PlayingCard) it.next();
                if (card.getFace().equalsIgnoreCase(pickDiscard.getFace()) && card.getSuit().equalsIgnoreCase(pickDiscard.getSuit())) {
                    this.faceDownCardList.remove(card);
                    return;
                }
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mDrawerLayout.closeDrawer(5);
        switch (position) {
            case 1:
                ((TableActivity) this.mContext).setUpGameSettings();
                return;
            case 2:
                ((TableActivity) this.mContext).setUpPlayerDiscards();
                return;
            case 3:
                ((TableActivity) this.mContext).setLastHandEvent();
                return;
            case 4:
                ((TableActivity) this.mContext).showScoreBoardView();
                return;
            case 5:
                ((TableActivity) this.mContext).setGameInfo();
                ((TableActivity) this.mContext).showGameInfo();
                return;
            case 6:
                ((TableActivity) this.mContext).setGameInfo();
                ((TableActivity) this.mContext).setReportProblem();
                return;
            default:
                return;
        }
    }

    public void showLastGameResult(Event lastHandEvent) {
        if (lastHandEvent != null) {
            launchGameResultsFragment(lastHandEvent);
        } else {
            showGenericDialog(this.mContext, "No entries found.");
        }
    }

    public boolean canShowGameButtons() {
        if (this.mSubFragment.getVisibility() == View.VISIBLE) {
            return false;
        }
        return true;
    }

    private void animateCards(int position, final Event event) {
        if (((TableActivity) this.mContext).isFromIamBack()) {
            handleSendDealEvent(event);
        } else if (position > this.mDummyVies.size() - 1) {
            handleSendDealEvent(event);
        } else {
            final ImageView iv = (ImageView) this.mDummyVies.get(position);
            iv.setVisibility(View.VISIBLE);
            TransferAnimation an = new TransferAnimation(iv);
            an.setDuration(50);
            an.setListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    TablesFragment.this.hideView(TablesFragment.this.mUserTossCard);
                    LinearLayout linearLayout = TablesFragment.this.mRummyView.getCard();
                    ((ImageView) linearLayout.findViewById(R.id.cardImageView)).setImageResource(R.drawable.closedcard);
                    TablesFragment.this.hideView(iv);

                    TablesFragment.this.mRummyView.addCard(linearLayout);
                    if (TablesFragment.this.count < TablesFragment.this.mDummyVies.size()) {
                        TablesFragment.this.count++;
                        TablesFragment.this.animateCards(TablesFragment.this.count, event);
                        return;
                    }
                    TablesFragment.this.count = 0;
                    TablesFragment.this.clearAnimationData();
                    TablesFragment.this.handleSendDealEvent(event);
                }
            });
            an.setDestinationView(this.mUserTossCard).animate();
        }
    }

    @Subscribe
    public void onMessageEvent(GameEvent event) {
        if (event.name().equalsIgnoreCase("SERVER_DISCONNECTED")) {
            cancelTimers();
        }
    }

    public void cancelTimers() {
        hideQuickAction();
        cancelTimer(this.playerTurnOutTimer);
        cancelTimer(this.mGameScheduleTimer);
        cancelTimer(this.meldTimer);
    }

    private void setUpPlayerCardsUI(GamePlayer player) {
        String seat;
        if (this.mGamePlayerMap.get(player.getUser_id()) != null) {
            player.setPlayerlevel(((GamePlayer) this.mGamePlayerMap.get(player.getUser_id())).getPlayerlevel());
        }
        if (player.getSeat() != null) {
            seat = player.getSeat();
        } else {
            seat = "1";
        }
        switch (Integer.parseInt(seat)) {
            case 2:
                showView(this.mPlayer2Cards);
                return;
            case 3:
                showView(this.mPlayer3Cards);
                return;
            case 4:
                showView(this.mPlayer4Cards);
                return;
            case 5:
                showView(this.mPlayer5Cards);
                return;
            case 6:
                showView(this.mPlayer6Cards);
                return;
            default:
                return;
        }
    }

    private void sendTurnUpdateMessage(boolean isYoursTurn) {
        try {
            Intent intent = new Intent("TURN_UPDATE_EVENT");
            intent.putExtra("turn_update", isYoursTurn);
            LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
        } catch (Exception e) {
            TLog.e(TAG, "Error : sendTurnUpdateMessage() :" + e.getLocalizedMessage());
        }
    }

    private void checkMeld(ArrayList<ArrayList<PlayingCard>> meldGroup) {
        Log.d(TAG, "Inside checkMeld************************************");
        meldGroup = checkDiscardedCardIfPresent(meldGroup);

        List<MeldBox> mMeldBoxes = new ArrayList();
        Iterator it = meldGroup.iterator();
        while (it.hasNext()) {
            ArrayList<PlayingCard> meldCards = (ArrayList) it.next();
            MeldBox box = new MeldBox();
            box.setMeldBoxes(meldCards);
            mMeldBoxes.add(box);
        }
        MeldRequest request = new MeldRequest();
        request.setCommand("check_meld");
        request.setMeldBoxes(mMeldBoxes);
        request.setFace("null");
        request.setSuit("null");
        request.setUuid(Utils.generateUuid());
        request.setTableId(this.tableId);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.checkMeldListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "doMelds" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private ArrayList<ArrayList<PlayingCard>> checkDiscardedCardIfPresent(ArrayList<ArrayList<PlayingCard>> meldGroup) {
        try {
            int flag = 0;
            String discard = this.mDiscardedCard.getFace() + this.mDiscardedCard.getSuit();

            for (int i = 0; i < meldGroup.size(); i++) {
                for (int k = 0; k < meldGroup.get(i).size(); k++) {
                    flag++;
                }
            }

            Log.d("local", "Total cards: " + flag);

            if (flag > 13) {
                for (int i = 0; i < meldGroup.size(); i++) {
                    for (int k = 0; k < meldGroup.get(i).size(); k++) {
                        flag++;
                        if (discard.equalsIgnoreCase(meldGroup.get(i).get(k).getFace() + meldGroup.get(i).get(k).getSuit())) {
                            if (meldGroup.get(i).size() > 1)
                                meldGroup.get(i).remove(k);
                            else if (meldGroup.get(i).size() == 1)
                                meldGroup.remove(i);

                            return meldGroup;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: checkDiscardedCardIfPresent(TablesFrag) -->> " + e.toString());
        }
        return meldGroup;
    }


    // TOURNAMENT METHODS //////////////////////////////////////////////////////////////////////////////////

    private void checkGameType() {
        try {
            if (getArguments().getString("gameType") != null && getArguments().getString("gameType").equalsIgnoreCase("tournament")) {
                levelTimerLayout.setVisibility(View.VISIBLE);
                normal_game_bar.setVisibility(View.GONE);
                tourneyBar.setVisibility(View.VISIBLE);

                tourney_expanded_layout.post(new Runnable() {
                    @Override
                    public void run() {
                        tourneyInfoMaxHeight = tourney_expanded_layout.getHeight();
                        animateTourneyInfo(false);
                    }
                });

                if (getArguments().getString("tourneyId") != null)
                    this.mTourneyId = getArguments().getString("tourneyId");

                Log.d(TAG, "Tourney ID: " + this.mTourneyId);
                this.tid_tourney_tv.setText(this.mTourneyId);
            } else {
                levelTimerLayout.setVisibility(View.GONE);
                normal_game_bar.setVisibility(View.VISIBLE);
                tourneyBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: checkGameType-->> " + e.toString());
        }
    }

    private void checkTournament() {
        try {
            if (this.strIsTourneyTable.equalsIgnoreCase("yes")) {
                levelTimerLayout.setVisibility(View.VISIBLE);
                normal_game_bar.setVisibility(View.GONE);
                tourneyBar.setVisibility(View.VISIBLE);

                tourney_expanded_layout.post(new Runnable() {
                    @Override
                    public void run() {
                        tourneyInfoMaxHeight = tourney_expanded_layout.getHeight();
                        animateTourneyInfo(false);
                    }
                });

                Log.d(TAG, "Tourney ID: " + this.mTourneyId);
                this.tid_tourney_tv.setText(this.mTourneyId);
            } else {
                levelTimerLayout.setVisibility(View.GONE);
                normal_game_bar.setVisibility(View.VISIBLE);
                tourneyBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: checkGameType-->> " + e.toString());
        }
    }

    private void setLevelTimer() {
        try {
            if (this.levelTimer != null)
                this.levelTimer.cancel();

            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss aa");
            level_number_tv.setText(this.mLevelsResponse.getLeveldetails());

            if (this.mLevelsResponse.getEntry().equalsIgnoreCase("0") || this.mLevelsResponse.getEntry().equalsIgnoreCase("0.0")) {
                this.tourney_type_tv.setText("FREE TOURNEY");
                this.entry_tourney_tv.setText("Free");
            } else {
                this.tourney_type_tv.setText("CASH TOURNEY");
                this.entry_tourney_tv.setText(this.mLevelsResponse.getEntry());
            }

            if (this.mLevelsResponse.getFinalprize().equalsIgnoreCase("true"))
                this.tourney_prize_tv.setText(this.mLevelsResponse.getPrize());
            else
                this.tourney_prize_tv.setText("TBA");

            this.rebuy_tourney_tv.setText(this.mLevelsResponse.getNextlevelrebuyin());
            this.game_level_tv.setText(this.mLevelsResponse.getCurrentlevelminimumchips() + "/" + this.mLevelsResponse.getNextlevelminchips());

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

                    levelTimerValue.setText(strMins + ":" + strSeconds);
                }

                public void onFinish() {
                    //Log.d("local","done!");
                }

            }.start();
        } catch (Exception e) {
            Log.e(TAG, "EXP: setLevelTimer-->> " + e.toString());
        }
    }

    private void getTournamentDetails() {
        try {
            Log.d(TAG, "Getting Tournament details");
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

    private void updatePlayersRank() {
        try {
            for (int i = 0; i < this.mPlayerBoxesAll.size(); i++) {
                ((TextView) (this.mPlayerBoxesAll.get(i).findViewById(R.id.player_rank_tv))).setText("");
                ((TextView) (this.mPlayerBoxesAll.get(i).findViewById(R.id.player_rank_tv))).setVisibility(View.GONE);
            }

            if (this.strIsTourneyTable.equalsIgnoreCase("yes") && this.mPlayersRank.getTournamentId().equalsIgnoreCase(this.mTourneyId)) {
                for (int i = 0; i < this.mPlayersList.size(); i++) {
                    if (this.mPlayerBoxesForRanks.size() > 0)
                        setUpPlayerRank(this.mPlayerBoxesForRanks.get(this.mPlayersList.get(i).getNick_name()), this.mPlayersList.get(i));

                    if (this.mPlayersList.get(i).getNick_name().equalsIgnoreCase(this.userData.getNickName()))
                        this.rank_tourney_tv.setText(this.mPlayersList.get(i).getRank());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: updatePlayersRank-->> " + e.toString());
        }
    }

    private void checkTourneyBalance() {
        try {
            Iterator it = Utils.tableDetailsList.iterator();
            while (it.hasNext()) {
                Event event = (Event) it.next();
                if (event.getEventName().equalsIgnoreCase("TOURNEY_BALANCE")) {
                    List<Tourney> listT = event.getTourneys();

                    for (Tourney t : listT) {
                        if (t.getTournament_id().equalsIgnoreCase(this.mTourneyId))
                            this.balance_tourney_tv.setText(t.getTourney_inplay());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: checkTourneyBalance-->> " + e.toString());
        }
    }

    private void showGenericDialogWithMessage(String message, final String action) {
        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_generic);
        dialog.setCanceledOnTouchOutside(false);

        TextView dialog_msg_tv = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);

        dialog_msg_tv.setText(message);

        if (action.equalsIgnoreCase("disqualified")) {
            TablesFragment.this.disqualifyDialog = dialog;
        }
        ok_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void displayTourneyResults(List<GamePlayer> tourneyResults) {
        final Dialog dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tournament_results);
        dialog.setCanceledOnTouchOutside(true);

        ListView tournament_results = (ListView) dialog.findViewById(R.id.tournament_results);
        ImageView popUpCloseBtn = (ImageView) dialog.findViewById(R.id.popUpCloseBtn);
        popUpCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TourneyResultsAdapter tournamentsResultsAdapter = new TourneyResultsAdapter(getContext(), tourneyResults);
        tournament_results.setAdapter(tournamentsResultsAdapter);
        if (tournamentsResultsAdapter != null) {
            tournamentsResultsAdapter.notifyDataSetChanged();
        }
        dialog.show();
    }

    private void removePlayerLevelFromBox(String nickName) {
        String name = "";
        for (int i = 0; i < this.mPlayerBoxesAll.size(); i++) {
            name = ((TextView) (this.mPlayerBoxesAll.get(i).findViewById(R.id.player_name_tv))).getText().toString();

            if (name.equalsIgnoreCase(nickName)) {
                ((TextView) (this.mPlayerBoxesAll.get(i).findViewById(R.id.player_rank_tv))).setText("");
                ((TextView) (this.mPlayerBoxesAll.get(i).findViewById(R.id.player_rank_tv))).setVisibility(View.GONE);
                break;
            }
        }
    }

    private void showRebuyinDialog(final Event event) {
        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_leave_table);
        dialog.setCanceledOnTouchOutside(false);

        final TextView dialog_msg_tv = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        Button yes_btn = (Button) dialog.findViewById(R.id.yes_btn);
        Button no_btn = (Button) dialog.findViewById(R.id.no_btn);

        final String message = "As you do not have sufficient chips for next game, you have to rebuy. " + event.getBuyin_amount() + " cash chips will be deducted. Please click YES to rebuy.";

        long millis = Integer.parseInt(event.getMeldTimeOut()) * 1000;

        this.levelTimer = new CountDownTimer(millis, 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                dialog_msg_tv.setText(message + "\nYou have " + seconds + " seconds left.");
            }

            public void onFinish() {
                clearData();
                clearSelectedCards();
                clearStacks();
                dialog.dismiss();
                TablesFragment.this.isTourneyEnd = true;
            }

        }.start();

        yes_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doRebuyin(event);

                if (TablesFragment.this.disqualifyDialog != null) {
                    TablesFragment.this.disqualifyDialog.dismiss();
                }
                dialog.dismiss();
            }
        });

        no_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();
                clearSelectedCards();
                clearStacks();
                dialog.dismiss();
                TablesFragment.this.isTourneyEnd = true;
            }
        });

        dialog.show();
    }

    private void doRebuyin(Event event) {
        try {
            TournamentsDetailsRequest request = new TournamentsDetailsRequest();
            request.setCommand("tournament_rebuyin");
            request.setUuid(Utils.generateUuid());
            request.setAmount(event.getValue_rake());
            request.setLevel(event.getTo_level());
            request.setTournament_id(this.mTourneyId);

            try {
                GameEngine.getInstance();
                GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.rebuyinListener);
            } catch (GameEngineNotRunning gameEngineNotRunning) {
                Toast.makeText(this.mContext.getApplicationContext(), R.string.error_restart, Toast.LENGTH_SHORT).show();
                TLog.e(TAG, "doRebuyin" + gameEngineNotRunning.getLocalizedMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "EXP: doRebuyin-->> " + e.toString());
        }
    }

    private void leaveTournament() {
        if (((RummyApplication) this.mContext.getApplication()).getJoinedTableIds().size() == 1) {
            ((TableActivity) this.mContext).setIsBackPressed(true);
        }
        showLoadingDialog(this.mContext);
        LeaveTableRequest request = new LeaveTableRequest();
        request.setEventName("leave_tournament");
        request.setUuid(Utils.generateUuid());
        request.setTournament_id(this.mTourneyId);
        try {
            GameEngine.getInstance();
            GameEngine.sendRequestToEngine(this.mContext.getApplicationContext(), Utils.getObjXMl(request), this.leaveTableListner);
        } catch (GameEngineNotRunning gameEngineNotRunning) {
            TLog.d(TAG, "leave_tournament" + gameEngineNotRunning.getLocalizedMessage());
        }
    }

    private void doMeldTemp(EngineRequest engineRequest) {
        showView(this.mDropPlayer);

        ((TableActivity) this.mContext).closeSettingsMenu();
        String successUserId = engineRequest.getSucessUserId();
        String successUserName = engineRequest.getSucessUserName();
        if (successUserId != null && successUserName != null) {
            if (successUserId.equalsIgnoreCase(this.userData.getUserId())) {
                showView(this.mClosedCard);
                setUserOptionsOnValidShow();
                return;
            }
            showView(this.mClosedCard);
            this.meldTimeOut = engineRequest.getTimeout();
            this.opponentValidShow = true;
            SoundPoolManager.getInstance().playSound(R.raw.meld);
            ((TableActivity) this.mContext).dismissScoreBoard();
            removeGameResultFragment();
            this.canLeaveTable = false;
            this.isPlacedShow = true;
            this.isTossEventRunning = false;
            this.isCardsDistributing = false;
            this.meldMsgUdid = engineRequest.getMsg_uuid();
            startMeldTimer(Integer.parseInt(Utils.formatString(engineRequest.getTimeout())), String.format("%s placed valid show , %s", new Object[]{successUserName, this.mContext.getString(R.string.meld_success_msg)}), this.mGameShecduleTv);
            if (getTotalCards() > 0) {
                invisibleView(this.mShowBtn);
                disableView(this.mShowBtn);
                showView(this.mDeclareBtn);
                enableView(this.mDeclareBtn);
                if (!((TableActivity) this.mContext).isIamBackShowing()) {
                    showDeclareHelpView();
                    return;
                }
                return;
            }
            //invisibleView(this.mDeclareBtn);      // Before
            showView(this.mDeclareBtn);             // After
            showView(this.mShowBtn);
            disableView(this.mShowBtn);
            disableView(this.mDropPlayer);
            disableView(this.sortCards);
        }

        Utils.MELD_REQUEST = null;
    }

    private void doShowTemp(Event event) {
        showView(this.mDropPlayer);
        showView(this.sortCards);
        showView(this.mShowBtn);

        Log.d(TAG, "Inside SHOW************************************");
        hideView(this.mReshuffleView);
        sendTurnUpdateMessage(false);
        ((TableActivity) this.mContext).closeSettingsMenu();
        if (this.strIsTourneyTable.equalsIgnoreCase("yes") && this.playerCards.size() == 0)
            clearData();
        else
            handleShowEvent(event);

        Utils.SHOW_EVENT = null;
    }

    private void showInitialButtons() {
        Log.e(TAG, "SHOWING INITIAL BUTTONS ++++++++++++++++++++++");
        showView(sortCards);
        showView(mDropPlayer);
        showView(mShowBtn);
        hideView(mDeclareBtn);
    }
}
