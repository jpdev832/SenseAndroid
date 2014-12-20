package com.staticvillage.sense.android.data;

import com.mongodb.DBObject;

public interface JSONDBObject<T> {
	public DBObject toDBObject();
	public T fromDBObject(DBObject obj);
}
