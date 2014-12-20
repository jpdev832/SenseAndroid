package com.staticvillage.sense.android.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.staticvillage.sense.android.location.Geo;

import android.hardware.SensorEvent;
import android.util.SparseArray;

public class SensorData {
	public String name;
	public String tag;
	public String app_id;
	public String session_id;
	public long timestamp = 0;
	public Geo geo;
	
	protected HashMap<String, Double> values;
	protected SparseArray<String> sensorEventIndex;
	
	/**
	 * Determine whether field is a sensor data base type
	 * 
	 * @param field field to verify
	 * @return Is Base Field
	 */
	public static boolean isBaseField(String field){
		if(field.equals("name"))
			return true;
		else if(field.equals("tag"))
			return true;
		else if(field.equals("app_id"))
			return true;
		else if(field.equals("session_id"))
			return true;
		else if(field.equals("timestamp"))
			return true;
		else if(field.equals("geo"))
			return true;
		
		return false;
	}
	
	/**
	 * Initialize a new SensorData instance from MongoDB DBObject
	 * 
	 * @param obj MongoDB DBObject
	 * @return new SensorData instance
	 */
	public static SensorData initFromDBObject(DBObject obj){
		SensorData data = new SensorData();
		data.name = (String) obj.get("name");
		data.tag = (String) obj.get("tag");
		data.app_id = (String) obj.get("app_id");
		data.session_id = (String) obj.get("session_id");
		data.timestamp = (Long) obj.get("timestamp");
		if(obj.containsField("geo")){
			Object geo = obj.get("geo");
			data.geo = (geo == null) ? null : new Geo().fromDBObject((DBObject)JSON.parse(obj.get("geo").toString()));
		}else
			data.geo = null;
		
		Map<String, Object> m = obj.toMap();
		
		int i=0;
		for(Entry<String, Object> entry : m.entrySet()){
			if(isBaseField(entry.getKey()) || entry.getKey().equals("_id"))
				continue;
			
			data.values.put(entry.getKey(), (Double)entry.getValue());
			data.sensorEventIndex.put(i++, entry.getKey());
		}
		
		return data;
	}
	
	protected SensorData(){
		this("");
	}
	public SensorData(String name){
		this(name, new HashMap<String, Double>(), new SparseArray<String>());
	}
	public SensorData(String name, Map<String, Double> values, SparseArray<String> indicies){
		this.name = name;
		this.values = new HashMap<String, Double>(values);
		this.sensorEventIndex = indicies;
	}
	
	/**
	 * Add a new sensor property
	 * 
	 * @param property property name
	 * @param defaultValue default property value
	 * @param sensorEventIndex android sensor type value
	 */
	public void addProperty(String property, double defaultValue, int sensorEventIndex){
		values.put(property, defaultValue);
		this.sensorEventIndex.append(sensorEventIndex, property);
	}
	
	/**
	 * Get JSON representing SensorData instance
	 * 
	 * @return JSON String
	 */
	public String getJSON(){
		return getDBObject().toString();
	}
	
	/**
	 * Get DBObject representing SensorData instance
	 * 
	 * @return DBObject
	 */
	public DBObject getDBObject(){
		BasicDBObject obj = new BasicDBObject();
		obj.append("name", this.name);
		obj.append("tag", this.tag);
		obj.append("app_id", this.app_id);
		obj.append("session_id", this.session_id);
		obj.append("timestamp", this.timestamp);
		obj.append("geo", (geo == null) ? "" : geo.toDBObject());
		
		for(Entry<String, Double> entry : values.entrySet()){
			obj.put(entry.getKey(), entry.getValue());
		}
		
		return obj;
	}
	
	/**
	 * Initialize SensorData instance from android SensorEvent data
	 * @param event
	 */
	public void initFromSensorEvent(SensorEvent event){
		float[] sValues = event.values;
		
		for(int i=0;i<sValues.length;i++){
			if(!values.containsKey(sensorEventIndex.get(i)))
				continue;
			
			values.put(sensorEventIndex.get(i), (Double)((double)sValues[i]));
		}
	}
	
	/**
	 * Get property values
	 * 
	 * @return SensorData property values
	 */
	public double[] getValues(){
		double[] v = new double[values.size()];
		int i=0;
		for(Double d : values.values()){
			v[i] = d;
			i++;
		}
		
		return v;
	}
	
	/**
	 * Sensor name
	 * @return sensor name
	 */
	public String getName(){
		return name;
	}
}
