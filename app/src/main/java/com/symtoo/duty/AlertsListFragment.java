package com.symtoo.duty;

import com.symtoo.duty.data.AlertsContract;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.util.Log;

public class AlertsListFragment extends ListFragment implements
			LoaderManager.LoaderCallbacks<Cursor>{
	
	public static final String ARG_TYPE = "alert_type";
	private AlertAdapter adapter;
	Handler handler = new Handler();
	protected String type;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		this.type = args.getString(ARG_TYPE);

		fillData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void markAsRead() {
		Log.d("MREAD", "Marking as read");
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... none) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				markItemsAsRead();
				return null;
			}
		}.execute();
	}

	private void markItemsAsRead() {
		
		if (type == null) {
			Log.d("ListFragment", "MarkAsRead: Type is null");
			return ;
		}
		
		ContentValues values = new ContentValues();
		values.put(AlertsContract.SEEN, "1");
		
		String where = AlertsContract.SEEN + " = ?";
		String[] selectionArgs = { "0" };
		
		Uri uri = AlertsContract.CONTENT_URI_ALARMS;
		if (type.equals(AlertsContract.TYPE_BACKUP)) {
			uri = AlertsContract.CONTENT_URI_BACKUPS;
		}

		Context context = getActivity();
		try {
			ContentResolver cr = context.getContentResolver();
			// Log.d("MREAD", "Content resolver vybraty");
			int count = cr.update(uri, values, where, selectionArgs);
			Log.d("ListFragment", "markItemAsRead: updated " + count);
		} catch (NullPointerException e) {
			// Log.d("MREAD", "Null poinster exception");
		}
	}

	private void fillData() {
		int loaderCount = 640;
		if (this.type.equals(AlertsContract.TYPE_BACKUP)) {
			loaderCount = 642;
		}
		
		getLoaderManager().initLoader(loaderCount, null, this);
		String[] from = { AlertsContract.BODY, AlertsContract.RECEIVED_AT };
		int[] to = new int[] { R.id.body, R.id.received_at };
		adapter = new AlertAdapter(getActivity(), R.layout.row, null, from, to, 0);
		setListAdapter(adapter);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d("LOADER", "onCreateLoader");
		Uri uri = AlertsContract.CONTENT_URI_ALARMS;
		if (this.type.equals(AlertsContract.TYPE_BACKUP)) {
			uri = AlertsContract.CONTENT_URI_BACKUPS;
		}
		
		CursorLoader cl = new CursorLoader(getActivity(), uri, AlertsContract.PROJECTION, 
								null, null, AlertsContract.DEFAULT_SORT_ORDER);
		return cl;
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		ListView lv = getListView();
		int lastVisible = lv.getLastVisiblePosition();
		int lastCount   = lv.getCount();

		adapter.swapCursor(cursor);
		
		if (lastCount - 1 == lastVisible || lastVisible == -1) {
			lv.setSelection(lv.getCount());
		}
		
		if (isVisible() == true) {
			markAsRead();
		}
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getLoaderManager().destroyLoader(Integer.valueOf(this.type));
	}
}