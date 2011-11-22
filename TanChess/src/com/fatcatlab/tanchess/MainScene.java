package com.fatcatlab.tanchess;

import javax.microedition.khronos.opengles.GL10;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.AnimatedSpriteMenuItem;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.sprite.Sprite;
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
	protected static final int MENU_ROBOT = 1;
	protected static final int MENU_HELP = 2;
	protected static final int MENU_CONNECT = 3;
	
	public static int BLUETOOTH_STATE = -1;
	public static final int BLUETOOTH_OFF = 0;
	public static final int BLUETOOTH_ON = 1;
	public static final int BLUETOOTH_CONNECTING = 2;

	// 锟斤拷时锟斤拷锟斤拷锟斤拷锟�ooper.prepare只锟斤拷锟斤拷一锟轿碉拷锟斤拷锟斤拷
	static public boolean firstTimeClickConnection = true;

	public GameScene mGameScene;
	public HelpScene mHelpScene;
	public BTGameScene mBtGameScene;
	
	
	public PopupMenuSprite mPopupMenuSprite;

	private Texture mTexture;
	private TextureRegion mBackground;
	private TextureRegion mTitle;
	private TiledTextureRegion mMenuItemPlay;
	private TiledTextureRegion mMenuItemRobot;
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
		MainScene.this.mMenuItemRobot = TextureRegionFactory
		.createTiledFromAsset(MainScene.this.mTexture,
				StartActivity.Instance, "robot.png", 640, 120, 2, 1);
		MainScene.this.mEngine.getTextureManager().loadTextures(mTexture);

		mGameScene = new GameScene(3, mEngine);
		mGameScene.loadImageResource();
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mGameScene.mTexture);
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mGameScene.mPropShowTexture);
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


		mBtGameScene = new BTGameScene(3,mEngine);
		mBtGameScene.loadImageResource();
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mBtGameScene.mTexture);
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mBtGameScene.mWinShowTexture);
		MainScene.this.mEngine.getTextureManager().loadTextures(
				mBtGameScene.mPropShowTexture);
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
		AnimatedSpriteMenuItem robotButton = new AnimatedSpriteMenuItem(
				MENU_ROBOT, mMenuItemRobot) {
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
		this.mMenuScene.addMenuItem(robotButton);
		this.mMenuScene.addMenuItem(helpButton);
		this.mMenuScene.addMenuItem(connnectButton);
		playButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		robotButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		helpButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		connnectButton.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.setMenuAnimator(new PositionalMenuAnimator(160 - 85,
				230, 15));
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
	       	StartActivity.SCENE_STATE = StartActivity.STATE_GAMESCENE;
			mGameScene.onLoadScene();
			return true;
		case MENU_ROBOT:
	       	StartActivity.SCENE_STATE = StartActivity.STATE_AIGAME;
			mGameScene.onLoadScene();
			return true;
		case MENU_HELP:
	       	StartActivity.SCENE_STATE = StartActivity.STATE_HELPDOC;
			mHelpScene.onLoadScene();
			return true;
		case MENU_CONNECT:
			Intent intent=new Intent();
	        intent.setClass(StartActivity.Instance, BluetoothControlActivity.class);
	        StartActivity.Instance.startActivity(intent);
	        BLUETOOTH_STATE = BLUETOOTH_CONNECTING;
			return true;
		default:
			return false;
		}
	}

	
}
