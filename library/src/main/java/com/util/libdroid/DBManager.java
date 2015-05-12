package com.util.libdroid;

import android.content.Context;

import com.util.libdroid.db.Cache;
import com.util.libdroid.db.Configuration;

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


}
