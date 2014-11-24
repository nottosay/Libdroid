package com.util.libdroid.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;

import com.util.libdroid.utils.LogUtils;
import com.util.libdroid.utils.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * 缓存类
 */
public final class Cache {

	public static final int DEFAULT_CACHE_SIZE = 2*1024;

	private static Context sContext;

	private static DatabaseHelper sDatabaseHelper;

	private static Map<Class<? extends Model>, TableInfo> mTableInfos;

    private static LruCache<String, Model> sEntities;

    private static boolean sIsInitialized = false;

	public static synchronized void initialize(Configuration configuration) {
		if (sIsInitialized) {
			return;
		}

		sContext = configuration.getContext();

		sDatabaseHelper = new DatabaseHelper(configuration);

		mTableInfos = new HashMap<Class<? extends Model>, TableInfo>();

        sEntities = new LruCache<String, Model>(DEFAULT_CACHE_SIZE);

        try {
            scanForModel(configuration.getContext());
        }
        catch (IOException e) {
            LogUtils.e("Couldn't open source path.", e);
        }

		openDatabase();

		sIsInitialized = true;

	}

	public static synchronized void clear() {
        sEntities.evictAll();
	}

	public static synchronized void dispose() {
		closeDatabase();
		mTableInfos = null;
		sDatabaseHelper = null;
		sIsInitialized = false;
	}

	// Database access
	public static boolean isInitialized() {
		return sIsInitialized;
	}

	public static synchronized SQLiteDatabase openDatabase() {
		return sDatabaseHelper.getWritableDatabase();
	}

	public static synchronized void closeDatabase() {
		sDatabaseHelper.close();
	}

	// Context access
	public static Context getContext() {
		return sContext;
	}

    // Entity cache
    public static String getIdentifier(Class<? extends Model> type, Long id) {
        return getTableName(type) + "@" + id;
    }

    public static String getIdentifier(Model entity) {
        return getIdentifier(entity.getClass(), entity.getId());
    }

    public static synchronized void addEntity(Model entity) {
        sEntities.put(getIdentifier(entity), entity);
    }

    public static synchronized Model getEntity(Class<? extends Model> type, long id) {
        return sEntities.get(getIdentifier(type, id));
    }

    public static synchronized void removeEntity(Model entity) {
        sEntities.remove(getIdentifier(entity));
    }

    // Model cache
    public static synchronized Collection<TableInfo> getTableInfos() {
        return mTableInfos.values();
    }


    public static synchronized TableInfo getTableInfo(Class<? extends Model> type) {
        return mTableInfos.get(type);
    }

    public static synchronized String getTableName(Class<? extends Model> type) {
        return mTableInfos.get(type) == null?null:mTableInfos.get(type).getTableName();
    }


    private  static  void scanForModel(Context context) throws IOException {
        String packageName = context.getPackageName();
        String sourcePath = context.getApplicationInfo().sourceDir;
        List<String> paths = new ArrayList<String>();

        if (sourcePath != null && !(new File(sourcePath).isDirectory())) {
            DexFile dexfile = new DexFile(sourcePath);
            Enumeration<String> entries = dexfile.entries();

            while (entries.hasMoreElements()) {
                paths.add(entries.nextElement());
            }
        }
        // Robolectric fallback
        else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                String path = resources.nextElement().getFile();
                if (path.contains("bin") || path.contains("classes")) {
                    paths.add(path);
                }
            }
        }

        for (String path : paths) {
            File file = new File(path);
            scanForModelClasses(file, packageName, context.getClassLoader());
        }
    }

    private static void scanForModelClasses(File path, String packageName, ClassLoader classLoader) {
        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                scanForModelClasses(file, packageName, classLoader);
            }
        }
        else {
            String className = path.getName();

            // Robolectric fallback
            if (!path.getPath().equals(className)) {
                className = path.getPath();

                if (className.endsWith(".class")) {
                    className = className.substring(0, className.length() - 6);
                }
                else {
                    return;
                }

                className = className.replace(System.getProperty("file.separator"), ".");

                int packageNameIndex = className.lastIndexOf(packageName);
                if (packageNameIndex < 0) {
                    return;
                }

                className = className.substring(packageNameIndex);
            }

            try {
                Class<?> discoveredClass = Class.forName(className, false, classLoader);
                if (ReflectionUtils.isModel(discoveredClass)) {

                    Class<? extends Model> modelClass = (Class<? extends Model>) discoveredClass;
                    mTableInfos.put(modelClass, new TableInfo(modelClass));
                }

            }
            catch (ClassNotFoundException e) {
                LogUtils.e("Couldn't create class.", e);
            }
        }
    }
}
