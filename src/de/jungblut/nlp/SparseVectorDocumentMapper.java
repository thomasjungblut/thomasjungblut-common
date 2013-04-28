package de.jungblut.nlp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.jungblut.datastructure.InvertedIndex;
import de.jungblut.datastructure.InvertedIndex.DocumentMapper;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

/**
 * Mapper that maps sparse vectors into a set of their indices so they can be
 * used in the {@link InvertedIndex} for fast lookup.
 * 
 * @author thomas.jungblut
 * 
 */
public final class SparseVectorDocumentMapper implements
    DocumentMapper<DoubleVector, Integer> {

  @Override
  public Set<Integer> mapDocument(DoubleVector v) {
    Set<Integer> set = new HashSet<>(v.getLength());
    Iterator<DoubleVectorElement> iterateNonZero = v.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      set.add(next.getIndex());
    }
    return set;
  }

}
