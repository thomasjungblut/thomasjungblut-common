package de.jungblut.math.cuda;

import java.util.Random;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;
import jcuda.jcublas.cublasPointerMode;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jungblut.math.dense.DenseDoubleMatrix;

/**
 * Matrix utilities for CUDA graphics card greater version 400, e.g. Nvidia
 * 480gtx.
 * 
 * <br/>
 * -Djava.library.path="/lib/;${env_var:PATH}" must be added to the running VM.
 * If you have the cuda libs in the /lib folder or under windows in your path
 * variables.
 * 
 * @author thomas.jungblut
 * 
 */
public final class JCUDAMatrixUtils {

  private static final Logger LOG = LogManager
      .getLogger(JCUDAMatrixUtils.class);

  public static boolean EXCEPTIONS_ENABLED = false;
  public static boolean CUBLAS2_AVAILABLE = false;

  private static cublasHandle handle;

  static {
    try {
      JCuda.setExceptionsEnabled(EXCEPTIONS_ENABLED);
      cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
      JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
      // verify that compute capability of 1.3 is available, because only
      // here is the double precision operation allowed.
      if (cudaDeviceProp.major <= 1 && cudaDeviceProp.minor < 3) {
        throw new IllegalArgumentException(
            "WARN Double precision computing only allowed since capability 1.3! You have "
                + cudaDeviceProp.major
                + "."
                + cudaDeviceProp.minor
                + "! If you have exceptions turned off, then this may result in strange behaviour.");
      }
      // actually here is only cublas2 available.
      if (Integer.parseInt(cudaDeviceProp.getName().replaceAll("[^\\d]", "")) > 400) {
        JCublas2.setExceptionsEnabled(EXCEPTIONS_ENABLED);
        JCublas2.initialize();
        CUBLAS2_AVAILABLE = true;
        handle = new cublasHandle();
        JCublas2.cublasCreate(handle);
        JCublas2.cublasSetPointerMode(handle,
            cublasPointerMode.CUBLAS_POINTER_MODE_HOST);
      } else {
        JCublas.setExceptionsEnabled(EXCEPTIONS_ENABLED);
        JCublas.cublasInit();
      }

      // cleanup that handle at the end of this process
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          JCUDAMatrixUtils.cublasDestroy(handle);
        }
      });

      LOG.info("Using device " + cudaDeviceProp.getName()
          + " with total RAM of "
          + FileUtils.byteCountToDisplaySize(cudaDeviceProp.totalGlobalMem)
          + ". Compute capability: " + cudaDeviceProp.major + "."
          + cudaDeviceProp.minor);

    } catch (Throwable e) {
      // e.printStackTrace();
      LOG.error(e.getLocalizedMessage());
    }
  }

  /**
   * Multiplies matrix A with matrix B and returns a new matrix.
   */
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b) {
    return multiply(a, b, false, false);
  }

  /**
   * Multiplies matrix A with matrix B (these are pointers, thus the dimension
   * must be passed and returns a new matrix.
   */
  public static DenseDoubleMatrix multiply(Pointer a, Pointer b,
      MatrixDimension dim) {

    // Prepare the pointer for the result in DEVICE memory
    Pointer deviceResultPointer = new Pointer();
    int resMatrixSize = dim.getM() * dim.getN();
    int transA = dim.isTransposeA() ? cublasOperation.CUBLAS_OP_T
        : cublasOperation.CUBLAS_OP_N;
    int transB = dim.isTransposeB() ? cublasOperation.CUBLAS_OP_T
        : cublasOperation.CUBLAS_OP_N;

    if (CUBLAS2_AVAILABLE) {
      JCuda.cudaMalloc(deviceResultPointer, Sizeof.DOUBLE * resMatrixSize);
      Pointer alpha = Pointer.to(new double[] { 1.0d });
      Pointer beta = Pointer.to(new double[] { 0.0d });
      JCublas2.cublasDgemm(handle, transA, transB, dim.getM(), dim.getN(),
          dim.getK(), alpha, a, dim.getLdA(), b, dim.getLdB(), beta,
          deviceResultPointer, dim.getLdC());
      freePointer(alpha);
      freePointer(beta);
    } else {
      JCublas.cublasAlloc(resMatrixSize, Sizeof.DOUBLE, deviceResultPointer);
      JCublas.cublasDgemm(transA == 0 ? 'n' : 'y', transB == 0 ? 'n' : 'y',
          dim.getM(), dim.getN(), dim.getK(), 1d, a, dim.getLdA(), b,
          dim.getLdB(), 0d, deviceResultPointer, dim.getLdC());
    }

    JCuda.cudaDeviceSynchronize();

    DenseDoubleMatrix matrix = getMatrix(deviceResultPointer, dim.getM(),
        dim.getN());

    freePointer(deviceResultPointer);

    return matrix;
  }

  /**
   * Multiplies matrix a with matrix b and returns a new matrix. You can add
   * transpose flags for both matrices.
   */
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b, boolean transposeA, boolean transposeB) {
    Pointer matrixPointerA = memcpyMatrix(a);
    Pointer matrixPointerB = memcpyMatrix(b);
    DenseDoubleMatrix matrix = multiply(matrixPointerA, matrixPointerB,
        new MatrixDimension(a, b, transposeA, transposeB));
    freePointer(matrixPointerA);
    freePointer(matrixPointerB);
    return matrix;
  }

  /**
   * Copies the given matrix to the device memory in column major format.
   * 
   * @return a pointer to this matrix.
   */
  public static Pointer memcpyMatrix(DenseDoubleMatrix a) {
    int matrixSizeA = a.getColumnCount() * a.getRowCount();
    double[] matrix = a.getColumnMajorMatrix();
    Pointer deviceMatrixA = new Pointer();
    JCuda.cudaMalloc(deviceMatrixA, matrixSizeA * Sizeof.DOUBLE);
    if (CUBLAS2_AVAILABLE) {
      JCublas2.cublasSetMatrix(a.getRowCount(), a.getColumnCount(),
          Sizeof.DOUBLE, Pointer.to(matrix), a.getRowCount(), deviceMatrixA,
          a.getRowCount());
    } else {
      JCublas.cublasSetMatrix(a.getRowCount(), a.getColumnCount(),
          Sizeof.DOUBLE, Pointer.to(matrix), a.getRowCount(), deviceMatrixA,
          a.getRowCount());
    }

    return deviceMatrixA;
  }

  /**
   * Read a matrix from device memory.
   * 
   * @param src the head pointer to the matrix.
   * @param rows the number of rows.
   * @param columns the number of columns
   * @return a new matrix with the results from device.
   */
  public static DenseDoubleMatrix getMatrix(Pointer src, int rows, int columns) {
    double[] raw = new double[rows * columns];
    Pointer dst = Pointer.to(raw);
    if (CUBLAS2_AVAILABLE) {
      JCublas2.cublasGetMatrix(rows, columns, Sizeof.DOUBLE, src, rows, dst,
          rows);
    } else {
      JCublas.cublasGetMatrix(rows, columns, Sizeof.DOUBLE, src, rows, dst,
          rows);
    }
    return new DenseDoubleMatrix(raw, rows, columns);
  }

  /**
   * Frees the given pointer.
   * 
   * @param p the pointer to free
   */
  public static void freePointer(Pointer p) {
    JCuda.cudaFree(p);
  }

  private static void cublasDestroy(cublasHandle handle) {
    if (CUBLAS2_AVAILABLE) {
      JCublas2.cublasDestroy(handle);
    } else {
      JCublas.cublasShutdown();
    }
  }

  /*
   * Simple benchmarking between CPU and GPU.
   */
  public static void main(String[] args) {

    int n = 40000;
    int k = 784;
    int m = 300;

    DenseDoubleMatrix a = new DenseDoubleMatrix(n, k, new Random());
    DenseDoubleMatrix b = new DenseDoubleMatrix(k, m, new Random());
    long start = System.currentTimeMillis();
    DenseDoubleMatrix multiplyGPU = multiply(a, b);
    LOG.info("GPU took: " + (System.currentTimeMillis() - start) / 1000f + "s!");
    start = System.currentTimeMillis();
    DenseDoubleMatrix multiplyCPU = (DenseDoubleMatrix) a.multiply(b);
    LOG.info("CPU took: " + (System.currentTimeMillis() - start) / 1000f + "s!");
    LOG.info("Matrix difference: " + multiplyCPU.subtract(multiplyGPU).sum());
  }

}
