package com.fatcatlab.tanchess;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;

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
		public boolean isTargetLarge = false;
		public ActionStruct(ChessmanSprite f, Vector2 t) {
			this.from = f;
			this.to = t;
			maxSpeed = 0;
			if (this.from.isSmall()) {
				maxSpeed = MAX_S_SPEED;
			} else if (this.from.isLarge()) {
				maxSpeed = MAX_L_SPEED;
			} else if (this.from.isMedium()) {
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
			float distance = fromPos.dst(toPos);
			float coefficient = speed / distance;
			Vector2 from2ToVec = new Vector2(toPos.x - fromPos.x, toPos.y - fromPos.y);
			Vector2 impulse = new Vector2(coefficient * from2ToVec.x, coefficient * from2ToVec.y);
			StartActivity.Instance.mSound.fireSound.play();
			from.mBody.applyLinearImpulse(impulse, from.mBody.getPosition());
		}
	}
	
	public Brain owner;
	private Hashtable<Integer, ChessmanSprite> allChessmans;
	private Hashtable<Integer, ChessmanSprite> myChessmans;
	private Hashtable<Integer, ChessmanSprite> rivalChessmans;
	private Hashtable<Integer, PropSprite> myProps;
	private Vector<ActionStruct> actionArray;
	public boolean player;

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
	private final float hingeHalfWidth = 19.0f;
	private final float hingeHalfHeight = 2.0f;
	private final Vector2 L_Hinge = new Vector2(halfScreenWidth - 75, halfScreenHeight );
	private final Vector2 R_Hinge = new Vector2(halfScreenWidth + 75, halfScreenHeight );

	private final float attack_speed_inaccuracy = 1.0f;
	private final float defence_speed_inaccuracy = 0.01f;
	private final float attack_distance_inaccuracy = 4.0f;
	
	private boolean canPowerUp = false;
	private boolean canForbid = false;
	private boolean canEnlarge = false;
	private boolean canExchange = false;
	private int mPlayerValue = 0;
	private int usedProp = -1;
	
	//forbid记录动作的action
	private boolean isInForbidMode = false;
	private ChessmanSprite chessmanInForbidMode = null;
	
	//必须防御（最大子危险）
	private boolean isLargestInDanger = false;
	
	private boolean isShowingPropImage = false;
	private boolean isUsingEnlargeOrExchangeProp = false;
	
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
	}

	public void simulate() {
		this.fillPropState();
		this.calculate();
	}

	protected void calculate() {
		if(shouldCalculate()) {
			useProp();
			// 所有的模拟计算要在完成prop动画后进行
			// isShowingPropImage负责控制白色大图的显示
			// isUsingEnlargeOrExchangeProp负责控制白色大图显示之后Enlarge或Exchange道具的延迟显示
			final GameScene currentScene = StartActivity.Instance.getmMainScene().mGameScene;
			currentScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {	
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) {
					if(!isShowingPropImage && !isUsingEnlargeOrExchangeProp) {
						checkLargeChessmanDefence();
						if(!isLargestInDanger)
						{
							calculateAttack();
							calculateDefence();		
						}
						doAction();
						currentScene.unregisterUpdateHandler(pTimerHandler);
					}
				}
			}));
		} else {
			ActionStruct as = new ActionStruct(this.chessmanInForbidMode, this.getLargestRivalChessman().getPosition());
			as.actionType = ATTACK_ACTION;						
			as.doAction();
			isInForbidMode = false;
		}
	}
	
	protected boolean shouldCalculate(){
		if( !isInForbidMode || getLargestRivalSize()!= ChessmanSprite.LARGE_SIZE 
				|| this.chessmanInForbidMode == null)
			return true;
		else if(this.chessmanInForbidMode.isDead == true)
			return true;
		else
			return false;
	}
	
	protected void useProp(){
		if(this.usedProp == -1) {
			this.usedProp = createUseProp();
		}
		if(usedProp == PropSprite.FORBID && !this.getLargestRivalChessman().isLarge()) {
			this.usedProp = createUseProp();
		}
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
	
	protected boolean isInDanger(ChessmanSprite myChessman, Vector2 finalPos) {
		boolean result = false;
		Vector2 currentPosition = myChessman.getPosition();
		myChessman.setPosition(finalPos.x, finalPos.y);
		for (Iterator<Integer> it = rivalChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite rivalChessman = rivalChessmans.get(key);
			if(rivalChessman.isDead)
				continue;
			if(checkBumpHingeWhenAttack(rivalChessman, myChessman))
				continue;
			if (checkInLine(rivalChessman, myChessman, false))
				continue;
			if (canBounceOff(rivalChessman, myChessman, false)){
				// 如果不会碰到其他东西，并且能打出去，并且打的子的位置在安全的地带，那么就反击
				result = true;
			}
		}
		myChessman.setPosition(currentPosition.x, currentPosition.y);
		return result;
	}
	
	protected boolean ForbidProcess(){
		ChessmanSprite rivalChessman = getLargestRivalChessman();
		if(rivalChessman == null)
			return false;
		if(!rivalChessman.isLarge())
			return false;
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext(); ){
			Integer key = (Integer)iter.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if(myChessman.isDead)
				continue;
			if (checkBumpHingeWhenAttack(myChessman, rivalChessman))
				continue;
			if (!checkInLine(myChessman, rivalChessman, false)){
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
		ChessmanSprite bestChoice = this.getBestNotLargeChessman(myChessmans);
		workToDoOnEnlarge(bestChoice);
		return true;
	}
	
	protected ChessmanSprite getBestNotLargeChessman(Hashtable<Integer, ChessmanSprite> array) {
		ChessmanSprite bestChoice = null;
		for(Iterator<Integer> iter = array.keySet().iterator() ; iter.hasNext();){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = array.get(key);
			if(chessman.isDead)
				continue;
			if(bestChoice == null) {
				bestChoice = chessman;
				continue;
			}
			if(chessman.isLarge()) 
				continue;
			if(chessman.isMedium() && bestChoice.isSmall()) {
				bestChoice = chessman;
				continue;
			}
			if(chessman.isSmall() && bestChoice.isMedium())
				continue;
			if(ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH, 
					ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, chessman.getPosition()) ) {
				bestChoice = chessman;
			}
			else if(ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH, 
					ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, chessman.getPosition()) )  {
				if(!ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH, 
						ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, bestChoice.getPosition()) ) {
					bestChoice = chessman;
				}
			}
		}
		return bestChoice;
	}
	
	protected ChessmanSprite getBestRivalChessman() {
		ChessmanSprite largestRival = this.getLargestRivalChessman();
		if(this.getLargestRivalChessman().isLarge() || rivalChessmans.size() <= 1) {
			return largestRival;
		}
		return this.getBestNotLargeChessman(this.rivalChessmans);
	}
	
	protected boolean ExchangeProcess(){
		ChessmanSprite bestChoice = this.getBestRivalChessman();
		workToDoOnExchange(bestChoice);
		return true;
	}
	
	protected void workToDoOnExchange(ChessmanSprite chessman){
		this.propAnimation(PropSprite.CHANGE);
		final ChessmanSprite toExchangeChessman = chessman;
		isUsingEnlargeOrExchangeProp = true;
		final GameScene currentScene = StartActivity.Instance.getmMainScene().mGameScene;
		currentScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {	
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				if(!isShowingPropImage) {
					// 白色大图显示完以后才进行exchange
					toExchangeChessman.exchange();
					rivalChessmans.remove(new Integer(toExchangeChessman.chessmanID));
					myChessmans.put(new Integer(toExchangeChessman.chessmanID), toExchangeChessman);
					currentScene.unregisterUpdateHandler(pTimerHandler);
					currentScene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {	
						@Override
						public void onTimePassed(final TimerHandler pTimerHandler2) {
							isUsingEnlargeOrExchangeProp = false;
							currentScene.unregisterUpdateHandler(pTimerHandler2);
						}
					}));
				}
			}
		}));
	}
	
	protected void workToDoOnEnlarge(ChessmanSprite chessman){
		this.propAnimation(PropSprite.ENLARGE);
		final ChessmanSprite toEnlargeChessman = chessman;
		isUsingEnlargeOrExchangeProp = true;
		final GameScene currentScene = StartActivity.Instance.getmMainScene().mGameScene;
		currentScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {	
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				if(!isShowingPropImage) {
					// 白色大图显示完以后才进行enlarge
					toEnlargeChessman.changeSize();
					currentScene.unregisterUpdateHandler(pTimerHandler);
					currentScene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {	
						@Override
						public void onTimePassed(final TimerHandler pTimerHandler2) {
							isUsingEnlargeOrExchangeProp = false;
							currentScene.unregisterUpdateHandler(pTimerHandler2);
						}
					}));
				}
			}
		}));
	}
	
	protected void workToDoOnForbid(){
		// 已经在propAnimation中实现了changePlayers
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
			// 遍历自己的棋子
			Integer key = (Integer)it.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if (myChessman.isDead)
				continue;
			for (Iterator<Integer> it2 = rivalChessmans.keySet().iterator(); it2.hasNext();) {
				// 遍历对手的棋子
				Integer key2 = (Integer) it2.next();
				ChessmanSprite rivalChessman = rivalChessmans.get(key2);
				if (rivalChessman.isDead)
					continue;
				boolean hitHinge = checkBumpHingeWhenAttack(myChessman, rivalChessman);
				if(hitHinge)
					continue;
				boolean canBounceOffWithoutPower = canBounceOff(myChessman, rivalChessman, false);
				if (canBounceOffWithoutPower) {
					boolean hitOtherChessman = checkInLine(myChessman, rivalChessman, true);
					if (!hitOtherChessman) {
						this.createAttackActionStruct(myChessman, rivalChessman);
					}
				} else if(this.canPowerUp && !rivalChessman.isSmall()){
					//TODO PowerUp的算法：
					if(rivalChessman.isMedium() && this.usedProp != PropSprite.POWERUP)
						// 如果当前选择使用的道具不是PowerUp并且目标棋子是中型棋子 跳出
						if(rivalChessmans.size() > 1)
							// 针对对方只有一个棋子做优化
							continue;
					boolean canBounceOffWithPower = canBounceOff(myChessman, rivalChessman, true);
					if(canBounceOffWithPower) {
						boolean hitOtherChessman = checkInLine(myChessman, rivalChessman, false);
						if (!hitOtherChessman) {
							// 改为一定创建action 但是在最后对大子做筛选
							ActionStruct as = this.createAttackActionStruct(myChessman, rivalChessman);
							as.usePowerUp = true;
						}
					}
				}
			}
		}
	}
	
	protected ActionStruct createAttackActionStruct(ChessmanSprite myChessman, ChessmanSprite rivalChessman){
		int myPoint = myChessman.calculateAttackingValue();
		int rivalPoint = rivalChessman.calculateAttackedValue();
		int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, rivalChessman.getPosition());
		if(this.player == Brain.PLAYER1 && rivalChessman.getPosition().y > this.halfScreenHeight
				|| this.player == Brain.PLAYER2 && rivalChessman.getPosition().y < this.halfScreenHeight) {
			rivalPoint *= 4;
		}
		ActionStruct as = new ActionStruct(myChessman, rivalChessman.getPosition());
		if(rivalChessman.isLarge())
			as.isTargetLarge = true;
		as.actionType = ATTACK_ACTION;
		as.point = myFinalPoint - myPoint + rivalPoint * 5;
		if(myChessman.isLarge() && isInDanger(myChessman, rivalChessman.getPosition()))
			as.point -= 5000;
		actionArray.add(as);
		Log.d("CAN BOUNCE OFF", "from:"+myChessman.chessmanID+" to:"+rivalChessman.chessmanID+" type:attack"+" double:false"
				+" point:"+as.point);
		return as;
	}
	
	protected ActionStruct createDoubleAttackActionStruct(ChessmanSprite myChessman, ChessmanSprite rivalChessman1, ChessmanSprite rivalChessman2){
		int myPoint = myChessman.calculateAttackingValue();
		int rivalPoint1 = rivalChessman1.calculateAttackedValue();
		int rivalPoint2 = rivalChessman2.calculateAttackedValue();
		int rivalPoint = (rivalPoint1 + rivalPoint2) * 2;
		Vector2 attackPos = getMidpoint(rivalChessman1.getPosition(), rivalChessman2.getPosition());
		int myFinalPoint = ChessmanSprite.calculateValue(myChessman.value, attackPos);
		ActionStruct as = new ActionStruct(myChessman, attackPos);
		if(rivalChessman1.isLarge() || rivalChessman2.isLarge())
			as.isTargetLarge = true;
		as.actionType = ATTACK_ACTION;
		as.point = myFinalPoint - myPoint + rivalPoint * 5;
		if(myChessman.isLarge())
			as.point -= 5000;
		actionArray.add(as);
		Log.d("CAN BOUNCE OFF", "from:"+myChessman.chessmanID+" to:"+rivalChessman1.chessmanID+" type:attack"+" double:true"
				+"point:"+as.point);
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
			if(as.isTargetLarge) {
				// 如果目标是大子 一定攻击
				selectedAS = as;
				break;
			}
			if (as.point >= maxPoint) {
				selectedAS = as;
				maxPoint = as.point;
			}
		}
		if(selectedAS.usePowerUp == true)
		{
			propAnimation(PropSprite.POWERUP);
			usedProp = this.createUseProp();
		}
		selectedAS.from.setSelected();
		final ActionStruct todoAS = selectedAS;
		final GameScene currentScene = StartActivity.Instance.getmMainScene().mGameScene;
		currentScene.registerUpdateHandler(new TimerHandler(0.5f, true, new ITimerCallback() {	
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				todoAS.from.setUnselected();
				todoAS.doAction();
				actionArray.clear();
				currentScene.unregisterUpdateHandler(pTimerHandler);			
			}
		}));
	}
	
	protected void propAnimation(int type){
		Enumeration<Integer> en = myProps.keys();
		while(en.hasMoreElements()){
			Integer key = (Integer)en.nextElement();
			PropSprite prop = myProps.get(key);
			if( prop.category ==  type ){
				if( this.player == Brain.PLAYER1 ){
					owner.setmPlayer1Score(prop.gameScene.getmBrain().getmPlayer1Score() - prop.score);
				}else{
					owner.setmPlayer2Score(prop.gameScene.getmBrain().getmPlayer2Score() - prop.score);
				}
				prop.func(true);
				break;
			}
		}
		isShowingPropImage = true;
		final GameScene currentScene = StartActivity.Instance.getmMainScene().mGameScene;
		currentScene.registerUpdateHandler(new TimerHandler(2.0f, true, new ITimerCallback() {	
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				isShowingPropImage = false;
				currentScene.unregisterUpdateHandler(pTimerHandler);
			}
		}));
	}

	protected boolean canBounceOff(ChessmanSprite from, ChessmanSprite to , boolean powerUp) {
		return canBounceOffBase(from.getPosition(), to.getPosition(), from.getScale(), to.getScale(), null , null, powerUp, to.isLarge());
	}
	
	
	/*
	 * 检验两次是否能够打掉 
	 */
	protected boolean canBounceOffTwoTimes(ChessmanSprite from, ChessmanSprite to)
	{
		Vector2 fromFinalPos = new Vector2();
		Vector2 toFinalPos = new Vector2();
		boolean isToLarge = to.isLarge();
		canBounceOffBase(from.getPosition(), to.getPosition(), from.getScale(), to.getScale(), fromFinalPos, toFinalPos, false, isToLarge);
		if(canBounceOffBase(fromFinalPos, toFinalPos, from.getScale(), to.getScale(), fromFinalPos, toFinalPos, false, isToLarge)){
			return true;
		}
		return false;
	}
	
	protected boolean canBounceOffBase(Vector2 fromPos, Vector2 toPos, float fromSize , float toSize ,Vector2 fromFinalPos, Vector2 toFinalPos, boolean powerUp, boolean isToLarge){
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
		float from2ToDistance = fromPos.dst(toPos);
		// 对能否打出大子进行优化 
		if(isToLarge) {
			if(powerUp)
				from2ToDistance += toR;
			else
				from2ToDistance += toR * 3;
		}
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

	protected float getMoveDistance(ChessmanSprite chessman, float v) {
		float result = 0.0f;
		if (chessman.isLarge()) {
			result = (v * v) / (2 * this.L_LinearDamping / this.mass);
		} else if (chessman.isMedium()) {
			result = (v * v) / (2 * this.M_LinearDamping / this.mass);
		} else if (chessman.isSmall()) {
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
	
	protected float getRealHingeR(ChessmanSprite from, ChessmanSprite to) {
		float realhingeR = hingeHalfWidth;
		float fromMinusHalfScreen = from.getPosition().y - halfScreenHeight;
		float toMinusHalfScreen = to.getPosition().y - halfScreenHeight;
		if(fromMinusHalfScreen * toMinusHalfScreen < 0)
			return hingeHalfWidth;
		if(fromMinusHalfScreen > 0) {
			fromMinusHalfScreen -= from.getScale() * 26 + hingeHalfHeight;
		}
		else {
			fromMinusHalfScreen += from.getScale() * 26 + hingeHalfHeight;
		}
		
		if(toMinusHalfScreen > 0) {
			toMinusHalfScreen -= to.getScale() * 26 + hingeHalfHeight;
		}
		else {
			toMinusHalfScreen += to.getScale() * 26 + hingeHalfHeight;
		}
		if(fromMinusHalfScreen * toMinusHalfScreen > 0) {
			if(Math.abs(fromMinusHalfScreen - toMinusHalfScreen) < ChessmanSprite.SMALL_SIZE * 26)
				realhingeR = hingeHalfHeight;
		}
		return realhingeR;
	}

	protected boolean checkBumpHingeWhenAttack(ChessmanSprite from, ChessmanSprite to) {
		boolean result = true;
		float from2ToDistance = from.getPosition().dst(to.getPosition());
		float realhingeR = this.getRealHingeR(from, to);
		float L_HingeR = this.calculateNeededR(from, to, L_Hinge);
		float R_HingeR = this.calculateNeededR(from, to, R_Hinge);
		if (!checkChessmanInLine(from, to.getPosition(), L_Hinge, realhingeR, L_HingeR, from2ToDistance)
				&& !checkChessmanInLine(from, to.getPosition(), R_Hinge, realhingeR, R_HingeR, from2ToDistance))
			result = false;
		return result;
	}
	
	protected boolean checkBumpHingeWhenDefence(ChessmanSprite from, Vector2 toPos){
		float from2ToDistance = from.getPosition().dst(toPos);
		if((checkChessmanBetweenLine(from, toPos, L_Hinge, hingeHalfWidth, from2ToDistance))  == false 
				&& (checkChessmanBetweenLine(from, toPos, R_Hinge, hingeHalfWidth , from2ToDistance) == false))
			return false;
		return true;
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
	
	protected boolean checkInLine(ChessmanSprite from, ChessmanSprite to, boolean shouldConcernAboutDouble){
		return checkInLineBase(from, to, null, shouldConcernAboutDouble);
	}
	
	protected boolean checkInLineWithException(ChessmanSprite from, ChessmanSprite to, ChessmanSprite exception, boolean shouldConcernAboutDouble){
		return checkInLineBase(from, to, exception, shouldConcernAboutDouble);
	}

	/*
	 * shouldConcernAboutDouble为true的时候去计算靠近边沿可以打的情况。不然不考虑
	 */
	protected boolean checkInLineBase(ChessmanSprite from, ChessmanSprite to, ChessmanSprite exception, boolean shouldConcernAboutDouble) {
		float from2ToDistance = from.getPosition().dst(to.getPosition());
		for (Iterator<Integer> it = allChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite current = allChessmans.get(key);
			if(current.isDead)
				continue;
			if (current != from && current != to && current != exception) {
				Vector2 currentPosition = current.getPosition();
				float checkR = this.calculateNeededR(from, to, currentPosition);
				float currentR = 26 * current.getScale();
				// 调整。如果to和current距离比较近并且都在边沿。可以考虑打出去。to和from必须都为对方的子
				if(shouldConcernAboutDouble) {
					boolean isToAndCurrentCloseEnough =  to.getPosition().dst(currentPosition) - attack_distance_inaccuracy < (from.getScale() * 2 + to.getScale() + current.getScale() ) * 26; 
					boolean isToAndCurrentBothRival = to.getGroup() != this.player && current.getGroup() != this.player;
					boolean isToAndCurrentBothNearBorder = isNearBorder(to, to.getScale() * 26) && isNearBorder(current, current.getScale() * 26);
					/*if(from.chessmanID == 28) {
						Log.d("CAN BOUNCE OFF", "chessman28---to:"+to.chessmanID+" current:"+current.chessmanID+" isToAndCurrentCloseEnough:"
								+isToAndCurrentCloseEnough+" isToAndCurrentBothRival:"+isToAndCurrentBothRival+" isToAndCurrentBothNearBorder:"+isToAndCurrentBothNearBorder);
					}*/
					if (isToAndCurrentCloseEnough && isToAndCurrentBothRival && isToAndCurrentBothNearBorder) {
						boolean existChessmanBetweenFromAndCurrent = this.checkInLineWithException(from, current, to, false);
						boolean existChessmanBetweenFromAndTo = this.checkInLineWithException(from, to, current, false);
						/*if(from.chessmanID == 28) {
							Log.d("CAN BOUNCE OFF", "chessman28---to:"+to.chessmanID+" current:"+current.chessmanID+" existChessmanBetweenFromAndCurrent:"
									+existChessmanBetweenFromAndCurrent+" existChessmanBetweenFromAndTo:"+existChessmanBetweenFromAndTo);
						}*/
						if(!existChessmanBetweenFromAndCurrent && !existChessmanBetweenFromAndTo)
						{
							this.createDoubleAttackActionStruct(from, to, current);
						}
					}
				}
				if(this.checkChessmanInLine(from, to, currentPosition, currentR, checkR, from2ToDistance)) {
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
		float current2ToDistance = currentPosition.dst(toVector);
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
		float from2ToDistance = chessman.getPosition().dst(toPos);
		float distance_in_box2d = from2ToDistance / mPixelToMeterRatio;
		float fromDamping = 0.0f;
		if (chessman.isSmall()) {
			fromDamping = this.S_LinearDamping;
		} else if (chessman.isLarge()) {
			fromDamping = this.L_LinearDamping;
		} else if (chessman.isMedium()) {
			fromDamping = this.M_LinearDamping;
		}
		float speed = this.calculateSpeedHelp(distance_in_box2d, fromDamping);
		return speed;
	}
	
	protected void calculateDefence() {
		calculateDefencePriority();
	}
	
	protected boolean LargeChessmanBeatBack(ChessmanSprite myChessman, ChessmanSprite rivalChessman){
		//如果不会碰到其他东西，并且能打出去，并且打的子的位置在安全的地带，那么就反击
		if( !checkBumpHingeWhenAttack(myChessman, rivalChessman) && !checkInLine(myChessman, rivalChessman, false)){
			if (canBounceOff(myChessman, rivalChessman, false))
			{
				if (ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH, ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, rivalChessman.getPosition())){
					createAttackActionStruct(myChessman, rivalChessman);
					return true;
				}else if(random(10) && ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH, ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, rivalChessman.getPosition())){
					createAttackActionStruct(myChessman, rivalChessman);
					return true;
				}
			}
			else if( canBounceOff(myChessman, rivalChessman, true) && rivalChessman.getScale() == ChessmanSprite.LARGE_SIZE ){
				if (ChessmanSprite.checkInZone(ChessmanSprite.HIGH_SAFETY_ZONE_WIDTH, ChessmanSprite.HIGH_SAFETY_ZONE_HEIGHT, rivalChessman.getPosition())){
					createAttackActionStruct(myChessman, rivalChessman);
					return true;
				}else if(random(50) && ChessmanSprite.checkInZone(ChessmanSprite.NORMAL_SAFETY_ZONE_WIDTH, ChessmanSprite.NORMAL_SAFETY_ZONE_HEIGHT, rivalChessman.getPosition())){
					createAttackActionStruct(myChessman, rivalChessman);
					return true;
				}
			}
		}
		return false;
	}
	
	protected void checkLargeChessmanDefence(){
		//TODO 防御最大子被打出去算法
		List<ChessmanSprite> largestList = getLargestMyChessman();
		if(largestList.size() == 0)
		{
			this.isLargestInDanger = false;
			return;
		}
		for(int i = 0 ; i < largestList.size() ; i++){
			ChessmanSprite myChessman = largestList.get(i);
			for(Iterator<Integer> it = rivalChessmans.keySet().iterator(); it.hasNext();) {
				Integer key = (Integer) it.next();
				ChessmanSprite rivalChessman = rivalChessmans.get(key);
				if(rivalChessman.isDead)
					continue;
				if( checkBumpHingeWhenAttack(rivalChessman, myChessman) )
					continue;
				if (checkInLine(rivalChessman, myChessman, false))
					continue;
				if (canBounceOff(rivalChessman, myChessman, false)){
					//如果不会碰到其他东西，并且能打出去，并且打的子的位置在安全的地带，那么就反击
					if ( !LargeChessmanBeatBack(myChessman, rivalChessman))
						calculateChessmanDefence(myChessman);
				}
				else if (canBounceOff(rivalChessman, myChessman, true)){
					//如果自己只有唯一一个大子了，则必须考虑，有2个或者以上大子的话则50的概率去考虑
					if( largestList.size() <= 1 || random(50))
						if ( !LargeChessmanBeatBack(myChessman,rivalChessman))
							calculateChessmanDefence(myChessman);
				}
			}
		}
		if(actionArray.size() != 0)
			this.isLargestInDanger = true;
		else
			this.isLargestInDanger = false;
	}
	
	protected void calculateChessmanDefence(ChessmanSprite myChessman){
		Vector2 bestPos = new Vector2(this.halfScreenWidth, this.halfScreenHeight);
		Vector2 destinationVector = getDefenceDirection(myChessman);
		boolean willHitOtherChess = false;
		boolean willHitHinge = false;
		for (Iterator<Integer> it2 = allChessmans.keySet().iterator(); it2.hasNext();) {
			Integer key2 = (Integer) it2.next();
			ChessmanSprite check = allChessmans.get(key2);
			float checkR = check.getScale() * 26;
			if(check == myChessman)
				continue;
			if (check.isDead )
				continue;
			float from2ToDistance = myChessman.getPosition().dst(check.getPosition());
			if( checkBumpHingeWhenDefence(myChessman, destinationVector) ){
				willHitHinge = true;
				break;
			}
			if(checkChessmanBetweenLine(myChessman, destinationVector, check.getPosition(), checkR, from2ToDistance) )
			{
				willHitOtherChess = true;
				break;
			}
		}
		ActionStruct as = new ActionStruct(myChessman, destinationVector);
		as.defenceSpeed = this.calculateSpeed(myChessman, destinationVector);
		as.point = myChessman.calculateDefenceValue(bestPos) - myChessman.calculateDefenceValue();
		as.actionType = DEFENCE_ACTION;
		if(as.defenceSpeed > as.maxSpeed)
			// 这个数值需要大于棋子在hinge之间滑动的point(目前是0)
			as.point = 1;
		if(willHitHinge) 
			as.point -= 10000;
		if(willHitOtherChess)
			as.point -= 1000;
		if(myChessman.isSmall())
			as.point -= 5000;
		Log.d("CAN BOUNCE OFF", "from:"+myChessman.chessmanID+" to:bestPos"+" type:defence"+" double:false"
				+"point:"+as.point);
		actionArray.add(as);
	}
	
	protected void calculateDefencePriority(){
		for (Iterator<Integer> it = myChessmans.keySet().iterator(); it.hasNext();) {
			Integer key = (Integer) it.next();
			ChessmanSprite myChessman = myChessmans.get(key);
			if (myChessman.isDead)
				continue;
			calculateChessmanDefence(myChessman);
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
			toPos = new Vector2(this.L_Hinge.x + this.hingeHalfWidth + selectedR, this.halfScreenHeight);
		else
			toPos = new Vector2(this.R_Hinge.x - this.hingeHalfWidth - selectedR, this.halfScreenHeight);
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
		//float from2ToDistance = from.getPosition().dst(to.getPosition());
		//float current2ToDistance = currentPos.dst(to.getPosition());
		float includeAngle = this.getIncludeAngle(from.getPosition(), to.getPosition(), currentPos );
		if( includeAngle < 0 ) {
			return from.getScale() * 26;
		}
		else {
			return to.getScale() * 26;
		}
	}
	
	/*
	 * 是否已经靠近边缘了，nearDistance可以修改 
	 */
	protected boolean isNearBorder(ChessmanSprite chessman , float nearDistance){
		float x = chessman.getPosition().x;
		float y = chessman.getPosition().y;
		if(ChessmanSprite.checkAlive(x , y)) {
			if (!ChessmanSprite.checkAlive(x-nearDistance, y-nearDistance) || !ChessmanSprite.checkAlive(x+nearDistance, y+nearDistance)){
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
		float largest = ChessmanSprite.SMALL_SIZE;
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(rivalChessmans.get(key).getScale() > largest)
				largest = rivalChessmans.get(key).getScale();
		}
		return largest;
	}
	
	/*
	 * 获取当前对方的最小的棋子的大小
	 */
	protected float getSmallestMySize(){
		float smallest = ChessmanSprite.LARGE_SIZE;
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(myChessmans.get(key).getScale() < smallest)
				smallest = myChessmans.get(key).getScale();
		}
		return smallest;
	}
	
	/*
	 * 获取当前对方最大的棋子
	 */
	protected ChessmanSprite getLargestRivalChessman(){
		float largestSize = 0;
		ChessmanSprite largestChessman = null;
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(rivalChessmans.get(key).getScale() > largestSize)
			{
				largestSize = rivalChessmans.get(key).getScale();
				largestChessman = rivalChessmans.get(key);
			}
		}
		return largestChessman;
	}
	
	/*
	 * 获取自己大子的队列
	 */
	protected List<ChessmanSprite> getLargestMyChessman(){
		List<ChessmanSprite> _largest = new LinkedList<ChessmanSprite>();
		for(Iterator<Integer> iter = myChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = iter.next();
			if(myChessmans.get(key).isLarge())
			{
				_largest.add(myChessmans.get(key));
			}
		}
		return _largest;
	}
	
	/*
	 * 初始化的时候确定使用哪个道具，在使用过之后也需要重新生成
	 */
	private int createUseProp(){
		int randomNumber = (int)(Math.random() * 100);
		int decidedPropToUse = -1;
		if(randomNumber < 30 && getLargestRivalSize() == ChessmanSprite.LARGE_SIZE && couldUseProp(PropSprite.FORBID)) {
			decidedPropToUse = PropSprite.FORBID;
		}
		else if(randomNumber < 60 && getLargestRivalSize() != ChessmanSprite.SMALL_SIZE && couldUseProp(PropSprite.CHANGE)) {
			decidedPropToUse = PropSprite.CHANGE;
		}
		else if(randomNumber < 90 && getSmallestMySize() != ChessmanSprite.LARGE_SIZE && couldUseProp(PropSprite.ENLARGE) 
				&& !(myChessmans.size() == 1 && this.getLargestMyChessman().size() != 0)) {
			decidedPropToUse = PropSprite.ENLARGE;
		}
		else {
			decidedPropToUse = PropSprite.POWERUP;
		}
		Log.d("PROP", "Decide Prop To Use:"+decidedPropToUse+" Random Number:"+randomNumber);
		return decidedPropToUse;
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
			if(chessman.isDead)
				continue;
			result += chessman.value;
		}
		for(Iterator<Integer> iter = rivalChessmans.keySet().iterator() ; iter.hasNext() ; ){
			Integer key = (Integer)iter.next();
			ChessmanSprite chessman = rivalChessmans.get(key);
			if(chessman.isDead)
				continue;
			result += chessman.value/2;
		}
		return result;
	}

	
}
