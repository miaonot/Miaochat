package com.miaonot.www.miaochat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by miaonot on 16-5-14.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String CREATE_FRIEND = "CREATE TABLE friend (" +
            "user_id TEXT," +
            "id TEXT," +
            "nickname TEXT)";

    public static final String CREATE_USER = "CREATE TABLE user (" +
            "is_auto_sign_in INTEGER," +
            "user_id TEXT," +
            "password TEXT)";

    public static final String CREATE_INFORMATION = "CREATE TABLE information (" +
            "id TEXT," +
            "inf_from TEXT," +
            "inf_to TEXT," +
            "time DATETIME," +
            "content TEXT)";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FRIEND);
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_INFORMATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
