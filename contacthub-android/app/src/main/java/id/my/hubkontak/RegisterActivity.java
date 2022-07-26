package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_CITY;
import static id.my.hubkontak.utils.SessionManager.KEY_EMAIL;
import static id.my.hubkontak.utils.SessionManager.KEY_MARKETING_CODE;
import static id.my.hubkontak.utils.SessionManager.KEY_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_PROVINCE;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;
import static id.my.hubkontak.utils.SharedPref.KEY_PROFILE_IS_COMPLETE;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getDeviceId;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Toolbar toolbar;
    private EditText edtFullName, edtUsername,edtWhatsApp,edtEmail,edtPassword;
    private Button btnDaftar;
    private SessionManager session;
    private CheckBox chkAgreement;
    private SharedPref sharedPref;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String referralCode = "";
    private List<String> arrGreeting = new ArrayList<>();
    private Spinner spinGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        session = new SessionManager(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinGreeting = (Spinner) findViewById(R.id.spinGreeting);
        edtFullName = (EditText) findViewById(R.id.edtNama);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtWhatsApp = (EditText) findViewById(R.id.edtWhatsApp);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        chkAgreement = (CheckBox) findViewById(R.id.checkBox);

        btnDaftar = (Button) findViewById(R.id.btnDaftar);
        sharedPref = new SharedPref(this);

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtFullName.getText())){
                    Toast.makeText(RegisterActivity.this,"Nama Lengkap tidak boleh kosong",Toast.LENGTH_SHORT);
                }else if (TextUtils.isEmpty(edtUsername.getText())) {
                    Toast.makeText(RegisterActivity.this, "Username tidak boleh kosong", Toast.LENGTH_SHORT);
                }else if (TextUtils.isEmpty(edtWhatsApp.getText())) {
                    Toast.makeText(RegisterActivity.this, "No WhatsApp tidak boleh kosong", Toast.LENGTH_SHORT);
                }else if (TextUtils.isEmpty(edtEmail.getText())) {
                    Toast.makeText(RegisterActivity.this, "Email tidak boleh kosong", Toast.LENGTH_SHORT);
                }else if (TextUtils.isEmpty(edtPassword.getText())) {
                    Toast.makeText(RegisterActivity.this, "Password tidak boleh kosong", Toast.LENGTH_SHORT);
                }else{
                    doRegister(edtFullName.getText().toString(), edtUsername.getText().toString(),edtWhatsApp.getText().toString(),
                            edtEmail.getText().toString(),edtPassword.getText().toString(),arrGreeting.get(spinGreeting.getSelectedItemPosition()));
                }
            }
        });
        chkAgreement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeButtonRegister();
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initializeButtonRegister();
            }
        });
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initializeButtonRegister();
            }
        });
        edtWhatsApp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initializeButtonRegister();
            }
        });
        edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initializeButtonRegister();
            }
        });
        edtFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initializeButtonRegister();
            }
        });
        try{
            String referrerUrl = sharedPref.getSessionStr(SharedPref.REFERRER_URL);
            String referrer = referrerUrl;
            if (!referrerUrl.contains("http")){
                if (referrerUrl.contains("referrer")){
                    String base_url = "https://play.google.com/store/apps/details?id=id.co.kamil.autochat&";
                    referrerUrl = base_url + referrerUrl;
                    Uri uri = Uri.parse(referrerUrl);
                    referrer = uri.getQueryParameter("referrer");
                }else if(referrerUrl.contains("utm_source")){
                    referrer = "";
                }
            }else{
                Uri uri = Uri.parse(referrerUrl);
                referrer = uri.getQueryParameter("referrer");
            }
            Log.d(TAG,"referrerUrl:" + referrerUrl);
            Log.d(TAG,"referrer:" + referrer);
            referralCode = referrer;
            Bundle params = new Bundle();
            params.putString("referrerUrl", referrerUrl);
            params.putString("referrer", referrer);
            mFirebaseAnalytics.logEvent("SignupActivityReferal", params);
        }catch (Exception e){
            e.printStackTrace();
        }
        loadGreeting();
    }
    private void displayGreeting() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrGreeting);
        spinGreeting.setAdapter(adapter);
    }
    private void initializeButtonRegister(){
        if (TextUtils.isEmpty(edtFullName.getText()) || TextUtils.isEmpty(edtUsername.getText()) || TextUtils.isEmpty(edtWhatsApp.getText()) ||
                TextUtils.isEmpty(edtEmail.getText()) || TextUtils.isEmpty(edtPassword.getText()) || chkAgreement.isChecked() == false){
            btnDaftar.setEnabled(false);
        }else{
            btnDaftar.setEnabled(true);
        }
    }
    //    private
    private void loadGreeting() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_GREETING)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        arrGreeting.clear();
                        for (int i=0;i<result.length();i++){
                            String greetingId = result.getJSONObject(i).getString("GreetingId");
                            arrGreeting.add(greetingId);
                        }
                        displayGreeting();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(getApplicationContext())
                                .setMessage(errorResponse(error))
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    private void doRegister(String namaLengkap,String userName, String whatsApp, String email, String password,String greeting){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Name", namaLengkap);
            parameter.put("Nickname", namaLengkap);
            parameter.put("Username", userName);
            parameter.put("Greeting", greeting);
            parameter.put("WhatsApp", whatsApp);
            parameter.put("Email", email);
            parameter.put("Password", password);
            parameter.put("Platform", "android");
            parameter.put("ReferralCode", referralCode);
            parameter.put("DeviceId", getDeviceId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_REGISTER)
                .buildUpon()
                .toString();

        progressDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        JSONObject result = response.getJSONObject("result");
                        JSONObject user = result.getJSONObject("User");
                        String email = user.getString("Email");
                        String nickname = user.getString("Nickname");
                        String user_id = user.getString("UserId");

                        String name = result.getString("Name");
                        String marketingCode = result.getString("MarketingCode");
                        String cust_id = result.getString("CustomerId");
                        String greeting = result.getString("Greeting");
                        boolean is_owner = result.getBoolean("IsOwner");
                        String foto = result.optString("Foto");
                        String city = result.optString("CityId");

                        JSONObject auth = result.getJSONObject("Auth");
                        String token = auth.getString("Key");
                        session.createLoginSession(user_id,cust_id,email,nickname,name,greeting,is_owner,foto,token);
                        //                        set Profile Session
                        HashMap<String,String> mapProfile = new HashMap<>();
                        mapProfile.put(KEY_NAME,name);
                        mapProfile.put(KEY_EMAIL,email);
                        mapProfile.put(KEY_USER_ID,user_id);
                        mapProfile.put(KEY_MARKETING_CODE,marketingCode);
                        session.setProfile(mapProfile);
                        sharedPref.createSession(KEY_PROFILE_IS_COMPLETE,false);
                        sharedPref.createSession(KEY_CITY,city);
                        session.setSession(KEY_PROVINCE,null);
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    new AlertDialog.Builder(RegisterActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK",null)
                            .show();
                }
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}