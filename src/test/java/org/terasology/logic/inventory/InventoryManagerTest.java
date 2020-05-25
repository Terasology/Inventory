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

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryManagerTest {

    private static ModuleTestingEnvironment mte;

    private EntityManager entityManager;
    private InventoryManager inventoryManager;
    private BlockManager blockManager;
    private EntityRef inventory;

    @BeforeAll
    public static void setup() throws Exception {
        mte = new ModuleTestingEnvironment() {
            @Override
            public Set<String> getDependencies() {
                return Sets.newHashSet("CoreAssets", "Inventory");
            }
        };
        mte.setup();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        mte.tearDown();
    }

    @BeforeEach
    public void beforeEach() {
        Context hostContext = mte.getHostContext();

        entityManager = hostContext.get(EntityManager.class);
        inventoryManager = hostContext.get(InventoryManager.class);
        blockManager = hostContext.get(BlockManager.class);

        inventory = entityManager.create(new InventoryComponent(3));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void giveItem_zeroBlocks(int amount) {
        EntityRef blockItem = getBlockItem(entityManager, blockManager, amount);
        inventoryManager.giveItem(inventory, EntityRef.NULL, blockItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assert.assertEquals("No slots should be filled", 0, filledSlots.size());
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, -1})
    public void giveItem_zeroItems(byte amount) {
        EntityRef item = getPrefabItem(entityManager, amount, false);
        inventoryManager.giveItem(inventory, EntityRef.NULL, item);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assert.assertEquals("No slots should be filled", 0, filledSlots.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 99})
    public void giveItem_blockStack(int amount) {
        EntityRef blockItem = getBlockItem(entityManager, blockManager, amount);
        inventoryManager.giveItem(inventory, EntityRef.NULL, blockItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assert.assertEquals("Exactly one slot should be filled", 1, filledSlots.size());
        filledSlots.forEach(item -> {
            Assert.assertEquals("Slot should contain exactly the added block", blockItem, item);

            final int maxStackSize = blockItem.getComponent(ItemComponent.class).maxStackSize;
            Assert.assertEquals("Stack count should be " + amount + " or maximum stack size",
                    Math.min(amount, maxStackSize), item.getComponent(ItemComponent.class).stackCount);
        });
    }

    @Test
    public void giveItem_nonStackableItem() {
        EntityRef singleItem = getPrefabItem(entityManager, (byte) 1, false);
        inventoryManager.giveItem(inventory, EntityRef.NULL, singleItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assert.assertEquals("Exactly one slot should be filled", 1, filledSlots.size());
        filledSlots.forEach(slot -> {
            Assert.assertEquals("Slot should contain exactly the stackable item", singleItem, slot);
            Assert.assertEquals("Unexpected stack count!", 1, slot.getComponent(ItemComponent.class).stackCount);
        });
    }

    @ParameterizedTest
    @ValueSource(bytes = {1, 2, 99})
    public void giveItem_stackableItem(byte amount) {
        final EntityRef stackedItem = getPrefabItem(entityManager, amount, true);

        Assert.assertFalse(stackedItem.getComponent(ItemComponent.class).stackId.isEmpty());

        inventoryManager.giveItem(inventory, EntityRef.NULL, stackedItem);

        final List<EntityRef> filledSlots = getFilledSlots(inventory);
        Assert.assertEquals("Exactly one slot should be filled", 1, filledSlots.size());
        filledSlots.forEach(slot -> {
            Assert.assertEquals("Slot should contain exactly the stackable item", stackedItem, slot);

            final int maxStackSize = stackedItem.getComponent(ItemComponent.class).maxStackSize;
            Assert.assertEquals("Unexpected stack count!",
                    Math.min(amount, maxStackSize), slot.getComponent(ItemComponent.class).stackCount);
        });
    }

    private static EntityRef getBlockItem(EntityManager entityManager, BlockManager blockManager, int amount) {
        final String uri = "CoreAssets:Dirt";
        final BlockItemFactory factory = new BlockItemFactory(entityManager);
        final EntityRef blockItem = factory.newInstance(blockManager.getBlockFamily(uri), amount);

        Assert.assertNotEquals("Cannot create a block item instance for '" + uri + "'",
                EntityRef.NULL, blockItem);
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
