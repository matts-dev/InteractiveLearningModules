package enigma.engine.sorting;

import java.util.HashSet;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.utilities.LERPSprite;

public class QuickSortableArray extends SortableArray {
	private static final Color pivotColor = Color.PURPLE;
	private Stack<IndexInterval> callStack = new Stack<IndexInterval>();

	// protected ArrayList<VisualColumn> selectedItems = new
	// ArrayList<VisualColumn>();
	protected HashSet<VisualColumn> selectedItems = new HashSet<VisualColumn>();

	private enum Stage {
		PICK_PIVOT, // select a pivot
		CACHE_PIVOT, // tuck away pivot to end/start of array
		LOW_TO_HIGH_SCAN, // scan from low to high for something larger than pivot.
		HIGH_TO_LOW_SCAN, // scan from high to for something smaller than pivot.
		SWAP, // swap the two incorrectly placed items found from scanning (and repeat)
		POSITION_PIVOT // restore pivot to its correct space.
	};

	private Stage stage = Stage.PICK_PIVOT;
	private VisualColumn cachedPivot = null;
	protected LERPSprite lowMarker;
	protected LERPSprite highMarker;
	protected VisualColumn heightChecker;

	protected final String PIVOT_SELECT_PROMPT = "Pick a pivot.";
	protected final String CACHE_PIVOT_PROMPT = "Move the pivot out of the way; swap it with something.";
	protected final String LOW_HIGH_PROMPT = "Scan over elements smaller than pivot.";
	protected final String HIGH_LOW_PROMPT = "Scan over elements larger than pivot.";
	protected final String SWAP_ELEMENTS_PROMPT = "Swap incorrectly positioned elements, if needed.";
	protected final String POSITION_PIVOT_PROMPT = "Position the pivot back into the array.";

	protected float intervalYOffset = 20f;
	protected float intervalHeight = 5f;
	protected Color intervalColor = Color.GRAY; 
	
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

		instruction.setText("Pick a pivot.");
	}

	boolean didAtleastOneLowToHigh = false;
	boolean atLeastOneCallHighToLow = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see enigma.engine.sorting.SortableArray#stepIndexComplete()
	 * 
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	@Override
	protected boolean stepIndexComplete() {
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
			return stepIndexComplete_SWAP(currentCall);
		} else if (stage == Stage.POSITION_PIVOT) {
			return stepIndexComplete_POSITION_PIVOT(currentCall, true);
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
			setMarkerToPosition(lowMarker, currentCall.minRangeIndex + 1);
			setMarkerToPosition(highMarker, currentCall.maxRangeIndex);
			lowMarker.translate(-Gdx.graphics.getWidth() / 2, 0);
			highMarker.translate(Gdx.graphics.getWidth() / 2, 0);

			setMarkerLERPToPosition(lowMarker, currentCall.minRangeIndex + 1);
			setMarkerLERPToPosition(highMarker, currentCall.maxRangeIndex);

			currentCall.highAndLowMarkersPlaced = true;
		} else { // DO LOW_TO_HIGH SCAN
			// CHECK IF STATE SHOULD CHANGE
			if (currentCall.curLowIdx > currentCall.maxRangeIndex) {
				stage = Stage.HIGH_TO_LOW_SCAN;
				if (didAtleastOneLowToHigh) {
					didAtleastOneLowToHigh = false;
					return stepIndexComplete();
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

			// if user has already did this stage at least once, then this will redundantly
			// wait.
			// go ahead and complete next stage for smooth UX.
			if (shouldCompleteNextStage) {
				return stepIndexComplete();
			}

		}

		atLeastOneCallHighToLow = true;
		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	/**
	 * @param currentCall
	 * @return
	 */
	private boolean stepIndexComplete_SWAP(IndexInterval currentCall) {
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

			if (!swapOccured) {
				return stepIndexComplete();
			}
		}

		return false;
	}

	/**
	 * @param currentCall
	 * @return true if this iteration/stackframe is complete; false otherwise.
	 */
	private boolean stepIndexComplete_POSITION_PIVOT(IndexInterval currentCall, boolean animate) {
		int pivotIndex = currentCall.curHighIdx;
		
		if(animate) {
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
		if(callStack.size() != 0) {
			IndexInterval currentFrame = callStack.peek();
			
			
			Vector2 lower = arrayIndexPositions.get(currentFrame.minRangeIndex);
			Vector2 higher = arrayIndexPositions.get(currentFrame.maxRangeIndex);
			
			Color cacheColor = sr.getColor();
			float a = cacheColor.a;
			float b = cacheColor.b;
			float g = cacheColor.g;
			float r = cacheColor.r;
			sr.setColor(intervalColor);
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
	}

	protected void IO() {
		if (allowUserInput()) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && dragTarget == null) {

				if (!lastMovePlayer) {
					nextSolveStep(true);
					lastMovePlayer = false;
				} else {

					lastMovePlayer = !compareUserAgainstSolution();
					//
					// if(!lastMovePlayer) {
					// //if comparing against solution didn't update any animations (returns false),
					// //then user was wanting to see next step.
					// nextSolveStep(true);
					// }
				}
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				reverseLastMove();
				reverseLastSolutionStep();
			}
		}
	}

	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);

		if (callStack.size() > 0) {
			IndexInterval currentStackFrame = callStack.peek();
			if (currentStackFrame != null && currentStackFrame.highAndLowMarkersPlaced) {
				highMarker.draw(batch);
				lowMarker.draw(batch);
			}
		}

		instruction.draw(batch);
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
		String previousPrompt = instruction.getText();

		switch (stage) {
		case PICK_PIVOT:
			instruction.setText(PIVOT_SELECT_PROMPT);
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

	@Override
	public boolean nextSolveStep(boolean allowIncrementIteration) {
		boolean result = super.nextSolveStep(allowIncrementIteration);

		updateInstruction();

		return result;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		boolean result = super.touchDown(screenX, screenY, pointer, button, camera);

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
		// check that selecion is within the active interval
		if (curFrame.indexInInterval(selectedIndex)) {
			resetSelectedItems();
			selected.setAlwaysForceColorOverride(true);
			selected.setOverrideColor(pivotColor);
			selectedItems.add(selected);
			lastMovePlayer = true;
		}
		dragTarget = null;
	}

	public void handleTouchDown_LOW_TO_HIGH(IndexInterval curFrame, int selectedIndex, VisualColumn selected) {
		// check that selection is within the active interval
		if (curFrame.indexInInterval(selectedIndex) && selectedIndex != getPivotCacheLocation(curFrame)) {
			// if user hasn't selected before, then add it as selection.
			if (!selectedItems.contains(selected)) {
				selected.setAlwaysForceColorOverride(true);
				selected.setOverrideColor(Color.RED);
				selectedItems.add(selected);
			} else {
				// user selected item previously selected, remove this item.
				selected.setAlwaysForceColorOverride(false);
				selectedItems.remove(selected);
			}
			lastMovePlayer = true;
		}

		dragTarget = null;
	}

	public void handleTouchDown_HIGH_TO_LOW(IndexInterval curFrame, int selectedIndex, VisualColumn selected) {
		boolean selectedIsRed = selected.getOverrideColorReference().equals(Color.RED)
				&& selected.shouldForceColorOverride();

		// check that selection is within the active interval
		if (curFrame.indexInInterval(selectedIndex) && selectedIndex != getPivotCacheLocation(curFrame)
				&& !selectedIsRed) {
			// if user hasn't selected before, then add it as selection.
			if (!selectedItems.contains(selected)) {
				selected.setAlwaysForceColorOverride(true);
				selected.setOverrideColor(Color.BLUE);
				selectedItems.add(selected);
			} else {
				// user selected item previously selected, remove this item.
				selected.setAlwaysForceColorOverride(false);
				selectedItems.remove(selected);
			}
			lastMovePlayer = true;
		}

		dragTarget = null;
	}

	private void resetSelectedItems() {
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
		boolean shouldFlagNextMoveComputer = false;

		if (callStack.size() != 0) {
			IndexInterval currentFrame = callStack.peek();
			if (stage == Stage.PICK_PIVOT) {
				handleCompare_PICK_PIVOT(currentFrame);
			} else if (stage == Stage.CACHE_PIVOT) {
				handleCompare_CACHE_PIVOT(currentFrame);
			} else if (stage == Stage.LOW_TO_HIGH_SCAN) {
				handleCompare_LOW_TO_HIGH(currentFrame);
			} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
				handleCompare_HIGH_TO_LOW(currentFrame);
			} else if (stage == Stage.SWAP) {
				handleCompare_SWAP(currentFrame);
			} else if (stage == Stage.POSITION_PIVOT) {
				handleCompare_POSITION_PIVOT(currentFrame);
			}
		}
		return shouldFlagNextMoveComputer;
	}

	public void handleCompare_PICK_PIVOT(IndexInterval currentFrame) {
		int correctPivotIdx = getPivotIndex(currentFrame);
		VisualColumn correctPivot = elements.get(correctPivotIdx);

		resetSelectedItems();
		boolean foundElement = false;
		if (selectedItems.contains(correctPivot)) {
			foundElement = true;
			// and go to next step?
			greenCheck.startDimming();
			while (stage == Stage.PICK_PIVOT) {
				stepIndexComplete();
			}
			updateInstruction();
		}
		if (!foundElement) {
			// show red x
			redX.startDimming();
		}

		// always clear moves before going to stage that allows movement.
		iterationMoveHistory.clear();
	}

	/**
	 * @param currentFrame
	 */
	public void handleCompare_CACHE_PIVOT(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();
			return;
		}

		if (iterationMoveHistory.size() == 1) {
			MoveHistoryEntry hist = iterationMoveHistory.peek();

			// check if user placed pivot in the correct place.
			if (hist.toIndex == getPivotCacheLocation(currentFrame) && hist.fromIndex == getPivotIndex(currentFrame)
					|| hist.fromIndex == getPivotCacheLocation(currentFrame) && hist.toIndex == getPivotIndex(currentFrame)
					) {
				greenCheck.startDimming();
				setMarkerLERPToPosition(iterationMarker, currentFrame.minRangeIndex);
				stage = Stage.LOW_TO_HIGH_SCAN;
				VisualColumn cachedPivot = elements.get(getPivotCacheLocation(currentFrame));
				selectedItems.remove(cachedPivot);
				iterationMoveHistory.clear();

				// call next step to get arrows positioned.
				stepIndexComplete();

				updateInstruction();
			} else {
				redX.startDimming();
				reverseLastMove();
			}
		}
	}

	private void handleCompare_LOW_TO_HIGH(IndexInterval currentFrame) {
		boolean readyForNextStage = false;
		int lowIdx = currentFrame.curLowIdx;
		VisualColumn eleAtLowIdx = elements.get(lowIdx);
		
		//ELEMENT SHOULD BE RED
		if (eleAtLowIdx.getValue() <= cachedPivot.getValue()) {
			//check that the color correct and that it is currently displaying the color.
			if(eleAtLowIdx.getOverrideColorReference().equals(Color.RED) && eleAtLowIdx.shouldForceColorOverride()) {
				//User correctly selected red!
				greenCheck.startDimming();

				//update visuals
				currentFrame.curLowIdx++;
				setMarkerLERPToPosition(lowMarker, currentFrame.curLowIdx);
				
				if(currentFrame.curLowIdx >= currentFrame.maxRangeIndex) {
					readyForNextStage = true;
				}
			} else {
				//User does not have correct element highlighted.
				redX.startDimming();
			}

		} else {
			//ELEMENT SHOULD BE BLUE! (no color for user)
			if(!eleAtLowIdx.shouldForceColorOverride()) {
				//User correctly selected red!
				greenCheck.startDimming();

				//pointer should not move
				eleAtLowIdx.setOverrideColor(Color.BLUE);
				eleAtLowIdx.setAlwaysForceColorOverride(true);
				readyForNextStage = true;
			} else {
				//User does not have correct element highlighted.
				redX.startDimming();
			}
		}
		
		//check that they did not highlight elements below the stopping/swap point.
		if(readyForNextStage) {
			for(int idx = currentFrame.curLowIdx + 1; idx < currentFrame.maxRangeIndex && idx < currentFrame.curHighIdx; idx++) {
				VisualColumn ele = elements.get(idx);
				if(ele.shouldForceColorOverride()) {
					ele.setAlwaysForceColorOverride(false);
					redX.startDimming();
					greenCheck.cancelDimming();
					readyForNextStage = false;
					
					//note: this only occurs when the user should be ready for next stage (other than extra high lights)
					//so, we did to de-color the swapping point.
					eleAtLowIdx.setAlwaysForceColorOverride(false); 
				}
			}
		}
		
		
		readyForNextStage |= currentFrame.curLowIdx == currentFrame.maxRangeIndex;  
		if (readyForNextStage) {
			// We're at the end of the array, force the next step (high to low), but rarely something else
			while(stage == Stage.LOW_TO_HIGH_SCAN) {
				stepIndexComplete();
			}
			updateInstruction();
			//resetSelectedItems(); //this will cause selected items to lose color.
		}
	}

	private void handleCompare_HIGH_TO_LOW(IndexInterval currentFrame) {
		boolean readyForNextStage = false;
		int highIdx = currentFrame.curHighIdx;
		VisualColumn eleAtHighIdx = elements.get(highIdx);

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
			}

		} else {
			// ELEMENT SHOULD BE RED! (no color for user)
			if (!eleAtHighIdx.shouldForceColorOverride() || eleAtHighIdx.getOverrideColorReference().equals(Color.RED)) {
				// User correctly selected red!
				greenCheck.startDimming();

				// pointer should not move
				eleAtHighIdx.setOverrideColor(Color.RED);
				eleAtHighIdx.setAlwaysForceColorOverride(true);
				readyForNextStage = true;
			} else {
				// User does not have correct element highlighted.
				redX.startDimming();
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

		//if they're at the last index, then they're automatically ready for next stage.
		readyForNextStage |= currentFrame.curHighIdx == currentFrame.minRangeIndex; 
		if (readyForNextStage) {
			// We're at the end of the array, force the next step (high to low)
			// while(stage != Stage.SWAP && stage != Stage.POSITION_PIVOT) {
			while (stage == Stage.HIGH_TO_LOW_SCAN) {
				stepIndexComplete();
			}
			updateInstruction();

		}
	}

	public void handleCompare_SWAP(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();
			return;
		}

		if (iterationMoveHistory.size() == 1 || iterationMoveHistory.size() == 0) {
			boolean correct = false;
			
			//if the indices are passed one another, no swapping is needed (also make sure they didn't make a move)
			correct |= currentFrame.curHighIdx <= currentFrame.curLowIdx && iterationMoveHistory.size() == 0;

			if (iterationMoveHistory.size() == 1) {
				MoveHistoryEntry hist = iterationMoveHistory.peek();
				correct |= hist.toIndex == currentFrame.curHighIdx && hist.fromIndex == currentFrame.curLowIdx
						|| hist.toIndex == currentFrame.curLowIdx && hist.fromIndex == currentFrame.curHighIdx;
			}

			if (correct) {
				greenCheck.startDimming();

				// -----
				if (currentFrame.curLowIdx < currentFrame.curHighIdx) {
					// *do not* change these values before position pivot check
					// position pivot requires old values before swap.
					currentFrame.curLowIdx++;
					currentFrame.curHighIdx--;
					stage = Stage.LOW_TO_HIGH_SCAN;
				} else {
					stage = Stage.POSITION_PIVOT;
				}
				iterationMoveHistory.clear();
				// ------------

				updateInstruction();
			} else {
				redX.startDimming();
				reverseLastMove();
			}
		}
	}

	/**
	 * @param currentFrame
	 */
	private void handleCompare_POSITION_PIVOT(IndexInterval currentFrame) {
		continueReversingUserMoves = false;

		if (iterationMoveHistory.size() > 1) {
			redX.startDimming();
			continueReversingUserMoves = true;
			reverseLastMove();
			return;
		}

		if (iterationMoveHistory.size() == 1 || iterationMoveHistory.size() == 0) {
			boolean correct = false;
			//correct |= currentFrame...

			if (iterationMoveHistory.size() == 1) {
				MoveHistoryEntry hist = iterationMoveHistory.peek();
				correct |= hist.toIndex == getPivotCacheLocation(currentFrame) && hist.fromIndex == currentFrame.curHighIdx
						|| hist.toIndex == currentFrame.curHighIdx && hist.fromIndex == getPivotCacheLocation(currentFrame);
			}

			if (correct) {
				greenCheck.startDimming();
				stepIndexComplete_POSITION_PIVOT(currentFrame, false);
				updateInstruction();
				VisualColumn cachedPivot = elements.get(getPivotCacheLocation(currentFrame));
				selectedItems.remove(cachedPivot);
				resetSelectedItems();
			} else {
				redX.startDimming();
				reverseLastMove();
			}
		}

	}

	private int getPivotCacheLocation(IndexInterval currentFrame) {
		return currentFrame.minRangeIndex;
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
