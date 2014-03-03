package com.example.home;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.finalapp.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;



public class CamActivity extends Activity {

	static final int REQUEST_IMAGE_CAPTURE = 1;

	ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cam);
		dispatchTakePictureIntent();

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
			//mImageView.setImageBitmap(imageBitmap);

			//Saving in Parse server
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] dataTostore = stream.toByteArray();


			final ParseFile imgFile = new ParseFile ("img.png", dataTostore);
			imgFile.saveInBackground();


			//save pic in parse
			ParseQuery<ParseObject> query = ParseQuery.getQuery("usersAuthoirzed");
			query.whereEqualTo("Name", ParseUser.getCurrentUser().get("Name"));
			query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (object != null) {
						object.put("pic",imgFile);
						object.saveInBackground();
						finish();
					}
				}
			});

		}
	}




}








