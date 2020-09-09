// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.rendering.nui.layers.ingame;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.nui.Canvas;

public class BeforeInventoryCellRendered implements Event {
    private final Canvas canvas;

    public BeforeInventoryCellRendered(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
