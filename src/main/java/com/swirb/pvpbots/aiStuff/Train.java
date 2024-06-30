package com.swirb.pvpbots.basicallyGarbage;

import com.swirb.pvpbots.client.Client;
import com.swirb.pvpbots.basicallyGarbage.nn.NeuralNetwork;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class Train extends Client {

    // this actually works I think, they don't learn anything tho (or I didn't let it run for long enough idk man)
    // to recreate: set up a box at world spawn, put some zombies inside and start it

    private final NeuralNetwork network;

    public Train(Level level) {
        super(level, "AI", null);
        this.network = new NeuralNetwork(4, 10, 4);
    }

    public void tick() {
        Entity attacker = this.nearest(50, 50, 50, Objects::nonNull);
        if (attacker == null) {
            return;
        }

        double[][] trainInputs = new double[][]{{this.getX(), this.getZ(), attacker.getX(), attacker.getZ()}};
        double[] inputs = new double[]{this.getX(), this.getZ(), attacker.getX(), attacker.getZ()};

        double deltaX = this.getX() - attacker.getX();
        double deltaZ = this.getZ() - attacker.getZ();
        int w = 0;
        int a = 0;
        int s = 0;
        int d = 0;
        if (this.getDirection() == Direction.NORTH) {
            if (deltaZ > 0) {
                s = 1;
            }
            else w = 1;
            if (deltaX > 0) {
                d = 1;
            }
            else a = 1;
        }
        else if (this.getDirection() == Direction.SOUTH) {
            if (deltaZ > 0) {
                w = 1;
            }
            else s = 1;
            if (deltaX > 0) {
                a = 1;
            }
            else d = 1;
        }
        else if (this.getDirection() == Direction.EAST) {
            if (deltaZ > 0) {
                d = 1;
            }
            else a = 1;
            if (deltaX > 0) {
                w = 1;
            }
            else s = 1;
        }
        else if (this.getDirection() == Direction.WEST) {
            if (deltaZ > 0) {
                a = 1;
            }
            else d = 1;
            if (deltaX > 0) {
                s = 1;
            }
            else w = 1;
        }
        double[][] expectedOutputs = new double[][]{{w, a, s, d}}; ///w, a, s, d

        long stamp = System.currentTimeMillis();
        this.network.fit(trainInputs, expectedOutputs, 1000);
        System.out.println((System.currentTimeMillis() - stamp) + "ms");

        List<Double> output = network.predict(inputs);
        System.out.println(output.toString());
        this.input.W = output.get(0) < 0.5;
        this.input.A = output.get(1) < 0.5;
        this.input.S = output.get(2) < 0.5;
        this.input.D = output.get(3) < 0.5;
        super.tick();
    }
}
