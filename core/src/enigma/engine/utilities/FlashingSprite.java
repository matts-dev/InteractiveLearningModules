package enigma.engine.utilities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class FlashingSprite extends LERPSprite{
	private float minAlpha;
	private float changeRange;
	private float currentAlphaOffset = 0;
	private boolean shouldFlash;
	
//	//sin function was flickery, so emulating it.
//	private float spotInRange = 0;
//	private float sign = 1;
//	private float maxHalfRange = 1000;
	
	public FlashingSprite(Texture texture, float minimumAlpha) {
		super(texture);
		
		this.minAlpha = MathUtils.clamp(minimumAlpha, 0, 1);
		this.changeRange = (1 - minAlpha);
	}
	
	public void logic() {
		super.logic();
		
		if(shouldFlash) {

			this.currentAlphaOffset = (float) (changeRange * Math.sin( (double) System.currentTimeMillis() * (180 / Math.PI) * (0.00005)));
			setAlpha(minAlpha + (currentAlphaOffset * changeRange));
		}
	}
	
	public void setShouldFlash(boolean flashSprite) {
		this.shouldFlash = flashSprite;
		
		if(!shouldFlash) {
			setAlpha(1);
		}
	}

}
