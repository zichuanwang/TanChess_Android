package com.fatcatlab.tanchess;

import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;


public class GunsightSprite extends TiledSprite {
	
	final static int NORMAL_GUNSIGHT_INDEX = 0;
	final static int POWERUP_GUNSIGHT_INDEX = 1;
	
	public GunsightSprite(final float pX, final float pY, final TiledTextureRegion pTiledTextureRegion) {
		super(pX, pY, pTiledTextureRegion.getTileWidth(), pTiledTextureRegion.getTileHeight(), pTiledTextureRegion);
	}
	
	public void showNormalGunsight() {
		this.setVisible(true);
		this.setCurrentTileIndex(NORMAL_GUNSIGHT_INDEX);
	}		
	
	public void showPowerUpGunSight() {
		this.setVisible(true);
		this.setCurrentTileIndex(POWERUP_GUNSIGHT_INDEX);
	}
}
