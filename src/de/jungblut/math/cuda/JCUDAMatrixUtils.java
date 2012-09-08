package de.jungblut.math.cuda;

import java.util.Random;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;
import jcuda.jcublas.cublasPointerMode;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
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

  public static boolean CUDA_AVAILABLE = false;

  private static cublasHandle handle;

  static {
    try {
      // Disable exceptions and omit subsequent error checks
      JCublas2.setExceptionsEnabled(true);
      JCuda.setExceptionsEnabled(true);
      cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
      JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
      // actually here is only cublas2 available.
      if (Integer.parseInt(cudaDeviceProp.getName().replaceAll("[^\\d]", "")) > 400) {
        JCublas2.initialize();
        CUDA_AVAILABLE = true;
        System.out
            .println("Using device " + cudaDeviceProp.getName()
                + " with total RAM of " + cudaDeviceProp.totalGlobalMem
                + " bytes!");
      }

      handle = new cublasHandle();
      JCublas2.cublasCreate(handle);
      JCublas2.cublasSetPointerMode(handle,
          cublasPointerMode.CUBLAS_POINTER_MODE_HOST);
      // cleanup that handle at the end of this process
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          JCUDAMatrixUtils.cublasDestroy(handle);
        }
      });

    } catch (Throwable e) {
      // e.printStackTrace();
      System.out.println(e.getLocalizedMessage());
    }
  }

  /**
   * Multiplies matrix a with matrix b and returns a new matrix.
   */
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b) {
    return multiply(a, b, false, false);
  }

  /**
   * Multiplies matrix a with matrix b and returns a new matrix. You can add
   * transpose flags for both matrices.
   */
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b, boolean transposeA, boolean transposeB) {
    Pointer matrixPointerA = memcpyMatrix(a);
    Pointer matrixPointerB = memcpyMatrix(b);

    int m = a.getRowCount();
    int n = b.getColumnCount();
    int k = a.getColumnCount();

    // leading dimensions
    int ldA = a.getRowCount();
    int ldB = b.getRowCount();
    int ldC = a.getRowCount();

    // recalculate the parameters for transposes
    if (transposeA && transposeB) {
      m = a.getColumnCount();
      n = b.getRowCount();
      k = b.getColumnCount();
      ldC = a.getColumnCount();
    } else if (transposeB) {
      n = b.getRowCount();
    } else if (transposeA) {
      m = a.getColumnCount();
      k = a.getRowCount();
      ldC = a.getColumnCount();
    }

    // Prepare the pointer for the result in DEVICE memory
    Pointer deviceResultPointer = new Pointer();
    int resMatrixSize = m * n;
    JCuda.cudaMalloc(deviceResultPointer, Sizeof.DOUBLE * resMatrixSize);

    Pointer alpha = Pointer.to(new double[] { 1.0d });
    Pointer beta = Pointer.to(new double[] { 0.0d });

    int transA = transposeA ? cublasOperation.CUBLAS_OP_T
        : cublasOperation.CUBLAS_OP_N;
    int transB = transposeB ? cublasOperation.CUBLAS_OP_T
        : cublasOperation.CUBLAS_OP_N;

    JCublas2.cublasDgemm(handle, transA, transB, m, n, k, alpha,
        matrixPointerA, ldA, matrixPointerB, ldB, beta, deviceResultPointer,
        ldC);

    JCuda.cudaDeviceSynchronize();

    DenseDoubleMatrix matrix = getMatrix(deviceResultPointer, m, n);

    freePointer(matrixPointerA);
    freePointer(matrixPointerB);
    freePointer(deviceResultPointer);
    freePointer(alpha);
    freePointer(beta);
    return matrix;
  }

  private static Pointer memcpyMatrix(DenseDoubleMatrix a) {
    int matrixSizeA = a.getColumnCount() * a.getRowCount();

    double[] matrix = new double[matrixSizeA];
    // store in column major format
    for (int i = 0; i < a.getColumnCount(); i++) {
      double[] column = a.getColumn(i);
      System.arraycopy(column, 0, matrix, i * column.length, column.length);
    }

    Pointer deviceMatrixA = new Pointer();
    JCuda.cudaMalloc(deviceMatrixA, matrixSizeA * Sizeof.DOUBLE);
    JCublas2.cublasSetMatrix(a.getRowCount(), a.getColumnCount(),
        Sizeof.DOUBLE, Pointer.to(matrix), a.getRowCount(), deviceMatrixA,
        a.getRowCount());

    return deviceMatrixA;
  }

  private static DenseDoubleMatrix getMatrix(Pointer src, int rows, int columns) {
    double[] raw = new double[rows * columns];
    Pointer dst = Pointer.to(raw);
    JCublas2
        .cublasGetMatrix(rows, columns, Sizeof.DOUBLE, src, rows, dst, rows);
    return new DenseDoubleMatrix(raw, rows, columns);
  }

  private static void cublasDestroy(cublasHandle handle) {
    JCublas2.cublasDestroy(handle);
  }

  private static void freePointer(Pointer p) {
    JCuda.cudaFree(p);
  }

  public static void main(String[] args) {

    int n = 40000;
    int k = 784;
    int m = 300;

    DenseDoubleMatrix a = new DenseDoubleMatrix(n, k, new Random());
    DenseDoubleMatrix b = new DenseDoubleMatrix(k, m, new Random());
    long start = System.currentTimeMillis();
    DenseDoubleMatrix multiplyCPU = (DenseDoubleMatrix) a.multiply(b);
    System.out.println("CPU took: " + (System.currentTimeMillis() - start)
        / 1000f + "s!");
    start = System.currentTimeMillis();
    DenseDoubleMatrix multiplyGPU = multiply(a, b);
    System.out.println("GPU took: " + (System.currentTimeMillis() - start)
        / 1000f + "s!");
    System.out.println("Matrix difference: "
        + multiplyCPU.subtract(multiplyGPU).sum());
  }

}
