package com.example.home;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.finalapp.StaticVals;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MyCustomReceiver extends BroadcastReceiver{
	String mSSID_name;
	boolean isEnabled= false;
	WifiManager wifi;
	String mApartment;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Parse.initialize(arg0, "6xDnXjWOcfFRJMlrIJxdVnvtjlTqRgtq8VnSh9Qn", "cxSxdJr5nke0qSxPAIYgU8TwLmzsXZnyzM5U4z8t");
		
		final Intent intent = arg1;
		final Context context = arg0;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<ScanResult> wifiList;
				int i;
				try 
				{
					JSONObject json = new JSONObject(intent.getStringExtra("com.parse.Data"));
					mSSID_name= json.getString("SSID");
					mApartment= json.getString("Apartment");

					wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

					//--- make sure that wifi is enabled ---//
					if(wifi.isWifiEnabled())
					{
						isEnabled= true;
					}
					else
						wifi.setWifiEnabled(true);

					wifiList = wifi.getScanResults();
					while(wifiList == null)
					{
						wifiList = wifi.getScanResults();
					}
					i=0;
					while(i < wifiList.size())
					{
						if(wifiList.get(i).SSID.equals(mSSID_name))
							break;

						i++;
					}
					ParseInstallation inst = ParseInstallation.getCurrentInstallation();
					ArrayList<String> strArray = (ArrayList<String>)inst.get("Name_Apartment");
					final ArrayList<String> nameArr = new ArrayList<String>();
					for(String str : strArray)
					{
						if(str.endsWith(mApartment))
						{
							nameArr.add(str.substring(0, str.length() - (mApartment.length() + 1)));
						}
					}
					//					ParseUser user = ParseUser.getCurrentUser();
					ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
					query.whereEqualTo("Apartment", mApartment);
					List<ParseObject> objects;
					try {
						objects = query.find();
						if(i < wifiList.size())
						{//our SSID is in range
							double dist = calculateDistance(wifiList.get(i).level-30, wifiList.get(i).frequency);

							if(dist <= 5)
							{
								for(ParseObject oUser : objects)
								{
									if(nameArr.contains(oUser.getString("Name")))
									{
										oUser.put("IsHome", StaticVals.inHome);
										oUser.save();
									}
								}
							}
							else
							{
								for(ParseObject oUser : objects)
								{
									if(nameArr.contains(oUser.getString("Name")))
									{
										oUser.put("IsHome", StaticVals.outHome);
										oUser.save();
									}

								}
							}
						}
						else
						{
							for(ParseObject oUser : objects)
							{
								if(nameArr.contains(oUser.getString("Name")))
								{
									oUser.put("IsHome", StaticVals.outHome);
									oUser.save();
								}
							}
						}


						if(!isEnabled)
							wifi.setWifiEnabled(false);

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
				catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
		}).start();		

	}

	public double calculateDistance(double signalLevelInDb, double freqInMHz) {
		double exp = (27.55 - (20 * Math.log10(freqInMHz)) + signalLevelInDb) / 20.0;
		return Math.pow(10.0, exp);
	}
}
