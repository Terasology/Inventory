// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.events;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

@ServerEvent
public class ChangeSelectedInventorySlotRequest implements Event {

    private int slot;

    protected ChangeSelectedInventorySlotRequest() {
    }

    public ChangeSelectedInventorySlotRequest(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
