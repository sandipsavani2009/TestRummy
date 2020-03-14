package in.glg.rummy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.fragments.TournamentDetailsFragment;
import in.glg.rummy.models.GamePlayer;

/**
 * Created by GridLogic on 31/1/18.
 */

public class WinnersAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentDetailsFragment mTournamentDetailsFragment;
    private List<GamePlayer> winners;

    public WinnersAdapter(Context context, List<GamePlayer> winners, TournamentDetailsFragment tournamentDetailsFragment)
    {
        this.context = context;
        this.winners = winners;
        this.mTournamentDetailsFragment = tournamentDetailsFragment;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.winners.size();
    }

    @Override
    public GamePlayer getItem(int position) {
        return (GamePlayer) this.winners.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v;
        if (convertView == null) {
            v = this.inflater.inflate(R.layout.prize_list_adapter_item, parent, false);
        } else {
            v = convertView;
        }
        final GamePlayer winner = getItem(position);


        TextView position_tv = (TextView) v.findViewById(R.id.position_tv);
        TextView prizeAmount_tv = (TextView) v.findViewById(R.id.prizeAmount_tv);
        TextView player_tv = (TextView) v.findViewById(R.id.player_tv);

        position_tv.setText(winner.getRank());
        prizeAmount_tv.setText(winner.getPrize_amount());
        player_tv.setText(winner.getNick_name());

        player_tv.setVisibility(View.VISIBLE);

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
