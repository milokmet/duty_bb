package com.symtoo.duty;

import android.content.Context;
import android.text.format.Time;
import android.text.format.DateUtils;

public class Utils {
	protected static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

	public static String formatTimeStampString(Context context, long when) {
		Time then = new Time();
		then.set(when);
		Time now = new Time();
		now.setToNow();
		
		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
					DateUtils.FORMAT_ABBREV_ALL |
					DateUtils.FORMAT_CAP_AMPM;
					
					
		String datum = DateUtils.formatDateTime(context, when, format_flags);
		
		String time = android.text.format.DateFormat.getTimeFormat(context).format(when);
		
		if (then.yearDay != now.yearDay) {
			return datum + ", " + time;
		} else {
			return time;
		}
	}
}
