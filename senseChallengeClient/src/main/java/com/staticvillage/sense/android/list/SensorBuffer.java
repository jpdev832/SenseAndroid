package com.staticvillage.sense.android.list;

import java.util.ArrayList;
import java.util.HashMap;

import com.staticvillage.sense.android.data.SensorData;

public class SensorBuffer {
	private HashMap<String, ArrayList<SensorData>> map;
	private int bufferSize;
	
	public SensorBuffer(int bufferSize){
		this.bufferSize = bufferSize;
		this.map = new HashMap<String, ArrayList<SensorData>>();
	}
	
	/**
	 * Add captured data to buffer. When buffer is reached for sensor type
	 * it is returned and reset.
	 * 
	 * @param data SensorData to save to buffer
	 * @return SensorData to report
	 */
	public SensorData[] add(SensorData data){
		if(!map.containsKey(data.getName()))
			map.put(data.getName(), new ArrayList<SensorData>(bufferSize));
		
		ArrayList<SensorData> list = map.get(data.getName());
		list.add(data);
		
		if(list.size() == bufferSize){
			SensorData[] aData = list.toArray(new SensorData[]{});
			list.clear();
			
			return aData;
		}
		
		return null;
	}
	
	/**
	 * Flush all data from buffer
	 * 
	 * @return All sensor info left to report
	 */
	public SensorData[][] flush(){
		ArrayList<SensorData[]> data = new  ArrayList<SensorData[]>();
		for(ArrayList<SensorData> dataset : map.values()){
			if(dataset.size() != 0)
				data.add(dataset.toArray(new SensorData[]{}));
			
			dataset.clear();
		}
		
		return data.toArray(new SensorData[][]{});
	}
}
