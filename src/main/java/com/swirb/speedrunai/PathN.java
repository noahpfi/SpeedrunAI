package com.swirb.speedrunai;

import com.swirb.speedrunai.utils.PathIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PathN {

    private List<StarN> nodes;
    private int length;
    private double cost;
    private boolean found;
    private boolean completed;
    private PathIteratorN iterator;

    public PathN(List<StarN> nodes) {
        this.nodes = nodes;
        this.length = nodes.size();
        this.cost = this.calculateCost();
        this.found = false;
        this.completed = false;
        this.iterator = new PathIteratorN(this.nodes);
    }

    public PathN() {
        this.nodes = new ArrayList<>();
        this.found = false;
        this.completed = false;
    }

    private void recalculate() {
        this.length = this.nodes.size();
        this.cost = this.calculateCost();
        this.iterator = new PathIteratorN(this.nodes);
        this.found = false;
        this.completed = false;
    }

    private double calculateCost() {
        double cost = 0;
        for (StarN node : this.nodes) {
            cost += node.F();
        }
        return cost;
    }

    public List<StarN> nodes() {
        return this.nodes;
    }

    public StarN start() {
        return this.nodes.get(0);
    }

    public StarN end() {
        return this.nodes.get(this.length - 1);
    }

    public int length() {
        return this.length;
    }

    public double cost() {
        return this.cost;
    }

    public boolean found() {
        return this.found;
    }

    public boolean completed() {
        return this.completed;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setPath(List<StarN> nodes) {
        this.nodes = nodes;
        this.recalculate();
    }

    public void setPath(PathN path) {
        this.nodes = path.nodes;
        this.recalculate();
    }

    public void add(StarN node) {
        this.nodes.add(node);
        this.recalculate();
    }

    public void add(int index, StarN node) {
        this.nodes.add(index, node);
        this.recalculate();
    }

    public void addAll(List<StarN> nodes) {
        this.nodes.addAll(nodes);
        this.recalculate();
    }

    public void addAll(int index, List<StarN> nodes) {
        this.nodes.addAll(index, nodes);
        this.recalculate();
    }

    public void remove(StarN node) {
        this.nodes.remove(node);
        this.recalculate();
    }

    public StarN nodeAfter(StarN start) {
        if (!this.nodes.contains(start) || this.nodes.indexOf(start) + 1 >= this.length) {
            return null;
        }
        return this.nodes.get(this.nodes.indexOf(start) + 1);
    }

    public void cutOutNodes(List<StarN> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public void cutOutNodes(Set<StarN> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public PathIteratorN iterator() {
        return this.iterator;
    }
}
