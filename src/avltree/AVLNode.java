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

public class AVLNode {

    static final int LEFT_HIGH = -1;
    static final int BALANCED = 0;
    static final int RIGHT_HIGH = 1;

    int balance = BALANCED;

    AVLNode left;
    AVLNode right;
    Comparable data;

}

