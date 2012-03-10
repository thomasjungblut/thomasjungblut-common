package de.jungblut.recommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;

public class MovieLensReader {

  public static DoubleMatrix getUserMovieRatings() {

    // - UserIDs range between 1 and 6040 = columns
    // - MovieIDs range between 1 and 3952 = rows
    // leaving zero column/row blank for a test rating
    DoubleMatrix matrix = new SparseDoubleColumnMatrix(3952 + 1, 6040 + 1);
    try (BufferedReader br = new BufferedReader(new FileReader(
        "files/ml-1m/ratings.dat"))) {
      String line;
      while ((line = br.readLine()) != null) {
        // UserID::MovieID::Rating::Timestamp
        String[] split = line.split("::");
        matrix.set(Integer.parseInt(split[1]), Integer.parseInt(split[0]),
            Double.parseDouble(split[2]));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return matrix;
  }

  public static HashMap<Integer, String> getMovieLookupTable() {
    HashMap<Integer, String> map = new HashMap<Integer, String>();
    try (BufferedReader br = new BufferedReader(new FileReader(
        "files/ml-1m/movies.dat"))) {
      String line;
      while ((line = br.readLine()) != null) {
        // MovieID::Title::Genres
        String[] split = line.split("::");
        map.put(Integer.parseInt(split[0]), split[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }

}
