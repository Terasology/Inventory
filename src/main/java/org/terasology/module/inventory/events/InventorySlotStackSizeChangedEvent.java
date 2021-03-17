// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.event.Event;

/**
 */
public class InventorySlotStackSizeChangedEvent implements Event {
    private int slot;
    private int oldSize;
    private int newSize;

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
