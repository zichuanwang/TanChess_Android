package com.fatcatlab.tanchess;

import java.io.Serializable;

public class ChessmanCollisionStruct extends BaseMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7528961044821315316L;
	public float Position_x;
	public float Position_y;
	public float Angle;
	public ChessmanCollisionStruct(float px, float py, float Angle)
	{
		this.Position_x = px;
		this.Position_y = py;
		this.Angle = Angle;
	}
}
