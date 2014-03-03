package com.example.bills;

public class paymentObject 
{	/*payment descriptors in payment list */
	String paymentObjectId;
	String paymentDate; // date when payment was paid
	
	/*payment information*//* fields of bill information*/
	String  category;
	String  createdby;
	String  paymentDuration;
	String  paidBy;
	String  paymentDueDate;
	String 	numOfmonths;
	String 	amount;
	String 	perAmount;
	boolean isPaid;		// boolean determines whether payment was paid


	public paymentObject(String aPaymentObjectId,String aCategory,String aPaymentDate,boolean aPaymentPaid,
						 String aCreatedBy,String aPaymentDuration,String aPaidBy,
						 String aPaymentDueDate,String aNumOfMonths,String aAmount,String aPerAmount) 
	{
		this.paymentObjectId = aPaymentObjectId;
		this.category		 = aCategory;
		this.paymentDate 	 = aPaymentDate;
		this.isPaid	    	 = aPaymentPaid;
		this.createdby  	 = aCreatedBy;
		this.paymentDuration = aPaymentDuration;
		this.paidBy		 	 = aPaidBy;
		this.paymentDueDate  = aPaymentDueDate;
		this.numOfmonths	 = aNumOfMonths;
		this.amount			 = aAmount;
		this.perAmount	     = aPerAmount;
	}
	public void setPaymentObject(String aPaymentDate,boolean aPaymentPaid,
					String aCreatedBy,String aPaymentDuration,String aPaidBy,
					String aPaymentDueDate,String aNumOfMonths,String aAmount,String aPerAmount)
	{
		this.paymentDate 	 = aPaymentDate;
		this.isPaid	    	 = aPaymentPaid;
		this.createdby  	 = aCreatedBy;
		this.paymentDuration = aPaymentDuration;
		this.paidBy		 	 = aPaidBy;
		this.paymentDueDate  = aPaymentDueDate;
		this.numOfmonths	 = aNumOfMonths;
		this.amount			 = aAmount;
		this.perAmount	     = aPerAmount;
	}

}
