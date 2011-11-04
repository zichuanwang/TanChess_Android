package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import com.fatcatlab.tanchess.BTMessage.PacketCodes;

public class BTPropSprite extends PropSprite{

	public BTPropSprite(float pX, float pY, TextureRegion pTextureRegion,
			Engine pEngine) {
		super(pX, pY, pTextureRegion, pEngine);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void workToDoOnKeyUp(int category, int id) {
		BTMessage msg = new BTMessage();
		msg.packetCodes = PacketCodes.PLAY_SOUND_EVENT;
		msg.baseMessage = new PropIDStruct(category,id);
		StartActivity.Instance.getmMainScene().mBtGameScene.sendMessage(msg);
	}

}
