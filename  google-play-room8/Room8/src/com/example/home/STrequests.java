package com.example.home;

import java.util.ArrayList;

public class STrequests extends ArrayList<reqObj>{
private static final long serialVersionUID = 3L;
	
	private static STrequests requests_arr = null;

	//private constructor - preventing other classes to build another instance
	private STrequests() {}

	// Static 'instance' method
	public synchronized static STrequests getInstance() 
	{
		if(requests_arr == null)
			requests_arr = new STrequests();
		
		return requests_arr;
	}
}
