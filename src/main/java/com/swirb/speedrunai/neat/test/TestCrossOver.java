package com.swirb.speedrunai.neat.test;

import com.swirb.speedrunai.neat.Connection;
import com.swirb.speedrunai.neat.Genome;
import com.swirb.speedrunai.neat.Node;

import java.util.ArrayList;
import java.util.Random;

public class TestCrossOver {

    public static void main(String[] args) {
        Genome parent1 = new Genome();

        Node output = new Node(Node.TYPE.OUTPUT, 4);
        Node hidden = new Node(Node.TYPE.HIDDEN, 5);
        Node in1 = new Node(Node.TYPE.INPUT, 1);
        Node in2 = new Node(Node.TYPE.INPUT, 2);
        Node in3 = new Node(Node.TYPE.INPUT, 3);

        parent1.addNode(output);
        parent1.addNode(hidden);
        parent1.addNode(in1);
        parent1.addNode(in2);
        parent1.addNode(in3);

        parent1.addConnection(new Connection(in1, output, new Random().nextFloat(), true, 1));
        parent1.addConnection(new Connection(in2, output, new Random().nextFloat(), true, 2));
        parent1.addConnection(new Connection(in3, output, new Random().nextFloat(), true, 3));
        parent1.addConnection(new Connection(in2, hidden, new Random().nextFloat(), true, 4));
        parent1.addConnection(new Connection(hidden, output, new Random().nextFloat(), true, 5));
        parent1.addConnection(new Connection(in1, hidden, new Random().nextFloat(), true, 8));

        Genome parent2 = new Genome();

        Node outp = new Node(Node.TYPE.OUTPUT, 4);
        Node hid1 = new Node(Node.TYPE.HIDDEN, 5);
        Node hid2 = new Node(Node.TYPE.HIDDEN, 6);
        Node inp1 = new Node(Node.TYPE.INPUT, 1);
        Node inp2 = new Node(Node.TYPE.INPUT, 2);
        Node inp3 = new Node(Node.TYPE.INPUT, 3);

        parent2.addNode(outp);
        parent2.addNode(hid1);
        parent2.addNode(hid2);
        parent2.addNode(inp1);
        parent2.addNode(inp2);
        parent2.addNode(inp3);

        parent2.addConnection(new Connection(inp1, outp, 1f, true, 1));
        parent2.addConnection(new Connection(inp2, outp, 1f, false, 2));
        parent2.addConnection(new Connection(inp3, outp, 1f, true, 3));
        parent2.addConnection(new Connection(inp2, hid1, 1f, true, 4));
        parent2.addConnection(new Connection(hid1, outp, 1f, false, 5));
        parent2.addConnection(new Connection(hid1, hid2, 1f, true, 6));
        parent2.addConnection(new Connection(hid2, outp, 1f, true, 7));
        parent2.addConnection(new Connection(inp3, hid1, 1f, true, 9));
        parent2.addConnection(new Connection(inp1, hid2, 1f, true, 10));

        Genome parent3 = new Genome();

        Node out = new Node(Node.TYPE.OUTPUT, 4);
        Node out2 = new Node(Node.TYPE.OUTPUT, 13);
        Node hidden1 = new Node(Node.TYPE.HIDDEN, 5);
        Node hidden2 = new Node(Node.TYPE.HIDDEN, 6);
        Node hidden3 = new Node(Node.TYPE.HIDDEN, 7);
        Node hidden4 = new Node(Node.TYPE.HIDDEN, 8);
        Node input1 = new Node(Node.TYPE.INPUT, 1);
        Node input2 = new Node(Node.TYPE.INPUT, 2);
        Node input3 = new Node(Node.TYPE.INPUT, 3);
        Node input4 = new Node(Node.TYPE.INPUT, 9);
        Node input5 = new Node(Node.TYPE.INPUT, 10);
        Node input6 = new Node(Node.TYPE.INPUT, 11);
        Node input7 = new Node(Node.TYPE.INPUT, 12);
        Node input8 = new Node(Node.TYPE.INPUT, 14);
        Node input9 = new Node(Node.TYPE.INPUT, 15);
        Node input10 = new Node(Node.TYPE.INPUT, 16);
        Node input11 = new Node(Node.TYPE.INPUT, 17);
        Node input12 = new Node(Node.TYPE.INPUT, 18);

        parent3.addNode(out);
        parent3.addNode(out2);
        parent3.addNode(hidden1);
        parent3.addNode(hidden2);
        parent3.addNode(hidden3);
        parent3.addNode(hidden4);
        parent3.addNode(input1);
        parent3.addNode(input2);
        parent3.addNode(input3);
        parent3.addNode(input4);
        parent3.addNode(input5);
        parent3.addNode(input6);
        parent3.addNode(input7);
        parent3.addNode(input8);
        parent3.addNode(input9);
        parent3.addNode(input10);
        parent3.addNode(input11);
        parent3.addNode(input12);

        parent3.addConnection(new Connection(input1, out, 1f, true, 1));
        parent3.addConnection(new Connection(input4, out, 1f, false, 2));
        parent3.addConnection(new Connection(input3, out2, 1f, true, 3));
        parent3.addConnection(new Connection(input2, hidden1, 1f, true, 4));
        parent3.addConnection(new Connection(hidden1, out, 1f, false, 5));
        parent3.addConnection(new Connection(input3, hidden2, 1f, true, 6));
        parent3.addConnection(new Connection(hidden2, out, 1f, true, 7));
        parent3.addConnection(new Connection(input5, hidden1, 1f, true, 9));
        parent3.addConnection(new Connection(input1, hidden3, 1f, true, 10));

        parent3.addConnection(new Connection(hidden3, hidden4, 1f, true, 11));
        parent3.addConnection(new Connection(input6, hidden2, 1f, true, 12));
        parent3.addConnection(new Connection(input1, out2, 1f, true, 13));
        parent3.addConnection(new Connection(hidden2, hidden3, 1f, true, 14));
        parent3.addConnection(new Connection(hidden4, hidden1, 1f, true, 15));
        parent3.addConnection(new Connection(input4, out, 1f, true, 16));
        parent3.addConnection(new Connection(input2, hidden1, 1f, true, 17));

        Genome parent4 = new Genome();
        for (int i = 0; i <= 16; i++) {
            parent4.addNode(new Node(Node.TYPE.INPUT, i));
        }
        for (int i = 17; i <= 25; i++) {
            parent4.addNode(new Node(Node.TYPE.OUTPUT, i));
        }
        for (int i = 26; i <= 150; i++) {
            parent4.addNode(new Node(Node.TYPE.HIDDEN, i));
        }
        for (int i = 0; i <= 450; i++) {
            Node inNode = new ArrayList<>(parent4.nodes().values()).get(new Random().nextInt(parent4.nodes().size()));
            Node outNode = getOutNode(parent4, inNode);
            parent4.addConnection(new Connection(inNode, outNode, new Random().nextFloat(), true, i));
        }

        GenomePrinter.printGenome(parent1, "C:/Users/noahp/Desktop/testAI/parent1.png");
        GenomePrinter.printGenome(parent2, "C:/Users/noahp/Desktop/testAI/parent2.png");
        GenomePrinter.printGenome(parent3, "C:/Users/noahp/Desktop/testAI/parent3.png");
        GenomePrinter.printGenome(parent4, "C:/Users/noahp/Desktop/testAI/parent4.png");

        Genome child = parent1.crossOver(parent2, parent1);
        GenomePrinter.printGenome(child, "C:/Users/noahp/Desktop/testAI/child.png");

        Genome childChild = parent1.crossOver(parent3, child);
        GenomePrinter.printGenome(childChild, "C:/Users/noahp/Desktop/testAI/childChild.png");
    }

    private static Node getOutNode(Genome parent, Node inNode) {
        Node outNode = new ArrayList<>(parent.nodes().values()).get(new Random().nextInt(parent.nodes().size()));
        if (outNode.innovation() == inNode.innovation()) {
            outNode = getOutNode(parent, inNode);
        }
        return outNode;
    }
}
