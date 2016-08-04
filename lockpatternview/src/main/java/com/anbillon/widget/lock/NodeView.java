package com.anbillon.widget.lock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * This class represents a node view in {@link LockPatternView}.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
@SuppressLint("ViewConstructor") final class NodeView extends View {
  private int centerX;
  private int centerY;
  private int radius;
  private float innerRadiusRate = 0.4f;
  private int colorInner;
  private int colorOutter;
  private int colorNodeOn;
  private Paint paint;
  private boolean isHighlight;
  private int nodeNumber;

  NodeView(Context context, int colorInner, int colorOutter, int colorNodeOn) {
    super(context);
    this.colorInner = colorInner;
    this.colorOutter = colorOutter;
    this.colorNodeOn = colorNodeOn;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);

    width = Math.min(width, height);
    radius = centerX = centerY = width / 2;
  }

  @Override protected void onDraw(Canvas canvas) {
    if (isHighlight) {
      /* inner circle */
      paint.setStyle(Paint.Style.FILL);
      paint.setColor(colorNodeOn);
      canvas.drawCircle(centerX, centerY, radius * innerRadiusRate, paint);
    } else {
      /* outter circle */
      paint.setStyle(Paint.Style.FILL);
      paint.setColor(colorOutter);
      canvas.drawCircle(centerX, centerY, radius, paint);
      /* inner circle */
      paint.setColor(colorInner);
      canvas.drawCircle(centerX, centerY, radius * innerRadiusRate, paint);
    }
  }

  void setNodeNumber(int number) {
    this.nodeNumber = number;
  }

  int getNodeNumber() {
    return nodeNumber;
  }

  void setHighlight(boolean isHighlight) {
    this.isHighlight = isHighlight;
    if (isHighlight) {
      startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.node_on));
    } else {
      clearAnimation();
    }

    invalidate();
  }

  boolean isHighlight() {
    return isHighlight;
  }

  int getCenterX() {
    return (getLeft() + getRight()) / 2;
  }

  int getCenterY() {
    return (getTop() + getBottom()) / 2;
  }
}
