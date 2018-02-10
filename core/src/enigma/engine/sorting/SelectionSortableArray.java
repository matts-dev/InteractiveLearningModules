package enigma.engine.sorting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;

public class SelectionSortableArray extends SortableArray{
	protected Marker swapMarker;
	private int currentMinimum;
	private int currentMinimumIdx;
	private boolean shouldSwapAfterIteration = true;
	private String iterationTimerKey = "iterTimerKey";
	
	public SelectionSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		timer.setTimer(iterationTimerKey, 0);
		
		swapMarker = configureMarker(Color.RED);
		setMarkerToPosition(swapMarker, 0);
		setupIterationVars();
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
		if(stepIndex < elements.size() - 1) {
			stepIndex++;
			setMarkerLERPToPosition(stepMarker, stepIndex);
			int stepValue = elements.get(stepIndex).getValue();
			if(stepValue < currentMinimum ) {
				setMarkerLERPToPosition(swapMarker, stepIndex);
				currentMinimum = stepValue;
				currentMinimumIdx = stepIndex;
			} 
		} else {
			return true;
		}
		
//		if(stepIndex > 0 && stepIndex < elements.size()) {
//			//check left neighbor
//			VisualColumn step = elements.get(stepIndex);
//			VisualColumn leftNeighbor = elements.get(stepIndex - 1);
//			
//			if (leftNeighbor.getValue() > step.getValue() ){
//				
//				forceSwap(step, leftNeighbor, true);
//				setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
//				setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
//				stepIndex--;
//				setMarkerLERPToPosition(stepMarker, stepIndex);
//			} 
//			
//			//check if solution is complete (this prevents extra enter being pressed)
//			else if(stepIndex != 0) {
//				//check the next left pair //note that the step index has already been decremented
//				step = elements.get(stepIndex);
//				leftNeighbor = elements.get(stepIndex - 1); 
//				if (leftNeighbor.getValue() <= step.getValue() ){
//					stepIndex = 0;
//					//return false;//original
//					return true;
//				}
//			}
//		} else {
//			//the step is out of bounds, treat this as a solved sort.
//			return true;
//		}
		
		//check if this step completed iteration
		//if(stepIndex == 0) return true; //this will cause it to skip some iteration updates
		
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
		solution = new SelectionSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
	}
	
	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);
		if(drawSwapMarker()) {
			swapMarker.draw(batch);
		}
	}

	@Override
	protected void incrementIteration() {
		if(shouldSwapAfterIteration && iterationIndex < elements.size()) {
			elements.get(iterationIndex).setOverrideColor(Color.GRAY);
			elements.get(currentMinimumIdx).setOverrideColor(Color.RED);
			swapWithAnimation(iterationIndex, currentMinimumIdx, false);
			timer.setTimer(iterationTimerKey, 500);
		}
		
		//this must come after swap. Otherwise indexes will be updated values, not old values. 
		super.incrementIteration();
		
		setMarkerLERPToPosition(swapMarker, iterationIndex);
		setupIterationVars();
	}

	private void setupIterationVars() {
		if(iterationIndex < elements.size()) {
			currentMinimum = elements.get(iterationIndex).getValue();
			currentMinimumIdx = iterationIndex;
		}
	}

	public boolean drawSwapMarker() {
//		for(VisualColumn element : elements) {
//			if(element.isInterpolating()) {
//				return false;
//			}
//		}
//		return true;
		return timer.timerUp(iterationTimerKey);
	}	
	
	@Override
	public boolean shouldDrawStepMarker() {
		return timer.timerUp(iterationTimerKey);
	}

	@Override
	protected boolean allowUserInput() {
		return super.allowUserInput() && timer.timerUp(iterationTimerKey);
	}
}























//
