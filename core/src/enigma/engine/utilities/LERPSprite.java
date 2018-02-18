package enigma.engine.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import enigma.engine.Tools;

public class LERPSprite extends Sprite {
	private static Vector2 utilVec = new Vector2();
	
	protected boolean interpolateToPoint = false;
	protected Vector2 interpolatePoint = new Vector2();
	protected float interpolateSpeed = Tools.convertSpeedTo60FPSValue(10f);
	
	public LERPSprite(Texture texture) {
		super(texture);
	}
	
	public void logic() {
		if (interpolateToPoint) {
			float deltaSpeed = interpolateSpeed * Gdx.graphics.getDeltaTime();

			float distance = Vector2.dst(interpolatePoint.x, interpolatePoint.y, getX(), getY());
			if (distance < 0.001f) {
				interpolateToPoint = false;
				return;
			}

			Vector2 direction = LERPSprite.utilVec.set(interpolatePoint.x - getX(), interpolatePoint.y - getY());
			direction = direction.nor();
			direction.scl(deltaSpeed);

			if (direction.len() > distance) {
				direction.nor();
				direction.scl(distance);
			}

			translate(direction.x, direction.y);
		}
	}
	
	public void setInterpolatePoint(float x, float y) {
		interpolateToPoint = true;
		this.interpolatePoint.set(x, y);
	}
	
	public boolean isInterpolating() {
		return interpolateToPoint;
	}
	
	public void setInterpolateSpeedFactor(float factor) {
		interpolateSpeed = Tools.convertSpeedTo60FPSValue(factor);
	}
}
