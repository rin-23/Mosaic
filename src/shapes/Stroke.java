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
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Stroke  {
    
    /* Stroke Global ID */
    private static int ID = 1;
    
    /* Basic Attributes */
    private ArrayList<Point> points;
    private boolean isBoundary;
    private int id;
    
    /* Growth Attributes */
    private Point centroid;
    private Dimension dimension;
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
    //if strokeType is -1 the determine type autmoatically
    public void finishPoint(Point p, AVLTree pointsTree, int sType) {
        addPoint(p);
        
        //close the long curve if  the last point is close to the first point. 
        if (points.size() > 10) {
            if (Utilities.distance(p, points.get(0)) < 15) {
                ArrayList<Point> tempPoints = Utilities.interpolate(p, points.get(0));
                points.addAll(tempPoints);
            }  
        }
        
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
        if (sType != -1) {
            this.strokeType = sType;
        } else {
            setStrokeType(); 
        }
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
        
        // Set Centroid and Calculate Bounding Box
        centroid = new Point(0, 0);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
                               
        for (Point p : points) {
            centroid.x += p.x;
            centroid.y += p.y;
            
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);           
        }        
        centroid.x = Math.round(centroid.x / (float)points.size());
        centroid.y = Math.round(centroid.y / (float)points.size());
        
        dimension = new Dimension(maxX - minX, maxY - minY);        
        
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
    
    public Dimension getDimension() {
        return this.dimension;
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
    
//    //Check if last point of this stroke is close to the first point of the given stroke
//    //In which case it can be extended
//    //Only extened non-polygonal boundary strokes
//    public boolean canBeExtendedByStroke(Stroke stroke, float distance) {
//        if (!this.isBoundary || !stroke.isBoundary)  {
//            return false; 
//        }
//        
//        if (this.strokeType == Constants.POLYGON || 
//            stroke.strokeType == Constants.POLYGON) 
//        {
//            return false; 
//        }
//        Point lastPoint = this.points.get(this.points.size()-1);
//        Point firstPoint = stroke.points.get(0);
//        return Utilities.distance(lastPoint, firstPoint) < distance;
//    }
//    
//    //Connect last point of this stroke to the first point of the given stroke
//    //In which this stroke is extended
//    //Only extened non-polygonal boundary strokes
//    public static Stroke combineStrokes(Stroke strokeToExtend, Stroke extensionStroke) {
//        if (!strokeToExtend.isBoundary || !extensionStroke.isBoundary)  {
//            return null; 
//        }
//        
//        if (strokeToExtend.strokeType == Constants.POLYGON || 
//            extensionStroke.strokeType == Constants.POLYGON) 
//        {
//            return null; 
//        }
//        
//        Point lastPoint = strokeToExtend.points.get(strokeToExtend.points.size()-1);
//        Point firstPoint = extensionStroke.points.get(0);
//        
//        ArrayList<Point> inBetweenPoints = Utilities.interpolate(lastPoint, firstPoint);
//        int newSize = strokeToExtend.points.size() + extensionStroke.points.size() + inBetweenPoints.size();
//        ArrayList<Point> newPoints = new ArrayList<Point>(newSize);
//        
//        newPoints.addAll(strokeToExtend.points);
//        newPoints.addAll(inBetweenPoints);
//        newPoints.addAll(extensionStroke.points);
//        
//        Stroke newStroke = new Stroke(newPoints, false, -1, 
//                strokeToExtend.getPolygonColor(), strokeToExtend.strokeType);
//       
//        return newStroke;
//         
//    }
    
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
    
    @Override
    public Stroke clone()  {
        Stroke s = new Stroke(this.points, !this.isBoundary, -1, this.getPolygonColor(), this.getStrokeType());
        return s;
    }

}
