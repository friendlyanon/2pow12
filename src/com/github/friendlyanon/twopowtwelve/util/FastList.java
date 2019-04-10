package com.github.friendlyanon.twopowtwelve.util;

import lombok.val;

import java.util.Iterator;

/**
 * FastList is an ArrayList-like implementation without any of the
 * synchronization and iterator invalidation for faster, albeit unsafe operation
 */
public class FastList<E>
    extends java.util.AbstractList<E>
    implements java.util.List<E>, Iterable<E>, java.util.RandomAccess
{
    private final Object[] data;
    private int size;

    public FastList(int capacity) {
        data = new Object[capacity];
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            return -1;
        }
        for (var i = size - 1; i >= 0; --i) {
            if (data[i] == o) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean add(E e) {
        data[size++] = e;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E remove(int i) {
        val o = data[i];
        if (i != --size) {
            System.arraycopy(data, ++i, data, i - 1, size - i + 1);
        }
        data[size] = null;
        return (E) o;
    }

    @Override
    public boolean remove(Object o) {
        val idx = indexOf(o);
        if (idx == -1) {
            return false;
        }
        remove(idx);
        return true;
    }

    @Override
    public void clear() {
        for (var i = 0; i < size; ++i) {
            data[i] = null;
        }
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) data[index];
    }

    @Override
    public int size() {
        return size;
    }

    private class Itr implements Iterator<E> {
        int cursor = size - 1;

        Itr() {}

        @Override
        public boolean hasNext() {
            return cursor >= 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            return (E) data[cursor--];
        }
    }
}
