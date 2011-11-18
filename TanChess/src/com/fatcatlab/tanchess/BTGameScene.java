package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import android.util.Log;
import com.fatcatlab.tanchess.BTMessage.PacketCodes;

public class BTGameScene extends GameScene {

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	protected boolean mIsHost;
	private boolean hasReceivedCCA = false;

	// Member object for the chat services

	public BTGameScene(int pLayerCount, Engine baseEngine) {
		super(pLayerCount, baseEngine);
		// TODO Auto-generated constructor stub
	}
	
	protected boolean isInCharge() {
		boolean result = false;
		if(mBrain.getCurrentPlayer() == Brain.PLAYER1 && mIsHost) {
			result = true;
		}
		else if(mBrain.getCurrentPlayer() == Brain.PLAYER2 && !mIsHost) {
			result = true;
		}
		return result;
	}
	
	protected void sendCollisionChessman() {
		ChessmanCollisionArray array = this.mBrain.generateChessmanCollisionArray();
		BTMessage message = new BTMessage();
		message.packetCodes = PacketCodes.CHESSMAN_COLLISION_EVENT;
		message.baseMessage = array;
		this.sendMessage(message);
	}
	
	public void updateCollisionChessmanData() {
		if( this.isInCharge() ) {
			Log.d("BT","send cca");
	        this.sendCollisionChessman();
	        this.hasSentUpdateData = true;
	        if(!this.mBrain.isForbidPropOn) {
	        	this.rivalHaschangeTurn = true;
	        }
	        else {
	        	BTMessage message = new BTMessage();
				message.packetCodes = PacketCodes.CHANGE_TURN_EVENT;
				this.sendMessage(message);
	        }
	    }
	    else {
	    	Log.d("BT","register new cca timer");
	    	this.registerUpdateHandler(new TimerHandler(0.3f, true, new ITimerCallback() {
	    		float time = 0;
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) {
					time += 0.3f;
					Log.d("BT","cca timer:"+new Float(time).toString());
					if(time >= 5.0f || hasReceivedCCA) {
						if(hasReceivedCCA) {
							hasReceivedCCA = false;
							Log.d("BT","receive cca");
						}
						else {
							Log.d("BT","waiting cca out of time");
						}
						unregisterUpdateHandler(pTimerHandler);
						BTGameScene.this.hasSentUpdateData = true;
					}
				}
			}));
	    }
	}
	
	protected void changePlayer() {
		if(this.isInCharge() || mBrain.isForbidPropOn) {
			BTMessage message = new BTMessage();
			message.packetCodes = PacketCodes.CHANGE_TURN_EVENT;
			this.sendMessage(message);
		}
		
		if(mBrain.isForbidPropOn == true)
    	{
    		this.shutDownForbid();
    		return;
    	}
		else {
			if(this.isInCharge()) {
				mBrain.changePlayerWhenBTConnecting();
			}
			else {
				mBrain.changePlayer();
			}
		}
	}

	protected void onLoadScene() {
		super.onLoadScene();
		if(isFirstTime == true)
			Log.d("first time","yes");
		else
			Log.d("first time","no");
			
		StartActivity.SCENE_STATE = StartActivity.STATE_BTGAMESCENE;
		
		if(BluetoothService.getService().isMacAddressLarger()) {
			mIsHost = true;
		}
		else {
			mIsHost = false;
			mBrain.setPlayer1Forbad();
		}
	}
	
	
	protected BTChessmanSprite createChessman(float posX, float posY, float scale, TextureRegion image, boolean group) {
		int SCREEN_WIDTH = StartActivity.CAMERA_WIDTH;
    	int SCREEN_HEIGHT = StartActivity.CAMERA_HEIGHT;
    	BTChessmanSprite sprite;
    	if(group == Brain.GROUP1)
    		sprite = new BTChessmanSprite(SCREEN_WIDTH / 2 + posX - this.mChessmanRedRgn.getWidth() / 2,
    			SCREEN_HEIGHT / 2 + posY - this.mChessmanRedRgn.getHeight() / 2, mChessmanRedRgn, mChessmanGreenRgn, image, mEngine);
    	else
    		sprite = new BTChessmanSprite(SCREEN_WIDTH / 2 + posX - this.mChessmanRedRgn.getWidth() / 2,
        			SCREEN_HEIGHT / 2 + posY - this.mChessmanRedRgn.getHeight() / 2, mChessmanGreenRgn, mChessmanRedRgn, image, mEngine);
    	return (BTChessmanSprite)this.createChessmanHelp(sprite, posX, posY, scale, image, group);
    }

	public void HandleMessage(BTMessage msg) {
		Log.d("BT", msg.packetCodes.name());
		switch (msg.packetCodes) {
		case CHESSMAN_SELECT_EVENT: {
			ChessmanIDStruct idStruct = (ChessmanIDStruct)msg.baseMessage;
			Log.d("BT", "CHESSMAN_SELECT_EVENT"+new Integer(idStruct.chessmanID).toString());
			mBrain.setChessmanSelected(idStruct.chessmanID);
			break;
		}
		case CHESSMAN_ENLARGE_EVENT: {
			ChessmanIDStruct idStruct = (ChessmanIDStruct)msg.baseMessage;
			mBrain.setEnlarge(idStruct.chessmanID);
			break;
		}
		case CHESSMAN_CHANGE_EVENT: {
			ChessmanIDStruct idStruct = (ChessmanIDStruct)msg.baseMessage;
			mBrain.setChange(idStruct.chessmanID);
			break;
		}
		case CHESSMAN_MOVE_EVENT: {
			ChessmanMoveStruct moveStruct = (ChessmanMoveStruct)msg.baseMessage;
			Log.d("BT", "CHESSMAN_MOVE_STRUCT"+new Integer(moveStruct.chessmanID).toString());
			mBrain.setChessmanMove(moveStruct.chessmanID, moveStruct.x, moveStruct.y);
			break;
		}
		case CHESSMAN_COLLISION_EVENT: {
			Log.d("BT", "receive chessman collision event");
			ChessmanCollisionArray cca = (ChessmanCollisionArray)msg.baseMessage;
			this.mBrain.reconcileChessmanCollisionArray(cca);
			hasReceivedCCA = true;
			break;
		}
		case PLAY_SOUND_EVENT: {
			PropIDStruct propStruct = (PropIDStruct)msg.baseMessage;
			mBrain.setPropClick(propStruct.PropID, propStruct.category);
			break;
		}
		case CHANGE_TURN_EVENT: {
			this.rivalHaschangeTurn = true;
			break;
		}
		}
	}

	@Override
	protected void createProp(float posX, float posY, TextureRegion rgn, boolean group, int category) {
		BTPropSprite sprite = new BTPropSprite(posX - rgn.getWidth() / 2, posY - rgn.getHeight() / 2, rgn, mEngine);
		this.createPropHelp(sprite, rgn, group, category);	
	}

	public void sendMessage(BTMessage msg) {
		if (BluetoothService.getService().getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}
		if (msg != null) {
			BluetoothService.getService().write(msg);
		}
	}

	// use for test
	public void handleString(String str) {
		Log.d("handlerString", str);
	}
	
	// use for test
	public void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (BluetoothService.getService().getState() != BluetoothService.STATE_CONNECTED) {
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			BluetoothService.getService().write(send);
		}
	}
}
