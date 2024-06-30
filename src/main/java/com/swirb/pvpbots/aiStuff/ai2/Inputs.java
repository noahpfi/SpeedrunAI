package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.basicallyGarbage.ai.Type;

import java.util.List;

public class Inputs {

    public Node2 inputCX;
    public Node2 inputCY;
    public Node2 inputCZ;
    public Node2 inputAX;
    public Node2 inputAY;
    public Node2 inputAZ;
    public Node2 inputCH;

    public Inputs(AI2 ai) {
        this.inputCX = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputCY = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputCZ = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputAX = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputAY = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputAZ = new Node2(ai.idAssigner().nextId(), Type.INPUT);
        this.inputCH = new Node2(ai.idAssigner().nextId(), Type.INPUT);
    }

    public List<Node2> inputs() {
        return List.of(this.inputCX, this.inputCY, this.inputCZ, this.inputAX, this.inputAY, this.inputAZ, this.inputCH);
    }
}
