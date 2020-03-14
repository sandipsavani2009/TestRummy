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
import in.glg.rummy.models.PrizeList;

/**
 * Created by GridLogic on 12/12/17.
 */

public class PrizeListAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentDetailsFragment mTournamentDetailsFragment;
    private List<PrizeList> prizeLists;
    private String status;

    public PrizeListAdapter(Context context, List<PrizeList> prizeLists, TournamentDetailsFragment tournamentDetailsFragment, String status)
    {
        this.context = context;
        this.prizeLists = prizeLists;
        this.mTournamentDetailsFragment = tournamentDetailsFragment;
        this.status = status;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.prizeLists.size();
    }

    @Override
    public PrizeList getItem(int position) {
        return (PrizeList) this.prizeLists.get(position);
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
        final PrizeList prizeList = getItem(position);


        TextView position_tv = (TextView) v.findViewById(R.id.position_tv);
        TextView prizeAmount_tv = (TextView) v.findViewById(R.id.prizeAmount_tv);
        TextView player_tv = (TextView) v.findViewById(R.id.player_tv);

        position_tv.setText(prizeList.getRank());
        prizeAmount_tv.setText(prizeList.getPrize_amount());

        if(status.equalsIgnoreCase("completed"))
        {

        }

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
