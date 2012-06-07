package de.jungblut.kaggle;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;

import de.jungblut.nlp.Tokenizer;

/**
 * My simple submission for the kaggle facebook competition. <br/>
 * Basically calculates all missing edges between possible friends and then
 * filters by specific numbers like pagerank and inlink counts to calculate a
 * probability for an edge between them.
 * 
 * @author thomas.jungblut
 * 
 */
public class FacebookCompetition {

  private static final String TRAIN_FILE = "/Users/thomas.jungblut/Downloads/FB/train/train.csv";
  private static final String TEST_FILE = "/Users/thomas.jungblut/Downloads/FB/test/test.csv";
  private static final String TEST_OUT_FILE = "/Users/thomas.jungblut/Downloads/FB/test/test_out.csv";
  private static final String HAMA_GRAPH_IN_FILE = "/Users/thomas.jungblut/Downloads/FB/hama-graph-in/graph.csv";
  private static final String HAMA_PR_OUT_30_FILE = "/Users/thomas.jungblut/Downloads/FB/pr-out/pr_30.csv";
  private static final String HAMA_PR_OUT_85_FILE = "/Users/thomas.jungblut/Downloads/FB/pr-out/pr_85.csv";
  private static final String HAMA_INLNK_OUT_FILE = "/Users/thomas.jungblut/Downloads/FB/inlnk-out/inlink.csv";

  /**
   * Reads the training file into RAM, skips the first line and calculates the
   * graph.
   */
  private static HashMultimap<String, String> readTrainFile() {
    HashMultimap<String, String> map = HashMultimap.create();
    try {
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(TRAIN_FILE), Charset.defaultCharset());
      Iterator<String> iterator = readAllLines.iterator();
      Iterators.skip(iterator, 1);
      while (iterator.hasNext()) {
        String[] split = iterator.next().split(",");
        map.put(split[0], split[1]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return map;
  }

  /**
   * Calculates the missing edges in the facebook graph for each vertex.
   */
  public HashMultimap<String, String> getAllMissingEdges(
      final HashMultimap<String, String> graph) {
    HashMultimap<String, String> missingEdges = HashMultimap.create();
    for (String vertex : graph.keySet()) {
      final Set<String> set = graph.get(vertex);
      for (String outEdge : set) {
        if (!graph.get(outEdge).contains(vertex)) {
          missingEdges.put(outEdge, vertex);
        }
      }
    }
    return missingEdges;
  }

  /**
   * Recommends up to ten friends for each vertex in the given testfiles.
   * 
   * @param pagerank
   */
  public void recommendFriends(HashMultimap<String, String> graph,
      HashMultimap<String, String> allMissingEdges, TIntIntHashMap inlinkCount,
      TIntDoubleHashMap pagerank) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("source_node,destination_nodes\n");
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(TEST_FILE), Charset.defaultCharset());
      Iterator<String> iterator = readAllLines.iterator();
      final int max = readAllLines.size() - 1;
      int count = 1;
      Iterators.skip(iterator, 1);
      while (iterator.hasNext()) {
        String predVertex = iterator.next();
        Set<String> set = allMissingEdges.get(predVertex);
        // add friends of friends of friends to this set
        set.addAll(getFriendsOfFriends(graph, predVertex, 3));
        List<String> recEdges = recommendFriendsInternal(predVertex, set,
            graph, allMissingEdges, inlinkCount, pagerank);
        Preconditions.checkArgument(recEdges.size() <= 10,
            "Friendlist was too long: " + recEdges.size());
        String append = "";
        for (String s : recEdges) {
          append += s + " ";
        }
        sb.append(predVertex + "," + append + "\n");
        System.out.println("Progress: " + (count++) + "/" + max);
      }
      Files.write(FileSystems.getDefault().getPath(TEST_OUT_FILE), sb
          .toString().getBytes(), StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class InlinkDTO implements Comparable<InlinkDTO> {

    final String name;
    final int inlinkCount;
    final int outlinkCount;
    final double rank;
    final int commonFriends;
    final double weight;

    public InlinkDTO(String name, int inlinkCount, int outlinkCount,
        double rank, int commonFriends) {
      super();
      this.name = name;
      this.inlinkCount = inlinkCount;
      this.outlinkCount = outlinkCount;
      this.rank = rank;
      this.commonFriends = commonFriends;
      weight = (inlinkCount / (outlinkCount + 1)) * 0.3 + rank * 0.7;
    }

    // TODO make the comparator of DOOM! that takes all three metrices into
    // account!
    @Override
    public int compareTo(InlinkDTO o) {
      int common = Integer.compare(o.commonFriends, commonFriends);
      // if there is no distinction, we use a weighted mixture of pagerank and
      // inlinkCount/outlinkcount
      if (common == 0) {
        return Double.compare(o.weight, weight);
      }
      return common;
    }

    @Override
    public String toString() {
      return "InlinkDTO [name=" + name + ", inlinkCount=" + inlinkCount
          + ", outlinkCount=" + outlinkCount + ", rank=" + rank
          + ", commonFriends=" + commonFriends + ", weight=" + weight + "]";
    }

  }

  /**
   * Internal logic for friend recommendation.
   */
  private List<String> recommendFriendsInternal(String vertex,
      Set<String> candidates, HashMultimap<String, String> graph,
      HashMultimap<String, String> allMissingEdges, TIntIntHashMap inlinkCount,
      TIntDoubleHashMap pagerank) {
    List<String> out = new LinkedList<>();
    PriorityQueue<InlinkDTO> queue = new PriorityQueue<>();
    Set<String> realEdges = graph.get(vertex);
    for (String s : candidates) {
      if (!realEdges.contains(s)) {
        int id = Integer.parseInt(s);
        int count = inlinkCount.get(id);
        double rank = pagerank.get(id);
        int commonFriends = countCommonFriends(graph, vertex, s);
        queue.add(new InlinkDTO(s, count, graph.get(s).size(), rank,
            commonFriends));
      }
    }

    for (int i = 0; i < 10; i++) {
      InlinkDTO poll = queue.poll();
      if (poll == null)
        break;
      else
        out.add(poll.name);
    }

    return out;
  }

  public HashSet<String> getFriendsOfFriends(
      HashMultimap<String, String> graph, String start, int degree) {
    HashSet<String> set = new HashSet<>();
    List<String> nextDegree = new LinkedList<>();
    nextDegree.add(start);
    for (int i = 0; i < degree; i++) {
      List<String> tmp = new LinkedList<>();
      for (String vertex : nextDegree) {
        set.add(vertex);
        Set<String> edges = graph.get(vertex);
        for (String s : edges) {
          tmp.add(s);
        }
      }
      nextDegree.clear();
      nextDegree.addAll(tmp);
    }

    set.remove(start);
    return set;
  }

  public int countCommonFriends(HashMultimap<String, String> graph,
      String left, String right) {
    Set<String> leftEdges = graph.get(left);
    Set<String> rightEdges = graph.get(right);
    int count = 0;
    for (String e : rightEdges) {
      if (leftEdges.contains(e)) {
        count++;
      }
    }
    return count;
  }

  @SuppressWarnings("unused")
  private static void convertTrainCSVToHamaGraph() {
    HashMultimap<String, String> readTrainFile = readTrainFile();
    final Set<String> set = new TreeSet<>(readTrainFile.keySet());
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(HAMA_GRAPH_IN_FILE));
      for (String s : set) {
        Set<String> set2 = readTrainFile.get(s);
        writer.write(s + "\t"
            + Tokenizer.concat(set2.toArray(new String[set2.size()]), "\t")
            + "\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (writer != null)
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  private static TIntIntHashMap readInlinkCount() {
    TIntIntHashMap map = new TIntIntHashMap();
    try {
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(HAMA_INLNK_OUT_FILE), Charset.defaultCharset());
      for (String line : readAllLines) {
        String[] split = line.split("\t");
        map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return map;
  }

  private static TIntDoubleHashMap readPagerank() {
    TIntDoubleHashMap map = new TIntDoubleHashMap();
    try {
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(HAMA_PR_OUT_85_FILE), Charset.defaultCharset());
      for (String line : readAllLines) {
        String[] split = line.split("\t");
        map.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return map;
  }

  public static void main(String[] args) {
    FacebookCompetition comp = new FacebookCompetition();
    final HashMultimap<String, String> graph = readTrainFile();
    final HashMultimap<String, String> allMissingEdges = comp
        .getAllMissingEdges(graph);
    final TIntIntHashMap inlinkCount = readInlinkCount();
    final TIntDoubleHashMap pagerank = readPagerank();
    comp.recommendFriends(graph, allMissingEdges, inlinkCount, pagerank);
  }

}
