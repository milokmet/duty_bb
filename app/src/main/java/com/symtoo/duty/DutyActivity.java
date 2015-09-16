package com.symtoo.duty;

import java.util.ArrayList;

import com.symtoo.duty.data.AlertsContract;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DutyActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public static final String ALARMS = "alarm";
	public static final String BACKUPS = "backups";
	
	protected static final String DTAG = AlertsContract.DTAG;
	
    protected SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        setContentView(R.layout.activity_duty);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++ ) {
        	actionBar.addTab(
        			actionBar.newTab()
        				.setText(mSectionsPagerAdapter.getPageTitle(i))
        				.setTabListener(this));
        }
        // TODO: Move to async task and add scheduled job for it
        deleteOldAlerts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_duty, menu);
        MenuItem dutyProfileItem = menu.findItem(R.id.menu_duty_profile);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dutyProfileItem.setChecked(sharedPrefs.getBoolean("duty_profile", false));
        
        return true;
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void deleteOldAlerts() {

    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences((Context)this);
    	int maxAge = sharedPrefs.getInt("max_age", AlertsContract.DEFAULT_MAX_AGE);
    	long maxAgeSeconds = maxAge * 24 * 60 * 60 * 1000;
    	
    	Time today = new Time(Time.getCurrentTimezone());
    	today.setToNow();
    	long olderThan = today.toMillis(true) - maxAgeSeconds;
    	
    	String selection = AlertsContract.RECEIVED_AT + " < ?";
    	String[] selectionArgs = { String.valueOf(olderThan) };
    	
    	getContentResolver().delete(AlertsContract.CONTENT_URI_ALERTS, selection, selectionArgs);
    }

    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_settings:
    			Intent i = new Intent(this, SettingsActivity.class);
    			startActivity(i);
    			break;
    		case R.id.menu_duty_profile:
    			toggleDutyProfile(item);
    			break;
    		case R.id.menu_duty_calls:
    			receiveDutyCalls();
    			break;
    		case R.id.menu_purge_alarms:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder
    				.setMessage(R.string.dialog_purge_message)
    				.setTitle(R.string.dialog_purge_title);
    			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   getContentResolver().delete(AlertsContract.CONTENT_URI_ALERTS, "", null);
    		           }
    		       });
    			
    			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {}
    		    });
    			
    			AlertDialog dialog = builder.create();
    			dialog.show();
    			break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }

    public void receiveDutyCalls() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    		.setMessage(R.string.dialog_receive_calls_message)
    		.setTitle(R.string.dialog_receive_calls_title);
    	
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int id) {
    			SmsManager sm = SmsManager.getDefault();
    			sm.sendTextMessage("640", null, "JA", null, null);
    			
    			Context context = getApplicationContext();
    			Toast toast = Toast.makeText(context, R.string.receive_calls_sent, Toast.LENGTH_SHORT);
    			toast.show();
    		}
    	});
    	
    	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
		});
    	
    	AlertDialog dialog = builder.create();
    	dialog.show();
    }
    
    public void toggleDutyProfile(MenuItem item) {
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    	Boolean profile = sharedPrefs.getBoolean(Settings.DUTY_PROFILE, false);
    	Boolean toggle = !profile ? true : false;
    	
    	sharedPrefs.edit().putBoolean(Settings.DUTY_PROFILE, toggle).commit();
    	
    	item.setChecked(toggle);
    	
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
     	
    	public final ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    	
    	public SectionsPagerAdapter(FragmentManager fm) {
    		super(fm);
    		addFragment(AlertsContract.TYPE_ALARM);
    		addFragment(AlertsContract.TYPE_BACKUP);
    	}

    	protected void addFragment(String type) {
    		Fragment fragment = new AlertsListFragment();
    		Bundle args = new Bundle();
    		args.putString(AlertsListFragment.ARG_TYPE, type);
    		fragment.setArguments(args);
    		fragments.add(fragment);
    	}
    	
    	@Override
    	public Fragment getItem(int index) {
    		return fragments.get(index);
    	}

    	@Override
    	public int getCount() {
    		return fragments.size();
    	}

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: 
                	return getString(R.string.tab_alarms).toUpperCase();
                case 1: 
                	return getString(R.string.tab_backups).toUpperCase();
            }
            return null;
        }
    }
}