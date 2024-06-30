package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.basicallyGarbage.ai.Rng;
import com.swirb.pvpbots.basicallyGarbage.ai.Type;
import com.swirb.pvpbots.client.Client;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Genome3 {

    private final int id;
    private final Client client;
    private final AI3 ai;
    private final Set<Connection3> connections;
    private final Set<Node3> nodes;
    private final List<Node3> inputs;
    private final List<Node3> outputs;
    private final IdSupplier3 idSupplier;

    public Genome3(int id, Client client, AI3 ai) {
        this.id = id;
        this.client = client;
        this.ai = ai;
        this.connections = new HashSet<>();
        this.nodes = new HashSet<>();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.idSupplier = new IdSupplier3();
        this.initialize();
    }

    public void addConnection(Connection3 connection, Genome3 g0, Genome3 g1) {
        if (connection == null) {
            throw new UnsupportedOperationException("[AI] -> addConnection: connection is null");
        }
        if (connection.to() == null || connection.from() == null) {
            throw new UnsupportedOperationException("[AI] -> addConnection: connection's nodes are null");
        }
        if (this.connections.contains(connection)) {    // getConnection with the same id already exists
            throw new UnsupportedOperationException("[AI] -> addConnection: connection already exists");
        }
        if (g0 != null && g1 != null && g0.connections.contains(connection) && g1.connections.contains(connection)) {
            boolean g0a = g0.getConnection(connection).active();
            boolean g1a = g1.getConnection(connection).active();
            if ((g0a && !g1a) || (!g0a && g1a)) {
                connection.setActive(!Rng.pass(Setting3.GENE_DISABLE_CHANCE));
            }
        }
        this.nodes.add(connection.from());
        this.nodes.add(connection.to());
        this.connections.add(connection);
    }

    public Genome3 crossProduct(Genome3 other) {
        if (this.connections.isEmpty() || other.connections.isEmpty()) {
            throw new AssertionError("[AI] -> crossProduct: genome has no connections");
        }
        if (this.nodes.isEmpty() || other.nodes.isEmpty()) {
            throw new AssertionError("[AI] -> crossProduct: genome has no nodes");
        }
        double g0f = this.fitness();
        double g1f = other.fitness();
        Genome3 fitter;
        Genome3 lessFit;
        if (g0f > g1f) {
            fitter = this;
            lessFit = other;
        }
        else {
            fitter = other;
            lessFit = this;
        }
        Genome3 genome = new Genome3(this.ai.trainer().currentGeneration().idSupplier().nextGenomeId(), new Client(this.ai.level(), this.client.name + "_cr", null), this.ai);
        for (Connection3 connection : new ArrayList<>(fitter.connections)) {
            if (lessFit.connections.contains(connection)) {
                this.addConnection(Rng.random(new Connection3[] {connection, lessFit.getConnection(connection)}).copy(), fitter, lessFit);
                continue;
            }
            this.addConnection(connection.copy(), fitter, lessFit);
        }
        genome.mutateAddNode();
        genome.mutateAddConnection();
        genome.mutateChangeConnectionWeight();
        return genome;
    }

    public void mutateAddNode() {
        if (Rng.pass(Setting3.MUTATION_ADD_NODE_CHANCE)) {
            Connection3 connection = Rng.random(this.connections);
            Node3 node = new Node3(this.idSupplier.nextNodeId(), Type.HIDDEN, Rng.randomValue());
            this.nodes.add(node);
            this.connections.add(new Connection3(this.idSupplier.nextConnectionId(), connection.from(), node, 1.0D, true));
            this.connections.add(new Connection3(this.idSupplier.nextConnectionId(), node, connection.to(), connection.weight(), true));
            connection.setActive(false);
        }
    }

    public void mutateAddConnection() {
        if (Rng.pass(Setting3.MUTATION_ADD_CONNECTION_CHANCE)) {
            for (int i = 0; i <= 20; i++) {
                Node3 from = Rng.random(this.nodes);
                Node3 to = Rng.random(this.nodes);
                if (from.equals(to) || from.type() == Type.OUTPUT || (from.type() == Type.INPUT && to.type() == Type.INPUT) || this.willCauseRecursion(from, from)) {
                    continue;
                }
                this.connections.add(new Connection3(this.idSupplier.nextConnectionId(), from, to, Rng.randomValue(), true));
            }
            PvPBots.getInstance().getLogger().warning("[AI] -> mutateAddConnection: Failed to add getConnection after 20 attempts");
        }
    }

    public void mutateChangeConnectionWeight() {
        if (Rng.pass(Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT)) {
            for (Connection3 connection : this.connections) {
                if (Rng.pass(Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM)) {
                    connection.setWeight(Rng.random(-Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE, Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE));
                    continue;
                }
                connection.setWeight(Rng.random(-Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_ERROR, Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_ERROR));
            }
        }
    }

    public void calculateEntirely() {
        for (Node3 node : this.inputs) {
            this.calculate(node);
        }
    }

    private void calculate(Node3 from) {
        Set<Connection3> connections = this.connectionsWithFromNode(from);
        double weightedSum = connections.stream().filter(Connection3::active).mapToDouble(c -> c.from().value() * c.weight()).sum();
        for (Connection3 connection : connections) {
            connection.from().setValue(this.ai.activation().activate(weightedSum));
            this.calculate(connection.to());
        }
    }

    public void applyInputs() {
        if (this.inputs.size() != 5) {
            throw new AssertionError("[AI] -> applyOutputs: output size does not match up (size: " + outputs.size() + ")");
        }
        Entity attacker = this.client.nearest(5, 5, 5, e -> e instanceof Monster);
        this.inputs.get(0).setValue(this.client.getX());
        this.inputs.get(0).setValue(this.client.getZ());
        this.inputs.get(0).setValue(attacker == null ? 0.0D : attacker.getX());
        this.inputs.get(0).setValue(attacker == null ? 0.0D : attacker.getZ());
        this.inputs.get(0).setValue(this.client.getHealth());
    }

    public void applyOutputs() {
        if (this.outputs.size() != 4) {
            throw new AssertionError("[AI] -> applyOutputs: output size does not match up (size: " + outputs.size() + ")");
        }
        this.client.input.W = this.outputs.get(0).value() > 0.5D;
        this.client.input.A = this.outputs.get(1).value() > 0.5D;
        this.client.input.S = this.outputs.get(2).value() > 0.5D;
        this.client.input.D = this.outputs.get(3).value() > 0.5D;
    }

    private boolean willCauseRecursion(Node3 from, Node3 current) {
        for (Connection3 connection : this.connectionsWithFromNode(current)) {
            if (connection.to() == null) {
                throw new AssertionError("[AI] -> willCauseRecursion: getConnection's to node is null");
            }
            if (connection.to().type() == Type.OUTPUT) {
                return false;
            }
            if (connection.to().equals(from)) {
                return true;
            }
            if (this.willCauseRecursion(from, connection.to())) {
                return true;
            }
        }
        return false;
    }

    public Connection3 getConnection(Connection3 connection) {
        for (Connection3 c : this.connections) {
            if (c.equals(connection)) return c;
        }
        return null;
    }

    public Set<Connection3> connectionsWithFromNode(Node3 from) {
        return this.connections.stream().filter(connection -> connection.from().equals(from)).collect(Collectors.toSet());
    }

    public double fitness() {
        return this.client.getHealth() * (this.client.tickCount + 1);
    }

    public double compatibilityDistance(Genome3 genome) {
        Genome3 larger;
        Genome3 smaller;
        if (this.connections.size() < genome.connections.size()) {
            larger = genome;
            smaller = this;
        }
        else {
            larger = this;
            smaller = genome;
        }
        int D = 0;
        int E = 0;
        double weight = 0.0D;
        int i = 0;
        int k = 0;
        for (Connection3 connection : larger.connections) {
            Connection3 c = smaller.getConnection(connection);
            if (c == null) {
                if (i <= smaller.connections.size()) {
                    D++;
                }
                else E++;
            }
            else {
                weight += Math.abs(connection.weight() - c.weight());
                k++;
            }
            i++;
        }
        double W = weight / k;
        double N = larger.connections.size();
        double C0 = Setting3.COMP_DISTANCE_WEIGHT_DISJOINT;
        double C1 = Setting3.COMP_DISTANCE_WEIGHT_EXCESS;
        double C2 = Setting3.COMP_DISTANCE_WEIGHT_AVG_WEIGHT;
        // (C0 * E) / N + (C1 * D) / N + (C2 * W)
        return ((C0 * (double) E) / N) + ((C1 * (double) D) / N) + (C2 * W);
    }

    private void initialize() {
        this.inputs.add(new Node3(this.idSupplier.nextNodeId(), Type.INPUT, 0.0D));  // client x
        this.inputs.add(new Node3(this.idSupplier.nextNodeId(), Type.INPUT, 0.0D));  // client z
        this.inputs.add(new Node3(this.idSupplier.nextNodeId(), Type.INPUT, 0.0D));  // attacker x
        this.inputs.add(new Node3(this.idSupplier.nextNodeId(), Type.INPUT, 0.0D));  // attacker z
        this.inputs.add(new Node3(this.idSupplier.nextNodeId(), Type.INPUT, 0.0D));  // client health
        this.outputs.add(new Node3(this.idSupplier.nextNodeId(), Type.OUTPUT, Rng.randomValue()));    // W
        this.outputs.add(new Node3(this.idSupplier.nextNodeId(), Type.OUTPUT, Rng.randomValue()));    // A
        this.outputs.add(new Node3(this.idSupplier.nextNodeId(), Type.OUTPUT, Rng.randomValue()));    // S
        this.outputs.add(new Node3(this.idSupplier.nextNodeId(), Type.OUTPUT, Rng.randomValue()));    // D
        this.nodes.addAll(this.inputs);
        this.nodes.addAll(this.outputs);
    }

    public int id() {
        return this.id;
    }

    public Client client() {
        return this.client;
    }

    public Set<Connection3> connections() {
        return this.connections;
    }

    public Set<Node3> nodes() {
        return this.nodes;
    }

    public List<Node3> inputs() {
        return this.inputs;
    }

    public List<Node3> outputs() {
        return this.outputs;
    }

    public IdSupplier3 idSupplier() {
        return this.idSupplier;
    }

    public Genome3 copy() {
        Genome3 genome = new Genome3(this.id, new Client(this.ai.level(), this.client.name + "_c", null), this.ai);
        genome.nodes.addAll(this.nodes.stream().map(Node3::copy).collect(Collectors.toSet()));
        genome.connections.addAll(this.connections.stream().map(Connection3::copy).collect(Collectors.toSet()));
        return genome;
    }

    public boolean equals(Object o) {
        return o instanceof Genome3 && ((Genome3) o).id == this.id;
    }
}
