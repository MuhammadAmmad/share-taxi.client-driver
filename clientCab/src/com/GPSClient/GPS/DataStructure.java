package com.GPSClient.GPS;

import com.example.gpstracking.R.string;

public class DataStructure {
	
	double latitude;
	double longitude;
	String AndroidID;
	String lineNum;
	String direction;
	
	public DataStructure(double latitude, double longitude, String AndroidID, String lineNum, String direction)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.AndroidID = AndroidID;
		this.lineNum = lineNum;
		this.direction = direction;
	}

}
