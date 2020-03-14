package in.glg.rummy.utils;

/**
 * Created by GridLogic on 31/8/17.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.glg.rummy.R;
import in.glg.rummy.api.UrlBuilder;
import in.glg.rummy.api.builder.xml.CommonXmlBuilder;
import in.glg.rummy.api.builder.xml.XmlInterface;
import in.glg.rummy.models.EngineRequest;
import in.glg.rummy.models.Event;
import in.glg.rummy.models.GameInfo;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.apache.commons.lang3.StringUtils.capitalize;

public class Utils {
    private static String TAG = Utils.class.getName();

    public static int BET = 3;
    public static int CHIPS = 2;
    public static String CLUB = "c";
    public static String DIAMOND = "d";
    public static int GAME_TYPE = 1;
    public static final String GAME_TYPE_101 = "101";
    public static final String GAME_TYPE_201 = "201";
    public static final String GAME_TYPE_BEST_OF_2 = "Best of 2";
    public static final String GAME_TYPE_BEST_OF_3 = "Best of 3";
    public static final String GAME_TYPE_BEST_OF_6 = "Best of 6";
    public static final String GAME_TYPE_PR_JOKER = "Joker";
    public static final String GAME_TYPE_PR_NO_JOKER = "No Joker";
    public static String HEART = "h";
    public static boolean HOME_BACK_PRESSED = false;
    public static long MELD_TIMER_REMAINING = 0;
    public static int PLAYERS = 4;
    public static final String PR = "PR";
    public static final String PR_JOKER = "PR_JOKER";
    public static final String PR_NO_JOKER = "PR_NOJOKER";
    public static boolean SHOW_LOBBY = false;
    public static String SPADE = "s";
    public static int TIMER_INTERVAL = 1000;
    public static int VARIANT = 0;
    public static final String ZOPIM_CHAT_ACCOUNT_KEY = "2Vj6FS3wY2GXOGUlzVrEEw5whxfszYDj";
    public static boolean isFromSocketDisconnection = false;
    public static ArrayList<Event> tableDetailsList = new ArrayList();

    //Server Url
//    public static String SERVER_ADDRESS = "https://www.tajrummy.com/";
                public static String SERVER_ADDRESS = "http://rh.glserv.info/";

                public static String API_SERVER_ADDRESS = "http://api.glserv.info/";


                public static String getBalanceUrl = "api/v1/update-user-balance";
                public static String checkSumUrl = "api/v1/get-checksum/";
                public static String getUserInfoUrl = "api/v1/get-user-info/";


//      Engine IP
//    public static String ENGINE_IP = "180.179.43.251";
//    public static String ENGINE_IP = "54.88.243.113";
    public static String ENGINE_IP = "";
    //  License Code
//    public static String WEBENGAGE_LICENSE_CODE = "aa1325b9";      // LIVE KEY
              public static String WEBENGAGE_LICENSE_CODE = "82618168";     // TEST KEY
    // Events
//    public static String events_url = "https://www.tajrummynetwork.com/egcs/track-game-eventlogs/";
    public static String events_url = "http://trn.glserv.info/egcs/track-game-eventlogs/";


    public static String DATE_IS_AFTER = "greaterDate";
    public static String DATE_IS_BEFORE = "beforeDate";
    public static String DATE_IS_EQUAL = "equalDate";

    public static String CLIENT_TYPE = "apk";       // android or apk

    public static EngineRequest MELD_REQUEST = null;
    public static Event SHOW_EVENT = null;
    public static boolean DEAL_SENT = false;

    public static String GOOGLE_CLIENT_ID = "175815169646-vj2km1e1n2q3ff1g59o7dkijrlqg5ma8.apps.googleusercontent.com";
    public static String PR_JOKER_POINTS = "0";

    public static String PRIVACY_URL = "https://www.tajrummy.com/13cards-rummy-privacy-policy/";
    public static String TERMS_CONDITIONS_URL = "https://www.tajrummy.com/online-rummy-terms&conditions/";
    public static String APK_DOWNLOAD_URL = "https://www.tajrummy.com/rummy-signup-2500/";

    public static boolean FLAG_OPPOSITE_USER = true;

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public static <T> String getObjXMl(XmlInterface<T> entity) {
        return new CommonXmlBuilder().getXmlForEntity(entity);
    }

    public static <T> T getObject(String xmlobj, Class<? extends T> entity) {
        return new CommonXmlBuilder().getEntityForXml(xmlobj, entity);
    }

    public static void registerBus(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unRegisterBus(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void sendEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public static void sendRequest(Object event) {
        EventBus.getDefault().post(event);
    }

    public static Rect getViewLocation(View v) {
        int[] loc_int = new int[2];
        if (v == null) {
            return null;
        }
        try {
            v.getLocationOnScreen(loc_int);
            Rect location = new Rect();
            location.left = loc_int[0];
            location.top = loc_int[1];
            location.right = location.left + v.getWidth();
            location.bottom = location.top + v.getHeight();
            return location;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static int convertDpToPixel(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertPixelsToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static String formatString(String string) {
        return string.split("\\.")[0];
    }

    public static String formatPrizeMoney(String string) {
        return new DecimalFormat("#.####").format(Double.parseDouble(string));
    }

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static String formatTableName(String tableType) {
        if (!tableType.startsWith(PR)) {
            return WordUtils.capitalize(tableType.replaceAll("_", " ").toLowerCase());
        }
        String[] prJokerStr = tableType.toLowerCase().split("_");
        return String.format("%s - %s", new Object[]{prJokerStr[0].toUpperCase(), WordUtils.capitalize(prJokerStr[1])});
    }

    public static String getTableType(String tableCost) {
        if (tableCost.contains("FUN")) {
            return "(FREE)";
        }
        return "(CASH)";
    }

    public static String getVariantType(String tableType) {
        if (tableType == null || tableType.length() <= 0) {
            return null;
        }
        if (tableType.contains("POOL") || tableType.contains("BEST_OF_3")) {
            return "Pools";
        }
        if (tableType.contains("BEST_OF_2") || tableType.contains("BEST_OF_6")) {
            return "Deals";
        }
        if (tableType.contains(PR)) {
            return "Points";
        }
        return null;
    }

    public static GameInfo getGameInfo(String tableType) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setFullCount("80");
        gameInfo.setExtraTime("15 Sec");
        gameInfo.setMoveTime("30 Sec");
        gameInfo.setMaxExtraTime("90 Sec");
        if (tableType.equalsIgnoreCase("101_POOL")) {
            gameInfo.setFirstDrop("20");
            gameInfo.setMiddleDrop("40");
        } else if (tableType.equalsIgnoreCase("201_POOL")) {
            gameInfo.setFirstDrop("25");
            gameInfo.setMiddleDrop("50");
        } else if (tableType.contains("BEST_OF")) {
            gameInfo.setFirstDrop("NO");
            gameInfo.setMiddleDrop("NO");
        } else if (tableType.contains(PR)) {
            gameInfo.setFirstDrop("20 x Bet value");
            gameInfo.setMiddleDrop("40 x Bet value");
            gameInfo.setFullCount("80 x Bet value");
            gameInfo.setExtraTime("15 Sec");
            gameInfo.setMoveTime("40 Sec");
            gameInfo.setMaxExtraTime("60 Sec");
        }
        return gameInfo;
    }

    public static List<Event> removeDuplicateEvents(List<Event> input) {
        List<Event> listEvents = new ArrayList();
        listEvents.addAll(input);
        return new ArrayList(new LinkedHashSet(listEvents));
    }

    public static String getVersionNumber(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "Unable to get the version";
        }
    }

    public static String getChipsType(String tableCost) {
        if (tableCost.contains("FUN")) {
            return "Free";
        }
        return "Cash";
    }

    public static String getWebSite() {
        String webSite = "";
        if (UrlBuilder.BASE_URL.contains("devserv")) {
            return UrlBuilder.TEST_WEBSITE;
        }
        return UrlBuilder.LIVE_WEBSITE;
    }

    public static String convertTimeStampToAnyDateFormat(String inputDate, String format) {
        String outputDate = "";
        try {
            inputDate = inputDate.substring(0, inputDate.length() - 2);
            long timeStamp = Long.parseLong(inputDate);
            Date d = new Date(timeStamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat(format); // Set your date format
            outputDate = sdf.format(d);
            return outputDate;
        } catch (Exception e) {
            Log.e("local", "EXP: converting timestamp to any date: " + e.toString());
        }
        return outputDate;
    }

    public static String addSecondsToDate(String inputDate, String format, String seconds) {
        inputDate = inputDate.substring(0, inputDate.length() - 2);
        seconds = seconds.substring(0, seconds.length() - 2);
        String outputDate = "";
        try {
            long timeStamp = Long.parseLong(inputDate) + Long.parseLong(seconds);
            Date d = new Date(timeStamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat(format); // Set your date format
            outputDate = sdf.format(d);
            return outputDate;
        } catch (Exception e) {
            Log.e("local", "EXP: converting timestamp to any date: " + e.toString());
        }
        return outputDate;
    }

    public static String toTitleCase(String input) {
        input = input.toLowerCase();
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String compareDateWithCurrentDate(String inputTimestamp) {
        inputTimestamp = inputTimestamp.substring(0, inputTimestamp.length() - 2);

        try {
            long timeStamp = Long.parseLong(inputTimestamp);
            Date d = new Date(timeStamp * 1000);
            Date now = new Date();

            if (d.after(now)) {
                return DATE_IS_AFTER;
            } else if (d.before(now)) {
                return DATE_IS_BEFORE;
            } else if (d.equals(now)) {
                return DATE_IS_EQUAL;
            }

        } catch (Exception e) {
            Log.e("local", "EXP: converting timestamp to any date: " + e.toString());
        }

        return null;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int verCode = pInfo.versionCode;
            return String.valueOf(verCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Utils.class.getName(), "EXP: " + e.toString());
            return "";
        }
    }

    public static boolean isEditTextEmpty(EditText field) {
        if (field.getText().toString().equalsIgnoreCase(""))
            return true;
        else
            return false;
    }

    public static boolean isStringEmpty(String field) {
        if (field != null && !field.equalsIgnoreCase("") && !field.equalsIgnoreCase("null"))
            return true;
        else
            return false;
    }

    public static void showLoading(Dialog loadingDialog) {
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    public static void hideLoading(Dialog loadingDialog) {
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    public static boolean compareStringForNull(String string) {
        if (string == null || string.equalsIgnoreCase("") || string.equalsIgnoreCase("null") || string.equalsIgnoreCase("NA")
                || string.equalsIgnoreCase("N/A"))
            return false;
        else
            return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static String convertDateFormats(String strInputDate, String inputFormat, String outputFormat) {
        String strOutput = "";
        try {
            SimpleDateFormat inputSDF = new SimpleDateFormat(inputFormat);
            SimpleDateFormat outputSDF = new SimpleDateFormat(outputFormat);

            Date inputDate = inputSDF.parse(strInputDate);

            strOutput = outputSDF.format(inputDate);
        } catch (Exception e) {
            Log.e("flow", "EXP: convertDateFormats:CommonMethods: " + e.toString());
        }
        return strOutput;
    }

    public static boolean checkSpecialCharacters(String text) {
        String specialCharacters = " !#$%&'()*+,-./:;<=>?@[]^_`{|}~";
        String str2[] = text.split("");
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (specialCharacters.contains("" + text.charAt(i)))
                return true;
        }
        return false;
    }

    public static boolean checkSpecialCharactersAndNumbers(String text, boolean allowWideSpace) {
        String specialCharacters;
        if (allowWideSpace)
            specialCharacters = "!#$%&'()*+,-./:;<=>?@[]^_`{|}~1234567890";
        else
            specialCharacters = " !#$%&'()*+,-./:;<=>?@[]^_`{|}~1234567890";

        String str2[] = text.split("");
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (specialCharacters.contains("" + text.charAt(i)))
                return true;
        }
        return false;
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public static void setEditTextMaxLength(EditText editText, int length) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(filterArray);
    }

    public static boolean isFutureDate(String strDate, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date today = new Date();
        sdf.format(today);

        try {
            if (sdf.parse(strDate).compareTo(sdf.parse(sdf.format(today))) > 0) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            Log.e(TAG, "EXP: isFutureDate-->> " + e.toString());
            return false;
        }
    }

    public static String getSerialNumber() {
        return Build.SERIAL.toUpperCase();
    }

    public static boolean isPANValid(String pan_number) {
        Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");

        Matcher matcher = pattern.matcher(pan_number.toUpperCase());
        if (matcher.matches())
            return true;
        else
            return false;
    }

    public static boolean compareDrawable(Drawable d1, Drawable d2) {
        try {
            Bitmap bitmap1 = ((BitmapDrawable) d1).getBitmap();
            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, stream1);
            stream1.flush();
            byte[] bitmapdata1 = stream1.toByteArray();
            stream1.close();

            Bitmap bitmap2 = ((BitmapDrawable) d2).getBitmap();
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
            stream2.flush();
            byte[] bitmapdata2 = stream2.toByteArray();
            stream2.close();

            return bitmapdata1.equals(bitmapdata2);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }


    public static void showGenericMsgDialog(Context context, String message)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_generic);
        ((TextView) dialog.findViewById(R.id.dialog_msg_tv)).setText(message);
        ((Button) dialog.findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((ImageView) dialog.findViewById(R.id.popUpCloseBtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
