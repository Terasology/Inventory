// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.events.InventorySlotChangedEvent;

/**
 * Diagnostic tools for chasing inventory save/load item-loss bugs. Originally in JoshariasSurvival, moved here
 * since it's Inventory's own data being inspected - see Terasology/JoshariasSurvival#77.
 */
@RegisterSystem
public class InventoryDiagnosticsSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(InventoryDiagnosticsSystem.class);

    @In
    private InventoryManager inventoryManager;

    /**
     * Diagnostic: at DEBUG level, trace every inventory slot change, everywhere - silent otherwise. Enable by
     * raising this class's log level (e.g. in logback.xml).
     */
    @ReceiveEvent
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        logger.debug("inventoryTrace: entity id={} slot={} old={} new={}",
                entity.getId(),
                event.getSlot(),
                describeItem(event.getOldItem()),
                describeItem(event.getNewItem()));
    }

    /**
     * Public so other modules' own diagnostics (e.g. Workstation) can format an item the same way.
     */
    public static String describeItem(EntityRef item) {
        if (!item.exists()) {
            return "none";
        }
        DisplayNameComponent displayNameComponent = item.getComponent(DisplayNameComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        String name = displayNameComponent != null ? displayNameComponent.name : "?";
        int stackCount = itemComponent != null ? itemComponent.stackCount : -1;
        return String.format("%s x%d id=%d persistent=%b", name, stackCount, item.getId(), item.isPersistent());
    }

    /**
     * Public so other modules' own diagnostics (e.g. Workstation) can dump an inventory the same way.
     *
     * @param entity any entity with an {@link org.terasology.module.inventory.components.InventoryComponent}
     */
    public static String dumpSlots(EntityRef entity, InventoryManager inventoryManager) {
        StringBuilder result = new StringBuilder();
        int numSlots = inventoryManager.getNumSlots(entity);
        for (int slot = 0; slot < numSlots; slot++) {
            EntityRef item = inventoryManager.getItemInSlot(entity, slot);
            if (!item.exists()) {
                continue;
            }
            result.append(String.format("slot %d: %s%n", slot, describeItem(item)));
        }
        return result.length() == 0 ? "Inventory empty" : result.toString();
    }

    @Command(shortDescription = "Diagnostic: dump inventory slot contents and their save-persistence flag",
            runOnServer = true)
    public String inventoryDump(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        return dumpSlots(character, inventoryManager);
    }
}
