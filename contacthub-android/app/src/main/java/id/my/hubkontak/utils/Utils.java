package id.my.hubkontak.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.my.hubkontak.R;

public class Utils {

    private static final String TAG = "Utils";

    public static void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    public static String[] toStringArray(JSONArray array) {
        if(array==null)
            return null;

        String[] arr=new String[array.length()];
        for(int i=0; i<arr.length; i++) {
            arr[i]=array.optString(i);
        }
        return arr;
    }

    public static List<String> toListArray(JSONArray array) {
        if(array==null)
            return null;
        List<String> arr = new ArrayList<>();
        for(int i=0; i<array.length(); i++) {
            arr.add(array.optString(i));
        }
        return arr;
    }

    public static String formatRupiah(double nominal){
        Locale ID = new Locale("in", "ID");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(ID);
        return numberFormat.format((double)nominal);
    }

    public static void hideKeyboard(Context context, View view){
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String currencyID(double nominal){
        Locale locale = new Locale("in", "ID");
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(locale);
        return rupiah.format((double)nominal);
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    public static int pxToDp(int px, Context context) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static void errorResponse(final Context context, VolleyError error){

        if (error instanceof NoConnectionError){
            Toast.makeText(context, R.string.toast_no_internet, Toast.LENGTH_SHORT).show();
        }else if(error instanceof TimeoutError){
            Toast.makeText(context, R.string.toast_timeout, Toast.LENGTH_SHORT).show();
        }else if(error instanceof ServerError){
            Toast.makeText(context, R.string.toast_server_error , Toast.LENGTH_SHORT).show();
        }else if(error instanceof ParseError){
            Toast.makeText(context, R.string.toast_terjadi_kesalahan , Toast.LENGTH_SHORT).show();
        }else if(error instanceof AuthFailureError){
            Toast.makeText(context, R.string.toast_terjadi_kesalahan , Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, R.string.toast_terjadi_kesalahan , Toast.LENGTH_SHORT).show();
        }
    }

    public static int errorResponse(VolleyError error){
        if (error instanceof NoConnectionError){
            return R.string.toast_no_internet;
        }else if (error instanceof TimeoutError){
            return R.string.toast_timeout;
        }else if (error instanceof ServerError){
            return R.string.toast_server_error;
        }else if (error instanceof ParseError){
            return R.string.toast_terjadi_kesalahan;
        }else if (error instanceof AuthFailureError){
            return R.string.toast_terjadi_kesalahan;
        }else {
            return R.string.toast_terjadi_kesalahan;
        }
    }
    public static String getAddress(Context context, double lat, double lng) {
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            address = addresses.get(0).getAddressLine(0);

            Log.v("LOCATION", address);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ADDRESS", e.getMessage());
            address = "Gagal Memuat Lokasi";
        }
        String[] split = address.split(",");
        return split[0];
    }

    public static String getAddressFull(Context context, double lat, double lng) {
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            address = addresses.get(0).getAddressLine(0);

            Log.v("ADDRESSFULL", address);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ADDRESSFULL", e.getMessage());
            address = "Gagal Memuat Lokasi";
        }
        return address;
    }

    public static String codeDays(String code){
        switch (code){
            case "0":
                return "Minggu";
            case "1":
                return "Senin";
            case "2":
                return "Selasa";
            case "3":
                return "Rabu";
            case "4":
                return "Kamis";
            case "5":
                return "Jumat";
            case "6":
                return "Sabtu";
            default:
                return null;
        }
    }
    public static String ConvertBitmapToString(Bitmap bitmap){
        String encodedImage = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        encodedImage= "data:image/png;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
        encodedImage= Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
        return encodedImage;
    }
    public static String getFileExtension(File file){
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        }catch (Exception e){
            return "";
        }
    }
    public static void openAppPlaystore(Context context) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id="+appId));
            context.startActivity(webIntent);
        }
    }
    public static boolean isContactExist(Context context, String number) {
/// number is the phone number
        try {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
                Uri lookupUri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(number));
                String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
                Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, selection, null, null);
                try {
                    if (cur.moveToFirst()) {
                        Log.i(TAG,"DisplayName : " + cur.getColumnName(2));
                        return true;
                    }
                }
                finally {
                    if (cur != null)
                        cur.close();
                }
            }

        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return false;
    }
    public static boolean saveLocalContact(Context context,String name, String number) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        try
        {
            ContentProviderResult[] res = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        }
        catch (RemoteException e)
        {
            Log.e(TAG,e.getMessage());
        }
        catch (OperationApplicationException e)
        {
            Log.e(TAG,e.getMessage());
        }
        return false;
    }
    public static String getDeviceId(Context context){
        String androidId = Settings.Secure.getString (context.getContentResolver (),
                Settings.Secure.ANDROID_ID);
        return androidId;
    }
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, packageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
