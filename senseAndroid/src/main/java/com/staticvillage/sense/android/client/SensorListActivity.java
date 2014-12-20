package com.staticvillage.sense.android.client;

import java.util.List;

import com.staticvillage.sense.android.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SensorListActivity extends ListActivity {
	private String[] names;
	private int[] typeValues;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_list);
		
		SensorManager manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
		
		names = new String[sensors.size()];
		typeValues = new int[sensors.size()];
		for(int i=0;i<sensors.size();i++){
			names[i] = sensors.get(i).getName();
			typeValues[i] = sensors.get(i).getType();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String name = names[position];
		int typeVal = typeValues[position];
		
		Intent intent = new Intent(this, SensorEditActivity.class);
		intent.putExtra(SensorEditActivity.EXTRA_SENSOR, name);
		intent.putExtra(SensorEditActivity.EXTRA_SENSOR_TYPE, typeVal);
		startActivity(intent);
	}
}
