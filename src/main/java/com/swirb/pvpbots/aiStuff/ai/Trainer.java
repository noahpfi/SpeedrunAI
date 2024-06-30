package com.swirb.pvpbots.basicallyGarbage.ai;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.client.Client;

import java.util.*;

public class Trainer {

    private final AI ai;
    private int generation;
    private int generationSize;
    private Generation currentGeneration;
    private Genome lastFittest;

    public Trainer(AI ai) {
        this.ai = ai;
        this.generation = 0;
        this.generationSize = 0;
        this.currentGeneration = new Generation(this.ai);
        this.lastFittest = null;
    }

    public void nextGeneration() {
        this.generation++;
        this.currentGeneration.updateFitness();
        this.currentGeneration.upFailed();
        for (Species species : this.currentGeneration.species()) {
            for (Genome genome : species.genomes()) {
                PvPBots.getClientHandler().disconnect(genome.client(), "new Generation");
            }
        }
        double sum = 0.0D;
        for (Species species : this.currentGeneration.species()) {
            sum += species.avgFitness();
        }
        Map<Species, Genome> selected = new HashMap<>();
        Set<Species> toBeRemoved = new HashSet<>();
        for (Species species : this.currentGeneration.species()) {
            //this.removeWorst(species, species.best());
            if (species.generationsFailed() > this.ai.setting().GENERATION_FAILS_MAX) {
                toBeRemoved.add(species);
            }
            else if (Math.floor(species.avgFitness() / sum * this.generationSize) < 1.0D) {
                toBeRemoved.add(species);
            }
            if (!species.best().isEmpty()) {
                selected.put(species, species.best().get(0));
            }
        }
        this.currentGeneration.species().removeAll(toBeRemoved);
        int i = 0;
        for (Species species : this.currentGeneration.species()) {
            i += species.genomes().size();
        }
        PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: Building generation " + this.generation + " with " + this.currentGeneration.species().size() + "active (total genomes: " + i + ")");
        if (this.currentGeneration.species().isEmpty()) {
            PvPBots.getInstance().getLogger().severe("[AI] -> nextGeneration: entire generation died.");
            return;
        }

        int size = 0;
        Map<Species, Set<Genome>> olds = new HashMap<>();
        for (Species species : this.currentGeneration.species()) {
            olds.put(species, new HashSet<>(species.genomes()));
            species.clearGenomes();
            Genome s = selected.get(species);
            if (s == null) {
                continue;
            }
            species.addGenome(s);
            size++;
        }
        while (size < this.generationSize) {
            Species r = Rng.random(this.currentGeneration.species());
            Set<Genome> old = olds.get(r);
            if (old == null) {
                continue;
            }
            Genome genome;
            if (Rng.pass(this.ai.setting().GENERATION_COPY_CHANCE)) {
                genome = Rng.random(old).copy();
                genome.mutateAddNode();
                genome.mutateAddConnection();
                genome.mutateChangeConnectionWeight();
            }
            else genome = Genome.crossProduct(Rng.random(old), Rng.random(old));
            if (genome != null) {
                r.addGenome(genome);
            }
            size++;
        }
        this.currentGeneration.species().removeIf(species -> species.genomes().isEmpty());
        for (Species species : this.currentGeneration.species()) {
            species.update();
        }
        this.lastFittest = this.currentGeneration.best();
        PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: Best performing genome's fitness: " + this.lastFittest.fitness() + ", species: " + this.lastFittest.species().id());
    }

    public void setup(int generationSize) {
        this.generationSize = generationSize;
        if (this.generation != 0) {
            return;
        }
        Genome start = this.startGenome();
        for (int i = 0; i <= this.generationSize; i++) {
            Genome genome = start.copy();
            for (Gene gene : genome.genes()) {
                double range = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
                gene.setWeight(Rng.random(-range, range));
            }
            this.currentGeneration.addGenome(genome);
        }
    }

    private Genome startGenome() {
        Genome genome = new Genome(this.ai, new Client(this.ai.level(), "AI", null), null);
        double range = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
        for (int i = 0; i <= this.ai.inputSize(); i++) {
            genome.addGene(new Gene(this.ai.idAssigner().nextId(), new Node(this.ai.idAssigner().nextId(), Type.INPUT), new Node(this.ai.idAssigner().nextId(), Type.HIDDEN), Rng.random(-range, range), true), null, null);
        }
        for (int i = 0; i <= this.ai.outputSize(); i++) {
            genome.addGene(new Gene(this.ai.idAssigner().nextId(), new Node(this.ai.idAssigner().nextId(), Type.HIDDEN), new Node(this.ai.idAssigner().nextId(), Type.OUTPUT), Rng.random(-range, range), true), null, null);
        }
        return genome;
    }

    private void removeWorst(Species species, List<Genome> best) {
        List<Genome> genomes = new ArrayList<>(best);
        genomes.subList(0, (int) Math.floor((best.size()) * this.ai.setting().GENERATION_DEATH_PERCENTAGE)).clear();
        for (Genome genome : genomes) {
            species.removeGenome(genome);
        }
    }

    public int generation() {
        return this.generation;
    }

    public Generation currentGeneration() {
        return this.currentGeneration;
    }

    public Genome lastFittest() {
        return this.lastFittest;
    }
}
