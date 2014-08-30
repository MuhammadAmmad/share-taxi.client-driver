package com.idan.clienttaxicab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

class UpdateServerTask extends AsyncTask<DataStructure, Void, Void> {
	@Override
	protected Void doInBackground(DataStructure... dataStructures) 
	{
		try {
			for (DataStructure dataStructure : dataStructures) 
			{
				sendLocationUpdateToServer(dataStructure);
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void sendLocationUpdateToServer(DataStructure curr_location) 
	{
		final HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://sharetaxi6.appspot.com/postLocation");

		try {
			String longitudeStr = String.valueOf(curr_location.longitude);
			String latitudeStr = String.valueOf(curr_location.latitude);

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("longitude", longitudeStr));
			pairs.add(new BasicNameValuePair("latitude", latitudeStr));
			pairs.add(new BasicNameValuePair("androidID",
					curr_location.AndroidID));
			httppost.setEntity(new UrlEncodedFormEntity(pairs));

			// Execute HTTP Post Request
			httpclient.execute(httppost);

		} catch (ClientProtocolException e) 
		{
			
		} catch (IOException e) 
		{
			
		}

	}

	
}
