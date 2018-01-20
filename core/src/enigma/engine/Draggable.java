package enigma.engine;

public abstract class Draggable extends Positionable {
	/**
	 * When a component is touched, it usually isn't touched at its position. It is
	 * normally touched as some distance away from its positional XY. So, when
	 * dragging one cannot simply update the XY position. The fix to this, is to
	 * track the touch offset from the position, and use that when ever the item is
	 * dragged.
	 */
	public abstract void setTouchOffset(float offsetX, float offsetY);

	public abstract void startedDragging(float x, float y);
	public abstract void draggedToPoint(float x, float y);
	public abstract void endedDragging(float x, float y);

	public abstract void setInterpolatePoint(float x, float y);
}
