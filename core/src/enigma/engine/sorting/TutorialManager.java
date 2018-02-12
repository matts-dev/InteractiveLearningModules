package enigma.engine.sorting;

import com.badlogic.gdx.graphics.OrthographicCamera;

public abstract class TutorialManager {

	public abstract SortableArray getArray();
	
	protected abstract void createInstructions();

	public abstract boolean active();

	public abstract void logic();

	public abstract boolean touchDown(int screenX, int screenY, int pointer, int button, OrthographicCamera camera);

	public abstract boolean touchUp(int screenX, int screenY, int pointer, int button, OrthographicCamera camera);

	public abstract boolean touchDragged(int screenX, int screenY, int pointer, OrthographicCamera camera);
}
