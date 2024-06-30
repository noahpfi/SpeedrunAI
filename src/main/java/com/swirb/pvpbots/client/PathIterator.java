package com.swirb.pvpbots.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class PathIterator {

    private final List<PathN> items;
    private int iterator;
    private PathN current;
    private PathN next;

    public PathIterator(List<PathN> nodes) {
        this.items = nodes;
        this.iterator = 0;
        this.setCurrent();
        this.setNext();
    }

    public PathIterator(Set<PathN> nodes) {
        this.items = new ArrayList<>(nodes);
        this.iterator = 0;
        this.setCurrent();
    }

    private boolean setCurrent() {
        if (this.items == null || this.items.isEmpty() || this.iterator < 0) {
            this.current = null;
            return false;
        }
        this.current = this.items.get(this.iterator);
        return true;
    }

    private boolean setNext() {
        if (this.items == null || this.items.isEmpty() || this.onLast()) {
            this.next = null;
            return false;
        }
        this.next = this.items.get(this.iterator + 1);
        return true;
    }

    public PathN current() {
        return this.current;
    }

    public PathN currentNext() {
        return this.next;
    }

    public boolean hasNext() {
        return this.next != null;
    }

    public boolean next() {
        if (this.iterator + 1 >= this.items.size()) {
            return false;
        }
        this.iterator++;
        return this.setCurrent() && this.setNext();
    }

    public boolean back() {
        if (this.iterator - 1 < 0) {
            return false;
        }
        this.iterator--;
        return this.setCurrent() && this.setNext();
    }

    public void reset() {
        this.iterator = 0;
        this.setCurrent();
        this.setNext();
    }

    public boolean onFirst() {
        return this.iterator - 1 == this.items.size();
    }

    public boolean onLast() {
        return this.iterator + 1 == this.items.size();
    }
}
