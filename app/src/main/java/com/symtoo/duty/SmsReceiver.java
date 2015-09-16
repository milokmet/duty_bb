package com.symtoo.duty;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.symtoo.duty.data.AlertsContract;

public class SmsReceiver extends BroadcastReceiver 
{
	private static final List<String> ALERTS_FROM = Arrays.asList(AlertsContract.TYPE_ALARM, AlertsContract.TYPE_BACKUP);
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Log.d("SmsReceiver", "intent action received - SMS_RECEIVED");
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			HashMap<String, Sms> msg;
			String msgFrom;
			
			if (bundle != null && bundle.containsKey("pdus")) {
				try {
					Object[] pdus = (Object[]) bundle.get("pdus");
					msg = new HashMap<String, Sms>(pdus.length);
					msgs = new SmsMessage[pdus.length];
					
					for (int i=0; i < msgs.length; i++) {
						msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
						msgFrom = msgs[i].getOriginatingAddress();
						
						if (!msg.containsKey(msgFrom)) {
							// sender does not exists
							msg.put(msgFrom, new Sms(msgs[i]));
						} else {
							msg.get(msgFrom).merge(msgs[i]);
						}
					}
					
					for (String key : msg.keySet()) {
						if (ALERTS_FROM.contains(key)) {
							abortBroadcast();
							saveAlert(context, msg.get(key));
						}
					}

				} catch (Exception e) {
					// TODO: Exception
				}
			}
		}
	}
	
	protected void saveAlert(Context context, Sms sms) {
		Log.d("saveAlert", "Starting the alert saving");
		ContentValues values = new ContentValues();
		values.put(AlertsContract.BODY, sms.getBody());
		values.put(AlertsContract.TYPE, sms.getFrom());
		values.put(AlertsContract.RECEIVED_AT, sms.getReceivedAt());
		
		Uri uri = AlertsContract.CONTENT_URI_ALARMS;
		if (sms.getFrom().equals(AlertsContract.TYPE_BACKUP)) {
			uri = AlertsContract.CONTENT_URI_BACKUPS;
		}
		
		Uri insUri = context.getContentResolver().insert(uri, values);
		Log.d("DutySMS", "uri: " + insUri);
	}
	
	public class Sms {
		protected String body;
		protected String from;
		protected long receivedAt;
		
		public Sms(SmsMessage message) {
			body = message.getMessageBody();
			from = message.getOriginatingAddress();
			receivedAt = message.getTimestampMillis();
		}
		
		public void merge(SmsMessage message) {
			body = body + message.getMessageBody();
		}
		
		public String getBody() {
			return body;
		}
		
		public String getFrom() {
			return from;
		}
		
		public long getReceivedAt() {
			return receivedAt;
		}
	}
}
