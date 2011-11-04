package com.fatcatlab.tanchess;

import java.io.Serializable;

public class PropIDStruct extends BaseMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3948816827332188299L;
	public int category;
	public int PropID;
	public PropIDStruct(int _c, int _p) {
		// TODO Auto-generated constructor stub
		category = _c;
		PropID = _p;
	}

}
