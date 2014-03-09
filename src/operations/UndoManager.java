/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package operations;

import avltree.AVLTree;
import java.util.ArrayDeque;
import java.util.ArrayList;
import shapes.Stroke;
import constants.Constants;
import imagecloning.ClonePanel;
import java.awt.Point;
import shapes.CPoint;

/**
 *
 * @author rinatabdrashitov
 */
public class UndoManager {
        private static ArrayDeque<UndoState> undoQueue = new ArrayDeque<>();        
       
        public static void saveState()
        { 
            if (undoQueue.size() == 5) {
                undoQueue.poll();
            }            
            UndoState state = new UndoState();
            state.boundaryStrokes = new ArrayList<>(ClonePanel.boundaryStrokes.size());
            for (Stroke st : ClonePanel.boundaryStrokes) {
                Stroke newS = st.clone();
                state.boundaryStrokes.add(newS);
            }
            
            state.flexibleStrokes = new ArrayList(ClonePanel.flexibleStrokes.size());                     
            for (Stroke st : ClonePanel.flexibleStrokes) {
                Stroke newS = st.clone();
                state.flexibleStrokes.add(newS);
            }
    
            undoQueue.add(state);            
        }
        
        public static void undo() 
        {
            if (!undoQueue.isEmpty()) {
                
                UndoState state = undoQueue.pollLast();                
                ClonePanel.boundaryStrokes = state.boundaryStrokes;
                ClonePanel.flexibleStrokes = state.flexibleStrokes;
                
                Constants.clonePointsTree = new AVLTree();
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
    }
}
