package id.my.hubkontak;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.my.hubkontak.models.ModelArticle;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;

import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.currencyID;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class DetailContactActivity extends AppCompatActivity {

    private String customerId;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private List<String[]> detailContact = new ArrayList<>();
    private ListView listInfo;
    private Toolbar toolbar;
    private AppCompatImageView imgCover;
    private ImageView imgProfile;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contact);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String greeting = getIntent().getStringExtra("Greeting");
        String customerName = getIntent().getStringExtra("Name");
        getSupportActionBar().setTitle(greeting + " " + customerName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        apiKey = userDetail.get(KEY_TOKEN);
        customerId = getIntent().getStringExtra("CustomerId");

        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgCover = (AppCompatImageView) findViewById(R.id.imgCover);
        listInfo = (ListView) findViewById(R.id.listInfo);
        imgProfile.setClipToOutline(true);
        loadDetailContact();
    }

    private void loadDetailContact(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_CONTACT_DETAIL)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("CustomerId", customerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.stop();
                finish();
            }
        });
        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                progressDialog.dismiss();
                try {
                    detailContact.clear();
                    String status = response.getString("status");
                    JSONObject result = response.getJSONObject("result");
                    if (status.equals("OK")){
                        String keys[] = new String[]{
                                "CustomerId","AccountId","Greeting","Name","Nickname","Address","WhatsApp","Email","BusinessName",
                                "BusinessTypeId","CreatedAt","DateOfBirth","Facebook",
                                "Gender","Hoby","Instagram","InterestId","Product","ProfessionId","RelationshipStatus",
                                "Religion","Website","Tokopedia","Bukalapak","Shopee","CityName"

                        };
                        String labels[] = new String[]{
                                null,null,"Sapaan","Nama","Nama Panggilan","Alamat","WhatsApp","Email","Nama Bisnis","Jenis Bisnis","Tanggal bergabung","Tanggal lahir","Facebook",
                                "Jenis Kelamin","Hobi","Instagram","Ketertarikan","Nama Produk","Profesi","Status",
                                "Agama","Website","Tokopedia","Bukalapak","Shopee","Kota"

                        };
                        for (int i = 0;i<keys.length;i++){
                            if (labels[i] == null){
                                continue;
                            }else if(keys[i].equals("Nickname") ||keys[i].equals("Email")){
                                detailContact.add(new String[]{labels[i],result.getJSONObject("User").getString(keys[i])});
                            }else{
                                detailContact.add(new String[]{labels[i],result.getString(keys[i])});
                            }
                        }
                        String cover = API.BASE_URL_UPLOADS + result.optString("CoverContact");
                        String foto = API.BASE_URL_UPLOADS + result.optString("Foto");

                        Picasso.with(getApplicationContext()).load(cover).placeholder(R.drawable.icon).error(R.drawable.icon)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgCover);

                        Picasso.with(getApplicationContext()).load(foto).placeholder(R.drawable.icon).error(R.drawable.icon)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgProfile);

                    }
                    displayInfo();
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(DetailContactActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
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
                            new AlertDialog.Builder(DetailContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(DetailContactActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(DetailContactActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(DetailContactActivity.this)
                            .setMessage(errorResponse(error))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
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

    private void displayInfo() {
        final InfoContactAdapter infoContactAdapter = new InfoContactAdapter(detailContact,this);
        listInfo.setAdapter(infoContactAdapter);
    }

    class InfoContactAdapter extends BaseAdapter {

        public List<String[]> listArticle;

        public Context context;
        ArrayList<String[]> arraylist;

        private static final int resource = R.layout.item_info_contact;

        public InfoContactAdapter(List<String[]> apps, Context context) {
            this.listArticle = apps;
            this.context = context;
            arraylist = new ArrayList<String[]>();
            arraylist.addAll(listArticle);

            this.notifyDataSetChanged();
        }

        public void addListItemToAdapter(List<String[]> list){
            this.listArticle.addAll(list);
            arraylist = new ArrayList<String[]>();
            arraylist.addAll(listArticle);
        }

        @Override
        public int getCount() {
            return listArticle.size();
        }

        @Override
        public String[] getItem(int position) {
            return listArticle.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(resource,parent,false);

            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtSubtitle = (TextView) view.findViewById(R.id.txtSubTitle);

            final String[] itemContact = getItem(position);
            txtTitle.setText(itemContact[0]);
            txtSubtitle.setText(itemContact[1]);

            return view;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}