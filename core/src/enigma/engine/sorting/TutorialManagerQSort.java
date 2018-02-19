package enigma.engine.sorting;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;

import enigma.engine.sorting.QuickSortableArray.Stage;

public class TutorialManagerQSort extends TutorialManager {
	private ArrayList<String> instructions = new ArrayList<String>();
	private QuickSortableArray array;
	private boolean allowArrayIO = false;
	private int instructionIdx = 0;
	private boolean captureRightClicks = true;
	private boolean rightClickPressed = false;
	
	//commands
	private final char cmdSymbol = '$';
	private final String selectPivotCmd = cmdSymbol + "pvt";
	private final String cachePivotCmd = cmdSymbol + "ch";
	private final String show1LowPtrScanCmd = cmdSymbol + "aiMvRedPtr";
	private final String moveLowPtrOnceCmd = cmdSymbol + "playerMvRedPtr1Time";
	private final String endLowPtrCmd = cmdSymbol + "endLowPtrStage";
	private final String moveHighPtrOnceCmd = cmdSymbol + "playerMvBluePtr1Time";
	private final String endHighPtrCmd = cmdSymbol + "endHighPtrStage";
	private final String swapPtrsCmd = cmdSymbol + "swapPtrs";
	private final String repositionPvtCmd = cmdSymbol + "placebackPivot";
	private final String baseCaseSelectCmd = cmdSymbol + "bcase";
	
	//state variables
	private boolean waitForCachePivot = false;
	private boolean waitForLowHigh = false;
	private boolean waitForLowPtrIdx = false;
	private int lowPtrWaitIndex = -1;
	private boolean waitEndLowPtr = false;
	private boolean waitForHighPtrIdx = false;
	private int highPtrWaitIndex = -1;
	private boolean waitEndHighPtr = false;
	private boolean waitPtrSwap = false;
	private boolean repositionPvt = false;
	private boolean waitForBaseComplete = false;
	private IndexInterval cachedFrameForBaseCase = null;
	
	
	
	public TutorialManagerQSort(float x, float y, float elementWidth, int numElements, int maxElementValue) {
		int max = maxElementValue;
//		int[] sourceArray = {
//				(int) (max * .6), 
//				(int) (max * .4), 
//				(int) (max * .5), 
//				(int) (max * .7),
//				(int) (max * 1),
//				(int) (max * .2),
//				(int) (max * .9)
//				};
		int[] sourceArray = {
				(int) (max * .5), 
				(int) (max * .3), 
				(int) (max * .6), 
				(int) (max * .7),
				(int) (max * 1),
				(int) (max * .2),
				(int) (max * .9)
				};
		
		array = new QuickSortableArray(x, y, elementWidth, maxElementValue, sourceArray);
		array.centerOnPoint(Gdx.graphics.getWidth() * .5f, Gdx.graphics.getHeight() * .2f);
		
		createInstructions();
		array.overrideInstructionTo(instructions.get(instructionIdx));
		
		array.setToggleSolutionSolver(false);
		array.togglePromptUpdates(false);
		array.forceHideIcons = true;
	}
	
	@Override
	protected void createInstructions() {
		// Standard welcome instructions
//		instructions.add("Welcome to the quicksort tutorial! right click or press enter");
//		instructions.add(String.format("Press the %s button at anytime to see controls", GlobalStrings.showKeybindsButton));
//		instructions.add(String.format("Press the %s button again to close the dialog.", GlobalStrings.showKeybindsButton));
		
		// Quicksort introduction
		instructions.add("Quicksort is among the fastest sorting algorithms");
		instructions.add("The first step of Quicksort is to 'partition' the array.");
		instructions.add("To partition, we select some pivot element within the array.");
		instructions.add("Different quicksort variants choose the pivot differently.");
		instructions.add("We'll just use the middle element.");
		instructions.add(selectPivotCmd);
		instructions.add("Click on the middle element to make it the pivot.");
		
		
		instructions.add("Now, the purple element is now our pivot.");
		instructions.add("Our goal is to figure something out about the pivot...");
		instructions.add("We want to figure out it's correct place in the sorted array.");
		instructions.add("So, the center might be the wrong (sorted) pivot spot...");
		instructions.add("We can figure out the pivot's correct spot by 'partitioning'.");
		
		instructions.add("So, what exactly is 'partitoning the array'?");
		instructions.add("Well, basically it's re-arranging elements based on the pivot.");
		instructions.add("We want elements smaller than the pivot to the left...");
		instructions.add("...and we want elements bigger than pivot to the right.");
		
		instructions.add("Before partitioning, we need to move the pivot out of the way.");
		instructions.add("Basically, we're hiding the pivot until we know where it goes.");
		instructions.add("Different quicksort variants hide the pivot in different ways...");
		instructions.add("We'll just tuck it away at the start of our array sequence.");
		
		instructions.add(cachePivotCmd);
		instructions.add("Drag the pivot to the first element, and release to swap.");
		instructions.add("So, the pivot's hidden -- but what are those arrow 'pointers'?");
		instructions.add("The pointers are our place holders while we partition.");
		
		instructions.add("In regards to their colors...");
		instructions.add("Let's say that anything 'red' is 'smaller' than the pivot...");
		instructions.add("...and anything blue is bigger than the pivot.");
		instructions.add("So: red = smaller; blue = bigger.");
		
		instructions.add("Now, we're going to 'color-code' elements based on size"); //maybe shouldn't say this
		instructions.add("Let's move red pointer right marking red (smaller) elements");
		instructions.add(show1LowPtrScanCmd);
		instructions.add("Like that!");
		instructions.add("The next element is also smaller than the pivot...");
		instructions.add("...since it is smaller, it should be red too.");
		instructions.add(show1LowPtrScanCmd);
		instructions.add(moveLowPtrOnceCmd);
		instructions.add("You try, click the next element to cycle colors.");
		instructions.add("Don't actually think of this as color coding...");
		instructions.add("What we're really doing is sliding the red pointer.");
		instructions.add("We're sliding it over smaller elements...");
		instructions.add("...searching for a larger element.");
		instructions.add("The red pointer's element is larger than the purple pivot...");
		instructions.add("Since it is larger, we should label it blue.");
		instructions.add(endLowPtrCmd);
		instructions.add("Continue clicking on the element until it is blue.");
		
		instructions.add("Here's where we change things up...");
		instructions.add("...now we want to slide the blue pointer over larger elements.");
		instructions.add("Essentially, it's looking for a smaller-than-pivot element");
		instructions.add("Since the element at the blue pointer is larger...");
		instructions.add(moveHighPtrOnceCmd);
		instructions.add("... it needs to be blue; please cycle its colors to blue.");
		instructions.add("Now the blue pointer has found a smaller element!");
		instructions.add(endHighPtrCmd);
		instructions.add("Please label it red, since it is smaller than the pivot.");
		
		instructions.add("The moment we've been waiting for! ...");
		instructions.add("...the two pointers have found their targets.");
		instructions.add("The next step is to swap the elements at the two pointers! ");
		instructions.add("Swapping corrects the relative position of the elements...");
		instructions.add("...we're trying to get all red on the left, and all blue on the right.");
		instructions.add(swapPtrsCmd);
		instructions.add("Swap the two pointer's elements (click and drag).");
		
		instructions.add("Did you notice the pointers update?");
		instructions.add("The red pointer moved right, the blue moved left.");
		
		instructions.add("Now what? Well, in this case we re-position the pivot.");
		instructions.add("But normally, we will repeat blue and red pointer scanning.");
		instructions.add("Normally, we scan again until the pointers cross.");
		instructions.add("But only when the pointers cross each other...");
		instructions.add("...do we reposition the pivot into the array.");
		
		instructions.add("Now then, since our pointers crossed over each other...");
		instructions.add("...we're ready to position the pivot!");
		instructions.add("We need to swap the pivot into it's correct place...");
		instructions.add("...and the correct place is at one of the pointers.");
		instructions.add("Can you guess which pointer we will swap with the pivot?");
		instructions.add("Since we want all red on the left, and all blue on the right...");
		instructions.add("We will swap with red element currently at the blue pointer...");
		instructions.add("We do this because we hid the pivot on the left (red) side.");
		instructions.add(repositionPvtCmd);
		instructions.add("Swap the pivot with the blue pointer element, please.");
		
		instructions.add("We've now completed our first partition!");
		instructions.add("Quicksort is a recursive algorithm, ...");
		instructions.add("... which means we recurse(repeat) on smaller sub-problems");
		instructions.add("In this case, we have two smaller sub-problems, ...");
		instructions.add("...our two subproblems are beside our last pivot!");
		instructions.add("So, let's repeat and partition on both sides of the old pivot.");
		
		instructions.add("Starting with the left half, we need a pivot.");
		instructions.add("When two middle elements, choose left as the pivot.");
		instructions.add(selectPivotCmd);
		instructions.add("Above the left gray bar, pick the pivot (middle) element.");
		
		instructions.add("Now we need to hide the pivot again...");
		instructions.add("... we're going to hide it to the far left again.");
		instructions.add("It may seem like we're always hiding it in the leftmost spot...");
		instructions.add("...but we can only hide it within the current subproblem...");
		instructions.add("... which means always hide the pivot within the gray bar");
		instructions.add(cachePivotCmd);
		instructions.add("Now, hide the pivot on the far left (swap).");
		
		instructions.add("Now we need to mark elements red again...");
		instructions.add("... the first scan always starts at the red pointer...");
		instructions.add("we mark elements red until reaching a bigger element");
		instructions.add(endLowPtrCmd);
		instructions.add("Scan over all the red elements, and mark the last one blue.");
		
		
		instructions.add("Nice! now time to start our left scan...");
		instructions.add("The second scan always starts at the blue pointer...");
		instructions.add("...and moves right, marking blue elements...");
		instructions.add("...until it reaches a red element.");
		instructions.add(endHighPtrCmd);
		instructions.add("From blue pointer, mark larger elements blue.");
		
		instructions.add("This time we don't need to swap the pointers...");
		instructions.add("...the blue pointer has already passed the red pointer");
		instructions.add("When our pointers cross places, it is time to position the pivot.");
		
//		instructions.add("But note: algorithms are rigid, they cannot just skip steps...");
//		instructions.add("...the algorithm must check if it should do a swap.");
//		instructions.add("So remember, even though we see that no swap is needed...");
//		instructions.add("...the algorithm still has a step which checks for swapping.");
		instructions.add("Remember, every time move the pivot out of hiding...");
		instructions.add("...we move it to the spot of the blue pointer.");
		instructions.add(repositionPvtCmd);
		instructions.add("Now then, correctly position pivot into the subarray.");
		instructions.add("We just completed another partitioning...");
		instructions.add("...which means we are done with this sub-problem.");
		instructions.add("notice our new pivot split the region into two halves.");
		instructions.add("Now we need to pick a pivot one of these halves.");
		
		instructions.add("Notice the gray underline only covers 1 element.");
		instructions.add("So, we only have 1 element to pick a pivot from...");
		instructions.add("...which means this element is already correctly positioned.");
		instructions.add("This is what we call a base case...");
		instructions.add("Base cases are essentially the end the of recursion.");
		instructions.add(baseCaseSelectCmd);
		instructions.add("Please click the one element to mark purple.");
		
		instructions.add("Now we only have two elements...");
		instructions.add("...which is another base case!");
		instructions.add("Again, we don't need to recursively partition anymore.");
		instructions.add("We only need to swap the two if they're out of order.");
		instructions.add(baseCaseSelectCmd);
		instructions.add("Since these two are out of order, let's swap the elements..");
		
		instructions.add("Again we're presented with the two element base case.");
		instructions.add(baseCaseSelectCmd);
		instructions.add("They're out of order, so let's swap.");
		instructions.add("And we have it, the entire array has been sorted!");
		
		instructions.add("Now that you've seen the basics...");
		instructions.add("...you're ready to try some more challenging arrays.");
		instructions.add("If you get stuck, press 'enter' to see the next step...");
		instructions.add("I suggest letting the computer solve the first one...");
		instructions.add("...it may help to see how it is done quickly.");
		instructions.add("But after a few, try to solve the steps on your own.");
		instructions.add("Remember, press 'space' at anytime to see keyboard shortcuts.");
		instructions.add("Now, press 'N' for a normal array, or 'B' for a big array.");
		
		

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
			if(instruction.equals(selectPivotCmd)){
				allowArrayIO = true;
				waitForCachePivot  = true;
				return true;
			} else if (instruction.equals(cachePivotCmd)) {
				allowArrayIO = true;
				waitForLowHigh = true;
				return true;
			}
			else if (instruction.equals(show1LowPtrScanCmd)) {
				array.setToggleSolutionSolver(true);
				
				array.nextSolveStep(false);
				
				array.setToggleSolutionSolver(false);
				return true;
			}
			else if (instruction.equals(moveLowPtrOnceCmd)) {
				allowArrayIO = true;
				waitForLowPtrIdx = true;
				lowPtrWaitIndex = array.getCurrentLowPtr() + 1;
				return true;
			}
			else if (instruction.equals(endLowPtrCmd)) {
				allowArrayIO = true;
				waitEndLowPtr = true;
				return true;
			}
			else if (instruction.equals(moveHighPtrOnceCmd)) {
				allowArrayIO = true;
				waitForHighPtrIdx = true;
				highPtrWaitIndex = array.getCurrentHighPtr() - 1;
				return true;
			}
			else if (instruction.equals(endHighPtrCmd)) {
				allowArrayIO = true;
				waitEndHighPtr = true;
				return true;
			}
			else if (instruction.equals(swapPtrsCmd)) {
				allowArrayIO = true;
				waitPtrSwap = true;
				return true;
			}
			else if (instruction.equals(repositionPvtCmd)) {
				QuickSortableArray.Stage stage = array.getStage();
				while(stage == Stage.SWAP) {
					array.setToggleSolutionSolver(true);
					array.attemptSwapStep(true);
					stage = array.getStage();
					array.setToggleSolutionSolver(false);
				}
				
				allowArrayIO = true;
				repositionPvt = true;
				return true;
			}else if (instruction.equals(baseCaseSelectCmd)) {
				allowArrayIO = true;
				waitForBaseComplete = true;
				cachedFrameForBaseCase = array.peekCurrentStackFrame();
				return true;
			}
			
		}
		return false;
	}

	private void pollArrayWaiting() {
		if(waitForCachePivot) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.CACHE_PIVOT)) {
				waitForCachePivot = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitForLowHigh) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.LOW_TO_HIGH_SCAN)) {
				waitForLowHigh = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitForLowPtrIdx) {
			//QuickSortableArray.Stage stage = array.getStage();
			int lowPtr = array.getCurrentLowPtr();
			
			if(lowPtr == lowPtrWaitIndex ) {
				waitForLowPtrIdx = false;
				lowPtrWaitIndex = -1;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitEndLowPtr) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.HIGH_TO_LOW_SCAN)) {
				waitEndLowPtr = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		}else if (waitForHighPtrIdx) {
			//QuickSortableArray.Stage stage = array.getStage();
			int highPtr = array.getCurrentHighPtr();
			
			if(highPtr == highPtrWaitIndex ) {
				waitForHighPtrIdx = false;
				highPtrWaitIndex = -1;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitEndHighPtr) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.SWAP)) {
				waitEndHighPtr = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitPtrSwap) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.LOW_TO_HIGH_SCAN) 
					|| stage.equals(QuickSortableArray.Stage.POSITION_PIVOT) ) {
				waitPtrSwap = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (repositionPvt) {
			QuickSortableArray.Stage stage = array.getStage();
			if(stage.equals(QuickSortableArray.Stage.PICK_PIVOT)) {
				repositionPvt = false;
				allowArrayIO = false;
				nextInstructionText();
			}
		} else if (waitForBaseComplete) {
			//when the person is on another stack frame, they've finished the base case.
			if(array.peekCurrentStackFrame() != cachedFrameForBaseCase) {
				waitForBaseComplete = false;
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
