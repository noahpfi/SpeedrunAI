package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.PvPBots;
import com.swirb.pvpbots.basicallyGarbage.ai.Activation;
import com.swirb.pvpbots.basicallyGarbage.ai.IdAssigner;
import com.swirb.pvpbots.basicallyGarbage.ai.Setting;
import net.minecraft.world.level.Level;

public class AI2 {

    private final Level level;
    private final Activation activation;
    private final Setting setting;
    private final IdAssigner idAssigner;
    private final Trainer2 trainer;
    private int ticks;

    public AI2(Level level, int initialSize, Activation activation) {
        this.level = level;
        this.activation = activation;
        this.setting = new Setting();
        this.idAssigner = new IdAssigner();
        this.trainer = new Trainer2(this, initialSize);
        this.ticks = -1;
    }

    public boolean train(double targetFitness) {
        if (this.ticks == -1) {
            this.trainer.setup();
        }
        else if (this.trainer.currentGeneration().allClientsDead()) {
            this.trainer.nextGeneration();
        }
        this.trainer.currentGeneration().calculateEntirely();
        this.trainer.currentGeneration().applyOutputs();

        this.trainer.currentGeneration().updateFitness();
        Genome2 best = AI2.this.trainer.fittest().getSecond();
        if (best.fitness() >= targetFitness) {
            PvPBots.getInstance().getLogger().info("[AI] -> train: target fitness reached! (" + best.fitness() + ") took " + this.trainer.iteration() + " generations.");
            return true;
        }
        this.ticks++;
        return false;
    }

    public double fitness(Genome2 genome) {
        return genome.client().getHealth() * (genome.client().tickCount + 1);
    }

    public Level level() {
        return this.level;
    }

    public Activation activation() {
        return this.activation;
    }

    public Setting setting() {
        return this.setting;
    }

    public IdAssigner idAssigner() {
        return this.idAssigner;
    }

    public Trainer2 trainer() {
        return this.trainer;
    }
}
