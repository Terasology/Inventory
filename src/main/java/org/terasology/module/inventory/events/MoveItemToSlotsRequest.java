// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;

import java.util.Collection;
import java.util.List;

/**
 * Represents the request to move the item smarly to to one or more of the specified slots.
 * Stacks will be filled up first before an empty slot will be used.
 *
 * Usually triggered by a shift click on an item.
 *
 */
@ServerEvent
public class MoveItemToSlotsRequest extends AbstractMoveItemRequest {

    private List<Integer> toSlots;

    protected MoveItemToSlotsRequest() {
    }

    public MoveItemToSlotsRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory,
            List<Integer> toSlots, int changeId, Collection<EntityRef> clientSideTempEntities) {
        super(instigator, fromInventory, fromSlot, toInventory, changeId, clientSideTempEntities);
        this.toSlots = toSlots;
    }

    public List<Integer> getToSlots() {
        return toSlots;
    }
}
