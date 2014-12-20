package com.staticvillage.sense.android.data;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about configured sensors
 * 
 * @author joelparrish
 */
public class SensorInfo {
	public String name;
	public String description;
	public int typeValue;
	public HashMap<String, Integer> properties;
	
	public SensorInfo(){
		properties = new HashMap<String, Integer>();
		
		//update later
		description = "";
	}
	
	/**
	 * Get JSON string representing sensor configuration
	 * 
	 * @return JSON string
	 */
	public JSONObject toJSONObject(){
		try {
			JSONObject obj = new JSONObject();
			obj.put("name", name);
			obj.put("type_value", typeValue);
			
			JSONArray props = new JSONArray();
			for(Entry<String, Integer> pair : properties.entrySet()){
				JSONObject property = new JSONObject();
				property.put("property", pair.getKey());
				property.put("index", pair.getValue());
				
				props.put(property);
			}
			
			obj.put("properties", props);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Initialize instance of SensorInfo from JSONObject
	 * 
	 * @param obj JSONObject
	 * @return SensorInfo instance
	 */
	public static SensorInfo fromJSONObject(JSONObject obj){
		SensorInfo info = new SensorInfo();
		try {
			info.name = obj.getString("name");
			info.typeValue = obj.getInt("type_value");
			
			JSONArray props = obj.getJSONArray("properties");
			for(int i=0;i<props.length();i++){
				JSONObject prop = props.getJSONObject(i);
				String property = prop.getString("property");
				int index = prop.getInt("index");
				
				info.properties.put(property, index);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return info;
	}
}
