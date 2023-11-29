// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.module.inventory.components.InventoryItem;

import java.util.List;
/**
 * This event is sent to fill the inventory of a player with the requested items.
 */
public class RequestInventoryEvent implements Event {
    public List<InventoryItem> items;
    public RequestInventoryEvent(List<InventoryItem> items) {
        this.items = items;
    }
}
