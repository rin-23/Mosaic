package imagecloning;

import operations.UndoManager;
import constants.Constants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import operations.Grow;
import operations.ScreenImage;
import operations.Utilities;
import shapes.Brush;
import shapes.Stroke;

public class GlassPanel extends JComponent {
    
    public static int operation = Constants.NONE;
    public static int stage = Constants.NONE;
    public static ArrayList<Stroke> strokes = null;
    public static Point dragP = null;
    public static int state;
    
    public GlassPanel () {
        super();
        strokes = new ArrayList<Stroke>();
        MouseDispatcher mouseDispatcher = new MouseDispatcher();
        super.addMouseListener(mouseDispatcher);
        super.addMouseMotionListener(mouseDispatcher);
    }
    
    @Override
    public void paintComponent(Graphics g) {       
        if (operation == Constants.DRAG) {
            if (stage == Constants.STAGE_SURFACE) {
                for (Stroke s : strokes) {
                    s.paint(g, Color.red, true);
                }
            } else if (stage == Constants.STAGE_DRAG) {
                for (Stroke s : strokes) {
                    s.paint(g, Color.red, true);
                }
            } else if (stage == Constants.STAGE_SINK) {
                sinkStrokes();
                clearPoints();
            }
        }
    }
    
    public void surfaceStrokes() {
        JComponent from = MainFrame.drawPanel;
        if (DrawPanel.setByDrop) {
            from = MainFrame.dropPanel;
        }
        
        for (Stroke s : DrawPanel.selectedStrokes) {
            ArrayList<Point> newP = Utilities.transform(s.getPoints(),
                    from, MainFrame.glassPanel);
            Stroke newS = new Stroke(newP, false, -1, s.getPolygonColor(),
                    s.getStrokeType());
            strokes.add(newS);
        }
    }
    
    public void shiftStrokes(int x, int y) {
        ArrayList<Stroke> newStrokesClone = new ArrayList<Stroke>();
        for (Stroke s : strokes) {
            ArrayList<Point> newP = Utilities.shift(s.getPoints(), x, y);
            Stroke newS = new Stroke(newP, false, -1, s.getPolygonColor(),
                        s.getStrokeType());
            newStrokesClone.add(newS);
        }
        strokes = newStrokesClone;
    }
    
    public void sinkStrokes() {
        UndoManager.saveState();
        ArrayList<Stroke> newStrokesClone = new ArrayList<Stroke>();
        
        Point mouseDrop = SwingUtilities.convertPoint(MainFrame.glassPanel,
                dragP, MainFrame.dropPanel);
        
        Point mouseClone = SwingUtilities.convertPoint(MainFrame.glassPanel,
                dragP, MainFrame.clonePanel);
        
        if (mouseDrop.x >= 0 && mouseDrop.x <= 100 && mouseDrop.y >= 0) {
            for (Stroke s : strokes) {
                ArrayList<Point> newP = Utilities.transform(s.getPoints(), 
                        MainFrame.glassPanel, MainFrame.dropPanel);
                Stroke newS = new Stroke(newP, false, -1, s.getPolygonColor(),
                        s.getStrokeType());
                newStrokesClone.add(newS);
            }

            if (newStrokesClone.size() == 1) {
                MainFrame.dropPanel.addStroke(newStrokesClone.get(0),
                        mouseDrop);
            }
            
            MainFrame.dropPanel.repaint();
        } else if (mouseClone.x >= 0 && mouseClone.y >= 0) {
            for (Stroke s : strokes) {
                ArrayList<Point> newP = Utilities.transform(s.getPoints(), 
                        MainFrame.glassPanel, MainFrame.clonePanel);
                Stroke newS = new Stroke(newP, true, -1, s.getPolygonColor(),
                        s.getStrokeType());
                newStrokesClone.add(newS);
            }
            
            // Grow the strokes that need to be re-grown
            if (state== Constants.FLEXIBLE_TO_FLEXIBLE) {
				if(!newStrokesClone.isEmpty())
					Grow.growStrokes(newStrokesClone, true);
            } else if (state == Constants.SOLID_TO_SOLID) {
                for (Stroke st: newStrokesClone) {
					Grow.growSolidStrokes(st);
				}
	        } else {
				if (!newStrokesClone.isEmpty())
					Grow.growStrokes(newStrokesClone, false);
            }
            
            MainFrame.clonePanel.repaint();            
        }
        
        Main.mainFrame.setCursor(Cursor.CROSSHAIR_CURSOR);
        super.setVisible(false);
        
    }
    
    public void clearPoints() {
        stage = Constants.NONE;
        strokes.clear();
        strokes = new ArrayList<Stroke>();
        dragP = null;
        
        
        DrawPanel.brush = new Brush();
		DrawPanel.brushes = new ArrayList<Brush>();
		DrawPanel.brushPoints = new ArrayList<Point>();
        DrawPanel.brushRectangle = null;
        DrawPanel.brushRectPoints = new Point[2];
        MainFrame.drawPanel.repaint();
    }
    
    public class MouseDispatcher extends MouseAdapter
        implements MouseMotionListener{

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            MainFrame.clonePanel.cloneImage =
                    ScreenImage.createImage(MainFrame.clonePanel);
            dragP = e.getPoint();
            surfaceStrokes();
            stage = Constants.STAGE_SURFACE;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            shiftStrokes(e.getPoint().x - dragP.x, e.getPoint().y - dragP.y);
            dragP = e.getPoint();
            stage = Constants.STAGE_DRAG;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            stage = Constants.STAGE_SINK;
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e){}
    }
}