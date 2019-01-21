package de.jungblut.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;

import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.VectorDocumentDistanceMeasurer;
import de.jungblut.jrpt.LimitedPriorityQueue;
import de.jungblut.math.DoubleVector;
import de.jungblut.nlp.SparseVectorDocumentMapper;

/**
 * Inverted Index, mainly developed for sparse vectors to speedup dimension
 * lookups for fast distance measurement and search space reduction. But of
 * course it can also be used to behave like a fulltext index to find relevant
 * documents by their textual representation.
 * 
 * @author thomas.jungblut
 * 
 * @param <DOCUMENT_TYPE> the type of document one wants to retrieve.
 * @param <KEY_TYPE> the type of key that is going to be extracted out of
 *          documents and is searchable (needs hashCode&equals implementations).
 */
public final class InvertedIndex<DOCUMENT_TYPE, KEY_TYPE> {

  private final HashMultimap<KEY_TYPE, Integer> index = HashMultimap.create();
  private final DocumentMapper<DOCUMENT_TYPE, KEY_TYPE> docMapper;
  private final DocumentDistanceMeasurer<DOCUMENT_TYPE, KEY_TYPE> docMeasurer;

  private List<DOCUMENT_TYPE> documents;
  private List<Set<KEY_TYPE>> keys;

  /**
   * @param mapper the mapper that transforms each document to a look-up-able
   *          key set that can be searched on.
   */
  private InvertedIndex(DocumentMapper<DOCUMENT_TYPE, KEY_TYPE> mapper,
      DocumentDistanceMeasurer<DOCUMENT_TYPE, KEY_TYPE> measurer) {
    this.docMapper = mapper;
    this.docMeasurer = measurer;
  }

  /**
   * Builds this inverted index.
   * 
   * @param items the items that needs to be indexed.
   */
  public void build(List<DOCUMENT_TYPE> items) {
    Preconditions.checkNotNull(items, "Documents should not be NULL!");
    Preconditions.checkArgument(!items.isEmpty(),
        "Documents should contain at least a single item!");

    // do a defensive read-only random access copy of the documents
    this.documents = Collections.unmodifiableList(new ArrayList<>(items));
    this.keys = new ArrayList<>(items.size());
    for (int i = 0; i < documents.size(); i++) {
      DOCUMENT_TYPE doc = documents.get(i);
      Set<KEY_TYPE> keySet = docMapper.mapDocument(doc);
      this.keys.add(keySet);
      // for each key part, index the document as an index
      for (KEY_TYPE key : keySet) {
        index.put(key, i);
      }
    }
  }

  /**
   * Queries this invertex index. This is not bounding the result, so you'll get
   * all items.
   * 
   * @param document the document to query with
   * @return an array of results descending sorted, so the best matching item
   *         resides on the first index.
   */
  public List<DistanceResult<DOCUMENT_TYPE>> query(DOCUMENT_TYPE document) {
    return query(document, Integer.MAX_VALUE, Double.MAX_VALUE);
  }

  /**
   * Queries this invertex index. This is not bounding the result, so you'll get
   * all items that have at least minDistance.
   * 
   * @param document the document to query with
   * @param minDistance the minimum (lower than: <=) distance the items should
   *          have.
   * @return an array of results descending sorted, so the best matching item
   *         resides on the first index.
   */
  public List<DistanceResult<DOCUMENT_TYPE>> query(DOCUMENT_TYPE document,
      double minDistance) {
    return query(document, Integer.MAX_VALUE, minDistance);
  }

  /**
   * Queries this inverted index.
   * 
   * @param document the document to query with-
   * @param maxResults the maximum number of results to obtain.
   * @param minDistance the minimum (lower than: <=) distance the items should
   *          have.
   * @return an array list of results descending sorted, so the best matching
   *         item resides on the first index.
   */
  public List<DistanceResult<DOCUMENT_TYPE>> query(DOCUMENT_TYPE document,
      int maxResults, double minDistance) {
    // basic sanity checks
    Preconditions.checkNotNull(document, "Document should not be NULL!");
    Preconditions.checkArgument(maxResults > 0,
        "Maximum number of results must be positive and greater than zero! Given: "
            + maxResults);
    Preconditions
        .checkArgument(
            minDistance >= 0 && minDistance <= Double.MAX_VALUE,
            "Minimum Distance must be between 0d and Double.MAX_VALUE (both inclusive). Given: "
                + minDistance);

    Set<KEY_TYPE> keys = docMapper.mapDocument(document);
    Set<Integer> allSet = new HashSet<>();
    // retrieve all sets
    for (KEY_TYPE key : keys) {
      Set<Integer> set = index.get(key);
      if (set != null && !set.isEmpty()) {
        allSet.addAll(set);
      }
    }
    LimitedPriorityQueue<DistanceResult<DOCUMENT_TYPE>> queue = new LimitedPriorityQueue<>(
        maxResults);
    // now measure distances and apply the filters
    for (Integer docIndex : allSet) {
      DOCUMENT_TYPE candidateDoc = documents.get(docIndex);
      Set<KEY_TYPE> candidateKeys = this.keys.get(docIndex);
      double distance = docMeasurer.measure(document, keys, candidateDoc,
          candidateKeys);
      if (distance <= minDistance) {
        queue.add(new DistanceResult<>(distance, candidateDoc), distance);
      }
    }

    List<DistanceResult<DOCUMENT_TYPE>> res = new ArrayList<>(queue.size());
    // the prio queue polls from worst matching to best
    DistanceResult<DOCUMENT_TYPE> distRes = null;
    while ((distRes = queue.poll()) != null) {
      res.add(distRes);
    }
    // so we need to reverse the list afterwards
    Collections.reverse(res);

    return res;
  }

  /**
   * Measurer that measures distance of two documents.
   * 
   * @param <DOCUMENT_TYPE> the type of the documents to index.
   * @param <KEY_TYPE> the look-up-able part of the document.
   */
  public static interface DocumentDistanceMeasurer<DOCUMENT_TYPE, KEY_TYPE> {

    /**
     * Measures the distance (value between 0.0 and 1.0) between a reference
     * document and a candidate document.
     * 
     * @param reference the reference document.
     * @param referenceKeys the reference document key parts.
     * @param doc the candidate document.
     * @param docKeys the candidate document key parts.
     * @return a value between 0d and 1d where 0d is most similar.
     */
    public double measure(DOCUMENT_TYPE reference, Set<KEY_TYPE> referenceKeys,
        DOCUMENT_TYPE doc, Set<KEY_TYPE> docKeys);

  }

  /**
   * Mapper that maps a document to its keys.
   * 
   * @param <DOCUMENT_TYPE> the type of the documents to index.
   * @param <KEY_TYPE> the type of the key that will be returned (usually a
   *          smaller abstraction fragment of the document).
   */
  public static interface DocumentMapper<DOCUMENT_TYPE, KEY_TYPE> {

    /**
     * Maps the document into its smaller parts.
     * 
     * @param doc the document to map.
     * @return a set of keys that this document consists of.
     */
    public Set<KEY_TYPE> mapDocument(DOCUMENT_TYPE doc);

  }

  /**
   * Create an inverted index out of two mapping interfaces: a mapper that maps
   * documents to its key parts and a distance measurer that measures distance
   * between two documents.
   * 
   * @param mapper the {@link DocumentMapper}.
   * @param measurer the {@link DocumentDistanceMeasurer}.
   * @return a brand new inverted index.
   */
  public static <KEY_TYPE, DOCUMENT_TYPE> InvertedIndex<DOCUMENT_TYPE, KEY_TYPE> create(
      DocumentMapper<DOCUMENT_TYPE, KEY_TYPE> mapper,
      DocumentDistanceMeasurer<DOCUMENT_TYPE, KEY_TYPE> measurer) {
    return new InvertedIndex<>(mapper, measurer);
  }

  /**
   * Creates an inverted index for vectors (usually sparse vectors are used)
   * that maps dimensions to the corresponding vectors if they are non-zero.
   * 
   * @param measurer the distance measurer on two vectors.
   * @return a brand new inverted index.
   */
  public static InvertedIndex<DoubleVector, Integer> createVectorIndex(
      DistanceMeasurer measurer) {
    DocumentMapper<DoubleVector, Integer> mapper = new SparseVectorDocumentMapper();
    DocumentDistanceMeasurer<DoubleVector, Integer> meas = VectorDocumentDistanceMeasurer
        .<Integer> with(measurer);
    return new InvertedIndex<>(mapper, meas);
  }

}
