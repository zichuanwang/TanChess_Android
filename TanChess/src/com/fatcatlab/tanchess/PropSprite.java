package com.fatcatlab.tanchess;


import javax.microedition.khronos.opengles.GL10;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import android.graphics.PointF;

import com.badlogic.gdx.math.Vector2;

public class PropSprite extends Sprite {
	
	public static final int POWERUP = 1;
	public static final int FORBID	= 2;
	public static final int ENLARGE	= 3;
	public static final int CHANGE	= 4;
	public static final int POWERUP_NEED_SCORE = 10;
	public static final int FORBID_NEED_SCORE = 18;
	public static final int ENLARGE_NEED_SCORE = 26;
	public static final int CHANGE_NEED_SCORE = 40;
	protected Engine mEngine;
	
	public boolean mSelected = false;
	public boolean group;
	public int category;
	public boolean isForbad = true;
	public boolean isValid;
	public int score;
	protected CDRect mCDBackRect = null;
	protected CDRect mCDFrontRect = null;
	protected CDRect mCDFrontExtraRect = null;
	
	public int propID = -1;
	protected double mCurrentPer = 1;
	protected PointF currentPos = new PointF(20,20);
    
	public PropSprite(float pX, float pY, TextureRegion pTextureRegion, Engine pEngine) {
		super(pX, pY, pTextureRegion);
		this.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mEngine = pEngine;
		
		float points[] = {0, 0, 0, 40, 40, 40, 40, 0};
		mCDBackRect = new CDRect(0, 0, 4, points);
		mCDBackRect.setAlpha(0.3f);
		mCDBackRect.setColor(0, 0, 0);
		this.attachChild(mCDBackRect);
        float[] vertices = {0, 0, 0, 40, 20, 40, 20, 0};
        mCDFrontExtraRect = new CDRect(0, 0, 4, vertices);
        mCDFrontExtraRect.setAlpha(0.3f);
        mCDFrontExtraRect.setColor(0, 0, 0);
		this.attachChild(mCDFrontExtraRect);
	}
	
	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		switch(pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			if(!isValid)
				break;
			if(isForbad)
				break;
			if(category == ENLARGE && gameScene.getmBrain().checkLastBiggestChess())
				break;
			mSelected = true;
			break;
		case TouchEvent.ACTION_MOVE:
			if(this.mSelected) {
			}
			break;
		case TouchEvent.ACTION_UP:
			if(this.mSelected) {
				this.gameScene.spendScore(this.score);
				this.func(false);
				this.mSelected = false;
				this.workToDoOnKeyUp(category, this.propID);
			}
			break;
		}
		return true;
	}
		
	
	public Vector2 getPosition() {
		//System.out.println(this.getWidth());
		return new Vector2(this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2);
	}
	
	@Override
	public void setScale(final float pScale) {
		super.setScale(pScale);
		mScale = pScale;
	}
	
	public float getScale() {
		return mScale;
	}
	
	public boolean getGroup() {
		return this.mGroup;
	}
	
	public void setGroup(boolean group) {
		this.mGroup = group;
	}
	
	public void setGameScene(GameScene scene) {
		gameScene = scene;
	}
	
	public void checkValid(int newScore) {
		if(newScore >= score)
			isValid = true;
		else
			isValid = false;
	}
	
	protected void func(boolean aiMode) {
		this.isForbad = true;
		switch(category) {
		case POWERUP:
			StartActivity.Instance.mSound.powerUpSound.play();
			if(!aiMode)
				gameScene.turnOnPowerUp();
			break;
		case FORBID:
			StartActivity.Instance.mSound.teleportEffectSound.play();
			if(!aiMode)
				gameScene.turnOnForbid();
			break;
		case ENLARGE:
			StartActivity.Instance.mSound.changeSound.play();
			if(!aiMode)
				gameScene.trunOnEnlarge();
			break;
		case CHANGE:
			StartActivity.Instance.mSound.teleportSound.play();
			if(!aiMode)
				gameScene.turnOnExchange();
			break;
		default:
			break;
		}
		gameScene.showPropImage(this.category);
	}
	
	protected void drawRectStandardProcedure(int points, float[] vertices) {
		 if(mCDFrontRect != null)
 			this.detachChild(mCDFrontRect);
         mCDFrontRect = null;
         mCDFrontRect = new CDRect(0, 0, points, vertices);
         mCDFrontRect.setAlpha(0.3f);
         mCDFrontRect.setColor(0, 0, 0);
 		this.attachChild(mCDFrontRect);
	}
	
	public void drawCDRect(int have) {
		int need = this.score;
		double per = (double)have / (double)need;
		if( per > 0.99 )
			per = 1;
		else if(per < 0.01)
			per = 0;
		
		if(per - mCurrentPer < 0.01 && per - mCurrentPer > -0.01)
		{
			return;
		}
		else if( mCurrentPer < per )
		{
	        mCurrentPer += 0.02;
		}
		else if( mCurrentPer > per )
		{
	        mCurrentPer -= 0.02;
		}
		if( mCurrentPer > 1 )
		{
			mCurrentPer = 1;
		}
		else if(mCurrentPer < 0)
		{
			mCurrentPer = 0;
		}
		per = mCurrentPer;
				
		
		if(per == 1) {
			this.mCDBackRect.setVisible(false);
		}
		else {
			this.mCDBackRect.setVisible(true);
		}
		int shadow_width = 20;
		double radius = shadow_width * 1.414;
		
		currentPos.set( 20 + (float)(radius * Math.sin(2 * Math.PI * per)), 20 - (float)(radius * Math.cos(2 * Math.PI * per)));
	    if( per < 0.125f )
	    {
	    	int points = 5;
	        float[] vertices = {20, 20, 20, 40, 40, 40, 40, 0, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }
	    else if( per < 0.375f ) {
	    	int points = 4;
	        float[] vertices = {20, 20, 20, 40, 40, 40, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }
	    else if( per < 0.5f ) {
	    	int points = 3;
	        float[] vertices = {20, 20, 20, 40, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }

	    else if( per < 0.625 ) {
	    	int points = 5;
	        float[] vertices = {20, 20, 20, 0, 0, 0, 0, 40, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }

	    else if( per < 0.875f ) {
	    	int points = 4;
	        float[] vertices = {20, 20, 20, 0, 0, 0, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }

	    else {
	    	int points = 3;
	        float[] vertices = {20, 20, 20, 0, currentPos.x, currentPos.y};
	        drawRectStandardProcedure(points, vertices);
	    }
	    if( per < 0.5f ) {
	    	mCDFrontExtraRect.setVisible(true);
		}	
	    else {
	    	mCDFrontExtraRect.setVisible(false);
	    }
	}
	
	protected float mScale = 1.0f;
	protected boolean mGroup;
	protected GameScene gameScene;
	
	
	protected void workToDoOnKeyUp(int category, int id)
	{
		
	}
	
	
	
}