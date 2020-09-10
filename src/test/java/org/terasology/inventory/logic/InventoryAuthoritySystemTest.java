// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.inventory.logic.action.GiveItemAction;
import org.terasology.inventory.logic.action.RemoveItemAction;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.inventory.logic.events.BeforeItemRemovedFromInventory;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;
import org.terasology.inventory.logic.events.InventorySlotStackSizeChangedEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

/**
 *
 */
public class InventoryAuthoritySystemTest {
    private InventoryAuthoritySystem inventoryAuthoritySystem;
    private EntityRef instigator;
    private EntityRef inventory;
    private InventoryComponent inventoryComp;
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        inventoryAuthoritySystem = new InventoryAuthoritySystem();
        instigator = Mockito.mock(EntityRef.class);
        inventory = Mockito.mock(EntityRef.class);
        inventoryComp = new InventoryComponent(5);
        Mockito.when(inventory.getComponent(InventoryComponent.class)).thenReturn(inventoryComp);

        entityManager = Mockito.mock(EntityManager.class);
        inventoryAuthoritySystem.setEntityManager(entityManager);
    }

    @Test
    public void removePartOfStack() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        EntityRef itemCopy = Mockito.mock(EntityRef.class);
        ItemComponent itemCompCopy = new ItemComponent();
        setupItemRef(itemCopy, itemCompCopy, 2, 10);

        Mockito.when(entityManager.copy(item)).thenReturn(itemCopy);

        RemoveItemAction action = new RemoveItemAction(instigator, item, false, 1);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertEquals(1, itemComp.stackCount);
        Assert.assertEquals(1, itemCompCopy.stackCount);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).iterateComponents();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(itemCopy, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(itemCopy, atLeast(0)).iterateComponents();
        Mockito.verify(itemCopy).saveComponent(itemCompCopy);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(entityManager).copy(item);

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, itemCopy);

        Assert.assertTrue(action.isConsumed());
        Assert.assertEquals(itemCopy, action.getRemovedItem());
    }

    @Test
    public void removeWholeStack() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, false, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertTrue(action.isConsumed());
        Assert.assertEquals(item, action.getRemovedItem());

        Assert.assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item, atLeast(0)).iterateComponents();
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void removePartOfStackWithDestroy() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 1);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertEquals(1, itemComp.stackCount);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        Assert.assertTrue(action.isConsumed());
        Assert.assertNull(action.getRemovedItem());
    }

    @Test
    public void removeWholeStackWithDestroy() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertTrue(action.isConsumed());
        Assert.assertNull(action.getRemovedItem());

        Assert.assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).destroy();
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, times(1)).send(any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void removeOverOneStack() {
        EntityRef item1 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp1 = new ItemComponent();
        setupItemRef(item1, itemComp1, 2, 10);

        inventoryComp.itemSlots.set(0, item1);

        EntityRef item2 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp2 = new ItemComponent();
        setupItemRef(item2, itemComp2, 2, 10);

        inventoryComp.itemSlots.set(1, item2);

        RemoveItemAction action = new RemoveItemAction(instigator, Arrays.asList(item1, item2), false, 3);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));
        Assert.assertEquals(3, itemComp1.stackCount);
        Assert.assertEquals(1, itemComp2.stackCount);
        Assert.assertTrue(action.isConsumed());
        Assert.assertEquals(item1, action.getRemovedItem());

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, atLeast(0)).iterateComponents();
        Mockito.verify(item1).saveComponent(itemComp1);
        Mockito.verify(item2, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2, atLeast(0)).iterateComponents();
        Mockito.verify(item2).saveComponent(itemComp2);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item1, item2);
    }

    @Test
    public void removeOverOneStackWithDestroy() {
        EntityRef item1 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp1 = new ItemComponent();
        setupItemRef(item1, itemComp1, 2, 10);

        inventoryComp.itemSlots.set(0, item1);

        EntityRef item2 = Mockito.mock(EntityRef.class);
        ItemComponent itemComp2 = new ItemComponent();
        setupItemRef(item2, itemComp2, 2, 10);

        inventoryComp.itemSlots.set(1, item2);

        RemoveItemAction action = new RemoveItemAction(instigator, Arrays.asList(item1, item2), true, 3);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertEquals(EntityRef.NULL, inventoryComp.itemSlots.get(0));
        Assert.assertEquals(1, itemComp2.stackCount);
        Assert.assertTrue(action.isConsumed());
        Assert.assertNull(action.getRemovedItem());

        Mockito.verify(item1, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item1, atLeast(0)).exists();
        Mockito.verify(item1, atLeast(0)).iterateComponents();
        Mockito.verify(item1).destroy();
        Mockito.verify(item2, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item2, atLeast(0)).iterateComponents();
        Mockito.verify(item2).saveComponent(itemComp2);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item1, item2);
    }

    @Test
    public void removeWholeStackWithVeto() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        inventoryComp.itemSlots.set(0, item);

        Mockito.when(inventory.send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class))).then(
                invocation -> {
                    BeforeItemRemovedFromInventory event =
                            (BeforeItemRemovedFromInventory) invocation.getArguments()[0];
                    event.consume();
                    return null;
                });

        RemoveItemAction action = new RemoveItemAction(instigator, item, true, 2);
        inventoryAuthoritySystem.removeItem(action, inventory);

        Assert.assertFalse(action.isConsumed());
        Assert.assertNull(action.getRemovedItem());

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);
    }

    @Test
    public void addItemToEmpty() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(BeforeItemPutInInventory.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        Assert.assertEquals(item, inventoryComp.itemSlots.get(0));
        Assert.assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToPartial() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        ItemComponent partialItemComp = new ItemComponent();
        EntityRef partialItem = Mockito.mock(EntityRef.class);
        setupItemRef(partialItem, partialItemComp, 2, 10);

        inventoryComp.itemSlots.set(0, partialItem);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item, atLeast(0)).iterateComponents();
        Mockito.verify(item).destroy();
        Mockito.verify(partialItem, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(partialItem, atLeast(0)).exists();
        Mockito.verify(partialItem, atLeast(0)).iterateComponents();
        Mockito.verify(partialItem).saveComponent(partialItemComp);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, partialItem);

        Assert.assertEquals(partialItem, inventoryComp.itemSlots.get(0));
        Assert.assertEquals(4, partialItemComp.stackCount);
        Assert.assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToPartialAndOverflow() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        ItemComponent partialItemComp = new ItemComponent();
        EntityRef partialItem = Mockito.mock(EntityRef.class);
        setupItemRef(partialItem, partialItemComp, 9, 10);

        inventoryComp.itemSlots.set(0, partialItem);

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(item, atLeast(0)).iterateComponents();
        Mockito.verify(item).saveComponent(itemComp);
        Mockito.verify(partialItem, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(partialItem, atLeast(0)).exists();
        Mockito.verify(partialItem, atLeast(0)).iterateComponents();
        Mockito.verify(partialItem).saveComponent(partialItemComp);
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory).saveComponent(inventoryComp);
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotStackSizeChangedEvent.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(InventorySlotChangedEvent.class));
        Mockito.verify(inventory, times(1)).send(ArgumentMatchers.any(BeforeItemPutInInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item, partialItem);

        Assert.assertEquals(partialItem, inventoryComp.itemSlots.get(0));
        Assert.assertEquals(item, inventoryComp.itemSlots.get(1));
        Assert.assertEquals(10, partialItemComp.stackCount);
        Assert.assertEquals(1, itemComp.stackCount);
        Assert.assertTrue(action.isConsumed());
    }

    @Test
    public void addItemToEmptyWithVeto() {
        ItemComponent itemComp = new ItemComponent();
        EntityRef item = Mockito.mock(EntityRef.class);
        setupItemRef(item, itemComp, 2, 10);

        Mockito.when(inventory.send(ArgumentMatchers.any(BeforeItemPutInInventory.class))).then(
                invocation -> {
                    BeforeItemPutInInventory event = (BeforeItemPutInInventory) invocation.getArguments()[0];
                    event.consume();
                    return null;
                });

        GiveItemAction action = new GiveItemAction(instigator, item);
        inventoryAuthoritySystem.giveItem(action, inventory);

        Mockito.verify(item, atLeast(0)).getComponent(ItemComponent.class);
        Mockito.verify(item, atLeast(0)).exists();
        Mockito.verify(inventory, atLeast(0)).getComponent(InventoryComponent.class);
        Mockito.verify(inventory, times(5)).send(ArgumentMatchers.any(BeforeItemPutInInventory.class));

        Mockito.verifyNoMoreInteractions(instigator, inventory, entityManager, item);

        Assert.assertFalse(action.isConsumed());

    }

    private void setupItemRef(EntityRef item, ItemComponent itemComp, int stackCount, int stackSize) {
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = "stackId";
        Mockito.when(item.exists()).thenReturn(true);
        Mockito.when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        Mockito.when(item.iterateComponents()).thenReturn(new LinkedList<>());
    }

    private EntityRef createItem(String stackId, int stackCount, int stackSize) {
        ItemComponent itemComp = new ItemComponent();
        itemComp.stackCount = (byte) stackCount;
        itemComp.maxStackSize = (byte) stackSize;
        itemComp.stackId = stackId;
        EntityRef item = Mockito.mock(EntityRef.class);
        Mockito.when(item.exists()).thenReturn(true);
        Mockito.when(item.getComponent(ItemComponent.class)).thenReturn(itemComp);
        Mockito.when(item.iterateComponents()).thenReturn(new LinkedList<>());
        return item;
    }

    @Test
    public void testMoveItemToSlotsWithSplittingToMultipleStacks() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 8, stackSize);
        EntityRef itemB1 = createItem("B", 8, stackSize);
        EntityRef itemA2 = createItem("A", 7, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);
        toInventoryComp.itemSlots.set(2, itemB1);
        toInventoryComp.itemSlots.set(3, itemA2);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA3 = createItem("A", 4, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA3);

        List<Integer> toSlots = Arrays.asList(0, 1, 2, 3, 4);

        // The method that gets tested:
        inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory, toSlots);

        Assert.assertEquals(10, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(9, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertFalse(fromInventoryComp.itemSlots.get(fromSlot).exists());
    }

    @Test
    public void testMoveItemToSlotsWithSplittingToMultipleStacksAndEmptySlot() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 8, stackSize);
        EntityRef itemB1 = createItem("B", 8, stackSize);
        EntityRef itemA2 = createItem("A", 7, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);
        toInventoryComp.itemSlots.set(2, itemB1);
        toInventoryComp.itemSlots.set(3, itemA2);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA3 = createItem("A", 8, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA3);

        List<Integer> toSlots = Arrays.asList(0, 1, 2, 3, 4);

        // The method that gets tested:
        inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory, toSlots);

        Assert.assertEquals(10, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(10, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(3, itemA3.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(itemA3, toInventoryComp.itemSlots.get(1));
        Assert.assertFalse(fromInventoryComp.itemSlots.get(fromSlot).exists());
    }

    @Test
    public void testMoveItemToSlotsWithToLessSpaceInTargetSlots() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 8, stackSize);
        EntityRef itemB1 = createItem("B", 8, stackSize);
        EntityRef itemA2 = createItem("A", 7, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);
        toInventoryComp.itemSlots.set(2, itemB1);
        toInventoryComp.itemSlots.set(3, itemA2);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA3 = createItem("A", 4, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA3);

        List<Integer> toSlots = Arrays.asList(0, 2);

        // The method that gets tested:
        boolean result = inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory,
                toSlots);
        Assert.assertTrue(result);

        Assert.assertEquals(10, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(7, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(2, itemA3.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(itemA3, fromInventoryComp.itemSlots.get(fromSlot));
    }

    @Test
    public void testMoveItemToSlotsWithTargetVetos() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 8, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA2 = createItem("A", 5, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA2);

        // Placement to slots 1 gets blocked by veto
        Mockito.when(inventory.send(ArgumentMatchers.any(BeforeItemPutInInventory.class))).then(
                invocation -> {
                    Object arg = invocation.getArguments()[0];
                    if (arg instanceof BeforeItemPutInInventory) {
                        BeforeItemPutInInventory event = (BeforeItemPutInInventory) arg;
                        if (event.getSlot() == 1) {
                            event.consume();
                        }
                    }
                    return null;
                });

        List<Integer> toSlots = Arrays.asList(0, 1, 2, 3, 4);

        // The method that gets tested:
        boolean result = inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory,
                toSlots);
        Assert.assertTrue(result);

        /*
         * The free slot 1 can't be used since it's blocked:
         * => A1 gets still filled up and the rest of the items gets placed at slot 2
         */
        Assert.assertEquals(10, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(3, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(EntityRef.NULL, toInventoryComp.itemSlots.get(1));
        Assert.assertEquals(itemA2, toInventoryComp.itemSlots.get(2));
        Assert.assertFalse(fromInventoryComp.itemSlots.get(fromSlot).exists());
    }

    /**
     * A shift click isn't possible because the removal of the item gets blocked
     */
    @Test
    public void testMoveItemToSlotsWithRemovalVeto() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 8, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA2 = createItem("A", 5, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA2);

        // Placement to slots 1 gets blocked by veto
        Mockito.when(fromInventory.send(ArgumentMatchers.any(BeforeItemRemovedFromInventory.class))).then(
                invocation -> {
                    Object arg = invocation.getArguments()[0];
                    if (arg instanceof BeforeItemRemovedFromInventory) {
                        BeforeItemRemovedFromInventory event = (BeforeItemRemovedFromInventory) arg;
                        if (event.getSlot() == 1) {
                            event.consume();
                        }
                    }
                    return null;
                });

        List<Integer> toSlots = Arrays.asList(0, 1, 2, 3, 4);

        // The method that gets tested:
        boolean result = inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory,
                toSlots);
        Assert.assertFalse(result);

        Assert.assertEquals(8, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(5, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(EntityRef.NULL, toInventoryComp.itemSlots.get(1));
        Assert.assertEquals(itemA2, fromInventoryComp.itemSlots.get(fromSlot));
    }

    @Test
    public void testMoveItemToSlotsWithFullTargetInventorySlots() {
        int stackSize = 10;
        EntityRef toInventory = inventory;
        InventoryComponent toInventoryComp = toInventory.getComponent(InventoryComponent.class);
        EntityRef itemA1 = createItem("A", 10, stackSize);
        EntityRef itemB1 = createItem("B", 8, stackSize);
        EntityRef itemA2 = createItem("A", 10, stackSize);
        toInventoryComp.itemSlots.set(0, itemA1);
        toInventoryComp.itemSlots.set(1, itemB1);
        toInventoryComp.itemSlots.set(2, itemA2);

        EntityRef fromInventory = Mockito.mock(EntityRef.class);
        InventoryComponent fromInventoryComp = new InventoryComponent(5);
        Mockito.when(fromInventory.getComponent(InventoryComponent.class)).thenReturn(fromInventoryComp);
        EntityRef itemA3 = createItem("A", 4, stackSize);
        int fromSlot = 1;
        fromInventoryComp.itemSlots.set(fromSlot, itemA3);

        List<Integer> toSlots = Arrays.asList(0, 1, 2);

        // The method that gets tested:
        boolean result = inventoryAuthoritySystem.moveItemToSlots(instigator, fromInventory, fromSlot, toInventory,
                toSlots);
        Assert.assertFalse(result);

        Assert.assertEquals(10, itemA1.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(10, itemA2.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(4, itemA3.getComponent(ItemComponent.class).stackCount);
        Assert.assertEquals(itemA3, fromInventoryComp.itemSlots.get(fromSlot));
    }

}