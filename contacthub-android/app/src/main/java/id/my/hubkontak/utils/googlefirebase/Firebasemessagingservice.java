package id.my.hubkontak.utils.googlefirebase;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.my.hubkontak.AffiliateActivity;
import id.my.hubkontak.MainActivity;
import id.my.hubkontak.R;
import id.my.hubkontak.SinglePageArticleActivity;
import id.my.hubkontak.SplashActivity;
import id.my.hubkontak.utils.AMQSubscriber;
import id.my.hubkontak.utils.API;
import id.my.hubkontak.utils.DBContract;
import id.my.hubkontak.utils.SessionManager;

import static id.my.hubkontak.utils.API.AMQ_HOST;
import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;

public class Firebasemessagingservice extends FirebaseMessagingService {

    private static final String TAG = Firebasemessagingservice.class.getSimpleName();
    private SessionManager sessionManager;
    private HashMap<String, String> userDetail;

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String titleNotification = "";
        String bodyNotification = "";
        if (remoteMessage.getNotification() != null) {
            titleNotification = remoteMessage.getNotification().getTitle();
            bodyNotification = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        sessionManager = new SessionManager(getApplicationContext());
        userDetail = sessionManager.getUserDetails();
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            JSONObject object =  new JSONObject(remoteMessage.getData());

            try {
                SessionManager sessionManager = new SessionManager(this);
                Date dateNow = Calendar.getInstance().getTime();
                String action = object.getString("action");
                Log.e(TAG,"actionFirebase:" + action);
                if ("required_sync".equals(action)) {
                    if (dateNow.getTime() > dateFromString(sessionManager.getAmqUpdatedAt()).getTime()){
                        AmqTask runner = new AmqTask();
                        runner.execute();
                    }
                }else if("link".equals(action)){
                    String link = object.getString("Link");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    browserIntent.putExtra("link",link);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    String channelId = getString(R.string.default_notification_channel_id);
                    if(isAppIsInBackground(getApplicationContext()) == false){
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId )
                                .setSmallIcon(R.drawable. ic_launcher_foreground )
                                .setContentTitle( titleNotification )
                                .setContentIntent(pendingIntent)
                                .setStyle( new NotificationCompat.InboxStyle())
                                .setContentText( bodyNotification ) ;
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE );
                        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                            int importance = NotificationManager. IMPORTANCE_HIGH ;
                            NotificationChannel notificationChannel = new NotificationChannel( channelId , channelId , importance) ;
                            mBuilder.setChannelId( channelId ) ;
                            assert mNotificationManager != null;
                            mNotificationManager.createNotificationChannel(notificationChannel) ;
                        }
                        assert mNotificationManager != null;
                        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
                    }

                }else if("referral".equals(action)){ // referral masuk
                    Intent browserIntent = new Intent(getApplicationContext(), AffiliateActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    String channelId = getString(R.string.default_notification_channel_id);
                    if(isAppIsInBackground(getApplicationContext()) == false){
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId )
                                .setSmallIcon(R.drawable. ic_launcher_foreground )
                                .setContentTitle( titleNotification )
                                .setContentIntent(pendingIntent)
                                .setStyle( new NotificationCompat.InboxStyle())
                                .setContentText( bodyNotification ) ;
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE );
                        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                            int importance = NotificationManager. IMPORTANCE_HIGH ;
                            NotificationChannel notificationChannel = new NotificationChannel( channelId , channelId , importance) ;
                            mBuilder.setChannelId( channelId ) ;
                            assert mNotificationManager != null;
                            mNotificationManager.createNotificationChannel(notificationChannel) ;
                        }
                        assert mNotificationManager != null;
                        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
                    }else{
                        Intent intent = new Intent("notif_referral");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                }else if("contact".equals(action)){ // kontak di save oleh orang lain
                    Intent browserIntent = new Intent(getApplicationContext(), SplashActivity.class)
                            .putExtra("callFragment","ContactShareFragment");
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    String channelId = getString(R.string.default_notification_channel_id);
                    if(isAppIsInBackground(getApplicationContext()) == false){
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId )
                                .setSmallIcon(R.drawable. ic_launcher_foreground )
                                .setContentTitle( titleNotification )
                                .setContentIntent(pendingIntent)
                                .setStyle( new NotificationCompat.InboxStyle())
                                .setContentText( bodyNotification ) ;
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE );
                        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                            int importance = NotificationManager. IMPORTANCE_HIGH ;
                            NotificationChannel notificationChannel = new NotificationChannel( channelId , channelId , importance) ;
                            mBuilder.setChannelId( channelId ) ;
                            assert mNotificationManager != null;
                            mNotificationManager.createNotificationChannel(notificationChannel) ;
                        }
                        assert mNotificationManager != null;
                        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
                    }
                }else if("article".equals(action)){
                    String articleTitle = object.optString("ArticleTitle");
                    String articleUrl = object.optString("ArticleUrl");
                    Intent browserIntent = new Intent(getApplicationContext(), SinglePageArticleActivity.class)
                            .putExtra("title", articleTitle)
                            .putExtra("url", articleUrl);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    String channelId = getString(R.string.default_notification_channel_id);
                    if(isAppIsInBackground(getApplicationContext()) == false){
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId )
                                .setSmallIcon(R.drawable. ic_launcher_foreground )
                                .setContentTitle( titleNotification )
                                .setContentIntent(pendingIntent)
                                .setStyle( new NotificationCompat.InboxStyle())
                                .setContentText( bodyNotification ) ;
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE );
                        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                            int importance = NotificationManager. IMPORTANCE_HIGH ;
                            NotificationChannel notificationChannel = new NotificationChannel( channelId , channelId , importance) ;
                            mBuilder.setChannelId( channelId ) ;
                            assert mNotificationManager != null;
                            mNotificationManager.createNotificationChannel(notificationChannel) ;
                        }
                        assert mNotificationManager != null;
                        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
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
                    amq.start(getApplicationContext(),AMQ_HOST,exchange,new String[]{"save_contact_" + userDetail.get(KEY_USER_ID),"share_contact_" + userDetail.get(KEY_USER_ID)});
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
    private Date dateFromString(String datetime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = sdf.parse(datetime);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }
        return date;
    }
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetails();

        sessionManager.setFirebaseToken(token);
        final String API_KEY = userDetail.get(SessionManager.KEY_TOKEN);

        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final String url = Uri.parse(API.API_UPDATE_FIREBASE_TOKEN)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG, "API RESPONSE : " + response);
                    String message = response.getString("resultMsg");
                    Log.d(TAG, message);
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.i(TAG, "ERROR RESPONSE : " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("X-API-KEY", API_KEY);
                return header;
            }
        };
        RetryPolicy retryPolicy = new DefaultRetryPolicy(DBContract.SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }
}
