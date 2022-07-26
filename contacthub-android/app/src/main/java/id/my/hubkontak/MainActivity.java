package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.my.hubkontak.fragments.AccountFragment;
import id.my.hubkontak.fragments.ContactSaveFragment;
import id.my.hubkontak.fragments.ContactShareFragment;
import id.my.hubkontak.fragments.HomeFragment;
import id.my.hubkontak.utils.AMQSubscriber;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.db.ModelContactSave;
import id.my.hubkontak.utils.db.ModelContactShare;

import static id.my.hubkontak.utils.API.AMQ_HOST;
import static id.my.hubkontak.utils.API.API_SINGKRON_LOCAL_CONTACT;
import static id.my.hubkontak.utils.API.API_UPDATE_FIREBASE_TOKEN;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager fragmentManager;
    private boolean doubleBackToExitPressedOnce = false;
    private SessionManager sessionManager;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private ModelContactSave modelContactSave;
    private ModelContactShare modelContactShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);
        userDetail = sessionManager.getUserDetails();
        Log.i(TAG,userDetail.toString());
        apiKey = userDetail.get(KEY_TOKEN);
        modelContactSave = new ModelContactSave(this);
        modelContactShare = new ModelContactShare(this);

        final HomeFragment fragmentHome = new HomeFragment();
        final ContactSaveFragment fragmentSaveContact = new ContactSaveFragment();
        final ContactShareFragment fragmentShareContact = new ContactShareFragment();
        final AccountFragment fragmentAkun = new AccountFragment();
        final Fragment[] currentFragment = new Fragment[1];
        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (getFragmentVisible() == null) {
            if (fragmentManager.findFragmentByTag("HOME") == null) {
                fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentHome, "HOME").commit();
                currentFragment[0] = fragmentHome;
                bottomNavigationView.setSelectedItemId(R.id.beranda);
            }
        }else {
            currentFragment[0] = getFragmentVisible();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                try{
                    switch (menuItem.getItemId()){
                        case R.id.beranda:
                            if (fragmentManager.findFragmentByTag("HOME") == null) {
                                fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentHome, "HOME").hide(currentFragment[0]).commit();
                            }else {
                                if (!fragmentHome.isVisible()) {
                                    fragmentManager.beginTransaction().hide(currentFragment[0]).show(fragmentHome).commit();
                                }
                            }
                            Intent intent = new Intent("FragmentHome");
                            intent.putExtra("message", "refresh");
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                            currentFragment[0] = fragmentHome;
                            break;
                        case R.id.whislist:
                            if (fragmentManager.findFragmentByTag("SAVECONTACT") == null) {
                                fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentSaveContact, "SAVECONTACT").hide(currentFragment[0]).commit();
                            }else {
                                if (!fragmentSaveContact.isVisible()) {
                                    fragmentManager.beginTransaction().hide(currentFragment[0]).show(fragmentSaveContact).commit();
                                }
                            }
                            currentFragment[0] = fragmentSaveContact;
                            break;
                        case R.id.transaksi:
                            if (fragmentManager.findFragmentByTag("SHARECONTACT") == null) {
                                fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentShareContact, "SHARECONTACT").hide(currentFragment[0]).commit();
                            }else {
                                if (!fragmentShareContact.isVisible()) {
                                    fragmentManager.beginTransaction().hide(currentFragment[0]).show(fragmentShareContact).commit();
                                }
                            }
                            currentFragment[0] = fragmentShareContact;
                            break;
                        case R.id.akun:
                            if (fragmentManager.findFragmentByTag("AKUN") == null) {
                                fragmentManager.beginTransaction().add(R.id.frameLayout, fragmentAkun, "AKUN").hide(currentFragment[0]).commit();
                            }else {
                                if (!fragmentAkun.isVisible()) {
                                    fragmentManager.beginTransaction().hide(currentFragment[0]).show(fragmentAkun).commit();
                                }
                            }
                            currentFragment[0] = fragmentAkun;
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }
                String token = Objects.requireNonNull(task.getResult()).getToken();
                sessionManager.setFirebaseToken(token);
                sendTokenToServer(token);

                Log.d(TAG, "FIREBASE TOKEN : " + token);
            }
        });
        getDataSave();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //TODO anything
    }
    private void getDataSave() {
        try {
            boolean is_singkron = false;
            List<String[]> list_save = modelContactSave.getAll(userDetail.get(KEY_USER_ID));
            if(list_save.size()==0){
                is_singkron = true;
            }
            List<String[]> list_share = modelContactShare.getAll(userDetail.get(KEY_USER_ID));
            if(list_share.size()==0){
                is_singkron = true;
            }
            if (is_singkron){
                singkronkan_save_kontak();
            }else{
                AmqTask runner = new AmqTask();
                runner.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void singkronkan_save_kontak() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_SINGKRON_LOCAL_CONTACT)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,response.toString());
                AmqTask runner = new AmqTask();
                runner.execute();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-API-KEY",apiKey);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void sendTokenToServer(String token) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_UPDATE_FIREBASE_TOKEN)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("FirebaseToken",token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-API-KEY",apiKey);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private class AmqTask extends AsyncTask<String, String, String> {

        private String resp;

        private AMQSubscriber amq = new AMQSubscriber();
        @Override
        protected String doInBackground(String... params) {
            publishProgress("Consuming..."); // Calls onProgressUpdate()
            try {
                if (sessionManager.isLoggedIn()){
                    String exchange = "guestapk";
                    amq.start(MainActivity.this,AMQ_HOST,exchange,new String[]{"save_contact_" + userDetail.get(KEY_USER_ID),"share_contact_" + userDetail.get(KEY_USER_ID)});
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
                resp = e.getMessage();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            fragment.onActivityResult(requestCode,resultCode,data);
        }
    }
    private Fragment getFragmentVisible(){
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragmentList){
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }
}