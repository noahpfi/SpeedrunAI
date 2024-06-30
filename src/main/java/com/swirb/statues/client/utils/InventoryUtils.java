package com.swirb.statues.client.utils;

import com.mojang.datafixers.util.Pair;
import com.swirb.statues.client.Client;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class InventoryUtils {

    private final Client client;

    public InventoryUtils(Client client) {
        this.client = client;
    }

    private boolean contains(List<ItemStack> list, ItemStack itemStack) {
        for (ItemStack item : list) {
            if (item.getItem() == itemStack.getItem()) {
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> getAll(List<ItemStack> list, Predicate<Item> predicate) {
        List<ItemStack> has = new ArrayList<>();
        for (ItemStack itemStack : list) {
            if (predicate.test(itemStack.getItem())) {
                has.add(itemStack);
            }
        }
        return has;
    }

    public Pair<Section, Integer> of(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        if (this.client.getInventory().offhand.get(0).getItem() == itemStack.getItem()) {
            return new Pair<>(Section.OFFHAND, 0);
        }
        if (this.contains(this.client.getInventory().armor, itemStack)) {
            for (int i = 0; i < this.client.getInventory().armor.size(); i++) {
                if (this.client.getInventory().armor.get(i).getItem() == itemStack.getItem()) {
                    return new Pair<>(Section.ARMOR, i);
                }
            }
        }
        if (this.contains(this.client.getInventory().items, itemStack)) {
            for (int i = 0; i < this.client.getInventory().items.size(); i++) {
                if (this.client.getInventory().items.get(i).getItem() == itemStack.getItem()) {
                    return new Pair<>(Section.ITEMS, i);
                }
            }
        }
        return null;
    }

    public void swap(ItemStack itemStack, Section sectionTo, int indexTo) {
        if (itemStack == null || sectionTo == null || !this.contains(this.client.getInventory().getContents(), itemStack) || this.of(itemStack) == null) {
            return;
        }
        Pair<Section, Integer> indexes = this.of(itemStack);
        this.swap(indexes.getFirst(), indexes.getSecond(), sectionTo, indexTo);
    }

    public void swap(Section sectionFrom, int indexFrom, Section sectionTo, int indexTo) {
        if (sectionFrom == null || sectionTo == null) {
            return;
        }
        ItemStack itemStack = get(sectionFrom, indexFrom);
        set(sectionFrom, indexFrom, get(sectionTo, indexTo));
        set(sectionTo, indexTo, itemStack);
    }

    public ItemStack get(Section section, int index) {
        if (section == Section.OFFHAND && index == 0) {
            return client.getInventory().offhand.get(index);
        }
        if (section == Section.ARMOR && index >= 0 && index < 4) {
            return client.getInventory().armor.get(index);
        }
        if (section == Section.ITEMS && index >= 0 && index < 36) {
            return client.getInventory().items.get(index);
        }
        return null;
    }

    public void set(Section section, int index, ItemStack itemStack) {
        if (section == Section.OFFHAND && index == 0) {
            client.getInventory().offhand.set(index, itemStack);
            return;
        }
        if (section == Section.ARMOR && index >= 0 && index < 4) {
            client.getInventory().armor.set(index, itemStack);
            return;
        }
        if (section == Section.ITEMS && index >= 0 && index < 36) {
            client.getInventory().items.set(index, itemStack);
        }
    }

    public enum Section {
        OFFHAND,
        ARMOR,
        ITEMS
    }
}
