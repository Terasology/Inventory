// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.systems;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.components.InventoryItemComponent;
import org.terasology.module.inventory.components.StartingInventoryComponent;
import org.terasology.module.inventory.events.RequestInventoryEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
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

    /**
     * Collect entities without inventory component, which have nested items configured in the {@link StartingInventoryComponent}.
     */
    private final Set<String> entitiesWithoutInventory = Sets.newHashSet();

    @Override
    public void initialise() {
        blockFactory = new BlockItemFactory(entityManager);
    }

    @ReceiveEvent
    public void onStartingInventory(OnPlayerSpawnedEvent event,
                                    EntityRef player,
                                    StartingInventoryComponent startingInventory) {
        player.send(new RequestInventoryEvent(startingInventory.items));
    }

    @ReceiveEvent(components = InventoryComponent.class)
    public void onRequestInventory(RequestInventoryEvent event, EntityRef player) {
        entitiesWithoutInventory.clear();
        addItemsTo(event.items, player, player.getParentPrefab().getName());
        player.removeComponent(StartingInventoryComponent.class);
        logErrors();
    }

    /**
     * Collectively log errors found during the assembly of the starting inventory.
     * <p>
     * The collected errors in {@link #entitiesWithoutInventory} will be empty after this call returns.
     */
    private void logErrors() {
        if (!entitiesWithoutInventory.isEmpty()) {
            logger.warn("Cannot add starting inventory objects to entities without inventory component!\n{}", entitiesWithoutInventory);
            entitiesWithoutInventory.clear();
        }
    }

    /**
     * Ensure that the item references a non-empty URI and a quantity greater than zero.
     * <p>
     * This method logs WARNINGs if the item could not be validated.
     *
     * @param item the inventory item to validate
     * @return true if the item has non-empty URI and quantity greater zero, false otherwise
     */
    private boolean isValid(InventoryItemComponent item) {
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

    /**
     * Attempt to resolve the given {@code item} as block or item and add it to the entity's inventory.
     * <p>
     * If the item cannot be resolved the target inventory will not be changed.
     * <p>
     * <strong>Calling this method may add errors to {@link #entitiesWithoutInventory}.</strong>
     *
     * @param entity the entity with {@link InventoryComponent} to add the item to
     * @param item the item to add to the entity's inventory
     */
    private void addToInventory(EntityRef entity, InventoryItemComponent item) {

        //TODO(Java9): Use Optional::or instead (https://docs.oracle.com/javase/9/docs/api/java/util/Optional
        // .html#or-java.util.function.Supplier-)
        //             This article describes the issue and provides the solution used here:
        //                  https://www.baeldung.com/java-optional-or-else-optional#1-lazy-evaluation
        final Optional<Supplier<EntityRef>> possibleItem =
                resolveAsItem(item.uri).map(Optional::of).orElseGet(() -> resolveAsBlock(item.uri));

        if (possibleItem.isPresent()) {
            Stream.generate(possibleItem.get())
                    .limit(item.quantity)
                    .map(i -> addItemsTo(item.items, i, item.uri))
                    .forEach(o -> inventoryManager.giveItem(entity, EntityRef.NULL, o));
        } else {
            logger.warn("Could not resolve '{}' to either block or item.", item.uri);
        }
    }

    /**
     * Adds all valid objects to this entity if it has an item component.
     * <p>
     * Inventory objects are valid if {@link #isValid(InventoryItemComponent)} holds.
     * <p>
     * If the list of nested items is empty or the entity does not have an inventory component this method does
     * nothing.
     * <p>
     * <strong>Calling this method may add errors to {@link #entitiesWithoutInventory}.</strong>
     *
     * @param items the objects to add to the entity's inventory
     * @param entity the entity to add the starting inventory objects to
     * @param entityDescriptor a descriptive string (name or Uri) for the entity, used for logging
     */
    private EntityRef addItemsTo(List<InventoryItemComponent> items, EntityRef entity,
                                 String entityDescriptor) {
        if (items.isEmpty() || entity.hasComponent(InventoryComponent.class)) {
            items.stream()
                    .filter(this::isValid)
                    .forEach(item -> addToInventory(entity, item));
        } else {
            entitiesWithoutInventory.add(entityDescriptor);
        }
        return entity;
    }

    /**
     * Attempt to resolve the given URI as block and yield a supplier for new block items.
     *
     * @param uri the URI to resolve as block item
     * @return an optional supplier for block items if the URI references a block family, empty otherwise
     */
    private Optional<Supplier<EntityRef>> resolveAsBlock(final String uri) {
        return Optional.ofNullable(blockManager.getBlockFamily(uri))
                .map(blockFamily -> () -> blockFactory.newInstance(blockFamily));
    }

    /**
     * Attempt to resolve the given URI as item prefab and yield a supplier to create new item instances.
     * <p>
     * The prefab the object URI resolves to must have an {@link ItemComponent}.
     *
     * @param uri the URI to resolve as item prefab
     * @return an optional supplier for a prefab item if the URI resolves to a prefab, empty otherwise
     */
    private Optional<Supplier<EntityRef>> resolveAsItem(String uri) {
        return Optional.ofNullable(prefabManager.getPrefab(uri))
                .filter(prefab -> prefab.hasComponent(ItemComponent.class))
                .map(prefab -> () -> entityManager.create(uri));
    }
}
