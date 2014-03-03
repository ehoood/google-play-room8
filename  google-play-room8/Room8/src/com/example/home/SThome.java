package com.example.home;

import java.util.ArrayList;

public class SThome extends ArrayList<HomeObj>{
	private static final long serialVersionUID = 2L;
	
	private static SThome home_arr = null;

	//private constructor - preventing other classes to build another instance
	private SThome() {}

	// Static 'instance' method
	public synchronized static SThome getInstance() 
	{
		if(home_arr == null)
			home_arr = new SThome();
		
		return home_arr;
	}
}
