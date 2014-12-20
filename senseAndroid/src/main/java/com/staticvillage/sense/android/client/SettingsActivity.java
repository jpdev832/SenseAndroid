package com.staticvillage.sense.android.client;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new GeneralSettingsFragment())
			.commit();
	}
	
}
