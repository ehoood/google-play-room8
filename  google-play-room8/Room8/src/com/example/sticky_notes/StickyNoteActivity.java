package com.example.sticky_notes;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.home.HomeActivity;
import com.example.login.MainActivity;
import com.example.sticky_notes.StickyDialog.OnSelectedListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class StickyNoteActivity extends SplitActionBarActivity implements OnSelectedListener{
	GridView gv;
	CustomAdapter adapter;
	STnotesArray stickyArr = STnotesArray.getInstance();
	ProgressDialog ringProgressDialog;
	ActionBar actionBar;
	Handler mHandler;
	StickyDialog new_dialog;
	int k=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sticky_note);

		actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_sticky_main);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));

		ringProgressDialog = ProgressDialog.show(StickyNoteActivity.this, "progress", "waiting for server...");

		stickyArr.clear();
		//build stickyNote array from DB parse
		ParseQuery<ParseObject> query = ParseQuery.getQuery("StickyNotes");
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> notes, ParseException e) 
			{
				if (e == null) 
				{
					for(ParseObject note : notes)
					{//for each note from notes
						StickyNoteObj new_note = new StickyNoteObj();
						new_note.headLine = note.getString("HeadLine");
						new_note.note = note.getString("Note");
						new_note.owner = note.getString("Owner");
						new_note.updatedDate = note.getUpdatedAt();
						new_note.id = note.getObjectId();
						Date today = new Date();
						Date noteDate = note.getDate("Reminder");
						if(noteDate != null)
						{
							new_note.reminder = noteDate;
							if(today.getYear() >= noteDate.getYear() && 
									today.getMonth() >= noteDate.getMonth() && 
									today.getDate() >= noteDate.getDate())
							{
								new_note.alertVisiblity = 0;
							}
						}
						stickyArr.add(new_note);
					}
				} 
				else
				{
					e.printStackTrace();
					Toast.makeText(StickyNoteActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}
				if(ringProgressDialog.isShowing())
					ringProgressDialog.dismiss();		

				adapter.notifyDataSetChanged();
			}
		});

		gv= (GridView)findViewById(R.id.list_sticky);
		adapter= new CustomAdapter(this, R.layout.sticky_note_item, stickyArr);
		gv.setAdapter(adapter);

		setAlllisteners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.sticky_note, menu);

		menu.getItem(3).setIcon(R.drawable.sticky_tab_blue);
		return true;
	}

	class CustomAdapter extends ArrayAdapter<StickyNoteObj>{

		Context context; 
		int layoutResourceId;    
		ArrayList<StickyNoteObj> data = null;
		private LayoutInflater mInflater; 	

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<StickyNoteObj> data) {

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
				convertView = mInflater.inflate(R.layout.sticky_note_item, null);

				holder = new ViewHolder();

				//fill the views
				holder.title = (TextView) convertView.findViewById(R.id.sticky_textview);
				holder.alertView = (ImageView) convertView.findViewById(R.id.sticky_alert_pic);

				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.title.setText(data.get(position).headLine);
			holder.alertView.setVisibility(data.get(position).alertVisiblity);

			return convertView;
		}


		class ViewHolder {		
			TextView title;	
			ImageView alertView;
		}

	}

	@Override
	public void onConfirmSelected(Integer year, Integer month, 
			Integer day, String ID, final boolean isReminder) {
		new_dialog.dismiss();

		final Date date = new Date();
		date.setDate(day);
		date.setMonth(month - 1);
		date.setYear(year - 1900);

		for(StickyNoteObj note : stickyArr)
		{
			if(note.id == ID)
			{
				if(isReminder)
				{
					note.reminder = date;
					Date today = new Date();
					if(today.getYear() >= date.getYear() && 
							today.getMonth() >= date.getMonth() && 
							today.getDate() >= date.getDate())
					{
						note.alertVisiblity = 0; //visible
					}
					else
						note.alertVisiblity = 4; //invisible
				}
				else
				{
					note.reminder = null;
					note.alertVisiblity = 4;
				}
			}
		}
		
		adapter.notifyDataSetChanged();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("StickyNotes");
		query.getInBackground(ID, new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject object, ParseException e) {
				if(isReminder)
					object.put("Reminder", date);
				else
					object.remove("Reminder");
				object.saveInBackground();
			}

		});
	}

	public void deleteCheckedItems() {
		int sizeAdapter= adapter.getCount();
		for (int i = sizeAdapter-1; i >= 0; i--) {
			View view = gv.getChildAt(i);

			CheckBox cv = (CheckBox) view.findViewById(R.id.sticky_checkBox_item);
			if(cv.isChecked())
			{
				ParseQuery<ParseObject> query = ParseQuery.getQuery("StickyNotes");
				query.getInBackground(stickyArr.get(i).id, new GetCallback<ParseObject>() {
					public void done(ParseObject note, ParseException e) {
						if (e == null) {
							//delete from server
							try {
								note.delete();
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
							note.saveInBackground();
						}
						else
						{
							e.printStackTrace();
							Toast.makeText(StickyNoteActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
						}
					}
				});
				stickyArr.remove(i);				
			}
			adapter.notifyDataSetChanged();
		}
		Toast.makeText(getApplicationContext(), "Selected Items Cleared", Toast.LENGTH_SHORT).show();
	}

	public void setAlllisteners()
	{	
		final ImageButton addButtonCustom = (ImageButton)findViewById(R.id.sticky_addBtn);
		final ImageButton editButtonCustom = (ImageButton)findViewById(R.id.sticky_editBtn);
		final ImageButton homeButton = (ImageButton)findViewById(R.id.home_btn_sticky);
		
		editButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				for(int i=0; i< adapter.getCount(); i++)
				{
					((CheckBox)gv.getChildAt(i).findViewById(R.id.sticky_checkBox_item)).setVisibility(0); //show all checkbox
				}
				actionBar.setCustomView(R.layout.action_sticky_main_delete);
				final ImageButton deleteButtonCustom = (ImageButton)findViewById(R.id.sticky_deleteBtn);
				deleteButtonCustom.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) 
					{
						deleteCheckedItems();
						for(int i=0; i< adapter.getCount(); i++)
						{
							((CheckBox)gv.getChildAt(i).findViewById(R.id.sticky_checkBox_item)).setVisibility(4); //show all checkbox
						}
						actionBar.setCustomView(R.layout.action_sticky_main);
						setAlllisteners();
					}
				});
				
				final ImageButton cancelButtonCustom = (ImageButton)findViewById(R.id.sticky_cancelEditBtn);
				cancelButtonCustom.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) 
					{
						for(int i=0; i< adapter.getCount(); i++)
						{
							((CheckBox)gv.getChildAt(i).findViewById(R.id.sticky_checkBox_item)).setVisibility(4); //show all checkbox
						}
						actionBar.setCustomView(R.layout.action_sticky_main);
						setAlllisteners();
					}
				});
			}
		});
		
		addButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				// Switching to Edit New note screen
				Intent intent = new Intent(StickyNoteActivity.this, Edit_new_noteActivity.class);
				startActivity(intent);
			}
		});

		homeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(StickyNoteActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		});

		//onitemclick for listview
		gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Switching to Edit note screen
				Intent intent = new Intent(StickyNoteActivity.this, Edit_new_noteActivity.class);

				intent.putExtra(Edit_new_noteActivity.HEAD_LINE, stickyArr.get(position).headLine);
				intent.putExtra(Edit_new_noteActivity.NOTE, stickyArr.get(position).note);
				intent.putExtra(Edit_new_noteActivity.ID, stickyArr.get(position).id);
				intent.putExtra(Edit_new_noteActivity.IS_EXIST, true);
				intent.putExtra(Edit_new_noteActivity.POSITION, position);

				startActivity(intent);
				return false;
			}
		});

		gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				new_dialog = new StickyDialog();
				Bundle args = new Bundle();
				args.putString(StickyDialog.ARG_BODY, stickyArr.get(position).note);
				args.putString(StickyDialog.ARG_TITLE, stickyArr.get(position).headLine);
				args.putString(StickyDialog.ARG_OWNER, stickyArr.get(position).owner);
				args.putString(StickyDialog.ARG_DATE, (stickyArr.get(position).updatedDate).toGMTString());
				args.putString(StickyDialog.ARG_ID, stickyArr.get(position).id);
				if(stickyArr.get(position).reminder != null)
				{
					Integer d = stickyArr.get(position).reminder.getDate();
					Integer mon = stickyArr.get(position).reminder.getMonth() + 1;
					Integer y = stickyArr.get(position).reminder.getYear() + 1900;
					String dateStr = d.toString() + "/" + mon.toString() + "/" + y.toString();
					args.putString(StickyDialog.ARG_REMINDER, dateStr);
				}

				new_dialog.setArguments(args);
				new_dialog.show(getFragmentManager(), "newDialog");
			}
		});
	}
}


