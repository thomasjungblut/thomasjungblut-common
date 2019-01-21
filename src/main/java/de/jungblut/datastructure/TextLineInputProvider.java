package de.jungblut.datastructure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.AbstractIterator;

/**
 * Line reader for plain text that contains data in lines.
 * 
 * @author thomas.jungblut
 * 
 */
public class TextLineInputProvider extends InputProvider<String> {

  private Path path;

  public TextLineInputProvider(String path) {
    this.path = Paths.get(path);
  }

  public TextLineInputProvider(URI path) {
    this.path = Paths.get(path);
  }

  public TextLineInputProvider(Path path) {
    this.path = path;
  }

  @SuppressWarnings("resource")
  @Override
  public Iterable<String> iterate() {
    try {
      final BufferedReader br = new BufferedReader(
          new FileReader(path.toFile()));
      return Iterables.from(new AbstractIterator<String>() {
        @Override
        protected String computeNext() {
          try {
            String line = br.readLine();
            if (line != null) {
              return line;
            }
            br.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

          return endOfData();
        }
      });
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return null;
  }
}
