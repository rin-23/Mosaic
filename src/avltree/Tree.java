package avltree;

/*
    Copyright (c) 2004 by Robert J Colquhoun, All Rights Reserved

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import java.util.Iterator;

/**
* Tree provides a common interface for various tree implementations 
*/
public interface Tree {

    public static final int TYPE_AVL = 0;
    
    /**
     * Return whether the specified object is in the tree.
     * @return contains the item
     */
    public Comparable get(Comparable c);

    /**
     * If the tree contains any items.
     * @return is empty
     */
    public boolean isEmpty();
    
    /**
     * Iterator to traverse through tree items
     * @return the iterator 
     */
    public Iterator iterator();

    /**
     * Searches for the specified item.
     * If the item is missing inserts it into the tree
     * @return the item
     */
    public Comparable probe(Comparable c);

    /**
     * Removes the specified item from the tree
     * @return the removed item
     */
    public Comparable remove(Comparable c);
}
