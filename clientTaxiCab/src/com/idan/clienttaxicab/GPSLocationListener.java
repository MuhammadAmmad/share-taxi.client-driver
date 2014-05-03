package com.idan.clienttaxicab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class GPSLocationListener implements LocationListener {
	private Context context;
	private String AndroidId;
	private String lineNumber;

	public GPSLocationListener(Context context) {
		this.context = context;
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		AndroidId = Settings.Secure.getString(
				this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
		// sendLocToServer(new DataStructure(lat, lng, AndroidId, "4", "N"));
		UpdateServerTask serverTask = new UpdateServerTask();
		DataStructure dataStructure = new DataStructure(lat, lng, AndroidId);
		serverTask.execute(new DataStructure[] { dataStructure });
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this.context, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this.context, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

}
