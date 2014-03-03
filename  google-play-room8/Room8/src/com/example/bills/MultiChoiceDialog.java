package com.example.bills;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.example.finalapp.R;

public class MultiChoiceDialog extends DialogFragment
{
	protected static final String ARG_STRING_ID = "ARG_STRING_ID";
	protected static final String Bool_Array_ID = "Bool_Array_ID";
	protected static final String IsDebt = "IsDebt";
	protected static final String TitleDialogSrc = "TitleDialogSrc";
	protected static final String PositiveSrc = "PositiveSrc";
	protected static final String NagativeSrc = "NagativeSrc";
	protected static final String IsFromInfo = "IsFromInfo";

	MultiChoiceDialog temp;
	protected CharSequence[] mRoom8Name;
	boolean mCheckedItems[];
	int mtitleSrc;
	int mposSrc;
	int mnagSrc;

	OnSelectedVisibleListener mCallback;
	ArrayList<String> mSelectedItems;
	boolean mIsFromInfo;

	public interface OnSelectedVisibleListener 
	{
		public void onCancelSelected(boolean  isAdminDialog);
		public void onConfirmSelected(ArrayList<String>  isAdminDialog,boolean[] checkedItems);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		try 
		{
			mCallback  = (OnSelectedVisibleListener)getActivity();
			Bundle bun = getArguments();
			mRoom8Name = bun.getCharSequenceArray(ARG_STRING_ID);
			mCheckedItems = bun.getBooleanArray(Bool_Array_ID);
			mtitleSrc = bun.getInt(TitleDialogSrc);
			mposSrc = bun.getInt(PositiveSrc);
			mnagSrc = bun.getInt(NagativeSrc);
			mIsFromInfo = bun.getBoolean(IsFromInfo, false);
			
			//boolean for the debt dialog
			boolean isDebt = bun.getBoolean(IsDebt, false);

			mSelectedItems = new ArrayList<String>();  // Where we track the selected items

			if(!isDebt)
			{
				for(int mSelectedItemsIndex = 0 ; mSelectedItemsIndex < mRoom8Name.length ; mSelectedItemsIndex++ )
				{
					if(mCheckedItems[mSelectedItemsIndex] == true)
					{
						mSelectedItems.add((String)mRoom8Name[mSelectedItemsIndex]);
					}
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setCancelable(false) // force to choose the buttons
			// Set the dialog title
			.setTitle(mtitleSrc)
			// Specify the list array, the items to be selected by default (null for none),
			// and the listener through which to receive call backs when items are selected
			.setMultiChoiceItems(mRoom8Name, bun.getBooleanArray(Bool_Array_ID),
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which,boolean isChecked) 
				{
					if (isChecked) 
					{
						// If the user checked the item, add it to the selected items
						mSelectedItems.add((String)mRoom8Name[which]);
						mCheckedItems[which] = true;
					} else if (mSelectedItems.contains((String)mRoom8Name[which])) {
						// Else, if the item is already in the array, remove it 
						mSelectedItems.remove(((String)mRoom8Name[which]));
						mCheckedItems[which] = false;
					}
				}
			})
			// Set the action buttons
			.setPositiveButton(mposSrc, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int id) 
				{
					// User clicked OK, so save the mSelectedItems results somewhere
					// or return them to the component that opened the dialog
					if(!mIsFromInfo)
						mCallback.onConfirmSelected(mSelectedItems,mCheckedItems);
					else if(mSelectedItems.size() > 1)
					{
						Toast.makeText(getActivity(), "error! please select only one roommate", Toast.LENGTH_SHORT).show();
					}
					else
						mCallback.onConfirmSelected(mSelectedItems,mCheckedItems);
				}
			})
			.setNegativeButton(mnagSrc, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) 
				{
					mCallback.onCancelSelected(true);
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
