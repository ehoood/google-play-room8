package com.example.sticky_notes;

import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalapp.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Edit_new_noteActivity extends Activity {

	protected static final String HEAD_LINE = "HEAD_LINE";
	protected static final String NOTE = "NOTE";
	protected static final String ID = "ID";
	protected static final String IS_EXIST = "IS_EXIST";
	protected static final String POSITION = "POSITION";

	EditText mTitle_edit_text;  
	EditText mBody_edit_text;
	boolean isServerDone;
	ProgressDialog ringProgressDialog;
	String idToAdd;
	Date updateDateToadd;
	boolean isSavedDone;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_new_note);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_sticky_edit);
		final ImageButton saveButtonCustom = (ImageButton)findViewById(R.id.sticky_saveBtn);

		mTitle_edit_text = (EditText)findViewById(R.id.sticky_title);  
		mBody_edit_text = (EditText)findViewById(R.id.sticky_body);  

		if(getIntent().getBooleanExtra(IS_EXIST, false))
		{// if this is an edit note that already exists
			mTitle_edit_text.setText(getIntent().getStringExtra(HEAD_LINE));
			mBody_edit_text.setText(getIntent().getStringExtra(NOTE));
		}

		saveButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				ringProgressDialog = ProgressDialog.show(Edit_new_noteActivity.this, "progress", "waiting for server...");

				if(getIntent().getBooleanExtra(IS_EXIST, false))
				{//the note is already in the DB, we need to update
					//save the note in DB and in list view vectors
					ParseQuery<ParseObject> query = ParseQuery.getQuery("StickyNotes");
					query.getInBackground(getIntent().getStringExtra(ID), new GetCallback<ParseObject>() {
						public void done(ParseObject note, ParseException e) {
							if (e == null) {
								// Now let's update it with some new data..
								note.put("HeadLine", mTitle_edit_text.getText().toString());
								note.put("Note", mBody_edit_text.getText().toString());
								note.saveInBackground();

								//update the singleton arraylists 
								StickyNoteObj noteToUpdate = STnotesArray.getInstance().get(getIntent().getIntExtra(POSITION, 0));
								noteToUpdate.headLine = mTitle_edit_text.getText().toString();
								noteToUpdate.note = mBody_edit_text.getText().toString();
							}
							else
							{
								e.printStackTrace();
								Toast.makeText(Edit_new_noteActivity.this, 
										getResources().getString(R.string.please_check_internet_connection), 
										Toast.LENGTH_LONG
										).show();
							}

							if(ringProgressDialog.isShowing())
								ringProgressDialog.dismiss();

							finish();						
						}
					});
				} //if
				else
				{// new note to add to DB					
					final ParseObject new_note = new ParseObject("StickyNotes");
					new_note.put("HeadLine", mTitle_edit_text.getText().toString());
					new_note.put("Note", mBody_edit_text.getText().toString());
					new_note.put("Owner", (String)ParseUser.getCurrentUser().get("Name"));
					new_note.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
					new_note.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							idToAdd = new_note.getObjectId();
							updateDateToadd = new_note.getUpdatedAt();

							//add this note to the array
							StickyNoteObj stickyToAdd = new StickyNoteObj();
							stickyToAdd.headLine =  mTitle_edit_text.getText().toString();
							stickyToAdd.note = mBody_edit_text.getText().toString();
							stickyToAdd.owner = (String)ParseUser.getCurrentUser().get("Name");
							stickyToAdd.id = idToAdd;
							stickyToAdd.updatedDate = updateDateToadd;

							STnotesArray.getInstance().add(stickyToAdd);

							if(ringProgressDialog.isShowing())
								ringProgressDialog.dismiss();

							finish();
						}
					});
				}				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_new_note, menu);
		return true;
	}

}
