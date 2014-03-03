package com.example.bills;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.example.finalapp.R;

public class DatePickerFragmentNoDays extends DialogFragment {

	OnDateSelectedListener2 mCallback;
	NumberPicker monthPicker;
	NumberPicker yearPicker;

	interface OnDateSelectedListener2
	{
		public void onDateDone(int month,int year);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		// Use the current date as the default date in the picker
		View npView			       = inflater.inflate(R.layout.number_picker_dialog, null);
		monthPicker   = (NumberPicker) npView.findViewById(R.id.min_picker);
		yearPicker	   = (NumberPicker) npView.findViewById(R.id.max_picker);

		monthPicker.setMaxValue(12);
		monthPicker.setMinValue(1);
		yearPicker.setMaxValue(3000);
		yearPicker.setMinValue(2010);

		mCallback = (OnDateSelectedListener2)getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Date of payment:");
		builder.setView(npView);
		builder.setPositiveButton("Okay",new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				mCallback.onDateDone(monthPicker.getValue(),yearPicker.getValue());
			}
		});
		builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				mCallback.onDateDone(0,0);	
			}
		});
		// Create a new instance of DatePickerDialog and return it
		return builder.create();
	}
}
