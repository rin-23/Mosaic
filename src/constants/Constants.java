package constants;

import avltree.AVLTree;
import java.awt.Point;
import java.util.ArrayList;

public class Constants {
    
    public static final int NONE = 0;
    public static final int DRAW = 1;
    public static final int BRUSH = 2;
    public static final int BRUSH_RECTANGLE = 3;
    public static final int DRAG = 4;
    
    public static final int STAGE_SURFACE = 5;
    public static final int STAGE_DRAG = 6;
    public static final int STAGE_SINK = 7;
    
    public static final int DRAW_GUIDE = 8;
    
    public static final int COLOR = 10;
    public static final int ERASE = 11;
    public static final int SELECT = 12;
    
    public static int COLOR_GRADIENT = 0;
    
    public static int THRESHOLD = 5;
    public static final float ITERATION = 100;
    
    public static final int HEIGHT = 500;
    public static final int DRAW_WIDTH = 400;
    public static final int CLONE_WIDTH = 600;
    
    public static final float SELECTION_PERCENT = 0.2f;
    public static final int PHOTOSHOP_MODE = 1000;

    public static float SMOOTHNESS = 0.2f;
    
    public static ArrayList<Point> circleMap = new ArrayList<Point>();
    
    public static AVLTree clonePointsTree = new AVLTree();
    public static AVLTree drawPointsTree = new AVLTree();
	
    public static final int POLYGON = 1;
    public static final int CURVE = 2;
	
    public static final int FLEXIBLE_TO_FLEXIBLE = 0;
    public static final int FLEXIBLE_TO_SOLID = 1;
    public static final int SOLID_TO_SOLID = 2;
    
    
    
    
    
    
    public Constants() {} /* Don't instantiate, use directly */
    
    // This is to get a list of points that are within the
    // THRESHOLD radius when the central point is at (0, 0).
    public static void circleMap() {
        circleMap.clear();
        for (int i = -1 * THRESHOLD; i <= THRESHOLD; i++) {
            for (int j = -1 * THRESHOLD; j <= THRESHOLD; j++) {
                int distance = (i * i) + (j * j);
                if (distance != 0 && distance <= (THRESHOLD * THRESHOLD)) {
                    circleMap.add(new Point(i, j));
                }
            }
        }
    }

}
