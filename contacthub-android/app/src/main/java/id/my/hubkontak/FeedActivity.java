package id.my.hubkontak;

import androidx.annotation.Nullable;
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
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.my.hubkontak.models.ModelFeed;
import id.my.hubkontak.utils.SessionManager;

import static id.my.hubkontak.utils.API.API_LIST_FEED;
import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = FeedActivity.class.getSimpleName();
    private static final int REQUEST_FEED = 100;
    private Toolbar toolbar;
    private SessionManager session;
    private HashMap<String, String> userDetails;
    private String apiKey;
    private EditText edtCari;
    private RelativeLayout layoutError;
    private ListView listFeed;
    private View ftView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;
    private int currentPage = 1;
    private List<ModelFeed> dataFeed = new ArrayList<>();
    private HandlerList mHandler;
    private ListFeedAdapter listFeedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        session = new SessionManager(this);
        userDetails = session.getUserDetails();

        apiKey = userDetails.get(KEY_TOKEN);

        edtCari = (EditText) findViewById(R.id.edtCari);
        layoutError = (RelativeLayout) findViewById(R.id.layoutError);
        listFeed = (ListView) findViewById(R.id.listFeed);


        mHandler = new HandlerList();
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.view_footer_loading,null);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        edtCari.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 600; // Milliseconds
            Handler m_handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (m_handler != null) {
                    m_handler.removeCallbacksAndMessages(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                m_handler = new Handler();
                m_handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(1);
                    }
                }, DELAY);
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadData(1);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(1);
            }
        });
        listFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, dataFeed.get(i).getUrl());
                startActivityForResult(new Intent(FeedActivity.this,EditFeedActivity.class)
                    .putExtra("FeedId",dataFeed.get(i).getFeedId())
                    .putExtra("Title",dataFeed.get(i).getTitle())
                    .putExtra("Description",dataFeed.get(i).getDescription())
                    .putExtra("Category",dataFeed.get(i).getCategory())
                    .putExtra("Image",dataFeed.get(i).getImage())
                    .putExtra("PhoneNo",dataFeed.get(i).getPhoneNo())
                    .putExtra("Reason",dataFeed.get(i).getReason())
                    .putExtra("Status",dataFeed.get(i).getStatus()),REQUEST_FEED);
            }
        });
        listFeed.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listFeed == null || listFeed.getChildCount() == 0) ?
                                0 : listFeed.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if(view.getLastVisiblePosition()==totalItemCount-1 && listFeed.getCount()>=10 && isLoading == false){
                    isLoading = true;
                    int nextPage = currentPage + 1;
                    loadData(nextPage);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FEED){
            if (resultCode == RESULT_OK){
                loadData(1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed,menu);
        menu.findItem(R.id.actDelete).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadData(int page){
        currentPage = 1;
        swipeRefreshLayout.setRefreshing(true);
        if (page > 1){
            mHandler.sendEmptyMessage(0);
        }else{
            displayList();
            notfound(true);
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API_LIST_FEED)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("page",page);
            parameter.put("numberPage",50);
            if (!TextUtils.isEmpty(edtCari.getText())){
                parameter.put("search",edtCari.getText().toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataFeed.clear();
        isLoading = true;
        notfound(true);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,response.toString());
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    final JSONArray result = response.getJSONObject("result").getJSONArray("data");
                    if (status.equals("OK")){
                        if(result.length() > 0){
                            currentPage = page;
                            for (int i=0;i<result.length();i++){
                                Log.e(TAG,result.getJSONObject(i).toString());
                                String feedId = result.getJSONObject(i).getString("FeedId");
                                String CreatedAt = result.getJSONObject(i).getString("CreatedAt");
                                String CreatedBy = result.getJSONObject(i).getString("CreatedBy");
                                String Description = result.getJSONObject(i).getString("Description");
                                String status_feed = result.getJSONObject(i).getString("Status");
                                String Title = result.getJSONObject(i).getString("Title");
                                String PhoneNo = result.getJSONObject(i).getString("PhoneNo");
                                String reason = result.getJSONObject(i).getString("Reason");
                                String Category = result.getJSONObject(i).getString("Category");
                                String UpdatedAt = result.getJSONObject(i).optString("UpdatedAt");
                                String UpdatedBy = result.getJSONObject(i).optString("UpdtaedBy");
                                String Email = result.getJSONObject(i).getJSONObject("User").getString("Email");
                                String Nickname = result.getJSONObject(i).getJSONObject("User").getString("Nickname");
                                String imgFeed = result.getJSONObject(i).optString("Image");
                                String url = result.getJSONObject(i).optString("Url");

                                String decode_html = Html.fromHtml(Html.fromHtml(Description).toString()).toString();
                                if (decode_html.length() > 100){
                                    decode_html = decode_html.substring(0,100);
                                }
                                ModelFeed modelFeed = new ModelFeed();
                                modelFeed.setFeedId(feedId);
                                modelFeed.setCreatedAt(CreatedAt);
                                modelFeed.setCreatedBy(CreatedBy);
                                modelFeed.setDescription(decode_html);
                                modelFeed.setTitle(Title);
                                modelFeed.setPhoneNo(PhoneNo);
                                modelFeed.setEmail(Email);
                                modelFeed.setImage(imgFeed);
                                modelFeed.setNickname(Nickname);
                                modelFeed.setUpdatedAt(UpdatedAt);
                                modelFeed.setUpdatedBy(UpdatedBy);
                                modelFeed.setUrl(url);
                                modelFeed.setCategory(Category);
                                modelFeed.setStatus(status_feed);
                                modelFeed.setReason(reason);

                                dataFeed.add(modelFeed);
                            }
                            notfound(false);
                        }else{
                            notfound(true);
                        }
                    }else{
                        notfound(true);
                        Toast.makeText(FeedActivity.this,message,Toast.LENGTH_SHORT);
                    }

                    if (page > 1){
                        if (status.equals("OK") && result.length() > 1){
                            mHandler.sendEmptyMessage(1);
                        }else{
                            mHandler.sendEmptyMessage(3);
                        }
                    }else{
                        displayList();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(FeedActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    notfound(true);
                    if (page > 1){
                        mHandler.sendEmptyMessage(3);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (page > 1){
                    mHandler.sendEmptyMessage(3);
                }
                swipeRefreshLayout.setRefreshing(false);
                Log.i(TAG,"Volley Error : " + errorResponse(error));
                notfound(true);
                isLoading = false;

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(FeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(FeedActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(FeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(FeedActivity.this)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId() == R.id.actAdd){
            startActivityForResult(new Intent(this,EditFeedActivity.class),REQUEST_FEED);
        }
        return super.onOptionsItemSelected(item);
    }
    public class HandlerList extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    listFeed.addFooterView(ftView);
                    break;
                case 1:
                    listFeedAdapter.addListItemToAdapter((ArrayList<ModelFeed>)msg.obj);
                    listFeed.removeFooterView(ftView);
                    isLoading = false;
                    break;
                case 3:
                    listFeed.removeFooterView(ftView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void notfound(boolean isNotFound){
        if (isNotFound){
            layoutError.setVisibility(View.VISIBLE);
            listFeed.setVisibility(View.GONE);
        }else{
            layoutError.setVisibility(View.GONE);
            listFeed.setVisibility(View.VISIBLE);
        }
    }

    private void displayList() {
        listFeedAdapter = new ListFeedAdapter(dataFeed,this);
        listFeed.setAdapter(listFeedAdapter);
    }

    class ListFeedAdapter extends BaseAdapter {

        public List<ModelFeed> listFeed;

        public Context context;
        ArrayList<ModelFeed> arraylist;

        private static final int resource = R.layout.item_feed;

        public ListFeedAdapter(List<ModelFeed> apps, Context context) {
            this.listFeed = apps;
            this.context = context;
            arraylist = new ArrayList<ModelFeed>();
            arraylist.addAll(listFeed);

            this.notifyDataSetChanged();
        }

        public void addListItemToAdapter(List<ModelFeed> list){
            this.listFeed.addAll(list);
            arraylist = new ArrayList<ModelFeed>();
            arraylist.addAll(listFeed);
        }

        @Override
        public int getCount() {
            return listFeed.size();
        }

        @Override
        public ModelFeed getItem(int position) {
            return listFeed.get(position);
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
            TextView txtCreatedAt = (TextView) view.findViewById(R.id.txtCreatedAt);
            TextView txtCreatedBy = (TextView) view.findViewById(R.id.txtCreatedBy);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            TextView txtStatus = (TextView) view.findViewById(R.id.txtStatus);
            ImageView imgFeed = (ImageView) view.findViewById(R.id.imgFeed);


            final ModelFeed itemContact = getItem(position);
            txtTitle.setText(itemContact.getTitle());
            txtCreatedAt.setText(itemContact.getCreatedAt());
            txtCreatedBy.setText(itemContact.getNickname());
            txtStatus.setText(itemContact.getStatus());
            if (itemContact.getStatus().equals("publish")){
                txtStatus.setTextColor(getResources().getColor(R.color.md_green_600));
            }else{
                txtStatus.setTextColor(getResources().getColor(R.color.md_red_600));
            }
            if (itemContact.getDescription().length() > 200){
                txtDescription.setText(itemContact.getDescription().substring(0,200));
            }else{
                txtDescription.setText(itemContact.getDescription());
            }
            if (itemContact.getImage()!=null){
                String imgUrl = BASE_URL_UPLOADS + itemContact.getImage();

                Picasso.with(context).load(imgUrl).placeholder(R.drawable.ic_image).error(R.drawable.ic_image)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgFeed);
            }

            return view;
        }
    }
}