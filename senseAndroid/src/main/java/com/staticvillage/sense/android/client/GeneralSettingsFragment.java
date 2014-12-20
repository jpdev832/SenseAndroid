package com.staticvillage.sense.android.client;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.staticvillage.sense.android.R;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class GeneralSettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.general_settings);
		
		if(!servicesConnected()){
			CheckBoxPreference chkGeo = (CheckBoxPreference) findPreference(getString(R.string.pref_location_enable));
			chkGeo.setEnabled(false);
		}else{
			String prefName = getString(R.string.pref_location_enable);
			CheckBoxPreference chkGeo = (CheckBoxPreference) findPreference(prefName);
			chkGeo.setEnabled(true);
		}
	}
	
	/**
	 * Check if google play services is connected
	 * 
	 * @return status
	 */
	private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Sense","Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    getActivity(),
                    1324);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                errorDialog.show();
            }
        }
        
        return false;
    }
}
