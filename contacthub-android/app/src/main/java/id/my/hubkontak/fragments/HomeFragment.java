package id.my.hubkontak.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.TreeSet;

import id.my.hubkontak.AffiliateActivity;
import id.my.hubkontak.ArticleActivity;
import id.my.hubkontak.CreditActivity;
import id.my.hubkontak.EditProfileActivity;
import id.my.hubkontak.FeedActivity;
import id.my.hubkontak.HistoryWithdrawActivity;
import id.my.hubkontak.LoginActivity;
import id.my.hubkontak.MainActivity;
import id.my.hubkontak.R;
import id.my.hubkontak.ReadFeedActivity;
import id.my.hubkontak.SettingContactActivity;
import id.my.hubkontak.SinglePageArticleActivity;
import id.my.hubkontak.models.ModelArticle;
import id.my.hubkontak.models.ModelFeed;
import id.my.hubkontak.utils.AMQSubscriber;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;
import id.my.hubkontak.utils.view.Expandableheightlistview;
import id.my.hubkontak.utils.view.LinearLayoutSpacing;
import id.my.hubkontak.utils.view.RecyclerViewItemClick;
import id.my.hubkontak.utils.view.Scrollingpagerindicator;

import static android.app.Activity.RESULT_OK;
import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static id.my.hubkontak.utils.API.AMQ_HOST;
import static id.my.hubkontak.utils.API.API_LIST_ARTICLE;
import static id.my.hubkontak.utils.API.API_LIST_FEED;
import static id.my.hubkontak.utils.API.API_LIST_FEED_PUBLIC;
import static id.my.hubkontak.utils.API.API_SINGKRON_LOCAL_CONTACT;
import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_ALLOW_SHARE_PROFILE;
import static id.my.hubkontak.utils.SessionManager.KEY_EMAIL;
import static id.my.hubkontak.utils.SessionManager.KEY_FOTO;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_CONTACT_FRIEND;
import static id.my.hubkontak.utils.SessionManager.KEY_LIMIT_SAVE_MY_CONTACT;
import static id.my.hubkontak.utils.SessionManager.KEY_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_CONTACT_FRIEND_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_BY;
import static id.my.hubkontak.utils.SessionManager.KEY_SAVE_MY_CONTACT_INTEREST;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;
import static id.my.hubkontak.utils.SharedPref.KEY_CS_TOPUP;
import static id.my.hubkontak.utils.SharedPref.KEY_MESSAGE_TOPUP;
import static id.my.hubkontak.utils.SharedPref.KEY_PROFILE_IS_COMPLETE;
import static id.my.hubkontak.utils.Utils.currencyID;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.isPackageInstalled;

public class HomeFragment extends Fragment implements ViewTreeObserver.OnScrollChangedListener, View.OnFocusChangeListener {


    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final int REQUEST_UPDATE_PROFILE = 100;
    private List<ItemLayanan> layanan = new ArrayList<>();
    private SessionManager sessionManager;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private ImageView imgRefresh,imgAkun;
    private ShimmerFrameLayout shimmerArticle;
    private TextView txtNama,txtSaldo;
    private Button btnTopup;
    private RotateAnimation animation;
    private boolean balanceLoaded = true;
    private String stringHeader = "";
    private Expandableheightlistview listFeed;
    private HistoriSaldoAdapter historiSaldoAdapter;
    private NestedScrollView mScrollView;
    private SwipeRefreshLayout swipe_refresh;
    private List<ModelArticle> dataArticle = new ArrayList<>();
    private RecyclerView gridArticle;
    private LinearLayout notfoundLayoutArticle;
    private ArticleAdapter articleAdapter;
    private SharedPref sharePref;
    private TextView txtNotifProfile;
    private HashMap<String, String> settingContact;
    private Switch switchAutoSave;
    private String urlFoto;
    private boolean isRefresh = true;
    private boolean isLoadingCredit = false;
    private boolean isLoadingArticle =false;
    private int currentPage;
    private List<ModelFeed> dataFeed = new ArrayList<>();
    private boolean isLoading;
    private HandlerList mHandler;
    private View ftView;
    private ListFeedAdapter listFeedAdapter;
    private ShimmerFrameLayout shimmerBanner;
    private Scrollingpagerindicator dotsIndicator;
    private RecyclerView gridSlider;
    private List<ItemSlider> slider = new ArrayList<>();
    private int scrollPosition;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            if (message.equals("refresh")){
                load();
            }
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        sessionManager = new SessionManager(getContext());
        sharePref = new SharedPref(getContext());
        userDetail = sessionManager.getUserDetails();

        apiKey = userDetail.get(KEY_TOKEN);

        mHandler = new HandlerList();
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.view_footer_loading,null);

        shimmerBanner = (ShimmerFrameLayout) view.findViewById(R.id.shimmerBanner);
        dotsIndicator = (Scrollingpagerindicator) view.findViewById(R.id.dotsIndicator);
        gridSlider = (RecyclerView) view.findViewById(R.id.gridSlider);

        RecyclerView gridLayanan = view.findViewById(R.id.gridLayanan);
        mScrollView = (NestedScrollView) view.findViewById(R.id.mScrollView);
        imgRefresh = (ImageView) view.findViewById(R.id.imgRefresh);
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        shimmerArticle = (ShimmerFrameLayout) view.findViewById(R.id.shimmerArticle);
        imgAkun = (ImageView) view.findViewById(R.id.imgAkun);
        txtNotifProfile = (TextView) view.findViewById(R.id.txtNotifProfile);
        txtNama = (TextView) view.findViewById(R.id.txtNama);
        txtSaldo = (TextView) view.findViewById(R.id.txtSaldo);
        switchAutoSave = (Switch) view.findViewById(R.id.switchAutoSave);

        listFeed = (Expandableheightlistview) view.findViewById(R.id.listFeed);
        gridArticle = (RecyclerView) view.findViewById(R.id.gridArticle);
        notfoundLayoutArticle = (LinearLayout) view.findViewById(R.id.notfound_article);

        txtNama.setText(userDetail.get(KEY_NAME));
        btnTopup = (Button) view.findViewById(R.id.btnTopup);

        imgAkun.setClipToOutline(true);
        txtNotifProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getContext(), EditProfileActivity.class),REQUEST_UPDATE_PROFILE);
            }
        });
        btnTopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageManager pm = getContext().getPackageManager();
                boolean isInstalled1 = isPackageInstalled("com.whatsapp", pm);
                boolean isInstalled2 = isPackageInstalled("com.whatsapp.w4b", pm);
                String package_name = "";
                if (isInstalled1){
                    package_name = "com.whatsapp";
                }else if (isInstalled2){
                    package_name = "com.whatsapp.w4b";
                }else{
                    Toast.makeText(getContext(), "Aplikasi WhatsApp belum diinstall", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = userDetail.get(KEY_EMAIL);

                String phoneNumber = sharePref.getSessionStr(KEY_CS_TOPUP);
                String text = sharePref.getSessionStr(KEY_MESSAGE_TOPUP);// Replace with your message.
                String toNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
                text = text.replace("[email]",email);
                if (TextUtils.isEmpty(text)){
                    text = "";
                }
                try {
                    if (TextUtils.isEmpty(phoneNumber)){
                        Toast.makeText(getContext(), "Contact CS tidak tersedia, sialahkan hubungi Admin", Toast.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage(package_name);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+ URLEncoder.encode(text, "UTF-8") ));
                        startActivity(intent);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBalance();
            }
        });
        animation = (RotateAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (balanceLoaded == false) {
                    imgRefresh.startAnimation(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        layanan.add(new ItemLayanan("Affiliate", R.drawable.affiliate));
        layanan.add(new ItemLayanan("Penghasilan", R.drawable.withdraw));
        layanan.add(new ItemLayanan("Credit", R.drawable.credit));
        layanan.add(new ItemLayanan("Article", R.drawable.article));
        layanan.add(new ItemLayanan("Ads Feed", R.drawable.article));
        layanan.add(new ItemLayanan("Singkron Kontak", R.drawable.article));

        gridArticle.addOnItemTouchListener(new RecyclerViewItemClick(getContext(), gridArticle, new RecyclerViewItemClick.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                startActivity(new Intent(getActivity(), SinglePageArticleActivity.class)
                        .putExtra("title", dataArticle.get(position).getTitle())
                        .putExtra("url", dataArticle.get(position).getUrl())
                );
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        listFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getContext(), ReadFeedActivity.class).putExtra("FeedId",dataFeed.get(i).getFeedId()));
            }
        });
        LayananAdapter layananAdapter = new LayananAdapter(layanan, getContext());

        gridLayanan.setLayoutManager(new GridLayoutManager(getContext(),4, LinearLayoutManager.VERTICAL, false));
        gridLayanan.setAdapter(layananAdapter);
        gridLayanan.addOnItemTouchListener(new RecyclerViewItemClick(getContext(), gridLayanan, new RecyclerViewItemClick.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (layanan.get(position).nama.equals("Credit")) {
                    startActivity(new Intent(getContext(), CreditActivity.class));
                }else if (layanan.get(position).nama.equals("Penghasilan")) {
                    startActivity(new Intent(getContext(), HistoryWithdrawActivity.class));
                }
                else if (layanan.get(position).nama.equals("Affiliate")) {
                    startActivity(new Intent(getContext(), AffiliateActivity.class));
                }
                else if (layanan.get(position).nama.equals("Article")) {
                    startActivity(new Intent(getContext(), ArticleActivity.class));
                }
                else if (layanan.get(position).nama.equals("Ads Feed")) {
                    startActivity(new Intent(getContext(), FeedActivity.class));
                }
                else if (layanan.get(position).nama.equals("Singkron Kontak")) {
                    singkronkan_save_kontak();
                }
                else {
                    Snackbar.make(view, "Fitur masih dalam proses pengembangan", Snackbar.LENGTH_SHORT).show();
                }
//                Snackbar.make(view, "Fitur masih dalam proses pengembangan", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
                loadSlider();
            }
        });
        switchAutoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                doSimpanEnableAutoSave();
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                load();
            }
        });
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("FragmentHome"));

        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (mScrollView.getChildAt(0).getBottom()
                        <= (mScrollView.getHeight() + mScrollView.getScrollY())) {
                    //scroll view is at bottom
//                    getFeed(currentPage+1);
                }
            }
        });
        intializeSlider();
        return view;
    }

    private void intializeSlider() {
        shimmerBanner.startShimmerAnimation();
        gridSlider.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        final LinearLayoutManager layoutManager = (LinearLayoutManager)gridSlider.getLayoutManager();
        gridSlider.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem = Objects.requireNonNull(layoutManager).findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount() - 1;
                scrollPosition = layoutManager.findFirstVisibleItemPosition() + 1;

                if (lastItem == totalItemCount) {
                    scrollPosition = 0;
                }
            }
        });
        gridSlider.addItemDecoration(new LinearLayoutSpacing(Objects.requireNonNull(getContext()), R.dimen.sliderSpacing));
        loadSlider();
    }

    private void visibleBanner(boolean visible){
        if (visible){
            shimmerBanner.setVisibility(View.GONE);
            gridSlider.setVisibility(View.VISIBLE);
            dotsIndicator.setVisibility(View.VISIBLE);
        }else{
            shimmerBanner.setVisibility(View.VISIBLE);
            gridSlider.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
        }
    }
    private void loadSlider() {
        visibleBanner(false);

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_BANNER)
                .buildUpon()
                .toString();


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    slider.clear();
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    final JSONArray result = response.getJSONObject("result").getJSONArray("data");
                    if (status.equals("OK")){
                        if(result.length() > 0){
                            for (int i=0;i<result.length();i++){
                                slider.add(new ItemSlider(BASE_URL_UPLOADS + result.getJSONObject(i).getString("Image"),result.getJSONObject(i).getString("Link")));
                            }
                            gridSlider.setAdapter(new SliderAdapter(slider, getContext()));
                            visibleBanner(true);
                            LinearSnapHelper snapHelper = new LinearSnapHelper() {
                                @Override
                                public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                                    View centerView = findSnapView(layoutManager);
                                    if (centerView == null)
                                        return RecyclerView.NO_POSITION;

                                    int position = layoutManager.getPosition(centerView);
                                    int targetPosition = -1;
                                    if (layoutManager.canScrollHorizontally()) {
                                        if (velocityX < 0) {
                                            targetPosition = position - 1;
                                        } else {
                                            targetPosition = position + 1;
                                        }
                                    }

                                    if (layoutManager.canScrollVertically()) {
                                        if (velocityY < 0) {
                                            targetPosition = position - 1;
                                        } else {
                                            targetPosition = position + 1;
                                        }
                                    }

                                    final int firstItem = 0;
                                    final int lastItem = layoutManager.getItemCount() - 1;
                                    targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));
                                    return targetPosition;
                                }
                            };
                            if (gridSlider.getOnFlingListener() == null){
                                snapHelper.attachToRecyclerView(gridSlider);
                            }
                            dotsIndicator.attachToRecyclerView(gridSlider);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String json;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(getContext(),LoginActivity.class));
                                            getActivity().finish();
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
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(getContext())
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
                headers.put("X-API-KEY",apiKey);
                return headers;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private class AmqTask extends AsyncTask<String, String, String> {

        private String resp;

        private AMQSubscriber amq = new AMQSubscriber();
        @Override
        protected String doInBackground(String... params) {
            publishProgress("Consuming..."); // Calls onProgressUpdate()
            try {
                if (sessionManager.isLoggedIn()){
                    String exchange = "guestapk";
                    amq.start(getContext(),AMQ_HOST,exchange,new String[]{"save_contact_" + userDetail.get(KEY_USER_ID),"share_contact_" + userDetail.get(KEY_USER_ID)});
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
                resp = e.getMessage();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
    private void singkronkan_save_kontak() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading ..");
        progressDialog.show();
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API_SINGKRON_LOCAL_CONTACT)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Log.i(TAG,response.toString());
                AmqTask runner = new AmqTask();
                runner.execute();
                new AlertDialog.Builder(getContext())
                        .setMessage("Proses singkronisasi kontak sedang berjalan")
                        .setPositiveButton("OK",null)
                        .show();;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-API-KEY",apiKey);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            load();
        }
    }

    private void load() {
        getFeed(1);
//        getHistoryCredit();
        getBalance();
        loadArticle();
        displayProfileIsComplete();
        checkStatusAutoSave();
        loadFoto();
        swipe_refresh.setRefreshing(false);
        isRefresh = false;
    }

    private void getFeed(int page){
        Log.i(TAG,apiKey);
        currentPage = 1;
        if (page > 1){
            mHandler.sendEmptyMessage(0);
        }else{
            displayListFeed();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API_LIST_FEED_PUBLIC)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("page",page);
            parameter.put("numberPage",50);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataFeed.clear();
        isLoading = true;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,response.toString());
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
                                modelFeed.setEmail(Email);
                                modelFeed.setImage(imgFeed);
                                modelFeed.setNickname(Nickname);
                                modelFeed.setUpdatedAt(UpdatedAt);
                                modelFeed.setUpdatedBy(UpdatedBy);
                                modelFeed.setUrl(url);
                                modelFeed.setCategory(Category);
                                modelFeed.setStatus(status_feed);

                                dataFeed.add(modelFeed);
                            }
                        }
                    }else{
                        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT);
                    }

                    if (page > 1){
                        if (status.equals("OK") && result.length() > 1){
                            mHandler.sendEmptyMessage(1);
                        }else{
                            mHandler.sendEmptyMessage(3);
                        }
                    }else{
                        displayListFeed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT);
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
                Log.i(TAG,"Volley Error : " + errorResponse(error));
                isLoading = false;

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(getContext(),LoginActivity.class));
                                            getActivity().finish();
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
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(getContext())
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


    private void displayListFeed() {
        listFeedAdapter = new ListFeedAdapter(dataFeed,getContext());
        listFeed.setAdapter(listFeedAdapter);
        listFeed.setExpanded(true);
    }
    private void loadFoto(){
        urlFoto = API.BASE_URL_UPLOADS + sessionManager.getSession(KEY_FOTO);
        Picasso.with(getContext()).load(urlFoto).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgAkun);
    }
    private void checkStatusAutoSave(){
        try {
            settingContact = sessionManager.getSettingContact();
            if (settingContact.get(KEY_ALLOW_SHARE_PROFILE).equals("true")){
                switchAutoSave.setChecked(true);
            }else{
                switchAutoSave.setChecked(false);
            }
        }catch (NullPointerException e){

        }catch (Exception e){

        }
    }
    private void doSimpanEnableAutoSave() {

        boolean allow = false;
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_UPDATE_PROFILE)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            if(switchAutoSave.isChecked()){
                allow = true;
            }
            parameter.put("AllowedShareProfile", allow);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG,apiKey);
        Log.e(TAG,parameter.toString());
        boolean finalAllow = allow;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        HashMap<String,String> map = new HashMap<>();
                        sessionManager.setSession(KEY_ALLOW_SHARE_PROFILE, String.valueOf(finalAllow));
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(getContext())
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                    }
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

                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(getContext())
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
    private void displayProfileIsComplete(){
        if (sharePref.getSessionBool(KEY_PROFILE_IS_COMPLETE) == true){
            txtNotifProfile.setVisibility(GONE);
        }else{
            txtNotifProfile.setVisibility(VISIBLE);
        }
    }
    private void displayArticle(){
        articleAdapter = new ArticleAdapter(dataArticle,getContext());
        gridArticle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        gridArticle.setAdapter(articleAdapter);

    }
    @Override
    public void onStart() {
        super.onStart();
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mScrollView.getViewTreeObserver().removeOnScrollChangedListener(this);
    }
    private void articleFound(boolean visible){
        if (visible){
            gridArticle.setVisibility(VISIBLE);
            notfoundLayoutArticle.setVisibility(GONE);
        }else{
            notfoundLayoutArticle.setVisibility(VISIBLE);
            gridArticle.setVisibility(GONE);
        }
    }
    //    private
    private void loadArticle(){
        if (isLoadingArticle){
            return;
        }
        isLoadingArticle= true;
        dataArticle.clear();
        shimmerArticle.setVisibility(VISIBLE);
        gridArticle.setVisibility(GONE);
        shimmerArticle.startShimmerAnimation();

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API_LIST_ARTICLE)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("page",1);
            parameter.put("numberPage",5);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        gridArticle.setVisibility(GONE);
        notfoundLayoutArticle.setVisibility(GONE);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                isLoadingArticle = false;
                shimmerArticle.setVisibility(GONE);
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    final JSONArray result = response.getJSONObject("result").getJSONArray("data");
                    if (status.equals("OK")){
                        if(result.length() > 0){
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
                            articleFound(true);
                        }else{
                            articleFound(false);
                        }
                    }else{
                        articleFound(false);
                        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT);
                    }
                    displayArticle();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isLoadingArticle = false;

                shimmerArticle.setVisibility(GONE);

                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(getContext(),LoginActivity.class));
                                            getActivity().finish();
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
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(getContext())
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
    private void getBalance(){
        Log.e(TAG,"GetBalance");
        balanceLoaded = false;
        imgRefresh.startAnimation(animation);
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_BALANCE)
                .buildUpon()
                .toString();


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                balanceLoaded = true;
                try {
                    String status = response.getString("status");

                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        if (result.length()>0){
                            Double balance = result.getJSONObject(0).optDouble("Balance");
                            JSONObject customer = result.getJSONObject(0).optJSONObject("Customer");
                            String name = customer.optString("Name");
                            if (balance != null){
                                NumberFormat formatter = new DecimalFormat("#,###");
                                String formattedNumber = formatter.format(balance);
                                txtSaldo.setText(formattedNumber);
                                if (balance <= 0){
                                    sessionManager.setSession(KEY_ALLOW_SHARE_PROFILE,"false");
                                    switchAutoSave.setChecked(false);
                                }
                            }
                            if (name != null){
                                txtNama.setText(name);
                            }
//                            Toast.makeText(getContext(), "Berhasil Load Total Credit", Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                balanceLoaded = true;
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(getContext(),LoginActivity.class));
                                            getActivity().finish();
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
                            new AlertDialog.Builder(getContext())
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(getContext())
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
    //    private
//    private void getHistoriArtikel(){
//        historiSaldoAdapter = new HistoriSaldoAdapter(getContext());
//        listTransaksi.setVisibility(GONE);
//        notfoundLayout.setVisibility(VISIBLE);
//        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
//        final String url = Uri.parse(API.API_LIST_ARTICLE)
//                .buildUpon()
//                .toString();
//
//        JSONObject parameter = new JSONObject();
//        try {
//            parameter.put("page", 1);
//            parameter.put("numberPage", 10);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, parameter, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//
//                try {
//                    String status = response.getString("status");
//
//                    if (status.equals("OK")){
//
//                        JSONArray result = response.getJSONObject("result").getJSONArray("data");
//                        if (result.length()>0){
//                            listTransaksi.setVisibility(VISIBLE);
//                            notfoundLayout.setVisibility(GONE);
//                            listTransaksi.setAdapter(historiSaldoAdapter);
//                            for (int i = 0;i<result.length();i++){
//                                String creditId = result.getJSONObject(i).getString("CreditId");
//                                String tag = result.getJSONObject(i).getString("Tag");
//                                String description = result.getJSONObject(i).getString("Description");
//                                String tgl = result.getJSONObject(i).getString("CreatedAt");
//                                String refNo = result.getJSONObject(i).getString("RefNo");
//                                String statusTransaksi = result.getJSONObject(i).getString("Status");
//                                Double debit = result.getJSONObject(i).optDouble("Debit");
//                                Double kredit = result.getJSONObject(i).optDouble("Kredit");
//                                if (refNo.equals(null)){
//                                    refNo = "";
//                                }
//                                Double total = 0.0;
//                                if (debit>0){
//                                    total = debit;
//                                }else if(kredit > 0){
//                                    total = kredit;
//                                }
//                                final ItemHistoriSaldo item = new ItemHistoriSaldo();
//                                item.setId("# " + creditId);
//                                item.setTitle(description);
//                                item.setKeterangan("");
//                                item.setTanggal(tgl);
//                                item.setJenis(tag);
//                                item.setNo_ref(refNo);
//                                item.setStatus(statusTransaksi);
//                                item.setTotal(currencyID(total));
//
//
//                                DateFormat formatTgl = new SimpleDateFormat("yyyy-MM-dd");
//                                DateFormat formatTglIndo = new SimpleDateFormat("dd MMM yyyy");
//                                Date tanggal = formatTgl.parse(tgl);
//                                Date now = new Date();
//
//                                Calendar cal = Calendar.getInstance();
//                                cal.setTime(formatTgl.parse(formatTgl.format(now)));
//                                cal.add(Calendar.DATE, -1 );
//
//                                Date yesterday = cal.getTime();
//
//                                if (!stringHeader.equals(formatTgl.format(tanggal))){
//                                    if (formatTgl.format(tanggal).equals(formatTgl.format(now))){
//                                        item.setHeader("Hari ini");
//                                    }else if (formatTgl.format(yesterday).equals(formatTgl.format(tanggal))) {
//                                        item.setHeader("Kemarin");
//                                    }else{
//                                        item.setHeader(formatTglIndo.format(tanggal));
//                                    }
//                                    stringHeader = formatTgl.format(tanggal);
//                                    historiSaldoAdapter.addSectionHeaderItem(item);
//                                }else{
//                                    item.setHeader(formatTglIndo.format(tanggal));
//                                }
//                                Log.i(TAG,"item:" + item.getId());
//                                historiSaldoAdapter.addItem(item);
//                            }
//
//                        }
//                    }
//                }catch (JSONException e){
//                    e.printStackTrace();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                String json;
//                NetworkResponse networkResponse = error.networkResponse;
//                if (networkResponse != null) {
//                    if (networkResponse.statusCode == 401){
//
//                        json = new String(networkResponse.data);
//                        try {
//                            JSONObject jsonObject = new JSONObject(json);
//                            String message = jsonObject.getString("message");
//                            new AlertDialog.Builder(getContext())
//                                    .setMessage(message)
//                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            sessionManager.clearData();
//                                            startActivity(new Intent(getContext(),LoginActivity.class));
//                                            getActivity().finish();
//                                        }
//                                    })
//                                    .show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }else{
//                        json = new String(networkResponse.data);
//                        try {
//                            JSONObject jsonObject = new JSONObject(json);
//                            String message = jsonObject.getString("message");
//                            new AlertDialog.Builder(getContext())
//                                    .setMessage(message)
//                                    .setPositiveButton("OK",  null)
//                                    .show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                }else {
//                    new AlertDialog.Builder(getContext())
//                            .setMessage(errorResponse(error))
//                            .setPositiveButton("OK",null)
//                            .show();
//                }
//            }
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> header = new HashMap<>();
//                header.put("X-API-KEY",apiKey);
//                return header;
//            }
//        };
//        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        jsonObjectRequest.setRetryPolicy(retryPolicy);
//        requestQueue.add(jsonObjectRequest);
//    }
//    private void getHistoryCredit(){
//        if(isLoadingCredit){
//            return;
//        }
//        isLoadingCredit = true;
//        historiSaldoAdapter = new HistoriSaldoAdapter(getContext());
//        listTransaksi.setVisibility(GONE);
//        notfoundLayout.setVisibility(VISIBLE);
//        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
//        final String url = Uri.parse(API.API_HISTORY_CREDIT)
//                .buildUpon()
//                .toString();
//
//        JSONObject parameter = new JSONObject();
//        try {
//            parameter.put("page", 1);
//            parameter.put("numberPage", 10);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                isLoadingCredit = false;
//                try {
//                    String status = response.getString("status");
//
//                    if (status.equals("OK")){
//
//                        stringHeader = "";
//                        JSONArray result = response.getJSONObject("result").getJSONArray("data");
//                        if (result.length()>0){
//                            listTransaksi.setVisibility(VISIBLE);
//                            notfoundLayout.setVisibility(GONE);
//                            listTransaksi.setAdapter(historiSaldoAdapter);
//                            for (int i = 0;i<result.length();i++){
//                                String creditId = result.getJSONObject(i).getString("CreditId");
//                                String tag = result.getJSONObject(i).getString("Tag");
//                                String description = result.getJSONObject(i).getString("Description");
//                                String tgl = result.getJSONObject(i).getString("CreatedAt");
//                                String refNo = result.getJSONObject(i).getString("RefNo");
//                                String statusTransaksi = result.getJSONObject(i).getString("Status");
//                                Double debit = result.getJSONObject(i).optDouble("Debit");
//                                Double kredit = result.getJSONObject(i).optDouble("Kredit");
//
//                                if (tag.equals("topup") || tag.equals("fee_affiliate") || tag.equals("fee_other")){
//
//                                }else{
//                                    continue;
//                                }
//                                if (refNo.equals(null)){
//                                    refNo = "";
//                                }
//                                Double total = 0.0;
//                                if (debit>0){
//                                    total = debit;
//                                }else if(kredit > 0){
//                                    total = kredit;
//                                }
//                                final ItemHistoriSaldo item = new ItemHistoriSaldo();
//                                item.setId("# " + creditId);
//                                item.setTitle(description);
//                                item.setKeterangan("");
//                                item.setTanggal(tgl);
//                                item.setJenis(tag);
//                                item.setNo_ref(refNo);
//                                item.setStatus(statusTransaksi);
//                                item.setTotal(currencyID(total));
//
//
//                                DateFormat formatTgl = new SimpleDateFormat("yyyy-MM-dd");
//                                DateFormat formatTglIndo = new SimpleDateFormat("dd MMM yyyy");
//                                Date tanggal = formatTgl.parse(tgl);
//                                Date now = new Date();
//
//                                Calendar cal = Calendar.getInstance();
//                                cal.setTime(formatTgl.parse(formatTgl.format(now)));
//                                cal.add(Calendar.DATE, -1 );
//
//                                Date yesterday = cal.getTime();
//
//                                if (!stringHeader.equals(formatTgl.format(tanggal))){
//                                    if (formatTgl.format(tanggal).equals(formatTgl.format(now))){
//                                        item.setHeader("Hari ini");
//                                    }else if (formatTgl.format(yesterday).equals(formatTgl.format(tanggal))) {
//                                        item.setHeader("Kemarin");
//                                    }else{
//                                        item.setHeader(formatTglIndo.format(tanggal));
//                                    }
//                                    stringHeader = formatTgl.format(tanggal);
//                                    historiSaldoAdapter.addSectionHeaderItem(item);
//                                }else{
//                                    item.setHeader(formatTglIndo.format(tanggal));
//                                }
//                                Log.i(TAG,"item:" + item.getId());
//                                historiSaldoAdapter.addItem(item);
//                            }
//
//                        }
//                    }
//                }catch (JSONException e){
//                    e.printStackTrace();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                isLoadingCredit = false;
//
//                String json;
//                NetworkResponse networkResponse = error.networkResponse;
//                if (networkResponse != null) {
//                    if (networkResponse.statusCode == 401){
//
//                        json = new String(networkResponse.data);
//                        try {
//                            JSONObject jsonObject = new JSONObject(json);
//                            String message = jsonObject.getString("message");
//                            new AlertDialog.Builder(getContext())
//                                    .setMessage(message)
//                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            sessionManager.clearData();
//                                            startActivity(new Intent(getContext(),LoginActivity.class));
//                                            getActivity().finish();
//                                        }
//                                    })
//                                    .show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }else{
//                        json = new String(networkResponse.data);
//                        try {
//                            JSONObject jsonObject = new JSONObject(json);
//                            String message = jsonObject.getString("message");
//                            new AlertDialog.Builder(getContext())
//                                    .setMessage(message)
//                                    .setPositiveButton("OK",  null)
//                                    .show();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                }else {
//                    new AlertDialog.Builder(getContext())
//                            .setMessage(errorResponse(error))
//                            .setPositiveButton("OK",null)
//                            .show();
//                }
//            }
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> header = new HashMap<>();
//                header.put("X-API-KEY",apiKey);
//                return header;
//            }
//        };
//        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        jsonObjectRequest.setRetryPolicy(retryPolicy);
//        requestQueue.add(jsonObjectRequest);
//    }

    @Override
    public void onScrollChanged() {
        int scrollY = mScrollView.getScrollY();
        //title.setTextColor(Color.rgb(color,color,color));

        if (scrollY==0){
            swipe_refresh.setEnabled(true);
        }else {
            if (!swipe_refresh.isRefreshing()){
                swipe_refresh.setEnabled(false);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        displayProfileIsComplete();
        checkStatusAutoSave();
        loadFoto();
    }

    class HistoriSaldoAdapter extends BaseAdapter {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;

        private List<ItemHistoriSaldo> mData = new ArrayList<>();
        private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

        private LayoutInflater mInflater;

        public HistoriSaldoAdapter(Context context) {
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
            HistoriSaldoAdapter.ViewHolder holder = null;
            int rowType = getItemViewType(position);

            if (convertView == null) {
                holder = new HistoriSaldoAdapter.ViewHolder();
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.item_list_histori_saldo, null);
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
                holder = (HistoriSaldoAdapter.ViewHolder) convertView.getTag();
            }
            switch (rowType) {
                case TYPE_ITEM:
                    holder.txtId.setText(mData.get(position).getId());
                    holder.txtStatus.setText(mData.get(position).getStatus());
                    holder.txtTitle.setText(mData.get(position).getTitle());
                    holder.txtKeterangan.setText(mData.get(position).getKeterangan());
                    holder.txtTotal.setText(mData.get(position).getTotal());
                    if (mData.get(position).getStatus().equals("success")){
                        holder.txtStatus.setTextColor(getResources().getColor(R.color.md_green_500));
                    }else if (mData.get(position).getStatus().equals("pending")) {
                        holder.txtStatus.setTextColor(getResources().getColor(R.color.md_deep_orange_500));
                    }else{
                        holder.txtStatus.setTextColor(getResources().getColor(R.color.md_red_500));
                    }
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
    public static class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

        private List<ModelArticle> list;
        private Context context;

        ArticleAdapter(List<ModelArticle> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_article_home, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String description = list.get(position).getDescription();
            holder.txtTitle.setText(list.get(position).getTitle());
            holder.txtCreatedAt.setText(list.get(position).getCreatedAt());
//            holder.txtCreatedBy.setText(list.get(position).getCreatedBy());
            holder.txtDescription.setText(description);

            if (list.get(position).getImage()!=null){
                String imgUrl = BASE_URL_UPLOADS + list.get(position).getImage();

                Picasso.with(context).load(imgUrl).placeholder(R.drawable.ic_image).error(R.drawable.ic_image).into(holder.imgArticle);
            }
            /*Picasso.with(context).load(list.get(position).getGambar())
                    .placeholder(R.drawable.placeholder_img2)
                    .error(R.drawable.placeholder_img2)
                    .into(holder.imgProduk);*/
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder{

            private ImageView imgArticle;
            private TextView txtTitle,txtCreatedAt,txtCreatedBy,txtDescription;

            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
                txtCreatedAt = (TextView) itemView.findViewById(R.id.txtCreatedAt);

//                txtCreatedBy = (TextView) itemView.findViewById(R.id.txtCreatedBy);
                txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
                imgArticle = (ImageView) itemView.findViewById(R.id.imgArticle);

            }
        }
    }
    public static class LayananAdapter extends RecyclerView.Adapter<LayananAdapter.ViewHolder> {

        private List<ItemLayanan> list;
        private Context context;

        LayananAdapter(List<ItemLayanan> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layanan, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.txtLayanan.setText(list.get(position).getNama());
            holder.imgLayanan.setImageResource(list.get(position).getGambar());

            /*Picasso.with(context).load(list.get(position).getGambar())
                    .placeholder(R.drawable.placeholder_img2)
                    .error(R.drawable.placeholder_img2)
                    .into(holder.imgProduk);*/
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder{

            private ImageView imgLayanan;
            private TextView txtLayanan;

            ViewHolder(View itemView) {
                super(itemView);
                imgLayanan = itemView.findViewById(R.id.imgLayanan);
                txtLayanan = itemView.findViewById(R.id.txtLayanan);
            }
        }
    }

    private static class ItemLayanan{
        private String nama;
        private int gambar;

        ItemLayanan(String nama, int gambar) {
            this.nama = nama;
            this.gambar = gambar;
        }

        String getNama() {
            return nama;
        }
        int getGambar() {
            return gambar;
        }
    }

    class ListFeedAdapter extends BaseAdapter {

        public List<ModelFeed> listFeed;

        public Context context;
        ArrayList<ModelFeed> arraylist;

        private static final int resource = R.layout.item_feed_public;

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
            ImageView imgFeed = (ImageView) view.findViewById(R.id.imgFeed);


            final ModelFeed itemContact = getItem(position);
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
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgFeed);
            }

            return view;
        }
    }

    private static class ItemSlider{
        private String gambar,link;

        ItemSlider(String gambar,String link) {
            this.gambar = gambar;
            this.link = link;
        }

        public String getGambar() {
            return gambar;
        }

        public void setGambar(String gambar) {
            this.gambar = gambar;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

    }
    public static class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.ViewHolder> {

        private List<ItemSlider> list;
        private Context context;

        SliderAdapter(List<ItemSlider> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_slider, parent, false));
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imgSlider.setClipToOutline(true);
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displaymetrics);
            int screenWidth = displaymetrics.widthPixels;
            if (screenWidth > 480){
                holder.imgSlider.getLayoutParams().height = context.getResources().getDimensionPixelOffset(R.dimen.whislistHeight);
            }
            Picasso.with(context).load(list.get(position).getGambar())
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .fit()
                    .into(holder.imgSlider);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String link = list.get(position).getLink();
                        if (link != null){
                            if (link.isEmpty() == false){
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                context.startActivity(browserIntent);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder{

            private ImageView imgSlider;

            ViewHolder(View itemView) {
                super(itemView);
                imgSlider = itemView.findViewById(R.id.imgSlider);
            }
        }
    }
}