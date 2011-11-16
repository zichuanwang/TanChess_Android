package com.fatcatlab.tanchess;

import java.util.Enumeration;
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
		private float maxSpeed = 0;
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
		
		public void doAction() {
			if(actionType == ATTACK_ACTION) {
				doAttackAction();
			}
			else if(actionType == DEFENCE_ACTION) {
				doDefenceAction();
			}
		}
		
		protected void doAttackAction() {
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

	public AIController(boolean player) {
		this.myChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.rivalChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.actionArray = new Vector<ActionStruct>();
		this.props = null;
		this.player = player;
	}
	
	public void init(Hashtable<Integer, ChessmanSprite> chessmans, Hashtable<Integer, PropSprite> props) {
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
	}

	public void simulate() {
		this.calculate();
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
				boolean canBounceOff = canBounceOff(myChessman, rivalChessman);
				if (canBounceOff) {
					//Log.d("CAN BOUNCE OFF", "can bounce off");
					boolean hitHinge = checkBumpHinge(myChessman, rivalChessman);
					boolean hitOtherChessman = checkInLine(myChessman, rivalChessman);
					if (!hitHinge && !hitOtherChessman) {
						int myPoint = myChessman.calculateValue();
						int rivalPoint = rivalChessman.calculateValue();
						int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, rivalChessman.getPosition());
						ActionStruct as = new ActionStruct(myChessman, rivalChessman.getPosition());
						as.actionType = ATTACK_ACTION;
						as.point = myFinalPoint - myPoint + rivalPoint;
						Log.d("CAN BOUNCE OFF", "self:" + myChessman.chessmanID + " rival:" + rivalChessman.chessmanID
								+ " myPoint:"+myPoint+" rivalPoint:"+rivalPoint+" myFinalPoint:"+myFinalPoint+" sumPoint:"+as.point);
						actionArray.add(as);
					}
					else {
						Log.d("CAN BOUNCE OFF", "but hitHing:"+hitHinge+" hitOtherChessman:"+hitOtherChessman);
					}
				}
			}
		}
	}
	
	protected void doAction() {
		if(actionArray.isEmpty()) {
			Log.d("AI ERROR", "action array empty");
			return;
		}
		int maxPoint = 0;
		ActionStruct selectedAS = null;
		for(Iterator<ActionStruct> it = actionArray.iterator(); it.hasNext();) {
			ActionStruct as = (ActionStruct)(it.next()); 
			if (as.point >= maxPoint) {
				selectedAS = as;
				maxPoint = as.point;
			} 
		}
		//Log.d("CAN BOUNCE OFF", "decide!----self:" + selectedAS.from.chessmanID + " rival:" + toDo.to.chessmanID);
		selectedAS.doAction();
		actionArray.clear();
	}
	
	protected void calculate() {
		calculateDefence();	
		calculateAttack();
		doAction();
	}
	
	

	protected boolean canBounceOff(ChessmanSprite from, ChessmanSprite to) {
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

	public float getMoveDistance(ChessmanSprite chessman, float v) {
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

	public boolean checkOut(ChessmanSprite chessman, float distance, Vector2 vector) {
		boolean result = false;
		float x = 0;
		float y = 0;
		x =  chessman.getPosition().x + vector.x * distance;
		y =  chessman.getPosition().y + vector.y * distance;
		result = !ChessmanSprite.checkAlive(x, y);
		return result;
	}

	public boolean checkBumpHinge(ChessmanSprite from, ChessmanSprite to) {
		boolean result = true;
		float from2ToDistance = getDistance(from, to);
		if ((checkChessmanInLine(from, to, L_Hinge, hingeR, from2ToDistance) == false)
				&& (checkChessmanInLine(from, to, R_Hinge, hingeR, from2ToDistance) == false))
			result = false;
		return result;
	}

	public float getPointLineDistance(Vector2 point, float a, float b, float c) {
		return (float) Math.abs((a * point.x + b * point.y + c) / Math.sqrt(a * a + b * b));
	}

	public float getMoveDistance(float speed, float damping) {
		float distance = 0.0f;
		while (speed > attack_speed_inaccuracy) {
			distance += speed * this.STEPDT;
			speed *= (1.0f - damping * this.STEPDT);
		}
		return distance;
	}

	public boolean checkInLine(ChessmanSprite from, ChessmanSprite to) {
		float from2ToDistance = this.getDistance(from, to);
		for (Iterator<Integer> it = allChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite current = allChessmans.get(key);
			if(current.isDead)
				continue;
			if (current != from && current != to) {
				Vector2 currentPosition = current.getPosition();
				float currentR = 26 * current.getScale();
				if(this.checkChessmanInLine(from, to, currentPosition, currentR, from2ToDistance)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkChessmanInLine(ChessmanSprite from, ChessmanSprite to,
			Vector2 currentPosition, float currentR, float from2ToDistance) {
		float point2LineDistance;
		if (to.getPosition().x - from.getPosition().x != 0) {
			float k = (to.getPosition().y - from.getPosition().y)
					/ (to.getPosition().x - from.getPosition().x);
			float a = k;
			float c = -k * from.getPosition().x + from.getPosition().y;
			point2LineDistance = getPointLineDistance(currentPosition, a, -1, c);
		} else {
			point2LineDistance = getPointLineDistance(currentPosition, 1, 0, -1 * from.getPosition().x);
		}
		float fromR = 26 * from.getScale();
		if (fromR < point2LineDistance - currentR) {
			return false;
		}
		// 检查是否在from和to连线后方
		float current2ToDistance = (float) Math.sqrt((double) ((currentPosition.x - to.getPosition().x) * (currentPosition.x - to.getPosition().x))
						+ (double) ((currentPosition.y - to.getPosition().y) * (currentPosition.y - to.getPosition().y)));
		if (current2ToDistance > from2ToDistance) {
			// 检查是否同向
			Vector2 from2ToVec = new Vector2(to.getPosition().x
					- from.getPosition().x, to.getPosition().y
					- from.getPosition().y);
			Vector2 current2ToVec = new Vector2(to.getPosition().x
					- currentPosition.x, to.getPosition().y - currentPosition.y);
			float includeAngle = current2ToVec.x * from2ToVec.x
					+ current2ToVec.y * from2ToVec.y;
			if (includeAngle > 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean checkChessmanInLine(ChessmanSprite from, Vector2 toVector,
			Vector2 currentPosition, float currentR, float from2ToDistance) {
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
		float fromR = 26 * from.getScale();
		if (fromR < point2LineDistance - currentR) {
			return false;
		}
		// 检查是否同向
		Vector2 from2ToVec = new Vector2(toVector.x
				- from.getPosition().x, toVector.y
				- from.getPosition().y);
		Vector2 current2ToVec = new Vector2(toVector.x
				- currentPosition.x, toVector.y - currentPosition.y);
		float includeAngle = current2ToVec.x * from2ToVec.x
				+ current2ToVec.y * from2ToVec.y;
		if (includeAngle > 0) {
			return false;
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
			ChessmanSprite current = myChessmans.get(key);
			if (current.isDead)
				continue;
			//check if there is some chessman between current and the point
			Vector2 destinationVector = getDefenceDirection(current);
			boolean needCheck = true;
			for (Iterator<Integer> it2 = allChessmans.keySet().iterator(); it2.hasNext() && needCheck;) {
				Integer key2 = (Integer) it2.next();
				ChessmanSprite check = allChessmans.get(key2);
				if (check.isDead )
					continue;
				float from2ToDistance = current.getPosition().dst(check.getPosition());
				if( checkChessmanInLine(current, destinationVector, check.getPosition(), current.getScale()*26, from2ToDistance) )
				{
					needCheck = false;
				}
			}
			if(needCheck == false)
				continue;
			ActionStruct as = new ActionStruct(current, destinationVector);
			as.defenceSpeed = this.calculateSpeed(current, destinationVector);
			as.point = ChessmanSprite.calculateValue(current.value, bestPos) - current.calculateValue();
			as.actionType = DEFENCE_ACTION;
			actionArray.add(as);
		}
	}
	
	protected Vector2 getDefenceDirection(ChessmanSprite current) {
		boolean moveToLeftMiddle = true;
		if(current.getPosition().x < halfScreenWidth) {
			moveToLeftMiddle = false;
		}
		Vector2 toPos = null;
		float selectedR = current.getScale() * 26;
		if(moveToLeftMiddle)
			toPos = new Vector2(this.L_Hinge.x + this.hingeR + selectedR + 5, this.halfScreenHeight);
		else
			toPos = new Vector2(this.R_Hinge.x - this.hingeR - selectedR - 5, this.halfScreenHeight);
		return toPos;
	}
}
