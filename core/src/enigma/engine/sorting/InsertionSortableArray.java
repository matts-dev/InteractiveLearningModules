package enigma.engine.sorting;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;
import enigma.engine.utilities.Tuple2;

public class InsertionSortableArray extends SortableArray{
	boolean forceHidePrompts = false;
	int[] cachedSourceArray;
	
	public InsertionSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue, int seed) {
		super(x, y, elementWidth, numElements, maxElementValue, seed);
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		instruction.setText("Insert (swap) the element left, if needed.");
	}
	
	public InsertionSortableArray(float x, float y, float elementWidth, int maxElementValue, int[] sourceArray, boolean firstCall) {
		super(x, y, elementWidth, maxElementValue, sourceArray);
		
		if(firstCall) {
			overrideSolution = true;
			cachedSourceArray = sourceArray;
			solution = new InsertionSortableArray(x, y, elementWidth, maxElementValue, sourceArray, false);
		}
		
		setDrawIterationMarker(true);
		setDrawStepMarker(true);
		instruction.setText("Insert (swap) the element left, if needed.");
	}

	@Override
	public void drawPreSprites(SpriteBatch batch) {
		super.drawPreSprites(batch);
		
		if(!forceHidePrompts) {
			instruction.draw(batch);
		}
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
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.H)) {
			forceHidePrompts = !forceHidePrompts;
		}
	}
	


	@Override
	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		VisualColumn result = super.attemptSwapWithOtherElement(draggedElement);
		
		
		return result; 
	}

	@Override
	protected boolean stepIndexComplete() {
		if(stepIndex == 0) return true;
		
		if(!allowSolutionSolver) {
			return false;
		}
		
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
			else if(stepIndex != 0) {
				//check the next left pair //note that the step index has already been decremented
				step = elements.get(stepIndex);
				leftNeighbor = elements.get(stepIndex - 1); 
				if (leftNeighbor.getValue() <= step.getValue() ){
					stepIndex = 0;
					//return false;//original
					return true;
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
		
		if(!allowSolutionSolver) {
			return;
		}
		
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
		if(!overrideSolution) {
			solution = new InsertionSortableArray(getX(), getY(), elementWidth, elements.size(), maxElementValue, seed);
		} else {
			//int lastCorrectStep = solution.lastCorrectStep;
			//solution = new InsertionSortableArray(getX(), getY(), elementWidth, maxElementValue, cachedSourceArray, false);
			//solution.lastCorrectStep = lastCorrectStep;
		}
	}
	
	@Override
	protected void populateKeybindDisplay(ArrayList<Tuple2<String, String>> keyActionPairs) {
		keyActionPairs.add(new Tuple2<String, String>("N", "New Short Array."));
		keyActionPairs.add(new Tuple2<String, String>("B", "New Large Array."));
		//keyActionPairs.add(new Tuple2<String, String>("R", "Reverse Last Swap."));
		keyActionPairs.add(new Tuple2<String, String>("ENTER", "Next Step / Check Move."));
		keyActionPairs.add(new Tuple2<String, String>("H", "Hide Prompt Challenge."));
		keyActionPairs.add(new Tuple2<String, String>("C", "Change Color Scheme."));
	}



	@Override
	public TutorialManager getTutorialManager(float x, float y, float elementWidth, int numElements,
			int maxElementValue) {
		return new TutorialManagerISort(x, y, elementWidth, numElements, maxElementValue);
	}


	public void togglePromptUpdates(boolean showPracticePrompts) {
		forceHidePrompts = showPracticePrompts;
	}

	public int getCurrentSwapIndex() {
		return stepIndex;
	}
	
}























//
