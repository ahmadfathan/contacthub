package id.my.hubkontak;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.DBContract.TEMPLATE_SHARE;
import static id.my.hubkontak.utils.SessionManager.KEY_MARKETING_CODE;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.currencyID;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class AffiliateActivity extends AppCompatActivity {

    private static final String TAG = AffiliateActivity.class.getSimpleName();
    private Toolbar toolbar;
    private View ftView;
    private SwipeRefreshLayout swipeRefresh;
    private MyHandler mHandler;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private ListView listTransaksi;
    private LinearLayout notfoundLayout;
    private TextView txtKomisiBulanIni;
    private boolean isLoading = false;
    private int currentPage = 1;
    private String stringHeader;
    private HistoryAffiliateAdapter historyAffiliateAdapter;
    private String fromDate,toDate;
    private TextView txtPenghasilanBelumCair;
    private TextView txtTotalKomisi,txtDownline;
    private TextView txtTotalPenghasilan;
    private TextView txtTotalPenghasilanBulanIni;
    private Button btnAjakTeman;
    private SharedPref sharedPref;
    private String marketingCode,url_track_playstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affiliate);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Affiliate");


        Date today = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);

        Date lastDayOfMonth = calendar.getTime();

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar c = Calendar.getInstance();   // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);

        fromDate = sdf.format(c.getTime());
        toDate = sdf.format(lastDayOfMonth);


        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.view_footer_loading,null);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        mHandler = new MyHandler();


        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        sharedPref = new SharedPref(this);
        apiKey = userDetail.get(KEY_TOKEN);
        btnAjakTeman = (Button) findViewById(R.id.btnAjakTeman);

        listTransaksi = (ListView) findViewById(R.id.listTransaksi);
        notfoundLayout = (LinearLayout) findViewById(R.id.notfound);
        txtTotalPenghasilan = (TextView) findViewById(R.id.txtTotalPenghasilan);
        txtKomisiBulanIni = (TextView) findViewById(R.id.txtKomisi);
        txtTotalPenghasilanBulanIni = (TextView) findViewById(R.id.txtPenghasilanBulanIni);
        txtPenghasilanBelumCair = (TextView) findViewById(R.id.txtPenghasilanBelumCair);
        txtTotalKomisi = (TextView) findViewById(R.id.txtTotalKomisi);
        txtDownline = (TextView) findViewById(R.id.txtTotalDownline);
        btnAjakTeman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    marketingCode = userDetail.get(KEY_MARKETING_CODE);

                    url_track_playstore = Uri.parse("https://play.google.com/store/apps/details?id=id.my.hubkontak")
                            .buildUpon()
                            .appendQueryParameter("referrer",marketingCode)
                            .toString();

                    String konten = TEMPLATE_SHARE;
                    if (!TextUtils.isEmpty(sharedPref.getSessionStr(SharedPref.KEY_TEMPLATE_SHARE)) ){
                        konten = sharedPref.getSessionStr(SharedPref.KEY_TEMPLATE_SHARE);
                    }
                    konten = konten.replace("[linkweb]",url_track_playstore);

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch(Exception e) {
                    Toast.makeText(AffiliateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        listTransaksi.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listTransaksi == null || listTransaksi.getChildCount() == 0) ?
                                0 : listTransaksi.getChildAt(0).getTop();
                swipeRefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if(view.getLastVisiblePosition()==totalItemCount-1 && listTransaksi.getCount()>=10 && isLoading == false){
                    isLoading = true;
                    int nextPage = currentPage + 1;
                    getHistoryAffiliate(nextPage);
                }
            }
        });

        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
                getHistoryAffiliate(1);
                getSummaryAffiliate();
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHistoryAffiliate(1);
                getSummaryAffiliate();
            }
        });

    }


    //    private
    private void getHistoryAffiliate(int page){
        swipeRefresh.setRefreshing(true);

        if (page > 1){
            mHandler.sendEmptyMessage(0);
        }else{
            stringHeader = "";
            historyAffiliateAdapter = new HistoryAffiliateAdapter(this);
            listTransaksi.setVisibility(View.GONE);
            notfoundLayout.setVisibility(View.VISIBLE);

        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_HISTORY_AFFILIATE)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("fromDate", fromDate);
            parameter.put("toDate", toDate);
            parameter.put("page", page);
            parameter.put("numberPage", 50);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                try {
                    String status = response.getString("status");
                    JSONArray result = response.getJSONObject("result").getJSONArray("data");
                    if (status.equals("OK")){

                        if (result.length()>0){
                            if(page==1){
                                listTransaksi.setVisibility(View.VISIBLE);
                                notfoundLayout.setVisibility(View.GONE);
                                listTransaksi.setAdapter(historyAffiliateAdapter);
                            }
                            currentPage = page;
                            for (int i = 0;i<result.length();i++){
                                JSONObject downline = result.getJSONObject(i).getJSONObject("Downline");
                                String affiliateId = result.getJSONObject(i).getString("AffiliateId");
                                String tag = result.getJSONObject(i).getString("Tag");
//                                String description = result.getJSONObject(i).getString("Description");
                                String tgl = result.getJSONObject(i).getString("CreatedAt");
                                String refNo = result.getJSONObject(i).getString("RefNo");
                                boolean isPaid = result.getJSONObject(i).optBoolean("Paid");
                                Double total = result.getJSONObject(i).optDouble("Commission");;
                                String statusTransaksi = null;
                                if (isPaid){
                                    statusTransaksi = "Paid";
                                }else{
                                    statusTransaksi = "Unpaid";
                                }
                                if (refNo.equals(null) || refNo.equals("null") || refNo == null){
                                    refNo = "";
                                }
                                String description = "";
                                String nickname_downline = downline.getString("Nickname");
                                if(tag.equals("register")){
                                    description = "Penambahan Saldo Credit  sebesar "+ currencyID(total) +" dari Pendaftaran User " + nickname_downline;
                                }else if(tag.equals("topup")) {
                                    description = "Penambahan Komisi sebesar "+ currencyID(total) +" dari Top Up User " + nickname_downline;
                                }
                                final ItemHistoriSaldo item = new ItemHistoriSaldo();
                                item.setId("# " + affiliateId);
                                item.setTitle(description);
                                item.setKeterangan("Ref : " + refNo);
                                item.setTanggal(tgl);
                                item.setJenis(tag);
                                item.setNo_ref("");
                                item.setStatus(statusTransaksi);
                                item.setTotal(currencyID(total));


                                DateFormat formatTgl = new SimpleDateFormat("yyyy-MM-dd");
                                DateFormat formatTglIndo = new SimpleDateFormat("dd MMM yyyy");
                                Date tanggal = formatTgl.parse(tgl);
                                Date now = new Date();

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(formatTgl.parse(formatTgl.format(now)));
                                cal.add(Calendar.DATE, -1 );

                                Date yesterday = cal.getTime();

                                if (!stringHeader.equals(formatTgl.format(tanggal))){
                                    if (formatTgl.format(tanggal).equals(formatTgl.format(now))){
                                        item.setHeader("Hari ini");
                                    }else if (formatTgl.format(yesterday).equals(formatTgl.format(tanggal))) {
                                        item.setHeader("Kemarin");
                                    }else{
                                        item.setHeader(formatTglIndo.format(tanggal));
                                    }
                                    stringHeader = formatTgl.format(tanggal);
                                    historyAffiliateAdapter.addSectionHeaderItem(item);
                                }else{
                                    item.setHeader(formatTglIndo.format(tanggal));
                                }
                                Log.i(TAG,"item:" + item.getId());
                                historyAffiliateAdapter.addItem(item);
                            }

                        }
                    }
                    if (page > 1){
                        if (status.equals("OK") && result.length() > 1){

                            mHandler.sendEmptyMessage(1);
                        }else{
                            mHandler.sendEmptyMessage(3);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (page > 1){
                        mHandler.sendEmptyMessage(3);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    if (page > 1){
                        mHandler.sendEmptyMessage(3);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (page > 1){
                    mHandler.sendEmptyMessage(3);
                }
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(AffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(AffiliateActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(AffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(AffiliateActivity.this)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    class HistoryAffiliateAdapter extends BaseAdapter {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;

        private List<ItemHistoriSaldo> mData = new ArrayList<>();
        private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

        private LayoutInflater mInflater;

        public HistoryAffiliateAdapter(Context context) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final ItemHistoriSaldo item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSectionHeaderItem(final ItemHistoriSaldo item) {
            mData.add(item);
            sectionHeader.add(mData.size() - 1);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public ItemHistoriSaldo getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int rowType = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.item_affiliate, null);
                        holder.txtId = (TextView) convertView.findViewById(R.id.itemId);
                        holder.txtTitle = (TextView) convertView.findViewById(R.id.itemTitle);
                        holder.txtKeterangan= (TextView) convertView.findViewById(R.id.itemKeterangan);
                        holder.txtStatus= (TextView) convertView.findViewById(R.id.itemStatus);
                        holder.txtTotal= (TextView) convertView.findViewById(R.id.itemTotal);
                        holder.txtTgl = (TextView) convertView.findViewById(R.id.itemTanggal);
                        holder.txtJam = (TextView) convertView.findViewById(R.id.itemJam);


                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.item_list_sec_header, null);
                        holder.txtHeader = (TextView) convertView.findViewById(R.id.itemHeader);


                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            switch (rowType) {
                case TYPE_ITEM:
//                    holder.txtId.setText(mData.get(position).getId());
                    holder.txtTitle.setText(mData.get(position).getTitle());
//                    holder.txtKeterangan.setText(mData.get(position).getKeterangan());
//                    holder.txtTotal.setText(mData.get(position).getTotal());
                    try {
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        DateFormat formatTgl = new SimpleDateFormat("dd MMM yyyy");
                        DateFormat formatJam = new SimpleDateFormat("HH:mm:ss");
                        Date d = format.parse(mData.get(position).getTanggal());

                        holder.txtTgl.setText(formatTgl.format(d));
                        holder.txtJam.setText(formatJam.format(d));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case TYPE_SEPARATOR:
                    holder.txtHeader.setText(mData.get(position).getHeader());
                    break;
            }

            return convertView;
        }

        public class ViewHolder {
            public TextView txtHeader,txtId,txtTitle,txtKeterangan,txtTotal,txtTgl,txtJam,txtStatus;
        }
    }
    class ItemHistoriSaldo{
        String header,id,title,keterangan,total,tanggal,no_ref,jenis,status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNo_ref() {
            return no_ref;
        }

        public void setNo_ref(String no_ref) {
            this.no_ref = no_ref;
        }

        public String getJenis() {
            return jenis;
        }

        public void setJenis(String jenis) {
            this.jenis = jenis;
        }

        public String getTanggal() {
            return tanggal;
        }

        public void setTanggal(String tanggal) {
            this.tanggal = tanggal;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getKeterangan() {
            return keterangan;
        }

        public void setKeterangan(String keterangan) {
            this.keterangan = keterangan;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }


    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    listTransaksi.addFooterView(ftView);
                    break;
                case 1:
                    listTransaksi.removeFooterView(ftView);
                    isLoading = false;
                    historyAffiliateAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    listTransaksi.removeFooterView(ftView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void getSummaryAffiliate(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_SUMMARY_AFFILIATE)
                .buildUpon()
                .toString();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("fromDate",fromDate);
            parameter.put("toDate",toDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        txtKomisiBulanIni.setText("0");
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");

                    if (status.equals("OK")){
                        JSONObject result = response.getJSONObject("result");
                        Log.e(TAG,result.toString());
                        Double totalDownline = result.optDouble("totalDownline",0);
                        Double totalKomisi = result.optDouble("totalKomisi",0);
                        Double totalKomisiBulanIni = result.optDouble("totalKomisiBulanIni",0);
                        Double totalPenghasilan = result.optDouble("totalPenghasilan",0);
                        Double totalPenghasilanBulanIni = result.optDouble("totalPenghasilanBulanIni",0);
                        Double totalPenghasilanBelumCair = result.optDouble("totalPenghasilanBelumCair",0);


                        NumberFormat formatter = new DecimalFormat("#,###");
                        String fromatTotalDownline = formatter.format(totalDownline);
                        String fromatTotalKomisi = formatter.format(totalKomisi);
                        String fromatTotalKomisiBulanIni = formatter.format(totalKomisiBulanIni);
                        String fromatTotalPenghasilan = formatter.format(totalPenghasilan);
                        String fromatTotalPenghasilanBulanIni = formatter.format(totalPenghasilanBulanIni);
                        String fromatTotalPenghasilanBelumCair = formatter.format(totalPenghasilanBelumCair);

                        txtDownline.setText(fromatTotalDownline);
                        txtTotalPenghasilan.setText(fromatTotalPenghasilan);
                        txtTotalPenghasilanBulanIni.setText(fromatTotalPenghasilanBulanIni);
                        txtPenghasilanBelumCair.setText(fromatTotalPenghasilanBelumCair);

                        txtTotalKomisi.setText(fromatTotalKomisi);
                        txtKomisiBulanIni.setText(fromatTotalKomisiBulanIni);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(AffiliateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            new AlertDialog.Builder(AffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(AffiliateActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(AffiliateActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(AffiliateActivity.this)
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