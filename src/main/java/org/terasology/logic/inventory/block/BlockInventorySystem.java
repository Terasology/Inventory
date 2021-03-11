// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.inventory.block;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;
import org.terasology.engine.world.block.items.OnBlockToItem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockInventorySystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent(components = {InventoryComponent.class, RetainBlockInventoryComponent.class})
    public void copyBlockInventory(OnBlockToItem event, EntityRef blockEntity) {
        EntityRef inventoryItem = event.getItem();
        int slotCount = InventoryUtils.getSlotCount(blockEntity);
        inventoryItem.addComponent(new InventoryComponent(slotCount));
        for (int i = 0; i < slotCount; i++) {
            inventoryManager.switchItem(blockEntity, blockEntity, i, inventoryItem, i);
        }
        ItemComponent itemComponent = inventoryItem.getComponent(ItemComponent.class);
        if (InventoryUtils.isStackable(itemComponent)) {
            itemComponent.stackId = "";
            inventoryItem.saveComponent(itemComponent);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class, BlockItemComponent.class})
    public void onPlaced(OnBlockItemPlaced event, EntityRef itemEntity) {
        int slotCount = InventoryUtils.getSlotCount(itemEntity);
        for (int i = 0; i < slotCount; i++) {
            inventoryManager.switchItem(event.getPlacedBlock(), itemEntity, i, itemEntity, i);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class, DropBlockInventoryComponent.class, LocationComponent.class})
    public void dropContentsOfInventory(DoDestroyEvent event, EntityRef entity, LocationComponent location) {
        Vector3f position = location.getWorldPosition(new Vector3f());

        FastRandom random = new FastRandom();
        int slotCount = InventoryUtils.getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            EntityRef itemInSlot = InventoryUtils.getItemAt(entity, i);
            if (itemInSlot.exists()) {
                inventoryManager.removeItem(entity, entity, itemInSlot, false);
                itemInSlot.send(new DropItemEvent(position));
                itemInSlot.send(new ImpulseEvent(random.nextVector3f(30.0f, new Vector3f())));
            }
        }
    }
}
