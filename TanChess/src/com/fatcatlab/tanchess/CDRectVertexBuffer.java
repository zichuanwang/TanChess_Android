package com.fatcatlab.tanchess;

import org.anddev.andengine.opengl.util.FastFloatBuffer;
import org.anddev.andengine.opengl.vertex.VertexBuffer;

public class CDRectVertexBuffer extends VertexBuffer {

  public CDRectVertexBuffer(int segments, int pDrawType) {
    super(segments * 2, pDrawType);
  }

  void update(int segments, float[] points) {
    final int[] vertices = this.mBufferData;
    for(int i = 0; i < segments * 2; i++) {
    	vertices[i] = Float.floatToRawIntBits(points[i]);
    }

    final FastFloatBuffer buffer = this.getFloatBuffer();
    buffer.position(0);
    buffer.put(vertices);
    buffer.position(0);

    super.setHardwareBufferNeedsUpdate();
  }
}