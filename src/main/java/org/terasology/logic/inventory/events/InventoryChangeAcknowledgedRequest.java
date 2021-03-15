/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.inventory.events;

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
