package com.swirb.speedrunai.path;

import com.swirb.speedrunai.utils.PathIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Path {

    private List<Node> nodes;
    private int length;
    private double cost;
    private boolean found;
    private boolean completed;
    private PathIterator iterator;

    public Path(List<Node> nodes) {
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
        for (Node node : this.nodes) {
            cost += node.F();
        }
        return cost;
    }

    public List<Node> nodes() {
        return this.nodes;
    }

    public Node start() {
        return this.nodes.get(0);
    }

    public Node end() {
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

    public void setPath(List<Node> nodes) {
        this.nodes = nodes;
        this.recalculate();
    }

    public void setPath(Path path) {
        this.nodes = path.nodes;
        this.recalculate();
    }

    public void add(Node node) {
        this.nodes.add(node);
        this.recalculate();
    }

    public void add(int index, Node node) {
        this.nodes.add(index, node);
        this.recalculate();
    }

    public void addAll(List<Node> nodes) {
        this.nodes.addAll(nodes);
        this.recalculate();
    }

    public void addAll(int index, List<Node> nodes) {
        this.nodes.addAll(index, nodes);
        this.recalculate();
    }

    public void remove(Node node) {
        this.nodes.remove(node);
        this.recalculate();
    }

    public Node nodeAfter(Node start) {
        if (!this.nodes.contains(start) || this.nodes.indexOf(start) + 1 >= this.length) {
            return null;
        }
        return this.nodes.get(this.nodes.indexOf(start) + 1);
    }

    public void cutOutNodes(List<Node> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public void cutOutNodes(Set<Node> nodes) {
        this.nodes.removeAll(nodes);
        this.recalculate();
    }

    public PathIterator iterator() {
        return this.iterator;
    }
}
