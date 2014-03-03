package com.example.bills;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class BillPaymentsListActivity extends Activity 
{
	protected static final String ID = "ID";

	ListView lv;
	CustomAdapter paymentListAdapter;
	ProgressDialog ringProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bill_payments_list);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_paymentlist);

		ringProgressDialog = ProgressDialog.show(BillPaymentsListActivity.this, "progress", "waiting for server...");

		if(STBillPaymentsArray.getInstance().getsameDirectoryString() == null || !STBillPaymentsArray.getInstance().getsameDirectoryString().equals(STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).title))
		{
			STBillPaymentsArray.getInstance().clear();

			ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsPayments");
			query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
			query.whereEqualTo("Category", STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).title);
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> payments, ParseException e) 
				{
					if (e == null) 
					{
						if(payments.size() == 0)
						{
							STBillPaymentsArray.getInstance().setSameDierctoryString(null);
						}
						for(ParseObject payment : payments)
						{//for each note from notes
							paymentObject new_payment = new paymentObject
									(payment.getObjectId(),payment.getString("Category"),payment.getString("PaymentDate"),
											payment.getBoolean("IsPaymentPaid"),payment.getString("CreatedBy"),
											payment.getString("PaymentDuration"),payment.getString("PaidBy"),payment.getString("PaymentDueDate"),
											payment.getString("NumOfMonths"),payment.getString("Amount"),payment.getString("PerRoomieAmount"));
							STBillPaymentsArray.getInstance().add(new_payment);
							STBillPaymentsArray.getInstance().setSameDierctoryString(new_payment.category);
						}
					}
					else
					{
						e.printStackTrace();
						Toast.makeText(BillPaymentsListActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
					}

					if(ringProgressDialog.isShowing())
						ringProgressDialog.dismiss();		

					paymentListAdapter.notifyDataSetChanged();
				}
			});
		}
		else
		{
			if(ringProgressDialog.isShowing())
				ringProgressDialog.dismiss();
		}

		lv = (ListView)findViewById(R.id.paymentsList);	
		// Now create an array adapter and set it to display using our row
		paymentListAdapter = new CustomAdapter(this, R.layout.payment_list_row, STBillPaymentsArray.getInstance());
		lv.setAdapter(paymentListAdapter);


		lv.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
			{ 
				paymentObject tempPayment = STBillPaymentsArray.getInstance().get(position);
				Intent intent = new Intent(BillPaymentsListActivity.this, Bill_InformationActivity.class);

				intent.putExtra(Bill_InformationActivity.AMOUNT, tempPayment.amount);
				intent.putExtra(Bill_InformationActivity.CREATED_BY,tempPayment.createdby);
				intent.putExtra(Bill_InformationActivity.DUE_DATE,tempPayment.paymentDueDate);
				intent.putExtra(Bill_InformationActivity.ID,getIntent().getIntExtra(ID, -1));// id of bill category
				intent.putExtra(Bill_InformationActivity.PaymentID,position);// id of payment item
				intent.putExtra(Bill_InformationActivity.IS_EXIST,true);
				intent.putExtra(Bill_InformationActivity.IS_PAID,tempPayment.isPaid);
				intent.putExtra(Bill_InformationActivity.NUMOFMONTH,tempPayment.numOfmonths);
				intent.putExtra(Bill_InformationActivity.PAID_BY,tempPayment.paidBy);
				intent.putExtra(Bill_InformationActivity.PAYMENT_DURATION,tempPayment.paymentDuration);
				intent.putExtra(Bill_InformationActivity.PER_ROOMMATE,tempPayment.perAmount);

				startActivity(intent);

			}

		});
		final ImageButton addButtonCustom = (ImageButton)findViewById(R.id.addPaymentBtn);

		addButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				// Switching to Bill Information screen
				Intent intent = new Intent(BillPaymentsListActivity.this, Bill_InformationActivity.class);
				intent.putExtra("ID", getIntent().getIntExtra(ID, -1));
				startActivity(intent);
			}
		});
		final ImageButton refreshButtonCustom = (ImageButton)findViewById(R.id.refreshPaymentBtn);

		refreshButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				// refreshing payments in case some one added a new payment
				ringProgressDialog = ProgressDialog.show(BillPaymentsListActivity.this, "Hold On", "Refreshing Payments...");
				STBillPaymentsArray.getInstance().clear();

				ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsPayments");
				query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
				query.whereEqualTo("Category", STBillCategoriesArray.getInstance().get(getIntent().getIntExtra(ID, -1)).title);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> payments, ParseException e) 
					{
						if (e == null) 
						{
							if(payments.size() == 0)
							{
								STBillPaymentsArray.getInstance().setSameDierctoryString(null);
							}
							for(ParseObject payment : payments)
							{//for each note from notes
								paymentObject new_payment = new paymentObject
										(payment.getObjectId(),payment.getString("Category"),payment.getString("PaymentDate"),
												payment.getBoolean("IsPaymentPaid"),payment.getString("CreatedBy"),
												payment.getString("PaymentDuration"),payment.getString("PaidBy"),payment.getString("PaymentDueDate"),
												payment.getString("NumOfMonths"),payment.getString("Amount"),payment.getString("PerRoomieAmount"));
								STBillPaymentsArray.getInstance().add(new_payment);
								STBillPaymentsArray.getInstance().setSameDierctoryString(new_payment.category);
							}
						}
						else
						{
							e.printStackTrace();
							Toast.makeText(BillPaymentsListActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
						}

						if(ringProgressDialog.isShowing())
							ringProgressDialog.dismiss();		

						paymentListAdapter.notifyDataSetChanged();
					}
				});
			}
		});
		final ImageButton deleteButtonCustom = (ImageButton)findViewById(R.id.deletePayemntBtn);
		deleteButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				int sizeAdapter = paymentListAdapter.getCount();
				for (int i = sizeAdapter - 1; i >= 0 ; i--) 
				{
					View view = lv.getChildAt(i);
					CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_item_payment);

					if(cb.isChecked())
					{
						String id = STBillPaymentsArray.getInstance().get(i).paymentObjectId;
						//delete from Parse
						ParseQuery<ParseObject> query = ParseQuery.getQuery("BillsPayments");
						query.getInBackground(id, new GetCallback<ParseObject>(){
							public void done(ParseObject object, ParseException e) 
							{
								if (e == null)
								{
									try 
									{
										object.delete();
									} catch (ParseException e1)
									{
										e1.printStackTrace();
									}
									object.saveInBackground();
								} else {
									// something went wrong
									e.printStackTrace();
									Toast.makeText(BillPaymentsListActivity.this, 
											getResources().getString(R.string.please_check_internet_connection), 
											Toast.LENGTH_LONG
											).show();
									Log.d("ParseZivDeletePaymentFromParse","e = " + e.getMessage());
								}
							}
						});
						STBillPaymentsArray.getInstance().remove(i);				
					}
					paymentListAdapter.notifyDataSetChanged();
				}
				Toast.makeText(getApplicationContext(), "Selected Items Cleared", Toast.LENGTH_SHORT).show();

				for(int i = 0; i < paymentListAdapter.getCount(); i++)
				{
					View view = lv.getChildAt(i);
					CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_item_payment);
					cb.setChecked(false);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		paymentListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bill_payments_list, menu);
		return true;
	}

	class CustomAdapter extends ArrayAdapter<paymentObject>{

		Context context; 
		int layoutResourceId;    
		ArrayList<paymentObject> data = null;
		private LayoutInflater mInflater; 	
		Integer i;

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<paymentObject> data) {

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
				convertView = mInflater.inflate(R.layout.payment_list_row, null);

				holder = new ViewHolder();

				//fill the views
				holder.paymentName = (TextView) convertView.findViewById(R.id.paymentRowView);
				holder.paymentDate = (TextView) convertView.findViewById(R.id.paymentRowDate);


				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.paymentName.setText(data.get(position).category + " payment " + data.get(position).paymentDuration);

			if(data.get(position).isPaid)
			{
				holder.paymentDate.setText(data.get(position).paymentDate);
				holder.paymentDate.setTextColor(getResources().getColor(R.color.green));
			}
			else
			{
				holder.paymentDate.setText("Not Paid");
				holder.paymentDate.setTextColor(getResources().getColor(R.color.red));
			}

			return convertView;
		}


		class ViewHolder 
		{		
			TextView paymentName;	
			TextView paymentDate;
		}

	}

}
