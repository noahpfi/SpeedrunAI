package com.swirb.speedrunai.client;

public class Input {
    public boolean W;
    public boolean S;
    public boolean A;
    public boolean D;
    public boolean SPACE;
    public boolean SPACE_LAST;
    public boolean SHIFT;
    public boolean SPRINT;

    public boolean RIGHT_CLICK;
    public boolean LEFT_CLICK;
    public boolean RIGHT_CLICK_LAST;

    public boolean INVENTORY;
    public boolean SWAP;
    public boolean DROP;
    public boolean CTRL;
    public boolean SLOT0;
    public boolean SLOT1;
    public boolean SLOT2;
    public boolean SLOT3;
    public boolean SLOT4;
    public boolean SLOT5;
    public boolean SLOT6;
    public boolean SLOT7;
    public boolean SLOT8;

    public void sync() {
        this.RIGHT_CLICK_LAST = this.RIGHT_CLICK;
        this.SPACE_LAST = this.SPACE;
    }
}
