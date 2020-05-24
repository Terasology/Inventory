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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.moduletestingenvironment.ModuleTestingEnvironment;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Verify that the {@link ItemCommands} are working as expected.
 */
public class ItemCommandsTest {

    static final String URI_DIRT = "CoreAssets:Dirt";

    private static ModuleTestingEnvironment mte;

    private Context hostContext;
    private EntityManager entityManager;
    private InventoryManager inventoryManager;
    private BlockManager blockManager;
    private ItemCommands itemCommands;

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
        hostContext = mte.getHostContext();
        entityManager = hostContext.get(EntityManager.class);
        inventoryManager = hostContext.get(InventoryManager.class);
        blockManager = hostContext.get(BlockManager.class);
        itemCommands = hostContext.get(ItemCommands.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 99, 100})
    public void giveItemSingleBlockStack(int amount) {

        Context clientContext = mte.createClient();
        LocalPlayer localPlayer = clientContext.get(LocalPlayer.class);
        final EntityRef player = localPlayer.getClientEntity();
        final EntityRef character = localPlayer.getCharacterEntity();
        character.upsertComponent(InventoryComponent.class, maybeInventory -> maybeInventory.orElse(new InventoryComponent(40)));

        final BlockItemFactory factory = new BlockItemFactory(entityManager);
        final EntityRef blockItem = factory.newInstance(blockManager.getBlockFamily(URI_DIRT), amount);

        Assert.assertNotEquals("Cannot create a block item instance for '" + URI_DIRT + "'", EntityRef.NULL, blockItem);

        //TODO: I don't know what to pass in there, whether it needs to be the player, character, or something else
        itemCommands.give(player, URI_DIRT, amount, null);

        final List<EntityRef> filledSlots =
                character.getComponent(InventoryComponent.class).itemSlots.stream()
                        .filter(entityRef -> entityRef != EntityRef.NULL)
                        .collect(Collectors.toList());

        final int maxStackSize = Byte.toUnsignedInt(blockItem.getComponent(ItemComponent.class).maxStackSize);
        final int expectedFilledSlots = TeraMath.ceilToInt(amount / (float) maxStackSize);
        Assert.assertEquals("Unexpected number of filled inventory slots", expectedFilledSlots, filledSlots.size());

        filledSlots.forEach(item ->
                Assert.assertEquals("Slot should contain exactly the added block",
                        blockItem, item));

        filledSlots.forEach(item ->
                Assert.assertEquals("Item quantity diverges from requested amount",
                        amount, Byte.toUnsignedInt(item.getComponent(ItemComponent.class).stackCount)));

        for (int i = 0; i < filledSlots.size(); i++) {
            int actualStackCount = Byte.toUnsignedInt(filledSlots.get(i).getComponent(ItemComponent.class).stackCount);
            if (i == amount / maxStackSize) {
                Assert.assertEquals(amount % maxStackSize, actualStackCount);
            } else {
                Assert.assertEquals(maxStackSize, actualStackCount);
            }
        }
    }

}
