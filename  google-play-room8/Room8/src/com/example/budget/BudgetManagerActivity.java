package com.example.budget;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.finalapp.SplitActionBarActivity;
import com.example.home.HomeActivity;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.parse.ParseUser;

public class BudgetManagerActivity extends SplitActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget_manager);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		actionBar.setCustomView(R.layout.action_budget);
	
		onClickBtn();		
		
		final ImageButton homeButton = (ImageButton)findViewById(R.id.home_btn_budget);

		homeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(BudgetManagerActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		});
		
		final ImageButton graphButton = (ImageButton)findViewById(R.id.budget_graphBtn);

		graphButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Switching to home screen
				Intent intent = new Intent(BudgetManagerActivity.this, BudgetGraphActivity.class);
				startActivity(intent);
			}
		});
		
	}
	
	public void onClickBtn()
	{
		ImageButton addBudget = (ImageButton) findViewById(R.id.budget_addBtn);
		ImageButton editBudget = (ImageButton) findViewById(R.id.budget_editBtn);
		if (ParseUser.getCurrentUser().getBoolean("BudgetfirstTime"))
		{
			addBudget.setOnClickListener(new View.OnClickListener() {		
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BudgetManagerActivity.this,BudgetAddActivity.class);
					startActivity(intent);
				}
			});
			
			editBudget.setOnClickListener(new View.OnClickListener() {		
				@Override
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(), "You have to add a budget first", Toast.LENGTH_LONG).show();
				}
			});
		}else
		{
			//not first time click on edit
			addBudget.setOnClickListener(new View.OnClickListener() {		
				@Override
				public void onClick(View v) {
					Toast.makeText(getApplicationContext(), "You already have a budget you can edit it, using the pencil button", Toast.LENGTH_LONG).show();
				}
			});
			
			editBudget.setOnClickListener(new View.OnClickListener() {		
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BudgetManagerActivity.this,BudgetEditActivity.class);
					startActivity(intent);
				}
			});
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		onClickBtn();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.shopping_list, menu);
		
		menu.getItem(2).setIcon(R.drawable.budget_tab_blue);
		return true;
	}
}


