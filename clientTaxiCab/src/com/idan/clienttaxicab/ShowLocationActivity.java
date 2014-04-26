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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShowLocationActivity extends Activity {
	
	private Button btnStartService;
	private Button btnStopService;
	private Intent intent;
	protected String lineNumber = null;
	protected String currentState;
	private boolean enableMenu = true;
	private TextView currentStateTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_location);
		this.currentState = "Offline";

		btnStartService = (Button) findViewById(R.id.buttonStart);
		btnStartService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(verifyLineNumberSelected()){
					setOnlineState();
					intent = new Intent(getBaseContext(),
							UpdateServerOnLocationChangeService.class);
					intent.putExtra("lineNumber", lineNumber);
					startService(intent);
					Toast.makeText(getApplicationContext(), "starting service",
							Toast.LENGTH_SHORT).show();// TODO add some static
														// notice for the current
														// app situation
				}
			}
		});

		btnStopService = (Button) findViewById(R.id.buttonStop);
		btnStopService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lineNumber = null;
				setOffineState();
				SendStopSignalToServerTask sendStopSignalToServerTask = new SendStopSignalToServerTask();
				String androidID = Settings.Secure.getString(
						getContentResolver(), Settings.Secure.ANDROID_ID);
				sendStopSignalToServerTask.execute(androidID);
				stopService(new Intent(getBaseContext(),
						UpdateServerOnLocationChangeService.class));
				Toast.makeText(getApplicationContext(), "stopping service",
						Toast.LENGTH_SHORT).show();
			}
		});
		
		currentStateTextView = (TextView) findViewById(R.id.currentState);
		currentStateTextView.setText(currentState);
	}

	protected void setOffineState() {
		enableLineNumberSelection();
		currentState = "Offline";
		currentStateTextView.setText(currentState);
	}

	protected boolean verifyLineNumberSelected() {
		if(lineNumber == null){
			Toast.makeText(getApplicationContext(), "Please select a line number",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

	protected void setOnlineState() {
		disableLineNumberSelection();
		currentState = "Online";
		currentStateTextView.setText(currentState);
	}

	protected void enableLineNumberSelection() {
		this.enableMenu = true;
	}

	protected void disableLineNumberSelection() {
		this.enableMenu = false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupEnabled(R.id.groupMenuItems, enableMenu);
		return true;
	}

	private class SendStopSignalToServerTask extends
			AsyncTask<String, Void, Void>// TODO do do
	{
		@Override
		protected Void doInBackground(String... deviceIDs) {
			try {
				for (String deviceID : deviceIDs) {
					sendStopSignalToServer(deviceID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public void sendStopSignalToServer(String deviceID) {
		final HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://sharetaxi6.appspot.com/stopDevice");

		try {
			// Add your data
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("androidID", deviceID));
			httppost.setEntity(new UrlEncodedFormEntity(pairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost); // TODO handle
																	// response
																	// from
																	// server

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.show_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.four:
			Toast.makeText(this, "You selected line number 4",
					Toast.LENGTH_SHORT).show();
			this.lineNumber = "4";
			break;
		case R.id.fourA:
			Toast.makeText(this, "You selected line number 4a",
					Toast.LENGTH_SHORT).show();
			this.lineNumber = "4a";
			break;
		case R.id.five:
			Toast.makeText(this, "You selected line number 5",
					Toast.LENGTH_SHORT).show();
			this.lineNumber = "5";
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* Request updates at startup */
	// @Override
	// protected void onResume() {
	// super.onResume();
	// Intent locationServiceIntent = new Intent(this,
	// UpdateServerOnLocationChangeService.class);
	// this.startService(locationServiceIntent);
	// }
	//
	// /* Remove the locationlistener updates when Activity is paused */
	// @Override
	// protected void onPause() {
	// super.onPause();
	// }
}