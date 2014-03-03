package com.example.home;

import java.util.ArrayList;

import com.example.finalapp.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class HomeRequestsDialog extends DialogFragment{

	protected static final String NAMES = "NAMES";
	protected static final String USERS_AMES = "USERS_AMES";
	protected static final String DIALOG_NAME = "DIALOG_NAME";

	OnSelectedListener mCallback;
	CharSequence[] mReqNames;
	ArrayList<String> mReqUserNames;
	private int mDialogName;

	public interface OnSelectedListener 
	{
		public void onItemSelected(String name, String user_name);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		try 
		{
			mCallback  = (OnSelectedListener)getActivity();
			Bundle bun = getArguments();
			mReqNames = bun.getCharSequenceArray(NAMES);
			mReqUserNames = bun.getStringArrayList(USERS_AMES);
			mDialogName = bun.getInt(DIALOG_NAME);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Set the dialog title
			builder.setTitle(mDialogName);
			// Specify the list array, the items to be selected by default (null for none),
			// and the listener through which to receive call backs when items are selected
			builder.setItems(mReqNames,
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mCallback.onItemSelected((String)mReqNames[which], mReqUserNames.get(which));
				}
			}); 
			return builder.create();

		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(getActivity().toString() + "must implement OnSelectedVisibleListener");
		}

	}
}
