package enigma.engine.utilities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.DrawableString;
import enigma.engine.TextureLookup;
import enigma.engine.Touchable;

public class TextButton extends LERPSprite implements Touchable{
	private DrawableString text;
	private long lastSwitchTime;
	private long switchDelayMs = 350;
	
	public TextButton(String text) {
		super(TextureLookup.buttonBlack);
		this.text = new DrawableString(text, true);
		lastSwitchTime = System.currentTimeMillis();
		
		float txtWid = this.text.width();
		float txtHgt = this.text.height();
		
		this.setSize(txtWid + this.text.height(), txtHgt * 2f);
		recenter();
	}

	public void draw(SpriteBatch batch) {
		super.draw(batch);
		text.draw(batch);
	}
	
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		recenter();
	}

	public void recenter() {
		float xOff = this.getWidth() / 2;
		float yOff = this.getHeight() / 2;
		
		text.setXY(getX() + xOff,  getY() + yOff);
	}
	
	
	@Override
	public void logic() {
		throw new RuntimeException("Please call the logic with current time as an argument");
	}

	public void logic(long currentTime) {
		super.logic();
		if(currentTime > lastSwitchTime + switchDelayMs) {
			lastSwitchTime = currentTime;
						
			//alternate textures.
			if(getTexture() == TextureLookup.buttonBlack) {
				setTexture(TextureLookup.buttonGrey);
			} else {
				setTexture(TextureLookup.buttonBlack);
			}
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		//fire button if this is clicked? We'll see.
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera) {
		return false;
	}

	public void setCenterPosition(float x, float y) {
		x -= getWidth() / 2 ;
		y -= getHeight() / 2;
		
		setPosition(x, y);
		recenter();
	}

}
