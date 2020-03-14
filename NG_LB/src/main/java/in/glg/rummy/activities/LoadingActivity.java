package in.glg.rummy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import in.glg.rummy.R;
import in.glg.rummy.RummyApplication;
import in.glg.rummy.utils.Utils;

public class LoadingActivity extends BaseActivity {
   private static final String TAG = LoadingActivity.class.getSimpleName();
   private boolean isBackpressed = false;
   private Handler mHeartBeatHandler;
   private Handler mNetworkHandler;
   private RelativeLayout mRootLayout;

   private static  LoadingActivity mloadingactivity = null;

   private void launchSplashScreen() {
      this.launchNewActivity(new Intent(this, TransparentActivity.class), false);
   }

   private void resetNetworkHandler() {
      if(this.mNetworkHandler != null) {
         this.mNetworkHandler.removeCallbacks((Runnable)null);
         this.mNetworkHandler.removeCallbacksAndMessages((Object)null);
         this.mNetworkHandler = null;
      }
   }

   private void setNetworkConnectionTimer() {
      this.resetNetworkHandler();
      this.mNetworkHandler = new Handler();
      this.mNetworkHandler.postDelayed(new Runnable() {
         public void run() {
            LoadingActivity.this.startGameEngine();
         }
      }, 2000L);
   }

   private void startGameEngine() {
      if(Utils.isNetworkAvailable(this)) {
         (new SetupAsyncTask()).execute(new Void[0]);
      } else {
         this.setNetworkConnectionTimer();
      }
   }

   protected int getLayoutResource() {
      return R.layout.activity_loading;
   }

   protected int getToolbarResource() {
      return 0;
   }

   public void onBackPressed() {
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.e("vikas","Calling loading activity");
      mloadingactivity = this;
      setUpFullScreen();
      this.mRootLayout = (RelativeLayout) findViewById(R.id.pop_up_bg_layout);
      TextView messageTv = (TextView) findViewById(R.id.loading_tv);
      RummyApplication app = (RummyApplication) getApplication();
      Bundle bundle = getIntent().getExtras();
      boolean isSocketDisconnected = false;
      if (bundle != null) {
         isSocketDisconnected = bundle.getBoolean("isSocketDisconnected");
      }
      if (isSocketDisconnected) {
         messageTv.setVisibility(View.VISIBLE);
         if (app != null && app.getJoinedTableIds().size() > 0) {
            messageTv.setText("Oops.you have lost your internet connection.Your game has been set to Auto Play,please wait while we attempt to reconnect.");
         }
      } else {
         //this.mRootLayout.setBackgroundResource(ContextCompat.getColor(this, R.color.transparent));
         this.mRootLayout.setBackgroundResource(R.color.transparent);
         messageTv.setVisibility(View.GONE);
      }
      startGameEngine();
   }

   public class SetupAsyncTask extends AsyncTask<Void, Void, Void> {
      protected void onPreExecute() {
         super.onPreExecute();
         LoadingActivity.this.resetNetworkHandler();
      }

      protected Void doInBackground(Void... params) {
         SystemClock.sleep(1000);
         return null;
      }

      protected void onPostExecute(Void result) {
         super.onPostExecute(result);
         LoadingActivity.this.launchSplashScreen();
      }
   }

   public static Activity getInstance()
   {
      return mloadingactivity;
   }
}
