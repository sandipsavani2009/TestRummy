package in.glg.rummy.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.glg.rummy.GameRoom.TableActivity;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.adapter.IamBackAdapter;
import in.glg.rummy.api.OnResponseListener;
import in.glg.rummy.api.requests.TableDetailsRequest;
import in.glg.rummy.api.response.JoinTableResponse;
import in.glg.rummy.engine.GameEngine;
import in.glg.rummy.exceptions.GameEngineNotRunning;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.PlayingCard;
import in.glg.rummy.utils.TLog;
import in.glg.rummy.utils.Utils;

public class IamBackFragment extends BaseFragment {
   private static final String TAG = TablesFragment.class.getSimpleName();
   private IamBackAdapter adapter;
   private List<PlayingCard> cards;
   private Button iamBackBtn;
   private OnResponseListener iamBackListner = new OnResponseListener(JoinTableResponse.class) {
      public void onResponse(Object response) {
         if (((TableActivity) IamBackFragment.this.getActivity()) != null) {
            ((TableActivity) IamBackFragment.this.getActivity()).setIamBackFlag();
            ((TableActivity) IamBackFragment.this.getActivity()).showGameTablesLayoutOnImaBack();
         }
      }
   };
   private List<String> mJoinedTableIds = new ArrayList();
   private PlayingCard mJokerCard;
   private RecyclerView mRecyclerView;
   private TextView mSubTitle;
   private TextView mTitle;

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);
      Log.w("testing", "I am Back fragment Started");
      View v = inflater.inflate(R.layout.fragment_iam_back, container, false);
      this.cards = new ArrayList();
      this.mRecyclerView = (RecyclerView) v.findViewById(R.id.cards_rv);
      this.mTitle = (TextView) v.findViewById(R.id.iam_back_title);
      this.mSubTitle = (TextView) v.findViewById(R.id.iam_back_sub_title);
      v.setFocusable(true);
      v.setFocusableInTouchMode(true);
      this.iamBackBtn = (Button) v.findViewById(R.id.iam_back_btn);
      this.iamBackBtn.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            IamBackFragment.this.sendIamBack();
         }
      });
      setPlayerDiscards();
      this.mJoinedTableIds = ((RummyApplication) getActivity().getApplication()).getJoinedTableIds();
      return v;
   }

   @SuppressLint("WrongConstant")
   private void setPlayerDiscards()
   {
      this.adapter = new IamBackAdapter(this.cards, getContext(), this.mJokerCard);

      this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 0, false));
      this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
      this.mRecyclerView.setAdapter(this.adapter);

      clearDiscardedCards();
   }

   public void showAutoPlayCards(PlayingCard card, Event event)
   {
      this.cards.add(card);
      this.adapter.notifyDataSetChanged();
      if (this.cards != null && this.cards.size() > 0) {
         this.mTitle.setText("Your game was set to Auto Play and " + this.cards.size() + "/" + 5 + " turns have been played and below are the discarded cards.Please click on the I am back button to continue with your game.");
      }
   }

   public void disableIamBackButton() {
      this.iamBackBtn.setEnabled(false);
      this.iamBackBtn.setClickable(false);
      this.mSubTitle.setText(R.string.iam_back_error_info);
   }

   public void enableIamBackButton() {
      this.iamBackBtn.setEnabled(true);
      this.iamBackBtn.setClickable(true);
      this.mSubTitle.setText(getString(R.string.info_iam_back));
   }

   private void popFragment() {
      getActivity().getSupportFragmentManager().popBackStack(IamBackFragment.class.getName(), 1);
   }

   private void sendIamBack() {
      RummyApplication app = (RummyApplication) getActivity().getApplication();
      TableDetailsRequest request = new TableDetailsRequest();
      request.setCommand("autoplaystatus");
      request.setUuid(Utils.generateUuid());
      request.setUserId(String.valueOf(app.getUserData().getUserId()));
      try {
         GameEngine.getInstance();
         GameEngine.sendRequestToEngine(getActivity().getApplicationContext(), Utils.getObjXMl(request), this.iamBackListner);
      } catch (GameEngineNotRunning gameEngineNotRunning) {
         TLog.d(TAG, "sendIamBack" + gameEngineNotRunning.getLocalizedMessage());
      }
   }

   public void onSaveInstanceState(Bundle outState) {
   }

   public void clearDiscardedCards() {
      if (this.cards != null) {
         this.cards.clear();
         this.adapter.notifyDataSetChanged();
      }
      this.iamBackBtn.setEnabled(true);
      this.iamBackBtn.setClickable(true);
      this.mSubTitle.setText(getString(R.string.info_iam_back));
      this.mTitle.setText(getString(R.string.info_msg_iam_back));
   }

   public void resetAutoPlayCards(GamePlayer gamePlayer) {
      String autoPlay = gamePlayer.getAutoplay();
      String autoPlayCountStr = gamePlayer.getAutoplay_count();
      if (autoPlay != null && autoPlay.equalsIgnoreCase("True") && autoPlayCountStr != null && Integer.parseInt(autoPlayCountStr) <= 1) {
         clearDiscardedCards();
      }
   }

   public void setJokerCard(PlayingCard jokerCard) {
      this.mJokerCard = jokerCard;
      if (this.adapter != null) {
         this.adapter.setJokerCard(jokerCard);
      }
      this.adapter.notifyDataSetChanged();
   }
}
