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
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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

import id.my.hubkontak.models.ModelArticle;
import id.my.hubkontak.utils.SessionManager;

import static id.my.hubkontak.utils.API.API_LIST_ARTICLE;
import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.errorResponse;

public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = ArticleActivity.class.getSimpleName();
    private EditText edtCari;
    private RelativeLayout layoutError;
    private ListView listArticle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListArticleAdapter listArticleAdapter;
    private List<ModelArticle> dataArticle = new ArrayList<>();
    private Toolbar toolbar;
    private SessionManager session;
    private HashMap<String, String> userDetails;
    private String apiKey;
    private boolean isLoading = false;
    private View ftView;
    private HandlerList mHandler;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        session = new SessionManager(this);
        userDetails = session.getUserDetails();

        apiKey = userDetails.get(KEY_TOKEN);

        edtCari = (EditText) findViewById(R.id.edtCari);
        layoutError = (RelativeLayout) findViewById(R.id.layoutError);
        listArticle = (ListView) findViewById(R.id.listArticle);

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
        listArticle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG,dataArticle.get(i).getUrl());
                startActivity(new Intent(ArticleActivity.this, SinglePageArticleActivity.class)
                        .putExtra("title", dataArticle.get(i).getTitle())
                        .putExtra("url", dataArticle.get(i).getUrl())
                );

            }
        });
        listArticle.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listArticle == null || listArticle.getChildCount() == 0) ?
                                0 : listArticle.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if(view.getLastVisiblePosition()==totalItemCount-1 && listArticle.getCount()>=10 && isLoading == false){
                    isLoading = true;
                    int nextPage = currentPage + 1;
                    loadData(nextPage);
                }
            }
        });
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
        final String url = Uri.parse(API_LIST_ARTICLE)
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
        dataArticle.clear();
        isLoading = true;
        notfound(true);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
                                String articleId = result.getJSONObject(i).getString("ArticleId");
                                String CreatedAt = result.getJSONObject(i).getString("CreatedAt");
                                String CreatedBy = result.getJSONObject(i).getString("CreatedBy");
                                String Description = result.getJSONObject(i).getString("Description");
                                String Slug = result.getJSONObject(i).getString("Slug");
                                String Tag = result.getJSONObject(i).getString("Tag");
                                String Title = result.getJSONObject(i).getString("Title");
                                String UpdatedAt = result.getJSONObject(i).optString("UpdatedAt");
                                String UpdatedBy = result.getJSONObject(i).optString("UpdtaedBy");
                                String Email = result.getJSONObject(i).getJSONObject("User").getString("Email");
                                String Nickname = result.getJSONObject(i).getJSONObject("User").getString("Nickname");
                                String imgArticle = result.getJSONObject(i).optString("Image");
                                String url = result.getJSONObject(i).optString("Url");

                                String decode_html = Html.fromHtml(Html.fromHtml(Description).toString()).toString();
                                if (decode_html.length() > 100){
                                    decode_html = decode_html.substring(0,100);
                                }
                                ModelArticle modelArticle = new ModelArticle();
                                modelArticle.setArticleId(articleId);
                                modelArticle.setCreatedAt(CreatedAt);
                                modelArticle.setCreatedBy(CreatedBy);
                                modelArticle.setDescription(decode_html);
                                modelArticle.setTitle(Title);
                                modelArticle.setSlug(Slug);
                                modelArticle.setEmail(Email);
                                modelArticle.setImage(imgArticle);
                                modelArticle.setNickname(Nickname);
                                modelArticle.setSlug(Slug);
                                modelArticle.setUpdatedAt(UpdatedAt);
                                modelArticle.setUpdatedBy(UpdatedBy);
                                modelArticle.setUrl(url);
                                modelArticle.setTag(Tag);

                                dataArticle.add(modelArticle);
                            }
                            notfound(false);
                        }else{
                            notfound(true);
                        }
                    }else{
                        notfound(true);
                        Toast.makeText(ArticleActivity.this,message,Toast.LENGTH_SHORT);
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
                    Toast.makeText(ArticleActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
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
                            new AlertDialog.Builder(ArticleActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(ArticleActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(ArticleActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(ArticleActivity.this)
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
        }
        return super.onOptionsItemSelected(item);
    }
    public class HandlerList extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    listArticle.addFooterView(ftView);
                    break;
                case 1:
                    listArticleAdapter.addListItemToAdapter((ArrayList<ModelArticle>)msg.obj);
                    listArticle.removeFooterView(ftView);
                    isLoading = false;
                    break;
                case 3:
                    listArticle.removeFooterView(ftView);
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
            listArticle.setVisibility(View.GONE);
        }else{
            layoutError.setVisibility(View.GONE);
            listArticle.setVisibility(View.VISIBLE);
        }
    }

    private void displayList() {
        listArticleAdapter = new ListArticleAdapter(dataArticle,this);
        listArticle.setAdapter(listArticleAdapter);
    }

    class ListArticleAdapter extends BaseAdapter {

        public List<ModelArticle> listArticle;

        public Context context;
        ArrayList<ModelArticle> arraylist;

        private static final int resource = R.layout.item_article;

        public ListArticleAdapter(List<ModelArticle> apps, Context context) {
            this.listArticle = apps;
            this.context = context;
            arraylist = new ArrayList<ModelArticle>();
            arraylist.addAll(listArticle);

            this.notifyDataSetChanged();
        }

        public void addListItemToAdapter(List<ModelArticle> list){
            this.listArticle.addAll(list);
            arraylist = new ArrayList<ModelArticle>();
            arraylist.addAll(listArticle);
        }

        @Override
        public int getCount() {
            return listArticle.size();
        }

        @Override
        public ModelArticle getItem(int position) {
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
            TextView txtCreatedAt = (TextView) view.findViewById(R.id.txtCreatedAt);
            TextView txtCreatedBy = (TextView) view.findViewById(R.id.txtCreatedBy);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            ImageView imgArticle = (ImageView) view.findViewById(R.id.imgArticle);


            final ModelArticle itemContact = getItem(position);
            txtTitle.setText(itemContact.getTitle());
            txtCreatedAt.setText(itemContact.getCreatedAt());
            txtCreatedBy.setText(itemContact.getNickname());
            if (itemContact.getDescription().length() > 200){
                txtDescription.setText(itemContact.getDescription().substring(0,200));
            }else{
                txtDescription.setText(itemContact.getDescription());
            }
            if (itemContact.getImage()!=null){
                String imgUrl = BASE_URL_UPLOADS + itemContact.getImage();

                Picasso.with(context).load(imgUrl).placeholder(R.drawable.ic_image).error(R.drawable.ic_image)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgArticle);
            }

            return view;
        }
    }
}