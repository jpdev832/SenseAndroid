package com.staticvillage.sense.android.data;

import java.util.HashMap;

import android.util.SparseArray;

public class SensorBuilder {
	private String name;
	private HashMap<String, Double> values;
	private SparseArray<String> sensorEventIndices;
	
	public SensorBuilder(String name){
		this.name = name;
		this.values = new HashMap<String, Double>();
		this.sensorEventIndices = new SparseArray<String>();
	}
	
	public void addProperty(String property, int sensorEventIndex){
		values.put(property, 0.0);
		sensorEventIndices.append(sensorEventIndex, property);
	}
	
	public SensorData toSensorData(){
		return new SensorData(name, (HashMap<String, Double>)values.clone(), sensorEventIndices.clone());
	}
}
