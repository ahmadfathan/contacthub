package id.my.hubkontak.utils.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ModelContactSave {
    private static final String TAG = ModelContactSave.class.getSimpleName();
    private DBAdapter dbAdapter;
    private Context context;
    private static final String TABLE_NAME = "tb_contact_save";

    public static final String ContactId = "ContactId";
    public static final String CustomerId = "CustomerId";
    public static final String Name = "Name";
    public static final String Greeting = "Greeting";
    public static final String WhatsApp = "WhatsApp";
    public static final String Gender = "Gender";
    public static final String DateOfBirth = "DateOfBirth";
    public static final String UserId = "UserId";
    public static final String UpdatedAt = "UpdatedAt";
    public static final String CreatedAt = "CreatedAt";
    public static final String Facebook = "Facebook";
    public static final String Instagram = "Instagram";
    public static final String Website = "Website";
    public static final String Tokopedia = "Tokopedia";
    public static final String Bukalapak = "Bukalapak";
    public static final String Shopee = "Shopee";
    public static final String CityId = "CityId";
    public static final String CityName = "CityName";
    public static final String Foto = "Foto";


    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + ContactId + " VARCHAR(255),"
                    + CustomerId + " VARCHAR(255),"
                    + Name + " TEXT,"
                    + Greeting + " TEXT,"
                    + WhatsApp + " TEXT,"
                    + Gender + " TEXT,"
                    + DateOfBirth + " TEXT,"
                    + UserId + " TEXT,"
                    + Facebook + " TEXT,"
                    + Instagram + " TEXT,"
                    + Website + " TEXT,"
                    + Tokopedia + " TEXT,"
                    + Bukalapak + " TEXT,"
                    + Shopee + " TEXT,"
                    + CityId + " TEXT,"
                    + CityName + " TEXT,"
                    + UpdatedAt + " TEXT,"
                    + CreatedAt + " TEXT,"
                    + Foto + " TEXT"
                    + ")";

    public ModelContactSave(Context context) {
        dbAdapter = new DBAdapter(context);
        this.context = context;
    }
    public long insert(String contactId,String customerId,String name,String greeting,String whatsApp,String gender,String dateOfBirth,String userId,String updatedAt,String createdAt,
                       String facebook,String instagram,String website,String tokopedia,String bukalapak,String shopee,String cityId,String cityName,String foto){
        SQLiteDatabase dbb = dbAdapter.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(getContactId(), contactId);
        contentValues.put(getCustomerId(), customerId);
        contentValues.put(getName(), name);
        contentValues.put(getGreeting(), greeting);
        contentValues.put(getWhatsApp(), whatsApp);
        contentValues.put(getGender(), gender);
        contentValues.put(getDateOfBirth(), dateOfBirth);
        contentValues.put(getUserId(), userId);
        contentValues.put(getUpdatedAt(), updatedAt);
        contentValues.put(getCreatedAt(), createdAt);
        contentValues.put(getFacebook(), facebook);
        contentValues.put(getInstagram(), instagram);
        contentValues.put(getWebsite(), website);
        contentValues.put(getTokopedia(), tokopedia);
        contentValues.put(getBukalapak(), bukalapak);
        contentValues.put(getShopee(), shopee);
        contentValues.put(getCityId(), cityId);
        contentValues.put(getCityName(), cityName);
        contentValues.put(getFoto(), foto);

        long id = dbb.insert(getTableName(), null , contentValues);
        return id;
    }
    public int delete(String contactId,String userId){
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        String[] whereArgs ={contactId,userId};

        int count =db.delete(getTableName() ,getContactId()+" = ? " + getUserId() + " = ? ",whereArgs);
        return  count;
    }
    public List<String[]> getPaginate(int page, int numberPage, String search,String userId)
    {
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        int offset = (numberPage * page) - numberPage;
        String raw_query = "SELECT * FROM " + getTableName() + " WHERE "+ getUserId()+" = ? LIMIT ? OFFSET ? GROUP BY " + getCustomerId();
        String where_args[] = new String[]{userId,String.valueOf(numberPage), String.valueOf(offset)};
        if (search.isEmpty() == false){
            raw_query = "SELECT * FROM " + getTableName() + " WHERE  "+ getUserId() +"= ? AND ? LIKE '%" + search + "%' OR ? LIKE '%" + search + "%' LIMIT ? OFFSET ? ";
            where_args = new String[]{userId,getName(), getWhatsApp(), String.valueOf(numberPage), String.valueOf(offset)};
        }
        Cursor cursor =db.rawQuery(raw_query, where_args);
        List<String[]> list = new ArrayList<>();
        while (cursor.moveToNext())
        {
            String contactId = cursor.getString(cursor.getColumnIndex(getContactId()));
            String customerId = cursor.getString(cursor.getColumnIndex(getCustomerId()));
            String name = cursor.getString(cursor.getColumnIndex(getName()));
            String greeting = cursor.getString(cursor.getColumnIndex(getGreeting()));
            String whatsApp = cursor.getString(cursor.getColumnIndex(getWhatsApp()));
            String gender = cursor.getString(cursor.getColumnIndex(getGender()));
            String dateOfBirth = cursor.getString(cursor.getColumnIndex(getDateOfBirth()));
            String user_Id = cursor.getString(cursor.getColumnIndex(getUserId()));
            String facebook = cursor.getString(cursor.getColumnIndex(getFacebook()));
            String instagram = cursor.getString(cursor.getColumnIndex(getInstagram()));
            String website = cursor.getString(cursor.getColumnIndex(getWebsite()));
            String tokopedia = cursor.getString(cursor.getColumnIndex(getTokopedia()));
            String bukalapak = cursor.getString(cursor.getColumnIndex(getBukalapak()));
            String shopee = cursor.getString(cursor.getColumnIndex(getShopee()));
            String city_id = cursor.getString(cursor.getColumnIndex(getCityId()));
            String city_name = cursor.getString(cursor.getColumnIndex(getCityName()));
            String foto = cursor.getString(cursor.getColumnIndex(getFoto()));
            list.add(new String[]{
                    contactId,
                    customerId,
                    name,
                    greeting,
                    whatsApp,
                    gender,
                    dateOfBirth,
                    user_Id,
                    facebook,
                    instagram,
                    website,
                    tokopedia,
                    bukalapak,
                    shopee,
                    city_id,
                    city_name,
                    foto
            });
        }
        return list;
    }
    public List<String[]> getAll(String userId)
    {
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        String raw_query = "SELECT * FROM " + getTableName() + " WHERE " + getUserId() + " = ? GROUP BY " + getCustomerId();
        Cursor cursor =db.rawQuery(raw_query,new String[]{userId});
        List<String[]> list = new ArrayList<>();
        while (cursor.moveToNext())
        {
            String contactId = cursor.getString(cursor.getColumnIndex(getContactId()));
            String customerId = cursor.getString(cursor.getColumnIndex(getCustomerId()));
            String name = cursor.getString(cursor.getColumnIndex(getName()));
            String greeting = cursor.getString(cursor.getColumnIndex(getGreeting()));
            String whatsApp = cursor.getString(cursor.getColumnIndex(getWhatsApp()));
            String gender = cursor.getString(cursor.getColumnIndex(getGender()));
            String dateOfBirth = cursor.getString(cursor.getColumnIndex(getDateOfBirth()));
            String user_Id = cursor.getString(cursor.getColumnIndex(getUserId()));
            String facebook = cursor.getString(cursor.getColumnIndex(getFacebook()));
            String instagram = cursor.getString(cursor.getColumnIndex(getInstagram()));
            String website = cursor.getString(cursor.getColumnIndex(getWebsite()));
            String tokopedia = cursor.getString(cursor.getColumnIndex(getTokopedia()));
            String bukalapak = cursor.getString(cursor.getColumnIndex(getBukalapak()));
            String shopee = cursor.getString(cursor.getColumnIndex(getShopee()));
            String city_id = cursor.getString(cursor.getColumnIndex(getCityId()));
            String city_name = cursor.getString(cursor.getColumnIndex(getCityName()));
            String foto = cursor.getString(cursor.getColumnIndex(getFoto()));
            list.add(new String[]{contactId,customerId,name,greeting,whatsApp,gender,dateOfBirth,user_Id,facebook,instagram,website,tokopedia,bukalapak,shopee,city_id,city_name,foto});
        }
        return list;
    }
    public boolean checkIsSaved(String customerId, String userId){
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        String raw_query = "SELECT * FROM " + getTableName() + " WHERE " + getUserId() + "='" + userId + "'  AND " + getCustomerId() + " = '" + customerId  + "' LIMIT 1";
        String where_args[] = new String[]{};
        Cursor cursor =db.rawQuery(raw_query, where_args);
        if (cursor.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }
    public String[] getDataContact(String contact_id,String userId){
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        String raw_query = "SELECT * FROM " + getTableName() + " WHERE " + getUserId() + "='" + userId + "'  AND " + getContactId() + " = '" + contact_id  + "' LIMIT 1";
        String where_args[] = new String[]{};
        Cursor cursor =db.rawQuery(raw_query, where_args);
        String[] result = new String[]{};
        Log.e(TAG,"getDataContact : " + contact_id + "," + userId + "," + cursor.getCount());
        while (cursor.moveToNext())
        {
            String contactId = cursor.getString(cursor.getColumnIndex(getContactId()));
            String customerId = cursor.getString(cursor.getColumnIndex(getCustomerId()));
            String name = cursor.getString(cursor.getColumnIndex(getName()));
            String greeting = cursor.getString(cursor.getColumnIndex(getGreeting()));
            String whatsApp = cursor.getString(cursor.getColumnIndex(getWhatsApp()));
            String gender = cursor.getString(cursor.getColumnIndex(getGender()));
            String dateOfBirth = cursor.getString(cursor.getColumnIndex(getDateOfBirth()));
            String user_jd = cursor.getString(cursor.getColumnIndex(getUserId()));
            String facebook = cursor.getString(cursor.getColumnIndex(getFacebook()));
            String instagram = cursor.getString(cursor.getColumnIndex(getInstagram()));
            String website = cursor.getString(cursor.getColumnIndex(getWebsite()));
            String tokopedia = cursor.getString(cursor.getColumnIndex(getTokopedia()));
            String bukalapak = cursor.getString(cursor.getColumnIndex(getBukalapak()));
            String shopee = cursor.getString(cursor.getColumnIndex(getShopee()));
            String city_id = cursor.getString(cursor.getColumnIndex(getCityId()));
            String city_name = cursor.getString(cursor.getColumnIndex(getCityName()));
            String foto = cursor.getString(cursor.getColumnIndex(getFoto()));
            result = new String[]{contactId,customerId,name,greeting,whatsApp,gender,dateOfBirth,user_jd,facebook,instagram,website,tokopedia,bukalapak,shopee,city_id,city_name,foto};
        }
        return result;
    }
    public long update(String customerId,String name,String greeting,String whatsApp,String gender,String dateOfBirth,
                       String updatedAt,String createdAt,String facebook,String instagram,String website,String tokopedia,
                       String bukalapak,String shopee,String cityId,String cityName,String foto){
        SQLiteDatabase dbb = dbAdapter.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(getCustomerId(), customerId);
        contentValues.put(getName(), name);
        contentValues.put(getGreeting(), greeting);
        contentValues.put(getWhatsApp(), whatsApp);
        contentValues.put(getGender(), gender);
        contentValues.put(getDateOfBirth(), dateOfBirth);
        contentValues.put(getUpdatedAt(), updatedAt);
        contentValues.put(getCreatedAt(), createdAt);
        contentValues.put(getFacebook(), facebook);
        contentValues.put(getInstagram(), instagram);
        contentValues.put(getWebsite(), website);
        contentValues.put(getTokopedia(), tokopedia);
        contentValues.put(getBukalapak(), bukalapak);
        contentValues.put(getShopee(), shopee);
        contentValues.put(getCityId(), cityId);
        contentValues.put(getCityName(), cityName);
        contentValues.put(getFoto(), foto);

        long id = dbb.update(getTableName() , contentValues,"CustomerId=?",new String[]{customerId});

        return id;
    }

    public int deleteAll() {
        SQLiteDatabase db = dbAdapter.getWritableDatabase();
        String[] whereArgs ={};

        int count = db.delete(getTableName() ,null,whereArgs);
        return count;
    }
    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getCustomerId() {
        return CustomerId;
    }

    public static String getName() {
        return Name;
    }

    public static String getGreeting() {
        return Greeting;
    }

    public static String getWhatsApp() {
        return WhatsApp;
    }

    public static String getGender() {
        return Gender;
    }

    public static String getDateOfBirth() {
        return DateOfBirth;
    }

    public static String getUserId() {
        return UserId;
    }

    public static String getUpdatedAt() {
        return UpdatedAt;
    }

    public static String getCreatedAt() {
        return CreatedAt;
    }

    public static String getCreateTable() {
        return CREATE_TABLE;
    }

    public static String getContactId() {
        return ContactId;
    }

    public static String getFacebook() {
        return Facebook;
    }

    public static String getInstagram() {
        return Instagram;
    }

    public static String getWebsite() {
        return Website;
    }

    public static String getTokopedia() {
        return Tokopedia;
    }

    public static String getBukalapak() {
        return Bukalapak;
    }

    public static String getShopee() {
        return Shopee;
    }

    public static String getCityId() {
        return CityId;
    }

    public static String getCityName() {
        return CityName;
    }

    public static String getFoto() {
        return Foto;
    }
}
