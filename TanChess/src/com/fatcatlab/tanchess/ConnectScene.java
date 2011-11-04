package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.region.TextureRegion;


public class ConnectScene extends AbstractGameScene {

	public Texture mTexture;
	public TextureRegion mComingSoonRgn;
	public Sprite mComingSoonSprite;

	public ConnectScene(int pLayerCount, Engine baseEngine) {
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
		mComingSoonSprite = new HelpDocSprite(0, 0,
				this.mComingSoonRgn.getWidth(),
				this.mComingSoonRgn.getHeight(), this.mComingSoonRgn);
		lastChild.attachChild(new Sprite(0, 0, StartActivity.Instance
				.getmMainScene().mGameScene.getmBackground()));
		lastChild.attachChild(this.mComingSoonSprite);
		this.mEngine.setScene(this);
		StartActivity.SCENE_STATE = StartActivity.STATE_CONNECT;
		
	}

	@Override
	protected void unloadScene() {
		// TODO Auto-generated method stub

	}

}
