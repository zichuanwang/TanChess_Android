package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;

public class AIController {

	Hashtable<Integer, ChessmanSprite> allChessmans;
	Hashtable<Integer, ChessmanSprite> myChessmans;
	Hashtable<Integer, ChessmanSprite> rivalChessmans;
	Hashtable<Integer, PropSprite> props;
	HashMap<Integer, HitChessmansStruct> doMap;
	boolean player;

	// used to calculate the weight
	private final int W_LARGESIZE = 5;
	private final int W_MEDIUM_SIZE = 2;
	private final int W_SMALLSIZE = 1;

	private final int P_SAFEST = 5;
	private final int P_SAFER = 2;
	private final int P_UNSAFE = -1;

	private final float W_SCALE = 0.2f;
	private final float mPixelToMeterRatio = 32.0f;

	private final float L_LinearDamping = 4.0f;
	private final float M_LinearDamping = 2.5f;
	private final float S_LinearDamping = 1.0f;

	private final float STEPDT = 1.0f / 30.0f;

	// calculate from my test. damping may equal f
	/*
	 * private final float L_LinearDamping = 1.52f; private final float
	 * M_LinearDamping = 0.95f; private final float S_LinearDamping = 0.38f;
	 */

	// the m of the chess
	private final float mass = 3.0f;

	private final int halfScreenWidth = StartActivity.CAMERA_WIDTH / 2;
	private final int halfScreenHeight = StartActivity.CAMERA_HEIGHT / 2;
	private final int halfChessboardWidth = GameScene.CHESSBOARD_WIDTH/2;
	private final int halfChessboardHeight = GameScene.CHESSBOARD_HEIGHT/2;

	// hinge 's property
	private final int hingeWidth = 50;
	// Array to carry the Hinge's 4 point
	private final Vector2 L_Hinge[] = {
			new Vector2(halfChessboardWidth - 75 , halfChessboardHeight - 2),
			new Vector2(halfChessboardWidth - 75 + hingeWidth, halfChessboardHeight - 2),
			new Vector2(halfChessboardWidth - 75 , halfChessboardHeight + 2),
			new Vector2(halfChessboardWidth - 75 + hingeWidth, halfChessboardHeight + 2) };
	private final Vector2 R_Hinge[] = {
			new Vector2(halfChessboardWidth + 75, halfChessboardHeight - 2),
			new Vector2(halfChessboardWidth + 75 + hingeWidth, halfChessboardHeight - 2),
			new Vector2(halfChessboardWidth + 75, halfChessboardHeight + 2),
			new Vector2(halfChessboardWidth + 75 + hingeWidth, halfChessboardHeight + 2) };

	// size from scale to pix
	private final int scaleToPix = 55;
	// inaccuracy of speed
	private final float speed_inaccuracy = 1.0f;

	public AIController(boolean player) {
		this.myChessmans = new Hashtable();
		this.rivalChessmans = new Hashtable();
		this.props = null;
		this.player = player;
	}

	public void print(Hashtable<Integer, ChessmanSprite> chessmans,
			Hashtable<Integer, PropSprite> props) {
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
		this.calculate();

	}

	public void calculate() {
		Enumeration<Integer> en1 = myChessmans.keys();
		Enumeration<Integer> en2 = rivalChessmans.keys();

		// System.out.println("en1.size:" + myChessmans.size());
		// System.out.println("en2.size:" + rivalChessmans.size());

		// System.out.println("cankickoff:"+canBounceOff(myChessmans.get(new
		// Integer(27)), rivalChessmans.get(new Integer(4))));
		System.out.println("hinge:"
				+ this.checkBumpHinge(myChessmans.get(new Integer(29)),
						rivalChessmans.get(new Integer(2))));

		/*
		 * boolean r = checkInLine(myChessmans.get(new Integer(27)),
		 * rivalChessmans.get(new Integer(4))); System.out.println(r);
		 */

		// used to get the detail position
		// ChessmanSprite chess = rivalChessmans.get(new Integer(1));
		// System.out.println("x:"+chess.getPosition().x);
		// System.out.println("y:"+chess.getPosition().y);

		if (true)
			return;

		for (Iterator it = myChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite myChessman = myChessmans.get(key);

			for (Iterator it2 = rivalChessmans.keySet().iterator(); it2
					.hasNext();) {
				Integer key2 = (Integer) it2.next();
				ChessmanSprite rivalChessman = rivalChessmans.get(key2);

				if (!this.checkBumpHinge(myChessman, rivalChessman)) {
					if (canBounceOff(myChessman, rivalChessman)) {
						if (!checkInLine(myChessman, rivalChessman)) {
							Log.d("can", myChessman.chessmanID + "--hit--"
									+ rivalChessman.chessmanID);
							this.calculateWeight(myChessman, rivalChessman);
						}
					}
				}
			}
		}

	}

	/*
	 * ---------------------------->x | | p1 | | p2 | | y
	 */

	public boolean canBounceOff(ChessmanSprite from, ChessmanSprite to) {
		boolean result = false;
		float mSpeed = 33.0f;
		float mAngle = 0.0f;
		float fromDamping = 0.0f;
		float toDamping = 0.0f;
		if (from.getScale() == ChessmanSprite.SMALL_SIZE) {
			mSpeed *= 0.63f / 33 * 45;
			fromDamping = this.S_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.LARGE_SIZE) {
			mSpeed *= 1.6f / 33 * 45;
			fromDamping = this.L_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			mSpeed *= 1.3f / 33 * 45;
			fromDamping = this.M_LinearDamping;
		}
		if (to.getScale() == ChessmanSprite.SMALL_SIZE) {
			toDamping = this.S_LinearDamping;
		} else if (to.getScale() == ChessmanSprite.LARGE_SIZE) {
			toDamping = this.L_LinearDamping;
		} else if (to.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			toDamping = this.M_LinearDamping;
		}

		// float tan = (to.getPosition().y - from.getPosition().y)
		// / (to.getPosition().x - from.getPosition().x);

		mAngle = (float) Math.atan((to.getPosition().y - from.getPosition().y)
				/ (to.getPosition().x - from.getPosition().x));
		if (mAngle < 0) {
			if (to.getPosition().y > from.getPosition().y)
				mAngle = (float) (mAngle + Math.PI);
			else
				mAngle = (float) (-mAngle + Math.PI);
		} else {
			if (to.getPosition().y < from.getPosition().y)
				mAngle = (float) (mAngle + Math.PI);
		}

		// System.out.println("angle:" + mAngle);

		// Vector2 impulse = new Vector2(mSpeed * (float) Math.sin(mAngle),
		// mSpeed
		// * -(float) Math.cos(mAngle));

		mSpeed = mSpeed / this.mass;

		float distance_in_box2d = getDistance(from, to)
				/ this.mPixelToMeterRatio;
		// System.out.println("distance_in_box2d:" + distance_in_box2d);

		float distance = 0.0f;
		float time = 0.0f;
		while (mSpeed > speed_inaccuracy && distance_in_box2d > distance) {
			distance += mSpeed * this.STEPDT;
			mSpeed *= 1.0f - fromDamping * this.STEPDT;
			time += this.STEPDT;
		}
		// System.out.println("time:" + time);
		// System.out.println("distance:" + distance);

		// if the distance between the two chess is larger than the from chess
		// can move , return false;
		if (distance_in_box2d > distance)
			return false;

		float distance_can_move = this.getMoveDistance(mSpeed, toDamping);

		// System.out.println("canmove :" + distance_can_move);

		result = this.checkOut(to, distance_can_move * this.mPixelToMeterRatio,
				mAngle);
		// System.out.println("result:" + result);
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

	public boolean checkOut(ChessmanSprite chessman, float distance,
			double radians) {
		int Quadrant = -1;
		boolean result = false;
		float x = 0;
		float y = 0;
		if (radians < (Math.PI) / 2 && radians > 0)
			Quadrant = 1;
		else if (radians > (Math.PI) / 2 && radians < Math.PI)
			Quadrant = 2;
		else if (radians > Math.PI && radians < Math.PI / 2 * 3)
			Quadrant = 3;
		else
			Quadrant = 4;
		switch (Quadrant) {
		case 1:
			x = (float) (distance / Math.sin(radians) + chessman.getPosition().x);
			y = (float) (distance / Math.cos(radians) + chessman.getPosition().y);
			break;
		case 2:
			x = (float) (chessman.getPosition().x - distance
					/ Math.sin(radians));
			y = (float) (distance / Math.abs(Math.cos(radians)) + chessman
					.getPosition().y);
			break;
		case 3:
			x = (float) (chessman.getPosition().x - distance
					/ Math.abs(Math.sin(radians)));
			y = (float) (chessman.getPosition().y - distance
					/ Math.abs(Math.cos(radians)));
			break;
		case 4:
			x = (float) (distance / Math.abs(Math.sin(radians)) + chessman
					.getPosition().x);
			y = (float) (chessman.getPosition().y - distance
					/ Math.cos(radians));
			break;
		}
		result = ChessmanSprite.checkAlive(x, y);
		return !result;
	}

	// true if will touch
	public boolean checkBumpHinge(ChessmanSprite from, ChessmanSprite to) {
		boolean result = false;

		if (from.getPosition().x == to.getPosition().x) {
			float pos_x = from.getPosition().x;
			float r = from.getScale() * (26 + 2);
			if ((pos_x > L_Hinge[0].x - r && pos_x < L_Hinge[1].x + r)
					|| (pos_x > R_Hinge[0].x - r && pos_x < R_Hinge[1].x + r))
				return true;
			else
				return false;
		}
		if (from.getPosition().y == to.getPosition().y) {
			float pos_y = from.getPosition().y;
			float r = from.getScale() * (26 + 2);
			if ((pos_y > L_Hinge[0].y - r && pos_y < L_Hinge[1].y + r)
					|| (pos_y > R_Hinge[0].y - r && pos_y < R_Hinge[1].y + r))
				return true;
			else
				return false;
		}

		float k = (to.getPosition().y - from.getPosition().y)
				/ (to.getPosition().x - from.getPosition().x);
		float a = k;
		float c = -k * from.getPosition().x + from.getPosition().y;

		// should know the two chess are in the same side
		boolean inSameSide = false;
		if ((to.getPosition().y - this.halfScreenHeight)
				* (from.getPosition().y - this.halfScreenHeight) < 0)
			inSameSide = false;
		else
			inSameSide = true;

		// check hinge if both chess are in same side than should check the
		// --TO-- chess, else check the FROM chess
		for (int i = 0; i < 4; i++) {
			if (inSameSide == false) {
				System.out.println("dis:"+getPointLineDistance(L_Hinge[i], a, -1, c));
				System.out.println("left:"+L_Hinge[i].x + "      "+L_Hinge[i].y);
				if (getPointLineDistance(L_Hinge[i], a, -1, c) < from
						.getScale() * 28|| getPointLineDistance(R_Hinge[i], a, -1, c) < from
								.getScale() * 28)
					return true;
			} else {
				if (getPointLineDistance(L_Hinge[i], a, -1, c) < to.getScale()
						* 28|| getPointLineDistance(R_Hinge[i], a, -1, c) < to
								.getScale() * 28)
					return true;
			}

		}

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
		for (Iterator it = allChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite current = allChessmans.get(key);
			if (current != from && current != to) {

				float k = (to.getPosition().y - from.getPosition().y)
						/ (to.getPosition().x - from.getPosition().x);
				float a = k;
				float c = -k * from.getPosition().x + from.getPosition().y;
				float distance = getPointLineDistance(current.getPosition(), a,
						-1, c);

				if (((current.getPosition().x - from.getPosition().x)
						* (current.getPosition().x - to.getPosition().x) > 0)
						&& ((current.getPosition().y - from.getPosition().y)
								* (current.getPosition().y - to.getPosition().y) > 0)) {

					boolean shouldCheck = false;
					if ((from.getPosition().x - to.getPosition().x > 0)
							&& (current.getPosition().x < to.getPosition().x)) {
						if ((from.getPosition().y - to.getPosition().y > 0)
								&& (current.getPosition().y < to.getPosition().y))
							shouldCheck = true;
						else if ((from.getPosition().y - to.getPosition().y < 0)
								&& (current.getPosition().y > to.getPosition().y))
							shouldCheck = true;
					} else if ((from.getPosition().x - to.getPosition().x < 0)
							&& (current.getPosition().x > to.getPosition().x)) {
						if ((from.getPosition().y - to.getPosition().y > 0)
								&& (current.getPosition().y < to.getPosition().y))
							shouldCheck = true;
						else if ((from.getPosition().y - to.getPosition().y < 0)
								&& (current.getPosition().y > to.getPosition().y))
							shouldCheck = true;
					}

					/*
					 * if (((from.getPosition().x - to.getPosition().x > 0) &&
					 * (current .getPosition().x < to.getPosition().x)) ||
					 * ((from.getPosition().x - to.getPosition().x < 0) &&
					 * (current .getPosition().x > to.getPosition().x))) {
					 */
					if (shouldCheck) {
						// System.out.println("dis1::::" + distance + "     id:"
						// + current.chessmanID);
						if (getPointLineDistance(current.getPosition(), a, -1,
								c) < 26 * (current.getScale() + to.getScale())) {
							if (this.ifKickPointOutOfBoard(from, current, k))
								return true;
						}
					}
				}


				// 如果在2个棋子中间，那么要算from的半径+挡住的半径

				if (((current.getPosition().x - from.getPosition().x)
						* (current.getPosition().x - to.getPosition().x) < 0)
						&& ((current.getPosition().y - from.getPosition().y)
								* (current.getPosition().y - to.getPosition().y) < 0)) {

					// else {
					// System.out.println("dis2::::" + distance + "     id:"
					// + current.chessmanID);
					if (getPointLineDistance(current.getPosition(), a, -1, c) < 26 * (current
							.getScale() + from.getScale()))
						if (this.ifKickPointOutOfBoard(from, current, k))
							return true;
				}
			}
		}
		return false;
	}

	// check if the kick point is out of the border
	// 璁＄������氦�规����妫�����
	public boolean ifKickPointOutOfBoard(ChessmanSprite from,
			ChessmanSprite current, float k) {
		float x = ((current.getPosition().x / k) + current.getPosition().y + k
				* from.getPosition().x - from.getPosition().y)
				/ (k + 1 / k);
		float y = k * (x - from.getPosition().x) + from.getPosition().y;
		return !ChessmanSprite.checkAlive(x, y);
	}

	// struct of Chessman from && chessman to
	class HitChessmansStruct {
		public ChessmanSprite from;
		public ChessmanSprite to;

		public HitChessmansStruct(ChessmanSprite f, ChessmanSprite t) {
			this.from = f;
			this.to = t;
		}
	}

	// calculate the weight of the action
	public int calculateWeight(ChessmanSprite from, ChessmanSprite to) {
		int weight = 0;
		if (to.getScale() == ChessmanSprite.LARGE_SIZE) {
			weight += this.W_LARGESIZE;
		} else if (to.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			weight += this.W_MEDIUM_SIZE;
		} else {
			weight += this.W_SMALLSIZE;
		}

		float distance = this.getDistance(from, to);
		float _distance = 26 * (from.getScale() + to.getScale());

		float x = to.getPosition().x
				- (to.getPosition().x - from.getPosition().x)
				* (distance / _distance);
		float y = to.getPosition().y
				- (to.getPosition().y - from.getPosition().y)
				* (distance / _distance);

		// System.out.println("x:" + x + "    y:" + y);
		// System.out.println("end");

		return weight;
	}

}
