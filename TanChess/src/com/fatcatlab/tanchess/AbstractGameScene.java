package com.fatcatlab.tanchess;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.opengl.texture.region.BaseTextureRegion;
 
 
public abstract class AbstractGameScene extends Scene {
       
        protected Engine mEngine;
 
        public AbstractGameScene(int pLayerCount, Engine baseEngine) {
                super(pLayerCount);
                mEngine = baseEngine;
        }
       
        // ===========================================================
        // Inherited Methods
        // ===========================================================
 
        protected abstract void onLoadResources();
 
        protected abstract void onLoadScene();
       
        protected abstract void unloadScene();
 
        protected abstract void onLoadComplete();
       
        // ===========================================================
        // Methods
        // ===========================================================
 
        public void LoadResources(boolean loadSceneAutomatically){
                this.onLoadResources();
                if(loadSceneAutomatically){
                        this.onLoadScene();
                }
        }
       
        public void LoadScene(){
                this.onLoadScene();
        }
        
        protected float getCenterX(BaseTextureRegion tr) {
            	return (StartActivity.CAMERA_WIDTH - tr.getWidth()) / 2;
        }
        
        protected float getCenterY(BaseTextureRegion tr) {
        		return (StartActivity.CAMERA_HEIGHT - tr.getHeight()) / 2;
        }
}
