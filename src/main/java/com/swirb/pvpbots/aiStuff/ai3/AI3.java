package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.basicallyGarbage.ai.Activation;
import net.minecraft.world.level.Level;

public class AI3 {

    private final Level level;
    private final Activation activation;
    private final int generationSize;
    private final Trainer3 trainer;
    private final IdSupplier3 idSupplier;
    private int ticks;

    public AI3(Level level, Activation activation, int generationSize) {
        this.level = level;
        this.activation = activation;
        this.generationSize = generationSize;
        this.trainer = new Trainer3(this);
        this.idSupplier = new IdSupplier3();
        this.ticks = -1;
    }

    public boolean train(double targetFitness) {
        if (this.ticks == -1) {
            this.ticks = 0;
            this.trainer.setupFirstGeneration(5, 4);
        }
        else if (this.ticks == 300) {
            this.ticks = 0;
            this.trainer.nextGeneration();
        }
        long stamp = System.currentTimeMillis();
        this.trainer.currentGeneration().applyInputs();
        this.trainer.currentGeneration().calculateEntirely();
        this.trainer.currentGeneration().applyOutputs();
        System.out.println("updated values (" + (System.currentTimeMillis() - stamp) + "ms)");

        Genome3 best = this.trainer.fittest().getSecond();
        if (best.fitness() >= targetFitness) {
            PvPBots.getInstance().getLogger().info("[AI] -> train: target fitness reached! (" + best.fitness() + ") took " + this.trainer.iteration() + " generations.");
            return true;
        }
        this.ticks++;
        return false;
    }

    public Level level() {
        return this.level;
    }

    public Activation activation() {
        return this.activation;
    }

    public int generationSize() {
        return this.generationSize;
    }

    public Trainer3 trainer() {
        return this.trainer;
    }

    public IdSupplier3 idSupplier() {
        return idSupplier;
    }
}
