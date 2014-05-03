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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class UpdateServerOnLocationChangeService extends Service {

	// private Thread serviceThread;

	private final long MINIMUM_TIME_FOR_UPDATE = 1000;// by milliseconds
	private final long MINIMUM_DISTANCE_FOR_UPDATE = 1;// by meters

	private GPSLocationListener locationListener;
	private LocationManager locationManager;
	// private String provider;
	// private Intent intent;
	private boolean isActive = false;
	private String lineNumber;
	private String deviceID;

	@Override
	public void onCreate() {
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		 if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		 {
		 promptUserToEnableGPS();
		 }
		// Define the criteria how to select the location provider -> use
		// default
		// Criteria criteria = new Criteria();
		// provider = locationManager.getBestProvider(criteria, false);

		locationListener = new GPSLocationListener(getBaseContext());

		// serviceThread = new Thread(new Runnable() {
		// public void run() {
		// locationManager.requestLocationUpdates(provider, 1, 1,
		// locationListener);
		// }
		// });

	}

	 private void promptUserToEnableGPS()
	 {
		 
		 Toast.makeText(getApplicationContext(),
					 "stopped sending location updates to ShareTaxiServer",
					 Toast.LENGTH_SHORT).show();
//	 Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//	 startActivity(intent);
	 }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isActive == false) {
			isActive = true;
			this.lineNumber = intent.getStringExtra("lineNumber");
			locationListener.setLineNumber(this.lineNumber);
			Toast.makeText(getBaseContext(),
					"service ************************  starting",
					Toast.LENGTH_SHORT).show();
			
			
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			
			this.deviceID = Settings.Secure.getString(
					getContentResolver(), Settings.Secure.ANDROID_ID);
			
			SendStartSignalTask serverTask = new SendStartSignalTask();
			DataStructure dataStructure = new DataStructure(location.getLatitude(), location.getLongitude(), this.deviceID, this.lineNumber);
//			DataStructure dataStructure = new DataStructure(31.11, 31.11, this.deviceID, this.lineNumber);
			serverTask.execute(new DataStructure[] { dataStructure });

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MINIMUM_TIME_FOR_UPDATE,
					MINIMUM_DISTANCE_FOR_UPDATE, locationListener);


			// If we get killed, after returning from here, restart
			return START_STICKY;
		} else {
			return START_STICKY;
		}

	}

	@Override
	public void onDestroy() {
		this.isActive = false;
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
		// Toast.makeText(getApplicationContext(),
		// "stopped sending location updates to ShareTaxiServer",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// this service does not support binding.
		return null;
	}
	
	

}
