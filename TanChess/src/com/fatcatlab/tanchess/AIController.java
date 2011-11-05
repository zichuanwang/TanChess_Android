package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;

public class AIController {

	Hashtable<Integer, ChessmanSprite> myChessmans;
	Hashtable<Integer, ChessmanSprite> rivalChessmans;
	Hashtable<Integer, PropSprite> props;
	boolean player;

	// used to calculate the weight
	private final int W_LARGESIZE = 5;
	private final int W_MEDIUM_SIZE = 2;
	private final int W_SMALLSIZE = 1;

	private final float W_SCALE = 0.2f;
	private final float mPixelToMeterRatio = 32.0f;

	/*
	 * private final float L_LinearDamping = 4.0f; private final float
	 * M_LinearDamping = 2.5f; private final float S_LinearDamping = 1.0f;
	 */
	// calculate from my test. damping may equal f
	private final float L_LinearDamping = 1.52f;
	private final float M_LinearDamping = 0.95f;
	private final float S_LinearDamping = 0.38f;

	// the m of the chess
	private final float mass = 3.0f;

	private final int halfScreenWidth = StartActivity.CAMERA_WIDTH / 2;
	private final int halfScreenHeight = StartActivity.CAMERA_HEIGHT / 2;

	// hinge 's property
	private final int hingeWidth = 38;
	private final int hingeHeight = 4;
	// Array to carry the Hinge's 4 point
	private final Vector2 L_Hinge[] = {
			new Vector2(halfScreenWidth - 75, halfScreenHeight - 2),
			new Vector2(halfScreenWidth - 75 + hingeWidth, halfScreenHeight - 2),
			new Vector2(halfScreenWidth - 75, halfScreenHeight + 2),
			new Vector2(halfScreenWidth - 75 + hingeWidth, halfScreenHeight + 2) };
	private final Vector2 R_Hinge[] = {
			new Vector2(halfScreenWidth + 75, halfScreenHeight - 2),
			new Vector2(halfScreenWidth + 75 +hingeWidth, halfScreenHeight - 2),
			new Vector2(halfScreenWidth + 75, halfScreenHeight + 2),
			new Vector2(halfScreenWidth + 75 + hingeWidth, halfScreenHeight + 2) };
	
	//size from scale to pix
	private final int scaleToPix = 60;
	

	public AIController(boolean player) {
		this.myChessmans = new Hashtable();
		this.rivalChessmans = new Hashtable();
		this.props = null;
		this.player = player;
	}

	public void print(Hashtable<Integer, ChessmanSprite> chessmans,
			Hashtable<Integer, PropSprite> props) {
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

			/*
			 * System.out.println("id:" + chessman.chessmanID + "---" +
			 * chessman.body.getPosition().x + "&&" +
			 * chessman.body.getPosition().y);
			 */
		}

		this.props = props;

		// not useful test for ai
		// myChessmans.get(new Integer(20)).body.applyLinearImpulse(impulse,
		// myChessmans.get(new Integer(20)).body.getPosition());
		this.calculate();

	}

	public void calculate() {
		Enumeration<Integer> en1 = myChessmans.keys();
		Enumeration<Integer> en2 = rivalChessmans.keys();

		System.out.println("en1.size:" + myChessmans.size());
		System.out.println("en2.size:" + rivalChessmans.size());

		// canBounceOff(myChessmans.get(new Integer(16)),
		// rivalChessmans.get(new Integer(11)));

		// used to get the detail position
		// ChessmanSprite chess = rivalChessmans.get(new Integer(1));
		// System.out.println("x:"+chess.getPosition().x);
		// System.out.println("y:"+chess.getPosition().y);

		for (Iterator it = myChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			System.out.println("my id:" + key.intValue());
			
			for (Iterator it2 = rivalChessmans.keySet().iterator(); it2
					.hasNext();) {
				Integer key2 = (Integer) it2.next();
				ChessmanSprite rivalChessman = rivalChessmans.get(key2);
				
				System.out.println("rival id:" + key2.intValue());
				//check if will touch the hinge
				if(this.checkBumpHinge(myChessman, rivalChessman))
				{
					System.out.println("Hinge");
					continue;
				}
				
				//check if will kick the rival chess off
				if (canBounceOff(myChessman, rivalChessman)) {
					System.out.println("can!");
				} 
			}
		}

		/*
		 * while (en1.hasMoreElements()) { Integer current = (Integer)
		 * en1.nextElement(); // ChessmanSprite myChessman =
		 * myChessmans.get((Integer) en1 // .nextElement()); ChessmanSprite
		 * myChessman = myChessmans.get(current);
		 * System.out.print(current.intValue()); while (en2.hasMoreElements()) {
		 * ChessmanSprite rivalChessman = rivalChessmans.get((Integer) en2
		 * .nextElement()); // if can throw away the chess , than calculate the
		 * weight and // minus the weight of own position // final float
		 * pixelToMeterRatio = this.mPixelToMeterRatio; //
		 * shape.setPosition(position.x * pixelToMeterRatio - //
		 * this.mShapeHalfBaseWidth, position.y * pixelToMeterRatio - //
		 * this.mShapeHalfBaseHeight);
		 * 
		 * if (canBounceOff(myChessman, rivalChessman)) {
		 * System.out.println("can!"); } else System.out.println("cannot!"); } }
		 */

	}

	/*
	 * ---------------------------->x | | p1 | | p2 | | y
	 */

	public boolean canBounceOff(ChessmanSprite from, ChessmanSprite to) {
		boolean result = false;
		float mSpeed = 33.0f;
		float mAngle = 0.0f;
		float mDamping = 0.0f;
		if (from.getScale() == ChessmanSprite.SMALL_SIZE) {
			mSpeed *= 0.63f / 33 * 45;
			mDamping = this.S_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.LARGE_SIZE) {
			mSpeed *= 1.6f / 33 * 45;
			mDamping = this.L_LinearDamping;
		} else if (from.getScale() == ChessmanSprite.MEDIUM_SIZE) {
			mSpeed *= 1.3f / 33 * 45;
			mDamping = this.M_LinearDamping;
		}

		float tan = (to.getPosition().y - from.getPosition().y)
				/ (to.getPosition().x - from.getPosition().x);

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

		Vector2 impulse = new Vector2(mSpeed * (float) Math.sin(mAngle), mSpeed
				* -(float) Math.cos(mAngle));

		float distance_in_box2d = getDistance(from, to)
				/ this.mPixelToMeterRatio;
		// System.out.println("distance:" + distance_in_box2d * 32);
		mSpeed = mSpeed / this.mPixelToMeterRatio;
		// s = ((vt)^2-(v0)^2)/2a
		float v_collide = (float) Math.sqrt(mSpeed * mSpeed - 2 * mDamping
				* distance_in_box2d / mass);
		// System.out.println("V:" + v_collide);

		if (v_collide == Float.NaN)
			return false;
		// float distance_to_border = (float) (to.getPosition().x /
		// Math.abs(Math.cos(mAngle/180*Math.PI)));

		float distance_can_move = this.getMoveDistance(to, v_collide)
				* this.mPixelToMeterRatio;

		result = this.checkOut(to, distance_can_move, mAngle);
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

	//true if will touch
	public boolean checkBumpHinge(ChessmanSprite from, ChessmanSprite to) {
		boolean result = false;
		
		float k = (to.getPosition().y-from.getPosition().y)/(to.getPosition().x - from.getPosition().x);
		float a = k;
		float c = -k*from.getPosition().x+from.getPosition().y;
		
		//should know the two chess are in the same side
		boolean inSameSide = false;
		if ((to.getPosition().y-this.halfScreenHeight)*(from.getPosition().y-this.halfScreenHeight) < 0)
			inSameSide = false;
		else
			inSameSide = true;
		
		//check hinge   if both chess are in same side than should check the --TO-- chess, else check the FROM chess
		for(int i = 0 ; i < 4 ; i ++)
		{
			if(inSameSide == false)
			{
				if(getPointLineDistance(L_Hinge[i], a, -1, c) < from.getScale()*this.scaleToPix ||getPointLineDistance(R_Hinge[i], a, -1, c) < from.getScale()*this.scaleToPix  )
					return true;
			}
			else
			{
				if(getPointLineDistance(L_Hinge[i], a, -1, c) < to.getScale()*this.scaleToPix|| getPointLineDistance(R_Hinge[i], a, -1, c) < to.getScale()*this.scaleToPix )
					return true;
			}
				
		}
		
		return result;
	}
	
	//point: (x,y)    line : ax+by+c = 0
	public float getPointLineDistance(Vector2 point, float a, float b, float c)
	{
		return (float) Math.abs((a * point.x + b * point.y + c)
				/ Math.sqrt(a * a + b * b));
	}

}
