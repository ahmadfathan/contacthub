package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.my.hubkontak.installreferrer.Application;
import id.my.hubkontak.installreferrer.ReferrerReceiver;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.API.API_STARTUP;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SharedPref.KEY_CLICK_ADS_MESSAGE;
import static id.my.hubkontak.utils.SharedPref.KEY_CS_TOPUP;
import static id.my.hubkontak.utils.SharedPref.KEY_MESSAGE_TOPUP;
import static id.my.hubkontak.utils.SharedPref.KEY_TEMPLATE_SHARE;
import static id.my.hubkontak.utils.SharedPref.KEY_URL_BANTUAN;
import static id.my.hubkontak.utils.SharedPref.KEY_URL_KEBIJAKAN_PRIVASI;
import static id.my.hubkontak.utils.SharedPref.REFERRER_URL;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getDeviceId;
import static id.my.hubkontak.utils.Utils.isContactExist;
import static id.my.hubkontak.utils.Utils.openAppPlaystore;
import static id.my.hubkontak.utils.Utils.saveLocalContact;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int REQUEST_PERMISSION_STORAGE = 500;
    private static final int REQUEST_PERMISSION_CONTACT = 400;
    private static final int REQUEST_PERMISSION_PHONE_STATE = 401;

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private String affiliation;

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private String[][] kontakWabot = {};
    private String currentVersion = "0";
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData();
        }
    };
    private SharedPref sharePref;
    private int try_again_version;
    private TextView txtVersion;
    private KeyguardManager.KeyguardLock lock;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int currentVersionCode = 1;
    private boolean isPermissionStorage = false;
    private boolean isPermissionContact = false;
    private boolean isCheckVersion = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        updateData();
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, new IntentFilter(ReferrerReceiver.ACTION_UPDATE_DATA));
//        ModelContactSave modelContactSave = new ModelContactSave(this);
//        modelContactSave.deleteAll();
//        ModelContactShare modelContactShare = new ModelContactShare(this);
//        modelContactShare.deleteAll();


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharePref = new SharedPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        txtVersion = (TextView) findViewById(R.id.txtVersion);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        final InstallReferrerClient referrerClient;

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        try {
                            ReferrerDetails response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();

                            Log.i(TAG, "referrerUrl:" + referrerUrl);
                            Log.i(TAG, "referrerClickTime:" + referrerClickTime);
                            Log.i(TAG, "appInstallTime:" + appInstallTime);
                            Log.i(TAG, "instantExperienceLaunched:" + instantExperienceLaunched);

                            sharePref.createSession(REFERRER_URL, referrerUrl);
                            Bundle params = new Bundle();
                            params.putString("referrerUrl", referrerUrl);
                            mFirebaseAnalytics.logEvent("SplashReferrerUrl", params);

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            Toast.makeText(SplashActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        //Toast.makeText(SplashActivity.this, "FEATURE_NOT_SUPPORTED", Toast.LENGTH_SHORT).show();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        //Toast.makeText(SplashActivity.this, "SERVICE_UNAVAILABLE", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                //Toast.makeText(SplashActivity.this, "onInstallReferrerServiceDisconnected", Toast.LENGTH_SHORT).show();
            }
        });

        try_again_version = sharePref.getSessionInt("try_again_version");
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

            txtVersion.setText(currentVersion);

            callRequestPermission();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void singkron(){
        if (session.isLoggedIn()){
            String action = getIntent().getStringExtra("action");
            Log.e(TAG,"action: " + action);
            try {
                if (action != null){
                    if (action.equals("notif_link") || action.equals("link")){
                        String link = getIntent().getStringExtra("link");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(browserIntent);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }else{
            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
        }
        finish();
    }
    private void checkVersion(){
        isCheckVersion = true;
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_STARTUP)
                .buildUpon()
                .toString();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    final JSONObject result = response.getJSONObject("result");
                    final boolean updateRequired = result.optBoolean("UpdateRequired",false);
                    final JSONArray csContact = result.optJSONArray("CS_CONTACT");
                    final String csContactTopup = result.optString("CS_CONTACT_TOPUP");
                    final String csMessageTopup = result.optString("CS_MESSAGE_TOPUP");
                    final String click_ads_message = result.optString("CLICK_ADS_MESSAGE");
                    final String url_bantuan = result.optString("URL_BANTUAN");
                    final String url_kebijakan_privasi = result.optString("URL_KEBIJAKAN_PRIVASI");
                    final String templateShare = result.optString("TEMPLATE_SHARE");
                    final String messageUpdate = result.optString("MessageUpdate");
                    final int versionCodeApp = result.optInt("VersionCode",1);
                    Log.e(TAG,"csContactTopup:" + csContactTopup);
                    sharePref.createSession(KEY_URL_BANTUAN,url_bantuan);
                    sharePref.createSession(KEY_URL_KEBIJAKAN_PRIVASI,url_kebijakan_privasi);
                    sharePref.createSession(KEY_CS_TOPUP,csContactTopup);
                    sharePref.createSession(KEY_MESSAGE_TOPUP,csMessageTopup);
                    sharePref.createSession(KEY_TEMPLATE_SHARE,templateShare);
                    sharePref.createSession(KEY_CLICK_ADS_MESSAGE,click_ads_message);
                    Log.e(TAG,"csMessageTopup:" + csMessageTopup);
                    try {
                        kontakWabot = new String[csContact.length()][2];
                        for(int i =0;i<csContact.length();i++){
                            kontakWabot[i][0] = csContact.getJSONObject(i).getString("Phone");
                            kontakWabot[i][1] = csContact.getJSONObject(i).getString("Name");
                        }
                        for(int i =0;i<csContact.length();i++){
                            Log.i(TAG,"kontakWabot:" + kontakWabot[i][0] + " - " + kontakWabot[i][1]);
                        }
                    }catch (NullPointerException e){

                    }

                    if (status.equals("OK")){
                        if (currentVersionCode != versionCodeApp){
                            if (try_again_version==0 || try_again_version >=10){
                                if (updateRequired){
                                    new AlertDialog.Builder(SplashActivity.this)
                                            .setMessage(messageUpdate)
                                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    openAppPlaystore(SplashActivity.this);
                                                    finish();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }else{
                                    new AlertDialog.Builder(SplashActivity.this)
                                            .setMessage(messageUpdate)
                                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    openAppPlaystore(SplashActivity.this);
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton("Lain kali", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (try_again_version>=10){
                                                        try_again_version = 0;
                                                    }
                                                    try_again_version++;
                                                    sharePref.createSession("try_again_version",try_again_version);
                                                    checkContactWabot();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }

                            }else{
                                try_again_version++;
                                sharePref.createSession("try_again_version",try_again_version);
                                checkContactWabot();
                            }
                            Log.i(TAG,"try:" + try_again_version);

                        }else{
                            checkContactWabot();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.i(TAG,"Volley Error : " + error.getMessage());
                //errorResponse(getApplicationContext(),error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!SplashActivity.this.isFinishing()) {
                            try {
                                new AlertDialog.Builder(SplashActivity.this)
                                        .setMessage(getString(errorResponse(error)))
                                        .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                checkVersion();
                                            }
                                        })
                                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            } catch (WindowManager.BadTokenException e) {
                                Log.e("WindowManagerBad ", e.toString());
                            }
                        }
                    }
                });

            }
        });
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }


    private void callRequestPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS}, REQUEST_PERMISSION_STORAGE);

                isPermissionStorage = false;
            }else{
                isPermissionStorage = true;
                checkVersion();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CONTACT){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionContact = true;
            }else{
                isPermissionContact = false;
            }
        }
        if(requestCode == REQUEST_PERMISSION_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionStorage = true;
            }else{
                isPermissionStorage = false;
            }
        }

        if (isPermissionStorage && isPermissionContact) {
            checkVersion();
        }else{
            callRequestPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void checkContactWabot() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            List<Integer> indexKontak = new ArrayList<>();
            for(int x=0;x<kontakWabot.length;x++){

                if (isContactExist(this,kontakWabot[x][0])){
                    indexKontak.add(x);
                }
            }

            if (indexKontak.size() == kontakWabot.length){
                Log.i(TAG,"Count Kontak Wabot : " + kontakWabot.length);
                Log.i(TAG,"Kontak CS sudah terdaftar");
            }else{
                boolean add = true;
                for(int a = 0;a<kontakWabot.length;a++){
                    add = true;
                    for(int x = 0;x<indexKontak.size();x++){
                        if (a==indexKontak.get(x)){
                            add = false;
                            break;
                        }
                    }
                    if (add){
                        saveLocalContact(this,kontakWabot[a][1],kontakWabot[a][0]);
                        Log.i(TAG,"Add Kontak : " + kontakWabot[a][1]);
                    }
                }
            }
            isPermissionContact = true;
            singkron();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS}, REQUEST_PERMISSION_CONTACT);
            Toast.makeText(this, "Membutuhkan Permission Kontak", Toast.LENGTH_SHORT).show();
            isPermissionContact = false;
            callRequestPermission();
        }
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, new IntentFilter(ReferrerReceiver.ACTION_UPDATE_DATA));
        super.onResume();
    }


    private void updateData() {
        affiliation = Application.getReferrerDataRaw(this);
        if (affiliation.contains("Undefined")){
            affiliation = "";
        }
        if (affiliation.contains("utm_source") || affiliation.contains("utm_medium")){
            affiliation = "";
        }

//        session.setValue(KEY_AFFILIATION,affiliation);
        Log.i(TAG,"affiliation: " + affiliation);
    }

}