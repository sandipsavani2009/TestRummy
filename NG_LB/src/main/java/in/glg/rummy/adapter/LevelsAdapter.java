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
import in.glg.rummy.models.Levels;
import in.glg.rummy.utils.Utils;

/**
 * Created by GridLogic on 12/12/17.
 */

public class LevelsAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentDetailsFragment mTournamentDetailsFragment;
    private List<Levels> levels;
    private String status;


    public LevelsAdapter(Context context, List<Levels> levels, TournamentDetailsFragment tournamentDetailsFragment)
    {
        this.context = context;
        this.levels = levels;
        this.mTournamentDetailsFragment = tournamentDetailsFragment;
        this.status = status;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.levels.size();
    }

    @Override
    public Levels getItem(int position) {
        return (Levels) this.levels.get(position);
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
            v = this.inflater.inflate(R.layout.tourney_levels_adapter_item, parent, false);
        } else {
            v = convertView;
        }
        final Levels levels = getItem(position);


        TextView level_tv = (TextView) v.findViewById(R.id.level_tv);
        TextView scheduleTime_tv = (TextView) v.findViewById(R.id.scheduleTime_tv);
        TextView bet_tv = (TextView) v.findViewById(R.id.bet_tv);
        TextView qualify_tv = (TextView) v.findViewById(R.id.qualify_tv);
        TextView rebuy_tv = (TextView) v.findViewById(R.id.rebuy_tv);

        level_tv.setText(String.valueOf(position+1));
        scheduleTime_tv.setText(Utils.convertTimeStampToAnyDateFormat(levels.getStart_time(), "hh:mm") + " to "+
                                Utils.addSecondsToDate(levels.getStart_time(), "hh:mm", levels.getTime_duration()));
        bet_tv.setText(levels.getBet());
        qualify_tv.setText(levels.getQualifying_points());

        if(levels.getLevel_buying().equalsIgnoreCase("0.00") || levels.getLevel_buying().equalsIgnoreCase("0.0") ||
                levels.getLevel_buying().equalsIgnoreCase("0"))
            rebuy_tv.setText("NA");
        else
            rebuy_tv.setText(levels.getLevel_buying());


        //Log.d("local", "Intervals: "+levels.getDelay_between_level()+ " at "+String.valueOf(position+1));

        return v;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
