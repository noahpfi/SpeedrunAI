package com.swirb.pvpbots.basicallyGarbage.ai;

import com.swirb.pvpbots.PvPBots;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.bukkit.scheduler.BukkitRunnable;

public class AI {

    private final Level level;
    private final int inputSize;
    private final int outputSize;
    private final Activation activation;
    private final Setting setting;
    private final IdAssigner idAssigner;
    private final Trainer trainer;

    public AI(Level level, int inputSize, int outputSize, Activation activation) {
        this.level = level;
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.activation = activation;
        this.setting = new Setting();
        this.idAssigner = new IdAssigner();
        this.trainer = new Trainer(this);
    }

    public void train(int generationSize, double targetFitness) {
        this.trainer.setup(generationSize);
        new BukkitRunnable() {
            public void run() {
                AI.this.trainer.nextGeneration();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        long stamp = System.currentTimeMillis();
                        while (System.currentTimeMillis() - stamp < 30) {
                            AI.this.trainer.currentGeneration().updateFitness();
                            Genome best = AI.this.trainer.lastFittest();
                            // show generation
                            if (best.fitness() >= targetFitness) {
                                PvPBots.getInstance().getLogger().info("[AI] -> train: target fitness reached! (" + best.fitness() + ") took " + AI.this.trainer.generation() + " generations.");
                                return;
                            }
                        }
                    }
                }.runTaskTimer(PvPBots.getInstance(), 0, 0);
            }
        }.runTaskTimer(PvPBots.getInstance(), 0, 200);
    }

    public double fitness(Genome genome) {
        // inputs: x y z of client + x y z of attacker + health
        // outputs: w a s d space
        Entity attacker = genome.client().nearest(5, 5, 5, entity -> entity instanceof Monster);
        double[] inputs = new double[7];
        inputs[0] = genome.client().getX();
        inputs[1] = genome.client().getY();
        inputs[2] = genome.client().getZ();
        inputs[3] = attacker == null ? 0.0D : attacker.getX();
        inputs[4] = attacker == null ? 0.0D : attacker.getY();
        inputs[5] = attacker == null ? 0.0D : attacker.getZ();
        inputs[6] = genome.client().getHealth() / 20.0F;
        double[] outputs = genome.calculate(inputs);
        genome.client().input.W = outputs[0] > 0.5;
        genome.client().input.A = outputs[1] > 0.5;
        genome.client().input.S = outputs[2] > 0.5;
        genome.client().input.D = outputs[3] > 0.5;
        genome.client().input.SPACE = outputs[4] > 0.5;
        return genome.client().isAlive() ? genome.client().getHealth() * genome.client().tickCount + 1 : 0.0D;
    }

    public Level level() {
        return this.level;
    }

    public int inputSize() {
        return this.inputSize;
    }

    public int outputSize() {
        return this.outputSize;
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
}
