package enigma.engine.sorting;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class TutorialManagerISort extends TutorialManager {
	private ArrayList<String> instructions = new ArrayList<String>();
	private InsertionSortableArray array;
	private boolean allowArrayIO = false;
	private int instructionIdx = 0;
	private boolean captureRightClicks = true;
	private boolean rightClickPressed = false;
	
	//commands
	private final char cmdSymbol = '$';
	private final String stepToSecondElementCmd = cmdSymbol + "stepSecond";
	private final String autoSolve1Swap = cmdSymbol + "doOneSwap";
	private final String swapCmd = cmdSymbol + "swap";
	
	//state variables
	private boolean stepToSecondElement = false;
	private boolean waitPtrSwap = false;
	private int swapIdx;
	
	public TutorialManagerISort(float x, float y, float elementWidth, int numElements, int maxElementValue) {
		int max = maxElementValue;
		int[] sourceArray = {
				(int) (max * .9), 
				(int) (max * .6), 
				(int) (max * .3), 
				(int) (max * .5),
				(int) (max * .8),
				(int) (max * .9),
				(int) (max * .8)
				};
		
		array = new InsertionSortableArray(x, y, elementWidth, maxElementValue, sourceArray, true);
		array.centerOnPoint(Gdx.graphics.getWidth() * .5f, Gdx.graphics.getHeight() * .2f);
		array.onlyAllowSingleSwap = true;
		
		createInstructions();
		array.overrideInstructionTo(instructions.get(instructionIdx));
		
		array.setToggleSolutionSolver(false);
		array.togglePromptUpdates(false);
	}
	
	@Override
	protected void createInstructions() {

		// introduction
		instructions.add("Welcome! Press enter to get started!");

		instructions.add("Insertion sort is a general sorting algorithm...");
		instructions.add("...it works by 'inserting' elements into a sorted region.");
		instructions.add("It has the same algorithmic complexity as selection sort...");
		instructions.add("...but insertion sort is generally faster in practice.");
		
		instructions.add("The algorithm works by building up a sorted region on the left...");
		instructions.add("...it does ths by taking steps to the right, element by element.");
		instructions.add("The gray pointer marks how far right we have moved.");
		instructions.add(stepToSecondElementCmd);
		
		instructions.add("The gray pointer marks how far right we have moved.");
		instructions.add("Here's our first rule: if the left element is bigger, swap with it.");
		instructions.add("Since the element to the left of the gray pointer is bigger...");
		instructions.add("...lets swap the gray pointer element with it.");
		instructions.add(autoSolve1Swap);
		
		instructions.add("This made the array slightly more sorted...");
		instructions.add("Our goal is to build up a sorted array on the left side.");
		instructions.add("Let's keep going right to see how this all works together");
		instructions.add(autoSolve1Swap);

		instructions.add("Again, the element on the left is larger...");
		instructions.add("So let's swap the gray pointer with it...");
		instructions.add(autoSolve1Swap);
		
		instructions.add("Here's something new...");
		instructions.add("...we swapped, but there's still something bigger on the left...");
		instructions.add("...so we swap again.");
		instructions.add("let's swap one more time.");
		instructions.add(autoSolve1Swap);
		instructions.add("Notice our first 3 elements are sorted...");
		instructions.add("... this is our growing sorted region.");
		instructions.add("Everytime we swap, we're inserting into the sorted region!");
		instructions.add("Let's continue moving right.");
		instructions.add(autoSolve1Swap);
		instructions.add("Again, we've got a bigger element on the left.");
		instructions.add("How about this time you swap the elements...");
		
		instructions.add(swapCmd);
		instructions.add("Click and drag the element onto the left element.");
		instructions.add("Nice! Let's keep going since the left element is larger...");
		instructions.add(swapCmd);
		instructions.add("...please correctly swap with the left element.");
		
		instructions.add("Great! ");
		instructions.add("Now, to the left of the yellow pointer is smaller...");
		instructions.add("...so we're done swapping, let's move the gray pointer.");
		instructions.add(autoSolve1Swap);

		instructions.add("Looks like we need to swap again.");
		instructions.add(swapCmd);
		instructions.add("Please correctly swap with the left element.");
		
		instructions.add("Let's continue moving the gray pointer.");
		instructions.add(autoSolve1Swap);
		instructions.add("This element is the same as its left side...");
		instructions.add("It is correctly positioned, let's move the gray pointer.");
		instructions.add(autoSolve1Swap);
		

		instructions.add("We're almost there, but we need to do a few swaps.");

		instructions.add(swapCmd);
		instructions.add("Please swap with the left pointer.");
		
		instructions.add(swapCmd);
		instructions.add("One more swap!");
		instructions.add(autoSolve1Swap);

		instructions.add("And we have it, the array is sorted!");
		instructions.add("I think you're ready to try without guideance.");
		instructions.add("But if you do get stuck, press enter to see the next step.");
		instructions.add("Oh yeah, press space at anytime to see the keyboard shortcuts.");
		instructions.add("Now, press 'B' for a big array or 'N' for a normal array.");
			
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
			if(instruction.equals(stepToSecondElementCmd)){
				//allowArrayIO = true;
				stepToSecondElement  = true;
				return true;
			} 
			else if (instruction.equals(autoSolve1Swap)) {
				array.setToggleSolutionSolver(true);
				array.nextSolveStep(true);
				array.setToggleSolutionSolver(false);
				
				//nextInstructionText();
				return true;
			}
			else if (instruction.equals(swapCmd)) {
				swapIdx = array.getCurrentSwapIndex();
				allowArrayIO = true;
				waitPtrSwap = true;
				return true;
			}
		}
		return false;
	}

	private void pollArrayWaiting() {
		if(stepToSecondElement) {
			array.setToggleSolutionSolver(true);
			array.nextSolveStep(true);
			array.setToggleSolutionSolver(false);
			
			stepToSecondElement = false;
			nextInstructionText();

		}
		else if (waitPtrSwap) {
			int arraySwapPosition = array.getCurrentSwapIndex();
			if(swapIdx != arraySwapPosition) {
				waitPtrSwap = false;
				allowArrayIO = false;
				nextInstructionText();
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
