package enigma.engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entity {
	public abstract void draw(SpriteBatch batch);
	public abstract void logic();
	public abstract void dispose();
}
