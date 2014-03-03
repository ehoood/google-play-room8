package com.example.login;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.home.HomeActivity;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends Activity {
	EditText editTextEmail;
	EditText editTextPass;
	Boolean isEmpty;
	Boolean isEmailValid;
	ProgressDialog ringProgressDialog;
	Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

		Parse.initialize(this, "6xDnXjWOcfFRJMlrIJxdVnvtjlTqRgtq8VnSh9Qn", "cxSxdJr5nke0qSxPAIYgU8TwLmzsXZnyzM5U4z8t");

		editTextEmail = (EditText)findViewById(R.id.editTextEmailLogin);
		editTextPass = (EditText)findViewById(R.id.editTextPasswordLogin);

		// Listening to register new account link
		registerScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
				startActivity(intent);
			}
		});

		button = (Button) findViewById(R.id.btnLogin);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(isFieldsEmpty())
					return; //check if fields are empty
				ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait", "Logging in");
				ParseUser.logInInBackground(editTextEmail.getText().toString(), editTextPass.getText().toString(), new LogInCallback() {
					public void done(ParseUser user, ParseException e) {
						if (user != null) 
						{
							ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("usersAuthoirzed");
							query.whereEqualTo("Email", editTextEmail.getText().toString());
							query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
							query.findInBackground(new FindCallback<ParseObject>() {
								public void done(List<ParseObject> users, ParseException e) {
									if (e == null) 
									{
										if(users.size() != 0)
										{
											if((Boolean)users.get(0).get("isConfirmed"))
											{
												ParseUser.getCurrentUser().put("isConfirmed", true);
												ParseUser.getCurrentUser().saveInBackground();

												if(ringProgressDialog.isShowing())
												{
													ringProgressDialog.dismiss();
												}
												
												Intent intent = new Intent(MainActivity.this, HomeActivity.class);
												intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
												startActivity(intent);
											}
											else 
												Toast.makeText(MainActivity.this, 
														getResources().getString(R.string.waiting_for_confirmAdmin), 
														Toast.LENGTH_LONG
														).show();
										}
										else
										{
											try {
												ParseUser.getCurrentUser().delete();
												ParseUser.getCurrentUser().saveInBackground();
											} catch (ParseException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
										}
									} 
									else
									{
										Toast.makeText(MainActivity.this, 
												getResources().getString(R.string.please_check_internet_connection), 
												Toast.LENGTH_LONG
												).show();
									}
								}
							});
						} 
						else 
						{
							switch(e.getCode())
							{
							case ParseException.ACCOUNT_ALREADY_LINKED:
								Toast.makeText(MainActivity.this, 
										getResources().getString(R.string.error_already_loged_in), 
										Toast.LENGTH_LONG
										).show();
								MainActivity.this.editTextEmail.setError(getResources().getString(R.string.please_change_field));
								MainActivity.this.editTextPass.setError(getResources().getString(R.string.please_change_field));
								break;

							case ParseException.USERNAME_MISSING:
								MainActivity.this.editTextEmail.setError(getResources().getString(R.string.error_invalid_email));
								break;

							case ParseException.PASSWORD_MISSING:
								MainActivity.this.editTextPass.setError(getResources().getString(R.string.error_invalid_password));
								break;

							case ParseException.OBJECT_NOT_FOUND:
								Toast.makeText(MainActivity.this, 
										getResources().getString(R.string.error_incorrect_PasswordOrEmail), 
										Toast.LENGTH_LONG
										).show();
								MainActivity.this.editTextEmail.setError(getResources().getString(R.string.please_change_field));
								MainActivity.this.editTextPass.setError(getResources().getString(R.string.please_change_field));
								break;

							default:
								e.printStackTrace();
								Toast.makeText(MainActivity.this, 
										getResources().getString(R.string.please_check_internet_connection), 
										Toast.LENGTH_LONG
										).show();
								break;
							}
						}
						if(ringProgressDialog.isShowing())
						{
							ringProgressDialog.dismiss();
						}
					}
				});
			}
		});
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean isFieldsEmpty(){
		boolean isEmptyField= false;
		//----check isempty to all editTexts---//
		if(editTextEmail.getText().length() == 0)
		{
			MainActivity.this.editTextEmail.setError(getResources().getString(R.string.error_missing_email));
			isEmptyField= true;
		}
		if(editTextPass.getText().length() == 0)
		{
			MainActivity.this.editTextPass.setError(getResources().getString(R.string.error_missing_password));
			isEmptyField= true;
		}

		return isEmptyField;
	}

}
