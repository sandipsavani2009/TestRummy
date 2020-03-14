package in.glg.rummy.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

public class TransferAnimation extends Animation {
   View destinationView;
   long duration;
   TimeInterpolator interpolator;
   AnimationListener listener;
   ViewGroup parentView;
   int transX;
   int transY;

   public TransferAnimation(View view) {
      this.view = view;
      this.destinationView = null;
      this.interpolator = new AccelerateDecelerateInterpolator();
      this.duration = 50L;
      this.listener = null;
   }

   public void animate() {
      this.parentView = (ViewGroup)this.view.getParent();
      ViewGroup rootView = (ViewGroup) this.view.getRootView();
      while (!this.parentView.equals(rootView)) {
         this.parentView.setClipChildren(false);
         this.parentView = (ViewGroup) this.parentView.getParent();
      }
      rootView.setClipChildren(false);
      int[] locationDest = new int[2];
      int[] locationView = new int[2];
      this.view.getLocationOnScreen(locationView);
      this.destinationView.getLocationOnScreen(locationDest);
      this.transX = locationDest[0] - locationView[0];
      this.transY = locationDest[1] - locationView[1];
      this.view.animate().scaleX(1.0F).scaleY(1.0F).translationX((float)this.transX).translationY((float)this.transY).setInterpolator(this.interpolator).setDuration(this.duration).setListener(new AnimatorListenerAdapter() {
         public void onAnimationEnd(Animator animator) {
            if(TransferAnimation.this.getListener() != null) {
               TransferAnimation.this.getListener().onAnimationEnd(TransferAnimation.this);
            }

         }

         public void onAnimationStart(Animator animator) {
            if(TransferAnimation.this.getListener() != null) {
               TransferAnimation.this.getListener().onAnimationStart(TransferAnimation.this);
            }

         }
      });
   }

   public View getDestinationView() {
      return this.destinationView;
   }

   public long getDuration() {
      return this.duration;
   }

   public TimeInterpolator getInterpolator() {
      return this.interpolator;
   }

   public AnimationListener getListener() {
      return this.listener;
   }

   public TransferAnimation setDestinationView(View view) {
      this.destinationView = view;
      return this;
   }

   public TransferAnimation setDuration(long duration) {
      this.duration = duration;
      return this;
   }

   public TransferAnimation setInterpolator(TimeInterpolator interpolator) {
      this.interpolator = interpolator;
      return this;
   }

   public TransferAnimation setListener(AnimationListener listener) {
      this.listener = listener;
      return this;
   }
}
