/*
 * Copyright 2014 MovingBlocks
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 */
public class StartingInventorySystemTest {

    private static final Logger logger = LoggerFactory.getLogger(StartingInventorySystemTest.class);

    private InventoryAuthoritySystem inventoryAuthoritySystem;
    private StartingInventorySystem startingInventorySystem;
    private EntityRef instigator;
    private EntityRef inventory;
    private InventoryComponent inventoryComp;
    private EntityManager entityManager;
    private BlockManager blockManager;
    private InventoryManager inventoryManager;
    private BlockItemFactory blockItemFactory;

    @Before
    public void setup() {
        inventoryAuthoritySystem = new InventoryAuthoritySystem();
        startingInventorySystem = new StartingInventorySystem();
        instigator = mock(EntityRef.class);
        inventory = mock(EntityRef.class);
        inventoryComp = new InventoryComponent(5);
        when(inventory.getComponent(InventoryComponent.class)).thenReturn(inventoryComp);

        entityManager = mock(EntityManager.class);
        inventoryAuthoritySystem.setEntityManager(entityManager);
        startingInventorySystem.entityManager = entityManager;
        blockManager = mock(BlockManager.class);
        startingInventorySystem.blockManager = blockManager;
        startingInventorySystem.inventoryManager = inventoryAuthoritySystem;
        inventoryManager = inventoryAuthoritySystem;
        blockItemFactory = mock(BlockItemFactory.class);
        startingInventorySystem.blockFactory = blockItemFactory;
    }

    // test give block

    // test give item

    // test does try add too much

    // test provided not added again

    @Test
    public void giveBlock() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);
        logger.debug("Item {}", item);

        // Setup blockManager to return the correct families
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily("test:blockFamilyA")).thenReturn(blockFamily);

        // Setup the factory to return the right instances
        when(blockItemFactory.newInstance(blockFamily, 1)).thenReturn(item);

        logger.debug("Calling");

        // Create the starting inventory
        StartingInventoryComponent component = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = "test:blockFamilyA";
        inventoryItem.quantity = 1;
        component.items.add(inventoryItem);
        when(inventory.getComponent(StartingInventoryComponent.class)).thenReturn(component);

        startingInventorySystem.onStartingInventory(null, inventory);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory).saveComponent(component);
        Mockito.verify(item).hashCode();
        Mockito.verify(inventory, atLeast(1)).getComponent(StartingInventoryComponent.class);
        Mockito.verify(inventory, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(blockManager, atLeast(1)).getBlockFamily("test:blockFamilyA");
        Mockito.verify(blockItemFactory, atLeast(1)).newInstance(blockFamily, 1);
        Mockito.verify(inventory, atLeast(1)).getParentPrefab();
        Mockito.verify(inventory, times(1)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(inventory, times(1)).send(any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(inventory, entityManager, blockManager, blockItemFactory, item);

        assertEquals(item, inventoryComp.itemSlots.get(0));
    }

    private void setupItemRef(EntityRef item, ItemComponent itemComp, int stackCount, int stackSize) {
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = "stackId";
        when(item.exists()).thenReturn(true);
        when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        when(item.iterateComponents()).thenReturn(new LinkedList<>());
    }

    private EntityRef createItem(String stackId, int stackCount, int stackSize) {
        ItemComponent itemComp = new ItemComponent();
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = stackId;
        EntityRef item = mock(EntityRef.class);
        when(item.exists()).thenReturn(true);
        when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        when(item.iterateComponents()).thenReturn(new LinkedList<>());
        return item;
    }

    //=======================================================================================
    public static class BlockFamilyA implements BlockFamily {

        @Override
        public BlockUri getURI() {
            return new BlockUri("test:blockFamilyA");
        }

        @Override
        public String getDisplayName() {
            return "Block Family A";
        }

        @Override
        public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
            return null;
        }

        @Override
        public Block getArchetypeBlock() {
            return new Block();
        }

        @Override
        public Block getBlockFor(BlockUri blockUri) {
            return new Block();
        }

        @Override
        public Iterable<Block> getBlocks() {
            return null;
        }

        @Override
        public Iterable<String> getCategories() {
            return null;
        }

        @Override
        public boolean hasCategory(String category) {
            return false;
        }
    }

    public static class BlockFamilyB implements BlockFamily {

        @Override
        public BlockUri getURI() {
            return new BlockUri("test:blockFamilyB");
        }

        @Override
        public String getDisplayName() {
            return "Block Family B";
        }

        @Override
        public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
            return null;
        }

        @Override
        public Block getArchetypeBlock() {
            return new Block();
        }

        @Override
        public Block getBlockFor(BlockUri blockUri) {
            return new Block();
        }

        @Override
        public Iterable<Block> getBlocks() {
            return null;
        }

        @Override
        public Iterable<String> getCategories() {
            return null;
        }

        @Override
        public boolean hasCategory(String category) {
            return false;
        }
    }

}
