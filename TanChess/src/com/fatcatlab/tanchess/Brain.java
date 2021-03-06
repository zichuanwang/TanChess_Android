package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.anddev.andengine.input.touch.TouchEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Brain {
	public static final boolean PLAYER1 = false;
	public static final boolean PLAYER2 = true;
	protected int mPlayer1Life, mPlayer2Life;
	protected int mPlayer1Score = 0, mPlayer2Score = 0;

	// all the chesses
	private Hashtable<Integer, ChessmanSprite> mChessmans = new Hashtable<Integer, ChessmanSprite>();
	// all the property sprites
	// private Vector<PropSprite> mProps = new Vector<PropSprite>();
	private Hashtable<Integer, PropSprite> mProps = new Hashtable<Integer, PropSprite>();

	private Vector<ChessmanSprite> mToDestroy = new Vector<ChessmanSprite>();
	// the count of the chesses and sprites
	private int mCount = 0;
	private int pCount = 0;
	public static final boolean GROUP1 = false;
	public static final boolean GROUP2 = true;

	private boolean mCurrentPlayer = PLAYER1;

	private GameScene gameScene;
	public boolean isForbidPropOn = false;
	public boolean isGameOver = false;

	// ai part
	public boolean is_AI = false;
	private AIController aiController;
	//private AIController aiController2;
	
	Brain(int player1Life, int player2Life) {
		mPlayer1Life = player1Life;
		mPlayer2Life = player2Life;
	}

	public void init() {
		mCurrentPlayer = PLAYER1;
		isGameOver = false;
		isForbidPropOn = false;
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = mChessmans.get(key);
			if (chessman.getGroup() == GROUP1)
				chessman.isForbad = false;
			else
				chessman.isForbad = true;
		}
		if (StartActivity.SCENE_STATE == StartActivity.STATE_AIGAME) {
			this.is_AI = true;
			aiController = new AIController(PLAYER2);
			aiController.owner = this;
			//aiController2 = new AIController(PLAYER1);
			//aiController2.owner = this; 
		}
	}

	public int getPlayerLife(boolean player) {
		int result = 0;
		if (player == PLAYER1) {
			result = mPlayer1Life;
		} else if (player == PLAYER2) {
			result = mPlayer2Life;
		}
		return result;
	}

	public int getPlayerScore(boolean player) {
		int result = 0;
		if (player == PLAYER1) {
			result = mPlayer1Score;
		} else if (player == PLAYER2) {
			result = mPlayer2Score;
		}
		return result;
	}

	public void addChessman(ChessmanSprite sprite) {
		mChessmans.put(mCount, sprite);
		sprite.chessmanID = mCount;
		mCount++;
	}

	private void destroyChessmanBody() {
		ChessmanSprite sprite;
		for (Iterator<ChessmanSprite> it = mToDestroy.iterator(); it.hasNext();) {
			sprite = (ChessmanSprite) (it.next());
			sprite.body.setActive(false);
		}
		mToDestroy.clear();
	}

	public void checkDrop() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = mChessmans.get(key);
			// Check whether a chessman is dropped of not
			if (chessman.isDead == false && chessman.checkAlive() == false) {
				chessman.isDead = true;
				chessman.fadeOut();
				if (chessman.getGroup() == GROUP1) {
					if (this.mCurrentPlayer == PLAYER2)
						this.mPlayer2Score += chessman.value / 2;
					this.mPlayer1Score += chessman.value;
					this.mPlayer1Life--;
				} else {
					if (this.mCurrentPlayer == PLAYER1)
						this.mPlayer1Score += chessman.value / 2;
					this.mPlayer2Score += chessman.value;
					this.mPlayer2Life--;
				}
				checkPropValid();
				mToDestroy.add(chessman);
				gameScene.stopDestroyChessman();
				StartActivity.Instance.mSound.dropSound.play();
			}
			if (chessman.body != null) {
				Vector2 linear_speed_vec = chessman.body.getLinearVelocity();
				float linear_speed = linear_speed_vec.x * linear_speed_vec.x
						+ linear_speed_vec.y * linear_speed_vec.y;
				if (linear_speed <= 0.08f) {
					chessman.body.setLinearVelocity(new Vector2(0.0f, 0.0f));
				}
			}
		}
	}

	public void spendScore(int score) {
		if (this.mCurrentPlayer == PLAYER1)
			this.mPlayer1Score -= score;
		else
			this.mPlayer2Score -= score;
		checkPropValid();
	}

	public void changePlayerWhenBTConnecting() {
		this.destroyChessmanBody();
		mCurrentPlayer = !mCurrentPlayer;
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = mChessmans.get(key);
			chessman.isForbad = true;
		}
		PropSprite sprite;
		Enumeration<Integer> props = mProps.keys();
		while (props.hasMoreElements()) {
			Integer key = (Integer) props.nextElement();
			sprite = mProps.get(key);
			sprite.isForbad = true;
		}
	}

	public void changePlayer() {
		this.destroyChessmanBody();
		if (!this.isForbidPropOn) {
			Enumeration<Integer> en = mChessmans.keys();
			while (en.hasMoreElements()) {
				Integer key = (Integer) en.nextElement();
				ChessmanSprite chessman = mChessmans.get(key);
				if (mCurrentPlayer == PLAYER1) {
					if (chessman.getGroup() == GROUP1)
						chessman.isForbad = true;
					else if (this.is_AI == false)
						chessman.isForbad = false;
				} else {
					if (chessman.getGroup() == GROUP1)
						chessman.isForbad = false;
					else
						chessman.isForbad = true;
				}
			}
			PropSprite sprite;
			Enumeration<Integer> props = mProps.keys();
			while (props.hasMoreElements()) {
				Integer key = (Integer) props.nextElement();
				sprite = mProps.get(key);
				if (mCurrentPlayer == PLAYER1) {
					if (sprite.group == GROUP1)
						sprite.isForbad = true;
					else if (this.is_AI == false)
						sprite.isForbad = false;
				} else {
					if (sprite.group == GROUP1)
						sprite.isForbad = false;
					else
						sprite.isForbad = true;
				}
			}
			// exchange the player
			mCurrentPlayer = !mCurrentPlayer;
		}
	}
	
	public void simulateAI() {
		if (this.is_AI) {
			if (this.mCurrentPlayer == aiController.player) {
				aiController.init(this.mChessmans, this.mProps,
						(aiController.player == Brain.PLAYER1) ? mPlayer1Score : mPlayer2Score);
				aiController.simulate();
			}
			/*if (this.mCurrentPlayer == aiController2.player) {
				aiController2.init(this.mChessmans, this.mProps,
						(aiController2.player == Brain.PLAYER1) ? mPlayer1Score : mPlayer2Score);
				aiController2.simulate();
			}*/
		}
	}

	public void checkPropValid() {
		PropSprite sprite;
		Enumeration<Integer> props = mProps.keys();
		while (props.hasMoreElements()) {
			Integer key = (Integer) props.nextElement();
			sprite = mProps.get(key);
			if (sprite.group == GROUP1)
				sprite.checkValid(this.mPlayer1Score);
			else
				sprite.checkValid(this.mPlayer2Score);
		}
	}

	public boolean checkValid() {
		Vector2 linear_speed_vec;
		float linear_speed;
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			if (sprite.body == null)
				continue;
			// ������ͬʱѡ����������
			if (sprite.isSelected)
				return false;
			linear_speed_vec = sprite.body.getLinearVelocity();
			linear_speed = linear_speed_vec.x * linear_speed_vec.x
					+ linear_speed_vec.y * linear_speed_vec.y;
			if (linear_speed != 0)
				return false;
			float opacity = sprite.getOpacity();
			if (opacity != 1.0f && opacity != 0.0f)
				return false;
		}
		en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			if (sprite.body != null) {
				sprite.body.setAngularVelocity(0.0f);
			}
		}
		return true;
	}

	public void addProp(PropSprite sprite) {
		mProps.put(pCount, sprite);
		sprite.propID = pCount;
		pCount++;
	}

	public void turnOnPowerUp() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isPowerUp = true;
		}
	}

	public void shutDownPowerUp() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isPowerUp = false;
		}
	}

	public void drawPropCD() {
		PropSprite sprite;
		Enumeration<Integer> props = mProps.keys();
		while (props.hasMoreElements()) {
			Integer key = (Integer) props.nextElement();
			sprite = mProps.get(key);
			if (sprite.group == GROUP1)
				sprite.drawCDRect(mPlayer1Score);
			else
				sprite.drawCDRect(mPlayer2Score);
		}
	}

	public boolean stopDestroyedChessman() {
		ChessmanSprite sprite;
		for (Iterator<ChessmanSprite> it = mToDestroy.iterator(); it.hasNext();) {
			sprite = (ChessmanSprite) (it.next());
			if (sprite.getOpacity() != 0.0f)
				return false;
			Body body = sprite.body;
			body.setLinearVelocity(new Vector2(0.0f, 0.0f));
			body.setAngularVelocity(0.0f);
		}
		return true;
	}

	public void turnOnForbid() {
		this.isForbidPropOn = true;
	}

	public void shutDownForbid() {
		this.isForbidPropOn = false;
		PropSprite sprite;
		Enumeration<Integer> props = mProps.keys();
		while (props.hasMoreElements()) {
			Integer key = (Integer) props.nextElement();
			sprite = mProps.get(key);
			if (this.mCurrentPlayer == PLAYER1) {
				if (sprite.group == GROUP1)
					sprite.isForbad = false;
			} else {
				if (sprite.group == GROUP2)
					sprite.isForbad = false;
			}
		}
	}

	public void turnOnEnlarge() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isEnlarge = true;
		}
	}

	public void shutDownEnlarge() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isEnlarge = false;
		}
	}

	public void turnOnExchange() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isChange = true;
		}
	}

	public void shutDownExchange() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isChange = false;
		}
	}

	public void setGameScene(GameScene scene) {
		gameScene = scene;
	}

	public boolean getCurrentPlayer() {
		return mCurrentPlayer;
	}

	public boolean checkGameOver() {
		if (this.mPlayer1Life > 0 && this.mPlayer2Life > 0)
			return false;
		this.isGameOver = true;
		if (this.mPlayer1Life <= 0 && this.mPlayer2Life <= 0)
			gameScene.showWinSprite(true, true);
		if (this.mPlayer1Life <= 0) {
			gameScene.showWinSprite(GROUP2, false);
		} else {
			gameScene.showWinSprite(GROUP1, false);
		}
		return true;
	}

	public void changePlayerLifeWhenExchange() {
		if (this.getCurrentPlayer() == PLAYER1) {
			this.mPlayer1Life++;
			this.mPlayer2Life--;
		} else {
			this.mPlayer1Life--;
			this.mPlayer2Life++;
		}
	}

	public boolean checkLastBiggestChess() {
		int life = 0;
		if (this.getCurrentPlayer() == PLAYER1)
			life = this.mPlayer1Life;
		else
			life = this.mPlayer2Life;
		if (life == 1) {
			Enumeration<Integer> en = mChessmans.keys();
			while (en.hasMoreElements()) {
				Integer key = (Integer) en.nextElement();
				ChessmanSprite sprite = mChessmans.get(key);
				if (sprite.getGroup() != this.getCurrentPlayer())
					continue;
				if (sprite.isDead == false) {
					if (sprite.getScale() == ChessmanSprite.LARGE_SIZE)
						return true;
				}
			}
		}
		return false;
	}

	public void setChessmanSelected(int id) {
		ChessmanSprite chessmanSprite = mChessmans.get(id);
		chessmanSprite.setAlpha(0.6f);
	}

	public void setChessmanMove(int id, float x, float y) {
		ChessmanSprite chessmanSprite = mChessmans.get(id);
		chessmanSprite.setAlpha(1.0f);
		Vector2 vector = new Vector2(x, y);
		chessmanSprite.mBody.applyLinearImpulse(vector,
				chessmanSprite.mBody.getPosition());
		StartActivity.Instance.mSound.fireSound.play();
	}

	public void setPlayer1Forbad() {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			sprite.isForbad = true;
		}
	}

	public void setPropClick(int id, int category) {
		PropSprite propSprite = mProps.get(id);
		this.gameScene.spendScore(propSprite.score);
		switch (propSprite.category) {
		case PropSprite.POWERUP:
			StartActivity.Instance.mSound.powerUpSound.play();
			break;
		case PropSprite.FORBID:
			StartActivity.Instance.mSound.teleportEffectSound.play();
			this.turnOnForbid();
			break;
		case PropSprite.ENLARGE:
			StartActivity.Instance.mSound.changeSound.play();
			break;
		case PropSprite.CHANGE:
			StartActivity.Instance.mSound.teleportSound.play();
			break;
		}
		gameScene.showPropImage(category);
	}

	public void setChange(int id) {
		ChessmanSprite chessmanSprite = mChessmans.get(id);
		chessmanSprite.exchange();
		this.gameScene.shutDownEnlarge();
	}

	public void setEnlarge(int id) {
		ChessmanSprite chessmanSprite = mChessmans.get(id);
		chessmanSprite.changeSize();

	}

	public ChessmanCollisionArray generateChessmanCollisionArray() {
		ChessmanCollisionArray array = new ChessmanCollisionArray();
		Enumeration<Integer> en = mChessmans.keys();
		int i = 0;
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite sprite = mChessmans.get(key);
			ChessmanCollisionStruct ccs = new ChessmanCollisionStruct(
					sprite.getPosition().x, sprite.getPosition().y,
					sprite.chessmanID);
			array.addItemWithCCS(ccs, i);
			i++;
		}
		return array;
	}

	public void reconcileChessmanCollisionArray(ChessmanCollisionArray array) {
		for (int i = 0; i < 32; i++) {
			ChessmanCollisionStruct ccs = array.getCSSAtIndex(i);
			ChessmanSprite sprite = this.mChessmans.get(ccs.ID);
			boolean selfDied = ChessmanSprite.checkAlive(
					sprite.getPosition().x, sprite.getPosition().y);
			boolean rivalDied = ChessmanSprite.checkAlive(ccs.Position_x,
					ccs.Position_y);
			if (selfDied && !rivalDied) {
				sprite.isDead = false;
				sprite.body.setActive(true);
				sprite.fadeIn();
				if (this.mCurrentPlayer == Brain.PLAYER1) {
					if (sprite.getGroup() == Brain.PLAYER1) {
						this.mPlayer1Life++;
						this.mPlayer1Score -= sprite.value;
					} else {
						this.mPlayer2Life++;
						this.mPlayer1Score -= sprite.value / 2;
						this.mPlayer2Score -= sprite.value;
					}
				} else {
					if (sprite.getGroup() == Brain.PLAYER1) {
						this.mPlayer1Life++;
						this.mPlayer1Score -= sprite.value / 2;
						this.mPlayer2Score -= sprite.value;
					} else {
						this.mPlayer2Life++;
						this.mPlayer2Score -= sprite.value;
					}
				}
			} else if (!selfDied && rivalDied) {
				sprite.isDead = true;
				sprite.body.setActive(false);
				sprite.fadeOut();
				if (this.mCurrentPlayer == Brain.PLAYER1) {
					if (sprite.getGroup() == Brain.PLAYER1) {
						this.mPlayer1Life--;
						this.mPlayer1Score += sprite.value;
					} else {
						this.mPlayer2Life--;
						this.mPlayer1Score += sprite.value / 2;
						this.mPlayer2Score += sprite.value;
					}
				} else {
					if (sprite.getGroup() == Brain.PLAYER1) {
						this.mPlayer1Life--;
						this.mPlayer1Score += sprite.value / 2;
						this.mPlayer2Score += sprite.value;
					} else {
						this.mPlayer2Life--;
						this.mPlayer2Score += sprite.value;
					}
				}
			}
			sprite.body.setTransform(new Vector2(ccs.Position_x / 32,
					ccs.Position_y / 32), sprite.body.getAngle());
		}
	}
	
	public int getmPlayer1Score() {
		return mPlayer1Score;
	}

	public void setmPlayer1Score(int mPlayer1Score) {
		this.mPlayer1Score = mPlayer1Score;
	}

	public int getmPlayer2Score() {
		return mPlayer2Score;
	}

	public void setmPlayer2Score(int mPlayer2Score) {
		this.mPlayer2Score = mPlayer2Score;
	}
	
	float getLargestPermittedLength(ChessmanSprite current, float max_length) {
	    float radius = 26;
		float distance, R, r;
		r = current.getScale() * radius;
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = mChessmans.get(key);
			if(chessman.isForbad && !chessman.isChange)
				continue;
			if (!chessman.isForbad && chessman.isChange)
				continue;
			if(chessman == current)
				continue;
			R = chessman.getScale() * radius;
			distance = current.getPosition().dst(chessman.getPosition());
			if(distance < max_length + 2 * R) {
				max_length = ( r + ( distance - r - R ) / 2 ) < max_length ? (r + ( distance - r - R ) / 2 ) : max_length;
			}
		}
		return max_length;
	}
	
	float getMaxLength(ChessmanSprite chessman, float radius) {
		// ��������2��
	    return this.getLargestPermittedLength(chessman, radius * chessman.getScale() * 2);
	}

	boolean containsTouchLocation(ChessmanSprite chessman, TouchEvent pSceneTouchEvent)
	{
		float touchX = pSceneTouchEvent.getX();
		float touchY = pSceneTouchEvent.getY();
		float distance = chessman.getPosition().dst(touchX, touchY);
	    int radius = 26;
		float result = this.getMaxLength(chessman, radius);
		if( distance > result - 0.1 )
		{
			return false;
		}
		return true;
	}
	
	private ChessmanSprite currentSelectedChessman = null;
	
	public void onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
		Enumeration<Integer> en = mChessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = mChessmans.get(key);
			if (chessman.isForbad && !chessman.isChange)
				continue;
			if (!chessman.isForbad && chessman.isChange)
				continue;
			if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
				if(this.containsTouchLocation(chessman, pSceneTouchEvent)) {
					if(chessman.onTouch(pSceneTouchEvent)) {
						currentSelectedChessman = chessman;
					}
					break;
				}
			}
			else if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_MOVE) {
				if(currentSelectedChessman != null) {
					currentSelectedChessman.onTouch(pSceneTouchEvent);
				}
			}
			else if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
				if(currentSelectedChessman != null) {
					currentSelectedChessman.onTouch(pSceneTouchEvent);
				}
				currentSelectedChessman = null;
			}
		}
	}
}
