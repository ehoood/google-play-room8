package com.example.bills;

import java.util.ArrayList;

//singleton class
public class STBillPaymentsArray extends ArrayList<paymentObject> 
{
	private static final long serialVersionUID = 3073452913481864927L;

	private static STBillPaymentsArray billPaymentArray = null;
	private String sameDirectoryString  = null;

	//private constructor - preventing other classes to build another instance
	private STBillPaymentsArray() {}

	// Static 'instance' method
	public synchronized static STBillPaymentsArray getInstance() 
	{
		if (billPaymentArray == null)
		{
			billPaymentArray = new STBillPaymentsArray();
		}
		return billPaymentArray;
	}
	public synchronized void setSameDierctoryString(String aSetStringValue)
	{
		sameDirectoryString = aSetStringValue;
	}
	public synchronized String getsameDirectoryString()
	{
		return sameDirectoryString;
	}
	
}