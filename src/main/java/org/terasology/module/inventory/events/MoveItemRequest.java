// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;

import java.util.Collection;

/**
 */
@ServerEvent
public class MoveItemRequest extends AbstractMoveItemRequest {
    private int toSlot;

    protected MoveItemRequest() {
    }

    public MoveItemRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory,
            int toSlot, int changeId, Collection<EntityRef> clientSideTempEntities) {
        super(instigator, fromInventory, fromSlot, toInventory, changeId, clientSideTempEntities);
        this.toSlot = toSlot;
    }

    public int getToSlot() {
        return toSlot;
    }

}
