package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_COVER;
import static id.my.hubkontak.utils.SessionManager.KEY_FOTO;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.Utils.ConvertBitmapToString;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getFileExtension;

public class CoverActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final String TAG = "CoverActivity";
    private static final int REQ_GALLERY_FOTO = 101;
    private static final int REQ_CAMERA_COVER = 102;
    private ImageView imgCover;
    private Button btnUbah;
    private SharedPref sharedPref;
    private SessionManager sessionManager;
    private String apiKey;
    private String urlCover;

    CharSequence[] items = {"Kamera", "Galeri","Hapus Foto"};
    private File coverFile;
    private String coverPath;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sharedPref = new SharedPref(this);
        sessionManager = new SessionManager(this);

        HashMap<String, String> userDetail = sessionManager.getUserDetails();

        apiKey = userDetail.get(KEY_TOKEN);
        imgCover = (ImageView) findViewById(R.id.imgCover);
        btnUbah = (Button) findViewById(R.id.btnUbah);

        urlCover = API.BASE_URL_UPLOADS + sessionManager.getSession(KEY_COVER);
        Picasso.with(this).load(urlCover).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgCover);

        btnUbah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CoverActivity.this)
                        .setTitle("Pilih Foto")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    if (ActivityCompat.checkSelfPermission(CoverActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(CoverActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }else{
                                        requestCamera();
                                    }
                                }else if(which==1){
                                    loadGalery(REQ_GALLERY_FOTO);
                                }else{
                                    removeCover();
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            coverFile = createImageFile("foto");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (coverFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "id.my.hubkontak",
                    coverFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQ_CAMERA_COVER);
        }
    }
    private void loadGalery(int requestCode){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CoverActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile(String img) throws IOException {
        // Create an image file name
        Locale ID = new Locale("in", "ID");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",ID).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        coverPath = image.getAbsolutePath();

        Log.i(TAG, img + " Path " + image.getAbsolutePath());
        return image;
    }
    private void uploadFoto(){
        ProgressDialog progressDialog = new ProgressDialog(CoverActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(CoverActivity.this);
        final String url = Uri.parse(API.API_UPLOAD_COVER)
                .buildUpon()
                .toString();

        JSONObject parameter = new JSONObject();
        try {
            parameter.put("CoverContact", coverPath);
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
                        String cover = result.optString("CoverContact");
                        sessionManager.setSession(KEY_COVER,cover);
                        urlCover = API.BASE_URL_UPLOADS + cover;
                        Picasso.with(CoverActivity.this).load(urlCover ).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgCover);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imgCover.setImageDrawable(getDrawable(R.drawable.blank_profile));
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(CoverActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
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
                            new AlertDialog.Builder(CoverActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(CoverActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(CoverActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(CoverActivity.this)
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
    private void removeCover(){
        ProgressDialog progressDialog = new ProgressDialog(CoverActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(CoverActivity.this);
        final String url = Uri.parse(API.API_REMOVE_COVER)
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
                        String cover = result.optString("CoverContact");
                        sessionManager.setSession(KEY_COVER,cover);
                        urlCover = API.BASE_URL_UPLOADS + cover;
                        Picasso.with(CoverActivity.this).load(urlCover ).placeholder(R.drawable.blank_profile).error(R.drawable.blank_profile)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgCover);
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imgCover.setImageDrawable(getDrawable(R.drawable.blank_profile));
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(CoverActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
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
                            new AlertDialog.Builder(CoverActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            sessionManager.clearData();
                                            startActivity(new Intent(CoverActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(CoverActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(CoverActivity.this)
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
        if (requestCode == REQ_GALLERY_FOTO) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    Uri imageSelected = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(imageSelected, filePath, null, null, null);
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
                        bitmap.compress(Bitmap.CompressFormat.PNG,80,stream);

                        coverPath = ConvertBitmapToString(bitmap);
                        imgCover.setImageBitmap(bitmap);
                        uploadFoto();
                    }
                }
            }
        }
        if (requestCode == REQ_CAMERA_COVER && resultCode == RESULT_OK) {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 8;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(coverPath, bmOptions);
            imgCover.setImageBitmap(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG,80,stream);
            String path = ConvertBitmapToString(bitmap);
            coverPath = path;
            uploadFoto();
        }
    }

}