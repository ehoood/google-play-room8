package com.example.home;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;

import com.example.finalapp.R;
import com.example.finalapp.StaticVals;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class MyCustomReceiver extends BroadcastReceiver{
	String mSSID_name;
	boolean isEnabled= false;
	WifiManager wifi;
	String mApartment;
	String mName_Apartment;
	boolean isAck;

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
					isAck= json.getBoolean("isAck");

					if(isAck)
					{ //got ack from room8
						String name= json.getString("name");
						boolean isHome= json.getBoolean("isHome");

						for(HomeObj room8 : SThome.getInstance())
						{
							if(room8.name.equals(name))
							{
								if(isHome)
								{
									room8.visibiltyProg = 4; //invisible
									room8.window = R.drawable.window;	
								}
								else
								{
									room8.visibiltyProg = 4; //invisible
									room8.window = R.drawable.window_dark;
								}
							}
						}
						
						if(HomeActivity.mHandler != null)
						{
							//send message to handler so it will notify the adapter in homeActivity
							Message msg = HomeActivity.mHandler.obtainMessage();
							HomeActivity.mHandler.sendMessage(msg);
						}
					}
					else
					{ //check if in home
						mSSID_name= json.getString("SSID");
						mApartment= json.getString("Apartment");
						mName_Apartment= json.getString("Name_Apartment"); 

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
						String name = new String();
						for(String str : strArray)
						{
							StringTokenizer nameApartment = new StringTokenizer(str, "_");
							String tempName = nameApartment.nextToken();
							String tempApart = nameApartment.nextToken();
							if(tempApart.equals(mApartment))
							{
								name = tempName;
								break;
							}
						}

						boolean isHome;
						
						if(i < wifiList.size())
						{//our SSID is in range
							double dist = calculateDistance(wifiList.get(i).level-30, wifiList.get(i).frequency);

							if(dist <= 5)
							{
								isHome = true;
							}
							else
							{
								isHome = false;
							}
						}
						else
						{
							isHome = false;
						}


						if(!isEnabled)
							wifi.setWifiEnabled(false);

						sendAck(isHome, name);
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

	void sendAck(boolean isHome, String name)
	{
		try {
			JSONObject data = new JSONObject();
			data.put("isAck", true);
			data.put("action", "com.example.UPDATE_IS_HOME");
			data.put("name", name);
			data.put("isHome", isHome);
			ParsePush push = new ParsePush();
			push.setChannel(mApartment); // Set our Installation query
			push.setData(data);
			push.sendInBackground(new SendCallback() {
				@Override
				public void done(ParseException e) {
					if(e != null)
					{ //error
						e.printStackTrace();
					}
				}
			});
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
