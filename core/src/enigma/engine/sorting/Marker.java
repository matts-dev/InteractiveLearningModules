package enigma.engine.sorting;

import com.badlogic.gdx.graphics.Texture;

import enigma.engine.utilities.LERPSprite;

public class Marker extends LERPSprite {
	/**
	 * Warning, this index is not enforced and it is up to user to make sure that it
	 * is always set to a valid state when user updates the LERPSprite location.
	 */

	private int pointingToIndex = -1;

	public Marker(Texture texture) {
		super(texture);
	}

	@Override
	public void setInterpolatePoint(float x, float y) {
		throw new RuntimeException("Method is non-safe for Marker type. Please use signature that includes index");
	}
	
	public void setInterpolatePoint(float x, float y, int index) {
		super.setInterpolatePoint(x, y);
		this.pointingToIndex = index;
	}

	public void setIndexlocation(int idx) {
		this.pointingToIndex = idx;
	}

	public int getPointingIndexLocation() {
		return pointingToIndex;
	}

}
