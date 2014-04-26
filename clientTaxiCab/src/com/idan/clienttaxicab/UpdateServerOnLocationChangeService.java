package com.idan.clienttaxicab;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
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

	@Override
	public void onCreate() {
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isActive == false) {
			isActive = true;
			this.lineNumber = intent.getStringExtra("lineNumber");
			locationListener.setLineNumber(this.lineNumber);
			Toast.makeText(getBaseContext(),
					"service ************************  starting",
					Toast.LENGTH_SHORT).show();

			// serviceThread.start();

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
