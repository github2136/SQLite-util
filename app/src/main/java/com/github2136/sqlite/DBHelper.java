package com.github2136.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github2136.sqliteutil.TableUtil;

/**
 * Created by yubin on 2017/7/20.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "sql.db";
    private static final int DB_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TableUtil.getCreateSQL(Entity.class));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TableUtil.getUpdateSQL(Entity.class, oldVersion, newVersion));
    }
}
