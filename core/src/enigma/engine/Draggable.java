package enigma.engine;

public interface Draggable {
	/**
	 * When a component is touched, it usually isn't touched at its position. It is
	 * normally touched as some distance away from its positional XY. So, when
	 * dragging one cannot simply update the XY position. The fix to this, is to
	 * track the touch offset from the position, and use that when ever the item is
	 * dragged.
	 */
	public void setTouchOffset(float offsetX, float offsetY);

	public void startedDragging(float x, float y);
	public void draggedToPoint(float x, float y);
	public void endedDragging(float x, float y);
}
