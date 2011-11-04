package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import android.util.Log;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

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
	
	protected void changePlayer() {
		if(this.isInCharge() || mBrain.isForbidPropOn) {
			// 發送change turn消息
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
    	TextureRegion rgn;
    	if(group == Brain.GROUP1)
    		rgn = this.mChessmanRedRgn;
    	else
    		rgn = this.mChessmanGreenRgn;
    	int SCREEN_WIDTH = StartActivity.CAMERA_WIDTH;
    	int SCREEN_HEIGHT = StartActivity.CAMERA_HEIGHT;
    	BTChessmanSprite sprite = new BTChessmanSprite(SCREEN_WIDTH / 2 + posX - rgn.getWidth() / 2,
    			SCREEN_HEIGHT / 2 + posY - rgn.getHeight() / 2, rgn, image, mEngine);
    	sprite.setScale(scale);
    	sprite.setGroup(group);
    	
    	FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 2.0f);
    	Shape shape = new Rectangle(SCREEN_WIDTH / 2 + posX - 26 * scale, SCREEN_HEIGHT / 2 + posY - 26 * scale, 52 * scale, 52 * scale);
    	if(group == Brain.GROUP2) {
    		shape.setRotation(180.0f);
    	}
    	Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, shape, BodyType.DynamicBody, objectFixtureDef);
    	body.setBullet(true);
		MassData mass = body.getMassData();
		mass.mass = 3.0f;
		body.setMassData(mass);
		
		if( scale == ChessmanSprite.SMALL_SIZE ) {
			body.setLinearDamping(1.0f);
			sprite.value = 2;
		}
		else if( scale == ChessmanSprite.LARGE_SIZE ) {
			body.setLinearDamping(4.0f);
			sprite.value = 8;
		}
		else if( scale == ChessmanSprite.MEDIUM_SIZE ) {
			body.setLinearDamping(2.5f);
			sprite.value = 4;
		}
		body.setAngularDamping(2.0f);
		PhysicsConnector pc = new PhysicsConnector(sprite, body, true, true);
		sprite.setmPhysicsConnector(pc);
		this.mPhysicsWorld.registerPhysicsConnector(pc);
		
		//this.mPhysicsWorld.clearPhysicsConnectors();
    	sprite.setBody(body);
    	mBrain.addChessman(sprite);
    	this.getChild(1).attachChild(sprite);
    	final IEntity lastChild = this.getLastChild();
    	lastChild.attachChild(sprite.mGunsight);
    	
    	this.registerTouchArea(sprite);
    	sprite.body = body;
    	sprite.setGameScene(this);
    	return sprite;
    }

	public void HandleMessage(BTMessage msg) {
		Log.d("message", msg.packetCodes.name());
		switch (msg.packetCodes) {
		case CHESSMAN_SELECT_EVENT:
			ChessmanIDStruct idStruct = (ChessmanIDStruct)msg.baseMessage;
			Log.d("CHESSMAN_SELECT_EVENT", new Integer(idStruct.chessmanID).toString());
			mBrain.setChessmanSelected(idStruct.chessmanID);
			break;
		case CHESSMAN_ENLARGE_EVENT:
			ChessmanIDStruct idStruct2 = (ChessmanIDStruct)msg.baseMessage;
			mBrain.setEnlarge(idStruct2.chessmanID);
			break;
		case CHESSMAN_CHANGE_EVENT:
			ChessmanIDStruct idStruct3 = (ChessmanIDStruct)msg.baseMessage;
			mBrain.setChange(idStruct3.chessmanID);
			break;
		case CHESSMAN_MOVE_EVENT:
			ChessmanMoveStruct moveStruct = (ChessmanMoveStruct)msg.baseMessage;
			Log.d("CHESSMAN_MOVE_STRUCT", new Integer(moveStruct.chessmanID).toString());
			mBrain.setChessmanMove(moveStruct.chessmanID, moveStruct.x, moveStruct.y);
			break;
		case PLAY_SOUND_EVENT:
			PropIDStruct propStruct = (PropIDStruct)msg.baseMessage;
			mBrain.setPropClick(propStruct.PropID, propStruct.category);
		}
	}

	@Override
	protected void createProp(float posX, float posY, TextureRegion rgn,
			boolean group, int category) {
		
		BTPropSprite sprite = new BTPropSprite(posX - rgn.getWidth() / 2, posY - rgn.getHeight() / 2, rgn, mEngine);
    	sprite.setScale(0.85f);
    	if(group) {
    		sprite.setRotation(180.0f);
    	}
    	switch(category) {
    	case PropSprite.POWERUP:
    		sprite.score = PropSprite.POWERUP_NEED_SCORE;
    		break;
    	case PropSprite.FORBID:
    		sprite.score = PropSprite.FORBID_NEED_SCORE;
    		break;
    	case PropSprite.ENLARGE:
    		sprite.score = PropSprite.ENLARGE_NEED_SCORE;
    		break;
    	case PropSprite.CHANGE:
    		sprite.score = PropSprite.CHANGE_NEED_SCORE;
    		break;
    	default:
    		break;
    	}
    	this.registerTouchArea(sprite);
    	this.getChild(0).attachChild(sprite);
    	this.mBrain.addProp(sprite);
    	sprite.category = category;
    	sprite.group = group;
    	sprite.setGameScene(this);
		
		
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

	@Override
	public ChessmanSprite createNewChessman(float posX, float posY,
			float scale, TextureRegion image, boolean group, float rotation) {
		TextureRegion rgn;
    	if(group == Brain.GROUP1)
    		rgn = this.mChessmanRedRgn;
    	else
    		rgn = this.mChessmanGreenRgn;
    	BTChessmanSprite sprite = new BTChessmanSprite( posX - rgn.getWidth() / 2,
    			 posY - rgn.getHeight() / 2, rgn, image, mEngine);
    	sprite.setScale(scale);
    	//because group should be changed so here should use !group
    	sprite.setGroup(!group);
    	
    	FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 2.0f);
    	Shape shape = new Rectangle( posX - 26 * scale,  posY - 26 * scale, 52 * scale, 52 * scale);
    	shape.setRotation(rotation);
    	sprite.setRotation(rotation);
    	Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, shape, BodyType.DynamicBody, objectFixtureDef);
    	body.setBullet(true);
    	MassData mass = body.getMassData();
		mass.mass = 3.0f;
		body.setMassData(mass);
		
		if( scale == ChessmanSprite.SMALL_SIZE ) {
			body.setLinearDamping(1.0f);
			sprite.value = 2;
		}
		else if( scale == ChessmanSprite.LARGE_SIZE ) {
			body.setLinearDamping(4.0f);
			sprite.value = 8;
		}
		else if( scale == ChessmanSprite.MEDIUM_SIZE ) {
			body.setLinearDamping(2.5f);
			sprite.value = 4;
		}
		body.setAngularDamping(2.0f);
		PhysicsConnector pc = new PhysicsConnector(sprite, body, true, true);
		sprite.setmPhysicsConnector(pc);
		this.mPhysicsWorld.registerPhysicsConnector(pc);
		
		//this.mPhysicsWorld.clearPhysicsConnectors();
    	sprite.setBody(body);
    	mBrain.addChessman(sprite);
    	this.getChild(1).attachChild(sprite);
    	final IEntity lastChild = this.getLastChild();
    	lastChild.attachChild(sprite.mGunsight);
    	//注锟结触锟斤拷锟斤拷锟斤拷
    	this.registerTouchArea(sprite);
    	sprite.body = body;
    	sprite.setGameScene(this);
    	sprite.isForbad = false;
    	return sprite;
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
