package enigma.engine.utilities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class DimmingSprite extends Sprite{
	private int dimDuration = 500;
	private long dimStart = 0;
	
	public DimmingSprite(Texture texture) {
		super(texture);
	}
	
	public void startDimming() {
		dimStart = System.currentTimeMillis();
	}
	
	public void logic() {
		long current = System.currentTimeMillis();
		if(current < dimDuration + dimStart) {
			int progress = (int)(current - dimStart);
			setAlpha(1f - (float) progress / dimDuration);
		}  else {
			setAlpha(0.0f);
		}
	}

	public void cancelDimming() {
		dimStart = 0;
	}
}
