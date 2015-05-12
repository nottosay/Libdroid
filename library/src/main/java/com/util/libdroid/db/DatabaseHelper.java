package com.util.libdroid.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.util.libdroid.utils.IOUtils;
import com.util.libdroid.utils.SQLiteUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public final class DatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = "DatabaseHelper";

    private final String mSqlFilePath;

    private Configuration configuration;

    public DatabaseHelper(Configuration configuration) {
        super(configuration.getContext(), configuration.getDatabaseName(), null, configuration.getDatabaseVersion());
        this.configuration = configuration;
        mSqlFilePath = configuration.getSqlFilePath();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        executePragmas(db);
    }

    ;

    @Override
    public void onCreate(SQLiteDatabase db) {
        executePragmas(db);
        executeCreate(db);
        executeSqlFiles(db, -1, db.getVersion());
        executeCreateIndex(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        executePragmas(db);
        executeCreate(db);
        executeSqlFiles(db, oldVersion, newVersion);
    }


    private void executePragmas(SQLiteDatabase db) {
        if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.i(TAG, "Foreign Keys supported. Enabling foreign key features.");
        }
    }

    private void executeCreateIndex(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (TableInfo tableInfo : Cache.getTableInfos()) {
                String[] definitions = SQLiteUtils.createIndexDefinition(tableInfo);

                for (String definition : definitions) {
                    db.execSQL(definition);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void executeCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (TableInfo tableInfo : Cache.getTableInfos()) {
                db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private boolean executeSqlFiles(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean migrationExecuted = false;
        try {
            final List<String> files = Arrays.asList(Cache.getContext().getAssets().list(mSqlFilePath));

            db.beginTransaction();
            try {
                for (String file : files) {
                    try {
                        final int version = Integer.valueOf(file.replace(".sql", ""));

                        if (version > oldVersion && version <= newVersion) {
                            executeSqlScript(db, file);
                            migrationExecuted = true;

                            Log.i(TAG, file + " executed succesfully.");
                        }
                    } catch (NumberFormatException e) {
                        Log.w("Skipping invalidly named file: " + file, e);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to execute migrations.", e);
        }

        return migrationExecuted;
    }

    private void executeSqlScript(SQLiteDatabase db, String file) {

        InputStream stream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            stream = Cache.getContext().getAssets().open(mSqlFilePath + "/" + file);

            inputStreamReader = new InputStreamReader(stream);

            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            if (!"".equals(sb.toString())) {
                String[] sqls = sb.toString().split(";");
                for (String sql : sqls) {
                    db.execSQL(sql);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to execute " + file, e);

        } finally {
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(stream);
        }
    }


}
