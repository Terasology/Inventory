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

    private BlockItemFactory blockFactory;

    @Override
    public void initialise() {
        blockFactory = new BlockItemFactory(entityManager);
    }

    @ReceiveEvent(components = {StartingInventoryComponent.class, InventoryComponent.class})
    public void onStartingInventory(OnAddedComponent event, EntityRef entityRef) {
        StartingInventoryComponent startingInventoryComponent =
                entityRef.getComponent(StartingInventoryComponent.class);
        if (!startingInventoryComponent.provided) {
            InventoryComponent inventoryComponent = entityRef.getComponent(InventoryComponent.class);
            logger.info("Adding starting inventory to {}, entity has {} slots",
                    entityRef.getParentPrefab().getName(), inventoryComponent.itemSlots.size());

            for (StartingInventoryComponent.InventoryItem item : startingInventoryComponent.items) {
                addToInventory(entityRef, item, inventoryComponent);
            }
            startingInventoryComponent.provided = true;
            entityRef.saveComponent(startingInventoryComponent);
        }
    }

    private boolean addToInventory(EntityRef entityRef,
                                   StartingInventoryComponent.InventoryItem item,
                                   InventoryComponent inventoryComponent) {
        String uri = item.uri;
        int quantity = item.quantity;
        return addToInventory(entityRef, uri, quantity, inventoryComponent);
    }

    private boolean addToInventory(EntityRef entityRef,
                                   String uri,
                                   int quantity,
                                   InventoryComponent inventoryComponent) {
        // Determine if this is a Block or Item
        logger.info("Adding {} {}", quantity, uri);
        BlockFamily blockFamily = blockManager.getBlockFamily(uri);
        boolean success = true;
        if (blockFamily != null) {
            // try give blocks
            success = tryAddBlocks(entityRef, blockFamily, quantity, inventoryComponent);
        } else {
            // try give items
            success = tryAddItems(entityRef, uri, quantity, inventoryComponent);
        }
        if (!success) {
            logger.error("Failed to add {} {}", quantity, uri);
        }
        return success;
    }

    private boolean tryAddBlocks(EntityRef entityRef,
                                 BlockFamily blockFamily,
                                 int quantity,
                                 InventoryComponent inventoryComponent) {
        long available = availableSlots(inventoryComponent);
        if (available >= 1) {
            if (quantity > 99) {
                logger.warn("Block stack of > 99 found. Currently maximum block stack size is 99. Adding 99");
            }
            return inventoryManager.giveItem(
                    entityRef, EntityRef.NULL, blockFactory.newInstance(blockFamily, Math.min(quantity, 99)));
        }
        return false;
    }

    private boolean tryAddItems(EntityRef entityRef,
                                String uri,
                                int quantity,
                                InventoryComponent inventoryComponent) {
        long available = availableSlots(inventoryComponent);
        // Find out of the item is stackable
        Prefab prefab = Assets.getPrefab(uri).orElse(null);
        if (prefab == null) {
            logger.error("Failed to find prefab {}", uri);
            return false;
        }
        ItemComponent component = prefab.getComponent(ItemComponent.class);
        if (component == null) {
            logger.error("Failed to find ItemComponent for {}", uri);
            return false;
        }
        if (component.stackId.length() == 0) {
            // Item is not stackable, one slot used per item
            if (available >= quantity) {
                for (int i = 0; i < quantity; ++i) {
                    inventoryManager.giveItem(entityRef, EntityRef.NULL, entityManager.create(uri));
                }
            } else {
                logger.error("Insufficient inventory space to add {} {}", quantity, uri);
            }
        } else {
            // Item stackable, inventory manager will handle stacking, but we still need to know
            // how much we have added
            int numFullStacks = quantity / component.maxStackSize;
            int remainder = quantity % component.maxStackSize;

            // Add full stacks
            for (int i = 0; i < numFullStacks; ++i) {
                for (int j = 0; j < component.maxStackSize; ++j) {
                    inventoryManager.giveItem(entityRef,
                            EntityRef.NULL, entityManager.create(uri));
                }
            }

            // Add remainder
            for (int i = 0; i < remainder; ++i) {
                inventoryManager.giveItem(entityRef,
                        EntityRef.NULL, entityManager.create(uri));
            }
        }
        return true;
    }

    private long availableSlots(InventoryComponent inventoryComponent) {
        return inventoryComponent.itemSlots.stream().filter(ref -> ref.equals(EntityRef.NULL)).count();
    }
}
