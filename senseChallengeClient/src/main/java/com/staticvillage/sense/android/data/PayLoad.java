package com.staticvillage.sense.android.data;

public class PayLoad {
	private String collection;
	private SensorData[] data;
	
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public SensorData[] getData() {
		return data;
	}
	public void setData(SensorData[] data) {
		this.data = data;
	}
}
