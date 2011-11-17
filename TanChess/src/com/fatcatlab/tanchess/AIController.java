package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import com.badlogic.gdx.math.Vector2;

import android.util.Log;

public class AIController {
	
	private final static int ATTACK_ACTION = 0;
	private final static int DEFENCE_ACTION = 1;
	
	class ActionStruct {
		public ChessmanSprite from;
		public Vector2 to;
		public int point = 0;
		public int actionType = ATTACK_ACTION;
		public float defenceSpeed;
		public float maxSpeed = 0;
		public boolean usePowerUp = false;
		public ActionStruct(ChessmanSprite f, Vector2 t) {
			this.from = f;
			this.to = t;
			maxSpeed = 0;
			if (this.from.getScale() == ChessmanSprite.SMALL_SIZE) {
				maxSpeed = MAX_S_SPEED;
			} else if (this.from.getScale() == ChessmanSprite.LARGE_SIZE) {
				maxSpeed = MAX_L_SPEED;
			} else if (this.from.getScale() == ChessmanSprite.MEDIUM_SIZE) {
				maxSpeed = MAX_M_SPEED;
			}
		}
		
		protected void doAction() {
			if(actionType == ATTACK_ACTION) {
				doAttackAction();
			}
			else if(actionType == DEFENCE_ACTION) {
				doDefenceAction();
			}
		}
		
		protected void doAttackAction() {
			if(usePowerUp == true)
				this.doMoveAction(this.from, this.to, maxSpeed * 1.4f);
			else
				this.doMoveAction(this.from, this.to, maxSpeed);
		}
		
		protected void doDefenceAction() {
			if(maxSpeed < defenceSpeed)
				this.doMoveAction(this.from, to, maxSpeed);
			else 
				this.doMoveAction(this.from, to, defenceSpeed);
		}
		
		protected void doMoveAction(ChessmanSprite from, Vector2 toPos, float speed) {
			Vector2 fromPos = from.getPosition();
			float distance = getDistance(fromPos, toPos);
			float coefficient = speed / distance;
			Vector2 from2ToVec = new Vector2(toPos.x - fromPos.x, toPos.y - fromPos.y);
			Vector2 impulse = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
			from.mBody.applyLinearImpulse(impulse, from.mBody.getPosition());
		}
	}
	
	Hashtable<Integer, ChessmanSprite> allChessmans;
	Hashtable<Integer, ChessmanSprite> myChessmans;
	Hashtable<Integer, ChessmanSprite> rivalChessmans;
	Hashtable<Integer, PropSprite> props;
	Vector<ActionStruct> actionArray;
	boolean player;

	private final float MAX_S_SPEED = 0.63f * 45.0f;
	private final float MAX_M_SPEED = 1.3f * 45.0f;
	private final float MAX_L_SPEED = 1.6f * 45.0f;

	//private final float W_SCALE = 0.2f;
	// private final float W_SCALE = 0.2f;
	private final float mPixelToMeterRatio = 32.0f;

	private final float L_LinearDamping = 4.0f;
	private final float M_LinearDamping = 2.5f;
	private final float S_LinearDamping = 1.0f;
	private final float STEPDT = 1.0f / 30.0f;
	private final float mass = 3.0f;
	private final int halfScreenWidth = StartActivity.CAMERA_WIDTH / 2;
	private final int halfScreenHeight = StartActivity.CAMERA_HEIGHT / 2;
	// hinge 's property
	private final float hingeR = 19.0f;
	private final Vector2 L_Hinge = new Vector2(halfScreenWidth - 75, halfScreenHeight );
	private final Vector2 R_Hinge = new Vector2(halfScreenWidth + 75, halfScreenHeight );

	private final float attack_speed_inaccuracy = 1.0f;
	private final float defence_speed_inaccuracy = 0.01f;
	
	private boolean canPowerUp = false;
	private boolean canFobid = false;
	private boolean canEnlarge = false;
	private boolean canExchange = false;
	private int mPlayerValue = 0;

	public AIController(boolean player) {
		this.myChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.rivalChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.actionArray = new Vector<ActionStruct>();
		this.props = null;
		this.player = player;
	}
	
	public void init(Hashtable<Integer, ChessmanSprite> chessmans, Hashtable<Integer, PropSprite> props ,int value) {
		myChessmans.clear();
		rivalChessmans.clear();
		this.allChessmans = chessmans;
		Enumeration<Integer> en = chessmans.keys();
		while (en.hasMoreElements()) {
			Integer key = (Integer) en.nextElement();
			ChessmanSprite chessman = chessmans.get(key);
			if (chessman.isDead == false) {
				if (chessman.mGroup == this.player)
					myChessmans.put(key, chessman);
				else
					rivalChessmans.put(key, chessman);
			}
		}
		this.props = props;
		this.mPlayerValue = value;
	}

	public void simulate() {
		this.fillPropState();
		this.calculate();
	}
	
	protected void fillPropState(){
		canPowerUp = false;
		canFobid = false;
		canEnlarge = false;
		canExchange = false;
		if(mPlayerValue >= PropSprite.POWERUP_NEED_SCORE)
			canPowerUp = true;
		if(mPlayerValue >= PropSprite.FORBID_NEED_SCORE)
			canFobid = true;
		if(mPlayerValue >= PropSprite.ENLARGE_NEED_SCORE)
			canEnlarge = true;
		if(mPlayerValue >= PropSprite.CHANGE_NEED_SCORE)
			canExchange = true;
		
		Log.d("PROP", "power:"+canPowerUp);
		Log.d("PROP", "forbid:"+canFobid);
		Log.d("PROP", "canenlarge:"+canEnlarge);
		Log.d("PROP", "canexchange:"+canExchange);
		
	}
	
	protected void calculateAttack() {
		for (Iterator<Integer> it = myChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer)it.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if (myChessman.isDead)
				continue;
			for (Iterator<Integer> it2 = rivalChessmans.keySet().iterator(); it2.hasNext();) {
				Integer key2 = (Integer) it2.next();
				ChessmanSprite rivalChessman = rivalChessmans.get(key2);
				if (rivalChessman.isDead)
					continue;
				boolean canBounceOffWithoutPower = canBounceOff(myChessman, rivalChessman, false);
				if (canBounceOffWithoutPower) {
					//Log.d("CAN BOUNCE OFF", "can bounce off");
					boolean hitHinge = checkBumpHinge(myChessman, rivalChessman);
					boolean hitOtherChessman = checkInLine(myChessman, rivalChessman);
					if (!hitHinge && !hitOtherChessman) {
						this.createActionStruct(myChessman, rivalChessman);
					}
					else {
						//Log.d("CAN BOUNCE OFF", "but hitHing:"+hitHinge+" hitOtherChessman:"+hitOtherChessman);
					}
				}else if(this.canPowerUp){
					boolean canBounceOffWithPower = canBounceOff(myChessman, rivalChessman, true);
					if(canBounceOffWithPower){
						if(rivalChessman.getScale() == ChessmanSprite.LARGE_SIZE && this.random(80))
						{
							ActionStruct as = this.createActionStruct(myChessman, rivalChessman);
							as.usePowerUp = true;
						}else if(rivalChessman.getScale() == ChessmanSprite.MEDIUM_SIZE && this.random(20)){
							ActionStruct as = this.createActionStruct(myChessman, rivalChessman);
							as.usePowerUp = true;
						}
					}
				}
			}
		}
	}
	
	protected ActionStruct createActionStruct(ChessmanSprite myChessman, ChessmanSprite rivalChessman){
		int myPoint = myChessman.calculateValue();
		int rivalPoint = rivalChessman.calculateValue();
		int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, rivalChessman.getPosition());
		ActionStruct as = new ActionStruct(myChessman, rivalChessman.getPosition());
		as.actionType = ATTACK_ACTION;
		as.point = myFinalPoint - myPoint + rivalPoint;
		//Log.d("CAN BOUNCE OFF", "self:" + myChessman.chessmanID + " rival:" + rivalChessman.chessmanID
		//		+ " myPoint:"+myPoint+" rivalPoint:"+rivalPoint+" myFinalPoint:"+myFinalPoint+" sumPoint:"+as.point);
		actionArray.add(as);
		return as;
	}
	
	protected void doAction() {
		if(actionArray.isEmpty()) {
			Log.d("AI ERROR", "action array empty");
			return;
		}
		int maxPoint = -100000;
		ActionStruct selectedAS = null;
		for(Iterator<ActionStruct> it = actionArray.iterator(); it.hasNext();) {
			ActionStruct as = (ActionStruct)(it.next()); 
			Log.d("CAN BOUNCE OFF", "type:"+as.actionType+" point"+as.point+" from:"+as.from.chessmanID);
			if (as.point >= maxPoint) {
				selectedAS = as;
				maxPoint = as.point;
			} 
		}
		//Log.d("CAN BOUNCE OFF", "decide!----self:" + selectedAS.from.chessmanID + " rival:" + toDo.to.chessmanID);
		if(selectedAS.usePowerUp == true)
		{
			Enumeration<Integer> en = props.keys();
			while(en.hasMoreElements()){
				Integer key = (Integer)en.nextElement();
				PropSprite prop = props.get(key);
				if( prop.category ==  PropSprite.POWERUP ){
					prop.gameScene.spendScore(PropSprite.POWERUP_NEED_SCORE);
					prop.func(true);
				}
			}
		}
		selectedAS.doAction();
		actionArray.clear();
	}
	
	protected void calculate() {
		//calculateDefence();	
		calculateAttack();
		doAction();
	}
	
	

	protected boolean canBounceOff(ChessmanSprite from, ChessmanSprite to , boolean powerUp) {
		boolean result = false;
		float impulse = 0;
		float fromDamping = 0.0f;
		float toDamping = 0.0f;
		if (from.getScale() == ChessmanSprite.SMALL_SIZE) {
			impulse = this.MAX_S_SPEED;
			fromDamping = this.S_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.LARGE_SIZE) {
			impulse = this.MAX_L_SPEED;
			fromDamping = this.L_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			impulse = this.MAX_M_SPEED;
			fromDamping = this.M_LinearDamping;
		}
		if (to.getScale() == ChessmanSprite.SMALL_SIZE) {
			toDamping = this.S_LinearDamping;
		} else if (to.getScale() == ChessmanSprite.LARGE_SIZE) {
			toDamping = this.L_LinearDamping;
		} else if (to.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			toDamping = this.M_LinearDamping;
		}
		float speed = impulse / this.mass;
		if(powerUp)
			speed*=1.4f;
		float fromR = from.getScale() * 26;
		float toR = to.getScale() * 26;
		float from2ToDistance = getDistance(from, to);
		float distance_in_box2d = (from2ToDistance - fromR - toR) / mPixelToMeterRatio;
		float distance_try = 0.0f;
		while (speed > attack_speed_inaccuracy && distance_in_box2d > distance_try) {
			distance_try += speed * this.STEPDT;
			speed *= 1.0f - fromDamping * this.STEPDT;
		}
		if (distance_in_box2d > distance_try || speed < attack_speed_inaccuracy)
			return false;
		float distance_can_move = this.getMoveDistance(speed, toDamping) * mPixelToMeterRatio;
		float coefficient = 1 / from2ToDistance;
		Vector2 from2ToVec = new Vector2(to.getPosition().x - from.getPosition().x, 
				to.getPosition().y - from.getPosition().y);
		Vector2 impulseVec = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
		result = this.checkOut(to, distance_can_move, impulseVec);
		return result;
	}

	protected float getDistance(ChessmanSprite c1, ChessmanSprite c2) {
		return (float) Math.sqrt((c2.getPosition().x - c1.getPosition().x)
				* (c2.getPosition().x - c1.getPosition().x)
				+ (c2.getPosition().y - c1.getPosition().y)
				* (c2.getPosition().y - c1.getPosition().y));
	}
	
	protected float getDistance(Vector2 v1, Vector2 v2) {
		return (float) Math.sqrt((v2.x - v1.x)
				* (v2.x - v1.x)
				+ (v2.y - v1.y)
				* (v2.y - v1.y));
	}

	protected float getMoveDistance(ChessmanSprite chessman, float v) {
		float result = 0.0f;
		if (chessman.getScale() == ChessmanSprite.LARGE_SIZE) {
			result = (v * v) / (2 * this.L_LinearDamping / this.mass);
		} else if (chessman.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			result = (v * v) / (2 * this.M_LinearDamping / this.mass);
		} else if (chessman.getScale() == ChessmanSprite.SMALL_SIZE) {
			result = (v * v) / (2 * this.S_LinearDamping / this.mass);
		}
		return result;
	}

	protected boolean checkOut(ChessmanSprite chessman, float distance, Vector2 vector) {
		boolean result = false;
		float x = 0;
		float y = 0;
		x =  chessman.getPosition().x + vector.x * distance;
		y =  chessman.getPosition().y + vector.y * distance;
		result = !ChessmanSprite.checkAlive(x, y);
		return result;
	}

	protected boolean checkBumpHinge(ChessmanSprite from, ChessmanSprite to) {
		boolean result = true;
		float from2ToDistance = getDistance(from, to);
		float L_checkR = this.calculateNeededR(from, to, L_Hinge);
		float R_checkR = this.calculateNeededR(from, to, R_Hinge);
		
		if ((checkChessmanInLine(from, to, L_Hinge, hingeR, L_checkR, from2ToDistance) == false)
				&& (checkChessmanInLine(from, to, R_Hinge, hingeR , R_checkR , from2ToDistance) == false))
			result = false;
		return result;
	}

	protected float getPointLineDistance(Vector2 point, float a, float b, float c) {
		return (float) Math.abs((a * point.x + b * point.y + c) / Math.sqrt(a * a + b * b));
	}

	protected float getMoveDistance(float speed, float damping) {
		float distance = 0.0f;
		while (speed > attack_speed_inaccuracy) {
			distance += speed * this.STEPDT;
			speed *= (1.0f - damping * this.STEPDT);
		}
		return distance;
	}

	protected boolean checkInLine(ChessmanSprite from, ChessmanSprite to) {
		float from2ToDistance = this.getDistance(from, to);
		for (Iterator<Integer> it = allChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite current = allChessmans.get(key);
			if(current.isDead)
				continue;
			if (current != from && current != to) {
				Vector2 currentPosition = current.getPosition();
				float checkR = this.calculateNeededR(from, to, currentPosition);
				float currentR = 26 * current.getScale();
				if(this.checkChessmanInLine(from, to, currentPosition, currentR, checkR , from2ToDistance )) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean checkChessmanInLine(ChessmanSprite from, ChessmanSprite to,
			Vector2 currentPosition, float currentR , float checkR , float from2ToDistance) {
		return this.checkChessmanInLine(from, to.getPosition(), currentPosition, currentR, checkR , from2ToDistance);
	}
	
	protected boolean checkChessmanBetweenLine(ChessmanSprite from, Vector2 toVector,
			Vector2 currentPosition, float currentR, float from2ToDistance) {
		boolean result = this.checkChessmanInLine(from, toVector, currentPosition, currentR , from.getScale()*26, from2ToDistance);
		if(result) {
			float includeAngle = this.getIncludeAngle(from.getPosition(), toVector, currentPosition);
			if(includeAngle < 0)
				result = false;
		}
		return result;
	}
	
	protected float getIncludeAngle(Vector2 fromVector, Vector2 toVector, Vector2 currentVector) {
		float includeAngle = 0;
		Vector2 from2ToVec = new Vector2(toVector.x
				- fromVector.x, toVector.y
				- fromVector.y);
		Vector2 current2ToVec = new Vector2(toVector.x
				- currentVector.x, toVector.y - currentVector.y);
		includeAngle = current2ToVec.x * from2ToVec.x
				+ current2ToVec.y * from2ToVec.y;
		return includeAngle;
	}
	
	protected boolean checkChessmanInLine(ChessmanSprite from, Vector2 toVector,
			Vector2 currentPosition, float currentR , float checkR, float from2ToDistance) {
		float point2LineDistance;
		if (toVector.x - from.getPosition().x != 0) {
			float k = (toVector.y - from.getPosition().y)
					/ (toVector.x - from.getPosition().x);
			float a = k;
			float c = -k * from.getPosition().x + from.getPosition().y;
			point2LineDistance = getPointLineDistance(currentPosition, a, -1, c);
		} else {
			point2LineDistance = getPointLineDistance(currentPosition, 1, 0, -1 * from.getPosition().x);
		}
		if (checkR < point2LineDistance - currentR) {
			return false;
		}
		// 检查是否在from和to连线后方
		float current2ToDistance = (float) Math.sqrt((double) ((currentPosition.x - toVector.x) * (currentPosition.x - toVector.x))
						+ (double) ((currentPosition.y - toVector.y) * (currentPosition.y - toVector.y)));
		if (current2ToDistance > from2ToDistance) {
			// 检查是否同向
			float includeAngle = this.getIncludeAngle(from.getPosition(), toVector, currentPosition);
			if (includeAngle > 0) {
				return false;
			}
		}
		return true;
	}
		
	protected float calculateSpeedHelp(float distance_in_box2d, float fromDamping) {
		float speed = defence_speed_inaccuracy;
		float distance_try = 0;
		while ( distance_in_box2d > distance_try) {
			distance_try += speed * this.STEPDT;
			speed /= 1.0f - fromDamping * this.STEPDT;
		}
		speed *= this.mass;
		return speed;
	}
	
	protected float calculateSpeed(ChessmanSprite chessman, Vector2 toPos) {
		float from2ToDistance = getDistance(chessman.getPosition(), toPos);
		float distance_in_box2d = from2ToDistance / mPixelToMeterRatio;
		float fromDamping = 0.0f;
		if (chessman.getScale() == ChessmanSprite.SMALL_SIZE) {
			fromDamping = this.S_LinearDamping;
		} else if (chessman.getScale() == ChessmanSprite.LARGE_SIZE) {
			fromDamping = this.L_LinearDamping;
		} else if (chessman.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			fromDamping = this.M_LinearDamping;
		}
		float speed = this.calculateSpeedHelp(distance_in_box2d, fromDamping);
		return speed;
	}
	
	protected void calculateDefence() {
		//find the biggest chess that could move to the center of the chessboard. 
		// check if there exits other chessmans between it and the point.
		Vector2 bestPos = new Vector2(this.halfScreenWidth, this.halfScreenHeight);
		for (Iterator<Integer> it = myChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if (myChessman.isDead)
				continue;
			//check if there is some chessman between current and the point
			Vector2 destinationVector = getDefenceDirection(myChessman);
			boolean needCheck = false;
			for (Iterator<Integer> it2 = allChessmans.keySet().iterator(); it2.hasNext();) {
				Integer key2 = (Integer) it2.next();
				ChessmanSprite check = allChessmans.get(key2);
				float checkR = check.getScale() * 26;
				if(check == myChessman)
					continue;
				if (check.isDead )
					continue;
				float from2ToDistance = myChessman.getPosition().dst(check.getPosition());
				if(checkChessmanBetweenLine(myChessman, destinationVector, check.getPosition(), checkR, from2ToDistance))
				{
					needCheck = true;
					break;
					/*destinationVector = this.getAlternativeDefenceDirection(myChessman);
					if(checkChessmanInLine(myChessman, destinationVector, check.getPosition(), checkR, from2ToDistance)) {
						needCheck = true;
					}*/
				}
			}
			ActionStruct as = new ActionStruct(myChessman, destinationVector);
			as.defenceSpeed = this.calculateSpeed(myChessman, destinationVector);
			as.point = ChessmanSprite.calculateValue(myChessman.value, bestPos) - myChessman.calculateValue();
			as.actionType = DEFENCE_ACTION;
			if(as.defenceSpeed > as.maxSpeed)
				as.point -= 1000;
			if(needCheck) 
				as.point -= 10000;
			if(myChessman.getScale() == ChessmanSprite.SMALL_SIZE)
				as.point -= 5000;
			actionArray.add(as);
		}
	}
	
	protected Vector2 getAlternativeDefenceDirection(ChessmanSprite current) {
		return getDefenceDirectionHelp(current, false);
	}
	
	protected Vector2 getDefenceDirectionHelp(ChessmanSprite current, boolean isFirstChoice) {
		boolean moveToLeftMiddle = true;
		if(isFirstChoice) {
			if(current.getPosition().x < halfScreenWidth) {
				moveToLeftMiddle = false;
			}
		}
		else {
			if(current.getPosition().x > halfScreenWidth) {
				moveToLeftMiddle = false;
			}
		}
		Vector2 toPos = null;
		float selectedR = current.getScale() * 26;
		if(moveToLeftMiddle)
			toPos = new Vector2(this.L_Hinge.x + this.hingeR + selectedR, this.halfScreenHeight);
		else
			toPos = new Vector2(this.R_Hinge.x - this.hingeR - selectedR, this.halfScreenHeight);
		return toPos;
	}
	
	protected Vector2 getDefenceDirection(ChessmanSprite current) {
		return getDefenceDirectionHelp(current, true);
	}
	
	/*
	 * Input the percentage, return true || false
	 */
	public boolean random(float percentage){
		float randomNumber = (float) Math.random();
		float Boundary = percentage / 100.0f;
		if(randomNumber < Boundary)
			return true;
		else
			return false;
	}
	
	protected float calculateNeededR(ChessmanSprite from, ChessmanSprite to, Vector2 currentPos){
		if( this.getIncludeAngle(from.getPosition(), to.getPosition(), currentPos ) < 0 )
			return from.getScale()*26;
		else
			return to.getScale()*26;
	}
	
	
}
