// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic.action;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class MoveItemAction implements Event {
    private final EntityRef instigator;
    private final EntityRef to;
    private final int slotFrom;
    private final int slotTo;
    private final int count;

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
