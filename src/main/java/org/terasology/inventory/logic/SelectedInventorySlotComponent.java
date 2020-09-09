// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

public class SelectedInventorySlotComponent implements Component {
    @Replicate
    public int slot;
}
