package de.jungblut.writable;

import com.google.common.base.Preconditions;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.sparse.SparseDoubleVector;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

/**
 * Writable class for dense and sparse matrices.
 */
public final class MatrixWritable implements Writable {

    public static final byte DENSE_DOUBLE_MATRIX = 0x01;
    public static final byte SPARSE_DOUBLE_ROW_MATRIX = 0x02;

    private DoubleMatrix mat;

    public MatrixWritable() {
    }

    public MatrixWritable(DoubleMatrix mat) {
        Preconditions.checkNotNull(mat);
        this.mat = mat;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        final byte flag = in.readByte();
        switch (flag) {
            case DENSE_DOUBLE_MATRIX:
                mat = readDenseMatrix(in);
                break;
            case SPARSE_DOUBLE_ROW_MATRIX:
                mat = readSparseMatrix(in);
                break;
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (mat.isSparse()) {
            out.writeByte(SPARSE_DOUBLE_ROW_MATRIX);
            writeSparseMatrix((SparseDoubleRowMatrix) mat, out);
        } else {
            out.writeByte(DENSE_DOUBLE_MATRIX);
            writeDenseMatrix((DenseDoubleMatrix) mat, out);
        }
    }

    public DoubleMatrix getMatrix() {
        return this.mat;
    }

    /**
     * Writes a sparse matrix to the given output stream. The layout is as simple
     * as in memory, first the dimension is written (row count, column count as
     * integer) then the number of non-zero row vectors are written (integer).
     * Afterwards for each non-zero row vector the row index (integer) itself is
     * written as well as the count of non-zero elements in the row vector. Then
     * for each non-zero element an integer column index and a double value is
     * following.
     *
     * @param mat the sparse matrix to serialize.
     * @param out the output.
     * @throws IOException in case of an IO error.
     */
    public static void writeSparseMatrix(SparseDoubleRowMatrix mat, DataOutput out)
            throws IOException {
        out.writeInt(mat.getRowCount());
        out.writeInt(mat.getColumnCount());
        int[] rowIndices = mat.rowIndices();
        out.writeInt(rowIndices.length);
        for (int row : rowIndices) {
            out.writeInt(row);
            DoubleVector rowVector = mat.getRowVector(row);
            out.writeInt(rowVector.getLength());
            Iterator<DoubleVectorElement> iterateNonZero = rowVector.iterateNonZero();
            while (iterateNonZero.hasNext()) {
                DoubleVectorElement next = iterateNonZero.next();
                out.writeInt(next.getIndex());
                out.writeDouble(next.getValue());
            }
        }
    }

    /**
     * Reads a sparse matrix from the given input stream.
     *
     * @param in the input stream.
     * @return a new sparse matrix from the stream.
     * @throws IOException in case of an IO error.
     */
    public static SparseDoubleRowMatrix readSparseMatrix(DataInput in)
            throws IOException {
        SparseDoubleRowMatrix mat = new SparseDoubleRowMatrix(in.readInt(),
                in.readInt());

        final int numRowIndices = in.readInt();
        for (int i = 0; i < numRowIndices; i++) {
            final int rowIndex = in.readInt();
            final int numColumns = in.readInt();
            DoubleVector row = new SparseDoubleVector(mat.getColumnCount());
            for (int j = 0; j < numColumns; j++) {
                row.set(in.readInt(), in.readDouble());
            }
            mat.setRowVector(rowIndex, row);
        }

        return mat;
    }

    /**
     * Writes a dense matrix to the given output stream. The layout is as simple
     * as in memory, first the dimension is written (row count, column count as
     * integer) afterwards there are following row*columns doubles in a row major
     * fashion.
     *
     * @param mat the dense matrix to serialize.
     * @param out the output.
     * @throws IOException in case of an IO error.
     */
    public static void writeDenseMatrix(DenseDoubleMatrix mat, DataOutput out)
            throws IOException {
        out.writeInt(mat.getRowCount());
        out.writeInt(mat.getColumnCount());
        for (int row = 0; row < mat.getRowCount(); row++) {
            for (int col = 0; col < mat.getColumnCount(); col++) {
                out.writeDouble(mat.get(row, col));
            }
        }
    }

    /**
     * Reads a dense matrix from the given input stream.
     *
     * @param in the input stream.
     * @return a new dense matrix from the stream.
     * @throws IOException in case of an IO error.
     */
    public static DenseDoubleMatrix readDenseMatrix(DataInput in)
            throws IOException {
        DenseDoubleMatrix mat = new DenseDoubleMatrix(in.readInt(), in.readInt());
        for (int row = 0; row < mat.getRowCount(); row++) {
            for (int col = 0; col < mat.getColumnCount(); col++) {
                mat.set(row, col, in.readDouble());
            }
        }
        return mat;
    }

}
