package de.jungblut.nlp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.JaccardDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.reader.TwentyNewsgroupReader;

/**
 * A near duplicate finder for any kind of text, uses min hashing and the
 * jaccard distance to measure the equality of texts fast.
 * 
 * @author thomas.jungblut
 * 
 */
public class NearDuplicateFinder {

  public static void main(String[] args) throws Exception {

    Tuple3<List<String[]>, DenseIntVector, String[]> twentyNewsgroups = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File(
            "files/20news-bydate/20news-bydate-train/"));

    List<DoubleVector> wordFrequencyVectorized = VectorizerUtils
        .tfIdfVectorize(twentyNewsgroups.getFirst());

    List<int[]> minHashes = new ArrayList<>();
    MinHash minHash = MinHash.create(10);
    for (DoubleVector v : wordFrequencyVectorized) {
      int[] minHashed = minHash.minHashVector(v);
      minHashes.add(minHashed);
    }

    clusterUsingDistances(twentyNewsgroups.getFirst(), wordFrequencyVectorized,
        minHashes, minHash);
  }

  private static void clusterUsingDistances(List<String[]> titleLookup,
      List<DoubleVector> wordFrequencyVectorized, List<int[]> minHashes,
      MinHash minHash) {
    DistanceMeasurer measurer = new JaccardDistance();
    HashSet<Integer> set = new HashSet<>();
    for (int i = 0; i < minHashes.size(); i++) {
      if (set.add(i)) {
        List<String> adjacentDocuments = new ArrayList<>();
        for (int j = 0; j < minHashes.size(); j++) {
          if (j != i) {
            double sim = minHash.measureSimilarity(minHashes.get(i),
                minHashes.get(j));
            if (sim == 1.0) {
              double sim2 = measurer.measureDistance(
                  wordFrequencyVectorized.get(i),
                  wordFrequencyVectorized.get(j));
              if (sim2 < 0.1d) {
                adjacentDocuments.add(buildSummary(titleLookup.get(j)));
                set.add(j);
              }
            }
          }
        }

        if (adjacentDocuments.size() > 1) {
          System.out.println(buildSummary(titleLookup.get(i)));
          for (String s : adjacentDocuments) {
            if (!s.isEmpty())
              System.out.println("\t" + s);
          }
        }
      }
    }

  }

  private static String buildSummary(String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < (tokens.length > 5 ? 5 : 0); i++) {
      sb.append(tokens[i]);
      sb.append(' ');
    }
    return sb.toString();

  }

}
