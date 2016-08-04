package com.anbillon.widget.lock;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * A lock pattern view to draw pattern with gesture.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class LockPatternView extends ViewGroup {
  private int lineColor = 0x6600aaaa;
  private float lineWidth = 12;
  private int sideNodeCount = 3;
  private int colorInner = 0xff888888;
  private int colorOutter = 0xffdddddd;
  private int colorNodeOn = 0xff00aaaa;
  private boolean autoClear = true;
  private float nodeAreaExpand = 10;
  private int nodeSize = 100;

  private float currentX;
  private float currentY;
  private boolean hasCompleted;
  private int totalNodes;

  private Paint paint;
  private List<NodeView> nodeCache = new ArrayList<>();
  private OnPatternCompleteListener onPatternCompleteListener;

  public LockPatternView(Context context) {
    this(context, null);
  }

  public LockPatternView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs, defStyleAttr, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

    int specSize = 0;
    switch (widthSpecMode) {
      case MeasureSpec.AT_MOST:
      case MeasureSpec.EXACTLY:
        specSize = Math.min(widthSpecSize, heightSpecSize);
        break;

      default:
      case MeasureSpec.UNSPECIFIED:
        break;
    }

    for (int i = 0; i < totalNodes; i++) {
      final NodeView child = (NodeView) getChildAt(i);
      int childSpec = MeasureSpec.makeMeasureSpec(nodeSize, MeasureSpec.EXACTLY);
      child.measure(childSpec, childSpec);
    }

    setMeasuredDimension(specSize, specSize);
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (!changed) {
      return;
    }

    float areaWidth = (right - left) / sideNodeCount;
    for (int i = 0; i < totalNodes; i++) {
      NodeView node = (NodeView) getChildAt(i);
      int row = i / sideNodeCount;
      int col = i % sideNodeCount;
      int l = (int) (col * areaWidth + (areaWidth - nodeSize) / 2);
      int t = (int) (row * areaWidth + (areaWidth - nodeSize) / 2);
      int r = l + nodeSize;
      int b = t + nodeSize;
      node.layout(l, t, r, b);
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    for (int i = 1; i < nodeCache.size(); i++) {
      NodeView firstNode = nodeCache.get(i - 1);
      NodeView secondNode = nodeCache.get(i);

      canvas.drawLine(firstNode.getCenterX(), firstNode.getCenterY(), secondNode.getCenterX(),
          secondNode.getCenterY(), paint);
    }

    if (nodeCache.size() > 0) {
      NodeView lastNode = nodeCache.get(nodeCache.size() - 1);
      canvas.drawLine(lastNode.getCenterX(), lastNode.getCenterY(), currentX, currentY, paint);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (hasCompleted) {
      return true;
    }

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        currentX = event.getX();
        currentY = event.getY();
        NodeView currentNode = getNodeAt(currentX, currentY);
        if (currentNode != null && !currentNode.isHighlight()) {
          if (nodeCache.size() > 0) {
            NodeView lastNode = nodeCache.get(nodeCache.size() - 1);
            addMiddleNode(lastNode, currentNode);
          }
          currentNode.setHighlight(true);
          nodeCache.add(currentNode);
        }

        if (nodeCache.size() > 0) {
          invalidate();
        }
        break;

      case MotionEvent.ACTION_UP:
        if (nodeCache.size() == 0) {
          return true;
        }

        hasCompleted = true;
        handleCallbak();
        if (!autoClear) {
          if (nodeCache.size() > 0) {
            NodeView lastNode = nodeCache.get(nodeCache.size() - 1);
            currentX = lastNode.getCenterX();
            currentY = lastNode.getCenterY();
          }
          invalidate();
          return true;
        }

        clearPattern();
        break;
    }

    return true;
  }

  private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray a =
        getContext().obtainStyledAttributes(attrs, R.styleable.LockPatternView, defStyleAttr,
            defStyleRes);
    colorInner = a.getColor(R.styleable.LockPatternView_colorInner, colorInner);
    colorOutter = a.getColor(R.styleable.LockPatternView_colorInner, colorOutter);
    colorNodeOn = a.getColor(R.styleable.LockPatternView_colorNodeOn, colorNodeOn);
    lineColor = a.getColor(R.styleable.LockPatternView_lineColor, lineColor);
    lineWidth = a.getDimension(R.styleable.LockPatternView_lineWidth, lineWidth);
    nodeAreaExpand = a.getDimension(R.styleable.LockPatternView_nodeAreaExpand, nodeAreaExpand);
    sideNodeCount = a.getInteger(R.styleable.LockPatternView_sideNodeCount, sideNodeCount);
    autoClear = a.getBoolean(R.styleable.LockPatternView_autoClear, autoClear);
    a.recycle();

    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(lineColor);
    paint.setStrokeWidth(lineWidth);

    /* add all node into current ViewGroup */
    totalNodes = sideNodeCount * sideNodeCount;
    for (int i = 0; i < totalNodes; i++) {
      NodeView nodeView = new NodeView(getContext(), colorInner, colorOutter, colorNodeOn);
      nodeView.setNodeNumber(i);
      nodeView.setHighlight(false);
      addView(nodeView);
    }

    /* clear flag, ViewGroup will not invoke onDraw when background is transparent default */
    setWillNotDraw(false);
  }

  /**
   * To get {@link NodeView} with given x and y coordinate.
   *
   * @param x x coordinate
   * @param y y coordinate
   * @return {@link NodeView} if existed, otherwise return null(between two node).
   */
  private NodeView getNodeAt(float x, float y) {
    for (int i = 0; i < getChildCount(); i++) {
      NodeView node = (NodeView) getChildAt(i);
      if (!(x >= node.getLeft() - nodeAreaExpand && x < node.getRight() + nodeAreaExpand) || !(y
          >= node.getTop() - nodeAreaExpand && y < node.getBottom() + nodeAreaExpand)) {
        continue;
      }

      return node;
    }

    return null;
  }

  private void addNode(int index) {
    NodeView nodeView = (NodeView) getChildAt(index);
    if (nodeView == null || nodeView.isHighlight()) {
      return;
    }

    nodeView.setHighlight(true);
    nodeCache.add(nodeView);
  }

  /**
   * Add middle {@link NodeView} between node A and node B.
   *
   * @param na {@link NodeView} A
   * @param nb {@link NodeView} B
   */
  private void addMiddleNode(NodeView na, NodeView nb) {
    if (na.getNodeNumber() > nb.getNodeNumber()) {
      NodeView nc = na;
      na = nb;
      nb = nc;
    }

    int nodeNumberA = na.getNodeNumber();
    int nodeNumberB = nb.getNodeNumber();

    int nodeACoordinateX = nodeNumberA % sideNodeCount;
    int nodeACoordinateY = nodeNumberA / sideNodeCount;
    int nodeBCoordinateX = nodeNumberB % sideNodeCount;
    int nodeBCoordinateY = nodeNumberB / sideNodeCount;

    int minCoordinateX = Math.min(nodeACoordinateX, nodeBCoordinateX);
    int maxCoordinateX = Math.max(nodeACoordinateX, nodeBCoordinateX);
    int minCoordinateY = Math.min(nodeACoordinateY, nodeBCoordinateY);
    int maxCoordinateY = Math.max(nodeACoordinateY, nodeBCoordinateY);
    int diffX = nodeACoordinateX - nodeBCoordinateX;
    int diffY = nodeBCoordinateY - nodeACoordinateY;

    /* horizon, vertical and oblique */
    if (nodeACoordinateY == nodeBCoordinateY) {
      for (int i = minCoordinateX + 1; i < maxCoordinateX; i++) {
        int index = minCoordinateY * sideNodeCount + i;
        addNode(index);
      }
    } else if (nodeACoordinateX == nodeBCoordinateX) {
      for (int i = minCoordinateY + 1; i < maxCoordinateY; i++) {
        int index = i * sideNodeCount + minCoordinateX;
        addNode(index);
      }
    } else if (Math.abs(diffX) == Math.abs(diffY) && Math.abs(diffX) > 0) {
      for (int i = 1; i < Math.abs(diffX); i++) {
        int x = diffX > 0 ? nodeACoordinateX - i : nodeACoordinateX + i;
        int y = diffX > 0 ? nodeACoordinateY + i : nodeACoordinateY + i;
        int index = x + y * sideNodeCount;
        addNode(index);
      }
    }
  }

  private void handleCallbak() {
    if (onPatternCompleteListener == null) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (NodeView nodeView : nodeCache) {
      sb.append(nodeView.getNodeNumber());
    }

    onPatternCompleteListener.onPatternComplete(sb.toString());
  }

  /**
   * Set on lock pattern complete listener.
   *
   * @param l {@link OnPatternCompleteListener}
   */
  public void setOnPatternCompleteListener(OnPatternCompleteListener l) {
    onPatternCompleteListener = l;
  }

  /**
   * Clear pattern manually if {@code autoClear} is false.
   */
  public void clearPattern() {
    hasCompleted = false;
    for (int i = 0; i < nodeCache.size(); i++) {
      NodeView nodeView = nodeCache.get(i);
      nodeView.setHighlight(false);
    }
    nodeCache.clear();
    invalidate();
  }

  /**
   * nterface definition for a callback to be invoked when lock pattern complete.
   */
  public interface OnPatternCompleteListener {
    /**
     * Invoked when lock pattern complete.
     *
     * @param password password
     */
    void onPatternComplete(String password);
  }
}
