package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.view.ItemRecyclerTag;
import id.my.hubkontak.utils.view.RecyclerTagAdapter;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_ALLOW_SHARE_PROFILE;
import static id.my.hubkontak.utils.SessionManager.KEY_CITY;
import static id.my.hubkontak.utils.SessionManager.KEY_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_CONTACT_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_MY_CONTACT;
import static id.my.hubkontak.utils.SessionManager.KEY_PROVINCE;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_KEY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_KEY_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_VALUE;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_OTHER_VALUE_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.convertDpToPixel;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class SettingContactActivity extends AppCompatActivity {

    private static final String TAG = SettingContactActivity.class.getSimpleName();
    private static final int REQUEST_MY_INTEREST = 100;
    private static final int REQUEST_INTEREST_FRIEND = 101;
    private static final String JENIS_KELAMIN = "jenis kelamin";
    private static final String AGAMA = "agama";
    private static final String KETERTARIKAN = "ketertarikan";
    private static final String ACAK = "acak";
    private static final String KOTA = "kota";
    private static final String LAKI_LAKI = "Laki-laki";
    private static final String PEREMPUAN = "Perempuan";
    private static final String ISLAM = "Islam";
    private static final String KATOLIK = "Kristen Katolik";
    private static final String PROTESTAN = "Kristen Protestan";
    private static final String HINDU = "Hindu";
    private static final String BUDHA = "Budha";
    private static final String OTHER = "other";
    private Spinner spinSaveMyContactBy,spinSaveContactFriendBy;
    private EditText edtLimitSaveMyContact,edtLimitSaveContactFriend;
    private Button btnSimpan;
    private CheckBox chkAllow;
    private List<String> arrSpinner = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int processProgressdialog  = 0;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private Toolbar toolbar;
    private HashMap<String, String> settingContact;
    private LinearLayout layInterestFriend,layMyInterest,layAgama,layJenisKelamin,layKota,layAgamaFriend,layJenisKelaminFriend,layKotaFriend;
    private List<String> arrInterest = new ArrayList<>();
    private RecyclerView recyclerViewMyInterest,recyclerViewInterestFriend;
    private ImageButton btnCariMyInterest,btnCariInterestFriend;

    private RecyclerTagAdapter adapterMyTag,adapterTagFriend;
    private List<ItemRecyclerTag> listMyInterest = new ArrayList<>();
    private List<ItemRecyclerTag> listInterestFriend = new ArrayList<>();
    private JSONArray excludeMyInterest = new JSONArray();
    private JSONArray excludeInterestFriend = new JSONArray();
    private Spinner spinAgama,spinJenisKelamin,spinKota,spinAgamaFriend,spinJenisKelaminFriend,spinKotaFriend,spinProvinsi,spinProvinsiFriend;
    private List<String> arrAgama = new ArrayList<>();
    private List<String> arrJenisKelamin = new ArrayList<>();
    private List<String> arrProvinsi = new ArrayList<>();
    private List<String> arrProvinsiId = new ArrayList<>();
    private List<String> arrKota = new ArrayList<>();
    private List<String> arrKotaId = new ArrayList<>();
    private List<String> arrKotaFriend = new ArrayList<>();
    private List<String> arrKotaFriendId = new ArrayList<>();
    private List<String[]> arrKotaProvinsi = new ArrayList<>();
    private String selected_kota = "";
    private String selected_kota_friend = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_contact);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        settingContact = session.getSettingContact();
        apiKey = userDetail.get(KEY_TOKEN);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        layInterestFriend = (LinearLayout) findViewById(R.id.layInterestFriend);
        layMyInterest = (LinearLayout) findViewById(R.id.layMyInterest);
        layAgama = (LinearLayout) findViewById(R.id.layAgama);
        layJenisKelamin = (LinearLayout) findViewById(R.id.layJenisKelamin);
        layKota = (LinearLayout) findViewById(R.id.layKota);
        layAgamaFriend = (LinearLayout) findViewById(R.id.layAgamaFriend);
        layJenisKelaminFriend = (LinearLayout) findViewById(R.id.layJenisKelaminFriend);
        layKotaFriend = (LinearLayout) findViewById(R.id.layKotaFriend);

        spinAgama = (Spinner) findViewById(R.id.spinAgama);
        spinJenisKelamin = (Spinner) findViewById(R.id.spinJenisKelamin);
        spinKota = (Spinner) findViewById(R.id.spinKota);
        spinProvinsi = (Spinner) findViewById(R.id.spinProvinsi);
        spinAgamaFriend = (Spinner) findViewById(R.id.spinAgamaFriend);
        spinJenisKelaminFriend = (Spinner) findViewById(R.id.spinJenisKelaminFriend);
        spinProvinsiFriend = (Spinner) findViewById(R.id.spinProvinsiFriend);
        spinKotaFriend = (Spinner) findViewById(R.id.spinKotaFriend);
        spinSaveMyContactBy = (Spinner) findViewById(R.id.spinSaveMyContactBy);
        spinSaveContactFriendBy = (Spinner) findViewById(R.id.spinSaveContactFriendBy);
        edtLimitSaveMyContact = (EditText) findViewById(R.id.edtLimitSaveMyContact);
        edtLimitSaveContactFriend = (EditText) findViewById(R.id.edtLimitSaveContactFriend);
        chkAllow = (CheckBox) findViewById(R.id.checkBox);
        btnSimpan =(Button) findViewById(R.id.btnSimpan);
        btnCariMyInterest =(ImageButton) findViewById(R.id.btnCariMyInterest);
        btnCariInterestFriend =(ImageButton) findViewById(R.id.btnCariInterestFriend);

        btnCariMyInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingContactActivity.this, CariInterestActivity.class);
                i.putExtra("exclude",excludeMyInterest.toString());
                startActivityForResult(i,REQUEST_MY_INTEREST);
            }
        });
        btnCariInterestFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingContactActivity.this, CariInterestActivity.class);
                i.putExtra("exclude",excludeInterestFriend.toString());
                startActivityForResult(i,REQUEST_INTEREST_FRIEND);
            }
        });
        recyclerViewMyInterest = (RecyclerView) findViewById(R.id.gridMyInterest);
        recyclerViewMyInterest.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMyInterest.setMinimumHeight((int) convertDpToPixel(35,this));
        new LinearSnapHelper().attachToRecyclerView(recyclerViewMyInterest);


        recyclerViewInterestFriend = (RecyclerView) findViewById(R.id.gridInterestFriend);
        recyclerViewInterestFriend.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewInterestFriend.setMinimumHeight((int) convertDpToPixel(35,this));
        new LinearSnapHelper().attachToRecyclerView(recyclerViewInterestFriend);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerViewMyInterest.setLayoutManager(layoutManager);
        FlexboxLayoutManager layoutManagerFriend = new FlexboxLayoutManager();
        layoutManagerFriend.setFlexWrap(FlexWrap.WRAP);
        layoutManagerFriend.setFlexDirection(FlexDirection.ROW);
        recyclerViewInterestFriend.setLayoutManager(layoutManagerFriend);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSimpan();
            }
        });
//        loadInterest();

        spinSaveMyContactBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                layMyInterest.setVisibility(View.GONE);
                layAgama.setVisibility(View.GONE);
                layJenisKelamin.setVisibility(View.GONE);
                layKota.setVisibility(View.GONE);
                if (arrSpinner.get(i).equals(KETERTARIKAN)){
                    layMyInterest.setVisibility(View.VISIBLE);
                }else if(arrSpinner.get(i).equals(AGAMA)){
                    layAgama.setVisibility(View.VISIBLE);
                }else if(arrSpinner.get(i).equals(KOTA)){
                    layKota.setVisibility(View.VISIBLE);
                }else if(arrSpinner.get(i).equals(JENIS_KELAMIN)){
                    layJenisKelamin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinSaveContactFriendBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                layInterestFriend.setVisibility(View.GONE);
                layAgamaFriend.setVisibility(View.GONE);
                layJenisKelaminFriend.setVisibility(View.GONE);
                layKotaFriend.setVisibility(View.GONE);
                if (arrSpinner.get(i).equals(KETERTARIKAN)){
                    layInterestFriend.setVisibility(View.VISIBLE);
                }else if (arrSpinner.get(i).equals(AGAMA)) {
                    layAgamaFriend.setVisibility(View.VISIBLE);
                }else if (arrSpinner.get(i).equals(JENIS_KELAMIN)) {
                    layJenisKelaminFriend.setVisibility(View.VISIBLE);
                }else if (arrSpinner.get(i).equals(KOTA)) {
                    layKotaFriend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int province_id = 0;
                if (!arrProvinsiId.get(spinProvinsi.getSelectedItemPosition()).equals("")){
                    province_id = Integer.parseInt(arrProvinsiId.get(spinProvinsi.getSelectedItemPosition()));
                }
                loadCity(province_id,selected_kota);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinProvinsiFriend.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int province_id = 0;
                if (!arrProvinsiId.get(spinProvinsiFriend.getSelectedItemPosition()).equals("")){
                    province_id = Integer.parseInt(arrProvinsiId.get(spinProvinsiFriend.getSelectedItemPosition()));
                }
                loadCityFriend(province_id,selected_kota_friend);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        loadSpinner();
        loadProvinsi();

        interestAdapterSetup();
    }

    private void loadProvinsi() {
        arrProvinsi.clear();
        arrProvinsiId.add("");
        arrProvinsi.add("Pilih Provinsi");
        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_PROVINCE)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");
                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        for (int i = 0; i<result.length(); i++){
                            String provinceId = result.getJSONObject(i).getString("ProvinceId");
                            String province = result.getJSONObject(i).getString("Province");
                            arrProvinsiId.add(provinceId);
                            arrProvinsi.add(province);
                        }
                        displayProvince();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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

    private void displayProvince() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrProvinsi);
        spinProvinsi.setAdapter(adapter);
        spinProvinsiFriend.setAdapter(adapter);
        loadCityAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MY_INTEREST ){
            if (resultCode == RESULT_OK){
                try {
                    JSONArray interestId = new JSONArray(data.getStringExtra("interestId"));
                    for (int i = 0;i<interestId.length();i++){
                        listMyInterest.add(new ItemRecyclerTag(interestId.getString(i),interestId.getString(i)));
                    }
                    adapterMyTag.notifyDataSetChanged();
                    reloadExcludeInterest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == REQUEST_INTEREST_FRIEND ){
            if (resultCode == RESULT_OK){
                try {
                    JSONArray interestId = new JSONArray(data.getStringExtra("interestId"));
                    for (int i = 0;i<interestId.length();i++){
                        listInterestFriend.add(new ItemRecyclerTag(interestId.getString(i),interestId.getString(i)));
                    }
                    adapterTagFriend.notifyDataSetChanged();
                    reloadExcludeInterest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadSpinner() {
        arrSpinner.clear();
        arrSpinner.add(ACAK);
        arrSpinner.add(KETERTARIKAN);
        arrSpinner.add(JENIS_KELAMIN);
        arrSpinner.add(AGAMA);
        arrSpinner.add(KOTA);

        arrAgama.clear();
        arrJenisKelamin.clear();

        arrJenisKelamin.add(LAKI_LAKI);
        arrJenisKelamin.add(PEREMPUAN);

        arrAgama.add(ISLAM);
        arrAgama.add(KATOLIK);
        arrAgama.add(PROTESTAN);
        arrAgama.add(HINDU);
        arrAgama.add(BUDHA);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrSpinner);
        ArrayAdapter<String> adapterAgama = new ArrayAdapter<>(this, R.layout.item_spinner, arrAgama);
        ArrayAdapter<String> adapterJenisKelamin = new ArrayAdapter<>(this, R.layout.item_spinner, arrJenisKelamin);
        spinSaveContactFriendBy.setAdapter(adapter);
        spinSaveMyContactBy.setAdapter(adapter);
        spinAgama.setAdapter(adapterAgama);
        spinAgamaFriend.setAdapter(adapterAgama);
        spinJenisKelamin.setAdapter(adapterJenisKelamin);
        spinJenisKelaminFriend.setAdapter(adapterJenisKelamin);

//        loadData();
    }
    private void loadCityAll() {

        arrKotaProvinsi.clear();
        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        JSONObject parameter = new JSONObject();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_CITY)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");
                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");

                        for (int i=0;i<result.length();i++){
                            String cityId = result.getJSONObject(i).getString("CityId");
                            String city = result.getJSONObject(i).getString("City");
                            String provinceId = result.getJSONObject(i).getString("ProvinceId");
                            arrKotaProvinsi.add(new String[]{cityId,provinceId});
                        }
                        loadData();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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
    private void loadInterest() {

        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_INTEREST)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        arrInterest.clear();
                        for (int i=0;i<result.length();i++){
                            String interestId = result.getJSONObject(i).getString("InterestId");
                            arrInterest.add(interestId);
                        }
//                        displayInterest();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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

//    private void displayInterest() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrInterest);
//        spinInterestFriend.setAdapter(adapter);
//        spinMyInterest.setAdapter(adapter);
//
//        loadSpinner();
//    }
    private void loadData() {
        try{
            for (int x = 0;x<arrSpinner.size();x++){

                String save_friend_by = settingContact.get(KEY_SAVE_CONTACT_FRIEND_BY);
                String save_my_by = settingContact.get(KEY_SAVE_MY_CONTACT_BY);
                String save_other_key = settingContact.get(KEY_SAVE_OTHER_KEY);
                String save_other_key_friend = settingContact.get(KEY_SAVE_OTHER_KEY_FRIEND);
                String save_other_value = settingContact.get(KEY_SAVE_OTHER_VALUE);
                String save_other_value_friend = settingContact.get(KEY_SAVE_OTHER_VALUE_FRIEND);

                if (save_friend_by.equals("random")){
                    save_friend_by = ACAK;
                }else if(save_friend_by.equals("interest")){
                    save_friend_by = KETERTARIKAN;
                }else if(save_friend_by.equals(OTHER)){
                    if (save_other_key_friend.equals("Gender")){
                        save_friend_by = JENIS_KELAMIN;
                        for (int i = 0;i<arrJenisKelamin.size();i++){
                            if(save_other_value_friend.equals(arrJenisKelamin.get(i))){
                                spinJenisKelaminFriend.setSelection(i);
                                break;
                            }
                        }
                    }else if (save_other_key_friend.equals("Religion")) {
                        save_friend_by = AGAMA;
                        for (int i = 0;i<arrAgama.size();i++){
                            if(save_other_value_friend.equals(arrAgama.get(i))){
                                spinAgamaFriend.setSelection(i);
                                break;
                            }
                        }
                    }else if (save_other_key_friend.equals("CityId")) {
                        selected_kota_friend = save_other_value_friend;
                        save_friend_by = KOTA;
                        String province_selected_friend = null;
                        for (int i =0 ;i<arrKotaProvinsi.size();i++){
                            if(save_other_value_friend.equals(arrKotaProvinsi.get(i)[0])){
                                province_selected_friend = arrKotaProvinsi.get(i)[1];
                                break;
                            }
                        }
                        for (int i=0;i<arrProvinsiId.size();i++){
                            if (province_selected_friend.equals(arrProvinsiId.get(i))){
                                spinProvinsiFriend.setSelection(i);
                                break;
                            }
                        }
                        loadCityFriend(Integer.parseInt(province_selected_friend),save_other_value_friend);
                    }
                }
                if (save_my_by.equals("random")){
                    save_my_by = ACAK;
                }else if(save_my_by.equals("interest")){
                    save_my_by = KETERTARIKAN;
                }else if(save_my_by.equals(OTHER)){
                    if (save_other_key.equals("Gender")){
                        save_my_by = JENIS_KELAMIN;
                        for (int i = 0;i<arrJenisKelamin.size();i++){
                            if(save_other_value.equals(arrJenisKelamin.get(i))){
                                spinJenisKelamin.setSelection(i);
                                break;
                            }
                        }
                    }else if (save_other_key.equals("Religion")) {
                        save_my_by = AGAMA;
                        for (int i = 0;i<arrAgama.size();i++){
                            if(save_other_value.equals(arrAgama.get(i))){
                                spinAgama.setSelection(i);
                                break;
                            }
                        }
                    }else if (save_other_key.equals("CityId")) {
                        selected_kota = save_other_value;
                        save_my_by = KOTA;
                        String province_selected = null;
                        for (int i =0 ;i<arrKotaProvinsi.size();i++){
                            if(save_other_value.equals(arrKotaProvinsi.get(i)[0])){
                                province_selected = arrKotaProvinsi.get(i)[1];
                                break;
                            }
                        }
                        for (int i=0;i<arrProvinsiId.size();i++){
                            if (province_selected.equals(arrProvinsiId.get(i))){
                                spinProvinsi.setSelection(i);
                                break;
                            }
                        }
                        loadCity(Integer.parseInt(province_selected),save_other_value);
                    }

                }
                layInterestFriend.setVisibility(View.GONE);
                layAgamaFriend.setVisibility(View.GONE);
                layKotaFriend.setVisibility(View.GONE);
                layJenisKelaminFriend.setVisibility(View.GONE);
                layMyInterest.setVisibility(View.GONE);
                layAgama.setVisibility(View.GONE);
                layKota.setVisibility(View.GONE);
                layJenisKelamin.setVisibility(View.GONE);

                if (arrSpinner.get(x).equals(save_friend_by)){
                    spinSaveContactFriendBy.setSelection(x);
                    if(arrSpinner.get(x).equals(KETERTARIKAN)){
                        layInterestFriend.setVisibility(View.VISIBLE);
                    }else if(arrSpinner.get(x).equals(AGAMA)){
                        layAgamaFriend.setVisibility(View.VISIBLE);
                    }else if(arrSpinner.get(x).equals(JENIS_KELAMIN)){
                        layJenisKelaminFriend.setVisibility(View.VISIBLE);
                    }else if(arrSpinner.get(x).equals(KOTA)){
                        layKotaFriend.setVisibility(View.VISIBLE);
                    }
                }
                if (arrSpinner.get(x).equals(save_my_by)){
                    spinSaveMyContactBy.setSelection(x);
                    if (arrSpinner.get(x).equals(KETERTARIKAN)){
                        layMyInterest.setVisibility(View.VISIBLE);
                    }else if (arrSpinner.get(x).equals(AGAMA)){
                        layAgama.setVisibility(View.VISIBLE);
                    }else if (arrSpinner.get(x).equals(KOTA)){
                        layKotaFriend.setVisibility(View.VISIBLE);
                    }else if (arrSpinner.get(x).equals(JENIS_KELAMIN)){
                        layJenisKelamin.setVisibility(View.VISIBLE);
                    }
                }
            }

//        for(int i = 0;i<arrInterest.size();i++){
//            if (arrInterest.get(i).equals(settingContact.get(KEY_SAVE_CONTACT_FRIEND_INTEREST))){
//                spinInterestFriend.setSelection(i);
//            }
//            if (arrInterest.get(i).equals(settingContact.get(KEY_SAVE_MY_CONTACT_INTEREST))){
//                spinMyInterest.setSelection(i);
//            }
//        }
            try {

                chkAllow.setChecked(false);
                if (settingContact.get(KEY_ALLOW_SHARE_PROFILE).equals("true")){
                    chkAllow.setChecked(true);
                }
            }catch (NullPointerException e){

            }catch (Exception e){

            }

            String limit_contact_friend = settingContact.get(KEY_LIMIT_SAVE_CONTACT_FRIEND);
            String limit_my_contact = settingContact.get(KEY_LIMIT_SAVE_MY_CONTACT);
            if (limit_contact_friend.equals(null) || limit_contact_friend.equals("null") || limit_contact_friend == null){
                limit_contact_friend = "";
            }
            if (limit_my_contact.equals(null) || limit_my_contact.equals("null") || limit_my_contact == null){
                limit_my_contact = "";
            }
            edtLimitSaveContactFriend.setText(limit_contact_friend);
            edtLimitSaveMyContact.setText(limit_my_contact);


            listMyInterest.clear();
            listInterestFriend.clear();
            excludeInterestFriend = new JSONArray();
            excludeMyInterest = new JSONArray();
            try {
                JSONArray dataInterestIdFriend = new JSONArray(settingContact.get(KEY_SAVE_CONTACT_FRIEND_INTEREST));
                for (int i = 0;i<dataInterestIdFriend.length();i++){
                    listInterestFriend.add(new ItemRecyclerTag(dataInterestIdFriend.getString(i),dataInterestIdFriend.getString(i)));
                }
                JSONArray dataMyInterestId = new JSONArray(settingContact.get(KEY_SAVE_MY_CONTACT_INTEREST));
                for (int i = 0;i<dataMyInterestId.length();i++){
                    listMyInterest.add(new ItemRecyclerTag(dataMyInterestId.getString(i),dataMyInterestId.getString(i)));
                }
                reloadExcludeInterest();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            interestAdapterSetup();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void loadCity(int provinceId,String selected_id) {

        arrKotaId.clear();
        arrKota.clear();
        arrKotaId.add("");
        arrKota.add("Pilih Kota");
        if (spinProvinsi.getSelectedItemPosition() == 0){
            return;
        }
        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("ProvinceId",provinceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_CITY)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");
                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        int index_selected_city = 0;

                        for (int i=0;i<result.length();i++){
                            String cityId = result.getJSONObject(i).getString("CityId");
                            String city = result.getJSONObject(i).getString("City");
                            String provinceId = result.getJSONObject(i).getString("ProvinceId");
                            arrKotaId.add(cityId);
                            arrKota.add(city);
                            if (selected_id.equals(cityId)){
                                index_selected_city = i+1;
                            }

                        }
                        displayCity();
                        spinKota.setSelection(index_selected_city);

                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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
    private void loadCityFriend(int provinceId,String selected_id) {

        arrKotaFriendId.clear();
        arrKotaFriend.clear();
        arrKotaFriendId.add("");
        arrKotaFriend.add("Pilih Kota");
        if (spinProvinsiFriend.getSelectedItemPosition() == 0){
            return;
        }
        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("ProvinceId",provinceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_CITY)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");
                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        int index_selected_city = 0;

                        for (int i=0;i<result.length();i++){
                            String cityId = result.getJSONObject(i).getString("CityId");
                            String city = result.getJSONObject(i).getString("City");
                            String provinceId = result.getJSONObject(i).getString("ProvinceId");
                            arrKotaFriendId.add(cityId);
                            arrKotaFriend.add(city);
                            if (selected_id.equals(cityId)){
                                index_selected_city = i+1;
                            }
                        }
                        displayCityFriend();
                        spinKotaFriend.setSelection(index_selected_city);
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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
    private void displayCity() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrKota);
        spinKota.setAdapter(adapter);
    }

    private void displayCityFriend() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrKotaFriend);
        spinKotaFriend.setAdapter(adapter);
    }
    private void doSimpan() {

        boolean allow = false;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        processProgressdialog = processProgressdialog + 1;
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_UPDATE_PROFILE)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        String save_contact_friend_by = null;
        String save_my_contact_by = null;

        String other_key = "";
        String other_key_friend = "";
        String other_value_friend = "";
        String other_value = "";
        try {
            if (arrSpinner.get(spinSaveMyContactBy.getSelectedItemPosition()).equals(JENIS_KELAMIN)) {
                other_key = "Gender";
                other_value = arrJenisKelamin.get(spinJenisKelamin.getSelectedItemPosition());
            }else if (arrSpinner.get(spinSaveMyContactBy.getSelectedItemPosition()).equals(AGAMA)){
                other_key = "Religion";
                other_value = arrAgama.get(spinAgama.getSelectedItemPosition());
            }else if (arrSpinner.get(spinSaveMyContactBy.getSelectedItemPosition()).equals(KOTA)){
                other_key = "CityId";
                other_value = arrKotaId.get(spinKota.getSelectedItemPosition());
            }

            if (arrSpinner.get(spinSaveContactFriendBy.getSelectedItemPosition()).equals(JENIS_KELAMIN)) {
                other_key_friend = "Gender";
                other_value_friend = arrJenisKelamin.get(spinJenisKelaminFriend.getSelectedItemPosition());
            }else if (arrSpinner.get(spinSaveContactFriendBy.getSelectedItemPosition()).equals(AGAMA)){
                other_key_friend = "Religion";
                other_value_friend = arrAgama.get(spinAgamaFriend.getSelectedItemPosition());
            }else if (arrSpinner.get(spinSaveContactFriendBy.getSelectedItemPosition()).equals(KOTA)){
                other_key_friend = "CityId";
                other_value_friend = arrKotaFriendId.get(spinKotaFriend.getSelectedItemPosition());
            }
            if(chkAllow.isChecked()){
                allow = true;
            }else{
                allow = false;
            }
            save_contact_friend_by = arrSpinner.get(spinSaveContactFriendBy.getSelectedItemPosition());
            save_my_contact_by = arrSpinner.get(spinSaveMyContactBy.getSelectedItemPosition());
            if(save_contact_friend_by.equals(ACAK)){
                save_contact_friend_by = "random";
            }else if(save_contact_friend_by.equals(KETERTARIKAN)) {
                save_contact_friend_by = "interest";
            }else{
                save_contact_friend_by = "other";

            }
            if(save_my_contact_by.equals(ACAK)){
                save_my_contact_by = "random";
            }else if(save_my_contact_by.equals(KETERTARIKAN)) {
                save_my_contact_by = "interest";
            }else{
                save_my_contact_by = "other";
            }

            parameter.put("AllowedShareProfile", allow);
            parameter.put("SaveContactFriendBy", save_contact_friend_by);
            parameter.put("SaveMyContactBy",  save_my_contact_by);
            parameter.put("SaveMyContactInterest",  excludeMyInterest);
            parameter.put("SaveContactFriendInterest",  excludeInterestFriend);
            parameter.put("LimitSaveMyContactDay", edtLimitSaveMyContact.getText().toString());
            parameter.put("LimitSaveContactFriendDay", edtLimitSaveContactFriend.getText().toString());
            parameter.put("SaveMyContactOtherKey", other_key);
            parameter.put("SaveMyContactOtherValue", other_value);
            parameter.put("SaveContactFriendOtherKey", other_key_friend);
            parameter.put("SaveContactFriendOtherValue", other_value_friend);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG,apiKey);
        Log.e(TAG,parameter.toString());
        boolean finalAllow = allow;
        String finalSave_contact_friend_by = save_contact_friend_by;
        String finalSave_my_contact_by = save_my_contact_by;
        String finalOther_key = other_key;
        String finalOther_key_friend = other_key_friend;
        String finalOther_value_friend = other_value_friend;
        String finalOther_value = other_value;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        HashMap<String,String> map = new HashMap<>();
                        if (finalAllow == true){
                            map.put(KEY_ALLOW_SHARE_PROFILE, "true");
                            Log.e(TAG,"allow:true");
                        }else{
                            Log.e(TAG,"allow:false");
                            map.put(KEY_ALLOW_SHARE_PROFILE, "false");
                        }

                        map.put(KEY_SAVE_CONTACT_FRIEND_BY, finalSave_contact_friend_by);
                        map.put(KEY_SAVE_MY_CONTACT_BY, finalSave_my_contact_by);
                        map.put(KEY_LIMIT_SAVE_CONTACT_FRIEND, edtLimitSaveContactFriend.getText().toString());
                        map.put(KEY_LIMIT_SAVE_MY_CONTACT, edtLimitSaveMyContact.getText().toString());
                        map.put(KEY_SAVE_CONTACT_FRIEND_INTEREST, excludeInterestFriend.toString());
                        map.put(KEY_SAVE_MY_CONTACT_INTEREST, excludeMyInterest.toString());

                        map.put(KEY_SAVE_OTHER_KEY, finalOther_key);
                        map.put(KEY_SAVE_OTHER_KEY_FRIEND, finalOther_key_friend);
                        map.put(KEY_SAVE_OTHER_VALUE_FRIEND, finalOther_value_friend);
                        map.put(KEY_SAVE_OTHER_VALUE, finalOther_value);
                        session.setSettingContact(map);
                        finish();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(SettingContactActivity.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                processProgressdialog = processProgressdialog - 1;
                if(processProgressdialog == 0){
                    progressDialog.dismiss();
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    json = new String(networkResponse.data);
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String message = jsonObject.getString("message");

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(SettingContactActivity.this)
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
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                header.put("X-API-KEY",apiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadExcludeInterest(){
        excludeMyInterest = new JSONArray();
        excludeInterestFriend = new JSONArray();
        for (int i = 0;i<listMyInterest.size();i++){
            excludeMyInterest.put(listMyInterest.get(i).getId());
        }
        for (int i = 0;i<listInterestFriend.size();i++){
            excludeInterestFriend.put(listInterestFriend.get(i).getId());
        }
    }
    private void interestAdapterSetup(){
        adapterMyTag = new RecyclerTagAdapter(listMyInterest, this,new RecyclerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i = 0; i< listMyInterest.size(); i++){
                    if (listMyInterest.get(i).getId().equals(item.getId())){
                        listMyInterest.remove(i);
                        adapterMyTag.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeInterest();
            }
        });
        recyclerViewMyInterest.setAdapter(adapterMyTag);

        adapterTagFriend = new RecyclerTagAdapter(listInterestFriend, this,new RecyclerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i = 0; i< listInterestFriend.size(); i++){
                    if (listInterestFriend.get(i).getId().equals(item.getId())){
                        listInterestFriend.remove(i);
                        adapterTagFriend.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeInterest();
            }
        });
        recyclerViewInterestFriend.setAdapter(adapterTagFriend);
    }
}