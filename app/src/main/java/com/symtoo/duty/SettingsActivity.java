package com.symtoo.duty;


import android.app.ActionBar;

import android.os.Bundle;

import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
		
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new Settings())
			.commit();
	}
}