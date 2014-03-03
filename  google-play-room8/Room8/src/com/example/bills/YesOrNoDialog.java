package com.example.bills;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.finalapp.R;

public class YesOrNoDialog extends DialogFragment 
{
	protected static final String ARG_STRING_ID = "ARG_STRING_ID";
	
	MultiChoiceDialog temp;
	OnSelecteChoiceListener mCallback;
	String string;

	public interface OnSelecteChoiceListener 
	{
		public void onYesSelected(boolean  isAdminDialog);
		public void onNoSelected(boolean isAdminDialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		try
        {
            mCallback  = (OnSelecteChoiceListener) getActivity();
    		// Use the Builder class for convenient dialog construction
            
            Bundle bun = getArguments();
    		string     = (String)bun.get(ARG_STRING_ID);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setCancelable(false) // force to choose the buttons
    		.setMessage(string)
    		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
    		{
    			public void onClick(DialogInterface dialog, int id)
    			{
    				mCallback.onYesSelected(true);
    			}
    		})
    		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() 
    		{
    			public void onClick(DialogInterface dialog, int id)
    			{
    				mCallback.onNoSelected(true);
    			}
    		});
    		// Create the AlertDialog object and return it
    		return builder.create();

		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(getActivity().toString() + "must implement OnSelecteChoiceListener");
		}
		
	}
}