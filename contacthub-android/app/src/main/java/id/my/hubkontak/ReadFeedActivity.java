package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.se.omapi.Session;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import id.my.hubkontak.models.ModelFeed;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.API.API_CLICK_FEED;
import static id.my.hubkontak.utils.API.API_LIST_FEED_PUBLIC;
import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.SharedPref.KEY_CLICK_ADS_MESSAGE;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class ReadFeedActivity extends AppCompatActivity {

    private static final String TAG = ReadFeedActivity.class.getSimpleName();
    private String feedId;
    private SessionManager sessionManager;
    private HashMap<String, String> userDetails;
    private String apiKey;
    private ProgressDialog progressDialog;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtCreatedAt;
    private TextView txtCreatedBy;
    private ImageView imFeed;
    private Button btnHubungi;
    private Toolbar toolbar;
    private String click_ads_message;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_feed);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sessionManager = new SessionManager(this);
        userDetails = sessionManager.getUserDetails();
        apiKey = userDetails.get(KEY_TOKEN);
        sharedPref = new SharedPref(this);
        click_ads_message = sharedPref.getSessionStr(KEY_CLICK_ADS_MESSAGE);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtCreatedAt = (TextView) findViewById(R.id.txtCreatedAt);
        txtCreatedBy = (TextView) findViewById(R.id.txtCreatedBy);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        imFeed = (ImageView) findViewById(R.id.imgFeed);
        btnHubungi = (Button) findViewById(R.id.btnHubungi);

        feedId = getIntent().getStringExtra("FeedId");

        loadFeed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFeed(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_LIST_FEED_PUBLIC)
                .buildUpon()
                .toString();

        JSONObject filter = new JSONObject();
        try {
            filter.put("FeedId",feedId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("filter",filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Log.e(TAG,response.toString());
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    final JSONArray result = response.getJSONObject("result").getJSONArray("data");
                    if (status.equals("OK")){
                        if(result.length() > 0){
                            for (int i=0;i<1;i++){
                                String feedId = result.getJSONObject(i).getString("FeedId");
                                String PhoneNo = result.getJSONObject(i).getString("PhoneNo");
                                String CreatedAt = result.getJSONObject(i).getString("CreatedAt");
                                String CreatedBy = result.getJSONObject(i).getString("CreatedBy");
                                String Description = result.getJSONObject(i).getString("Description");
                                String status_feed = result.getJSONObject(i).getString("Status");
                                String Title = result.getJSONObject(i).getString("Title");
                                String Category = result.getJSONObject(i).getString("Category");
                                String UpdatedAt = result.getJSONObject(i).optString("UpdatedAt");
                                String UpdatedBy = result.getJSONObject(i).optString("UpdtaedBy");
                                String Email = result.getJSONObject(i).getJSONObject("User").getString("Email");
                                String Nickname = result.getJSONObject(i).getJSONObject("User").getString("Nickname");
                                String imgFeed = result.getJSONObject(i).optString("Image");
                                String url = result.getJSONObject(i).optString("Url");

                                txtDescription.setText(Description);
                                txtTitle.setText(Title);
                                txtCreatedAt.setText(CreatedAt);
                                txtCreatedBy.setText(Nickname);
                                if (Description.length() > 200){
                                    txtDescription.setText(Description.substring(0,200));
                                }else{
                                    txtDescription.setText(Description);
                                }
                                if (imgFeed !=null){
                                    String imgUrl = BASE_URL_UPLOADS + imgFeed;

                                    Picasso.with(ReadFeedActivity.this).load(imgUrl).placeholder(R.drawable.ic_image).error(R.drawable.ic_image)
                                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imFeed);
                                }
                                btnHubungi.setEnabled(false);
                                if (PhoneNo != null && !PhoneNo.equals("") ){
                                    btnHubungi.setEnabled(true);
                                    btnHubungi.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            readFeed(feedId);
                                        }
                                    });
                                }

                            }
                        }
                    }else{
                        Toast.makeText(ReadFeedActivity.this,message,Toast.LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(ReadFeedActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(ReadFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(ReadFeedActivity.this,LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(ReadFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(ReadFeedActivity.this)
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK",null)
                            .show();
                }
                //alertCobaLagi(getString(errorResponse(error)));
            }
        }){
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-API-KEY", apiKey);
                return headers;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void readFeed(String feedId) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_CLICK_FEED +  "/" + feedId)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Log.e(TAG,response.toString());
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    if (status.equals("OK")){
                        final JSONObject result = response.optJSONObject("result");
                        String phoneNo = result.optString("PhoneNo");
                        if (phoneNo != null) {
                            String phoneNumber = phoneNo;
                            String text = click_ads_message.replace("[judul]","*" + txtTitle.getText().toString() + "*");// Replace with your message.
                            String toNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
                            if (TextUtils.isEmpty(text)){
                                text = "";
                            }
                            try {
                                if (toNumber.substring(0,2).equals("62") == false){
                                    toNumber = toNumber.replace("+","")
                                            .replace(" ","")
                                            .replace("-","")
                                            .replace("(","")
                                            .replace(")","");
                                    if (toNumber.substring(0,2).equals("08")){
                                        toNumber = "62" + toNumber.substring(1);
                                    }
                                    if (toNumber.substring(0).equals("8")){
                                        toNumber = "62" + toNumber;
                                    }
                                }
                                if (TextUtils.isEmpty(phoneNumber)){
                                    Toast.makeText(ReadFeedActivity.this, "No. WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setPackage("com.whatsapp");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+ URLEncoder.encode(text, "UTF-8") ));
                                    startActivity(intent);
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(ReadFeedActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ReadFeedActivity.this,message,Toast.LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(ReadFeedActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(ReadFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(ReadFeedActivity.this,LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(ReadFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(ReadFeedActivity.this)
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK",null)
                            .show();
                }
                //alertCobaLagi(getString(errorResponse(error)));
            }
        }){
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("X-API-KEY", apiKey);
                return headers;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);

    }
}