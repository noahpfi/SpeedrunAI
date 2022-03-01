package com.swirb.speedrunai.neat.test;

import com.swirb.speedrunai.neat.Connection;
import com.swirb.speedrunai.neat.Genome;
import com.swirb.speedrunai.neat.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class GenomePrinter {
	
	public static void printGenome(Genome genome, String path) {
		Random r = new Random();
		HashMap<Integer, Point> nodeGenePositions = new HashMap<>();
		int nodeSize = 25;
		int connectionSizeBulb = 5;
		int imageSize = 512;
		BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, imageSize, imageSize);
		for (Node node : genome.nodes().values()) {
			if (node.type() == Node.TYPE.INPUT) {
				//int x = (node.innovation() / (countNodesByType(genome, Node.TYPE.INPUT) + 1)) * imageSize;
				int x = imageSize / (highestInnovation(genome, Node.TYPE.INPUT) + 1) * node.innovation();
				int y = imageSize - nodeSize / 2;
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fillOval((x - nodeSize / 2), (y - nodeSize / 2), nodeSize, nodeSize);
				nodeGenePositions.put(node.innovation(), new Point(x, y));
			} else if (node.type() == Node.TYPE.HIDDEN) {
				int x = r.nextInt(imageSize - nodeSize * 2) + nodeSize;
				int y = r.nextInt(imageSize - nodeSize * 3) + (int) (nodeSize * 1.5F);
				//int x = imageSize / (highestInnovation(genome, Node.TYPE.HIDDEN) + 1) * node.innovation();
				//int y = imageSize / (highestInnovation(genome, Node.TYPE.HIDDEN) + 1) * node.innovation();
				graphics.setColor(Color.GRAY);
				graphics.fillOval((x - nodeSize / 2), (y - nodeSize / 2), nodeSize, nodeSize);
				nodeGenePositions.put(node.innovation(), new Point(x, y));
			} else if (node.type() == Node.TYPE.OUTPUT) {
				int x = r.nextInt(imageSize - nodeSize * 2) + nodeSize;
				//int x = imageSize / (highestInnovation(genome, Node.TYPE.OUTPUT) + 1) * node.innovation();
				int y = nodeSize / 2;
				graphics.setColor(Color.YELLOW);
				graphics.fillOval((x - nodeSize / 2), (y - nodeSize / 2), nodeSize, nodeSize);
				nodeGenePositions.put(node.innovation(), new Point(x, y));
			}
			else {
				System.out.println("no type");
			}
		}
		graphics.setColor(Color.BLACK);
		for (Connection connection : genome.connections().values()) {
			Point inNode = nodeGenePositions.get(connection.inNode().innovation());
			Point outNode = nodeGenePositions.get(connection.outNode().innovation());
			Point lineVector = new Point((int) ((outNode.x - inNode.x) * 0.95f), (int) ((outNode.y - inNode.y) * 0.95f));
			graphics.setColor(connection.expressed() ? Color.BLACK : Color.RED);
			graphics.drawLine(inNode.x, inNode.y, inNode.x + lineVector.x, inNode.y + lineVector.y);
			graphics.fillRect(inNode.x + lineVector.x - connectionSizeBulb / 2, inNode.y + lineVector.y - connectionSizeBulb / 2, connectionSizeBulb, connectionSizeBulb);
			graphics.drawString("" + connection.weight(), (int) (inNode.x + lineVector.x * 0.25F + 5.0F), (int) (inNode.y + lineVector.y * 0.25F));
		}
		graphics.setColor(Color.BLACK);
		for (Node node : genome.nodes().values()) {
			Point point = nodeGenePositions.get(node.innovation());
			graphics.drawString("" + node.innovation(), point.x, point.y);
		}
		try {
			ImageIO.write(image, "PNG", new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int countNodesByType(Genome genome, Node.TYPE type) {
		int c = 0;
		for (Node node : genome.nodes().values()) {
			if (node.type() == type) {
				c++;
			}
		}
		return c;
	}

	private static int highestInnovation(Genome genome, Node.TYPE type) {
		int c = 0;
		for (Node node : genome.nodes().values()) {
			if (node.type() == type) {
				if (c < node.innovation()) {
					c = node.innovation();
				}
			}
		}
		return c;
	}
}
