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
import in.glg.rummy.models.WaitingPlayers;

/**
 * Created by GridLogic on 28/12/17.
 */

public class WaitingPlayersAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentDetailsFragment mTournamentDetailsFragment;
    private List<WaitingPlayers> waitingPlayers;


    public WaitingPlayersAdapter(Context context, List<WaitingPlayers> waitingPlayers, TournamentDetailsFragment tournamentDetailsFragment)
    {
        this.context = context;
        this.waitingPlayers = waitingPlayers;
        this.mTournamentDetailsFragment = tournamentDetailsFragment;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.waitingPlayers.size();
    }

    @Override
    public WaitingPlayers getItem(int position) {
        return (WaitingPlayers) this.waitingPlayers.get(position);
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
            v = this.inflater.inflate(R.layout.joined_players_adapter_item, parent, false);
        } else {
            v = convertView;
        }
        final WaitingPlayers waitingPlayers = getItem(position);

        TextView serialNo_tv = (TextView) v.findViewById(R.id.serialNo_tv);
        TextView nickName_tv = (TextView) v.findViewById(R.id.nickName_tv);

        serialNo_tv.setText(String.valueOf(position+1));
        nickName_tv.setText(waitingPlayers.getNick_name());

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
