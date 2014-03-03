package com.example.bills;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bills.MultiChoiceDialog.OnSelectedVisibleListener;
import com.example.finalapp.R;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class Edit_new_bill_categoryActivity extends Activity implements OnSelectedVisibleListener
{
	protected static final String TITLE = "TITLE";
	protected static final String LINK = "LINK";
	protected static final String VISIBLETO = "VISIBLETO";
	protected static final String CREATEDBY = "CREATEDBY";
	protected static final String ID = "ID";
	protected static final String IS_EXIST = "IS_EXIST";
	protected static final String POSITION = "POSITION";

	//STBillCategoriesArray mBillCategoryArray = STBillCategoriesArray.getInstance();
	ProgressDialog ringProgressDialog;
	MultiChoiceDialog newDialog;
	CharSequence[] mRoom8Name;
	ArrayList<String> visibleToRoommates;
	billCategoryObject newBillCategory;
	boolean[] mCheckedItemsArray;
	boolean incorrectUrlFlag;
	boolean billExists;
	boolean nextWasClicked;
	Bundle args;
	Intent nextIntent;
	EditText link;//save to server
	EditText visibleToText;//save to server
	EditText titleText;//save to server
	EditText createdBy;//save to server
	String categoryId;
	int selectedSize;
	Date currentDate;


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_new_bill_category);

		newBillCategory	   = null;
		incorrectUrlFlag   = false;
		nextWasClicked     = false;
		newDialog 		   = new MultiChoiceDialog();
		args 		       = new Bundle();
		visibleToRoommates = new ArrayList<String>();
		Button nextButton   = (Button)findViewById(R.id.btnNext);
		Button doneButton   = (Button)findViewById(R.id.btnDone);
		link 	    		= (EditText)findViewById(R.id.link_for_payment);
		visibleToText 		= (EditText)findViewById(R.id.visible_to_names);
		titleText 	  		= (EditText)findViewById(R.id.editTitle);
		createdBy           = (EditText)findViewById(R.id.edit_create);
		billExists 			= getIntent().getBooleanExtra(IS_EXIST, false);

		createdBy.setText(ParseUser.getCurrentUser().get("Name").toString());

		if(billExists)
		{
			categoryId 				 = STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(POSITION, -1)).id;
			StringBuilder tempString = new StringBuilder();

			titleText.setText(getIntent().getStringExtra(TITLE));
			link.setText(getIntent().getStringExtra(LINK));
			createdBy.setText(getIntent().getStringExtra(CREATEDBY));

			visibleToRoommates = getIntent().getStringArrayListExtra(VISIBLETO);

			for(int tempIndex = 0 ; tempIndex < visibleToRoommates.size();tempIndex++)
			{

				if(tempIndex == visibleToRoommates.size()-1)
				{
					tempString.append(visibleToRoommates.get(tempIndex));
				}
				else
				{
					tempString.append(visibleToRoommates.get(tempIndex)+",");
				}
			}

			visibleToText.setText(tempString.toString());
			nextButton.setEnabled(false);
		}
		getRoommateListFromServer();
		/*Handling VisibleTO field*/
		visibleToText.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				if(mRoom8Name == null)//waiting for query to retrieve all users in apartment
				{
					getRoommateListFromServer();
				}
				newDialog.setArguments(args);
				newDialog.show(getFragmentManager(), "newDialog");
			}
		});
		/*Handling Clicking Next Button*/
		nextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) 
			{	//only entering here when a new bill category was created
				String url = link.getText().toString();

				checkUrl(url);//changes the incorrectFlag to false if URL is valid

				nextWasClicked = true;

				if(incorrectUrlFlag == false)
				{
					boolean result = checkEmptyFields();

					if(result == false)
					{	 /* can continue with next button*/
						String title 	    = titleText.getText().toString();
						String tmpcreatedBy = createdBy.getText().toString();

						nextIntent = new Intent(Edit_new_bill_categoryActivity.this,BillPaymentsListActivity.class);
						newBillCategory = new billCategoryObject(url, title, tmpcreatedBy, visibleToRoommates,null,null);
						updateParse(billExists);
					}
				}
				else
				{
					Toast.makeText(Edit_new_bill_categoryActivity.this,getResources().getString(R.string.url_error),Toast.LENGTH_LONG).show();
				}
			}
		});
		/*Handling Clicking Done Button*/
		doneButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				String url 	   = link.getText().toString();
				boolean result = checkEmptyFields();

				checkUrl(url);//changes the incorrectFlag to false if URL is valid

				if(incorrectUrlFlag == false)
				{//URL is valid
					if(result == false)
					{

						String title	 	= titleText.getText().toString();
						String tmpcreatedBy = createdBy.getText().toString();


						if(billExists)
						{ //changes some of the fields of the category in phone memory and in server
							STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(POSITION, -1)).title = title;
							STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(POSITION, -1)).link  = url;
							STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(POSITION, -1)).visibleTo = new ArrayList<String>(visibleToRoommates);
							STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(POSITION, -1)).createdBy = tmpcreatedBy;
						}
						else
						{ // new item in array of categories
							newBillCategory = new billCategoryObject(url, title, tmpcreatedBy, visibleToRoommates,null,null);
						}

						updateParse(billExists);
					}
				}
				else
				{//URL is invalid
					Toast.makeText(Edit_new_bill_categoryActivity.this,getResources().getString(R.string.url_error),Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_new_bill, menu);
		return true;
	}

	@Override
	public void onCancelSelected(boolean isAdminDialog) 
	{

	}
	public boolean checkEmptyFields()
	{
		boolean missingVisibleFieldFlag = false;
		boolean missingTitleFieldFlag   = false;

		String visibleField = visibleToText.getText().toString();
		String titleField = titleText.getText().toString();

		if(visibleField.matches(""))
		{
			Edit_new_bill_categoryActivity.this.visibleToText.setError(getResources().getString(R.string.missingVisibleTo));
			missingVisibleFieldFlag = true;
		}
		if(titleField.matches(""))
		{
			Edit_new_bill_categoryActivity.this.titleText.setError(getResources().getString(R.string.missingTitle));
			missingTitleFieldFlag = true;
		}
		if(missingTitleFieldFlag == false && missingVisibleFieldFlag == false)
		{		/*Fields are OK */
			return false;
		}
		else
		{		/*Missing fields!!!!!!!*/
			return true;
		}
	}
	public void getRoommateListFromServer()
	{
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		query.whereEqualTo("isConfirmed", true);
		List<ParseUser> objects;
		int room8NameIndex;
		int sizeOfReturnedTable;

		try {
			objects = query.find();
			sizeOfReturnedTable = objects.size();
			mRoom8Name = new String[sizeOfReturnedTable];
			mCheckedItemsArray = new boolean[mRoom8Name.length];

			for(room8NameIndex = 0; room8NameIndex < sizeOfReturnedTable ; room8NameIndex++)
			{
				mRoom8Name[room8NameIndex] = (CharSequence) objects.get(room8NameIndex).get("Name");
			}

			args.putCharSequenceArray(MultiChoiceDialog.ARG_STRING_ID, mRoom8Name);
			args.putBooleanArray(MultiChoiceDialog.Bool_Array_ID, mCheckedItemsArray);
			args.putInt(MultiChoiceDialog.TitleDialogSrc, R.string.visible_to_names);
			args.putInt(MultiChoiceDialog.PositiveSrc, R.string.ok);
			args.putInt(MultiChoiceDialog.NagativeSrc, R.string.cancel);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void onConfirmSelected(ArrayList<String> aStringArray,boolean[] aCheckedItemsArray) 
	{
		int aStringArrayIndex; 
		int sizeOfaStringArray;

		sizeOfaStringArray = aStringArray.size();
		visibleToRoommates.clear();
		for(int checkItemsIndex=0;checkItemsIndex < mCheckedItemsArray.length;checkItemsIndex++)
		{
			mCheckedItemsArray[checkItemsIndex] = aCheckedItemsArray[checkItemsIndex];
		}
		StringBuilder tempString = new StringBuilder();

		for(aStringArrayIndex = 0; aStringArrayIndex < sizeOfaStringArray;aStringArrayIndex++)
		{
			visibleToRoommates.add(aStringArray.get(aStringArrayIndex));

			if(aStringArrayIndex == sizeOfaStringArray-1)
			{
				tempString.append(aStringArray.get(aStringArrayIndex));
			}
			else
			{
				tempString.append(aStringArray.get(aStringArrayIndex)+",");
			}


			visibleToText.setText(tempString.toString());
		}
		if(sizeOfaStringArray == 0)
		{
			visibleToText.setText("");
		}

		selectedSize = sizeOfaStringArray;	/* amount of room mates chosen, passes over to bill Information */
	}

	public void checkUrl(String aUrlToValidate)
	{
		if(!aUrlToValidate.matches(""))/*checks if there is an input from the user*/
		{	
			if(!Patterns.WEB_URL.matcher(aUrlToValidate).matches())/* Checks if URL is valid*/
			{		
				incorrectUrlFlag = true;
			}
			else
			{	/*Vaild url*/
				incorrectUrlFlag = false;
			}
		}
		else
		{		/*user left an empty link field.*/
			incorrectUrlFlag = false;
		}
	}
	public void doneFromParse(ParseObject aNew_category)
	{
		int newBillCategoryLocation = -2;

		if(newBillCategory != null)//if newBillCategory is not null it means we came here from a new bill creation
		{
			newBillCategory.updateDate = aNew_category.getUpdatedAt();
			newBillCategory.id  	   = aNew_category.getObjectId();

			if(newBillCategory.visibleTo.contains(ParseUser.getCurrentUser().getString("Name")))
			{
				STBillCategoriesArray.getInstance().add(newBillCategory);
				newBillCategoryLocation = STBillCategoriesArray.getInstance().indexOf(newBillCategory);
			}
		}

		if(ringProgressDialog.isShowing())
			ringProgressDialog.dismiss();

		if(newBillCategory.visibleTo.contains(ParseUser.getCurrentUser().getString("Name")))
		{
			if(nextWasClicked)
			{
				if(newBillCategoryLocation != -1 && newBillCategoryLocation != -2)
				{
					nextIntent.putExtra(ID, newBillCategoryLocation);
					startActivity(nextIntent);
				}

			}
			else // done was pressed
			{
				if(billExists)
				{
					STBillCategoriesArray.getInstance().
					get(getIntent().getIntExtra(POSITION, -1)).updateDate = aNew_category.getUpdatedAt();
				}

				finish();//going back to Bills Activity
			}
		}
		else
			finish();
	}
	public void updateParse(boolean aBillExists)
	{
		if(!aBillExists)
		{	//create a new entry in table
			ringProgressDialog = ProgressDialog.show(Edit_new_bill_categoryActivity.this, "Please Wait", "Creating new Category");
			final ParseObject new_category = new ParseObject("BillsCategory");
			new_category.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
			new_category.put("Category", titleText.getText().toString());
			new_category.put("Link", link.getText().toString());
			new_category.put("CreatedBy", createdBy.getText().toString());
			new_category.put("VisibleTo", visibleToRoommates);
			new_category.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) 
				{
					doneFromParse(new_category);
				}
			});
		}
		else
		{ // change fields in table
			ringProgressDialog = ProgressDialog.show(Edit_new_bill_categoryActivity.this, "Please Wait", "Updating Category");

			ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsCategory");
			query.whereEqualTo("objectId", categoryId);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> categories, ParseException e) 
				{
					if (e == null) 
					{
						final ParseObject category = categories.get(0);
						category.put("Category", titleText.getText().toString());
						category.put("Link", link.getText().toString());
						category.put("VisibleTo", visibleToRoommates);
						category.put("CreatedBy", createdBy.getText().toString());
						category.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) 
							{
								doneFromParse(category);
							}
						});
					}
					else
					{
						Toast.makeText(Edit_new_bill_categoryActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
					}
				}
			});
		}
	}
}
