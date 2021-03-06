package imagecloning;

import MenuToolbar.ColorChooser;

import constants.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import javax.swing.JPanel;
import operations.Grow;
import operations.ScreenImage;
import operations.UndoManager;
import operations.Utilities;
import shapes.Brush;
import shapes.CPoint;
import shapes.Stroke;

public class ClonePanel extends JPanel 
{
    public static int operation = Constants.DRAW;
    public static Stroke stroke = null;
    
    public static Stroke guidedStroke = null;
    public static Stroke hiddenGuideStroke = null;
    public static ArrayList<Stroke> selectedStrokesClone = null;
    private static int strokeNumber;
    public static int guidedOverlapFactor = 50; //spacing factor between strokes along guided line
    public static int oldStrokeNumber;
    
    public static Stroke eraseStroke = null;
    public static Stroke colorStroke = null;

    public static ArrayList<Stroke> boundaryStrokes = null;
    public static ArrayList<Stroke> flexibleStrokes = null;
    public static ArrayList<Stroke> guidedStrokes = null;
    // This is used to restore the flexibleStrokes to its old state
    // when the user changes the frequency of guided strokes
    public static ArrayList<Stroke> flexibleStrokesClone = null;
    // This is used to restore the pointTree to its old state
    // when the user changes the frequency of guided strokes
    public static ArrayList<Integer> intersectedStrokeIDs = null;
    
    // For displaying image
    public float imageAlpha;
    public BufferedImage image;
    public BufferedImage cloneImage; //For improving efficience
    
    // For photoshop
    private boolean start_photoshopMode = false;
    public static ArrayList<Stroke> photoshopStrokes = new ArrayList<Stroke>();
    private Point photoshopStartBrushing = new Point(-1, -1);
   
    public boolean useImage = false;	
    
    private Color bgColor = Color.white;
    
    public static int state;
    
    public ClonePanel () {
        super();
        
        boundaryStrokes = new ArrayList<Stroke>();
        flexibleStrokes = new ArrayList<Stroke>();
        guidedStrokes = new ArrayList<Stroke>();
                
        imageAlpha = 0.5f;
        
        MouseDispatcher mouseDispatcher = new MouseDispatcher();
        super.addMouseListener(mouseDispatcher);
        super.addMouseMotionListener(mouseDispatcher);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        
        /* Paint background, strokes, and brushes from scratch */
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(bgColor);
        g2D.fillRect(0, 0, super.getWidth(), super.getHeight());
        
        if (GlassPanel.stage == Constants.STAGE_DRAG || 
                GlassPanel.stage == Constants.STAGE_SURFACE || useImage) {
            g.drawImage(cloneImage, 0, 0, this);

        } else {
            /* START - Resize and Paint Transparent Background Image */
            if (image != null && imageAlpha > 0) {
                // Resieze the image if it's too big
                int h = image.getHeight();
                int w = image.getWidth();
                if (h > 500 || w > 600) {
                    int newH, newW;
                    if (h / (float)500> w / (float)600){
                            newH = 500;
                            newW = Math.round(w / (float)h * newH);
                    } else {
                            newW = 600;
                            newH = Math.round(h / (float)w * newW);
                    }
                    int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
                    BufferedImage resizedImage = new BufferedImage(newW, newH, type);
                    Graphics2D resizeG = resizedImage.createGraphics();
                    resizeG.drawImage(image, 0, 0, newW, newH, null);
                    image = resizedImage;
                } else {
                    
                }

                // Create an image that is transparent based on the original image
                BufferedImage bi = new BufferedImage(w, h,
                        BufferedImage.TYPE_INT_ARGB);
                Graphics alphaG = bi.getGraphics();
                alphaG.drawImage(image, 0, 0, null);
                float[] scales = { 1f, 1f, 1f, (float)imageAlpha };
                float[] offsets = new float[4];
                RescaleOp rop = new RescaleOp(scales, offsets, null);
                g2D.drawImage(bi, rop, 10, 18);
            }
            /* END - Resize and Paint Transparent Background Image */

            if (boundaryStrokes.size() > 0) {
                for (Stroke s : boundaryStrokes) {
                    s.paint(g, Color.black, false);
                }
            }

            if (flexibleStrokes.size() > 0) {
                for (Stroke s : flexibleStrokes) {
                    s.paint(g, Color.black, false);
                }
            }

            if (photoshopStrokes.size() > 0){
                    for (Stroke s:photoshopStrokes){
                        s.paint(g, Color.red, false);
                    }
            }
        }

        /* Paint new stroke */
        if (operation == Constants.DRAW && stroke != null) {
                stroke.paint(g, Color.black, false);
        }

        /* Paint new guided stroke */
        if (operation == Constants.DRAW_GUIDE && guidedStroke != null) {
            guidedStroke.paint(g, Color.red, false);
        }
        
        /* Paint new coloring stroke */
        if (operation == Constants.COLOR && colorStroke != null) {
            Color newColor = ColorChooser.getInstance().getCurrentColor();
            colorStroke.paint(g, newColor, false);
        }
        
        /* Paint new erase stroke */
        if (operation == Constants.ERASE && eraseStroke != null) {
            eraseStroke.paint(g, Color.CYAN, false);
        }
		
    }
    
    public void clearPanel() {
        for (Stroke s : boundaryStrokes) {
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.remove(new CPoint(p.x, p.y, s.getID(),
                        s.getIsBoundary()));
            }
        }
        boundaryStrokes.clear();
        for (Stroke s : flexibleStrokes) {
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.remove(new CPoint(p.x, p.y, s.getID(),
                        s.getIsBoundary()));
            }
        }
        flexibleStrokes.clear();
    }
    
    /* - START HERE - Stroke Operations */
    
    public void start_stroke(Point p) {
        stroke = new Stroke();
        extend_stroke(p);
    }
    
    public void extend_stroke(Point p) {
        stroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_stroke(Point p) {
        UndoManager.saveState();
        //boundary strokes are not polygons
        stroke.finishPoint(p, Constants.clonePointsTree, Constants.CURVE);
        boundaryStrokes.add(stroke);
        ArrayList<Stroke> singleStroke = new ArrayList<Stroke>();
        singleStroke.add(stroke);
        // Grow method is called here to dynamically deforme
        // other flexible strokes intersecting the new stroke drawn
        Grow.growStrokes(singleStroke, true);
        stroke = null;
        super.repaint();
        
    }
    
    /* - END HERE - Stroke Operations */
    
    /* - START HERE - Coloring Operations */
    
    public void start_color(Point p) {
        colorStroke = new Stroke();
        extend_color(p);
    }
    
    public void extend_color(Point p) {
        colorStroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_color(Point p) {
        UndoManager.saveState();
        colorStroke.finishPoint(p, null, Constants.CURVE);
        
        ArrayList<Integer> IDs = Utilities.findStrokes(colorStroke.getPoints(),
                "Clone Panel");
        
        Color newColor = ColorChooser.getInstance().getCurrentColor();
        ArrayList<Stroke> allStrokes = new ArrayList<Stroke>();
        allStrokes.addAll(boundaryStrokes);
        allStrokes.addAll(flexibleStrokes);
            
        boolean setBackgroundColor = true;
            
        if (IDs.isEmpty()) {
            IDs = Utilities.findStroke(p, "Clone Panel");
            outerloop:           
            for (int i = 0; i < IDs.size(); i++) {
                for (Stroke s : allStrokes) {
                    if (s.getID() == IDs.get(i)
                            && s.getStrokeType() == Constants.POLYGON) {
                        s.setPolygonColor(newColor);
                        setBackgroundColor = false;
                        break outerloop;
                    }
                }
            }
        } else {
            ArrayList<Stroke> strokesToColor = new ArrayList<>();
            for (int i = 0; i < IDs.size(); i++) {
                for (Stroke s : allStrokes) {
                    if (s.getID() == IDs.get(i) && s.getStrokeType() == Constants.POLYGON) {
                        strokesToColor.add(s);
                    }
                }
            }     
            
            if (strokesToColor.size()>0) setBackgroundColor = false;
            
            int R = newColor.getRed();
            int G = newColor.getGreen();
            int B = newColor.getBlue();
            double colorStep;
            if (Constants.COLOR_GRADIENT < 0) {
                int maxColor = Math.max(Math.max(R, G), B);
                int difference = maxColor;
                colorStep =  difference / strokesToColor.size(); 
            } else if (Constants.COLOR_GRADIENT == 0) {
                colorStep = 0;
            } else {
                int minColor = Math.min(Math.min(R, G), B);
                int difference = 255 - minColor;
                colorStep = difference / strokesToColor.size(); 
            }
            colorStep *= Constants.COLOR_GRADIENT/100.0;
            int stepIndex = 0;
            for (Stroke s : strokesToColor) {                
                int newR = (int) Math.round(R + stepIndex * colorStep);
                int newG = (int) Math.round(G + stepIndex * colorStep);
                int newB = (int) Math.round(B + stepIndex * colorStep);
                
                if (newR < 0) newR = 0;                
                if (newG < 0) newG = 0;                
                if (newB < 0) newB = 0;
                
                if (newR > 255) newR = 255;                
                if (newG > 255) newG = 255;                
                if (newB > 255) newB = 255;
                
                Color gColor = new Color(newR / 255f, newG / 255f,
                        newB / 255f, newColor.getAlpha() / 255f);
                s.setPolygonColor(gColor);
                stepIndex ++;
            }
        }

        if (setBackgroundColor) {
            bgColor = newColor;
        }
        colorStroke = null;
        super.repaint();
    }
    
    /* - END HERE - Coloring Operations */
    
    /* - BEGIN HERE - Photoshop Operations */	
    public void start_photoshopBrush(Point p)  {
        /*
         * Assert that the user has clicked on the draw panel first
         */
         if (MainFrame.drawPanel.photoshopStartingPoint == null)
         {
             System.out.println("You have to clik on the right panel first");
             start_photoshopMode = false;
         } else {
             start_photoshopMode = true;
         }
         photoshopStartBrushing = p;
         extend_photoshopBrush(p);
    }            
    
    public void extend_photoshopBrush(Point p) {
        if (start_photoshopMode){
            Point dist = new Point ();
            dist.x = p.x-photoshopStartBrushing.x;
            dist.y = p.y-photoshopStartBrushing.y;
            // we capture the strokes that are under the brushing Area
            MainFrame.drawPanel.extend_photoshopMode(dist);
            
            Point startPoint = MainFrame.drawPanel.photoshopStartingPoint;
            int x = photoshopStartBrushing.x-startPoint.x;
            int y = photoshopStartBrushing.y-startPoint.y;
                
            for (Stroke s: DrawPanel.selectedStrokes)
            {
                 ArrayList<Point> newP;
                 newP = Utilities.transform(s.getPoints(),MainFrame.drawPanel,
                         MainFrame.clonePanel);
                 newP = Utilities.shift(s.getPoints(), x, y);
                 Stroke newS = new Stroke(newP, true, -1, s.getPolygonColor(),
                         s.getStrokeType());
                 if (photoshopStrokes == null )
                 { 
                     photoshopStrokes = new ArrayList<Stroke> ();
                 }
                 photoshopStrokes.add(newS);
            } 
        
		DrawPanel.selectedStrokes.clear();
        }
    } 
   
    public void finish_photoshopBrush(Point p){
        UndoManager.saveState();
        Point dist = new Point ();
        dist.x = p.x-photoshopStartBrushing.x;
        dist.y = p.y-photoshopStartBrushing.y;
        MainFrame.drawPanel.finish_photoshopMode(dist);

        if (photoshopStrokes != null) photoshopStrokes.clear();

        Point startPoint = MainFrame.drawPanel.photoshopStartingPoint;
        int x = photoshopStartBrushing.x-startPoint.x;
        int y = photoshopStartBrushing.y-startPoint.y;
        
        ArrayList<Stroke> newStrokes = new ArrayList<Stroke>();
        for (Stroke s : DrawPanel.selectedStrokes) { 
			//System.out.println("FUCK");
            ArrayList<Point> newP;
            newP = Utilities.transform(s.getPoints(),MainFrame.drawPanel, MainFrame.clonePanel);
            newP = Utilities.shift(s.getPoints(), x, y);
            Stroke newS = new Stroke(newP, true, -1, s.getPolygonColor(), s.getStrokeType());
            newStrokes.add(newS);
        }
        
        if (state == Constants.FLEXIBLE_TO_FLEXIBLE) {
			if(!newStrokes.isEmpty())
				Grow.growStrokes(newStrokes, true);
        } else if (state == Constants.FLEXIBLE_TO_SOLID) {
            if (!newStrokes.isEmpty())
				Grow.growStrokes(newStrokes, false);
        } else if (state == Constants.SOLID_TO_SOLID) {
            for (Stroke st : newStrokes) {
				Grow.growSolidStrokesKaran(st);
			}
	
        }
         
        DrawPanel.brush = new Brush();
		DrawPanel.brushes = new ArrayList<Brush>();
		DrawPanel.brushPoints = new ArrayList<Point>();
        DrawPanel.brushRectangle = null;
        DrawPanel.brushRectPoints = new Point[2];
        MainFrame.drawPanel.repaint();
    }
    /* - END HERE - Photoshop Operations */
    
    /* - START HERE - Guide Operations */
    
    public void start_guide(Point p) {
        guidedStroke = new Stroke();
        extend_guide(p);
    }
    
    public void extend_guide(Point p) {
        guidedStroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_guide(Point p) {
        UndoManager.saveState();
        guidedStroke.finishPoint(p, null, Constants.CURVE);
            
        // Save the current state before regrowth
        selectedStrokesClone = DrawPanel.selectedStrokes;
        flexibleStrokesClone = flexibleStrokes;
        
        //Convert all selected strokes to clone space
        //Calculate average size of strokes selected
        ArrayList<Stroke> copiedStrokes = new ArrayList<>();
        double averageDiameter = 0;
        for (Stroke s: DrawPanel.selectedStrokes) {
            Stroke temps = new Stroke(s.getPoints(), false, -1,
                s.getPolygonColor(), s.getStrokeType());
            copiedStrokes.add(temps);
            Dimension dim = temps.getDimension();
            double diameter = Math.sqrt(Math.pow(dim.width, 2) + Math.pow(dim.height, 2));            
            averageDiameter += diameter;
        }
        averageDiameter /= copiedStrokes.size();
        
        //Set how far apart each stroke is to each other
        double factor = guidedOverlapFactor/100.0;
        int frequency = (int) (averageDiameter * factor);       
        oldStrokeNumber = 0;
        guidedStrokes.clear();
               
        for (int i = 0; i < guidedStroke.getPoints().size(); i += frequency) 
        {           
            // Randomely selected a stroke
            int index = (int)Math.round(Math.random() * (copiedStrokes.size() - 1));            
            Stroke centroidS = copiedStrokes.get(index);
            Point centroid = centroidS.getCentroid();

            Point point = guidedStroke.getPoints().get(i);
            
            // Shift points
            ArrayList<Point> shiftedP = Utilities.shift(centroidS.getPoints(), 
                                                        point.x - centroid.x, 
                                                        point.y - centroid.y);
            
            // Rotate points
            ArrayList<Point> rotatedP = null;
            Point lastPoint = new Point(point.x, point.y - 10);
            Point nextPoint = null;
            if (i + frequency > guidedStroke.getPoints().size() - 1) {
                nextPoint = guidedStroke.getPoints().get(guidedStroke
                        .getPoints().size() - 1);
            } else {
                nextPoint = guidedStroke.getPoints().get(i + frequency);
            }
            if (nextPoint.x != point.x || nextPoint.y != point.y) {
                double cos = Utilities.cosine(lastPoint, point, nextPoint);
                if (nextPoint.x > point.x) {
                    rotatedP = Utilities.rotatePoints(shiftedP, cos, 1);
                } else {
                    rotatedP = Utilities.rotatePoints(shiftedP, cos, -1);
                }
                Stroke rotatedS = new Stroke(rotatedP, true, -1,
                        centroidS.getPolygonColor(), centroidS.getStrokeType());
                guidedStrokes.add(rotatedS);
            } else {
                Stroke shiftedS = new Stroke(shiftedP, true, -1,
                        centroidS.getPolygonColor(), centroidS.getStrokeType());
                guidedStrokes.add(shiftedS);
            }
            
            oldStrokeNumber++;
        } 
        Grow.growStrokes(guidedStrokes, true);

        super.repaint();
        hiddenGuideStroke = new Stroke(guidedStroke.getPoints(), false, -1,
                guidedStroke.getPolygonColor(), guidedStroke.getStrokeType());
        guidedStroke = null;        
        
        DrawPanel.brush = new Brush();
	DrawPanel.brushes = new ArrayList<Brush>();
	DrawPanel.brushPoints = new ArrayList<Point>();
        DrawPanel.brushRectangle = null;
        DrawPanel.brushRectPoints = new Point[2];
        MainFrame.drawPanel.repaint();
    }
    
    synchronized public void again_guide() {
        
        oldStrokeNumber = oldStrokeNumber - Grow.notGrown;
        
        // Remove the old strokes already drawn
        for (int i = 0; i < oldStrokeNumber; i++) {
            Stroke s = flexibleStrokes.get(flexibleStrokes.size() - 1);
            for (Point p : s.getPoints()) {
                Constants.clonePointsTree.remove(new CPoint(p.x, p.y,
                        s.getID(), s.getIsBoundary()));
            }
            flexibleStrokes.remove(flexibleStrokes.size() - 1);
        }

        // Restore the previous state
        if (intersectedStrokeIDs != null) {
            for (Stroke s : flexibleStrokes) {
                if (intersectedStrokeIDs.contains(s.getID())) {
                    for (Point p : s.getPoints()) {
                        Constants.clonePointsTree.remove(new CPoint(p.x, p.y,
                                s.getID(), s.getIsBoundary()));
                    }
                }
            }
            for (Stroke s : flexibleStrokesClone) {
                if (intersectedStrokeIDs.contains(s.getID())) {
                    for (Point p : s.getPoints()) {
                        Constants.clonePointsTree.probe(new CPoint(p.x, p.y,
                                s.getID(), s.getIsBoundary()));
                    }
                }
            }
        }
        flexibleStrokes = flexibleStrokesClone;
        
        //Convert all selected strokes to clone space
        //Calculate average size of strokes selected
        ArrayList<Stroke> copiedStrokes = new ArrayList<>();
        double averageDiameter = 0;
        for (Stroke s: selectedStrokesClone) {
            Stroke temps = new Stroke(s.getPoints(), false, -1,
                s.getPolygonColor(), s.getStrokeType());
            copiedStrokes.add(temps);
            Dimension dim = temps.getDimension();
            double diameter = Math.sqrt(Math.pow(dim.width, 2) + Math.pow(dim.height, 2));            
            averageDiameter += diameter;
        }
        averageDiameter /= copiedStrokes.size();
        
        //Set how far apart each stroke is to each other
        double factor = guidedOverlapFactor/100.0;
        int frequency = (int) (averageDiameter * factor);       

        guidedStrokes.clear();
          oldStrokeNumber = 0;  
           
        for (int i = 0; i < hiddenGuideStroke.getPoints().size(); i += frequency) {
            // Randomely selected a stroke
            int index = (int)Math.round(Math.random() * (copiedStrokes.size() - 1));            
            Stroke centroidS = copiedStrokes.get(index);
            Point centroid = centroidS.getCentroid();

            Point point = hiddenGuideStroke.getPoints().get(i);
            
            // Shift points
            ArrayList<Point> shiftedP = Utilities.shift(centroidS.getPoints(),
                    point.x - centroid.x, point.y - centroid.y);
            // Rotate points
            ArrayList<Point> rotatedP = null;
            Point lastPoint = new Point(point.x, point.y - 10);
            Point nextPoint = null;
            if (i + frequency > hiddenGuideStroke.getPoints().size() - 1) {
                nextPoint = hiddenGuideStroke.getPoints().get(hiddenGuideStroke
                        .getPoints().size() - 1);
            } else {
                nextPoint = hiddenGuideStroke.getPoints().get(i + frequency);
            }
            if (nextPoint.x != point.x || nextPoint.y != point.y) {
                double cos = Utilities.cosine(lastPoint, point, nextPoint);
                if (nextPoint.x > point.x) {
                    rotatedP = Utilities.rotatePoints(shiftedP, cos, 1);
                } else {
                    rotatedP = Utilities.rotatePoints(shiftedP, cos, -1);
                }
                Stroke rotatedS = new Stroke(rotatedP, true, -1,
                        centroidS.getPolygonColor(), centroidS.getStrokeType());
                guidedStrokes.add(rotatedS);
            } else {
                Stroke shiftedS = new Stroke(shiftedP, true, -1,
                        centroidS.getPolygonColor(), centroidS.getStrokeType());
                guidedStrokes.add(shiftedS);
            }
            oldStrokeNumber++;
        }

        Grow.growStrokes(guidedStrokes, true);       
        
        super.repaint();

    }
    
    /* - END HERE - Guide Operations */
    
    /* - START HERE - Erase Operations */
    
    public void start_erase(Point p) {
        eraseStroke = new Stroke();
        extend_erase(p);
    }
    
    public void extend_erase(Point p) {
        eraseStroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_erase(Point p) {
        UndoManager.saveState();
        eraseStroke.finishPoint(p, null, Constants.CURVE);
        
        ArrayList<Integer> IDs = Utilities.findStrokes(eraseStroke.getPoints(),
                "Clone Panel");
        
        ArrayList<Stroke> unerasedBStrokes = null;
        ArrayList<Stroke> unerasedFStrokes = null;
        // This is to keep track of how many
        // newly drawn guided strokes are erased
        ArrayList<Stroke> removedNewFStrokes = null;
        
            
        if (IDs.isEmpty()) {
            IDs = Utilities.findStroke(p, "Clone Panel");
            if (!IDs.isEmpty()) {
                unerasedBStrokes = new ArrayList<Stroke>();
                unerasedFStrokes = new ArrayList<Stroke>();
                removedNewFStrokes = new ArrayList<Stroke>();
                boolean foundB = false;
                boolean foundF = false;
                for (Stroke s : boundaryStrokes) {
                    if (s.getID() != IDs.get(0)) {
                        unerasedBStrokes.add(s);
                    } else {
                        foundB = true;
                        for (Point point : s.getPoints()) {
                            Constants.clonePointsTree.remove(new CPoint(point.x,
                                    point.y, s.getID(), s.getIsBoundary()));
                        }
                    }
                }
                for (int i = 0; i < flexibleStrokes.size(); i++) {
                    Stroke s = flexibleStrokes.get(i);
                    if (s.getID() != IDs.get(0)) {
                        unerasedFStrokes.add(s);
                    } else {
                        foundF = true;
                        for (Point point : s.getPoints()) {
                            Constants.clonePointsTree.remove(new CPoint(point.x,
                                    point.y, s.getID(), s.getIsBoundary()));
                        }
                        if (i >= flexibleStrokes.size() - oldStrokeNumber) {
                            if (!removedNewFStrokes.contains(s)){
                                removedNewFStrokes.add(s);
                            }
                        }
                    }
                }
                if (!foundB) {
                    unerasedBStrokes = null;
                }
                if (!foundF) {
                    unerasedFStrokes = null;
                }
            }
        } else {
            unerasedBStrokes = new ArrayList<Stroke>();
            unerasedFStrokes = new ArrayList<Stroke>();
            removedNewFStrokes = new ArrayList<Stroke>();
            boolean foundB = false;
            boolean foundF = false;
            for (Stroke s : boundaryStrokes) {
                boolean found = false;
                for (int i = 0; i < IDs.size(); i++) {
                    if (s.getID() == IDs.get(i)) {
                        found = true;
                        foundB = true;
                        break;
                    }
                }
                if (!found) {
                    unerasedBStrokes.add(s);
                } else {
                    for (Point point : s.getPoints()) {
                        Constants.clonePointsTree.remove(new CPoint(point.x,
                                point.y, s.getID(), s.getIsBoundary()));
                    }
                }
            }
            for (int i = 0; i < flexibleStrokes.size(); i++) {
                Stroke s = flexibleStrokes.get(i);
                boolean found = false;
                for (int j = 0; j < IDs.size(); j++) {
                    if (s.getID() == IDs.get(j)) {
                        found = true;
                        foundF = true;
                        break;
                    }
                }
                if (!found) {
                    unerasedFStrokes.add(s);
                } else {
                    for (Point point : s.getPoints()) {
                        Constants.clonePointsTree.remove(new CPoint(point.x,
                                point.y, s.getID(), s.getIsBoundary()));
                        if (i >= flexibleStrokes.size() - oldStrokeNumber) {
                            if (!removedNewFStrokes.contains(s)){
                                removedNewFStrokes.add(s);
                            }
                        }
                    }
                }
            }
            if (!foundB) {
                unerasedBStrokes = null;
            }
            if (!foundF) {
                unerasedFStrokes = null;
            }
        }
        
        if (unerasedBStrokes != null) {
            boundaryStrokes = unerasedBStrokes;
        }
        if (unerasedFStrokes != null) {
            flexibleStrokes = unerasedFStrokes;
        }
        
        if (removedNewFStrokes != null) {
            oldStrokeNumber = oldStrokeNumber - removedNewFStrokes.size();
        }
        flexibleStrokesClone = flexibleStrokes;
        
        eraseStroke = null;
        super.repaint();
    }
    
    /** - END HERE - Erase Operations **/
    
    /* Customized MouseAdapter class */
    public class MouseDispatcher extends MouseAdapter
        implements MouseMotionListener{

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {

            MainFrame.clonePanel.cloneImage =
                    ScreenImage.createImage(MainFrame.clonePanel);

            if (operation == Constants.DRAW) {
                start_stroke(e.getPoint());
                useImage = true;
            } else if (operation == Constants.PHOTOSHOP_MODE ) {
               start_photoshopBrush(e.getPoint());
                useImage = true;
            } else if (operation == Constants.DRAW_GUIDE) {
                start_guide(e.getPoint());
                useImage = true;
            } else if (operation == Constants.COLOR) {
                start_color(e.getPoint());
                useImage = true;
            } else if (operation == Constants.ERASE) {
                start_erase(e.getPoint());
                useImage = true;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (operation == Constants.DRAW) {
                extend_stroke(e.getPoint());
            } else if (operation == Constants.PHOTOSHOP_MODE){
                extend_photoshopBrush(e.getPoint());
                repaint();
            } else if (operation == Constants.DRAW_GUIDE) {
                extend_guide(e.getPoint());
            } else if (operation == Constants.COLOR) {
                extend_color(e.getPoint());
            } else if (operation == Constants.ERASE) {
                extend_erase(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (operation == Constants.DRAW) {
                finish_stroke(e.getPoint());
                useImage = false;
            } else if (operation == Constants.PHOTOSHOP_MODE){
                finish_photoshopBrush(e.getPoint());
		useImage = false;
                repaint();
            } else if (operation == Constants.DRAW_GUIDE) {
                finish_guide(e.getPoint());
		useImage = false;
            } else if (operation == Constants.COLOR) {
                finish_color(e.getPoint());
                useImage = false;
            } else if (operation == Constants.ERASE) {
                finish_erase(e.getPoint());
                useImage = false;
            }
			
        }

        @Override
        public void mouseClicked(MouseEvent e){}
    }
    
}
