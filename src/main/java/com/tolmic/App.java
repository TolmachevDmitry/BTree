package com.tolmic;

import com.tolmic.btree.BTree;

public class App 
{
    public static void main( String[] args )
    {
        BTree<Integer> tree = BTree.createNumberBTree(3);

        for (int i = 0; i < 12; i++) {
            tree.add((int) (Math.random() * 20));
        }

        tree.outShelfs((a) -> {
            return a;
        });
    }
}
