package com.example.finalapp;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Parse.initialize(this, "6xDnXjWOcfFRJMlrIJxdVnvtjlTqRgtq8VnSh9Qn", "cxSxdJr5nke0qSxPAIYgU8TwLmzsXZnyzM5U4z8t");
	}
}
