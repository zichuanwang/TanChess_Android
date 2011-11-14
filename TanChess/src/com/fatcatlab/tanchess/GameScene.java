
package com.fatcatlab.tanchess;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.MassData;

import android.util.Log;

public class GameScene extends AbstractGameScene {
	
	public Texture mTexture;
	protected TextureRegion mChessboard;
	protected TextureRegion mBackground;
	public TextureRegion getmBackground() {
		return mBackground;
	}

	public TextureRegion mChessmanRedRgn;
	public TextureRegion mChessmanGreenRgn;
	protected TextureRegion mHorseRgn;
	protected TextureRegion mLeafRgn;
	protected TextureRegion mPresentRgn;
	protected TextureRegion mBellRgn;
	protected TextureRegion mSnowRgn;
	protected TextureRegion mSockRgn;
	protected TextureRegion mHatRgn;
	protected TextureRegion mCookiemanRgn;
	protected TextureRegion mCrutchRgn;
	protected TextureRegion mDavidsdeerRgn;
	protected TextureRegion mHingeRgn;
	protected TextureRegion mPowerUpPropRgn;
	protected TextureRegion mForbidPropRgn;
	protected TextureRegion mEnlargePropRgn;
	protected TextureRegion mChangePropRgn;
	protected TextureRegion mTurnMarkRgn;
	
	public Texture mWinShowTexture;
	public TiledTextureRegion mStarRgn;
	public TiledTextureRegion mWinRgn;
	public TiledSprite mWinSprite;
	public TiledSprite mStarSprite;
	
	protected Sprite mTurnMark[] = new Sprite[2];
	protected static final int CHESSBOARD_LATTICE_WIDTH = 30, CHESSBOARD_LATTICE_HEIGHT = 40;
	public static final int CHESSBOARD_WIDTH = 250, CHESSBOARD_HEIGHT = 370;
	
	public boolean isValid = false;
	public boolean turnValid = false;
	
	protected float turnMarkOpacity = 0;
	protected boolean turnMarkFlag = false;
	
	protected PhysicsWorld mPhysicsWorld;
	
	protected boolean isFirstTime = true;
	protected boolean hasSentUpdateData = false;
	protected boolean isSendingUpdateData = false;
	protected boolean rivalHaschangeTurn = false;
	
	
	public void loadImageResource() {
		mTexture = new Texture(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mBackground = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "background.png", 0, 0);
		mChessboard = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "chessboard.png", 320, 0);
		mChessmanRedRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "red.png", 640, 0);
		mChessmanGreenRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "green.png", 640, 60);
		mCrutchRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "crutch.png", 700, 0);
		mCookiemanRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "cookieman.png", 700, 60);
		mDavidsdeerRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "david'sdeer.png", 700, 120);
		mHorseRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "horse.png", 700, 180);
		mLeafRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "leaf.png", 700, 240);
		mPresentRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "present.png", 700, 300);
		mSnowRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "snow.png", 700, 360);
		mSockRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "sock.png", 700, 420);
		mHatRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "hat.png", 760, 0);
		mBellRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "bell.png", 760, 60);
		mHingeRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "hinge.png", 760, 120);
		mPowerUpPropRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "powerup.png", 760, 160);
		mForbidPropRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "forbid.png", 760, 200);
		mEnlargePropRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "enlarge.png", 760, 240);
		mChangePropRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "change.png", 760, 280);
		mTurnMarkRgn = TextureRegionFactory.createFromAsset(mTexture, StartActivity.Instance, "turnmark.png", 760, 320);
	}
	
	public GameScene(int pLayerCount, Engine baseEngine) {
        super(pLayerCount, baseEngine);
    }
	
	@Override
    protected void onLoadResources(){
    	
    }
    
    @Override
    protected void onLoadScene() {
    	
    	this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0), true, 1, 0);
    	
    	this.setOnAreaTouchTraversalFrontToBack();
    	
    	this.getChild(0).attachChild((new Sprite(0, 0, this.mBackground)));
    	this.getChild(1).attachChild((new Sprite(0, 0, this.mChessboard)));
    	
    	mTurnMark[0] = new Sprite(160 - mTurnMarkRgn.getWidth() / 2, 423 - mTurnMarkRgn.getHeight() / 2, this.mTurnMarkRgn);
    	mTurnMark[1] = new Sprite(160 - mTurnMarkRgn.getWidth() / 2, 57 - mTurnMarkRgn.getHeight() / 2, this.mTurnMarkRgn);
    	this.getChild(0).attachChild(mTurnMark[0]);
    	this.getChild(0).attachChild(mTurnMark[1]);
    	mTurnMark[0].setVisible(false);
    	mTurnMark[1].setVisible(false);
    	mTurnMark[0].setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    	mTurnMark[1].setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    	
    	//Group1
    	createChessman(0.0f, CHESSBOARD_LATTICE_HEIGHT * 3.5f, ChessmanSprite.LARGE_SIZE, mDavidsdeerRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.SMALL_SIZE, mCrutchRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.SMALL_SIZE, mCrutchRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 2.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mHatRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 2.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mHatRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 3.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mSockRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 3.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mSockRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 4.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mHatRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 4.0f, CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mHatRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 3.0f, CHESSBOARD_LATTICE_HEIGHT * 2.5f, ChessmanSprite.MEDIUM_SIZE, mBellRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 3.0f, CHESSBOARD_LATTICE_HEIGHT * 2.5f, ChessmanSprite.MEDIUM_SIZE, mBellRgn, Brain.GROUP1);
    	createChessman(0, CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 2.0f, CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 2.0f, CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP1);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 4.0f, CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP1);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 4.0f, CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP1);
    	//Group2
    	createChessman(0.0f, -CHESSBOARD_LATTICE_HEIGHT * 3.5f, ChessmanSprite.LARGE_SIZE, mHorseRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.SMALL_SIZE, mCrutchRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.SMALL_SIZE, mCrutchRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 2.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mLeafRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 2.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mLeafRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 3.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mPresentRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 3.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mPresentRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 4.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mLeafRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 4.0f, -CHESSBOARD_LATTICE_HEIGHT * 4.5f, ChessmanSprite.MEDIUM_SIZE, mLeafRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 3.0f, -CHESSBOARD_LATTICE_HEIGHT * 2.5f, ChessmanSprite.MEDIUM_SIZE, mCookiemanRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 3.0f, -CHESSBOARD_LATTICE_HEIGHT * 2.5f, ChessmanSprite.MEDIUM_SIZE, mCookiemanRgn, Brain.GROUP2);
    	createChessman(0, -CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 2.0f, -CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 2.0f, -CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP2);
    	createChessman(CHESSBOARD_LATTICE_WIDTH * 4.0f, -CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP2);
    	createChessman(-CHESSBOARD_LATTICE_WIDTH * 4.0f, -CHESSBOARD_LATTICE_HEIGHT * 1.5f, ChessmanSprite.SMALL_SIZE, mSnowRgn, Brain.GROUP2);    	
    	this.setTouchAreaBindingEnabled(true);
    	
    	createProp(55, 455, mPowerUpPropRgn, Brain.GROUP1, PropSprite.POWERUP);
    	createProp(125, 455, mForbidPropRgn, Brain.GROUP1, PropSprite.FORBID);
    	createProp(195, 455, mEnlargePropRgn, Brain.GROUP1, PropSprite.ENLARGE);
    	createProp(265, 455, mChangePropRgn, Brain.GROUP1, PropSprite.CHANGE);
    	
    	createProp(265, 25, mPowerUpPropRgn, Brain.GROUP2, PropSprite.POWERUP);
    	createProp(195, 25, mForbidPropRgn, Brain.GROUP2, PropSprite.FORBID);
    	createProp(125, 25, mEnlargePropRgn, Brain.GROUP2, PropSprite.ENLARGE);
    	createProp(55, 25, mChangePropRgn, Brain.GROUP2, PropSprite.CHANGE);
    	
    	mBrain.init();
    	mBrain.setGameScene(this);
    	
    	MyContactListener listener = new MyContactListener(this);
    	//����烽�锟�nge
    	int hingeInterval = 75;
    	int hingeHeightInterval = 2;
    	listener._bodyA = createHinge(StartActivity.CAMERA_WIDTH / 2 - hingeInterval, StartActivity.CAMERA_HEIGHT / 2 - hingeHeightInterval, mHingeRgn);
    	listener._bodyB = createHinge(StartActivity.CAMERA_WIDTH / 2 + hingeInterval, StartActivity.CAMERA_HEIGHT / 2 - hingeHeightInterval, mHingeRgn);
    	this.mPhysicsWorld.setContactListener(listener);
    	
    	this.mEngine.setScene(this);
    	Log.d("MyLog","gamescene.onloadscene");
    	this.registerUpdateHandler(new TimerHandler(0.3f, true, new ITimerCallback() {
    		
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				//System.out.println("tick");
				mBrain.checkGameOver();
				mBrain.checkDrop();
				checkTurn();
				if (mBrain.isGameOver == true)
					unregisterUpdateHandler(pTimerHandler);			
			}
		}));
    	this.registerUpdateHandler(new TimerHandler(0.03f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				mBrain.drawPropCD();
				turnMarkFadeFunc();
			}
		}));
    	this.registerUpdateHandler(this.mPhysicsWorld);
       	
       	
    }
    protected void checkTurn() {
    	boolean tmp = isValid;
    	isValid = this.mBrain.checkValid();
    	if(tmp == false && isValid == true)
    	{
    		Log.d("confirm change turn","confirm change turn3");
    		confirmChangeTurn();
    	}
    	
    }
    
    public void updateCollisionChessmanData()
    {
    	hasSentUpdateData = true;
    	rivalHaschangeTurn = true;
    }
    
    protected void confirmChangeTurn()
    {
    	if(!isFirstTime){
    		if(!hasSentUpdateData){
    			if(!isSendingUpdateData){
    				isSendingUpdateData = true;
    				this.updateCollisionChessmanData();
    			}
    			isValid = false;
    			return;
    		}
    		if(!rivalHaschangeTurn){
    			isValid = false;
    			return;
    		}
	    	Log.d("confirm change turn", "here");
    		hasSentUpdateData = false;
    		isSendingUpdateData = false;
    		rivalHaschangeTurn = false;
    		this.changePlayer();
    	}
    	else {
    		Log.d("confirm change turn", "first time");
			isFirstTime = false;
    		StartActivity.Instance.mSound.startSound.play();
		}
		StartActivity.Instance.mSound.turnSound.play();
    	Log.d("confirm change turn", "play sound");
		turnValid = true;
		if(mBrain.getCurrentPlayer() == Brain.PLAYER1) {
			mTurnMark[0].setVisible(true);
			mTurnMark[1].setVisible(false);
		}
		else {
			mTurnMark[0].setVisible(false);
			mTurnMark[1].setVisible(true);
		}
    }
    
    
    
    
    @Override
    protected void unloadScene() {}

    @Override
    protected void onLoadComplete() {}
    
    protected ChessmanSprite createChessman(float posX, float posY, float scale, TextureRegion image, boolean group) {
    	int SCREEN_WIDTH = StartActivity.CAMERA_WIDTH;
    	int SCREEN_HEIGHT = StartActivity.CAMERA_HEIGHT;
    	ChessmanSprite sprite;
    	if(group == Brain.GROUP1)
    		sprite = new ChessmanSprite(SCREEN_WIDTH / 2 + posX - this.mChessmanRedRgn.getWidth() / 2,
    			SCREEN_HEIGHT / 2 + posY - this.mChessmanRedRgn.getHeight() / 2, mChessmanRedRgn, mChessmanGreenRgn, image, mEngine);
    	else
    		sprite = new ChessmanSprite(SCREEN_WIDTH / 2 + posX - this.mChessmanRedRgn.getWidth() / 2,
        			SCREEN_HEIGHT / 2 + posY - this.mChessmanRedRgn.getHeight() / 2, mChessmanGreenRgn, mChessmanRedRgn, image, mEngine);
    	return this.createChessmanHelp(sprite, posX, posY, scale, image, group);
    }
    
    protected ChessmanSprite createChessmanHelp(ChessmanSprite sprite, float posX, float posY, float scale, TextureRegion image, boolean group) {
    	int SCREEN_WIDTH = StartActivity.CAMERA_WIDTH;
    	int SCREEN_HEIGHT = StartActivity.CAMERA_HEIGHT;
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
    
    protected void turnMarkFadeFunc() {
    	if( turnMarkFlag )
    	{
    		turnMarkOpacity -= 0.05f;
    	}
    	else
    	{
    		turnMarkOpacity += 0.05f;
    	}
    	if( turnMarkOpacity <= 0 )
    	{
    		turnMarkFlag = false;
    	}
    	if( turnMarkOpacity >= 1 )
    	{
    		turnMarkFlag = true;
    	}	
    	if( mTurnMark[0].isVisible()) {
            mTurnMark[0].setAlpha(turnMarkOpacity);
        }
        else if( mTurnMark[1].isVisible()) {
            mTurnMark[1].setAlpha(turnMarkOpacity);
        }
    }
    
    public PhysicsWorld getmPhysicsWorld() {
		return mPhysicsWorld;
	}

    protected Body createHinge(float posX, float posY, TextureRegion rgn) {
    	Sprite sprite = new Sprite(posX - rgn.getWidth() / 2, posY - rgn.getHeight() / 2, rgn);
    	FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10.0f, 1.0f, 20.0f);
    	Body body;
		body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, sprite, BodyType.StaticBody, objectFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, true));
		final IEntity lastChild = this.getLastChild();
    	lastChild.attachChild(sprite);
    	return body;
    }
    protected void createPropHelp(PropSprite sprite, TextureRegion rgn, boolean group, int category) {
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
    protected void createProp(float posX, float posY, TextureRegion rgn, boolean group, int category) {
    	PropSprite sprite = new PropSprite(posX - rgn.getWidth() / 2, posY - rgn.getHeight() / 2, rgn, mEngine);
    	this.createPropHelp(sprite, rgn, group, category);
    }
    
    protected void changePlayer() {
    	Log.d("confirm change turn", "changgepa");
    	if(mBrain.isForbidPropOn == true)
    	{
    		this.shutDownForbid();
    		return;
    	}
    	this.mBrain.changePlayer();
    }
    
    public void spendScore(int score) {
    	this.mBrain.spendScore(score);
    }

    public void turnOnPowerUp() {
    	mBrain.turnOnPowerUp();
    }
    
    public void shutDownPowerUp() {
    	mBrain.shutDownPowerUp();
    }
    
    public void turnOnForbid()
    {
    	mBrain.turnOnForbid();
    }
    
    public void shutDownForbid()
    {
    	mBrain.shutDownForbid();
    }
    
    public void trunOnEnlarge()
    {
    	mBrain.turnOnEnlarge();
    }
    
    public void shutDownEnlarge()
    {
    	mBrain.shutDownEnlarge();
    }
    
    public void turnOnExchange()
    {
    	mBrain.turnOnExchange();
    }
    
    public void shutDownExchange()
    {
    	mBrain.shutDownExchange();
    }
    
    public void stopDestroyChessman() {
    	this.registerUpdateHandler(new TimerHandler(0.3f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				System.out.println("destroy tick");
				if(mBrain.stopDestroyedChessman())
					unregisterUpdateHandler(pTimerHandler);
			}
		}));
    }
    
    protected Brain mBrain = new Brain(16, 16);
    
    public Brain getmBrain() {
		return mBrain;
	}
    
    public void popupMenu()
    {
    	this.getChild(1).attachChild(StartActivity.Instance.getmMainScene().mPopupMenuSprite);
    	this.registerTouchArea(StartActivity.Instance.getmMainScene().mPopupMenuSprite);
    	StartActivity.Instance.isPopupMenu = true;
    }
    
    public void shutDownPopupMenu()
    {
		this.getChild(1).detachChild(StartActivity.Instance.getmMainScene().mPopupMenuSprite);
    	this.unregisterTouchArea(StartActivity.Instance.getmMainScene().mPopupMenuSprite);
    	StartActivity.Instance.isPopupMenu = false;
    }
    
    public void showWinSprite(boolean group, boolean even) {
		StartActivity.Instance.mSound.winSound.play();
    	
		this.registerUpdateHandler(new TimerHandler(5f, true,
				new ITimerCallback() {
					@Override
					public void onTimePassed(final TimerHandler pTimerHandler) {
						StartActivity.Instance.reloadMainScene();
					}
				}));
		
		if (even == false) {
			if (group == Brain.GROUP1) {
				this.mWinSprite.setCurrentTileIndex(0, 0);
				this.mStarSprite.setCurrentTileIndex(0, 0);
			} else {
				this.mWinSprite.setCurrentTileIndex(1, 0);
				this.mStarSprite.setCurrentTileIndex(1, 0);
			}
			this.mWinSprite.setPosition(60, 140);
			this.mStarSprite.setPosition(60, 140);
			this.getChild(1).attachChild(this.mStarSprite);
			this.mStarSprite.setScale(0);
			this.getChild(1).attachChild(this.mWinSprite);
			this.mWinSprite.setScale(0);
			
			this.registerUpdateHandler(new TimerHandler(0.03f, true,
					new ITimerCallback() {
						float rotate = 10;
						float scale = 0.1f;
						@Override
						public void onTimePassed(final TimerHandler pTimerHandler) {
							if(rotate > 0 )
							{
								mStarSprite.setRotation(mStarSprite.getRotation() + rotate);
								if(scale > 1)
									scale = 1;
								mWinSprite.setScale(scale);
								mStarSprite.setScale(scale);
								scale *= 1.2;
								rotate -= 0.15;
							}
							else
							{
								unregisterUpdateHandler(pTimerHandler);
							}
						}
					}));
			
		}
		else
		{
			this.mWinSprite.setCurrentTileIndex(2, 1);
			this.mWinSprite.setPosition(60, 140);
			this.getChild(1).attachChild(this.mWinSprite);
			this.registerUpdateHandler(new TimerHandler(0.03f, true,
					new ITimerCallback() {
						float rotate = 10;
						float scale = 0.1f;
						@Override
						public void onTimePassed(final TimerHandler pTimerHandler) {
							if(rotate > 0 )
							{
								mStarSprite.setRotation(mStarSprite.getRotation() + rotate);
								if(scale > 1)
									scale = 1;
								mWinSprite.setScale(scale);
								scale *= 1.2;
								rotate -= 0.15;
							}
							else
							{
							unregisterUpdateHandler(pTimerHandler);
							}
						}
					}));
		}
	}
    
    protected void sendMessage(BTMessage msg) {
	}

}
