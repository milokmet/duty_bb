package com.symtoo.duty.data;

import android.net.Uri;

public class AlertsContract {
	public static final String AUTHORITY = "com.symtoo.duty.provider";
	public static final String ALERTS = "alerts";
	public static final String ALARMS = "alarms";
	public static final String BACKUPS = "backups";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri CONTENT_URI_ALERTS = Uri.parse(CONTENT_URI + "/" + ALERTS);
	public static final Uri CONTENT_URI_ALARMS = Uri.parse(CONTENT_URI + "/" + ALARMS);
	public static final Uri CONTENT_URI_BACKUPS = Uri.parse(CONTENT_URI + "/" + BACKUPS);
	
	public static final String _ID = "_id";
	public static final String TYPE = "sender";
	public static final String BODY = "body";
	public static final String RECEIVED_AT = "received_at";
	public static final String SEEN = "seen";
	
	public static final String TYPE_ALARM = "640";
	public static final String TYPE_BACKUP = "642";
	
	public static final String[] PROJECTION = { _ID, TYPE, BODY, RECEIVED_AT, SEEN };
	
	public static final String DEFAULT_SORT_ORDER = RECEIVED_AT + " ASC";
	
	public static final String ALARMS_MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.com.symtoo.duty.alarms";
	public static final String ALARMS_MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.symtoo.duty.alarms";
	public static final String BACKUPS_MIME_TYPE_ITEM = "vnd.android.cursor.item/vnd.com.symtoo.duty.backup";
	public static final String BACKUPS_MIME_TYPE_DIR = "vnd.android.cursor.dir/vnd.com.symtoo.duty.backup";

	// Action pre broadcast
	public static final String ALARM_RECEIVED = "com.symtoo.duty.ALARM_RECEIVED";
	public static final String BACKUP_RECEIVED = "com.symtoo.duty.BACKUP_RECEIVED";
	public static final String ALARM_UPDATED = "com.symtoo.duty.ALARM_UPDATED";
	public static final String BACKUP_UPDATED = "com.symtoo.duty.BACKUP_UPDATED";

	public static final String DTAG = "Duty";
	public static final int DEFAULT_MAX_AGE = 7;
	
}
