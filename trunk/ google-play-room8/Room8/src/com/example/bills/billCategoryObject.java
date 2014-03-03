package com.example.bills;

import java.util.ArrayList;
import java.util.Date;

public class billCategoryObject 
{
	Date   updateDate;
	String link;
	String title;
	String createdBy;
	String id;
	ArrayList<String> visibleTo;
	
	public billCategoryObject(String aLink, String aTitle,String aCreatedBy,ArrayList<String> aVisibleTo,String aId,Date aDateCreatedUpdated) 
	{
		this.link       = aLink;
		this.title      = aTitle;
		this.createdBy  = aCreatedBy;
		this.visibleTo  = new ArrayList<String>(aVisibleTo);
		this.id	        = aId;
		this.updateDate = aDateCreatedUpdated;
	}
}
