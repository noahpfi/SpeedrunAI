package com.swirb.pvpbots.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Path {

    private List<PathN> nodes;
    private int length;
    private double cost;
    private boolean found;
    private boolean completed;
    private PathIterator iterator;

    public Path(List<PathN> nodes) {
        this.nodes = nodes;
        this.length = nodes.size();
        this.cost = this.calculateCost();
        this.found = false;
        this.completed = false;
        this.iterator = new PathIterator(this.nodes);
    }

    public Path() {
        this.nodes = new ArrayList<>();
        this.found = false;
        this.completed = false;
    }

    private void recalculate() {
        this.length = this.nodes.size();
        this.cost = this.calculateCost();
        this.iterator = new PathIterator(this.nodes);
        this.found = false;
        this.completed = false;
    }

    private double calculateCost() {
        double cost = 0;
        for (PathN node : this.nodes) {
            cost += node.F();
        }
        return cost;
    }

    public List<PathN> nodes() {
        return this.nodes;
    }

    public PathN start() {
        return this.nodes.get(0);
    }

    public PathN end() {
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

    public void setPath(List<PathN> nodes) {
        this.nodes = nodes;
        this.recalculate();
    }

    public void setPath(Path path) {
        this.nodes = path.nodes;
        this.recalculate();
    }

    public void add(PathN node) {
        this.nodes.add(node);
        this.recalculate();
    }

    public void add(int index, PathN node) {
        this.nodes.add(index, node);
        this.recalculate();
    }

    public void addAll(List<PathN> nodes) {
        this.nodes.addAll(nodes);
        this.recalculate();
    }

    public void addAll(int index, List<PathN> nodes) {
        this.nodes.addAll(index, nodes);
        this.recalculate();
    }

    public void remove(PathN node) {
        this.nodes.remove(node);
        this.recalculate();
    }

    public PathN nodeAfter(PathN start) {
        if (!this.nodes.contains(start) || this.nodes.indexOf(start) + 1 >= this.length) {
            return null;
        }
        return this.nodes.get(this.nodes.indexOf(start) + 1);
    }

    public void cutOutNodes(List<PathN> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public void cutOutNodes(Set<PathN> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public PathIterator iterator() {
        return this.iterator;
    }
}
