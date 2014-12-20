package com.staticvillage.sense.android.client;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseRetriever;
import com.staticvillage.sense.android.client.adapter.SensorAdapter;
import com.staticvillage.sense.android.client.data.SenseDB;
import com.staticvillage.sense.android.client.data.SensorDetail;
import com.staticvillage.sense.android.data.SensorData;
import com.staticvillage.sense.android.data.SensorInfo;
import com.staticvillage.sense.android.data.SensorInfoManager;
import com.staticvillage.sense.android.exception.NetworkUnavailableException;
import com.staticvillage.sense.android.logger.SensorLogger;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CaptureActivity extends ListActivity implements Observer {
	public static final int MAX_HISTORY = 30;
	
	private SensorLogger logger;
	private XYPlot plot;
	private ImageView imgCapture;
	private ImageView imgStop;
	private EditText txtTag;
	private String connString;
	private String db;
    private int[] colors;
	private SensorAdapter adapter;
	private HashMap<String, SimpleXYSeries[]> seriesMap;
	private HashMap<String, SensorInfo> sensorInfo;
	private boolean locationEnabled;
	private boolean offlineMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		
		plot = (XYPlot)findViewById(R.id.mySimpleXYPlot);
		imgCapture = (ImageView)findViewById(R.id.imgCapturePlay);
		imgStop = (ImageView)findViewById(R.id.imgCaptureStop);
		txtTag = (EditText)findViewById(R.id.txtTag);
		
		SizeMetrics sm = new SizeMetrics(0, SizeLayoutType.FILL, 0, SizeLayoutType.FILL);
		
		plot.setBackgroundColor(Color.WHITE);
		plot.getGraphWidget().setSize(sm);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
		
		plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
		plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
		
		plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);
		
		plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
		
		colors = getResources().getIntArray(R.array.androidcolors);
		
		imgCapture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startCapture();
			}
		});
		
		imgStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCapture();
			}
		});
		
		ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle("Capture");
            actionbar.setIcon(R.drawable.ic_home_black_48dp);
        }
		
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		init();
	}

	@Override
	protected void onPause() {
		stopCapture();
		super.onPause();
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
	public void update(Observable logger, Object payload) {
		SensorData data = (SensorData)payload;
		
		double[] values = data.getValues();
		SimpleXYSeries[] series = seriesMap.get(data.getName());
		
		for(int i=0; i<values.length; i++){
			addDataPoint(series[i], values[i]);
		}
		
		plot.redraw();
	}
	
	/**
	 * Initialize series, list, and preference settings
	 */
	public void init(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		this.connString = pref.getString(AccountActivity.KEY_CONN_STR, "");
		this.db = pref.getString(AccountActivity.KEY_DB, "");
        int bufferSize = Integer.valueOf(pref.getString(getString(R.string.pref_buffer_size), "10"));
		this.locationEnabled = pref.getBoolean(getString(R.string.pref_location_enable), false);
		this.offlineMode = pref.getBoolean(getString(R.string.pref_offline_mode), false);
		
		if(!offlineMode){
			ConnectivityManager cm =
			        (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
			 
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null &&
			                      activeNetwork.isConnectedOrConnecting();
			
			if(!isConnected){
				Toast.makeText(this, "Please check network connection or enable offline mode", Toast.LENGTH_LONG).show();
				finish();
			}
			
			verifyConnection();
		}
		
		sensorInfo = new HashMap<String, SensorInfo>();
		SensorInfo[] sensors = SensorInfoManager.loadSensorInfo(this);
		for(SensorInfo info : sensors){
			sensorInfo.put(info.name, info);
		}
		
		initSeries();
		initList();
		
		logger = new SensorLogger(this, "Sense", bufferSize);
		logger.addObserver(this);
	}
	
	/**
	 * Verify connection to mongodb instance
	 */
	public void verifyConnection(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		String db = preferences.getString(AccountActivity.KEY_DB, "");
		String connStr = preferences.getString(AccountActivity.KEY_CONN_STR, "");
		
		//update to actually detect connection
		SenseRetriever retriever = new SenseRetriever(connStr, db);
		retriever.addObserver(this);
		try {
			retriever.init();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Toast.makeText(this, "Please verify account settings or enable offline mode from settings", Toast.LENGTH_LONG).show();
			finish();
		} catch(Exception ex){
			Toast.makeText(this, "Please verify account settings or enable offline mode from settings", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	/**
	 * Initialize series map info
	 */
	public void initSeries(){
		seriesMap = new HashMap<String, SimpleXYSeries[]>();
		
		for(SensorInfo info : sensorInfo.values()){
			ArrayList<SimpleXYSeries> series = new ArrayList<SimpleXYSeries>(info.properties.size());
			for(String property : info.properties.keySet()){
				series.add(new SimpleXYSeries(property));
			}
			
			seriesMap.put(info.name, series.toArray(new SimpleXYSeries[]{}));
		}
	}
	
	/**
	 * Initialize list with sensors
	 */
	public void initList(){
		ArrayList<SensorDetail> sd = new ArrayList<SensorDetail>(sensorInfo.size());
		for(SensorInfo info : sensorInfo.values()){
			SensorDetail d = new SensorDetail();
			d.name = info.name;
			d.summary = info.description;
			d.checked = false;
			
			sd.add(d);
		}
		
		adapter = new SensorAdapter(this, sd);
		setListAdapter(adapter);
	}
	
	/**
	 * Add a data point to the graph
	 * 
	 * @param series series to add data point to
	 * @param value data point value
	 */
	public void addDataPoint(SimpleXYSeries series, double value){
		if(series.size() > MAX_HISTORY){
			series.removeFirst();
			series.addLast(MAX_HISTORY, value);
		}else
			series.addLast(series.size(), value);

        //re-number to give even look
        for(int i=0;i<series.size();i++){
            series.setX(i, i);
        }
	}
	
	/**
	 * Start capturing sensor data
	 */
	public void startCapture(){
		enableEdit(false);
		imgCapture.setVisibility(View.INVISIBLE);
		imgStop.setVisibility(View.VISIBLE);
		plot.clear();
		
		int counter = 0;
		int index;
		
		String[] names = adapter.getEnabled();
		for(String name : names){
			for(SimpleXYSeries series : seriesMap.get(name)){
				index = counter % colors.length;
				plot.addSeries(series, new LineAndPointFormatter(colors[index], null, null, null));
				
				counter++;
			}
		
			logger.addSensor(name);
		}
		
		try {
			if(locationEnabled)
				logger.enableLocation();
			else
				logger.disableLocation();
			
			String session;
			if(offlineMode){
				File file = new File(getFilesDir(), logger.getSessionId());
				session = logger.start(file, txtTag.getText().toString());
			}else{
				session = logger.start(connString, db, txtTag.getText().toString());
			}
			
			SenseDB sdb = new SenseDB(this);
			sdb.openRW();
			
			for(String name : names){
				Long ret = sdb.addEntry(name, txtTag.getText().toString(), session, System.currentTimeMillis(), !offlineMode);
				Log.d("sense", "db val: "+ret);
			}
			
			sdb.close();
		} catch (NetworkUnavailableException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Stop capturing sensor data
	 */
	public void stopCapture(){
		imgCapture.setVisibility(View.VISIBLE);
		imgStop.setVisibility(View.INVISIBLE);
		
		logger.stop();
		logger.removeAllSensors();
		enableEdit(true);
	}
	
	/**
	 * Enable/Disable editing
	 * 
	 * @param enabled editing enabled
	 */
	public void enableEdit(boolean enabled){
        txtTag.setEnabled(enabled);
        adapter.setEnabled(enabled);
	}
}
