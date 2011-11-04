package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.Hashtable;

import com.badlogic.gdx.math.Vector2;

public class AIController {

	Hashtable<Integer, ChessmanSprite> myChessmans;
	Hashtable<Integer, ChessmanSprite> rivalChessmans;
	Hashtable<Integer, PropSprite> props;
	boolean player;
	
	private final int W_LARGESIZE = 5;
	private final int W_MEDIUM_SIZE = 2;
	private final int W_SMALLSIZE = 1;
	
	private final float W_SCALE = 0.2f;
	

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
			
			/*System.out.println("id:" + chessman.chessmanID + "---"
					+ chessman.body.getPosition().x + "&&"
					+ chessman.body.getPosition().y);
					*/
		}
		
		this.props = props;
		
		// not useful test for ai
		float mSpeed = 50;
		float angle = 90;
		Vector2 impulse = new Vector2(mSpeed
				* (float) Math.sin(angle / 180 * Math.PI), mSpeed
				* -(float) Math.cos(angle / 180 * Math.PI));
		myChessmans.get(new Integer(20)).body.applyLinearImpulse(impulse,
				myChessmans.get(new Integer(20)).body.getPosition());
	}

	public void calculate() {
		Enumeration<Integer> en1 = myChessmans.keys();
		Enumeration<Integer> en2 = rivalChessmans.keys();
		while (en1.hasMoreElements()) {
			ChessmanSprite myChessman = myChessmans.get((Integer) en1
					.nextElement());
			while (en2.hasMoreElements()) {
				ChessmanSprite rivalChessman = rivalChessmans.get((Integer) en2
						.nextElement());
				//if can throw away the chess , than calculate the weight and minus the weight of own position
			}
		}

	}

}
