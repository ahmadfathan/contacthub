package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class WithdrawAffiliateActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText edtTotalWithdraw,edtBankName,edtNoRek,edtNamaPemilikRekening;
    private Button btnRequestWithdraw;
    private int totalWithdraw = 0;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_affiliate);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        apiKey = userDetail.get(KEY_TOKEN);

        edtTotalWithdraw = (EditText) findViewById(R.id.edtTotalWithdraw);
        edtBankName = (EditText) findViewById(R.id.edtBank);
        edtNoRek  = (EditText) findViewById(R.id.edtNoRek);
        edtNamaPemilikRekening  = (EditText) findViewById(R.id.edtNamaPemilikRekening);
        btnRequestWithdraw = (Button) findViewById(R.id.btnWithdraw);

        btnRequestWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(edtBankName.getText())){
                    edtBankName.setError("Field ini tidak boleh kosong");
                    edtBankName.requestFocus();
                }else if(TextUtils.isEmpty(edtNoRek.getText())) {
                    edtNoRek.setError("Field ini tidak boleh kosong");
                    edtNoRek.requestFocus();
                }else if(TextUtils.isEmpty(edtNamaPemilikRekening.getText())) {
                    edtNamaPemilikRekening.setError("Field ini tidak boleh kosong");
                    edtNamaPemilikRekening.requestFocus();
                }else if(totalWithdraw <= 0){
                    Toast.makeText(WithdrawAffiliateActivity.this, "Tidak ada komisi yang bisa dicairkan", Toast.LENGTH_SHORT).show();
                }else{
                    requestWithdraw();
                }
            }
        });
        getAmountWithdraw();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAmountWithdraw(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_AMOUNT_WITHDRAW)
                .buildUpon()
                .toString();
        totalWithdraw = 0;
        edtTotalWithdraw.setText("0");
        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");

                    if (status.equals("OK")){
                        JSONObject result = response.getJSONObject("result");
                        Double totalAmount = result.optDouble("totalAmount",0);

                        totalWithdraw = (int) Math.round(totalAmount);

                        NumberFormat formatter = new DecimalFormat("#,###");
                        String formattedNumber = formatter.format(totalWithdraw);
                        edtTotalWithdraw.setText(formattedNumber);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(WithdrawAffiliateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(WithdrawAffiliateActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK",null)
                            .show();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("X-API-KEY",apiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    private void requestWithdraw() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_WITHDRAW_REQUEST)
                .buildUpon()
                .toString();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("WithdrawBank",edtBankName.getText().toString());
            parameter.put("WithdrawNoRek",edtNoRek.getText().toString());
            parameter.put("WithdrawPemilikRek",edtNamaPemilikRekening.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        Toast.makeText(WithdrawAffiliateActivity.this, "Request Withdraw berhasil, data sedang diproses oleh admin", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        Toast.makeText(WithdrawAffiliateActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(WithdrawAffiliateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(WithdrawAffiliateActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(WithdrawAffiliateActivity.this)
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK",null)
                            .show();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("X-API-KEY",apiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
}