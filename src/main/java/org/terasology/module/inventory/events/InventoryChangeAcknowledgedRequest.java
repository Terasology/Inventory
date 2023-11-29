// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.events;

import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.OwnerEvent;

/**
 * Message to acknowledge an inventory change request as complete, to be sent back to the requester so they can stop
 * predicting the change.
 *
 */
@OwnerEvent
public class InventoryChangeAcknowledgedRequest extends NetworkEvent {
    private int changeId;

    protected InventoryChangeAcknowledgedRequest() {
    }

    public InventoryChangeAcknowledgedRequest(int changeId) {
        this.changeId = changeId;
    }

    public int getChangeId() {
        return changeId;
    }
}
