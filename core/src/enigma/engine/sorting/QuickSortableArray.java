package enigma.engine.sorting;

import java.util.Stack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;

public class QuickSortableArray extends SortableArray{
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
	
	public QuickSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		callStack.push(new IndexInterval(0, elements.size()-1));
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
	}

	@Override
	public void logic() {
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
		if(stage == Stage.PICK_PIVOT) {
			if(cachedPivot == null) {
				IndexInterval currentCall = callStack.peek();
				int middleIndex = (currentCall.highIndex - currentCall.lowIndex) / 2 + currentCall.lowIndex;
				cachedPivot = elements.get(middleIndex);
				cachedPivot.setOverrideColor(Color.PURPLE);
			} else {
				
			}
			
		} else {
			
		}
		
		//delete this line of code...
		if(stepIndex == 0) return true;
		
		return false;
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
	public int lowIndex;
	public int highIndex;
	
	public IndexInterval(int inclusiveLower, int inclusiveHigher) {
		lowIndex = inclusiveHigher;
		highIndex = inclusiveHigher;
	}
}

















//
