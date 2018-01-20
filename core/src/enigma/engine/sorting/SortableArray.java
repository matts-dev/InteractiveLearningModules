package enigma.engine.sorting;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import enigma.engine.Draggable;
import enigma.engine.Entity;
import enigma.engine.Positionable;
import enigma.engine.TextureLookup;
import enigma.engine.Tools;
import enigma.engine.Touchable;

public class SortableArray implements Entity, Positionable, Touchable {
	public static final float MAX_HEIGHT = 300f;
	protected ShapeRenderer sr;
	protected Rectangle boundBox;
	protected float elementWidth;
	protected float spacingWidth;
	protected Vector2 touchOffset = new Vector2();
	protected ArrayList<VisualColumn> elements;
	protected int minimumElementValue = 1;
	
	//utility for converting touches
	private Vector3 convertedTouchVect = new Vector3(0, 0, 0);
	private Draggable dragTarget = null;

	public SortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue) {
		float initWidth = elementWidth * numElements;
		this.elementWidth = elementWidth;
		this.spacingWidth = elementWidth * 0.1f;

		sr = TextureLookup.shapeRenderer;
		boundBox = new Rectangle(x, y, initWidth, SortableArray.MAX_HEIGHT);

		generateElements(numElements, maxElementValue);
		setPosition(x, y);
	}

	private void generateElements(int numElements, int maxElementValue) {
		Random rng = new Random();

		elements = new ArrayList<VisualColumn>(numElements);

		for (int idx = 0; idx < numElements; ++idx) {
			int elementValue = rng.nextInt(maxElementValue + 1 - minimumElementValue) + minimumElementValue;
			VisualColumn vc = new VisualColumn(0, 0, elementValue, maxElementValue, MAX_HEIGHT, elementWidth);
			vc.setPosition(getX() + idx * (elementWidth + spacingWidth), getY());
			elements.add(vc);
		}
	}

	@Override
	public void setPosition(float x, float y) {
		boundBox.setPosition(x, y);
		for (VisualColumn element : elements) {
			element.translate(x - getX(), y - getY());
		}
	}

	@Override
	public void translate(float x, float y) {
		boundBox.setPosition(boundBox.getX() + x, boundBox.getY() + y);
		for (VisualColumn element : elements) {
			element.translate(x, y);
		}
	}

	@Override
	public float getX() {
		return boundBox.getX();
	}

	@Override
	public float getY() {
		return boundBox.getY();
	}

	@Override
	public void draw(SpriteBatch batch) {
		for (VisualColumn element : elements) {
			element.draw(batch);
		}
	}

	@Override
	public void logic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void centerOnPoint(float x, float y) {
		setPosition(x, y);
		float width = elements.size() * (elementWidth + spacingWidth);
		this.translate(-width * 0.5f, 0);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		boolean handled = false;
		Tools.convertMousePointsIntoGameCoordinates(camera, convertedTouchVect);
		
		for (VisualColumn element : elements) {
			if (element.contains(convertedTouchVect.x, convertedTouchVect.y)) {
				dragTarget = element;
				dragTarget.startedDragging(convertedTouchVect.x, convertedTouchVect.y);
				handled = true;
				break;
			}
		}

		return handled;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		boolean handled = false;
		Tools.convertMousePointsIntoGameCoordinates(camera, convertedTouchVect);
		
		if (dragTarget != null) {
			dragTarget.endedDragging(convertedTouchVect.x, convertedTouchVect.y);
			handled = true;
		}
		dragTarget = null;

		return handled;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera) {
		boolean handled = false;
		
		Tools.convertMousePointsIntoGameCoordinates(camera, convertedTouchVect);
		if (dragTarget != null) {
			dragTarget.draggedToPoint(convertedTouchVect.x, convertedTouchVect.y);
		}

		return handled;
	}
}
