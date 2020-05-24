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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterSystem(RegisterMode.AUTHORITY)
public class StartingInventorySystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(StartingInventorySystem.class);

    @In
    BlockManager blockManager;

    @In
    InventoryManager inventoryManager;

    @In
    EntityManager entityManager;

    @In
    PrefabManager prefabManager;

    BlockItemFactory blockFactory;

    @Override
    public void initialise() {
        blockFactory = new BlockItemFactory(entityManager);
    }

    @ReceiveEvent
    public void onStartingInventory(OnPlayerSpawnedEvent event,
                                    EntityRef entityRef,
                                    StartingInventoryComponent startingInventory) {
        addItemsIfPossible(entityRef, startingInventory.items);
        entityRef.removeComponent(StartingInventoryComponent.class);
    }

    /**
     * Ensure that the item references a non-empty URI and a quantity greater than zero.
     * <p>
     * This method logs WARNINGs if the item could not be validated.
     *
     * @param item the inventory item to validate
     * @return true if the item has non-empty URI and quantity greater zero, false otherwise
     */
    private boolean isValid(StartingInventoryComponent.InventoryItem item) {
        if (item.uri == null || item.uri.isEmpty()) {
            logger.warn("Improperly specified starting inventory item: Uri is null");
            return false;
        }
        if (item.quantity <= 0) {
            logger.warn("Improperly specified starting inventory item: quantity for '{}' less than zero ({})",
                    item.uri, item.quantity);
            return false;
        }
        return true;
    }

    private void addToInventory(EntityRef entityRef,
                                StartingInventoryComponent.InventoryItem item) {

        Optional<Supplier<EntityRef>> possibleItem =
                resolveAsBlock(item).map(Optional::of).orElseGet(() -> resolveAsItem(item));

        possibleItem.ifPresent(itemCreator -> {
            Stream.generate(itemCreator)
                    .limit(item.quantity)
                    .peek(i -> addItemsIfPossible(i, item.items))
                    .collect(Collectors.toList())
                    .forEach(o -> inventoryManager.giveItem(entityRef, EntityRef.NULL, o));
        });
    }

    /**
     * Adds all valid objects to this entity if it has an item component.
     * <p>
     * Inventory objects are valid if {@link #isValid(StartingInventoryComponent.InventoryItem)} holds.
     * <p>
     * If the list of nested items is empty or the entity does not have an inventory component this method does
     * nothing.
     *
     * @param entity the entity to add the starting inventory objects to
     * @param items the objects to add to the entity's inventory
     */
    private void addItemsIfPossible(EntityRef entity,
                                    List<StartingInventoryComponent.InventoryItem> items) {
        if (entity.hasComponent(InventoryComponent.class)) {
            items.stream()
                    .filter(this::isValid)
                    .forEach(item -> addToInventory(entity, item));
        } else {
            logger.warn(
                    "Cannot add starting inventory objects to entity without inventory component!\n{}",
                    entity.toFullDescription());
        }
    }

    /**
     * Attempt to resolve the given item as block and yield a supplier for new block items.
     *
     * @param object the item to resolve as block item
     * @return an optional supplier for block items if the item references a block family, empty otherwise
     */
    private Optional<Supplier<EntityRef>> resolveAsBlock(StartingInventoryComponent.InventoryItem object) {
        return Optional.ofNullable(blockManager.getBlockFamily(object.uri))
                .map(blockFamily -> () -> blockFactory.newInstance(blockFamily));
    }

    /**
     * Attempt to resolve the given item as item prefab and yield a supplier to create new item instances.
     * <p>
     * The prefab the object URI resolves to must have an {@link ItemComponent}.
     *
     * @param object the item to resolve as item prefab
     * @return an optional supplier for a prefab item if the object URI resolves to a prefab, empty otherwise
     */
    private Optional<Supplier<EntityRef>> resolveAsItem(StartingInventoryComponent.InventoryItem object) {
        return Optional.ofNullable(prefabManager.getPrefab(object.uri))
                .filter(prefab -> prefab.hasComponent(ItemComponent.class))
                .map(prefab -> () -> entityManager.create(object.uri));
    }
}
