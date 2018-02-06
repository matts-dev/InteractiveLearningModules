package enigma.engine.sorting;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import enigma.engine.CourseModule;
import enigma.engine.Draggable;
import enigma.engine.TextureLookup;

enum SortType{SELECTION, INSERTION, QUICK}

public class SortModule extends CourseModule {
	/** a vector to hold converted touch coordinates into game world coordinates */
	//private Vector3 convertedTouchVect = new Vector3(0, 0, 0);
	private boolean devMode = true;
	protected ShapeRenderer sr;
	protected Draggable dragTarget = null;

	private SortableArray array;

	private float elementWidth = 30;
	private int numElements;
	private Random rng;
	
	private SortType sortType = SortType.QUICK;

	/**
	 * Constructor
	 * 
	 * @param camera
	 *            the Orthographic camera. This is used to convert points
	 */
	public SortModule(OrthographicCamera camera) {
		super(camera);
		sr = TextureLookup.shapeRenderer;
//		rng = new Random();
		rng = new Random(33); //set seed for all generation by uncommenting this line. 

		// randomly chose between 7-9 elements
		numElements = rng.nextInt(3) + 7;

		createNewArray();
	}

	private void createNewArray() {
		//array = new InsertionSortableArray(Gdx.graphics.getWidth() / 2, 200, elementWidth, numElements, 10, rng.nextInt());
		//array = new SelectionSortableArray(Gdx.graphics.getWidth() / 2, 200, elementWidth, numElements, 10, rng.nextInt());
		
		if(sortType == SortType.QUICK) {
			array = new QuickSortableArray(Gdx.graphics.getWidth() / 2, 200, elementWidth, numElements, 10, rng.nextInt());
		} else if(sortType == SortType.INSERTION) {
			array = new InsertionSortableArray(Gdx.graphics.getWidth() / 2, 200, elementWidth, numElements, 10, rng.nextInt());
		} else if(sortType == SortType.SELECTION) {
			array = new SelectionSortableArray(Gdx.graphics.getWidth() / 2, 200, elementWidth, numElements, 10, rng.nextInt());
		}
		array.centerOnPoint(Gdx.graphics.getWidth() * .5f, Gdx.graphics.getHeight() * .2f);
	}

	@Override
	public void logic() {
		super.logic();
		array.logic();
		
		
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
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
				sortType = SortType.SELECTION;
				createNewArray();
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
				sortType = SortType.INSERTION;
				createNewArray();
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
				sortType = SortType.QUICK;
				createNewArray();
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {

			}
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
			createNewArray();
		}

	}

	@Override
	public void draw(SpriteBatch batch) {
		// draw any sub modules (super call)
		super.draw(batch);
		
		boolean batchWasDrawing = false;
		if (batch.isDrawing()) {
			array.drawPreSprites(batch);
			
			
			// shape rendering cannot begin while batch isDrawing.
			batch.end();
			// flag that batch should start back after drawing of columns.
			batchWasDrawing = true;
		}
		if (sr.isDrawing()) throw new RuntimeException("The shape rendering should not be pre-drawing");

		sr.setColor(TextureLookup.foregroundColor);
		sr.begin(ShapeType.Filled);
		array.draw(batch);
		sr.end();

		if (batchWasDrawing) {
			// restore batch drawing.
			batch.begin();
			array.drawPostSprites(batch);
		}
	}

	@Override
	public void dispose() {
		// dispose any sub-modules
		super.dispose();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return array.touchDown(screenX, screenY, pointer, button, camera);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return array.touchUp(screenX, screenY, pointer, button, camera);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return array.touchDragged(screenX, screenY, pointer, camera);
		
	}
}
