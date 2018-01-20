package enigma.engine.sorting;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import enigma.engine.Draggable;
import enigma.engine.Entity;
import enigma.engine.Positionable;
import enigma.engine.TextureLookup;

public class VisualColumn implements Entity, Positionable, Draggable {
	protected ShapeRenderer sr;
	protected Rectangle box;
	protected Vector2 touchOffset = new Vector2();
	protected int value;

	public VisualColumn(float initWidth, float initHeight) {
		this(0, 0, initWidth, initHeight);
	}

	public VisualColumn(float x, float y, float initWidth, float initHeight) {
		sr = TextureLookup.shapeRenderer;
		box = new Rectangle(x, y, initWidth, initHeight);
	}
	
	public VisualColumn(float x, float y, int value, int maxValue, float maxHeight, float initWidth) {
		this(x, y, initWidth, maxHeight * (float) value / maxValue);
		this.value = value;
	}

	@Override
	public void draw(SpriteBatch batch) {
		// The user of this class should configure the drawing before this draw call is
		// called.
		// Specifically, stop SpriteBatch from drawing sprites, and start the shape
		// renderer.
		if (batch.isDrawing()) throw new RuntimeException("Please disble batch before calling draw");
		if (!sr.isDrawing())
			throw new RuntimeException("Please configure TextureLookup shape renderer before calling draw");
		
		sr.rect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}

	@Override
	public void logic() {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void setPosition(float x, float y) {
		box.setPosition(x, y);
	}

	@Override
	public void translate(float x, float y) {
		float oldX = box.getX();
		float oldY = box.getY();
		box.setPosition(x + oldX, oldY + y);
	}

	@Override
	public float getX() {
		return box.getX();
	}

	@Override
	public float getY() {
		return box.getY();
	}

	public boolean contains(float ptX, float ptY) {
		return box.contains(ptX, ptY);
	}

	@Override
	public void draggedToPoint(float x, float y) {
		setPosition(x + touchOffset.x, y + touchOffset.y);
	}

	@Override
	public void startedDragging(float x, float y) {
		setTouchOffset(getX() - x, getY() - y);
	}

	@Override
	public void endedDragging(float x, float y) {
	}

	@Override
	public void setTouchOffset(float offsetX, float offsetY) {
		touchOffset.set(offsetX, offsetY);
	}
	
	public void setValue(int value) {
		this.value = value;
		
	}
}
