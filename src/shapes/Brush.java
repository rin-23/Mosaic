package shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

public class Brush implements Serializable {
    
    public static double d = 20;
    private ArrayList<Point> points;

    public Brush() {
        points = new ArrayList<Point>();
    }

    public Brush(ArrayList<Point> pointsArray) {
        this.points = pointsArray;
    }
    
    public void addPoint(Point p) {
        points.add(p);
    }

    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        Color color = new Color((float)100/255, 
            (float)149/255, (float)237/255, (float)0.05);
        g2D.setColor(color);

        if (points.size() > 0) {
            for (Point p : points) {
                double coordX = p.x - d / 2;
                double coordY = p.y - d / 2;
                g2D.fill(new Ellipse2D.Double(coordX, coordY, d, d));
            }
        }
    }

    public void clear() {
        points.clear();
    }

    public ArrayList getPoints(){
        return points;
    }

}