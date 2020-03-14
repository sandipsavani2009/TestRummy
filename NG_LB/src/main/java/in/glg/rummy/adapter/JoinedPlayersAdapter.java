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
import in.glg.rummy.models.JoinedPlayers;

/**
 * Created by GridLogic on 12/12/17.
 */

public class JoinedPlayersAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentDetailsFragment mTournamentDetailsFragment;
    private List<JoinedPlayers> joinedPlayers;


    public JoinedPlayersAdapter(Context context, List<JoinedPlayers> joinedPlayers, TournamentDetailsFragment tournamentDetailsFragment)
    {
        this.context = context;
        this.joinedPlayers = joinedPlayers;
        this.mTournamentDetailsFragment = tournamentDetailsFragment;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.joinedPlayers.size();
    }

    @Override
    public JoinedPlayers getItem(int position) {
        return (JoinedPlayers) this.joinedPlayers.get(position);
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
        final JoinedPlayers joinedPlayers = getItem(position);

        TextView serialNo_tv = (TextView) v.findViewById(R.id.serialNo_tv);
        TextView nickName_tv = (TextView) v.findViewById(R.id.nickName_tv);

        serialNo_tv.setText(String.valueOf(position+1));
        nickName_tv.setText(joinedPlayers.getNick_name());

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
