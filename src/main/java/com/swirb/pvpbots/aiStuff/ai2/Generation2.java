package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.mojang.datafixers.util.Pair;
import com.swirb.pvpbots.PvPBots;

import java.util.*;

public class Generation2 {

    private final AI2 ai;
    private final Set<Species2> species;

    public Generation2(AI2 ai) {
        this.ai = ai;
        this.species = new HashSet<>();
    }

    public void addGenome(Genome2 genome) {
        this.evaluate(genome).addGenome(genome);
    }

    public void addSpecies(Species2 species) {
        this.species.add(species);
    }

    public void removeSpecies(Species2 species2) {
        this.species.remove(species2);
    }

    private Species2 evaluate(Genome2 genome) {
        for (Species2 species : this.species) {
            if (species.isCompatible(genome)) {
                return species;
            }
        }
        Species2 s = new Species2(this.ai, genome);
        this.species.add(s);
        return s;
    }

    public void updateFitness() {
        for (Species2 species : this.species) {
            species.updateFitness();
        }
    }

    public void calculateEntirely() {
        for (Species2 species : this.species) {
            species.calculateEntirely();
        }
    }

    public void applyOutputs() {
        for (Species2 species : this.species) {
            species.applyOutputs();
        }
    }

    public void respawnClients() {
        for (Species2 species : this.species) {
            for (Genome2 genome : species.genomes()) {
                PvPBots.getClientHandler().respawn(genome.client());
            }
        }
    }

    public boolean allClientsDead() {
        for (Species2 species : this.species) {
            for (Genome2 genome : species.genomes()) {
                if (genome.client().getHealth() != 0.0F) {
                    return false;
                }
            }
        }
        return true;
    }

    public Pair<Species2, Genome2> bestGenome() {
        Pair<Species2, Genome2> best = null;
        double fitness = 0.0D;
        for (Species2 species : this.species) {
            Genome2 genome = species.bestGenomes().get(0);
            double genomeF = genome.fitness();
            if (genomeF > fitness) {
                best = new Pair<>(species, genome);
                fitness = genomeF;
            }
        }
        if (best == null) {
            int i = 0;
            for (Species2 species : this.species) {
                i += species.genomes().size();
            }
            throw new AssertionError("[AI] -> bestGenome: could not find best genome of generation (species: " + this.species.size() + ", total genomes: " + i + ")");
        }
        return best;
    }

    public Species2 getSpecies(Species2 species) {
        for (Species2 s : this.species) {
            if (s.id() == species.id()) {
                return s;
            }
        }
        return null;
    }

    public Set<Species2> species() {
        return this.species;
    }
}
