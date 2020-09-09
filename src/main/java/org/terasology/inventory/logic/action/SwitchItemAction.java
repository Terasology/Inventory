// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic.action;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class SwitchItemAction extends AbstractConsumableEvent {
    private final EntityRef instigator;
    private final EntityRef to;
    private final int slotFrom;
    private final int slotTo;

    public SwitchItemAction(EntityRef instigator, int slotFrom, EntityRef to, int slotTo) {
        this.instigator = instigator;
        this.to = to;
        this.slotFrom = slotFrom;
        this.slotTo = slotTo;
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

    public EntityRef getInstigator() {
        return instigator;
    }
}
