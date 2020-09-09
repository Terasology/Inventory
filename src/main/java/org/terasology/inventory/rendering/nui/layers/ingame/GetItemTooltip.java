// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inventory.rendering.nui.layers.ingame;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.nui.widgets.TooltipLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetItemTooltip implements Event {
    private final List<TooltipLine> tooltipLines;

    public GetItemTooltip() {
        tooltipLines = new ArrayList<>();
    }

    public GetItemTooltip(String defaultTooltip) {
        this.tooltipLines = new ArrayList<>(Arrays.asList(new TooltipLine(defaultTooltip)));
    }

    public List<TooltipLine> getTooltipLines() {
        return tooltipLines;
    }
}
