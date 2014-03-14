package com.example.finalapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

public class helpDialog extends DialogFragment
{
	protected static final String currentActivityHelpXml = "currentActivityHelpXml";
	
	int mHelpLayout;
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    Bundle bun	= getArguments(); // get arguments

	    mHelpLayout = bun.getInt(currentActivityHelpXml);// get the layout XML for current activity that activates help icon.
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(mHelpLayout, null));
	    
	    return builder.create();
	}

}
