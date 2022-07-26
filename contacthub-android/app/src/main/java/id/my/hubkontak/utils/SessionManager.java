package id.my.hubkontak.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "CONTACTHUBPREF";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_USER_ID = "user_id"; // UserId
    public static final String KEY_CUST_ID = "customer_id"; // CustomerId
    public static final String KEY_TOKEN = "token"; // Token User
    public static final String KEY_EMAIL = "email"; // Email User
    public static final String KEY_NICKNAME = "nickname"; // Nickname User
    public static final String KEY_NAME = "name"; // Name User
    public static final String KEY_GREETING = "greeting";
    public static final String KEY_IS_OWNER = "is_owner";
    public static final String KEY_FOTO = "foto";
    public static final String KEY_BUSINESS_NAME = "business_name";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_TGL_LAHIR = "tgl_lahir";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_WHATSAPP = "whatsapp";
    public static final String KEY_FACEBOOK = "facebook";
    public static final String KEY_INSTAGRAM = "instagram";
    public static final String KEY_WEBSITE = "website";
    public static final String KEY_HOBI = "hobi";
    public static final String KEY_PRODUCT = "product";
    public static final String KEY_STATUS_KAWIN = "status_kawin";
    public static final String KEY_INTEREST = "interest";
    public static final String KEY_BUSINESS_TYPE = "business_type";
    public static final String KEY_PROFESSION = "profession";
    public static final String KEY_AGAMA = "agama";
    public static final String KEY_MARKETING_CODE = "marketingCode";
    public static final String KEY_TOKOPEDIA = "tokopedia";
    public static final String KEY_BUKALAPAK = "bukalapak";
    public static final String KEY_SHOPEE = "shopee";
    public static final String KEY_COVER = "cover";
    public static final String KEY_CITY = "city";
    public static final String KEY_PROVINCE = "province";

    public static final String KEY_ALLOW_SHARE_PROFILE = "allow_share_profile";
    public static final String KEY_SAVE_CONTACT_FRIEND_BY = "save_contact_friend_by";
    public static final String KEY_SAVE_MY_CONTACT_BY = "save_my_contact_by";
    public static final String KEY_LIMIT_SAVE_CONTACT_FRIEND = "limit_save_contact_friend";
    public static final String KEY_LIMIT_SAVE_MY_CONTACT = "limit_save_my_contact";
    public static final String KEY_SAVE_CONTACT_FRIEND_INTEREST = "save_contact_friend_interest";
    public static final String KEY_SAVE_MY_CONTACT_INTEREST = "save_my_contact_interest";
    public static final String KEY_SAVE_OTHER_VALUE_FRIEND = "KEY_SAVE_OTHER_VALUE_FRIEND";
    public static final String KEY_SAVE_OTHER_VALUE = "KEY_SAVE_OTHER_VALUE";
    public static final String KEY_SAVE_OTHER_KEY = "KEY_SAVE_OTHER_KEY";
    public static final String KEY_SAVE_OTHER_KEY_FRIEND = "KEY_SAVE_OTHER_KEY_FRIEND";


    public static final String QUEUE_NAME = "QUEUE_NAME"; // Queue Name Rabbit
    public static final String AMQ_UPDATEAT = "AMQ_UPDATEAT "; // Update terakhir amq running
    public static final String AMQ_EXCHANGE_NAME = "AMQ_EXCHANGE_NAME "; // Exchange Name
    public static final String AMQ_BIND_KEY = "AMQ_BIND_KEY "; // Routing Key
    private static final String FIREBASE_TOKEN = "firebasetoken";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setAmqUpdatedAt(String updatedAt) {
        editor.putString(AMQ_UPDATEAT, updatedAt);
        editor.commit();
    }
    public String getAmqUpdatedAt(){
        return pref.getString(AMQ_UPDATEAT,null);
    }

    public void setAmqExchangeName(String value) {
        editor.putString(AMQ_EXCHANGE_NAME, value);
        editor.commit();
    }
    public String getAmqExchangeName(){
        return pref.getString(AMQ_EXCHANGE_NAME,null);
    }


    public void setAmqQueueName(String value) {
        editor.putString(QUEUE_NAME, value);
        editor.commit();
    }
    public String getAmqQueueName(){
        return pref.getString(QUEUE_NAME,null);
    }

    public void setAmqBindKey(String value) {
        editor.putString(AMQ_BIND_KEY, value);
        editor.commit();
    }
    public String getAmqBindKey(){
        return pref.getString(AMQ_BIND_KEY,null);
    }
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void createLoginSession(String user_id,String cust_id,String email,String nickname,String name,
                                   String greeting,boolean is_owner,String foto, String token){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_ID, user_id);
        editor.putString(KEY_CUST_ID, cust_id);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NICKNAME, nickname);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_GREETING, greeting);
        editor.putBoolean(KEY_IS_OWNER, is_owner);
        editor.putString(KEY_FOTO, foto);
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void setToken(String token){
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void setFirebaseToken(String firebasetoken){
        editor.putString(FIREBASE_TOKEN, firebasetoken);
        editor.commit();
    }

    public String getFirebaseToken() {
        return pref.getString(FIREBASE_TOKEN, null);
    }

    public void setSettingContact(HashMap<String,String> profile){
        editor.putString(KEY_ALLOW_SHARE_PROFILE, profile.get(KEY_ALLOW_SHARE_PROFILE));
        editor.putString(KEY_SAVE_CONTACT_FRIEND_BY, profile.get(KEY_SAVE_CONTACT_FRIEND_BY));
        editor.putString(KEY_SAVE_MY_CONTACT_BY, profile.get(KEY_SAVE_MY_CONTACT_BY));
        editor.putString(KEY_LIMIT_SAVE_CONTACT_FRIEND, profile.get(KEY_LIMIT_SAVE_CONTACT_FRIEND));
        editor.putString(KEY_LIMIT_SAVE_MY_CONTACT, profile.get(KEY_LIMIT_SAVE_MY_CONTACT));
        editor.putString(KEY_SAVE_MY_CONTACT_INTEREST, profile.get(KEY_SAVE_MY_CONTACT_INTEREST));
        editor.putString(KEY_SAVE_CONTACT_FRIEND_INTEREST, profile.get(KEY_SAVE_CONTACT_FRIEND_INTEREST));
        editor.putString(KEY_SAVE_OTHER_KEY, profile.get(KEY_SAVE_OTHER_KEY));
        editor.putString(KEY_SAVE_OTHER_VALUE, profile.get(KEY_SAVE_OTHER_VALUE));
        editor.putString(KEY_SAVE_OTHER_KEY_FRIEND, profile.get(KEY_SAVE_OTHER_KEY_FRIEND));
        editor.putString(KEY_SAVE_OTHER_VALUE_FRIEND, profile.get(KEY_SAVE_OTHER_VALUE_FRIEND));

        editor.commit();
    }

    public HashMap<String, String> getSettingContact(){

        HashMap<String, String> user = new HashMap<>();

        user.put(KEY_ALLOW_SHARE_PROFILE, pref.getString(KEY_ALLOW_SHARE_PROFILE, null));
        user.put(KEY_SAVE_CONTACT_FRIEND_BY, pref.getString(KEY_SAVE_CONTACT_FRIEND_BY, null));
        user.put(KEY_SAVE_MY_CONTACT_BY, pref.getString(KEY_SAVE_MY_CONTACT_BY, null));
        user.put(KEY_LIMIT_SAVE_CONTACT_FRIEND, pref.getString(KEY_LIMIT_SAVE_CONTACT_FRIEND, null));
        user.put(KEY_LIMIT_SAVE_MY_CONTACT, pref.getString(KEY_LIMIT_SAVE_MY_CONTACT, null));
        user.put(KEY_SAVE_CONTACT_FRIEND_INTEREST, pref.getString(KEY_SAVE_CONTACT_FRIEND_INTEREST, null));
        user.put(KEY_SAVE_MY_CONTACT_INTEREST, pref.getString(KEY_SAVE_MY_CONTACT_INTEREST, null));
        user.put(KEY_SAVE_OTHER_KEY, pref.getString(KEY_SAVE_OTHER_KEY, null));
        user.put(KEY_SAVE_OTHER_VALUE, pref.getString(KEY_SAVE_OTHER_VALUE, null));
        user.put(KEY_SAVE_OTHER_KEY_FRIEND, pref.getString(KEY_SAVE_OTHER_KEY_FRIEND, null));
        user.put(KEY_SAVE_OTHER_VALUE_FRIEND, pref.getString(KEY_SAVE_OTHER_VALUE_FRIEND, null));
        return user;
    }
    public void setProfile(HashMap<String,String> profile){
        editor.putString(KEY_USER_ID, profile.get(KEY_USER_ID));
        editor.putString(KEY_NAME, profile.get(KEY_NAME));
        editor.putString(KEY_NICKNAME, profile.get(KEY_NICKNAME));
        editor.putString(KEY_EMAIL, profile.get(KEY_EMAIL));
        editor.putString(KEY_GREETING, profile.get(KEY_GREETING));
        editor.putString(KEY_WHATSAPP, profile.get(KEY_WHATSAPP));
        editor.putString(KEY_GENDER, profile.get(KEY_GENDER));
        editor.putString(KEY_TGL_LAHIR, profile.get(KEY_TGL_LAHIR));
        editor.putString(KEY_ADDRESS, profile.get(KEY_ADDRESS));
        editor.putString(KEY_FACEBOOK, profile.get(KEY_FACEBOOK));
        editor.putString(KEY_INSTAGRAM, profile.get(KEY_INSTAGRAM));
        editor.putString(KEY_WEBSITE, profile.get(KEY_WEBSITE));
        editor.putString(KEY_PROFESSION, profile.get(KEY_PROFESSION));
        editor.putString(KEY_PRODUCT, profile.get(KEY_PRODUCT));
        editor.putString(KEY_HOBI, profile.get(KEY_HOBI));
        editor.putString(KEY_AGAMA, profile.get(KEY_AGAMA));
        editor.putString(KEY_INTEREST, profile.get(KEY_INTEREST));
        editor.putString(KEY_STATUS_KAWIN, profile.get(KEY_STATUS_KAWIN));
        editor.putString(KEY_BUSINESS_NAME, profile.get(KEY_BUSINESS_NAME));
        editor.putString(KEY_BUSINESS_TYPE, profile.get(KEY_BUSINESS_TYPE));
        editor.putString(KEY_FOTO, profile.get(KEY_FOTO));
        editor.putString(KEY_MARKETING_CODE, profile.get(KEY_MARKETING_CODE));
        editor.putString(KEY_TOKOPEDIA, profile.get(KEY_TOKOPEDIA));
        editor.putString(KEY_BUKALAPAK, profile.get(KEY_BUKALAPAK));
        editor.putString(KEY_SHOPEE, profile.get(KEY_SHOPEE));

        editor.commit();
    }
    public HashMap<String, String> getUserDetails(){

        HashMap<String, String> user = new HashMap<>();

        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_GREETING, pref.getString(KEY_GREETING, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_FOTO, pref.getString(KEY_FOTO, null));

        user.put(KEY_NICKNAME,pref.getString(KEY_NICKNAME,null));
        user.put(KEY_GREETING,pref.getString(KEY_GREETING,null));
        user.put(KEY_WHATSAPP,pref.getString(KEY_WHATSAPP,null));
        user.put(KEY_GENDER,pref.getString(KEY_GENDER,null));
        user.put(KEY_TGL_LAHIR,pref.getString(KEY_TGL_LAHIR,null));
        user.put(KEY_ADDRESS,pref.getString(KEY_ADDRESS,null));
        user.put(KEY_FACEBOOK,pref.getString(KEY_FACEBOOK,null));
        user.put(KEY_INSTAGRAM,pref.getString(KEY_INSTAGRAM,null));
        user.put(KEY_WEBSITE,pref.getString(KEY_WEBSITE,null));
        user.put(KEY_PROFESSION,pref.getString(KEY_PROFESSION,null));
        user.put(KEY_PRODUCT,pref.getString(KEY_PRODUCT,null));
        user.put(KEY_HOBI,pref.getString(KEY_HOBI,null));
        user.put(KEY_AGAMA,pref.getString(KEY_AGAMA,null));
        user.put(KEY_INTEREST,pref.getString(KEY_INTEREST,null));
        user.put(KEY_STATUS_KAWIN,pref.getString(KEY_STATUS_KAWIN,null));
        user.put(KEY_BUSINESS_NAME,pref.getString(KEY_BUSINESS_NAME,null));
        user.put(KEY_BUSINESS_TYPE,pref.getString(KEY_BUSINESS_TYPE,null));
        user.put(KEY_TOKOPEDIA,pref.getString(KEY_TOKOPEDIA,null));
        user.put(KEY_BUKALAPAK,pref.getString(KEY_BUKALAPAK,null));
        user.put(KEY_SHOPEE,pref.getString(KEY_SHOPEE,null));


        user.put(KEY_MARKETING_CODE, pref.getString(KEY_MARKETING_CODE, null));
        return user;
    }
    public void setSession(String key,String val){
        editor.putString(key, val);
        editor.commit();
    }
    public void clearData(){
        editor.clear();
        editor.commit();

        editor.putBoolean(IS_LOGIN, false);
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false);
        editor.putString(KEY_USER_ID, "");
        editor.putString(KEY_CUST_ID, "");
        editor.putString(KEY_TOKEN, "");
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public void addUrlCollection(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }
    public String getUrlCollection(String key) {
        return pref.getString(key, null);
    }

    public String getSession(String key) {
        return pref.getString(key,"");
    }
}
