package de.jungblut.reader;

import java.awt.color.ColorSpace;

/**
 * Represents the LUV colorspace. Algorithms taken from http://www.easyrgb.com/
 *
 * @author Derek Chen-Becker
 * @author thomas.jungblut (made some corrections to the algorithm)
 */
public final class LUVColorSpace extends ColorSpace {

    private static final long serialVersionUID = 1L;

    public LUVColorSpace() {
        super(ColorSpace.TYPE_Luv, 3);
    }

    // array indices
    private final int L = 0, U = 1, V = 2;
    private final int X = 0, Y = 1, Z = 2;
    private final int R = 0, G = 1, B = 2;

    // Constants
    private float EPSILON = 0.008856f;

    private float ref_X = 95.047f; // Observer= 2°, Illuminant= D65
    private float ref_Y = 100f;
    private float ref_Z = 108.883f;

    private static ColorSpace RGB = ColorSpace
            .getInstance(ColorSpace.CS_LINEAR_RGB);

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        float[] result = new float[3];

        float var_U = (4f * colorvalue[X])
                / (colorvalue[X] + (15f * colorvalue[Y]) + (3f * colorvalue[Z]));
        float var_V = (9f * colorvalue[Y])
                / (colorvalue[X] + (15f * colorvalue[Y]) + (3f * colorvalue[Z]));

        float var_Y = colorvalue[Y] / 100f;
        if (var_Y > EPSILON) {
            var_Y = (float) Math.pow(var_Y, 1f / 3f);
        } else {
            var_Y = (7.787f * var_Y) + (16f / 116f);
        }

        float ref_U = (4f * ref_X) / (ref_X + (15f * ref_Y) + (3f * ref_Z));
        float ref_V = (9f * ref_Y) / (ref_X + (15f * ref_Y) + (3f * ref_Z));

        result[L] = (116f * var_Y) - 16f;
        result[U] = 13f * result[L] * (var_U - ref_U);
        result[V] = 13f * result[L] * (var_V - ref_V);

        return result;
    }

    @Override
    public float[] fromRGB(float[] rgbvalue) {
        return fromCIEXYZ(toCIEXYZ(rgbvalue));
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        float result[] = new float[3];

        float var_R = (colorvalue[R] / 255f); // R from 0 to 255
        float var_G = (colorvalue[G] / 255f); // G from 0 to 255
        float var_B = (colorvalue[B] / 255f); // B from 0 to 255

        if (var_R > 0.04045f) {
            var_R = (float) Math.pow(((var_R + 0.055f) / 1.055f), 2.4);
        } else {
            var_R = var_R / 12.92f;
        }
        if (var_G > 0.04045f) {
            var_G = (float) Math.pow(((var_G + 0.055f) / 1.055f), 2.4);
        } else {
            var_G = var_G / 12.92f;
        }
        if (var_B > 0.04045f) {
            var_B = (float) Math.pow(((var_B + 0.055f) / 1.055f), 2.4);
        } else {
            var_B = var_B / 12.92f;
        }

        var_R = var_R * 100f;
        var_G = var_G * 100f;
        var_B = var_B * 100f;

        // Observer. = 2°, Illuminant = D65
        result[X] = var_R * 0.4124f + var_G * 0.3576f + var_B * 0.1805f;
        result[Y] = var_R * 0.2126f + var_G * 0.7152f + var_B * 0.0722f;
        result[Z] = var_R * 0.0193f + var_G * 0.1192f + var_B * 0.9505f;

        return result;
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        // convert to XYZ first, then to sRGB
        return RGB.fromCIEXYZ(toCIEXYZ(colorvalue));
    }

}
