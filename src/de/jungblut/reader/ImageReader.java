package de.jungblut.reader;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
   * Calculates subimage windows with the sliding window algorithm.
   * 
   * @param source the source image to chunk.
   * @param windowWidth the window width.
   * @param windowHeight the window height.
   * @param verticalStride the vertical stride between image offsets.
   * @param horizontalStride the horizontal stride between image offsets.
   * @return a list of subimages containing the windows, internally pointing to
   *         the source image.
   */
  public static List<BufferedImage> getSlidingWindowPatches(
      BufferedImage source, int windowWidth, int windowHeight,
      int verticalStride, int horizontalStride) {
    List<BufferedImage> list = new ArrayList<>();

    for (int height = 0; height < (source.getHeight() - windowHeight); height += verticalStride) {
      for (int width = 0; width < (source.getWidth() - windowWidth); width += horizontalStride) {
        BufferedImage subimage = source.getSubimage(width, height, windowWidth,
            windowHeight);
        list.add(subimage);
      }
    }

    return list;
  }

  /**
   * Returns the given image as a vector, where each dimension is mapped to a
   * given pixel in the image. The value of each dimension is the greyscaled
   * value that was calculated by averaging the RGB information at each pixel
   * and thus guaranteed to be between 0 and 255.
   * 
   * @param img a buffered image.
   * @return height*width dimensional vector.
   */
  public static DoubleVector readImageAsGreyScale(BufferedImage img) {
    final int h = img.getHeight();
    final int w = img.getWidth();

    DoubleVector vector = new DenseDoubleVector(h * w);
    int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
    for (int i = 0; i < rgb.length; i++) {
      int red = (rgb[i] >> 16) & 0xFF;
      int green = (rgb[i] >> 8) & 0xFF;
      int blue = (rgb[i] >> 0) & 0xFF;
      vector.set(i, (red + green + blue) / 3d);
    }
    return vector;
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
