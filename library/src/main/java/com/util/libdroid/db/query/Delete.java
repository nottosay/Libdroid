package com.util.libdroid.db.query;

import com.util.libdroid.db.Model;

public final class Delete implements Sqlable {
	public Delete() {
	}

	public From from(Class<? extends Model> table) {
		return new From(table, this);
	}

	@Override
	public String toSql() {
		return "DELETE ";
	}
}