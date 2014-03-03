package com.example.home;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.finalapp.R;
import com.example.home.HomeRequestsDialog.OnSelectedListener;
import com.example.login.RegistrationActivity;
import com.parse.ParseUser;

public class HomeAdminActivity extends Activity implements OnSelectedListener{
	EditText edit_text_SSID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_admin);

		edit_text_SSID= (EditText)findViewById(R.id.editTextSSID);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.action_get_ssid);

		final ImageButton pickSSIDbtn = (ImageButton) findViewById(R.id.pick_SSID);
		pickSSIDbtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{
				ArrayList<String> ssidList = new ArrayList<String>();
				WifiManager wifiManager = (WifiManager) HomeAdminActivity.this.getSystemService(Context.WIFI_SERVICE);

				//--- make sure that wifi is enabled ---//
				if(!wifiManager.isWifiEnabled())
				{
					Bundle bun = new Bundle();
					bun.putString(Alert_Dialog.ARG_STRING_ID, "Enable Wifi to view available networks");

					DialogFragment new_dialog = new Alert_Dialog();
					new_dialog.setArguments(bun);
					new_dialog.show(getFragmentManager(), "alert");
				}
				else
				{
					List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
					while(list == null)
					{
						list = wifiManager.getConfiguredNetworks();
					}

					for(WifiConfiguration wifi : list ) 
					{
						ssidList.add(wifi.SSID);
					}

					CharSequence[] ssidNames;
					ssidNames = new CharSequence[ssidList.size()];
					for(int i=0; i<ssidList.size(); i++)
					{
						ssidNames[i] = (CharSequence)(ssidList.get(i).subSequence(1, ssidList.get(i).length() - 1));
					}

					Bundle bun = new Bundle();
					bun.putInt(HomeRequestsDialog.DIALOG_NAME, R.string.dialogSSID);
					bun.putCharSequenceArray(HomeRequestsDialog.NAMES, ssidNames);
					bun.putStringArrayList(HomeRequestsDialog.USERS_AMES,ssidList); // don't care (only for suits to the dialog)

					DialogFragment new_dialog = new HomeRequestsDialog();
					new_dialog.setArguments(bun);
					new_dialog.show(getFragmentManager(), "ssidDialog");
				}
			}           
		});

		final Button button = (Button) findViewById(R.id.continue_button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{
				if(!isFieldEmpty())
				{
					//put SSID in admin's row
					ParseUser.getCurrentUser().put("SSID", edit_text_SSID.getText().toString());
					ParseUser.getCurrentUser().saveInBackground();
					//go to home_continue
					Intent intent = new Intent(HomeAdminActivity.this, HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(String name, String user_name) {
		edit_text_SSID.setText(name);
	}
	
	private boolean isFieldEmpty() {
		if(edit_text_SSID.getText().length() == 0)
		{
			HomeAdminActivity.this.edit_text_SSID.setError("missing wifi network");
			return true;
		}
		return false;
	}
}
