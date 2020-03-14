package in.glg.rummy.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.glg.rummy.NetworkProvider.VolleySingleton;
import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.activities.HomeActivity;
import in.glg.rummy.api.response.LoginResponse;
import in.glg.rummy.fragments.LobbyFragment;
import in.glg.rummy.models.Table;
import in.glg.rummy.utils.PrefManager;
import in.glg.rummy.utils.Utils;

import static in.glg.rummy.utils.RummyConstants.ACCESS_TOKEN_REST;
import static in.glg.rummy.utils.RummyConstants.UNIQUE_ID_REST;

public class LobbyAdapter extends BaseAdapter
{
   private static final String TAG = LobbyAdapter.class.getSimpleName();
   private Context context;
   private LayoutInflater inflater;
   private LobbyFragment mLobbyFragment;
   private List<Table> tables;

   public LobbyAdapter(Context ctx, List<Table> tables, LobbyFragment lobbyFragment) {
      this.context = ctx;
      this.tables = tables;
      this.mLobbyFragment = lobbyFragment;
      this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   private void setUserSeating(Table t, ImageView user1, ImageView user2, ImageView user3, ImageView user4, ImageView user5, ImageView user6) {
      switch (Integer.parseInt(t.getCurrent_players())) {
         case 0:
            user1.setImageResource(R.drawable.lobby_player_off);
            user2.setImageResource(R.drawable.lobby_player_off);
            user3.setImageResource(R.drawable.lobby_player_off);
            user4.setImageResource(R.drawable.lobby_player_off);
            user5.setImageResource(R.drawable.lobby_player_off);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 1:
            user1.setImageResource(R.drawable.lobby_player_off);
            user2.setImageResource(R.drawable.lobby_player_off);
            user3.setImageResource(R.drawable.lobby_player_off);
            user4.setImageResource(R.drawable.lobby_player_on);
            user5.setImageResource(R.drawable.lobby_player_off);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 2:
            user1.setImageResource(R.drawable.lobby_player_on);
            user2.setImageResource(R.drawable.lobby_player_off);
            user3.setImageResource(R.drawable.lobby_player_off);
            user4.setImageResource(R.drawable.lobby_player_on);
            user5.setImageResource(R.drawable.lobby_player_off);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 3:
            user1.setImageResource(R.drawable.lobby_player_on);
            user2.setImageResource(R.drawable.lobby_player_on);
            user3.setImageResource(R.drawable.lobby_player_on);
            user4.setImageResource(R.drawable.lobby_player_off);
            user5.setImageResource(R.drawable.lobby_player_off);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 4:
            user1.setImageResource(R.drawable.lobby_player_on);
            user2.setImageResource(R.drawable.lobby_player_on);
            user3.setImageResource(R.drawable.lobby_player_on);
            user4.setImageResource(R.drawable.lobby_player_on);
            user5.setImageResource(R.drawable.lobby_player_off);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 5:
            user1.setImageResource(R.drawable.lobby_player_on);
            user2.setImageResource(R.drawable.lobby_player_on);
            user3.setImageResource(R.drawable.lobby_player_on);
            user4.setImageResource(R.drawable.lobby_player_on);
            user5.setImageResource(R.drawable.lobby_player_on);
            user6.setImageResource(R.drawable.lobby_player_off);
            break;
         case 6:
            user1.setImageResource(R.drawable.lobby_player_on);
            user2.setImageResource(R.drawable.lobby_player_on);
            user3.setImageResource(R.drawable.lobby_player_on);
            user4.setImageResource(R.drawable.lobby_player_on);
            user5.setImageResource(R.drawable.lobby_player_on);
            user6.setImageResource(R.drawable.lobby_player_on);
            break;
      }
      if (Integer.parseInt(t.getMaxplayer()) == 2) {
         user1.setVisibility(View.VISIBLE);
         user2.setVisibility(View.INVISIBLE);
         user3.setVisibility(View.INVISIBLE);
         user4.setVisibility(View.VISIBLE);
         user5.setVisibility(View.INVISIBLE);
         user6.setVisibility(View.INVISIBLE);
         return;
      }
      user1.setVisibility(View.VISIBLE);
      user2.setVisibility(View.VISIBLE);
      user3.setVisibility(View.VISIBLE);
      user4.setVisibility(View.VISIBLE);
      user5.setVisibility(View.VISIBLE);
      user6.setVisibility(View.VISIBLE);
   }

   private void showBuyInPopUp(Table table) {
      try {
      String balance;
      RummyApplication app = (RummyApplication) ((HomeActivity) this.context).getApplication();
      LoginResponse userData = ((RummyApplication) context.getApplicationContext()).getUserData();
      int playerBalance;

      final DecimalFormat format = new DecimalFormat("0.#");
      if (table.getTable_cost().equalsIgnoreCase("CASH_CASH")) {
         balance = app.getUserData().getRealChips();
         playerBalance = (int) Float.parseFloat(userData.getRealChips());
      } else {
         balance = app.getUserData().getFunChips();
         playerBalance = (int) Float.parseFloat(userData.getFunChips());
      }
      final Dialog dialog = new Dialog(this.context);
      dialog.requestWindowFeature(1);
      dialog.setContentView(R.layout.table_details_pop_up);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
      dialog.show();
      TextView minBuyTv = (TextView) dialog.findViewById(R.id.min_buy_value_tv);
      TextView maxBuyTv = (TextView) dialog.findViewById(R.id.max_buy_value_tv);
      TextView balanceTv = (TextView) dialog.findViewById(R.id.balance_value_tv);
      final EditText buyInTv = (EditText) dialog.findViewById(R.id.buy_in_value_tv);
      ((TextView) dialog.findViewById(R.id.bet_value_tv)).setText(table.getBet()+"");
      minBuyTv.setText(table.getMinimumbuyin()+"");
      maxBuyTv.setText(table.getMaximumbuyin()+"");
      final String maximumBuyIn = table.getMaximumbuyin();
      final int max;
      if(playerBalance<Integer.parseInt(maximumBuyIn))
         max = playerBalance;
      else
         max = Integer.parseInt(maximumBuyIn);

      final int min = Integer.parseInt(table.getMinimumbuyin());

      boolean decreaseBalance = true;
      if (balance.contains(".")) {
         String subBalance = balance.substring(balance.lastIndexOf(".") + 1);
         if (subBalance != null && subBalance.length() > 0) {
            decreaseBalance = Integer.parseInt(subBalance) > 50;
         }
      }
      final float balanceInt = new Float((float) Math.round(Float.parseFloat(balance))).floatValue();
      balanceTv.setText(String.valueOf(format.format((double) balanceInt)));
      final Table table2 = table;
      ((Button) dialog.findViewById(R.id.join_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
            if (buyInTv.getText() == null || buyInTv.getText().length() <= 0) {
               LobbyAdapter.this.mLobbyFragment.showErrorPopUp("Please enter minimum amount");
               return;
            }
            float selectedBuyInAmt = Float.valueOf(buyInTv.getText().toString()).floatValue();
            if (selectedBuyInAmt <= balanceInt || selectedBuyInAmt >= Float.valueOf((float) max).floatValue()) {
               if (selectedBuyInAmt > Float.valueOf((float) max).floatValue()) {
                  LobbyAdapter.this.mLobbyFragment.showErrorPopUp("You can take only ( " + maximumBuyIn + " ) " + "in to the table");
               } else if (selectedBuyInAmt < Float.valueOf((float) min).floatValue()) {
                  LobbyAdapter.this.mLobbyFragment.showErrorPopUp("Please enter minimum amount");
               } else {
                  LobbyAdapter.this.mLobbyFragment.joinTable(table2, buyInTv.getText().toString());
               }
            } else if (table2.getTable_cost().contains("CASH_CASH")) {
               showErrorBalanceBuyChips("You don't have enough balance to join this table, please deposit now.");
               //LobbyAdapter.this.mLobbyFragment.showErrorChipsDialog(LobbyAdapter.this.context, String.format("%s - %s", new Object[]{"You don't have enough balance to join this table , please deposit from website", Utils.getWebSite()}));
            } else {
               LobbyAdapter.this.mLobbyFragment.showLowBalanceDialog(LobbyAdapter.this.context, "You don't have enough balance to join this table. Please click OK to reload your chips");
            }
         }
      });
      ((Button) dialog.findViewById(R.id.cancel_btn)).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();
         }
      });
      SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seek_bar);
      seekBar.setMax((max - min) / 1);
      seekBar.setProgress(seekBar.getMax());
      if (Float.valueOf((float) max).floatValue() <= balanceInt) {
         buyInTv.setText(String.valueOf(max));
      } else {
         float newBalance = balanceInt;
         if (decreaseBalance) {
            newBalance = balanceInt - 1.0f;
         }
         buyInTv.setText(String.valueOf(format.format((double) newBalance)));
      }
      final int i = min;
      final float f = balanceInt;
      final EditText editText = buyInTv;
      seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
         public void onStopTrackingTouch(SeekBar seekBar) {
         }

         public void onStartTrackingTouch(SeekBar seekBar) {
         }

         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String sliderValue = String.valueOf(format.format((double) (i + (progress * 1))));
            if (Float.valueOf(sliderValue).floatValue() >= f) {
               sliderValue = String.valueOf(format.format((double) f));
            }
            editText.setText(sliderValue);
         }
      });

      }catch (Exception e){
         Log.e(TAG+"",e+"");
      }
   }

   public int getCount() {
      return this.tables.size();
   }

   public Table getItem(int position) {
      return (Table) this.tables.get(position);
   }

   public long getItemId(int position) {
      return (long) position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      View v;
      if (convertView == null) {
         v = this.inflater.inflate(R.layout.lobby_adapter_item, parent, false);
      } else {
         v = convertView;
      }
      final Table t = getItem(position);
      String tableType = Utils.formatTableName(t.getTable_type());
      setUserSeating(t, (ImageView) v.findViewById(R.id.user_1), (ImageView) v.findViewById(R.id.user_2), (ImageView) v.findViewById(R.id.user_3), (ImageView) v.findViewById(R.id.user_4), (ImageView) v.findViewById(R.id.user_5), (ImageView) v.findViewById(R.id.user_6));
      ImageView playerIv = (ImageView) v.findViewById(R.id.lobby_players_iv);
      TextView seatingTv = (TextView) v.findViewById(R.id.table_siting);
      TextView joinTv = (TextView) v.findViewById(R.id.join_tv);
      TextView noOfPlayersTv = (TextView) v.findViewById(R.id.noOfPlayersTv);
      if (Integer.parseInt(t.getCurrent_players()) > 0) {
         playerIv.setImageResource(R.drawable.player_on);
//         seatingTv.setTextColor(ContextCompat.getColor(this.context, R.color.Orange));
//         joinTv.setTextColor(ContextCompat.getColor(this.context, R.color.Orange));
      } else {
         playerIv.setImageResource(R.drawable.lobby_players_gray);
//         seatingTv.setTextColor(ContextCompat.getColor(this.context, R.color.grey_dark));
//         joinTv.setTextColor(ContextCompat.getColor(this.context, R.color.grey_dark));
      }
      //noOfPlayersTv.setText(Html.fromHtml("<b>" + t.getTotal_player() + "</b>" + "<font color=#A1A09F>" + " Players" + "</font>"));
      noOfPlayersTv.setText(t.getTotal_player());
      ((TextView) v.findViewById(R.id.table_type)).setText(tableType);
      //((TextView) v.findViewById(R.id.table_bet)).setText("Bet: " + t.getBet() + " " + "Chips: " + WordUtils.capitalize(Utils.getChipsType(t.getTable_cost())));
      ((TextView) v.findViewById(R.id.betAmount)).setText(t.getBet());
      ((TextView) v.findViewById(R.id.chipsAmount)).setText(WordUtils.capitalize(Utils.getChipsType(t.getTable_cost())));

      seatingTv.setText(this.context.getString(R.string.seating) + " " + t.getCurrent_players() + " of " + t.getMaxplayer());
      ((TextView) v.findViewById(R.id.table_total_players)).setText(t.getTotal_player());
      v.setOnClickListener(new OnClickListener() {
         public void onClick(View v)
         {

            openConfirmDialog(t);


         }
      });
      return v;
   }

   private void getBalanceFromServer(final Table t)
   {
      this.mLobbyFragment.showLoadingDialog(context);
      try
      {

         final String TOKEN = PrefManager.getString(context, ACCESS_TOKEN_REST, "");
         RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();

         String apiURL = Utils.API_SERVER_ADDRESS+Utils.getBalanceUrl;
         Log.w(TAG, TOKEN);
         Log.w(TAG, apiURL);

         final StringRequest stringRequest = new StringRequest(Request.Method.GET, apiURL,
                 new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        mLobbyFragment.dismissLoadingDialog();
                       Log.e(TAG, "Response: "+response.toString());

                       try {
                          JSONObject jsonObject = new JSONObject(response.toString());

                          String status = jsonObject.getString("status");
                          String msg = jsonObject.getString("message");
                          if(status != null && status.equalsIgnoreCase("success"))
                          {
                             String real_chips = jsonObject.getString("balance");
                            // Log.w(TAG, "UNIQUE ID: "+jsonObject.getString("unique_id"));
                           //  PrefManager.saveString(context, UNIQUE_ID_REST, jsonObject.getString("unique_id"));
                             LoginResponse userData = ((RummyApplication) ((Activity)context).getApplication()).getUserData();
                             //userData.setFunChips("");
                             //userData.setFunInPlay("");
                             if(real_chips != null)
                             {
                                userData.setRealChips(real_chips);
                             }

                             //userData.setRealInPlay("");
                             //userData.setLoyalityChips("");
                             processToJoinGame(t);
                          }
                          else
                          {
                             if(msg != null)
                             {
                                Utils.showGenericMsgDialog(context,msg);
                             }

                          }


                       }
                       catch (Exception e){
                           Log.e("JsonException",""+e.toString());
                       }

                    }
                 },
                 new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                       Utils.showGenericMsgDialog(context,"Something went wrong, Please try again after some time");
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
         };

         stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                 DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, //TIMEOUT INTERVAL (Default: 2500ms)
                 2,    //No.Of Retries (Default: 1)
                 2));  //BackOff Multiplier (Default: 1.0)

         //add request to queue
         queue.add(stringRequest);

      } catch (Exception e) {

         Utils.showGenericMsgDialog(context,"Something went wrong, Please try again after some time");
         e.printStackTrace();
      }
   }


   private void processToJoinGame(Table t)
   {
      if(t.getJoined_players().contains(PrefManager.getString(context, "username", " ")))
      {
         if (!t.getTable_type().startsWith(Utils.PR)) {
            LobbyAdapter.this.mLobbyFragment.joinTable(t, "0");
         } else if (LobbyAdapter.this.mLobbyFragment.isFoundTable(t)) {
            LobbyAdapter.this.mLobbyFragment.setSelectedTable(t);
            LobbyAdapter.this.mLobbyFragment.launchTableActivity();
         } else {
            String balance;
            LobbyAdapter.this.mLobbyFragment.setSelectedTable(t);
            RummyApplication app = (RummyApplication) ((HomeActivity) LobbyAdapter.this.context).getApplication();
            DecimalFormat format = new DecimalFormat("0.#");
            if (t.getTable_cost().equalsIgnoreCase("CASH_CASH")) {
               balance = app.getUserData().getRealChips();
            } else {
               balance = app.getUserData().getFunChips();
            }
            if (new Float((float) Math.round(Float.parseFloat(balance))).floatValue() >= Float.valueOf(t.getMinimumbuyin()).floatValue()) {
               try {
                  LobbyAdapter.this.showBuyInPopUp(t);
               }catch (Exception e){
                  Log.e(TAG+"",e+"");
               }
            } else if (t.getTable_cost().contains("CASH_CASH")) {
               showErrorBalanceBuyChips("You don't have enough balance to join this table, please deposit now.");
               //LobbyAdapter.this.mLobbyFragment.showErrorChipsDialog(LobbyAdapter.this.context, String.format("%s - %s", new Object[]{"You don't have enough balance to join this table , please deposit from website", Utils.getWebSite()}));
            } else {
               LobbyAdapter.this.mLobbyFragment.showLowBalanceDialog(LobbyAdapter.this.context, "You don't have enough balance to join this table. Please click OK to reload your chips");
            }
         }
      }
      else
      {
         if (!t.getTable_type().startsWith(Utils.PR)) {
            LobbyAdapter.this.mLobbyFragment.joinTable(t, "0");
         } else if (LobbyAdapter.this.mLobbyFragment.isFoundTable(t)) {
            LobbyAdapter.this.mLobbyFragment.setSelectedTable(t);
            LobbyAdapter.this.mLobbyFragment.launchTableActivity();
         } else {
            String balance;
            LobbyAdapter.this.mLobbyFragment.setSelectedTable(t);
            RummyApplication app = (RummyApplication) ((HomeActivity) LobbyAdapter.this.context).getApplication();
            DecimalFormat format = new DecimalFormat("0.#");
            if (t.getTable_cost().equalsIgnoreCase("CASH_CASH")) {
               balance = app.getUserData().getRealChips();
            } else {
               balance = app.getUserData().getFunChips();
            }
            if (new Float((float) Math.round(Float.parseFloat(balance))).floatValue() >= Float.valueOf(t.getMinimumbuyin()).floatValue()) {
               try {
                  LobbyAdapter.this.showBuyInPopUp(t);
               }catch (Exception e){
                  Log.e(TAG+"",e+"");
               }
            } else if (t.getTable_cost().contains("CASH_CASH")) {
               showErrorBalanceBuyChips("You don't have enough balance to join this table, please deposit now.");
               //LobbyAdapter.this.mLobbyFragment.showErrorChipsDialog(LobbyAdapter.this.context, String.format("%s - %s", new Object[]{"You don't have enough balance to join this table , please deposit from website", Utils.getWebSite()}));
            } else {
               LobbyAdapter.this.mLobbyFragment.showLowBalanceDialog(LobbyAdapter.this.context, "You don't have enough balance to join this table. Please click OK to reload your chips");
            }
         }
        // openConfirmDialog(t);
      }

   }

   private void openConfirmDialog(final Table t)
   {
      final Dialog dialog = new Dialog(context, R.style.DialogTheme);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      dialog.setContentView(R.layout.dialog_confirm);

      TextView title = (TextView) dialog.findViewById(R.id.title);
      Button yes_btn = (Button) dialog.findViewById(R.id.yes_btn);
      Button no_btn = (Button) dialog.findViewById(R.id.no_btn);

      title.setText("Do you wish to join this table?");

      no_btn.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view) {
            dialog.dismiss();
         }
      });

      yes_btn.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View view)
         {
            dialog.dismiss();


            getBalanceFromServer(t);

         }
      });

      dialog.show();
   }

   public void notifyDataSetChanged() {
      super.notifyDataSetChanged();
   }

    public void showErrorBalanceBuyChips(String text)
    {
        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.requestWindowFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_error_balance_buy_chips);

        TextView label = (TextView) dialog.findViewById(R.id.dialog_msg_tv);
        Button deposit = (Button) dialog.findViewById(R.id.ok_btn);
        Button cancel = (Button) dialog.findViewById(R.id.cancel);

        label.setText(text);

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        deposit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                  dialog.dismiss();
                //  mLobbyFragment.checkPlayerDeposit(context);
            }
        });

        dialog.show();
    }




}
