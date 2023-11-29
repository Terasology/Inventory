// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NoReplicate;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.Collection;

@ServerEvent
public abstract class AbstractMoveItemRequest implements Event {
    private EntityRef instigator;

    private EntityRef fromInventory = EntityRef.NULL;
    private int fromSlot;
    private EntityRef toInventory = EntityRef.NULL;

    private int changeId;

    @NoReplicate
    private Collection<EntityRef> clientSideTempEntities;

    protected AbstractMoveItemRequest() {
    }

    public AbstractMoveItemRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory, int changeId,
                                   Collection<EntityRef> clientSideTempEntities) {
        this.instigator = instigator;
        this.fromInventory = fromInventory;
        this.fromSlot = fromSlot;
        this.toInventory = toInventory;
        this.changeId = changeId;
        this.clientSideTempEntities = clientSideTempEntities;
    }

    public EntityRef getFromInventory() {
        return fromInventory;
    }

    public int getFromSlot() {
        return fromSlot;
    }

    public EntityRef getToInventory() {
        return toInventory;
    }

    public int getChangeId() {
        return changeId;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public Collection<EntityRef> getClientSideTempEntities() {
        return clientSideTempEntities;
    }

    public void setClientSideTempEntities(Collection<EntityRef> clientSideTempEntities) {
        this.clientSideTempEntities = clientSideTempEntities;
    }
}
