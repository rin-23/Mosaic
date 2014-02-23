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

public class AVLTree implements Tree {

    private AVLNode root;

    @Override
    public Comparable get(Comparable c) {
        AVLNode n = root;
        while (n != null) {
            int val = c.compareTo(n.data);
            if (val == 0) {
                return n.data;
            }
            n = (val < 0) ? n.left : n.right;
        }
        return null;
    }

    AVLNode getRoot() {
        //needed by iterator
        return root;
    }

    @Override
    public boolean isEmpty() {
        return (root == null);
    }

    @Override
    public Iterator iterator() {
        return new AVLIterator(this);
    }

    @Override
    public Comparable probe(Comparable c) {
        if (root == null) {
            root = new AVLNode();
            root.data = c;
            return c;
        }
        AVLNode n = root;
        AVLNode base = root;
        AVLNode baseParent = null;
        boolean baseParentPath = false;
        boolean[] path = new boolean[10];
        int depth = 0;
        while (n != null) {
            int val = c.compareTo(n.data);
            if (val == 0) {
                return (Comparable)n.data;
            } 
            if (path[depth++] = (val < 0)) {
                if (n.left == null) {
                    n.left = new AVLNode();
                    n.left.data = c;
                    break;
                }
                if (n.left.balance != AVLNode.BALANCED) {
                    baseParent = n;
                    baseParentPath = true;
                    base = n.left;
                    depth = 0;
                }
                n = n.left;
            } else {
                if (n.right == null) {
                    n.right = new AVLNode();
                    n.right.data = c;
                    break;
                }
                if (n.right.balance != AVLNode.BALANCED) {
                    baseParent = n;
                    baseParentPath = false;
                    base = n.right;
                    depth = 0;
                }
                n = n.right;
            }
            if (depth >= path.length) {
                boolean[] old = path;
                path = new boolean[old.length + (old.length >> 1)];
                System.arraycopy(old, 0, path, 0, old.length);
            }
        }
        //update balance factors
        n = base;
        for (int i = 0; i < depth; i++) {
            if (path[i]) {
                n.balance--;
                n = n.left;
            } else {
                n.balance++;
                n = n.right;
            }
        }
        //now need to do rotations
        if (base.balance == -2) {
            //left
            n = base.left;
            if (n.balance == AVLNode.LEFT_HIGH) {
                base = ll(base);
                base.balance = AVLNode.BALANCED;
                base.right.balance = AVLNode.BALANCED;
            } else {
                base = lr(base);
                if (base.balance == AVLNode.LEFT_HIGH) {
                    base.left.balance = AVLNode.BALANCED;
                    base.right.balance = AVLNode.RIGHT_HIGH;
                } else if (base.balance == AVLNode.BALANCED) {
                    base.left.balance = AVLNode.BALANCED;
                    base.right.balance = AVLNode.BALANCED;
                } else {
                    base.left.balance = AVLNode.LEFT_HIGH;
                    base.right.balance = AVLNode.BALANCED;
                }
                base.balance = AVLNode.BALANCED;
            }
        } else if (base.balance == +2) {
            //right
            n = base.right;
            if (n.balance == AVLNode.RIGHT_HIGH) {
                base = rr(base);
                base.balance = AVLNode.BALANCED;
                base.left.balance = AVLNode.BALANCED;
            } else {
                base = rl(base);
                if (base.balance == AVLNode.RIGHT_HIGH) {
                    base.left.balance = AVLNode.LEFT_HIGH;
                    base.right.balance = AVLNode.BALANCED;
                } else if (base.balance == AVLNode.BALANCED) {
                    base.left.balance = AVLNode.BALANCED;
                    base.right.balance = AVLNode.BALANCED;
                } else { //base.balance == LEFT_HIGH
                    base.left.balance = AVLNode.BALANCED;
                    base.right.balance = AVLNode.RIGHT_HIGH;
                }
                base.balance = AVLNode.BALANCED;
            }
        }
        if (baseParent != null) {
            if (baseParentPath) {
                baseParent.left = base;
            } else {
                baseParent.right = base;
            }
        } else {
            root = base;
        }
        return c;
    }

    @Override
    public Comparable remove(Comparable c) {
        if (root == null) {
            return null;
        }
        AVLNode n = root;
        AVLNode[] nodes = new AVLNode[10];
        boolean[] path = new boolean[10];
        int depth = 0;
        nodes[depth] = new AVLNode();
        nodes[depth].left = root;
        path[depth++] = true;
        while (n != null) {
            int val = c.compareTo(n.data);
            if (val == 0) {
                break; //have match
            }
            nodes[depth] = n;
            path[depth] = (val < 0);
            n = (path[depth]) ? n.left : n.right;
            if (++depth >= nodes.length) {
                AVLNode[] old = nodes;
                nodes = new AVLNode[old.length + (old.length >> 1)];
                System.arraycopy(old, 0, nodes, 0, old.length);
                boolean[] old2 = path;
                path = new boolean[old2.length + (old2.length >> 1)];
                System.arraycopy(old2, 0, path, 0, old2.length);
            }
        }
        //delete item
        if (n != null) {
            if (n.right == null) {
                if (path[depth - 1]) {
                    nodes[depth - 1].left = n.left;
                } else {
                    nodes[depth - 1].right = n.left;
                }
            } else {
                AVLNode r = n.right;
                if (r.left == null) {
                    r.left = n.left;
                    r.balance = n.balance;
                    if (path[depth - 1]) {
                        nodes[depth - 1].left = r;
                    } else {
                        nodes[depth - 1].right = r;
                    }
                    //cannot overflow  as only 1 node added
                    nodes[depth] = r;
                    path[depth++] = false;
                    
                } else {
                    AVLNode s;
                    int index = depth;
                    if (++depth >= nodes.length) {
                        AVLNode[] old = nodes;
                        nodes = new AVLNode[old.length + (old.length >> 1)];
                        System.arraycopy(old, 0, nodes, 0, old.length);
                        boolean[] old2 = path;
                        path = new boolean[old2.length + (old2.length >> 1)];
                        System.arraycopy(old2, 0, path, 0, old2.length);
                    }
                    while (true) {
                        nodes[depth] = r;
                        path[depth] = true;
                        if (++depth >= nodes.length) {
                            AVLNode[] old = nodes;
                            nodes = new AVLNode[old.length + (old.length >> 1)];
                            System.arraycopy(old, 0, nodes, 0, old.length);
                            boolean[] old2 = path;
                            path = new boolean[old2.length + (old2.length >> 1)];
                            System.arraycopy(old2, 0, path, 0, old2.length);
                        }
                        s = r.left;
                        if (s.left == null) { break; }
                        r = s;
                    }
                    s.left = n.left;
                    r.left = s.right;
                    s.right = n.right;
                    s.balance = n.balance;
                    if (path[index - 1]) {
                        nodes[index - 1].left = s;
                    } else {
                        nodes[index - 1].right = s;
                    }
                    nodes[index] = s;
                    path[index] = false;
                }
            }
            //update balance factors
            for (int i = depth - 1; i > 0; i--) {
                if (path[i]) {
                    nodes[i].balance++;
                    if (nodes[i].balance == +1) {
                        break;
                    } else if (nodes[i].balance == +2) {
                        AVLNode y = nodes[i];
                        AVLNode x = y.right;
                        if (x.balance == AVLNode.LEFT_HIGH) {
                            //left
                            AVLNode w = x.left;
                            x.left = w.right;
                            w.right = x;
                            y.right = w.left;
                            w.left = y;
                            if (w.balance == AVLNode.RIGHT_HIGH) {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.LEFT_HIGH;
                            } else if (w.balance == AVLNode.BALANCED) {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.BALANCED;
                            } else {
                                x.balance = AVLNode.RIGHT_HIGH;
                                y.balance = AVLNode.BALANCED;
                            }
                            w.balance = AVLNode.BALANCED;
                            if (path[i - 1]) {
                                nodes[i - 1].left = w;
                            } else {
                                nodes[i - 1].right = w;
                            }
                        } else {
                            //right
                            y.right = x.left;
                            x.left = y;
                            if (path[i - 1]) {
                                nodes[i - 1].left = x;
                            } else {
                                nodes[i - 1].right = x;
                            }
                            if (x.balance == AVLNode.BALANCED) {
                                x.balance = AVLNode.LEFT_HIGH;
                                y.balance = AVLNode.RIGHT_HIGH;
                                break;
                            } else {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.BALANCED;
                            }
                        }
                    }
                } else {
                    nodes[i].balance--;
                    if (nodes[i].balance == -1) {
                        break;
                    } else if (nodes[i].balance == -2) {
                        AVLNode y = nodes[i];
                        AVLNode x = y.left;
                        if (x.balance == AVLNode.RIGHT_HIGH) {
                            //right
                            AVLNode w = x.right;
                            x.right = w.left;
                            w.left = x;
                            y.left = w.right;
                            w.right = y;
                            if (w.balance == AVLNode.LEFT_HIGH) {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.RIGHT_HIGH;
                            } else if (w.balance == AVLNode.BALANCED) {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.BALANCED;
                            } else {
                                x.balance = AVLNode.LEFT_HIGH;
                                y.balance = AVLNode.BALANCED;
                            }
                            w.balance = AVLNode.BALANCED;
                            if (path[i - 1]) {
                                nodes[i - 1].left = w;
                            } else {
                                nodes[i - 1].right = w;
                            }
                        } else {
                            //left
                            y.left = x.right;
                            x.right = y;
                            if (path[i - 1]) {
                                nodes[i - 1].left = x;
                            } else {
                                nodes[i - 1].right = x;
                            }
                            if (x.balance == AVLNode.BALANCED) {
                                x.balance = AVLNode.RIGHT_HIGH;
                                y.balance = AVLNode.LEFT_HIGH;
                                break;
                            } else {
                                x.balance = AVLNode.BALANCED;
                                y.balance = AVLNode.BALANCED;
                            }
                        }
                    }
                }
            }
            if (nodes[0].left != root) {
                root = nodes[0].left;
            }
            return n.data;
        } else {
            return null;
        }
    }
    
    private AVLNode ll(AVLNode tree) {
        if (tree == null) return null;
        AVLNode node = tree.left;
        tree.left = node.right;
        node.right = tree;
        return node;
    }

    private AVLNode rr(AVLNode tree) {
        if (tree == null) return null;
        AVLNode node = tree.right;
        tree.right = node.left;
        node.left = tree;
        return node;
    }

    private AVLNode lr(AVLNode tree) {
        tree.left = rr(tree.left);
        return ll(tree);
    }

    private AVLNode rl(AVLNode tree) {
        tree.right = ll(tree.right);
        return rr(tree);
    }
    
    private static int check(AVLNode node) throws Exception {
        int height = 0;
        int lh = 0;
        int rh = 0;
        if (node == null) {
            return height;
        }
        if (node.left != null) {
            lh = check(node.left);
        }
        if (node.right != null) {
            rh = check(node.right);
        }
        if (node.balance != (rh - lh)) {
            throw new Exception("Unbalanced tree is: " +
                    node.balance + " should be " + (rh - lh));
        }
        if (lh > height) height = lh;
        if (rh > height) height = rh;
        return ++height;
    }

//    This is how AVL Tree is used.
//
//    public static void main(String[] args) {
//        CPoint p1 = new CPoint(1, 2);
//        CPoint p2 = new CPoint(2, 1);
//        AVLTree tree = new AVLTree();
//        tree.probe(p1);
//        tree.probe(p2);
//        Iterator iter = tree.iterator();
//        while (iter.hasNext()) {
//            CPoint p = (CPoint) iter.next();
//            System.out.println(p);
//        }
//    }

}