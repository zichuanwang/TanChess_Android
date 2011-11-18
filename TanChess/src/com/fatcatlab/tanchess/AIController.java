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
				this.doMoveAction(this.from, this.to, maxSpeed * 1.0f);
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
	Hashtable<Integer, PropSprite> myProps;
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
	private final float attack_distance_inaccuracy = 4.0f;
	
	//边界冗余 
	private final int PowerUpBorderRedundancy = 10 ; 
	
	private boolean canPowerUp = false;
	private boolean canForbid = false;
	private boolean canEnlarge = false;
	private boolean canExchange = false;
	private int mPlayerValue = 0;
	private int usedProp = 0;
	
	//forbid记录动作的action
	private boolean isInForbidMode = false;
	private ChessmanSprite chessmanInForbidMode = null;
	
	public AIController(boolean player) {
		this.myChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.rivalChessmans = new Hashtable<Integer, ChessmanSprite>();
		this.actionArray = new Vector<ActionStruct>();
		this.myProps = new Hashtable<Integer, PropSprite>();
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
		en = props.keys();
		while(en.hasMoreElements()){
			Integer key = (Integer) en.nextElement();
			PropSprite prop = props.get(key);
			if (prop.group == this.player)
				this.myProps.put(key, prop);
		}
		this.mPlayerValue = value;
		usedProp = this.createUseProp();
	}

	public void simulate() {
		this.fillPropState();
		this.calculate();
	}

	protected void calculate() {
		if(shouldCalculate())
		{
			useProp();
			calculateDefence();	
			calculateAttack();
			doAction();
		}else{
			ActionStruct as = new ActionStruct(this.chessmanInForbidMode, this.getLargestRivalChessman().getPosition());
			as.actionType = ATTACK_ACTION;						
			as.doAction();
			isInForbidMode = false;
		}
	}
	
	protected boolean shouldCalculate(){
		if( !isInForbidMode || (getLargestRivalSize()!= ChessmanSprite.LARGE_SIZE 
				|| (this.chessmanInForbidMode == null) ))
			return true;
		else if(this.chessmanInForbidMode.isDead == true)
			return true;
		else
			return false;
	}
	
	protected void useProp(){
		if(canForbid == true && this.usedProp == PropSprite.FORBID)
		{
			if( ForbidProcess() )
			{
				this.usedProp = createUseProp();
				mPlayerValue -= PropSprite.FORBID_NEED_SCORE;
			}
		}
		else if(canEnlarge == true && this.usedProp == PropSprite.ENLARGE)
		{
			if (EnlargeProcess())
			{
				this.usedProp = createUseProp();
				mPlayerValue -= PropSprite.ENLARGE_NEED_SCORE;
			}
		}
		else if(canExchange == true && this.usedProp == PropSprite.CHANGE)
		{
			if (ExchangeProcess())
			{
				mPlayerValue -= PropSprite.CHANGE_NEED_SCORE;
				this.usedProp = createUseProp();
			}
		}
		if( mPlayerValue < PropSprite.POWERUP_NEED_SCORE )
			canPowerUp = false;
	}
	
	protected boolean ForbidProcess(){
		ChessmanSprite rivalChessman = getLargestRivalChessman();
		if(rivalChessman == null)
			return false;
		if(rivalChessman.getScale() != ChessmanSprite.LARGE_SIZE)
			return false;
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if ( checkBumpHinge(myChessman, rivalChessman) )
				continue;
			if ( !checkInLine(myChessman, rivalChessman, false)){
				//如果打不出，并且加力也打不出，去判断是否两次打能够打出
				if ( !canBounceOff(myChessman, rivalChessman, false) && !canBounceOff(myChessman, rivalChessman, true) ){
					if ( canBounceOffTwoTimes(myChessman, rivalChessman) )
					{
						this.isInForbidMode = true;
						this.chessmanInForbidMode = myChessman;
						ActionStruct as = new ActionStruct(myChessman, rivalChessman.getPosition());
						as.actionType = ATTACK_ACTION;						
						as.point = 100000;
						actionArray.add(as);
						workToDoOnForbid();
						return true;
					}
				}
			} else {
				//TODO 斜线能不能打掉。
			}
		}
		return false;
	}
	
	protected boolean EnlargeProcess(){
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = myChessmans.get(key);
			if(chessman.getScale() == ChessmanSprite.MEDIUM_SIZE && ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH,
					ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, chessman.getPosition()))
			{
				workToDoOnEnlarge(chessman);
				return true;
			} else if(chessman.getScale() == ChessmanSprite.MEDIUM_SIZE && ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH,
					ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, chessman.getPosition()) && random(25)){
				workToDoOnEnlarge(chessman);
				return true;
			} else if(chessman.getScale() == ChessmanSprite.SMALL_SIZE && (this.getLargestRivalSize() == ChessmanSprite.SMALL_SIZE)) {
				if ( ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH,
					ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, chessman.getPosition()) && random(50))
				{
					workToDoOnEnlarge(chessman);
					return true;
				}else if ( ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH,
					ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, chessman.getPosition()) && random(25))
				{
					workToDoOnEnlarge(chessman);
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean ExchangeProcess(){
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = rivalChessmans.get(key);
			if( chessman.getScale() == ChessmanSprite.LARGE_SIZE )
			{
				if(ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH, 
						ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, chessman.getPosition() )) 
				{
					workToDoOnExchange(chessman);
					return true;
				}else if(ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH, 
						ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, chessman.getPosition()) && this.random(50))
				{
					workToDoOnExchange(chessman);
					return true;
				}else if(rivalChessmans.size() <= 3 && this.random(50)){
					workToDoOnExchange(chessman);
					return true;
				}
			}else if( chessman.getScale() == ChessmanSprite.MEDIUM_SIZE ){
				if(ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH, 
						ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, chessman.getPosition()) && this.random(20) ) 
				{
					workToDoOnExchange(chessman);
					return true;
				}
			}
		}
		return false;
	}
	
	protected void workToDoOnExchange(ChessmanSprite chessman){
		this.propAnimation(PropSprite.CHANGE);
		chessman.exchange();
		rivalChessmans.remove(new Integer(chessman.chessmanID));
		myChessmans.put(new Integer(chessman.chessmanID), chessman);
	}
	
	protected void workToDoOnEnlarge(ChessmanSprite chessman){
		this.propAnimation(PropSprite.ENLARGE);
		chessman.changeSize();
	}
	
	protected void workToDoOnForbid(){
		this.propAnimation(PropSprite.FORBID);
	}
	
	protected void fillPropState(){
		canPowerUp = false;
		canForbid = false;
		canEnlarge = false;
		canExchange = false;
		if(mPlayerValue >= PropSprite.POWERUP_NEED_SCORE)
			canPowerUp = true;
		if(mPlayerValue >= PropSprite.FORBID_NEED_SCORE)
			canForbid = true;
		if(mPlayerValue >= PropSprite.ENLARGE_NEED_SCORE)
			canEnlarge = true;
		if(mPlayerValue >= PropSprite.CHANGE_NEED_SCORE)
			canExchange = true;
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
					if( !hitHinge ){
						boolean hitOtherChessman = checkInLine(myChessman, rivalChessman ,true);
						if (!hitOtherChessman) {
							this.createActionStruct(myChessman, rivalChessman);
						}
						else{
							//Log.d("CAN BOUNCE OFF", "but hitHing:"+hitHinge+" hitOtherChessman:"+hitOtherChessman);
						}
					}
				}else if(this.canPowerUp && rivalChessman.getScale() != ChessmanSprite.SMALL_SIZE){
					//TODO powerup的算法：
					boolean canBounceOffWithPower = canBounceOff(myChessman, rivalChessman, true);
					if(canBounceOffWithPower){
						boolean hitHinge = checkBumpHinge(myChessman, rivalChessman);
						if (!hitHinge){
							boolean hitOtherChessman = checkInLine(myChessman, rivalChessman, true);
							if (!hitHinge && !hitOtherChessman) {
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
		//Log.d("ATTACK POINT:", ""+as.point );
		return as;
	}
	
	protected ActionStruct createActionStruct(ChessmanSprite myChessman, ChessmanSprite rivalChessman , ChessmanSprite _rivalChessman){
		int myPoint = myChessman.calculateValue();
		int rivalPoint1 = rivalChessman.calculateValue();
		int rivalPoint2 = _rivalChessman.calculateValue();
		int rivalPoint = (int) ((rivalPoint1+rivalPoint2) * 0.6f);
		int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, rivalChessman.getPosition());
		Vector2 attackPos = getMidpoint(rivalChessman.getPosition(), _rivalChessman.getPosition());
		ActionStruct as = new ActionStruct(myChessman, attackPos );
		as.actionType = ATTACK_ACTION;
		as.point = myFinalPoint - myPoint + rivalPoint;
		//Log.d("CAN BOUNCE OFF", "self:" + myChessman.chessmanID + " rival:" + rivalChessman.chessmanID
		//		+ " myPoint:"+myPoint+" rivalPoint:"+rivalPoint+" myFinalPoint:"+myFinalPoint+" sumPoint:"+as.point);
		actionArray.add(as);
		//Log.d("ATTACK POINT:", ""+as.point );
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
			//Log.d("CAN BOUNCE OFF", "type:"+as.actionType+" point"+as.point+" from:"+as.from.chessmanID);
			if (as.point >= maxPoint) {
				selectedAS = as;
				maxPoint = as.point;
			} 
		}
		//Log.d("CAN BOUNCE OFF", "decide!----self:" + selectedAS.from.chessmanID + " rival:" + toDo.to.chessmanID);
		if(selectedAS.usePowerUp == true)
		{
			propAnimation(PropSprite.POWERUP);
		}
		selectedAS.doAction();
		actionArray.clear();
	}
	
	protected void propAnimation(int type){
		Enumeration<Integer> en = myProps.keys();
		while(en.hasMoreElements()){
			Integer key = (Integer)en.nextElement();
			PropSprite prop = myProps.get(key);
			if( prop.category ==  type ){
				if( this.player == Brain.GROUP1 ){
					prop.gameScene.getmBrain().setmPlayer1Score(prop.gameScene.getmBrain().getmPlayer1Score() - prop.score);
				}else{
					prop.gameScene.getmBrain().setmPlayer2Score(prop.gameScene.getmBrain().getmPlayer2Score() - prop.score);
				}
				prop.func(true);
				break;
			}
		}
	}

	protected boolean canBounceOff(ChessmanSprite from, ChessmanSprite to , boolean powerUp) {
		return canBounceOff(from.getPosition(), to.getPosition(), from.getScale(), to.getScale(), null , null, powerUp);
	}
	
	
	/*
	 * 检验两次是否能够打掉 
	 */
	protected boolean canBounceOffTwoTimes(ChessmanSprite from, ChessmanSprite to)
	{
		Vector2 fromFinalPos = new Vector2();
		Vector2 toFinalPos = new Vector2();
		canBounceOff(from.getPosition(), to.getPosition(), from.getScale(), to.getScale(), fromFinalPos, toFinalPos, false);
		if(canBounceOff(fromFinalPos, toFinalPos, from.getScale(), to.getScale(), fromFinalPos, toFinalPos, false)){
			return true;
		}
		return false;
	}
	
	protected boolean canBounceOff(Vector2 fromPos, Vector2 toPos, float fromSize , float toSize ,Vector2 fromFinalPos, Vector2 toFinalPos, boolean powerUp){
		boolean result = false;
		float impulse = 0;
		float fromDamping = 0.0f;
		float toDamping = 0.0f;
		if (fromSize == ChessmanSprite.SMALL_SIZE) {
			impulse = this.MAX_S_SPEED;
			fromDamping = this.S_LinearDamping;
		} else if (fromSize == ChessmanSprite.LARGE_SIZE) {
			impulse = this.MAX_L_SPEED;
			fromDamping = this.L_LinearDamping;
		} else if (fromSize == ChessmanSprite.MEDIUM_SIZE) {
			impulse = this.MAX_M_SPEED;
			fromDamping = this.M_LinearDamping;
		}
		if (toSize == ChessmanSprite.SMALL_SIZE) {
			toDamping = this.S_LinearDamping;
		} else if (toSize == ChessmanSprite.LARGE_SIZE) {
			toDamping = this.L_LinearDamping;
		} else if (toSize == ChessmanSprite.MEDIUM_SIZE) {
			toDamping = this.M_LinearDamping;
		}
		float speed = impulse / this.mass;
		if(powerUp)
			speed *= 1.4f;
		float fromR = fromSize * 26;
		float toR = toSize * 26;
		float from2ToDistance = getDistance(fromPos, toPos);
		float distance_in_box2d = (from2ToDistance - fromR - toR) / mPixelToMeterRatio;
		float distance_try = 0.0f;
		while (speed > attack_speed_inaccuracy && distance_in_box2d > distance_try) {
			distance_try += speed * this.STEPDT;
			speed *= 1.0f - fromDamping * this.STEPDT;
		}
		if (distance_in_box2d >= distance_try || speed <= attack_speed_inaccuracy)
			return false;
		float distance_can_move = this.getMoveDistance(speed, toDamping) * mPixelToMeterRatio;
		float coefficient = 1 / from2ToDistance;
		Vector2 from2ToVec = new Vector2(toPos.x - fromPos.x, 
				toPos.y - fromPos.y);
		Vector2 impulseVec = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
		if(fromFinalPos != null && toFinalPos != null){
			toFinalPos.x = getToChessmanFinalPos(toPos, distance_can_move, impulseVec).x;
			toFinalPos.y = getToChessmanFinalPos(toPos, distance_can_move, impulseVec).y;
			fromFinalPos.x = getFromChessmanFinalPos( toPos, toR+fromR ,new Vector2(-impulseVec.x, -impulseVec.y)).x;
			fromFinalPos.y = getFromChessmanFinalPos( toPos, toR+fromR ,new Vector2(-impulseVec.x, -impulseVec.y)).y;
			result = this.checkOut(toFinalPos);
		}else {
			result = this.checkOut(toPos, distance_can_move, impulseVec);
		}
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

	protected boolean checkOut(Vector2 position, float distance, Vector2 vector) {
		boolean result = false;
		Vector2 finalPos = getMoveFinalPos(position, distance, vector);
		result = !ChessmanSprite.checkAlive(finalPos.x, finalPos.y);
		return result;
	}
	
	protected boolean checkOut(Vector2 position){
		return !ChessmanSprite.checkAlive(position.x, position.y);
	}
	
	protected Vector2 getToChessmanFinalPos(Vector2 position, float distance ,Vector2 direction){
		return getMoveFinalPos(position, distance, direction);
	}
	
	protected Vector2 getFromChessmanFinalPos(Vector2 toPos, float _r , Vector2 direction ){
		return getMoveFinalPos(toPos, _r, direction);
	}
	
	protected Vector2 getMoveFinalPos(Vector2 position, float distance ,Vector2 direction){
		float x = 0;
		float y = 0;
		x =  position.x + direction.x * distance;
		y =  position.y + direction.y * distance;
		return new Vector2(x,y);
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

	/*
	 * shouldConcernAboutRedundancy为true的时候去计算靠近边沿可以打的情况。不然不考虑
	 */
	protected boolean checkInLine(ChessmanSprite from, ChessmanSprite to, boolean shouldConcernAboutRedundancy) {
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
				if( this.checkChessmanInLine(from, to, currentPosition, currentR, checkR , from2ToDistance )) {
					//调整。如果to和current距离比较近并且都在边沿。可以考虑打出去
					if(( getDistance(to, current) - attack_distance_inaccuracy < ( from.getScale() * 2 + to.getScale() + current.getScale() )*26 ) 
							&&shouldConcernAboutRedundancy )
					{
						if ( isNearBorder(to , PowerUpBorderRedundancy) )
						{
							boolean shouldCreateActionStruct = true;
							//TODO 遍历其他的棋子。判断是否在之间，是否有挡着
							for( Iterator<Integer> iter = allChessmans.keySet().iterator() ; iter.hasNext(); ){
								Integer key2 = (Integer) iter.next();
								ChessmanSprite check = allChessmans.get(key2);
								if( !check.isDead || check ==from || check == to || check == current)
									continue;
								Vector2 toPoint = getMidpoint(to.getPosition(), current.getPosition());
								float distance = getDistance(from.getPosition(), toPoint);
								if( checkChessmanBetweenLine(from, toPoint , check.getPosition() , currentR, distance))
								{
									shouldCreateActionStruct = false;
									break;
								}
							}
							if(shouldCreateActionStruct)
								//create the structure
								this.createActionStruct( from, to ,current );
						}
					}else{
						return true;
					}
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
				}
			}
			ActionStruct as = new ActionStruct(myChessman, destinationVector);
			as.defenceSpeed = this.calculateSpeed(myChessman, destinationVector);
			as.point = ChessmanSprite.calculateValue(myChessman.value, bestPos) - myChessman.calculateValue();
			as.point *= 0.16f;
			as.actionType = DEFENCE_ACTION;
			if(as.defenceSpeed > as.maxSpeed)
				as.point = 1;
			if(needCheck) 
				as.point -= 10000;
			if(myChessman.getScale() == ChessmanSprite.SMALL_SIZE)
				as.point -= 5000;
			//Log.d("DEFENCE POINT:", ""+as.point );
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
	
	/*
	 * 判断棋子的位置(中间或者两边）返回需要考虑的半径
	 */
	protected float calculateNeededR(ChessmanSprite from, ChessmanSprite to, Vector2 currentPos){
		if( this.getIncludeAngle(from.getPosition(), to.getPosition(), currentPos ) < 0 )
			return from.getScale()*26;
		else
			return to.getScale()*26;
	}
	
	/*
	 * 是否已经靠近边缘了，redundancy 可以修改 
	 */
	protected boolean isNearBorder(ChessmanSprite chessman , int Redundancy){
		float x = chessman.getPosition().x;
		float y = chessman.getPosition().y;
		if( ChessmanSprite.checkAlive( x , y ) ){
			if (!ChessmanSprite.checkAlive(x-Redundancy, y -Redundancy) || !ChessmanSprite.checkAlive(x+Redundancy, y+Redundancy)){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * 获取中点
	 */
	public Vector2 getMidpoint(Vector2 vec1, Vector2 vec2){
		return new Vector2((vec1.x+vec2.x)/2, (vec1.y+vec2.y)/2);
	}
	
	/*
	 * 获取当前对方的最大的棋子的大小
	 */
	protected float getLargestRivalSize(){
		float largest = 0;
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(rivalChessmans.get(key).getScale() > largest)
				largest = rivalChessmans.get(key).getScale();
		}
		return largest;
	}
	
	/*
	 * 获取当前对方最大的棋子
	 */
	protected ChessmanSprite getLargestRivalChessman(){
		float largest = 0;
		ChessmanSprite _largest = null;
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(rivalChessmans.get(key).getScale() > largest)
			{
				largest = rivalChessmans.get(key).getScale();
				_largest = rivalChessmans.get(key);
			}
		}
		return _largest;
	}
	
	/*
	 * 初始化的时候确定使用哪个道具，在使用过之后也需要重新生成
	 */
	private int createUseProp(){
		if(random(20) && getLargestRivalSize() == ChessmanSprite.LARGE_SIZE && couldUseProp(PropSprite.FORBID))
			return PropSprite.FORBID;
		else if(random(40) && getLargestRivalSize() != ChessmanSprite.SMALL_SIZE && couldUseProp(PropSprite.CHANGE))
			return PropSprite.CHANGE;
		else if(couldUseProp(PropSprite.ENLARGE))
			return PropSprite.ENLARGE;
		else
			return -1;
	}
	
	/*
	 * 判断能量是否够某个功能
	 * 输入能量的类型，输出true&false
	 */
	private boolean couldUseProp(int type){
		if( type == PropSprite.FORBID)
		{
			return mPlayerValue+getMaxValue() >= PropSprite.FORBID_NEED_SCORE ? true:false;
		}
		else if( type == PropSprite.ENLARGE)
		{
			return mPlayerValue+getMaxValue() >= PropSprite.ENLARGE_NEED_SCORE ? true:false;
		}
		else if (type == PropSprite.CHANGE)
		{
			return mPlayerValue+getMaxValue() >= PropSprite.CHANGE_NEED_SCORE ? true:false;
		}else
			return false;
		
	}
	
	/*
	 * 获取所有的棋子可能的最大得分
	 */
	private int getMaxValue(){
		int result = 0;
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = myChessmans.get(key);
			result += chessman.value/2;
		}
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = rivalChessmans.get(key);
			result += chessman.value;
		}
		return result;
	}

	
}
