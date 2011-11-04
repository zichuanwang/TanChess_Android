package com.fatcatlab.tanchess;

import java.util.ArrayList;

import org.anddev.andengine.entity.scene.menu.animator.BaseMenuAnimator;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;

public class PositionalMenuAnimator extends BaseMenuAnimator {

    /** 
     * Sets the menu buttons vertically downward starting at position 0,0
     */
    public PositionalMenuAnimator(){
        super();
        this._baseX = 0;
        this._baseY = 0;
    }

    /**
     * Sets the menu buttons vertically downward starting at a set pX,pY
     */
    public PositionalMenuAnimator(final float pX, final float pY){
        super();
        this._baseX = pX;
        this._baseY = pY;
    }

    /**
     * Sets the menu buttons vertically downward starting at a set pX,pY with a set line spacing
     */
    public PositionalMenuAnimator(final float pX, final float pY, final float pLineSpacing){
        super(pLineSpacing);
        this._baseX = pX;
        this._baseY = pY;
    }

    // ===========================================================
    // Fields
    // ===========================================================

    private float _baseX;
    private float _baseY;

    // ===========================================================
    // Inherited Methods
    // ===========================================================

    @Override
    public void prepareAnimations(ArrayList<IMenuItem> pMenuItems,
            float pCameraWidth, float pCameraHeight) {}

    @Override
    public void buildAnimations(ArrayList<IMenuItem> pMenuItems,
            float pCameraWidth, float pCameraHeight) {

        float offsetX = _baseX;
        float offsetY = _baseY;

        final int menuItemCount = pMenuItems.size();
        for(int i = 0; i < menuItemCount; i++) {
            final IMenuItem menuItem = pMenuItems.get(i);

            menuItem.setPosition(offsetX , offsetY);

            offsetY += menuItem.getHeight() + this.mMenuItemSpacing;
        }

    }
}
