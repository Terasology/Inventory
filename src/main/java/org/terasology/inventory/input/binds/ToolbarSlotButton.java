// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.inventory.input.binds;

import org.terasology.engine.input.BindButtonEvent;

/**
 *
 */
public class ToolbarSlotButton extends BindButtonEvent {
    private final int slot;

    public ToolbarSlotButton(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
