package de.jungblut.reader;

import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleVector;

public class ImageReaderTest extends TestCase {

  private static final String LENNA_PATH = "files/img/lenna.png";

  @Test
  public void testLUVReader() throws Exception {

    DoubleVector[] luvVectors = ImageReader.readImageAsLUV(ImageIO
        .read(new File(LENNA_PATH)));
    assertEquals(512 * 512, luvVectors.length);
    for (int i = 0; i < luvVectors.length; i++) {
      assertEquals(3, luvVectors[i].getLength());
    }

  }

  @Test
  public void testRGBReader() throws Exception {

    DoubleVector[] rgbVectors = ImageReader.readImageAsRGB(ImageIO
        .read(new File(LENNA_PATH)));
    assertEquals(512 * 512, rgbVectors.length);
    for (int i = 0; i < rgbVectors.length; i++) {
      assertEquals(3, rgbVectors[i].getLength());
      assertTrue(rgbVectors[i].get(0) >= 0 && rgbVectors[i].get(0) <= 255);
      assertTrue(rgbVectors[i].get(1) >= 0 && rgbVectors[i].get(1) <= 255);
      assertTrue(rgbVectors[i].get(2) >= 0 && rgbVectors[i].get(2) <= 255);
    }

  }

}
