package imagecloning;

import MenuToolbar.ColorChooser;
import constants.Constants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import operations.Utilities;
import shapes.Stroke;

public class DropPanel extends JPanel {
    
    public static int operation = Constants.NONE;
    
    public ArrayList<Stroke> strokeArray;
    public ArrayList<Stroke> displayStrokeArray;
    public ArrayList<Stroke> singleStrokeArray;
    public ArrayList<Integer> highlightedSquare;
    
    public DropPanel () {
        super();
        
        strokeArray = new ArrayList<Stroke>();
        for (int i = 0; i < 6; i++) {
            strokeArray.add(null);
        }
        
        displayStrokeArray = new ArrayList<Stroke>();
        for (int i = 0; i < 6; i++) {
            displayStrokeArray.add(null);
        }
        
        singleStrokeArray = new ArrayList<Stroke>();
        singleStrokeArray.add(0, null);
        
        highlightedSquare = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            highlightedSquare.add(0);
        }
        
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
        
        int lx = 10;
        int ty = 22;
        for (int i = 0; i < 6; i++) {
            ty = 22 + i * 84;
            if (highlightedSquare.get(i) == 0) {
                g2D.setColor(Color.blue);
            } else {
                g2D.setColor(Color.red);
            }
            g2D.drawRect(lx, ty, 74, 74);
        }
      
        for (int i = 0; i < 6; i++) {
            Stroke s = displayStrokeArray.get(i);
            if (s != null) {
                s.paint(g, Color.black, false);
            }
        }
        
    }
    
    public void clearSquare() {
        for (int i = 0; i < 6; i++) {
            highlightedSquare.set(i, 0);
        }
    }
    
    public int whichSquare(Point p) {
        int lx = 10;
        int rx = 84;
        int ty = 22;
        int by = -1;
        int index = -1;
        for (int i = 0; i < 6; i++) {
            ty = 22 + i * 84;
            by = ty + 74;
            if (p.x >= lx && p.x <= rx
                    && p.y >= ty && p.y <= by) {
                index = i;
            }
        }
        return index;
    }
    
    public void addStroke(Stroke s, Point p) {
        int index = whichSquare(p);
        if (index != -1) {
            strokeArray.set(index, s);
            growStorke(s, index);
        }
    }
    
    public void growStorke (Stroke s, int index) {
        int lx = 10 + 5; int rx = 84 - 5;
        int ty = 22 + index * 84 + 5;
        int by = ty + 74 - 5;

        int centroidX = (lx + rx) / 2;
        int centroidY = (ty + by) / 2;
        
        Stroke displayS = new Stroke(s.getPoints(), true, -1,
                s.getPolygonColor(), s.getStrokeType());
        
        displayS.setCentroid(centroidX, centroidY);
        boolean stopGrowing = false;

        for (int i = 1; i <= Constants.ITERATION; i++) {
            for (int j = 0; j < displayS.getgrowthVectors().size(); j++) {
                if (!stopGrowing) {
                    Point newP = new Point();
                    newP.x = Math.round(displayS.getCentroid().x +
                            (i / (float)Constants.ITERATION)
                            * displayS.getgrowthVectors().get(j).x);
                    newP.y = Math.round(displayS.getCentroid().y +
                            (i / (float)Constants.ITERATION)
                            * displayS.getgrowthVectors().get(j).y);
                    if (newP.x <= lx || newP.x >= rx
                            || newP.y <= ty || newP.y >= by) {
                        stopGrowing = true;
                    } else {
                        displayS.getNewPoints().set(j, newP);             
                    }
                }
            }
            displayS.setPoints(displayS.getNewPoints());
        }
        
        ArrayList<Point> smoothDisplayP =
                Utilities.smoothPoints(displayS.getPoints());
        Stroke smoothDisplayS = new Stroke(smoothDisplayP, false, -1,
                s.getPolygonColor(), s.getStrokeType());
        
        displayStrokeArray.set(index, smoothDisplayS);
    }
    
    public class MouseDispatcher extends MouseAdapter
        implements MouseMotionListener{

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            int index = whichSquare(e.getPoint());
            clearSquare();
            if (index != -1) {
                highlightedSquare.set(index, 1);
                if (strokeArray.get(index) != null) {
                    if (operation == Constants.SELECT) {
                        singleStrokeArray.set(0, strokeArray.get(index));
                        DrawPanel.selectedStrokes = singleStrokeArray;
                        DrawPanel.setByDrop = true;
                        MainFrame.drawPanel.repaint();
                    } else if (operation == Constants.COLOR) {
                        Color newColor = ColorChooser.getInstance().getCurrentColor();
                        // Actual Stroke
                        Stroke as = strokeArray.get(index);
                        as.setPolygonColor(newColor);
                        strokeArray.set(index, as);
                        // Display Stroke
                        Stroke ds = displayStrokeArray.get(index);
                        ds.setPolygonColor(newColor);
                        displayStrokeArray.set(index, ds);
                    } else if (operation == Constants.ERASE) {
                        strokeArray.set(index, null);
                        displayStrokeArray.set(index, null);
                    }
                }
                
            }
            repaint(); 
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e){}
    }
    
}