package com.fatcatlab.tanchess;

public class ChessmanMoveStruct extends BaseMessage {
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
