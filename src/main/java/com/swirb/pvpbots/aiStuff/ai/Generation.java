package com.swirb.pvpbots.basicallyGarbage.ai;

import java.util.HashSet;
import java.util.Set;

public class Generation {

    private final AI ai;
    private final Set<Species> species;

    public Generation(AI ai) {
        this.ai = ai;
        this.species = new HashSet<>();
    }

    public void addGenome(Genome genome) {
        this.evaluate(genome).addGenome(genome);
    }

    private Species evaluate(Genome genome) {
        for (Species species : this.species) {
            if (species.compatible(genome)) {
                genome.setSpecies(species);
                return species;
            }
        }
        Species s = new Species(this.ai, genome);
        this.species.add(s);
        return s;
    }

    public void updateFitness() {
        for (Species species : this.species) {
            species.updateFitness();
        }
    }

    public void upFailed() {
        for (Species species : this.species) {
            species.failed();
        }
    }

    public Genome best() {
        Genome best = null;
        double fitness = 0.0D;
        for (Species species : this.species) {
            Genome genome = species.best().get(0);
            if (genome.fitness() > fitness) {
                best = genome;
                fitness = genome.fitness();
            }
        }
        return best;
    }

    public Set<Species> species() {
        return this.species;
    }
}
