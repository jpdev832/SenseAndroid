package com.staticvillage.sense.android.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

public class SensorInfoManager {
	public static final String SENSOR_FILENAME = "senor_info";
	
	/**
	 * Create initial file
	 * 
	 * @param context context
	 */
	public static void initBaseData(Context context){
		JSONArray sensors = new JSONArray();
		saveSensorInfo(context, sensors);
	}
	
	/**
	 * Add sensor configuration
	 * 
	 * @param context context
	 * @param name sensor name
	 * @param typeValue android type value
	 * @param properties properties associated with sensor
	 */
	public static void addSensor(Context context, String name, int typeValue, Map<String, Integer> properties){
		JSONArray sensors = SensorInfoManager.loadSensorInfoJSON(context);
		
		SensorInfo info = new SensorInfo();
		info.name = name;
		info.typeValue = typeValue;
		
		for(Entry<String, Integer> pair : properties.entrySet()){
			info.properties.put(pair.getKey(), pair.getValue());
		}
		
		saveSensorInfo(context, sensors);
	}
	
	/**
	 * Save configuration to file
	 * 
	 * @param context context
	 * @param sensors JSONArray
	 */
	public static void saveSensorInfo(Context context, JSONArray sensors){
		File file = new File(context.getFilesDir(), SENSOR_FILENAME);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(sensors.toString());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Save configuration to file
	 * 
	 * @param context context
	 * @param sensors sensor data
	 */
	public static void saveSensorInfo(Context context, SensorInfo[] sensors){
		JSONArray sArray = new JSONArray();
		for(SensorInfo s : sensors){
			sArray.put(s.toJSONObject());
		}
		
		saveSensorInfo(context, sArray);
	}
	
	/**
	 * Load configuration from file
	 * @param context context
	 * @return JSONArray sensor data
	 */
	public static JSONArray loadSensorInfoJSON(Context context){
		File file = new File(context.getFilesDir(), SENSOR_FILENAME);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder builder = new StringBuilder();
			String read = null;
			
			while((read = reader.readLine()) != null){
				builder.append(read);
			}
			
			JSONArray sensors = new JSONArray(builder.toString());
			return sensors;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return null;
	}
	
	/**
	 * Get Sensor configuration from file
	 * 
	 * @param context context
	 * @return Array of SensorData
	 */
	public static SensorInfo[] loadSensorInfo(Context context){
		JSONArray sensors = SensorInfoManager.loadSensorInfoJSON(context);
		if(sensors == null)
			return null;
		
		SensorInfo[] infos = new SensorInfo[sensors.length()];
		
		for(int i=0;i<sensors.length();i++){
			try {
				infos[i] = SensorInfo.fromJSONObject(sensors.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return infos;
	}
}
