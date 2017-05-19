package localization.processor;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A cluster that contains a group of points
 */
public class Cluster {

    private final List<Point> pointList = new ArrayList<Point>();
    private final Polygon poly = new Polygon();
    private Rectangle bounds = null;

    public List<Point> getPoints() {
        return pointList;
    }
    public Polygon getPolygon() {
        return poly;
    }
    public int size() {
        return pointList.size();
    }
    public void addPoint(Point p) {
        pointList.add(p);
        poly.addPoint(p.x,p.y);
        if(bounds != null && !bounds.contains(p)) {
            bounds = null; // force recalculation
        }
    }
    public Rectangle getBounds() {
        if(bounds == null)
            return bounds = poly.getBounds();
        return bounds;
    }
    public float getHeightToWidthRatio() {
        Rectangle r = getBounds();
        return r.height / (float) r.width;
    }
}
