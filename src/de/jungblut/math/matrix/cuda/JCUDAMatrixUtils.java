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

public class JCUDAMatrixUtils {

  // TODO transpose can be actually done on GPU as well
  public static DenseDoubleMatrix multiply(DenseDoubleMatrix a,
      DenseDoubleMatrix b) {

    // TODO move to static initializer
    // Enable exceptions and omit subsequent error checks
    JCublas2.setExceptionsEnabled(true);
    JCuda.setExceptionsEnabled(true);

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

    // TODO move to static initializer
//    cudaDeviceProp cudaDeviceProp = new cudaDeviceProp();
//    JCuda.cudaGetDeviceProperties(cudaDeviceProp, 0);
//    System.out.println("Found device: " + cudaDeviceProp.getName());

    cublasHandle handle = new cublasHandle();
    JCublas2.cublasCreate(handle);
    JCublas2.cublasSetPointerMode(handle,
        cublasPointerMode.CUBLAS_POINTER_MODE_DEVICE);

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
    cublasDestroy(handle);

    return matrix;
  }

  private static Pointer memcpyMatrix(DenseDoubleMatrix a) {
    int matrixSizeA = a.getColumnCount() * a.getRowCount();

    double[][] values = a.getValues();
    double[] matrix = new double[matrixSizeA];
    for (int i = 0; i < a.getRowCount(); i++) {
      System.arraycopy(values[i], 0, matrix, i * values[i].length,
          values[i].length);
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
    JCublas2.cublasGetMatrix(rows, columns, Sizeof.DOUBLE, src, rows, dst, rows);

    return new DenseDoubleMatrix(raw, rows, columns);
  }

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
