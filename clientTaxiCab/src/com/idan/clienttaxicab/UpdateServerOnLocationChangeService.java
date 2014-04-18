package com.idan.clienttaxicab;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class UpdateServerOnLocationChangeService extends Service{
	
	private Thread serviceThread;
	
	@Override
	  public void onCreate() {
		
		serviceThread = new Thread(new Runnable() {
	        public void run() {
	            //TODO
	        }
	    });
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	      serviceThread.start();

	      // If we get killed, after returning from here, restart
	      return START_STICKY;
	  }

	  @Override
	  public void onDestroy() {
	    Toast.makeText(this, "stopped sending location updates to ShareTaxiServer", Toast.LENGTH_SHORT).show();
	  }

	@Override
	public IBinder onBind(Intent arg0) {
		//this service does not support binding. 
		return null;
	}

}
