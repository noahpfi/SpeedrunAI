package com.swirb.speedrunai.neat;

public class Innovation {

    private int innovationConnection;
    private int innovationNode;

    public Innovation() {
        this.innovationConnection = 0;
        this.innovationNode = 0;
    }

    public int innovationConnection() {
        this.innovationConnection++;
        return this.innovationConnection;
    }

    public int innovationNode() {
        this.innovationNode++;
        return this.innovationNode;
    }
}
