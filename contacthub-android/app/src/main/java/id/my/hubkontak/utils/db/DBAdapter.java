package id.my.hubkontak.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import id.my.hubkontak.utils.Telegram;

public class DBAdapter extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 13;
    private static final String DATABASE_NAME = "db_contact_hub";
    private Telegram telegram;
    public DBAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        telegram = new Telegram(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ModelContactSave.getCreateTable());
            db.execSQL(ModelContactShare.getCreateTable());
        } catch (Exception e) {
            telegram.send(getClass().getSimpleName() + "::" + getClass().getEnclosingMethod() + " : " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ModelContactSave.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + ModelContactShare.getTableName());
        onCreate(db);
    }
}
