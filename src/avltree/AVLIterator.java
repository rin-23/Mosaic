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

class AVLIterator implements java.util.Iterator {

    private AVLTree tree;
    private AVLNode[] nodes = new AVLNode[10];
    private AVLNode current;
    private int depth = 0;

    AVLIterator(AVLTree tree) {
        this.tree = tree;
        AVLNode n = tree.getRoot();
        while (n != null) {
            nodes[depth] = n;
            if (++depth >= nodes.length) {
                AVLNode[] old = nodes;
                nodes = new AVLNode[old.length + (old.length >> 1)];
                System.arraycopy(old, 0, nodes, 0, old.length);
            }
            n = n.left;
        }
    }

    @Override
    public boolean hasNext() {
        return (depth > 0);
    }

    @Override
    public Object next() {
        if (depth <= 0) {
            current = null;
            return null;
        }
        current = nodes[depth - 1];
        nodes[depth - 1] = null;
        if (current.right != null) {
            AVLNode n = current.right;
            while (n != null) {
                nodes[depth] = n;
                if (++depth >= nodes.length) {
                    AVLNode[] old = nodes;
                    nodes = new AVLNode[old.length + (old.length >> 1)];
                    System.arraycopy(old, 0, nodes, 0, old.length);
                }
                n = n.left;
            }
        } else {
            while (depth > 0 && nodes[depth - 1] == null) {
                depth--;
            }
        }
        return current.data;
    }

    @Override
    public void remove() {
        if (current != null) {
            tree.remove(current.data);
        }
        //recalc next position(..as tree might have rotated).
        if (depth > 0) {
            Comparable next = nodes[depth - 1].data;
            AVLNode n = tree.getRoot();
            depth = 0;
            while (n != null) {
                int val = next.compareTo(n.data);
                nodes[depth] = (val <= 0) ? n : null;
                if (++depth >= nodes.length) {
                    AVLNode[] old = nodes;
                    nodes = new AVLNode[old.length + (old.length >> 1)];
                    System.arraycopy(old, 0, nodes, 0, old.length);
                }
                if (val == 0) {
                    break; //match
                }
                n = (val < 0) ? n.left : n.right;
            }
        }
        current = null;
    }

}