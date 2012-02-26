package de.jungblut.math.matrix.cuda;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas2;
import jcuda.jcublas.cublasHandle;
import jcuda.jcublas.cublasOperation;
import jcuda.jcublas.cublasPointerMode;
import jcuda.runtime.JCuda;
import jcuda.runtime.cudaDeviceProp;
import jcuda.runtime.cudaMemcpyKind;
import de.jungblut.math.DenseDoubleMatrix;

// -Djava.library.path="/lib/;${env_var:PATH}" must be added to the running VM
public class JCUDAMatrixUtils {

  public static boolean CUDA_AVAILABLE = false;
  private static cublasHandle handle;

  static {
    try {
      // Disable exceptions and omit subsequent error checks
      JCublas2.setExceptionsEnabled(true);
      JCuda.setExceptionsEnabled(true);
      cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
      JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
      System.out.println("Using device " + cudaDeviceProp.getName()
          + " with VRAM of " + cudaDeviceProp.totalGlobalMem + " bytes!");
      handle = new cublasHandle();
      JCublas2.cublasCreate(handle);
      JCublas2.initialize();
      JCublas2.cublasSetPointerMode(handle,
          cublasPointerMode.CUBLAS_POINTER_MODE_DEVICE);
      CUDA_AVAILABLE = true;
    } catch (Throwable e) {
      // e.printStackTrace();
      System.out.println(e.getLocalizedMessage());
    }
  }

  // TODO transpose can be actually done on GPU as well
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b) {
    Pointer alpha = new Pointer();
    JCuda.cudaMalloc(alpha, Sizeof.DOUBLE);
    JCuda.cudaMemcpy(alpha, Pointer.to(new double[] { 1.0d }), Sizeof.DOUBLE,
        cudaMemcpyKind.cudaMemcpyHostToDevice);

    Pointer beta = new Pointer();
    JCuda.cudaMalloc(beta, Sizeof.DOUBLE);
    JCuda.cudaMemcpy(beta, Pointer.to(new double[] { 0.0d }), Sizeof.DOUBLE,
        cudaMemcpyKind.cudaMemcpyHostToDevice);

    Pointer matrixPointerA = memcpyMatrix(a);
    Pointer matrixPointerB = memcpyMatrix(b);

    // Prepare the pointer for the result in DEVICE memory
    Pointer deviceResultPointer = new Pointer();
    int resMatrixSize = a.getRowCount() * b.getColumnCount();
    JCuda.cudaMalloc(deviceResultPointer, Sizeof.DOUBLE * resMatrixSize);

    JCublas2.cublasDgemm(handle, cublasOperation.CUBLAS_OP_N,
        cublasOperation.CUBLAS_OP_N, a.getRowCount(), b.getColumnCount(),
        a.getColumnCount(), alpha, matrixPointerA, a.getRowCount(),
        matrixPointerB, b.getRowCount(), beta, deviceResultPointer,
        a.getRowCount());

    JCuda.cudaDeviceSynchronize();

    DenseDoubleMatrix matrix = getMatrix(deviceResultPointer, a.getRowCount(),
        b.getColumnCount());

    freePointer(matrixPointerA);
    freePointer(matrixPointerB);
    freePointer(deviceResultPointer);
//    cublasDestroy(handle);

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

  @SuppressWarnings("unused") // seems to have problems with latest CUDA
  private static final void cublasDestroy(cublasHandle handle) {
    JCublas2.cublasDestroy(handle);
  }

  private static void freePointer(Pointer p) {
    JCuda.cudaFree(p);
  }

  public static void main(String[] args) {
    DenseDoubleMatrix a = new DenseDoubleMatrix(new double[][] { { 1, 2, 3 },
        { 4, 5, 6 } });
    DenseDoubleMatrix b = new DenseDoubleMatrix(new double[][] { { 6, -1 },
        { 3, 2 }, { 0, -3 } });

    System.out.println(a.multiply(b));
    System.out.println(multiply(a, b));
  }

}
