package com.example.gallery;

import java.util.ArrayList;

import com.example.home.HomeObj;
import com.example.home.SThome;

public class STgallery extends ArrayList<GalleryObj>{
	private static final long serialVersionUID = 2L;

	private static STgallery gallery_arr = null;

	//private constructor - preventing other classes to build another instance
	private STgallery() {}

	// Static 'instance' method
	public synchronized static STgallery getInstance() 
	{
		if(gallery_arr == null)
			gallery_arr = new STgallery();
		
		return gallery_arr;
	}
}


