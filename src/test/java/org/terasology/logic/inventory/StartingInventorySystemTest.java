/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.inventory;

import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class StartingInventorySystemTest {

    @Test
    @Disabled
    public void giveSingleBlock() {
        Assert.fail("Player inventory should contain exactly the single block.");
    }

    @Test
    @Disabled
    public void giveBlockStack() {
        Assert.fail("Player inventory should contain exactly the block stack.");
    }

    @Test
    @Disabled
    public void giveSingleItem() {
        Assert.fail("Player inventory should contain exactly the single item.");
    }

    @Test
    @Disabled
    public void giveNonStackableItem() {
        Assert.fail("Player inventory should contain multiple instances of the non-stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingStackSize() {
        Assert.fail("Player inventory should contain multiple slots filled with the stackable item.");
    }

    @Test
    @Disabled
    public void giveStackableItemExceedingInventorySize() {
        Assert.fail("Player inventory should be filled completely with non-stackable item.");
    }

    @Test
    @Disabled
    public void giveMultipleItems() {
        Assert.fail("Player inventory should contain all given items in correct amount.");
    }

    @Test
    @Disabled
    public void giveStackableItemSplitStacks() {
        Assert.fail("Player inventory should contain a single item stack with combined amount.");
    }
}
