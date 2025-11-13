package com.tolmic.btree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class BTree<T> implements IBTree<T> {

    private class Node {
        public List<T> elems;
        public List<Node> children;

        public Node(List<T> elems, List<Node> children) {
            this.elems = elems;
            this.children = children;
        }

        public Node() {
            elems = new ArrayList<>();
            children = new ArrayList<>();
        }

        public T getElem(int index) {
            return elems.get(index);
        }

        public Node getChild(int index) {
            return children.get(index);
        }

        public int size() {
            return elems.size();
        }
    }

    private class SplitedNode {
        Node left;
        Node right;

        T middleElem;

        public SplitedNode(Node left, Node right, T middleElem) {
            this.left = left;
            this.right = right;
            this.middleElem = middleElem;
        }
    }

    private int t = 1;
    private Comparator<T> comparator;
    private Node root = null;
    
    public BTree(int t, Comparator<T> comparator) {
        this.t = t;
        this.root = new Node(new ArrayList<>(), new ArrayList<>());
        this.comparator = comparator;
    }

    public BTree(int t) {
        this.t = t;
    }

    public static BTree<Integer> createNumberBTree(int t) {
        return new BTree<Integer>(t, (a, b) -> {
            if (a < b) {
                return -1;
            } else if (a > b) {
                return 1;
            }
            
            return 0;
        });
    }

    public boolean containsKey(T value) {
        Node curr = root;
        
        while (curr != null) {
            int n = curr.elems.size();

            for (int i = 0; i < n; i++) {
                int compareResult = comparator.compare(value, curr.elems.get(i));

                if (compareResult == 0) {
                    return true;
                } else if (compareResult == -1 || i == n - 1) {
                    curr = curr.children.get(i == n - 1 ? n : i);
                    break;
                }                
            }
        }

        return false;
    }

    private void beginFullRoot(T elem) {
        root.elems.add(elem);
        root.children.add(null);
        root.children.add(null);
    }

    private List<T> getSubElemList(List<T> list, int a, int b) {
        ArrayList<T> sybList = new ArrayList<>();

        for (int i = a; i < b; i++) {
            sybList.add(list.get(i));
        }

        return sybList;
    }

    private List<Node> getSubChildList(List<Node> list, int a, int b) {
        ArrayList<Node> sybList = new ArrayList<>();

        for (int i = a; i < b; i++) {
            sybList.add(list.get(i));
        }
        
        return sybList;
    }

    private SplitedNode splitNode(Node node) {
        int n = node.size();

        Node left = new Node(getSubElemList(node.elems, 0, n / 2), 
                                getSubChildList(node.children, 0, n / 2 + 1));
        Node right = new Node(getSubElemList(node.elems, n / 2 + 1, n),
                                getSubChildList(node.children, n / 2 + 1, n + 1));

        T middleElem = node.getElem(n / 2);

        return new SplitedNode(left, right, middleElem);
    }

    private void raiseToEmptyNode(Node parent, SplitedNode splitedNode) {
        parent.elems = new ArrayList<>(Arrays.asList(splitedNode.middleElem));
        parent.children = new ArrayList<>(Arrays.asList(splitedNode.left, splitedNode.right));

        root = parent;
    }

    private void raiseToNonEpmtyNode(int nodeIndex, Node parent, SplitedNode splitedNode) {
        parent.elems.add(nodeIndex, splitedNode.middleElem);
        parent.children.add(nodeIndex, splitedNode.left);
        parent.children.set(nodeIndex + 1, splitedNode.right);
    }

    private void splitCurrAndRiseMiddleToPrev(Node curr, Node parent, int nodeIndex) {
        SplitedNode splitedNode = splitNode(curr);

        if (parent.size() == 0) {
            raiseToEmptyNode(parent, splitedNode);
        } else {
            raiseToNonEpmtyNode(nodeIndex, parent, splitedNode);
        }
    }

    private int findDescIndex(List<T> elems, T e) {
        for (int i = 0; i < elems.size(); i++) {
            if (comparator.compare(e, elems.get(i)) == -1) {
                return i;
            }
        }

        return elems.size();
    }

    private void addToLeaf(int index, Node leaf, T elem) {
        if (index > leaf.size()) {
            leaf.elems.add(elem);
        } else {
            leaf.elems.add(index, elem);
        }
        
        leaf.children.add(null);
    }

    private Node chooseGottenPart(Node prev, int childIndex, T elem) {
        return prev.getChild(comparator.compare(elem, prev.getElem(childIndex)) == -1 ? childIndex : childIndex + 1);
    }

    private void goToLeaf(T elem) {
        Node curr = root;
        Node prev = new Node();
        int childIndex = 0;

        while (curr != null) {
            if (curr.size() == 2 * t - 1) {
                splitCurrAndRiseMiddleToPrev(curr, prev, childIndex);
                prev = chooseGottenPart(prev, childIndex, elem);
            } else {
                prev = curr;
            }

            childIndex  = findDescIndex(prev.elems, elem);
            curr        = curr.getChild(childIndex);
        }

        addToLeaf(childIndex, prev, elem);
    }

    public void add(T elem) {
        if (root.size() == 0) {
            beginFullRoot(elem);
        } else {
            goToLeaf(elem);
        }
    }
    
    public void outShelfs(Function<T, Number> keyExtractor) {
        Deque<Node> d = new ArrayDeque<>();
        d.addLast(root);

        while (!d.isEmpty()) {
            int count = d.size();

            for (int i = 0; i < count; i++) {
                Node curr = d.pollFirst();

                String nodeEl = curr.elems.stream()
                                                .map(e -> (Integer) keyExtractor.apply(e))
                                                .map(Object::toString)
                                                .collect(Collectors.joining(", ", "[", "]"));

                System.out.print(nodeEl + " ");

                for (Node child : curr.children) {
                    if (child == null) {
                        break;
                    }

                    d.addLast(child);
                }
            }

            System.out.println();
        }
    }

    // Be ready, it is hurd :-)
    public void remove(T key) {
        
    }

}