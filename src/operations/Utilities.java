package operations;

import MenuToolbar.ColorChooser;
import avltree.AVLTree;
import constants.Constants;
import imagecloning.ClonePanel;
import imagecloning.MainFrame;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import shapes.Stroke;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import shapes.CPoint;

public class Utilities {
    
    public Utilities() {} /* Don't instantiate, use directly */
     
    /* Transform points from a JPanel to another JPanel*/
    public static ArrayList<Point> transform(ArrayList<Point> points,
            JComponent from, JComponent to) {
        ArrayList<Point> transformed = new ArrayList<Point>();
        for (Point p : points) {
            transformed.add(SwingUtilities.convertPoint(from, p, to));
        }
        return transformed;
    }
    
    /* Shift an array of points by x and y*/
    public static ArrayList<Point> shift(ArrayList<Point> points, int x, int y){
        ArrayList<Point> shifted = new ArrayList<Point>();
        for (Point p : points) {
            shifted.add(new Point(p.x + x, p.y + y));
        }
        return shifted;
    }
    
    /* Calculate distance between Point p and Point q */
    public static double distance(Point p, Point q){
        return Math.sqrt((double)((p.x-q.x) * (p.x-q.x)
                + (p.y-q.y) * (p.y-q.y)));
    }
    
    /* Get a list of points within the radius of the threshold */
    public static ArrayList<Point> getCirlePoints(Point p) {
        ArrayList<Point> circlePoints = new ArrayList<Point>();
        for (Point point : Constants.circleMap) {
            circlePoints.add(new Point(p.x + point.x, p.y + point.y));
        }
        return circlePoints;
    }
    
    /* 
     * Perform linear interpolation between Point p0 and Point p1
     * and return the interpolated points as ArrayList<Point>
     */
    public static ArrayList<Point> interpolate(Point p0, Point p1){
        
        ArrayList<Point> extraPoints = new ArrayList<Point>();
        
        int x0 = p0.x; 
        int y0 = p0.y;
        int x1 = p1.x; 
        int y1 = p1.y;		

        // Check which orientation we should interpolate
        if (Math.abs(x0 - x1) > Math.abs(y0 - y1)) {
            // Interpolate over x
            if (x0 > x1) {
                for (int x = x0 - 1; x > x1; x--) {
                    float y = (float)y0 + ((float)((x - x0) * y1
                            - (x - x0) * y0))/(float)(x1 - x0);
                    Point p = new Point(x, Math.round(y));
                    extraPoints.add(p);
                }
            } else {
                for (int x = x0 + 1; x < x1; x++) {
                    float y = (float)y0 + ((float)((x - x0) * y1
                            - (x - x0) * y0))/(float)(x1 - x0);
                    Point p = new Point(x, Math.round(y));
                    extraPoints.add(p);
                }
            }
        } else {
            // Interpolate over y
            if (y0 > y1) {
                for (int y = y0 - 1; y > y1; y--) {
                    float x = (float)x0 + ((float)((y - y0) * x1
                            - (y - y0) * x0))/(float)(y1 - y0);
                    Point p = new Point(Math.round(x), y);
                    extraPoints.add(p);
                }
            } else {
                for (int y = y0 + 1; y < y1; y++) {
                    float x = (float)x0 + ((float)((y - y0) * x1
                            - (y - y0) * x0))/(float)(y1 - y0);
                    Point p = new Point(Math.round(x), y);
                    extraPoints.add(p);
                }
            }
        }
        
        return extraPoints;
        
    }
    
    /* Smooth the middle point */
    public static Point smoothPoint(Point fp, Point mp, Point lp) {
        int avgX = (lp.x + fp.x) / 2;
        int avgY = (lp.y + fp.y) / 2;
        int newX = Math.round(mp.x + (avgX - mp.x)
                * Constants.SMOOTHNESS);
        int newY = Math.round(mp.y + (avgY - mp.y)
                * Constants.SMOOTHNESS);
        return new Point(newX, newY);
    }
    
    /* Smooth an array of points */
    public static ArrayList<Point> smoothPoints (ArrayList<Point> points) {
        if (points.size() > 2) {
            ArrayList<Point> smoothedPoints = new ArrayList<Point>();
            smoothedPoints.add(points.get(0));
            for (int i = 0; i < points.size() - 2; i++) {
                Point fp = smoothedPoints.get(i);
                Point mp = points.get(i + 1);
                Point lp = points.get(i + 2);
                Point newMP = smoothPoint(fp, mp, lp);
                smoothedPoints.add(newMP);
            }
            smoothedPoints.add(points.get(points.size() - 1));
            return smoothedPoints;
        } else {
            return points;
        }
    }
    
    public static double cosine(Point secondLastPoint, Point lastPoint,
            Point newPoint) {
        Point v1 = new Point(lastPoint.x - secondLastPoint.x,
                lastPoint.y - secondLastPoint.y);
        Point v2 = new Point(lastPoint.x - newPoint.x,
                lastPoint.y - newPoint.y);

        double dotProduct = v1.x * v2.x + v1.y * v2.y;
        double magnitude_v1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
        double magnitude_v2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y);
        double cosine = dotProduct / (magnitude_v1 * magnitude_v2);

        return cosine;
    }
    
    public static ArrayList<Point> rotatePoints(ArrayList<Point> points,
            double cos, int direction) {
        ArrayList<Point> rotatedPoints = new ArrayList<Point>();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            double sin = Math.sqrt(1 - cos * cos) * direction;
            int x = (int)Math.round(p.x * cos - p.y * sin);
            int y = (int)Math.round(p.x * sin + p.y * cos);
            Point newP = new Point(x, y);
            rotatedPoints.add(newP);
        }
        
        Point oldCentroid = new Point(0,0);
        for (Point p : points) {
            oldCentroid.x += p.x;
            oldCentroid.y += p.y;
        }
        oldCentroid.x = oldCentroid.x / points.size();
        oldCentroid.y = oldCentroid.y / points.size();
        
        Point rotatedCentroid = new Point(0,0);
        for (Point p : rotatedPoints) {
            rotatedCentroid.x += p.x;
            rotatedCentroid.y += p.y;
        }
        rotatedCentroid.x = rotatedCentroid.x / rotatedPoints.size();
        rotatedCentroid.y = rotatedCentroid.y / rotatedPoints.size();
        
        for (int i = 0; i < rotatedPoints.size(); i++) {
            Point p = rotatedPoints.get(i);
            int x = p.x - rotatedCentroid.x + oldCentroid.x;
            int y = p.y - rotatedCentroid.y + oldCentroid.y;
            rotatedPoints.set(i, new Point(x, y));
        }
        
        for (int i = 1; i < rotatedPoints.size() - 1; i++) {
            Point lastPoint = rotatedPoints.get(i-1);
            Point currPoint = rotatedPoints.get(i);
            Point nextPoint = rotatedPoints.get(i+1);
            int avgX = (nextPoint.x + lastPoint.x) / 2;
            int avgY = (nextPoint.y + lastPoint.y) / 2;
            int newX = Math.round(currPoint.x + (avgX - currPoint.x)
                    * (float)1);
            int newY = Math.round(currPoint.y + (avgY - currPoint.y)
                    * (float)1);
            rotatedPoints.set(i, new Point(newX, newY));
        }
        
        return rotatedPoints;
    }
    
    public static void save(File file, String type) {
        
        if (type.equals("svg")) {
            ArrayList<Stroke> allStrokes = new ArrayList<Stroke>();
            allStrokes.addAll(ClonePanel.flexibleStrokes);
            allStrokes.addAll(ClonePanel.boundaryStrokes);
            if (!file.getAbsolutePath().endsWith(".svg")) {
                file = new File(file.getAbsoluteFile() + ".svg");
            }
            Utilities.convertToSVGFile(file, allStrokes);
        } else if (type.equals("ic")) {
            Utilities.convertToICFile(file, ClonePanel.boundaryStrokes,
                    ClonePanel.flexibleStrokes);
        } else if (type.equals("jpg")) {
            try {
                BufferedImage img = ScreenImage.createImage(MainFrame.clonePanel);
                ScreenImage.writeImage(img, file.getAbsolutePath());
            } catch (IOException e) {                     
            }
        }

    }
    
    public static void saveAs() {
        
        JFileChooser filechooser = new JFileChooser();
        FileNameExtensionFilter filter1 = new FileNameExtensionFilter("jpg images", "jpg");
        FileNameExtensionFilter filter2 = new FileNameExtensionFilter("svg images", "svg");
        FileNameExtensionFilter filter3 = new FileNameExtensionFilter("ic images", "ic");

        filechooser.setFileFilter(filter1);
        filechooser.setFileFilter(filter2);
        filechooser.setFileFilter(filter3);
        
        int result = filechooser.showSaveDialog(MainFrame.clonePanel);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = filechooser.getSelectedFile();
            if (filechooser.getFileFilter().getDescription().contains("svg")) {
                ArrayList<Stroke> allStrokes = new ArrayList<Stroke>();
                allStrokes.addAll(ClonePanel.flexibleStrokes);
                allStrokes.addAll(ClonePanel.boundaryStrokes);
                if (!file.getAbsolutePath().endsWith(".svg")) {
                    file = new File(file.getAbsoluteFile() + ".svg");
                }
                MainFrame.file = file;
                MainFrame.type = "svg";
                Utilities.convertToSVGFile(file, allStrokes);
            } else if (filechooser.getFileFilter().getDescription().contains("ic")) {
                if (!file.getAbsolutePath().endsWith(".ic")) {
                    file = new File(file.getAbsoluteFile() + ".ic");
                }
                MainFrame.file = file;
                MainFrame.type = "ic";
                Utilities.convertToICFile(file, ClonePanel.boundaryStrokes,
                        ClonePanel.flexibleStrokes);
            } else if (filechooser.getFileFilter().getDescription().contains("jpg")) {
                try {
                    BufferedImage img = ScreenImage.createImage(MainFrame.clonePanel);
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".jpg")) {
                        path = path + ".jpg";
                    }
                    MainFrame.file = new File(path);
                    MainFrame.type = "jpg";
                    ScreenImage.writeImage(img, path);
                } catch (IOException e) {                     
                }
            }
        }
        
    }
    
    public static void loadSVGFile(File selectedFile){
        
        ArrayList<Stroke> strokeList = new ArrayList<Stroke>();

        BufferedReader buffer = null;
        String line;
        try {
            buffer = new BufferedReader(new FileReader(selectedFile));
        } catch(FileNotFoundException exc) {
        }
        try {
            while ((line = buffer.readLine()) != null) {
                //System.out.println(line + "\n ...");
                if (line.startsWith("<polyline points=\"")) {
                    //System.out.println(line);
                    Stroke newStroke;
                    ArrayList<Point> points = new ArrayList<Point>();
                    String[] lineCut = line.split(" ");
                    for (int i = 2; i <lineCut.length-1; i++) {
                        Scanner s = new Scanner(lineCut[i]).useDelimiter(",");
                        points.add(new Point(s.nextInt(),s.nextInt()));
                    }
                    //System.out.println(lineCut[lineCut.length-1]);
                    newStroke = new Stroke(points, false, -1,
                            null, Constants.CURVE);
                    newStroke.setIsBoundary(true);
                    strokeList.add(newStroke);
                }
            }
            //System.out.println("strokes size " + strokeList.size());
            buffer.close();
        } catch (IOException ex) {
        }

        ClonePanel.boundaryStrokes = strokeList;
        
        for (Stroke s : ClonePanel.boundaryStrokes) {
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.probe(new CPoint(p.x, p.y,
                        s.getID(), s.getIsBoundary()));
            }
        }
        
    }
    
    public static void convertToSVGFile(File file, ArrayList<Stroke> strokes){
         
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?> \n");
            writer.write("<svg \nxmlns=\"http://www.w3.org/2000/svg\" \n");
            writer.write("version=\"1.1\"\nwidth=\"1000\" \nheight=\"1000\"> \n");
            for (int i = 0; i <strokes.size(); i++) { 
                writer.write("<polyline points=\" ");
                Stroke curr = strokes.get(i);
                ArrayList<Point> pnt_stroke = curr.getPoints();
                for (int j=0; j <pnt_stroke.size(); j++) { 
                    pnt_stroke.get(j);
                    writer.write(pnt_stroke.get(j).x+ "," +pnt_stroke.get(j).y+" ");
                }
                writer.write("\"\n style=\"fill:none;stroke:black;stroke-width:1\" /> \n");
            }
            writer.write("</svg>");
            writer.close();

        } catch (IOException e) {
        }
       
    }
    
    public static void loadICFile(File selectedFile){
        
        ArrayList<Stroke> bStrokeList = new ArrayList<Stroke>();
        ArrayList<Stroke> fStrokeList = new ArrayList<Stroke>();
                
        BufferedReader buffer = null;
        String line;
        try {
            buffer = new BufferedReader(new FileReader(selectedFile));
        } catch(FileNotFoundException ex) {
        }
        try {
            while ((line = buffer.readLine()) != null) {
                if (line.startsWith("<boundary points=\"")) {
                    Stroke newStroke;
                    ArrayList<Point> points = new ArrayList<Point>();
                    Color color = null;
                    int type = 0;
                    String[] lineCut = line.split(" ");
                    for (int i = 2; i < lineCut.length - 1 ; i++) {
                        Scanner s = new Scanner(lineCut[i]).useDelimiter(",");
                        if (i == 2) {
                            type = s.nextInt();
                            int R = s.nextInt();
                            if (R != 256) {
                                color = new Color(R/255f, s.nextInt()/255f,
                                    s.nextInt()/255f, s.nextInt()/255f);
                            }
                        } else {
                            points.add(new Point(s.nextInt(),s.nextInt()));
                        }
                    }
                    newStroke = new Stroke(points, false, -1, color, type);
                    newStroke.setIsBoundary(true);
                    bStrokeList.add(newStroke);
                } else if (line.startsWith("<flexible points=\"")) {
                    Stroke newStroke;
                    ArrayList<Point> points = new ArrayList<Point>();
                    Color color = null;
                    int type = 0;
                    String[] lineCut = line.split(" ");
                    for (int i = 2; i < lineCut.length - 1; i++) {
                        Scanner s = new Scanner(lineCut[i]).useDelimiter(",");
                        if (i == 2) {
                            type = s.nextInt();
                            int R = s.nextInt();
                            if (R != 256) {
                                color = new Color(R/255f, s.nextInt()/255f,
                                    s.nextInt()/255f, s.nextInt()/255f);
                            }
                        } else {
                            points.add(new Point(s.nextInt(),s.nextInt()));
                        }
                    }
                    newStroke = new Stroke(points, true, -1, color, type);
                    newStroke.setIsBoundary(false);
                    fStrokeList.add(newStroke);
                }
                
            }
            buffer.close();
        } catch (IOException ex) {
        }          
        
        ClonePanel.boundaryStrokes = bStrokeList;
        ClonePanel.flexibleStrokes = fStrokeList;
        
        for (Stroke s : ClonePanel.boundaryStrokes) {
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.probe(new CPoint(p.x, p.y,
                        s.getID(), s.getIsBoundary()));
            }
        }
        
        for (Stroke s : ClonePanel.flexibleStrokes) {
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.probe(new CPoint(p.x, p.y,
                        s.getID(), s.getIsBoundary()));
            }
        }       
    }
    
    public static void convertToICFile(File file, ArrayList<Stroke> bStrokes,
            ArrayList<Stroke> fStrokes){
         
        try {
            FileWriter writer = new FileWriter(file);     
            for (int i = 0; i <bStrokes.size(); i++) { 
                writer.write("<boundary points=\" ");
                Stroke curr = bStrokes.get(i);
                int type = curr.getStrokeType();
                Color color = curr.getPolygonColor();
                if (color == null) {
                    writer.write(type + ",256 ");
                } else {
                    writer.write(type + "," + color.getRed() + ","
                            + color.getGreen() + "," + color.getBlue()
                            + "," + color.getAlpha() + " ");
                }
                ArrayList<Point> pnt_stroke = curr.getPoints();
                for (int j=0; j <pnt_stroke.size(); j++) { 
                    pnt_stroke.get(j);
                    writer.write(pnt_stroke.get(j).x + ","
                            + pnt_stroke.get(j).y + " ");
                }
                writer.write("\"\n");
            }
            for (int i = 0; i <fStrokes.size(); i++) { 
                writer.write("<flexible points=\" ");
                Stroke curr = fStrokes.get(i);
                Color color = curr.getPolygonColor();
                int type = curr.getStrokeType();
                if (color == null) {
                    writer.write(type + ",256 ");
                } else {
                    writer.write(type + "," + color.getRed() + ","
                            + color.getGreen() + "," + color.getBlue()
                            + "," + color.getAlpha() + " ");
                }
                ArrayList<Point> pnt_stroke = curr.getPoints();
                for (int j=0; j <pnt_stroke.size(); j++) { 
                    pnt_stroke.get(j);
                    writer.write(pnt_stroke.get(j).x + ","
                            + pnt_stroke.get(j).y + " ");
                }
                writer.write("\"\n");
            }
            writer.close();

        } catch (IOException e) {
        }
       
    }
    
    public static void open() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter1 =
                new FileNameExtensionFilter("svg images", "svg");
        FileNameExtensionFilter filter2 = 
                new FileNameExtensionFilter("ic images", "ic");
        fileChooser.setFileFilter(filter1);
        fileChooser.setFileFilter(filter2);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            MainFrame.file = fileChooser.getSelectedFile();
            MainFrame.clonePanel.clearPanel();
            if (fileChooser.getFileFilter().getDescription().
                    contains("svg")) {
                MainFrame.type = "svg";
                Utilities.loadSVGFile(MainFrame.file);
            } else if (fileChooser.getFileFilter().getDescription().
                    contains("ic")) {
                MainFrame.type = "ic";
                Utilities.loadICFile(MainFrame.file);
            }
            MainFrame.clonePanel.repaint();
        }
    }
    
    public static ArrayList<Integer> findStroke(Point p, String panelName) {
        
        int rightMax = 0;
        AVLTree pointsTree = new AVLTree();
        ArrayList<Integer> leftIDs = new ArrayList<Integer>();
        ArrayList<Integer> rightIDs = new ArrayList<Integer>();
        ArrayList<Integer> matchedIDs = new ArrayList<Integer>();
        
        if (panelName.equals("Draw Panel")) {
            rightMax = 400;
            pointsTree = Constants.drawPointsTree;
        } else if (panelName.equals("Clone Panel")) {
            rightMax = 600;
            pointsTree = Constants.clonePointsTree;
        }
        
        // Go all the way to the left
        for (int x = p.x; x >= 0; x--) {
            CPoint cp = (CPoint) pointsTree.get(new CPoint(x, p.y, -1, false));
            if (cp != null) {
                leftIDs.add(cp.id);
            }
        }
        
        // Go all the way to the right
        for (int x = p.x + 1; x <= rightMax; x++) {
            CPoint cp = (CPoint) pointsTree.get(new CPoint(x, p.y, -1, false));
            if (cp != null) {
                rightIDs.add(cp.id);
            }
        }
        
        // Compare IDs
        for (int i = 0; i < leftIDs.size(); i++) {
            for (int j = 0; j < rightIDs.size(); j++) {
                if (leftIDs.get(i).equals(rightIDs.get(j))) {
                    matchedIDs.add(leftIDs.get(i));
                }
            }
        }
        
        return matchedIDs;
        
    }
    
    public static ArrayList<Integer> findStrokes(ArrayList<Point> points,
            String panelName) {
        
        AVLTree pointsTree = new AVLTree();
        ArrayList<Integer> IDs = new ArrayList<Integer>();
        
        if (panelName.equals("Draw Panel")) {
            pointsTree = Constants.drawPointsTree;
        } else if (panelName.equals("Clone Panel")) {
            pointsTree = Constants.clonePointsTree;
        }
        
        for (Point p : points) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    CPoint cp = (CPoint) pointsTree.get(new CPoint(p.x + i,
                            p.y + j, -1, false));
                    if (cp != null) {
                        if (!IDs.contains(cp.id)) {
                            IDs.add(cp.id);
                        }
                    }
                }
            }
        }

        return IDs;   
        
    }
    
    public static Cursor getCursor(String type) throws IOException {
        Toolkit toolkit = Toolkit.getDefaultToolkit(); 
        BufferedImage image = ImageIO.read(new File(type + ".png"));
        Point hotSpot = new Point(0,0);
        if (type.equals("paint")) {
            hotSpot = new Point(0, 25);
            Color c = ColorChooser.getInstance().getCurrentColor();
            if (c != null) {
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        if (image.getRGB(i, j) != 0) { 
                            image.setRGB(i, j, c.getRGB());
                        }
                    } 
                }
            }
        }
        Cursor cursor = toolkit.createCustomCursor(image, hotSpot, type);
        return cursor;
    }
     
}