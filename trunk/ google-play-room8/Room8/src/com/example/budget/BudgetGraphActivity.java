package com.example.budget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalapp.R;
import com.example.home.HomeObj;
import com.example.home.SThome;
import com.example.shopping.ShoppingList;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class BudgetGraphActivity extends Activity {
	private ArrayList<String> namesArr = new ArrayList<String>();
	int tmp;
	LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budget_graph);

		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int currMonth = cal.get(Calendar.MONTH);
		currMonth++;

		String [] strArr;

		switch (currMonth) {
		case 1:
			strArr = new String[] {"Jan"};
			break;
		case 2:
			strArr = new String[] {"Jan", "Feb"};
			break;
		case 3:
			strArr = new String[] {"Jan", "Feb", "Mar"};
			break;
		case 4:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr"};
			break;
		case 5:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May"};
			break;
		case 6:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun"};
			break;
		case 7:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul"};
			break;
		case 8:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug"};
			break;
		case 9:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug","Sep"};
			break;
		case 10:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug","Sep","Oct"};
			break;
		case 11:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
			break;
		case 12:
			strArr = new String[] {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			break;
		default:
			strArr = new String[] {"Jan"};
			break;
		}

		strArr = new String[] {"Jan", "Feb", "Mar","Apr"};
		currMonth=4;

		ParseQuery<ParseObject> queryLog = ParseQuery.getQuery("LogBudget");
		queryLog.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
		queryLog.whereEqualTo("Name", ParseUser.getCurrentUser().get("Name"));
		List<ParseObject> objects;
		try {
			objects = queryLog.find();
			if (objects.size()>0)
			{
				GraphViewData[] data = new GraphViewData[currMonth];
				for (int i=0; i<currMonth; i++) {
					double monthAmount = objects.get(0).getDouble(strArr[i]);
					data[i] = new GraphViewData(i, monthAmount);
				}
				GraphViewSeries exampleSeries = new GraphViewSeries(data);
				BarGraphView graphViewB = new BarGraphView(
						this
						, "My Expenses"
						);
				graphViewB.setDrawValuesOnTop(true);
				graphViewB.addSeries(exampleSeries); // data
				graphViewB.setHorizontalLabels(strArr);
				layout = (LinearLayout) findViewById(R.id.graph1);
				layout.addView(graphViewB);
			}else
			{
				Toast.makeText(getApplicationContext(), "You must have bills first for presenting graphs", Toast.LENGTH_LONG).show();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String Name;
		int j =0;


		LineGraphView graphViewL = new LineGraphView(
				this
				, "Apartment Expenses"
				);

		// Get names of users
		for (HomeObj user : SThome.getInstance())
		{
			//namesArr.add(user.name);
			Name = user.name;
			ParseQuery<ParseObject> query = ParseQuery.getQuery("LogBudget");
			query.whereEqualTo("Apartment", ParseUser.getCurrentUser().get("Apartment"));
			query.whereEqualTo("Name", Name);
			List<ParseObject> objectsNew;
			try {
				objectsNew = query.find();
				if (objectsNew.size() > 0)
				{
					
					GraphViewData[] data2 = new GraphViewData[currMonth];

					for (int i=0; i<currMonth; i++) {
						double monthAmountNew = objectsNew.get(0).getDouble(strArr[i]);
						data2[i] = new GraphViewData(i, monthAmountNew);
					}
					graphViewL.addSeries(new GraphViewSeries(user.name, new GraphViewSeriesStyle(Color.rgb(100 + j*20, 100 - j*20, 100 + j*20), 3), data2));

				}else
				{
					Toast.makeText(getApplicationContext(), "You must have bills first for presenting graphs", Toast.LENGTH_LONG).show();
				}
				
				
			} catch (ParseException e) {
				e.printStackTrace();
			}

			j++;

		}

		// set legend
		graphViewL.setShowLegend(true);
		graphViewL.setLegendAlign(LegendAlign.BOTTOM);
		graphViewL.setLegendWidth(200);

		graphViewL.setHorizontalLabels(strArr);

		layout = (LinearLayout) findViewById(R.id.graph2);
		layout.addView(graphViewL);

	}
}
