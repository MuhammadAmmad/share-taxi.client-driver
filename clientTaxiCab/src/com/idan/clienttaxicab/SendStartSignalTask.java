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

import android.os.AsyncTask;

class SendStartSignalTask extends AsyncTask<DataStructure, Void, Void> {
	
	@Override
	protected Void doInBackground(DataStructure... dataStructures) 
	{
		try {
			for (DataStructure dataStructure : dataStructures) {
				sendStartSignalToServer(dataStructure);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private void sendStartSignalToServer(DataStructure curr_location) 
	{
		final HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://sharetaxi6.appspot.com/startDevice");

		try {
			// Add your data
//			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("longitude", String.valueOf(curr_location.longitude)));
			pairs.add(new BasicNameValuePair("latitude", String.valueOf(curr_location.latitude)));
			pairs.add(new BasicNameValuePair("androidID", curr_location.AndroidID));
			pairs.add(new BasicNameValuePair("lineNum", curr_location.lineNum));
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
}
