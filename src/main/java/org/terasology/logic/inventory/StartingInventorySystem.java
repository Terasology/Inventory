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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem(RegisterMode.AUTHORITY)
public class StartingInventorySystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(StartingInventorySystem.class);

    @In
    BlockManager blockManager;

    @In
    InventoryManager inventoryManager;

    @In
    EntityManager entityManager;

    @ReceiveEvent(components = {StartingInventoryComponent.class, InventoryComponent.class})
    public void onStartingInventory(OnAddedComponent event, EntityRef entityRef) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        StartingInventoryComponent startingInventoryComponent =
                entityRef.getComponent(StartingInventoryComponent.class);
        InventoryComponent inventoryComponent = entityRef.getComponent(InventoryComponent.class);
        logger.info("Adding starting inventory to {}, entity has {}  slots",
                entityRef.getParentPrefab().getName(), inventoryComponent.itemSlots.size());

        int addedItems = 0;
        for (StartingInventoryComponent.InventoryItem item : startingInventoryComponent.items) {
            String uri = item.uri;

            logger.info("Adding {} {}", item.quantity, uri);
            BlockFamily blockFamily = blockManager.getBlockFamily(uri);
            if (blockFamily != null) {
                // If the quantity specified exceeds the maxStackSize then nothing will be added, therefore
                // at most one slot will be used // TODO split into maxStackSize chunks
                if (hasSpace(inventoryComponent.itemSlots.size(), addedItems, 1)) {
                    inventoryManager.giveItem(
                            entityRef, EntityRef.NULL, blockFactory.newInstance(blockFamily, item.quantity));
                    ++addedItems;
                } else {
                    logger.error("Insufficient inventory space to add {} {}", item.quantity, uri);
                }
            } else {
                // todo get stack size from ItemComponent
                // Find out of the item is stackable
                Prefab prefab = Assets.getPrefab(item.uri).orElse(null);
                if (prefab == null) {
                    logger.error("Failed to find prefab {}", item.uri);
                    continue;
                }
                ItemComponent component = prefab.getComponent(ItemComponent.class);
                if (component == null) {
                    logger.error("Failed to find ItemComponent for {}", item.uri);
                    continue;
                }
                logger.info("'{}' {}", component.stackId, component.maxStackSize);
                if (component.stackId.length() == 0) {
                    // Item is not stackable, one slot used per item
                    if (hasSpace(inventoryComponent.itemSlots.size(), addedItems, item.quantity)) {
                        for (int i = 0; i < item.quantity; ++i) {
                            inventoryManager.giveItem(entityRef,
                                    EntityRef.NULL, entityManager.create(uri));
                            ++addedItems;
                        }
                    } else {
                        logger.error("Insufficient inventory space to add {} {}", item.quantity, uri);
                    }
                } else {
                    // Item stackable, inventory manager will handle stacking, but we still need to know
                    // how much we have added
                    int numFullStacks = item.quantity / component.maxStackSize;
                    int remainder = item.quantity % component.maxStackSize;
                    logger.info("quantity {}, maxStack {}, full {}, rem {}",
                            item.quantity, component.maxStackSize, numFullStacks, remainder);
                    // Add full stacks
                    for (int i = 0; i < numFullStacks; ++i) {
                        for (int j = 0; j < component.maxStackSize; ++j) {
                            inventoryManager.giveItem(entityRef,
                                    EntityRef.NULL, entityManager.create(uri));
                        }
                        // Every full stack is one slot
                        ++addedItems;
                    }

                    // Add remainder
                    for (int i = 0; i < remainder; ++i) {
                        inventoryManager.giveItem(entityRef,
                                EntityRef.NULL, entityManager.create(uri));
                    }
                    if (remainder > 0) {
                        ++addedItems;
                    }
                    logger.info("Added {} slots total", addedItems);
                }

            }
        }
    }

    private boolean hasSpace(int numSlots, int addedSoFar, int toAdd) {
        return addedSoFar + toAdd <= numSlots;
    }
}
