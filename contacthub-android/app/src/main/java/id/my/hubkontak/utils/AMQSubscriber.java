package id.my.hubkontak.utils;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import id.my.hubkontak.utils.db.ModelContactSave;
import id.my.hubkontak.utils.db.ModelContactShare;

import static id.my.hubkontak.utils.SessionManager.KEY_USER_ID;
import static id.my.hubkontak.utils.Utils.getDeviceId;
import static id.my.hubkontak.utils.Utils.isContactExist;
import static id.my.hubkontak.utils.Utils.saveLocalContact;

public class AMQSubscriber {
    static final long ONE_MINUTE_IN_MILLIS=1000;//millisecs
    private static final int TIME_TO_WAIT = 5000;
    private static final String TAG = AMQSubscriber.class.getSimpleName();
    public static Handler myHandler = new Handler();
    private HashMap<String, String> userDetail;

    public void start(Context context, String host, String exchange_name, String[] bindingKey) {


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(5672);
        Connection connection = null;
        SessionManager sessionManager = new SessionManager(context);
        userDetail = sessionManager.getUserDetails();
        sessionManager.setAmqUpdatedAt(getDate(60 * 5));
        try {
            String queueName = getDeviceId(context);
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange_name, "topic");
            channel.queueDeclare(queueName,true,false,false,null);

            for (String key : bindingKey) {
                channel.queueBind(queueName, exchange_name, key);
            }
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            Runnable timerStop = new Runnable() {
                @Override
                public void run() {
                    try {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        channel.close();
                    } catch (IOException | TimeoutException e) {
                        e.printStackTrace();
                    }
                }
            };

            myHandler.postDelayed(timerStop, TIME_TO_WAIT);
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String message = new String(delivery.getBody(), "UTF-8");
//                System.out.println(" [x] Received '" +
//                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
//                try {
//                    String[] routing_key = delivery.getEnvelope().getRoutingKey().split("_");
//                    String tipe_save = routing_key[0];
//                    String user_id = "";
//                    if (tipe_save.equals("update")){
////                        pass
//                    }else{
//                        user_id = routing_key[2];
//                    }
//
//                    JSONObject payload = new JSONObject(message);
//                    if (tipe_save.equals("save")){
//                        boolean res_insert = insert_contact_save(context,payload,user_id);
//                        if (res_insert){
//                            channel.basicAck(0,true);
//                        }
//                    }
//                    if(tipe_save.equals("share")){
//                        boolean res_share = insert_contact_share(context,payload,user_id);
//                        if (res_share){
//                            channel.basicAck(0,true);
//                        }
//                    }
//                    if(tipe_save.equals("update")){
//                        boolean res_share = update_contact(context,payload);
//                        if (res_share){
//                            channel.basicAck(0,true);
//                        }
//                    }
//                    if(!tipe_save.equals("save") && !tipe_save.equals("share") && !tipe_save.equals("update")){
//                        channel.basicNack(0,true,true);
//                        Log.e(TAG,"Nack");
//                    }
////                    if(delivery.getEnvelope().getRoutingKey().equals("save_contact_" + userDetail.get(KEY_USER_ID)) ||
////                            delivery.getEnvelope().getRoutingKey().equals("share_contact_" + userDetail.get(KEY_USER_ID))){
////
////                    }else{
////                        channel.basicNack(0,true,true);
////                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                myHandler.removeCallbacks(timerStop);
//                myHandler.postDelayed(timerStop, TIME_TO_WAIT);
//            };
            channel.basicConsume(queueName, false, queueName, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" +
                            envelope.getRoutingKey() + "':'" + message + "'");
                    System.out.println("ConsumerTag:" + consumerTag);
                    try {
                        long deliveryTag = envelope.getDeliveryTag();
                        String[] routing_key = envelope.getRoutingKey().split("_");
                        String tipe_save = routing_key[0];
                        String user_id = "";
                        if (tipe_save.equals("update")){
//                        pass
                        }else{
                            user_id = routing_key[2];
                        }

                        JSONObject payload = new JSONObject(message);
                        if (tipe_save.equals("save")){
                            boolean res_insert = insert_contact_save(context,payload,user_id);
                            if (res_insert){
                                channel.basicAck(deliveryTag,true);
                            }
                        }
                        if(tipe_save.equals("share")){
                            boolean res_share = insert_contact_share(context,payload,user_id);
                            if (res_share){
                                channel.basicAck(deliveryTag,true);
                            }
                        }
                        if(tipe_save.equals("update")){
                            boolean res_share = update_contact(context,payload);
                            if (res_share){
                                channel.basicAck(deliveryTag,true);
                            }
                        }
                        if(!tipe_save.equals("save") && !tipe_save.equals("share") && !tipe_save.equals("update")){
                            channel.basicNack(deliveryTag,true,true);
                            Log.e(TAG,"Nack");
                        }
//                    if(delivery.getEnvelope().getRoutingKey().equals("save_contact_" + userDetail.get(KEY_USER_ID)) ||
//                            delivery.getEnvelope().getRoutingKey().equals("share_contact_" + userDetail.get(KEY_USER_ID))){
//
//                    }else{
//                        channel.basicNack(0,true,true);
//                    }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    myHandler.removeCallbacks(timerStop);
                    myHandler.postDelayed(timerStop, TIME_TO_WAIT);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
    private boolean update_contact(Context context,JSONObject contact){
        boolean status = false;
        ModelContactSave modelContactSave = new ModelContactSave(context);
        ModelContactShare modelContactShare = new ModelContactShare(context);
        try {
//            cek apakah userid dan customerid sudah tersedia, jika belum maka insert
//            jika ada maka update
            if (contact.getString(modelContactSave.CityName).equals("None")) contact.put (modelContactSave.CityName,"");
            if (contact.getString(modelContactSave.CityId).equals("None"))  contact.put (modelContactSave.CityId,"");
            if (contact.getString(modelContactSave.Shopee).equals("None"))  contact.put (modelContactSave.Shopee,"");
            if (contact.getString(modelContactSave.Bukalapak).equals("None"))  contact.put (modelContactSave.Bukalapak,"");
            if (contact.getString(modelContactSave.Tokopedia).equals("None"))  contact.put (modelContactSave.Tokopedia,"");
            if (contact.getString(modelContactSave.Website).equals("None"))  contact.put (modelContactSave.Website,"");
            if (contact.getString(modelContactSave.Instagram).equals("None"))  contact.put (modelContactSave.Instagram,"");
            if (contact.getString(modelContactSave.Facebook).equals("None"))  contact.put (modelContactSave.Facebook,"");
            if (contact.getString(modelContactSave.CreatedAt).equals("None"))  contact.put (modelContactSave.CreatedAt,"");
            if (contact.getString(modelContactSave.UpdatedAt).equals("None"))  contact.put (modelContactSave.UpdatedAt,"");
            if (contact.getString(modelContactSave.DateOfBirth).equals("None"))  contact.put (modelContactSave.DateOfBirth,"");
            if (contact.getString(modelContactSave.Gender).equals("None"))  contact.put (modelContactSave.Gender,"");
            if (contact.getString(modelContactSave.WhatsApp).equals("None"))  contact.put (modelContactSave.WhatsApp,"");
            if (contact.getString(modelContactSave.Greeting).equals("None"))  contact.put (modelContactSave.Greeting,"");
            if (contact.getString(modelContactSave.Name).equals("None"))  contact.put (modelContactSave.Name,"");
            if (contact.getString(modelContactSave.CustomerId).equals("None"))  contact.put (modelContactSave.CustomerId,"");
            if (contact.getString(modelContactSave.Foto).equals("None"))  contact.put (modelContactSave.Foto,"");

            long update_data = 0;
            long update_data_2 = 0;
            try {
                Log.e(TAG,contact.getString(modelContactSave.CustomerId));
                update_data = modelContactSave.update(
                        contact.getString(modelContactSave.CustomerId),
                        contact.getString(modelContactSave.Name),
                        contact.getString(modelContactSave.Greeting),
                        contact.getString(modelContactSave.WhatsApp),
                        contact.getString(modelContactSave.Gender),
                        contact.getString(modelContactSave.DateOfBirth),
                        contact.getString(modelContactSave.UpdatedAt),
                        contact.getString(modelContactSave.CreatedAt),
                        contact.getString(modelContactSave.Facebook),
                        contact.getString(modelContactSave.Instagram),
                        contact.getString(modelContactSave.Website),
                        contact.getString(modelContactSave.Tokopedia),
                        contact.getString(modelContactSave.Bukalapak),
                        contact.getString(modelContactSave.Shopee),
                        contact.getString(modelContactSave.CityId),
                        contact.getString(modelContactSave.CityName),
                        contact.getString(modelContactSave.Foto)
                );
                update_data_2 = modelContactShare.update(
                        contact.getString(modelContactShare.CustomerId),
                        contact.getString(modelContactShare.Name),
                        contact.getString(modelContactShare.Greeting),
                        contact.getString(modelContactShare.WhatsApp),
                        contact.getString(modelContactShare.Gender),
                        contact.getString(modelContactShare.DateOfBirth),
                        contact.getString(modelContactShare.UpdatedAt),
                        contact.getString(modelContactShare.CreatedAt),
                        contact.getString(modelContactShare.Facebook),
                        contact.getString(modelContactShare.Instagram),
                        contact.getString(modelContactShare.Website),
                        contact.getString(modelContactShare.Tokopedia),
                        contact.getString(modelContactShare.Bukalapak),
                        contact.getString(modelContactShare.Shopee),
                        contact.getString(modelContactShare.CityId),
                        contact.getString(modelContactShare.CityName),
                        contact.getString(modelContactShare.Foto),
                        "",
                        contact.getString(modelContactShare.IsSaved)
                );
                if (update_data > 0 || update_data_2 > 0){
                    Log.e(TAG, "Update Successfully");
                    status = true;
                }else{
                    Log.e(TAG,String.valueOf(update_data_2));
                    Log.e(TAG,String.valueOf(update_data));
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }
    private boolean insert_contact_save(Context context,JSONObject contact,String user_id){
        boolean status = false;
        ModelContactSave modelContactSave = new ModelContactSave(context);
        try {
//            cek apakah userid dan customerid sudah tersedia, jika belum maka insert
//            jika ada maka update
            if (contact.getString(modelContactSave.CityName).equals("None")) contact.put (modelContactSave.CityName,"");
            if (contact.getString(modelContactSave.CityId).equals("None"))  contact.put (modelContactSave.CityId,"");
            if (contact.getString(modelContactSave.Shopee).equals("None"))  contact.put (modelContactSave.Shopee,"");
            if (contact.getString(modelContactSave.Bukalapak).equals("None"))  contact.put (modelContactSave.Bukalapak,"");
            if (contact.getString(modelContactSave.Tokopedia).equals("None"))  contact.put (modelContactSave.Tokopedia,"");
            if (contact.getString(modelContactSave.Website).equals("None"))  contact.put (modelContactSave.Website,"");
            if (contact.getString(modelContactSave.Instagram).equals("None"))  contact.put (modelContactSave.Instagram,"");
            if (contact.getString(modelContactSave.Facebook).equals("None"))  contact.put (modelContactSave.Facebook,"");
            if (contact.getString(modelContactSave.CreatedAt).equals("None"))  contact.put (modelContactSave.CreatedAt,"");
            if (contact.getString(modelContactSave.UpdatedAt).equals("None"))  contact.put (modelContactSave.UpdatedAt,"");
            if (contact.getString(modelContactSave.DateOfBirth).equals("None"))  contact.put (modelContactSave.DateOfBirth,"");
            if (contact.getString(modelContactSave.Gender).equals("None"))  contact.put (modelContactSave.Gender,"");
            if (contact.getString(modelContactSave.WhatsApp).equals("None"))  contact.put (modelContactSave.WhatsApp,"");
            if (contact.getString(modelContactSave.Greeting).equals("None"))  contact.put (modelContactSave.Greeting,"");
            if (contact.getString(modelContactSave.Name).equals("None"))  contact.put (modelContactSave.Name,"");
            if (contact.getString(modelContactSave.CustomerId).equals("None"))  contact.put (modelContactSave.CustomerId,"");
            if (contact.getString(modelContactSave.Foto).equals("None"))  contact.put (modelContactSave.Foto,"");
            String contactId = contact.getString(modelContactSave.ContactId);
            String userId = userDetail.get(KEY_USER_ID);
            Log.e(TAG,"insertContact:"  +  contactId + "," + user_id);
            String[] cari_contact = modelContactSave.getDataContact(contactId, userId);
            if (cari_contact.length > 0){
                Log.e(TAG,"Update Contact UserId " + userDetail.get(KEY_USER_ID));
                long update_data = modelContactSave.update(
                        contact.getString(modelContactSave.CustomerId),
                        contact.getString(modelContactSave.Name),
                        contact.getString(modelContactSave.Greeting),
                        contact.getString(modelContactSave.WhatsApp),
                        contact.getString(modelContactSave.Gender),
                        contact.getString(modelContactSave.DateOfBirth),
                        contact.getString(modelContactSave.UpdatedAt),
                        contact.getString(modelContactSave.CreatedAt),
                        contact.getString(modelContactSave.Facebook),
                        contact.getString(modelContactSave.Instagram),
                        contact.getString(modelContactSave.Website),
                        contact.getString(modelContactSave.Tokopedia),
                        contact.getString(modelContactSave.Bukalapak),
                        contact.getString(modelContactSave.Shopee),
                        contact.getString(modelContactSave.CityId),
                        contact.getString(modelContactSave.CityName),
                        contact.getString(modelContactSave.Foto)
                );
                if (update_data > 0){
                    Log.e(TAG, "Update Successfully");
                    status = true;
                }
            }else{
                Log.e(TAG,"Insert Contact UserId " + userDetail.get(KEY_USER_ID));
                long post_data = modelContactSave.insert(
                        contact.getString(modelContactSave.ContactId),
                        contact.getString(modelContactSave.CustomerId),
                        contact.getString(modelContactSave.Name),
                        contact.getString(modelContactSave.Greeting),
                        contact.getString(modelContactSave.WhatsApp),
                        contact.getString(modelContactSave.Gender),
                        contact.getString(modelContactSave.DateOfBirth),
                        user_id, // Pemilik data ini
                        contact.getString(modelContactSave.UpdatedAt),
                        contact.getString(modelContactSave.CreatedAt),
                        contact.getString(modelContactSave.Facebook),
                        contact.getString(modelContactSave.Instagram),
                        contact.getString(modelContactSave.Website),
                        contact.getString(modelContactSave.Tokopedia),
                        contact.getString(modelContactSave.Bukalapak),
                        contact.getString(modelContactSave.Shopee),
                        contact.getString(modelContactSave.CityId),
                        contact.getString(modelContactSave.CityName),
                        contact.getString(modelContactSave.Foto)
                );
                if (post_data>0){
                    boolean contact_exist = isContactExist(
                            context,
                            contact.getString(modelContactSave.WhatsApp)
                    );
                    if (contact_exist == false){
                        saveLocalContact(
                                context,
                                contact.getString(modelContactSave.Name),
                                contact.getString(modelContactSave.WhatsApp)
                        );
                    }
                    status = true;
                    Log.e(TAG, "Insert Successfully");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    private boolean insert_contact_share(Context context,JSONObject contact,String user_id){
        boolean status = false;
        ModelContactShare modelContactShare = new ModelContactShare(context);
        try {
//            cek apakah userid dan customerid sudah tersedia, jika belum maka insert
//            jika ada maka update
            if (contact.getString(modelContactShare.CityName).equals("None")) contact.put (modelContactShare.CityName,"");
            if (contact.getString(modelContactShare.CityId).equals("None"))  contact.put (modelContactShare.CityId,"");
            if (contact.getString(modelContactShare.Shopee).equals("None"))  contact.put (modelContactShare.Shopee,"");
            if (contact.getString(modelContactShare.Bukalapak).equals("None"))  contact.put (modelContactShare.Bukalapak,"");
            if (contact.getString(modelContactShare.Tokopedia).equals("None"))  contact.put (modelContactShare.Tokopedia,"");
            if (contact.getString(modelContactShare.Website).equals("None"))  contact.put (modelContactShare.Website,"");
            if (contact.getString(modelContactShare.Instagram).equals("None"))  contact.put (modelContactShare.Instagram,"");
            if (contact.getString(modelContactShare.Facebook).equals("None"))  contact.put (modelContactShare.Facebook,"");
            if (contact.getString(modelContactShare.CreatedAt).equals("None"))  contact.put (modelContactShare.CreatedAt,"");
            if (contact.getString(modelContactShare.UpdatedAt).equals("None"))  contact.put (modelContactShare.UpdatedAt,"");
            if (contact.getString(modelContactShare.DateOfBirth).equals("None"))  contact.put (modelContactShare.DateOfBirth,"");
            if (contact.getString(modelContactShare.Gender).equals("None"))  contact.put (modelContactShare.Gender,"");
            if (contact.getString(modelContactShare.WhatsApp).equals("None"))  contact.put (modelContactShare.WhatsApp,"");
            if (contact.getString(modelContactShare.Greeting).equals("None"))  contact.put (modelContactShare.Greeting,"");
            if (contact.getString(modelContactShare.Name).equals("None"))  contact.put (modelContactShare.Name,"");
            if (contact.getString(modelContactShare.CustomerId).equals("None"))  contact.put (modelContactShare.CustomerId,"");
            if (contact.getString(modelContactShare.Foto).equals("None"))  contact.put (modelContactShare.Foto,"");
            String[] cari_contact = modelContactShare.getDataContact(contact.getString(modelContactShare.ContactId), userDetail.get(KEY_USER_ID));
            if (cari_contact.length > 0){
                long update_data = modelContactShare.update(
                        contact.getString(modelContactShare.CustomerId),
                        contact.getString(modelContactShare.Name),
                        contact.getString(modelContactShare.Greeting),
                        contact.getString(modelContactShare.WhatsApp),
                        contact.getString(modelContactShare.Gender),
                        contact.getString(modelContactShare.DateOfBirth),
                        contact.getString(modelContactShare.UpdatedAt),
                        contact.getString(modelContactShare.CreatedAt),
                        contact.getString(modelContactShare.Facebook),
                        contact.getString(modelContactShare.Instagram),
                        contact.getString(modelContactShare.Website),
                        contact.getString(modelContactShare.Tokopedia),
                        contact.getString(modelContactShare.Bukalapak),
                        contact.getString(modelContactShare.Shopee),
                        contact.getString(modelContactShare.CityId),
                        contact.getString(modelContactShare.CityName),
                        contact.getString(modelContactShare.Foto),
                        contact.getString(modelContactShare.ContactId),
                        contact.getString(modelContactShare.IsSaved)
                );
                if(update_data > 0){
                    status = true;
                }else{
                    status = false;
                }
            }else{
                long post_data = modelContactShare.insert(
                        contact.getString(modelContactShare.ContactId),
                        contact.getString(modelContactShare.CustomerId),
                        contact.getString(modelContactShare.Name),
                        contact.getString(modelContactShare.Greeting),
                        contact.getString(modelContactShare.WhatsApp),
                        contact.getString(modelContactShare.Gender),
                        contact.getString(modelContactShare.DateOfBirth),
                        user_id, // Pemilik data ini
                        contact.getString(modelContactShare.UpdatedAt),
                        contact.getString(modelContactShare.CreatedAt),
                        contact.getString(modelContactShare.Facebook),
                        contact.getString(modelContactShare.Instagram),
                        contact.getString(modelContactShare.Website),
                        contact.getString(modelContactShare.Tokopedia),
                        contact.getString(modelContactShare.Bukalapak),
                        contact.getString(modelContactShare.Shopee),
                        contact.getString(modelContactShare.CityId),
                        contact.getString(modelContactShare.CityName),
                        contact.getString(modelContactShare.Foto),
                        contact.getString(modelContactShare.IsSaved)
                );
                if (post_data>0){
                    boolean contact_exist = isContactExist(
                            context,
                            contact.getString(modelContactShare.WhatsApp)
                    );
                    if (contact_exist == false){
                        saveLocalContact(
                                context,
                                contact.getString(modelContactShare.Name),
                                contact.getString(modelContactShare.WhatsApp)
                        );
                    }
                    status = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    private String getDate(int timedeltaSeconds){

        Calendar date = Calendar.getInstance();
        long t = date.getTimeInMillis();
        Date afterAddingTenMins = new Date(t + (timedeltaSeconds * ONE_MINUTE_IN_MILLIS));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(afterAddingTenMins);
        return strDate;
    }
}
