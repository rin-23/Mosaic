package operations;

import constants.Constants;
import imagecloning.ClonePanel;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import static java.lang.Float.NaN;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collections;
import javax.vecmath.Point2f;
import shapes.CPoint;
import shapes.Stroke;
import operations.PointUtils;
import static operations.PointUtils.add;
import static operations.PointUtils.scale;
import static operations.PointUtils.sub;

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
                for (int j = 0; j < s.getgrowthVectors().size(); j++) 
                {
                    if (s.getgrowthMask().get(j) == 0) 
                    {
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
    
    public static boolean getGrownStrokePoints(ArrayList<Point2f> points, ArrayList<Point2f> newPoints, ArrayList<Float> weights) 
    {
        int numOfPoints = points.size();
        Point2f centroid = new Point2f(0,0);
        for (Point2f p : points) 
        {
            centroid = add(centroid, p);
        }
        centroid = scale(centroid, 1.0f/points.size());
        
        ArrayList<Point2f> growthVectors = new ArrayList<Point2f>();
        for (Point2f p : points){
            growthVectors.add(sub(p, centroid));
        }
        
        ArrayList<Integer> growthMask = new ArrayList<Integer>();
        for (int i = 0; i < points.size(); i++){
            growthMask.add(0);
        }
        
        for (int i = 1; i <= Constants.ITERATION; i++) 
        {
            for (int j = 0; j < numOfPoints; j++) 
            {
                if (growthMask.get(j) == 0) // still growing 
                {
                    Point2f newP = add(centroid, scale(growthVectors.get(j), i / (float)Constants.ITERATION ));
                    
                    ArrayList<Point> circlePoints = Utilities.getCirlePoints(new Point((int)Math.round(newP.x), 
                                                                                       (int)Math.round(newP.y)));
                    if (intersectGrowth(circlePoints)){
                        growthMask.set(j, 1);
                        weights.set(j, 1.5f);
                    } else {
                        newPoints.set(j, newP);
                        weights.set(j, 1.0f);
                    }                   
                }
            }   
            float occur = (float) Collections.frequency(growthMask, 1);
            if (occur/(float)points.size() > 0.3)
                return true;
        }
        
        return Collections.frequency(growthMask, 1) > 0.1;
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
    public static ArrayList<Stroke> getIntersectedFlexibleStrokes(ArrayList<Stroke> newStrokes) {
        
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
    
    public static void growSolidStrokesKaran(Stroke newStroke) 
    {
        ArrayList<Point2f> target = new ArrayList<>(newStroke.getPoints().size());
        ArrayList<Point2f> source = new ArrayList<>(newStroke.getPoints().size());
        ArrayList<Float> weigths = new ArrayList<>(newStroke.getPoints().size());
        
        for (Point p: newStroke.getPoints()) {
            target.add(new Point2f(0,0));
            source.add(new Point2f((float)p.x, (float)p.y));
            weigths.add(1.0f);
        }
        
        boolean needToGrow;
        
        for (int i = 0; i < 50; i++) 
        {
            //ASSIGN HIGHER WEIGHTS TO THE POINTS THAT COLLIDED
            needToGrow = getGrownStrokePoints(source, target, weigths); 
            if (!needToGrow){
                break;
            } else { 
                ArrayList<Point2f> transformedPoints = transformations(target, source, weigths);
                source = transformedPoints;
            }
        }
        
        ArrayList<Point> finalSource = new ArrayList<Point>(newStroke.getPoints().size());
        for (Point2f p: target) {
            Point p2 = new Point((int)Math.round(p.x), (int)Math.round(p.y));
            finalSource.add(p2);
        }
        
        Stroke transPoints = new Stroke(finalSource, 
                                        true, 
                                        -1, 
                                        newStroke.getPolygonColor(), 
                                        newStroke.getStrokeType());
        
//        Stroke grownStroke = new Stroke(nextGrownPoints, 
//                                true, 
//                                -1, 
//                                newStroke.getPolygonColor(), 
//                                newStroke.getStrokeType());

        for (Point p : transPoints.getPoints()) {
              Constants.clonePointsTree.probe(new CPoint(p.x, p.y, transPoints.getID(), transPoints.getIsBoundary()));
         }
         
//         for (Point p : grownStroke.getPoints()) {
//              Constants.clonePointsTree.probe(new CPoint(p.x, p.y, grownStroke.getID(), grownStroke.getIsBoundary()));
//         }
        
//        ClonePanel.flexibleStrokes.add(grownStroke);
        ClonePanel.flexibleStrokes.add(transPoints);
     }

    public static ArrayList<Point2f> transformations(ArrayList<Point2f> points1, ArrayList<Point2f> points2, ArrayList<Float> weighting) 
    {
        assert(points1.size() == points2.size());
        assert(weighting.size() == points1.size());

        int N_POINTS = points1.size();
        float totalWeight=0.0f;
        for (Float a : weighting) {
            totalWeight += a; 
        }

        //1.  Translation is given by the centres of mass between the two curves
        Point2f center1 = new Point2f(0,0);
        Point2f center2 = new Point2f(0,0);

        for (int i = 0; i < N_POINTS; i++)
        {
            center1 = add(center1, scale(points1.get(i), weighting.get(i)));
            center2 = add(center2, scale(points2.get(i), weighting.get(i)));
        }
        
        center1 = scale(center1, 1.0f/totalWeight);
        center2 = scale(center2, 1.0f/totalWeight);
        
        float fitRotate;
        Point2f newCenter = center1;
        Point2f fitTranslate = sub(center1, center2);

        //Set up matrix A_pq
        double[] A_pq = new double[4];
        for (int i=0;i<4;i++) {
                A_pq[i]=0.0f;
        }

        for (int i=0; i < N_POINTS; i++) { //JAMES CHNAGE FROM EDGEPOINTSET TO arcLengthSamples
                Point2f p_i = sub(points1.get(i),center1);
                Point2f q_i = sub(points2.get(i),center2);

                A_pq[0]+=p_i.x*q_i.x*weighting.get(i);
                A_pq[1]+=p_i.x*q_i.y*weighting.get(i);
                A_pq[2]+=p_i.y*q_i.x*weighting.get(i);
                A_pq[3]+=p_i.y*q_i.y*weighting.get(i);
        }

        //Solve for S, where S=sqrt(A_pq^TA_pq)
        double[] A_pqTA_pq=new double[4];
        A_pqTA_pq[0]=A_pq[0]*A_pq[0]+A_pq[2]*A_pq[2];
        A_pqTA_pq[1]=A_pq[0]*A_pq[1]+A_pq[2]*A_pq[3];
        A_pqTA_pq[2]=A_pq[0]*A_pq[1]+A_pq[2]*A_pq[3];
        A_pqTA_pq[3]=A_pq[1]*A_pq[1]+A_pq[3]*A_pq[3];

        //square root can be found using eigenvalues of this 2x2 matrix
        //Direct method to obtain eigenvalues
        double a, b, c, d;
        a=A_pqTA_pq[0];
        b=A_pqTA_pq[1];
        c=A_pqTA_pq[2];
        d=A_pqTA_pq[3];

        double r_1=(a+d)/2.0+Math.sqrt(((a+d)*(a+d))/4.0+b*c-a*d);
        double r_2=(a+d)/2.0-Math.sqrt(((a+d)*(a+d))/4.0+b*c-a*d);

        double []sqrtA=new double[4]; //sqrt(A) where A_pq^T A_pq
        double []Sinv=new double[4];
        double []R=new double[4];

        if (r_1!=0.0f&&r_2!=0.0f) { //If matrix is not RANK DEFICIENT

            double m=0;
            double p=0;

            if (r_2!=r_1) {
                    m=(sqrt(r_2)-sqrt(r_1))/(r_2-r_1);
                    p=(r_2*sqrt(r_1)-r_1*sqrt(r_2))/(r_2-r_1);
            }
            else if (r_2==r_1) {
                    m=1/(4*r_1);
                    p=sqrt(r_1)/2;
            }

            //sqrt(A)=m*A+p*I
            sqrtA[0]=m*A_pqTA_pq[0]+p;
            sqrtA[1]=m*A_pqTA_pq[1];
            sqrtA[2]=m*A_pqTA_pq[2];
            sqrtA[3]=m*A_pqTA_pq[3]+p;

            //S^(-1) = (1/ad-bc)(d -b; -c a)
            float determinant=(float) (1/(sqrtA[0]*sqrtA[3]-sqrtA[1]*sqrtA[2]));
            Sinv[0]=determinant*sqrtA[3];
            Sinv[1]=determinant*(-sqrtA[1]);
            Sinv[2]=determinant*(-sqrtA[2]);
            Sinv[3]=determinant*sqrtA[0];

            //finally, R=A_pq*S^(-1)
            R[0]=A_pq[0]*Sinv[0]+A_pq[1]*Sinv[2];
            R[1]=A_pq[0]*Sinv[1]+A_pq[1]*Sinv[3];
            R[2]=A_pq[2]*Sinv[0]+A_pq[3]*Sinv[2];
            R[3]=A_pq[2]*Sinv[1]+A_pq[3]*Sinv[3];

            if (abs(R[0]-R[3])<0.001f&&abs(R[1]-R[2])>0.001f) {
                if (R[1]<0.0) {
                        fitRotate=(float) (-cos(R[0])*180.0/Math.PI);
                }
                else {
                        fitRotate=(float) (cos(R[0])*180.0/Math.PI);
                }
            }
            else {
                 if (R[1]<0.0) {
                        fitRotate=(float) (acos(R[0])*180.0/Math.PI);
                } else {
                        fitRotate=(float) (-acos(R[0])*180.0/Math.PI);
                }
            }
        }
        else { //MATRIX A_pq is RANK DEFICIENT
            //use arctangent of 1st tangent to approximate
            fitRotate=(float) (-Math.atan2(points1.get(points1.size()-1).y-points1.get(0).y,
                    points1.get(points1.size()-1).x-points1.get(0).x)*180.0f/Math.PI);
        }
        
        if (!(fitRotate > 0 || -1*fitRotate > 0)) 
        {
            fitRotate = 0.0f;
        }
        
        ArrayList<Point2f>  points3 = new ArrayList<Point2f>(points1.size());
        for (int i = 0; i < points2.size(); i++) 
        {
            Point2f p = new Point2f(points2.get(i));
            p = add(p, fitTranslate);
            p = GetRotatedZ(sub(p, newCenter), -1*fitRotate);
            points3.add(add(p, newCenter));
        }
        

        System.out.println(fitRotate);
        System.out.println(fitTranslate);
        
        return points3;
    }  
    
    public static Point GetRotatedZ(Point p, float angle) {
        float sinAngle = (float)Math.sin(Math.PI * angle / 180);
        float cosAngle = (float)Math.cos(Math.PI * angle / 180);
    
	return new Point(Math.round(p.x * cosAngle - p.y * sinAngle), 
                         Math.round(p.x * sinAngle + p.y * cosAngle));
    }
    
    public static Point2f GetRotatedZ(Point2f p, float angle) {
        float sinAngle = (float)Math.sin(Math.PI * angle / 180);
        float cosAngle = (float)Math.cos(Math.PI * angle / 180);
    
	return new Point2f(p.x * cosAngle - p.y * sinAngle, 
                           p.x * sinAngle + p.y * cosAngle);
    }
    
    public static void growSolidStrokes(Stroke newStroke)
    {
        //TO DO: Assume we only have one stroke
        Point centroid = newStroke.getCentroid();
        //Precalculate expansion vector that takes the point from the centroid to its final position
        ArrayList<Point> expansionVectors = newStroke.getgrowthVectors();

        double moveCentroidBy[] = {0, 0};
        ArrayList<double[]> moveCentroidArray = new ArrayList<double[]>();
        moveCentroidArray.add(moveCentroidBy);

        startTime = System.currentTimeMillis();

        do {
            for (double[] m : moveCentroidArray) {
                centroid.x = (int) Math.round(centroid.x - m[0]);
                centroid.y = (int) Math.round(centroid.y - m[1]);
            }

            //Failed to add the stroke within 10 seconds. Abort.
            if ((System.currentTimeMillis() - startTime) > 10 * 1000) {
                System.out.println("Could not add the stroke. Please try again");
                return;
            }

            moveCentroidArray = moveCentroid(centroid, expansionVectors);
        } while (moveCentroidArray != null);

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
			cloneStroke.finishPoint(expandingPoint, Constants.drawPointsTree, -1);
		}
                
        // After the newPoints are re-assigned, add the points
        // back to pointTree for proper intersection dection
        for (Point p : cloneStroke.getPoints()) {
            Constants.clonePointsTree.probe(new CPoint(p.x, p.y, cloneStroke.getID(), cloneStroke.getIsBoundary()));
        }
		
	    ClonePanel.flexibleStrokes.add(cloneStroke);
		
	}
	


}
