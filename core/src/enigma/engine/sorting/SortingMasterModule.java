package enigma.engine.sorting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import enigma.engine.CourseModule;

public class SortingMasterModule extends CourseModule {
	/**
	 * Constructor
	 * 
	 * @param camera
	 *            the Orthographic camera. This is used to convert points
	 */
	public SortingMasterModule(OrthographicCamera camera) {
		super(camera);
		//subModules.add(new SortModule(camera));
		subModules.add(new QSortInstructionModule(camera));
//		subModules.add(new QSortPracticeModule(camera));
		loadCurrentModule();
	}

	@Override
	public void logic() {
		super.logic();
	}

	@Override
	public void IO() {
		// do sub module IO
		super.IO();

		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
		}

		if (devMode && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {

			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {

			}
		}

	}

	@Override
	public void draw(SpriteBatch batch) {
		// draw any sub modules (super call)
		super.draw(batch);
	}

	@Override
	public void dispose() {
		// dispose any sub-modules
		super.dispose();
	}
}
