package com.swirb.speedrunai.gui;

import javax.swing.*;
import java.awt.*;

public class Screen extends JFrame {

    public static void main(String[] args) {
        new Screen("lol?");
    }

    public Screen(String title) {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(700, 400);
        this.setBackground(Color.LIGHT_GRAY);
        this.setVisible(true);
        this.getGraphics().setColor(Color.BLACK);
        this.getGraphics().drawLine(0, 0, 700, 500);
        this.repaint();
    }
}
