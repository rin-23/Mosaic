package shapes;

import avltree.AVLTree;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;
import operations.Utilities;
import constants.Constants;
import java.awt.Polygon;

public class Stroke implements Serializable {
    
    /* Stroke Global ID */
    private static int ID = 1;
    
    /* Basic Attributes */
    private ArrayList<Point> points;
    private boolean isBoundary;
    private int id;
    
    /* Growth Attributes */
    private Point centroid;
    private ArrayList<Point> growthVectors;
    private ArrayList<Integer> growthMask;
    private ArrayList<Point> newPoints;

    /* Color Attributes */
    private Color polygonColor = null;
    private boolean strokeDone = false;
    private int strokeType = Constants.CURVE;
    // polygonPoints is only used the first time a shpae is drawn
    private ArrayList<Point> polygonPoints;
    private Polygon polygon = null;

    /* Constructor used when drawing a new stroke */
    public Stroke() {     
        points = new ArrayList<Point>();
        
        // Newly Drawn strokes are considered as a boundary,
        // especially when drawn in ClonePanel
        isBoundary = true;
        
        // Each stroke created has its unique ID
        this.id = ID;
        ID++;
    }
    
    /* Add points when drawing a new storke */
    public void addPoint(Point p) {
        if (points.isEmpty()) {
            points.add(p);
        } else {
            Point lastPoint = points.get(points.size() - 1);
            if (Utilities.distance(p, lastPoint) > 4) {
                if (points.size() == 1) {
                    points.add(p);
                } else {
                    Point secondLastPoint = points.get(points.size() - 2);
                    Point newLastPoint = Utilities.smoothPoint(secondLastPoint,
                            lastPoint, p);
                    points.set(points.size() - 1, newLastPoint);
                    points.add(p);
                }
            } 
        }
    }
    
    /* Add the last point when drawing a new storke */
    public void finishPoint(Point p, AVLTree pointsTree) {
        addPoint(p);
        // Smooth points
        for (int i = 0; i < 3; i++) {
            points = Utilities.smoothPoints(points);
        }
        // Interpolate points
        points = interpolatePoints(points);
        // Hash points based on boolean hash
        if (pointsTree != null) {
            for (Point point : points) {
                pointsTree.probe(new CPoint(point.x, point.y,
                        this.id, this.isBoundary));
            }
        } 
        // Set if the stroke is a CURVE or a closed POLYGON
        setStrokeType();
        // Set polygon color and polygon for color
        // filling if the stroke is a closed POLYGON
        if (strokeType == Constants.POLYGON) {
            
            // Below replace points, which could have split endings,
            // with polygonPoints, which have close endings.
            // This might be based on user setting
            if (pointsTree != null) {
                for (Point point : points) {
                    pointsTree.remove(new CPoint(point.x, point.y,
                            this.id, this.isBoundary));
                }
                for (Point point : polygonPoints) {
                    pointsTree.probe(new CPoint(point.x, point.y,
                            this.id, this.isBoundary));
                }
            }
            points = polygonPoints;
            // Above replace points, which could have split endings,
            // with polygonPoints, which have close endings.
            // This might be based on user setting
            
            setPolygon(points);
        }
        // Set that the stroke is done being drawn
        strokeDone = true;
    }
    
    /*
     * Constructor used when creating a stroke using
     * existing points, intended for moving and growing.
     * 
     * If only created for dragging on GlassPanel to show
     * centroid, setGrowth should be false to save memory.
     * 
     * If created for growing purposes (in ClonePanel),
     * setGrowth should be true to set up variable properly.
     */
    public Stroke(ArrayList<Point> oldPoints, boolean setGrowth, int newID,
            Color color, int type) {
        points = oldPoints;
        strokeType = type;
        if (strokeType == Constants.POLYGON) {
            polygonColor = color;
            setPolygon(points);
        }
        strokeDone = true;
        
        // Strokes intended for moving and growing
        // are considered to be flexible and non-boundary
        isBoundary = false;
        
        // Set Centroid
        centroid = new Point(0, 0);
        for (Point p : points) {
            centroid.x += p.x;
            centroid.y += p.y;
        }        
        centroid.x = Math.round(centroid.x / (float)points.size());
        centroid.y = Math.round(centroid.y / (float)points.size());
        
        // Each stroke created has its unique ID
        if (newID >= 0) {
            this.id = newID;
        } else {
            this.id = ID;
            ID++;
        }

        if (setGrowth) {
            setGrowth();
        }
    }
    
    /* Set up growth variables for strokes intended for growing */
    private void setGrowth() {   
        // Set up GrowthVectors
        growthVectors = new ArrayList<Point>();
        for (Point p : points){
            Point vector = new Point(p.x - centroid.x, p.y - centroid.y);
            growthVectors.add(vector);
        }
        
        // Set up GrowthMask
        growthMask = new ArrayList<Integer>();
        for (int i = 0; i < points.size(); i++){
            growthMask.add(0);
        }
        
        // Set up NewPoints
        newPoints = new ArrayList<Point>();
        for (int i = 0; i < points.size(); i++){
            newPoints.add(new Point(centroid.x, centroid.y));
        }
    }
    

    
    /* Interpolate an array of points */
    public ArrayList<Point> interpolatePoints(ArrayList<Point> points) {
        ArrayList<Point> interpolatedPoints = new ArrayList<Point>();
        // Interpolate points
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            interpolatedPoints.add(p1);
            interpolatedPoints.addAll(Utilities.interpolate(p1, p2));
        }
        interpolatedPoints.add(points.get(points.size() - 1));
        return interpolatedPoints;
    }
    
    /* Paint the storke with/without the centroid */
    public void paint(Graphics g, Color color, boolean paintCentroid) {
        
        Graphics2D g2D = (Graphics2D) g;
        
        if (strokeDone && strokeType == Constants.POLYGON
                && polygonColor != null) {
            g2D.setColor(polygonColor);
            g2D.fill(polygon);
        }

        //g2D.setClip(null);
        
        g2D.setColor(color);
        if (points.size() == 1) {
            g2D.draw(new Line2D.Double(points.get(0), points.get(0)));
        } else if (points.size() > 1) {
            for (int i = 0; i < points.size() - 1; i++){
                g2D.draw(new Line2D.Double(points.get(i),
                    points.get(i+1)));
            }
        }
        
        if (paintCentroid) {
            g2D.fill(new Ellipse2D.Double(centroid.x - 1,
                    centroid.y - 1, 2, 2));
        }
    }

    /* - START HERE - GET and SET operations */
    
    public int getID() {
        return this.id;
    }

	public boolean getIsBoundary () {
        return this.isBoundary;
    }
    
    public void setIsBoundary (boolean flag) {
        this.isBoundary = flag;
    }
    
    public Point getCentroid(){
        return this.centroid; 
   }
    
    public void setCentroid(int x, int y) {
        this.centroid.x = x;
        this.centroid.y = y;
    }
    
    public ArrayList<Point> getPoints(){
        return this.points;
    }
    
    public void setPoints(ArrayList<Point> newP){
        this.points = newP;
        if (strokeType == Constants.POLYGON) {
            setPolygon(points);
        }
        
    }
    
    public ArrayList<Point> getgrowthVectors(){
        return this.growthVectors;
    }
    
    public ArrayList<Integer> getgrowthMask(){
        return this.growthMask;
    }
    
    public ArrayList<Point> getNewPoints(){
        return this.newPoints;
    }
    
    // Test if a s stroke is NOT grown at all
    // by checking if its points consist entirely
    // of the single centroid point
    public boolean notGrown(){
        boolean notGrown = true;
        for (Point p : this.points) {
            if (p.x != this.centroid.x || p.y != this.centroid.y) {
                notGrown = false;
            }
        }
        return notGrown;
    }
            
    /* - END HERE - GET and SET operations */
    
    /* - BEGIN HERE - Color operations */
    
    public Color getPolygonColor() {
        return this.polygonColor;
    }
    
    public void setPolygonColor(Color newColor) {
        if (this.strokeType == Constants.POLYGON) {
            this.polygonColor = newColor;
        }
    }
    
    public int getStrokeType() {
        return this.strokeType;
    }
    
    // Check if Point p2 is in the 3x3 box of Point p1
    private boolean inPointSquare(Point p1, Point p2) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (p1.x + i == p2.x && p1.y + j == p2.y) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Check if this stroke is open or closed, and return
    // the index after which should be removed from polygonPoints
    private int isClosed() {
        boolean closed = false;
        int removeIndex = -1;
        ArrayList<Point> pointsClone = (ArrayList<Point>) points.clone();
        while (pointsClone.size() >= 5 && !closed) {
            for (int i = 4; i < pointsClone.size(); i++) {
                if (inPointSquare(pointsClone.get(0), pointsClone.get(i))) {
                    closed = true;
                    removeIndex = i;
                    break;
                }
            }
            if (closed) {
                polygonPoints = pointsClone;
                return removeIndex;
            } else {
                pointsClone.remove(0);
            }
        }
        return removeIndex;
    }
    
    private void setStrokeType() {
        int removeIndex = -1;
        removeIndex = isClosed();
        if (removeIndex != -1) {
            this.strokeType = Constants.POLYGON;
            int size = polygonPoints.size();
            for (int i = removeIndex + 1; i < size; i++) {
                polygonPoints.remove(polygonPoints.size() - 1);
            }
        }
        
    }
    
    /* Check if the stroke contains a point */
    public boolean intersectsPoint(Point p) {
        CPoint cp = new CPoint(p.x, p.y, this.id, false);
        if (Constants.clonePointsTree.get(cp) != null) {
            return true;
        }
        return false;
    }
    
    public Polygon getPolygon() {
        return this.polygon;
    }
    
    private void setPolygon(ArrayList<Point> polyPoints) {
        this.polygon = new Polygon();
        for (int i = 0; i < polyPoints.size(); i++) {
            Point polygonPoint = polyPoints.get(i);
            this.polygon.addPoint(polygonPoint.x, polygonPoint.y);
        }
    }
    
    /* - ENDS HERE - Color operations */
    
    @Override
    public String toString() {
        return ("Stroke: id = " + id + ", is boundary = " + isBoundary);
    }		

}
