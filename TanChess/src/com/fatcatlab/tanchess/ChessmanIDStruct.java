package com.fatcatlab.tanchess;

import java.io.Serializable;

public class ChessmanIDStruct extends BaseMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7503173779130344964L;
	public int chessmanID;
	public ChessmanIDStruct(int id){
		this.chessmanID = id;
	}
}
