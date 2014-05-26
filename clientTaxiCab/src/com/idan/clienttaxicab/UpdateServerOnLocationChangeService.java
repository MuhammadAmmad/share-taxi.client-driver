package com.idan.clienttaxicab;

import com.idan.clienttaxicab.ShowLocationActivity.ResponseReceiver;

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

	private final long MINIMUM_TIME_FOR_UPDATE = 5000;// by milliseconds
	private final long MINIMUM_DISTANCE_FOR_UPDATE = 5;// by meters

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

		// if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		// {
		// promptUserToEnableGPS();
		// }
		 
		 locationListener = new GPSLocationListener(getBaseContext());
		
		
		
		// Define the criteria how to select the location provider -> use
		// default
		// Criteria criteria = new Criteria();
		// provider = locationManager.getBestProvider(criteria, false);

		

		// serviceThread = new Thread(new Runnable() {
		// public void run() {
		// locationManager.requestLocationUpdates(provider, 1, 1,
		// locationListener);
		// }
		// });

	}

	 

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if (isActive == false) 
		{
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MINIMUM_TIME_FOR_UPDATE,
					MINIMUM_DISTANCE_FOR_UPDATE, locationListener);
			
			Location location = null;
			for (int i = 0; i < 60; i++)
			{
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location == null)
				{
					try 
					{
						Thread.sleep(500);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
				else
				{
					break;
				}
			}
			
			if (location == null)
			{
				Toast.makeText(getBaseContext(),
						"there is an error getting the device's location, please try again later",
						Toast.LENGTH_SHORT).show();
				
				locationManager.removeUpdates(locationListener);
				
				// processing done here�.
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
				sendBroadcast(broadcastIntent);
				
//				isActive = false;
				
				return START_REDELIVER_INTENT;
			}
			
			isActive = true;
			
//			locationManager.requestLocationUpdates(
//					LocationManager.GPS_PROVIDER, MINIMUM_TIME_FOR_UPDATE,
//					MINIMUM_DISTANCE_FOR_UPDATE, locationListener);
			
			this.lineNumber = intent.getStringExtra("lineNumber");
			locationListener.setLineNumber(this.lineNumber);
			Toast.makeText(getBaseContext(),
					"drive safe!",
					Toast.LENGTH_SHORT).show();
			
			
			this.deviceID = Settings.Secure.getString(
					getContentResolver(), Settings.Secure.ANDROID_ID);
			
			SendStartSignalTask serverTask = new SendStartSignalTask();
			DataStructure dataStructure = new DataStructure(location.getLatitude(), location.getLongitude(), this.deviceID, this.lineNumber);
//			DataStructure dataStructure = new DataStructure(31.11, 31.11, this.deviceID, this.lineNumber);
			serverTask.execute(new DataStructure[] { dataStructure });

			


			// If we get killed, after returning from here, restart
			return START_REDELIVER_INTENT;
		} else {
			return START_REDELIVER_INTENT;
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
