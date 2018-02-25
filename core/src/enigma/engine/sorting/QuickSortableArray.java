package enigma.engine.sorting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.utilities.Tuple2;

public class QuickSortableArray extends SortableArray {
	private static final Color pivotColor = new Color(Color.PURPLE);
	private Stack<IndexInterval> callStack = new Stack<IndexInterval>();
	boolean allowStepSolver = true;

	// protected ArrayList<VisualColumn> selectedItems = new
	// ArrayList<VisualColumn>();
	protected HashSet<VisualColumn> selectedItems = new HashSet<VisualColumn>();

	public enum Stage {
		PICK_PIVOT, // select a pivot
		CACHE_PIVOT, // tuck away pivot to end/start of array
		LOW_TO_HIGH_SCAN, // scan from low to high for something larger than pivot.
		HIGH_TO_LOW_SCAN, // scan from high to for something smaller than pivot.
		SWAP, // swap the two incorrectly placed items found from scanning (and repeat)
		POSITION_PIVOT // restore pivot to its correct space.
	};

	private Stage stage = Stage.PICK_PIVOT;
	private VisualColumn cachedPivot = null;
	protected Marker lowMarker;
	protected Marker highMarker;
	protected VisualColumn heightChecker;


	protected boolean allowPromptUpdate = true;
	protected final String PIVOT_SELECT_PROMPT = "Pick the pivot in the middle of the region.";
	protected final String CACHE_PIVOT_PROMPT = "Move the pivot out of the way; swap it with a left element.";
	protected final String LOW_HIGH_PROMPT = "At the 'red' pointer, scan (label) rightward.";
	protected final String HIGH_LOW_PROMPT = "At the 'blue' pointer, scan (label) leftward.";
	protected final String SWAP_ELEMENTS_PROMPT = "Swap at the pointers (if needed).";
	protected final String POSITION_PIVOT_PROMPT = "Position (unhide) the pivot back into the array.";
	protected Sprite hidePivotIcon;
	protected Sprite swapElementsIcon;
	protected Sprite restorePivotIcon;
	private boolean drawIcons = true;
	
	protected boolean forceHidePrompts = false;
	protected boolean forceHideIcons = false;
	
	
	protected float intervalYOffset = 20f;
	protected float intervalHeight = 5f;
	protected Color intervalColor = new Color(Color.GRAY);

	/** Flag for special-cases/state. */
	boolean didAtleastOneLowToHigh = false;
	boolean atLeastOneCallHighToLow = false;
	
	public QuickSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		callStack.push(new IndexInterval(0, elements.size() - 1));

		lowMarker = configureMarker(Color.RED);
		highMarker = configureMarker(Color.BLUE);

		heightChecker = new VisualColumn(0, 0, 10, maxElementValue, MAX_HEIGHT, elementWidth);
		setDrawIterationMarker(false);
		setDrawStepMarker(false);

		instruction.setText(PIVOT_SELECT_PROMPT);
		configureIcons();
	}

	public QuickSortableArray(float x, float y, float elementWidth, int maxElementValue, int[] sourceArray) {
		super(x, y, elementWidth, maxElementValue, sourceArray);
		
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		callStack.push(new IndexInterval(0, elements.size() - 1));

		lowMarker = configureMarker(Color.RED);
		highMarker = configureMarker(Color.BLUE);

		heightChecker = new VisualColumn(0, 0, 10, maxElementValue, MAX_HEIGHT, elementWidth);
		setDrawIterationMarker(false);
		setDrawStepMarker(false);

		instruction.setText(PIVOT_SELECT_PROMPT);
		configureIcons();
	}
	
	@Override
	protected void populateKeybindDisplay(ArrayList<Tuple2<String, String>> keyActionPairs) {
		keyActionPairs.add(new Tuple2<String, String>("N", "New Short Array."));
		keyActionPairs.add(new Tuple2<String, String>("B", "New Large Array."));
		keyActionPairs.add(new Tuple2<String, String>("R", "Reverse Last Swap."));
		//keyActionPairs.add(new Tuple2<String, String>("Right Click", "Next Step / Check Move."));
		keyActionPairs.add(new Tuple2<String, String>("ENTER", "Next Step / Check Move."));
		keyActionPairs.add(new Tuple2<String, String>("H", "Hide Instructions Challenge."));
		keyActionPairs.add(new Tuple2<String, String>("J", "Hide Icons Challenge."));
	}
	
	private void configureIcons() {
		hidePivotIcon = new Sprite(TextureLookup.qsortHidePivotIcon);   
		swapElementsIcon = new Sprite(TextureLookup.qsortSwapQmarkIcon);
		restorePivotIcon = new Sprite(TextureLookup.qsortRestorePivotIcon);
		
		sizeAndPositionIcon(hidePivotIcon);
		sizeAndPositionIcon(swapElementsIcon);
		sizeAndPositionIcon(restorePivotIcon);
	}

	private void sizeAndPositionIcon(Sprite icon) {
		float size = 32;
		icon.setSize(size, size);
		icon.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() * 0.99f);
		//icon.translate(-size/2, -size/2);
		icon.translate(-size, -size);
		icon.setAlpha(0.5f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see enigma.engine.sorting.SortableArray#stepIndexComplete()
	 * 
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	@Override
	protected boolean stepIndexComplete() {
		return stepIndexComplete(allowStepSolver);
	}

	private boolean stepIndexComplete(boolean allowComputerSolving) {
		if (allowComputerSolving) {
			if (callStack.size() == 0) {
				return false;
			}

			IndexInterval currentCall = callStack.peek();
			if (stage == Stage.PICK_PIVOT) {
				return stepIndexComplete_PICK_PIVOT(currentCall);
			} else if (stage == Stage.CACHE_PIVOT) {
				return stepIndexComplete_CACHE_PIVOT(currentCall);
			} else if (stage == Stage.LOW_TO_HIGH_SCAN) {
				return stepIndexComplete_LOW_TO_HIGH_SCAN(currentCall);
			} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
				return stepIndexComplete_HIGH_TO_LOW_SCAN(currentCall);
			} else if (stage == Stage.SWAP) {
				return stepIndexComplete_SWAP(currentCall, false);
			} else if (stage == Stage.POSITION_PIVOT) {
				return stepIndexComplete_POSITION_PIVOT(currentCall, true);
			}
			return false;
		}
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_PICK_PIVOT(IndexInterval currentCall) {
		// BASE CASES
		if (currentCall.minRangeIndex > currentCall.maxRangeIndex) {
			callStack.pop();
			return true;
		}

		// Handle case where there are only two elements to choose from.
		if (currentCall.minRangeIndex + 1 == currentCall.maxRangeIndex
				|| currentCall.minRangeIndex == currentCall.maxRangeIndex) {
			int leftVal = elements.get(currentCall.minRangeIndex).getValue();
			int rightVal = elements.get(currentCall.maxRangeIndex).getValue();
			if (rightVal < leftVal) {
				swapWithAnimation(currentCall.minRangeIndex, currentCall.maxRangeIndex, true);
			}
			VisualColumn left = elements.get(currentCall.minRangeIndex);
			VisualColumn right = elements.get(currentCall.maxRangeIndex);
			left.setOverrideColor(pivotColor);
			right.setOverrideColor(pivotColor);
			left.setAlwaysForceColorOverride(true);
			right.setAlwaysForceColorOverride(true);

			callStack.pop();
			return true;
		}

		// handle case where pivot has not been chosen (and there are more than 2
		// elements in range)
		if (cachedPivot == null) {
			int middleIndex = getPivotIndex(currentCall);
			cachedPivot = elements.get(middleIndex);
			cachedPivot.setOverrideColor(pivotColor);
			cachedPivot.setAlwaysForceColorOverride(true);
			stage = Stage.CACHE_PIVOT;
			currentCall.middleIndex = middleIndex;

			setDrawIterationMarker(true);
			setMarkerToPosition(iterationMarker, middleIndex);
		}

		return false;
	}

	public int getPivotIndex(IndexInterval currentFrame) {
		return (currentFrame.maxRangeIndex - currentFrame.minRangeIndex) / 2 + currentFrame.minRangeIndex;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_CACHE_PIVOT(IndexInterval currentCall) {
		int pivotCacheIdx = getPivotCacheLocation(currentCall);

		elements.get(pivotCacheIdx).setOverrideColor(Color.GRAY);
		swapWithAnimation(currentCall.middleIndex, pivotCacheIdx, true); // maybe should not record history.
		stage = Stage.LOW_TO_HIGH_SCAN;

		setMarkerLERPToPosition(iterationMarker, pivotCacheIdx);
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_LOW_TO_HIGH_SCAN(IndexInterval currentCall) {
		if (!currentCall.highAndLowMarkersPlaced) {
			animateMarkersToPositionAndUpdateFrame(currentCall);
		} else { // DO LOW_TO_HIGH SCAN
			// CHECK IF STATE SHOULD CHANGE
			if (currentCall.curLowIdx > currentCall.maxRangeIndex) {
				stage = Stage.HIGH_TO_LOW_SCAN;
				if (didAtleastOneLowToHigh) {
					didAtleastOneLowToHigh = false;
					return stepIndexComplete(true);
				} else {
					return false;
				}
			}

			// SCAN TOWARDS HIGH ELEMENTS
			int lowIdx = currentCall.curLowIdx;
			VisualColumn eleAtLowIdx = elements.get(lowIdx);
			if (eleAtLowIdx.getValue() <= cachedPivot.getValue()) {
				// The low ptr points to valid element
				eleAtLowIdx.setOverrideColor(Color.RED);
				eleAtLowIdx.setAlwaysForceColorOverride(true);
				currentCall.curLowIdx++;
				setMarkerLERPToPosition(lowMarker, currentCall.curLowIdx);
				didAtleastOneLowToHigh = true;

				// if we're at the end of the array, go ahead and go to next move.
				if (lowIdx == currentCall.maxRangeIndex) {
					return stepIndexComplete();
				}

			} else {
				// The low ptr points to element that needs to be swapped to other side of
				// pivot.
				eleAtLowIdx.setOverrideColor(Color.BLUE);
				eleAtLowIdx.setAlwaysForceColorOverride(true);
				stage = Stage.HIGH_TO_LOW_SCAN;

				// clear skipping state
				didAtleastOneLowToHigh = false;
			}
		}
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_HIGH_TO_LOW_SCAN(IndexInterval currentCall) {
		// middle check and bounds check.
		if (currentCall.curHighIdx <= currentCall.minRangeIndex) {
			atLeastOneCallHighToLow = false;
			stage = Stage.SWAP;
			return false;
		}

		int highIdx = currentCall.curHighIdx;
		VisualColumn eleAtHighIdx = elements.get(highIdx);
		if (eleAtHighIdx.getValue() > cachedPivot.getValue()) {
			// if (eleAtHighIdx.getValue() >= cachedPivot.getValue()) { // my previous
			// implementation only has >
			// the high ptr points to a valid element.
			eleAtHighIdx.setOverrideColor(Color.BLUE);
			eleAtHighIdx.setAlwaysForceColorOverride(true);
			currentCall.curHighIdx--;
			setMarkerLERPToPosition(highMarker, currentCall.curHighIdx);

		} else {
			// the high ptr points to small element that should be on other side of pivot.
			eleAtHighIdx.setOverrideColor(Color.RED);
			eleAtHighIdx.setAlwaysForceColorOverride(true);
			stage = Stage.SWAP;

			// reset call at least once flag, since we're changing stage.
			boolean shouldCompleteNextStage = atLeastOneCallHighToLow;
			atLeastOneCallHighToLow = false;

			// if user already did this stage atleast once, this will redundantly wait.
			// go ahead and complete next stage for smooth UX.
			if (shouldCompleteNextStage) {
				// do next stage, but do not allow it to proceed passed swap
				return stepIndexComplete_SWAP(currentCall, true);
			}

		}

		atLeastOneCallHighToLow = true;
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_SWAP(IndexInterval currentCall, boolean disableStepSkip) {
		int newLowIdx = currentCall.curLowIdx + 1;
		int newHighIdx = currentCall.curHighIdx - 1;
		boolean swapOccured = false;

		// make sure that the swap indices are still on appropriate side of pivot.
		if (currentCall.curLowIdx < currentCall.curHighIdx) {
			swapWithAnimation(currentCall.curLowIdx, currentCall.curHighIdx, true);
			setMarkerLERPToPosition(lowMarker, newLowIdx);
			setMarkerLERPToPosition(highMarker, newHighIdx);
			swapOccured = true;
		}

		// this now checks the updated positions
		if (currentCall.curLowIdx < currentCall.curHighIdx) {
			// *do not* change these values before position pivot check
			// position pivot requires old values before swap.
			currentCall.curLowIdx++;
			currentCall.curHighIdx--;
			stage = Stage.LOW_TO_HIGH_SCAN;
		} else {
			stage = Stage.POSITION_PIVOT;

			if (!swapOccured && !disableStepSkip) {
				return stepIndexComplete(true);
			}
		}

		return false;
	}
	
	public boolean attemptSwapStep(boolean disableStepSkip) {
		
		if(stage == Stage.SWAP && callStack.size() != 0) {
			stepIndexComplete_SWAP(callStack.peek(), disableStepSkip);
			return true;
		}
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_POSITION_PIVOT(IndexInterval currentCall, boolean animate) {
		int pivotIndex = currentCall.curHighIdx;

		if (animate) {
			swapWithAnimation(pivotIndex, getPivotCacheLocation(currentCall), true);
		}
		callStack.pop(); // pop this call off of stack.

		IndexInterval right = new IndexInterval(pivotIndex + 1, currentCall.maxRangeIndex);
		IndexInterval left = new IndexInterval(currentCall.minRangeIndex, pivotIndex - 1);

		if (right.maxRangeIndex >= right.minRangeIndex) {
			callStack.push(right); // push right half
		}
		if (left.maxRangeIndex >= left.minRangeIndex) {
			callStack.push(left); // push left half (this makes right come first)
		}

		resetNonPurpleColors();
		cachedPivot = null;
		stage = Stage.PICK_PIVOT;
		setDrawIterationMarker(false);

		if (callStack.size() == 0) {
			// solution complete, turn remaining elements to purple
			for (VisualColumn element : elements) {
				element.setOverrideColor(pivotColor);
				element.setAlwaysForceColorOverride(true);
			}
		}

		return true;
	}

	private void resetNonPurpleColors() {
		for (VisualColumn element : elements) {
			if (!element.getOverrideColorReference().equals(pivotColor)) {
				element.setAlwaysForceColorOverride(false);
			}
		}

	}

	@Override
	public void draw(SpriteBatch batch) {
		if (callStack.size() != 0) {
			IndexInterval currentFrame = callStack.peek();

			Vector2 lower = arrayIndexPositions.get(currentFrame.minRangeIndex);
			Vector2 higher = arrayIndexPositions.get(currentFrame.maxRangeIndex);

			Color cacheColor = sr.getColor();
			float a = cacheColor.a;
			float b = cacheColor.b;
			float g = cacheColor.g;
			float r = cacheColor.r;
			sr.setColor(intervalColor.r, intervalColor.g, intervalColor.b, intervalColor.a);
			sr.rect(lower.x, lower.y - intervalYOffset, higher.x - lower.x + elementWidth, intervalHeight);
			sr.setColor(r, g, b, a);
		}
		super.draw(batch);
	}

	@Override
	public void logic() {
		super.logic();
		lowMarker.logic();
		highMarker.logic();
		heightChecker.logic();
		
		if(stage == Stage.LOW_TO_HIGH_SCAN) {
			lowMarker.setShouldFlash(true);
			highMarker.setShouldFlash(false);
		} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
			lowMarker.setShouldFlash(false);
			highMarker.setShouldFlash(true);
		}
		else {
			lowMarker.setShouldFlash(false);
			highMarker.setShouldFlash(false);
		}
	}

	protected void IO() {
		if (allowUserInput()) {
			if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || captureEnterEvent()) && dragTarget == null) {

				if (!lastMovePlayer) {
					nextSolveStep(true);
					lastMovePlayer = false;
				} else {
					lastMovePlayer = !compareUserAgainstSolution();
				}
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				reverseLastMove();
				reverseLastSolutionStep();
			}
			
			if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
				forceHidePrompts = !forceHidePrompts;
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
				forceHideIcons = !forceHideIcons;
			}
		}
	}

	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);

		if (callStack.size() > 0) {
			IndexInterval currentStackFrame = callStack.peek();
			if (currentStackFrame != null && currentStackFrame.highAndLowMarkersPlaced) {
				lowMarker.draw(batch);
				highMarker.draw(batch);
			}
		}

		if(!forceHidePrompts) {
			instruction.draw(batch);
		}

		if (drawIcons && !forceHideIcons) {
			switch (stage) {
			case CACHE_PIVOT:
				hidePivotIcon.draw(batch);
				break;
			case SWAP:
				swapElementsIcon.draw(batch);
				break;
			case POSITION_PIVOT:
				restorePivotIcon.draw(batch);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void drawPostSprites(SpriteBatch batch) {
		super.drawPostSprites(batch);

	}

	@Override
	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		VisualColumn result = super.attemptSwapWithOtherElement(draggedElement);

		return result;
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
		solution = new QuickSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
	}

	@Override
	protected void incrementIteration() {
		super.incrementIteration();
	}

	protected void updateInstruction() {
		if (allowPromptUpdate) {

			String previousPrompt = instruction.getText();

			switch (stage) {
			case PICK_PIVOT:
				if(callStack.size() == 0) {
					//gwt does not like String.format(...) for some reason.
					instruction.setText("Complete; press <space> for instructions.");
				} else if (callStack.peek().maxRangeIndex == callStack.peek().minRangeIndex + 1) {
					instruction.setText("Base Case: Swap the two elements if needed.");
				}else if (callStack.peek().maxRangeIndex == callStack.peek().minRangeIndex) {
					instruction.setText("Base Case: Only 1 element within array, select it.");
				}  
				else {
					instruction.setText(PIVOT_SELECT_PROMPT);
				}
				break;
			case CACHE_PIVOT:
				instruction.setText(CACHE_PIVOT_PROMPT);
				break;
			case LOW_TO_HIGH_SCAN:
				instruction.setText(LOW_HIGH_PROMPT);
				break;
			case HIGH_TO_LOW_SCAN:
				instruction.setText(HIGH_LOW_PROMPT);
				break;
			case SWAP:
				instruction.setText(SWAP_ELEMENTS_PROMPT);
				break;
			case POSITION_PIVOT:
				instruction.setText(POSITION_PIVOT_PROMPT);
				break;
			}

			if (!previousPrompt.equals(instruction.getText())) {
				instruction.startAnimation();
			}
		}
	}

	@Override
	public boolean nextSolveStep(boolean allowIncrementIteration) {
		boolean result = super.nextSolveStep(allowIncrementIteration);

		updateInstruction();

		return result;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		boolean result = super.touchDown(screenX, screenY, pointer, button, camera);

		// if right click was pressed, don't allow dragging. This will be captured on
		// next loop.
		if (enterEvent) {
			dragTarget = null;
		}

		VisualColumn selected = (VisualColumn) dragTarget;
		if (selected != null && callStack.size() != 0) {
			int selectedIndex = getIndex(selected);
			IndexInterval curFrame = callStack.peek();

			if (stage == Stage.PICK_PIVOT) {
				handleTouchDown_SELECT_PIVOT(curFrame, selectedIndex, selected);
			} else if (stage == Stage.LOW_TO_HIGH_SCAN) {
				handleTouchDown_LOW_TO_HIGH(curFrame, selectedIndex, selected);
			} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
				handleTouchDown_HIGH_TO_LOW(curFrame, selectedIndex, selected);
			}

			// for now, just always release the drag target.
			// dragTarget = null;

		}

		return result;
	}

	public void handleTouchDown_SELECT_PIVOT(IndexInterval curFrame, int selectedIndex, VisualColumn selected) {
		//special base case handling
		if (curFrame.minRangeIndex + 1 == curFrame.maxRangeIndex) {
			resetSelectedItemsWithoutRemoval();
			selectedItems.clear();
			lastMovePlayer = true;
			return;
		}
		
		
		// check that selection is within the active interval
		if (curFrame.indexInInterval(selectedIndex)) {
			resetSelectedItemsWithoutRemoval();
			selectedItems.clear();

			selected.setAlwaysForceColorOverride(true);
			selected.setOverrideColor(pivotColor);
			selectedItems.add(selected);
			lastMovePlayer = true;
		}
		dragTarget = null;
	}

	public void handleTouchDown_LOW_TO_HIGH(IndexInterval curFrame, int selectedIndex, VisualColumn selected) {
		if (!curFrame.highAndLowMarkersPlaced) {
			animateMarkersToPositionAndUpdateFrame(curFrame);
			dragTarget = null;
			return;
		}

		// check that selection is within the active interval
		if (curFrame.indexInInterval(selectedIndex) && selectedIndex != getPivotCacheLocation(curFrame)
				&& selectedIndex >= curFrame.curLowIdx) {
			if (updateTouchedElementDisplay(selected, Color.RED, Color.BLUE)) {
				selectedItems.add(selected);
			} else {
				selectedItems.remove(selected);
			}
			lastMovePlayer = true;
		}
		dragTarget = null;
	}

	public boolean updateTouchedElementDisplay(VisualColumn touched, Color firstColorCycleInSequence,
			Color secondColorCycleInSequence) {
		boolean touchedIsColored = false;

		if (!touched.shouldForceColorOverride()) {
			// element is not selected
			touched.setAlwaysForceColorOverride(true);
			touched.setOverrideColor(firstColorCycleInSequence);
			touchedIsColored = true;
		} else {
			// element is selected.
			if (touched.getOverrideColorReference().equals(firstColorCycleInSequence)) {
				// color was touched and made red, turn it to blue.
				touched.setOverrideColor(secondColorCycleInSequence);
				touchedIsColored = true;
			} else {
				// color is blue, make it unhighlighted.
				touched.setAlwaysForceColorOverride(false);
				touchedIsColored = false;
			}
		}

		return touchedIsColored;
	}

	public void handleTouchDown_HIGH_TO_LOW(IndexInterval curFrame, int selectedIndex, VisualColumn selected) {
		// boolean selectedBelongsToMinIdx =
		// selected.getOverrideColorReference().equals(Color.RED) &&
		// selected.shouldForceColorOverride();
		boolean selectedBelongsToMinIdx = selectedIndex <= curFrame.curLowIdx;

		// check that selection is within the active interval
		// if (curFrame.indexInInterval(selectedIndex) && selectedIndex !=
		// getPivotCacheLocation(curFrame)
		// && !selectedIsRed && selectedIndex <= curFrame.curHighIdx) {
		// // if user hasn't selected before, then add it as selection.
		// if (!selectedItems.contains(selected)) {
		// selected.setAlwaysForceColorOverride(true);
		// selected.setOverrideColor(Color.BLUE);
		// selectedItems.add(selected);
		// } else {
		// // user selected item previously selected, remove this item.
		// selected.setAlwaysForceColorOverride(false);
		// selectedItems.remove(selected);
		// }
		// lastMovePlayer = true;
		// }
		//
		// check that selection is within the active interval
		if (curFrame.indexInInterval(selectedIndex) && selectedIndex != getPivotCacheLocation(curFrame)
				&& !selectedBelongsToMinIdx && selectedIndex <= curFrame.curHighIdx) {
			if (updateTouchedElementDisplay(selected, Color.BLUE, Color.RED)) {
				selectedItems.add(selected);
			} else {
				selectedItems.remove(selected);
			}
			lastMovePlayer = true;
		}

		dragTarget = null;
	}

	private void resetSelectedItemsWithoutRemoval() {
		for (VisualColumn element : selectedItems) {
			element.setAlwaysForceColorOverride(false);
		}
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		return super.touchUp(screenX, screenY, pointer, button, camera);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera) {
		return super.touchDragged(screenX, screenY, pointer, camera);
	}

	@Override
	protected boolean compareUserAgainstSolution() {
		// computer should attempt to solve next move
		boolean nextMvCmp = false;

		if (callStack.size() != 0) {
			IndexInterval currentFrame = callStack.peek();
			if (stage == Stage.PICK_PIVOT) {
				nextMvCmp = handleCompare_PICK_PIVOT(currentFrame);
			} else if (stage == Stage.CACHE_PIVOT) {
				nextMvCmp = handleCompare_CACHE_PIVOT(currentFrame);
			} else if (stage == Stage.LOW_TO_HIGH_SCAN) {
				nextMvCmp = handleCompare_LOW_TO_HIGH(currentFrame);
			} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
				nextMvCmp = handleCompare_HIGH_TO_LOW(currentFrame);
			} else if (stage == Stage.SWAP) {
				nextMvCmp = handleCompare_SWAP(currentFrame);
			} else if (stage == Stage.POSITION_PIVOT) {
				nextMvCmp = handleCompare_POSITION_PIVOT(currentFrame);
			}
		}
		return nextMvCmp;
	}

	public boolean handleCompare_PICK_PIVOT(IndexInterval currentFrame) {

		int preCallStackSize = callStack.size();
		int correctPivotIdx = getPivotIndex(currentFrame);
		VisualColumn correctPivot = elements.get(correctPivotIdx);
		boolean foundElement = false;
		boolean correctlySwappedBaseCase = false;

		//special base case logic.
		if (currentFrame.minRangeIndex + 1 == currentFrame.maxRangeIndex) {
			// special base case (2 elements) handling
			// simply check that they are in the right order
			VisualColumn minEle = elements.get(currentFrame.minRangeIndex);
			VisualColumn maxEle = elements.get(currentFrame.maxRangeIndex);
			if (minEle.getValue() <= maxEle.getValue()) {
				correctlySwappedBaseCase = true;
			} 
		}
		
		resetSelectedItemsWithoutRemoval();
		if (selectedItems.contains(correctPivot) || correctlySwappedBaseCase) {
			foundElement = true;
			// and go to next step?
			greenCheck.startDimming();

			// the base cases (2 elements) will just pop off the stack in SELECT_PIVOT set,
			// So, we need to checkt to see if call stack was modified in loop
			while (stage == Stage.PICK_PIVOT && preCallStackSize == callStack.size()) {
				stepIndexComplete(true);
			}

			// clear selected if we skipped a stack frame
			if (preCallStackSize != callStack.size()) {
				selectedItems.clear();
			}

			updateInstruction();
		}
		if (!foundElement) {
			if (selectedItems.size() != 0) {
				// user made a move, but it was incorrect
				redX.startDimming();

				// allow next enter press to directly trigger solver
				if (allowStepSolver) {
					lastMovePlayer = false;
				}
				selectedItems.clear();
			} else {
				// user did not make a move, show solution
				stepIndexComplete();

				// signal that next move is to be done by the computer.
				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}

		// always clear moves before going to stage that allows movement.
		iterationMoveHistory.clear();
		return false;
	}

	/**
	 * @param currentFrame
	 * @return
	 */
	public boolean handleCompare_CACHE_PIVOT(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() == 0 && allowStepSolver) {
			// player did not make a move
			stepIndexComplete_CACHE_PIVOT(currentFrame);

			// if solver is allowed, this will return true.
			return allowStepSolver;
		}

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();

			if (iterationMoveHistory.size() != 0) {
				return false;
			} else {
				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}

		if (iterationMoveHistory.size() == 1) {
			MoveHistoryEntry hist = iterationMoveHistory.peek();

			// check if user placed pivot in the correct place.
			if (hist.toIndex == getPivotCacheLocation(currentFrame) && hist.fromIndex == getPivotIndex(currentFrame)
					|| hist.fromIndex == getPivotCacheLocation(currentFrame)
							&& hist.toIndex == getPivotIndex(currentFrame)) {
				greenCheck.startDimming();
				setMarkerLERPToPosition(iterationMarker, currentFrame.minRangeIndex);
				stage = Stage.LOW_TO_HIGH_SCAN;
				VisualColumn cachedPivot = elements.get(getPivotCacheLocation(currentFrame));
				selectedItems.remove(cachedPivot);
				iterationMoveHistory.clear();

				// call next step to get arrows positioned.
				stepIndexComplete(true);

				updateInstruction();
			} else {
				redX.startDimming();
				reverseLastMove();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}
		return false;
	}

	private boolean handleCompare_LOW_TO_HIGH(IndexInterval currentFrame) {
		boolean readyForNextStage = false;
		int lowIdx = currentFrame.curLowIdx;
		VisualColumn eleAtLowIdx = elements.get(lowIdx);

		if (selectedItems.size() == 0 && currentFrame.curLowIdx == currentFrame.minRangeIndex + 1 && allowStepSolver) {
			// user hasn't selected anything, they must want to see solution.
			stepIndexComplete_LOW_TO_HIGH_SCAN(currentFrame);

			// if solver is allowed, this will return true.
			return allowStepSolver;
		}

		// ELEMENT SHOULD BE RED
		if (eleAtLowIdx.getValue() <= cachedPivot.getValue()) {
			// check that the color correct and that it is currently displaying the color.
			if (eleAtLowIdx.getOverrideColorReference().equals(Color.RED) && eleAtLowIdx.shouldForceColorOverride()) {
				// User correctly selected red!
				greenCheck.startDimming();

				// update visuals
				currentFrame.curLowIdx++;
				setMarkerLERPToPosition(lowMarker, currentFrame.curLowIdx);

				if (currentFrame.curLowIdx >= currentFrame.maxRangeIndex) {
					readyForNextStage = true;
				}
			} else {
				// User does not have correct element highlighted.
				redX.startDimming();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}

		} else {
			// ELEMENT SHOULD BE BLUE!
			if (eleAtLowIdx.getOverrideColorReference().equals(Color.BLUE) && eleAtLowIdx.shouldForceColorOverride()) {
				// User correctly selected blue!
				greenCheck.startDimming();

				// pointer should not move
				eleAtLowIdx.setOverrideColor(Color.BLUE);
				eleAtLowIdx.setAlwaysForceColorOverride(true);
				readyForNextStage = true;
			} else {
				// User does not have correct element highlighted.
				redX.startDimming();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}

		// check that they did not highlight elements below the stopping/swap point.
		if (readyForNextStage) {
			for (int idx = currentFrame.curLowIdx + 1; idx <= currentFrame.maxRangeIndex
					&& idx <= currentFrame.curHighIdx; idx++) {
				VisualColumn ele = elements.get(idx);
				if (ele.shouldForceColorOverride()) {
					ele.setAlwaysForceColorOverride(false);
					redX.startDimming();
					greenCheck.cancelDimming();
					readyForNextStage = false;

					// note: this only occurs when the user should be ready for next stage (other
					// than extra high lights)
					// so, we did to de-color the swapping point.
					eleAtLowIdx.setAlwaysForceColorOverride(false);
				}
			}
		}

		readyForNextStage |= currentFrame.curLowIdx == currentFrame.maxRangeIndex;
		if (readyForNextStage) {
			// We're at the end of the array, force the next step (high to low), but rarely
			// something else
			while (stage == Stage.LOW_TO_HIGH_SCAN) {
				stepIndexComplete(true);
			}
			updateInstruction();
			// resetSelectedItems(); //this will cause selected items to lose color.
		}
		return false;
	}

	private boolean handleCompare_HIGH_TO_LOW(IndexInterval currentFrame) {
		boolean readyForNextStage = false;
		int highIdx = currentFrame.curHighIdx;
		VisualColumn eleAtHighIdx = elements.get(highIdx);

		if (selectedItems.size() == 0 && currentFrame.curHighIdx == currentFrame.maxRangeIndex && allowStepSolver) {
			stepIndexComplete_HIGH_TO_LOW_SCAN(currentFrame);

			// if solver is allowed, this will return true.
			return allowStepSolver;
		}

		// ELEMENT SHOULD BE BLUE
		if (eleAtHighIdx.getValue() > cachedPivot.getValue()) {
			// check that the color correct and that it is currently displaying the color.
			if (eleAtHighIdx.getOverrideColorReference().equals(Color.BLUE)
					&& eleAtHighIdx.shouldForceColorOverride()) {
				// User correctly selected red!
				greenCheck.startDimming();

				// update visuals
				currentFrame.curHighIdx--;
				setMarkerLERPToPosition(highMarker, currentFrame.curHighIdx);
			} else {
				// User does not have correct element highlighted.
				redX.startDimming();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}

		} else {
			// ELEMENT SHOULD BE RED! (no color for user)
			// if (!eleAtHighIdx.shouldForceColorOverride() ||
			// eleAtHighIdx.getOverrideColorReference().equals(Color.RED)) {
			if (eleAtHighIdx.shouldForceColorOverride() && eleAtHighIdx.getOverrideColorReference().equals(Color.RED)) {
				// User correctly selected red!
				greenCheck.startDimming();

				// pointer should not move
				eleAtHighIdx.setOverrideColor(Color.RED);
				eleAtHighIdx.setAlwaysForceColorOverride(true);
				readyForNextStage = true;
			} else {
				// User does not have correct element highlighted.
				redX.startDimming();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}

		// check that they did not highlight elements below the stopping/swap point.
		if (readyForNextStage) {
			for (int idx = currentFrame.curHighIdx - 1; idx > currentFrame.minRangeIndex
					&& idx > currentFrame.curLowIdx; idx--) {
				VisualColumn ele = elements.get(idx);
				if (ele.shouldForceColorOverride()) {
					ele.setAlwaysForceColorOverride(false);
					redX.startDimming();
					greenCheck.cancelDimming();
					readyForNextStage = false;
					eleAtHighIdx.setAlwaysForceColorOverride(false);
				}
			}
		}

		// if they're at the last index, then they're automatically ready for next
		// stage.
		readyForNextStage |= currentFrame.curHighIdx == currentFrame.minRangeIndex;
		if (readyForNextStage) {
			// We're at the end of the array, force the next step (high to low)
			// while(stage != Stage.SWAP && stage != Stage.POSITION_PIVOT) {
			while (stage == Stage.HIGH_TO_LOW_SCAN) {
				// set flag to false so correction doesn't automatically do next swap step.
				atLeastOneCallHighToLow = false;
				stepIndexComplete_HIGH_TO_LOW_SCAN(currentFrame);
				// stepIndexComplete();
			}
			updateInstruction();

		}
		return false;
	}

	public boolean handleCompare_SWAP(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();
			return false;
		}

		if (iterationMoveHistory.size() == 1 || iterationMoveHistory.size() == 0) {
			boolean correct = false;

			// check that high and low ptrs are what the user swapped.
			if (iterationMoveHistory.size() == 1) {
				MoveHistoryEntry hist = iterationMoveHistory.peek();
				correct |= hist.toIndex == currentFrame.curHighIdx && hist.fromIndex == currentFrame.curLowIdx
						|| hist.toIndex == currentFrame.curLowIdx && hist.fromIndex == currentFrame.curHighIdx;
			}

			// if the indices are passed one another, no swapping is needed
			if (currentFrame.curHighIdx <= currentFrame.curLowIdx) {
				// make sure they didn't make a move in this case
				if (iterationMoveHistory.size() == 0) {
					correct = true;
				} else {
					correct = false;
				}
			}

			if (correct) {
				greenCheck.startDimming();

				// Check if there is more swapping to be done
				if (currentFrame.curLowIdx < currentFrame.curHighIdx) {
					boolean ptrsWereAdjacent = currentFrame.curLowIdx == currentFrame.curHighIdx - 1;

					// *do not* change these values before position pivot check
					// position pivot requires old values before swap.
					currentFrame.curLowIdx++;
					currentFrame.curHighIdx--;

					// (note: if swapped indices adjacent, go to position pivot after ptr
					// increments)
					if (ptrsWereAdjacent) {
						setMarkerLERPToPosition(lowMarker, currentFrame.curLowIdx);
						setMarkerLERPToPosition(highMarker, currentFrame.curHighIdx);
						stage = Stage.POSITION_PIVOT;
					} else {
						lerpMarkersToCorrectPositions(currentFrame);
						stage = Stage.LOW_TO_HIGH_SCAN;
					}

				} else {
					stage = Stage.POSITION_PIVOT;
				}
				iterationMoveHistory.clear();

				updateInstruction();
			} else {
				redX.startDimming();
				reverseLastMove();

				// if they only did 1 move, then allow solving on next step
				if (allowStepSolver) {
					return iterationMoveHistory.size() <= 1;
				} else {
					return false;
				}

			}
		}
		return false;
	}

	private boolean handleCompare_POSITION_PIVOT(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();
			return false;
		}

		if (iterationMoveHistory.size() == 1 || iterationMoveHistory.size() == 0) {
			boolean correct = false;

			// user did not make a move, show them solution
			if (iterationMoveHistory.size() == 0) {
				if (allowStepSolver) {
					stepIndexComplete_POSITION_PIVOT(currentFrame, true);
					return true;
				} else {
					redX.startDimming();
					return false;
				}
			}

			// NOTE: it shouldn't be possible for the correct pivot loc to be where we
			// cached it.
			// if pivot is already in the correct position, then don't require a swap.
			// correct |= getPivotCacheLocation(currentFrame) == currentFrame.curHighIdx &&
			// iterationMoveHistory.size() == 0;

			if (iterationMoveHistory.size() == 1) {
				MoveHistoryEntry hist = iterationMoveHistory.peek();
				correct |= hist.toIndex == getPivotCacheLocation(currentFrame)
						&& hist.fromIndex == currentFrame.curHighIdx
						|| hist.toIndex == currentFrame.curHighIdx
								&& hist.fromIndex == getPivotCacheLocation(currentFrame);
			}

			if (correct) {
				greenCheck.startDimming();
				stepIndexComplete_POSITION_PIVOT(currentFrame, false);
				updateInstruction();
				VisualColumn cachedPivot = elements.get(getPivotCacheLocation(currentFrame));
				selectedItems.remove(cachedPivot);
				resetSelectedItemsWithoutRemoval();
				selectedItems.clear();
			} else {
				redX.startDimming();
				reverseLastMove();

				// if solver is allowed, this will return true.
				return allowStepSolver;
			}
		}
		return false;
	}

	private int getPivotCacheLocation(IndexInterval currentFrame) {
		return currentFrame.minRangeIndex;
	}

	// private boolean checkMarkersValid(IndexInterval currentFrame) {
	// return lowMarker.getPointingIndexLocation() == currentFrame.curLowIdx
	// && highMarker.getPointingIndexLocation() == currentFrame.curHighIdx;
	// }

	private void lerpMarkersToCorrectPositions(IndexInterval currentFrame) {
		setMarkerLERPToPosition(lowMarker, currentFrame.curLowIdx);
		setMarkerLERPToPosition(highMarker, currentFrame.curHighIdx);
	}

	private void animateMarkersToPositionAndUpdateFrame(IndexInterval currentCall) {
		setMarkerToPosition(lowMarker, currentCall.minRangeIndex + 1);
		setMarkerToPosition(highMarker, currentCall.maxRangeIndex);
		lowMarker.translate(-Gdx.graphics.getWidth() / 2, 0);
		highMarker.translate(Gdx.graphics.getWidth() / 2, 0);

		setMarkerLERPToPosition(lowMarker, currentCall.minRangeIndex + 1);
		setMarkerLERPToPosition(highMarker, currentCall.maxRangeIndex);

		currentCall.highAndLowMarkersPlaced = true;
	}

	@Override
	public void overrideInstructionTo(String overrideText) {
		this.instruction.setText(overrideText);
		this.instruction.startAnimation();
	}

	@Override
	public TutorialManager getTutorialManager(float x, float y, float elementWidth, int numElements,
			int maxElementValue) {
		return new TutorialManagerQSort(x, y, elementWidth, numElements, maxElementValue);
	}

	public void setToggleSolutionSolver(boolean enableOrDisable) {
		allowStepSolver = enableOrDisable;
	}

	public Stage getStage() {
		return this.stage;
	}

	public void togglePromptUpdates(boolean onOrOff) {
		allowPromptUpdate = onOrOff;
	}

	public int getCurrentLowPtr() {
		if(callStack.size() != 0) {
			IndexInterval peek = callStack.peek();
			return peek.curLowIdx;
		}
		return -1;
	}

	public int getCurrentHighPtr() {
		if(callStack.size() != 0) {
			IndexInterval peek = callStack.peek();
			return peek.curHighIdx;
		}
		return -1;
	}

	public IndexInterval peekCurrentStackFrame() {
		if(callStack.size() != 0)
		{
			return callStack.peek();
		}
		return null;
	}
}

// ------------------------- HELPER CLASSES ----------------------------------

class IndexInterval {
	// valid interval that this recursive call to quick sort will take place over.
	public int minRangeIndex;
	public int maxRangeIndex;

	// variables that would be local to stackframe.
	public int middleIndex;
	public int curLowIdx;
	public int curHighIdx;

	// variables to help with animation.
	public boolean highAndLowMarkersPlaced = false;

	public IndexInterval(int inclusiveLower, int inclusiveHigher) {
		minRangeIndex = inclusiveLower;
		curLowIdx = minRangeIndex + 1;

		maxRangeIndex = inclusiveHigher;
		curHighIdx = maxRangeIndex;
	}

	public boolean indexInInterval(int testIdx) {
		return minRangeIndex <= testIdx && maxRangeIndex >= testIdx;
	}
}

//
