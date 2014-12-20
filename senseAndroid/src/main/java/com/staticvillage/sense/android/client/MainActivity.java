package com.staticvillage.sense.android.client;

import java.io.File;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.data.SensorInfoManager;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private ImageView imgCapture;
	private ImageView imgRetrieve;
	private ImageView imgHistory;
	private ImageView imgSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imgCapture = (ImageView)findViewById(R.id.imgCapture);
		imgRetrieve = (ImageView)findViewById(R.id.imgRetrieve);
		imgHistory = (ImageView)findViewById(R.id.imgHistory);
		imgSettings = (ImageView)findViewById(R.id.imgSettings);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		
		imgCapture.setOnClickListener(this);
		imgRetrieve.setOnClickListener(this);
		imgHistory.setOnClickListener(this);
		imgSettings.setOnClickListener(this);
		
		init();
	}
	
	public void init(){
		File file = new File(getFilesDir(), SensorInfoManager.SENSOR_FILENAME);
		if(!file.exists())
			SensorInfoManager.initBaseData(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.ab_account:
	        	Intent intent = new Intent(this, AccountActivity.class);
	        	startActivity(intent);
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
		if(view == imgCapture){
			Intent intent = new Intent(this, CaptureActivity.class);
			startActivity(intent);
		}else if(view == imgRetrieve){
			Intent intent = new Intent(this, RetrieveActivity.class);
			startActivity(intent);
		}else if(view == imgHistory){
			Intent intent = new Intent(this, HistoryActivity.class);
			startActivity(intent);
		}else if(view == imgSettings){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
	}

}
