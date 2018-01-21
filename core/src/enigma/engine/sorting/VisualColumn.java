package enigma.engine.sorting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.Tools;

public class VisualColumn extends Draggable {
	private static Vector2 utilVec = new Vector2();

	protected ShapeRenderer sr;
	protected Rectangle box;
	protected Vector2 touchOffset = new Vector2();
	protected int value;

	protected boolean interpolateToPoint = false;
	protected Vector2 interpolatePoint = new Vector2();
	protected float interpolateSpeed = Tools.convertSpeedTo60FPSValue(10f);

	private Color overrideColor = new Color();

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

		if (interpolateToPoint) {
			// use color override
			Color cacheColor = sr.getColor();
			float a = cacheColor.a;
			float b = cacheColor.b;
			float g = cacheColor.g;
			float r = cacheColor.r;
			sr.setColor(overrideColor.r, overrideColor.g, overrideColor.b, overrideColor.a);
			sr.rect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
			sr.setColor(r, g, b, a);
		} else {
			sr.rect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
		}

	}

	@Override
	public void logic() {
		if (interpolateToPoint) {
			float deltaSpeed = interpolateSpeed * Gdx.graphics.getDeltaTime();

			float distance = Vector2.dst(interpolatePoint.x, interpolatePoint.y, getX(), getY());
			if (distance < 0.001f) {
				interpolateToPoint = false;
				return;
			}

			Vector2 direction = VisualColumn.utilVec.set(interpolatePoint.x - getX(), interpolatePoint.y - getY());
			direction = direction.nor();
			direction.scl(deltaSpeed);

			if (direction.len() > distance) {
				direction.nor();
				direction.scl(distance);
			}

			translate(direction.x, direction.y);
		}
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

	@Override
	public void setInterpolatePoint(float x, float y) {
		interpolateToPoint = true;
		this.interpolatePoint.set(x, y);
	}

	public boolean colidingWith(VisualColumn otherElement) {
		// since these are not rotated, we can do simply rectangle collision testing.
		return otherElement.box.overlaps(box) || otherElement.box.contains(box);
	}
	
	public void setOverrideColor(Color overrideColor) {
//		this.overrideColor.set(overrideColor.toIntBits());
		this.overrideColor.r =overrideColor.r; 
		this.overrideColor.g =overrideColor.g;
		this.overrideColor.b =overrideColor.b;
		this.overrideColor.a =overrideColor.a;
	}

	public boolean isInterpolating() {
		return interpolateToPoint;
	}

	public int getValue() {
		return value;
	}
}
