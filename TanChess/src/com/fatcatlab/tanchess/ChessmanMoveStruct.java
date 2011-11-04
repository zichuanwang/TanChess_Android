package com.fatcatlab.tanchess;

import java.io.Serializable;

public class ChessmanMoveStruct extends BaseMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2394206649925660377L;
	public int chessmanID;
	public float x;
	public float y;
	public ChessmanMoveStruct(int id, float _x, float _y)
	{
		this.chessmanID = id;
		this.x = _x;
		this.y = _y;
	}
}
