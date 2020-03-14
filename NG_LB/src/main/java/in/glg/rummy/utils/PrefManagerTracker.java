package in.glg.rummy.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManagerTracker {
    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void saveBool(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key, String defValue) {
        return context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).getString(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        return context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).getInt(key, defValue);
    }

    public static boolean getBool(Context context, String key, boolean defValue) {
        return context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).getBoolean(key, defValue);
    }

    public static void createSharedPreferencesTracker(Context context)
    {
        SharedPreferences sharedPreferencesTracker = context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0); // 0 - for private mode

    }
    public static void clearPreferences(Context context) {
        context.getSharedPreferences(RummyConstants.PREFS_FILE_NAME_TRACK, 0).edit().clear().commit();
    }
}
