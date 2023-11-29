// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.nui.databinding.ReadOnlyBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class InventoryScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryScreen.class);

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);
    }

    @Override
    public boolean isModal() {
        return false;
    }

    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    @Override
    public void onClosed() {
        /*
          The code below was originally taken from moveItemSmartly() in
          InventoryCell.class and slightly modified to work here.

          The way items are being moved to and from the hotbar is really
          similar to what was needed here to take them out of the transfer
          slot and sort them into the inventory.
        */
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;

        EntityRef fromEntity = movingItem;
        int fromSlot = 0;

        InventoryComponent playerInventory = playerEntity.getComponent(InventoryComponent.class);
        if (playerInventory == null) {
            return;
        }
        CharacterComponent characterComponent = playerEntity.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Character entity of player had no character component");
            return;
        }
        int totalSlotCount = playerInventory.itemSlots.size();

        EntityRef interactionTarget = characterComponent.predictedInteractionTarget;
        InventoryComponent interactionTargetInventory = interactionTarget.getComponent(InventoryComponent.class);


        EntityRef targetEntity;
        List<Integer> toSlots = new ArrayList<>(totalSlotCount);
        if (fromEntity.equals(playerEntity) && interactionTarget.exists() && interactionTargetInventory != null) {
            targetEntity = interactionTarget;
            toSlots = IntStream.range(0, interactionTargetInventory.itemSlots.size()).boxed().collect(Collectors.toList());
        } else {
            targetEntity = playerEntity;
            toSlots = IntStream.range(0, totalSlotCount).boxed().collect(Collectors.toList());
        }

        inventoryManager.moveItemToSlots(getTransferEntity(), fromEntity, fromSlot, targetEntity, toSlots);

        EntityRef item = InventoryUtils.getItemAt(movingItem, 0);
        int count = InventoryUtils.getStackCount(item);

        InventoryUtils.dropItems(item, count, localPlayer);
    }
}
