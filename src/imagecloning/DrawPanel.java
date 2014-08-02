package imagecloning;

import MenuToolbar.ColorChooser;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import constants.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import operations.Utilities;
import shapes.Brush;
import shapes.CPoint;
import shapes.Stroke;

public class DrawPanel extends JPanel 
{
    public static int operation = Constants.DRAW;
    
    public static ArrayList<Stroke> strokes = null;
    public static ArrayList<Brush> brushes = null;
    public static ArrayList<Point> brushPoints = null;
    public static ArrayList<Stroke> selectedStrokes = null;
    public static boolean setByDrop = false;
    
    public static Stroke colorStroke = null;
    public static Stroke eraseStroke = null;
    
    public static Point[] brushRectPoints = null;
    public static Rectangle2D brushRectangle = null;
    public static int brushingOperation = Constants.BRUSH;

    public static Stroke stroke = null;
    public static Brush brush = null;
    public static Stroke selectStroke = null;

    public static Brush photoshopBrush = null;
    public Point photoshopStartingPoint = null;

    public DrawPanel () {
        super();
        
        strokes = new ArrayList<Stroke>();
        brushes = new ArrayList<Brush>();
        brushPoints = new ArrayList<Point>();
        selectedStrokes = new ArrayList<Stroke>();
        brushRectPoints = new Point[2];
        
        MouseDispatcher mouseDispatcher = new MouseDispatcher();
        super.addMouseListener(mouseDispatcher);
        super.addMouseMotionListener(mouseDispatcher);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        
        /* Paint background, strokes, and brushes from scratch */
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.white);
        g2D.fillRect(0, 0, super.getWidth(), super.getHeight());
        
        if (strokes.size() > 0) {
            for (Stroke s : strokes) {
                s.paint(g, Color.black, false);
            }
        }
        
        if (brushes.size() > 0) {
            for (Brush b : brushes) {
                b.paint(g);
            }
        }
        
        /* Paint new stroke, brush, or brush rectangle */
        if (operation == Constants.DRAW && stroke != null) {
            stroke.paint(g, Color.black, false);
        }
        if (operation == Constants.BRUSH && brush != null) {
            brush.paint(g);
        }
        if (operation == Constants.BRUSH_RECTANGLE & brushRectangle != null){
            g2D.setColor(Color.black);
            g2D.draw(brushRectangle);
	}
        
         if (operation == Constants.PHOTOSHOP_MODE &&
                photoshopStartingPoint != null) {
            g2D.setColor(Color.blue);
            g2D.drawLine(photoshopStartingPoint.x-10, photoshopStartingPoint.y, 
                    photoshopStartingPoint.x+10, photoshopStartingPoint.y);
            g2D.drawLine(photoshopStartingPoint.x, photoshopStartingPoint.y-10, 
                    photoshopStartingPoint.x, photoshopStartingPoint.y+10);
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
        
        /* Paint new select stroke */
        if (operation == Constants.SELECT && selectStroke != null) {
            selectStroke.paint(g, Color.LIGHT_GRAY, false);
        }
        
        /* Highlight selected stroke */
        if (selectedStrokes != null && selectedStrokes.size() > 0
                && !setByDrop) {
            for (Stroke s : selectedStrokes) {
                s.paint(g, Color.RED, false);
            } 
        }
        
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
        stroke.finishPoint(p, Constants.drawPointsTree, -1);
        super.repaint();
        strokes.add(stroke);
    }
    
    /* - END HERE - Stroke Operations */
    
    /* - START HERE - Brush Operations */
    
    public void start_brush(Point p) {
        brush = new Brush();
        extend_brush(p);
    }
    
    public void extend_brush(Point p) {
        brush.addPoint(p);
        brushPoints.add(p);
        super.repaint();
    }
    
    public void finish_brush(Point p) {
        brush.addPoint(p);
        brushPoints.add(p);
        brushingOperation = Constants.BRUSH;
        super.repaint();
        brushes.add(brush);
    }
    
    /* - END HERE - Brush Operations */
    
    /* - START HERE - Select Operations */
    
    public void start_select(Point p) {
        selectStroke = new Stroke();
        extend_select(p);
    }
    
    public void extend_select(Point p) {
        selectStroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_select(Point p) {
        
        selectStroke.finishPoint(p, null, -1);
        
        ArrayList<Integer> IDs = Utilities.findStrokes(selectStroke.getPoints(),
                "Draw Panel");
        
        selectedStrokes = new ArrayList<Stroke>();
            
        if (IDs.isEmpty()) {
            IDs = Utilities.findStroke(p, "Draw Panel");
            if (!IDs.isEmpty()) {
                for (Stroke s : strokes) {
                    if (IDs.get(0).equals(s.getID())) {
                        selectedStrokes.add(s);
                    }
                }
            }
        } else {
            for (Stroke s : strokes) {
                boolean found = false;
                for (int i = 0; i < IDs.size(); i++) {
                    if (s.getID() == IDs.get(i)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    selectedStrokes.add(s);
                }
            }
        }
        
        if (!selectedStrokes.isEmpty()) {
            setByDrop = false;
        }
        
        selectStroke = null;
        MainFrame.dropPanel.clearSquare();
        MainFrame.dropPanel.repaint();
        super.repaint();

        if (DrawPanel.selectedStrokes.size() > 0) {
            try {
                Main.mainFrame.setCursor(Utilities.getCursor("drag"));
            } catch (IOException ex) {}
            GlassPanel.operation = Constants.DRAG;
            MainFrame.glassPanel.setVisible(true);
        }
		
    }
    
    /* - END HERE - Select Operations */
    
    /** - START HERE - Brush Rectangle Operations **/
    
    public void start_brush_rectangle(Point p) {
        brushRectPoints[0] = p;
    }
	
    public void extend_brush_rectangle(Point p) {
        Point p1 = new Point(Math.min(brushRectPoints[0].x, p.x),
                Math.min(brushRectPoints[0].y, p.y));
        Point p2 = new Point(Math.max(brushRectPoints[0].x, p.x),
                Math.max(brushRectPoints[0].y, p.y));
        Dimension dim = new Dimension(p2.x - p1.x, p2.y - p1.y);
        brushRectangle = new Rectangle2D.Double(p1.x, p1.y,
                dim.width, dim.height);
        super.repaint();
    }
    
    public void finish_brush_rectangle(Point p) {
        Point p1 = new Point(Math.min(brushRectPoints[0].x, p.x),
                Math.min(brushRectPoints[0].y, p.y));
        Point p2 = new Point(Math.max(brushRectPoints[0].x, p.x),
                Math.max(brushRectPoints[0].y, p.y));
        Dimension dim = new Dimension(p2.x - p1.x, p2.y - p1.y);
        brushRectangle = new Rectangle2D.Double(p1.x, p1.y,
                dim.width, dim.height);
        brushingOperation = Constants.BRUSH_RECTANGLE;
        super.repaint();
        setSelectedStrokes();
        if (DrawPanel.selectedStrokes.size() > 0) {
            GlassPanel.operation = Constants.DRAG;
            MainFrame.glassPanel.setVisible(true);
        }	
    }
    
    /** - END HERE - Brush Rectangle Operations **/
    
    /* - START HERE - Drag Operations */
    
    public void start_drag(Point p) {
        ArrayList<Integer> IDs = Utilities.findStroke(p, "Draw Panel");
        if (!IDs.isEmpty()) {
            for (Stroke s : strokes) {
                if (IDs.get(0).equals(s.getID())) {
                    selectedStrokes.add(s);
                    setByDrop = false;
                    GlassPanel.operation = Constants.DRAG;
                    MainFrame.glassPanel.setVisible(true);
                    repaint();
                }
            }
        }
    }
    
    /* - END HERE - Drag Operations */
    
    /* - START HERE - Color Operations */
    
    public void start_color(Point p) {
        colorStroke = new Stroke();
        extend_color(p);
    }
    
    public void extend_color(Point p) {
        colorStroke.addPoint(p);
        super.repaint();
    }
    
    public void finish_color(Point p) {
        
        colorStroke.finishPoint(p, null, -1);
        
        ArrayList<Integer> IDs = Utilities.findStrokes(colorStroke.getPoints(),
                "Draw Panel");
        
        Color newColor = ColorChooser.getInstance().getCurrentColor();
            
        if (IDs.isEmpty()) {
            IDs = Utilities.findStroke(p, "Draw Panel");
            outerloop:
            for (int i = 0; i < IDs.size(); i++) {
                for (Stroke s : strokes) {
                    if (s.getID() == IDs.get(i)
                            && s.getStrokeType() == Constants.POLYGON) {
                        s.setPolygonColor(newColor);
                        break outerloop;
                    }
                }
            }
        } else {
            ArrayList<Stroke> strokesToColor = new ArrayList<>();
            for (int i = 0; i < IDs.size(); i++) {
                for (Stroke s : strokes) {
                    if (s.getID() == IDs.get(i) && s.getStrokeType() == Constants.POLYGON) {
                        strokesToColor.add(s);
                    }
                }
            }  
            
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
        colorStroke = null;
        super.repaint();
    }
    
    /* - END HERE - Color Operations */
    
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
        
        eraseStroke.finishPoint(p, null, -1);
        
        ArrayList<Integer> IDs = Utilities.findStrokes(eraseStroke.getPoints(),
                "Draw Panel");
        
        ArrayList<Stroke> unerasedStrokes = null;
        ArrayList<Stroke> unerasedSelectedStrokes = null;
            
        if (IDs.isEmpty()) {
            IDs = Utilities.findStroke(p, "Draw Panel");
            if (!IDs.isEmpty()) {
                unerasedStrokes = new ArrayList<Stroke>();
                unerasedSelectedStrokes = new ArrayList<Stroke>();
                for (Stroke s : strokes) {
                    if (s.getID() != IDs.get(0)) {
                        unerasedStrokes.add(s);
                    } else {
                        for (Point point : s.getPoints()) {
                            Constants.drawPointsTree.remove(new CPoint(point.x,
                                    point.y, s.getID(), s.getIsBoundary()));
                        }
                    }
                }
                for (Stroke s : selectedStrokes) {
                    if (s.getID() != IDs.get(0)) {
                        unerasedSelectedStrokes.add(s);
                    }
                }
            }
        } else {
            unerasedStrokes = new ArrayList<Stroke>();
            unerasedSelectedStrokes = new ArrayList<Stroke>();
            for (Stroke s : strokes) {
                boolean found = false;
                for (int i = 0; i < IDs.size(); i++) {
                    if (s.getID() == IDs.get(i)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    unerasedStrokes.add(s);
                } else {
                    for (Point point : s.getPoints()) {
                        Constants.drawPointsTree.remove(new CPoint(point.x,
                                point.y, s.getID(), s.getIsBoundary()));
                    }
                }
            }
            for (Stroke s : selectedStrokes) {
                boolean found = false;
                for (int i = 0; i < IDs.size(); i++) {
                    if (s.getID() == IDs.get(i)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    unerasedSelectedStrokes.add(s);
                }
            }
        }
        
        if (unerasedStrokes != null) {
            strokes = unerasedStrokes;
        }
        
        if (unerasedSelectedStrokes != null) {
            selectedStrokes = unerasedSelectedStrokes;
        }
        
        eraseStroke = null;
        super.repaint();
    }
    
    /** - END HERE - Erase Operations **/
    
    /** - START HERE - Photoshop Operations **/
    
    public void start_photoshopMode(Point p) {
        brushingOperation = Constants.BRUSH;
        photoshopStartingPoint = p;
        brush = new Brush();
        super.repaint();
    }
    
   public void extend_photoshopMode(Point dist) {
        Point p = new Point(photoshopStartingPoint.x + dist.x,
					        photoshopStartingPoint.y + dist.y);
        brush.addPoint(p);
        brushPoints.add(p);
        super.repaint();
        brushes.add(brush);
        // here we draw only the part of the strokes under the brush 
        setSelectedStrokesForPhotoshopMode();
    }
    
    public void finish_photoshopMode(Point dist) {
        Point p = new Point(photoshopStartingPoint.x+dist.x, photoshopStartingPoint.y+dist.y);
        brush.addPoint(p);
        brushPoints.add(p);
		brushingOperation = Constants.BRUSH;
        super.repaint();
        brushes.add(brush);
        // draw all the stroke if more than 25% is under the brush area
        setSelectedStrokes();
    }
    
     public static void setSelectedStrokesForPhotoshopMode() {
        ArrayList<Point> oldP;
        ArrayList<Point> newP;
	    for (Stroke s : strokes) {
            boolean isNewStroke = true;
            oldP = (ArrayList<Point>)s.getPoints();
            newP = new ArrayList<Point>();
            int pointInBrushArea = 0;
            for (int i = 0; i < oldP.size(); i++) {
                if (inBrushAreas(oldP.get(i)))
                {
                    pointInBrushArea++;
                }
                if (inBrushAreas(oldP.get(i))) {
                    if (isNewStroke) {
                        isNewStroke = false;
                        newP.clear(); 
                    } 
                    newP.add(new Point(oldP.get(i).x, oldP.get(i).y));
                    if (i == oldP.size() - 1) {
                        Stroke newStroke = new Stroke(newP, false, -1,
                                null, Constants.CURVE);
                        selectedStrokes.add(newStroke);
                        setByDrop = false;
                    }
                    /*// try to select all the stroke when at list 25% of its points are in the brush area
                    if(i >= (int)(0.25*oldP.size())){
                        for (int j = i; j < oldP.size(); j++)
                        {
                            System.out.println("ici");
                            newP.add(new Point(oldP.get(j).x, oldP.get(j).y));
                            
                        }
                        Stroke newStroke = new Stroke(newP);
                        selectedStrokes.add(newStroke);
                        break;
                    }*/
                } else {
                    isNewStroke = true;
                    if (newP.size() > 0) {
                        Stroke newStroke = new Stroke((ArrayList<Point>)newP.clone(),
                                false, -1, null, Constants.CURVE);
                        selectedStrokes.add(newStroke);
                        setByDrop = false;
                        newP.clear();
                    }
                }
            }
            
            
        }
    }    
    /** - END HERE - Photoshop Operations **/
    
    /* - START HERE - Clone Operations */

    public static boolean inBrushAreas(Point point) {
        if(brushingOperation == Constants.BRUSH) {
            for (Point p : brushPoints) {
                double distance = Math.hypot((double)(point.x - p.x),
                        (double)(point.y - p.y));
                if (distance <= (Brush.d / 2)) return true;
            }
        } else if (brushingOperation == Constants.BRUSH_RECTANGLE){
            if (brushRectangle.contains(point.x, point.y)) return true;
        }
        return false;
    }

    public static void setSelectedStrokes() {
        // Treat each stroke as atomic
        for (Stroke s : strokes) {
            int pointsInBrushArea = 0;
            for (Point p : s.getPoints()) {
                if (inBrushAreas(p)) {
                    pointsInBrushArea++;
                }
            }
            if (pointsInBrushArea >= (Constants.SELECTION_PERCENT
                    * s.getPoints().size())) {
                selectedStrokes.add(s);
                setByDrop = false;
            } 
        }
    }
    
    /* - END HERE - Clone Operations */
    
    /* Customized MouseAdapter class */
    public class MouseDispatcher extends MouseAdapter
        implements MouseMotionListener{

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) {
                if (operation == Constants.DRAW) {
                    start_stroke(e.getPoint());
                } else if (operation == Constants.BRUSH) {
                    start_brush(e.getPoint());
                } else if (operation == Constants.BRUSH_RECTANGLE) {
                    start_brush_rectangle(e.getPoint());
                } else if (operation == Constants.DRAG) {
                    start_drag(e.getPoint());
                } else if (operation == Constants.PHOTOSHOP_MODE) {
                    start_photoshopMode(e.getPoint());
                } else if (operation == Constants.COLOR) {
                    start_color(e.getPoint());
                } else if (operation == Constants.ERASE) {
                    start_erase(e.getPoint());
                } else if (operation == Constants.SELECT) {
                    start_select(e.getPoint());
                }
            } else if (e.getButton() == 3) {
                operation = Constants.SELECT;
                start_select(e.getPoint());
            } 
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (operation == Constants.DRAW) {
                extend_stroke(e.getPoint());
            } else if (operation == Constants.BRUSH) {
                extend_brush(e.getPoint());
            } else if (operation == Constants.BRUSH_RECTANGLE){
		extend_brush_rectangle(e.getPoint());
            } else if (operation == Constants.COLOR) {
                extend_color(e.getPoint());
            } else if (operation == Constants.ERASE) {
                extend_erase(e.getPoint());
            } else if (operation == Constants.SELECT) {
                extend_select(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (operation == Constants.DRAW) {
                finish_stroke(e.getPoint());
            } else if (operation == Constants.BRUSH) {
                finish_brush(e.getPoint());
            } else if (operation == Constants.BRUSH_RECTANGLE){
                finish_brush_rectangle(e.getPoint());
            } else if (operation == Constants.COLOR) {
                finish_color(e.getPoint());
            } else if (operation == Constants.ERASE) {
                finish_erase(e.getPoint());
            } else if (operation == Constants.SELECT) {
                finish_select(e.getPoint());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e){}
    }
}
