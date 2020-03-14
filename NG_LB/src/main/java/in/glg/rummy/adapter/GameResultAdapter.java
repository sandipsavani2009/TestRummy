package in.glg.rummy.adapter;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GamePlayer;
import in.glg.rummy.models.MeldBox;
import in.glg.rummy.models.PlayingCard;
import in.glg.rummy.utils.Utils;
import in.glg.rummy.view.RummyView;

public class GameResultAdapter extends BaseAdapter {
   private Context context;
   private LayoutInflater inflater;
   private Event mEvent;
   private PlayingCard mJokerCard;
   private ArrayList<GamePlayer> mPlayersList;
   private boolean allUsersDropped = false;
   private boolean wrongShowAny = false;
   private boolean lostAny = false;
   private int dropCount = 0;
   private int timeoutCount = 0;
   private int dropTimeoutCount = 0;

   public GameResultAdapter(Context ctx, Event event) {
      this.context = ctx;
      this.mPlayersList = (ArrayList) event.getPlayer();
      this.mEvent = event;
      this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      for(GamePlayer gamePlayer : mPlayersList){
         if(gamePlayer.getResult().equalsIgnoreCase("drop")) {
            dropCount++;
            dropTimeoutCount++;
         }
         else if(gamePlayer.getResult().equalsIgnoreCase("eliminate"))
            wrongShowAny = true;
         else if(gamePlayer.getResult().equalsIgnoreCase("timeout")) {
            timeoutCount++;
            dropTimeoutCount++;
         }
         else if(gamePlayer.getResult().equalsIgnoreCase("meld_timeout") || gamePlayer.getResult().equalsIgnoreCase("table_leave") ||
                 gamePlayer.getResult().equalsIgnoreCase("meld"))
            lostAny = true;
      }
      if(dropCount==(mPlayersList.size()-1))
         allUsersDropped = true;

      setJokerCard(event);
   }

   private void addCardToRummyView(PlayingCard playerCard, RummyView view) {
      LinearLayout card = view.getGameResultCard();
      ImageView cardImg = (ImageView) card.findViewById(R.id.cardImageView);
      ImageView jockerCardImg = (ImageView) card.findViewById(R.id.game_results_jokerCardImg);
      int imgId = this.context.getResources().getIdentifier(String.format("%s%s", new Object[]{playerCard.getSuit(), playerCard.getFace()}), "drawable", this.context.getPackageName());
      cardImg.setVisibility(View.VISIBLE);
      cardImg.setImageResource(imgId);
      if (this.mJokerCard != null) {
         if (this.mJokerCard.getFace().equalsIgnoreCase(playerCard.getFace())) {
            jockerCardImg.setVisibility(View.VISIBLE);
         } else if (playerCard == null || !playerCard.getFace().equalsIgnoreCase("1")) {
            jockerCardImg.setVisibility(View.GONE);
         } else if (this.mJokerCard.getFace().equalsIgnoreCase("0")) {
            jockerCardImg.setVisibility(View.VISIBLE);
         } else {
            jockerCardImg.setVisibility(View.GONE);
         }
      }
      view.addGameResultCard(card);
   }

   private void setJokerCard(Event event) {
      PlayingCard jokerCard = new PlayingCard();
      jokerCard.setFace(event.getFace());
      jokerCard.setSuit(event.getSuit());
      this.mJokerCard = jokerCard;
   }

   public int getCount() {
      return this.mPlayersList.size();
   }

   public GamePlayer getItem(int position) {
      return (GamePlayer)this.mPlayersList.get(position);
   }

   public long getItemId(int position) {
      return (long)position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      View v;
      GamePlayer player = getItem(position);
      if (convertView == null) {
         v = this.inflater.inflate(R.layout.game_result_item, parent, false);
      } else {
         v = convertView;
      }
      TextView playerName = (TextView) v.findViewById(R.id.player_name_tv);
      TextView playerStatus = (TextView) v.findViewById(R.id.player_status_tv);
      TextView totalScore = (TextView) v.findViewById(R.id.player_total_count_tv);
      TextView pointsTv = (TextView) v.findViewById(R.id.player_count_tv);
      TextView progressTv = (TextView) v.findViewById(R.id.game_results_waiting_tv);
      ImageView aiIv = (ImageView) v.findViewById(R.id.ai_iv);
      ImageView scIv = (ImageView) v.findViewById(R.id.sc_iv);
      playerName.setText(player.getNick_name());
      String result = player.getResult();
      String aiEnable = player.getAiEnable();
      String scUse = player.getScUse();
      if (scUse == null || !scUse.equalsIgnoreCase("True")) {
         scIv.setVisibility(View.GONE);
      } else {
         scIv.setVisibility(View.VISIBLE);
      }
      if (aiEnable.equalsIgnoreCase("True")) {
         aiIv.setVisibility(View.VISIBLE);
      } else {
         aiIv.setVisibility(View.GONE);
      }
      RummyView rummyView = (RummyView) v.findViewById(R.id.player_cards_view);
      rummyView.removeViews();
      if (result != null) {
         if (result.equalsIgnoreCase("drop") || result.equalsIgnoreCase("timeout")) {
            rummyView.setVisibility(View.INVISIBLE);
         } else {
            rummyView.setVisibility(View.VISIBLE);
         }

         if(result.equalsIgnoreCase("winner") && allUsersDropped==true)
            rummyView.setVisibility(View.INVISIBLE);
         else if(result.equalsIgnoreCase("winner") && wrongShowAny==true)
            rummyView.setVisibility(View.INVISIBLE);

         if(result.equalsIgnoreCase("winner") && lostAny==true)
            rummyView.setVisibility(View.VISIBLE);

         if(timeoutCount==(mPlayersList.size()-1))
            rummyView.setVisibility(View.INVISIBLE);

         if(dropTimeoutCount==(mPlayersList.size()-1))
            rummyView.setVisibility(View.INVISIBLE);

         if (result.equalsIgnoreCase("winner")) {
            result = "winner";
            playerName.setTextColor(ContextCompat.getColor(this.context, R.color.highlight_font_color));
            playerStatus.setTextColor(ContextCompat.getColor(this.context, R.color.highlight_font_color));
         } else {
            playerName.setTextColor(ContextCompat.getColor(this.context, R.color.black));
            playerStatus.setTextColor(ContextCompat.getColor(this.context, R.color.black));
         }
         if (result.equalsIgnoreCase("eliminate")) {
            result = "Wrong show";
         } else if (result.equalsIgnoreCase("meld_timeout") || result.equalsIgnoreCase("table_leave") || result.equalsIgnoreCase("meld")) {
            result = "Lost";
         } else if (result.equalsIgnoreCase("drop") || result.equalsIgnoreCase("timeout")) {
            result = "Dropped";
         }
         playerStatus.setText(WordUtils.capitalize(result));
         playerStatus.setMovementMethod(new ScrollingMovementMethod());
      }
      if (this.mEvent.getTableType().contains(Utils.PR)) {
         String score = player.getScore();
         if (player.getResult().equalsIgnoreCase("winner")) {
            score = String.format("%s%s", new Object[]{"+", score});
         } else {
            score = String.format("%s%s", new Object[]{"-", score});
         }
         pointsTv.setText(player.getPoints());
         totalScore.setText(score);
      } else {
         String points = Utils.formatString(player.getPoints());
         if (points != null) {
            pointsTv.setText(points);
         }
         if (player.getTotalScore() != null) {
            totalScore.setText(Utils.formatString(player.getTotalScore()));
         }
      }
      List<MeldBox> meldBox = player.getMeldList();
      if (meldBox != null) {
         progressTv.setVisibility(View.GONE);
         for (MeldBox box : meldBox) {
            List<PlayingCard> cards = box.getMeldBoxes();
            if (cards != null && cards.size() > 0) {
               for (PlayingCard card : cards) {
                  addCardToRummyView(card, rummyView);
               }
               rummyView.addGameResultCard(rummyView.getGameResultCard());
            }
         }
      } else {
         progressTv.setVisibility(View.VISIBLE);
         playerStatus.setText("");
         pointsTv.setText("");
         totalScore.setText("");
      }
      return v;
   }
}
