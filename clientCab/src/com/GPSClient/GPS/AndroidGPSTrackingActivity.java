package com.GPSClient.GPS;

import android.R.string;
import android.app.Activity;
//import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.example.gpstracking.R;

public class AndroidGPSTrackingActivity extends Activity {
	
	Button btnShowLocation;
	
	
	GPSTracker gps;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        
        
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {		

		        gps = new GPSTracker(AndroidGPSTrackingActivity.this);

						
		        if(gps.canGetLocation()){
		        	
		        	double latitude = gps.getLatitude();
		        	double longitude = gps.getLongitude();
		        	
		        	
		        	Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_SHORT).show();		        	
		        	//new-Limor:
		        	String AndroidId = Settings.Secure.getString(getContentResolver(),
		        	         Settings.Secure.ANDROID_ID);
		        	//TODO: change to real values:
		        	String curr_direction = "N";
		        	String curr_line_num = "4";
		        	location curr_location = new location(latitude,longitude,AndroidId,curr_line_num,curr_direction);
		        	SendToServer dataToServer = new SendToServer();
		        	dataToServer.sendLocToServer(curr_location);
		        	
		        }else{
		        	gps.showSettingsAlert();
		        }
		        
		        //SEND DATA TO SERVER FROM HEAR!!!!!!!!!!!!
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
			}
		});
    }
    
}