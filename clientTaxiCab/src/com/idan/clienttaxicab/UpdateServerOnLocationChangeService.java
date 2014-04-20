package com.idan.clienttaxicab;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class UpdateServerOnLocationChangeService extends Service{
	
	private Thread serviceThread;
	
	private final long MINIMUM_TIME_FOR_UPDATE = 1000;//by milliseconds
	private final long MINIMUM_DISTANCE_FOR_UPDATE = 1;//by meters
	
    private ServiceLocationListener locationListener;
    private LocationManager locationManager;
    private String provider;
    private Intent intent;
    private boolean isActive = false;
    
	
	@Override
	public void onCreate() 
	{
		// Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the location provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
		locationListener = new ServiceLocationListener(getBaseContext());
		
//		serviceThread = new Thread(new Runnable() {
//	        public void run() {
//	        	locationManager.requestLocationUpdates(provider, 1, 1, locationListener);
//	        }
//	    });
		
	 }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) 
	  {
		  if(isActive == false){
			  isActive = true;
			  Toast.makeText(getBaseContext(), "service ************************  starting", Toast.LENGTH_SHORT).show();

//		      serviceThread.start();
		      
		      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_FOR_UPDATE, MINIMUM_DISTANCE_FOR_UPDATE, locationListener);

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
	      //Toast.makeText(getApplicationContext(), "stopped sending location updates to ShareTaxiServer", Toast.LENGTH_SHORT).show();
	  }

	@Override
	public IBinder onBind(Intent arg0) {
		//this service does not support binding. 
		return null;
	}

	
	public class ServiceLocationListener implements LocationListener{
		
		private Context context;
		private String AndroidId;
		
		public ServiceLocationListener(Context context){
			this.context = context;
		}
		
		@Override
		  public void onLocationChanged(Location location) {
		    double lat = location.getLatitude();
		    double lng = location.getLongitude();
		    AndroidId = Settings.Secure.getString(getContentResolver(),
       	         Settings.Secure.ANDROID_ID);
		    //sendLocToServer(new DataStructure(lat, lng, AndroidId, "4", "N"));
		    UpdateServerTask serverTask = new UpdateServerTask();
		    DataStructure dataStructure = new DataStructure(lat, lng, AndroidId, "4", "N");
		    serverTask.execute(new DataStructure[]{dataStructure});
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
	}
	
	public void sendLocToServer(DataStructure curr_location) {

		final HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://sharetaxi6.appspot.com/postLocation");

		try {
			// Add your data
			String longitudeStr = String.valueOf(curr_location.longitude);
			String latitudeStr = String.valueOf(curr_location.latitude);

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("longitude", longitudeStr));
			pairs.add(new BasicNameValuePair("latitude", latitudeStr));
			pairs.add(new BasicNameValuePair("androidID", curr_location.AndroidID));
			pairs.add(new BasicNameValuePair("lineNum", curr_location.lineNum));
			pairs.add(new BasicNameValuePair("direction", curr_location.direction));
			httppost.setEntity(new UrlEncodedFormEntity(pairs));
			
			//Toast.makeText(getBaseContext(), "gps info gathered by now is: long "+ longitudeStr + ", lat " + latitudeStr, Toast.LENGTH_SHORT).show();
			
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

	}
	  
	private class UpdateServerTask extends AsyncTask<DataStructure, Void, Void>{

		@Override
		protected Void doInBackground(DataStructure... dataStructures){			
			try {				
				for(DataStructure dataStructure : dataStructures){
					sendLocToServer(dataStructure);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	
}
