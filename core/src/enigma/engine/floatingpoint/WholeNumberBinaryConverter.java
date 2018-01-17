package enigma.engine.floatingpoint;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.DrawableString;

public class WholeNumberBinaryConverter implements GenericLearnModule
{
	private ArrayList<DrawableString> numerators = new ArrayList<DrawableString>(10); //the numbers inside of the division bar
	private ArrayList<DrawableString> denominators = new ArrayList<DrawableString>(10);
	private ArrayList<Sprite> divisorSymbols = new ArrayList<Sprite>(10); //the actual sideways L division symbol
	private ArrayList<DrawableString> answers = new ArrayList<DrawableString>(10); //answer above division symbol
	private DrawableString typingAnswer = new DrawableString(""); //this is what gets populated when the user types
	
	boolean bActive = true;
	
	@Override
	public void logic()
	{

	}

	@Override
	public void IO()
	{
		if(bActive)
		{
			for(int i = 0; i < 10; ++i)
			{
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0 + i))
				{
					typingAnswer.setText(typingAnswer.getText() + i);
					break;
				}
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))
			{
				String tempText = typingAnswer.getText();
				if(tempText.length() > 0)
				{
					//TODO update DrawableString to use a string buffer for frequent changes
					typingAnswer.setText(tempText.substring(0, tempText.length() - 1));
				}
			}
			if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS))
			{
				typingAnswer.setText(typingAnswer.getText() + '-');				
			}
		}
			
	}

	@Override
	public void draw(SpriteBatch batch, float lastModuleFraction)
	{
		if(bActive)
		{
			
		}
	}

	@Override
	public void dispose()
	{
		
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}
}
