// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic.events;

import org.terasology.engine.entitySystem.event.Event;

/**
 *
 */
public class InventorySlotStackSizeChangedEvent implements Event {
    private final int slot;
    private final int oldSize;
    private final int newSize;

    public InventorySlotStackSizeChangedEvent(int slot, int oldSize, int newSize) {
        this.slot = slot;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    public int getSlot() {
        return slot;
    }

    public int getOldSize() {
        return oldSize;
    }

    public int getNewSize() {
        return newSize;
    }
}
