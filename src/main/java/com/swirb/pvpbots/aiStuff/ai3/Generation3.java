package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.mojang.datafixers.util.Pair;
import com.swirb.pvpbots.PvPBots;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Generation3 {

    private final int id;
    private final Set<Species3> species;
    private final IdSupplier3 idSupplier;

    public Generation3(int id) {
        this.id = id;
        this.species = new HashSet<>();
        this.idSupplier = new IdSupplier3();
    }

    public void addGenome(Genome3 genome) {
        this.evaluate(genome).genomes().add(genome);
    }

    public void respawnClients() {
        for (Species3 species : this.species) {
            for (Genome3 genome : species.genomes()) {
                PvPBots.getClientHandler().respawn(genome.client());
            }
        }
    }

    public void removeClients() {
        Set<String> stringUUIDs = new HashSet<>();
        for (Species3 species : this.species) {
            for (Genome3 genome : species.genomes()) {
                PvPBots.getClientHandler().disconnect(genome.client(), "");
                stringUUIDs.add(genome.client().getStringUUID() + ".dat");
                stringUUIDs.add(genome.client().getStringUUID() + ".dat_old");
            }
        }
        int i = (int) Arrays.stream(Objects.requireNonNull(((CraftServer) Bukkit.getServer()).getServer().playerDataStorage.getPlayerDir().listFiles())).filter(file -> stringUUIDs.contains(file.getName()) && file.delete()).count();
        PvPBots.getInstance().getLogger().info("[AI] -> removeClients: deleted " + i + " out of " + stringUUIDs.size() + " files");
    }

    public void update() {
        for (Species3 species : this.species) {
            species.update();
        }
    }

    public void calculateEntirely() {
        for (Species3 species : this.species) {
            species.calculateEntirely();
        }
    }

    public void applyInputs() {
        for (Species3 species : this.species) {
            species.applyInputs();
        }
    }

    public void applyOutputs() {
        for (Species3 species : this.species) {
            species.applyOutputs();
        }
    }

    public double avgFitness() {
        double sum = 0.0D;
        for (Species3 species : this.species) {
            sum += species.avgFitness();
        }
        return sum / this.species.size();
    }

    public Pair<Species3, Genome3> bestGenome() {
        Pair<Species3, Genome3> best = null;
        double fitness = 0.0D;
        for (Species3 species : this.species) {
            Genome3 genome = species.bestGenomes().get(0);
            double genomeF = genome.fitness();
            if (genomeF > fitness) {
                best = new Pair<>(species, genome);
                fitness = genomeF;
            }
        }
        if (best == null) {
            int i = 0;
            for (Species3 species : this.species) {
                i += species.genomes().size();
            }
            throw new AssertionError("[AI] -> bestGenome: could not find best genome of generation (species: " + this.species.size() + ", total genomes: " + i + ")");
        }
        return best;
    }

    private Species3 evaluate(Genome3 genome) {
        for (Species3 species : this.species) {
            if (species.isCompatible(genome)) {
                return species;
            }
        }
        Species3 s = new Species3(this.idSupplier.nextSpeciesId());
        this.species.add(s);
        return s;
    }

    public int id() {
        return this.id;
    }

    public Set<Species3> species() {
        return this.species;
    }

    public IdSupplier3 idSupplier() {
        return this.idSupplier;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("details:\n");
        for (Species3 species : this.species()) {
            stringBuilder.append("species ").append(species.id()).append(":\n");
            for (Genome3 genome : species.genomes()) {
                StringBuilder s = new StringBuilder("genome " + genome.id() + " [");
                s.append("genome ").append(genome.id()).append(": [");
                s.append("inputs: ").append(genome.inputs().stream().map(Node3::id).collect(Collectors.toSet())).append(" | ");
                s.append("outputs: ").append(genome.outputs().stream().map(Node3::id).collect(Collectors.toSet())).append(" | ");
                s.append("connections: ").append(genome.connections().size()).append(" | ");
                s.append("nodes: ").append(genome.nodes().size()).append(" | ");
                for (Connection3 connection : genome.connections()) {
                    s.append("getConnection ").append(connection.id()).append(" [");
                    s.append("from (").append(connection.from().id()).append(") [").append(connection.from().type().name()).append(", ").append(connection.from().value()).append("], ");
                    s.append("to (").append(connection.to().id()).append(") [").append(connection.to().type().name()).append(", ").append(connection.to().value()).append("]] | ");
                }
                s.append("\nnodes [");
                for (Node3 node : genome.nodes()) {
                    s.append("[").append(node.id()).append("]\n");
                }
                s.append("]");
                stringBuilder.append(s);
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public Generation3 copy() {
        Generation3 generation = new Generation3(this.id);
        generation.species.addAll(this.species.stream().map(Species3::copy).collect(Collectors.toSet()));
        return generation;
    }

    public boolean equals(Object o) {
        return o instanceof Generation3 && ((Generation3) o).id == this.id;
    }
}
