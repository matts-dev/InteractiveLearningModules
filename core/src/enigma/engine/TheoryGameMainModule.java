package enigma.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;

import enigma.engine.sorting.SortingMasterModule;

public class TheoryGameMainModule extends CourseModule {
	public TheoryGameMainModule(OrthographicCamera camera) {
		super(camera);

		// add all supported modules		
		//subModules.add(new MasterRegularExpressionModule(camera)); //regular expression module
		//subModules.add(new MasterFSMModule(camera));
		//subModules.add(new MasterFloatingPointModule(camera));
		subModules.add(new SortingMasterModule(camera));


		// load the first module
		loadCurrentModule();
	}

	@Override
	public void logic() {
		super.logic();
	}
}
