package localization.processor;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import static localization.util.Colors.*;
import static localization.util.ImageUtils.*;


/**
 *
 * Processing of Images to Localize faces by skin content
 *
 */
public class DefaultFaceLocalizer extends Localizer {

    public static final int DEFAULT_MIN_CB = -22;
    public static final int DEFAULT_MAX_CB = 8;
    public static final int DEFAULT_MIN_CR = 10;
    public static final int DEFAULT_MAX_CR = 36;
    public static final int DEFAULT_MIN_FACE_AREA = 200;
    public static final float DEFAULT_MIN_HEIGHT_TO_WIDTH_RATIO = 0.95f;
    public static final float DEFAULT_MAX_HEIGHT_TO_WIDTH_RATIO = 2.3f;
    public static final float DEFAULT_MIN_AREA_COEFFICIENT = 0.25f;
    public static final float DEFAULT_MAX_AREA_COEFFICIENT = 4.5f;
    public static final float DEFAULT_MIN_SKIN_TO_AREA_RATIO = 0.3f;

    private static final int WHITE = Color.WHITE.getRGB();
    private static final int BLACK = Color.BLACK.getRGB();

    private int minCb, maxCb, minCr, maxCr, minFaceArea;
    private float minHeightToWidthRatio, maxHeightToWidthRatio, minAreaCoefficient, maxAreaCoefficient, minSkinToAreaRatio;
    private List<Rectangle> faces = new ArrayList<>();
    private BufferedImage prev = null;

    public DefaultFaceLocalizer() {
        this(DEFAULT_MIN_CB, DEFAULT_MAX_CB, DEFAULT_MIN_CR, DEFAULT_MAX_CR, DEFAULT_MIN_FACE_AREA,
                DEFAULT_MIN_HEIGHT_TO_WIDTH_RATIO, DEFAULT_MAX_HEIGHT_TO_WIDTH_RATIO,
                DEFAULT_MIN_AREA_COEFFICIENT, DEFAULT_MAX_AREA_COEFFICIENT, DEFAULT_MIN_SKIN_TO_AREA_RATIO);
    }
    public DefaultFaceLocalizer(int minCb, int maxCb, int minCr, int maxCr, int minFaceArea,
                         float minHeightToWidthRatio, float maxHeightToWidthRatio,
                         float minAreaCoefficient, float maxAreaCoefficient, float minSkinToAreaRatio) {
        this.minCb = minCb;
        this.maxCb = maxCb;
        this.minCr = minCr;
        this.maxCr = maxCr;
        this.minFaceArea = minFaceArea;
        this.minHeightToWidthRatio = minHeightToWidthRatio;
        this.maxHeightToWidthRatio = maxHeightToWidthRatio;
        this.minAreaCoefficient = minAreaCoefficient;
        this.maxAreaCoefficient = maxAreaCoefficient;
        this.minSkinToAreaRatio = minSkinToAreaRatio;
    }

    public int getMinCb() {
        return minCb;
    }
    public void setMinCb(int minCb) {
        this.minCb = minCb;
    }
    public int getMaxCb() {
        return maxCb;
    }
    public void setMaxCb(int maxCb) {
        this.maxCb = maxCb;
    }
    public int getMinCr() {
        return minCr;
    }
    public void setMinCr(int minCr) {
        this.minCr = minCr;
    }
    public int getMaxCr() {
        return maxCr;
    }
    public void setMaxCr(int maxCr) {
        this.maxCr = maxCr;
    }
    public int getMinFaceArea() {
        return minFaceArea;
    }
    public void setMinFaceArea(int minFaceArea) {
        this.minFaceArea = minFaceArea;
    }
    public float getMinHeightToWidthRatio() {
        return minHeightToWidthRatio;
    }
    public void setMinHeightToWidthRatio(float minHeightToWidthRatio) {
        this.minHeightToWidthRatio = minHeightToWidthRatio;
    }
    public float getMaxHeightToWidthRatio() {
        return maxHeightToWidthRatio;
    }
    public void setMaxHeightToWidthRatio(float maxHeightToWidthRatio) {
        this.maxHeightToWidthRatio = maxHeightToWidthRatio;
    }
    public float getMinAreaCoefficient() {
        return minAreaCoefficient;
    }
    public void setMinAreaCoefficient(float minAreaCoefficient) {
        this.minAreaCoefficient = minAreaCoefficient;
    }
    public float getMaxAreaCoefficient() {
        return maxAreaCoefficient;
    }
    public void setMaxAreaCoefficient(float maxAreaCoefficient) {
        this.maxAreaCoefficient = maxAreaCoefficient;
    }
    public float getMinSkinToAreaRatio() {
        return minSkinToAreaRatio;
    }
    public void setMinSkinToAreaRatio(float minSkinToAreaRatio) {
        this.minSkinToAreaRatio = minSkinToAreaRatio;
    }

    /**
     * Determines where faces might be in the image
     *
     * @return The list of bounds for each face
     */
    public List<Rectangle> localize(BufferedImage image) {
        if(image == null || image == prev)
            return faces;
        prev = image;

        int width = image.getWidth();
        int height = image.getHeight();
        int[] buffer = new int[width * height];
        image.getRGB(0,0, width, height, buffer, 0, width);
        int[] data = skinMap(buffer);

        binaryFillHoles(data, width, height, WHITE);
        erodeEdges(buffer, data, width, height, 40);
        boolean[] marked = binaryFillHoles(data, width, height, WHITE);

        faces.clear();
        for(Cluster c : getClusters(data, marked, minFaceArea, width, height)) {
            Rectangle rect = c.getBounds();
            faces.add(rect);
        }
        return faces;
    }
    private int[] skinMap(int[] buffer) {
        return skinMap(buffer, minCb, maxCb, minCr, maxCr);
    }
    private int[] skinMap(int[] buffer, int bMin, int bMax, int rMin, int rMax) {
        int[] data = new int[buffer.length];
        for(int i = 0; i < buffer.length; i++) {
            int red = getRed(buffer[i]);
            int green = getGreen(buffer[i]);
            int blue = getBlue(buffer[i]);
            double cB = cB(red, green, blue);
            double cR = cR(red, green, blue);

            if(cB > bMin && cB < bMax && cR > rMin && cR < rMax) {
                data[i] = WHITE;
            } else {
                data[i] = BLACK;
            }
        }
        return data;
    }

    /**
     *
     * Attempts to break continuity across edges
     *
     * @param imageIn The original buffer in color
     * @param imageOut The binary skin map buffer to write to
     * @param width image width
     * @param height image height
     * @param threshold
     */
    private static void erodeEdges(int[] imageIn, int[] imageOut, int width, int height, int threshold) {
        int[][] matrix = new int[3][3];
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                matrix[0][0] = getRed(imageIn[getPos(i - 1, j - 1, width)]);
                matrix[0][1] = getRed(imageIn[getPos(i - 1, j, width)]);
                matrix[0][2] = getRed(imageIn[getPos(i - 1, j + 1, width)]);
                matrix[1][0] = getRed(imageIn[getPos(i, j - 1, width)]);
                matrix[1][2] = getRed(imageIn[getPos(i - 1, j + 1, width)]);
                matrix[2][0] = getRed(imageIn[getPos(i + 1, j - 1, width)]);
                matrix[2][1] = getRed(imageIn[getPos(i + 1, j, width)]);
                matrix[2][2] = getRed(imageIn[getPos(i + 1, j + 1, width)]);
                int p = getPos(i, j, width);
                int edge = (int) convolution(matrix);
                if (edge > threshold) {
                    //imageOut[p] = WHITE;
                } else {
                    imageOut[p] = BLACK;
                }
            }
        }
    }
    private static double convolution(int[][] matrix){
        int gy = matrix[0][0] * -1 + matrix[0][1] * -2 + matrix[0][2] * -1
                + matrix[2][0] + matrix[2][1] * 2 + matrix[2][2];
        int gx = matrix[0][0] + matrix[0][2] * -1 + matrix[1][0] * 2
                + matrix[1][2] * -2 + matrix[2][0] + matrix[2][2] * -1;
        return Math.sqrt(Math.pow(gy, 2) + Math.pow(gx, 2));
    }
    private static boolean[] binaryFillHoles(int[] image, int width, int height, int color) {
        boolean[] visited = new boolean[image.length];
        int target = BLACK;
        int x, y;
        // mark all the pixels start from the outer edge's of the image

        for(x = 0, y = 0;x < width; x++) { // top row
            flood_iterative(image, visited, target, width, height, x, y);
        }
        for(x = 0,y = height - 1;x < width; x++) { // bottom row
            flood_iterative(image, visited, target, width, height, x, y);
        }
        for(x = 0, y = 0; y < height; y++) {
            flood_iterative(image, visited, target, width, height, x, y);
        }
        for(x = width - 1, y = 0; y < height; y++) {
            flood_iterative(image, visited, target, width, height, x, y);
        }
        // the unmarked pixels are to be filled in as they are the holes
        for(int i = 0; i < visited.length;i++) {
            if(!visited[i]) {
                image[i] = color;
            }
        }
        return visited;
    }
    private List<Cluster> getClusters(int[] image, boolean[] marked, int minPoints, int width, int height) {
        List<Cluster> clusters = new ArrayList<>();
        for(int i = 0; i < image.length; i++) {
            if(!marked[i]) {
                Cluster c = getCluster(clusters, new Point(i%width, i/width));
                Stack<Integer> s = flood_iterative(image, marked, WHITE, width, height, i/width, i%width);
                // flood is a fast way to find the groups of touching pixels
                while(!s.isEmpty()) {
                    int idx = s.pop();
                    marked[idx] = true;
                    c.addPoint(new Point(idx%width,idx/width));
                }

                if(c.size() < minPoints || c.getHeightToWidthRatio() < minHeightToWidthRatio || c.getHeightToWidthRatio() > maxHeightToWidthRatio) {
                    clusters.remove(c);
                }
                marked[i] = true;
            }
        }
        removeOutliers(clusters);
        suppressContainedClusters(clusters);
        return clusters;
    }
    private static Cluster getCluster(List<Cluster> clusters, Point p) {
        for(Cluster c : clusters) {
            if(c.getBounds().contains(p)) {
                return c;
            }
        }
        Cluster cluster = new Cluster();
        clusters.add(cluster);
        return cluster;
    }
    private static void suppressContainedClusters(List<Cluster> clusters) {
        List<Cluster> toRemove = new ArrayList<>();
        for(Cluster c : clusters) {
            for(Cluster cc : clusters) {
                if(c != cc && cc.getBounds().contains(c.getBounds())) {
                    toRemove.add(c);
                }
            }
        }
        clusters.removeAll(toRemove);
    }
    private void removeOutliers(List<Cluster> clusters) {
        if(clusters == null || clusters.isEmpty())
            return;

        double[] areas = new double[clusters.size()];
        for(int i = 0; i < clusters.size(); i++) {
            Rectangle r = clusters.get(i).getBounds();
            areas[i] = r.getWidth() * r.getHeight();
        }
        Arrays.sort(areas);
        int middle = clusters.size() / 2;
        double median = areas[middle];

        ListIterator it = clusters.listIterator();
        Cluster c;
        while(it.hasNext()) {
            c = (Cluster)it.next();
            Rectangle r = c.getBounds();

            double area = r.getWidth() * r.getHeight();
            double areaPrct = c.size() / area; // # Skin pixels vs Bounding Box area

            if(area > maxAreaCoefficient * median || area < median * minAreaCoefficient || areaPrct < minSkinToAreaRatio)
                it.remove();
        }
    }
}