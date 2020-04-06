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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private EntityRef entityRef;
    private InventoryComponent inventoryComp;
    private EntityManager entityManager;
    private BlockManager blockManager;
    private BlockItemFactory blockItemFactory;
    private PrefabManager prefabManager;

    @Before
    public void setup() {
        inventoryAuthoritySystem = new InventoryAuthoritySystem();
        startingInventorySystem = new StartingInventorySystem();
        entityRef = mock(EntityRef.class);
        inventoryComp = new InventoryComponent(5);
        when(entityRef.getComponent(InventoryComponent.class)).thenReturn(inventoryComp);

        entityManager = mock(EntityManager.class);
        inventoryAuthoritySystem.setEntityManager(entityManager);
        startingInventorySystem.entityManager = entityManager;
        blockManager = mock(BlockManager.class);
        startingInventorySystem.blockManager = blockManager;
        startingInventorySystem.inventoryManager = inventoryAuthoritySystem;
        blockItemFactory = mock(BlockItemFactory.class);
        startingInventorySystem.blockFactory = blockItemFactory;
        prefabManager = mock(PrefabManager.class);
        startingInventorySystem.prefabManager = prefabManager;
    }

    @Test
    public void giveSingleBlock() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = mock(EntityRef.class);
        setupItemRef(item, itemComp, 1, 10, "blockFamilyA", 1L);

        // Setup blockManager to return the correct families
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily("test:blockFamilyA")).thenReturn(blockFamily);

        // Setup the factory to return the right instances
        when(blockItemFactory.newInstance(blockFamily, 1)).thenReturn(item);

        // Create the starting inventory
        StartingInventoryComponent component = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = "test:blockFamilyA";
        inventoryItem.quantity = 1;
        component.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, component);

        // Assert that the block was added, only once
        assertEquals(item, inventoryComp.itemSlots.get(0));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(1));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(entityRef, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(blockManager, atLeast(1)).getBlockFamily("test:blockFamilyA");
        Mockito.verify(blockItemFactory, atLeast(1)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef).send(any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item);
    }

    @Test
    public void giveBlockStack() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = mock(EntityRef.class);
        setupItemRef(item, itemComp, 99, 99, "blockFamilyA", 1L);

        // Setup blockManager to return the correct families
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily("test:blockFamilyA")).thenReturn(blockFamily);

        // Setup the factory to return the right instances
        when(blockItemFactory.newInstance(blockFamily, 99)).thenReturn(item);

        // Create the starting inventory
        StartingInventoryComponent component = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = "test:blockFamilyA";
        inventoryItem.quantity = 110;
        component.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, component);

        // Assert that the block was added, only once
        assertEquals(item, inventoryComp.itemSlots.get(0));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(1));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(entityRef, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(blockManager, atLeast(1)).getBlockFamily("test:blockFamilyA");
        Mockito.verify(blockItemFactory, atLeast(1)).newInstance(blockFamily, 99);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef).send(any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item);
    }

    @Test
    public void giveSingleItem() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = mock(EntityRef.class);
        setupItemRef(item, itemComp, 1, 10, "itemA", 1L);

        // Setup blockManager to return null block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Setup to return item prefab
        Prefab prefab = mock(Prefab.class);
        when(prefab.getComponent(ItemComponent.class)).thenReturn(itemComp);
        String uri = "test:itemA";
        when(prefabManager.getPrefab(uri)).thenReturn(prefab);
        when(entityManager.create(uri)).thenReturn(item);

        // Create the starting inventory
        StartingInventoryComponent component = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 1;
        component.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, component);

        // Assert that the item was added, only once
        assertEquals(item, inventoryComp.itemSlots.get(0));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(1));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(entityRef, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(blockManager, atLeast(1)).getBlockFamily("test:itemA");
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityManager).create(uri);

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item);
    }

    @Test
    public void giveNonStackableItem() {
        // Create separate entities for equality checking
        ItemComponent itemComp1 = new ItemComponent();
        EntityRef item1 = mock(EntityRef.class);
        setupItemRef(item1, itemComp1, 1, 1, "", 1L);
        logger.debug("item1 {}", item1);

        ItemComponent itemComp2 = new ItemComponent();
        EntityRef item2 = mock(EntityRef.class);
        setupItemRef(item2, itemComp2, 1, 1, "", 2L);
        logger.debug("item2 {}", item2);

        // Setup blockManager to return null block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Setup to return item prefab
        Prefab prefab = mock(Prefab.class);
        when(prefab.getComponent(ItemComponent.class)).thenReturn(itemComp1);
        String uri = "test:itemA";
        when(prefabManager.getPrefab(uri)).thenReturn(prefab);
        when(entityManager.create(uri)).thenReturn(item1).thenReturn(item2);

        // Create the starting inventory
        StartingInventoryComponent startingInventoryComponent = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 3;
        startingInventoryComponent.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, startingInventoryComponent);

        assertNotEquals(item1, item2);
        assertEquals(item1, inventoryComp.itemSlots.get(0));
        assertEquals(item2, inventoryComp.itemSlots.get(1));
        assertEquals(item2, inventoryComp.itemSlots.get(2));
        assertNotEquals(EntityRef.NULL, inventoryComp.itemSlots.get(2));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(3));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, times(1)).saveComponent(itemComp1);
        Mockito.verify(item1, atLeast(0)).hashCode();
        Mockito.verify(item2, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2, atLeast(0)).exists();
        Mockito.verify(item2, times(2)).saveComponent(itemComp2);
        Mockito.verify(item2, atLeast(0)).hashCode();
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef, times(3)).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(blockManager).getBlockFamily("test:itemA");
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef, times(3)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef, times(3)).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityManager, times(3)).create(uri);

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item1, item2);
    }

    @Test
    public void giveStackableItemPlusOverflow() {
        // Create separate entities for equality checking
        ItemComponent itemComp1 = new ItemComponent();
        EntityRef item1 = mock(EntityRef.class);
        setupItemRef(item1, itemComp1, 1, 3, "itemA", 1L);
        logger.debug("item1 {}", item1);

        // Setup blockManager to return null block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Setup to return item prefab
        Prefab prefab = mock(Prefab.class);
        when(prefab.getComponent(ItemComponent.class)).thenReturn(itemComp1);
        String uri = "test:itemA";
        when(prefabManager.getPrefab(uri)).thenReturn(prefab);
        when(entityManager.create(uri)).thenReturn(item1);

        // Create the starting inventory
        StartingInventoryComponent startingInventoryComponent = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 4;
        startingInventoryComponent.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, startingInventoryComponent);

        assertEquals(item1, inventoryComp.itemSlots.get(0));
        assertEquals(item1, inventoryComp.itemSlots.get(1));
        assertNotEquals(EntityRef.NULL, inventoryComp.itemSlots.get(1));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(2));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, times(5)).saveComponent(itemComp1);
        Mockito.verify(item1, atLeast(0)).hashCode();
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef, times(2)).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(blockManager).getBlockFamily(uri);
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef, times(2)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef, times(2)).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityRef, times(3)).send(any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(entityManager, times(4)).create(uri);

//        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item1);
    }

    @Test
    public void giveTooManyNonStackableItem() {
        ItemComponent itemComp1 = new ItemComponent();
        EntityRef item1 = mock(EntityRef.class);
        setupItemRef(item1, itemComp1, 1, 1, "", 1L);
        logger.debug("item1 {}", item1);

        // Setup blockManager to return bull block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Setup to return item prefab
        Prefab prefab = mock(Prefab.class);
        when(prefab.getComponent(ItemComponent.class)).thenReturn(itemComp1);
        String uri = "test:itemA";
        when(prefabManager.getPrefab(uri)).thenReturn(prefab);
        when(entityManager.create(uri)).thenReturn(item1);

        // Create the starting inventory
        // Add 4 items, then try to add 2 more which should fail
        StartingInventoryComponent startingInventoryComponent = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 4;
        startingInventoryComponent.items.add(inventoryItem);
        inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 2;
        startingInventoryComponent.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, startingInventoryComponent);

        // Assert that the 4 items from the first InventoryItem were added, and the 2 from the second weren't
        assertEquals(item1, inventoryComp.itemSlots.get(0));
        assertEquals(item1, inventoryComp.itemSlots.get(1));
        assertEquals(item1, inventoryComp.itemSlots.get(2));
        assertEquals(item1, inventoryComp.itemSlots.get(3));
        assertNotEquals(EntityRef.NULL, inventoryComp.itemSlots.get(3));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(4));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, times(4)).saveComponent(itemComp1);
        Mockito.verify(item1, atLeast(0)).hashCode();
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef, times(4)).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(blockManager, times(2)).getBlockFamily("test:itemA");
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef, times(4)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef, times(4)).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityManager, times(4)).create(uri);

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item1);
    }

    @Test
    public void giveNonStackableItemThenStackable() {
        // Create separate entities for equality checking
        ItemComponent itemComp1 = new ItemComponent();
        EntityRef item1 = mock(EntityRef.class);
        setupItemRef(item1, itemComp1, 1, 1, "", 1L);
        logger.debug("item1 {}", item1);
        Prefab prefab1 = mock(Prefab.class);
        when(prefab1.getComponent(ItemComponent.class)).thenReturn(itemComp1);
        String uri1 = "test:itemA";
        when(prefabManager.getPrefab(uri1)).thenReturn(prefab1);
        when(entityManager.create(uri1)).thenReturn(item1);

        ItemComponent itemComp2 = new ItemComponent();
        EntityRef item2 = mock(EntityRef.class);
        setupItemRef(item2, itemComp2, 2, 5, "itemB", 2L);
        logger.debug("item2 {}", item2);
        Prefab prefab2 = mock(Prefab.class);
        when(prefab2.getComponent(ItemComponent.class)).thenReturn(itemComp2);
        String uri2 = "test:itemB";
        when(prefabManager.getPrefab(uri2)).thenReturn(prefab2);
        when(entityManager.create(uri2)).thenReturn(item2);

        // Setup blockManager to return bull block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Create the starting inventory
        // Add 3 items, then try to add 2 more which will succeed
        StartingInventoryComponent startingInventoryComponent = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri1;
        inventoryItem.quantity = 3;
        startingInventoryComponent.items.add(inventoryItem);
        inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri2;
        inventoryItem.quantity = 2;
        startingInventoryComponent.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, startingInventoryComponent);

        // Assert that the 3 items from the first InventoryItem were added,
        // and the 2 from the second were added in 1 stack
        assertEquals(item1, inventoryComp.itemSlots.get(0));
        assertEquals(item1, inventoryComp.itemSlots.get(1));
        assertEquals(item1, inventoryComp.itemSlots.get(2));
        assertNotEquals(EntityRef.NULL, inventoryComp.itemSlots.get(2));
        assertEquals(item2, inventoryComp.itemSlots.get(3));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(4));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, times(3)).saveComponent(itemComp1);
        Mockito.verify(item1, atLeast(0)).hashCode();
        Mockito.verify(item2, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2, atLeast(0)).exists();
        Mockito.verify(item2, times(2)).saveComponent(itemComp2);
        Mockito.verify(item2, atLeast(0)).hashCode();
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef, times(4)).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(blockManager).getBlockFamily(uri1);
        Mockito.verify(blockManager).getBlockFamily(uri2);
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef, times(4)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef, times(4)).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityRef, times(1)).send(any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(entityManager, times(3)).create(uri1);
        Mockito.verify(entityManager, times(2)).create(uri2);

//        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item1, item2);

    }

    @Test
    public void giveNonStackableItemThenStackableNoPrefab() {
        // Create separate entities for equality checking
        ItemComponent itemComp1 = new ItemComponent();
        EntityRef item1 = mock(EntityRef.class);
        setupItemRef(item1, itemComp1, 1, 1, "", 1L);
        logger.debug("item1 {}", item1);

        ItemComponent itemComp2 = new ItemComponent();
        EntityRef item2 = mock(EntityRef.class);
        setupItemRef(item2, itemComp2, 2, 5, "itemB", 2L);
        logger.debug("item2 {}", item2);

        // Setup blockManager to return bull block family
        BlockFamily blockFamily = new BlockFamilyA();
        when(blockManager.getBlockFamily(anyString())).thenReturn(null);

        // Setup to return item prefab
        Prefab prefab = mock(Prefab.class);
        when(prefab.getComponent(ItemComponent.class)).thenReturn(itemComp1);
        String uri = "test:itemA";
        when(prefabManager.getPrefab(uri)).thenReturn(prefab);
        when(entityManager.create(uri)).thenReturn(item1);
        when(entityManager.create("test:itemB")).thenReturn(item2);

        // Create the starting inventory
        // Add 3 items, then try to add 2 more which will succeed
        StartingInventoryComponent startingInventoryComponent = new StartingInventoryComponent();
        StartingInventoryComponent.InventoryItem inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = uri;
        inventoryItem.quantity = 3;
        startingInventoryComponent.items.add(inventoryItem);
        inventoryItem = new StartingInventoryComponent.InventoryItem();
        inventoryItem.uri = "test:itemB";
        inventoryItem.quantity = 2;
        startingInventoryComponent.items.add(inventoryItem);

        startingInventorySystem.onStartingInventory(null, entityRef, startingInventoryComponent);

        // Assert that the 3 items from the first InventoryItem were added,
        // and the 2 from the second weren't because it can't find a prefab
        assertEquals(item1, inventoryComp.itemSlots.get(0));
        assertEquals(item1, inventoryComp.itemSlots.get(1));
        assertEquals(item1, inventoryComp.itemSlots.get(2));
        assertNotEquals(EntityRef.NULL, inventoryComp.itemSlots.get(2));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(3));
        assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(4));
        assertFalse(entityRef.hasComponent(StartingInventoryComponent.class));

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, times(3)).saveComponent(itemComp1);
        Mockito.verify(item1, atLeast(0)).hashCode();
        Mockito.verify(item2, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2, atLeast(0)).exists();
        Mockito.verify(item2, times(0)).saveComponent(itemComp2);
        Mockito.verify(item2, atLeast(0)).hashCode();
        Mockito.verify(entityRef, atLeast(1)).getComponent(InventoryComponent.class);
        Mockito.verify(entityRef, times(3)).saveComponent(inventoryComp);
        Mockito.verify(entityRef).removeComponent(StartingInventoryComponent.class);
        Mockito.verify(entityRef).hasComponent(StartingInventoryComponent.class);
        Mockito.verify(blockManager).getBlockFamily("test:itemA");
        Mockito.verify(blockManager).getBlockFamily("test:itemB");
        Mockito.verify(blockItemFactory, times(0)).newInstance(blockFamily, 1);
        Mockito.verify(entityRef).getParentPrefab();
        Mockito.verify(entityRef, times(3)).send(any(BeforeItemPutInInventory.class));
        Mockito.verify(entityRef, times(3)).send(any(InventorySlotChangedEvent.class));
        Mockito.verify(entityManager, times(3)).create(uri);
        Mockito.verify(entityManager, times(0)).create("test:itemB");

        Mockito.verifyNoMoreInteractions(entityRef, entityManager, blockManager, blockItemFactory, item1, item2);
    }

    //=======================================================================================
    private void setupItemRef(EntityRef item,
                              ItemComponent itemComp,
                              int stackCount,
                              int stackSize,
                              String stackId,
                              long id) {
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = stackId;
        when(item.exists()).thenReturn(true);
        when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        when(item.iterateComponents()).thenReturn(new LinkedList<>());
        when(item.getId()).thenReturn(id);
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
}
