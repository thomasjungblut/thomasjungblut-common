package de.jungblut.math.minimize;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.partition.BlockPartitioner;
import de.jungblut.partition.Boundaries.Range;

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
public final class ParticleSwarmOptimization extends AbstractMinimizer {

  private final int numParticles;
  private final double alpha;
  private final double beta;
  private final double phi;
  private final int numThreads;

  public ParticleSwarmOptimization(int numParticles, double alpha, double beta,
      double phi, int numThreads) {
    this.numParticles = numParticles;
    this.alpha = alpha;
    this.beta = beta;
    this.phi = phi;
    this.numThreads = numThreads;
  }

  @Override
  public final DoubleVector minimize(CostFunction f, DoubleVector pInput,
      int maxIterations, boolean verbose) {
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    // setup
    Random random = new Random();
    DoubleVector globalBestPosition = pInput;
    final DoubleVector[] particlePositions = new DoubleVector[numParticles];
    final double[] particlePersonalBestCost = new double[numParticles];
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

    Set<Range> boundaries = new BlockPartitioner().partition(numThreads,
        numParticles).getBoundaries();

    // everything else will be seeded to the start position
    DoubleVector[] particlePersonalBestPositions = new DoubleVector[numParticles];
    Arrays.fill(particlePersonalBestPositions, pInput);
    double globalCost = f.evaluateCost(pInput).getFirst();

    // loop as long as we haven't reached our max iterations
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      ExecutorCompletionService<Tuple<Double, DoubleVector>> service = new ExecutorCompletionService<>(
          pool);
      for (Range r : boundaries) {
        service.submit(new CallableOptimization(f, pInput.getDimension(),
            globalCost, r, particlePositions, particlePersonalBestCost,
            particlePersonalBestPositions, globalBestPosition));
      }

      for (int i = 0; i < boundaries.size(); i++) {
        try {
          Future<Tuple<Double, DoubleVector>> poll = service.take();
          if (poll != null) {
            Tuple<Double, DoubleVector> tuple = poll.get();
            if (tuple != null) {
              // if we had a personal best, do we have a better global?
              if (tuple.getFirst() < globalCost) {
                globalCost = tuple.getFirst();
                globalBestPosition = tuple.getSecond();
              }
            }
          }
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }

      if (verbose) {
        System.out.print("Iteration " + iteration + " | Cost: " + globalCost
            + "\r");
        onIterationFinished(iteration, globalCost, globalBestPosition);
      }
    }

    pool.shutdownNow();

    return globalBestPosition;
  }

  private final class CallableOptimization implements
      Callable<Tuple<Double, DoubleVector>> {

    private final Random random = new Random();
    private final Range range;
    private final DoubleVector[] particlePositions;
    private final double[] particlePersonalBestCost;
    private final DoubleVector[] particlePersonalBestPositions;
    private final int dim;
    private final CostFunction f;

    private DoubleVector globalBestPosition;
    private double globalCost;

    public CallableOptimization(CostFunction f, int dim, double globalCost,
        Range range, DoubleVector[] particlePositions,
        double[] particlePersonalBestCost,
        DoubleVector[] particlePersonalBestPositions,
        DoubleVector globalBestPosition) {
      this.f = f;
      this.dim = dim;
      this.globalCost = globalCost;
      this.range = range;
      this.particlePositions = particlePositions;
      this.particlePersonalBestCost = particlePersonalBestCost;
      this.particlePersonalBestPositions = particlePersonalBestPositions;
      this.globalBestPosition = globalBestPosition;
    }

    @Override
    public Tuple<Double, DoubleVector> call() throws Exception {
      // loop over all particles and calculate new positions
      for (int particleIndex = range.getStart(); particleIndex < range.getEnd(); particleIndex++) {
        DoubleVector currentPosition = particlePositions[particleIndex];
        DoubleVector currentBest = particlePersonalBestPositions[particleIndex];
        DenseDoubleVector vec = new DenseDoubleVector(dim);
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
      return new Tuple<>(globalCost, globalBestPosition);
    }

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
      double phi, final int maxIterations, final int numThreads,
      final boolean verbose) {
    return new ParticleSwarmOptimization(numParticles, alpha, beta, phi,
        numThreads).minimize(f, pInput, maxIterations, verbose);
  }

}
