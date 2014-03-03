package com.example.shopping;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.parse.SaveCallback;

public class ShoppingEditActivity extends Activity {
	ListView lv;
	ProgressDialog ringProgressDialog;


	GroceryArray GroceryListArr= GroceryArray.getInstance();
	CustomAdapter GroceryAdapter;
	public static String curDate = "";
	AutoCompleteTextView AutoCompleteTV;
	private TextView mDateText;
	private static final String[] FOOD = new String[] {
		 "קוטג", "אבטיח", "מלון","אגס","אפרסק","שזיף","תפוח","בננה","לימון","תפוז","בצל לבן","בצל סגול",
		 "בצל ירוק","גזר","מלפפון ","עגבניה","שום","חסה","כרוב","אבוקדו","פטריות", "פלפל-גמבה","חציל","תפוח אדמה","בטטה",
		 "פטרוזיליה","שמיר","בזיליקום","נענע","שימור  רסק עגבניות","שימור  תירס","שימור  שעועית","שימור  זית","שימור  טונה",
		 "שימור  מלפפון חמוץ","שימור  גרגירי חומוס","חלב","גבינה צהובה","גבינה לבנה","גבינה צפתית","גבינה בולגרית","גבינת עיזים",
		 "גבינת פטה","יוגורט","חמאה","מרגרינה","שמנת מתוקה","דני","מילקי","גמדים","חומוס","טחינה","נקניק","חרדל","קטשופ","סחוג",
		 "כרוב סגול/לבן","מטבוחה","ביצים","רוטב צילי","רוטב סויה","גלידה","שום כתוש","בורקס","ג’חנון","פיצות קפואות","אפונה וגזר קפוא"
		 ,"בצק עלים","צ’יפס","חזה עוף","פרגיות","עוף","בשר טחון","בשר בקר","לבבות","נקניקיות","קבב","המבורגר","פסטה","אורז",
		 "פיתות","איטריות","קוסקוס","בורגול","לחם","לחם קל","פיתות","פירורי לחם","פופקורן למיקרו","מלח","סוכר","קמח","קמח מלא",
		 "קפה","נס קפה","שוקו","דגני בוקר","מרק עוף","עוגות","עוגיות","פיצוחים","חטיפ  מלוח","שוקולד","מים מינרלים","קולה","דיאט קולה"
		 ,"ספרייט","שתייה קלה","מיץ פטל","שתייה חריפה","יין","בירות","וודקה","רד בול ","מיץ חמוציות","מיץ אשכוליות","שמן זית","שמן טיגון"
		 ,"תרסיס שמן","רוטב בלסמי","חומץ","אקונומיקה","סבון לריצפה","סמרטוט רצפה","ספריי ניקוי לחלונות","ספריי לניקוי אבנית",
		 "מטהר אוויר לשירות ","אבקת כביסה","מרכך כביסה","שקיות זבל","סנביצ'ון","סבון כלים","ספוג לכלים",
		 "סמרטוטים קטנים","נייר טואלט","נייר סופג","מגבונים לחים","אבקה למדיח","ניילון נצמד","נייר אפייה","תבניות אפייה חד\"פ",
		 "כוסות חד\"פ","צלחות חד\"פ","מזלגות חד\"פ","כפיות חד\"פ","סכינים  חד\"פ","דאודורנט","מקלוני אוזני ","סבון רחצה","שמפו",
		 "מרכך","קרם לשיער","קרם גוף","סכיני גילוח","תחבושות","צמר גפן","מברשת שיניים"
		 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_edit);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_edit_shopping);

		lv = (ListView)findViewById(R.id.groceryList);	
		// Now create an array adapter and set it to display using our row
		GroceryAdapter= new CustomAdapter(ShoppingEditActivity.this, R.layout.row_of_grocery, GroceryListArr);
		lv.setAdapter(GroceryAdapter);

		AutoCompleteTV = (AutoCompleteTextView) findViewById(R.id.autocompletetextview);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.custum_auto_complete, FOOD);
		AutoCompleteTV.setAdapter(adapter);

		mDateText = (TextView) findViewById(R.id.date);

		long msTime = System.currentTimeMillis();  
		Date curDateTime = new Date(msTime);

		SimpleDateFormat formatter = new SimpleDateFormat("d'/'M'/'y");  
		curDate = formatter.format(curDateTime);        

		mDateText.setText(""+curDate);

		ImageButton addToCart = (ImageButton)findViewById(R.id.add_to_cart);
		addToCart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				GroceryListItem obj = new GroceryListItem(ShoppingEditActivity.this.AutoCompleteTV.getText().toString(), 1);
				GroceryListArr.add(obj);
				GroceryAdapter.notifyDataSetChanged();
				AutoCompleteTV.setText("");
			}
		});

		ImageButton save = (ImageButton)findViewById(R.id.menu_save);
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveList();
			}
		});

		ImageButton delete = (ImageButton)findViewById(R.id.menu_delete);
		delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				deleteCheckedItems();
			}
		});


	}


	public void deleteCheckedItems() {
		int itemCount = GroceryAdapter.getCount();
		for ( int i=itemCount-1; i >= 0; i--) {
			View view = lv.getChildAt(i);
			CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_item);
			if(cb.isChecked()){
				GroceryListArr.remove(i);
			}
			GroceryAdapter.notifyDataSetChanged();
		}
		Toast.makeText(getApplicationContext(), "Selected Items Cleared", Toast.LENGTH_SHORT).show();
	}

	public void saveList(){

		ringProgressDialog = ProgressDialog.show(ShoppingEditActivity.this, "Please wait", "Saving list in Server");

		//check if came from add or the list if from list take the position
		Intent intent = getIntent();
		final String [] strArr = intent.getStringArrayExtra("ListType");
		if (strArr[0].equals("Team ZUeS"))
		{
			// got from add button
			final ParseObject shoppingListTable = new ParseObject("ShoppingList");
			putInParseShoppingTable(shoppingListTable,strArr[0]);
		}else
		{
			// got from list item 
			ParseQuery<ParseObject> query = ParseQuery.getQuery("ShoppingList");
			query.getInBackground(strArr[0], new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) {
						//putInParseShoppingTable(listDB.get(0),parseId);
						EditText title = (EditText)findViewById(R.id.grocery_title);
						final String shoppListTitle = title.getText().toString();
						final String date = curDate.toString();

						ArrayList<String> ArrStringTemp = new ArrayList<String>();
						ArrayList<Integer> ArrIntTemp = new ArrayList<Integer>();

						int groceryArrLen = GroceryListArr.size();
						for (int i = 0; i<groceryArrLen; i++)
						{
							ArrStringTemp.add(GroceryListArr.get(i).item);
							ArrIntTemp.add(GroceryListArr.get(i).quantity);
						}
						object.put("title", shoppListTitle);
						object.put("date", date);
						object.put("groceryListString",ArrStringTemp);
						object.put("groceryListInt",ArrIntTemp);

						object.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
						try {
							object.save();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}

						//saving object id
						ShoppingListArray ShoppingListArr = ShoppingListArray.getInstance();
						int index = Integer.parseInt(strArr[1]);
						ShoppingListArr.get(index).setObj(shoppListTitle,date,strArr[0]);

						if(ringProgressDialog.isShowing())
						{
							ringProgressDialog.dismiss();
						}
						finish();

					} else {
						Toast.makeText(ShoppingEditActivity.this, 
								getResources().getString(R.string.please_check_internet_connection), 
								Toast.LENGTH_LONG
								).show();
						Log.d("listDB", "Error: " + e.getMessage());
					}
				}
			});
		}

	}


	public void putInParseShoppingTable(final ParseObject shoppingListTable, String from)
	{
		EditText title = (EditText)findViewById(R.id.grocery_title);
		final String shoppListTitle = title.getText().toString();
		final String date = curDate.toString();

		ArrayList<String> ArrStringTemp = new ArrayList<String>();
		ArrayList<Integer> ArrIntTemp = new ArrayList<Integer>();

		int groceryArrLen = GroceryListArr.size();
		for (int i = 0; i<groceryArrLen; i++)
		{
			ArrStringTemp.add(GroceryListArr.get(i).item);
			ArrIntTemp.add(GroceryListArr.get(i).quantity);
		}

		shoppingListTable.put("title", shoppListTitle);
		shoppingListTable.put("date", date);
		shoppingListTable.put("groceryListString",ArrStringTemp);
		shoppingListTable.put("groceryListInt",ArrIntTemp);

		shoppingListTable.put("Apartment", (String)ParseUser.getCurrentUser().get("Apartment"));
		shoppingListTable.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				//saving object id
				String objectId = shoppingListTable.getObjectId();
				ShoppingList obj = new ShoppingList(shoppListTitle,date,objectId);
				ShoppingListArray ShoppingListArr = ShoppingListArray.getInstance();
				ShoppingListArr.add(obj);

				if(ringProgressDialog.isShowing())
				{
					ringProgressDialog.dismiss();
				}
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		GroceryAdapter.notifyDataSetChanged();
	}

	class CustomAdapter extends ArrayAdapter<GroceryListItem>{

		Context context; 
		int layoutResourceId;    
		ArrayList<GroceryListItem> data = null;
		private LayoutInflater mInflater; 	
		Typeface font;
		Integer i;

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<GroceryListItem> data) {

			super(customAdapter, layoutResourceId, data);	
			this.layoutResourceId = layoutResourceId;
			this.context = customAdapter;
			this.data = data;
			this.mInflater = LayoutInflater.from(customAdapter);	
			font = Typeface.createFromAsset(getAssets(),
					"anka.ttf");
		}

		public View getView(final int position, View convertView, ViewGroup parent) {		

			ViewHolder holder = null;		       

			if (convertView == null) {

				//item_list
				convertView = mInflater.inflate(R.layout.row_of_grocery, null);

				holder = new ViewHolder();

				//fill the views
				holder.item = (TextView) convertView.findViewById(R.id.item_tv);
				holder.quantity = (TextView) convertView.findViewById(R.id.quantity_tv);
				holder.minus = (ImageButton)convertView.findViewById(R.id.minus_button);
				holder.plus = (ImageButton)convertView.findViewById(R.id.plus_button);


				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}
			holder.item.setText(data.get(position).item);
			holder.item.setTypeface(font);
			Integer q = data.get(position).quantity;
			holder.quantity.setText(q.toString());


			holder.minus.setTag(holder);
			holder.minus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer quan = data.get(position).quantity;
					String strQuantity = quan.toString();
					i = Integer.parseInt(strQuantity);
					if (CustomAdapter.this.i > 1)
					{
						ViewHolder holder1 = (ViewHolder)v.getTag();
						Integer num = (CustomAdapter.this.i-1);
						holder1.quantity.setText(Integer.toString(num));
						GroceryListArr.get(position).quantity=num;
					}
				}
			});

			holder.plus.setTag(holder);
			holder.plus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer quan = data.get(position).quantity;
					String strQuantity = quan.toString();
					i = Integer.parseInt(strQuantity);
					if (CustomAdapter.this.i < 99)
					{
						ViewHolder holder1 = (ViewHolder)v.getTag();
						Integer num = (CustomAdapter.this.i+1);
						holder1.quantity.setText(Integer.toString(num));
						GroceryListArr.get(position).quantity=num;
					}
				}
			});

			return convertView;
		}


		class ViewHolder {		
			TextView item;	
			TextView quantity;
			ImageButton minus;
			ImageButton plus;

		}

	}


}

