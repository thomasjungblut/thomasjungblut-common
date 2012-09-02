package de.jungblut.math.minimize;

import java.util.Arrays;
import java.util.Random;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Particle Swarm Optimization algorithm to minimize costfunctions. This works
 * quite like {@link GradientDescent}, but for this to work we don't need to
 * have a derivative, it is enough to provide the cost at a certain parameter
 * position (theta). For additional information you can browse the wikipedia
 * page: <a
 * href="http://en.wikipedia.org/wiki/Particle_swarm_optimization">Particle
 * swarm optimization article on Wikipedia</a>
 * 
 * @author thomas.jungblut
 * 
 */
public final class ParticleSwarmOptimization implements Minimizer {

  private final int numParticles;
  private final double alpha;
  private final double beta;
  private final double phi;

  public ParticleSwarmOptimization(int numParticles, double alpha, double beta,
      double phi) {
    this.numParticles = numParticles;
    this.alpha = alpha;
    this.beta = beta;
    this.phi = phi;
  }

  @Override
  public final DoubleVector minimize(CostFunction f, DoubleVector pInput,
      int maxIterations, boolean verbose) {
    // setup
    Random random = new Random();
    DoubleVector globalBestPosition = pInput;
    DoubleVector[] particlePositions = new DoubleVector[numParticles];
    double[] particlePersonalBestCost = new double[numParticles];
    // we are going to spread the particles a bit
    for (int i = 0; i < numParticles; i++) {
      particlePositions[i] = new DenseDoubleVector(Arrays.copyOf(
          pInput.toArray(), pInput.getLength()));
      for (int j = 0; j < particlePositions[i].getLength(); j++) {
        particlePositions[i].set(j, particlePositions[i].get(j)
            + particlePositions[i].get(j) * random.nextDouble());
      }
      particlePersonalBestCost[i] = f.evaluateCost(particlePositions[i])
          .getFirst();
    }
    // everything else will be seeded to the start position
    DoubleVector[] particlePersonalBestPositions = new DoubleVector[numParticles];
    Arrays.fill(particlePersonalBestPositions, pInput);
    double globalCost = f.evaluateCost(pInput).getFirst();

    // loop as long as we haven't reached our max iterations
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      // loop over all particles and calculate new positions
      for (int particleIndex = 0; particleIndex < particlePositions.length; particleIndex++) {
        DoubleVector currentPosition = particlePositions[particleIndex];
        DoubleVector currentBest = particlePersonalBestPositions[particleIndex];
        DenseDoubleVector vec = new DenseDoubleVector(pInput.getDimension());
        for (int index = 0; index < vec.getDimension(); index++) {
          double value = (phi * currentPosition.get(index)) // inertia
              + (alpha * random.nextDouble() * (currentBest.get(index) - currentPosition
                  .get(index))) // personal memory
              + (beta * random.nextDouble() * (globalBestPosition.get(index) - currentPosition
                  .get(index))); // group memory
          vec.set(index, value);
        }
        particlePositions[particleIndex] = vec;
        double cost = f.evaluateCost(vec).getFirst();
        // check if we have a personal best
        if (cost < particlePersonalBestCost[particleIndex]) {
          particlePersonalBestCost[particleIndex] = cost;
          particlePersonalBestPositions[particleIndex] = vec;
          // if we had a personal best, do we have a better global?
          if (cost < globalCost) {
            globalCost = cost;
            globalBestPosition = vec;
          }
        }
      }
      if (verbose) {
        System.out.print("Interation " + iteration + " | Cost: " + globalCost
            + "\r");
      }
    }

    return globalBestPosition;
  }

  /**
   * Minimize a function using particle swarm optimization.
   * 
   * @param f the cost function to minimize. Note that the returned gradient
   *          will be ignored, because it is not needed in this algorithm.
   * @param pInput the initial starting point of the algorithm / particles. This
   *          is very important to choose, since this is considered a fast
   *          algorithm you could try out multiple starting points.
   * @param numParticles how many particles to use.
   * @param alpha personal memory weighting.
   * @param beta group memory weighting.
   * @param phi own velocity weighting (inertia).
   * @param maxIterations how many iterations this algorithm should perform.
   * @param verbose if true prints progress to STDOUT.
   * @return a optimized parameter set for the costfunction.
   */
  public static DoubleVector minimizeFunction(CostFunction f,
      DoubleVector pInput, final int numParticles, double alpha, double beta,
      double phi, final int maxIterations, final boolean verbose) {
    return new ParticleSwarmOptimization(numParticles, alpha, beta, phi)
        .minimize(f, pInput, maxIterations, verbose);
  }

}
