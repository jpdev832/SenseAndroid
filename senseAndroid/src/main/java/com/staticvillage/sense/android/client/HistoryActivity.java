package com.staticvillage.sense.android.client;

import java.io.File;
import java.io.IOException;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseClient;
import com.staticvillage.sense.android.SenseListener;
import com.staticvillage.sense.android.client.adapter.HistoryCursorAdapter;
import com.staticvillage.sense.android.client.data.SenseDB;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryActivity extends ListActivity implements OnClickListener, SenseListener {
	private SenseClient client;
	private SenseDB db;
	private HistoryCursorAdapter adapter;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		db = new SenseDB(this);
		Cursor cursor = db.getEntries();
		
		adapter = new HistoryCursorAdapter(this, this, cursor);
		setListAdapter(adapter);
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("History");
		actionbar.setIcon(R.drawable.ic_home_black_48dp);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String connectionString = pref.getString(AccountActivity.KEY_CONN_STR, "");
		String cdb = pref.getString(AccountActivity.KEY_DB, "");
		
		client = new SenseClient(this, "Sense");
		if(!connectionString.isEmpty() && !cdb.isEmpty()){
			client.connect(connectionString, cdb);
		}
		
		progressDialog = new ProgressDialog(this);
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
	protected void onPause() {
		db.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		String sessionId = (String)view.getTag();
		
		File file = new File(getFilesDir(), sessionId);
		try {
			client.uploadFromFile(file);
			progressDialog.setTitle("Uploading Data...");
			progressDialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HistoryCursorAdapter.HistoryItemHolder holder = (HistoryCursorAdapter.HistoryItemHolder)v.getTag();
		String sessionId = (String)holder.imgUpload.getTag();
		String collection = holder.txtName.getText().toString();
		
		if(sessionId != null){
			Intent intent = new Intent(this, GraphActivity.class);
			intent.putExtra(GraphActivity.EXTRA_IS_FILE, true);
			intent.putExtra(GraphActivity.EXTRA_COLLECTION, collection);
			intent.putExtra(GraphActivity.EXTRA_SESSION, sessionId);
			startActivity(intent);
		}else{
			Toast.makeText(this, "File not available on device", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnect(String appId, String sessionId) {
		adapter.enableUpload();
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onConnectError(String message) {
		Toast.makeText(this, "Connection Error: "+message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDataUploaded(String sessionId) {
		db.updateEntry(sessionId, true);
		
		Cursor cursor = db.getEntries();
		
		adapter.update(sessionId);
		adapter.swapCursor(cursor);
		adapter.notifyDataSetChanged();
		
		File file = new File(getFilesDir(), sessionId);
		file.delete();
		progressDialog.dismiss();
	}
}
