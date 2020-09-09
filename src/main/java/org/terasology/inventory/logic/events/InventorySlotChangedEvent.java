// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 *
 */
public class InventorySlotChangedEvent implements Event {
    private final int slot;
    private final EntityRef oldItem;
    private final EntityRef newItem;

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
