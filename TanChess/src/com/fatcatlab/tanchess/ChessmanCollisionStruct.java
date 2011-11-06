package com.fatcatlab.tanchess;

public class ChessmanCollisionStruct extends BaseMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7528961044821315316L;
	public float Position_x;
	public float Position_y;
	public int ID;
	public ChessmanCollisionStruct(float px, float py, int id)
	{
		this.Position_x = px;
		this.Position_y = py;
		this.ID = id;
	}
}
