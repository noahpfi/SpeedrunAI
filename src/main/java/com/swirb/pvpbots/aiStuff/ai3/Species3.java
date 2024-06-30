package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.swirb.pvpbots.basicallyGarbage.ai.Rng;

import java.util.*;
import java.util.stream.Collectors;

public class Species3 {

    private final int id;
    private final Set<Genome3> genomes;
    private int generationsFailed;
    private double maxFitness;
    private Genome3 selected;

    public Species3(int id) {
        this.id = id;
        this.genomes = new HashSet<>();
        this.generationsFailed = 0;
        this.maxFitness = 0.0D;
        this.selected = null;
    }

    public void update() {
        this.selected = Rng.random(this.genomes);
        this.updateFitness();
        this.upFailed();
    }

    public void upFailed() {
        this.generationsFailed++;
    }

    public void updateFitness() {
        double fitness = 0.0D;
        for (Genome3 genome : this.genomes) {
            if (genome.fitness() > fitness) {
                fitness = genome.fitness();
            }
        }
        if (fitness > this.maxFitness) {
            this.generationsFailed = 0;
        }
        this.maxFitness = fitness;
    }

    public void calculateEntirely() {
        for (Genome3 genome : this.genomes) {
            genome.calculateEntirely();
        }
    }

    public void applyInputs() {
        for (Genome3 genome : this.genomes) {
            genome.applyInputs();
        }
    }

    public void applyOutputs() {
        for (Genome3 genome : this.genomes) {
            genome.applyOutputs();
        }
    }

    public List<Genome3> bestGenomes() {
        if (this.genomes.isEmpty()) {
            throw new AssertionError("[AI] bestGenomes: species has 0 genomes");
        }
        List<Genome3> best = new ArrayList<>(this.genomes);
        best.sort(Comparator.comparing(Genome3::fitness));
        return best;
    }

    public Genome3 getGenome(Genome3 genome) {
        for (Genome3 g : this.genomes) {
            if (g.equals(genome)) {
                return g;
            }
        }
        return null;
    }

    public double avgFitness() {
        double fitness = 0.0D;
        for (Genome3 genome : this.genomes) {
            fitness += genome.fitness();
        }
        return fitness / this.genomes.size();
    }

    public boolean isCompatible(Genome3 genome) {
        return this.selected.compatibilityDistance(genome) <= Setting3.COMP_DISTANCE_SPECIES_MAX;
    }

    public int id() {
        return this.id;
    }

    public Set<Genome3> genomes() {
        return this.genomes;
    }

    public int generationsFailed() {
        return this.generationsFailed;
    }

    public double maxFitness() {
        this.updateFitness();
        return this.maxFitness;
    }

    public Genome3 selected() {
        return this.selected;
    }

    public Species3 copy() {
        Species3 species = new Species3(this.id);
        species.genomes.addAll(this.genomes.stream().map(Genome3::copy).collect(Collectors.toSet()));
        species.generationsFailed = this.generationsFailed;
        species.maxFitness = this.maxFitness;
        species.selected = species.getGenome(this.selected);
        return species;
    }

    public boolean equals(Object o) {
        return o instanceof Species3 && ((Species3) o).id == this.id;
    }
}
