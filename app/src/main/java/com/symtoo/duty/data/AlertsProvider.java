package com.symtoo.duty.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class AlertsProvider extends ContentProvider {
	
	private static final int ALERTS = 0;
	private static final int ALARMS = 1;
	private static final int ALARM_ID = 2;
	private static final int BACKUPS = 3;
	private static final int BACKUP_ID = 4;
	
	private static final String DTAG = "PROV";	
	
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		uriMatcher.addURI(AlertsContract.AUTHORITY, AlertsContract.ALERTS, ALERTS);
		uriMatcher.addURI(AlertsContract.AUTHORITY, AlertsContract.ALARMS, ALARMS);
		uriMatcher.addURI(AlertsContract.AUTHORITY, AlertsContract.ALARMS + "/#", ALARM_ID);
		uriMatcher.addURI(AlertsContract.AUTHORITY, AlertsContract.BACKUPS, BACKUPS);
		uriMatcher.addURI(AlertsContract.AUTHORITY, AlertsContract.BACKUPS +"/#", BACKUP_ID);
	}
	
	protected static final String DATABASE_NAME = "duty";
	protected static final int DATABASE_VERSION = 3;
	protected static final String TB_NAME = "alerts";
	
	private SQLiteOpenHelper database;
	
	
	
	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		
		int count;
		
		switch (uriMatcher.match(uri)) {
		case ALERTS:
			count = db.delete(TB_NAME, selection, selectionArgs);
			break;

		case ALARMS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_ALARM);
			count = db.delete(TB_NAME, selection, selectionArgs);
			break;

		case BACKUPS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_BACKUP);
			count = db.delete(TB_NAME, selection, selectionArgs);
			break;
			
		case ALARM_ID:
		case BACKUP_ID:
			selectionArgs = appendToSelectionArgs(selectionArgs, uri.getPathSegments().get(1));
			String where = appendToSelection(selection, AlertsContract._ID);
			
			count = db.delete(TB_NAME, where, selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if (count > 1) {
			switch (uriMatcher.match(uri)) {
			case ALERTS:
				getContext().getContentResolver().notifyChange(AlertsContract.CONTENT_URI_ALARMS, null);
				getContext().getContentResolver().notifyChange(AlertsContract.CONTENT_URI_BACKUPS, null);
				break;
			case ALARMS:
			case BACKUPS:
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			}

			try {
                                Thread.sleep(500);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                        switch (uriMatcher.match(uri)) {
                        case ALERTS:
                                sendAlarmUpdatedBroadcast();
                                sendBackupUpdatedBroadcast();
                                break;
                        case ALARMS:
                                sendAlarmUpdatedBroadcast();
                                break;
                        case BACKUPS:
                                sendBackupUpdatedBroadcast();
                                break;
                        default:
                                break;
                        }
		}

		return count;
	}
	
	
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALARMS:
			return AlertsContract.ALARMS_MIME_TYPE_DIR;
		case ALARM_ID:
			return AlertsContract.ALARMS_MIME_TYPE_ITEM;
		case BACKUPS:
			return AlertsContract.BACKUPS_MIME_TYPE_DIR;
		case BACKUP_ID:
			return AlertsContract.BACKUPS_MIME_TYPE_ITEM;
		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
	}
	
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		
		switch (uriMatcher.match(uri)) {
		case ALARMS:
		case BACKUPS:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if (!values.containsKey(AlertsContract.TYPE)) {
			throw new NullPointerException("ContentValues must contain key " + AlertsContract.TYPE);
		}
		
		if (!values.containsKey(AlertsContract.BODY)) {
			throw new NullPointerException("ContentValues must contain key " + AlertsContract.BODY);
		}
		
		if (!values.containsKey(AlertsContract.RECEIVED_AT)) {
			throw new NullPointerException("ContentValues must contain key " + AlertsContract.RECEIVED_AT);
		}
		
		SQLiteDatabase db = database.getWritableDatabase();
		
		long id = db.insert(TB_NAME, null, values);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		switch (uriMatcher.match(uri)) {
		case ALARMS:
			sendAlarmReceivedBroadcast(values.getAsString(AlertsContract.BODY));
			break;
		case BACKUPS:
			sendBackupReceivedBroadcast(values.getAsString(AlertsContract.BODY));
			break;
		}
		
		if (id > 0) {
			return ContentUris.withAppendedId(uri, id);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if (sortOrder == null || sortOrder.equals("")) {
			sortOrder = AlertsContract.DEFAULT_SORT_ORDER;
		}
		
		switch (uriMatcher.match(uri)) {
		case ALARMS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_ALARM);
			break;
		case BACKUPS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_BACKUP);
			break;
		case ALARM_ID:
		case BACKUP_ID:
			selectionArgs = appendToSelectionArgs(selectionArgs, uri.getPathSegments().get(1));
			selection = appendToSelection(selection, AlertsContract._ID);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = database.getReadableDatabase();

		Cursor cursor =  db.query(TB_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}
	
	@Override 
	public synchronized int update(Uri uri, ContentValues values, String selection, 
			String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();

		int count;

		switch (uriMatcher.match(uri)) {
		case ALERTS:
			break;
		case ALARMS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_ALARM);
			break;
		case BACKUPS:
			selection = appendToSelection(selection, AlertsContract.TYPE);
			selectionArgs = appendToSelectionArgs(selectionArgs, AlertsContract.TYPE_BACKUP);
			break;
		case ALARM_ID:
		case BACKUP_ID:
			selection = appendToSelection(selection, AlertsContract._ID);
			selectionArgs = appendToSelectionArgs(selectionArgs, uri.getPathSegments().get(1));

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.update(TB_NAME, values, selection, selectionArgs);
		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			switch (uriMatcher.match(uri)) {
			case ALERTS:
				sendAlarmUpdatedBroadcast();
				sendBackupUpdatedBroadcast();
				break;
			case ALARMS:
				sendAlarmUpdatedBroadcast();
				break;
			case BACKUPS:
				sendBackupUpdatedBroadcast();
				break;
			default:
				break;
			}
		}
		return count;
	}

	private void sendAlarmReceivedBroadcast() {
		Intent i = new Intent(AlertsContract.ALARM_RECEIVED);
		getContext().sendBroadcast(i);
	}

	private void sendAlarmReceivedBroadcast(String body) {
		Intent i = new Intent(AlertsContract.ALARM_RECEIVED);
		i.putExtra(AlertsContract.BODY, body);
		getContext().sendBroadcast(i);
	}

	private void sendAlarmUpdatedBroadcast() {
		Intent i = new Intent(AlertsContract.ALARM_UPDATED);
		getContext().sendBroadcast(i);
	}
	
	private void sendBackupReceivedBroadcast() {
		Intent i = new Intent(AlertsContract.BACKUP_RECEIVED);
		getContext().sendBroadcast(i);		
	}
	
	private void sendBackupReceivedBroadcast(String body) {
		Intent i = new Intent(AlertsContract.BACKUP_RECEIVED);
		i.putExtra(AlertsContract.BODY, body);
		getContext().sendBroadcast(i);
	}

	private void sendBackupUpdatedBroadcast() {
		Intent i = new Intent(AlertsContract.BACKUP_UPDATED);
		getContext().sendBroadcast(i);
	}
	
	private String appendToSelection(String oldSelection, String column) {
		String where = column + " = ?";
		if (oldSelection != null && !oldSelection.equals("")) {
			where = "(" + oldSelection + ") AND " + where;
		}
		return where;
	}
	
	public void close() {
		database.close();
	}
	
	private String[] appendToSelectionArgs(String[] oldArgs, String newArg) {
		String[] newArgs;
		if (oldArgs != null) {
			newArgs = copyOfArray(oldArgs, oldArgs.length + 1);
		} else {
			newArgs = new String[1];
		}
		
		newArgs[newArgs.length - 1] = newArg;
		
		return newArgs;
	}
	
	protected static String[] copyOfArray(String[] old, int newLength) {
		String[] newArray = new String[newLength];
		
		int oldLength = old.length;
		for (int i = 0; i < Math.min(oldLength, newLength); i++) {
			if (i < oldLength) {
				newArray[i] = old[i];
			} else {
				newArray[i] = null;
			}
		}
		return newArray;
	}
	
	public static int countAlerts() {
		return 0;
	}
	
	public static int countUnseenAlerts(Context context) {
		return AlertsProvider.countUnseen(context, AlertsContract.CONTENT_URI_ALERTS);
	}
	
	public static int countUnseenAlarms(Context context) {
		return AlertsProvider.countUnseen(context, AlertsContract.CONTENT_URI_ALARMS);
	}
	
	public static int countUnseenBackups(Context context) {
		return AlertsProvider.countUnseen(context, AlertsContract.CONTENT_URI_BACKUPS);
	}
	
	private static int countUnseen(Context context, Uri uri) {
		Cursor c = context.getContentResolver()
				.query(uri,
						new String[] { "COUNT(*) as count"},
						AlertsContract.SEEN + " = ?",
						new String[] { "0" },
						null);
		
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		
		return count;
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TB_NAME + "("
					 + AlertsContract._ID + " INTEGER PRIMARY KEY, "
					 + AlertsContract.TYPE + " CAHR(3) NOT NULL, "
					 + AlertsContract.RECEIVED_AT + " DATETIME NOT NULL,"
					 + AlertsContract.BODY + " TEXT NOT NULL, "
					 + AlertsContract.SEEN + " BOOLEAN NOT NULL DEFAULT 0"
					 + ");"
					);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
			onCreate(db);
		}
	}
}
