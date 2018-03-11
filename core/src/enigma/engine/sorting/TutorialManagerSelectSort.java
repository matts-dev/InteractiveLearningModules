package enigma.engine.sorting;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class TutorialManagerSelectSort extends TutorialManager {
	private ArrayList<String> instructions = new ArrayList<String>();
	private SelectionSortableArray array;
	private boolean allowArrayIO = false;
	private int instructionIdx = 0;
	private boolean captureRightClicks = true;
	private boolean rightClickPressed = false;
	private int arraySize;
	
	//commands
	private final char cmdSymbol = '$';
	private final String stepElementCmd = cmdSymbol + "step";
	private final String enableMinSwapPtrCmd = cmdSymbol + "doOneSwap";
	private final String allowFinishCmd = cmdSymbol + "finish";
	
	//state variables
	private boolean stepToElement = false;
	private boolean enableFinish = false;
	
	
	public TutorialManagerSelectSort(float x, float y, float elementWidth, int numElements, int maxElementValue) {
		int max = maxElementValue;
		int[] sourceArray = {
				(int) (max * .4), 
				(int) (max * .5), 
				(int) (max * .3), 
				(int) (max * .6),
				(int) (max * .8),
				(int) (max * .9),
				(int) (max * .8)
				};
		arraySize = sourceArray.length;
		
		array = new SelectionSortableArray(x, y, elementWidth, maxElementValue, sourceArray, true);
		array.centerOnPoint(Gdx.graphics.getWidth() * .5f, Gdx.graphics.getHeight() * .2f);
		array.onlyAllowSingleSwap = true;
		
		createInstructions();
		array.overrideInstructionTo(instructions.get(instructionIdx));
		array.allowSwapMarkerDraw  = false;
		
		array.setToggleSolutionSolver(false);
		array.togglePromptUpdates(false);
	}
	
	@Override
	protected void createInstructions() {

		// introduction
		instructions.add("Welcome! Press enter to get started!");

		instructions.add("Selection sort is a general sorting algorithm...");
		instructions.add("The algorithm starts at the left-most location...");
		instructions.add("...see the gray pointer? That's the current location");
		
		instructions.add("In sorting, we'd like the smallest element at this location");
		instructions.add("This algorithm scans right looking for the smallest element.");
		instructions.add(stepElementCmd);
		
		instructions.add("The yellow pointer represents where we are at in the scan.");
		instructions.add("Since this element is larger, let's just scan over it.");
		instructions.add(stepElementCmd);
		instructions.add("This element is smaller than the gray pointer's element.");
		instructions.add("Let's leave a marker there...");
		instructions.add(enableMinSwapPtrCmd );
		instructions.add("...the red marker shows the smallest element seen so far.");
		instructions.add(stepElementCmd);
		instructions.add("Let's scan a few more times.");
		instructions.add(stepElementCmd);
		instructions.add("Visually we can see there are no smaller elements...");
		instructions.add("...but the algorithm must look at one element at a time.");
		instructions.add(stepElementCmd);
		instructions.add("The yellow pointer is at the end...");
		instructions.add("... so now we've scanned the entire array once.");
		instructions.add("We're ready to move the smallest element...");
		instructions.add("... it should be in leftmost spot (at the gray pointer).");
		instructions.add("We could just move it there...");
		instructions.add("...but what about the gray pointer's element?");
		instructions.add("Let's swap the gray and red pointer elements.");
		instructions.add("After swapping, we move the gray pointer right.");
		instructions.add(stepElementCmd);
		instructions.add(enableMinSwapPtrCmd );
		
		instructions.add("Now, this process repeats from the new gray pointer.");
		instructions.add("Note: the first element has been correctly sorted...");
		instructions.add("...everytime we swap, we correctly position an element...");
		instructions.add("...which means we're sorting the array left to right.");
		instructions.add("So, we should always swap with the gray pointers location.");
		
		instructions.add(enableMinSwapPtrCmd);
		instructions.add(allowFinishCmd);
		instructions.add("Continue pressing enter until the array is sorted.");
		
		instructions.add("Now this array has been sorted!");
		
		instructions.add("One last thing... ");
		instructions.add("When there wasn't any smaller elements...");
		instructions.add("...the gray pointer didn't swap with anything...");
		instructions.add("...it just moved to the right.");
		
		instructions.add("When that happens, the gray pointer is smallest in the scan...");
		instructions.add("...which means it is correctly sorted.");
		instructions.add("So, we just slide the gray pointer right and continue.");
		
		instructions.add("Now I think you're ready for more complicated arrays.");
		instructions.add("Before that, try hitting space to see the keyboard shortcuts.");
		instructions.add("You can do that at any time.");
		instructions.add("Now, to get a new random array...");
		instructions.add("Press N for a new array, or press B for a big array.");

			
	}

	@Override
	public boolean active() {
		return true;
	}
	
	@Override
	public SortableArray getArray() {
		return this.array;
	}
	
	@Override
	public void logic() {
		if(allowArrayIO) {
			array.IO();
		} 
		this.IO();
		
		pollArrayWaiting();
		
	}

	private void IO() {
		boolean simulateEnter = false;
		if(rightClickPressed) {
			//consume the right click.
			simulateEnter = true;
			rightClickPressed = false;
		}
		
		
		if((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || simulateEnter) && !allowArrayIO) {
			if(!array.instructionAnimating()) {
				nextInstructionText();
			}
		}
		
	}

	private void nextInstructionText() {
		int instrSize = instructions.size();
		String instruction = null;
		do {
			instructionIdx += 1;
			if ( instructionIdx < instrSize ) {
				instruction = instructions.get(instructionIdx);
			} else {
				instruction = null;
			}
		} while (processInstructionAsCommand(instruction) && instructionIdx < instructions.size());

		if(instructionIdx < instrSize) {
			array.overrideInstructionTo(instructions.get(instructionIdx));
		}
	}

	/**
	 * @param instruction
	 * @return true if the instruction was a command false otherwise
	 */
	private boolean processInstructionAsCommand(String instruction) {
		if(instruction == null || (instruction.length() == 0)) {
			return false;
		}
		
		if(instruction.charAt(0) == cmdSymbol){
			//simple string comparison is okay for this 
			//simple application I think.
			if(instruction.equals(stepElementCmd)){
				//allowArrayIO = true;
				stepToElement  = true;
				return true;
			} 
			else if (instruction.equals(enableMinSwapPtrCmd)) {
				array.setToggleSolutionSolver(true);
				array.nextSolveStep(true);
				array.setToggleSolutionSolver(false);
				array.allowSwapMarkerDraw = !array.allowSwapMarkerDraw;
				
				//nextInstructionText();
				return true;
			}
			else if (instruction.equals(allowFinishCmd)) {
				//swapIdx = array.getCurrentSwapIndex();
				allowArrayIO = true;
				enableFinish = true;
				return true;
			}
		}
		return false;
	}

	private void pollArrayWaiting() {
		if(stepToElement) {
			array.setToggleSolutionSolver(true);
			array.nextSolveStep(true);
			array.setToggleSolutionSolver(false);
			
			stepToElement = false;

		}
		else if (enableFinish) {
			array.allowSwapMarkerDraw = true;
			
			if(array.getIterationIndex() == arraySize - 1) {
				nextInstructionText();
				enableFinish = false;
				allowArrayIO = false;
			}
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		if (button == Input.Buttons.RIGHT) {
			if(captureRightClicks) {
				rightClickPressed = true;
			}
		}
		
		if(allowArrayIO) {
			array.touchDown(screenX, screenY, pointer, button, camera);
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {		
		if(allowArrayIO) {
			array.touchUp(screenX, screenY, pointer, button, camera);
		}
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera) {
		if(allowArrayIO) {
			array.touchDragged(screenX, screenY, pointer, camera);
		}				
		return false;
	}
}
