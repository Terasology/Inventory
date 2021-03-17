// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class MoveItemAction implements Event {
    private EntityRef instigator;
    private EntityRef to;
    private int slotFrom;
    private int slotTo;
    private int count;

    public MoveItemAction(EntityRef instigator, int slotFrom, EntityRef to, int slotTo, int count) {
        this.instigator = instigator;
        this.to = to;
        this.slotFrom = slotFrom;
        this.slotTo = slotTo;
        this.count = count;
    }

    public EntityRef getTo() {
        return to;
    }

    public int getSlotFrom() {
        return slotFrom;
    }

    public int getSlotTo() {
        return slotTo;
    }

    public int getCount() {
        return count;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
