package in.glg.rummy.utils;

/**
 * Created by GridLogic on 31/8/17.
 */

import android.content.Context;
import android.os.Vibrator;

public class VibrationManager {
    private static VibrationManager instance;
    private boolean mIsVibrate;
    private Vibrator mVibrator;
    private long[] pattern = new long[]{60, 100, 200, 300, 400};

    public static synchronized VibrationManager getInstance() {
        VibrationManager vibrationManager;
        synchronized (VibrationManager.class) {
            vibrationManager = instance;
        }
        return vibrationManager;
    }

    public static void CreateInstance() {
        if (instance == null) {
            instance = new VibrationManager();
        }
    }

    public void InitializeVibrator(Context context) throws Exception {
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
    }

    public void setVibration(boolean isVibrate) {
        this.mIsVibrate = isVibrate;
    }

    public void vibrate(int repeat) {
        if (this.mIsVibrate) {
            this.mVibrator.vibrate(100);
        }
    }
}