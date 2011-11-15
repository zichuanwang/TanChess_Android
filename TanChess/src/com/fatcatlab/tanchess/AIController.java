package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import com.badlogic.gdx.math.Vector2;

import android.util.Log;

public class AIController {

	Hashtable<Integer, ChessmanSprite> allChessmans;
	Hashtable<Integer, ChessmanSprite> myChessmans;
	Hashtable<Integer, ChessmanSprite> rivalChessmans;
	Hashtable<Integer, PropSprite> props;
	HashMap<Integer, HitChessmansStruct> doMap;
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
	private final int hingeWidth = 50;
	private final Vector2 L_Hinge = new Vector2(halfScreenWidth - 75 - hingeWidth / 2, halfScreenHeight );
	private final Vector2 R_Hinge = new Vector2(halfScreenWidth + 75 + hingeWidth / 2, halfScreenHeight );

	private final float speed_inaccuracy = 1.0f;

	public AIController(boolean player) {
		this.myChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.rivalChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.doMap = new HashMap<Integer, AIController.HitChessmansStruct>();
		this.props = null;
		this.player = player;
	}
	
	public void init(Hashtable<Integer, ChessmanSprite> chessmans, Hashtable<Integer, PropSprite> props) {
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
	private void doAction() {
		if(doMap.isEmpty()) 
			return;
		Set<Integer> keys = doMap.keySet();
		int key = 0;
		int maxPoint = 0;
		for (Iterator<Integer> iter = keys.iterator(); iter.hasNext();) {
			Integer integer = iter.next();
			HitChessmansStruct hcs = (HitChessmansStruct) doMap.get(integer);
			if (hcs.point > maxPoint) {
				key = integer.intValue();
				maxPoint = hcs.point;
			}
		}
		HitChessmansStruct toDo = doMap.get(key);
		Log.d("CAN BOUNCE OFF", "decide!----self:" + toDo.from.chessmanID + " rival:" + toDo.to.chessmanID);
		doMoveAction(toDo);
	}
	
	public void calculate() {
		int count = 0;
		for (Iterator<Integer> it = myChessmans.keySet().iterator(); it
				.hasNext();) {
			Integer key = (Integer) it.next();
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
					boolean hitHinge = checkBumpHinge(myChessman, rivalChessman);
					boolean hitOtherChessman = checkInLine(myChessman,
							rivalChessman);
					if (!hitHinge && !hitOtherChessman) {
						int myPoint = myChessman.calculateValue();
						int rivalPoint = rivalChessman.calculateValue();
						int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, rivalChessman.getPosition());
						HitChessmansStruct hcs = new HitChessmansStruct(myChessman, rivalChessman);
						hcs.point = myFinalPoint - myPoint + rivalPoint;
						Log.d("CAN BOUNCE OFF", "self:" + myChessman.chessmanID + " rival:" + rivalChessman.chessmanID
								+ " myPoint:"+myPoint+" rivalPoint:"+rivalPoint+" myFinalPoint:"+myFinalPoint+" sumPoint:"+hcs.point);
						doMap.put(count, hcs);
						count++;
					}
				}
			}
		}
		this.doAction();
		doMap.clear();
	}

	public boolean canBounceOff(ChessmanSprite from, ChessmanSprite to) {
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
		while (speed > speed_inaccuracy && distance_in_box2d > distance_try) {
			distance_try += speed * this.STEPDT;
			speed *= 1.0f - fromDamping * this.STEPDT;
		}
		if (distance_in_box2d > distance_try || speed < speed_inaccuracy)
			return false;
		float distance_can_move = this.getMoveDistance(speed, toDamping) * mPixelToMeterRatio;
		float coefficient = 1 / from2ToDistance;
		Vector2 from2ToVec = new Vector2(to.getPosition().x - from.getPosition().x, 
				to.getPosition().y - from.getPosition().y);
		Vector2 impulseVec = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
		result = this.checkOut(to, distance_can_move, impulseVec);
		return result;
	}

	public float getDistance(ChessmanSprite c1, ChessmanSprite c2) {
		return (float) Math.sqrt((c2.getPosition().x - c1.getPosition().x)
				* (c2.getPosition().x - c1.getPosition().x)
				+ (c2.getPosition().y - c1.getPosition().y)
				* (c2.getPosition().y - c1.getPosition().y));
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

	// true if will touch
	public boolean checkBumpHinge(ChessmanSprite from, ChessmanSprite to) {
		boolean result = true;
		float from2ToDistance = (float) Math
				.sqrt((double) ((from.getPosition().x - to.getPosition().x) * (from
						.getPosition().x - to.getPosition().x))
						+ (double) ((from.getPosition().y - to.getPosition().y) * (from
								.getPosition().y - to.getPosition().y)));
		if ((checkInLineChessman(from, to, L_Hinge, 28, from2ToDistance) == false)
				&& (checkInLineChessman(from, to, R_Hinge, 28, from2ToDistance) ==  false))
			result = false;
		System.out.println("from:"+from.chessmanID +"   to:"+to.chessmanID +"   result"+result);
		return result;
	}

	// point: (x,y) line : ax+by+c = 0
	public float getPointLineDistance(Vector2 point, float a, float b, float c) {
		return (float) Math.abs((a * point.x + b * point.y + c)
				/ Math.sqrt(a * a + b * b));
	}

	// chess can move by given a speed
	public float getMoveDistance(float speed, float damping) {
		float distance = 0.0f;
		while (speed > speed_inaccuracy) {
			distance += speed * this.STEPDT;
			speed *= (1.0f - damping * this.STEPDT);
			// System.out.println("speed:"+speed);
		}
		return distance;
	}

	// check whether there will be a chess in the line across the from chess and
	// to chess
	public boolean checkInLine(ChessmanSprite from, ChessmanSprite to) {
		float from2ToDistance = this.getDistance(from, to);
		for (Iterator<Integer> it = allChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite current = allChessmans.get(key);
			if(current.isDead)
				continue;
			if (current != from && current != to) {
				float point2LineDistance;
				if(to.getPosition().x - from.getPosition().x != 0) {
					float k = (to.getPosition().y - from.getPosition().y)
					/ (to.getPosition().x - from.getPosition().x);
					float a = k;
					float c = -k * from.getPosition().x + from.getPosition().y;
					point2LineDistance = getPointLineDistance(current.getPosition(), a,
							-1, c);
				}
				else {
					point2LineDistance = getPointLineDistance(current.getPosition(), 1,
							0, -1 * from.getPosition().x);
				}
				Log.d("AI TEST", "from:"+from.chessmanID+" to:"+to.chessmanID+" p2pdistance:"+from2ToDistance+
						" current:"+current.chessmanID+" p2ldistance:"+point2LineDistance);
				// 检查current到直线距离是否满足要求
				float currentR = 26 * current.getScale();
				float fromR = 26 *from.getScale();
				if(fromR < point2LineDistance - currentR) {
					Log.d("AI TEST", "from:"+from.chessmanID+" to:"+to.chessmanID+" current:"+current.chessmanID+" fromR smaller");
					continue;
				}
				// 检查是否在from和to连线后方
				float current2ToDistance = this.getDistance(current, to);
				if(current2ToDistance > from2ToDistance) {
					// 检查是否同向
					Vector2 from2ToVec = new Vector2(to.getPosition().x - from.getPosition().x, to.getPosition().y - from.getPosition().y);
					Vector2 current2ToVec = new Vector2(to.getPosition().x - current.getPosition().x, to.getPosition().y - current.getPosition().y);
					float includeAngle = current2ToVec.x * from2ToVec.x + current2ToVec.y * from2ToVec.y;
					if(includeAngle > 0) {
						Log.d("AI TEST", "from:"+from.chessmanID+" to:"+to.chessmanID+" current:"+current.chessmanID+" angle > 0");
						continue;
					}
				}
				Log.d("AI TEST", "from:"+from.chessmanID+" to:"+to.chessmanID+" return true");
				return true;
			}
		}
		return false;
	}

	
	/*
	 * Return true if will bump
	 */
	public boolean checkInLineChessman(ChessmanSprite from, ChessmanSprite to,
			Vector2 currentPosition, float currentR, float from2ToDistance) {
		float point2LineDistance;
		if (to.getPosition().x - from.getPosition().x != 0) {
			float k = (to.getPosition().y - from.getPosition().y)
					/ (to.getPosition().x - from.getPosition().x);
			float a = k;
			float c = -k * from.getPosition().x + from.getPosition().y;
			point2LineDistance = getPointLineDistance(currentPosition, a, -1, c);
		} else {
			point2LineDistance = getPointLineDistance(currentPosition, 1, 0, -1
					* from.getPosition().x);
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

	// check if the kick point is out of the border
	public boolean ifKickPointOutOfBoard(ChessmanSprite from,
			ChessmanSprite current, float k) {
		float x = ((current.getPosition().x / k) + current.getPosition().y + k
				* from.getPosition().x - from.getPosition().y)
				/ (k + 1 / k);
		float y = k * (x - from.getPosition().x) + from.getPosition().y;
		return !ChessmanSprite.checkAlive(x, y);
	}

	class HitChessmansStruct {
		public ChessmanSprite from;
		public ChessmanSprite to;
		public int point = 0;

		public HitChessmansStruct(ChessmanSprite f, ChessmanSprite t) {
			this.from = f;
			this.to = t;
		}
	}

	public void doMoveAction(HitChessmansStruct hcs) {
		float mSpeed = 33.0f;
		if (hcs.from.getScale() == ChessmanSprite.SMALL_SIZE) {
			mSpeed *= 0.63f / 33 * 45;
		} else if (hcs.from.getScale() == ChessmanSprite.LARGE_SIZE) {
			mSpeed *= 1.6f / 33 * 45;
		} else if (hcs.from.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			mSpeed *= 1.3f / 33 * 45;
		}
		ChessmanSprite to = hcs.to;
		ChessmanSprite from = hcs.from;
		float distance = getDistance(from, to);
		float coefficient = mSpeed / distance;
		Vector2 from2ToVec = new Vector2(to.getPosition().x - from.getPosition().x, to.getPosition().y - from.getPosition().y);
		Vector2 impulse = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
		hcs.from.mBody.applyLinearImpulse(impulse, hcs.from.mBody.getPosition());
	}
	
	public boolean chessmanMoveTo(ChessmanSprite chessman, Vector2 point){
		return false;
	}
	
	public void doDefence(){
		//find the biggest chess that could move to the center of the chessboard . check if there exits other chessmans between it and the point
	}
	

}
