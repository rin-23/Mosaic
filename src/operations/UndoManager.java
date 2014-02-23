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
            state.boundaryStrokes = ClonePanel.boundaryStrokes;
            state.flexibleStrokes = ClonePanel.flexibleStrokes;
            state.clonePointsTree = Constants.clonePointsTree;
            
            undoQueue.add(state);            
        }
        
        public static void undo() 
        {
            if (!undoQueue.isEmpty()) {
                UndoState state = undoQueue.pollLast();
                Constants.clonePointsTree = state.clonePointsTree;
                ClonePanel.boundaryStrokes = state.boundaryStrokes;
                ClonePanel.flexibleStrokes = state.flexibleStrokes;
            }
        }    
}
