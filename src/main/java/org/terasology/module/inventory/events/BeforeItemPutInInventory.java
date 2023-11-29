// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

public class BeforeItemPutInInventory extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef item;
    private int slot;

    public BeforeItemPutInInventory(EntityRef instigator, EntityRef item, int slot) {
        this.instigator = instigator;
        this.item = item;
        this.slot = slot;
    }

    public EntityRef getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
