/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.inventory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.extension.Dependencies;
import org.terasology.moduletestingenvironment.extension.UseWorldGenerator;

import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MTEExtension.class)
@UseWorldGenerator("ModuleTestingEnvironment:empty")
@Dependencies({"Inventory"})
public class InventoryManagerTest {

    @In
    protected EntityManager entityManager;
    @In
    protected InventoryManager inventoryManager;
    @In
    protected BlockManager blockManager;

    private EntityRef inventory;

    @BeforeEach
    public void beforeEach() {
        inventory = entityManager.create(new InventoryComponent(3));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void giveItem_zeroBlocks(int amount) {
        EntityRef blockItem = getBlockItem(entityManager, blockManager, amount);
        inventoryManager.giveItem(inventory, EntityRef.NULL, blockItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assertions.assertEquals(0, filledSlots.size(), "No slots should be filled");
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, -1})
    public void giveItem_zeroItems(byte amount) {
        EntityRef item = getPrefabItem(entityManager, amount, false);
        inventoryManager.giveItem(inventory, EntityRef.NULL, item);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assertions.assertEquals(0, filledSlots.size(), "No slots should be filled");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 99})
    public void giveItem_blockStack(int amount) {
        EntityRef blockItem = getBlockItem(entityManager, blockManager, amount);
        inventoryManager.giveItem(inventory, EntityRef.NULL, blockItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assertions.assertEquals(1, filledSlots.size(), "Exactly one slot should be filled");
        filledSlots.forEach(item -> {
            Assertions.assertEquals(blockItem, item, "Slot should contain exactly the added block");
            Assertions.assertEquals(amount, item.getComponent(ItemComponent.class).stackCount, "Unexpected stack count!");
        });
    }

    @Test
    public void giveItem_nonStackableItem() {
        EntityRef singleItem = getPrefabItem(entityManager, (byte) 1, false);
        inventoryManager.giveItem(inventory, EntityRef.NULL, singleItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assertions.assertEquals(1, filledSlots.size(), "Exactly one slot should be filled");
        filledSlots.forEach(item -> {
            Assertions.assertEquals(singleItem, item, "Slot should contain exactly the non-stackable item");
            Assertions.assertEquals(1, item.getComponent(ItemComponent.class).stackCount, "Unexpected stack count!");
        });
    }

    @ParameterizedTest
    @ValueSource(bytes = {1, 2, 99})
    public void giveItem_stackableItem(byte amount) {
        final EntityRef stackedItem = getPrefabItem(entityManager, amount, true);

        inventoryManager.giveItem(inventory, EntityRef.NULL, stackedItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assertions.assertEquals(1, filledSlots.size(), "Exactly one slot should be filled");
        filledSlots.forEach(item -> {
            Assertions.assertEquals(stackedItem, item, "Slot should contain exactly the stackable item");
            Assertions.assertEquals(amount, item.getComponent(ItemComponent.class).stackCount, "Unexpected stack count!");
        });
    }

    private static EntityRef getBlockItem(EntityManager entityManager, BlockManager blockManager, int amount) {
        final String uri = "CoreAssets:Dirt";
        final BlockItemFactory factory = new BlockItemFactory(entityManager);
        final EntityRef blockItem = factory.newInstance(blockManager.getBlockFamily(uri), amount);

        Assertions.assertNotEquals(EntityRef.NULL, blockItem,
                "Cannot create a block item instance for '" + uri + "'");
        return blockItem;
    }

    private static EntityRef getPrefabItem(EntityManager entityManager, byte amount, boolean stackable) {
        final String uri = "CoreAssets:Axe";
        final EntityRef item = entityManager.create(uri);
        item.updateComponent(ItemComponent.class, component -> {
            component.stackCount = amount;
            if (stackable) {
                component.stackId = uri;
            }
            return component;
        });
        return item;
    }

    private static List<EntityRef> getFilledSlots(EntityRef inventory) {
        return inventory.getComponent(InventoryComponent.class).itemSlots.stream()
                .filter(entityRef -> entityRef != EntityRef.NULL)
                .collect(Collectors.toList());
    }
}
