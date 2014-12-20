package com.staticvillage.sense.android.client;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseClient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AccountActivity extends Activity {
	public static final String KEY_HOSTNAME = "hostname";
	public static final String KEY_PORT 	= "port";
	public static final String KEY_DB 		= "db";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASS 	= "pass";
	public static final String KEY_CONN_STR = "connection_string";
	
	private EditText txtHost;
	private EditText txtPort;
	private EditText txtDB;
	private EditText txtUsername;
	private EditText txtPass;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("Account Settings");
		actionbar.setIcon(R.drawable.ic_home_black_48dp);
		
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new AccountSettingsFragment())
			.commit();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.account, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		saveSettings();
    	return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		saveSettings();
		super.onBackPressed();
	}

	/**
	 * Verify account settings
	 */
	public void verify(){
		progressDialog = new ProgressDialog(AccountActivity.this);
		progressDialog.setTitle("Verifying Account Settings");
		progressDialog.setMessage("Verification in progress...");
		progressDialog.show();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		final String conString = SenseClient.buildConectionString(
				prefs.getString(getString(R.string.pref_username), ""), 
				prefs.getString(getString(R.string.pref_password), ""), 
				prefs.getString(getString(R.string.pref_db), ""), 
				prefs.getString(getString(R.string.pref_hostname), ""), 
				prefs.getString(getString(R.string.pref_port), ""));

		VerifyTask vt = new VerifyTask();
		vt.execute(conString, prefs.getString(getString(R.string.pref_db), ""));
	}
	
	/**
	 * Result from verification test
	 * 
	 * @param verified verified
	 */
	public void verificationResult(boolean verified){
		progressDialog.dismiss();
		
		if(verified){
			saveSettings();
			Toast.makeText(this, "Account has been added", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "Invalid account!", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Save connection string
	 */
	private void saveSettings(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString(KEY_CONN_STR, SenseClient.buildConectionString(
				prefs.getString(getString(R.string.pref_username), ""), 
				prefs.getString(getString(R.string.pref_password), ""), 
				prefs.getString(getString(R.string.pref_db), ""), 
				prefs.getString(getString(R.string.pref_hostname), ""), 
				prefs.getString(getString(R.string.pref_port), "")));
		editor.commit();
	}
	
	/**
	 * Async verification task
	 * 
	 * @author joelparrish
	 */
	private class VerifyTask extends AsyncTask<String, Void, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			boolean verified = SenseClient.verifyAccount(params[0], params[1]);
			
			return verified;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			verificationResult(result);
		}
		
	}
}
