package com.fatcatlab.tanchess;

import javax.microedition.khronos.opengles.GL10;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.AnimatedSpriteMenuItem;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import android.content.Intent;
import android.util.Log;

public class MainScene extends AbstractGameScene implements
		IOnMenuItemClickListener {

	protected static final int MENU_PLAY = 0;
	protected static final int MENU_HELP = MENU_PLAY + 1;
	protected static final int MENU_CONNECT = MENU_HELP + 1;
	
	public static int BLUETOOTH_STATE = -1;
	public static final int BLUETOOTH_OFF = 0;
	public static final int BLUETOOTH_ON = 1;
	public static final int BLUETOOTH_CONNECTING = 2;

	// 锟斤拷时锟斤拷锟斤拷锟斤拷锟�ooper.prepare只锟斤拷锟斤拷一锟轿碉拷锟斤拷锟斤拷
	static public boolean firstTimeClickConnection = true;

	public GameScene mGameScene;
	public HelpScene mHelpScene;
	public ConnectScene mConnectScene;
	public BTGameScene mBtGameScene;
	
	
	public PopupMenuSprite mPopupMenuSprite;

	private Texture mTexture;
	private TextureRegion mBackground;
	private TextureRegion mTitle;
	private TiledTextureRegion mMenuItemPlay;
	private TiledTextureRegion mMenuItemHelp;
	private TiledTextureRegion mMenuItemConnect;

	protected MenuScene mMenuScene;

	public MainScene(int pLayerCount, Engine baseEngine) {

		super(pLayerCount, baseEngine);
		
		
	}

	public void workToDoOnLoad() {
		
		Texture menuTexture = new Texture(512, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegion mMenuRgn = TextureRegionFactory.createFromAsset(
				menuTexture, StartActivity.Instance, "gamecontrol.png", 0, 0);
		this.mEngine.getTextureManager().loadTextures(menuTexture);
		this.mPopupMenuSprite = new PopupMenuSprite(0, 0, mMenuRgn.getWidth(),
				mMenuRgn.getHeight(), mMenuRgn);

		MainScene.this.mTexture = new Texture(1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		MainScene.this.mBackground = TextureRegionFactory.createFromAsset(
				MainScene.this.mTexture, StartActivity.Instance,
				"background.png", 0, 0);
		MainScene.this.mTitle = TextureRegionFactory.createFromAsset(
				MainScene.this.mTexture, StartActivity.Instance,
				"gametitle.png", 320, 0);
		MainScene.this.mMenuItemPlay = TextureRegionFactory
				.createTiledFromAsset(MainScene.this.mTexture,
						StartActivity.Instance, "play.png", 640, 0, 2, 1);
		MainScene.this.mMenuItemHelp = TextureRegionFactory
				.createTiledFromAsset(MainScene.this.mTexture,
						StartActivity.Instance, "help.png", 640, 40, 2, 1);
		MainScene.this.mMenuItemConnect = TextureRegionFactory
				.createTiledFromAsset(MainScene.this.mTexture,
						StartActivity.Instance, "connect.png", 640, 80, 2, 1);
		MainScene.this.mEngine.getTextureManager().loadTextures(mTexture);

		mGameScene = new GameScene(3, mEngine);
		mGameScene.loadImageResource();
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mGameScene.mTexture);

		mGameScene.mWinShowTexture = new Texture(512, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mGameScene.mStarRgn = TextureRegionFactory.createTiledFromAsset(
				mGameScene.mWinShowTexture, StartActivity.Instance, "star.png",
				0, 0, 2, 1);
		mGameScene.mWinRgn = TextureRegionFactory.createTiledFromAsset(
				mGameScene.mWinShowTexture, StartActivity.Instance,
				"gameover.png", 0, 200, 2, 3);
		mGameScene.mStarSprite = new TiledSprite(0, 0, mGameScene.mStarRgn);
		mGameScene.mWinSprite = new TiledSprite(0, 0, mGameScene.mWinRgn);

		MainScene.this.mEngine.getTextureManager().loadTextures(
				mGameScene.mWinShowTexture);

		mHelpScene = new HelpScene(1, mEngine);
		mHelpScene.mTexture = new Texture(512, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mHelpScene.mHelpDocRgn = TextureRegionFactory.createFromAsset(
				mHelpScene.mTexture, StartActivity.Instance, "helpDoc.png", 0,
				0);
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mHelpScene.mTexture);

		mConnectScene = new ConnectScene(1, mEngine);
		mConnectScene.mTexture = new Texture(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mConnectScene.mComingSoonRgn = TextureRegionFactory.createFromAsset(
				mConnectScene.mTexture, StartActivity.Instance,
				"comingsoon.png", 0, 0);
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mConnectScene.mTexture);
		mBtGameScene = new BTGameScene(3,mEngine);
		mBtGameScene.loadImageResource();
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mBtGameScene.mTexture);

		mBtGameScene.mWinShowTexture = new Texture(512, 1024,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mBtGameScene.mStarRgn = TextureRegionFactory.createTiledFromAsset(
				mBtGameScene.mWinShowTexture, StartActivity.Instance, "star.png",
				0, 0, 2, 1);
		mBtGameScene.mWinRgn = TextureRegionFactory.createTiledFromAsset(
				mBtGameScene.mWinShowTexture, StartActivity.Instance,
				"gameover.png", 0, 200, 2, 3);
		mBtGameScene.mStarSprite = new TiledSprite(0, 0, mBtGameScene.mStarRgn);
		mBtGameScene.mWinSprite = new TiledSprite(0, 0, mBtGameScene.mWinRgn);

		MainScene.this.mEngine.getTextureManager().loadTextures(
				mBtGameScene.mWinShowTexture);
		
	}

	
	@Override
	protected void onLoadResources() {
		IAsyncCallback callback = new IAsyncCallback() {
			@Override
			public void workToDo() {
				MainScene.this.workToDoOnLoad();
			}

			@Override
			public void onComplete() {
				MainScene.this.onLoadScene();
			}
		};
		new AsyncTaskLoader().execute(callback);
	}

	@Override
	protected void onLoadScene() {
		final IEntity lastChild = this.getLastChild();
		lastChild.attachChild((new Sprite(0, 0, this.mBackground)));
		lastChild.attachChild((new Sprite(0, 0, this.mTitle)));
		this.createMenuScene();
		this.setChildScene(this.mMenuScene, false, true, true);
		// set this as the main scene after loading
		this.mEngine.setScene(this);
		if(StartActivity.SCENE_STATE == StartActivity.STATE_BTGAMESCENE)
		{
			mBtGameScene.onLoadScene();
		}
		Log.d("my tag", "load main");
	}

	@Override
	protected void unloadScene() {
		
		
		
	}

	@Override
	protected void onLoadComplete() {
	}

	protected void createMenuScene() {
		this.mMenuScene = new MenuScene(MainScene.this.mEngine.getCamera());

		AnimatedSpriteMenuItem playButton = new AnimatedSpriteMenuItem(
				MENU_PLAY, mMenuItemPlay) {
			@Override
			public void onSelected() {
				setCurrentTileIndex(1);
				super.onSelected();
			}

			@Override
			public void onUnselected() {
				setCurrentTileIndex(0);
				super.onUnselected();
			}
		};
		AnimatedSpriteMenuItem helpButton = new AnimatedSpriteMenuItem(
				MENU_HELP, mMenuItemHelp) {
			@Override
			public void onSelected() {
				setCurrentTileIndex(1);
				super.onSelected();
			}

			@Override
			public void onUnselected() {
				setCurrentTileIndex(0);
				super.onUnselected();
			}
		};
		AnimatedSpriteMenuItem connnectButton = new AnimatedSpriteMenuItem(
				MENU_CONNECT, mMenuItemConnect) {
			@Override
			public void onSelected() {
				setCurrentTileIndex(1);
				super.onSelected();
			}

			@Override
			public void onUnselected() {
				setCurrentTileIndex(0);
				super.onUnselected();
			}
		};

		this.mMenuScene.addMenuItem(playButton);
		this.mMenuScene.addMenuItem(helpButton);
		this.mMenuScene.addMenuItem(connnectButton);
		playButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		helpButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		connnectButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.setMenuAnimator(new PositionalMenuAnimator(160 - 85,
				260, 20));
		this.mMenuScene.buildAnimations();
		this.mMenuScene.setBackgroundEnabled(false);
		this.mMenuScene.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene,
			final IMenuItem pMenuItem, final float pMenuItemLocalX,
			final float pMenuItemLocalY) {
		StartActivity.Instance.mSound.clickSound.play();
		switch (pMenuItem.getID()) {
		case MENU_PLAY:
	       	StartActivity.SCENE_STATE = StartActivity.STATE_GAMESCENT;
			mGameScene.onLoadScene();
			return true;
		case MENU_HELP:
			/* End Activity. */
			//mHelpScene.onLoadScene();
			//ai
			
	       	StartActivity.SCENE_STATE = StartActivity.STATE_AIGAME;
			mGameScene.onLoadScene();
			return true;
		case MENU_CONNECT:
			Intent itent=new Intent();
	        itent.setClass(StartActivity.Instance, BluetoothControlActivity.class);
	        StartActivity.Instance.startActivity(itent);
	        BLUETOOTH_STATE = BLUETOOTH_CONNECTING;
			return true;
		default:
			return false;
		}
	}

	
}
