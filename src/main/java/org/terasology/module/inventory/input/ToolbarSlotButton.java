// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.inventory.input;

import org.terasology.engine.input.BindButtonEvent;

public class ToolbarSlotButton extends BindButtonEvent {
    private int slot;

    public ToolbarSlotButton(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
