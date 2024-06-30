package com.swirb.pvpbots.basicallyGarbage.ai;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.client.Client;

import java.util.*;

public class Genome {

    private final AI ai;
    private final Client client;
    private final int id;
    private Species species;
    private final Map<Integer, Gene> genes;

    public Genome(AI ai, Client client, Species species) {
        this.ai = ai;
        this.client = client;
        this.id = this.ai.idAssigner().nextId();
        this.species = species;
        this.genes = new HashMap<>();
    }

    public void addGene(Gene gene, Genome parent0, Genome parent1) {
        if (gene == null) {
            PvPBots.getInstance().getLogger().severe("[AI] -> adding Gene: Gene is null");
            return;
        }
        if (this.genes.containsKey(gene.id())) {
            PvPBots.getInstance().getLogger().severe("[AI] -> adding Gene: Genome already contains gene");
            return;
        }
        gene = gene.clone();
        if (parent0 != null && parent1 != null && parent0.containsGene(gene) && parent1.containsGene(gene)) {
            boolean p0d = parent0.getGene(gene).isActive();
            boolean p1d = parent1.getGene(gene).isActive();
            if ((p0d && !p1d) || (!p0d && p1d)) {
                gene.setActive(!Rng.pass(this.ai.setting().GENE_DISABLE_CHANCE));
            }
        }
        this.genes.put(gene.id(), gene);
    }

    public static Genome crossProduct(Genome parent0, Genome parent1) {
        if (parent0.genes.isEmpty() || parent1.genes.isEmpty()) {
            PvPBots.getInstance().getLogger().severe("[AI] -> crossProduct: Genome is empty");
            return null;
        }
        double p0f = parent0.fitness();
        double p1f = parent1.fitness();
        Genome fitter;
        Genome lessFit;
        if (p0f > p1f) {
            fitter = parent0;
            lessFit = parent1;
        }
        else {
            fitter = parent1;
            lessFit = parent0;
        }
        Genome child = new Genome(fitter.ai, new Client(fitter.client.level, fitter.client.name + "1", null), fitter.species());
        for (Gene gene : fitter.genes.values()) {
            if (lessFit.containsGene(gene)) {
                child.addGene(Rng.random(new Gene[] {gene, lessFit.getGene(gene)}), fitter, lessFit);
                continue;
            }
            child.addGene(gene, fitter, lessFit);
        }
        child.mutateAddNode();
        child.mutateAddConnection();
        child.mutateChangeConnectionWeight();
        return child;
    }

    public double[] calculate(double[] inputs) {
        return new FeedBackward(this, this.ai.activation(), inputs).calculate();
    }

    public void mutateAddNode() {
        if (Rng.pass(this.ai.setting().MUTATION_ADD_NODE_CHANCE)) {
            Gene gene = Rng.random(new ArrayList<>(this.genes.values()));
            Node node = new Node(this.ai.idAssigner().nextId(), Type.HIDDEN);
            this.addGene(new Gene(this.ai.idAssigner().nextId(), gene.from(), node, 1.0D, true), null, null);
            this.addGene(new Gene(this.ai.idAssigner().nextId(), node, gene.to(), gene.weight(), true), null, null);
            gene.setActive(false);
        }
    }

    public void mutateAddConnection() {
        if (Rng.pass(this.ai.setting().MUTATION_ADD_CONNECTION_CHANCE)) {
            Set<Connection> priorConnections = this.connections();
            for (int i = 0; i <= 20; i++) {
                Node from = Rng.random(this.nodes(true, false, true));
                Node to = Rng.random(this.nodes(false, true, true));
                Connection connection = new Connection(from, to);
                if (from.equals(to) || priorConnections.contains(connection)) {
                    continue;
                }
                this.addGene(new Gene(this.ai.idAssigner().nextId(), from, to, Rng.random(-1.0D, 1.0D), true), null, null);
                return;
            }
            PvPBots.getInstance().getLogger().warning("[AI] -> mutateAddConnection: Failed to add getConnection after 20 attempts.");
        }
    }

    public void mutateChangeConnectionWeight() {
        if (Rng.pass(this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT)) {
            if (Rng.pass(this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM)) {
                for (Gene gene : this.genes.values()) {
                    double r = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
                    gene.setWeight(Rng.random(-r, r));
                }
            }
            else {
                for (Gene gene : this.genes.values()) {
                    double error = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_ERROR;
                    gene.setWeight(gene.weight() + Rng.random(-error, error));
                }
            }
        }
    }

    public double fitness() {
        return this.ai.fitness(this);
    }

    public Set<Connection> connections() {
        Set<Connection> connections = new HashSet<>();
        for (Gene gene : this.genes.values()) {
            connections.add(new Connection(gene.from(), gene.to()));
        }
        return connections;
    }

    public Set<Connection> connectionsActive() {
        Set<Connection> connections = new HashSet<>();
        for (Gene gene : this.genes.values()) {
            if (gene.isActive()) {
                connections.add(new Connection(gene.from(), gene.to()));
            }
        }
        return connections;
    }

    public double compatibilityDistance(Genome g0, Genome g1) {
        g0 = g0.copy();
        g1 = g1.copy();
        int g0l = g0.genes.size();
        int g1l = g1.genes.size();
        if (g0l < g1l) {
            Genome gTmp = g0;
            g0 = g1;
            g1 = gTmp;
        }
        int D = 0;
        int E = 0;
        double weight = 0.0D;
        int i = 0;
        int k = 0;
        for (Gene gene : g0.genes.values()) {
            Gene g1g = g1.getGene(gene);
            if (g1g == null) {
                if (i <= g1.genes.size()) {
                    D++;
                }
                else E++;
            }
            else {
                weight += Math.abs(gene.weight() - g1g.weight());
                k++;
            }
            i++;
        }
        double W = weight / k;
        double N = g0.genes.size();
        double C0 = this.ai.setting().COMP_DISTANCE_WEIGHT_DISJOINT;
        double C1 = this.ai.setting().COMP_DISTANCE_WEIGHT_EXCESS;
        double C2 = this.ai.setting().COMP_DISTANCE_WEIGHT_AVG_WEIGHT;
        // (C0 * E) / N + (C1 * D) / N + (C2 * W)
        return ((C0 * (double) E) / N) + ((C1 * (double) D) / N) + (C2 * W);
    }

    public Genome copy() {
        Genome genome = new Genome(this.ai, new Client(this.client.level, this.client.name + "2", null), this.species);
        for (Map.Entry<Integer, Gene> gene : this.genes.entrySet()) {
            genome.genes.put(gene.getKey(), gene.getValue().clone());
        }
        return genome;
    }

    public Collection<Gene> genes() {
        return this.genes.values();
    }

    public Gene getGene(Gene gene) {
        return this.genes.get(gene.id());
    }

    public boolean containsGene(Gene gene) {
        return this.genes.containsKey(gene.id());
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public int id() {
        return this.id;
    }

    public Client client() {
        return this.client;
    }

    public Species species() {
        return this.species;
    }

    public Set<Node> nodes(boolean includeInputs, boolean includeOutputs, boolean includeHidden) {
        Set<Node> nodes = new HashSet<>();
        for (Node node : this.allNodes()) {
            if ((!includeInputs && node.type() == Type.INPUT) || (!includeOutputs && node.type() == Type.OUTPUT) || (!includeHidden && node.type() == Type.HIDDEN)) {
                continue;
            }
            nodes.add(node);
        }
        return nodes;
    }

    public Set<Node> allNodes() {
        Set<Node> nodes = new HashSet<>();
        for (Gene gene : this.genes.values()) {
            nodes.add(gene.from());
            nodes.add(gene.to());
        }
        return nodes;
    }
}
