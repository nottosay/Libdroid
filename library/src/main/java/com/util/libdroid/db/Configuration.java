package com.util.libdroid.db;


import android.content.Context;

import com.util.libdroid.utils.ReflectionUtils;

import java.util.List;

/*
  数据库配置类
 */
public class Configuration {

    public final static String SQL_PARSER_LEGACY = "legacy";
    public final static String SQL_PARSER_DELIMITED = "delimited";

	private Context mContext;
	private String mDatabaseName;
	private int mDatabaseVersion;
    private String mSqlParser;
    private String mSqlFilePath;
    private boolean isCopyDatabase;

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

    public String getSqlParser() {
        return mSqlParser;
    }

    public String getSqlFilePath() {
        return mSqlFilePath;
    }

    public boolean isCopyDatabase() {
        return isCopyDatabase;
    }

	public static class Builder {

		private static final String AA_DB_NAME = "AA_DB_NAME";
		private static final String AA_DB_VERSION = "AA_DB_VERSION";
        private final static String AA_SQL_PARSER = "AA_SQL_PARSER";
        private final static String AA_SQL_FILE_PATH = "AA_SQL_FILE_PATH";
        private final static String AA_COPY_DATABASE = "AA_COPY_DATABASE";
		private static final int DEFAULT_CACHE_SIZE = 1024;
		private static final String DEFAULT_DB_NAME = "Libdroid.db";
        private static final String DEFAULT_SQL_PARSER = SQL_PARSER_LEGACY;
        private static final String DEFAULT_SQL_FILE_PATH = "sqlfiles";

		private Context mContext;
		private Integer mCacheSize;
		private String mDatabaseName;
		private Integer mDatabaseVersion;
		private String mSqlParser;
		private String mSqlFilePath;
		private List<Class<? extends Model>> mModelClasses;


		public Builder(Context context) {
			mContext = context.getApplicationContext();
			mCacheSize = DEFAULT_CACHE_SIZE;
		}

		public Builder setDatabaseName(String databaseName) {
			mDatabaseName = databaseName;
			return this;
		}

		public Builder setDatabaseVersion(int databaseVersion) {
			mDatabaseVersion = databaseVersion;
			return this;
		}
		
		public Builder setSqlParser(String sqlParser) {
		    mSqlParser = sqlParser;
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

            // Get SQL parser from meta-data
            if (mSqlParser != null) {
                configuration.mSqlParser = mSqlParser;
            } else {
                configuration.mSqlParser = getMetaDataSqlParserOrDefault();
            }

            if(mSqlFilePath != null){
                configuration.mSqlFilePath = mSqlFilePath;
            } else {
                configuration.mSqlFilePath = getMetaDataSqlFilePathOrDefault();
            }

            configuration.isCopyDatabase = getMetaDataCopyDtabaseOrDefault();

			return configuration;
		}


		// Meta-data methods

		private String getMetaDataDatabaseNameOrDefault() {
			String aaName = ReflectionUtils.getMetaData(mContext, AA_DB_NAME);
			if (aaName == null) {
				aaName = DEFAULT_DB_NAME;
			}

			return aaName;
		}

		private int getMetaDataDatabaseVersionOrDefault() {
			Integer aaVersion = ReflectionUtils.getMetaData(mContext, AA_DB_VERSION);
			if (aaVersion == null || aaVersion == 0) {
				aaVersion = 1;
			}

			return aaVersion;
		}

        private String getMetaDataSqlParserOrDefault() {
            final String mode = ReflectionUtils.getMetaData(mContext, AA_SQL_PARSER);
            if (mode == null) {
                return DEFAULT_SQL_PARSER;
            }
            return mode;
        }
        private String getMetaDataSqlFilePathOrDefault() {
            final String mode = ReflectionUtils.getMetaData(mContext, AA_SQL_FILE_PATH);
            if (mode == null) {
                return DEFAULT_SQL_FILE_PATH;
            }
            return mode;
        }

        private boolean getMetaDataCopyDtabaseOrDefault() {
            final Boolean mode = ReflectionUtils.getMetaData(mContext, AA_COPY_DATABASE);
            if (mode == null) {
                return false;
            }
            return mode;
        }
	}
}
