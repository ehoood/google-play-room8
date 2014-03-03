package com.example.finalapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.bills.BillsActivity;
import com.example.budget.BudgetManagerActivity;
import com.example.gallery.GalleryActivity;
import com.example.shopping.ShoppingListActivity;
import com.example.sticky_notes.StickyNoteActivity;

public class SplitActionBarActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_split_action_bar);
		getActionBar().setSplitBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.split_action_bar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.bills:
		{
			if(!(this instanceof BillsActivity))
			{
				
				Intent intent = new Intent(SplitActionBarActivity.this, BillsActivity.class);
				startActivity(intent);
			}
			break;
		}
		case R.id.shopping:
		{
			if(!(this instanceof ShoppingListActivity))
			{
				Intent intent = new Intent(SplitActionBarActivity.this, ShoppingListActivity.class);
				startActivity(intent);
			}
			break;
		}
		case R.id.budget:
		{
			
			if(!(this instanceof BudgetManagerActivity))
			{

				Intent intent = new Intent(SplitActionBarActivity.this, BudgetManagerActivity.class);
				startActivity(intent);
			}
			break;
		}
		case R.id.schedule:
		{
			if(!(this instanceof StickyNoteActivity))
			{

				Intent intent = new Intent(SplitActionBarActivity.this, StickyNoteActivity.class);
				startActivity(intent);
			}
			break;
		}
		case R.id.gallery:
		{
			if(!(this instanceof GalleryActivity))
			{
				Intent intent = new Intent(SplitActionBarActivity.this, GalleryActivity.class);
				startActivity(intent);
			}
			break;
		}

		}
		return super.onOptionsItemSelected(item);
	}




}


