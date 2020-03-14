package in.glg.rummy.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.activities.HomeActivity;
import in.glg.rummy.adapter.CircularAdapter;
import in.glg.rummy.adapter.GameTypeAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.UrlBuilder;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.fancycoverflow.FancyCoverFlow;
import in.glg.rummy.models.BaseTrRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.utils.ErrorCodes;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;


public class HomeFragmentScroller extends BaseFragment implements OnItemClickListener, OnClickListener, OnCheckedChangeListener {
   private static final String TAG = HomeFragmentScroller.class.getSimpleName();
   private OnResponseListener chipLoadListner = new OnResponseListener(LoginResponse.class) {
      public void onResponse(Object response) {
         if (response != null) {
            LoginResponse loginResponse = (LoginResponse) response;
            String message = "";
            int code = Integer.parseInt(loginResponse.getCode());
            if (code == ErrorCodes.SUCCESS) {
               HomeFragmentScroller.this.mFunChips.setText(loginResponse.getFunChips());
               ((RummyApplication) HomeFragmentScroller.this.getActivity().getApplication()).getUserData().setFunChips(loginResponse.getFunChips());
               message = String.format("%s %s %s", new Object[]{HomeFragmentScroller.this.mContext.getString(R.string.chips_reload_success_msg), loginResponse.getFunChips(), HomeFragmentScroller.this.mContext.getString(R.string.lobby_chips_title).toLowerCase()});
            } else if (code == ErrorCodes.PLAYER_HAS_CHIPS_MORE_THAN_MINIMUN) {
               message = String.format("%s %s %s", new Object[]{HomeFragmentScroller.this.mContext.getString(R.string.balance_reload_err_msg), loginResponse.getMinToReload(), HomeFragmentScroller.this.mContext.getString(R.string.lobby_chips_title).toLowerCase()});
            }
            HomeFragmentScroller.this.showGenericDialog(HomeFragmentScroller.this.mContext, message);
         }
      }
   };
   private FancyCoverFlow fancyCoverFlow;
   private Button mBuyChipsBtn;
   private ImageView mChipRelodBtn;
   private FragmentActivity mContext;
   private TextView mFunChips;
   private TextView mFunInPlay;
   private ImageView mLeftArrow;
   private RadioGroup mRadioGroup;
   private TextView mRealChips;
   private TextView mRealInPlay;
   private ImageView mRightArrow;
   private TextView mUser;
   private TextView mVersionTv;
   private static String TOKEN = "";

   private void setBuildVersion() {
      this.mVersionTv.setText("" + Utils.getVersionNumber(this.getContext()));
   }

   private void setIdsToViews(View v) {
      this.mChipRelodBtn = (ImageView) v.findViewById(R.id.reload_chips_btn);
      this.mUser = (TextView) v.findViewById(R.id.user_name_tv);
      this.mRealChips = (TextView) v.findViewById(R.id.user_real_chips_value_tv);
      this.mRealInPlay = (TextView) v.findViewById(R.id.inplay_value_tv);
      this.mFunChips = (TextView) v.findViewById(R.id.user_fun_chips_tv);
      this.mFunInPlay = (TextView) v.findViewById(R.id.inplay_fun_tv);
      this.mRightArrow = (ImageView) v.findViewById(R.id.right_arrow_btn);
      this.mLeftArrow = (ImageView) v.findViewById(R.id.left_arrow_btn);
      this.mBuyChipsBtn = (Button) v.findViewById(R.id.buyChipsBtn);
      this.mVersionTv = (TextView) v.findViewById(R.id.build_version_tv);
      this.mRightArrow.setOnClickListener(this);
      this.mLeftArrow.setOnClickListener(this);
      this.mBuyChipsBtn.setOnClickListener(this);
      int tableCost = PrefManager.getInt(getContext(), "tableCost", 0);
      this.mRadioGroup = (RadioGroup) v.findViewById(R.id.pre_lobby_tab_group);
      this.mRadioGroup.setOnCheckedChangeListener(this);
      if (tableCost == 1) {
         this.mRadioGroup.check(R.id.cash_games_btn);
      } else {
         this.mRadioGroup.check(R.id.free_games_btn);
      }
      RadioButton cashBtn = (RadioButton) this.mRadioGroup.findViewById(R.id.cash_games_btn);
      RadioButton freeBtn = (RadioButton) this.mRadioGroup.findViewById(R.id.free_games_btn);
      Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
      cashBtn.setTypeface(font);
      freeBtn.setTypeface(font);
   }

   private void setUpGameTypeView(View v) {
      this.fancyCoverFlow = (FancyCoverFlow) v.findViewById(R.id.fancyCoverFlow);
      this.fancyCoverFlow.setAdapter(new CircularAdapter(new GameTypeAdapter(getActivity())));
      this.fancyCoverFlow.setMaxRotation(0);
      this.fancyCoverFlow.setScaleDownGravity(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);
      this.fancyCoverFlow.setUnselectedScale(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);
      this.fancyCoverFlow.setSelection(1000);
      this.fancyCoverFlow.setOnItemClickListener(this);
      this.mChipRelodBtn.setOnClickListener(this);
   }

   private void setUserDetails(LoginResponse userData) {
      if (userData != null) {
         String rupeeSymbol = this.mContext.getString(R.string.rupee_text);
         this.mUser.setText(userData.getNickName());
         String funChips = userData.getFunChips();
         if (funChips == null || funChips.length() <= 0) {
            funChips = "0";
         }
         String realChips = userData.getRealChips();
         if (realChips == null || realChips.length() <= 0) {
            realChips = String.format("%s %s", new Object[]{rupeeSymbol, "0"});
         } else {
            realChips = String.format("%s %s", new Object[]{rupeeSymbol, realChips});
         }
         Spannable realChipsStr = new SpannableString(realChips);
         realChipsStr.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this.mContext, R.color.white)), 0, 1, 33);
         String funInPlay = userData.getFunInPlay();
         if (funInPlay == null || funInPlay.length() <= 0) {
            funInPlay = "0";
         }
         String realInPlay = userData.getRealInPlay();
         if (realInPlay == null || realInPlay.length() <= 0) {
            realInPlay = String.format("%s %s", new Object[]{rupeeSymbol, "0"});
         } else {
            realInPlay = String.format("%s %s", new Object[]{rupeeSymbol, realInPlay});
         }
         Spannable realInPlayStr = new SpannableString(realInPlay);
         realInPlayStr.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this.mContext, R.color.white)), 0, 1, 33);
         this.mRealChips.setText(realChipsStr);
         this.mRealInPlay.setText(realInPlayStr);
         this.mFunChips.setText(funChips);
         this.mFunInPlay.setText(funInPlay);

         Crashlytics.setString("real_chips", realChipsStr+"");
         Crashlytics.setString("real_inplay", realInPlayStr+"");
         Crashlytics.setString("fun_chips", funChips+"");
         Crashlytics.setString("fun_inplay", funInPlay+"");

         this.mRealChips.setMovementMethod(new ScrollingMovementMethod());
         this.mRealInPlay.setMovementMethod(new ScrollingMovementMethod());
         this.mFunChips.setMovementMethod(new ScrollingMovementMethod());
         this.mFunInPlay.setMovementMethod(new ScrollingMovementMethod());
      }
   }

   private void showBuyChipsPopUp() {
      final Dialog buyChipsDialog = new Dialog(getContext());
      buyChipsDialog.requestWindowFeature(1);
      buyChipsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.black_trans85)));
      buyChipsDialog.setContentView(R.layout.dialog_buy_chips);
      final ProgressBar webPb = (ProgressBar) buyChipsDialog.findViewById(R.id.web_pb);
      ((ImageView) buyChipsDialog.findViewById(R.id.pop_up_close_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View view) {
            buyChipsDialog.dismiss();
         }
      });
      final WebView webView = (WebView) buyChipsDialog.findViewById(R.id.webView);
      webView.setWebChromeClient(new WebChromeClient() {
         public void onProgressChanged(WebView view, int progress) {
            if (progress == 100) {
               webPb.setVisibility(View.GONE);
               webView.setVisibility(View.VISIBLE);
            }
         }
      });
      webView.getSettings().setJavaScriptEnabled(true);
      webView.getSettings().setBuiltInZoomControls(true);
      webView.getSettings().setSupportZoom(true);
      webView.setWebViewClient(new WebViewController());
      webView.loadUrl(UrlBuilder.BASE_URL);
      buyChipsDialog.show();
   }

   public void loadChips() {
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

   public void onCheckedChanged(RadioGroup radioGroup, int checkedItemId) {
      RadioButton cashBtn = (RadioButton) radioGroup.findViewById(R.id.cash_games_btn);
      RadioButton freeBtn = (RadioButton) radioGroup.findViewById(R.id.free_games_btn);
      cashBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.White));
      freeBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.White));

      if(checkedItemId == R.id.cash_games_btn)
      {
         cashBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.highlight_font_scroller));
         PrefManager.saveInt(getContext(), "tableCost", 1);
      }
      else if(checkedItemId == R.id.free_games_btn)
      {
         freeBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.highlight_font_scroller));
         PrefManager.saveInt(getContext(), "tableCost", 0);
      }

   }

   public void onClick(View v) {

      if(v.getId() == R.id.buyChipsBtn)
      {
         //showGenericDialog(getContext(), String.format("%s - %s", new Object[]{"To buy Real Chips, please deposit from website", Utils.getWebSite()}));
         //checkPlayerDeposit(getContext());
      }
      else if(v.getId() == R.id.reload_chips_btn)
      {
         loadChips();
      }
      else if(v.getId() == R.id.left_arrow_btn)
      {
         this.fancyCoverFlow.onKeyDown(22, null);
      }
      else if(v.getId() == R.id.right_arrow_btn)
      {
         this.fancyCoverFlow.onKeyDown(21, null);
      }

   }

   private void openBrowserToBuyChips()
   {
      String url = Utils.SERVER_ADDRESS +"sendpaymentrequest/?client_type=" + Utils.CLIENT_TYPE+"&device_type="+getDeviceType();
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      Bundle bundle = new Bundle();
      Log.d(TAG, "TOKEN: "+TOKEN);
      bundle.putString("Authorization", "Token "+TOKEN);
      i.putExtra(Browser.EXTRA_HEADERS, bundle);
      startActivity(i);
   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View v = inflater.inflate(R.layout.fragment_home_scroller, container, false);
      try {
         this.mContext = getActivity();
         LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();

         Crashlytics.setUserIdentifier(userData.getUserId());
         Crashlytics.setString("user_id", userData.getUserId());
//      Crashlytics.setString("user_email", userData.getUserEmail());
         Crashlytics.setString("user_name", userData.getNickName());

         setIdsToViews(v);
         setUpGameTypeView(v);
         setUserDetails(userData);
         setBuildVersion();
         this.TOKEN = PrefManager.getString(getContext(), ACCESS_TOKEN_REST, "");
      } catch (Exception e) {
         e.printStackTrace();
         Log.e(TAG, e + "");
      }

      return v;
   }

   public void onDestroy() {
      super.onDestroy();
      if(EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().unregister(this);
         Log.d("flow", "onDestroy: Deregister HF");
      }

   }

   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      String variantType;
      int itemId = ((Integer) parent.getAdapter().getItem(position)).intValue();
      Log.e("itemId",itemId+"");
      if (itemId == R.drawable.strikes_rummy) {
         variantType = "Strikes";
      } else if (itemId == R.drawable.pools_rummy) {
         variantType = "Pools";
      } else if (itemId == R.drawable.tourney_prelobby) {
         variantType = "Tourneys";
         ((HomeActivity) getActivity()).showFragment(R.id.tournaments);
         return;
      } else {
         variantType = "Deals";
      }
      ((HomeActivity) getActivity()).setGameVariant(variantType);
      ((HomeActivity) getActivity()).showFragment(R.id.lobby);
   }

   public void saveLastCheckedRadioButton(int id) {
      PrefManager.saveInt(getContext(), "lastCheckedItem", id);
   }

   @Subscribe
   public void onMessageEvent(Event event)
   {
      if (event.getEventName().equalsIgnoreCase("BALANCE_UPDATE"))
      {
         LoginResponse userData = ((RummyApplication) getActivity().getApplication()).getUserData();
         userData.setFunChips(event.getFunChips());
         userData.setFunInPlay(event.getFunInPlay());
         userData.setRealChips(event.getReaChips());
         userData.setRealInPlay(event.getRealInPlay());
         setUserDetails(userData);
      }
   }

   public void onSaveInstanceState(Bundle outState) {
   }

   public void onStart() {
      super.onStart();
      if(!EventBus.getDefault().isRegistered(this)) {
         EventBus.getDefault().register(this);
      }

      MyWebEngage.trackScreenNameWE(MyWebEngage.PRE_LOBBY_SCREEN, getContext());
   }

   public class WebViewController extends WebViewClient {
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
         view.loadUrl(url);
         return true;
      }
   }

}
