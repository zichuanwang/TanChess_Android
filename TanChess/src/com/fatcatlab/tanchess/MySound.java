package com.fatcatlab.tanchess;

import java.io.IOException;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.util.Debug;

public class MySound {
	public Sound startSound;
	public Sound turnSound;
	public Sound hitStoneSound;
	public Sound hitHingeSound;
	public Sound selectSound;
	public Sound dropSound;
	public Sound fireSound;
	public Sound powerUpSound;
	public Sound loseSound;
	public Sound winSound;
	public Sound teleportSound;
	public Sound teleportEffectSound;
	public Sound changeSound;
	public Sound changeEffectSound;
	public Sound clickSound;
	
	public void loadSoundResource(Engine engine) {
		SoundFactory.setAssetBasePath("sound/");
		try {
			startSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "start.wav");
			turnSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "myturn.wav");
			hitHingeSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "hithinge.wav");
			hitStoneSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "hitstone.wav");
			selectSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "select.wav");
			dropSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "drop.wav");
			fireSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "fire.wav");
			powerUpSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "powerup.wav");
			loseSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "lose.wav");
			winSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "win.wav");
			teleportSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "teleport.wav");
			teleportEffectSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "teleport_ef.wav");
			changeSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "change.wav");
			changeEffectSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "change_ef.wav");
			clickSound = SoundFactory.createSoundFromAsset(engine.getSoundManager(), StartActivity.Instance, "click.wav");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}
}
