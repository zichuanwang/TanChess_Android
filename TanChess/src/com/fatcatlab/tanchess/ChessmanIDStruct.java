package com.fatcatlab.tanchess;

public class ChessmanIDStruct extends BaseMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7503173779130344964L;
	public int chessmanID;
	public ChessmanIDStruct(int id){
		this.chessmanID = id;
	}
}
