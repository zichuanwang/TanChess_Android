package com.fatcatlab.tanchess;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class PopupMenuSprite extends Sprite {

	
	private final int FIRST_X = 120;
	private final int LAST_X = 200;
	
	private final int FIRST_Y_BEGIN = 60;
	private final int FIRST_Y_END = 140;
	
	private final int SECOND_Y_BEGIN = 200;
	private final int SECOND_Y_END = 280;
	
	private final int THIRD_Y_BEGIN = 340;
	private final int THIRD_Y_END = 420;
	
	public PopupMenuSprite(float pX, float pY, float pWidth, float pHeight,
			TextureRegion pTextureRegion) {
		super(pX, pY, pWidth, pHeight, pTextureRegion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			float pTouchAreaLocalX, float pTouchAreaLocalY) {
		// TODO Auto-generated method stub
		if(pTouchAreaLocalX > this.FIRST_X &&
				pTouchAreaLocalX < this.LAST_X)
		{
			int status = -1;
			if(pTouchAreaLocalY > this.FIRST_Y_BEGIN &&
					pTouchAreaLocalY < this.FIRST_Y_END)
				status = 0;
			if(pTouchAreaLocalY > this.SECOND_Y_BEGIN &&
					pTouchAreaLocalY < this.SECOND_Y_END )
				status = 1;
			if(pTouchAreaLocalY > this.THIRD_Y_BEGIN &&
					pTouchAreaLocalY < this.THIRD_Y_END )
				status = 2;
			switch(status)
			{
			case 0:
				StartActivity.Instance.mSound.clickSound.play();
				StartActivity.Instance.getmMainScene().mGameScene.shutDownPopupMenu();
				break;
			case 1:
				StartActivity.Instance.mSound.clickSound.play();
				StartActivity.Instance.reloadGameScene();
				StartActivity.Instance.getmMainScene().mGameScene.onLoadScene();
				StartActivity.Instance.getmMainScene().mGameScene.shutDownPopupMenu();
				break;
			case 2:
				StartActivity.Instance.mSound.clickSound.play();
				StartActivity.Instance.reloadMainScene();
				break;
			}
			
		}
		return true;
	}

}
