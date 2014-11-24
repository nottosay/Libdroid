
package com.util.libdroid.utils;


import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;


public class IOUtils {

    public static void closeQuietly(final Closeable closeable) {

        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (final IOException e) {
            LogUtils.e("Couldn't close closeable.", e);
        }
    }

    public static void closeQuietly(final Cursor cursor) {

        if (cursor == null) {
            return;
        }

        try {
            cursor.close();
        } catch (final Exception e) {
            LogUtils.e("Couldn't close cursor.", e);
        }
    }
}
