package in.glg.rummy.utils;

import android.content.Context;
import android.util.Log;

import com.webengage.sdk.android.Analytics;
import com.webengage.sdk.android.User;
import com.webengage.sdk.android.UserProfile;
import com.webengage.sdk.android.WebEngage;
import com.webengage.sdk.android.utils.Gender;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.glg.rummy.models.LoginResponseRest;

public class MyWebEngage
{
    public static final String WE_LOG = "webengage_log";

    public static final String USER_LOGGED_IN       = "User Login";
    public static final String USER_REGISTERED      = "User Registered";
    public static final String PAYMENT_PAGE_VISITED = "PaymentPageVisited";
    public static final String PAYMENT_FAILED       = "PaymentFailed";
    public static final String PAYMENT_SUCCESS      = "PaymentSuccess";
    public static final String WITHDRAW_INITIATED   = "WithdrawInitiated";
    public static final String KYC_UPLOADED         = "KYCUploaded";
    public static final String MOBILE_VERIFIED      = "MobileVerification";
    public static final String PROFILE_UPDATED      = "ProfileUpdate";
    public static final String SUPPORT_QUERY        = "SupportQueryRaised";
    public static final String REPORT_BUG           = "ReportBug";
    public static final String REFER_FRIEND         = "RAFRequest";
    public static final String EMAIL_UPDATE         = "EmailUpdate";
    public static final String MOBILE_UPDATE        = "MobileUpdate";
    public static final String PROMOTION_CHECKED    = "Promotion Checked";

    public static final String BONUS_CODE                   = "bonuscode";
    public static final String MODE_OF_DEPOSIT              = "modeofdeposit";
    public static final String FIRST_DEPOSIT                = "firstdeposit";
    public static final String USER_ID                      = "userId";
    public static final String DEVICE_TYPE                  = "device_type";
    public static final String CLIENT_TYPE                  = "client_type";
    public static final String AMOUNT                       = "amount";
    public static final String WE_EMAIL                     = "we_email";
    public static final String WE_PHONE                     = "we_phone";
    public static final String WE_GENDER                    = "we_gender";
    public static final String EMAIL_VERIFICATION_STATUS    = "EmailVerificationStatus";
    public static final String MOBILE_VERIFICATION_STATUS   = "MobileVerificationStatus";

    // Screen Tags
    public static final String LOGIN_SCREEN             = "Login Screen";
    public static final String REGISTRATION_SCREEN      = "Registration Screen";
    public static final String PRE_LOBBY_SCREEN         = "Pre-lobby Screen";
    public static final String LOBBY_SCREEN             = "Lobby Screen";
    public static final String PROFILE_SCREEN           = "Profile Screen";
    public static final String KYC_SCREEN               = "KYC Screen";
    public static final String RAF_SCREEN               = "RAF Screen";
    public static final String PREFERENCES_SCREEN       = "Preferences Screen";
    public static final String ACC_OVERVIEW_SCREEN      = "Account Overview Screen";
    public static final String DEPOSIT_SCREEN           = "Deposit Screen";
    public static final String WITHDRAW_SCREEN          = "Withdraw Screen";
    public static final String SUPPORT_SCREEN           = "Support Screen";
    public static final String GAME_TABLE_SCREEN        = "Game Table Screen";
    public static final String TOURNEY_LOBBY_SCREEN     = "Tourney Lobby Screen";
    public static final String TOURNEY_DETAILS_SCREEN   = "Tourney Details Screen";

    public static void doWebEngageLogin(LoginResponseRest response)
    {

        try {
            Log.e(WE_LOG, "doWebEngageLogin");
            User weUser = WebEngage.get().user();
            weUser.login(String.valueOf(response.getPlayerid()));

//            Log.e(WE_LOG, "");
            if(response.getEmail()!=null)
                weUser.setEmail(response.getEmail());

            if(response.getGender()!=null) {
                if(response.getGender().equalsIgnoreCase("male"))
                    weUser.setGender(Gender.MALE);
                else if(response.getGender().equalsIgnoreCase("female"))
                    weUser.setGender(Gender.FEMALE);
                else
                    weUser.setGender(Gender.OTHER);
            }

            if(response.getMobile()!=null)
                weUser.setPhoneNumber(response.getMobile().toString());

            if(response.getDob()!=null)
                weUser.setBirthDate(response.getDob().toString());

            if(response.getFirstname()!=null)
                weUser.setFirstName(response.getFirstname());

            if(response.getLastname()!=null)
                weUser.setLastName(response.getLastname().toString());

            if(response.getCity()!=null)
                weUser.setAttribute("city", response.getCity());

            if(response.getState()!=null)
                weUser.setAttribute("State", response.getState());

            if(response.getZipcode()!=null)
                weUser.setAttribute("pincode", response.getZipcode());

            if(response.getCreationip()!=null)
                weUser.setAttribute("RegisterationIP", response.getCreationip());

            if(response.getFirstDepositDate()!=null)
            weUser.setAttribute("firstDepositDate", getDateObject(response.getFirstDepositDate().toString()));

            if(response.getVerified()!=null)
                weUser.setAttribute("EmailVerificationStatus", response.getVerified());

            if(response.getMobileverified()!=null)
                weUser.setAttribute("MobileVerificationStatus", response.getMobileverified());

            if(response.getKycverified()!=null)
                weUser.setAttribute("KYC_STATUS", response.getKycverified());

            if(response.getNetworkOverlap()!=null)
                weUser.setAttribute("networkoverlap", response.getNetworkOverlap().toString());

            if(response.getLastLogin()!=null)
                weUser.setAttribute("LastLoginDate", getDateObject(response.getLastLogin().toString()));

            if(response.getRegisteredon()!=null)
                weUser.setAttribute("RegisterationDate", getDateObject(response.getRegisteredon()));

            if(response.getRegisterDeviceType()!=null)
                weUser.setAttribute("RegistrationDeviceType", response.getRegisterDeviceType());

            if(response.getRegisterClientType()!=null)
                weUser.setAttribute("RegistrationClientType", response.getRegisterClientType());

            if(response.getLastDepositAt()!=null)
            weUser.setAttribute("LastDepositDate", getDateObject(response.getLastDepositAt().toString()));

            if(response.getFirstWithdrawalAt()!=null)
            weUser.setAttribute("FirstWithdrawalDate", getDateObject(response.getFirstWithdrawalAt().toString()));

            if(response.getLastWithdrawalAt()!=null)
            weUser.setAttribute("LastWithdrawalDate", getDateObject(response.getLastWithdrawalAt().toString()));

            if(response.getLastWithdrawalAt()!=null)
                weUser.setAttribute("InactiveDays", response.getLastLoginDays());

            weUser.setAttribute("device_type", "Mobile");
            weUser.setAttribute("client_type", Utils.CLIENT_TYPE);

            if(response.getDeposits()!=null)
                weUser.setAttribute("totalNumberOfDeposits", response.getDeposits());
            else
                weUser.setAttribute("totalNumberOfDeposits", 0);

            if(response.getSumdeposit()!=null)
                weUser.setAttribute("totalDeposits", Float.parseFloat(response.getSumdeposit()));
            else
                weUser.setAttribute("totalDeposits", 0);

            if(response.getWithdrawls()!=null)
                weUser.setAttribute("totalNumberOfWithdrawals", response.getWithdrawls());
            else
                weUser.setAttribute("totalNumberOfWithdrawals", 0);

            if(response.getSumwithdrawl()!=null)
                weUser.setAttribute("totalWithdrawals", Float.parseFloat(response.getSumwithdrawl()));
            else
                weUser.setAttribute("totalWithdrawals", 0);

            weUser.setUserProfile(new UserProfile.Builder()
                    .build());

          /*  Map<String, Object> customAttributes = new HashMap<>();
            weUser.setAttributes(customAttributes);*/
            Log.e(MyWebEngage.WE_LOG, "User attributes are set for user: "+response.getPlayerid());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(WE_LOG, e+"@185");
        }
    }


    public static void trackWEEvent(String eventName, Map<String, Object> map)
    {
        Analytics weAnalytics = WebEngage.get().analytics();

        if(map==null)
            weAnalytics.track(eventName);
        else
            weAnalytics.track(eventName, map);

        Log.w(MyWebEngage.WE_LOG, "WebEngage event tracked: "+eventName);
    }

    public static void trackLoginRegisterEventWE(String event_name, String userId)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(USER_ID, userId);
        map.put(DEVICE_TYPE, "Mobile");
        map.put(CLIENT_TYPE, Utils.CLIENT_TYPE);

        trackWEEvent(event_name, map);
    }

    public static void trackRegisterEventWE(JSONObject object, String userId)
    {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put(MyWebEngage.USER_ID, userId);
            map.put(MyWebEngage.DEVICE_TYPE, "Mobile");
            map.put(MyWebEngage.CLIENT_TYPE, Utils.CLIENT_TYPE);
            map.put(MyWebEngage.WE_EMAIL, object.getString("email"));
            map.put(MyWebEngage.WE_PHONE, object.getString("phone"));
            map.put(MyWebEngage.WE_GENDER, object.getString("gender").toLowerCase());

            MyWebEngage.trackWEEvent(MyWebEngage.USER_REGISTERED, map);
        } catch (Exception e){

        }
    }

    public static void trackScreenNameWE(String screen, Context context)
    {
        Analytics weAnalytics = WebEngage.get().analytics();

        Map<String,Object> screenData = new HashMap<String, Object>();
        screenData.put(MyWebEngage.USER_ID, PrefManager.getString(context, RummyConstants.PLAYER_USER_ID, ""));

        weAnalytics.setScreenData(screenData);
        weAnalytics.screenNavigated(screen, screenData);
    }

    public static Date getDateObject(String dateStr)
    {
        Date date = null;
        try {
            dateStr = dateStr.replace("T", " ");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = format.parse(dateStr);
        } catch (Exception e) {
            Log.e(WE_LOG, "EXP: getDateObject->> "+e.getMessage());
        }
        return date;
    }

}
