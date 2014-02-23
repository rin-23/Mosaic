/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package operations;

import avltree.AVLTree;
import java.io.Serializable;
import java.util.ArrayList;
import shapes.Stroke;

/**
 *
 * @author rinatabdrashitov
 */
public class UndoState implements Serializable{
    
    public AVLTree clonePointsTree = null;
        
    public ArrayList<Stroke> boundaryStrokes = null;
    public ArrayList<Stroke> flexibleStrokes = null;
    
}
