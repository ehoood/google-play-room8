package com.example.bills;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.example.bills.BillDatePickerFragment.OnBillDateSelectedListener;
import com.example.bills.DatePickerFragmentNoDays.OnDateSelectedListener2;
import com.example.bills.MultiChoiceDialog.OnSelectedVisibleListener;
import com.example.bills.YesOrNoDialog.OnSelecteChoiceListener;
import com.example.finalapp.R;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Bill_InformationActivity extends Activity 
implements OnSelecteChoiceListener,OnSelectedVisibleListener,OnBillDateSelectedListener,OnDateSelectedListener2
{
	protected static final String IS_EXIST 		   = "IS_EXIST";
	protected static final String ID 	   		   = "ID";
	protected static final String PaymentID  	   = "PaymentID";
	protected static final String IS_PAID 	   	   = "IS_PAID";
	protected static final String AMOUNT 	   	   = "AMOUNT";
	protected static final String PER_ROOMMATE 	   = "PER_ROOMMATE";
	protected static final String PAYMENT_DURATION = "PAYMENT_DURATION";
	protected static final String NUMOFMONTH 	   = "NUMOFMONTH";
	protected static final String DUE_DATE 	   	   = "DUE_DATE";
	protected static final String PAID_BY 	       = "PAID_BY";
	protected static final String CREATED_BY 	   = "CREATED_BY";

	MultiChoiceDialog newDialog;
	boolean 	      saveClicked    			 = false;
	boolean 		  resumedFromUrl 			 = false;
	boolean           definedForTheFirstTimeFlag = true;
	boolean 		  paymentExists;
	ImageButton  	  saveBtn;
	RadioButton  	  paidBtn;
	String 	     	  Category;
	String 		      paymentDate = ""; // date when payment was paid
	EditText 	      dueDate;//save to server
	EditText 	 	  createdBy;//save to server
	EditText 	 	  paidBy;//save to server
	EditText 	 	  amountPerRoomie;//save to server in a table of debts
	EditText 		  editAmount;//save to server
	EditText 		  editPaymentDuration;
	EditText 	 	  numberOfMonths;
	String		 	  createdByStr;
	String		 	  dueDateStr;
	String			  paidByStr;
	String		 	  amountPerRoomieStr;
	String		 	  editAmountStr;
	String		 	  editPaymentDurationStr;
	String		 	  numberOfMonthsStr;
	int 		 	  mMonth;
	int 	     	  mYear;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bill__information);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_bill_information);

		saveBtn				   = (ImageButton)findViewById(R.id.bill_info_saveBtn);
		paidBtn 			   = (RadioButton)findViewById(R.id.statusPaid);
		RadioGroup statusGroup = (RadioGroup)findViewById(R.id.billStatusRadioGroup);
		dueDate				   = (EditText)findViewById(R.id.editDueDate);
		createdBy              = (EditText)findViewById(R.id.edit_create);
		paidBy				   = (EditText)findViewById(R.id.edit_paidByView);
		amountPerRoomie		   = (EditText)findViewById(R.id.amountPerRoomie);
		editAmount			   = (EditText)findViewById(R.id.editAmount);
		editPaymentDuration	   = (EditText)findViewById(R.id.editPaymentDuration);
		numberOfMonths		   = (EditText)findViewById(R.id.editNumberOfMonths);

		paymentExists = getIntent().getBooleanExtra(IS_EXIST, false);
		saveClicked   = false;
		statusGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{

				if (checkedId == R.id.statusPaid) 
				{
					DisableEnableAllViews(false);
				} 
				else
				{
					if (checkedId == R.id.statusUnpaid) 
					{
						DisableEnableAllViews(true);
					}
				}
			}
		});

		if(paymentExists)
		{
			createdBy.setText(getIntent().getStringExtra(CREATED_BY));
			paidBy.setText(getIntent().getStringExtra(PAID_BY));
			amountPerRoomie.setText(getIntent().getStringExtra(PER_ROOMMATE));
			editAmount.setText(getIntent().getStringExtra(AMOUNT));
			editPaymentDuration.setText(getIntent().getStringExtra(PAYMENT_DURATION));
			numberOfMonths.setText(getIntent().getStringExtra(NUMOFMONTH));
			dueDate.setText(getIntent().getStringExtra(DUE_DATE));
			paidBtn.setChecked(getIntent().getBooleanExtra(IS_PAID, false));

			if(editPaymentDuration.getText().toString().matches(""))
			{
				numberOfMonths.setEnabled(false);
			}
		}
		else
		{
			numberOfMonths.setEnabled(false);
			createdBy.setText(ParseUser.getCurrentUser().get("Name").toString());		
		}

		newDialog = new MultiChoiceDialog();

		saveBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				if(saveClicked == false)
				{
					saveClicked = true;
					/*saves in STPaymentArray and in parse*/
					saveInParseANDSTPArray();
					finish();
				}
			}
		});
		paidBy.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				Bundle args   = new Bundle();
				ArrayList<String> tempRoommates = STBillCategoriesArray.
						getInstance().get(getIntent().getIntExtra("ID", -1)).visibleTo;
				CharSequence roommies[] = new String[tempRoommates.size()];

				for(int tempIndex = 0 ; tempIndex < tempRoommates.size() ;tempIndex++)
				{
					roommies[tempIndex] = tempRoommates.get(tempIndex);
				}

				boolean mCheckedItemsArray[] = new boolean[roommies.length];
				args.putCharSequenceArray(MultiChoiceDialog.ARG_STRING_ID,roommies );
				args.putBooleanArray(MultiChoiceDialog.Bool_Array_ID, mCheckedItemsArray);//{false,false,false} instead of mCheckedArray
				args.putInt(MultiChoiceDialog.TitleDialogSrc, R.string.visible_to_names);
				args.putInt(MultiChoiceDialog.PositiveSrc, R.string.ok);
				args.putInt(MultiChoiceDialog.NagativeSrc, R.string.cancel);
				args.putBoolean(MultiChoiceDialog.IsFromInfo, true);

				newDialog.setArguments(args);
				newDialog.show(getFragmentManager(), "newDialog");
			}
		});
		numberOfMonths.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s) 
			{
				String numOfMonthsStr = numberOfMonths.getText().toString();
				int numOfMonthInt;
				int newMonth;

				if(!numOfMonthsStr.matches("0") && !numOfMonthsStr.matches(""))
				{
					numOfMonthInt = Integer.parseInt(numOfMonthsStr) - 1;

					if(!editPaymentDuration.getText().toString().matches(""))
					{
						newMonth = (numOfMonthInt + mMonth)%12;

						if(numOfMonthInt + mMonth > 12)
						{
							editPaymentDuration.setText(mMonth + "-" + newMonth + "/" + mYear + "-" + ((mYear+1)%2000));
						}
						else
						{
							if(newMonth != mMonth && mMonth != 12)
							{
								if(newMonth == 0)
								{
									newMonth = 12;
								}
								editPaymentDuration.setText(mMonth + "-" + newMonth + "/" + mYear);
							}
							else
							{
								editPaymentDuration.setText(mMonth + "/" + mYear);
							}
						}
					}
				}
				else
				{
					editPaymentDuration.setText(mMonth + "/" + mYear);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
		editAmount.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s) 
			{
				int amount;
				int devidor 	 = STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).visibleTo.size();
				String amountStr = editAmount.getText().toString();

				if( amountStr.matches(""))
				{
					amount	 = 0;
				}
				else
				{
					amount	 = Integer.parseInt(amountStr);
				}

				double perRoomie = (double)amount/ (double)devidor;

				if(perRoomie == 0)
				{
					amountPerRoomie.setText("");
				}
				else
				{
					amountPerRoomie.setText(String.valueOf(perRoomie));
				}

			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bill__information, menu);
		return true;
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

		saveClicked   		     = false;
		YesOrNoDialog new_dialog = new YesOrNoDialog();

		if(resumedFromUrl == true)/*pop up a dialog asking if bill was paid from url*/
		{
			Bundle args = new Bundle();
			args.putString(YesOrNoDialog.ARG_STRING_ID, getResources().getString(R.string.paid_from_url));
			new_dialog.setArguments(args);
			new_dialog.show(getFragmentManager(), "newAdminDialog");
		}
	}
	public void onClickDueDate(View v) 
	{
		DialogFragment newFragment = new BillDatePickerFragment();
		newFragment.show(getFragmentManager(),"datePicker");
	}
	public void onClickPaymentDate(View v)
	{
		DialogFragment newFragment = new DatePickerFragmentNoDays();
		newFragment.show(getFragmentManager(),"datePicker");
	}
	@SuppressLint("SimpleDateFormat")
	public void saveInParseANDSTPArray()
	{
		Category 		 	   = STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID,-1)).title;
		createdByStr 		   = createdBy.getText().toString();
		dueDateStr 			   = dueDate.getText().toString();
		paidByStr 			   = paidBy.getText().toString();
		amountPerRoomieStr 	   = amountPerRoomie.getText().toString();
		editAmountStr          = editAmount.getText().toString();
		editPaymentDurationStr = editPaymentDuration.getText().toString();
		numberOfMonthsStr      = numberOfMonths.getText().toString();

		if(paidBtn.isChecked())
		{
			long msTime = System.currentTimeMillis();  
			Date curDateTime = new Date(msTime);

			SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm"); 

			paymentDate = formatter.format(curDateTime);
		}

		if(!paymentExists)
		{//Saving data in PARSE and in adapter a new entry
			final ParseObject new_category = new ParseObject("BillsPayments");

			new_category.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
			new_category.put("Category",Category );
			new_category.put("CreatedBy",createdByStr);
			new_category.put("PaymentDuration",editPaymentDurationStr);
			new_category.put("PaidBy",paidByStr);
			new_category.put("PaymentDueDate",dueDateStr);
			new_category.put("IsPaymentPaid",paidBtn.isChecked()); // true is payment was paid, false otherwise
			new_category.put("NumOfMonths",numberOfMonthsStr);
			new_category.put("Amount",editAmountStr);
			new_category.put("PerRoomieAmount",amountPerRoomieStr);

			new_category.put("PaymentDate",paymentDate); 		   //contains the date the payment was paid 

			try {
				new_category.save();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String objectId = new_category.getObjectId();
			paymentObject obj = new paymentObject(objectId,Category,paymentDate,paidBtn.isChecked(),
					createdByStr,editPaymentDurationStr,paidByStr,dueDateStr,
					numberOfMonthsStr,editAmountStr,amountPerRoomieStr);
			STBillPaymentsArray.getInstance().add(obj);

		}
		else
		{ // saving in parse and in adapter the changed fields on existing entry
			ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsPayments");
			query.whereEqualTo("objectId", STBillPaymentsArray.getInstance().get(getIntent().getIntExtra(PaymentID, -1)).paymentObjectId);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> categories, ParseException e) 
				{
					if (e == null) 
					{
						final ParseObject category = categories.get(0);
						category.put("CreatedBy",createdByStr);
						category.put("PaymentDuration",editPaymentDurationStr);
						category.put("PaidBy",paidByStr);
						category.put("PaymentDueDate",dueDateStr);
						category.put("IsPaymentPaid",paidBtn.isChecked()); // true is payment was paid, false otherwise
						category.put("NumOfMonths",numberOfMonthsStr);
						category.put("Amount",editAmountStr);
						category.put("PerRoomieAmount",amountPerRoomieStr);
						category.put("PaymentDate",paymentDate); 		   //contains the date the payment was paid

						category.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) 
							{
								STBillPaymentsArray.getInstance().get(getIntent().getIntExtra(PaymentID, -1)).setPaymentObject
								(paymentDate,paidBtn.isChecked(),createdByStr,editPaymentDurationStr,paidByStr,dueDateStr,
										numberOfMonthsStr,editAmountStr,amountPerRoomieStr);
							}
						});
					}
					else
					{
						e.printStackTrace();
						Toast.makeText(Bill_InformationActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
					}
				}
			});

		}
	}
	@Override
	public void onYesSelected(boolean isAdminDialog) 
	{	/*Bill was indeed paid by URL*/
		paidBtn.setChecked(true);
		DisableEnableAllViews(false);
		saveInParseANDSTPArray();
		insertInDebtsTable();// adjust debts of room mates
		finish();
	}

	@Override
	public void onNoSelected(boolean isAdminDialog)
	{	/*Bill was not paid by URL*/

	}

	public void PayBtn(View v)
	{

		String url = STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).link;
		if(!isFieldsEmpty())
		{
			if(!url.matches(""))
			{
				Uri uriUrl = Uri.parse("http://"+url);
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
				resumedFromUrl = true;
				startActivity(launchBrowser);
			}
			else
			{
				paidBtn.setChecked(true);
				/*need to save in STPaymentArray as well*/
				saveInParseANDSTPArray();
				insertInDebtsTable();// adjust debts of room mates
				finish();
			}
		}
	}
	public void insertInDebtsTable()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("RoommatesDebts");
		query.whereEqualTo("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
		query.whereEqualTo("PaidBy", paidByStr);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> categories, ParseException e) 
			{
				double addition;
				double amount;

				if (e == null) 
				{
					ArrayList<String> tempRoommates  = STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).visibleTo;
					int 		      numOfRoommates = tempRoommates.size();
					amount 		 = Double.parseDouble(amountPerRoomie.getText().toString());

					//save into LogBudget table all rommates paid money
					insertToLogBudget(tempRoommates, amount);

					if(categories.size() == 0)
					{//first payment since last debt
						final ParseObject new_category = new ParseObject("RoommatesDebts");
						new_category.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
						new_category.put("PaidBy", paidByStr);

						for(int i = 0 ; i < numOfRoommates ; i++)
						{
							if(!tempRoommates.get(i).toString().matches(paidByStr))
							{
								new_category.put(tempRoommates.get(i).toString(),amount);
							}
						}
						new_category.saveInBackground();
					}
					else
					{//not first payment
						final ParseObject category 	     = categories.get(0);

						for(int i = 0 ; i < numOfRoommates ; i++)
						{
							if(!tempRoommates.get(i).toString().matches(paidByStr))
							{
								addition = (Double)category.getDouble(tempRoommates.get(i).toString());
								category.put(tempRoommates.get(i).toString(),addition + amount);
							}
						}
						category.saveInBackground();
					}
				}
				else
				{
					e.printStackTrace();
					Toast.makeText(Bill_InformationActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
				}
			}
		});
	}
	public void DisableEnableAllViews(boolean aDisableEnableFlag)
	{
		int size;
		int innerSize;

		LinearLayout myLayout = (LinearLayout) findViewById(R.id.Bill_Information_Layout);
		size = myLayout.getChildCount();

		for ( int outerIndex = 0; outerIndex < size;  outerIndex++ )
		{
			View view = myLayout.getChildAt(outerIndex);
			innerSize = ((ViewGroup)view).getChildCount();

			for(int innerIndex = 0 ; innerIndex < innerSize ; innerIndex++)
			{
				View InnerView = ((ViewGroup)view).getChildAt(innerIndex);
				InnerView.setEnabled(aDisableEnableFlag); // Enable or Disable the view
			}

		}
	}
	@Override
	public void onCancelSelected(boolean isAdminDialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConfirmSelected(ArrayList<String> aStringArray,boolean[] checkedItems) 
	{
		int aStringArrayIndex; 
		int sizeOfaStringArray;

		sizeOfaStringArray = aStringArray.size();
		StringBuilder tempString = new StringBuilder();

		for(aStringArrayIndex = 0; aStringArrayIndex < sizeOfaStringArray;aStringArrayIndex++)
		{
			if(aStringArrayIndex == sizeOfaStringArray-1)
			{
				tempString.append(aStringArray.get(aStringArrayIndex));
			}
			else
			{
				tempString.append(aStringArray.get(aStringArrayIndex)+",");
			}
			paidBy.setText(tempString.toString());
		}
		if(sizeOfaStringArray == 0)
		{
			paidBy.setText("");
		}
	}
	@Override
	public void onBillDateDone(int year, int monthOfYear,int dayOfMonth) 
	{
		dueDate.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
	}

	@Override
	public void onDateDone(int month,int year) 
	{
		if(month != 0 && year != 0)
		{	
			mMonth = month;
			mYear  = year;

			editPaymentDuration.setText(month + "/" + year);
			numberOfMonths.setEnabled(true);
		}
		else//if both of the values equals to zero means cancel was pressed
		{
			editPaymentDuration.setText("");
			numberOfMonths.setEnabled(false);
		}
	}

	private void insertToLogBudget(ArrayList<String> RoommatesPaid,double amountPerRoomie){
		final ArrayList<String> paid = new ArrayList<String>(RoommatesPaid);
		final double amount = amountPerRoomie;
		ParseQuery<ParseObject> query = ParseQuery.getQuery("LogBudget");
		query.whereEqualTo("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> roommates, ParseException e) {
				if(e==null)
				{
					for(ParseObject roomie : roommates)
					{
						if(paid.contains(roomie.getString("Name")))
						{//this roomie has already paid in the past
							roomie.put("amount", amount + roomie.getDouble("amount"));
							paid.remove(roomie.getString("Name"));
							roomie.saveInBackground();
						}
					}
					
					for(String roomieName : paid)
					{//all new paid roomies
						final ParseObject new_paid = new ParseObject("LogBudget");
						new_paid.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
						new_paid.put("Name", roomieName);
						new_paid.put("amount", amount);
						new_paid.saveInBackground();
					}
				}
				else
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	public boolean isFieldsEmpty(){
		boolean isEmptyField= false;
		
		//----check isempty to all editTexts---//
		if(dueDate.getText().length() == 0)
		{
			Bill_InformationActivity.this.dueDate.setError("missing");
			isEmptyField= true;
		}
		if(paidBy.getText().length() == 0)
		{
			Bill_InformationActivity.this.paidBy.setError("missing");
			isEmptyField= true;
		}
		if(editAmount.getText().length() == 0)
		{
			Bill_InformationActivity.this.editAmount.setError("missing");
			isEmptyField= true;
		}	
		if(editPaymentDuration.getText().length() == 0)
		{
			Bill_InformationActivity.this.editPaymentDuration.setError("missing");
			isEmptyField= true;
		}
		if(numberOfMonths.getText().length() == 0)
		{
			Bill_InformationActivity.this.numberOfMonths.setError("missing");
			isEmptyField= true;
		}
		return isEmptyField;
	}
}
