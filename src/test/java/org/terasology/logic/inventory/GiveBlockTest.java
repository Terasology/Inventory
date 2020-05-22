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
import org.junit.jupiter.api.Test;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Verify that adding blocks to an inventory is working as expected.
 * <p>
 * Dual test to {@link GiveItemTest}.
 *
 * @see GiveItemTest
 */
public class GiveBlockTest {

    static final String URI_DIRT = "CoreAssets:Dirt";

    private static ModuleTestingEnvironment context;

    @BeforeAll
    public static void setup() throws Exception {
        context = new ModuleTestingEnvironment() {
            @Override
            public Set<String> getDependencies() {
                return Sets.newHashSet("CoreAssets", "Inventory");
            }
        };
        context.setup();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        context.tearDown();
    }

    @Test
    public void giveItemSingleBlock() {
        final Context hostContext = context.getHostContext();
        final EntityManager entityManager = hostContext.get(EntityManager.class);
        final InventoryManager inventoryManager = hostContext.get(InventoryManager.class);
        final BlockManager blockManager = hostContext.get(BlockManager.class);

        final EntityRef inventory = entityManager.create(new InventoryComponent(3));

        BlockItemFactory factory = new BlockItemFactory(entityManager);
        EntityRef blockItem = factory.newInstance(blockManager.getBlockFamily(URI_DIRT));
        Assert.assertNotEquals("Cannot create a block item instance for '" + URI_DIRT + "'", EntityRef.NULL, blockItem);

        inventoryManager.giveItem(inventory, EntityRef.NULL, blockItem);

        final List<EntityRef> filledSlots =
                inventory.getComponent(InventoryComponent.class).itemSlots.stream()
                        .filter(entityRef -> entityRef != EntityRef.NULL)
                        .collect(Collectors.toList());

        Assert.assertEquals("Exactly one slot should be filled", 1, filledSlots.size());

        filledSlots.forEach(item -> Assert.assertEquals("Slot should contain exactly the added block", blockItem, item));
    }
}
