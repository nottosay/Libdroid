package com.util.libdroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.util.libdroid.db.Cache;
import com.util.libdroid.db.Configuration;
import com.util.libdroid.db.Model;
import com.util.libdroid.db.TableInfo;
import com.util.libdroid.db.query.Delete;
import com.util.libdroid.db.query.Select;

import java.util.List;

/**
 * Created by wally.yan on 2014/11/5.
 */
public class DBManager {

    public static void initialize(Context context) {
        initialize(new Configuration.Builder(context).create());
    }

    public static void initialize(Configuration configuration) {
        initialize(configuration, false);
    }

    public static void initialize(Context context, boolean loggingEnabled) {
        initialize(new Configuration.Builder(context).create(), loggingEnabled);
    }

    public static void initialize(Configuration configuration, boolean loggingEnabled) {
        Cache.initialize(configuration);
    }

    public static void clearCache() {
        Cache.clear();
    }

    public static void dispose() {
        Cache.dispose();
    }

    public static SQLiteDatabase getDatabase() {
        return Cache.openDatabase();
    }

    public static void beginTransaction() {
        Cache.openDatabase().beginTransaction();
    }

    public static void endTransaction() {
        Cache.openDatabase().endTransaction();
    }

    public static void setTransactionSuccessful() {
        Cache.openDatabase().setTransactionSuccessful();
    }

    public static boolean inTransaction() {
        return Cache.openDatabase().inTransaction();
    }

    public static void execSQL(String sql) {
        Cache.openDatabase().execSQL(sql);
    }

    public static void execSQL(String sql, Object[] bindArgs) {
        Cache.openDatabase().execSQL(sql, bindArgs);
    }

    // Convenience methods
    public static void delete(Class<? extends Model> type, long id) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        new Delete().from(type).where(tableInfo.getIdName()+"=?", id).execute();
    }

    public static <T extends Model> T load(Class<T> type, long id) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return (T) new Select().from(type).where(tableInfo.getIdName()+"=?", id).executeSingle();
    }

    public static <T extends Model> List<T> loadAll(Class<T> type) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return new Select().from(type).execute();
    }

    public static <T extends Model> List<T> loadAll(Class<T> type,String whereStr,Object... mWhereArguments) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return new Select().from(type).where(whereStr,mWhereArguments).execute();
    }
}
