package enigma.engine.floatingpoint;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GenericLearnModule
{
	public void logic();
	public void IO() ;
	public void draw(SpriteBatch batch, float lastModuleFraction);
	public void dispose();
	public boolean touchDown(int screenX, int screenY, int pointer, int button);
	public boolean touchUp(int screenX, int screenY, int pointer, int button);
	public boolean touchDragged(int screenX, int screenY, int pointer);
}
