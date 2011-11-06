package com.fatcatlab.tanchess;

import java.io.Serializable;

public class BTMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 454678138249031645L;

	public enum PacketCodes {
		// NETWORK_ACK, // no packet
		// NETWORK_COINTOSS, // decide who is going to be the server
		// RIVAL_DEVICE_TYPE,
		CHESSMAN_SELECT_EVENT, // select one chessman
		CHESSMAN_MOVE_EVENT, // �ƶ�
		PLAY_SOUND_EVENT, CHESSMAN_CHANGE_EVENT, // prop ��
		CHESSMAN_ENLARGE_EVENT, // prop �Ŵ�
		CHESSMAN_COLLISION_EVENT,
		PROP_SELECT_EVENT, PROP_SHOW_EVENT, RESTART_REQUEST, REPLAY_REQUEST, RESTART_RESPOND, REPLAY_RESPOND, TOUCH_CANCEL_EVENT, CHANGE_TURN_EVENT, PACKET_NONE
	};

	//public int packetID;
	//public int ChessmanID;
	//public float Position_x;
	//public float Position_y;
	//public float Angle;
	//public Vector2 ChessmanImpulse;
	public PacketCodes packetCodes;
	public BaseMessage baseMessage;

	public BTMessage() {
		this.packetCodes = PacketCodes.PACKET_NONE;
	}
	
}


