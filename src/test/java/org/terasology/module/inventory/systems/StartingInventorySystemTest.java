// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.systems;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StartingInventorySystemTest {

    @Test
    @Disabled
    public void giveSingleBlock() {
        assertTrue(false, "Player inventory should contain exactly the single block.");
    }

    @Test
    @Disabled
    public void giveBlockStack() {
        assertTrue(false, "Player inventory should contain exactly the block stack.");
    }

    @Test
    @Disabled
    public void giveSingleItem() {
        assertTrue(false, "Player inventory should contain exactly the single item.");
    }

    @Test
    @Disabled
    public void giveNonStackableItem() {
        assertTrue(false, "Player inventory should contain multiple instances of the non-stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingStackSize() {
        assertTrue(false, "Player inventory should contain multiple slots filled with the stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingInventorySize() {
        assertTrue(false, "Player inventory should be filled completely with non-stackable item.");
    }

    @Test
    @Disabled
    public void giveMultipleItems() {
        assertTrue(false, "Player inventory should contain all given items in correct amount.");
    }

    @Test
    @Disabled
    public void giveStackableItemSplitStacks() {
        assertTrue(false, "Player inventory should contain a single item stack with combined amount.");
    }
}
