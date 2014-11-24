package com.util.libdroid.db;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.util.libdroid.db.query.Delete;
import com.util.libdroid.db.query.Select;
import com.util.libdroid.utils.LogUtils;
import com.util.libdroid.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Model {

	private Long mId = null;

	private final TableInfo mTableInfo;
	private final String idName;

	public Model() {
		mTableInfo = Cache.getTableInfo(getClass());
		idName = mTableInfo.getIdName();
	}

	public final Long getId() {
		return mId;
	}

	public final void delete() {
		Cache.openDatabase().delete(mTableInfo.getTableName(), idName+"=?", new String[] { getId().toString() });
		Cache.removeEntity(this);
	}

	public final Long save() {
		final SQLiteDatabase db = Cache.openDatabase();
		final ContentValues values = new ContentValues();

		for (Field field : mTableInfo.getFields()) {
			final String fieldName = mTableInfo.getColumnName(field);
			Class<?> fieldType = field.getType();

			field.setAccessible(true);

			try {
				Object value = field.get(this);

				if (value == null) {
					values.putNull(fieldName);
				}
				else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
					values.put(fieldName, (Byte) value);
				}
				else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
					values.put(fieldName, (Short) value);
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					values.put(fieldName, (Integer) value);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					values.put(fieldName, (Long) value);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					values.put(fieldName, (Float) value);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					values.put(fieldName, (Double) value);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					values.put(fieldName, (Boolean) value);
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					values.put(fieldName, value.toString());
				}
				else if (fieldType.equals(String.class)) {
					values.put(fieldName, value.toString());
				}
				else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
					values.put(fieldName, (byte[]) value);
				}
				else if (ReflectionUtils.isModel(fieldType)) {
					values.put(fieldName, ((Model) value).getId());
				}
				else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
					values.put(fieldName, ((Enum<?>) value).name());
				}
			}
			catch (IllegalArgumentException e) {
				LogUtils.e(e.getClass().getName(), e);
			}
			catch (IllegalAccessException e) {
                LogUtils.e(e.getClass().getName(), e);
			}
		}

		if (mId == null) {
			mId = db.insert(mTableInfo.getTableName(), null, values);
		}
		else {
			db.update(mTableInfo.getTableName(), values, idName+"=" + mId, null);
		}
		return mId;
	}

	// Convenience methods

	public static void delete(Class<? extends Model> type, long id) {
		TableInfo tableInfo = Cache.getTableInfo(type);
		new Delete().from(type).where(tableInfo.getIdName()+"=?", id).execute();
	}

	public static <T extends Model> T load(Class<T> type, long id) {
		TableInfo tableInfo = Cache.getTableInfo(type);
		return (T) new Select().from(type).where(tableInfo.getIdName()+"=?", id).executeSingle();
	}

    public static <T extends Model>  List<T> loadAll(Class<T> type) {
        TableInfo tableInfo = Cache.getTableInfo(type);
        return new Select().from(type).execute();
    }

	// Model population

	public final void loadFromCursor(Cursor cursor) {

        List<String> columnsOrdered = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
		for (Field field : mTableInfo.getFields()) {
			final String fieldName = mTableInfo.getColumnName(field);
			Class<?> fieldType = field.getType();
			final int columnIndex = columnsOrdered.indexOf(fieldName);

			if (columnIndex < 0) {
				continue;
			}

			field.setAccessible(true);

			try {
				boolean columnIsNull = cursor.isNull(columnIndex);
				Object value = null;
				if (columnIsNull) {
					field = null;
				}
				else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					value = cursor.getLong(columnIndex);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					value = cursor.getFloat(columnIndex);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					value = cursor.getDouble(columnIndex);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					value = cursor.getInt(columnIndex) != 0;
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					value = cursor.getString(columnIndex).charAt(0);
				}
				else if (fieldType.equals(String.class)) {
					value = cursor.getString(columnIndex);
				}
				else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
					value = cursor.getBlob(columnIndex);
				}
				else if (ReflectionUtils.isModel(fieldType)) {
					final long entityId = cursor.getLong(columnIndex);
					final Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

					Model entity = Cache.getEntity(entityType, entityId);
					if (entity == null) {
						entity = new Select().from(entityType).where(idName+"=?", entityId).executeSingle();
					}

					value = entity;
				}
				else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
					@SuppressWarnings("rawtypes")
					final Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
					value = Enum.valueOf(enumType, cursor.getString(columnIndex));
				}

				// Set the field value
				if (value != null) {
					field.set(this, value);
				}
			}
			catch (IllegalArgumentException e) {
				LogUtils.e(e.getClass().getName(), e);
			}
			catch (IllegalAccessException e) {
                LogUtils.e(e.getClass().getName(), e);
			}
			catch (SecurityException e) {
                LogUtils.e(e.getClass().getName(), e);
			}
		}
	}

	protected final <T extends Model> List<T> getMany(Class<T> type, String foreignKey) {
		return new Select().from(type).where(Cache.getTableName(type) + "." + foreignKey + "=?", getId()).execute();
	}

	@Override
	public String toString() {
		return mTableInfo.getTableName() + "@" + getId();
	}

}
