/**
 * Copyright 2007 Wei-ju Wu
 *
 * This file is part of TinyUML.
 *
 * TinyUML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * TinyUML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TinyUML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.tinyuml.draw;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.tinyuml.util.Command;

/**
 * A selection that holds a single rectangular shape. It is designed for reuse,
 * so once created, it can be reinitialized with another shape. A
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class NodeSelection implements Selection, NodeChangeListener {

  private static final Color ACCEPT_COLOR = new Color(0, 220, 50);
  private static final int HANDLE_SIZE = 5;
  private static final int HANDLE_NW = 0;
  private static final int HANDLE_NE = 1;
  private static final int HANDLE_SW = 2;
  private static final int HANDLE_SE = 3;
  private Rectangle2D[] handles = new Rectangle2D[4];

  private Node node;
  private DiagramOperations editor;
  private boolean isMoving, isResizing;
  private Point2D anchor = new Point2D.Double();
  private Point2D tmpPos = new Point2D.Double();
  private Dimension2D tmpSize = new DoubleDimension();
  private int resizeDirection = -1;

  /**
   * A boolean holder class. We avoid to use the CORBA package.
   */
  static class BooleanHolder {
    private boolean value;

    /**
     * Return the value.
     * @return the value
     */
    public boolean getValue() { return value; }

    /**
     * Sets the value.
     * @param aValue the value
     */
    public void setValue(boolean aValue) { value = aValue; }
  }

  /**
   * Constructor.
   * @param anEditor the editor instance this selection belongs to
   * @param aNode the node
   */
  public NodeSelection(DiagramOperations anEditor, Node aNode) {
    editor = anEditor;
    for (int i = 0; i < 4; i++) {
      handles[i] = new Rectangle2D.Double();
    }
    node = aNode;
    node.addNodeChangeListener(this);
    updateDimensions();
  }

  /**
   * Constructor for testing purposes only.
   */
  public NodeSelection() { }

  /**
   * {@inheritDoc}
   */
  public void updateDimensions() {
    copyShapeData();
    setHandlePositions();
  }

  /**
   * {@inheritDoc}
   */
  public DiagramElement getElement() { return node; }

  /**
   * {@inheritDoc}
   */
  public List<DiagramElement> getElements() {
    List<DiagramElement> result = new ArrayList<DiagramElement>();
    result.add(node);
    return result;
  }

  /**
   * Copies the size and position data of the shape to the selection data.
   */
  private void copyShapeData() {
    tmpPos.setLocation(node.getAbsoluteX1(), node.getAbsoluteY1());
    tmpSize.setSize(node.getSize());
  }

  /**
   * Determines the handle positions.
   */
  private void setHandlePositions() {
    double absx = node.getAbsoluteX1(), absy = node.getAbsoluteY1(),
      swidth = node.getSize().getWidth();
    double x = absx - HANDLE_SIZE, y = absy - HANDLE_SIZE,
      width = HANDLE_SIZE, height = HANDLE_SIZE;
    handles[0].setRect(x, y, width, height);

    // second handle
    x = absx + swidth;
    handles[1].setRect(x, y, width, height);

    // third handle
    x = absx - HANDLE_SIZE;
    y = absy + node.getSize().getHeight();
    handles[2].setRect(x, y, width, height);

    // fourth handle
    x = absx + swidth;
    handles[3].setRect(x, y, width, height);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isDragging() {
    return isMoving || isResizing;
  }

  /**
   * {@inheritDoc}
   */
  public void startDragging(double x, double y) {
    resizeDirection = getResizeHandle(x, y);
    isResizing = (resizeDirection >= 0);
    isMoving = !isResizing;
    anchor.setLocation(x, y);
  }

  /**
   * {@inheritDoc}
   */
  public void stopDragging(double x, double y) {
    if (isMoving) {
      moveSelectedNode(x, y);
    } else if (isResizing) {
      editor.resizeElement(node, tmpPos, tmpSize);
    }
    isMoving = false;
    isResizing = false;
    updateDimensions();
  }

  /**
   * Action to move the selected node to the specified position.
   * @param x the x coordinate
   * @param y the y coordinate
   */
  private void moveSelectedNode(double x, double y) {
    DiagramElement elem = editor.getDiagram().getChildAt(x, y);
    if (isDraggedToANewParent(elem)) {
      reparentNode((CompositeNode) elem);
    } else if (isDraggedOutOfParent(elem)) {
      reparentNode(editor.getDiagram());
    } else {
      Collection<Node> nodes = new ArrayList<Node>();
      nodes.add(node);
      MoveNodeOperation op = new MoveNodeOperation(node, node.getParent(),
        tmpPos);
      editor.moveElements(new Command[] {op});
    }
  }

  /**
   * Remove this node from its former parent and add it to the specified new
   * parent node.
   * @param newparent the new parent node
   */
  private void reparentNode(CompositeNode newparent) {
    MoveNodeOperation op = new MoveNodeOperation(node, newparent, tmpPos);
    editor.moveElements(new Command[] {op});
  }

  /**
   * Determines if the current element can be dropped into the specified
   * target. The main conditions are that elements can not be added to
   * themselves or to their ancestors.
   * @param dropTarget the drop target
   * @return true if can be dropped, false otherwise
   */
  private boolean isDraggedToANewParent(DiagramElement dropTarget) {
    return dropTarget != node && dropTarget.canNestElements() &&
      !dropTarget.isAncestor(node) &&
      dropTarget.getAbsoluteBounds().contains(getNodeTargetBounds());
  }

  /**
   * Returns the target bounds of the current node.
   * @return the target bounds
   */
  private Rectangle2D getNodeTargetBounds() {
    Rectangle2D result = node.getAbsoluteBounds();
    result.setRect(tmpPos.getX(), tmpPos.getY(), result.getWidth(),
      result.getHeight());
    return result;
  }

  /**
   * Determines whether the current element is going to be dragged out of a
   * nesting element into the outermost nester (which is the diagram itself).
   * @param dropTarget the dropTarget
   * @return true if dragged out of a nester, false otherwise
   */
  private boolean isDraggedOutOfParent(DiagramElement dropTarget) {
    return dropTarget == NullElement.getInstance() &&
           node.getParent() != editor.getDiagram();
  }

  /**
   * {@inheritDoc}
   */
  public void cancelDragging() { }

  /**
   * {@inheritDoc}
   */
  public void updatePosition(double x, double y) {
    double diffx = x - anchor.getX();
    double diffy = y - anchor.getY();
    if (isMoving) {
      moveSelection(diffx, diffy);
    } else {
      resizeSelection(diffx, diffy);
    }
  }

  /**
   * Moves the selection by the specified amount.
   * @param diffx the difference to the selection anchor x coordinate
   * @param diffy the difference to the selection anchor y coordinate
   */
  protected void moveSelection(double diffx, double diffy) {
    Diagram diagram = editor.getDiagram();
    // Can not move out of the left border
    if (node.getAbsoluteX1() + diffx < diagram.getOrigin().getX()) {
      diffx = diagram.getOrigin().getX() - node.getAbsoluteX1();
    }
    // and not out of the top border
    if (node.getAbsoluteY1() + diffy < diagram.getOrigin().getY()) {
      diffy = diagram.getOrigin().getY() - node.getAbsoluteY1();
    }

    tmpPos.setLocation(node.getAbsoluteX1() + diffx,
      node.getAbsoluteY1() + diffy);
    diagram.snap(tmpPos);
  }

  /**
   * Resizes the specified selection.
   * @param diffx the difference to the selection anchor x coordinate
   * @param diffy the difference to the selection anchor y coordinate
   */
  private void resizeSelection(double diffx, double diffy) {
    BooleanHolder xFlagHolder = new BooleanHolder();
    BooleanHolder yFlagHolder = new BooleanHolder();
    xFlagHolder.setValue(false);
    yFlagHolder.setValue(false);
    // parent truncation and minimum size truncation have to be mutual
    // exclusive
    diffx = truncateToMinimumWidth(diffx, xFlagHolder);
    if (!xFlagHolder.getValue()) diffx = truncateToParentX(diffx, xFlagHolder);
    if (!xFlagHolder.getValue())
      diffx = truncateToParentWidth(diffx, xFlagHolder);

    diffy = truncateToMinimumHeight(diffy, yFlagHolder);
    if (!yFlagHolder.getValue()) diffy = truncateToParentY(diffy, yFlagHolder);
    if (!yFlagHolder.getValue())
      diffy = truncateToParentHeight(diffy, yFlagHolder);

    switch (resizeDirection) {
      case HANDLE_SE:
        resizeSe(diffx, diffy);
        break;
      case HANDLE_NE:
        resizeNe(diffx, diffy);
        break;
      case HANDLE_NW:
        resizeNw(diffx, diffy);
        break;
      case HANDLE_SW:
        resizeSw(diffx, diffy);
        break;
      default:
        break;
    }
  }

  /**
   * Resize using the SE handle.
   * @param diffx the diffx value
   * @param diffy the diffy value
   */
  protected void resizeSe(double diffx, double diffy) {
    tmpSize.setSize(node.getSize().getWidth() + diffx,
      node.getSize().getHeight() + diffy);
    new CornerSnap(editor.getDiagram(), tmpPos, tmpSize).snapRightLower();
  }

  /**
   * Resize using the NE handle.
   * @param diffx the diffx value
   * @param diffy the diffy value
   */
  protected void resizeNe(double diffx, double diffy) {
    tmpPos.setLocation(node.getAbsoluteX1(),
      node.getAbsoluteY1() + diffy);
    tmpSize.setSize(node.getSize().getWidth() + diffx,
      node.getSize().getHeight() - diffy);
    new CornerSnap(editor.getDiagram(), tmpPos, tmpSize).snapRightUpper();
  }

  /**
   * Resize using the NW handle.
   * @param diffx the diffx value
   * @param diffy the diffy value
   */
  protected void resizeNw(double diffx, double diffy) {
    tmpPos.setLocation(node.getAbsoluteX1() + diffx,
      node.getAbsoluteY1() + diffy);
    tmpSize.setSize(node.getSize().getWidth() - diffx,
      node.getSize().getHeight() - diffy);
    new CornerSnap(editor.getDiagram(), tmpPos, tmpSize).snapLeftUpper();
  }

  /**
   * Resize using the SW handle.
   * @param diffx the diffx value
   * @param diffy the diffy value
   */
  protected void resizeSw(double diffx, double diffy) {
    tmpPos.setLocation(node.getAbsoluteX1() + diffx,
      node.getAbsoluteY1());
    tmpSize.setSize(node.getSize().getWidth() - diffx,
      node.getSize().getHeight() + diffy);
    new CornerSnap(editor.getDiagram(), tmpPos, tmpSize).snapLeftLower();
  }

  /**
   * Truncate the specified diffx, depending on the resizing direction.
   * A selection can not be made smaller than the Node's minimum size
   * @param diffx the diffx value
   * @param success a BooleanHolder to store the success result
   * @return the diffx value truncated to the minimum size if necessary
   */
  private double truncateToMinimumWidth(double diffx, BooleanHolder success) {
    double dx = westernSwap(diffx);
    if (node.getSize().getWidth() + dx < node.getMinimumSize().getWidth()) {
      dx = node.getMinimumSize().getWidth() - node.getSize().getWidth();
      success.setValue(true);
    }
    return westernSwap(dx);
  }

  /**
   * If the resize direction is on one of the western handles, this function
   * return the negative of x, otherwise x.
   * @param x the x value
   * @return -x if resizeDirection is in the west, x otherwise
   */
  private double westernSwap(double x) {
    return (resizeDirection == HANDLE_NW || resizeDirection == HANDLE_SW) ?
      -x : x;
  }

  /**
   * If the resize direction is on one of the northern
   * @param y the y value
   * @return the negative y if the resize direction is in the north, y, else
   */
  private double northernSwap(double y) {
    return (resizeDirection == HANDLE_NW || resizeDirection == HANDLE_NE) ?
      -y : y;
  }

  /**
   * Truncate the specified diffy, depending on the resizing direction.
   * A selection can not be made smaller than the Node's minimum size
   * @param diffy the diffy value
   * @param success a BooleanHolder to store the success result
   * @return the diffy value truncated to the minimum size if necessary
   */
  private double truncateToMinimumHeight(double diffy, BooleanHolder success) {
    double dy = northernSwap(diffy);
    if (node.getSize().getHeight() + dy < node.getMinimumSize().getHeight()) {
      dy = node.getMinimumSize().getHeight() - node.getSize().getHeight();
      success.setValue(true);
    }
    return northernSwap(dy);
  }

  /**
   * Truncates the drag position to the parent's absolute x position.
   * @param diffx the diffx value
   * @param success a BooleanHolder to store the success result
   * @return the truncated value
   */
  private double truncateToParentX(double diffx, BooleanHolder success) {
    double dx = diffx;
    if (node.getParent() != null &&
        node.getAbsoluteX1() + diffx < node.getParent().getAbsoluteX1()) {
      dx -= ((node.getAbsoluteX1() + diffx) - node.getParent().getAbsoluteX1());
      success.setValue(true);
    }
    return dx;
  }

  /**
   * Truncates the drag position to the parent's absolute y position.
   * @param diffy the diffy value
   * @param success a BooleanHolder to store the success result
   * @return the truncated value
   */
  private double truncateToParentY(double diffy, BooleanHolder success) {
    double dy = diffy;
    if (node.getParent() != null &&
        node.getAbsoluteY1() + diffy < node.getParent().getAbsoluteY1()) {
      dy -= ((node.getAbsoluteY1() + diffy) - node.getParent().getAbsoluteY1());
      success.setValue(true);
    }
    return dy;
  }

  /**
   * Truncates the specified diffx to the parent's width to prevent that
   * it is drawn outside of the parent's bounds.
   * @param diffx the diffx value
   * @param success a BooleanHolder to store the success result
   * @return the truncated diffx value
   */
  private double truncateToParentWidth(double diffx, BooleanHolder success) {
    double dx = diffx;
    if (node.getParent() != null &&
        node.getAbsoluteX2() + diffx > node.getParent().getAbsoluteX2()) {
      dx -= (node.getAbsoluteX2() + diffx) - node.getParent().getAbsoluteX2();
      success.setValue(true);
    }
    return dx;
  }

  /**
   * Truncates the specified diffy to the parents height to prevent that the
   * selection is drawn outside of it's parent's bounds.
   * @param diffy the diffy value
   * @param success a BooleanHolder to store the success result
   * @return the truncated diffy value
   */
  private double truncateToParentHeight(double diffy, BooleanHolder success) {
    double dy = diffy;
    if (node.getParent() != null &&
        node.getAbsoluteY2() + diffy > node.getParent().getAbsoluteY2())  {
      dy -= (node.getAbsoluteY2() + diffy) - node.getParent().getAbsoluteY2();
      success.setValue(true);
    }
    return dy;
  }

  /**
   * {@inheritDoc}
   */
  public void draw(DrawingContext drawingContext) {
    if (isDragging()) {
      if (isMoving) drawDropTargetSilhouette(drawingContext);
      drawSilhouette(drawingContext);
    } else {
      drawHandles(drawingContext);
    }
  }

  /**
   * Draws the rectangular silhouette of this selection.
   * @param drawingContext the DrawingContext object
   */
  private void drawSilhouette(DrawingContext drawingContext) {
    drawingContext.drawRectangle(tmpPos.getX(), tmpPos.getY(),
      tmpSize.getWidth(), tmpSize.getHeight(), null);
  }

  /**
   * Draws a drop target silhouette if possible.
   * @param drawingContext the DrawingContext
   */
  private void drawDropTargetSilhouette(DrawingContext drawingContext) {
    DiagramElement element = editor.getDiagram().getChildAt(tmpPos.getX(),
      tmpPos.getY());
    if (isDraggedToANewParent(element)) {
      Rectangle2D targetBounds = element.getAbsoluteBounds();
      // create an even larger bounds rectangle and draw it
      Rectangle2D targetSilhouette = (Rectangle2D) targetBounds.clone();
      targetSilhouette.setRect(targetBounds.getX() - 5, targetBounds.getY(),
        targetBounds.getWidth() + 10, targetBounds.getHeight() + 5);
      drawingContext.drawRectangle(targetBounds.getX() - 5,
        targetBounds.getY() - 5, targetBounds.getWidth() + 10,
        targetBounds.getHeight() + 10, ACCEPT_COLOR, null);
    }
  }

  /**
   * A selected ClassShape displays resizing handles.
   * @param drawingContext the DrawingContext
   */
  private void drawHandles(DrawingContext drawingContext) {
    for (int i = 0; i < handles.length; i++) {
      drawingContext.fillRectangle(handles[i].getX(), handles[i].getY(),
        handles[i].getWidth(), handles[i].getHeight(), Color.BLACK);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean contains(double x, double y) {
    if (node.contains(x, y)) return true;
    return getResizeHandle(x, y) >= 0;
  }

  /**
   * Returns the resize handle index at the specified position.
   * @param x the x coordinate
   * @param y the y coordinate
   * @return the handle number
   */
  private int getResizeHandle(double x, double y) {
    for (int i = 0; i < 4; i++) {
      if (handles[i].contains(x, y)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public Cursor getCursorForPosition(double x, double y) {
    switch (getResizeHandle(x, y)) {
      case HANDLE_NW:
        return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
      case HANDLE_NE:
        return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
      case HANDLE_SW:
        return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
      case HANDLE_SE:
        return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
      default:
        return Cursor.getDefaultCursor();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void nodeResized(Node aNode) {
    updateDimensions();
  }

  /**
   * {@inheritDoc}
   */
  public void nodeMoved(Node aNode) { }
}
