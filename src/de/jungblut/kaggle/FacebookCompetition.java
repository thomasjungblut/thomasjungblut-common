package de.jungblut.kaggle;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
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
  private static final String HAMA_PR_OUT_FILE = "/Users/thomas.jungblut/Downloads/FB/pr-out/pr.csv";
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
      HashMultimap<String, String> allMissingEdges, TIntIntHashMap inlinkCount) {
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
        List<String> recEdges = recommendFriendsInternal(set, graph,
            allMissingEdges, inlinkCount);
        Preconditions.checkArgument(recEdges.size() <= 10,
            "Friendlist was too long: " + recEdges.size());
        String append = "";
        for (String s : recEdges) {
          append += s + " ";
        }
        sb.append(predVertex + "," + append + "\n");
        if (count % 1000 == 0)
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

    public InlinkDTO(String name, int inlinkCount) {
      super();
      this.name = name;
      this.inlinkCount = inlinkCount;
    }

    @Override
    public int compareTo(InlinkDTO o) {
      // return Integer.compare(o.inlinkCount, inlinkCount);
      return Integer.compare(inlinkCount, o.inlinkCount);
    }

  }

  /**
   * Internal logic for friend recommendation.<br/>
   * ->Recommends up to ten friends with lowest inlink numbers (we ignore 0 and
   * 1 since this are spammers?)
   */
  private static List<String> recommendFriendsInternal(Set<String> set,
      HashMultimap<String, String> graph,
      HashMultimap<String, String> allMissingEdges, TIntIntHashMap inlinkCount) {
    List<String> out = new LinkedList<>();
    PriorityQueue<InlinkDTO> queue = new PriorityQueue<>();
    for (String s : set) {
      int count = inlinkCount.get(Integer.parseInt(s));
      if (count > 1) {
        queue.add(new InlinkDTO(s, count));
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

  @SuppressWarnings("unused")
  private static void convertTrainCSVToHamaGraph() {
    HashMultimap<String, String> readTrainFile = readTrainFile();
    final Set<String> set = new TreeSet<String>(readTrainFile.keySet());
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

  private TIntIntHashMap readInlinkCount() {
    TIntIntHashMap map = new TIntIntHashMap();
    HashMultiset<Integer> distribution = HashMultiset.create();
    try {
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(HAMA_INLNK_OUT_FILE), Charset.defaultCharset());
      for (String line : readAllLines) {
        String[] split = line.split("\t");
        map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        distribution.add(Integer.parseInt(split[1]));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(HAMA_INLNK_OUT_FILE
          + "_dist.csv"));
      for (int i : distribution.elementSet()) {
        writer.write(i + "\t" + distribution.count(i) + "\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return map;
  }

  public static void main(String[] args) {
    FacebookCompetition comp = new FacebookCompetition();
    final HashMultimap<String, String> graph = readTrainFile();
    final HashMultimap<String, String> allMissingEdges = comp
        .getAllMissingEdges(graph);
    final TIntIntHashMap inlinkCount = comp.readInlinkCount();
    comp.recommendFriends(graph, allMissingEdges, inlinkCount);
  }

}
