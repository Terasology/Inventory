// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 */
public class InventorySlotChangedEvent implements Event {
    private int slot;
    private EntityRef oldItem;
    private EntityRef newItem;

    public InventorySlotChangedEvent(int slot, EntityRef oldItem, EntityRef newItem) {
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public int getSlot() {
        return slot;
    }

    public EntityRef getOldItem() {
        return oldItem;
    }

    public EntityRef getNewItem() {
        return newItem;
    }
}
