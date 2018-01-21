package enigma.engine.sorting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.Draggable;
import enigma.engine.TextureLookup;

public class InsertionSortableArray extends SortableArray{
	
	public InsertionSortableArray(float x, float y, float elementWidth, int numElements, int maxElementValue) {
		super(x, y, elementWidth, numElements, maxElementValue);
		drawIterationMarker = true;
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);
	}

	@Override
	public void logic() {
		super.logic();
		
		IO();
	}

	private void IO() {
		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			nextSolveStep();
		}
	}
	

	@Override
	public VisualColumn attemptSwapWithOtherElement(Draggable draggedElement) {
		return super.attemptSwapWithOtherElement(draggedElement);
	}

	@Override
	protected boolean stepIndexComplete() {
		if(stepIndex == 0) return true;
		
		if(stepIndex > 0 && stepIndex < elements.size()) {
			//check left neighbor
			VisualColumn step = elements.get(stepIndex);
			VisualColumn leftNeighbor = elements.get(stepIndex - 1);
			
			if (leftNeighbor.getValue() > step.getValue() ){
				forceSwap(step, leftNeighbor);
				setLERPToPosition(stepIndex, leftNeighbor, TextureLookup.getBlueColor());
				setLERPToPosition(stepIndex - 1, step, TextureLookup.getRedColor());
				stepIndex--;
			} else {
				stepIndex = 0;
			}
		}
		
		//check if this step completed iteration
		if(stepIndex == 0) return true;
		
		return false;
	}
	
}
