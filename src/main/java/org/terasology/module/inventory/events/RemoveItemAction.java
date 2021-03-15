// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Removed the specified item from the inventory of the entity it was sent to. If the remove was successful, the
 * event will be consumed.
 *
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class RemoveItemAction extends AbstractConsumableEvent {
    private EntityRef instigator;
    private List<EntityRef> items;
    private boolean destroyRemoved;
    private Integer count;

    private EntityRef removedItem;

    public RemoveItemAction(EntityRef instigator, EntityRef item, boolean destroyRemoved) {
        this(instigator, Arrays.asList(item), destroyRemoved);
    }

    public RemoveItemAction(EntityRef instigator, EntityRef item, boolean destroyRemoved, int count) {
        this(instigator, Arrays.asList(item), destroyRemoved, count);
    }

    public RemoveItemAction(EntityRef instigator, List<EntityRef> items, boolean destroyRemoved) {
        this.instigator = instigator;
        this.items = items;
        this.destroyRemoved = destroyRemoved;
    }

    public RemoveItemAction(EntityRef instigator, List<EntityRef> items, boolean destroyRemoved, int count) {
        this.instigator = instigator;
        this.items = items;
        this.destroyRemoved = destroyRemoved;
        this.count = count;
    }

    public List<EntityRef> getItems() {
        return items;
    }

    public Integer getCount() {
        return count;
    }

    public boolean isDestroyRemoved() {
        return destroyRemoved;
    }

    public void setRemovedItem(EntityRef removedItem) {
        this.removedItem = removedItem;
    }

    public EntityRef getRemovedItem() {
        return removedItem;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
