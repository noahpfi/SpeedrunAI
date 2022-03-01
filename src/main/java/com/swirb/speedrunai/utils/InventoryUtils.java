package com.swirb.speedrunai.utils;

import com.mojang.datafixers.util.Pair;
import com.swirb.speedrunai.client.Client;
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

    // works
    public List<Item> containsAll(Predicate<Item> predicate) {
        List<Item> has = new ArrayList<>();
        for (ItemStack itemStack : this.client.getInventory().getContents()) {
            if (predicate.test(itemStack.getItem())) {
                has.add(itemStack.getItem());
            }
        }
        return has;
    }

    // should work
    private boolean contains(List<ItemStack> list, ItemStack itemStack) {
        for (ItemStack item : list) {
            if (item.getItem() == itemStack.getItem()) {
                return true;
            }
        }
        return false;
    }

    // works
    public List<ItemStack> getAll(Predicate<Item> predicate) {
        List<ItemStack> has = new ArrayList<>();
        for (ItemStack itemStack : this.client.getInventory().getContents()) {
            if (predicate.test(itemStack.getItem())) {
                has.add(itemStack);
            }
        }
        return has;
    }

    // works
    public List<ItemStack> getAll(List<ItemStack> list, Predicate<Item> predicate) {
        List<ItemStack> has = new ArrayList<>();
        for (ItemStack itemStack : list) {
            if (predicate.test(itemStack.getItem())) {
                has.add(itemStack);
            }
        }
        return has;
    }

    // works
    public ItemStack get(List<ItemStack> list, Predicate<Item> predicate) {
        for (ItemStack itemStack : list) {
            if (predicate.test(itemStack.getItem())) {
                return itemStack;
            }
        }
        return null;
    }

    // works
    public ItemStack get(Item item) {
        for (ItemStack itemStack : this.client.getInventory().getContents()) {
            if (itemStack.getItem() == item) {
                return itemStack;
            }
        }
        return null;
    }

    // works
    public ItemStack get(Predicate<Item> predicate) {
        for (ItemStack itemStack : this.client.getInventory().getContents()) {
            if (predicate.test(itemStack.getItem())) {
                return itemStack;
            }
        }
        return null;
    }

    // works
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

    // should work
    public void swap(ItemStack itemStack, Section sectionTo, int indexTo) {
        if (itemStack == null || sectionTo == null || !this.contains(this.client.getInventory().getContents(), itemStack) || this.of(itemStack) == null) {
            return;
        }
        Pair<Section, Integer> indexes = this.of(itemStack);
        this.swap(indexes.getFirst(), indexes.getSecond(), sectionTo, indexTo);
    }

    // works
    public void swap(Section sectionFrom, int indexFrom, Section sectionTo, int indexTo) {
        if (sectionFrom == null || sectionTo == null) {
            return;
        }
        ItemStack itemStack = get(sectionFrom, indexFrom);
        set(sectionFrom, indexFrom, get(sectionTo, indexTo));
        set(sectionTo, indexTo, itemStack);
    }

    // works
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

    // works
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

    public boolean hasFood() {
        for (ItemStack itemStack : this.client.getInventory().getContents()) {
            if (itemStack.getItem().isEdible()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack bestFood() {
        ItemStack itemStack = null;
        int nutrition = 0;
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem().isEdible()) {
                int i = Objects.requireNonNull(item.getItem().getFoodProperties()).getNutrition();
                if (i > nutrition) {
                    nutrition = i;
                    itemStack = item;
                }
            }
        }
        return itemStack;
    }

    public ItemStack worstFood() {
        ItemStack itemStack = null;
        int nutrition = 20;
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem().isEdible()) {
                int i = Objects.requireNonNull(item.getItem().getFoodProperties()).getNutrition();
                if (i < nutrition) {
                    nutrition = i;
                    itemStack = item;
                }
            }
        }
        return itemStack;
    }

    public ItemStack avgFood() {
        ItemStack itemStack = null;
        int nutrition = 0;
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem().isEdible()) {
                int i = Objects.requireNonNull(item.getItem().getFoodProperties()).getNutrition();
                if (i > nutrition && i < 6) {
                    nutrition = i;
                    itemStack = item;
                }
            }
        }
        return itemStack;
    }

    public ItemStack food(int nutrition) {
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem().isEdible()) {
                if (Objects.requireNonNull(item.getItem().getFoodProperties()).getNutrition() == nutrition) {
                    return item;
                }
            }
        }
        return null;
    }

    public ItemStack optimalEating() {
        if (this.client.getHealth() < 6 && this.bestFood() != null) {
            return this.bestFood();
        }
        if (this.client.getHealth() < 12 && this.client.getFoodData().foodLevel < 18 && this.avgFood() != null) {
            return this.avgFood();
        }
        int i = 20 - this.client.getFoodData().foodLevel;
        ItemStack food = this.food(i);
        if (food != null) {
            return food;
        }
        if (this.worstFood() != null) {
            return this.worstFood();
        }
        if (this.avgFood() != null) {
            return this.avgFood();
        }
        if (this.bestFood() != null) {
            return this.bestFood();
        }
        return null;
    }

    public ItemStack weapon() {
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem() instanceof SwordItem || item.getItem() instanceof AxeItem) {
                return item;
            }
        }
        return null;
    }

    public ItemStack shootingWeapon() {
        for (ItemStack item : this.client.getInventory().getContents()) {
            if (item.getItem() instanceof BowItem || item.getItem() instanceof CrossbowItem || item.getItem() instanceof TridentItem) {
                return item;
            }
        }
        return null;
    }

    public ItemStack bestToolFor(BlockState blockState) {
        ItemStack itemStack = null;
        float speed = 0;
        for (ItemStack item : this.client.getInventory().getContents()) {
            float spd = item.getItem().getDestroySpeed(item, blockState);
            if (spd > speed) {
                itemStack = item;
                speed = spd;
            }
        }
        return itemStack;
    }

    public ItemStack craft(ItemStack itemStack) {
        CraftingMenu menu = (CraftingMenu) this.client.containerMenu;
        //menu.handlePlacement(this.client.input.SHIFT, recipe, this.client);
        this.client.server.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

        // click recipe -> place recipe in grid: handlePlaceRecipe
        // craft: handleContainerClick

        //this.client.connection.handlePlaceRecipe(new ServerboundPlaceRecipePacket(this.client.containerMenu, recipe, this.client.input.SHIFT));
        return null;
    }

    public Recipe<?> recipeFor(ItemStack itemStack) {
        return null;
    }

    public enum Section {
        OFFHAND,
        ARMOR,
        ITEMS
    }
}
