package com.fatcatlab.tanchess;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

//import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;

public class ChessmanSprite extends Sprite {
	
	public int chessmanID = -1;

	public static final float SMALL_SIZE = 0.35f;
	public static final float MEDIUM_SIZE = 0.47f;
	public static final float LARGE_SIZE = 0.67f;
	
	public static final int HIGH_SAFETY_ZONE_WIDTH = 100, HIGH_SAFETY_ZONE_HEIGHT = 80;
	public static final int NORMAL_SAFETY_ZONE_WIDTH = 180, NORMAL_SAFETY_ZONE_HEIGHT = 270;
	private static final int HIGH_SAFETY_COEFFICIENT = 50;
	private static final int NORMAL_SAFETY_COEFFICIENT = 30;
	private static final int LOW_SAFETY_COEFFICIENT = 10;
	
	private static final int LARGE_CHESSMAN_VALUE = 8;
	private static final int MEDIUM_CHESSMAN_VALUE = 4;
	private static final int SMALL_CHESSMAN_VALUE = 2;

	public boolean isDead = false;
	public boolean isForbad = true;
	public boolean isSelected = false;
	public boolean isPowerUp = false;
	public boolean isChange = false;
	public boolean isEnlarge = false;
	public boolean isPropShowing = false;
	public boolean isBetrayed = false;
	public int value;
	public Body body;
	protected PhysicsConnector mPhysicsConnector;

	protected float mSpeed = 0;
	protected float mScale = 1.0f;
	protected Body mBody = null;
	protected boolean mGroup;

	protected Texture mTexture;
	// private TextureRegion mBabyTextureRgn;
	protected TiledTextureRegion mGunsightRgn;
	protected Sprite mImage;
	protected Sprite mRivalcolor;
	public GunsightSprite mGunsight;

	protected Engine mEngine;
	public boolean mSelected = false;
	private GameScene gameScene;

	public PhysicsConnector getmPhysicsConnector() {
		return mPhysicsConnector;
	}

	public void setmPhysicsConnector(PhysicsConnector mPhysicsConnector) {
		this.mPhysicsConnector = mPhysicsConnector;
	}

	public ChessmanSprite(float pX, float pY, TextureRegion pTextureRegion, TextureRegion _pTextureRegion,
			TextureRegion image, Engine pEngine) {
		super(pX, pY, pTextureRegion);
		this.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		mRivalcolor =  new Sprite(pTextureRegion.getWidth() / 2 - image.getWidth()
				/ 2, pTextureRegion.getHeight() / 2 - image.getHeight() / 2,_pTextureRegion);
		mRivalcolor.setVisible(false);
		mRivalcolor.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.attachChild(mRivalcolor);

		mEngine = pEngine;
		mImage = new Sprite(pTextureRegion.getWidth() / 2 - image.getWidth()
				/ 2, pTextureRegion.getHeight() / 2 - image.getHeight() / 2,
				image);
		mImage.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.attachChild(mImage);
		mTexture = new Texture(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mGunsightRgn = TextureRegionFactory.createTiledFromAsset(mTexture,
				StartActivity.Instance, "gunsight.png", 0, 0, 2, 1);
		mEngine.getTextureManager().loadTextures(mTexture);

		mGunsight = new GunsightSprite(pTextureRegion.getWidth() / 2
				- mGunsightRgn.getWidth() / 4, pTextureRegion.getHeight() / 2
				- mGunsightRgn.getHeight() / 2, mGunsightRgn);
		// this.attachChild(mGunsight);
		mGunsight.setVisible(false);
	}

	public boolean onTouch(final TouchEvent pSceneTouchEvent) {
		switch (pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			if (isDead)
				return false;
			if (!gameScene.turnValid)
				return false;
			if (isChange) {
				this.workToDoOnChange(this.chessmanID);
				this.exchange();
				gameScene.shutDownExchange();
				return false;
			}
			if (isEnlarge) {
				// add codes to change the property of the sprite
				this.workToDoOnEnlarge(this.chessmanID);
				this.changeSize();
				return false;
			}
			if (isPropShowing)
				return false;
			
			this.setAlpha(0.6f);
			mImage.setBlendFunction(GL10.GL_SRC_ALPHA,
					GL10.GL_ONE_MINUS_SRC_ALPHA);
			this.mImage.setAlpha(0.6f);
			this.mSelected = true;
			gameScene.turnValid = false;
			gameScene.isValid = false;
			mGunsight.setScale(0);
			mGunsight.setPosition(this.getPosition().x - mGunsight.getWidth()
					/ 2, this.getPosition().y - mGunsight.getHeight() / 2);
			StartActivity.Instance.mSound.selectSound.play();
			
			this.workToDoOnKeyDown();
			
			break;
		case TouchEvent.ACTION_MOVE:
			if (this.mSelected) {
				// this.setPosition(pSceneTouchEvent.getX() - this.getWidth() /
				// 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
				Vector2 touchPoint = new Vector2(pSceneTouchEvent.getX(),
						pSceneTouchEvent.getY());
				float distance = this.distanceBetweenTwoPoints(
						this.getPosition(), touchPoint);

				// rotate
				double angle;
				if (touchPoint.x >= this.getPosition().x) {
					angle = 180
							+ Math.acos((this.getPosition().y - touchPoint.y)
									/ distance) / 2 / Math.PI * 360;
				} else {
					angle = 180
							- Math.acos((this.getPosition().y - touchPoint.y)
									/ distance) / 2 / Math.PI * 360;
				}

				mGunsight.setRotation((float) angle);

				// zoom
				if (distance <= 33.0) {
					mGunsight.setScale(distance / 33);
					mSpeed = distance;
				} else {
					mGunsight.setScale(1);
					mSpeed = 33;
				}

				// show
				if (isPowerUp)
					mGunsight.showPowerUpGunSight();
				else
					mGunsight.showNormalGunsight();
			}
			break;
		case TouchEvent.ACTION_UP:
			if (this.mSelected) {
				this.mSelected = false;
				this.setAlpha(1.0f);
				mImage.setBlendFunction(GL10.GL_ONE,
						GL10.GL_ONE_MINUS_SRC_ALPHA);
				this.mImage.setAlpha(1.0f);
				mGunsight.setVisible(false);

				float angle = mGunsight.getRotation();

				if (this.getScale() == SMALL_SIZE) {
					mSpeed *= 0.63f / 33 * 45;
				} else if (this.getScale() == LARGE_SIZE) {
					mSpeed *= 1.6f / 33 * 45;
				} else if (this.getScale() == MEDIUM_SIZE) {
					mSpeed *= 1.3f / 33 * 45;
				}
				if (isPowerUp) {
					mSpeed *= 1.4f;
					gameScene.shutDownPowerUp();
				}

				
				Vector2 impulse = new Vector2(mSpeed
						* (float) Math.sin(angle / 180 * Math.PI), mSpeed
						* -(float) Math.cos(angle / 180 * Math.PI));
				if (mSpeed != 0) {
					StartActivity.Instance.mSound.fireSound.play();
				}
				mBody.applyLinearImpulse(impulse, mBody.getPosition());
				mSpeed = 0;
				/*
				if (gameScene.getmBrain().isForbidPropOn == false) {
					gameScene.changePlayer();
				} else {
					gameScene.shutDownForbid();
					StartActivity.Instance.mSound.teleportEffectSound.play();
				}
				*/
				this.workToDoOnKeyUp(impulse.x,impulse.y);
			}
			break;
		}
		return true;
	}
	
	/*@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		
		return true;
	}*/

	public float distanceBetweenTwoPoints(Vector2 fromPoint, Vector2 toPoint) {
		float x = toPoint.x - fromPoint.x;
		float y = toPoint.y - fromPoint.y;
		return (float) Math.sqrt(x * x + y * y);
	}

	public Vector2 getPosition() {
		return new Vector2(this.getX() + this.getWidth() / 2, this.getY()
				+ this.getHeight() / 2);
	}

	@Override
	public void setScale(final float pScale) {
		super.setScale(pScale);
		mScale = pScale;
	}

	public float getScale() {
		return mScale;
	}

	public void setBody(Body body) {
		mBody = body;
	}
	
	private boolean checkInZone(int zoneWidth, int zoneHeight) {
		return ChessmanSprite.checkInZone(zoneWidth, zoneHeight, this.getPosition());
	}
	
	public static boolean checkInZone(int zoneWidth, int zoneHeight, Vector2 pos) {
		int halfScreenWidth = StartActivity.CAMERA_WIDTH / 2;
		int halfScreenHeight = StartActivity.CAMERA_HEIGHT / 2;
		if (pos.x < halfScreenWidth - zoneWidth / 2
				|| pos.x > halfScreenWidth + zoneWidth / 2) {
			return false;
		}
		if (pos.y < halfScreenHeight - zoneHeight / 2
				|| pos.y > halfScreenHeight + zoneHeight / 2) {
			return false;
		}
		return true;
	}
	
	public static int calculateValue(int value, Vector2 pos) {
		int result = ChessmanSprite.getRealValue(value);
		if(ChessmanSprite.checkInZone(HIGH_SAFETY_ZONE_WIDTH, HIGH_SAFETY_ZONE_HEIGHT, pos)) {
			result *= ChessmanSprite.HIGH_SAFETY_COEFFICIENT;
		}
		else if(ChessmanSprite.checkInZone(NORMAL_SAFETY_ZONE_WIDTH, NORMAL_SAFETY_ZONE_HEIGHT, pos)) {
			result *= ChessmanSprite.NORMAL_SAFETY_COEFFICIENT;
		}
		else {
			result *= ChessmanSprite.LOW_SAFETY_COEFFICIENT;
		}
		return result;
	}
	
	/*
	 * If large = 12 if normal = 4 if small = 0
	 */
	private static int getRealValue(int value) {
		int realValue = value;
		if(value == ChessmanSprite.SMALL_CHESSMAN_VALUE) {
			realValue = 1;
		}
		else if(value == ChessmanSprite.LARGE_CHESSMAN_VALUE) {
			realValue *= 3;
			realValue /= 2;
		}
		return realValue;
	}
	
	public int calculateValue() {
		return ChessmanSprite.calculateValue(this.value, this.getPosition());
	}
	
	public boolean checkAlive() {
		return this.checkInZone(GameScene.CHESSBOARD_WIDTH, GameScene.CHESSBOARD_HEIGHT);
	} 
	
	
	public static boolean checkAlive(float x, float y)
	{
		int halfScreenWidth = StartActivity.CAMERA_WIDTH / 2;
		int halfScreenHeight = StartActivity.CAMERA_HEIGHT / 2;
		if (x < halfScreenWidth - GameScene.CHESSBOARD_WIDTH / 2 || x > halfScreenWidth + GameScene.CHESSBOARD_WIDTH / 2) {
			return false;
		}
		if (y < halfScreenHeight - GameScene.CHESSBOARD_HEIGHT / 2 || y > halfScreenHeight + GameScene.CHESSBOARD_HEIGHT / 2) {
			return false;
		}
		return true;
	}

	public void fadeOut() {
		mImage.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mImage.registerEntityModifier(new AlphaModifier(0.3f, 1, 0));
		if(isBetrayed) {
			this.setAlpha(0);
			this.mRivalcolor.registerEntityModifier(new AlphaModifier(0.3f, 1, 0));
		}
		else {
			this.mRivalcolor.setVisible(false);
			this.registerEntityModifier(new AlphaModifier(0.3f, 1, 0));
		}
	}
	
	public void fadeIn() {
		mImage.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mImage.registerEntityModifier(new AlphaModifier(0.3f, 0, 1));
		if(isBetrayed) {
			this.setAlpha(1);
			this.mRivalcolor.registerEntityModifier(new AlphaModifier(0.3f, 0, 1));
		}
		else {
			this.mRivalcolor.setVisible(false);
			this.registerEntityModifier(new AlphaModifier(0.3f, 0, 1));
		}
	}

	public boolean getGroup() {
		return this.mGroup;
	}

	public void setGroup(boolean group) {
		this.mGroup = group;
	}

	public float getOpacity() {
		return mImage.getAlpha();
	}

	public void setGameScene(GameScene scene) {
		gameScene = scene;
	}
	
	public void changeSize() {
		if (this.getScale() == LARGE_SIZE)
			return;
		else if (this.getScale() == SMALL_SIZE)
			this.setScale(MEDIUM_SIZE);
		else if (this.getScale() == MEDIUM_SIZE)
			this.setScale(LARGE_SIZE);
		StartActivity.Instance.mSound.changeEffectSound.play();

		FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10.0f,
				1.0f, 2.0f);
		Shape shape = new Rectangle(
				this.getPosition().x - 26 * this.getScale(),
				this.getPosition().y - 26 * this.getScale(),
				52 * this.getScale(), 52 * this.getScale());
		shape.setRotation(this.getRotation());
		Body newBody = PhysicsFactory.createCircleBody(
				this.gameScene.getmPhysicsWorld(), shape, BodyType.DynamicBody,
				objectFixtureDef);
		MassData mass = newBody.getMassData();
		mass.mass = 3.0f;
		newBody.setMassData(mass);
		if (this.getScale() == ChessmanSprite.LARGE_SIZE) {
			newBody.setLinearDamping(4.0f);
			this.value = ChessmanSprite.LARGE_CHESSMAN_VALUE;
		} else if (this.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			newBody.setLinearDamping(2.5f);
			this.value = ChessmanSprite.MEDIUM_CHESSMAN_VALUE;
		}
		newBody.setAngularDamping(2.0f);
		this.gameScene.getmPhysicsWorld().destroyBody(this.body);
		this.gameScene.getmPhysicsWorld().unregisterPhysicsConnector(
				this.getmPhysicsConnector());
		PhysicsConnector newPc = new PhysicsConnector(this, newBody, true, true);
		this.setmPhysicsConnector(newPc);
		this.gameScene.getmPhysicsWorld().registerPhysicsConnector(newPc);
		this.body = newBody;
		this.setBody(this.body);
		gameScene.shutDownEnlarge();
	}

	public void exchange() {
		StartActivity.Instance.mSound.changeEffectSound.play();
		if (this.getGroup() == Brain.GROUP1) {
			this.setGroup(Brain.GROUP2);
		} else {
			this.setGroup(Brain.GROUP1);
		}
		this.gameScene.getmBrain().changePlayerLifeWhenExchange();
		this.isForbad = false;
		if(!isBetrayed)
			this.mRivalcolor.setVisible(true);
		else
			this.mRivalcolor.setVisible(false);
		isBetrayed = !isBetrayed;
}
	
	protected void workToDoOnKeyDown()
	{
		
	}
	
	protected void workToDoOnKeyUp(float x, float y)
	{
		
	}
	
	protected void workToDoOnChange(int id)
	{
		
	}
	
	protected void workToDoOnEnlarge(int id)
	{
		
	}

}
