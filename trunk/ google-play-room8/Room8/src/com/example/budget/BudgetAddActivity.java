package com.example.budget;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.shopping.ShoppingListActivity;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class BudgetAddActivity extends Activity {
	EditText editTextAmount;
	EditText custumDialogEditText;
	int res;
	int flag;
	ProgressDialog ringProgressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget_add);

		Button submitBtn = (Button) findViewById(R.id.budgetSubmitBtntAdd);
		submitBtn.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				editTextAmount = (EditText)findViewById(R.id.budgetAmountEditTextAdd);
				if (editTextAmount.getText().length() > 0)
				{
					// The user did not fill in a numeric amount
					managebudget(editTextAmount.getText().toString());
				}else
				{
					// The user filled in a numeric amount
					BudgetAddActivity.this.editTextAmount.setError(getResources().getString(R.string.error_missing_amount));
				}
			}
		});
	}

	public void managebudget (String amountStr)
	{
		RadioButton rdBtnBfrException = (RadioButton)findViewById(R.id.beforeRadioExceptionAdd);
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
			ringProgressDialog = ProgressDialog.show(BudgetAddActivity.this, "Please wait", "Saving in Server");
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

		// Saving params in Parse
		final ParseObject budgetTable = new ParseObject("Budget");
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
	}

	public void openDialog(String amountStr) {	
		final Dialog dialog = new Dialog(BudgetAddActivity.this);
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
					ringProgressDialog = ProgressDialog.show(BudgetAddActivity.this, "Please wait", "Saving in Server");
					saveInParse(strTmp);
				}else
				{
					BudgetAddActivity.this.custumDialogEditText.setError(getResources().getString(R.string.error_missing_amount));
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.budget_add, menu);
		return true;
	}

}
