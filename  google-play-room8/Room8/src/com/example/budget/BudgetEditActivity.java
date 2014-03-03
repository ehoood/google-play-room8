package com.example.budget;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.finalapp.R;
import com.example.finalapp.R.layout;
import com.example.finalapp.R.menu;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class BudgetEditActivity extends Activity {

	EditText editTextAmount;
	EditText custumDialogEditText;
	int res;
	int flag;
	int userBudget;
	int radioBtn;
	ProgressDialog ringProgressDialog;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget_edit);

		editTextAmount = (EditText)findViewById(R.id.budgetAmountEditTextEdit);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Budget");
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.whereEqualTo("username", ParseUser.getCurrentUser().get("username"));
		List<ParseObject> listDB;
		try {
			listDB = query.find();
			Log.d("listDB", "Retrieved " + listDB.size() + " size");
			userBudget = listDB.get(0).getInt("Amount");
			radioBtn = listDB.get(0).getInt("Radio");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		editTextAmount.setText(Integer.toString(userBudget));
		RadioButton radioExpBtnEdit = (RadioButton) findViewById(R.id.radioExceptionEdit);
		RadioButton bfrRadioBtnEdit = (RadioButton) findViewById(R.id.beforeRadioExceptionEdit);

		if (radioBtn == 1)
		{
			radioExpBtnEdit.setChecked(true);
			bfrRadioBtnEdit.setChecked(false);
		}else
		{
            // radioBtn = 2
			radioExpBtnEdit.setChecked(false);
			bfrRadioBtnEdit.setChecked(true);

		}

		Button submitBtn = (Button) findViewById(R.id.budgetSubmitBtntEdit);
		submitBtn.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (editTextAmount.getText().length() > 0)
				{
					// The user did not fill in a numeric amount
					managebudget(editTextAmount.getText().toString());
				}else
				{
					// The user filled in a numeric amount
					BudgetEditActivity.this.editTextAmount.setError(getResources().getString(R.string.error_missing_amount));
				}
			}
		});
	}

	public void managebudget (String amountStr)
	{
		RadioButton rdBtnBfrException = (RadioButton)findViewById(R.id.beforeRadioExceptionEdit);
		if (rdBtnBfrException.isChecked())
		{
			//before exception option
			openDialog(amountStr);
			flag = 2;
		}else
		{
			//sends push notification after exception option
			res = 0;
			flag = 1;
			ringProgressDialog = ProgressDialog.show(BudgetEditActivity.this, "Please wait", "Saving in Server");
			saveInParse(amountStr);
		}




	}

	public void saveInParse(String amountStr)
	{
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int currMonth = cal.get(Calendar.MONTH);
		currMonth++; // just to make January = 1, February =2 etc.

		
		ParseQuery<ParseObject> queryB = ParseQuery.getQuery("Budget");
		queryB.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		queryB.whereEqualTo("username", ParseUser.getCurrentUser().get("username"));
		List<ParseObject> listDB;
			try {
				listDB = queryB.find();
				// Saving params in Parse
				final ParseObject budgetTable = listDB.get(0);
				budgetTable.put("Amount", Integer.parseInt(amountStr));
				budgetTable.put("Res", res);
				budgetTable.put("Month", currMonth);
				budgetTable.put("username", ParseUser.getCurrentUser().get("username"));
				budgetTable.put("Apartment", ParseUser.getCurrentUser().get("Apartment"));
				
				if (flag == 1)
				{
					budgetTable.put("Radio", 1);
				}else
				{
					// flag = 2
					budgetTable.put("Radio", 2);
				}
				
				budgetTable.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						ParseUser user = ParseUser.getCurrentUser();
						user.put("BudgetfirstTime", false);
						try {
							user.save();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						if(ringProgressDialog.isShowing())
						{
							ringProgressDialog.dismiss();
						}
						Toast.makeText(getApplicationContext(), "Your decision has been saved", Toast.LENGTH_LONG).show();
						finish();
					}
				});
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
		
	}

	public void openDialog(String amountStr) {	
		final Dialog dialog = new Dialog(BudgetEditActivity.this);
		dialog.setContentView(R.layout.custom_budget_dialog);
		dialog.setTitle(R.string.budget_limit_header);
		dialog.show();
		// set the custom dialog components - text, image and button
		Button dialogButton = (Button) dialog.findViewById(R.id.Btn_custum_budget);
		custumDialogEditText = (EditText) dialog.findViewById (R.id.et_custum_budget);

		final String strTmp = amountStr;

		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (custumDialogEditText.getText().length() > 0) 
				{
					res = Integer.parseInt(custumDialogEditText.getText().toString());
					dialog.dismiss();
					ringProgressDialog = ProgressDialog.show(BudgetEditActivity.this, "Please wait", "Saving in Server");
					saveInParse(strTmp);
				}else
				{
					BudgetEditActivity.this.custumDialogEditText.setError(getResources().getString(R.string.error_missing_amount));
				}
			}
		});

	}
}
