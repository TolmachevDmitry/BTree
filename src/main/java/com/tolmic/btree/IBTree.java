package com.tolmic.btree;

public interface IBTree<T> {

    public boolean containsKey(T key);

    public void add(T elem);

    public void remove(T key);

}
