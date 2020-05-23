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

import com.google.common.collect.Lists;
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
                                    StartingInventoryComponent startingInventoryComponent) {

        InventoryComponent inventoryComponent =
                Optional.ofNullable(entityRef.getComponent(InventoryComponent.class))
                        .orElse(new InventoryComponent(40));
        entityRef.addOrSaveComponent(inventoryComponent);

        for (StartingInventoryComponent.InventoryItem item : startingInventoryComponent.items) {
            if (validateItem(item)) {
                addToInventory(entityRef, item, inventoryComponent);
            }
        }
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
    private boolean validateItem(StartingInventoryComponent.InventoryItem item) {
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
                                StartingInventoryComponent.InventoryItem item,
                                InventoryComponent inventoryComponent) {
        String uri = item.uri;
        int quantity = item.quantity;

        final List<EntityRef> objects =
                tryAsBlock(uri, quantity, item.items)
                        .map(Optional::of)
                        .orElseGet(() -> tryAsItem(uri, quantity))
                        .orElse(Lists.newArrayList());

        objects.forEach(o ->
                inventoryManager.giveItem(entityRef, EntityRef.NULL, o)
        );
    }

    /**
     * Adds an {@link InventoryComponent} to the given entity holding the specified items.
     *
     * @param entity the entity that should hold the nested inventory
     * @param items  items to be filled into the nested inventory
     */
    private void addNestedInventory(EntityRef entity,
                                    List<StartingInventoryComponent.InventoryItem> items) {
        InventoryComponent nestedInventory =
                Optional.ofNullable(entity.getComponent(InventoryComponent.class))
                        .orElseGet(() -> new InventoryComponent(30));
        entity.addOrSaveComponent(nestedInventory);
        items.stream()
                .filter(this::validateItem)
                .forEach(i -> addToInventory(entity, i, nestedInventory));
    }

    private Optional<List<EntityRef>> tryAsBlock(String uri,
                                                 int quantity,
                                                 List<StartingInventoryComponent.InventoryItem> nestedItems) {
        return Optional.ofNullable(blockManager.getBlockFamily(uri))
                .map(blockFamily ->
                        Stream.generate(() -> blockFactory.newInstance(blockFamily))
                                .limit(quantity)
                                .peek(block -> {
                                    if (!nestedItems.isEmpty()) {
                                        addNestedInventory(block, nestedItems);
                                    }
                                })
                                .collect(Collectors.toList()));
    }

    private Optional<List<EntityRef>> tryAsItem(String uri,
                                                int quantity) {
        return Optional.ofNullable(prefabManager.getPrefab(uri))
                .filter(p -> p.hasComponent(ItemComponent.class))
                .map(p -> Stream.generate(() -> entityManager.create(uri)).limit(quantity).collect(Collectors.toList()));
    }
}
