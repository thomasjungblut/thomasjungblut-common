package de.jungblut.nlp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;

/**
 * Vectorizing utility for basic tf-idf and wordcount vectorizing of
 * tokens/strings. It can also build inverted indices and dictionaries.
 * 
 * @author thomas.jungblut
 * 
 */
public final class VectorizerUtils {

  public static final String OUT_OF_VOCABULARY = "@__OOV__@";

  /**
   * Builds a sorted dictionary of tokens from a list of (tokenized) documents.
   * It treats tokens that are contained in at least 90% of all documents as
   * spam, they won't be included in the final dictionary.
   * 
   * This method is compatible to parallel streams.
   * 
   * @param tokenizedDocuments the documents that are already tokenized.
   * @return a sorted String array with tokens in it.
   */
  public static String[] buildDictionary(Stream<String[]> tokenizedDocuments) {
    return buildDictionary(tokenizedDocuments, 0.9f, 0);
  }

  /**
   * Builds a sorted dictionary of tokens from a list of (tokenized) documents.
   * It treats tokens that are contained in at least "stopWordPercentage"% of
   * all documents as spam, they won't be included in the final dictionary.
   * 
   * This method is compatible to parallel streams.
   * 
   * @param tokenizedDocuments the documents that are the base for the
   *          dictionary.
   * @param stopWordPercentage the percentage of how many documents must contain
   *          a token until it can be classified as spam. Ranges between 0f and
   *          1f, where 0f will actually return an empty dictionary.
   * @param minFrequency the minimum frequency a token must occur globally.
   *          (strict greater than supplied value)
   * @return a sorted String array with tokens in it.
   */
  public static String[] buildDictionary(Stream<String[]> tokenizedDocuments,
      float stopWordPercentage, int minFrequency) {
    Preconditions.checkArgument(stopWordPercentage >= 0f
        && stopWordPercentage <= 1f,
        "The provided stop word percentage is not between 0 and 1: "
            + stopWordPercentage);

    final ConcurrentHashMultiset<String> set = ConcurrentHashMultiset.create();

    AtomicLong numDocs = new AtomicLong();
    tokenizedDocuments.forEach((doc) -> {
      numDocs.incrementAndGet();
      // deduplication, because we want to measure how often a token is in a
      // doc, so we have to get distinct tokens in a document.
        set.addAll(ArrayUtils.deduplicate(doc));
      });

    final int threshold = (int) (stopWordPercentage * numDocs.get());
    Set<String> toRemove = new HashSet<>();
    // now remove the spam
    for (Entry<String> entry : set.entrySet()) {
      if (entry.getCount() > threshold || entry.getCount() < minFrequency) {
        toRemove.add(entry.getElement());
      }
    }
    // the end tag always has a good chance to be removed as it appears in 100%
    // of the data, thus we will always remove it from the remove set
    toRemove.remove(TokenizerUtils.START_TAG);
    toRemove.remove(TokenizerUtils.END_TAG);

    Set<String> elementSet = set.elementSet();
    // note that a call to removeAll will traverse the whole toRemove list for
    // every item in the set, which is slow + not what you usually expect from
    // such a method. Thus we will manually remove.
    for (String removal : toRemove) {
      elementSet.remove(removal);
    }
    
    set.add(OUT_OF_VOCABULARY); // add the out of vocabulary item

    String[] array = elementSet.toArray(new String[elementSet.size()]);
    elementSet = null;
    Arrays.sort(array);
    return array;
  }

  /**
   * Builds a transition array by traversing the documents and checking the
   * dictionary. If nothing was found in the dictionary, we set the out of
   * vocabulary index. This transition array is ready to be fed into a
   * {@link MarkovChain}.
   * 
   * @param dict the dictionary.
   * @param doc the document to build a transition.
   * @return the transition array.
   */
  public static int[] buildTransitionVector(String[] dict, String[] doc) {
    int[] toReturn = new int[doc.length];
    for (int i = 0; i < doc.length; i++) {
      int idx = Arrays.binarySearch(dict, doc[i]);
      if (idx >= 0) {
        toReturn[i] = idx;
      } else {
        idx = Arrays.binarySearch(dict, VectorizerUtils.OUT_OF_VOCABULARY);
        if (idx >= 0) {
          toReturn[i] = idx;
        }
      }
    }
    return toReturn;
  }

  /**
   * Builds an inverted index as multi map.
   * 
   * @param tokenizedDocuments the documents to index, already tokenized.
   * @param dictionary the dictionary of words that should be used to build this
   *          index.
   * @return a {@link HashMultimap} that contains a set of integers (index of
   *         the documents in the given input list) mapped by a token that was
   *         contained in the documents.
   */
  public static HashMultimap<String, Integer> buildInvertedIndexMap(
      List<String[]> tokenizedDocuments, String[] dictionary) {
    HashMultimap<String, Integer> indexMap = HashMultimap.create();
    for (int i = 0; i < tokenizedDocuments.size(); i++) {
      String[] tokens = tokenizedDocuments.get(i);
      for (String token : tokens) {
        // check if we have the word in our dictionary
        if (Arrays.binarySearch(dictionary, token) >= 0) {
          indexMap.put(token, i);
        }
      }
    }
    return indexMap;
  }

  /**
   * Builds an inverted index based on the given dictionary, adds just the
   * document index mappings to it.
   * 
   * @param tokenizedDocuments the documents to index, already tokenized.
   * @param dictionary the dictionary of words that should be used to build this
   *          index.
   * @return a two dimensional integer array, that contains the document ids
   *         (index in the given document list) on the same index that the
   *         dictionary maps the token.
   */
  public static int[][] buildInvertedIndexArray(
      List<String[]> tokenizedDocuments, String[] dictionary) {
    HashMultimap<String, Integer> invertedIndex = buildInvertedIndexMap(
        tokenizedDocuments, dictionary);
    int[][] docs = new int[dictionary.length][];

    for (int i = 0; i < dictionary.length; i++) {
      Set<Integer> set = invertedIndex.get(dictionary[i]);
      docs[i] = ArrayUtils
          .toPrimitiveArray(set.toArray(new Integer[set.size()]));
    }

    return docs;
  }

  /**
   * Builds an inverted index document count based on the given dictionary, so
   * at each dimension of the returned array, there is a count of how many
   * documents contained that token.
   * 
   * @param tokenizedDocuments the documents to index, already tokenized.
   * @param dictionary the dictionary of words that should be used to build this
   *          index.
   * @return a one dimensional integer array, that contains the number of
   *         documents on the same index that the dictionary maps the token.
   */
  public static int[] buildInvertedIndexDocumentCount(
      List<String[]> tokenizedDocuments, String[] dictionary) {
    HashMultimap<String, Integer> invertedIndex = buildInvertedIndexMap(
        tokenizedDocuments, dictionary);
    int[] docs = new int[dictionary.length];

    for (int i = 0; i < dictionary.length; i++) {
      Set<Integer> set = invertedIndex.get(dictionary[i]);
      docs[i] = set.size();
    }

    return docs;
  }

  /**
   * Vectorizes a given list of documents. Each vector will have the dimension
   * of how many words are in the build dictionary, each word will have its own
   * mapping in the vector. The value at a certain index (determined by the
   * position in the dictionary) will be the frequncy of the word in the
   * document.
   * 
   * @param tokenizedDocuments the array of documents.
   * @return a stream of sparse vectors, representing the documents as vectors
   *         based on word frequency.
   */
  public static Stream<DoubleVector> wordFrequencyVectorize(String[]... vars) {
    return wordFrequencyVectorize(Arrays.stream(vars));
  }

  /**
   * Vectorizes a given list of documents. Each vector will have the dimension
   * of how many words are in the build dictionary, each word will have its own
   * mapping in the vector. The value at a certain index (determined by the
   * position in the dictionary) will be the frequncy of the word in the
   * document.
   * 
   * @param tokenizedDocuments the list of documents.
   * @return a stream of sparse vectors, representing the documents as vectors
   *         based on word frequency.
   */
  public static Stream<DoubleVector> wordFrequencyVectorize(
      Stream<String[]> tokenizedDocuments) {
    return wordFrequencyVectorize(tokenizedDocuments,
        buildDictionary(tokenizedDocuments));
  }

  /**
   * Vectorizes a given list of documents and a dictionary. Each vector will
   * have the dimension of how many words are in the dictionary, each word will
   * have its own mapping in the vector. The value at a certain index
   * (determined by the position in the dictionary) will be the frequncy of the
   * word in the document.
   * 
   * @param tokenizedDocuments the list of documents.
   * @param dictionary the dictionary, must be sorted.
   * @return a stream of sparse vectors, representing the documents as vectors
   *         based on word frequency.
   */
  public static Stream<DoubleVector> wordFrequencyVectorize(
      Stream<String[]> tokenizedDocuments, final String[] dictionary) {

    int oovIndex = Arrays.binarySearch(dictionary, OUT_OF_VOCABULARY);

    return tokenizedDocuments.map(tokens -> {
      DoubleVector vector = new SparseDoubleVector(dictionary.length);
      HashMultiset<String> set = HashMultiset.create(Arrays.asList(tokens));
      for (String s : tokens) {
        int foundIndex = Arrays.binarySearch(dictionary, s);
        // simply ignore tokens we don't know or that are spam
        if (foundIndex >= 0) {
          // the index is equal to its mapped dimension
          vector.set(foundIndex, set.count(s));
        } else if (oovIndex >= 0) {
          vector.set(oovIndex, 1);
        }
      }
      return vector;
    });
  }

  /**
   * Vectorizes the given documents by the TF-IDF weighting.
   * 
   * @param tokenizedDocuments the documents to vectorize.
   * @param dictionary the dictionary extracted.
   * @param termDocumentCount the document count per token. The information can
   *          be retrieved through
   *          {@link #buildInvertedIndexDocumentCount(List, String[])}.
   * @return a list of sparse tf-idf weighted vectors.
   */
  public static List<DoubleVector> tfIdfVectorize(
      List<String[]> tokenizedDocuments, String[] dictionary,
      int[] termDocumentCount) {

    final int numDocuments = tokenizedDocuments.size();
    List<DoubleVector> list = new ArrayList<>(numDocuments);

    for (String[] document : tokenizedDocuments) {
      list.add(tfIdfVectorize(numDocuments, document, dictionary,
          termDocumentCount));
    }

    return list;
  }

  /**
   * Vectorizes the given single document by the TF-IDF weighting.
   * 
   * @param numDocuments the number of documents used in the corpus.
   * @param document the document to vectorize.
   * @param dictionary the dictionary extracted.
   * @param termDocumentCount the document count per token.
   * @return a sparse tf-idf weighted vectors.
   */
  public static DoubleVector tfIdfVectorize(int numDocuments,
      String[] document, String[] dictionary, int[] termDocumentCount) {

    final int numTokens = dictionary.length;
    DoubleVector vector = new SparseDoubleVector(numTokens);
    HashMultiset<String> termFrequencySet = HashMultiset.create(Arrays
        .asList(document));

    int oovIndex = Arrays.binarySearch(dictionary, OUT_OF_VOCABULARY);
    double docLog = FastMath.log(numDocuments);

    for (String token : document) {
      int index = Arrays.binarySearch(dictionary, token);
      if (index >= 0) {
        double tfIdf = termFrequencySet.count(token)
            * (docLog - FastMath.log(termDocumentCount[index]));
        vector.set(index, tfIdf);
      } else {
        if (oovIndex >= 0) {
          vector.set(oovIndex, 1d);
        }
      }
    }
    return vector;
  }

  /**
   * Given a multiset of generic elements we are going to return a list of all
   * the elements, sorted descending by their frequency.
   * 
   * @param set the given multiset.
   * @return a descending sorted list by frequency.
   */
  public static <E> ArrayList<Entry<E>> getMostFrequentItems(Multiset<E> set) {
    return getMostFrequentItems(set, null);
  }

  /**
   * Given a multiset of generic elements we are going to return a list of all
   * the elements, sorted descending by their frequency. Also can apply a filter
   * on the multiset, for example a filter for wordfrequency > 1.
   * 
   * @param set the given multiset.
   * @param filter if not null it filters by the given {@link Predicate}.
   * @return a descending sorted list by frequency.
   */
  public static <E> ArrayList<Entry<E>> getMostFrequentItems(Multiset<E> set,
      Predicate<Entry<E>> filter) {

    ArrayList<Entry<E>> list = Lists.newArrayList(filter == null ? set
        .entrySet() : Iterables.filter(set.entrySet(), filter));
    Collections.sort(list, new Comparator<Entry<E>>() {
      @Override
      public int compare(Entry<E> o1, Entry<E> o2) {
        return Integer.compare(o2.getCount(), o1.getCount());
      }
    });

    return list;
  }

  /**
   * Hashes the given vector into a new representation of a new n-dimensional
   * feature space. The hash beeing is used on the non-zero feature index. Thus
   * this vectorization method should be used for text data, that has a sparse
   * representation of its features.
   * 
   * @param inputFeature the (usually) sparse feature vector.
   * @param n the target dimension of the vector.
   * @param hashFunction the hashfunction to use. For example:
   *          Hashing.murmur3_128().
   * @return the new n-dimensional dense vector vectorized via the hashing
   *         trick.
   */
  public static DoubleVector hashVectorize(DoubleVector inputFeature, int n,
      com.google.common.hash.HashFunction hashFunction) {
    DoubleVector dense = new DenseDoubleVector(n);
    Iterator<DoubleVectorElement> iterateNonZero = inputFeature
        .iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      // get the hash as int
      int hash = hashFunction.hashInt(next.getIndex()).asInt();
      // abs it, as we don't want to have negative index access
      int bucket = Math.abs(hash) % n;
      // subtract 1 in case of negative values, else increment.
      // this replaces the second hash function proposed by Weinberger et al.
      dense.set(bucket, dense.get(bucket) + (hash < 0 ? -1d : 1d));
    }
    return dense;
  }

  /**
   * Hashes the given vectors into a new representation of a new n-dimensional
   * feature space. The hash beeing used is the Murmur128 Bit hashing function
   * on the non-zero feature index. Thus this vectorization method should be
   * used for text data, that has a sparse representation of its features.
   * 
   * @param seedVector the (usually) sparse feature vector.
   * @param n the target dimension of the vector.
   * @param hashFunction the hashfunction to use. For example:
   *          Hashing.murmur3_128().
   * @return the new n-dimensional dense vectors vectorized via the hashing
   *         trick.
   */
  public static DoubleVector[] hashVectorize(DoubleVector[] features, int n,
      com.google.common.hash.HashFunction hashFunction) {
    DoubleVector[] lst = new DoubleVector[features.length];
    for (int i = 0; i < features.length; i++) {
      lst[i] = hashVectorize(features[i], n, hashFunction);
    }
    return lst;
  }

  /**
   * Uses the hashing trick to provide a sparse numeric representation of the
   * given input. This is different from
   * {@link #hashVectorize(DoubleVector, int, com.google.common.hash.HashFunction)}
   * , as it takes raw tokenized documents directly and only using their hash
   * values to find the respective index in the newly created vector.
   * 
   * @param documents the tokenized documents.
   * @param hashFunction the hasher. This will be ignored when a parallel stream
   *          is passed, in this case it will use the {@link String#hashCode()},
   *          as it is thread-safe.
   * @param factory to create a new vector of size x
   * @return a stream of DoubleVectors
   */
  public static Stream<DoubleVector> sparseHashVectorize(
      Stream<String[]> documents,
      com.google.common.hash.HashFunction hashFunction,
      Supplier<DoubleVector> vectorFactory) {
    return documents.map((doc) -> sparseHashVectorize(doc,
        documents.isParallel() ? null : hashFunction, vectorFactory));
  }

  /**
   * Uses the hashing trick to provide a sparse numeric representation of the
   * given input. This is different from
   * {@link #hashVectorize(DoubleVector, int, com.google.common.hash.HashFunction)}
   * , as it takes raw tokenized documents directly and only using their hash
   * values to find the respective index in the newly created vector.
   * 
   * @param documents the tokenized documents.
   * @param hashFunction the hasher. If null it will use the Java hashcode for
   *          strings.
   * @param factory to create a new vector of size x
   * @return a DoubleVector
   */
  public static DoubleVector sparseHashVectorize(String[] doc,
      com.google.common.hash.HashFunction hashFunction,
      Supplier<DoubleVector> vectorFactory) {
    DoubleVector vec = vectorFactory.get();
    for (int i = 0; i < doc.length; i++) {
      int hash = 0;
      if (hashFunction == null) {
        hash = doc[i].hashCode();
      } else {
        hash = hashFunction.hashString(doc[i], Charset.defaultCharset())
            .asInt();
      }
      int idx = FastMath.abs(hash % vec.getDimension());
      vec.set(idx, vec.get(idx) + 1d);
    }
    return vec;
  }
}
