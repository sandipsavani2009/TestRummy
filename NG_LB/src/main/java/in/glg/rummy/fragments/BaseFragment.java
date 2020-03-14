package in.glg.rummy.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import in.glg.rummy.NetworkProvider.VolleySingleton;
import in.glg.rummy.R;
import in.glg.rummy.adapter.GameResultAdapter;
import in.glg.rummy.adapter.SplitWinnerAdapter;
import in.glg.rummy.api.requests.LoginRequest;
import in.glg.rummy.api.response.CheckMeldResponse;
import in.glg.rummy.fancycoverflow.FancyCoverFlow;
import in.glg.rummy.models.CheckMeldResult;
import in.glg.rummy.models.EngineRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.MeldBox;
import in.glg.rummy.models.MeldCard;
import in.glg.rummy.models.PlayingCard;
import in.glg.rummy.models.Results;
import in.glg.rummy.models.TableDetails;
import in.glg.rummy.utils.MyWebEngage;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.RummyConstants;
import in.glg.rummy.utils.Utils;
import in.glg.rummy.view.MeldView;
import in.glg.rummy.view.RummyView;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;

public class BaseFragment extends Fragment {

   private String TAG = BaseFragment.class.getName();
   private AlertDialog dialog;
   private Dialog mLoadingDialog;
   private ArrayList<LinearLayout> preMeldViews;
   public boolean userPlacedShow = false;

   public Dialog getLeaveTableDialog(Context context, String message) {
      final Dialog dialog = new Dialog(context, R.style.DialogTheme);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_leave_table);
      ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
      ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      return dialog;
   }

   public void showGenericDialog(Context context, String message) {
      final Dialog dialog = new Dialog(context, R.style.DialogTheme);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_generic);
      ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
      ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      dialog.show();
   }

   public void showErrorChipsDialog(Context context, String message) {
      final Dialog dialog = new Dialog(context);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_error_balance);
      ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
      ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      dialog.show();
   }

   public void showErrorBuyChipsDialog(final Context context, String message)
   {
      final Dialog dialog = new Dialog(context);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_error_buychips);
      ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
      ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      ((Button) dialog.findViewById(R.id.buychips_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            //launchFragment(new DepositFragment(), true, DepositFragment.class.getName());
           // checkPlayerDeposit(context);
            dialog.dismiss();
         }
      });
      dialog.show();
   }

   public void showErrorBalanceBuyChips(final Context context, String text)
   {
      final Dialog dialog = new Dialog(context, R.style.DialogTheme);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_error_balance_buy_chips);

      TextView label = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
      Button deposit = (Button) dialog.findViewById(R.id.ok_btn);
      Button cancel = (Button) dialog.findViewById(R.id.cancel);

      label.setText(text);

      cancel.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            dialog.dismiss();
         }
      });

      deposit.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            //checkPlayerDeposit(context);
            dialog.dismiss();
         }
      });

      dialog.show();
   }

   public void showErrorLoyaltyChipsDialog(final Context context, String message)
   {
      final Dialog dialog = new Dialog(context);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(R.layout.dialog_error_buychips);
      ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
      ((Button) dialog.findViewById(R.id.ok_btn2)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      ((LinearLayout) dialog.findViewById(R.id.button_layout)).setVisibility(View.GONE);
      ((Button) dialog.findViewById(R.id.ok_btn2)).setVisibility(View.VISIBLE);
      dialog.show();
   }

   public Dialog getDialog(Context context, int layoutResourceId) {
      Dialog dialog = new Dialog(context);
      dialog.requestWindowFeature(1);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.setContentView(layoutResourceId);
      return dialog;
   }

   public String getDeviceType() {
      if (getResources().getBoolean(R.bool.isTablet)) {
         return "Tablet";
      }
      return "Mobile";
   }

   @SuppressLint("WrongConstant")
   public void showHideView(boolean canShow, View view, boolean canGone) {
//      Log.e("autodrop","showHideView");
      int i = canShow ? 0 : canGone ? 8 : 4;
      view.setVisibility(i);
   }

   public void hideView(View view) {
      view.setVisibility(View.GONE);
   }

   public void invisibleView(View view) {
      view.setVisibility(View.INVISIBLE);
   }

   public void goneView(View view)
   {
      view.setVisibility(View.INVISIBLE);
   }

   public void showView(View view) {
//      Log.e("view@238",getResources().getResourceName(view.getId())+"");
      view.setVisibility(View.VISIBLE);
   }

   public void invisibleView() {
      getView().setVisibility(View.INVISIBLE);
   }

   public void disableView(View view) {
//      Log.e("view@248",getResources().getResourceName(view.getId())+"");
      view.setEnabled(false);
      view.setClickable(false);
      view.setAlpha(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);
   }

   public void disableClick(View view) {
      view.setEnabled(false);
      view.setClickable(false);
   }

   public void enableView(View view) {
      view.setEnabled(true);
      view.setClickable(true);
      view.setAlpha(1.0f);
   }

   public void showLongToast(Context context, String message) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
   }

   public void showDialog(Context context, String message) {
      this.dialog = new SpotsDialog(context, message);
      this.dialog.setCancelable(false);
      this.dialog.show();
   }

   public void dismissDialog() {
      if (this.dialog != null && this.dialog.isShowing()) {
         this.dialog.dismiss();
      }
   }

   public void dismissDialog(Dialog mDialog) {
      if (mDialog != null && mDialog.isShowing()) {
         mDialog.dismiss();
      }
   }

   public void launchFragment(Fragment fragment, String tag) {
      FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
      fragment.setArguments(new Bundle());
      fragmentTransaction.add(R.id.content_frame, fragment, tag);
      fragmentTransaction.addToBackStack(tag);
      fragmentTransaction.commit();
   }

   public void launchFragment(Fragment fragment, String tag, Bundle bundle)
   {
      FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
      fragment.setArguments(bundle);
       fragmentTransaction.replace(R.id.home_content_frame, fragment, tag);
      fragmentTransaction.addToBackStack(tag);
      fragmentTransaction.commit();
   }

    public void removeFragment(String tag) {
      FragmentManager fm = getActivity().getSupportFragmentManager();
      fm.beginTransaction().remove(fm.findFragmentByTag(tag));
   }

   public void launchNewActivity(Intent intent, boolean removeStack) {
      if (removeStack) {
         //intent.setFlags(268468224);
         intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      }
      startActivity(intent);
   }

   public void setColorFilter(ImageView view) {
   }

   public void showFragment(Fragment fragment) {
      FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
      ft.show(fragment);
      ft.commit();
   }

   public void hideFragment(Fragment fragment) {
      FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
      ft.hide(fragment);
      ft.commit();
   }

   public void showLoadingDialog(Context context)
   {
      try {
         this.mLoadingDialog = new Dialog(context, R.style.DialogTheme);
         this.mLoadingDialog.requestWindowFeature(1);
         this.mLoadingDialog.setContentView(R.layout.dialog_loading);
         this.mLoadingDialog.show();
      }
      catch (Exception e){

      }
   }

   public void dismissLoadingDialog()
   {
      try {
         if (this.mLoadingDialog != null && this.mLoadingDialog.isShowing()) {
            this.mLoadingDialog.dismiss();
         }
      }
      catch (Exception e){

      }
   }

   public void showWinnerFragment(final RelativeLayout linearLayout, final View winnerView, final View searchView, View splitRejectedView, Event winnerEvent, TableDetails tableDetails) {
      showView(linearLayout);
      showView(winnerView);
      invisibleView(splitRejectedView);
      boolean isFreeTable = false;
      if (tableDetails != null && tableDetails.getTableCost().contains("FUNCHIPS_FUNCHIPS")) {
         isFreeTable = true;
      }
      ((ListView) winnerView.findViewById(R.id.split_winner_lv)).setAdapter(new SplitWinnerAdapter(getContext(), winnerEvent.getPlayer(), isFreeTable));
      ((ImageView) winnerView.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            if (searchView.getVisibility() == View.VISIBLE) {
               BaseFragment.this.invisibleView(winnerView);
               return;
            }
            BaseFragment.this.hideView(winnerView);
            BaseFragment.this.hideView(linearLayout);
         }
      });
   }

   public void hideWinnerView(RelativeLayout linearLayout, View winnerView, View searchView, View splitRejectedView) {
      showView(linearLayout);
      invisibleView(winnerView);
      invisibleView(splitRejectedView);
      showView(searchView);
   }

   public void setUpPlayerRank(View view, GamePlayer player)
   {
      ((TextView) view.findViewById(R.id.player_rank_tv)).setText("("+player.getRank()+")");
      ((TextView) view.findViewById(R.id.player_rank_tv)).setVisibility(View.VISIBLE);
   }

   public void setUpPlayerDetails(View view, GamePlayer player, String dealerId, boolean isPlayerJoined)
   {
      ((RelativeLayout) view.findViewById(R.id.player_details_root_layout)).setAlpha(1.0f);
      ((TextView) view.findViewById(R.id.player_name_tv)).setText(player.getNick_name());
      if (isPlayerJoined) {
         hideDropPayerImage(view);
      }
      View ratingBar = view.findViewById(R.id.player_rating_bar);
      String playerLevelStr = player.getPlayerlevel();
      int rating = (playerLevelStr == null || playerLevelStr.length() <= 0) ? -1 : Integer.parseInt(playerLevelStr);
      setPlayerLevel(ratingBar, rating);
      setDealer(dealerId, player.getUser_id(), (ImageView) view.findViewById(R.id.player_dealer_iv));
      setPlayerSystem((ImageView) view.findViewById(R.id.player_system_iv), player.getDEVICE_ID());
      String points = player.getPoints();
      TextView playerPoints = (TextView) view.findViewById(R.id.player_points_tv);
      if (points == null) {
         String buyInAmount = player.getBuyinammount();
         points = buyInAmount != null ? buyInAmount : "0";
      }
      playerPoints.setText(String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(points))));
   }

   public void resetPlayerData(View view) {
      ((RelativeLayout) view.findViewById(R.id.player_details_root_layout)).setAlpha(FancyCoverFlow.SCALEDOWN_GRAVITY_CENTER);
      ((TextView) view.findViewById(R.id.player_name_tv)).setText("");
      setPlayerLevel(view.findViewById(R.id.player_rating_bar), 0);
      invisibleView((ImageView) view.findViewById(R.id.player_dealer_iv));
      invisibleView((ImageView) view.findViewById(R.id.player_system_iv));
      ((TextView) view.findViewById(R.id.player_points_tv)).setText("");
      hideDropPayerImage(view);
      RelativeLayout autoPlayLayout = (RelativeLayout) view.findViewById(R.id.auto_play_layout);
      if (autoPlayLayout != null) {
         invisibleView(autoPlayLayout);
      }
   }

   public void resetAutoPlayUI(View view) {
      RelativeLayout autoPlayLayout = (RelativeLayout) view.findViewById(R.id.auto_play_layout);
      if (autoPlayLayout != null) {
         invisibleView(autoPlayLayout);
      }
   }

   public void hideDropPayerImage(View view) {
      RelativeLayout dropImage = (RelativeLayout) view.findViewById(R.id.player_drop_iv);
      if (dropImage != null) {
         hideView(dropImage);
      }
   }

   public void setAutoPlayView(View view, GamePlayer gamePlayer) {
      String autoPlay = gamePlayer.getAutoplay();
      String autoPlayCount = gamePlayer.getAutoplay_count();
      String totalCount = gamePlayer.getTotalCount();
      RelativeLayout autoPlayLayout = (RelativeLayout) view.findViewById(R.id.auto_play_layout);
      TextView autoPlayCountTv = (TextView) view.findViewById(R.id.auto_play_count_tv);
      if (autoPlay == null) {
         hideView(autoPlayLayout);
      } else if (autoPlay.equalsIgnoreCase("True")) {
         showView(autoPlayLayout);
         if (autoPlayCount != null) {
            autoPlayCountTv.setText("Auto Play " + autoPlayCount + " Of " + totalCount);
         }
      } else {
         hideView(autoPlayLayout);
      }
   }

   public void hideAutoPlayView(View view) {
      hideView((RelativeLayout) view.findViewById(R.id.auto_play_layout));
   }

   public void setUserDetails(View view, GamePlayer player, String dealerId, boolean isPlayerJoined)
   {
      ((RelativeLayout) view.findViewById(R.id.player_details_root_layout)).setAlpha(1.0f);
      if (isPlayerJoined) {
         hideDropPayerImage(view);
      }
      ((TextView) view.findViewById(R.id.player_name_tv)).setText(player.getNick_name());
      View ratingBar = view.findViewById(R.id.player_rating_bar);
      String playerLevelStr = player.getPlayerlevel();
      int rating = (playerLevelStr == null || playerLevelStr.length() <= 0) ? -1 : Integer.parseInt(playerLevelStr);
      setPlayerLevel(ratingBar, rating);
      setDealer(dealerId, player.getUser_id(), (ImageView) view.findViewById(R.id.player_dealer_iv));
      setPlayerSystem((ImageView) view.findViewById(R.id.player_system_iv), player.getDEVICE_ID());
      String points = player.getPoints();
      TextView playerPoints = (TextView) view.findViewById(R.id.player_points_tv);
      if (points == null) {
         String buyInAmount = player.getBuyinammount();
         points = buyInAmount != null ? buyInAmount : "0";
      }
      playerPoints.setText(String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(points))));
   }

   public void setDealer(String mDealerId, String user_id, ImageView dealerImage) {
      if (mDealerId.equalsIgnoreCase(user_id)) {
         showView(dealerImage);
      }
   }

   public void setPlayerLevel(View ratingBar, int rating) {
      if (rating >= 0) {
         if (ratingBar == null || rating <= 0) {
            invisibleView(ratingBar);
            return;
         }
         showView(ratingBar);
         ImageView star1 = (ImageView) ratingBar.findViewById(R.id.star_1);
         ImageView star2 = (ImageView) ratingBar.findViewById(R.id.star_2);
         ImageView star3 = (ImageView) ratingBar.findViewById(R.id.star_3);
         ImageView star4 = (ImageView) ratingBar.findViewById(R.id.star_4);
         ImageView star5 = (ImageView) ratingBar.findViewById(R.id.star_5);
         switch (rating) {
            case 1:
               showView(star1);
               hideView(star2);
               hideView(star3);
               hideView(star4);
               hideView(star5);
               return;
            case 2:
               showView(star1);
               showView(star2);
               hideView(star3);
               hideView(star4);
               hideView(star5);
               return;
            case 3:
               showView(star1);
               showView(star2);
               showView(star3);
               hideView(star4);
               hideView(star5);
               return;
            case 4:
               showView(star1);
               showView(star2);
               showView(star3);
               showView(star4);
               hideView(star5);
               return;
            case 5:
               showView(star1);
               showView(star2);
               showView(star3);
               showView(star4);
               showView(star5);
               return;
            default:
               return;
         }
      }
   }

   public void setPlayerSystem(ImageView playerSystem, String system) {
      if (system == null || system.length() <= 0) {
         invisibleView(playerSystem);
         return;
      }
      showView(playerSystem);
      if (system.equalsIgnoreCase("Desktop") || system.equalsIgnoreCase("TV")) {
         playerSystem.setImageResource(R.drawable.desktop_icon);
      } else if (system.equalsIgnoreCase("Tablet")) {
         playerSystem.setImageResource(R.drawable.ipad_icon);
      } else {
         playerSystem.setImageResource(R.drawable.mobile);
      }
   }

   public void setPlayerPoints(View view, GamePlayer player) {
      String playerName = ((TextView) view.findViewById(R.id.player_name_tv)).getText().toString();
      if (playerName != null && !playerName.isEmpty()) {
         Log.e("TwoTables",String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(player.getTotalScore())))+"@555");
         Utils.PR_JOKER_POINTS = String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(player.getTotalScore())));
         ((TextView) view.findViewById(R.id.player_points_tv)).setText(String.valueOf(new DecimalFormat("0.#").format(Float.valueOf(player.getTotalScore()))));
      }
   }

   public void setGameResultsView(View view, Event event) {
      showView(view);

      ((TextView) view.findViewById(R.id.game_id_tv)).setText(event.getGameId());
      ((TextView) view.findViewById(R.id.table_id_tv)).setText(event.getTableId());

      PlayingCard jokerCard = new PlayingCard();
      jokerCard.setFace(event.getFace());
      jokerCard.setSuit(event.getSuit());
      ImageView jokerIv = (ImageView) view.findViewById(R.id.game_jocker_iv);
      ((ListView) view.findViewById(R.id.game_results_lv)).setAdapter(new GameResultAdapter(getActivity(), event));
      setJokerCard(jokerCard, jokerIv);
   }

   private void setJokerCard(PlayingCard jokerCard, ImageView mGameJockerIv) {
      if (jokerCard != null) {
         String cardValue = String.format("%s%s%s", new Object[]{"jocker_", jokerCard.getSuit(), jokerCard.getFace()});
         if (cardValue.equalsIgnoreCase("jocker_jo0")) {
            cardValue = "jocker_d1";
         }
         int imgId = getResources().getIdentifier(cardValue, "drawable", getActivity().getPackageName());
         mGameJockerIv.setVisibility(View.VISIBLE);
         mGameJockerIv.setImageResource(imgId);
      }
   }

   public void setGameResultsTimer(View view, String message) {
      TextView mTimerTv = (TextView) view.findViewById(R.id.game_timer);

      showView(mTimerTv);
      mTimerTv.setText(message);
   }

   public void resetTimerInfo(View view) {
      ((TextView) view.findViewById(R.id.game_timer)).setText("");
   }

   public void setMeldCardsView(View view, MeldCard meldCard) {
      showView(view);
      RummyView mMeldView = (RummyView) view.findViewById(R.id.meld_rummy_view);
      setJokerCard(meldCard.jokerCard, (ImageView) view.findViewById(R.id.game_jocker_iv));
      setPreMeldView(view, meldCard);
   }

   private void setPreMeldView(View view, MeldCard meldCard)
   {
      this.preMeldViews = new ArrayList();
      MeldView meldView = (MeldView) view.findViewById(R.id.meld_meld_view);
      meldView.removeViews();
      int index = 0;
      Iterator it = meldCard.meldGroup.iterator();
      while (it.hasNext()) {
         ArrayList<PlayingCard> groupList = (ArrayList) it.next();
         if (groupList.size() > 0) {
            LinearLayout linearLayout = meldView.getRummyLayout();
            RummyView rv = (RummyView) linearLayout.findViewById(R.id.meld_sc_rv);
            Iterator it2 = groupList.iterator();
            while (it2.hasNext())
            {
               addCardToRummyView((PlayingCard) it2.next(), index, rv, meldCard.jokerCard);
               index++;
            }
            this.preMeldViews.add(linearLayout);
            meldView.addMeldView(linearLayout);
         }
      }
   }

   public void setSmartCorrectionMeldView(View view, PlayingCard playingCard, EngineRequest engineRequest) {
      this.setJokerCard(playingCard, (ImageView)view.findViewById(R.id.game_jocker_iv));
      MeldView scMeldView = (MeldView)view.findViewById(R.id.sc_wmeld_view);
      scMeldView.removeViews();

      RummyView rummyView;
      LinearLayout var6;
      for(Iterator var7 = engineRequest.getWrongMeldList().iterator(); var7.hasNext(); scMeldView.addMeldView(var6)) {
         MeldBox var8 = (MeldBox)var7.next();
         var6 = scMeldView.getRummyLayout();
         rummyView = (RummyView)var6.findViewById(R.id.meld_sc_rv);
         List var15 = var8.getMeldBoxes();
         if(var15 != null && var15.size() > 0) {
            Iterator var16 = var15.iterator();

            while(var16.hasNext()) {
               this.addCardToSCRummyView((PlayingCard)var16.next(), 0, rummyView, playingCard);
            }
         }
      }

      MeldView var9 = (MeldView)view.findViewById(R.id.sc_cmeld_view);
      var9.removeViews();

      LinearLayout var11;
      for(Iterator var10 = engineRequest.getCheckMeldList().iterator(); var10.hasNext(); var9.addMeldView(var11)) {
         MeldBox var12 = (MeldBox)var10.next();
         var11 = var9.getRummyLayout();
         rummyView = (RummyView)var11.findViewById(R.id.meld_sc_rv);
         List var13 = var12.getMeldBoxes();
         if(var13 != null && var13.size() > 0) {
            Iterator var14 = var13.iterator();

            while(var14.hasNext()) {
               this.addCardToSCRummyView((PlayingCard)var14.next(), 0, rummyView, playingCard);
            }
         }
      }
   }

   public void updateScoreOnPreMeld(CheckMeldResponse checkMeldResponse, View mMeldCardsView)
   {
      if (checkMeldResponse != null)
      {
         Results results = checkMeldResponse.getResults();
         if (results != null)
         {
            TextView invalidShow = (TextView) mMeldCardsView.findViewById(R.id.invalid_show_tv);
            invalidShow.setVisibility(View.INVISIBLE);

            List<CheckMeldResult> checkMeldResultList = results.getCheckMeldResults();

            if (checkMeldResultList != null && checkMeldResultList.size() == this.preMeldViews.size())
            {
               for (int i = 0; i < checkMeldResultList.size(); i++)
               {
                  LinearLayout layOut = (LinearLayout) this.preMeldViews.get(i);
                  CheckMeldResult checkMeldResult = (CheckMeldResult) checkMeldResultList.get(i);
                  ImageView line = (ImageView) layOut.findViewById(R.id.meld_line_iv);
                  ImageView arrowIv = (ImageView) layOut.findViewById(R.id.down_arrow_iv);
                  TextView scoreTv = (TextView) layOut.findViewById(R.id.meld_score_tv);

                  scoreTv.setText(checkMeldResult.getScore());

                  if (checkMeldResult.getResult().equalsIgnoreCase("True") || checkMeldResult.getScore().equalsIgnoreCase("0"))
                  {
                     line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.meldScoreGreenColor));
                     scoreTv.setTextColor(ContextCompat.getColor(getContext(), R.color.meldScoreGreenColor));
                     invisibleView(scoreTv);
                     invisibleView(arrowIv);
                     invalidShow.setText("Invalid Show - Some sets are missing");
                  }
                  else
                  {
                     showView(invalidShow);
                     showView(scoreTv);
                     showView(arrowIv);
                     line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.meldScoreRedColor));
                     scoreTv.setTextColor(ContextCompat.getColor(getContext(), R.color.meldScoreRedColor));
                  }
               }

               if(!this.userPlacedShow)
                  invalidShow.setVisibility(View.INVISIBLE);
            }
         }
      }
   }

   private void setGroupView(RummyView mMeldView, ArrayList<ArrayList<PlayingCard>> meldGroup, PlayingCard jokerCard) {
      mMeldView.removeViews();
      int index = 0;
      Iterator it = meldGroup.iterator();
      while (it.hasNext()) {
         ArrayList<PlayingCard> groupList = (ArrayList) it.next();
         if (groupList.size() > 0) {
            Iterator it2 = groupList.iterator();
            while (it2.hasNext()) {
               addCardToRummyView((PlayingCard) it2.next(), index, mMeldView, jokerCard);
               index++;
            }
            mMeldView.addCard(mMeldView.getCard());
         }
      }
   }

   private void addCardToRummyView(PlayingCard var1, int var2, RummyView rummyView, PlayingCard var4) {
      LinearLayout var8 = rummyView.getCard();
      ImageView var9 = (ImageView)var8.findViewById(R.id.cardImageView);
      ImageView var7 = (ImageView)var8.findViewById(R.id.jokerCardImg);
      String var6 = String.format("%s%s", new Object[]{var1.getSuit(), var1.getFace()});
      int var5 = this.getResources().getIdentifier(var6, "drawable", this.getActivity().getPackageName());
      var9.setVisibility(View.VISIBLE);
      var9.setImageResource(var5);
      if(var4 != null) {
         if(var4.getFace().equalsIgnoreCase(var1.getFace())) {
            var7.setVisibility(View.VISIBLE);
         } else {
            var7.setVisibility(View.GONE);
         }
      }

      var9.setTag(String.format("%s%s", new Object[]{var6, String.valueOf(var2)}));
      var1.setIndex(String.valueOf(var2));
      rummyView.addCard(var8);
   }

   public void launchFragment(Fragment fragment, boolean addToBackStack, String tag) {
      FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.home_content_frame, fragment, tag);
      if (addToBackStack) {
         fragmentTransaction.addToBackStack(tag);
      }
      fragmentTransaction.commitAllowingStateLoss();
   }

   public void popFragment(String tag) {
      getActivity().getSupportFragmentManager().popBackStack(tag, 1);
   }



   public void showDropImage(View view) {
      showView((RelativeLayout) view.findViewById(R.id.player_drop_iv));
   }

   private void addCardToSCRummyView(PlayingCard var1, int var2, RummyView var3, PlayingCard var4) {
      LinearLayout var9 = var3.getSmartCorrectionCard();
      ImageView var6 = (ImageView)var9.findViewById(R.id.cardImageView);
      ImageView var8 = (ImageView)var9.findViewById(R.id.jokerCardImg);
      String var7 = String.format("%s%s", new Object[]{var1.getSuit(), var1.getFace()});
      int var5 = this.getResources().getIdentifier(var7, "drawable", this.getActivity().getPackageName());
      var6.setVisibility(View.VISIBLE);
      var6.setImageResource(var5);
      if(var4 != null) {
         if(var4.getFace().equalsIgnoreCase(var1.getFace())) {
            var8.setVisibility(View.VISIBLE);
         } else {
            var8.setVisibility(View.GONE);
         }
      }

      var6.setTag(String.format("%s%s", new Object[]{var7, String.valueOf(var2)}));
      var1.setIndex(String.valueOf(var2));
      var3.addCard(var9);
   }

   private void openBrowserToBuyChips(String TOKEN)
   {
      String url = Utils.SERVER_ADDRESS+"sendpaymentrequest/?client_type="
              + Utils.CLIENT_TYPE+"&device_type="+getDeviceType();
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      Bundle bundle = new Bundle();
      bundle.putString("Authorization", "Token "+TOKEN);
      i.putExtra(Browser.EXTRA_HEADERS, bundle);
      startActivity(i);
   }


   private void trackPaymentInitiatedEventWE()
   {
      Map<String, Object> map = new HashMap<>();
      map.put(MyWebEngage.USER_ID, PrefManager.getString(getContext(), RummyConstants.PLAYER_USER_ID, ""));
      map.put(MyWebEngage.DEVICE_TYPE, getDeviceType());
      map.put(MyWebEngage.CLIENT_TYPE, Utils.CLIENT_TYPE);
      MyWebEngage.trackWEEvent(MyWebEngage.PAYMENT_PAGE_VISITED, map);
   }

   public void hideKeyboard()
   {
      InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
   }
}
