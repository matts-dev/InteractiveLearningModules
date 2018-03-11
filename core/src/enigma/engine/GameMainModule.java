package enigma.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;

import enigma.engine.baseconversion.BaseConversionModule;
import enigma.engine.floatingpoint.MasterFloatingPointModule;
import enigma.engine.fsm.MasterFSMModule;
import enigma.engine.regex.MasterRegularExpressionModule;
import enigma.engine.sorting.ISortInstructionModule;
import enigma.engine.sorting.QSortInstructionModule;
import enigma.engine.sorting.QSortPracticeModule;

//@formatter:off
enum module
{
	REGEX,
	FSM,
	FLOAT_POINT,
	QSORT_INSTRUCTION,
	QSORT_PRACTICE,
	INSERT_SORT_INSTRUCTION,
	INSERT_SORT_PRACTICE,
	SELECTION_SORT_INSTRUCTION,
	SELECTION_SORT_PRACTICE,
	BASE_CONVERSION,
	BINARY	
}
//@formatter:on

public class GameMainModule extends CourseModule {
	public static final module compileTimeModuleSetting = module.BASE_CONVERSION;
	
	public GameMainModule(OrthographicCamera camera) {
		super(camera);

		switch (GameMainModule.compileTimeModuleSetting) {
		case REGEX:
			subModules.add(new MasterRegularExpressionModule(camera)); //regular expression module
			break;
		case FSM:
			subModules.add(new MasterFSMModule(camera));
			break;
		case FLOAT_POINT:
			subModules.add(new MasterFloatingPointModule(camera));
			break;
		case QSORT_INSTRUCTION:
			subModules.add(new QSortInstructionModule(camera));

			break;
		case QSORT_PRACTICE:
			subModules.add(new QSortPracticeModule(camera));

			break;
		case SELECTION_SORT_INSTRUCTION:

			break;
		case SELECTION_SORT_PRACTICE:

			break;
		case INSERT_SORT_INSTRUCTION:
			
			break;
		case BASE_CONVERSION:
			subModules.add(new BaseConversionModule(camera));
			break;
		case INSERT_SORT_PRACTICE:
			subModules.add(new ISortInstructionModule(camera));
			break;
		case BINARY:
			
			break;
		default:
			break;
		}
		

		// load the first module
		loadCurrentModule();
	}

	@Override
	public void logic() {
		super.logic();
	}
}


