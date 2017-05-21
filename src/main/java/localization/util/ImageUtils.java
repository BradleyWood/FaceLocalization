package localization.util;

import java.util.Stack;

/**
 * Created by Brad on 5/19/2017.
 */
public class ImageUtils {

    /**
     * Calculates the 2d index on a 1d array
     *
     * @param x
     * @param y
     * @param width Image Width
     * @return The index
     */
    public static int getPos(int x, int y, int width) {
        return y * width + x;
    }

    /**
     *
     * Finds and marks all the pixels of the same color that are touching each other.
     * Does not modify the original image. Simply marks the filled pixels as visited and returns
     * a Stack containing what should be filled.
     *
     * @param image The original buffer
     * @param visited The list of pixels that have already been filled. Marks newly filled fixels here.
     * @param target The target color.
     * @param width The image Width
     * @param height The image Height
     * @param row The row to start filling at
     * @param col The col to start filling at
     * @return A Stack containing the indexes of pixels that were visited (to be filled)
     */
    public static Stack<Integer> flood_iterative(int[] image, boolean[] visited, int target, int width, int height, int row, int col) {
        Stack<Integer> stk = new Stack<>(); // used to notify caller exactly what was filled

        int pos = row * width + col;
        Stack<Integer> stack = new Stack<>();
        stack.push(pos);
        while(!stack.isEmpty()) {
            Integer p = stack.pop();
            int r = p / width; // row,col
            int c = p % width;

            // if idx is not in image or idx visited already or not target THEN we will skip
            if (r < 0 || c < 0 || r >= height || c >= width || visited[p] || image[p] != target)
                continue;

            visited[p] = true;
            stk.push(p);

            stack.push(getPos(c, r - 1, width));
            stack.push(getPos(c, r + 1, width));
            stack.push(getPos(c - 1, r, width));
            stack.push(getPos(c + 1, r, width));
        }
        return stk;
    }

}
