package id.my.hubkontak.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "SHARPREFKONTAKHUB";
    public static final String KEY_CS_TOPUP = "CS_TOPUP";
    public static final String KEY_MESSAGE_TOPUP = "MESSAGE_TOPUP";
    public static final String KEY_TEMPLATE_SHARE = "KEY_TEMPLATE_SHARE";
    public static final String KEY_URL_BANTUAN = "KEY_URL_BANTUAN";
    public static final String KEY_URL_KEBIJAKAN_PRIVASI = "KEY_URL_KEBIJAKAN_PRIVASI";
    public static final String KEY_PROFILE_IS_COMPLETE = "KEY_PROFILE_IS_COMPLETE";
    public static final String KEY_CLICK_ADS_MESSAGE = "KEY_CLICK_ADS_MESSAGE";


    public static final String REFERRER_URL = "REFERRER_URL";
    // Constructor
    public SharedPref(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void createSession(String key,String value){
        editor.putString(key,value);
        editor.commit();
    }
    public void createSession(String key,boolean value){
        editor.putBoolean(key,value);
        editor.commit();
    }
    public void createSession(String key,int value){
        editor.putInt(key,value);
        editor.commit();
    }
    public void createSession(String key,long value){
        editor.putLong(key,value);
        editor.commit();
    }
    public void createSession(String key,float value){
        editor.putFloat(key,value);
        editor.commit();
    }
    public boolean getSessionBool(String key){
        return pref.getBoolean(key,false);
    }

    public String getSessionStr(String key){
        return pref.getString(key,"");
    }
    public int getSessionInt(String key){
        return pref.getInt(key,0);
    }
    public long getSessionLong(String key){
        return pref.getLong(key,0);
    }
    public float getSessionFloat(String key){
        return pref.getFloat(key,0);
    }

    public void clearSession(){
        editor.clear();
        editor.commit();
    }

}
