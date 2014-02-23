package operations;

import constants.Constants;
import imagecloning.ClonePanel;
import java.awt.Point;
import java.util.ArrayList;
import shapes.CPoint;
import shapes.Stroke;

/*
 * This class contains all methods related to stroke growth.
 */
public class Grow {
    
    public static int notGrown = 0;
    private static long startTime;
	
	public Grow() {} /* Don't instantiate, use directly */
    
    /* Grow the strokes in newStrokes (unless the function is called
     * after a stroke is drawn in ClonePanel) along with all the
     * intersected flexible strokes, so that they will both deform
     * to fit the boundary points and deform TOGETHER with all other
     * intersected flexible strokes.
     */
    public static void growStrokes(ArrayList<Stroke> newStrokes, boolean toFlexibleBackground) {
        
        notGrown = 0;
        
        // Get all flexible strokes that the strokes in
        // the newStrokes intersect
        ArrayList<Stroke> reGrowthStrokes; 
        if (toFlexibleBackground) {
            reGrowthStrokes = getIntersectedFlexibleStrokes(newStrokes);
        } else {
            reGrowthStrokes = new ArrayList<Stroke>(); //just empty
            //reGrowthStrokes.addAll(newStrokes);
        }
        // This means the method is called after strokes are dragged
        // from DrawPanel, as opposed to being called after a single
        // stroke is drawn in the ClonePanel. We should dynamically
        // grow all old/new intersected flexible strokes, but not the
        // drawn (boundary) stroke itself.
        if (newStrokes.get(0).getIsBoundary() == false) {
            reGrowthStrokes.addAll(newStrokes);
        }
        
        // A list of points that are within  
        // the THRESHOLD radius of a point
        ArrayList<Point> circlePoints = new ArrayList<Point>();

        for (int i = 1; i <= Constants.ITERATION; i++) {
            
            for (Stroke s : reGrowthStrokes) {
                
                // After the first iteration, first remove the old
                // newPoints of the current stroke from pointTree
                if (i > 1) {
                    for (Point p : s.getNewPoints()) {
                        Constants.clonePointsTree.remove(new CPoint(p.x, p.y,
                            s.getID(), s.getIsBoundary()));
                    }
                }
                
                // For each point, grow until there is a collision,
                // and then NEVER grows from this poing on
                for (int j = 0; j < s.getgrowthVectors().size(); j++) {
                    if (s.getgrowthMask().get(j) == 0) {
                        Point newP = new Point();
                        newP.x = Math.round(s.getCentroid().x
                                + i / (float)Constants.ITERATION
                                * s.getgrowthVectors().get(j).x);
                        newP.y = Math.round(s.getCentroid().y
                                + i / (float)Constants.ITERATION
                                * s.getgrowthVectors().get(j).y);

                        circlePoints = Utilities.getCirlePoints(newP);
                        if (intersectGrowth(circlePoints)){
                            s.getgrowthMask().set(j, 1);
                        } else {
                            s.getNewPoints().set(j, newP);
                        }                   
                    }
                }
                
                // After the newPoints are re-assigned, add the points
                // back to pointTree for proper intersection dection
                for (Point p : s.getNewPoints()) {
                    Constants.clonePointsTree.probe(new CPoint(p.x, p.y,
                    s.getID(), s.getIsBoundary()));
                }
            }
        }
        
        // Set the final newPoints as the actual display points
        for (Stroke s : reGrowthStrokes) {
            s.setPoints(s.getNewPoints());
            s.setPoints(s.interpolatePoints(s.getPoints()));
        }
        
        // Only add strokes that have grown and eliminate those
        // strokes that are still a single point (centroid) because
        // the centoid itself is within the threshold
        for (Stroke s : reGrowthStrokes) {
            if (!s.notGrown()) {
                ClonePanel.flexibleStrokes.add(s);
            } else {
                for (Point p : s.getPoints()) {
                    Constants.clonePointsTree.remove(new CPoint(p.x, p.y,
                        s.getID(), s.getIsBoundary()));
                }
                notGrown++;
            }
        }
 
    }
    
    /*
     * For all points in the list, return if any of 
     * the points alreay exists in the ClonePanel
     */
    public static boolean intersectGrowth(ArrayList<Point> points) {
        CPoint cp;
        for (Point p: points) {
            cp = new CPoint(p.x, p.y, -1, false);
            if (Constants.clonePointsTree.get(cp) != null) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Get a list of existing flexible strokes that the strokes in
     * newStrokes intersect and remove the existing ones from ClonePanel
     */
    public static ArrayList<Stroke>
            getIntersectedFlexibleStrokes(ArrayList<Stroke> newStrokes) {
        
        CPoint cp = null;
        CPoint newcp = null;
        
        // IDs of existing flexible strokes that are intersected
        ArrayList<Integer> removeID = new ArrayList<Integer>();
        // List of existing flexible strokes that are intersected
        ArrayList<Stroke> regrowthStrokes = new ArrayList<Stroke>();
        // List of existing flexible strokes that are NOT intersected
        ArrayList<Stroke> newFlexibleStrokes = new ArrayList<Stroke>();
        
        for (Stroke s : newStrokes) {
            for (Point p : s.getPoints()) {
                // This is to cover more space around this Point p
                // in case there is no actual point on flexible strokes
                // at the point of intersection
                for (int i = -2; i <= 2; i++) {
                    for (int j = -2; j <= 2; j++) {
                        cp = new CPoint(p.x + i, p.y + j, -1, false);
                        newcp = (CPoint) Constants.clonePointsTree.get(cp);
                        if (newcp != null && newcp.isBoundary != true) {
                            if (!removeID.contains(newcp.id)) {
                                removeID.add(newcp.id);
                            }
                        }
                    }
                }
            }
        }
        
        ClonePanel.intersectedStrokeIDs = removeID;
        
        // Add existing flexible strokes to either 
        // regrowthStrokes or newFlexibleStrokes
        for (Stroke s : ClonePanel.flexibleStrokes) {
            if (removeID.contains(s.getID())){
                for (Point p : s.getPoints()) {
                    cp = new CPoint(p.x, p.y, s.getID(), s.getIsBoundary());
                    Constants.clonePointsTree.remove(cp);
                }
                // Create a new storke for proper growth
                Stroke newS = new Stroke(s.getPoints(), true, s.getID(),
                        s.getPolygonColor(), s.getStrokeType());
                regrowthStrokes.add(newS);
            } else {
                newFlexibleStrokes.add(s);
            }
        }
        
        // This essentially remove all the existing
        // flexible strokes that are intersected
        ClonePanel.flexibleStrokes = newFlexibleStrokes;
        
        return regrowthStrokes;
  
    }
    

	public static void growSolidStrokes(Stroke newStroke) {
        
		
		//TO DO: Assume we only have one stroke
		Point centroid = newStroke.getCentroid();
         //Precalculate expansion vector that takes the point from the centroid to its final position
		ArrayList<Point> expansionVectors = newStroke.getgrowthVectors();
        
      	double moveCentroidBy[] = {0,0};
        ArrayList<double[]> moveCentroidArray = new ArrayList<double[]>();
		moveCentroidArray.add(moveCentroidBy);
		
		startTime = System.currentTimeMillis();
		 
		do {
			for (double[] m : moveCentroidArray) {
				centroid.x = (int) Math.round(centroid.x - m[0]);
				centroid.y = (int) Math.round(centroid.y - m[1]);
			}
			
			//Failed to add the stroke within 10 seconds. Abort.
			if((System.currentTimeMillis() - startTime) > 10*1000 ) {
				System.out.println("Could not add the stroke. Please try again");
				return;
			}
			
			moveCentroidArray = moveCentroid(centroid, expansionVectors);
		} while(moveCentroidArray!=null); 
		
		createdMovedStroke(centroid, expansionVectors);
		
	}
	
		   
	private static ArrayList<double[]> moveCentroid(Point centroid, ArrayList<Point> expansionVectors){
		
		ArrayList<double[]> moveCentroidArray = new ArrayList <double[]>();
		
		boolean needToReturn = false;
		         
        for (int i = 0; i <= Constants.ITERATION; i++) {
            for(Point vector: expansionVectors) {
				//Calculate unit vector    
				double length = Utilities.distance(vector, new Point(0,0));
				double unit_x =vector.x/length;
				double unit_y = vector.y/length;
				double scaledUnitVector[] = {Constants.THRESHOLD*unit_x, Constants.THRESHOLD*unit_y};
                double unitVector[] = {unit_x, unit_y};
				boolean similarexist = false;
                
                //Check if we are alrady moving in thise direction
                for (double[] v: moveCentroidArray) {
                    if ( (v[0] * unitVector[0] + v[1] * unitVector[1]) > 0.9) {
                        similarexist = true;
                        break;
                    }
                }
                
				if (!similarexist) {
                    Point expandingPoint = new Point(centroid);
                    expandingPoint.x = Math.round(centroid.x +  (vector.x*(i / (float)Constants.ITERATION)));
                    expandingPoint.y = Math.round(centroid.y +  (vector.y*(i / (float)Constants.ITERATION)));

                    // A list of points that are within  
                    // the THRESHOLD radius of a point
                    ArrayList<Point> circlePoints = new ArrayList<Point>();

                    circlePoints = Utilities.getCirlePoints(expandingPoint);
                    if (intersectGrowth(circlePoints)){
                        if (!moveCentroidArray.contains(scaledUnitVector)) {
                            moveCentroidArray.add(scaledUnitVector);
                        }
                        needToReturn = true;
                    }
                }
			}
						
			if (needToReturn) return moveCentroidArray;
		}
		
		return null;
	}
	
	private static void createdMovedStroke(Point centroid, ArrayList<Point> expansionVectors){
		
		//Simulate drainf of a stroke
		Stroke cloneStroke = new Stroke();
		
		
		//Add all points  until the last one
		for(int i =0; i < expansionVectors.size() - 1; i++) {
			Point expandingPoint = new Point(centroid);
			expandingPoint.x = centroid.x + (int) (expansionVectors.get(i).x);
			expandingPoint.y = centroid.y + (int) (expansionVectors.get(i).y);
			cloneStroke.addPoint(expandingPoint);
		}

		//Add the last point
		if (expansionVectors.size() >= 1) {
			Point expandingPoint = new Point(centroid);
			int i = expansionVectors.size() -1;
			expandingPoint.x = centroid.x + (int) (expansionVectors.get(i).x);
			expandingPoint.y = centroid.y + (int) (expansionVectors.get(i).y);
			cloneStroke.finishPoint(expandingPoint, Constants.drawPointsTree);
		}
                
        // After the newPoints are re-assigned, add the points
        // back to pointTree for proper intersection dection
        for (Point p : cloneStroke.getPoints()) {
            Constants.clonePointsTree.probe(new CPoint(p.x, p.y, cloneStroke.getID(), cloneStroke.getIsBoundary()));
        }
		
	    ClonePanel.flexibleStrokes.add(cloneStroke);
		
	}
	


}
