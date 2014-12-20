package com.staticvillage.sense.android.client;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseRetriever;
import com.staticvillage.sense.android.client.adapter.RetrieveAdapter;
import com.staticvillage.sense.android.client.data.RetrieveDetail;
import com.staticvillage.sense.android.data.RetrieveResult;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class RetrieveActivity extends Activity implements Observer, OnItemSelectedListener, OnItemClickListener {
	private Spinner collectionSpinner;
	private Spinner tagSpinner;
	private SenseRetriever retriever;
	private ArrayAdapter<String> collectionAdapter;
	private ArrayAdapter<String> tagAdapter;
	private RetrieveAdapter itemAdapter;
	private ListView lstRet;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrieve);
		
		ConnectivityManager cm =
		        (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		if(!isConnected){
			Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
			finish();
		}
		
		collectionSpinner = (Spinner)findViewById(R.id.spinRetrieveCol);
		tagSpinner = (Spinner)findViewById(R.id.spinRetTag);
		lstRet = (ListView)findViewById(R.id.lstRetItems);
		
		collectionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
		tagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
		itemAdapter = new RetrieveAdapter(this, new ArrayList<RetrieveDetail>());
		
		collectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		collectionSpinner.setAdapter(collectionAdapter);
		tagSpinner.setAdapter(tagAdapter);
		lstRet.setAdapter(itemAdapter);
		
		collectionSpinner.setOnItemSelectedListener(this);
		tagSpinner.setOnItemSelectedListener(this);
		lstRet.setOnItemClickListener(this);
		
		init();
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("Retrieve Results");
		actionbar.setIcon(R.drawable.ic_home_black_48dp);
	}
	
	@Override
	protected void onPause() {
		if(progressDialog != null)
			progressDialog.dismiss();
		
		super.onPause();
	}



	public void init(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		String db = preferences.getString(AccountActivity.KEY_DB, "");
		String connStr = preferences.getString(AccountActivity.KEY_CONN_STR, "");
		
		retriever = new SenseRetriever(connStr, db);
		retriever.addObserver(this);
		try {
			retriever.init();
			retriever.getCollections();
			showProgressDialog("Retrieving Collections");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Toast.makeText(this, "Unable to retrieve settings", Toast.LENGTH_SHORT).show();
			finish();
		} catch(Exception ex){
			Toast.makeText(this, "Please verify account settings", Toast.LENGTH_LONG).show();
			finish();
		}
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
		RetrieveResult result = (RetrieveResult)obj;
		
		if(result.type.equals(SenseRetriever.TYPE_COLLECTION)){
			updateCollection((String[])result.data);
		}else if(result.type.equals(SenseRetriever.TYPE_TAG)){
			updateTags((String[])result.data);
		}else if(result.type.equals(SenseRetriever.TYPE_RAW)){
			Log.d("Sense", "received objects");
			updateItems((DBObject[])result.data);
		}
	}

	public void updateCollection(String[] collections){
		progressDialog.dismiss();
		collectionAdapter.clear();
		
		for(String c:collections)
			if(c != null)
				collectionAdapter.add(c);
		
		collectionAdapter.notifyDataSetChanged();
	}
	
	private void updateTags(String[] tags) {
		progressDialog.dismiss();
		tagAdapter.clear();
		
		for(String tag:tags)
			tagAdapter.add(tag);
		
		tagAdapter.notifyDataSetChanged();
	}
	
	private void updateItems(DBObject[] data) {
		progressDialog.dismiss();
		itemAdapter.clear();
		
		for(DBObject sd:data){
			BasicDBObject bdbObj = (BasicDBObject)sd;
			BasicDBObject keys = (BasicDBObject) bdbObj.get("_id");
			
			RetrieveDetail rd = new RetrieveDetail();
			rd.collection = keys.getString(SenseRetriever.KEY_NAME);
			rd.timestamp = bdbObj.getLong(SenseRetriever.KEY_TIMESTAMP);
			rd.session = keys.getString(SenseRetriever.KEY_SESSION_ID);
			
			itemAdapter.add(rd);
		}
		
		itemAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position,
			long id) {
		if(adapterView == collectionSpinner){
			tagAdapter.clear();
			tagAdapter.notifyDataSetChanged();
			
			String collection = (String)adapterView.getItemAtPosition(position);
			retriever.getTags(collection);
			showProgressDialog("Retrieving Tags");
		}else if(adapterView == tagSpinner){
			String collection = collectionAdapter.getItem(collectionSpinner.getSelectedItemPosition());
			String tag = (String)adapterView.getItemAtPosition(position);
			
			if(collection == null || tag == null)
				return;

			DBObject criteria = new BasicDBObject(SenseRetriever.KEY_TAG, tag);
			DBObject match = new BasicDBObject("$match", criteria);
			
			DBObject dbId = new BasicDBObject(SenseRetriever.KEY_NAME, "$"+SenseRetriever.KEY_NAME);
			dbId.put(SenseRetriever.KEY_SESSION_ID, "$"+SenseRetriever.KEY_SESSION_ID);
			
			// Now the $group operation
			DBObject groupFields = new BasicDBObject("_id", dbId);
			groupFields.put(SenseRetriever.KEY_TIMESTAMP, new BasicDBObject( "$min", "$"+SenseRetriever.KEY_TIMESTAMP));
			DBObject group = new BasicDBObject("$group", groupFields);

			// Finally the $sort operation
			DBObject sort = new BasicDBObject("$sort", new BasicDBObject(SenseRetriever.KEY_TIMESTAMP, -1));

			// run aggregation
			List<DBObject> pipeline = Arrays.asList(match, group, sort);
			Log.d("sense", pipeline.toString());
			
			retriever.retrieveAggregate(collection, pipeline);
			showProgressDialog("Retrieving captured entries");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		if(adapterView == lstRet){
			RetrieveDetail data = (RetrieveDetail)adapterView.getItemAtPosition(position);
			
			Intent intent = new Intent(this, GraphActivity.class);
			intent.putExtra(GraphActivity.EXTRA_IS_FILE, false);
			intent.putExtra(GraphActivity.EXTRA_COLLECTION, data.collection);
			intent.putExtra(GraphActivity.EXTRA_SESSION, data.session);
			startActivity(intent);
		}
	}
	
	public void showProgressDialog(String msg){
		progressDialog = new ProgressDialog(RetrieveActivity.this);
		progressDialog.setTitle("Loading...");
		progressDialog.setMessage(msg);
		progressDialog.show();
	}
}
