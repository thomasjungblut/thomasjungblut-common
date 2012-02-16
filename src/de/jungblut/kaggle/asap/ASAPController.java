package de.jungblut.kaggle.asap;

import de.jungblut.weka.ClassificationTask;
import de.jungblut.weka.LearningController;
import weka.classifiers.Evaluation;

import java.io.File;
import java.util.List;

/**
 * @author thomas.jungblut
 */
public class ASAPController extends LearningController {


    @Override
    public File getTrainingInput() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getValidationInput() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getOutput() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ClassificationTask> getTasks() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getHeader() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getFilterColumn() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void outputEvaluation(Evaluation e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
