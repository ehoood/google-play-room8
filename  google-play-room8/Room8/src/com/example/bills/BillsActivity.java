package com.example.bills;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bills.MultiChoiceDialog.OnSelectedVisibleListener;
import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.finalapp.helpDialog;
import com.example.home.HomeActivity;
import com.example.home.HomeObj;
import com.example.home.SThome;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class BillsActivity extends SplitActionBarActivity implements OnSelectedVisibleListener
{
	protected static final String ID = "ID";
	GridView gv;
	CustomAdapter adapter;
	ProgressDialog ringProgressDialog;
	ActionBar actionBar;
	List<ParseObject> mdebtsRoomates;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bills);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_bills);

		ringProgressDialog = ProgressDialog.show(BillsActivity.this, "progress", "waiting for server...");

		if(STBillCategoriesArray.getInstance().size() == 0)
		{
			//build Bill categories array from DB parse
			ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsCategory");
			query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> categories, ParseException e) 
				{
					if (e == null) 
					{
						for(ParseObject category : categories)
						{//for each note from notes
							if(((ArrayList<String>)category.get("VisibleTo")).contains(ParseUser.getCurrentUser().getString("Name")))
							{
								@SuppressWarnings("unchecked")
								billCategoryObject new_category = new billCategoryObject
								(category.getString("Link"),category.getString("Category"),category.getString("CreatedBy"),(ArrayList<String>)category.get("VisibleTo"),category.getObjectId(),category.getUpdatedAt());
								STBillCategoriesArray.getInstance().add(new_category);
							}
						}
					}
					else
					{
						e.printStackTrace();
						Toast.makeText(BillsActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
					}
					if(ringProgressDialog.isShowing())
						ringProgressDialog.dismiss();		

					adapter.notifyDataSetChanged();
				}
			});
		}
		else
		{
			if(ringProgressDialog.isShowing())
				ringProgressDialog.dismiss();
		}

		gv = (GridView)findViewById(R.id.bill_gridView);
		adapter = new CustomAdapter(this, R.layout.bill_category_item, STBillCategoriesArray.getInstance());
		gv.setAdapter(adapter);

		final ImageButton addButtonCustom  = (ImageButton)findViewById(R.id.addBillCategoryBtn);
		final ImageButton editButtonCustom = (ImageButton)findViewById(R.id.bill_editBtn);
		final ImageButton homeButton       = (ImageButton)findViewById(R.id.home_btn_bills);
		final Button debtBtn 		       = (Button)findViewById(R.id.debt_btn);
		final ImageButton helpButton       = (ImageButton)findViewById(R.id.helpBtn);
		
		helpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				helpDialog newDialog = new helpDialog();
				Bundle args   = new Bundle();
				args.putInt("currentActivityHelpXml", R.layout.bill_help_layout);
				newDialog.setArguments(args);
				
				newDialog.show(getFragmentManager(), "new_Dialog");
			}
		});

		debtBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ringProgressDialog = ProgressDialog.show(BillsActivity.this, "progress", "waiting for server...");
				MultiChoiceDialog newDialog = new MultiChoiceDialog();
				Bundle args   = new Bundle();

				ParseQuery<ParseObject> query = ParseQuery.getQuery("RoommatesDebts");
				query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
				ArrayList<String> debtsStrArr = new ArrayList<String>();
				try {
					mdebtsRoomates = query.find();
					
					for(ParseObject roommate : mdebtsRoomates)
					{
						for(HomeObj homeRoommate : SThome.getInstance())
						{
							Double tempPay = roommate.getDouble(homeRoommate.name);
							if(tempPay != 0)
							{//this homeRoommate owes roommate tempPay money
								debtsStrArr.add(homeRoommate.name + " owes " + roommate.getString("PaidBy") +" "+ Double.toString(tempPay));
							}
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CharSequence debts[] = new String[debtsStrArr.size()];

				for(int tempIndex = 0 ; tempIndex < debtsStrArr.size() ;tempIndex++)
				{
					debts[tempIndex] = debtsStrArr.get(tempIndex);
				}

				
				
				if(debts.length > 0)
				{
					boolean mCheckedItemsArray[] = new boolean[debts.length];
					args.putCharSequenceArray(MultiChoiceDialog.ARG_STRING_ID,debts);
					args.putBooleanArray(MultiChoiceDialog.Bool_Array_ID, mCheckedItemsArray);//{false,false,false} instead of mCheckedArray
				}
				else
				{
					args.putString(MultiChoiceDialog.noDebts, "There are no debts between roommates");
					args.putBoolean(MultiChoiceDialog.noDebtsFlag, true);
				}
				
				args.putBoolean(MultiChoiceDialog.IsDebt, true);
				args.putInt(MultiChoiceDialog.TitleDialogSrc, R.string.pay_debt);
				args.putInt(MultiChoiceDialog.PositiveSrc, R.string.paid);
				args.putInt(MultiChoiceDialog.NagativeSrc, R.string.cancel);

				newDialog.setArguments(args);
				if(ringProgressDialog.isShowing())
					ringProgressDialog.dismiss();
				newDialog.show(getFragmentManager(), "new_Dialog");
			}
		});

		homeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(BillsActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		});

		editButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				editButtonCustom.setVisibility(View.INVISIBLE);//set invisible
				editButtonCustom.setClickable(false);
				editButtonCustom.setFocusable(false);

				for(int i=0; i< adapter.getCount(); i++)
				{
					((CheckBox)gv.getChildAt(i).findViewById(R.id.bill_category_checkBox_item)).setVisibility(0); //show all checkbox
				}

				final ImageButton deleteButtonCustom = (ImageButton)findViewById(R.id.bill_deleteBtn);
				deleteButtonCustom.setVisibility(0);//set visible
				deleteButtonCustom.setClickable(true);
				deleteButtonCustom.setFocusable(true);

				deleteButtonCustom.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) 
					{
						deleteCheckedItems();
						for(int i=0; i< adapter.getCount(); i++)
						{
							((CheckBox)gv.getChildAt(i).findViewById(R.id.bill_category_checkBox_item)).setVisibility(4); //hide all check boxes
						}
						deleteButtonCustom.setVisibility(4);//set invisible again
						deleteButtonCustom.setClickable(false);
						deleteButtonCustom.setFocusable(false);
						editButtonCustom.setVisibility(0);//set visible again	
						editButtonCustom.setClickable(true);
						editButtonCustom.setFocusable(true);
					}
				});

			}
		});
		addButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				// Switching to Edit New Bill screen
				Intent intent = new Intent(BillsActivity.this, Edit_new_bill_categoryActivity.class);
				startActivity(intent);
			}
		});
		gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Switching to Edit note screen
				Intent intent = new Intent(BillsActivity.this, Edit_new_bill_categoryActivity.class);

				intent.putExtra(Edit_new_bill_categoryActivity.CREATEDBY, STBillCategoriesArray.getInstance().get(position).createdBy);
				intent.putExtra(Edit_new_bill_categoryActivity.LINK, STBillCategoriesArray.getInstance().get(position).link);
				intent.putExtra(Edit_new_bill_categoryActivity.TITLE, STBillCategoriesArray.getInstance().get(position).title);
				intent.putExtra(Edit_new_bill_categoryActivity.VISIBLETO, STBillCategoriesArray.getInstance().get(position).visibleTo);
				intent.putExtra(Edit_new_bill_categoryActivity.IS_EXIST, true);
				intent.putExtra(Edit_new_bill_categoryActivity.POSITION, position);

				startActivity(intent);
				return false;
			}
		});

		gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
			{
				Intent intent = new Intent(BillsActivity.this, BillPaymentsListActivity.class);	
				intent.putExtra(ID, position);
				startActivity(intent);
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.bills, menu);

		menu.getItem(0).setIcon(R.drawable.bills_tab_blue);

		return true;
	}

	public void deleteCheckedItems() 
	{
		int sizeAdapter = adapter.getCount();
		for (int i = sizeAdapter - 1; i >= 0 ; i--)
		{

			View view = gv.getChildAt(i);

			CheckBox cv = (CheckBox) view.findViewById(R.id.bill_category_checkBox_item);
			if(cv.isChecked())
			{
				ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsCategory");
				query.getInBackground(STBillCategoriesArray.getInstance().get(i).id, new GetCallback<ParseObject>() {
					public void done(ParseObject bill, ParseException e) {
						if (e == null) {
							//delete from server
							try {
								bill.delete();
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
							bill.saveInBackground();
						}
						else
						{
							Toast.makeText(BillsActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
							e.printStackTrace();
						}
					}
				});
				ParseQuery<ParseObject> queryPayments = ParseQuery.getQuery("BillsPayments");
				queryPayments.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
				queryPayments.whereEqualTo("Category", STBillCategoriesArray.getInstance().get(i).title);
				queryPayments.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> payments, ParseException e) 
					{
						if (e == null) 
						{
							if(payments.size() != 0)
							{
								for(ParseObject payment : payments)
								{//for each note from notes
									try {
										payment.delete();
									} catch (ParseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									payment.saveInBackground();
								}
							}
						}
						else
						{
							Toast.makeText(BillsActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
							e.printStackTrace();
						}
					}
				});

				STBillCategoriesArray.getInstance().remove(i);				
			}
			adapter.notifyDataSetChanged();
		}
		Toast.makeText(getApplicationContext(), "Selected Items Cleared", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	class CustomAdapter extends ArrayAdapter<billCategoryObject>{

		Context context; 
		int layoutResourceId;    
		ArrayList<billCategoryObject> data = null;
		private LayoutInflater mInflater; 	

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<billCategoryObject> data) {

			super(customAdapter, layoutResourceId, data);	
			this.layoutResourceId = layoutResourceId;
			this.context = customAdapter;
			this.data = data;
			this.mInflater = LayoutInflater.from(customAdapter);						
		}

		public View getView(final int position, View convertView, ViewGroup parent) {		

			ViewHolder holder = null;		       

			if (convertView == null) {

				//item_list
				convertView = mInflater.inflate(R.layout.bill_category_item, null);

				holder = new ViewHolder();

				//fill the views
				holder.title = (TextView) convertView.findViewById(R.id.bill_category_textview);

				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.title.setText(data.get(position).title);

			return convertView;
		}


		class ViewHolder {		
			TextView title;	
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onCancelSelected(boolean isAdminDialog) {
		//nothing
	}
	@Override
	public void onConfirmSelected(ArrayList<String> stringArr,
			boolean[] checkedItems) {
		ringProgressDialog = ProgressDialog.show(BillsActivity.this, "progress", "clearing all selected debts");

		for(String strChecked : stringArr)
		{
			StringTokenizer strTok = new StringTokenizer(strChecked, " ");
			String owesRoomie = strTok.nextToken();
			strTok.nextToken(); //the word "owes"
			String paidRoomie = strTok.nextToken();
			double amount = Double.valueOf(strTok.nextToken());

			for(ParseObject debtRoommate : mdebtsRoomates)
			{
				if(paidRoomie.equals(debtRoommate.getString("PaidBy")))
				{//this paidRoomie has received debt money, need to clear debt from owesRoomie
					//put in logTableBills the money that this owesRoomie paid.
					insertToLogBills(owesRoomie, amount);

					debtRoommate.put(owesRoomie, 0); //debt has been paid, can erase debt from the table
					try {
						debtRoommate.save();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		if(ringProgressDialog.isShowing())
			ringProgressDialog.dismiss();
	}

	void insertToLogBills(String roomie, double amount) {
		final ParseObject new_log = new ParseObject("LogBills");
		new_log.put("Name", roomie);
		new_log.put("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		new_log.put("amount", amount);
		new_log.saveInBackground();
	}
}

