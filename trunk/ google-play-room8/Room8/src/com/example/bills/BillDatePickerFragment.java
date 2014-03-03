package com.example.bills;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class BillDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
	
	OnBillDateSelectedListener mCallback;
	
	interface OnBillDateSelectedListener
	{
		public void onBillDateDone(int year, int month, int day);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		mCallback = (OnBillDateSelectedListener)getActivity();
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		try
		{
			mCallback.onBillDateDone(year, month + 1, day);
		}
		catch (ClassCastException e)
		{
			e.printStackTrace();
		}
	}
}
