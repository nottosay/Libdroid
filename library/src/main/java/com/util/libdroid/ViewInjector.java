package com.util.libdroid;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;

import com.util.libdroid.utils.LogUtils;
import com.util.libdroid.view.ResLoader;
import com.util.libdroid.view.ViewFinder;
import com.util.libdroid.view.annotation.ContentView;
import com.util.libdroid.view.annotation.FindRes;
import com.util.libdroid.view.annotation.Find;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wally.yan on 2014/10/21.
 */
public class ViewInjector {

    public static void inject(View view) {
        inject(view, new ViewFinder(view));
    }

    public static void inject(Activity activity) {
        inject(activity, new ViewFinder(activity));
    }

    public static void inject(Fragment fragment) {
        inject(fragment, new ViewFinder(fragment));
    }

    public static void inject(Object mClass, View view) {
        inject(mClass, new ViewFinder(view));
    }

    public static void inject(Object mClass, Activity activity) {
        inject(mClass, new ViewFinder(activity));
    }

    public static void inject(Object mClass, Fragment fragment) {
        inject(mClass, new ViewFinder(fragment));
    }


    private static void inject(Object mClass, ViewFinder finder) {
        Class<?> classType = mClass.getClass();

        //inject contentView
        ContentView contentView = classType.getAnnotation(ContentView.class);
        if (contentView != null) {
            try {
                Method setContentViewMethod = classType.getMethod("setContentView", int.class);
                setContentViewMethod.invoke(mClass, contentView.value());
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        //inject  field
        Field[] fields = classType.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                Find injectView = field.getAnnotation(Find.class);
                if (injectView != null) {
                    try {
                        View view = finder.findViewById(injectView.value(), injectView.parentId());
                        if (view != null) {
                            field.setAccessible(true);
                            field.set(mClass, view);
                        }
                    } catch (Throwable e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                } else {
                    FindRes injectRes = field.getAnnotation(FindRes.class);
                    if (injectRes != null) {
                        try {
                            Object res = ResLoader.loadRes(injectRes.type(), finder.getContext(), injectRes.value());
                            if (res != null) {
                                field.setAccessible(true);
                                field.set(mClass, res);
                            }
                        } catch (Throwable e) {
                            LogUtils.e(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }
}
