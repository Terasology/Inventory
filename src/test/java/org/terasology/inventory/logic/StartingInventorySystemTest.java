// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class StartingInventorySystemTest {

    @Test
    @Ignore
    public void giveSingleBlock() {
        Assert.assertTrue("Player inventory should contain exactly the single block.", false);
    }

    @Test
    @Ignore
    public void giveBlockStack() {
        Assert.assertTrue("Player inventory should contain exactly the block stack.", false);
    }

    @Test
    @Ignore
    public void giveSingleItem() {
        Assert.assertTrue("Player inventory should contain exactly the single item.", false);
    }

    @Test
    @Ignore
    public void giveNonStackableItem() {
        Assert.assertTrue("Player inventory should contain multiple instances of the non-stackable item.", false);
    }

    @Test
    @Ignore
    public void giveStackableItemExceedingStackSize() {
        Assert.assertTrue("Player inventory should contain multiple slots filled with the stackable item.", false);
    }

    @Test
    @Ignore
    public void giveStackableItemExceedingInventorySize() {
        Assert.assertTrue("Player inventory should be filled completely with non-stackable item.", false);
    }

    @Test
    @Ignore
    public void giveMultipleItems() {
        Assert.assertTrue("Player inventory should contain all given items in correct amount.", false);
    }

    @Test
    @Ignore
    public void giveStackableItemSplitStacks() {
        Assert.assertTrue("Player inventory should contain a single item stack with combined amount.", false);
    }
}
