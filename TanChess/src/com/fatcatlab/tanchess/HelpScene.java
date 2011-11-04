package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class HelpScene extends AbstractGameScene{

	public Texture mTexture;
	public TextureRegion mHelpDocRgn;
	public HelpDocSprite helpDocSprite;
	
	public HelpScene(int pLayerCount, Engine baseEngine) {
		super(pLayerCount, baseEngine);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onLoadResources() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onLoadScene() {
		// TODO Auto-generated method stub
    	this.setOnAreaTouchTraversalFrontToBack();
    	final IEntity lastChild = this.getLastChild();
    	helpDocSprite = new HelpDocSprite(0,0,this.mHelpDocRgn.getWidth(),this.mHelpDocRgn.getHeight(),this.mHelpDocRgn);
    	this.registerUpdateHandler(new TimerHandler(0.02f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				helpDocSprite.update();
			}
		}));
    	lastChild.attachChild(new Sprite(0,0,StartActivity.Instance.getmMainScene().mGameScene.getmBackground()));
    	lastChild.attachChild(helpDocSprite);
    	this.registerTouchArea(helpDocSprite);
    	this.setTouchAreaBindingEnabled(true);
    	this.mEngine.setScene(this);
    	
    	StartActivity.SCENE_STATE = StartActivity.STATE_HELPDOC;
    	
	}

	@Override
	protected void unloadScene() {
		// TODO Auto-generated method stub
		
	}

}
