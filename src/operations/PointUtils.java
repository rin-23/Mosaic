/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package operations;
import java.awt.Point;
import javax.vecmath.Point2f;

/**
 *
 * @author Rinat
 */
public class PointUtils {
    
    public static Point add(Point p1, Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }
    
    public static Point sub(Point p1, Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }
    
    public static Point scale(Point p1, float scale) {
        return new Point((int) Math.floor(p1.x * scale + 0.5), (int) Math.floor(p1.y * scale + 0.5));
    }
    
    public static Point2f add(Point2f p1, Point2f p2) {
        return new Point2f(p1.x + p2.x, p1.y + p2.y);
    }
    
    public static Point2f sub(Point2f p1, Point2f p2) {
        return new Point2f(p1.x - p2.x, p1.y - p2.y);
    }
    
    public static Point2f scale(Point2f p1, float scale) {
        return new Point2f(p1.x * scale, p1.y * scale);
    }
    
}
