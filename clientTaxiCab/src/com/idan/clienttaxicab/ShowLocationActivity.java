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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ShowLocationActivity extends Activity  
{
	private Button btnStartService;
	private Button btnStopService;
	private Intent intent;

	  @Override
	  public void onCreate(Bundle savedInstanceState) 
	  {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_show_location);
	    
	    btnStartService = (Button) findViewById(R.id.buttonStart);
	    btnStartService.setOnClickListener(new View.OnClickListener() 
	    {			
			@Override
			public void onClick(View arg0) 
			{	
				intent = new Intent(getBaseContext(), UpdateServerOnLocationChangeService.class);
			    startService(intent);
		        Toast.makeText(getApplicationContext(), "starting service", Toast.LENGTH_SHORT).show();
			}
		});
	    
	    btnStopService = (Button) findViewById(R.id.buttonStop);
	    btnStopService.setOnClickListener(new View.OnClickListener() 
	    {			
			@Override
			public void onClick(View arg0) 
			{
				SendStopSignalToServerTask sendStopSignalToServerTask = new SendStopSignalToServerTask();
				String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
				sendStopSignalToServerTask.execute(androidID);
			    stopService(new Intent(getBaseContext(), UpdateServerOnLocationChangeService.class));
		        Toast.makeText(getApplicationContext(), "stopping service", Toast.LENGTH_SHORT).show();
			}
		});
	  }
	  
	  private class SendStopSignalToServerTask extends AsyncTask<String, Void, Void>// TODO do do
	  {
			@Override
			protected Void doInBackground(String... deviceIDs){			
				try {				
					for(String deviceID : deviceIDs){
						sendStopSignalToServer(deviceID);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				return null;
			}
	  }
	  
	  public void sendStopSignalToServer(String deviceID) 
	  {
			final HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://sharetaxi6.appspot.com/stopDevice");

			try {
				// Add your data
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("androidID", deviceID));
				httppost.setEntity(new UrlEncodedFormEntity(pairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost); //TODO handle response from server

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

    	}
	  
	  
	  
      
      
      

	  /* Request updates at startup */
//	  @Override
//	  protected void onResume() {
//	    super.onResume();
//	    Intent locationServiceIntent = new Intent(this, UpdateServerOnLocationChangeService.class);
//	    this.startService(locationServiceIntent);
//	  }
//
//	  /* Remove the locationlistener updates when Activity is paused */
//	  @Override
//	  protected void onPause() {
//	    super.onPause();
//	  }
} 