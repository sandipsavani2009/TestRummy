package in.glg.rummy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GamePlayer;

public class PointsScoreBoardAdapter extends BaseAdapter {
   private Context mContext;
   private List<Event> mGameResultList;
   private LayoutInflater mLayoutInflater;
   private String mUserId;

   public PointsScoreBoardAdapter(Context context, List<Event> gamePlayersList, String mUserId) {
      this.mContext = context;
      this.mGameResultList = gamePlayersList;
      this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
      this.mUserId = mUserId;
   }

   public int getCount() {
      return this.mGameResultList.size();
   }

   public Object getItem(int position) {
      return null;
   }

   public long getItemId(int position) {
      return 0L;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      View v;
      if (convertView == null) {
         v = this.mLayoutInflater.inflate(R.layout.points_sb_list_item, parent, false);
      } else {
         v = convertView;
      }
      TextView gameId = (TextView) v.findViewById(R.id.sb_game_id_tv);
      TextView resultTv = (TextView) v.findViewById(R.id.sb_result_tv);
      TextView countTv = (TextView) v.findViewById(R.id.sb_count_tv);
      TextView scoreTv = (TextView) v.findViewById(R.id.sb_score_tv);
      ((TextView) v.findViewById(R.id.sb_round_tv)).setText(String.valueOf(position + 1));
      Event event = (Event) this.mGameResultList.get(position);
      gameId.setText(event.getGameId());
      for (GamePlayer player : event.getPlayer()) {
         if (this.mUserId.equalsIgnoreCase(player.getUser_id())) {
            String score = player.getScore();
            if (player.getResult().equalsIgnoreCase("winner")) {
               score = String.format("%s%s", new Object[]{"+", score});
            } else {
               score = String.format("%s%s", new Object[]{"-", score});
            }
            String result = player.getResult();
            if (result != null) {
               if (result.equalsIgnoreCase("eliminate")) {
                  result = "Wrong show";
               } else if (result.equalsIgnoreCase("meld_timeout") || result.equalsIgnoreCase("table_leave") || result.equalsIgnoreCase("meld")) {
                  result = "Lost";
               } else if (result.equalsIgnoreCase("drop") || result.equalsIgnoreCase("timeout")) {
                  result = "Dropped";
               }
               resultTv.setText(WordUtils.capitalize(result));
            }
            countTv.setText(player.getPoints());
            scoreTv.setText(score);
         }
      }
      return v;
   }

   public void notifyDataSetChanged() {
      super.notifyDataSetChanged();
   }
}
