package com.swirb.speedrunai.neat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Genome {

    private final Map<Integer, Connection> connections;
    private final Map<Integer, Node> nodes;
    private final Innovation innovation;
    private int fitness;

    public Genome() {
        this.connections = new HashMap<>();
        this.nodes = new HashMap<>();
        this.innovation = new Innovation();
        this.setFitness();
    }

    public Genome(Map<Integer, Connection> connections, Map<Integer, Node> nodes) {
        this.connections = connections;
        this.nodes = nodes;
        this.innovation = new Innovation();
        this.setFitness();
    }

    public void mutationAddConnection() {
        Node node0 = this.nodes.get(new Random().nextInt(this.nodes.size()));
        Node node1 = this.nodes.get(new Random().nextInt(this.nodes.size()));
        for (Connection connection : this.connections.values()) {
            if ((connection.inNode().equals(node0) && connection.outNode().equals(node1)) || (connection.inNode().equals(node1) && connection.outNode().equals(node0))) {
                return;
            }
        }
        boolean reversed = (node0.type() == Node.TYPE.HIDDEN && node1.type() == Node.TYPE.INPUT) || (node0.type() == Node.TYPE.OUTPUT && node1.type() == Node.TYPE.HIDDEN) || (node0.type() == Node.TYPE.OUTPUT && node1.type() == Node.TYPE.INPUT);
        Connection connection = new Connection(reversed ? node1 : node0, reversed ? node0 : node1, (new Random().nextFloat() * 2F) - 1F, true, this.innovation.innovationConnection());
        this.connections.put(connection.innovation(), connection);
    }

    public void mutationAddNode() {
        Connection connection = this.connections.get(new Random().nextInt(this.connections.size()));
        connection.disable();
        Node newNode = new Node(Node.TYPE.HIDDEN, this.innovation.innovationNode());
        Connection connection0 = new Connection(connection.inNode(), newNode, 1.0F, true, this.innovation.innovationConnection());
        Connection connection1 = new Connection(newNode, connection.outNode(), connection.weight(), true, this.innovation.innovationConnection());
        this.nodes.put(newNode.innovation(), newNode);
        this.connections.put(connection0.innovation(), connection0);
        this.connections.put(connection1.innovation(), connection1);
    }

    public Genome crossOver(Genome parent0, Genome parent1) {
        Genome child = new Genome();
        Genome fitter = /*parent0.fitness() > parent1.fitness() ? parent0 : parent1*/ parent0;
        Genome lessFitter = /*parent0.fitness() > parent1.fitness() ? parent1 : parent0*/ parent1;
        for (Node node : fitter.nodes().values()) {
            child.addNode(node.copy());
        }
        for (Connection connection : fitter.connections().values()) {
            if (lessFitter.connections().containsKey(connection.innovation())) {
                child.addConnection(new Random().nextBoolean() ? connection.copy() : lessFitter.connections().get(connection.innovation()).copy());
                continue;
            }
            child.addConnection(connection.copy());
        }
        return child;
    }

    public Map<Integer, Connection> connections() {
        return this.connections;
    }

    public Map<Integer, Node> nodes() {
        return this.nodes;
    }

    public int fitness() {
        return this.fitness;
    }

    public void addConnection(Connection connection) {
        this.connections.put(connection.innovation(), connection);
    }

    public void addNode(Node node) {
        this.nodes.put(node.innovation(), node);
    }

    public void setFitness() {
        // evaluate fitness
        // else
        this.fitness = 0;
    }























}
