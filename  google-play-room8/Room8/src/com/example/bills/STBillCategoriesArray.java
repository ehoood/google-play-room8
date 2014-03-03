package com.example.bills;

import java.util.ArrayList;

//singleton class
public class STBillCategoriesArray extends ArrayList<billCategoryObject> 
{
/**
	 * 
	 */
	private static final long serialVersionUID = 3073452913481864927L;

	private static STBillCategoriesArray billCatergoryArray = null;

	//private constructor - preventing other classes to build another instance
	private STBillCategoriesArray() {}

	// Static 'instance' method
	public synchronized static STBillCategoriesArray getInstance() 
	{
		if (billCatergoryArray == null)
		{
			billCatergoryArray = new STBillCategoriesArray();
		}
		return billCatergoryArray;
	}
}