package in.glg.rummy.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import in.glg.rummy.R;
import in.glg.rummy.fragments.TournamentDetailsFragment;
import in.glg.rummy.fragments.TournamentsFragment;
import in.glg.rummy.models.Tournament;
import in.glg.rummy.utils.Utils;


/**
 * Created by GridLogic on 1/12/17.
 */

public class TournamentsAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private TournamentsFragment mTourneyFragment;
    private List<Tournament> tournaments;
    private List<Tournament> myFilteredList=null;  //

    public TournamentsAdapter(Context context, List<Tournament> tournaments, TournamentsFragment tournamentsFragment)
    {
        this.context = context;
        this.tournaments = tournaments;
        this.mTourneyFragment = tournamentsFragment;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.tournaments.size();
    }

    @Override
    public Tournament getItem(int position) {
        return (Tournament) this.tournaments.get(position);
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
            v = this.inflater.inflate(R.layout.tournament_adapter_item, parent, false);
        } else {
            v = convertView;
        }
        final Tournament tourney = getItem(position);

        LinearLayout llMainLayout = (LinearLayout) v.findViewById(R.id.llMainLayout);
        TextView entry_tv = (TextView) v.findViewById(R.id.entry_tv);
        TextView cashPrize_tv = (TextView) v.findViewById(R.id.cashPrize_tv);
        TextView startTime_tv = (TextView) v.findViewById(R.id.startTime_tv);
        TextView status_tv = (TextView) v.findViewById(R.id.status_tv);
        TextView players_tv = (TextView) v.findViewById(R.id.players_tv);
        TextView join_tv = (TextView) v.findViewById(R.id.join_tv);

        if(tourney.getEntry().equalsIgnoreCase("0") || tourney.getEntry().equalsIgnoreCase("0.0"))
            entry_tv.setText("Free");
        else
            entry_tv.setText(tourney.getEntry());

        cashPrize_tv.setText(tourney.getCashPrize());
        startTime_tv.setText(Utils.convertTimeStampToAnyDateFormat(tourney.getStartDate(), "dd MMM yy, hh:mm aa"));
        status_tv.setText(Utils.toTitleCase(tourney.getStatus()));
        players_tv.setText(tourney.getPlayers());
        join_tv.setTextColor(ContextCompat.getColor(this.context, R.color.colorAccent));

        if(tourney.getStatus().equalsIgnoreCase("running") || tourney.getStatus().equalsIgnoreCase("completed")
                || tourney.getStatus().equalsIgnoreCase("canceled") || tourney.getStatus().equalsIgnoreCase("registrations closed"))
            join_tv.setText("VIEW");
        else
            join_tv.setText("JOIN");

        if(tourney.getReg_status()!=null && tourney.getReg_status().equalsIgnoreCase("True"))
            llMainLayout.setBackgroundColor(ContextCompat.getColor(this.context, R.color.transparent));
        else
            llMainLayout.setBackgroundColor(ContextCompat.getColor(this.context, R.color.transparent));

        join_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                TournamentDetailsFragment tdf = new TournamentDetailsFragment();
                Bundle args = new Bundle();
                args.putString("tourneyID", tourney.getTourneyId());
                tdf.setArguments(args);
                TournamentsAdapter.this.mTourneyFragment.launchTDFragment(tdf, TournamentDetailsFragment.class.getName());
            }
        });

        return v;
    }



    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void addNewItem(Tournament tournament){
        //this.tournaments.add(tournament);
        notifyDataSetChanged();
    }

    public void filter(String filterText, List<Tournament> actualList)
    {
        List<Tournament> tempList = new ArrayList<>();
        myFilteredList = actualList;

        if (filterText.equalsIgnoreCase("ALL")) {
            tournaments = actualList;
        }
        else if(filterText.equalsIgnoreCase("FREE"))
        {
            for (Tournament tournament : myFilteredList) {
                if (tournament.getEntry().equalsIgnoreCase("0")) {
                    tempList.add(tournament);
                }
            }
            tournaments = tempList;
        }
        else if(filterText.equalsIgnoreCase("CASH"))
        {
            for (Tournament tournament : myFilteredList) {
                if (!tournament.getEntry().equalsIgnoreCase("0")) {
                    tempList.add(tournament);
                }
            }
            tournaments = tempList;
        }
        notifyDataSetChanged();
    }
}
