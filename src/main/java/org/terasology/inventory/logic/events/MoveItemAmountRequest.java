// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.logic.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;

import java.util.Collection;

/**
 *
 */
@ServerEvent
public class MoveItemAmountRequest extends AbstractMoveItemRequest {
    private int toSlot;
    private int amount;

    protected MoveItemAmountRequest() {
    }

    public MoveItemAmountRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory,
                                 int toSlot, int amount, int changeId, Collection<EntityRef> clientSideTempEntities) {
        super(instigator, fromInventory, fromSlot, toInventory, changeId, clientSideTempEntities);
        this.toSlot = toSlot;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public int getToSlot() {
        return toSlot;
    }
}
