package com.example.shopping;

import java.util.ArrayList;

//singleton class
public class ShoppingListArray extends ArrayList<ShoppingList> {

	private static ShoppingListArray instance =  null;
	//private constructor - preventing other classes to build another instance
	private ShoppingListArray() {}


	// Static 'instance' method
	public synchronized static ShoppingListArray getInstance() 
	{
		if (instance == null)
		{
			instance = new ShoppingListArray();
		}
		return instance;
	}
}