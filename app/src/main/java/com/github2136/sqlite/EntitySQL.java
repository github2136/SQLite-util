package com.github2136.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.github2136.sqliteutil.BaseSQLData;

/**
 * Created by yubin on 2017/8/22.
 */

public class EntitySQL extends BaseSQLData<Entity> {
    private static volatile EntitySQL instance;

    public static EntitySQL getInstance(Context context) {
        if (instance == null) {
            synchronized (EntitySQL.class) {
                if (instance == null) {
                    instance = new EntitySQL(context);
                }
            }
        }
        return instance;
    }

    private EntitySQL(Context context) {
        super(context);
    }

    @Override
    protected SQLiteOpenHelper getSQLHelper(Context context) {
        return new DBHelper(context);
    }
}
