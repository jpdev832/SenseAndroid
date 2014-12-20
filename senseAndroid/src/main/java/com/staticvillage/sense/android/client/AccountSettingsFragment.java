package com.staticvillage.sense.android.client;

import com.staticvillage.sense.android.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AccountSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.account_settings);
	}
	
}
