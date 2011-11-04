package com.fatcatlab.tanchess;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.util.GLHelper;

public class CDRect extends Shape {
  private final CDRectVertexBuffer vertexBuffer;
  private int filledMode = GL10.GL_TRIANGLE_FAN;
  private int segments;
  private float[] points;

  public CDRect(float pX, float pY, int seg, float[] vex) {
    super(pX, pY);
    segments = seg;
    points = vex;
    vertexBuffer = new CDRectVertexBuffer(segments, GL11.GL_STATIC_DRAW);
    BufferObjectManager.getActiveInstance().loadBufferObject(vertexBuffer);
    this.updateVertexBuffer();
  }

  @Override
  public float[] getSceneCenterCoordinates() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean collidesWith(IShape pOtherShape) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean contains(float pX, float pY) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public float[] convertSceneToLocalCoordinates(float pX, float pY) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public float[] convertLocalToSceneCoordinates(float pX, float pY) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void onUpdateVertexBuffer() {
    vertexBuffer.update(segments, points);
  }

  @Override
  protected CDRectVertexBuffer getVertexBuffer() {
    return vertexBuffer;
  }

  @Override
  protected boolean isCulled(Camera pCamera) {
    return false;
  }

  @Override
  protected void onInitDraw(final GL10 pGL) {
    super.onInitDraw(pGL);
    GLHelper.disableTextures(pGL);
    GLHelper.disableTexCoordArray(pGL);
  }

  @Override
  protected void drawVertices(GL10 gl, Camera pCamera) {
    gl.glDrawArrays(filledMode, 0, segments);
  }
  @Override
  public float getWidth() {
  	// TODO Auto-generated method stub
  	return 40;
  }

  @Override
  public float getHeight() {
  	// TODO Auto-generated method stub
  	return 40;
  }

  @Override
  public float getBaseWidth() {
  	// TODO Auto-generated method stub
  	return 40;
  }

  @Override
  public float getBaseHeight() {
  	// TODO Auto-generated method stub
  	return 40;
  }
}
 

