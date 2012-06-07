package de.jungblut.kaggle;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;

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

  private static final String TRAIN_FILE = "/Users/thomas.jungblut/Downloads/train.csv";
  private static final String TEST_FILE = "/Users/thomas.jungblut/Downloads/test/test.csv";
  private static final String TEST_OUT_FILE = "/Users/thomas.jungblut/Downloads/test/test_out.csv";

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
      HashMultimap<String, String> allMissingEdges) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("source_node,destination_nodes\n");
      List<String> readAllLines = Files.readAllLines(FileSystems.getDefault()
          .getPath(TEST_FILE), Charset.defaultCharset());
      Iterator<String> iterator = readAllLines.iterator();
      Iterators.skip(iterator, 1);
      while (iterator.hasNext()) {
        String predVertex = iterator.next();
        Set<String> set = allMissingEdges.get(predVertex);
        List<String> recEdges = recommendFriendsInternal(set, graph,
            allMissingEdges);
        Preconditions.checkArgument(recEdges.size() <= 10,
            "Friendlist was too long: " + recEdges.size());
        String append = "";
        for (String s : recEdges) {
          append += s + " ";
        }
        sb.append(predVertex + "," + append + "\n");
      }
      Files.write(FileSystems.getDefault().getPath(TEST_OUT_FILE), sb
          .toString().getBytes(), StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Internal logic for friend recommendation.
   */
  private List<String> recommendFriendsInternal(Set<String> set,
      HashMultimap<String, String> graph,
      HashMultimap<String, String> allMissingEdges) {
    List<String> out = new LinkedList<>();
    for (String s : set) {
      out.add(s);
      if (out.size() == 10)
        break;
    }
    return out;
  }

  public static void main(String[] args) {
    FacebookCompetition comp = new FacebookCompetition();
    final HashMultimap<String, String> graph = readTrainFile();
    final HashMultimap<String, String> allMissingEdges = comp
        .getAllMissingEdges(graph);
    comp.recommendFriends(graph, allMissingEdges);
  }

}
