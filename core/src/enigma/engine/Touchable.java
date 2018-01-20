package enigma.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;

public interface Touchable {
	public boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera);
	
	public boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera);
	
	public boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera);
}
