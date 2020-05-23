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
