package in.glg.rummy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.models.TournamentTables;

/**
 * Created by GridLogic on 12/2/18.
 */

public class TourneyTablesAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<TournamentTables> results;

    public TourneyTablesAdapter(Context context, List<TournamentTables> results)
    {
        this.context = context;
        this.results = results;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.results.size();
    }

    @Override
    public TournamentTables getItem(int position) {
        return (TournamentTables) this.results.get(position);
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
            v = this.inflater.inflate(R.layout.tourney_tables_adapter_item, parent, false);
        } else {
            v = convertView;
        }
        final TournamentTables resultItem = getItem(position);

        TextView tableId = (TextView) v.findViewById(R.id.tableId);
        TextView nickName = (TextView) v.findViewById(R.id.nickName);
        TextView high = (TextView) v.findViewById(R.id.high);
        TextView low = (TextView) v.findViewById(R.id.low);

        nickName.setText(String.valueOf(resultItem.getPlayers().size()));
        tableId.setText(resultItem.getTable_id());
        high.setText(resultItem.getHigh());
        low.setText(resultItem.getLow());

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
