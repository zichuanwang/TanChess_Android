package com.fatcatlab.tanchess;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class HelpDocSprite extends Sprite{

	public HelpDocSprite(float pX, float pY, float pWidth, float pHeight,
			TextureRegion pTextureRegion) {
		super(pX, pY, pWidth, pHeight, pTextureRegion);
		// TODO Auto-generated constructor stub
	}
	
	public final int ROLLBACK_COUNT = 50;
	
	public static final int SLIDEUP = 0;
	public static final int SLIDEDOWN = 1;
	public static final int ROLLBACKUP = 2;
	public static final int ROLLBACKDOWN = 3;
	
	public int display_status;

	public float beginPointY = 0;
	public float currentPointY = 0;
	public float lastPointY = 0;
	
	public int time = 0;
	public float speed = 0f;
	public boolean update = false;
	public boolean count = false;
	
	final float BUTTOM = -380;
	final float TOP = 0;
	
	public void update()
	{
		/*
		if(count == true)
		{
			time++;
			if(time > MAXTIME)
				time = MAXTIME;
		}
		if(update == true)
		{
			if(slide_direction == SLIDEUP)
				currentPointY -= time / 5;
			if(slide_direction == SLIDEDOWN)
				currentPointY += time / 5;
			if(currentPointY < BUTTOM)
			{
				currentPointY = BUTTOM;
				update = false;
				time = 0;
				return;
			}
			if(currentPointY > TOP)
			{
				currentPointY = TOP;
				update = false;
				time = 0;
				return;
			}
			this.setPosition(0,currentPointY);
			time--; 
			if(time < 0)
			{
				time = 0;
				update = false;
			}
		}
		*/
		if(count == true)
			time += 1/0.02;
		if(update == true)
		{
			switch(this.display_status)
			{
			case HelpDocSprite.SLIDEUP:
				if(currentPointY > BUTTOM)
				{
					currentPointY -= speed/10; 
					this.setPosition(0,currentPointY);
				}
				else
				{
					update = false;
					this.setPosition(0,BUTTOM);
				}
				break;
			case HelpDocSprite.SLIDEDOWN:
				if(currentPointY < TOP)
				{
					currentPointY += speed/10;
					this.setPosition(0,currentPointY);
				}
				else
				{
					update = false;
					this.setPosition(0, TOP);
				}
				break;
			case HelpDocSprite.ROLLBACKUP:
//				this.setPosition(0, TOP);
//				this.currentPointY = TOP;
				if(this.currentPointY < TOP)
				{
					currentPointY += speed/10;
					this.setPosition(0, currentPointY);
				}
				else
				{
					update = false;
					currentPointY = TOP;
					this.setPosition(0,TOP);
				}
				break;
			case HelpDocSprite.ROLLBACKDOWN:
				if(this.currentPointY > BUTTOM)
				{
					currentPointY -= speed/10;
					this.setPosition(0, currentPointY);
				}
				else
				{
					update = false;
					currentPointY = BUTTOM;
					this.setPosition(0,BUTTOM);
				}
//				this.setPosition(0, BUTTOM);
//				this.currentPointY = BUTTOM;
				break;
			}
			
			speed -= 8;
			if(speed <= 0 )
				update = false;
		}
		else
			this.setPosition(0, this.currentPointY);
	}
	
	@Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		
		switch(pSceneTouchEvent.getAction())
		{
		case TouchEvent.ACTION_DOWN:
			/*
			time = 0;
			count = true;
			beginPointY = pSceneTouchEvent.getY();
			*/
			time = 0;
			count = true;
			beginPointY = currentPointY;
			lastPointY = pSceneTouchEvent.getY();
			break;
		case TouchEvent.ACTION_MOVE:
			/*
			if(currentPointY >= BUTTOM || currentPointY <= TOP)
				this.setPosition(0, currentPointY+pSceneTouchEvent.getY()-beginPointY);
				*/
			currentPointY = currentPointY + pSceneTouchEvent.getY() - lastPointY;
			lastPointY = pSceneTouchEvent.getY();
			//Log.d("current", new Float(currentPointY).toString());
			break;
		case TouchEvent.ACTION_UP:
			/*
			currentPointY = currentPointY+pSceneTouchEvent.getY()-beginPointY;
			AlphaModifier am = new AlphaModifier(0.5f, 1, 0);
			if(currentPointY < BUTTOM)
			{
				this.registerEntityModifier(am);
				currentPointY = BUTTOM;
			}
			if(currentPointY > TOP)
			{
				this.registerEntityModifier(am);
				currentPointY = TOP;
			}
			count = false;
			update = true;
			time = MAXTIME - time;
			if(beginPointY > pSceneTouchEvent.getY())
				slide_direction = SLIDEUP;
			else
				slide_direction = SLIDEDOWN;
			this.setPosition(0, currentPointY);
			this.unregisterEntityModifier(am);
			*/
			update = true;
			count = false;
			currentPointY = currentPointY + pSceneTouchEvent.getY() - lastPointY;
			if(currentPointY < BUTTOM)
			{
				this.display_status = ROLLBACKDOWN;
				speed = (beginPointY - currentPointY);
				//Log.d("speed back down", new Float(speed).toString());
				break;
			}
			if(currentPointY > TOP)
			{
				this.display_status = ROLLBACKUP;
				speed = (beginPointY - currentPointY);
				//Log.d("speed back up", new Float(speed).toString());
				break;
			}
			if(beginPointY > currentPointY )
			{
				this.display_status = SLIDEUP;
				speed = (beginPointY - currentPointY) / time * 1000;
				//Log.d("speed up", new Float(speed).toString());
			}
			else
			{
				this.display_status = SLIDEDOWN;
				speed = (currentPointY - beginPointY) / time * 1000;
				//Log.d("speed down", new Float(speed).toString());
			}
			break;
		}
        return true;
	}
	
}