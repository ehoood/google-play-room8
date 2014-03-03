package com.example.shopping;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bills.BillsActivity;
import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.home.HomeActivity;
import com.example.sticky_notes.StickyNoteActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ShoppingListActivity extends SplitActionBarActivity {
	ShoppingListArray ShoppingListArr = ShoppingListArray.getInstance();
	GroceryArray GroceryListArr = GroceryArray.getInstance();
	ProgressDialog ringProgressDialog;
	ListView lv;
	CustomAdapter ShoppingListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ShoppingList", "onCreate");
		setContentView(R.layout.activity_shopping_list); 

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		actionBar.setCustomView(R.layout.action_shopping);
		
		fillData();

		lv = (ListView)findViewById(R.id.shopping_list);	
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) { 
				editItemInList (position);
			}

		});


		// Now create an array adapter and set it to display using our row
		ShoppingListAdapter= new CustomAdapter(ShoppingListActivity.this, R.layout.notes_row, ShoppingListArr);

		lv.setAdapter(ShoppingListAdapter);

		registerForContextMenu(lv);

		ImageButton addGrocery = (ImageButton)findViewById(R.id.addGroceryListBtn);
		addGrocery.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				createGroceryList();   	
			}
		});

		final ImageButton homeButton = (ImageButton)findViewById(R.id.home_btn_shopping);

		homeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(ShoppingListActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		});
	}

	public void editItemInList (int position)
	{
		final String parseId = ShoppingListArr.get(position).id;
		final String [] strArr = {parseId,Integer.toString(position)};

		ringProgressDialog = ProgressDialog.show(ShoppingListActivity.this, "Please wait", "Connecting to Server");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("ShoppingList");
		query.getInBackground(parseId, new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				Log.d("GetGroceryParse","done");
				if (e == null) {
					Intent intent = new Intent(ShoppingListActivity.this, ShoppingEditActivity.class);
					ArrayList<String> ArrStringTemp = (ArrayList<String>)object.get("groceryListString");
					ArrayList<Integer> ArrIntTemp = (ArrayList<Integer>)object.get("groceryListInt");
					int len = ArrStringTemp.size();
					GroceryListArr.clear();
					for (int i=0;i<len;i++)
					{
						GroceryListItem obj = new GroceryListItem(ArrStringTemp.get(i), ArrIntTemp.get(i));
						GroceryListArr.add(obj);
					}
					
					if(ringProgressDialog.isShowing())
					{
						ringProgressDialog.dismiss();
					}
					intent.putExtra("ListType", strArr);
					startActivity(intent);
				} else {
					// something went wrong
					Toast.makeText(ShoppingListActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
					Log.d("GetGroceryParseError","e = " + e.getMessage());

				}
			}
		});


	}
	
	@Override 
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Select The Action");   
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	} 

	@Override  
	public boolean onContextItemSelected(MenuItem item)
	{  
		AdapterContextMenuInfo selectedRow = (AdapterContextMenuInfo) item.getMenuInfo(); // To get the current selected item
		int pos = selectedRow.position;
		switch (item.getItemId()) {
		case R.id.context_del:
			Log.d("ParseShaiBefore","done");
			String id = ShoppingListArr.get(pos).id;
			//delete from Parse
			ParseQuery<ParseObject> query = ParseQuery.getQuery("ShoppingList");
			query.getInBackground(id, new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					Log.d("ParseShai","done");
					if (e == null) {
						try {
							object.delete();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						object.saveInBackground();
					} else {
						// something went wrong
						Toast.makeText(ShoppingListActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
						Log.d("ParseShai","e = " + e.getMessage());

					}
				}
			});
			ShoppingListAdapter.remove(ShoppingListAdapter.getItem(pos));
			ShoppingListAdapter.notifyDataSetChanged();

			return true;
		case R.id.context_edit:
			editItemInList (pos);
			return true;
		default:
			return super.onContextItemSelected(item);
		}


	}  

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ShoppingList", "onResume");
		ShoppingListAdapter.notifyDataSetChanged();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.shopping_list, menu);
		
		menu.getItem(1).setIcon(R.drawable.shopping_tab_blue);
		return true;		
	}



	private void createGroceryList() {
		Intent intent = new Intent(this, ShoppingEditActivity.class);
		GroceryListArr.clear();
		final String [] strArr = {"Team ZUeS",Integer.toString(-1)};
		intent.putExtra("ListType", strArr);
		startActivity(intent);    	
	}


	class CustomAdapter extends ArrayAdapter<ShoppingList>{

		Context context; 
		int layoutResourceId;    
		ArrayList<ShoppingList> data = null;
		private LayoutInflater mInflater; 	

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<ShoppingList> data) {

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
				convertView = mInflater.inflate(R.layout.notes_row, null);

				holder = new ViewHolder();

				//fill the views
				holder.title = (TextView) convertView.findViewById(R.id.title_text);
				holder.date = (TextView) convertView.findViewById(R.id.date_row);

				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.title.setText(data.get(position).title);
			holder.date.setText(data.get(position).date);

			return convertView;
		}


		class ViewHolder {		
			TextView title;	
			TextView date;
		}

	}

	private void fillData() {
		// Get all of the notes from the database and create the item list
		ringProgressDialog = ProgressDialog.show(ShoppingListActivity.this, "Please wait", "Connecting to Server");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("ShoppingList");
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> listDB, ParseException e) {
				if (e == null) {
					Log.d("listDB", "Retrieved " + listDB.size() + " size");
					if(ShoppingListArr.size() == 0)
					{
						for (ParseObject i : listDB)
						{
							ShoppingList obj = new ShoppingList(i.getString("title"),i.getString("date"),i.getObjectId());
							ShoppingListArr.add(obj);
						}
					}
					ShoppingListAdapter.notifyDataSetChanged();
					if(ringProgressDialog.isShowing())
					{
						ringProgressDialog.dismiss();
					}
				} else {
					Toast.makeText(ShoppingListActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
					Log.d("listDB", "Error: " + e.getMessage());
				}
			}
		});
	}
}
