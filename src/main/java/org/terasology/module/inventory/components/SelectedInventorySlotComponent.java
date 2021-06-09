// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.components;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class SelectedInventorySlotComponent implements Component<SelectedInventorySlotComponent> {
    @Replicate
    public int slot;

    @Override
    public void copy(SelectedInventorySlotComponent other) {
        this.slot = other.slot;
    }
}
