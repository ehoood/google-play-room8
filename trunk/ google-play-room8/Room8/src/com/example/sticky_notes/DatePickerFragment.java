package com.example.sticky_notes;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
implements DatePickerDialog.OnDateSetListener {
	
	OnDateSelectedListener mCallback;
	interface OnDateSelectedListener{
		public void onDateDone(int year, int month, int day);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		((StickyDialog)getTargetFragment()).pickDate.setTextColor(Color.BLACK);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		try
		{
			mCallback = (OnDateSelectedListener)getTargetFragment();
			mCallback.onDateDone(year, month, day);
		}
		catch (ClassCastException e)
		{
			e.printStackTrace();
		}
	}
}