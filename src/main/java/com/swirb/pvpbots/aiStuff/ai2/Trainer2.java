package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.mojang.datafixers.util.Pair;
import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.ai.*;
import com.swirb.pvpbots.basicallyGarbage.ai.Rng;
import com.swirb.pvpbots.basicallyGarbage.ai.Type;
import com.swirb.pvpbots.client.Client;

import java.util.*;

public class Trainer2 {

    private final AI2 ai;
    private final int generationSize;
    private int iteration;
    private Pair<Species2, Genome2> fittest;
    private final Generation2 gen;

    public Trainer2(AI2 ai, int generationSize) {
        this.ai = ai;
        this.generationSize = generationSize;
        this.iteration = 0;
        this.fittest = null;
        this.gen = new Generation2(this.ai);
    }

    public void setup() {
        if (this.iteration != 0) {
            return;
        }
        Genome2 start = this.startGenome();
        System.out.println("START GENOME: " + start.genes().values());
        for (int i = 0; i <= this.generationSize; i++) {
            Genome2 genome = start.copy();
            System.out.println("INIT GENOME: " + genome.genes().values());
            for (Gene2 gene : genome.genes().values()) {
                double range = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
                gene.setWeight(Rng.random(-range, range));
            }
            this.gen.addGenome(genome);
        }
    }

    private Genome2 startGenome() {
        Genome2 genome = new Genome2(this.ai, new Client(this.ai.level(), "AI", null));
        double range = this.ai.setting().MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;
        for (Node2 node : genome.inputs()) {
            Node2 to = new Node2(this.ai.idAssigner().nextId(), Type.HIDDEN);
            Gene2 toAdd = new Gene2(this.ai.idAssigner().nextId(), node, to, Rng.random(-range, range), true);  // set node's gene to the new gene
            genome.addGene(toAdd, null, null);
        }
        for (Node2 node : genome.outputs()) {
            Node2 from = new Node2(this.ai.idAssigner().nextId(), Type.HIDDEN);
            Gene2 toAdd = new Gene2(this.ai.idAssigner().nextId(), from, node, Rng.random(-range, range), true);    // set from's gene to the new gene
            genome.addGene(toAdd, null, null);
        }
        return genome;
    }

    public void nextGeneration() {
        this.iteration++;
        this.gen.respawnClients();
        double sum = 0.0D;
        Map<Species2, Set<Genome2>> olds = new HashMap<>();
        for (Species2 species : this.gen.species()) {
            sum += species.avgFitness();
            olds.put(species, species.genomes());
        }
        int size = 0;
        for (Species2 species : new HashSet<>(this.gen.species())) {
            this.removeWorst(this.gen.getSpecies(species));
            if (species.generationsFailed() > this.ai.setting().GENERATION_FAILS_MAX) {
                this.gen.removeSpecies(species);
            }
            if (Math.floor(species.avgFitness() / sum * this.generationSize) < 1.0D) {
                this.gen.removeSpecies(species);
            }
            // selected Genome of this species
            if (species.genomes().isEmpty()) {
                System.out.println("GENOME EMPTY");
                continue;
            }
            Genome2 selected = species.bestGenomes().get(0);
            species.clearGenomes();
            species.addGenome(selected);
            size++;
        }
        while (size < this.generationSize) {
            Species2 randomSpecies = Rng.random(this.gen.species());
            Set<Genome2> oldGenomes = olds.get(randomSpecies);
            if (oldGenomes == null) {
                continue;
            }
            Genome2 genome;
            if (Rng.pass(this.ai.setting().GENERATION_COPY_CHANCE)) {
                genome = Rng.random(oldGenomes).copy();
                genome.mutateAddNode();
                genome.mutateAddConnection();
                genome.mutateChangeConnectionWeight();
            }
            else genome = Genome2.crossProduct(Rng.random(oldGenomes), Rng.random(oldGenomes));
            if (genome != null) {
                randomSpecies.addGenome(genome);
            }
            size++;
        }
        this.gen.species().removeIf(species -> species.genomes().isEmpty());
        int i = 0;
        for (Species2 species : this.gen.species()) {
            species.update();
            species.updateFitness();
            species.upFailed();
            i += species.genomes().size();
        }
        this.fittest = this.gen.bestGenome();
        PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: Best performing genome in species " + this.fittest.getFirst().id() + " with fitness of " + this.fittest.getSecond().fitness() + "(generation: " + this.iteration + ", species: " + this.gen.species().size() + ", genomes total: " + i + ")");
    }

    public void removeWorst(Species2 species) {
        List<Genome2> genomes = species.bestGenomes();
        System.out.println(species.genomes().size() + "BEFORE REMOVING WORST");
        if (genomes.isEmpty()) {
            throw new AssertionError("[AI] -> removeWorst: species doesn't have any genomes");
        }
        if (genomes.size() == 1) {
            System.out.println("skipping removing worst, only one genome");
            return;
        }
        for (Genome2 genome : genomes.subList((int) Math.ceil(genomes.size() - Math.floor(genomes.size() * this.ai.setting().GENERATION_DEATH_PERCENTAGE)), genomes.size())) {
            species.removeGenome(genome);
        }
        System.out.println(species.genomes().size() + "AFTER REMOVING WORST");
    }

    public int generationSize() {
        return this.generationSize;
    }

    public int iteration() {
        return this.iteration;
    }

    public Pair<Species2, Genome2> fittest() {
        return this.fittest;
    }

    public Generation2 currentGeneration() {
        return this.gen;
    }
}
