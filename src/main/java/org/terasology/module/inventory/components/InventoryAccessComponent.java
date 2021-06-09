// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.inventory.components;

import org.terasology.engine.math.IntegerRange;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class InventoryAccessComponent implements Component<InventoryAccessComponent> {
    public Map<String, IntegerRange> input;
    public Map<String, IntegerRange> output;

    @Override
    public void copy(InventoryAccessComponent other) {
        this.input = other.input;
        this.output = other.output;
    }
}
