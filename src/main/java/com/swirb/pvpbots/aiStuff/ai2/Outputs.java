package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.basicallyGarbage.ai.Type;

import java.util.List;

public class Outputs {

    public Node2 outputW;
    public Node2 outputA;
    public Node2 outputS;
    public Node2 outputD;
    public Node2 outputSP;

    public Outputs(AI2 ai) {
        this.outputW = new Node2(ai.idAssigner().nextId(), Type.OUTPUT);
        this.outputA = new Node2(ai.idAssigner().nextId(), Type.OUTPUT);
        this.outputS = new Node2(ai.idAssigner().nextId(), Type.OUTPUT);
        this.outputD = new Node2(ai.idAssigner().nextId(), Type.OUTPUT);
        this.outputSP = new Node2(ai.idAssigner().nextId(), Type.OUTPUT);
    }

    public List<Node2> outputs() {
        return List.of(this.outputW, this.outputA, this.outputS, this.outputD, this.outputSP);
    }
}
