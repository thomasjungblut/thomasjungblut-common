package de.jungblut.kaggle.asap;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import weka.classifiers.Evaluation;
import de.jungblut.weka.ClassificationTask;
import de.jungblut.weka.LearningController;

/**
 * @author thomas.jungblut
 */
public class ASAPController extends LearningController {

  static final File baseInput = new File(
      "/Users/thomas.jungblut/kaggle/asap/files/");

  @Override
  public File getTrainingInput() {
    return new File(baseInput, "training_set_rel3.csv");
  }

  @Override
  public File getValidationInput() {
    return new File(baseInput, "training_set_rel3.csv");
  }

  @Override
  public File getOutput() {
    return new File(baseInput, "prediction.csv");
  }

  @Override
  public List<ClassificationTask> getTasks() {
    return Arrays.asList(new ClassificationTask[] { new GlobalModel(1)
//    ,new GlobalModel(2), new GlobalModel(3), new GlobalModel(4),
//        new GlobalModel(5), new GlobalModel(6), new GlobalModel(7),
//        new GlobalModel(8) 
    });
  }

  @Override
  public String[] getHeader() {
    return isDebug() ? new String[] { "prediction_id", "essay_id", "essay_set",
        "essay_weight", "predicted_score", "real_score", "essay" }
        : new String[] { "prediction_id", "essay_id", "essay_set",
            "essay_weight", "predicted_score" };
  }

  @Override
  public int getFilterColumn() {
    return 1;
  }

  @Override
  public void outputEvaluation(Evaluation e, ClassificationTask t) {
//    System.out.println("Kappa score: " + e.kappa() + " for task " + t.getId());
    System.out.println("\ntask " + t.getId());
    System.out.println(e.toSummaryString());
  }

  public static void main(String[] args) throws Exception {
    ASAPController control = new ASAPController();
    control.compute();
  }
}
