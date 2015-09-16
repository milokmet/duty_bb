package com.symtoo.duty;

import java.io.ObjectOutputStream.PutField;

import com.symtoo.duty.data.AlertsContract;

import com.symtoo.duty.preference.TimeRangePreference;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;


public class Settings extends PreferenceFragment implements OnPreferenceChangeListener {
	
	public static final String KEY_QUIET_HOURS_TIMERANGE = "quiet_hours_timerange";
	public static final String MAX_AGE			 = "max_age";
	public static final String QUIET_HOURS_ENABLED = "quiet_hours_enabled";
	public static final String QUIET_HOURS_START = "quiet_hours_start";
	public static final String QUIET_HOURS_END   = "quiet_hours_end";
	public static final String ALARM_SOUND       = "alarm_sound";
	public static final String BACKUP_SOUND      = "backup_sound";
	public static final String DUTY_PROFILE      = "duty_profile";
	
	public static final int DEFAULT_QUIET_HOURS_START = 1260;
	public static final int DEFAULT_QUIET_HOURS_END = 420;
	
	private TimeRangePreference mQuietHoursTimeRange;
	private Preference          mMaxAge;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
		addPreferencesFromResource(R.xml.preferences);
		
		Context context = getActivity();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		for (String prefName: new String[]{ALARM_SOUND, BACKUP_SOUND}) {
			try {
				String uriString = sharedPrefs.getString(prefName, null);
				Uri uri = Uri.parse(uriString);
				RingtonePreference preference = (RingtonePreference)findPreference(prefName);
				updateRingtonePreference(preference, uri);
				preference.setOnPreferenceChangeListener((OnPreferenceChangeListener)this);
			} catch (NullPointerException e) {
				
			}
		}
		
		mQuietHoursTimeRange = (TimeRangePreference)findPreference(KEY_QUIET_HOURS_TIMERANGE);
		mQuietHoursTimeRange.setStartTime(sharedPrefs.getInt(QUIET_HOURS_START, Settings.DEFAULT_QUIET_HOURS_START));
		mQuietHoursTimeRange.setEndTime(sharedPrefs.getInt(QUIET_HOURS_END, Settings.DEFAULT_QUIET_HOURS_END));
		
		mQuietHoursTimeRange.setOnPreferenceChangeListener((OnPreferenceChangeListener)this);
		
		
		mMaxAge = findPreference(MAX_AGE);
		int value = sharedPrefs.getInt(MAX_AGE, AlertsContract.DEFAULT_MAX_AGE);
		updateMaxAgePreference(mMaxAge, value);
		
		mMaxAge.setOnPreferenceChangeListener((OnPreferenceChangeListener)this);
		
		// Preference maxAge = findPreference("max_age");
		// maxAge.setOnPreferenceChangeListener((OnPreferenceChangeListener)context);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference instanceof RingtonePreference) {
			try {
				updateRingtonePreference(preference, Uri.parse((String)newValue));
				return true;
			} catch (NullPointerException e) {
				
			}
		} else if (preference == mMaxAge) {
			updateMaxAgePreference(preference, (Integer)newValue);
			return true;
		} else if (preference == mQuietHoursTimeRange) {
			
			SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getActivity());
			shp.edit()
				.putInt(QUIET_HOURS_START, mQuietHoursTimeRange.getStartTime())
				.putInt(QUIET_HOURS_END, mQuietHoursTimeRange.getEndTime())
				.commit();

			return true;
		}

		return false;
	}
	
	protected void updateRingtonePreference(Preference preference, Uri ringtoneUri) {
		Context context = getActivity();
		Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
		preference.setSummary(ringtone.getTitle(context));
	}
	
	public void updateMaxAgePreference(Preference preference, Integer value) {
		Resources res = getResources();	
		String summary = res.getQuantityString(R.plurals.max_notification_age_summary, value, value);
		preference.setSummary(summary);
	}
}
