package com.swirb.pvpbots.basicallyGarbage.ai;

import com.swirb.pvpbots.PvPBots;

import java.util.HashMap;
import java.util.Map;

public class FeedBackward {

    private final Genome genome;
    private final Activation activation;
    private final Map<Node, Double> inputs;

    public FeedBackward(Genome genome, Activation activation, double[] inputs) {
        this.genome = genome;
        this.activation = activation;
        this.inputs = new HashMap<>();
        if (genome.nodes(true, false, false).size() != inputs.length) {
            PvPBots.getInstance().getLogger().severe("[AI] -> feedBackward: inputs don't match up.");
            return;
        }
        int i = 0;
        for (Node node : this.genome.nodes(true, false, false)) {
            this.inputs.put(node, inputs[i]);
            i++;
        }
    }

    public double[] calculate() {
        int i = 0;
        double[] output = new double[this.genome.nodes(true, false, false).size()];
        for (Node node : this.genome.nodes(false, true, false)) {
            output[i] = this.output(node);
            i++;
        }
        return output;
    }

    public double output(Node node) {
        double sum = 0.0D;
        for (Gene gene : this.genome.genes()) {
            if (gene.to().equals(node) && gene.isActive()) {
                if (gene.from().type() == Type.INPUT) {
                    sum += this.inputs.get(gene.from()) * gene.weight();
                    continue;
                }
                sum += this.output(gene.from()) * gene.weight();
            }
        }
        return this.activation.activate(sum);
    }
}
