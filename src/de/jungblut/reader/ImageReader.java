package de.jungblut.reader;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * BufferedImage reader that exports the raw bytes as a feature vectors with
 * different encodings.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ImageReader {

  private ImageReader() {
    throw new IllegalAccessError();
  }

  /**
   * Returns the given image as a list of points in space, where each point is
   * encoded by its RGB values.
   * 
   * @param img a buffered image.
   * @return height*width number of vectors, each vector is 3 dimensional
   *         encoded for the RGB of the image.
   */
  public static DoubleVector[] readImageAsRGB(BufferedImage img) {
    final int h = img.getHeight();
    final int w = img.getWidth();
    // allocate a feature for each point in space
    DoubleVector[] vectors = new DoubleVector[h * w];
    int currentIndex = 0;
    // now get row-major data of the image
    int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
    for (int i = 0; i < rgb.length; i++) {
      double[] rgbValue = new double[3];
      // get the RGB values
      rgbValue[0] = (rgb[i] >> 16) & 0xFF; // RED
      rgbValue[1] = (rgb[i] >> 8) & 0xFF; // GREEN
      rgbValue[2] = (rgb[i] >> 0) & 0xFF; // BLUE
      vectors[currentIndex++] = new DenseDoubleVector(rgbValue);
    }

    return vectors;
  }

  /**
   * Returns the given image as a list of points in space, where each point is
   * encoded by its LUV value. LUV is: L = Luminescence; u = saturation; v = hue
   * angle.
   * 
   * @param img a buffered image.
   * @return height*width number of vectors, each vector is 3 dimensional
   *         encoded for the LUV of the image.
   */
  public static DoubleVector[] readImageAsLUV(BufferedImage img) {
    ColorSpace space = new LUVColorSpace();
    final int h = img.getHeight();
    final int w = img.getWidth();
    // allocate a feature for each point in space
    DoubleVector[] vectors = new DoubleVector[h * w];
    int currentIndex = 0;
    // now get row-major data of the image
    int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
    float[] rgbValue = new float[3];
    for (int i = 0; i < rgb.length; i++) {
      // get the RGB values
      rgbValue[0] = (rgb[i] >> 16) & 0xFF; // RED
      rgbValue[1] = (rgb[i] >> 8) & 0xFF; // GREEN
      rgbValue[2] = (rgb[i] >> 0) & 0xFF; // BLUE
      float[] luv = space.fromRGB(rgbValue);
      double[] arr = new double[luv.length];
      for (int x = 0; x < luv.length; x++) {
        arr[x] = luv[x];
      }
      vectors[currentIndex++] = new DenseDoubleVector(arr);
    }

    return vectors;
  }
}
