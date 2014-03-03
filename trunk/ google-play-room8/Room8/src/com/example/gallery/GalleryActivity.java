package com.example.gallery;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.finalapp.R.drawable;
import com.example.finalapp.R.id;
import com.example.finalapp.R.layout;
import com.example.finalapp.R.menu;
import com.example.finalapp.R.string;
import com.example.home.HomeActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class GalleryActivity extends SplitActionBarActivity 
{
	static final int REQUEST_IMAGE_CAPTURE = 1;
	ImageView mImageView;
	ProgressDialog ringProgressDialog;
	GridView gv;
	ArrayList<GalleryObj> gallaryArr;
	CustomAdapter adapter = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		actionBar.setCustomView(R.layout.action_gallery);

		gallaryArr = STgallery.getInstance();
		gallaryArr.clear();

		fillData();

		final ImageButton homeButton = (ImageButton)findViewById(R.id.home_btn_gallery);

		homeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(GalleryActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		});
		final ImageButton editButtonCustom = (ImageButton)findViewById(R.id.gallery_editBtn);
		editButtonCustom.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) 
			{
				editButtonCustom.setVisibility(View.INVISIBLE);//set invisible
				editButtonCustom.setClickable(false);
				editButtonCustom.setFocusable(false);

				for(int i=0; i< adapter.getCount(); i++)
				{
					((CheckBox)gv.getChildAt(i).findViewById(R.id.gallery_checkBox_item)).setVisibility(0); //show all checkbox
				}

				final ImageButton deleteButtonCustom = (ImageButton)findViewById(R.id.gallery_deleteBtn);
				deleteButtonCustom.setVisibility(0);//set visible
				deleteButtonCustom.setClickable(true);
				deleteButtonCustom.setFocusable(true);

				deleteButtonCustom.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) 
					{
						deleteCheckedItems();
						for(int i=0; i< adapter.getCount(); i++)
						{
							((CheckBox)gv.getChildAt(i).findViewById(R.id.gallery_checkBox_item)).setVisibility(4); //hide all check boxes
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
		final ImageButton takeApic = (ImageButton)findViewById(R.id.gallery_camBtn);

		takeApic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Switching to home screen
				dispatchTakePictureIntent();
			}
		});

		gv= (GridView)findViewById(R.id.gallery_gridView);
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) { 
				openPicDialog(position);
			}
		});

		adapter= new CustomAdapter(this, R.layout.gallery_item, gallaryArr);
		gv.setAdapter(adapter);

	}

	public void openPicDialog(int pos) {	
		final Dialog dialog = new Dialog(GalleryActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_gallery_dialog);
		dialog.show();

		LinearLayout lay = (LinearLayout)dialog.findViewById(R.id.custum_gallary_layout);
		
		Bitmap bmImg = gallaryArr.get(pos).pic;
		BitmapDrawable background = new BitmapDrawable(bmImg);
		lay.setBackgroundDrawable(background);
		
		dialog.setCanceledOnTouchOutside(true);		

	}


	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			mImageView = (ImageView) findViewById(R.id.imgView);

			//Saving in Parse server
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] dataTostore = stream.toByteArray();



			final ParseFile imgFile = new ParseFile ("img.png", dataTostore);
			imgFile.saveInBackground();

			GalleryObj tempPicObj = new GalleryObj();
			ParseObject obj = new ParseObject("Gallery");

			obj.put("pic",imgFile);
			obj.put("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
			try {
				obj.save();
				tempPicObj.id  = obj.getObjectId();
				tempPicObj.pic = imageBitmap;
				STgallery.getInstance().add(tempPicObj);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	public void deleteCheckedItems() 
	{
		int sizeAdapter = adapter.getCount();
		for (int i = sizeAdapter - 1; i >= 0 ; i--)
		{

			View view = gv.getChildAt(i);

			CheckBox cv = (CheckBox) view.findViewById(R.id.gallery_checkBox_item);
			if(cv.isChecked())
			{
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Gallery");
				query.getInBackground(STgallery.getInstance().get(i).id, new GetCallback<ParseObject>() {
					public void done(ParseObject galleryPic, ParseException e) {
						if (e == null) {
							//delete from server
							try {
								galleryPic.delete();
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
							galleryPic.saveInBackground();
						}
						else
						{
							Toast.makeText(GalleryActivity.this, 
									getResources().getString(R.string.please_check_internet_connection), 
									Toast.LENGTH_LONG
									).show();
							e.printStackTrace();
						}
					}
				});
				STgallery.getInstance().remove(i);				
			}
			adapter.notifyDataSetChanged();
		}
		Toast.makeText(getApplicationContext(), "Selected Items Cleared", Toast.LENGTH_SHORT).show();
	}
	private void fillData() {
		// Get all of the notes from the database and create the item list
		ringProgressDialog = ProgressDialog.show(GalleryActivity.this, "Please wait", "importing pics from Server");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Gallery");
		query.whereEqualTo("Apartment", ParseUser.getCurrentUser().getString("Apartment"));
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> listDB, ParseException e) {
				if (e == null) {

					for (ParseObject i : listDB)
					{							
						final GalleryObj picObj = new GalleryObj();
						picObj.id  = i.getObjectId();
						ParseFile imgFile = (ParseFile)i.get("pic");
						if (imgFile!=null)
						{
							imgFile.getDataInBackground(new GetDataCallback() {
								public void done(byte[] data, ParseException e) {
									if (e == null) {
										if (data != null)
										{
											Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
											picObj.pic = bitmap;
											STgallery.getInstance().add(picObj);
											adapter.notifyDataSetChanged();
											if(ringProgressDialog.isShowing())
											{
												ringProgressDialog.dismiss();
											}
										}
									} 
								}
							});
						}
					}
					if(ringProgressDialog.isShowing())
					{
						ringProgressDialog.dismiss();
					}

				} else {
					if(ringProgressDialog.isShowing())
					{
						ringProgressDialog.dismiss();
					}
					Toast.makeText(GalleryActivity.this, 
							getResources().getString(R.string.please_check_internet_connection), 
							Toast.LENGTH_LONG
							).show();
					Log.d("listDB", "Error: " + e.getMessage());
				}
			}
		});
		
	}


	class CustomAdapter extends ArrayAdapter<GalleryObj>{

		Context context; 
		int layoutResourceId;    
		ArrayList<GalleryObj> data = null;
		private LayoutInflater mInflater; 	

		public CustomAdapter(Context customAdapter, int layoutResourceId, ArrayList<GalleryObj> data) {

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
				convertView = mInflater.inflate(R.layout.gallery_item, null);

				holder = new ViewHolder();

				//fill the views
				holder.image = (ImageView) convertView.findViewById(R.id.gallery_imageView);


				convertView.setTag(holder);						
			} 
			else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();//			
			}

			if(data.get(position).pic != null)
			{
				holder.image.setImageBitmap(data.get(position).pic);
				holder.image.setVisibility(0);
			}


			return convertView;
		}


		class ViewHolder {		
			ImageView image;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		menu.getItem(4).setIcon(R.drawable.funzoneimg_pressed);
		return true;
	}

}
