package id.my.hubkontak;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.my.hubkontak.models.ModelArticle;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.SessionManager;
import id.my.hubkontak.utils.SharedPref;
import id.my.hubkontak.utils.upload.AndroidMultiPartEntity;

import static id.my.hubkontak.utils.API.API_LIST_ARTICLE;
import static id.my.hubkontak.utils.API.BASE_URL_UPLOADS;
import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.SessionManager.KEY_TOKEN;
import static id.my.hubkontak.utils.SessionManager.KEY_WHATSAPP;
import static id.my.hubkontak.utils.Utils.ConvertBitmapToString;
import static id.my.hubkontak.utils.Utils.convertDpToPixel;
import static id.my.hubkontak.utils.Utils.errorResponse;
import static id.my.hubkontak.utils.Utils.getFileExtension;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class EditFeedActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 92;
    private static final int REQ_CAMERA_FOTO = 91;
    private static final int REQ_GALLERY_FOTO = 90;
    private static final String TAG = EditFeedActivity.class.getSimpleName();
    private SharedPref sharedPref;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String apiKey;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private EditText edtTitle,edtDescription;
    private Spinner spinCategory;
    private ImageView imgFeed;
    private Button btnSimpan;
    private List<String> arrInterest = new ArrayList<>();
    private String feedId,title,description,category,statusFeed,imageFeed;
    private TextView txtStatus;
    private Spinner spinStatus;
    private List<String> arrStatus = new ArrayList<>();
    CharSequence[] items = {"Kamera", "Galeri"};
    private String fotoPath;
    private File fotoFile;
    private String phoneNo;
    private EditText edtPhone;
    private long totalSize = 0;
    private String reason;
    private TextView txtReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);

        feedId = getIntent().getStringExtra("FeedId");
        reason = getIntent().getStringExtra("Reason");
        title = getIntent().getStringExtra("Title");
        phoneNo = getIntent().getStringExtra("PhoneNo");
        description = getIntent().getStringExtra("Description");
        category = getIntent().getStringExtra("Category");
        statusFeed = getIntent().getStringExtra("Status");
        imageFeed = getIntent().getStringExtra("Image");

        sharedPref = new SharedPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        apiKey = userDetail.get(KEY_TOKEN);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);


        txtReason = (TextView) findViewById(R.id.txtReason);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        edtTitle = (EditText) findViewById(R.id.edtTitle);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtDescription = (EditText) findViewById(R.id.edtDescription);
        imgFeed = (ImageView) findViewById(R.id.imgFeed);
        imgFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(EditFeedActivity.this)
                        .setTitle("Pilih Foto")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    if (ActivityCompat.checkSelfPermission(EditFeedActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(EditFeedActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }else{
                                        requestCamera();
                                    }
                                }else if(which==1){
                                    loadGalery(REQ_GALLERY_FOTO);
                                }else{
                                    fotoPath = "";
                                    fotoFile = null;
                                    imgFeed.setImageResource(R.drawable.placeholder_image);
                                }
                            }
                        })
                        .show();
            }
        });
        spinStatus = (Spinner) findViewById(R.id.spinStatus);
        spinCategory = (Spinner) findViewById(R.id.spinCategory);

        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtTitle.getText())){
                    edtTitle.setError("Field ini tidak boleh kosong");
                    edtTitle.requestFocus();
                }else if (TextUtils.isEmpty(edtDescription.getText())) {
                    edtDescription.setError("Field ini tidak boleh kosong");
                    edtDescription.requestFocus();
                }else if (TextUtils.isEmpty(edtPhone.getText())) {
                    edtPhone.setError("Field ini tidak boleh kosong");
                    edtPhone.requestFocus();
                }else{
                    new doSimpan().execute();
                }
            }
        });
        displayStatus();
        loadInterest();

        if (feedId != null){
            edtTitle.setText(title);
            txtReason.setVisibility(View.VISIBLE);
            txtReason.setText(reason);
            edtPhone.setText(phoneNo);
            edtDescription.setText(description);
            if (imageFeed != null){
                String imgUrl = BASE_URL_UPLOADS + imageFeed;
                Picasso.with(this).load(imgUrl).placeholder(R.drawable.ic_image).error(R.drawable.ic_image)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).into(imgFeed);
            }
        }else{
            edtPhone.setText(userDetail.get(KEY_WHATSAPP));
            spinStatus.setVisibility(View.GONE);
            txtStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (feedId != null){
            getMenuInflater().inflate(R.menu.menu_feed, menu);
            menu.findItem(R.id.actAdd).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
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

        fotoPath = image.getAbsolutePath();

        Log.i(TAG, img + " Path " + image.getAbsolutePath());
        return image;
    }
    private void requestCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            fotoFile = createImageFile("foto");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (fotoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "id.my.hubkontak",
                    fotoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQ_CAMERA_FOTO);
        }
    }
    private void loadGalery(int requestCode){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
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
    private void loadInterest() {
        if(progressDialog.isShowing() == false){
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_LIST_INTEREST)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");

                    if (status.equals("OK")){
                        JSONArray result = response.getJSONArray("result");
                        arrInterest.clear();
                        for (int i=0;i<result.length();i++){
                            String interestId = result.getJSONObject(i).getString("InterestId");
                            arrInterest.add(interestId);
                        }
                        displayInterest();
                    }else{
                        if (progressDialog.isShowing() == false) {
                            new AlertDialog.Builder(EditFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(EditFeedActivity.this)
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
                            new AlertDialog.Builder(EditFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                    if (progressDialog.isShowing() == false) {
                        new AlertDialog.Builder(getApplicationContext())
                                .setMessage(errorResponse(error))
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
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
//                        bmOptions.inSampleSize = 100;

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
                        bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);

                        fotoPath = ConvertBitmapToString(bitmap);
                        imgFeed.setImageBitmap(bitmap);
                        fotoFile = f;
                    }
                }
            }
        }
        if (requestCode == REQ_CAMERA_FOTO && resultCode == RESULT_OK) {
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            bmOptions.inSampleSize = 100;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(fotoPath, bmOptions);
            imgFeed.setImageBitmap(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            String path = ConvertBitmapToString(bitmap);
            fotoPath = path;
        }
    }
    private void displayInterest() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrInterest);
        spinCategory.setAdapter(adapter);
        if (feedId != null) {
            for (int i = 0; i < arrInterest.size(); i++) {
                String interestId = arrInterest.get(i);
                if (interestId.equals(category)) {
                    spinCategory.setSelection(i);
                    break;
                }
            }
        }
    }
    private void displayStatus() {
        arrStatus.clear();
        arrStatus.add("publish");
        arrStatus.add("unpublish");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, arrStatus);
        spinStatus.setAdapter(adapter);
        if (feedId != null) {
            if (statusFeed.equals("publish") || statusFeed.equals("unpublish")) {
                if (statusFeed.equals("publish")){
                    spinStatus.setSelection(0);
                }else{
                    spinStatus.setSelection(1);
                }
            }else{
                spinStatus.setVisibility(View.GONE);
                txtStatus.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }else if(item.getItemId() == R.id.actDelete){
            new AlertDialog.Builder(this)
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteFeed();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFeed() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(API.API_DELETE_FEED)
                .buildUpon()
                .appendQueryParameter("FeedId",feedId)
                .toString();

        progressDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    final String status = response.getString("status");
                    final String message = response.getString("message");
                    if (status.equals("OK")) {
                        new AlertDialog.Builder(EditFeedActivity.this)
                                .setMessage("Feed berhasil dihapus")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                                .show();
                    }else{
                        new AlertDialog.Builder(EditFeedActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    Toast.makeText(EditFeedActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.i(TAG,"Volley Error : " + errorResponse(error));
                String json;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 401){

                        json = new String(networkResponse.data);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String message = jsonObject.getString("message");
                            new AlertDialog.Builder(EditFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            session.clearData();
                                            startActivity(new Intent(EditFeedActivity.this,LoginActivity.class));
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
                            new AlertDialog.Builder(EditFeedActivity.this)
                                    .setMessage(message)
                                    .setPositiveButton("OK",  null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }else {
                    new AlertDialog.Builder(EditFeedActivity.this)
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

    /**
     * Uploading the file to server
     * */
    private class doSimpan extends AsyncTask<Void, Integer, String> {
        final ProgressDialog progressDialog = new ProgressDialog(EditFeedActivity.this);


        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setMessage("Loading... "+progress[0]+"%");
            // Making progress bar visible
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();


            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                try {
                    File sourceFile = fotoFile;
                    entity.addPart("file", new FileBody(sourceFile));
                }catch (Exception e){
                    e.printStackTrace();
                }
                // Adding file data to http body

                // Extra parameters if you want to pass to server
                entity.addPart("Title", new StringBody(edtTitle.getText().toString()));
                entity.addPart("Description", new StringBody(edtDescription.getText().toString()));
                entity.addPart("PhoneNo", new StringBody(edtPhone.getText().toString()));
                entity.addPart("Category", new StringBody(arrInterest.get(spinCategory.getSelectedItemPosition())));
                if (txtStatus.getVisibility() == View.VISIBLE){
                    entity.addPart("Status", new StringBody(arrStatus.get(spinStatus.getSelectedItemPosition())));
                }

                totalSize = entity.getContentLength();

                HttpResponse response = null;
                httpclient.getConnectionManager().getSchemeRegistry().register( new Scheme("https", SSLSocketFactory.getSocketFactory(), 443) );
                if (feedId != null){
                    entity.addPart("FeedId", new StringBody(feedId));
                    HttpPut http = new HttpPut(API.API_UPDATE_FEED);
                    http.setEntity(entity);
                    http.setHeader("X-API-KEY", apiKey);
                    response = httpclient.execute(http);
                }else{
                    HttpPost http = new HttpPost(API.API_CREATE_FEED);
                    http.setHeader("X-API-KEY", apiKey);
                    http.setEntity(entity);
                    response = httpclient.execute(http);
                }
                // Making server call
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                responseString = EntityUtils.toString(r_entity);
                if (statusCode == 200) {
                    // Server response
                }

            } catch (ClientProtocolException e) {
                progressDialog.dismiss();
                responseString = e.toString();
            } catch (IOException e) {
                progressDialog.dismiss();
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);
            progressDialog.dismiss();
            // showing the server response in an alert dialog
            try {

                JSONObject jsonObject = new JSONObject(result);
                final String status = jsonObject.getString("status");
                final String pesan = jsonObject.getString("message");
                Log.i(TAG,jsonObject.toString());
                if (status.equals("OK")){
                    new AlertDialog.Builder(EditFeedActivity.this)
                            .setMessage("Ads Feed berhasil diinput dan akan  direview oleh admin terlebih dahulu")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            })
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(EditFeedActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }

    }


}