// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.systems;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StartingInventorySystemTest {

    @Test
    @Disabled
    public void giveSingleBlock() {
        Assertions.fail("Player inventory should contain exactly the single block.");
    }

    @Test
    @Disabled
    public void giveBlockStack() {
        Assertions.fail("Player inventory should contain exactly the block stack.");
    }

    @Test
    @Disabled
    public void giveSingleItem() {
        Assertions.fail("Player inventory should contain exactly the single item.");
    }

    @Test
    @Disabled
    public void giveNonStackableItem() {
        Assertions.fail("Player inventory should contain multiple instances of the non-stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingStackSize() {
        Assertions.fail("Player inventory should contain multiple slots filled with the stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingInventorySize() {
        Assertions.fail("Player inventory should be filled completely with non-stackable item.");
    }

    @Test
    @Disabled
    public void giveMultipleItems() {
        Assertions.fail("Player inventory should contain all given items in correct amount.");
    }

    @Test
    @Disabled
    public void giveStackableItemSplitStacks() {
        Assertions.fail("Player inventory should contain a single item stack with combined amount.");
    }
}
