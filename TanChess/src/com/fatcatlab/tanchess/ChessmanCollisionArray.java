package com.fatcatlab.tanchess;

public class ChessmanCollisionArray extends BaseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 502002245749089484L;
	public int[] chessmanID;
	public float[] Position_x;
	public float[] Position_y;
	public ChessmanCollisionArray(){
		chessmanID = new int[32];
		Position_x = new float[32];
		Position_y = new float[32];
	}
	public void addItemWithCCS(ChessmanCollisionStruct ccs, int index) {
		chessmanID[index] = ccs.ID;
		Position_x[index] = ccs.Position_x;
		Position_y[index] = ccs.Position_y;
	}
	public ChessmanCollisionStruct getCSSAtIndex(int index) {
		ChessmanCollisionStruct css = new ChessmanCollisionStruct(Position_x[index], Position_y[index], chessmanID[index]);
		return css;
	}
}
