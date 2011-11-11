package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import com.fatcatlab.tanchess.BTMessage.PacketCodes;

public class BTChessmanSprite extends ChessmanSprite {

	public BTChessmanSprite(float pX, float pY, TextureRegion pTextureRegion, TextureRegion _pTextureRegion,
			TextureRegion image, Engine pEngine) {
		super(pX, pY, pTextureRegion,_pTextureRegion, image, pEngine);
	}

	@Override
	protected void workToDoOnKeyDown() {
		BTMessage msg = new BTMessage();
		msg.packetCodes = PacketCodes.CHESSMAN_SELECT_EVENT;
		msg.baseMessage = new ChessmanIDStruct(this.chessmanID);
		StartActivity.Instance.getmMainScene().mBtGameScene.sendMessage(msg);
	}

	@Override
	protected void workToDoOnKeyUp(float x, float y) {
		BTMessage msg = new BTMessage();
		msg.packetCodes = PacketCodes.CHESSMAN_MOVE_EVENT;
		msg.baseMessage = new ChessmanMoveStruct(this.chessmanID, x, y);
		StartActivity.Instance.getmMainScene().mBtGameScene.sendMessage(msg);
	}

	@Override
	protected void workToDoOnChange(int id) {
		BTMessage msg = new BTMessage();
		msg.packetCodes = PacketCodes.CHESSMAN_CHANGE_EVENT;
		msg.baseMessage = new ChessmanIDStruct(id);
		StartActivity.Instance.getmMainScene().mBtGameScene.sendMessage(msg);
	}

	@Override
	protected void workToDoOnEnlarge(int id) {
		BTMessage msg = new BTMessage();
		msg.packetCodes = PacketCodes.CHESSMAN_ENLARGE_EVENT;
		msg.baseMessage = new ChessmanIDStruct(id);
		StartActivity.Instance.getmMainScene().mBtGameScene.sendMessage(msg);
	}

}
