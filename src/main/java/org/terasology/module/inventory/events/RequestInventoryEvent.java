// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.module.inventory.components.InventoryItemComponent;

import java.util.List;

public class RequestInventoryEvent implements Event {
    public List<InventoryItemComponent> items;
    public RequestInventoryEvent(List<InventoryItemComponent> items) {
        this.items = items;
    }
}
