package com.util.libdroid.db.query;


import com.util.libdroid.db.Cache;
import com.util.libdroid.db.Model;

public final class Update implements Sqlable {
	private Class<? extends Model> mType;

	public Update(Class<? extends Model> table) {
		mType = table;
	}

	public Set set(String set) {
		return new Set(this, set);
	}

	public Set set(String set, Object... args) {
		return new Set(this, set, args);
	}

	Class<? extends Model> getType() {
		return mType;
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		sql.append(Cache.getTableName(mType));
		sql.append(" ");

		return sql.toString();
	}
}
