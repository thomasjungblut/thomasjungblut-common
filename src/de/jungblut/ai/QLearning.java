package de.jungblut.ai;

import java.util.HashMap;
import java.util.Random;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;

/**
 * Vanilla Q-Learning algorithm that saves Q-Values as a table in memory. Based
 * on the explanation of EdX here: https://www.youtube.com/watch?v=W5IfMphITbY
 * and http://en.wikipedia.org/wiki/Q-learning
 * 
 * @author thomas.jungblut
 * 
 */
public final class QLearning {

  private static final float DEFAULT_EXPLORATION_PROBABILITY = 0.3f;

  /*
   * First dimension: states, second dimension for each state the associated
   * action. The values of each cell, represent the q value.
   */
  private final double[][] qValues;
  private final int numActions;

  // a map between the endstate index and the associated reward granted when
  // reaching the goal
  private final HashMap<Integer, Double> rewardMap = new HashMap<>();
  private final float explorationProbability;
  private final Random random;

  /**
   * Creates a QLearning instance with given number of states and actions and a
   * default exploration probability of 30%.
   * 
   * @param numStates the number of states in your model.
   * @param numActions the number of actions in your model
   */
  public QLearning(int numStates, int numActions) {
    this(numStates, numActions, DEFAULT_EXPLORATION_PROBABILITY);
  }

  /**
   * Creates a QLearning instance with given number of states and actions and
   * the given exploration probability.
   * 
   * @param numStates the number of states in your model.
   * @param numActions the number of actions in your model
   * @param explorationProbability the probability that the next action is
   *          randomly selected.
   */
  public QLearning(int numStates, int numActions, float explorationProbability) {
    this(numStates, numActions, explorationProbability, System
        .currentTimeMillis());
  }

  /**
   * Test and root constructor for checks and tests.
   */
  QLearning(int numStates, int numActions, float explorationProbability,
      long seed) {
    Preconditions.checkArgument(numStates > 1,
        "Number of states must be at least > 1. Supplied: " + numStates);
    Preconditions.checkArgument(numActions > 1,
        "Number of actions must be at least > 1. Supplied: " + numActions);
    Preconditions.checkArgument(explorationProbability >= 0f
        && explorationProbability <= 1f,
        "ExplorationProbability must be between 0f and 1f! Supplied: "
            + explorationProbability);
    // invert the probability, to save 1-X statements later on
    this.explorationProbability = 1f - explorationProbability;
    this.numActions = numActions;
    this.qValues = new double[numStates][numActions];
    this.random = new Random(seed);
  }

  // accessed to sanity check the qvalues
  double[][] getQValues() {
    return this.qValues;
  }

  /**
   * Adds an end state to this model and associate it with a reward.
   * 
   * @param endState the identifier of the endstate.
   * @param reward the reward- can also be negative for a penalty.
   */
  public void addEndState(int endState, double reward) {
    rewardMap.put(endState, reward);
  }

  /**
   * @return true if the provided state is an registered end state.
   */
  public boolean isEndState(int state) {
    return rewardMap.containsKey(state);
  }

  /**
   * @return the next action your model needs to take based on the current
   *         state. This is either randomly selected based on the given
   *         explorationProbability in the constructor, or by choosing the
   *         largest q-value of the current state.
   */
  public int getNextAction(int currentState) {
    // check if we need to explore
    if (explorationProbability > 0f
        && random.nextFloat() > explorationProbability) {
      // choose a random option of the statespace
      return random.nextInt(numActions);
    } else {
      return ArrayUtils.maxIndex(qValues[currentState]);
    }
  }

  /**
   * Updates the last state's q-value.
   * 
   * @param lastState the last state.
   * @param executedAction the action from the last state we took to this state.
   * @param currentState the current state we are in, after executed the
   *          executedAction from the lastState.
   * @param learningRate the learning rate.
   * @param discount the discount factor.
   */
  public void update(int lastState, int executedAction, int currentState,
      double learningRate, double discount) {
    double reward = 0;
    // check for rewards
    Double qVal = rewardMap.get(currentState);
    if (qVal != null) {
      reward = qVal.doubleValue();
    }

    double lastQValue = qValues[lastState][executedAction];
    qValues[lastState][executedAction] = lastQValue + learningRate
        * (reward + discount * ArrayUtils.max(qValues[currentState]));
  }

}
