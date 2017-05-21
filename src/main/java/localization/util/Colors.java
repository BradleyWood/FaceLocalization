package localization.util;

/**
 * Created by Brad on 5/19/2017.
 */
public class Colors {

    /**
     * Cb component in YCbCr color space
     *
     * @param red Red in RGB color space
     * @param green Green in RGB color space
     * @param blue Blue in RGB color space
     * @return The cb component
     */
    public static double cB(final int red, final int green, final int blue) {
        return -0.169 * red - 0.332 * green + 0.500 * blue;
    }

    /**
     * The Cr component in YCbCr color space
     *
     * @param red Red in RGB color space
     * @param green Green in RGB color space
     * @param blue Blue in RGB color space
     * @return The Cr component
     */
    public static double cR(final int red, final int green, final int blue) {
        return + 0.500 * red - 0.419 * green - 0.081 * blue;
    }
    public static int getAlpha(final int rgb) {
        return (rgb >> 24) & 0xFF;
    }
    public static int getRed(final int rgb) {
        return (rgb >> 16) & 0xFF;
    }
    public static int getGreen(final int rgb) {
        return (rgb >> 8) & 0xFF;
    }
    public static int getBlue(final int rgb) {
        return (rgb) & 0xFF;
    }
}
