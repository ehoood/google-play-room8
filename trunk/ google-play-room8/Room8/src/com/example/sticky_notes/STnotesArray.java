package com.example.sticky_notes;

import java.util.ArrayList;

//singleton class
public class STnotesArray extends ArrayList<StickyNoteObj> 
{
	private static final long serialVersionUID = 1L;

	private static STnotesArray notes_arr = null;

	//private constructor - preventing other classes to build another instance
	private STnotesArray() {}

	// Static 'instance' method
	public synchronized static STnotesArray getInstance() 
	{
		if(notes_arr == null)
			notes_arr = new STnotesArray();
			
		return notes_arr;
	}
}
