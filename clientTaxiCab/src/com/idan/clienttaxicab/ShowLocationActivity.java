package com.idan.clienttaxicab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ShowLocationActivity extends Activity  {
	  
	/** Called when the activity is first created. */
	private Button btnStartService;
	private Button btnStopService;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_show_location);
//	    latituteField = (TextView) findViewById(R.id.TextView02);
//	    longitudeField = (TextView) findViewById(R.id.TextView04);
	    
	    btnStartService = (Button) findViewById(R.id.buttonStart);
	    btnStartService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {	
				
				Intent intent = new Intent(getBaseContext(), UpdateServerOnLocationChangeService.class);
			    startService(intent);
		        Toast.makeText(getApplicationContext(), "starting service", Toast.LENGTH_SHORT).show();

				
			}
		});
	    
	    btnStopService = (Button) findViewById(R.id.buttonStop);
	    btnStopService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {	
				
				Intent intent = new Intent(getBaseContext(), UpdateServerOnLocationChangeService.class);
			    stopService(intent);
		        Toast.makeText(getApplicationContext(), "stopping service", Toast.LENGTH_SHORT).show();

				
			}
		});
	  
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