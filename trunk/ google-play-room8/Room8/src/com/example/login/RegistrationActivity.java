package com.example.login;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.finalapp.StaticVals;
import com.example.home.HomeActivity;
import com.example.home.HomeAdminActivity;
import com.example.login.ConfirmCancelDialog.OnConfirmCancelListener;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SignUpCallback;

public class RegistrationActivity extends Activity implements OnConfirmCancelListener{

	EditText editTextName;
	EditText editTextEmail;
	EditText editTextApartment;
	EditText editTextPassword;
	static boolean isConfirmed;
	ProgressDialog ringProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		actionBar.setCustomView(R.layout.action_register);
		
		editTextName= (EditText)findViewById(R.id.editTextname);
		editTextEmail= (EditText)findViewById(R.id.editTextEmailReg);
		editTextApartment= (EditText)findViewById(R.id.editTextApartment);
		editTextPassword= (EditText)findViewById(R.id.editTextPasswordReg);

		final Button button = (Button) findViewById(R.id.registerbutton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ringProgressDialog = ProgressDialog.show(RegistrationActivity.this, "Please wait", "waiting for server");
				
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereEqualTo("Apartment", editTextApartment.getText().toString());
				query.findInBackground(new FindCallback<ParseUser>() {
					public void done(List<ParseUser> objects, ParseException e) {
						if (e == null) {
							if(ringProgressDialog.isShowing())
							{
								ringProgressDialog.dismiss();
							}
							
							ConfirmCancelDialog new_dialog= new ConfirmCancelDialog();

							if(isFieldsEmpty()) //checks the editText fields and if one or more is empty do nothing and note errors.
								return;

							if(objects.isEmpty())
							{	//this apartment is new to server. 
								//create this user as an admin if he wants.
								Bundle args = new Bundle();
								args.putString(ConfirmCancelDialog.ARG_STRING_ID, getResources().getString(R.string.newAdmin));
								args.putBoolean(ConfirmCancelDialog.ARG_FLAG, true);
								new_dialog.setArguments(args);
								new_dialog.show(getFragmentManager(), "newAdminDialog");
							}
							else
							{	//this apartment exists in our server.
								//send request to admin for confirm this user as roommate
								Bundle args = new Bundle();
								args.putString(ConfirmCancelDialog.ARG_STRING_ID, getResources().getString(R.string.newUser));
								args.putBoolean(ConfirmCancelDialog.ARG_FLAG, false);
								new_dialog.setArguments(args);
								new_dialog.show(getFragmentManager(), "newUserDialog");
							}

						} else {
							Toast.makeText(RegistrationActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
							if(ringProgressDialog.isShowing())
							{
								ringProgressDialog.dismiss();
							}
						}
					}
				});
			}
		});
	}

	protected void setUser(boolean isAdmin){
		ringProgressDialog = ProgressDialog.show(RegistrationActivity.this, "Please wait", "waiting for server");

		ParseUser user = new ParseUser();
		user.setUsername(editTextEmail.getText().toString());
		user.setPassword(editTextPassword.getText().toString());
		user.setEmail(editTextEmail.getText().toString());
		user.put("Apartment", editTextApartment.getText().toString());
		user.put("Admin", isAdmin);
		user.put("Name", editTextName.getText().toString());
		user.put("isConfirmed", isConfirmed);
		user.put("BudgetfirstTime",true);

		user.signUpInBackground(new SignUpCallback() {
			public void done(ParseException e) {
				if (e == null) {
					//save in usersAuthoirzed
					ParseObject object = new ParseObject("usersAuthoirzed");
					object.put("isConfirmed", isConfirmed);
					object.put("Apartment", editTextApartment.getText().toString());
					object.put("Name", editTextName.getText().toString());
					object.put("Email", editTextEmail.getText().toString());
					try {
						object.save();
					} catch (ParseException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					PushService.subscribe(
							RegistrationActivity.this,
							RegistrationActivity.this.editTextApartment.getText().toString(), 
							PushApartmentActivity.class
							);//subscribe this user/admin to channel name of apartment

					ParseInstallation installation = ParseInstallation.getCurrentInstallation();
					installation.add("Name_Apartment", editTextName.getText().toString()+"_"+editTextApartment.getText().toString());
					try {
						installation.save();
					} catch (ParseException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					if(RegistrationActivity.isConfirmed) // this is admin
					{
						//---- setting the admin push activity and conditions for querys ----//
						PushService.subscribe(
								RegistrationActivity.this,
								RegistrationActivity.this.editTextApartment.getText().toString()
								+ "Admin", 
								HomeActivity.class
								);//subscribe this admin to channel name of apartment admin

						try {
							installation.save();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						if(ringProgressDialog.isShowing())
						{
							ringProgressDialog.dismiss();
						}
						
						Intent intent = new Intent(RegistrationActivity.this, HomeAdminActivity.class);
						startActivity(intent);						
					}
					else // this is a user that is not an admin
					{	
						//find SSID of admin and put it in SSID field
						ParseQuery<ParseUser> query = ParseUser.getQuery();
						query.whereEqualTo("Admin", true);
						query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
						query.findInBackground(new FindCallback<ParseUser>() {
							public void done(List<ParseUser> objects, ParseException e) {
								if (e == null) 
								{
									//put SSID of admin in this user's SSID
									ParseUser.getCurrentUser().put("SSID", objects.get(0).get("SSID"));
									ParseUser.getCurrentUser().saveInBackground();
								} 
								else 
								{
									// Something went wrong.
								}
							}
						});


						Toast.makeText(RegistrationActivity.this, getResources().getString(R.string.waiting_for_confirmAdmin), Toast.LENGTH_LONG).show();

						//send push request to admin to confirm this user//
						JSONObject data= new JSONObject();
						try {
							data.put("Name",RegistrationActivity.this.editTextName.getText().toString());
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
						try {
							data.put("Email",RegistrationActivity.this.editTextEmail.getText().toString());
						} catch (JSONException e1) {
							e1.printStackTrace();
						}

						// Send push notification to query
						ParsePush push = new ParsePush();
						push.setMessage(RegistrationActivity.this.editTextName.getText().toString() + 
								" "+getResources().getString(R.string.ask_admin_confirm_notification));
						push.setChannel(RegistrationActivity.this.editTextApartment.getText().toString() + "Admin");
						push.setData(data);
						push.sendInBackground();

						if(ringProgressDialog.isShowing())
						{
							ringProgressDialog.dismiss();
						}
						
						finish();
					}
				} 
				else {
					// Sign up didn't succeed. Look at the ParseException
					// to figure out what went wrong
					switch(e.getCode())
					{
					case ParseException.USERNAME_TAKEN:
						RegistrationActivity.this.editTextEmail.setError(getResources().getString(R.string.existing_email));
						break;

					case ParseException.INVALID_EMAIL_ADDRESS:
						RegistrationActivity.this.editTextEmail.setError(getResources().getString(R.string.error_invalid_email));
						break;

					default:
						e.printStackTrace();
						Toast.makeText(RegistrationActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
						break;
					}
				}
			}
		});
	}

	@Override
	public void onCancelSelected(boolean isAdminDialog) {
		isConfirmed= isAdminDialog;
		if(isAdminDialog)
		{
			Toast.makeText(RegistrationActivity.this, getResources().getString(R.string.incorrect_apartment), Toast.LENGTH_LONG).show();
			RegistrationActivity.this.editTextApartment.setError(getResources().getString(R.string.error_apartment));
		}
		else
		{
			Toast.makeText(RegistrationActivity.this, getResources().getString(R.string.existing_apartment), Toast.LENGTH_LONG).show();
			RegistrationActivity.this.editTextApartment.setError(getResources().getString(R.string.error_apartment));
		}
	}

	@Override
	public void onConfirmSelected(boolean isAdminDialog) {
		isConfirmed= isAdminDialog;
		
		if(!isAdminDialog)
		{
			if(!isNameExist()) //if the name is already exists in this apartment
				setUser(isAdminDialog);
		}
		else
			setUser(isAdminDialog);
		
	}

	public boolean isFieldsEmpty(){
		boolean isEmptyField= false;
		//----check isempty to all editTexts---//
		if(editTextEmail.getText().length() == 0)
		{
			RegistrationActivity.this.editTextEmail.setError(getResources().getString(R.string.error_missing_email));
			isEmptyField= true;
		}
		if(editTextPassword.getText().length() == 0)
		{
			RegistrationActivity.this.editTextPassword.setError(getResources().getString(R.string.error_missing_password));
			isEmptyField= true;
		}
		if(editTextApartment.getText().length() == 0)
		{
			RegistrationActivity.this.editTextApartment.setError(getResources().getString(R.string.error_missing_apartment));
			isEmptyField= true;
		}	
		if(editTextName.getText().length() == 0)
		{
			RegistrationActivity.this.editTextName.setError(getResources().getString(R.string.error_missing_name));
			isEmptyField= true;
		}
		return isEmptyField;
	}
	
	private boolean isNameExist() {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("Apartment", editTextApartment.getText().toString());
		try {
			List<ParseUser> users = query.find();
			for(ParseUser user : users)
			{
				if(((String)user.get("Name")).equals(editTextName.getText().toString()))
				{
					RegistrationActivity.this.editTextName.setError(getResources().getString(R.string.error_name_exists));
					return true;
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
