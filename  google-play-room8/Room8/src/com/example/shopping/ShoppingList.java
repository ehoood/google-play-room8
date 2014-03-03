package com.example.shopping;

public class ShoppingList {

	public String title;
	public String date;
	public String id;
	
	public ShoppingList(String title, String date, String id) {
		this.title = title;
		this.date = date;
		this.id = id;
	}
	
	public void setObj (String title, String date, String id) {
		this.title = title;
		this.date = date;
		this.id = id;
	}
}
