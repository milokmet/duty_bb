package com.symtoo.duty;

import com.symtoo.duty.data.AlertsContract;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlertAdapter extends SimpleCursorAdapter {

	protected LayoutInflater mInflater;
	private Context context;
	private Resources mRes;
	
	public AlertAdapter(Context ctx, int layout, Cursor c, String[] from, int[] to, int i) {
		super(ctx, layout, c, from, to, i);
		context = ctx;
		setViewBinder(new AlertsViewBinder(context));
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRes = context.getResources();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.d("BindView", "robim");
		super.bindView(view, context, cursor);
		if (cursor.getInt(cursor.getColumnIndex(AlertsContract.SEEN)) == 0) {
			view.setBackgroundColor(context.getResources().getColor(R.color.unread_background));
		} else {
			view.setBackgroundColor(context.getResources().getColor(R.color.read_background));
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row, null);
			
			holder = new ViewHolder();
			holder.body = (TextView) convertView.findViewById(R.id.body);
			holder.receivedAt = (TextView) convertView.findViewById(R.id.received_at);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Cursor c = getCursor();
		c.moveToPosition(position);
		
		if (c.getInt(c.getColumnIndex(AlertsContract.SEEN)) == 0) {
			convertView.setBackgroundColor(mRes.getColor(R.color.unread_background));
		} else {
			convertView.setBackgroundColor(mRes.getColor(R.color.read_background));
		}
		
		holder.body.setText(c.getString(c.getColumnIndex(AlertsContract.BODY)));
		holder.receivedAt.setText(Utils.formatTimeStampString(context, c.getLong(c.getColumnIndex(AlertsContract.RECEIVED_AT))));
		
		// return super.getView(position, convertView, parent);
		return convertView;
	}
	
	static class ViewHolder {
		TextView body;
		TextView receivedAt;
	}
}
