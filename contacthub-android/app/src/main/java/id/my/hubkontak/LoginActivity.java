package id.my.hubkontak;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_ADDRESS;
import static id.my.hubkontak.utils.SessionManager.KEY_AGAMA;
import static id.my.hubkontak.utils.SessionManager.KEY_ALLOW_SHARE_PROFILE;
import static id.my.hubkontak.utils.SessionManager.KEY_BUKALAPAK;
import static id.my.hubkontak.utils.SessionManager.KEY_BUSINESS_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_BUSINESS_TYPE;
import static id.my.hubkontak.utils.SessionManager.KEY_CITY;
import static id.my.hubkontak.utils.SessionManager.KEY_COVER;
import static id.my.hubkontak.utils.SessionManager.KEY_EMAIL;
import static id.my.hubkontak.utils.SessionManager.KEY_FACEBOOK;
import static id.my.hubkontak.utils.SessionManager.KEY_FOTO;
import static id.my.hubkontak.utils.SessionManager.KEY_GENDER;
import static id.my.hubkontak.utils.SessionManager.KEY_GREETING;
import static id.my.hubkontak.utils.SessionManager.KEY_HOBI;
import static id.my.hubkontak.utils.SessionManager.KEY_INSTAGRAM;
import static id.my.hubkontak.utils.SessionManager.KEY_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_CONTACT_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_MY_CONTACT;
import static id.my.hubkontak.utils.SessionManager.KEY_MARKETING_CODE;
import static id.my.hubkontak.utils.SessionManager.KEY_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_NICKNAME;
import static id.my.hubkontak.utils.SessionManager.KEY_PRODUCT;
import static id.my.hubkontak.utils.SessionManager.KEY_PROFESSION;
import static id.my.hubkontak.utils.SessionManager.KEY_PROVINCE;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_KEY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_KEY_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_VALUE;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_VALUE_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_SHOPEE;
import static id.my.hubkontak.utils.SessionManager.KEY_STATUS_KAWIN;
import static id.my.hubkontak.utils.SessionManager.KEY_TGL_LAHIR;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKOPEDIA;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;
import static id.my.hubkontak.utils.SessionManager.KEY_WEBSITE;
import static id.my.hubkontak.utils.SessionManager.KEY_WHATSAPP;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getDeviceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_STORAGE = 500;
    private static final int REQUEST_PERMISSION_CONTACT = 400;
    private static final int REQUEST_REGISTER = 100;
    private Button btnDaftar,btnLogin;
    private EditText edtEmail,edtPassword;
    private SessionManager session;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = new SharedPref(this);
        session = new SessionManager(this);

//        if (session.isLoggedIn()){
//            startActivity(new Intent(this,MainActivity.class));
//            finish();
//        }

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnDaftar = (Button) findViewById(R.id.btnDaftar);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtEmail.getText()) || TextUtils.isEmpty(edtPassword.getText())){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("Email dan Password tidak boleh kosong")
                            .setPositiveButton("OK",null)
                            .show();
                }else{
                    LoginApi(edtEmail.getText().toString(),edtPassword.getText().toString());
                }
            }
        });

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),RegisterActivity.class),REQUEST_REGISTER);
            }
        });

        callRequestPermission();
    }

    private void callRequestPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
                callRequestPermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REGISTER){
            if (resultCode == RESULT_OK){
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        }
    }

    //    private
    private void LoginApi(String email, String password){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Email", email);
            parameter.put("Password", password);
            parameter.put("Platform", "android");
            parameter.put("DeviceId", getDeviceId(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LOGIN)
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
                        String interestId = result.getString("InterestId");
                        String gender = result.getString("Gender");
                        String professionId = result.getString("ProfessionId");
                        String agama = result.getString("Religion");
                        String whatsApp = result.getString("WhatsApp");
                        String tgllahir = result.getString("DateOfBirth");
                        String address = result.getString("Address");
                        String marketingCode = result.getString("MarketingCode");
                        String cust_id = result.getString("CustomerId");
                        String greeting = result.getString("Greeting");
                        String relationshipStatus = result.getString("Greeting");
                        String businessType = result.getString("BusinessTypeId");
                        String product = result.getString("Product");
                        String businessName = result.getString("BusinessName");
                        String facebook = result.getString("Facebook");
                        String instagram = result.getString("Instagram");
                        String website = result.getString("Website");
                        String hobi = result.getString("Hoby");
                        String tokopedia = result.optString("Tokopedia");
                        String bukalapak = result.optString("Bukalapak");
                        String shopee = result.optString("Shopee");
                        String city = result.optString("CityId");
                        String provinceId = result.optString("ProvinceId");
                        boolean isComplete = result.optBoolean("IsCompleted",false);
                        boolean allowShareProfile = result.optBoolean("AllowedShareProfile", false);
                        String saveContactFriendBy = "";
                        String saveMyContactBy = "";
                        String saveContactFriendInterest = "";
                        String saveMyContactInterest = "";
                        String save_other_key = "";
                        String save_other_key_friend = "";
                        String save_other_value = "";
                        String save_other_value_friend = "";

                        JSONObject json_saveContactFriendBy = result.optJSONObject("SaveContactFriendBy");
                        JSONObject json_saveMyContactBy = result.optJSONObject("SaveMyContactBy");
                        JSONObject json_saveContactFriendInterest = result.optJSONObject("SaveContactFriendBy");
                        JSONObject json_saveMyContactInterest = result.optJSONObject("SaveMyContactBy");
                        if (json_saveContactFriendBy != null){
                            saveContactFriendBy = json_saveContactFriendBy.optString("Type",null);
                        }
                        if (json_saveMyContactBy != null){
                            saveMyContactBy = json_saveMyContactBy.optString("Type",null);
                        }
                        if (json_saveContactFriendInterest != null){
                            saveContactFriendInterest = json_saveContactFriendInterest.optString("InterestId",null);
                            save_other_key_friend = json_saveContactFriendBy.optString("OtherKey",null);
                            save_other_value_friend = json_saveContactFriendBy.optString("OtherValue",null);
                        }

                        if (json_saveMyContactInterest != null){
                            saveMyContactInterest = json_saveMyContactInterest.optString("InterestId",null);
                            save_other_key = json_saveMyContactBy.optString("OtherKey",null);
                            save_other_value = json_saveMyContactBy.optString("OtherValue",null);
                        }

                        String saveLimitContactFriend = result.optString("LimitSaveContactFriendDay",null);
                        String saveLimitMyContact = result.optString("LimitSaveMyContactDay",null);

                        boolean is_owner = result.getBoolean("IsOwner");
                        String foto = result.optString("Foto");
                        String cover = result.optString("CoverContact");

                        JSONObject auth = result.getJSONObject("Auth");
                        String token = auth.getString("Key");
                        session.createLoginSession(user_id,cust_id,email,nickname,name,greeting,is_owner,foto,token);
//                        set Profile Session
                        HashMap<String,String> map = new HashMap<>();
                        map.put(KEY_USER_ID,user_id);
                        map.put(KEY_GREETING,greeting);
                        map.put(KEY_GENDER,gender);
                        map.put(KEY_PROFESSION,professionId);
                        map.put(KEY_AGAMA,agama);
                        map.put(KEY_INTEREST,interestId);
                        map.put(KEY_STATUS_KAWIN,relationshipStatus);
                        map.put(KEY_BUSINESS_TYPE,businessType);

                        map.put(KEY_NAME,name);
                        map.put(KEY_NICKNAME,nickname);
                        map.put(KEY_WHATSAPP,whatsApp);
                        map.put(KEY_EMAIL,email);
                        map.put(KEY_TGL_LAHIR,tgllahir);
                        map.put(KEY_ADDRESS,address);
                        map.put(KEY_FACEBOOK,facebook);
                        map.put(KEY_INSTAGRAM,instagram);
                        map.put(KEY_WEBSITE,website);
                        map.put(KEY_HOBI,hobi);
                        map.put(KEY_BUSINESS_NAME,businessName);
                        map.put(KEY_PRODUCT,product);
                        map.put(KEY_MARKETING_CODE,marketingCode);
                        map.put(KEY_TOKOPEDIA,tokopedia);
                        map.put(KEY_BUKALAPAK,bukalapak);
                        map.put(KEY_SHOPEE,shopee);
                        session.setProfile(map);
                        session.setSession(KEY_FOTO,foto);
                        session.setSession(KEY_COVER,cover);
                        session.setSession(KEY_CITY,city);
                        session.setSession(KEY_PROVINCE,provinceId);
                        HashMap<String,String> mapSetting = new HashMap<>();
                        mapSetting.put(KEY_ALLOW_SHARE_PROFILE,String.valueOf(allowShareProfile));
                        mapSetting.put(KEY_LIMIT_SAVE_CONTACT_FRIEND,saveLimitContactFriend);
                        mapSetting.put(KEY_LIMIT_SAVE_MY_CONTACT,saveLimitMyContact);
                        mapSetting.put(KEY_SAVE_CONTACT_FRIEND_BY,saveContactFriendBy);
                        mapSetting.put(KEY_SAVE_MY_CONTACT_BY,saveMyContactBy);
                        mapSetting.put(KEY_SAVE_CONTACT_FRIEND_INTEREST,saveContactFriendInterest);
                        mapSetting.put(KEY_SAVE_MY_CONTACT_INTEREST,saveMyContactInterest);
                        mapSetting.put(KEY_SAVE_OTHER_KEY,save_other_key);
                        mapSetting.put(KEY_SAVE_OTHER_KEY_FRIEND,save_other_key_friend);
                        mapSetting.put(KEY_SAVE_OTHER_VALUE,save_other_value);
                        mapSetting.put(KEY_SAVE_OTHER_VALUE_FRIEND,save_other_value);

                        session.setSettingContact(mapSetting);

                        sharedPref.createSession(SharedPref.KEY_PROFILE_IS_COMPLETE,isComplete);
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }else{
                        new AlertDialog.Builder(LoginActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    new AlertDialog.Builder(LoginActivity.this)
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
                        new AlertDialog.Builder(LoginActivity.this)
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
}