package de.jungblut.clustering.model;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public final class Vector implements WritableComparable<Vector> {

    private double[] vector;

    public Vector() {
        super();
    }

    public Vector(Vector v) {
        int l = v.vector.length;
        this.vector = new double[l];
        System.arraycopy(v.vector, 0, this.vector, 0, l);
    }

    public Vector(double x, double y) {
        super();
        this.vector = new double[]{x, y};
    }

    public Vector(int cardinality) {
        this.vector = new double[cardinality];
    }

    public Vector(double... vec) {
        this.vector = vec;
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeInt(vector.length);
        for (double aVector : vector) out.writeDouble(aVector);
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        int size = in.readInt();
        vector = new double[size];
        for (int i = 0; i < size; i++)
            vector[i] = in.readDouble();
    }

    @Override
    public final int compareTo(Vector o) {

        boolean equals = true;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != o.vector[i]) {
                equals = false;
                return (int) (vector[i] - o.vector[i]);
            }
        }
        if (equals) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(vector);
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector other = (Vector) obj;
        return Arrays.equals(vector, other.vector);
    }

    public final double[] getVector() {
        return vector;
    }

    public final void setVector(double[] vector) {
        this.vector = vector;
    }

    @Override
    public final String toString() {
        return "Vector [vector=" + Arrays.toString(vector) + "]";
    }

    // some element-wise math stuff

    public final Vector times(Vector vector2) {
        Vector newVector = new Vector(
                Arrays.copyOf(this.vector, this.vector.length));
        for (int i = 0; i < newVector.vector.length; i++) {
            newVector.getVector()[i] = newVector.getVector()[i]
                    * vector2.getVector()[i];
        }
        return newVector;
    }

    public final Vector divide(Vector vector2) {
        Vector newVector = new Vector(
                Arrays.copyOf(this.vector, this.vector.length));
        for (int i = 0; i < newVector.vector.length; i++) {
            newVector.getVector()[i] = newVector.getVector()[i]
                    / vector2.getVector()[i];
        }
        return newVector;
    }

    public final Vector minus(Vector vector2) {
        Vector newVector = new Vector(
                Arrays.copyOf(this.vector, this.vector.length));
        for (int i = 0; i < newVector.vector.length; i++) {
            newVector.getVector()[i] = newVector.getVector()[i]
                    - vector2.getVector()[i];
        }
        return newVector;
    }

    public final Vector add(Vector vector2) {
        Vector newVector = new Vector(
                Arrays.copyOf(this.vector, this.vector.length));
        for (int i = 0; i < newVector.vector.length; i++) {
            newVector.getVector()[i] = newVector.getVector()[i]
                    + vector2.getVector()[i];
        }
        return newVector;
    }

}
