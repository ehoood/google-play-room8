package com.example.sticky_notes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.finalapp.R;
import com.example.sticky_notes.DatePickerFragment.OnDateSelectedListener;

public class StickyDialog extends DialogFragment implements OnDateSelectedListener{

	protected static final String ARG_TITLE = "TITLE";
	protected static final String ARG_BODY = "BODY";
	protected static final String ARG_DATE = "DATE";
	protected static final String ARG_OWNER = "OWNER";
	protected static final String ARG_ID = "ID";
	protected static final String ARG_REMINDER = "REMINDER";

	OnSelectedListener mCallback;
	String mStringBody;
	String mStringTitle;
	String mUpdatedDate;
	String mOwner;
	String mID;
	TextView mBodyTv;
	TextView mOwnerTv;
	TextView mDateTv;
	TextView mTitleTv;
	View mVbody;
	TextView pickDate;
	Integer y = 0,mon = 0,d = 0;
	ToggleButton toggle;


	// Container Activity must implement this interface
	public interface OnSelectedListener 
	{
		public void onConfirmSelected(Integer year, Integer month, 
				Integer day, String ID, final boolean isReminder);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try 
		{
			mCallback = (OnSelectedListener) getActivity();
			// Use the Builder class for convenient dialog construction

			Bundle bun= getArguments();
			mStringBody = (String)bun.get(ARG_BODY);
			mStringTitle = (String)bun.get(ARG_TITLE);
			mUpdatedDate = (String)bun.get(ARG_DATE);
			mOwner = (String)bun.get(ARG_OWNER);
			mID = (String)bun.get(ARG_ID);

			LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
			mVbody = inflater.inflate(R.layout.body_note_dialog, null);

			mBodyTv = (TextView)mVbody.findViewById(R.id.sticky_body_dialog);
			mOwnerTv = (TextView)mVbody.findViewById(R.id.sticky_owner_dialog);
			mDateTv = (TextView)mVbody.findViewById(R.id.sticky_updatedate_dialog);
			mTitleTv = (TextView)mVbody.findViewById(R.id.sticky_title_tv_dialog);

			mBodyTv.setText(mStringBody);
			mDateTv.setText("Updated at: " + mUpdatedDate);
			mOwnerTv.setText("Creator: " + mOwner);
			mTitleTv.setText("Title: " + mStringTitle);

			mBodyTv.setMovementMethod(new ScrollingMovementMethod());

			pickDate = (TextView)mVbody.findViewById(R.id.sticky_reminde_date_dialog);

			Button confirmBtn = (Button)mVbody.findViewById(R.id.sticky_confirm_dialog);

			toggle = (ToggleButton)mVbody.findViewById(R.id.sticky_reminde_switch_dialog);

			String dateReminder = bun.getString(ARG_REMINDER);
			if(dateReminder != null)
			{
				toggle.setChecked(true);
				((TextView)mVbody.findViewById(R.id.sticky_reminde_date_dialog)).setText(dateReminder);
				((TextView)mVbody.findViewById(R.id.sticky_reminde_date_dialog)).setVisibility(0);
			}

			toggle.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Is the toggle on?
					boolean on = ((ToggleButton) v).isChecked();

					if (on) {
						pickDate.setVisibility(0);
					} else {
						pickDate.setVisibility(4);
					}
				}
			});

			pickDate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					pickDate.setTextColor(Color.WHITE);
					DialogFragment newFragment = new DatePickerFragment();
					newFragment.setTargetFragment(StickyDialog.this, 123);
					newFragment.show(getFragmentManager(), "datePicker");
				}
			});

			confirmBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					//StickyDialog.this.dismiss();
					if(!toggle.isChecked())
						mCallback.onConfirmSelected(y, mon, d, mID, false);
					else
					{
						if(((TextView)mVbody.findViewById(R.id.sticky_reminde_date_dialog)).getText().equals("choose date"))
							Toast.makeText(getActivity(), 
									"please choose a date, or cancel reminder", 
									Toast.LENGTH_SHORT
									).show();
						else
							mCallback.onConfirmSelected(y, mon, d, mID, true);
					}
				}
			});

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setView(mVbody);

			// Create the AlertDialog object and return it
			return builder.create();
		} 
		catch (ClassCastException e) 
		{
			throw new ClassCastException(getActivity().toString()
					+ " must implement OnSelectedListener");
		}

	}

	@Override
	public void onDateDone(int year, int month, int day) {
		y = year;
		mon = month + 1;
		d = day;

		((TextView)mVbody.findViewById(R.id.sticky_reminde_date_dialog)).setText(d.toString() + "/" + mon.toString() + "/" + y.toString());
	}
}
