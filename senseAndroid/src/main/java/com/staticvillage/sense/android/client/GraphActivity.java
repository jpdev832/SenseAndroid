package com.staticvillage.sense.android.client;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.mongodb.BasicDBObject;
import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseClient;
import com.staticvillage.sense.android.SenseRetriever;
import com.staticvillage.sense.android.data.RetrieveResult;
import com.staticvillage.sense.android.data.SensorData;
import com.staticvillage.sense.android.data.SensorInfo;
import com.staticvillage.sense.android.data.SensorInfoManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.Toast;

public class GraphActivity extends Activity implements Observer {
	public static final String EXTRA_IS_FILE = "isFile";
	public static final String EXTRA_COLLECTION = "collection";
	public static final String EXTRA_SESSION = "session";
	
	private SenseRetriever retriever;
	private XYPlot plot;
	private int[] colors;
	private PointF minXY;
    private PointF maxXY;
    
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		plot = (XYPlot)findViewById(R.id.graphPlot);
		
		SizeMetrics sm = new SizeMetrics(0, SizeLayoutType.FILL, 0, SizeLayoutType.FILL);
		
		plot.setBackgroundColor(Color.WHITE);
		plot.getGraphWidget().setSize(sm);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
		
		plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
		plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
		
		plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
		
		plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
		plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
		
		colors = getResources().getIntArray(R.array.androidcolors);
		
		//Set of internal variables for keeping track of the boundaries
        plot.calculateMinMaxVals();
        minXY=new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue());
        maxXY=new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("Results");
		actionbar.setIcon(R.drawable.ic_assessment_black_48dp);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean isFile = extras.getBoolean(EXTRA_IS_FILE);
			String collection = extras.getString(EXTRA_COLLECTION);
			String session = extras.getString(EXTRA_SESSION);
			
			if(isFile){
				RetrieveFromFile(collection, session);
			}else{
				RetrieveFromDB(collection, session);
			}
		}
	}
	
	@Override
	protected void onPause() {
		if(progressDialog != null)
			progressDialog.dismiss();
		
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
	public void update(Observable observable, Object obj) {
		RetrieveResult res = (RetrieveResult)obj;
		SensorData[] sData = (SensorData[])res.data;
		
		if(sData.length < 1){
			progressDialog.dismiss();
			Toast.makeText(this, "Error occurred while retrieving data", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		SimpleXYSeries[] series = getXYSeries(sData[0].name);
		
		for(SensorData data : sData){
			double[] values = data.getValues();
			
			for(int i=0; i<values.length; i++){
				series[i].addLast(data.timestamp, values[i]);
			}
		}
		
		for(int i=0;i<series.length;i++)
			plot.addSeries(series[i], new LineAndPointFormatter(colors[i], null, null, null));
		
		plot.redraw();
		
		progressDialog.dismiss();
	}
	
	protected void RetrieveFromFile(String collection, String sessionId){
	    try {
	    	progressDialog = new ProgressDialog(GraphActivity.this);
			progressDialog.setTitle("Loading...");
			progressDialog.setMessage("Retrieving data");
			progressDialog.show();
			
			File file = new File(getFilesDir(), sessionId);
			RetrieveResult res = new RetrieveResult();
			res.data = SenseClient.retrieveFromFile(file, collection);
			
			update(null, res);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Failed to load file", Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void RetrieveFromDB(String collection, String sessionId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    String connStr = prefs.getString(AccountActivity.KEY_CONN_STR, "");
	    String db = prefs.getString(AccountActivity.KEY_DB, "");
	    
	    retriever = new SenseRetriever(connStr, db);
	    try {
	    	progressDialog = new ProgressDialog(GraphActivity.this);
			progressDialog.setTitle("Loading...");
			progressDialog.setMessage("Retrieving data");
			progressDialog.show();
	    	
			retriever.init();
			retriever.addObserver(this);
			
			BasicDBObject dbObj = new BasicDBObject(SenseRetriever.KEY_NAME, collection).
	    			append(SenseRetriever.KEY_SESSION_ID, sessionId);
			
			retriever.retrieve(collection, dbObj);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get XY series from name
	 * @param name series name
	 * @return XY Series
	 */
	public SimpleXYSeries[] getXYSeries(String name){
		SimpleXYSeries[] sa = null;
		
		SensorInfo[] sensorInfo = SensorInfoManager.loadSensorInfo(this);
		
		for(SensorInfo info : sensorInfo){
			if(info.name.equals(name)){
				ArrayList<SimpleXYSeries> series = new ArrayList<SimpleXYSeries>(info.properties.size());
				for(String property : info.properties.keySet()){
					series.add(new SimpleXYSeries(property));
				}
				
				sa = series.toArray(new SimpleXYSeries[]{});
				break;
			}
		}
		
		return sa;
	}
	
}
