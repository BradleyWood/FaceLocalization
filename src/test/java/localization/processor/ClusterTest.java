package localization.processor;

import junit.framework.TestCase;

import java.awt.*;

/**
 * Created by Brad on 5/18/2017.
 */
public class ClusterTest extends TestCase {

    public void testGetPoints() throws Exception {

    }
    public void testGetPolygon() throws Exception {
        Cluster s = new Cluster();
        assertEquals(false, s.getPolygon() == null);
        s.addPoint(new Point(10, 10));
        s.addPoint(new Point(0, 0));
        assertEquals(10,(int)s.getPolygon().getBounds().getHeight());
        assertEquals(10,(int)s.getPolygon().getBounds().getWidth());
    }

    public void testSize() throws Exception {
        Cluster cluster = new Cluster();
        assertEquals(0, cluster.size());
        cluster.addPoint(new Point(0,0));
        assertEquals(1, cluster.size());

    }
    public void testAddPoint() throws Exception {
        Cluster cluster = new Cluster();
        Point a = new Point(0, 0);
        Point b = new Point(10, 10);
        cluster.addPoint(a);
        cluster.addPoint(b);
        assertEquals(true, cluster.getPoints().get(0).equals(a));
        assertEquals(true, cluster.getPoints().get(1).equals(b));

    }

    public void testGetBounds() throws Exception {
        Cluster s = new Cluster();
        assertEquals(new Rectangle(0,0,0,0), s.getBounds());
        s.addPoint(new Point(0,0));
        s.addPoint(new Point(10, 10));
        assertEquals(new Rectangle(0,0, 10,10), s.getBounds());
        s.addPoint(new Point(20, 20));
        assertEquals(new Rectangle(0,0, 20, 20), s.getBounds());
    }

    public void testGetHeightToWidthRatio() throws Exception {

    }

}