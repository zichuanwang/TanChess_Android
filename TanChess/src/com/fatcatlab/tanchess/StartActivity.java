package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import com.fatcatlab.tanchess.BTMessage.PacketCodes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class StartActivity extends BaseGameActivity {

	public static final int CAMERA_WIDTH = 320;
	public static final int CAMERA_HEIGHT = 480;

	public static final int STATE_MAINSCENE = 0;
	public static final int STATE_GAMESCENE = 1;
	public static final int STATE_HELPDOC = 2;
	public static final int STATE_CONNECT = 3;
	public static final int STATE_BTGAMESCENE = 4;
	public static final int STATE_AIGAME = 5;

	public static int SCENE_STATE = 0;

	public MySound mSound;

	public boolean isPopupMenu = false;

	public static StartActivity Instance = null;

	private Camera mCamera;

	private LoadingScene mLoadingScene;
	private MainScene mMainScene;

	public MainScene getmMainScene() {
		return mMainScene;
	}
	
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		// TODO Auto-generated method stub
		BluetoothService.Init(StartActivity.Instance, mHandler);
		super.onCreate(pSavedInstanceState);
	}




	@Override
	public Engine onLoadEngine() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera)
				.setNeedsSound(true);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		return new Engine(engineOptions);
	}

	@Override
	public void onLoadResources() {
		StartActivity.Instance = this;
	}

	@Override
	public Scene onLoadScene() {
		// start loading of game scene but do not return it

		mSound = new MySound();
		mSound.loadSoundResource(mEngine);

		mMainScene = new MainScene(1, mEngine);
		mMainScene.LoadResources(false);

		// create a simple loading scene
		StartActivity.Instance.mEngine.registerUpdateHandler(new FPSLogger());
		mLoadingScene = new LoadingScene(1, mEngine);
		mLoadingScene.LoadResources(true);
		return mLoadingScene;
	}

	public void reloadMainScene() {
		// this.mMainScene.mGameScene = null;
		// this.mMainScene.mHelpScene = null;
		StartActivity.Instance.mEngine.clearUpdateHandlers();
		StartActivity.Instance.mMainScene = new MainScene(1, mEngine);
		StartActivity.Instance.mMainScene.LoadResources(false);
	}

	public void reloadGameScene() {
		// this.mMainScene.mGameScene = null;
		// this.mMainScene.mHelpScene = null;
		StartActivity.Instance.mEngine.clearUpdateHandlers();
		StartActivity.Instance.mMainScene = new MainScene(1, mEngine);
		StartActivity.Instance.mMainScene.workToDoOnLoad();
		// mEngine.setScene(mMainScene.mGameScene);
	}
	
	@Override
	public void onLoadComplete() {
		//this.showDialog(0);
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if (pKeyCode == KeyEvent.KEYCODE_BACK) { // && pEvent.getAction() ==
													// KeyEvent.ACTION_UP) {
			switch (StartActivity.SCENE_STATE) {
			case STATE_MAINSCENE:
				System.exit(0);
				break;
			case STATE_HELPDOC:
				this.reloadMainScene();
				break;
			case STATE_GAMESCENE:
				this.reloadMainScene();
				break;
			case STATE_CONNECT:
				this.reloadMainScene();
				break;
			case STATE_BTGAMESCENE:
				BTMessage message = new BTMessage();
				message.packetCodes = PacketCodes.QUIT_GAME;
				this.mMainScene.mBtGameScene.sendMessage(message);
				this.reloadMainScene();
				BluetoothService.getService().stop();
				break;
			case STATE_AIGAME:
				this.reloadMainScene();
				break;
			}
			StartActivity.SCENE_STATE = STATE_MAINSCENE;
			return true;
		}
		if (pKeyCode == KeyEvent.KEYCODE_MENU) {
			if(StartActivity.SCENE_STATE == STATE_GAMESCENE || 
					StartActivity.SCENE_STATE == STATE_AIGAME)
			if (isPopupMenu == false)
				StartActivity.Instance.mMainScene.mGameScene.popupMenu();
			else {
				StartActivity.Instance.mMainScene.mGameScene.shutDownPopupMenu();
			}
		}

		return super.onKeyDown(pKeyCode, pEvent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	
	 // The Handler that gets information back from the BluetoothChatService
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BTGameScene.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    break;
                case BluetoothService.STATE_CONNECTING:
                    break;
                case BluetoothService.STATE_LISTEN:
                	break;
                case BluetoothService.STATE_NONE:
                    break;
                }
                break;
            case BTGameScene.MESSAGE_WRITE:
                break;
            case BTGameScene.MESSAGE_READ:
                BTMessage message = (BTMessage) msg.obj;
                StartActivity.Instance.getmMainScene().mBtGameScene.HandleMessage(message);
                break;
            case BTGameScene.MESSAGE_DEVICE_NAME:
                break;
            }
        }
    };
	
	
	
}
