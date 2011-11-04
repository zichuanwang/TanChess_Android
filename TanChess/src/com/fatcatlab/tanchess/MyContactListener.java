package com.fatcatlab.tanchess;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;

public class MyContactListener implements ContactListener{
        
	public Body _bodyA, _bodyB;
	public GameScene gameScene;
	
	MyContactListener(GameScene scene) {
		super();
		gameScene = scene;
	}
    @Override
    public void endContact(Contact contact) {
    	Body body = contact.getFixtureA().getBody();
        Vector2 impulse_vec = body.getLinearVelocity();
    	body.setLinearVelocity(new Vector2(0.85f * impulse_vec.x, 0.85f * impulse_vec.y));
    	body = contact.getFixtureB().getBody();
    	impulse_vec = body.getLinearVelocity();
    	body.setLinearVelocity(new Vector2(0.85f * impulse_vec.x, 0.85f * impulse_vec.y));
    }
    @Override
    public void beginContact(Contact contact) {
    	if(contact.getFixtureA().getBody() != _bodyA && contact.getFixtureA().getBody() != _bodyB) {
    		StartActivity.Instance.mSound.hitStoneSound.play();  
        }
    	else {
    		StartActivity.Instance.mSound.hitHingeSound.play();
    	}
    }
}
