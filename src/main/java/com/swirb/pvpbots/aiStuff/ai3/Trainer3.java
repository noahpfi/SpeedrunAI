package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.mojang.datafixers.util.Pair;
import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.basicallyGarbage.ai.Rng;
import com.swirb.pvpbots.basicallyGarbage.ai.Type;
import com.swirb.pvpbots.client.Client;

import java.util.*;

public class Trainer3 {

    private final AI3 ai;
    private int iteration;
    private Generation3 generation;

    public Trainer3(AI3 ai) {
        this.ai = ai;
        this.iteration = 0;
        this.generation = null;
    }

    public void setupFirstGeneration(int inputs, int outputs) {
        Generation3 generation = new Generation3(this.ai.idSupplier().nextGenerationId());
        for (int i = 0; i <= this.ai.generationSize(); i++) {
            Species3 species = new Species3(generation.idSupplier().nextSpeciesId());
            generation.species().add(species);

            int id = generation.idSupplier().nextGenomeId();
            Genome3 genome = new Genome3(id, new Client(this.ai.level(), String.valueOf(id), null), this.ai);
            species.genomes().add(genome);

            double range = Setting3.MUTATION_CHANGE_CONNECTION_WEIGHT_RANDOM_RANGE;

            for (int j = 0; j <= inputs; j++) {
                Node3 input = new Node3(genome.idSupplier().nextNodeId(), Type.INPUT, 0.0D);
                Node3 hidden = new Node3(genome.idSupplier().nextNodeId(), Type.HIDDEN, Rng.randomValue());
                Connection3 connection = new Connection3(genome.idSupplier().nextConnectionId(), input, hidden, Rng.random(-range, range), true);
                genome.addConnection(connection, null, null);
            }
            for (int j = 0; j <= outputs; j++) {
                Node3 output = new Node3(genome.idSupplier().nextNodeId(), Type.OUTPUT, 0.0D);
                Node3 hidden = new Node3(genome.idSupplier().nextNodeId(), Type.HIDDEN, Rng.randomValue());
                Connection3 connection = new Connection3(genome.idSupplier().nextConnectionId(), hidden, output, Rng.random(-range, range), true);
                genome.addConnection(connection, null, null);
            }
        }
        generation.respawnClients();
        this.generation = generation;
    }

    //TODO genomes sometimes don't have connections which should not happen
    public void nextGeneration() {
        Generation3 generation = new Generation3(this.ai.idSupplier().nextGenerationId());
        int size = 0;
        double avgFitness = this.generation.avgFitness();
        Map<Species3, Set<Genome3>> olds = new HashMap<>();
        for (Species3 species : this.generation.species()) {
            this.removeWorst(species);
            for (Genome3 genome : new HashSet<>(species.genomes())) {
                if (genome.connections().isEmpty()) {
                    species.genomes().remove(genome);
                }
            }
            if (species.genomes().isEmpty()) {
                PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: species died, no genomes left");
                continue;
            }
            if (species.generationsFailed() > Setting3.GENERATION_FAILS_MAX) {
                PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: species died, failed too man generations");
                olds.put(species, species.genomes());
                continue;
            }
            if ((avgFitness / this.ai.generationSize()) > species.avgFitness()) {
                PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: species died, worse than expected average fitness");
                olds.put(species, species.genomes());
                continue;
            }
            Genome3 best = species.bestGenomes().get(0);
            species.genomes().clear();
            species.genomes().add(best);
            generation.species().add(species);
            size++;
        }
        while (size < this.ai.generationSize()) {
            Genome3 genome;
            Species3 species;
            if (olds.keySet().isEmpty()) {
                species = Rng.random(generation.species());
            }
            else {
                species = Rng.random(olds.keySet());
            }
            if (Rng.pass(Setting3.GENERATION_COPY_CHANCE)) {
                genome = Rng.random(species.genomes());
                genome.mutateAddNode();
                genome.mutateAddConnection();
                genome.mutateChangeConnectionWeight();
            }
            else genome = Rng.random(species.genomes()).crossProduct(Rng.random(species.genomes()));
            generation.addGenome(genome);
            size++;
        }
        generation.update();
        int i = generation.species().stream().mapToInt(species -> species.genomes().size()).sum();
        Pair<Species3, Genome3> fittest = generation.bestGenome();
        generation.respawnClients();
        this.generation = generation;
        this.iteration++;
        PvPBots.getInstance().getLogger().info("[AI] -> nextGeneration: Best performing genome in species " + fittest.getFirst().id() + " with fitness of " + fittest.getSecond().fitness() + "(generation: " + this.iteration + ", species: " + this.generation.species().size() + ", genomes total: " + i + ")");
    }

    public void removeWorst(Species3 species) {
        List<Genome3> genomes = species.bestGenomes();
        System.out.println(species.genomes().size() + " BEFORE REMOVING WORST");
        if (genomes.isEmpty()) {
            throw new AssertionError("[AI] -> removeWorst: species doesn't have any genomes");
        }
        if (genomes.size() == 1) {
            System.out.println("skipping removing worst, only one genome");
            return;
        }
        for (Genome3 genome : genomes.subList(genomes.size() - (int) (Math.floor(genomes.size() * Setting3.GENERATION_DEATH_PERCENTAGE)), genomes.size())) {
            species.genomes().remove(genome);
        }
        System.out.println(species.genomes().size() + " AFTER REMOVING WORST");
    }

    public int iteration() {
        return this.iteration;
    }

    public Generation3 currentGeneration() {
        return this.generation;
    }

    public Pair<Species3, Genome3> fittest() {
        return generation.bestGenome();
    }
}
