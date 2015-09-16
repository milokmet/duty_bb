package com.symtoo.duty;

import com.symtoo.duty.data.AlertsContract;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AlertsViewBinder implements SimpleCursorAdapter.ViewBinder {

	protected Context context;
	
	public AlertsViewBinder(Context ctx) {
		context = ctx;
	}
	
	public boolean setViewValue(View view, Cursor cursor, int index) {
		if (index == cursor.getColumnIndex(AlertsContract.RECEIVED_AT)) {
			long when = cursor.getLong(index);
			((TextView) view).setText(Utils.formatTimeStampString(context, when));
			return true;
		} else {
			return false;
		}
	}
}