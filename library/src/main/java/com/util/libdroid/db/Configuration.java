package com.util.libdroid.db;


import android.content.Context;

import com.util.libdroid.utils.ReflectionUtils;

/*
  数据库配置类
 */
public class Configuration {

    private Context mContext;
    private String mDatabaseName;
    private int mDatabaseVersion;
    private String mSqlFilePath;

    private Configuration(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public String getSqlFilePath() {
        return mSqlFilePath;
    }

    public static class Builder {

        private static final String LIB_DB_NAME = "LIB_DB_NAME";
        private static final String LIB_DB_VERSION = "LIB_DB_VERSION";
        private final static String LIB_SQL_FILE_PATH = "LIB_SQL_FILE_PATH";
        private final static String LIB_FOREIGN_KEY = "LIB_FOREIGN_KEY";
        private static final String DEFAULT_DB_NAME = "Libdroid.db";
        private static final String DEFAULT_SQL_FILE_PATH = "sqlfiles";

        private Context mContext;
        private String mDatabaseName;
        private Integer mDatabaseVersion;
        private String mSqlFilePath;


        public Builder(Context context) {
            mContext = context.getApplicationContext();
        }

        public Builder setDatabaseName(String databaseName) {
            mDatabaseName = databaseName;
            return this;
        }

        public Builder setDatabaseVersion(int databaseVersion) {
            mDatabaseVersion = databaseVersion;
            return this;
        }


        public Builder setSqlFilePath(String sqlFilePath) {
            mSqlFilePath = sqlFilePath;
            return this;
        }

        public Configuration create() {
            Configuration configuration = new Configuration(mContext);

            // Get database name from meta-data
            if (mDatabaseName != null) {
                configuration.mDatabaseName = mDatabaseName;
            } else {
                configuration.mDatabaseName = getMetaDataDatabaseNameOrDefault();
            }

            // Get database version from meta-data
            if (mDatabaseVersion != null) {
                configuration.mDatabaseVersion = mDatabaseVersion;
            } else {
                configuration.mDatabaseVersion = getMetaDataDatabaseVersionOrDefault();
            }

            // Get sqlfile path  from meta-data

            if (mSqlFilePath != null) {
                configuration.mSqlFilePath = mSqlFilePath;
            } else {
                configuration.mSqlFilePath = getMetaDataSqlFilePathOrDefault();
            }

            return configuration;
        }


        // Meta-data methods
        private String getMetaDataDatabaseNameOrDefault() {
            String aaName = ReflectionUtils.getMetaData(mContext, LIB_DB_NAME);
            if (aaName == null) {
                aaName = DEFAULT_DB_NAME;
            }

            return aaName;
        }

        private int getMetaDataDatabaseVersionOrDefault() {
            Integer aaVersion = ReflectionUtils.getMetaData(mContext, LIB_DB_VERSION);
            if (aaVersion == null || aaVersion == 0) {
                aaVersion = 1;
            }

            return aaVersion;
        }

        private String getMetaDataSqlFilePathOrDefault() {
            final String mode = ReflectionUtils.getMetaData(mContext, LIB_SQL_FILE_PATH);
            if (mode == null) {
                return DEFAULT_SQL_FILE_PATH;
            }
            return mode;
        }
    }
}
