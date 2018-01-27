package enigma.engine.sorting;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;

public class InsertionSortableArray extends SortableArray{
	
	public InsertionSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		drawIterationMarker = true;
		drawStepMarker = true;
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
		if(stepIndex == 0) return true;
		
		if(stepIndex > 0 && stepIndex < elements.size()) {
			//check left neighbor
			VisualColumn step = elements.get(stepIndex);
			VisualColumn leftNeighbor = elements.get(stepIndex - 1);
			
			if (leftNeighbor.getValue() > step.getValue() ){
				
				forceSwap(step, leftNeighbor, true);
				setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
				setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
				stepIndex--;
				setMarkerLERPToPosition(stepMarker, stepIndex);
			} 
			
			//check if solution is complete (this prevents extra enter being pressed)
			if(stepIndex != 0) {
				//check the next left pair //note that the step index has already been decremented
				step = elements.get(stepIndex);
				leftNeighbor = elements.get(stepIndex - 1); 
				if (leftNeighbor.getValue() <= step.getValue() ){
					stepIndex = 0;
					return false;
				}
			}
		} else {
			//the step is out of bounds, treat this as a solved sort.
			return true;
		}
		
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
		solution = new InsertionSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
	}
}























//
