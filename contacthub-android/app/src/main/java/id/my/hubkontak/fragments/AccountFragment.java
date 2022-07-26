package id.my.hubkontak.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.my.hubkontak.CoverActivity;
import id.my.hubkontak.CreditActivity;
import id.my.hubkontak.EditProfileActivity;
import id.my.hubkontak.LoginActivity;
import id.my.hubkontak.R;
import id.my.hubkontak.SettingContactActivity;
import id.my.hubkontak.WebviewActivity;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;
import id.my.hubkontak.utils.Utils;
import id.my.hubkontak.utils.view.RecyclerViewItemDecoration;

import static android.app.Activity.RESULT_OK;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.DBContract.TEMPLATE_SHARE;
import static id.my.hubkontak.utils.SessionManager.KEY_COVER;
import static id.my.hubkontak.utils.SessionManager.KEY_EMAIL;
import static id.my.hubkontak.utils.SessionManager.KEY_FOTO;
import static id.my.hubkontak.utils.SessionManager.KEY_MARKETING_CODE;
import static id.my.hubkontak.utils.SessionManager.KEY_NAME;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.ConvertBitmapToString;
import static id.my.hubkontak.utils.Utils.currencyID;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getDeviceId;
import static id.my.hubkontak.utils.Utils.getFileExtension;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();

    private static final int REQ_GALLERY_FOTO = 90;
    private static final int REQ_CAMERA_FOTO = 91;
    private static final int REQ_EDIT_AKUN = 100;
    private static final int REQ_SETTING_CONTACT = 101;
    private static final String KEY_EDIT_PROFIL = "Edit Profil";
    private static final String KEY_EDIT_COVER = "Edit Cover";
    private static final String KEY_SETTING_CONTACT = "Pengaturan";
    private static final String KEY_KEBIJAKAN_PRIVASI = "Kebijakan Privasi";
    private static final String KEY_RATING = "Beri kami rating";
    private static final String KEY_SHARE_APP = "Ajak Teman";
    private static final String KEY_BANTUAN = "Bantuan";
    private static final int IC_EDITPROFIL = R.drawable.ic_editprofil;
    private static final int IC_KEBIJAKAN_PRIVASI = R.drawable.ic_privacypolicy;
    private static final int IC_RATING = R.drawable.ic_rating;
    private static final int IC_BANTUAN = R.drawable.ic_help;
    private static final int IC_SHARE = R.drawable.ic_share;
    private static final int IC_CONTACT = R.drawable.ic_contact;
    private static final int IC_COVER = R.drawable.ic_cover;
    private static final int REQUEST_CAMERA = 92;

    private List<ItemAccount> arrayAkun = new ArrayList<>();
    private List<ItemAccount> arrayTentang = new ArrayList<>();
    private SessionManager sessionManager;
    private ImageView imgAkun;
    private TextView txtNama, txtEmail;
    private String ApiKey,url_track_playstore,marketingCode;
    private SharedPref sharedPref;

    CharSequence[] items = {"Kamera", "Galeri","Hapus Foto"};
    private String fotoPath;
    private File fotoFile;
    private String urlFoto;
    private TextView txtDeviceId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        sharedPref = new SharedPref(getContext());

        sessionManager = new SessionManager(getContext());

        HashMap<String, String> userDetail = sessionManager.getUserDetails();

        ApiKey = userDetail.get(KEY_TOKEN);
        marketingCode = userDetail.get(KEY_MARKETING_CODE);

        url_track_playstore = Uri.parse("https://play.google.com/store/apps/details?id=id.my.hubkontak")
                .buildUpon()
                .appendQueryParameter("referrer",marketingCode)
                .toString();

        txtDeviceId = view.findViewById(R.id.txtDeviceId);
        txtNama = view.findViewById(R.id.txtNama);
        txtEmail = view.findViewById(R.id.txtEmail);
        imgAkun = view.findViewById(R.id.imgAkun);

        txtNama.setText(userDetail.get(KEY_NAME));
        txtEmail.setText(userDetail.get(KEY_EMAIL));
        imgAkun.setClipToOutline(true);
        txtDeviceId.setText("DeviceId:" + getDeviceId(getContext()));
        Picasso.with(getActivity()).load(userDetail.get(KEY_FOTO))
                .error(R.drawable.blank_profile)
                .placeholder(R.drawable.blank_profile)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .fit()
                .into(imgAkun);

        RecyclerView gridAkun = view.findViewById(R.id.gridAkun);
        RecyclerView gridTentang = view.findViewById(R.id.gridTentang);

        arrayAkun.add(new ItemAccount(KEY_EDIT_PROFIL, IC_EDITPROFIL));
        arrayAkun.add(new ItemAccount(KEY_EDIT_COVER, IC_COVER));
        arrayAkun.add(new ItemAccount(KEY_SETTING_CONTACT, IC_CONTACT));
        arrayTentang.add(new ItemAccount(KEY_SHARE_APP, IC_SHARE));
        arrayTentang.add(new ItemAccount(KEY_KEBIJAKAN_PRIVASI, IC_KEBIJAKAN_PRIVASI));
        arrayTentang.add(new ItemAccount(KEY_RATING, IC_RATING));
        arrayTentang.add(new ItemAccount(KEY_BANTUAN, IC_BANTUAN));

        gridTentang.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        gridAkun.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        gridTentang.setNestedScrollingEnabled(true);
        gridAkun.setNestedScrollingEnabled(true);
        AccountAdapter akunAdapter = new AccountAdapter(arrayAkun, getActivity());
        AccountAdapter tentangAdapter = new AccountAdapter(arrayTentang, getActivity());
        gridTentang.setAdapter(tentangAdapter);
        gridAkun.setAdapter(akunAdapter);
        gridAkun.addItemDecoration(new RecyclerViewItemDecoration(Objects.requireNonNull(getActivity()),
                LinearLayoutManager.VERTICAL, getActivity().getResources().getDimensionPixelOffset(R.dimen.dividerMargin)));
        gridTentang.addItemDecoration(new RecyclerViewItemDecoration(Objects.requireNonNull(getActivity()),
                LinearLayoutManager.VERTICAL, getActivity().getResources().getDimensionPixelOffset(R.dimen.dividerMargin)));
        akunAdapter.setOnItemClickListener(new AccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (arrayAkun.get(position).getTitle().equals(KEY_EDIT_PROFIL)) {
                    startActivityForResult(new Intent(getActivity(), EditProfileActivity.class), REQ_EDIT_AKUN);
                }else if (arrayAkun.get(position).getTitle().equals(KEY_SETTING_CONTACT)) {
                    startActivityForResult(new Intent(getActivity(), SettingContactActivity.class), REQ_SETTING_CONTACT);
                }else if (arrayAkun.get(position).getTitle().equals(KEY_EDIT_COVER)) {
                    startActivity(new Intent(getActivity(), CoverActivity.class));
                }
            }
        });
        tentangAdapter.setOnItemClickListener(new AccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (arrayTentang.get(position).getTitle()) {
                    case KEY_KEBIJAKAN_PRIVASI:
                        startActivity(new Intent(getActivity(), WebviewActivity.class)
                                .putExtra("title", "Kebijakan Privasi")
                                .putExtra("url", sharedPref.getSessionStr(SharedPref.KEY_URL_KEBIJAKAN_PRIVASI))
                        );
                        break;
                    case KEY_RATING:
                        Utils.openAppPlaystore(Objects.requireNonNull(getActivity()));
                        break;
                    case KEY_SHARE_APP:
                        try {
                            String konten = TEMPLATE_SHARE;
                            if (!TextUtils.isEmpty(sharedPref.getSessionStr(SharedPref.KEY_TEMPLATE_SHARE)) ){
                                konten = sharedPref.getSessionStr(SharedPref.KEY_TEMPLATE_SHARE);
                            }
                            konten = konten.replace("[linkweb]",url_track_playstore);

                            String appId = getActivity().getPackageName();
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                            String sAux = konten;
                            i.putExtra(Intent.EXTRA_TEXT, sAux);
                            startActivity(Intent.createChooser(i, "Bagikan lewat"));
                        } catch(Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case KEY_BANTUAN:
                        startActivity(new Intent(getActivity(), WebviewActivity.class)
                                .putExtra("title", "Bantuan")
                                .putExtra("url", sharedPref.getSessionStr(SharedPref.KEY_URL_BANTUAN))
                        );
                        break;
                }
            }
        });

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setMessage("Apakah anda yakin akan keluar dari aplikasi ?")
                        .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logout();
                            }
                        }).setNegativeButton("Tidak", null).show();
            }
        });
        imgAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Pilih Foto")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }else{
                                        requestCamera();
                                    }
                                }else if(which==1){
                                    loadGalery(REQ_GALLERY_FOTO);
                                }else{
                                    removeFoto();
                                }
                            }
                        })
                        .show();
            }
        });
        urlFoto = API.BASE_URL_UPLOADS + sessionManager.getSession(KEY_FOTO);
        Picasso.with(getContext()).load(urlFoto)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile).into(imgAkun);
        return view;
    }

    private void removeFoto(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_REMOVE_FOTO)
                .buildUpon()
                .toString();

        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")){
                        JSONObject result = response.getJSONObject("result");
                        String foto = result.optString("Foto");
                        sessionManager.setSession(KEY_FOTO,foto);
                        urlFoto = API.BASE_URL_UPLOADS + foto;
                        Picasso.with(getContext()).load(urlFoto )
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile).into(imgAkun);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imgAkun.setImageDrawable(getContext().getDrawable(R.drawable.blank_profile));
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT);
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
                header.put("X-API-KEY",ApiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    private void requestCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            fotoFile = createImageFile("foto");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (fotoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(getContext(),
                    "id.my.hubkontak",
                    fotoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQ_CAMERA_FOTO);
        }
    }
    private void loadGalery(int requestCode){
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                loadGalery(requestCode);
            } else {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile(String img) throws IOException {
        // Create an image file name
        Locale ID = new Locale("in", "ID");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",ID).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        fotoPath = image.getAbsolutePath();

        Log.i(TAG, img + " Path " + image.getAbsolutePath());
        return image;
    }
    private void uploadFoto(){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_UPLOAD_FOTO)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Foto", fotoPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")){
                        JSONObject result = response.getJSONObject("result");
                        String foto = result.optString("Foto");
                        sessionManager.setSession(KEY_FOTO,foto);
                        urlFoto = API.BASE_URL_UPLOADS + foto;
                        Log.v(TAG,urlFoto);
                        Picasso.with(getContext()).load(urlFoto ).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgAkun);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imgAkun.setImageDrawable(getContext().getDrawable(R.drawable.blank_profile));
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT);
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
                header.put("X-API-KEY",ApiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCamera();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String file;
        if (requestCode == REQ_EDIT_AKUN) {
            if (resultCode == RESULT_OK) {
//                get_profile();
            }
        }
        if (requestCode == REQ_GALLERY_FOTO) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    Uri imageSelected = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContext().getContentResolver().query(imageSelected, filePath, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePath[0]);
                    String imagePath = cursor.getString(columnIndex);
                    cursor.close();

                    file = imagePath;
                    Log.i(TAG, "KTP Image Path " + file);
                    File f = new File(imagePath);

                    String extension = getFileExtension(f);

                    if (extension.toLowerCase().equals("jpg") || extension.toLowerCase().equals("jpeg") || extension.toLowerCase().equals("png")) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inSampleSize = 8;

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
                        bitmap.compress(Bitmap.CompressFormat.PNG,50,stream);

                        fotoPath = ConvertBitmapToString(bitmap);
                        imgAkun.setImageBitmap(bitmap);
                        uploadFoto();
                    }
                }
            }
        }
        if (requestCode == REQ_CAMERA_FOTO && resultCode == RESULT_OK) {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 8;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(fotoPath, bmOptions);
            imgAkun.setImageBitmap(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG,50,stream);
            String path = ConvertBitmapToString(bitmap);
            fotoPath = path;
            uploadFoto();
        }
    }

    private void get_profile(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_GET_PROFILE)
                .buildUpon()
                .toString();

        progressDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    if (status.equals("OK")){
                        sessionManager.clearData();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        Objects.requireNonNull(getActivity()).finish();
                    }else{
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",  null)
                            .show();
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
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                header.put("X-API-KEY",ApiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    private void logout(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String url = Uri.parse(API.API_LOGOUT)
                .buildUpon()
                .toString();

        progressDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    if (status.equals("OK")){
                        sessionManager.clearData();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        Objects.requireNonNull(getActivity()).finish();
                    }else{
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",  null)
                                .show();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",  null)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                sessionManager.clearData();
                startActivity(new Intent(getContext(), LoginActivity.class));
                Objects.requireNonNull(getActivity()).finish();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("X-API-KEY",ApiKey);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    private static class ItemAccount {
        String title;
        int icon;

        public ItemAccount(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }
        public String getTitle() {
            return title;
        }
        public int getIcon() {
            return icon;
        }
    }
    private static class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder>{

        private List<ItemAccount> list;
        private Context context;
        private OnItemClickListener mListener;

        public AccountAdapter(List<ItemAccount> list, Context context) {
            this.list = list;
            this.context = context;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_account, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.iconItem.setImageResource(list.get(position).getIcon());
            holder.txtTitle.setText(list.get(position).getTitle());
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private ImageView iconItem;
            private TextView txtTitle;

            ViewHolder(View itemView) {
                super(itemView);
                iconItem = itemView.findViewById(R.id.iconItem);
                txtTitle = itemView.findViewById(R.id.txtTitle);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(getAdapterPosition());
                    }
                });
            }
        }
        public interface OnItemClickListener {
            void onItemClick(int position);
        }
    }
}