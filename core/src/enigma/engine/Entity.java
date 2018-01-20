package enigma.engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Entity {
	public void draw(SpriteBatch batch);
	public void logic();
	public void dispose();
}
