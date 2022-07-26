package id.my.hubkontak.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import static id.my.hubkontak.utils.DBContract.SOCKET_TIMEOUT;
import static id.my.hubkontak.utils.DBContract.TELEGRAM_BOT_API_KEY;
import static id.my.hubkontak.utils.DBContract.TELEGRAM_CHAT_ID;

public class Telegram {

    private static final String TAG = "Telegram";
    private final Context context;

    public Telegram(Context context) {
        this.context = context;
    }
    public void send_chat_id(String chatId,String message){
        final RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        final String url = Uri.parse("https://api.telegram.org/bot" + TELEGRAM_BOT_API_KEY + "/sendmessage")
                .buildUpon()
                .appendQueryParameter("chat_id",chatId)
                .appendQueryParameter("text",message)
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG,error.getMessage());
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }
    public void send(String message){
        send_chat_id(TELEGRAM_CHAT_ID,message);
    }
}
