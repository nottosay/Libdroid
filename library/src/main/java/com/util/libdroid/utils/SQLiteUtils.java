package com.util.libdroid.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;

import com.util.libdroid.db.Cache;
import com.util.libdroid.db.Model;
import com.util.libdroid.db.TableInfo;
import com.util.libdroid.db.annotation.Column;
import com.util.libdroid.db.query.Delete;
import com.util.libdroid.db.query.Select;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SQLiteUtils {

    public enum SQLiteType {
        INTEGER, REAL, TEXT, BLOB
    }


    public static final boolean FOREIGN_KEYS_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;

    @SuppressWarnings("serial")
    private static final HashMap<Class<?>, SQLiteType> TYPE_MAP = new HashMap<Class<?>, SQLiteType>() {
        {
            put(byte.class, SQLiteType.INTEGER);
            put(short.class, SQLiteType.INTEGER);
            put(int.class, SQLiteType.INTEGER);
            put(long.class, SQLiteType.INTEGER);
            put(float.class, SQLiteType.REAL);
            put(double.class, SQLiteType.REAL);
            put(boolean.class, SQLiteType.INTEGER);
            put(char.class, SQLiteType.TEXT);
            put(byte[].class, SQLiteType.BLOB);
            put(Byte.class, SQLiteType.INTEGER);
            put(Short.class, SQLiteType.INTEGER);
            put(Integer.class, SQLiteType.INTEGER);
            put(Long.class, SQLiteType.INTEGER);
            put(Float.class, SQLiteType.REAL);
            put(Double.class, SQLiteType.REAL);
            put(Boolean.class, SQLiteType.INTEGER);
            put(Character.class, SQLiteType.TEXT);
            put(String.class, SQLiteType.TEXT);
            put(Byte[].class, SQLiteType.BLOB);
        }
    };

    private static HashMap<String, List<String>> sIndexGroupMap;


    public static String[] createIndexDefinition(TableInfo tableInfo) {
        final ArrayList<String> definitions = new ArrayList<String>();
        sIndexGroupMap = new HashMap<String, List<String>>();

        for (Field field : tableInfo.getFields()) {
            createIndexColumnDefinition(tableInfo, field);
        }

        if (sIndexGroupMap.isEmpty()) {
            return new String[0];
        }

        for (Map.Entry<String, List<String>> entry : sIndexGroupMap.entrySet()) {
            definitions.add(String.format("CREATE INDEX IF NOT EXISTS %s on %s(%s);",
                    "index_" + entry.getKey(),
                    tableInfo.getTableName(), TextUtils.join(", ", entry.getValue())));
        }

        return definitions.toArray(new String[definitions.size()]);
    }

    public static void createIndexColumnDefinition(TableInfo tableInfo, Field field) {
        final String name = tableInfo.getColumnName(field);
        final Column column = field.getAnnotation(Column.class);

        if (field.getName().equals(tableInfo.getIdName())) {
            return;
        }

        if (column.index()) {
            List<String> list = new ArrayList<String>();
            list.add(name);
            sIndexGroupMap.put(tableInfo.getTableName() + "_" + name, list);
        }

        if (column.indexGroups()) {
            List<String> list = sIndexGroupMap.get(tableInfo.getTableName());
            if (list == null) {
                list = new ArrayList<String>();
            }
            list.add(name);
            sIndexGroupMap.put(tableInfo.getTableName(), list);
        }
    }

    public static String createTableDefinition(TableInfo tableInfo) {
        final ArrayList<String> definitions = new ArrayList<String>();

        for (Field field : tableInfo.getFields()) {
            String definition = createColumnDefinition(tableInfo, field);
            if (!TextUtils.isEmpty(definition)) {
                definitions.add(definition);
            }
        }

        return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableInfo.getTableName(),
                TextUtils.join(", ", definitions));
    }

    public static String createColumnDefinition(TableInfo tableInfo, Field field) {
        StringBuilder definition = new StringBuilder();

        Class<?> type = field.getType();
        final String name = tableInfo.getColumnName(field);
        final Column column = field.getAnnotation(Column.class);

        if (TYPE_MAP.containsKey(type)) {
            definition.append(name);
            definition.append(" ");
            definition.append(TYPE_MAP.get(type).toString());
        } else if (ReflectionUtils.isModel(type)) {
            definition.append(name);
            definition.append(" ");
            definition.append(SQLiteType.INTEGER.toString());
        } else if (ReflectionUtils.isSubclassOf(type, Enum.class)) {
            definition.append(name);
            definition.append(" ");
            definition.append(SQLiteType.TEXT.toString());
        }

        if (!TextUtils.isEmpty(definition)) {
            if (name.equals(tableInfo.getIdName())) {
                definition.append(" PRIMARY KEY AUTOINCREMENT");
            } else if (column != null) {
                if (column.length() > -1) {
                    definition.append("(");
                    definition.append(column.length());
                    definition.append(")");
                }

                if (column.notNull()) {
                    definition.append(" NOT NULL ON CONFLICT ");
                    definition.append(column.onNullConflict().toString());
                }

                if (column.unique()) {
                    definition.append(" UNIQUE ON CONFLICT ");
                    definition.append(column.onUniqueConflict().toString());
                }
            }

            if (FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModel(type)) {
                definition.append(" REFERENCES ");
                definition.append(Cache.getTableInfo((Class<? extends Model>) type).getTableName());
                definition.append("(" + tableInfo.getIdName() + ")");
                definition.append(" ON DELETE ");
                definition.append(column.onDelete().toString().replace("_", " "));
                definition.append(" ON UPDATE ");
                definition.append(column.onUpdate().toString().replace("_", " "));
            }
        } else {
            LogUtils.e("No type mapping for: " + type.toString());
        }

        return definition.toString();
    }

    public static <T extends Model> List<T> processCursor(Class<? extends Model> type, Cursor cursor) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        String idName = tableInfo.getIdName();
        final List<T> entities = new ArrayList<T>();

        try {
            Constructor<?> entityConstructor = type.getConstructor();

            if (cursor.moveToFirst()) {
                List<String> columnsOrdered = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
                do {
                    Model entity = Cache.getEntity(type, cursor.getLong(columnsOrdered.indexOf(idName)));
                    if (entity == null) {
                        entity = (T) entityConstructor.newInstance();
                    }

                    entity.loadFromCursor(cursor);
                    entities.add((T) entity);
                }
                while (cursor.moveToNext());
            }

        }catch (Exception e) {
            LogUtils.e("Failed to process cursor.", e);
        }

        return entities;
    }

    private static int processIntCursor(final Cursor cursor) {
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        return 0;
    }

    public static <T extends Model> List<T> rawQuery(Class<? extends Model> type, String sql, String[] selectionArgs) {
        Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
        List<T> entities = processCursor(type, cursor);
        cursor.close();

        return entities;
    }

    public static int intQuery(final String sql, final String[] selectionArgs) {
        final Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
        final int number = processIntCursor(cursor);
        cursor.close();

        return number;
    }

    public static <T extends Model> T rawQuerySingle(Class<? extends Model> type, String sql, String[] selectionArgs) {
        List<T> entities = rawQuery(type, sql, selectionArgs);

        if (entities.size() > 0) {
            return entities.get(0);
        }

        return null;
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
        new Delete().from(type).where(tableInfo.getIdName() + "=?", id).execute();
    }

    public static void delete(Class<? extends Model> type, String whereStr, Object... mWhereArguments) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        new Delete().from(type).where(whereStr, mWhereArguments).execute();
    }

    public static <T extends Model> T load(Class<T> type, long id) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return (T) new Select().from(type).where(tableInfo.getIdName() + "=?", id).executeSingle();
    }

    public static <T extends Model> List<T> loadAll(Class<T> type) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return new Select().from(type).execute();
    }

    public static <T extends Model> List<T> loadAll(Class<T> type, String whereStr, Object... mWhereArguments) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return new Select().from(type).where(whereStr, mWhereArguments).execute();
    }
}
