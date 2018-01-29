package enigma.engine.sorting;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.utilities.LERPSprite;

public class QuickSortableArray extends SortableArray{
	private static final Color PivotColor = Color.PURPLE;
	private Stack<IndexInterval> callStack = new Stack<IndexInterval>();
	private enum Stage { 
		PICK_PIVOT, //select a pivot
		CACHE_PIVOT, //tuck away pivot to end/start of array
		LOW_TO_HIGH_SCAN, //scan from low to high for something larger than pivot.
		HIGH_TO_LOW_SCAN, //scan from high to for something smaller than pivot.
		SWAP, //swap the two incorrectly placed items found from scanning (and repeat)
		POSITION_PIVOT //restore pivot to its correct space.  
	};
	private Stage stage = Stage.PICK_PIVOT;
	private VisualColumn cachedPivot = null;
	protected LERPSprite lowMarker;
	protected LERPSprite highMarker;
	protected VisualColumn heightChecker;
	
	public QuickSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		callStack.push(new IndexInterval(0, elements.size()-1));
		
		lowMarker = configureMarker(Color.RED);
		highMarker= configureMarker(Color.BLUE);
		
		heightChecker = new VisualColumn(0, 0, 10, maxElementValue, MAX_HEIGHT, elementWidth);
	}
	
	@Override
	protected boolean stepIndexComplete() {
		if(callStack.size() == 0) {
			return false;
		}
		
		IndexInterval currentCall = callStack.peek();
		if(stage == Stage.PICK_PIVOT) {
			//BASE CASES
			if(currentCall.minRangeIndex >= currentCall.maxRangeIndex) {
				callStack.pop();
				return true;
			}
			if(currentCall.minRangeIndex + 1 == currentCall.maxRangeIndex) {
				int leftVal = elements.get(currentCall.minRangeIndex).getValue();
				int rightVal = elements.get(currentCall.maxRangeIndex).getValue();
				if(rightVal < leftVal) {
					swapWithAnimation(currentCall.minRangeIndex, currentCall.maxRangeIndex, true);
				}
				VisualColumn left = elements.get(currentCall.minRangeIndex);
				VisualColumn right= elements.get(currentCall.maxRangeIndex);
				left.setOverrideColor(PivotColor);
				right.setOverrideColor(PivotColor);
				left.setAlwaysForceColorOverride(true);
				right.setAlwaysForceColorOverride(true);
				
				callStack.pop();
				return true;
			}
			
			if(cachedPivot == null) {
				int middleIndex = (currentCall.maxRangeIndex - currentCall.minRangeIndex) / 2 + currentCall.minRangeIndex;
				cachedPivot = elements.get(middleIndex);
				cachedPivot.setOverrideColor(PivotColor);
				cachedPivot.setAlwaysForceColorOverride(true);
				stage = Stage.CACHE_PIVOT;
				currentCall.middleIndex = middleIndex;
			} 
		} else if(stage == Stage.CACHE_PIVOT) {
			elements.get(0).setOverrideColor(Color.GRAY);
			swapWithAnimation(currentCall.middleIndex, currentCall.minRangeIndex, true); //maybe should not record history.
			stage = Stage.LOW_TO_HIGH_SCAN;
		} else if (stage == Stage.LOW_TO_HIGH_SCAN) {
			if(!currentCall.highAndLowMarkersPlaced) {
				setMarkerToPosition(lowMarker, currentCall.minRangeIndex + 1);
				setMarkerToPosition(highMarker, currentCall.maxRangeIndex);
				lowMarker.translate(-Gdx.graphics.getWidth() / 2, 0);
				highMarker.translate(Gdx.graphics.getWidth() / 2, 0);
				
				setMarkerLERPToPosition(lowMarker, currentCall.minRangeIndex + 1);
				setMarkerLERPToPosition(highMarker, currentCall.maxRangeIndex);
				
				currentCall.highAndLowMarkersPlaced = true;
			} else {
				if (currentCall.curLowIdx > currentCall.maxRangeIndex) {
					stage = Stage.HIGH_TO_LOW_SCAN;
					return false;
				}
				
				int lowIdx = currentCall.curLowIdx;
				VisualColumn eleAtLowIdx = elements.get(lowIdx);
				if (eleAtLowIdx.getValue() <= cachedPivot.getValue()) {
					//The low ptr points to valid element
					eleAtLowIdx.setOverrideColor(Color.RED);
					eleAtLowIdx.setAlwaysForceColorOverride(true);
					currentCall.curLowIdx++;
					setMarkerLERPToPosition(lowMarker, currentCall.curLowIdx);
					
				} else {
					//The low ptr points to element that needs to be swapped to other side of pivot.
					eleAtLowIdx.setOverrideColor(Color.BLUE);
					eleAtLowIdx.setAlwaysForceColorOverride(true);
					stage = Stage.HIGH_TO_LOW_SCAN;
				}
			}
		} else if (stage == Stage.HIGH_TO_LOW_SCAN) {
			//middle check and bounds check.
			if (currentCall.curHighIdx <= currentCall.minRangeIndex) {
				stage = Stage.SWAP;
				return false;
			}

			int highIdx = currentCall.curHighIdx;
			VisualColumn eleAtHighIdx = elements.get(highIdx);
			//if (eleAtHighIdx.getValue() > cachedPivot.getValue()) {
			if (eleAtHighIdx.getValue() >= cachedPivot.getValue()) { //my previous implementation only has >
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
			}
		} else if (stage == Stage.SWAP) {
			int newLowIdx = currentCall.curLowIdx + 1;
			int newHighIdx = currentCall.curHighIdx - 1;
			
			// make sure that the swap indices are still on appropriate side of pivot.
			if(currentCall.curLowIdx < currentCall.curHighIdx) {
				swapWithAnimation(currentCall.curLowIdx, currentCall.curHighIdx, true);
				setMarkerLERPToPosition(lowMarker, newLowIdx);
				setMarkerLERPToPosition(highMarker, newHighIdx);
				
			}
			
			//this now checks the updated positions
			if(currentCall.curLowIdx < currentCall.curHighIdx) {
				stage = Stage.LOW_TO_HIGH_SCAN;
			} else {
				stage = Stage.POSITION_PIVOT;
			}
		} else if (stage == Stage.POSITION_PIVOT) {
			int pivotIndex = currentCall.curHighIdx;
			swapWithAnimation(pivotIndex, 0, true);
			callStack.pop(); //pop this call off of stack.
			
			IndexInterval left = new IndexInterval(pivotIndex + 1, currentCall.maxRangeIndex);
			IndexInterval right = new IndexInterval(currentCall.minRangeIndex, pivotIndex - 1); 
			
			if(left.maxRangeIndex > left.minRangeIndex) {
				callStack.push(left); //push left half
			}
			if(right.maxRangeIndex > right.minRangeIndex) {
				callStack.push(right); //push right half (this makes right come first)
			}
			
			resetNonPurpleColors();
			cachedPivot = null;
			stage = Stage.PICK_PIVOT;
			return true;
		}
		return false;
	}

	private void resetNonPurpleColors() {
		for(VisualColumn element : elements) {
			if(!element.getOverrideColorReference().equals(PivotColor)) {
				element.setAlwaysForceColorOverride(false);
			}
		}
		
	}

	@Override
	public void draw(SpriteBatch batch) {
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
		super.IO();

	}

	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);
		
		if(callStack.size() > 0) {
			IndexInterval currentStackFrame = callStack.peek();
			if(currentStackFrame != null && currentStackFrame.highAndLowMarkersPlaced) {
				highMarker.draw(batch);
				lowMarker.draw(batch);
			}
		}
	}

	@Override
	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		VisualColumn result = super.attemptSwapWithOtherElement(draggedElement);
		
		
		return result; 
	}


	
	@Override
	protected void completeNextStep() {
		if(stepIndex == 0) return;
		
		if(stepIndex > 0 && stepIndex < elements.size()) {
			//check left neighbor
			VisualColumn step = elements.get(stepIndex);
			VisualColumn leftNeighbor = elements.get(stepIndex - 1);
			
			if (leftNeighbor.getValue() >= step.getValue() ){
				forceSwap(step, leftNeighbor, true);
				setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
				setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
				stepIndex--;
				setMarkerLERPToPosition(stepMarker, stepIndex);
			} else {
				stepIndex = 0;
			}
		}
		
		//check if this step completed iteration
		if(stepIndex == 0) return;
		
		return;
	}
	
	@Override
	public void createSolutionArray(){
		solution = new QuickSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
	}

	@Override
	protected void incrementIteration() {
		super.incrementIteration();
	}

	
}

class IndexInterval{ 
	//valid interval that this recursive call to quick sort will take place over. 
	public int minRangeIndex;
	public int maxRangeIndex;
	
	//variables that would be local to stackframe.
	public int middleIndex;
	public int curLowIdx;
	public int curHighIdx;
	
	//variables to help with animation.
	public boolean highAndLowMarkersPlaced = false;
	
	public IndexInterval(int inclusiveLower, int inclusiveHigher) {
		minRangeIndex = inclusiveLower;
		curLowIdx = minRangeIndex + 1;
		
		maxRangeIndex = inclusiveHigher;
		curHighIdx = maxRangeIndex;
	}
}

















//
