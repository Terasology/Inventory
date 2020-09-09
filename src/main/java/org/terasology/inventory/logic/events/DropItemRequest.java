// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.logic.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.math.geom.Vector3f;

/**
 * A request for a player to drop an item. Is replicated onto the server
 */
@ServerEvent(lagCompensate = true)
public class DropItemRequest implements Event {

    private EntityRef item = EntityRef.NULL;
    private EntityRef inventory = EntityRef.NULL;
    private Vector3f impulse;
    private Vector3f newPosition;
    private int count;

    protected DropItemRequest() {
    }

    public DropItemRequest(EntityRef usedItem, EntityRef inventoryEntity, Vector3f impulse, Vector3f newPosition,
                           int count) {
        this.item = usedItem;
        this.inventory = inventoryEntity;
        this.impulse = impulse;
        this.newPosition = newPosition;
        this.count = count;
    }

    public DropItemRequest(EntityRef usedItem, EntityRef inventoryEntity, Vector3f impulse, Vector3f newPosition) {
        this(usedItem, inventoryEntity, impulse, newPosition, 1);
    }

    public EntityRef getItem() {
        return item;
    }

    public EntityRef getInventoryEntity() {
        return inventory;
    }

    public Vector3f getNewPosition() {
        return newPosition;
    }

    public Vector3f getImpulse() {
        return impulse;
    }

    public int getCount() {
        return count;
    }
}
