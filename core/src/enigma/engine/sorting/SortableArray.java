package enigma.engine.sorting;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import enigma.engine.Draggable;
import enigma.engine.Positionable;
import enigma.engine.TextureLookup;
import enigma.engine.Tools;
import enigma.engine.Touchable;
import enigma.engine.utilities.LERPSprite;

/**
 * A visual representation of an array that allows swapping of elements.
 * 
 * @author matts
 * @version 1/21/2018
 */
public class SortableArray extends Positionable implements Touchable {
	public static final float MAX_HEIGHT = 300f;

	protected Random rng;

	protected ShapeRenderer sr;
	protected Rectangle boundBox;
	protected float elementWidth;
	protected float spacingWidth;
	protected Vector2 touchOffset = new Vector2();
	protected ArrayList<VisualColumn> elements;
	protected ArrayList<VisualColumn> originalOrderingElements;
	private ArrayList<VisualColumn> interpolating = new ArrayList<VisualColumn>();

	// Marks the current iteration
	protected boolean drawIterationMarker = false;
	protected boolean drawStepMarker = false;
	protected LERPSprite iterationMarker;
	protected LERPSprite stepMarker;
	protected int iterationIndex = 0;
	protected int stepIndex = 0;
	protected Stack<MoveHistoryEntry> iterationMoveHistory;

	// draw by order of height, this means that smaller blocks will draw in front
	// and can be seen
	protected ArrayList<Vector2> arrayIndexPositions;
	protected int minimumElementValue = 1;

	// utility for converting touches
	private Vector3 convertedTouchVect = new Vector3(0, 0, 0);
	private Draggable dragTarget = null;

	protected int maxElementValue;

	protected int seed;

	protected SortableArray solution;
	protected boolean lastMovePlayer = false;

	/**
	 * Create a visual representation of an array that allows swapping of elements.
	 * 
	 * @param x
	 * @param y
	 * @param elementWidth
	 * @param numElements
	 * @param maxElementValue
	 */
	public SortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		float initWidth = elementWidth * numElements;
		this.elementWidth = elementWidth;
		this.spacingWidth = elementWidth;

		this.seed = seed;
		rng = new Random(seed);

		sr = TextureLookup.shapeRenderer;
		boundBox = new Rectangle(x, y, initWidth, SortableArray.MAX_HEIGHT);

		iterationMarker = configureMarker(Color.DARK_GRAY);
		stepMarker = configureMarker(Color.YELLOW);

		this.maxElementValue = maxElementValue;
		generateElements(numElements, maxElementValue);

		iterationMoveHistory = new Stack<MoveHistoryEntry>();

		// positional setup
		setMarkerToPosition(iterationMarker, 0);
		setPosition(x, y);
	}

	public SortableArray(SortableArray toClone) {
		// passing same seed should cause same element generation.
		this(toClone.getX(), toClone.getY(), toClone.elementWidth, toClone.elements.size(), toClone.maxElementValue,
				toClone.seed);
		centerOnPoint(getX(), getY());
	}

	private LERPSprite configureMarker(Color color) {
		LERPSprite sprite = new LERPSprite(TextureLookup.arrowUpSmall);
		sprite.setColor(color);
		sprite.setSize(elementWidth, sprite.getHeight());
		return sprite;
	}

	private void generateElements(int numElements, int maxElementValue) {
		elements = new ArrayList<VisualColumn>(numElements);
		originalOrderingElements = new ArrayList<VisualColumn>(numElements);
		arrayIndexPositions = new ArrayList<Vector2>(numElements);

		for (int idx = 0; idx < numElements; ++idx) {
			int elementValue = rng.nextInt(maxElementValue + 1 - minimumElementValue) + minimumElementValue;
			VisualColumn vc = new VisualColumn(0, 0, elementValue, maxElementValue, MAX_HEIGHT, elementWidth);
			vc.setPosition(getX() + idx * (elementWidth + spacingWidth), getY());
			elements.add(vc);
			originalOrderingElements.add(vc);

			Vector2 arraySlot = new Vector2(vc.getX(), vc.getY());
			arrayIndexPositions.add(arraySlot);
		}
	}

	@Override
	public void setPosition(float x, float y) {
		float translateX = x - getX();
		float translateY = y - getY();

		for (VisualColumn element : elements) {
			element.translate(translateX, translateY);
		}

		for (Vector2 loc : arrayIndexPositions) {
			float oldX = loc.x;
			float oldY = loc.y;

			loc.x = oldX + translateX;
			loc.y = oldY + translateY;
		}
		boundBox.setPosition(x, y);
		setMarkerToPosition(iterationMarker, iterationIndex);
		setMarkerToPosition(stepMarker, stepIndex);
	}

	@Override
	public void translate(float transX, float transY) {
		boundBox.setPosition(boundBox.getX() + transX, boundBox.getY() + transY);
		for (VisualColumn element : elements) {
			element.translate(transX, transY);
		}
		for (Vector2 loc : arrayIndexPositions) {
			float oldX = loc.x;
			float oldY = loc.y;

			loc.x = oldX + transX;
			loc.y = oldY + transY;
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
		interpolating.clear();
		for (VisualColumn element : elements) {
			element.draw(batch);
			if (element.isInterpolating()) {
				interpolating.add(element);
			}
		}

		// draw interpolating elements overtop of the others.
		for (VisualColumn element : interpolating) {
			element.draw(batch);
		}

		// if the target is being dragged, then draw overtop of it to ensure that it is
		// always seen regardless of height (ie draw order)
		if (dragTarget != null) {
			Color cachedColor = sr.getColor();
			float r = cachedColor.r;
			float g = cachedColor.g;
			float b = cachedColor.b;
			float a = cachedColor.a;

			sr.setColor(TextureLookup.getRedColor());
			dragTarget.draw(batch);
			sr.setColor(r, g, b, a);
		}
	}

	public void drawPreSprites(SpriteBatch batch) {
		if (drawStepMarker) stepMarker.draw(batch);

		if (drawIterationMarker) iterationMarker.draw(batch);
	}

	@Override
	public void logic() {
		IO();

		for (VisualColumn element : elements) {
			element.logic();
		}
		stepMarker.logic();
		iterationMarker.logic();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void centerOnPoint(float x, float y) {
		setPosition(x, y);
		float width = elements.size() * (elementWidth + spacingWidth);
		this.translate(-width * 0.5f, 0);
		setMarkerToPosition(iterationMarker, iterationIndex);
		setMarkerToPosition(stepMarker, stepIndex);
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

			VisualColumn swappedWith = attemptSwapWithOtherElement(dragTarget);
			int idx = getIndex(dragTarget);
			if (idx != -1) {
				setLERPToPosition(idx, dragTarget, TextureLookup.getRedColor());
			}
			if (swappedWith != null) {
				idx = getIndex(swappedWith);
				if (idx != -1) {
					setLERPToPosition(idx, swappedWith, TextureLookup.getBlueColor());
				}
			}
			handled = true;
		}
		dragTarget = null;
		lastMovePlayer = true;

		return handled;
	}

	protected void setLERPToPosition(int idxOfPosition, Draggable interpolatingItem, Color color) {
		Vector2 loc = arrayIndexPositions.get(idxOfPosition);
		interpolatingItem.setInterpolatePoint(loc.x, loc.y);
		VisualColumn casted = (VisualColumn) interpolatingItem;
		if (casted != null) {
			casted.setOverrideColor(color);
		}
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

	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		VisualColumn dragged = (VisualColumn) draggedElement;

		if (dragged == null) {
			// cast failed, wrong type
			return null;
		}

		for (int idx = 0; idx < elements.size(); ++idx) {
			VisualColumn currEle = elements.get(idx);
			if (dragged != currEle) {
				if (dragged.colidingWith(currEle)) {
					VisualColumn swappedElement = forceSwap(dragged, currEle, true);
					return swappedElement;
				}
			}
		}

		return null;
	}

	protected VisualColumn forceSwap(VisualColumn movingElement, VisualColumn displacedElement,
			boolean recordInHistory) {
		int fromIndex = getIndex(movingElement);
		int toIndex = getIndex(displacedElement);
		return forceSwap(fromIndex, toIndex, recordInHistory);
	}

	protected VisualColumn forceSwap(int fromIndex, int toIndex, boolean recordInHistory) {
		if (fromIndex != -1 && toIndex != -1) {
			VisualColumn temp = elements.get(fromIndex);
			VisualColumn displacedElement = elements.get(toIndex);
			elements.set(fromIndex, displacedElement);
			elements.set(toIndex, temp);

			if (recordInHistory) {
				swappedIndexCallback(fromIndex, toIndex);
			}

			return displacedElement;
		}
		return null;
	}

	/**
	 * A callback that alerts callee of the indices that have been swapped. Override
	 * this to use information.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 */
	protected void swappedIndexCallback(int fromIndex, int toIndex) {
		iterationMoveHistory.add(new MoveHistoryEntry(fromIndex, toIndex));
	}

	protected void reverseLastMove() {
		if (iterationMoveHistory.size() > 0) {
			MoveHistoryEntry tos = iterationMoveHistory.pop();
			if (tos != null) {
				VisualColumn from = elements.get(tos.fromIndex);
				VisualColumn to = elements.get(tos.toIndex);

				Vector2 fromLoc = arrayIndexPositions.get(tos.fromIndex);
				Vector2 toLoc = arrayIndexPositions.get(tos.toIndex);

				forceSwap(tos.fromIndex, tos.toIndex, false);

				from.setInterpolatePoint(toLoc.x, toLoc.y);
				to.setInterpolatePoint(fromLoc.x, fromLoc.y);
			}
		}
	}

	private int getIndex(Draggable element) {
		for (int idx = 0; idx < elements.size(); ++idx) {
			VisualColumn current = elements.get(idx);
			if (current == element) {
				return idx;
			}
		}
		return -1;
	}

	// ----------------------------- Solving Methods -------------------
	protected void setMarkerToPosition(LERPSprite marker, int idx) {
		if (iterationIndex < elements.size()) {
			Vector2 loc = arrayIndexPositions.get(idx);
			marker.setPosition(loc.x, loc.y - marker.getHeight() * 1.5f);
		}
	}

	protected void setMarkerLERPToPosition(LERPSprite marker, int idx) {
		if (idx < elements.size()) {
			Vector2 loc = arrayIndexPositions.get(idx);
			marker.setInterpolatePoint(loc.x, loc.y - marker.getHeight() * 1.5f);
		}
	}

	public boolean nextSolveStep(boolean allowIncrementIteration) {
		if (stepIndexComplete()) {
			if(allowIncrementIteration) {
				incrementIteration();
				
			}
		}

		if (solveCompleted()) {
			setMarkerLERPToPosition(stepMarker, elements.size() - 1);
		}
		
		//return stepIndexComplete();
		return false;
	}

	protected void completeNextStep() {
		throw new RuntimeException("Subclasses should be responsible for implementing solving algorithms.");
	}

	private void incrementIteration() {
		iterationIndex += 1;
		stepIndex = iterationIndex;

		setMarkerLERPToPosition(iterationMarker, iterationIndex);
		setMarkerLERPToPosition(stepMarker, iterationIndex);

		iterationMoveHistory.clear();
	}

	private boolean solveCompleted() {
		return iterationIndex >= elements.size();
	}

	protected boolean stepIndexComplete() {
		throw new RuntimeException("Subclasses are responsible for implementing solving behavior");
	}

	protected void IO() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			if (!lastMovePlayer || iterationMoveHistory.size() == 0) {
				nextSolveStep(true);
				lastMovePlayer = false;
			} else {
				compareUserAgainstSolution();
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			reverseLastMove();
		}

	}

	protected void compareUserAgainstSolution() {
		if (solution == null) {
			createSolutionArray();
		}
		//if the solution is ahead of current user, then back AI solution up to player's current move. 
		while(solution.iterationMoveHistory.size() > iterationMoveHistory.size()) {
			solution.reverseLastMove();
		}

		// catch AI up to user's valid iteration
		while (iterationIndex < solution.iterationIndex && iterationIndex != elements.size()) {
			solution.nextSolveStep(false);
		}

		//return if the array has been sorted.
		if (iterationIndex == elements.size()) {
			return;
		}
		
		for(int idx = iterationIndex; idx >= 0 && idx >= stepIndex; --idx) {
			if(solution.elements.get(idx).getValue() != elements.get(idx).getValue()) {
				//solution is wrong
			}
		}
		
		
	}

	protected void createSolutionArray() {
		// having a non-specific sortable array is useful, but having a non-specific
		// sortable array solve itself isn't meaningful because
		// it is specifically designed to be free of any sorting algorithm.
		throw new RuntimeException(
				"This method should be provided by a subclass. However it should not make the entire sortable array abstract.");
	}

	// ----------------------------- Solving Methods -------------------
}
