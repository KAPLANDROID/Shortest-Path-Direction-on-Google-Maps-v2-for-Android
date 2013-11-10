package com.kaplandroid.shortestpathdirection.googlemaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author KAPLANDROID
 */
public class GMapV2Direction {

	LatLng src, dest;
	public List<LatLng> pointToDraw;

	public static final int MODE_DRIVING = 1;
	public static final int MODE_WALKING = 2;

	public int mDirectionMode;

	public void setParams(LatLng src, LatLng dest, int mMode) {
		this.src = src;
		this.dest = dest;
		this.mDirectionMode = mMode;
	}

	public List<LatLng> getPointList(Context mContext) {
		if (src != null || dest != null) {
			// connect to map web service
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(makeUrl(src, dest));
			HttpResponse response;
			try {
				response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				InputStream is = null;

				is = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				reader.close();
				String result = sb.toString();
				JSONObject jsonObject = new JSONObject(result);
				JSONArray routeArray = jsonObject.getJSONArray("routes");
				JSONObject routes = routeArray.getJSONObject(0);
				JSONObject overviewPolylines = routes
						.getJSONObject("overview_polyline");
				String encodedString = overviewPolylines.getString("points");
				pointToDraw = decodePoly(encodedString);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return pointToDraw;
		} else {
			throw new NullPointerException(
					"Source or Destination coordinate is null. You must call \"setParams(LatLng,LatLng)\" method first!");
		}
	}

	private List<LatLng> decodePoly(String poly) {

		int len = poly.length();
		int index = 0;
		List<LatLng> decoded = new LinkedList<LatLng>();
		int lat = 0;
		int lng = 0;

		while (index < len) {
			int b;
			int shift = 0;
			int result = 0;
			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			decoded.add(new LatLng((lat / 1E5), (lng / 1E5)));
		}

		return decoded;
	}

	private String makeUrl(LatLng src, LatLng dest) {

		StringBuilder urlString = new StringBuilder();

		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		// from
		urlString.append("?origin=");
		urlString.append(Double.toString((double) src.latitude));
		urlString.append(",");
		urlString.append(Double.toString((double) src.longitude));
		// to
		urlString.append("&destination=");
		urlString.append(Double.toString((double) dest.latitude));
		urlString.append(",");
		urlString.append(Double.toString((double) dest.longitude));
		urlString.append("&sensor=false&units=metric");

		if (mDirectionMode == MODE_DRIVING) {
			urlString.append("&mode=driving");
		} else if (mDirectionMode == MODE_WALKING) {
			urlString.append("&mode=walking");
		}

		Log.d("Request URL", "URL=" + urlString.toString());
		return urlString.toString();
	}

	public interface DirecitonReceivedListener {
		public void OnDirectionListReceived(List<LatLng> mPointList);
	}
}
