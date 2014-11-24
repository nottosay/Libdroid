package sample.libdroid;

import android.app.Application;

import com.util.libdroid.DBManager;

/**
 * Created by wally.yan on 2014/11/21.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DBManager.initialize(this);
    }
}
