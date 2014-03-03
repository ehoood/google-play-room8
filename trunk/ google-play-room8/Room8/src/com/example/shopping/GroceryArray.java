package com.example.shopping;


import java.util.ArrayList;


//singleton class
public class GroceryArray extends ArrayList<GroceryListItem> {

	private static GroceryArray instance = null;
	//private constructor - preventing other classes to build another instance
	private GroceryArray() {}

	// Static 'instance' method
	public synchronized static GroceryArray getInstance() 
	{
		if (instance == null){
			instance = new GroceryArray();
		}
		return instance;
	}
}





