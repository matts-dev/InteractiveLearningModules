package enigma.engine.floatingpoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import enigma.engine.DrawableString;
import enigma.engine.Tools;

public class FP2WholeNumberBinaryModule extends FPComponentModule
{
	/** a vector to hold converted touch coordinates into game world coordinates */
	private Vector3 convVect = new Vector3(0, 0, 0);
	private boolean devMode = true;
	private Integer wholeNumber;
	private DrawableString wholeNumberDrawString = new DrawableString("");
	private boolean bShowWholeNumber = true;
	private int showWholeNumberInstructionIndex = -1;
	private int convertingToBinaryIndex = -1;

	/**
	 * Constructor
	 * 
	 * @param camera the Orthographic camera. This is used to convert points
	 * @param animatingInstruction
	 */
	public FP2WholeNumberBinaryModule(OrthographicCamera camera, AcknlowedgedInstruction animatingInstruction)
	{
		super(camera, animatingInstruction);
		setUpInstructions();
		reset();
	}

	@Override
	protected void reset()
	{
		if (wholeNumberDrawString != null)
		{
			wholeNumberDrawString.setVisible(false);
		}
	}

	@Override
	public void initialize()
	{
		super.initialize();
		if (wholeNumberDrawString != null)
		{
			wholeNumberDrawString.setVisible(false);
		}
	}

	protected void setUpInstructions()
	{
		instructionList.add("Consider whole number and the fraction separately.");

		showWholeNumberInstructionIndex = instructionList.size();
		instructionList.add("Here is the whole number portion.");

		convertingToBinaryIndex = instructionList.size();

	}

	void handleStateChanged(int newCurrentInstruction)
	{
		//boolean updateCurrentInstruction = true;
		if (newCurrentInstruction == showWholeNumberInstructionIndex)
		{
			if (wholeNumberDrawString != null)
			{
				wholeNumberDrawString.setVisible(true);
			}
		}
		updateCurrentInstruction();
		instruction.startAnimation();
	}

	@Override
	public void logic()
	{
		super.logic();
		instruction.animateLogic();
	}

	@Override
	public void IO()
	{
		// do sub module IO
		super.IO();

		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) && Gdx.input.isKeyJustPressed(Input.Keys.Q))
		{
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.K))
		{
			if (currentInstructionPointer == convertingToBinaryIndex)
			{
				handleConversionProcessStep();
			}
			else
			{
				currentInstructionPointer++;
				handleStateChanged(currentInstructionPointer);
			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.J))
		{
			currentInstructionPointer--;
			handleStateChanged(currentInstructionPointer);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R))
		{
		}

		if (devMode && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
		{
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0))
			{

			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9))
			{

			}
		}

	}

	private void handleConversionProcessStep()
	{
		boolean complete = false;
		
		
		
		
		if(complete)
		{
			currentInstructionPointer++;
			handleStateChanged(currentInstructionPointer);
		}
	}

	@Override
	public void setValueToConvert(float newNumberToConvert)
	{
		super.setValueToConvert(newNumberToConvert);
		wholeNumber = (int) Math.floor(Math.abs(newNumberToConvert));
		wholeNumberDrawString.setText(wholeNumber.toString());
		wholeNumberDrawString.setXY(Gdx.graphics.getWidth() * 0.25f, Gdx.graphics.getHeight() / 2);
	}

	@Override
	public void dispose()
	{
		// dispose any sub-modules
		super.dispose();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		Tools.convertMousePointsIntoGameCoordinates(camera, convVect);

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		Tools.convertMousePointsIntoGameCoordinates(camera, convVect);

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		Tools.convertMousePointsIntoGameCoordinates(camera, convVect);

		return false;
	}

	public void draw(SpriteBatch batch, float lastModuleFraction)
	{
		super.draw(batch);

		if (bShowWholeNumber)
		{
			wholeNumberDrawString.draw(batch);
		}

		if (getFractionDone() < 1.0f && lastModuleFraction >= 1.0f)
		{
			if (instruction != null)
			{
				instruction.draw(batch);
			}
		}
	}

}
