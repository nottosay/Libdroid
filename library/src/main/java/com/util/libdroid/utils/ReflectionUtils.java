package com.util.libdroid.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.util.libdroid.db.Model;
import com.util.libdroid.db.annotation.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */
public final class ReflectionUtils {

    public static boolean isModel(Class<?> type) {
        return isSubclassOf(type, Model.class) && (!Modifier.isAbstract(type.getModifiers()));
    }


    // Meta-data
    public static <T> T getMetaData(Context context, String name) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            if (ai.metaData != null) {
                return (T) ai.metaData.get(name);
            }
        } catch (Exception e) {
            LogUtils.w("Couldn't find meta-data: " + name);
        }

        return null;
    }

    public static Set<Field> getDeclaredColumnFields(Class<?> type) {
        Set<Field> declaredColumnFields = Collections.emptySet();

        if (ReflectionUtils.isSubclassOf(type, Model.class) || Model.class.equals(type)) {
            declaredColumnFields = new LinkedHashSet<Field>();

            Field[] fields = type.getDeclaredFields();
            Arrays.sort(fields, new Comparator<Field>() {
                @Override
                public int compare(Field field1, Field field2) {
                    return field2.getName().compareTo(field1.getName());
                }
            });
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    declaredColumnFields.add(field);
                }
            }

            Class<?> parentType = type.getSuperclass();
            if (parentType != null) {
                declaredColumnFields.addAll(getDeclaredColumnFields(parentType));
            }
        }

        return declaredColumnFields;
    }


    public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
        if (type.getSuperclass() != null) {
            if (type.getSuperclass().equals(superClass)) {
                return true;
            }

            return isSubclassOf(type.getSuperclass(), superClass);
        }

        return false;
    }
}