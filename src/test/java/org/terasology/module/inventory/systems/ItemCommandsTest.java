// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.systems;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.math.TeraMath;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.components.ItemCommands;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Verify that the {@link ItemCommands} are working as expected.
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies({"CoreAssets", "Inventory"})
class ItemCommandsTest {

    static final String URI_DIRT = "CoreAssets:Dirt";

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 99, 100})
    void giveItemSingleBlockStack(int amount, ModuleTestingHelper helper, EntityManager entityManager,
                                   BlockManager blockManager, ItemCommands itemCommands) throws IOException {
        Context clientContext = helper.createClient();
        LocalPlayer localPlayer = clientContext.get(LocalPlayer.class);
        EntityRef player = localPlayer.getClientEntity();
        EntityRef character = localPlayer.getCharacterEntity();
        character.upsertComponent(InventoryComponent.class, maybeInventory -> maybeInventory.orElse(new InventoryComponent(40)));

        BlockItemFactory factory = new BlockItemFactory(entityManager);
        EntityRef blockItem = factory.newInstance(blockManager.getBlockFamily(URI_DIRT), amount);

        assertNotEquals(EntityRef.NULL, blockItem, "Cannot create a block item instance for '" + URI_DIRT + "'");

        itemCommands.give(player, URI_DIRT, amount, null);

        List<EntityRef> filledSlots = InventoryUtils.filledSlots(character.getComponent(InventoryComponent.class));

        int maxStackSize = Byte.toUnsignedInt(blockItem.getComponent(ItemComponent.class).maxStackSize);
        int expectedFilledSlots = TeraMath.ceilToInt(amount / (float) maxStackSize);
        assertEquals(expectedFilledSlots, filledSlots.size(), "Unexpected number of filled inventory slots");

        // give() mints its own item entity via BlockCommands.giveBlock(); blockItem is only a maxStackSize reference.
        for (int i = 0; i < filledSlots.size(); i++) {
            int actualStackCount = Byte.toUnsignedInt(filledSlots.get(i).getComponent(ItemComponent.class).stackCount);
            if (i == amount / maxStackSize) {
                assertEquals(amount % maxStackSize, actualStackCount);
            } else {
                assertEquals(maxStackSize, actualStackCount);
            }
        }
    }
}
