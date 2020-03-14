package in.glg.rummy.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.glg.rummy.CommonMethods.CommonMethods;
import in.glg.rummy.NetworkProvider.VolleySingleton;
import in.glg.rummy.R;
import in.glg.rummy.activities.HomeActivity;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;

/**
 * Created by GridLogic on 27/12/17.
 */

public class GameLogsFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private String TAG = GameLogsFragment.class.getName();

    private Spinner duration_spinner;
    private LinearLayout dateSelector;
    private LinearLayout container;
    private LinearLayout llDropIcon;
    private EditText fromDate, toDate;
    private Button submit_btn;
    private ProgressBar progress;
    private CheckBox wager_cb, win_cb;
    private CheckBox fun_cb, cash_cb, bonus_cb, loyalty_cb, social_cb;
    private CheckBox fun_to_cb, cash_to_cb, bonus_to_cb, loyalty_to_cb, social_to_cb;

    private LinearLayout today;
    private LinearLayout past;
    private View today_selector;
    private View past_selector;
    private View duration_layout;

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private static String TOKEN = "";
    SimpleDateFormat dmyFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat dmyFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
    private static String dateSTR = "";

    StringRequest stringRequest;

    String[] durationList = { "Yesterday", "Last 7 days", "Last 30 days", "This Month", "Last Month", "Custom" };
    protected RequestQueue queue;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_game_logs, container, false);
        handleBackButton(v);
        setIdsToViews(v);
        setListners(v);
        setDateTimeField();
        this.TOKEN = PrefManager.getString(getContext(), ACCESS_TOKEN_REST, "");
        ((LinearLayout) v.findViewById(R.id.back_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GameLogsFragment.this.popFragment(GameLogsFragment.class.getName());
            }
        });

        duration_spinner.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, durationList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration_spinner.setAdapter(aa);

        showView(progress);
        getData("off", "off", "off", "off", "off", "off", "off", "off",
                "off", "off", "off", "off", "Today", "", "");

        return v;
    }

    private void setDateTimeField()
    {
        Calendar newCalendar = Calendar.getInstance();

        fromDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fromDate.setText(dmyFormat.format(newDate.getTime()));

            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        toDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                toDate.setText(dmyFormat.format(newDate.getTime()));

            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private void setListners(View v) {
        fromDate.setOnClickListener(this);
        toDate.setOnClickListener(this);
        submit_btn.setOnClickListener(this);
        llDropIcon.setOnClickListener(this);
        past.setOnClickListener(this);
        today.setOnClickListener(this);
    }

    private void setIdsToViews(View v) {
        duration_spinner = (Spinner) v.findViewById(R.id.duration_spinner);
        dateSelector = (LinearLayout) v.findViewById(R.id.dateSelector);
        fromDate = (EditText) v.findViewById(R.id.fromDate);
        toDate = (EditText) v.findViewById(R.id.toDate);
        submit_btn = (Button) v.findViewById(R.id.submit_btn);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        container = (LinearLayout) v.findViewById(R.id.container);
        llDropIcon = (LinearLayout) v.findViewById(R.id.llDropIcon);

        wager_cb = (CheckBox) v.findViewById(R.id.wager_cb);
        win_cb = (CheckBox) v.findViewById(R.id.win_cb);

        fun_cb = (CheckBox) v.findViewById(R.id.fun_cb);
        cash_cb = (CheckBox) v.findViewById(R.id.cash_cb);
        bonus_cb = (CheckBox) v.findViewById(R.id.bonus_cb);
        loyalty_cb = (CheckBox) v.findViewById(R.id.loyalty_cb);
        social_cb = (CheckBox) v.findViewById(R.id.social_cb);

        fun_to_cb = (CheckBox) v.findViewById(R.id.fun_to_cb);
        cash_to_cb = (CheckBox) v.findViewById(R.id.cash_to_cb);
        bonus_to_cb = (CheckBox) v.findViewById(R.id.bonus_to_cb);
        loyalty_to_cb = (CheckBox) v.findViewById(R.id.loyalty_to_cb);
        social_to_cb = (CheckBox) v.findViewById(R.id.social_to_cb);

        past = (LinearLayout) v.findViewById(R.id.past);
        today = (LinearLayout) v.findViewById(R.id.today);
        past_selector = (View) v.findViewById(R.id.past_selector);
        today_selector = (View) v.findViewById(R.id.today_selector);

        duration_layout = v.findViewById(R.id.duration_layout);
    }

    private void handleBackButton(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                Log.e("flagBackKey@u3", HomeActivity.flagBackKey+"");
                if(HomeActivity.flagBackKey == 1){
                    HomeActivity.flagBackKey =0;
                    return true;
                }
                HomeActivity.flagBackKey =1;
                if (keyCode != 4) {
                    return false;
                }
                GameLogsFragment.this.popFragment(GameLogsFragment.class.getName());
                return true;
            }
        });
    }

    private void getData(final String wager, final String win, final String fromFun, final String fromCash, final String fromBonus, final String fromLoyalty, final String fromSocial,
                         final String toFun, final String toCash, final String toBonus, final String toLoyalty, final String toSocial,
                         final String duration, final String fromDate, final String toDate)
    {
        try
        {
            if(stringRequest!=null)
                queue.cancelAll(stringRequest);

            queue = VolleySingleton.getInstance(getContext()).getRequestQueue();

            String apiURL = Utils.SERVER_ADDRESS+"api/v1/rummy-gamelogs-history/";

            stringRequest = new StringRequest(Request.Method.POST, apiURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.d(TAG, "Response: "+response.toString());

                            hideView(progress);
                            showView(container);

                            populateData(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error Resp: " + error.toString());
                            hideView(progress);

                            if(error.toString().contains("NoConnectionError"))
                                CommonMethods.showSnackbar(submit_btn, "Oops! Connectivity Problem. Please try again!");

                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError && response != null) {
                                try {
                                    String res = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    Log.d(TAG, "Error: "+res);
                                    if(res != null) {
                                        try{
                                            JSONObject json = new JSONObject(res.toString());
                                            if(json.getString("status").equalsIgnoreCase("Error"))
                                                Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                                            container.removeAllViews();
                                        }
                                        catch (Exception e){
                                            Log.e(TAG, "EXP: parsing error for login -->> "+e.toString());
                                        }
                                    }
                                } catch (UnsupportedEncodingException e1) {
                                    // Couldn't properly decode data to string
                                    e1.printStackTrace();
                                }
                            }
                        }
                    })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Token "+TOKEN);
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Wager", wager);
                    params.put("Win", win);

                    params.put("FromFun", fromFun);
                    params.put("FromCash", fromCash);
                    params.put("FromBonus", fromBonus);
                    params.put("FromLoyalty", fromLoyalty);
                    params.put("FromSocial", fromSocial);

                    params.put("ToFun", toFun);
                    params.put("ToCash", toCash);
                    params.put("ToBonus", toBonus);
                    params.put("ToLoyalty", toLoyalty);
                    params.put("ToSocial", toSocial);

                    params.put("customid", duration);

                    if(duration.equalsIgnoreCase("Custom")){
                        params.put("fromdate", fromDate);
                        params.put("todate", toDate);
                    }
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //TIMEOUT INTERVAL (Default: 2500ms)
                    2,    //No.Of Retries (Default: 1)
                    2));  //BackOff Multiplier (Default: 1.0)

            //add request to queue
            queue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateData(String response)
    {
        try
        {
            JSONObject mainObject = new JSONObject(response.toString());
            if(mainObject.getString("status").equalsIgnoreCase("Success"))
            {
                JSONArray depositList = mainObject.getJSONObject("data").getJSONArray("gamelogs_list");

                if(depositList.length()>0)
                {
                    for(int i=0; i<depositList.length(); i++)
                    {
                        //addRows(depositList.getJSONObject(i), i, depositList.length());
                        addRows(depositList.getJSONObject(i), i, depositList.length());
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "No DepositResponseData Available", Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e){
            Log.e(TAG, "EXP: populateData -->> "+e.toString());
        }
    }

    private void addRows(JSONObject object, int position, int totalSize)
    {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.logs_adapter_item, null);

        final  TextView date = (TextView) addView.findViewById(R.id.date);
        final  TextView time = (TextView) addView.findViewById(R.id.time);
        final  TextView tv_1 = (TextView) addView.findViewById(R.id.tv_1);
        final  TextView tv_2 = (TextView) addView.findViewById(R.id.tv_2);
        final  TextView tv_3 = (TextView) addView.findViewById(R.id.tv_3);
        final  TextView tv_4 = (TextView) addView.findViewById(R.id.tv_4);
        final  TextView tv_5 = (TextView) addView.findViewById(R.id.tv_5);
        final  TextView tv_6 = (TextView) addView.findViewById(R.id.tv_6);

        try
        {
            dateSTR = object.getString("timestamp");

            try {
                date.setText(String.valueOf(dateFormat.format(dmyFormat2.parse(dateSTR))));
            } catch (Exception e){ }
            try {
                time.setText(String.valueOf(timeFormat.format(dmyFormat2.parse(dateSTR))));
            } catch (Exception e){ }

            tv_1.setText("#"+object.getString("gameid"));
            tv_2.setText("TID: "+object.getString("tableid"));
            tv_3.setText(object.getString("gametype").replace("_", " "));
            tv_4.setText(object.getString("transactiontype"));
            if(object.getString("streamtype").toLowerCase().contains("cash"))
                tv_5.setText(getResources().getString(R.string.rupee_symbol)+object.getString("amount"));
            else
                tv_5.setText(object.getString("amount"));
            tv_6.setText(object.getString("streamtype"));

            if(object.getString("transactiontype").toLowerCase().contains("winning")){
                date.setTextColor(getResources().getColor(R.color.green));
                tv_1.setTextColor(getResources().getColor(R.color.green));
                tv_3.setTextColor(getResources().getColor(R.color.green));
                tv_5.setTextColor(getResources().getColor(R.color.green));
            }
        }
        catch (Exception e){
            Log.e(TAG, "EXP in adding rows: "+e.toString());
        }

        container.addView(addView);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String itemSelected = this.duration_spinner.getSelectedItem().toString();
        Log.d(TAG, "item Selected: "+itemSelected);

        container.removeAllViews();

        if(itemSelected.equalsIgnoreCase("Custom"))
        {
            dateSelector.setVisibility(View.VISIBLE);
            Date date = new Date();
            toDate.setText(dmyFormat.format(date));
        }
        else
        {
            getResults();
            dateSelector.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view)
    {
        if(view==fromDate)
        {
            fromDatePickerDialog.show();
        }
        else if(view==llDropIcon)
        {
            duration_spinner.performClick();
        }
        else if(view==toDate)
        {
            toDatePickerDialog.show();
        }
        else if(view==submit_btn)
        {
           getResults();
        }
        else if(view==past){
            toggleSelection(past_selector, today_selector);
            showView(duration_layout);
        }
        else if(view==today){
            toggleSelection(today_selector, past_selector);
            hideView(duration_layout);
            showView(progress);
            getData("off", "off", "off", "off", "off", "off", "off", "off",
                    "off", "off", "off", "off", "Today", "", "");
        }
    }

    private void toggleSelection(View activeView, View inactiveView)
    {
        if(stringRequest!=null)
            stringRequest.cancel();
        hideView(progress);
        container.removeAllViews();
        activeView.setVisibility(View.VISIBLE);
        inactiveView.setVisibility(View.INVISIBLE);
    }

    private void getResults()
    {
        container.removeAllViews();

        String wager="off", win="off", duration="";
        String fromFun="off", fromCash="off", fromBonus="off", fromLoyalty="off", fromSocial="off";
        String toFun="off", toCash="off", toBonus="off", toLoyalty="off", toSocial="off";

        if(this.wager_cb.isChecked())
            wager = "on";
        if(this.win_cb.isChecked())
            win = "on";

        if(this.fun_cb.isChecked())
            fromFun = "on";
        if(this.cash_cb.isChecked())
            fromCash = "on";
        if(this.bonus_cb.isChecked())
            fromBonus = "on";
        if(this.loyalty_cb.isChecked())
            fromLoyalty = "on";
        if(this.social_cb.isChecked())
            fromSocial = "on";

        if(this.fun_to_cb.isChecked())
            toFun = "on";
        if(this.cash_to_cb.isChecked())
            toCash = "on";
        if(this.bonus_to_cb.isChecked())
            toBonus = "on";
        if(this.loyalty_to_cb.isChecked())
            toLoyalty = "on";
        if(this.social_to_cb.isChecked())
            toSocial = "on";

        duration = this.duration_spinner.getSelectedItem().toString();

        int customErrorFlag=0;

        if(duration.equalsIgnoreCase("Last 7 days"))
            duration = "WeekTildate";
        else if(duration.equalsIgnoreCase("Last 30 days"))
            duration = "Last 30 Days";
        else if(duration.equalsIgnoreCase("This year"))
            duration = "TillYear";
        else if(duration.equalsIgnoreCase("This Month"))
            duration = "MonthTilldate";
        else if(duration.equalsIgnoreCase("Last Month"))
            duration = "LastMonth";
        else if(duration.equalsIgnoreCase("Custom"))
        {
            if(Utils.isEditTextEmpty(fromDate) || Utils.isEditTextEmpty(toDate))
            {
                customErrorFlag++;
                CommonMethods.showSnackbar(submit_btn, "Please specify date range");
            }
        }

        if(!duration.equalsIgnoreCase("") && !duration.equalsIgnoreCase("-- Select --") && customErrorFlag==0)
        {
            showView(progress);
            container.removeAllViews();
            getData(wager, win, fromFun, fromCash, fromBonus, fromLoyalty, fromSocial, toFun, toCash, toBonus, toLoyalty, toSocial
                    ,duration, fromDate.getText().toString(), toDate.getText().toString());
        }
        else if(!duration.equalsIgnoreCase("Custom"))
            CommonMethods.showSnackbar(submit_btn, "Please specify duration");
    }
}
