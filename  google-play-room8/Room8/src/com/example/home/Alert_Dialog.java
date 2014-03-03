package com.example.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class Alert_Dialog extends DialogFragment {

	public static final String ARG_STRING_ID = "STRINGID";
	String string;
	
	// Container Activity must implement this interface
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try 
        {
    		// Use the Builder class for convenient dialog construction
            
            Bundle bun= getArguments();
    		string= (String)bun.get(ARG_STRING_ID);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		builder.setMessage(string);
    		
    		// Create the AlertDialog object and return it
    		return builder.create();
        } 
        catch (ClassCastException e) 
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnSelectedListener");
        }
		
	}
}
