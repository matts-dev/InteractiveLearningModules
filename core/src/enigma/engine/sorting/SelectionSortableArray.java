package enigma.engine.sorting;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.utilities.Tuple2;

public class SelectionSortableArray extends SortableArray {
	protected Marker swapMarker;
	private int currentMinimum;
	private int currentMinimumIdx;
	private boolean shouldSwapAfterIteration = true;
	private boolean forceHidePrompts = false;

	int[] cachedSourceArray;
	private String iterationTimerKey = "iterTimerKey";
	@SuppressWarnings("unused")
	private VisualColumn lastClicked;
	private boolean scanningElements;
	public boolean allowSwapMarkerDraw = true;

	public SelectionSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue,
			int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		timer.setTimer(iterationTimerKey, 0);

		swapMarker = configureMarker(Color.RED);
		setMarkerToPosition(swapMarker, 0);
		setupIterationVars();
	}

	public SelectionSortableArray(float x, float y, float elementWidth, int maxElementValue, int[] sourceArray,
			boolean firstCall) {
		super(x, y, elementWidth, maxElementValue, sourceArray);

		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		timer.setTimer(iterationTimerKey, 0);

		swapMarker = configureMarker(Color.RED);
		setMarkerToPosition(swapMarker, 0);
		setupIterationVars();

		if (firstCall) {
			overrideSolution = true;
			cachedSourceArray = sourceArray;
			solution = new InsertionSortableArray(x, y, elementWidth, maxElementValue, sourceArray, false);
		}

		instruction.setText("<>.");
	}

	@Override
	public void translate(float transX, float transY) {
		super.translate(transX, transY);
		swapMarker.translate(transX, transY);
	}

	@Override
	public void centerOnPoint(float x, float y) {
		super.centerOnPoint(x, y);
		setMarkerToPosition(swapMarker, iterationIndex);
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
	}

	@Override
	public void logic() {
		swapMarker.logic();
		super.logic();
	}

	protected void IO() {
		super.IO();

	}

	@Override
	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		VisualColumn result = super.attemptSwapWithOtherElement(draggedElement);

		return result;
	}

	@Override
	protected boolean stepIndexComplete() {
		if (stepIndex < elements.size() - 1) {
			stepIndex++;
			setMarkerLERPToPosition(stepMarker, stepIndex);
			int stepValue = elements.get(stepIndex).getValue();
			if (stepValue < currentMinimum) {
				setMarkerLERPToPosition(swapMarker, stepIndex);
				currentMinimum = stepValue;
				currentMinimumIdx = stepIndex;
			}
		} else {
			return true;
		}

		// if(stepIndex > 0 && stepIndex < elements.size()) {
		// //check left neighbor
		// VisualColumn step = elements.get(stepIndex);
		// VisualColumn leftNeighbor = elements.get(stepIndex - 1);
		//
		// if (leftNeighbor.getValue() > step.getValue() ){
		//
		// forceSwap(step, leftNeighbor, true);
		// setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
		// setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
		// stepIndex--;
		// setMarkerLERPToPosition(stepMarker, stepIndex);
		// }
		//
		// //check if solution is complete (this prevents extra enter being pressed)
		// else if(stepIndex != 0) {
		// //check the next left pair //note that the step index has already been
		// decremented
		// step = elements.get(stepIndex);
		// leftNeighbor = elements.get(stepIndex - 1);
		// if (leftNeighbor.getValue() <= step.getValue() ){
		// stepIndex = 0;
		// //return false;//original
		// return true;
		// }
		// }
		// } else {
		// //the step is out of bounds, treat this as a solved sort.
		// return true;
		// }

		// check if this step completed iteration
		// if(stepIndex == 0) return true; //this will cause it to skip some iteration
		// updates

		return false;
	}

	@Override
	protected void completeNextStep() {
		if (stepIndex == 0) return;

		if (stepIndex > 0 && stepIndex < elements.size()) {
			// check left neighbor
			VisualColumn step = elements.get(stepIndex);
			VisualColumn leftNeighbor = elements.get(stepIndex - 1);

			if (leftNeighbor.getValue() >= step.getValue()) {
				forceSwap(step, leftNeighbor, true);
				setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
				setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
				stepIndex--;
				setMarkerLERPToPosition(stepMarker, stepIndex);
			} else {
				stepIndex = 0;
			}
		}

		// check if this step completed iteration
		if (stepIndex == 0) return;

		return;
	}

	@Override
	public void createSolutionArray() {
		solution = new SelectionSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
	}

	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);
		if (!forceHidePrompts) {
			instruction.draw(batch);
		}
		if (drawSwapMarker()) {
			swapMarker.draw(batch);
		}
	}

	@Override
	protected void incrementIteration() {
		if (shouldSwapAfterIteration && iterationIndex < elements.size()) {
			elements.get(iterationIndex).setOverrideColor(Color.GRAY);
			elements.get(currentMinimumIdx).setOverrideColor(Color.RED);
			swapWithAnimation(iterationIndex, currentMinimumIdx, false);
			timer.setTimer(iterationTimerKey, 500);
		}

		// this must come after swap. Otherwise indexes will be updated values, not old
		// values.
		super.incrementIteration();

		setMarkerLERPToPosition(swapMarker, iterationIndex);
		setupIterationVars();
	}

	private void setupIterationVars() {
		if (iterationIndex < elements.size()) {
			currentMinimum = elements.get(iterationIndex).getValue();
			currentMinimumIdx = iterationIndex;
		}
	}

	public boolean drawSwapMarker() {
		
		// for(VisualColumn element : elements) {
		// if(element.isInterpolating()) {
		// return false;
		// }
		// }
		// return true;
		return timer.timerUp(iterationTimerKey) && allowSwapMarkerDraw;
	}

	@Override
	public boolean shouldDrawStepMarker() {
		return timer.timerUp(iterationTimerKey);
	}

	@Override
	protected boolean allowUserInput() {
		return super.allowUserInput() && timer.timerUp(iterationTimerKey);
	}

	@Override
	public TutorialManager getTutorialManager(float x, float y, float elementWidth, int numElements,
			int maxElementValue) {

		return new TutorialManagerSelectSort(x, y, elementWidth, numElements, maxElementValue);
	}

	public void togglePromptUpdates(boolean showPracticePrompts) {
		forceHidePrompts = showPracticePrompts;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		boolean result = super.touchDown(screenX, screenY, pointer, button, camera);

		// if right click was pressed, don't allow dragging. This will be captured on
		// next loop.
		if (enterEvent) {
			dragTarget = null;
		}

		// while scanning, allow marking, not swapping.
		if (scanningElements) {
			// if ((VisualColumn) dragTarget != null) {
			//
			// if(lastClicked != null) {
			// lastClicked.setAlwaysForceColorOverride(false);
			// }
			//
			// lastClicked = (VisualColumn) dragTarget;
			// lastClicked.setAlwaysForceColorOverride(true);
			// lastClicked.setOverrideColor(Color.RED);
			//
			// }
			//
			// dragTarget = null;
		}

		return result;
	}

	public int getIterationIndex() {
		return iterationIndex;
	}
	
	@Override
	protected void populateKeybindDisplay(ArrayList<Tuple2<String, String>> keyActionPairs) {
		keyActionPairs.add(new Tuple2<String, String>("N", "New Short Array."));
		keyActionPairs.add(new Tuple2<String, String>("B", "New Large Array."));
		keyActionPairs.add(new Tuple2<String, String>("ENTER", "Next Step."));
		keyActionPairs.add(new Tuple2<String, String>("C", "Change Color Scheme."));
	}
}

//
