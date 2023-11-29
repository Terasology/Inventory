// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

import java.util.Arrays;
import java.util.List;

/**
 * This action adds the item into the inventory of the entity it was sent to. The item is either completely consumed
 * or not modified at all. If it was consumed, the isConsumed() will return <code>true</code>.
 *
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class GiveItemAction extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef item;
    private List<Integer> slots;

    public GiveItemAction(EntityRef instigator, EntityRef item) {
        this.instigator = instigator;
        this.item = item;
    }

    public GiveItemAction(EntityRef instigator, EntityRef item, int slot) {
        this(instigator, item, Arrays.asList(slot));
    }

    public GiveItemAction(EntityRef instigator, EntityRef item, List<Integer> slots) {
        this.instigator = instigator;
        this.item = item;
        this.slots = slots;
    }

    public EntityRef getItem() {
        return item;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
