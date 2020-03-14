package in.glg.rummy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import in.glg.rummy.R;
import in.glg.rummy.fancycoverflow.FancyCoverFlow;
import in.glg.rummy.fancycoverflow.FancyCoverFlowAdapter;

public class GameTypeAdapter extends FancyCoverFlowAdapter {
   private Context mContext;
   private int[] mGameTypes = new int[]{R.drawable.strikes_rummy, R.drawable.pools_rummy, R.drawable.deals_rummy, R.drawable.tourney_prelobby};

   public GameTypeAdapter(Context context) {
      this.mContext = context;
   }

   public int getCount() {
      return this.mGameTypes.length;
   }

   public View getCoverFlowItem(int i, View reuseableView, ViewGroup parent) {
      ImageView imageView;
      if (reuseableView != null) {
         imageView = (ImageView) reuseableView;
      } else {
         imageView = new ImageView(parent.getContext());
         imageView.setScaleType(ScaleType.FIT_XY);
         imageView.setLayoutParams(new FancyCoverFlow.LayoutParams((int) (this.mContext.getResources().getDimension(R.dimen.cover_item_width) / this.mContext.getResources().getDisplayMetrics().density), (int) (this.mContext.getResources().getDimension(R.dimen.cover_item_height) / this.mContext.getResources().getDisplayMetrics().density)));
      }
      imageView.setImageResource(getItem(i).intValue());
      return imageView;
   }

   public Integer getItem(int i) {
      return Integer.valueOf(this.mGameTypes[i]);
   }

   public long getItemId(int i) {
      return (long) i;
   }
}
