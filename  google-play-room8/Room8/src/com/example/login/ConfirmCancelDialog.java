package com.example.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.finalapp.R;

public class ConfirmCancelDialog extends DialogFragment {

	public static final String ARG_STRING_ID = "STRINGID";
	public static final String ARG_FLAG = "FLAG";
	OnConfirmCancelListener mCallback;
	String string;
	boolean flag;
	
	// Container Activity must implement this interface
    public interface OnConfirmCancelListener 
    {
        public void onCancelSelected(boolean isAdminDialog);
        public void onConfirmSelected(boolean isAdminDialog);
    }
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try 
        {
            mCallback = (OnConfirmCancelListener) getActivity();
    		// Use the Builder class for convenient dialog construction
            
            Bundle bun= getArguments();
    		string= (String)bun.get(ARG_STRING_ID);
    		flag= (Boolean)bun.get(ARG_FLAG);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setCancelable(false); // force to choose the buttons
    		builder.setMessage(string)
    		.setPositiveButton(R.string.Confirm, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				mCallback.onConfirmSelected(flag);
    			}
    		})
    		.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				mCallback.onCancelSelected(flag);
    			}
    		});
    		
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
