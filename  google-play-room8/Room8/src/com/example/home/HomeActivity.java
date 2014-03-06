package com.example.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.finalapp.StaticVals;
import com.example.home.HomeRequestsDialog.OnSelectedListener;
import com.example.login.ConfirmCancelDialog;
import com.example.login.ConfirmCancelDialog.OnConfirmCancelListener;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class HomeActivity extends SplitActionBarActivity implements OnSelectedListener, OnConfirmCancelListener{
	WifiManager wifi;
	boolean isEnabled= false;
	ProgressDialog ringProgressDialog;
	CustomAdapter adapter = null;
	GridView gv;
	ArrayList<HomeObj> homeArr;
	ArrayList<reqObj> requestsArr;
	ActionBar actionBar;
	TextView mReqNotification, mStickyNotification;
	static Handler mHandler;
	String email_roommate;
	int parseAmount; 
	int res;
	int userBudget;
	int monthparse;
	boolean firstTime = true;
	List<ParseObject> mlistDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		homeArr = SThome.getInstance();
		requestsArr = STrequests.getInstance();
		requestsArr.clear();
		homeArr.clear();

		actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));

		ringProgressDialog = ProgressDialog.show(HomeActivity.this, "in progress", "waiting for server...");

		ParseUser user = ParseUser.getCurrentUser();

		if(user.getBoolean("Admin"))
		{
			actionBar.setCustomView(R.layout.action_home_admi);
			mReqNotification = (TextView)findViewById(R.id.home_notification);

			findReq();
			initReqButton();
		}
		else
			actionBar.setCustomView(R.layout.action_home);

		ImageButton takeApic2 = (ImageButton)findViewById(R.id.home_camBtn);
		takeApic2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CamActivity.class);
				startActivity(intent);
			}
		});

		mStickyNotification = (TextView)findViewById(R.id.home_reminder_sticky_notification);
		//set stickynotif button onclick
		initStickyButton();

		//set all roomates to be not connected in field "IsHome"
		new Thread(new Runnable() {
			@Override
			public void run() {
				setAllNotconnected();
				refresh();
				if(HomeActivity.this.ringProgressDialog.isShowing())
					HomeActivity.this.ringProgressDialog.dismiss();
			}
		}).start();
		//--- send push to all apartment roommates ---//
		sendPushAll();

		setRefresh(); //set refresh button

		gv= (GridView)findViewById(R.id.home_list);
		adapter= new CustomAdapter(this, R.layout.home_item, homeArr);
		gv.setAdapter(adapter);

		handlerFunc();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_continue, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Thread(new Runnable() {
			@Override
			public void run() {
				refresh();							
			}
		}).start();		
		adapter.notifyDataSetChanged();

		new Thread(new Runnable() {
			@Override
			public void run() {
				//find all reminder sticky notifications
				findNotesNotf();	
				firstTime = ParseUser.getCurrentUser().getBoolean("BudgetfirstTime");
				//check if there is an exception in the budget
				if (!firstTime)
					checkBudgetNotfications();
			}
		}).start();
	}

	public void checkBudgetNotfications ()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Budget");
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.whereEqualTo("username", ParseUser.getCurrentUser().get("username"));
		try {
			mlistDB = query.find();
			Log.d("listDB", "Retrieved " + mlistDB.size() + " size");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(mlistDB.size() > 0)
		{
			userBudget = mlistDB.get(0).getInt("Amount");
			monthparse = mlistDB.get(0).getInt("Month");
			res = mlistDB.get(0).getInt("Res");

			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int currMonth = cal.get(Calendar.MONTH);
			currMonth++; // just to make January = 1, February =2 etc.
			if(currMonth == monthparse + 1) 
			{
				//next month from the pre budget month 
				ParseQuery<ParseObject> queryLog = ParseQuery.getQuery("LogBudget");
				queryLog.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
				queryLog.whereEqualTo("Name", ParseUser.getCurrentUser().get("Name"));
				List<ParseObject> objects;
				try {
					objects = queryLog.find();
					if(objects.size() > 0)
					{
						Date d = new Date();
						d.setMonth(monthparse - 1);
						StringTokenizer strTok = new StringTokenizer(d.toGMTString(), " ");
						strTok.nextToken(); //day

						parseAmount = objects.get(0).getInt("amount");
						objects.get(0).put("amount", 0);
						objects.get(0).put(strTok.nextToken(),parseAmount);
						objects.get(0).saveInBackground();

						mlistDB.get(0).put("Month", currMonth);
						mlistDB.get(0).saveInBackground();

						Message msg = HomeActivity.this.mHandler.obtainMessage();
						msg.what = StaticVals.Budget;
						HomeActivity.this.mHandler.sendMessage(msg);
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public void openDialog() {	
		final Dialog dialog = new Dialog(HomeActivity.this);
		dialog.setContentView(R.layout.custom_home_dialog);
		dialog.setTitle("Pay Attention");
		dialog.show();

		// set the custom dialog components - text, image and button
		Button dialogButton = (Button) dialog.findViewById(R.id.btnDialogHomeBudget);

		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

	}



	class CustomAdapter extends ArrayAdapter<HomeObj>{

		Context context; 
		int layoutResourceId;    
		ArrayList<HomeObj> data = null;
		private LayoutInflater mInflater; 	

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<HomeObj> data) {

			super(customAdapter, layoutResourceId, data);	
			this.layoutResourceId = layoutResourceId;
			this.context = customAdapter;
			this.data = data;
			this.mInflater = LayoutInflater.from(customAdapter);						
		}

		public View getView(final int position, View convertView, ViewGroup parent) {		

			ViewHolder holder = null;		       

			if (convertView == null) {

				//item_list
				convertView = mInflater.inflate(R.layout.home_item, null);

				holder = new ViewHolder();

				//fill the views
				holder.name = (TextView) convertView.findViewById(R.id.home_item_name);
				holder.image = (ImageView) convertView.findViewById(R.id.home_item_image);
				holder.prgBar = (ProgressBar) convertView.findViewById(R.id.home_item_progressBar);
				holder.window = (LinearLayout) convertView.findViewById(R.id.home_window);

				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.name.setText(data.get(position).name);
			holder.prgBar.setVisibility(data.get(position).visibiltyProg);
			holder.window.setBackgroundResource(data.get(position).window);
			if(data.get(position).pic != null)
				holder.image.setImageBitmap(data.get(position).pic);

			return convertView;
		}


		class ViewHolder {		
			TextView name;
			ImageView image;
			ProgressBar prgBar;
			LinearLayout window;
		}

	}

	public void refresh()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
		query.whereEqualTo("isConfirmed", true);
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) 
				{
					SThome.getInstance().clear();
					for(ParseObject user : objects)
					{
						final HomeObj room8 = new HomeObj();
						room8.name = user.getString("Name");
						room8.window = R.drawable.window_dark;
						room8.visibiltyProg = 0; //visible
						ParseFile imgFile = (ParseFile)user.get("pic");
						if (imgFile!=null)
						{
							imgFile.getDataInBackground(new GetDataCallback() {
								public void done(byte[] data, ParseException e) {
									if (e == null) {
										if (data != null)
										{
											Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
											room8.pic = bitmap;
										}
									} 
								}
							});
						}

						SThome.getInstance().add(room8);
						Message msg = HomeActivity.this.mHandler.obtainMessage();
						HomeActivity.this.mHandler.sendMessage(msg);
					}
				} 
				else 
				{
					Toast.makeText(HomeActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}

			};
		});
	}


	@Override
	public void onItemSelected(String name, String user_name) {
		email_roommate = user_name;

		ConfirmCancelDialog new_dialog= new ConfirmCancelDialog();
		Bundle args = new Bundle();

		args.putString(ConfirmCancelDialog.ARG_STRING_ID, 
				name + ", " + user_name + ", "+ getResources().getString(R.string.admin_confirmation));
		args.putBoolean(ConfirmCancelDialog.ARG_FLAG, true);
		new_dialog.setArguments(args);
		new_dialog.show(getFragmentManager(), "newDialog");		
	}

	@Override
	public void onCancelSelected(boolean isAdminDialog) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
		query.whereEqualTo("Email", HomeActivity.this.email_roommate);
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) 
				{
					if(!objects.isEmpty())
					{
						try {
							objects.get(0).delete();
							objects.get(0).saveInBackground();
							findReq();
							new Thread(new Runnable() {
								@Override
								public void run() {
									refresh();							
								}
							}).start();
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else
					{
						//can not find this email in users
					}
				} 
				else 
				{
					Toast.makeText(HomeActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}
			}
		});		
	}

	@Override
	public void onConfirmSelected(boolean isAdminDialog) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
		query.whereEqualTo("Email", HomeActivity.this.email_roommate);
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) 
				{
					if(!objects.isEmpty())
					{
						objects.get(0).put("isConfirmed", true);
						objects.get(0).saveInBackground();
						findReq();
						new Thread(new Runnable() {
							@Override
							public void run() {
								refresh();							
							}
						}).start();
					}
					else
					{
						//can not find this email in users
					}
				} 
				else 
				{
					Toast.makeText(HomeActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}
			}
		});		
	}

	void findReq()
	{
		//find all requests notifications
		ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
		query.whereEqualTo("isConfirmed", false);
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) 
				{
					if(objects.size() > 0)
					{
						HomeActivity.this.mReqNotification.setVisibility(0); //show notifications
						HomeActivity.this.mReqNotification.setText(((Integer)objects.size()).toString());
					}
					else
					{
						HomeActivity.this.mReqNotification.setVisibility(4); //invisible
					}
					for(ParseObject user : objects)
					{
						reqObj req = new reqObj();
						req.name = user.getString("Name");
						req.userName = user.getString("Email");
						STrequests.getInstance().add(req);
					}
				} 
				else 
				{
					Toast.makeText(HomeActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}
			};
		});
	}

	void initReqButton()
	{
		final ImageButton reqButton = (ImageButton)findViewById(R.id.home_requestBtn);
		reqButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CharSequence[] reqNames;
				ArrayList<String> names, userNames;
				names = new ArrayList<String>();
				userNames = new ArrayList<String>();
				for(reqObj req : STrequests.getInstance())
				{
					names.add(req.name);
					userNames.add(req.userName);
				}

				reqNames = new CharSequence[names.size()];
				for(int i=0; i<names.size(); i++)
				{
					reqNames[i] = (CharSequence)names.get(i);
				}

				Bundle bun = new Bundle();
				bun.putInt(HomeRequestsDialog.DIALOG_NAME, R.string.visible_to_names);
				bun.putCharSequenceArray(HomeRequestsDialog.NAMES, reqNames);
				bun.putStringArrayList(HomeRequestsDialog.USERS_AMES,userNames);

				DialogFragment new_dialog = new HomeRequestsDialog();
				new_dialog.setArguments(bun);
				new_dialog.show(getFragmentManager(), "reqDialog");
			}
		});
	}

	void findNotesNotf()
	{
		final Date today = new Date();

		ParseQuery<ParseObject> sticky_query = ParseQuery.getQuery("StickyNotes");
		sticky_query.whereExists("Reminder");
		sticky_query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		sticky_query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if(objects.size() > 0)
				{
					int ctr = 0;
					for(ParseObject note : objects)
					{
						Date noteDate = note.getDate("Reminder");

						if(today.getYear() >= noteDate.getYear() && 
								today.getMonth() >= noteDate.getMonth() && 
								today.getDate() >= noteDate.getDate())
						{
							HomeActivity.this.mStickyNotification.setVisibility(0);
							ctr++;
						}
					}
					HomeActivity.this.mStickyNotification.setText(((Integer)ctr).toString());
				}

			}

		});
	}

	void initStickyButton()
	{
		final ImageButton stickyNotif = (ImageButton)findViewById(R.id.home_reminder_sticky);
		stickyNotif.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent =  new Intent(HomeActivity.this, StickyNoteActivity.class);
				startActivity(intent);
			}
		});
	}

	void setAllNotconnected()
	{
		for(HomeObj room8 : SThome.getInstance())
		{
			room8.visibiltyProg = 0; //visible
			room8.window = room8.window = R.drawable.window_dark;
		}
	}

	void sendPushAll()
	{
		try {
			JSONObject data = new JSONObject();
			data.put("isAck", false);
			data.put("action", "com.example.UPDATE_IS_HOME");
			data.put("SSID", ParseUser.getCurrentUser().getString("SSID"));	
			data.put("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
			data.put("Name_Apartment", ParseUser.getCurrentUser().getString("Name") + "_" + ParseUser.getCurrentUser().getString("Apartment"));
			ParsePush push = new ParsePush();
			push.setChannel((String)ParseUser.getCurrentUser().get("Apartment"));
			push.setData(data);
			push.sendInBackground(new SendCallback() {
				@Override
				public void done(ParseException e) {
					if(e != null)
					{ //error
						Toast.makeText(HomeActivity.this, getResources().getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
				}
			});
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	void setRefresh()
	{
		final ImageButton refreshButton = (ImageButton)findViewById(R.id.home_refreshBtn);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						setAllNotconnected();
						sendPushAll();
						refresh();							
					}
				}).start();
			}
		});
	}

	void handlerFunc()
	{
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				adapter.notifyDataSetChanged();
				//				if(msg.what == StaticVals.NotConnected)
				//				{
				//					new Thread(new Runnable() {
				//						@Override
				//						public void run() {
				//							refresh();							
				//						}
				//					}).start(); 
				//				}
				//				else 
				if(msg.what == StaticVals.Budget)
				{
					if (userBudget < parseAmount - res)
					{
						openDialog();
					}
				}
				super.handleMessage(msg);
			}
		};
	}

//	public class ChangeStatusReceiver extends BroadcastReceiver{
//		@Override
//		public void onReceive(Context context, Intent intent) {
//
//	}
}
