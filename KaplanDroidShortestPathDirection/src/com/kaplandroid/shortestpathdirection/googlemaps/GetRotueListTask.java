package com.kaplandroid.shortestpathdirection.googlemaps;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.kaplandroid.shortestpathdirection.googlemaps.GMapV2Direction.DirecitonReceivedListener;

public class GetRotueListTask extends AsyncTask<Void, Void, Void> {
	private final Context mContext;
	GMapV2Direction mGMDirection = new GMapV2Direction();
	LatLng fromPosition;
	LatLng toPosition;
	List<LatLng> mPointList;
	private ProgressDialog dialog;
	private int mDirectionMode;

	DirecitonReceivedListener mListener;
 
	public GetRotueListTask(Context context, LatLng fromPosition,
			LatLng toPosition, int mDirectionMode,
			DirecitonReceivedListener mListener) {
		this.mContext = context;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.mDirectionMode = mDirectionMode;
		this.mListener = mListener;
	}

	@Override
	protected Void doInBackground(Void... params) {
		mGMDirection.setParams(fromPosition, toPosition, mDirectionMode);
		mPointList = mGMDirection.getPointList(this.mContext);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}

		if (mPointList != null) {
			mListener.OnDirectionListReceived(mPointList);
		} else {
			Toast.makeText(this.mContext, "Error downloading direction!",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		ConnectivityManager conMgr = (ConnectivityManager) mContext
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnectedOrConnecting()) {
			// Background: Connected to internet
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Downloading directions...");
			dialog.show();
		} else {
			this.cancel(true);
			Toast.makeText(mContext, "Not connected to internet!",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}
