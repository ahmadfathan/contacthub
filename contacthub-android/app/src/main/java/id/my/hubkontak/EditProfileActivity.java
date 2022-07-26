package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;
import id.my.hubkontak.utils.view.ItemRecyclerTag;
import id.my.hubkontak.utils.view.RecyclerTagAdapter;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_ADDRESS;
import static id.my.hubkontak.utils.SessionManager.KEY_AGAMA;
import static id.my.hubkontak.utils.SessionManager.KEY_BUKALAPAK;
import static id.my.hubkontak.utils.SessionManager.KEY_BUSINESS_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_BUSINESS_TYPE;
import static id.my.hubkontak.utils.SessionManager.KEY_CITY;
import static id.my.hubkontak.utils.SessionManager.KEY_EMAIL;
import static id.my.hubkontak.utils.SessionManager.KEY_FACEBOOK;
import static id.my.hubkontak.utils.SessionManager.KEY_GENDER;
import static id.my.hubkontak.utils.SessionManager.KEY_GREETING;
import static id.my.hubkontak.utils.SessionManager.KEY_HOBI;
import static id.my.hubkontak.utils.SessionManager.KEY_INSTAGRAM;
import static id.my.hubkontak.utils.SessionManager.KEY_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_NICKNAME;
import static id.my.hubkontak.utils.SessionManager.KEY_PRODUCT;
import static id.my.hubkontak.utils.SessionManager.KEY_PROFESSION;
import static id.my.hubkontak.utils.SessionManager.KEY_PROVINCE;
import static id.my.hubkontak.utils.SessionManager.KEY_SHOPEE;
import static id.my.hubkontak.utils.SessionManager.KEY_STATUS_KAWIN;
import static id.my.hubkontak.utils.SessionManager.KEY_TGL_LAHIR;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKOPEDIA;
import static id.my.hubkontak.utils.SessionManager.KEY_WEBSITE;
import static id.my.hubkontak.utils.SessionManager.KEY_WHATSAPP;
import static id.my.hubkontak.utils.Utils.convertDpToPixel;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    private static final int REQUEST_INTEREST = 100;
    private SessionManager session;
    private Toolbar toolbar;
    private EditText edtNama,edtNickname,edtWhatsApp,edtBusinessName,edtAddress,edtProduct,edtFacebook,edtInstagram,edtHobi,edtTglLahir,edtWebsite;
    private HashMap<String, String> userDetail;
    private Spinner spinGreeting;

    private List<String> arrayJK = new ArrayList<>();
    private List<String> arrayAgama = new ArrayList<>();

    private List<String> arrGreeting = new ArrayList<>();
    private Spinner spinGender,spinProfession,spinStatusKawin,spinAgama,spinBusinessType;
    private ProgressDialog progressDialog;
    private int processProgressdialog = 0;
    private List<String> arrProfession = new ArrayList<>();
    private List<String> arrInterest = new ArrayList<>();
    private List<String> arrBusinessType = new ArrayList<>();
    private List<String> arrayKawin = new ArrayList<>();
    private List<String> arrProvinsiId = new ArrayList<>();
    private List<String> arrProvinsi = new ArrayList<>();
    private List<String> arrKotaId = new ArrayList<>();
    private List<String> arrKota = new ArrayList<>();
    private CheckBox checkAgreement;
    private Button btnSimpan;
    private Calendar myCalendar;
    private String apiKey;
    private EditText edtEmail;
    private SharedPref sharedPref;
    private EditText edtTokopedia,edtBukalapak,edtShopee;
    private RecyclerTagAdapter adapterTag;
    private List<ItemRecyclerTag> listInterest = new ArrayList<>();
    private RecyclerView recyclerViewInterest;
    private ImageButton btnCariInterest;
    private JSONArray excludeInterest = new JSONArray();
    private Spinner spinProvinsi,spinKota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPref = new SharedPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        apiKey = userDetail.get(KEY_TOKEN);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);

        btnCariInterest = (ImageButton) findViewById(R.id.btnCariInterest);
        edtTokopedia = (EditText) findViewById(R.id.edtTokopedia);
        edtBukalapak = (EditText) findViewById(R.id.edtBukalapak);
        edtShopee = (EditText) findViewById(R.id.edtShopee);
        edtNama = (EditText) findViewById(R.id.edtNama);
        edtNickname = (EditText) findViewById(R.id.edtNickname);
        edtWhatsApp = (EditText) findViewById(R.id.edtWhatsApp);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtBusinessName = (EditText) findViewById(R.id.edtBusinessName);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        edtProduct = (EditText) findViewById(R.id.edtProduct);
        edtFacebook = (EditText) findViewById(R.id.edtFacebook);
        edtInstagram = (EditText) findViewById(R.id.edtInstagram);
        edtHobi = (EditText) findViewById(R.id.edtHobi);
        edtWebsite = (EditText) findViewById(R.id.edtWebsite);
        edtTglLahir = (EditText) findViewById(R.id.edtTglLahir);

        spinProvinsi = (Spinner) findViewById(R.id.spinProvinsi);
        spinKota = (Spinner) findViewById(R.id.spinKota);
        spinGreeting = (Spinner) findViewById(R.id.spinGreeting);
        spinGender = (Spinner) findViewById(R.id.spinGender);
        spinBusinessType = (Spinner) findViewById(R.id.spinBusinessType);
        spinAgama = (Spinner) findViewById(R.id.spinAgama);
        spinProfession = (Spinner) findViewById(R.id.spinProfession);
        spinStatusKawin = (Spinner) findViewById(R.id.spinRelationshipStatus);

        spinProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadCity();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        recyclerViewInterest = (RecyclerView) findViewById(R.id.gridInterest);
        recyclerViewInterest.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerViewInterest);


        recyclerViewInterest.setMinimumHeight((int) convertDpToPixel(35,this));
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerViewInterest.setLayoutManager(layoutManager);

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateTgl();
            }

        };
        btnCariInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfileActivity.this, CariInterestActivity.class);
                i.putExtra("exclude",excludeInterest.toString());
                startActivityForResult(i,REQUEST_INTEREST);
            }
        });
        edtTglLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditProfileActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        checkAgreement = (CheckBox) findViewById(R.id.checkBox);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtNama.getText())){
                    edtNama.setError("Field ini tidak boleh kosong");
                    edtNama.requestFocus();
                }else if (TextUtils.isEmpty(edtNickname.getText())) {
                    edtNickname.setError("Field ini tidak boleh kosong");
                    edtNickname.requestFocus();
                }else if (TextUtils.isEmpty(edtWhatsApp.getText())) {
                    edtWhatsApp.setError("Field ini tidak boleh kosong");
                    edtWhatsApp.requestFocus();
                }else if (TextUtils.isEmpty(edtEmail.getText())) {
                    edtEmail.setError("Field ini tidak boleh kosong");
                    edtEmail.requestFocus();
                }else if (TextUtils.isEmpty(edtTglLahir.getText())) {
                    edtTglLahir.setError("Field ini tidak boleh kosong");
                    edtTglLahir.requestFocus();
                }else if (TextUtils.isEmpty(edtAddress.getText())) {
                    edtAddress.setError("Field ini tidak boleh kosong");
                    edtAddress.requestFocus();
                }else if (TextUtils.isEmpty(edtHobi.getText())) {
                    edtHobi.setError("Field ini tidak boleh kosong");
                    edtHobi.requestFocus();
                }else if (TextUtils.isEmpty(edtBusinessName.getText())) {
                    edtBusinessName.setError("Field ini tidak boleh kosong");
                    edtBusinessName.requestFocus();
                }else if (TextUtils.isEmpty(edtProduct.getText())) {
                    edtProduct.setError("Field ini tidak boleh kosong");
                    edtProduct.requestFocus();
                }else if (spinKota.getSelectedItemPosition() == 0) {
                    Toast.makeText(EditProfileActivity.this, "Kota belum dipilih", Toast.LENGTH_SHORT).show();
                    spinKota.requestFocus();
                }else if(checkAgreement.isChecked() == false) {
                    Toast.makeText(EditProfileActivity.this, "Anda belum menyetujui persetujuan Edit Profile, silahkan ceklis persetujuannya", Toast.LENGTH_SHORT).show();
                    checkAgreement.requestFocus();
                }else{
                    doSimpan();
                }
            }
        });
        loadGreeting();
        loadReligion();
        loadJK();
        loadProfession();
        loadBusinessType();
        loadStatusKawin();
        getDataProfile();
        loadProvince();
        interestAdapterSetup();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTgl() {
        String myFormat = "yyyy-MM-dd";
        Locale ID = new Locale("in", "ID");
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, ID);

        edtTglLahir.setText(sdf.format(myCalendar.getTime()));
    }
    private void loadStatusKawin() {
        arrayKawin.clear();
        arrayKawin.add("Menikah");
        arrayKawin.add("Belum Menikah");
        arrayKawin.add("Cerai");
        displayStatusKawin();
    }

    private void loadJK(){
        arrayJK.clear();
        arrayJK.add("Laki-laki");
        arrayJK.add("Perempuan");
        displayGender();
    }
    private void loadReligion() {
        arrayAgama.clear();
        arrayAgama.add("Islam");
        arrayAgama.add("Kristen Katolik");
        arrayAgama.add("Kristen Protestan");
        arrayAgama.add("Hindu");
        arrayAgama.add("Budha");
        displayAgama();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INTEREST){
            if (resultCode == RESULT_OK){
                try {
                    JSONArray interestId = new JSONArray(data.getStringExtra("interestId"));
                    for(int i = 0;i<interestId.length();i++){
                        listInterest.add(new ItemRecyclerTag(interestId.getString(i),interestId.getString(i)));
                    }
                    reloadExcludeInterest();
                    adapterTag.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doSimpan() {
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
        JSONArray dataInterest = new JSONArray();
        try {
            dataInterest = new JSONArray();
            for(int i = 0;i<listInterest.size();i++){
                dataInterest.put(listInterest.get(i).getId());
            }
            parameter.put("Name", edtNama.getText().toString());
            parameter.put("Address", edtAddress.getText().toString());
            parameter.put("WhatsApp", edtWhatsApp.getText().toString());
            parameter.put("Facebook", edtFacebook.getText().toString());
            parameter.put("Instagram", edtInstagram.getText().toString());
            parameter.put("Website", edtWebsite.getText().toString());
            parameter.put("Greeting", arrGreeting.get(spinGreeting.getSelectedItemPosition()));
            parameter.put("Gender", arrayJK.get(spinGender.getSelectedItemPosition()));
            parameter.put("DateOfBirth", edtTglLahir.getText().toString());
            parameter.put("Hoby", edtHobi.getText().toString());
            parameter.put("BusinessName", edtBusinessName.getText().toString());
            parameter.put("Product", edtProduct.getText().toString());
            parameter.put("Nickname", edtNickname.getText().toString());
            parameter.put("Email", edtEmail.getText().toString());
            parameter.put("BusinessTypeId", arrBusinessType.get(spinBusinessType.getSelectedItemPosition()));
            parameter.put("ProfessionId", arrProfession.get(spinProfession.getSelectedItemPosition()));
            parameter.put("Religion", arrayAgama.get(spinAgama.getSelectedItemPosition()));
            parameter.put("InterestId", dataInterest);
            parameter.put("RelationshipStatus", arrayKawin.get(spinStatusKawin.getSelectedItemPosition()));
            parameter.put("IsCompleted", true);
            parameter.put("Tokopedia", edtTokopedia.getText().toString());
            parameter.put("Bukalapak", edtBukalapak.getText().toString());
            parameter.put("Shopee", edtShopee.getText().toString());
            parameter.put("CityId", arrKotaId.get(spinKota.getSelectedItemPosition()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray finalDataInterest = dataInterest;
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
                        JSONObject result = response.getJSONObject("result");
                        boolean isCompleted = result.optBoolean("IsCompleted",false);
                        HashMap<String,String> map = new HashMap<>();
                        session.setSession(KEY_GREETING,arrGreeting.get(spinGreeting.getSelectedItemPosition()));
                        session.setSession(KEY_PROFESSION,arrProfession.get(spinProfession.getSelectedItemPosition()));
                        session.setSession(KEY_AGAMA,arrayAgama.get(spinAgama.getSelectedItemPosition()));
                        session.setSession(KEY_INTEREST, finalDataInterest.toString());
                        session.setSession(KEY_STATUS_KAWIN,arrayKawin.get(spinStatusKawin.getSelectedItemPosition()));
                        session.setSession(KEY_GENDER,arrayJK.get(spinGender.getSelectedItemPosition()));
                        session.setSession(KEY_BUSINESS_TYPE,arrBusinessType.get(spinBusinessType.getSelectedItemPosition()));

                        session.setSession(KEY_NAME,edtNama.getText().toString());
                        session.setSession(KEY_NICKNAME,edtNickname.getText().toString());
                        session.setSession(KEY_WHATSAPP,edtWhatsApp.getText().toString());
                        session.setSession(KEY_EMAIL,edtEmail.getText().toString());
                        session.setSession(KEY_TGL_LAHIR,edtTglLahir.getText().toString());
                        session.setSession(KEY_ADDRESS,edtAddress.getText().toString());
                        session.setSession(KEY_FACEBOOK,edtFacebook.getText().toString());
                        session.setSession(KEY_INSTAGRAM,edtInstagram.getText().toString());
                        session.setSession(KEY_WEBSITE,edtWebsite.getText().toString());
                        session.setSession(KEY_HOBI,edtHobi.getText().toString());
                        session.setSession(KEY_BUSINESS_NAME,edtBusinessName.getText().toString());
                        session.setSession(KEY_PRODUCT,edtProduct.getText().toString());
                        session.setSession(KEY_BUKALAPAK,edtBukalapak.getText().toString());
                        session.setSession(KEY_TOKOPEDIA,edtTokopedia.getText().toString());
                        session.setSession(KEY_SHOPEE,edtShopee.getText().toString());
                        session.setSession(KEY_CITY,arrKotaId.get(spinKota.getSelectedItemPosition()));
                        session.setSession(KEY_PROVINCE,arrProvinsiId.get(spinProvinsi.getSelectedItemPosition()));
//                        session.setSession(map);

                        sharedPref.createSession(SharedPref.KEY_PROFILE_IS_COMPLETE,isCompleted);
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void loadBusinessType() {

        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_BUSINESS_TYPE)
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
                        arrBusinessType.clear();
                        for (int i=0;i<result.length();i++){
                            String businessTypeId = result.getJSONObject(i).getString("BusinessTypeId");
                            arrBusinessType.add(businessTypeId);
                        }
                        displayBusinessType();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void loadGreeting() {

        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_GREETING)
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
                        arrGreeting.clear();
                        for (int i=0;i<result.length();i++){
                            String greetingId = result.getJSONObject(i).getString("GreetingId");
                            arrGreeting.add(greetingId);
                        }
                        displayGreeting();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void loadProfession() {

        processProgressdialog = processProgressdialog + 1;
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_PROFESSION)
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
                        arrProfession.clear();
                        for (int i=0;i<result.length();i++){
                            String professionId = result.getJSONObject(i).getString("ProfessionId");
                            arrProfession.add(professionId);
                        }
                        displayProfession();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void loadProvince() {

        arrProvinsiId.clear();
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
                        int index_selected_province = 0;
                        String session_province = session.getSession(KEY_PROVINCE);
                        for (int i = 0; i<result.length(); i++){
                            String provinceId = result.getJSONObject(i).getString("ProvinceId");
                            String province = result.getJSONObject(i).getString("Province");
                            arrProvinsiId.add(provinceId);
                            arrProvinsi.add(province);
                            if (session_province.equals(provinceId)){
                                index_selected_province = i+1;

                            }
                        }
                        displayProvince();
                        spinProvinsi.setSelection(index_selected_province);
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void loadCity() {

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
            parameter.put("ProvinceId",Integer.parseInt(arrProvinsiId.get(spinProvinsi.getSelectedItemPosition())));
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
                            String session_city = session.getSession(KEY_CITY);
                            if (session_city.equals(cityId)){
                                index_selected_city = i+1;
                            }
                        }
                        displayCity();
                        spinKota.setSelection(index_selected_city);
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditProfileActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditProfileActivity.this)
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
                            new AlertDialog.Builder(EditProfileActivity.this)
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
    private void displayGreeting() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrGreeting);
        spinGreeting.setAdapter(adapter);
        getDataProfile();
    }
    private void displayGender() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrayJK);
        spinGender.setAdapter(adapter);
        getDataProfile();
    }
    private void displayStatusKawin() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrayKawin);
        spinStatusKawin.setAdapter(adapter);
        getDataProfile();
    }
    private void displayProfession() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrProfession);
        spinProfession.setAdapter(adapter);
        getDataProfile();
    }
    private void displayAgama() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrayAgama);
        spinAgama.setAdapter(adapter);
        getDataProfile();
    }

    private void displayProvince() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrProvinsi);
        spinProvinsi.setAdapter(adapter);
        getDataProfile();
        loadCity();
    }
    private void displayCity() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrKota);
        spinKota.setAdapter(adapter);
        getDataProfile();
    }
//    private void displayInterest() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrInterest);
//        spinInterest.setAdapter(adapter);
//        getDataProfile();
//    }
    private void displayBusinessType() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrBusinessType);
        spinBusinessType.setAdapter(adapter);
        getDataProfile();
    }

    private void getDataProfile() {
        edtNama.setText(userDetail.get(KEY_NAME));
        edtNickname.setText(userDetail.get(KEY_NICKNAME));
        edtWhatsApp.setText(userDetail.get(KEY_WHATSAPP));
        edtEmail.setText(userDetail.get(KEY_EMAIL));
        edtTglLahir.setText(userDetail.get(KEY_TGL_LAHIR));
        edtAddress.setText(userDetail.get(KEY_ADDRESS));
        edtFacebook.setText(userDetail.get(KEY_FACEBOOK));
        edtInstagram.setText(userDetail.get(KEY_INSTAGRAM));
        edtWebsite.setText(userDetail.get(KEY_WEBSITE));
        edtHobi.setText(userDetail.get(KEY_HOBI));
        edtBusinessName.setText(userDetail.get(KEY_BUSINESS_NAME));
        edtProduct.setText(userDetail.get(KEY_PRODUCT));
        edtTokopedia.setText(userDetail.get(KEY_TOKOPEDIA));
        edtBukalapak.setText(userDetail.get(KEY_BUKALAPAK));
        edtShopee.setText(userDetail.get(KEY_SHOPEE));

        try {
            if (userDetail.get(KEY_NICKNAME).equals("null")) edtNickname.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtNickname.setText("");
        }
        try {
            if (userDetail.get(KEY_WHATSAPP).equals("null")) edtWhatsApp.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtWhatsApp.setText("");
        }
        try {
            if (userDetail.get(KEY_EMAIL).equals("null")) edtEmail.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtEmail.setText("");
        }

        try {
            if (userDetail.get(KEY_TGL_LAHIR).equals("null")) edtTglLahir.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtTglLahir.setText("");
        }
        try {
            if (userDetail.get(KEY_ADDRESS).equals("null")) edtAddress.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtAddress.setText("");
        }

        try {
            if (userDetail.get(KEY_FACEBOOK).equals("null")) edtFacebook.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtFacebook.setText("");
        }
        try {
            if (userDetail.get(KEY_INSTAGRAM).equals("null")) edtInstagram.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtInstagram.setText("");
        }
        try {
            if (userDetail.get(KEY_WEBSITE).equals("null")) edtWebsite.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtWebsite.setText("");
        }
        try {
            if (userDetail.get(KEY_HOBI).equals("null")) edtHobi.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtHobi.setText("");
        }
        try {
            if (userDetail.get(KEY_BUSINESS_NAME).equals("null")) edtBusinessName.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtBusinessName.setText("");
        }
        try {
            if (userDetail.get(KEY_PRODUCT).equals("null")) edtProduct.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtProduct.setText("");
        }
        try {
            if (userDetail.get(KEY_TOKOPEDIA).equals("null")) edtTokopedia.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtTokopedia.setText("");
        }
        try {
            if (userDetail.get(KEY_BUKALAPAK).equals("null")) edtBukalapak.setText("");
        }catch (Exception e){
            e.printStackTrace();
            edtBukalapak.setText("");
        }
        try {
            if (userDetail.get(KEY_SHOPEE).equals("null")) edtShopee.setText("");

        }catch (Exception e){
            e.printStackTrace();
            edtShopee.setText("");
        }

        try {
            for(int i =0;i<arrGreeting.size();i++){
                if(arrGreeting.get(i).equals(userDetail.get(KEY_GREETING))){
                    spinGreeting.setSelection(i);
                }
            }
            for(int i =0;i<arrayKawin.size();i++){
                if(arrayKawin.get(i).equals(userDetail.get(KEY_STATUS_KAWIN))){
                    spinStatusKawin.setSelection(i);
                }
            }
            for(int i =0;i<arrBusinessType.size();i++){
                if(arrBusinessType.get(i).equals(userDetail.get(KEY_BUSINESS_TYPE))){
                    spinBusinessType.setSelection(i);
                }
            }
            for(int i =0;i<arrayAgama.size();i++){
                if(arrayAgama.get(i).equals(userDetail.get(KEY_AGAMA))){
                    spinAgama.setSelection(i);
                }
            }

            for(int i =0;i<arrayJK.size();i++){
                if(arrayJK.get(i).equals(userDetail.get(KEY_GENDER))){
                    spinGender.setSelection(i);
                }
            }
            for(int i =0;i<arrProfession.size();i++){
                if(arrProfession.get(i).equals(userDetail.get(KEY_PROFESSION))){
                    spinProfession.setSelection(i);
                }
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        listInterest.clear();
        excludeInterest = new JSONArray();
        try {
            JSONArray dataInterestId = new JSONArray(userDetail.get(KEY_INTEREST));
            for (int i = 0;i<dataInterestId.length();i++){
                listInterest.add(new ItemRecyclerTag(dataInterestId.getString(i),dataInterestId.getString(i)));
            }
            reloadExcludeInterest();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        interestAdapterSetup();
    }
    private void reloadExcludeInterest(){
        excludeInterest = new JSONArray();
        for (int i = 0;i<listInterest.size();i++){
            excludeInterest.put(listInterest.get(i).getId());
        }
    }
    private void interestAdapterSetup(){
        adapterTag = new RecyclerTagAdapter(listInterest, this,new RecyclerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i = 0; i< listInterest.size(); i++){
                    if (listInterest.get(i).getId().equals(item.getId())){
                        listInterest.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeInterest();
            }
        });
        recyclerViewInterest.setAdapter(adapterTag);
    }
}