package id.my.hubkontak;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.view.AdapterInterest;
import id.my.hubkontak.utils.view.ItemInterest;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class CariInterestActivity extends AppCompatActivity {

    private static final String TAG = CariInterestActivity.class.getSimpleName();
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private JSONArray excludeInterest;
    private SwipeRefreshLayout swipe_refresh;
    private EditText edtCari;
    private ListView listInterest;
    private List<ItemInterest> dataInterest = new ArrayList<>();
    private AdapterInterest interestAdapter;
    private Toolbar toolbar;
    private Button btnPilih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_interest);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        apiKey = userDetail.get(KEY_TOKEN);

        final String exclude = getIntent().getStringExtra("exclude");
        try {
            excludeInterest = new JSONArray(exclude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnPilih = (Button) findViewById(R.id.btnPilih);

        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        edtCari = (EditText) findViewById(R.id.edtCari);
        listInterest = (ListView) findViewById(R.id.listInterest);
//        listInterest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent();
//                intent.putExtra("id",dataInterest.get(i).getId());
//                intent.putExtra("title",dataInterest.get(i).getId());
//                setResult(RESULT_OK,intent);
//                finish();
//            }
//        });
        btnPilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ItemInterest> list = interestAdapter.arraylist;
                JSONArray interestChecklist = new JSONArray();

                for(int i = 0;i<list.size();i++){
                    if(list.get(i).isCheckbox()){
                        interestChecklist.put(list.get(i).getId());
                    }
                }
                if (interestChecklist.length()  == 0){
                    Toast.makeText(getApplicationContext(),"Belum ada jenis ketertarikan yang dipilih",Toast.LENGTH_SHORT);
                }else{
                    Intent intent = new Intent();
                    intent.putExtra("interestId",interestChecklist.toString());
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    interestAdapter.filter(edtCari.getText().toString().trim());
                    listInterest.invalidate();
                }catch (NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                loadInterest();

            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadInterest();
            }
        });
    }

    private void loadInterest() {
        swipe_refresh.setRefreshing(true);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_INTEREST)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        dataInterest.clear();
                        boolean is_exclude = false;
                        for (int i=0;i<result.length();i++){
                            String interestId = result.getJSONObject(i).getString("InterestId");
                            is_exclude = false;
                            for (int x = 0 ;x<excludeInterest.length();x++){
                                if (excludeInterest.get(x).equals(interestId)){
                                    is_exclude = true;
                                    break;
                                }
                            }
                            if (is_exclude == false){
                                dataInterest.add(new ItemInterest(interestId,interestId));
                            }
                        }
//                        displayInterest();
                    }else{
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT);
                    }
                    displayInterest();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(CariInterestActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(CariInterestActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(CariInterestActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(CariInterestActivity.this)
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

    private void displayInterest() {
        interestAdapter = new AdapterInterest(dataInterest,this);
        listInterest.setAdapter(interestAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}