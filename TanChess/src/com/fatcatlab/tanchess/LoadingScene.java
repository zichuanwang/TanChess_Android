package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

public class LoadingScene extends AbstractGameScene {
	    
    private Texture mTexture;
    private TextureRegion mLoading;
    
    public LoadingScene(int pLayerCount, Engine baseEngine) {
        super(pLayerCount, baseEngine);
    }
    
    @Override
    protected void onLoadResources(){
    	System.out.println("here i am hahaha");
    	LoadingScene.this.mTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		LoadingScene.this.mLoading = TextureRegionFactory.createFromAsset(LoadingScene.this.mTexture, StartActivity.Instance, "loading.png", 0, 0);
		LoadingScene.this.mEngine.getTextureManager().loadTextures(mTexture);
    }
    
    @Override
    protected void onLoadScene() {
        final IEntity lastChild = this.getLastChild();
    	lastChild.attachChild((new Sprite(0, 0, this.mLoading)));
    	System.out.println("load scene");
    }

    @Override
    protected void unloadScene() {}

    @Override
    protected void onLoadComplete() {}
   
    // ===========================================================
    // Methods
    // ===========================================================

}
