package com.symtoo.duty;

import java.util.Calendar;

import com.symtoo.duty.data.AlertsContract;
import com.symtoo.duty.data.AlertsProvider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	private static final int NOTIFICATION_ID = 1;
	private static final String DTAG = "NOTIF";

	private boolean mDutyProfile = false;
	private boolean mQuietHoursEnabled = false;
	private int mQuietHoursStart = 0;
	private int mQuietHoursEnd = 0;

	
	
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		mDutyProfile = sharedPrefs.getBoolean(Settings.DUTY_PROFILE, false);
		mQuietHoursEnabled = sharedPrefs.getBoolean(Settings.QUIET_HOURS_ENABLED, false);
		mQuietHoursStart = sharedPrefs.getInt(Settings.QUIET_HOURS_START, Settings.DEFAULT_QUIET_HOURS_START);
		mQuietHoursEnd = sharedPrefs.getInt(Settings.QUIET_HOURS_END, Settings.DEFAULT_QUIET_HOURS_END);
		
		int alarmsCount = AlertsProvider.countUnseenAlarms(context);
		int backupsCount = AlertsProvider.countUnseenBackups(context);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notificationManager.cancel(NOTIFICATION_ID);
		
		Log.d(DTAG, "alarmsCount (" +alarmsCount+ ") + backupsCount ("+backupsCount+") = " + (alarmsCount + backupsCount));
		
		if (alarmsCount + backupsCount < 1) {
			Log.d(DTAG, "Notif should be cancelled");
			return ;
		}
		
		Notification n;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		Intent notificationIntent = new Intent(context, DutyActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		int notificationsCount = alarmsCount + backupsCount;
		Resources res = context.getResources();
		String title = null;
		String text = null;
		
		if (alarmsCount > 0 && backupsCount > 0) {
			title = res.getQuantityString(R.plurals.notification_title_alerts, notificationsCount, notificationsCount); 
			text = String.format(res.getString(R.string.notification_backups_and_alarms), 
					res.getQuantityString(R.plurals.notification_text_alarms, alarmsCount, alarmsCount),
					res.getQuantityString(R.plurals.notification_text_backups, backupsCount, backupsCount));
		} else {
			if (alarmsCount > 0 && backupsCount == 0) {
				title = res.getQuantityString(R.plurals.notification_title_alarms, alarmsCount, alarmsCount);
			} else {
				title = res.getQuantityString(R.plurals.notification_title_backups, backupsCount, backupsCount);
			}
			text = intent.getStringExtra(AlertsContract.BODY);
		}
		
		n = builder
				.setContentTitle(title)
				.setContentText(text)
				.setSmallIcon(R.drawable.db)
				// .setNumber(notificationsCount)
				.setPriority(10)
				.setContentIntent(contentIntent)
				// .setStyle(new NotificationCompat.BigTextStyle().bigText("Tak BIG TEXT"))
				.build();
	
		if (mDutyProfile && !inQuietHours()) {
			String soundUri = null;
			if (intent.getAction().equals(AlertsContract.ALARM_RECEIVED)) {
				soundUri = sharedPrefs.getString(Settings.ALARM_SOUND, null);
			} else if (intent.getAction().equals(AlertsContract.BACKUP_RECEIVED)) {;
				soundUri = sharedPrefs.getString(Settings.BACKUP_SOUND, null);
			}

			if (soundUri != null) {
				n.sound 	= Uri.parse(soundUri);
			} else {
				n.defaults |= Notification.DEFAULT_SOUND;
			}
		} else {
			Log.d(DTAG, "Quiet hours in effect " + mQuietHoursStart + "/" + mQuietHoursEnd);
		}
		
		notificationManager.notify(NOTIFICATION_ID, n);
		Log.d(DTAG, "Notification was triggered");
	}

	private boolean inQuietHours() {
		if (mQuietHoursEnabled && (mQuietHoursStart != mQuietHoursEnd)) {
			Calendar calendar = Calendar.getInstance();
			int minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            if (mQuietHoursEnd < mQuietHoursStart) {
                // Starts at night, ends in the morning.
                return ((minutes > mQuietHoursStart) || (minutes < mQuietHoursEnd));
            } else {
                return ((minutes > mQuietHoursStart) && (minutes < mQuietHoursEnd));
            }
		}
		return false;
	}
}
