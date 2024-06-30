package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.ai.*;
import com.swirb.pvpbots.basicallyGarbage.ai.Rng;
import com.swirb.pvpbots.basicallyGarbage.ai.Type;
import com.swirb.pvpbots.client.Client;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

import java.util.*;

public class Genome2 {

    private final AI2 ai;
    private final Client client;
    private final int id;
    private final Map<Integer, Gene2> genes;
    private final List<Node2> inputs;
    private final List<Node2> outputs;

    public Genome2(AI2 ai, Client client) {
        this.ai = ai;
        this.client = client;
        this.id = this.ai.idAssigner().nextId();
        this.genes = new HashMap<>();
        this.inputs = new Inputs(this.ai).inputs();
        this.outputs = new Outputs(this.ai).outputs();
    }

    public void addGene(Gene2 gene, Genome2 parent0, Genome2 parent1) {
        if (gene == null) {
            throw new AssertionError("[AI] -> addGene: gene is null");
        }
        if (this.genes.containsKey(gene.id())) {
            throw new AssertionError("[AI] -> addGene: gene already exists");
        }
        if (parent0 != null && parent1 != null && parent0.containsGene(gene) && parent1.containsGene(gene)) {
            boolean p0d = parent0.getGene(gene).isActive();
            boolean p1d = parent1.getGene(gene).isActive();
            if ((p0d && !p1d) || (!p0d && p1d)) {
                gene.setActive(!Rng.pass(this.ai.setting().GENE_DISABLE_CHANCE));
            }
        }
        this.genes.put(gene.id(), gene);
    }

    public void calculateEntirely() {
        this.updateInputs();
        for (Node2 node : this.inputs) {
            node.calculateNext(this.ai.activation());
        }
    }

    public void applyOutputs() {
        if (this.outputs.size() != 5) {
            PvPBots.getInstance().getLogger().severe("[AI] -> applyOutputs: output size does not match up (size: " + outputs.size() + ")");
            return;
        }
        this.client.input.W = this.outputs.get(0).value() > 0.5D;
        this.client.input.A = this.outputs.get(1).value() > 0.5D;
        this.client.input.S = this.outputs.get(2).value() > 0.5D;
        this.client.input.D = this.outputs.get(3).value() > 0.5D;
        this.client.input.SPACE = this.outputs.get(4).value() > 0.5D;
    }

    public void updateInputs() {
        this.inputs.get(0).setValue(this.client.getX());
        this.inputs.get(1).setValue(this.client.getY());
        this.inputs.get(2).setValue(this.client.getZ());
        Entity attacker = this.client.nearest(5, 5, 5, entity -> entity instanceof Monster);
        this.inputs.get(3).setValue(attacker == null ? 0.0D : attacker.getX());
        this.inputs.get(4).setValue(attacker == null ? 0.0D : attacker.getY());
        this.inputs.get(5).setValue(attacker == null ? 0.0D : attacker.getZ());
        this.inputs.get(6).setValue(this.client.getHealth() / 20.F);
    }

    public static Genome2 crossProduct(Genome2 p0, Genome2 p1) {
        if (p0.genes.isEmpty() || p1.genes.isEmpty()) {
            PvPBots.getInstance().getLogger().severe("[AI] -> crossProduct: Genome is empty");
            return null;
        }
        double p0f = p0.fitness();
        double p1f = p1.fitness();
        Genome2 fitter;
        Genome2 lessFit;
        if (p0f > p1f) {
            fitter = p0;
            lessFit = p1;
        }
        else {
            fitter = p1;
            lessFit = p0;
        }
        Genome2 child = new Genome2(fitter.ai, new Client(fitter.client.level, fitter.id + "_cr", null));
        for (Gene2 gene : fitter.genes.values()) {
            if (lessFit.containsGene(gene)) {
                child.addGene(Rng.random(new Gene2[] {gene, lessFit.getGene(gene)}), fitter, lessFit);
                continue;
            }
            child.addGene(gene, fitter, lessFit);
        }
        child.mutateAddNode();
        child.mutateAddConnection();
        child.mutateChangeConnectionWeight();
        return child;
    }

    public void mutateAddNode() {
        if (Rng.pass(this.ai.setting().MUTATION_ADD_NODE_CHANCE)) {
            Gene2 gene = Rng.random(new ArrayList<>(this.genes.values()));
            Node2 node = new Node2(this.ai.idAssigner().nextId(), Type.HIDDEN);
            this.addGene(new Gene2(this.ai.idAssigner().nextId(), gene.from(), node, 1.0D, true), null, null);
            this.addGene(new Gene2(this.ai.idAssigner().nextId(), node, gene.to(), gene.weight(), true), null, null);
            gene.setActive(false);
        }
    }

    public void mutateAddConnection() {
        if (Rng.pass(this.ai.setting().MUTATION_ADD_CONNECTION_CHANCE)) {
            for (int i = 0; i <= 20; i++) {
                Node2 from = Rng.random(this.nodes(true, false, true));
                Node2 to = Rng.random(this.nodes(false, true, true));
                // from already has a getConnection, skip
                // to already will cause recursion
                if (from.gene() != null || from.willCauseRecursion(to)) {
                    continue;
                }
                this.addGene(new Gene2(this.ai.idAssigner().nextId(), from, to, Rng.random(-1.0D, 1.0D), true), null, null);
                return;
            }
            PvPBots.getInstance().getLogger().warning("[AI] -> mutateAddConnection: Failed to add getConnection after 20 attempts.");
        }
    }

    public void mutateChangeConnectionWeight() {
        if (Rng.pass(this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT)) {
            if (Rng.pass(this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM)) {
                for (Gene2 gene : this.genes.values()) {
                    double r = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
                    gene.setWeight(Rng.random(-r, r));
                }
            }
            else {
                for (Gene2 gene : this.genes.values()) {
                    double error = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_ERROR;
                    gene.setWeight(gene.weight() + Rng.random(-error, error));
                }
            }
        }
    }

    public Set<Node2> nodes(boolean includeInputs, boolean includeOutputs, boolean includeHidden) {
        Set<Node2> nodes = new HashSet<>();
        for (Node2 node : this.allNodes()) {
            if ((!includeInputs && node.type() == Type.INPUT) || (!includeOutputs && node.type() == Type.OUTPUT) || (!includeHidden && node.type() == Type.HIDDEN)) {
                continue;
            }
            nodes.add(node);
        }
        return nodes;
    }

    public Set<Node2> allNodes() {
        Set<Node2> nodes = new HashSet<>();
        for (Gene2 gene : this.genes.values()) {
            nodes.add(gene.from());
            nodes.add(gene.to());
        }
        return nodes;
    }

    public double compatibilityDistance(Genome2 genome) {
        Genome2 larger;
        Genome2 smaller;
        if (this.genes.size() < genome.genes.size()) {
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
        for (Gene2 gene : larger.genes.values()) {
            Gene2 g = smaller.getGene(gene);
            if (g == null) {
                if (i <= smaller.genes.size()) {
                    D++;
                }
                else E++;
            }
            else {
                weight += Math.abs(gene.weight() - g.weight());
                k++;
            }
            i++;
        }
        double W = weight / k;
        double N = larger.genes.size();
        double C0 = this.ai.setting().COMP_DISTANCE_WEIGHT_DISJOINT;
        double C1 = this.ai.setting().COMP_DISTANCE_WEIGHT_EXCESS;
        double C2 = this.ai.setting().COMP_DISTANCE_WEIGHT_AVG_WEIGHT;
        // (C0 * E) / N + (C1 * D) / N + (C2 * W)
        return ((C0 * (double) E) / N) + ((C1 * (double) D) / N) + (C2 * W);
    }

    public boolean containsGene(Gene2 gene) {
        return this.genes.containsKey(gene.id());
    }

    public Gene2 getGene(Gene2 gene) {
        return this.genes.get(gene.id());
    }

    public double fitness() {
        return this.ai.fitness(this);
    }

    public Client client() {
        return client;
    }

    public int id() {
        return this.id;
    }

    public List<Node2> inputs() {
        return this.inputs;
    }

    public List<Node2> outputs() {
        return this.outputs;
    }

    public Map<Integer, Gene2> genes() {
        return genes;
    }

    public Genome2 copy() {
        Genome2 genome = new Genome2(this.ai, new Client(this.ai.level(), this.id + "_c", null));
        genome.genes.putAll(this.genes);
        return genome;
    }
}
