package com.staticvillage.sense.android.client;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.client.adapter.SensorPropertyAdapter;
import com.staticvillage.sense.android.data.SensorInfo;
import com.staticvillage.sense.android.data.SensorInfoManager;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SensorEditActivity extends ListActivity implements OnClickListener {
	public static final String EXTRA_SENSOR = "sensor";
	public static final String EXTRA_SENSOR_TYPE = "sensor_type";
	
	private EditText txtSensorName;
	private EditText txtPropertyName;
	private EditText txtPropertyIndex;
	private Button btnAdd;
	private SensorPropertyAdapter adapter;
	private HashMap<String, SensorInfo> sensorInfo;
	private String sensorName;
	private int typeVal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_edit);
		
		sensorInfo = new HashMap<String, SensorInfo>();
		SensorInfo[] sensors = SensorInfoManager.loadSensorInfo(this);
		for(int i=0;i<sensors.length;i++){
			sensorInfo.put(sensors[i].name, sensors[i]);
		}
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			sensorName = extras.getString(EXTRA_SENSOR);
			typeVal = extras.getInt(EXTRA_SENSOR_TYPE);
			
			txtSensorName = (EditText)findViewById(R.id.txtSensorName);
			txtPropertyName = (EditText)findViewById(R.id.txtPropertyName);
			txtPropertyIndex = (EditText)findViewById(R.id.txtPropertyIndex);
			btnAdd = (Button)findViewById(R.id.btnPropertyAdd);
			
			ArrayList<Entry<String, String>> props = new ArrayList<Entry<String, String>>();
			
			if(sensorInfo.containsKey(sensorName)){
				Set<Entry<String, Integer>> lProps = sensorInfo.get(sensorName).properties.entrySet();
				
				for(Entry<String, Integer> pair : lProps){
					props.add(new AbstractMap.SimpleImmutableEntry<String, String>(pair.getKey(), String.valueOf(pair.getValue())));
				}
			}
			
			adapter = new SensorPropertyAdapter(this, props);
			setListAdapter(adapter);
			
			btnAdd.setOnClickListener(this);
			
			ActionBar actionbar = getActionBar();
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setTitle("Edit Sensor Properties");
			actionbar.setIcon(R.drawable.ic_home_black_48dp);
			
			txtSensorName.setText(sensorName);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sensor_edit, menu);
        return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_auto:
                showAutoFill();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View view) {
		String name = txtPropertyName.getText().toString();
		String index = txtPropertyIndex.getText().toString();
		
		if(name == null || index == null || name.isEmpty() || index.isEmpty()){
			Toast.makeText(this, "Please ensure fields are complete", Toast.LENGTH_SHORT).show();
			return;
		}
		
		adapter.add(new AbstractMap.SimpleImmutableEntry<String, String>(name, index));
		adapter.notifyDataSetChanged();
		
		txtPropertyName.setText("");
		txtPropertyIndex.setText("");
	}

	@Override
	protected void onPause() {
		save();
		super.onPause();
	}

    public void showAutoFill(){
        DialogFragment dialog = new SensorEditDialogFragment();
        dialog.show(getFragmentManager(), "AutoFillDialog");
    }

    public void autoFill(ArrayList<Pair> properties){
        adapter.clear();
        for(Pair pair : properties){
            adapter.add(new AbstractMap.SimpleImmutableEntry<String, String>(pair.getKey(), pair.getValue()));
        }

        adapter.notifyDataSetChanged();
    }
	
	private void save(){
		if(sensorInfo.containsKey(sensorName)){
			sensorInfo.get(sensorName).typeValue = typeVal;
			sensorInfo.get(sensorName).properties = adapter.getProperties();
		}else{
			SensorInfo info = new SensorInfo();
			info.name = sensorName;
			info.typeValue = typeVal;
			info.properties = adapter.getProperties();
			
			sensorInfo.put(sensorName, info);
		}
		
		SensorInfoManager.saveSensorInfo(this, sensorInfo.values().toArray(new SensorInfo[]{}));
	}
}
