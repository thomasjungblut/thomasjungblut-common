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
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;

import de.jungblut.nlp.Tokenizer;

/**
 * My simple submission for the kaggle facebook competition. <br/>
 * Basically calculates all missing edges between possible friends and then
 * filters by specific numbers like pagerank and inlink counts to calculate a
 * "probability" for an edge between them.
 * 
 * @author thomas.jungblut
 * 
 */
public class FacebookCompetition {

  private static final int MAXIMUM_FRIENDS_OF_FRIENDS = 500;
  private static final String TRAIN_FILE = "/Users/thomas.jungblut/Downloads/FB/train/train.csv";
  private static final String TEST_FILE = "/Users/thomas.jungblut/Downloads/FB/test/test.csv";
  private static final String TEST_OUT_FILE = "/Users/thomas.jungblut/Downloads/FB/test/test_out.csv";
  private static final String HAMA_GRAPH_IN_FILE = "/Users/thomas.jungblut/Downloads/FB/hama-graph-in/graph.csv";
<<<<<<< HEAD
=======
//  private static final String HAMA_PR_OUT_30_FILE = "/Users/thomas.jungblut/Downloads/FB/pr-out/pr_30.csv";
>>>>>>> 24d598d8df33dbdbb90beff600316d5aac049595
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
<<<<<<< HEAD
        List<String> recEdges = null;
        // start at minus one, because in the first iteration we don't have so
        // much new recommendations usually
        int lastSize = -1;
        for (int i = 2; i < 5; i++) {
          HashSet<String> candidates = new HashSet<>();
          Set<String> missingEdges = allMissingEdges.get(predVertex);
          HashSet<String> friendsOfFriends = getFriendsOfFriends(graph,
              predVertex, i);
          candidates.addAll(missingEdges);
          candidates.addAll(friendsOfFriends);
          recEdges = recommendFriendsInternal(predVertex, candidates,
              missingEdges, graph, allMissingEdges, inlinkCount, pagerank);
          Preconditions.checkArgument(recEdges.size() <= 10,
              "Friendlist was too long: " + recEdges.size());
          // if we have the same count after the last iteration, then skip
          if (recEdges.size() > 7 || lastSize == recEdges.size())
            break;
          lastSize = recEdges.size();
        }
        StringBuilder appender = new StringBuilder();
        appender.append(predVertex);
        appender.append(',');
=======
        HashSet<String> candidates = new HashSet<>();
        Set<String> missingEdges = allMissingEdges.get(predVertex);
        HashSet<String> friendsOfFriends = getFriendsOfFriends(graph,
            predVertex, 3);
        candidates.addAll(missingEdges);
        candidates.addAll(friendsOfFriends);
        List<String> recEdges = recommendFriendsInternal(predVertex,
            candidates, missingEdges, graph, allMissingEdges, inlinkCount,
            pagerank);
        Preconditions.checkArgument(recEdges.size() <= 10,
            "Friendlist was too long: " + recEdges.size());
        String append = "";
>>>>>>> 24d598d8df33dbdbb90beff600316d5aac049595
        for (String s : recEdges) {
          appender.append(s);
          appender.append(' ');
        }
        appender.append('\n');
        sb.append(appender.toString());
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
    // final int inlinkCount;
    // final int outlinkCount;
    // final double rank;
    final int commonFriends;
    final double weight;
    final boolean strongRecommendation;

    public InlinkDTO(String name, int inlinkCount, int outlinkCount,
        double rank, int commonFriends, boolean strongRecommendation) {
      super();
      this.name = name;
      this.commonFriends = commonFriends;
      this.strongRecommendation = strongRecommendation;
<<<<<<< HEAD
      this.weight = (inlinkCount / (outlinkCount + 1)) * 0.3 + rank * 0.7;
    }

    @Override
    public String toString() {
      return "InlinkDTO [name=" + name + ", commonFriends=" + commonFriends
          + ", weight=" + weight + ", strongRecommendation="
          + strongRecommendation + "]";
    }

    @Override
=======
      weight = (inlinkCount / (outlinkCount + 1)) * 0.3 + rank * 0.7;
    }

    @Override
    public String toString() {
      return "InlinkDTO [name=" + name + ", inlinkCount=" + inlinkCount
          + ", outlinkCount=" + outlinkCount + ", rank=" + rank
          + ", commonFriends=" + commonFriends + ", weight=" + weight
          + ", strongRecommendation=" + strongRecommendation + "]";
    }

    @Override
>>>>>>> 24d598d8df33dbdbb90beff600316d5aac049595
    public int compareTo(InlinkDTO o) {
      return ComparisonChain.start()
          .compare(o.strongRecommendation, strongRecommendation)
          .compare(o.commonFriends, commonFriends).compare(o.weight, weight)
          .result();
    }

  }

  /**
   * Internal logic for friend recommendation.
   */
  private List<String> recommendFriendsInternal(String vertex,
      Set<String> candidates, Set<String> missingEdges,
      HashMultimap<String, String> graph,
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
        boolean strongRecommendation = false;
        if (missingEdges.contains(s)) {
          strongRecommendation = true;
        }
        queue.add(new InlinkDTO(s, count, graph.get(s).size(), rank,
            commonFriends, strongRecommendation));
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

  public static HashSet<String> getFriendsOfFriends(
      HashMultimap<String, String> graph, String start, int degree) {
    HashSet<String> set = new HashSet<>();
    List<String> nextDegree = new LinkedList<>();
    nextDegree.add(start);
    for (int i = 0; i < degree; i++) {
      List<String> tmp = new LinkedList<>();
      for (String vertex : nextDegree) {
        set.add(vertex);
        if (set.size() > MAXIMUM_FRIENDS_OF_FRIENDS)
          break;
        Set<String> edges = graph.get(vertex);
        for (String s : edges) {
          tmp.add(s);
        }
        if (set.size() > MAXIMUM_FRIENDS_OF_FRIENDS)
          break;
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

  @SuppressWarnings("unused")
  private static void printDebugGraph(HashMultimap<String, String> graph,
      String node) {
    HashSet<String> friendsOfFriends = getFriendsOfFriends(graph, node, 3);
    for (String s : friendsOfFriends) {
      Set<String> set = graph.get(s);
      System.out.println(s + "\t"
          + Tokenizer.concat(set.toArray(new String[set.size()]), "\t"));
    }
    Set<String> set = graph.get(node);
    System.out.println(node + "\t"
        + Tokenizer.concat(set.toArray(new String[set.size()]), "\t"));
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
