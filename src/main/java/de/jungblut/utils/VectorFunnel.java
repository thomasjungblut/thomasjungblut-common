package de.jungblut.utils;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

import java.util.Iterator;

/**
 * A funnel that funnels a DoubleVector into bytes by taking the non-zero items
 * from a vector for sparse instances. For dense instances it completely puts
 * the vector into the byte representation (including zeros).
 *
 * @author thomas.jungblut
 */
public final class VectorFunnel implements Funnel<DoubleVector> {

    private static final long serialVersionUID = -7994812949854877907L;

    @Override
    public void funnel(DoubleVector from, PrimitiveSink into) {
        if (from.isSparse()) {
            Iterator<DoubleVectorElement> iterator = from.iterateNonZero();
            while (iterator.hasNext()) {
                DoubleVectorElement next = iterator.next();
                into.putInt(next.getIndex());
                into.putDouble(next.getValue());
            }
        } else {
            final int dimension = from.getDimension();
            // traverse backwards, because sparse vectors are iterated descending, so
            // we get the same funneling for same vectors of different types.
            for (int i = dimension - 1; i >= 0; i--) {
                into.putInt(i);
                into.putDouble(from.get(i));
            }
        }
    }

}
